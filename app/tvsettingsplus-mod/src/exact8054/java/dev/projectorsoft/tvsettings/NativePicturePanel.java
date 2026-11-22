package dev.projectorsoft.tvsettings;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Objects;

/** STOCK names and slider layout, with no PQ mutation or OEM reset callback. */
final class NativePicturePanel {
    enum Page { PICTURE, COLOR, MEMC }
    private static final String TAG = "TvSettingsPlusMOD";
    private static final String ROOT = "com.android.tv.settings";
    private static final String CONFIG = ROOT + ".partnercustomizer.tvsettingservice.TVSettingConfig";
    private static final String GAMMA = "picture_gamma";
    private static final String MEMC = "tv_picture_advance_video_mjc_effect";
    private static final Map<String, String> SLIDER_CONFIG = Map.of(
            "picture_brightness", "g_video__brightness", "picture_contrast", "g_video__contrast",
            "picture_saturation", "g_video__vid_sat", "picture_sharpness", "g_video__vid_shp",
            "picture_hue", "g_video__vid_hue", "picture_red_gain", "g_video__clr_gain_r",
            "picture_green_gain", "g_video__clr_gain_g", "picture_blue_gain", "g_video__clr_gain_b");
    private static final Set<String> COLOR = Set.of(PicturePanelState.TEMPERATURE,
            "picture_red_gain", "picture_green_gain", "picture_blue_gain");

    interface OptionsSource {
        String[][] options(String key) throws Exception;
    }

    private final Context context;
    private final OptionsSource options;
    private final Map<String, Preference> rows = new LinkedHashMap<>();
    private final ContentObserver listener = new ContentObserver(new Handler(Looper.getMainLooper())) {
        @Override public void onChange(boolean selfChange) { refresh(); }
    };
    private boolean listening;
    private Dialog slider;
    private Map<String, Integer> sliderSnapshot;
    private String sliderKey;
    private ProgressBar sliderBar;
    private TextView sliderValue;
    private int sliderMin, sliderMax, requestedValue, confirmations;
    private long confirmationDeadline;
    private boolean waitingForBackend;
    private final Handler confirmationHandler = new Handler(Looper.getMainLooper());
    private final Runnable confirmStep = this::confirmSliderStep;
    private final Runnable commitStep = this::flushSlider;
    private boolean localSliderPending;

    /** Mode must be OEM User (numeric ID 0, verified against the device's mode catalog). */
    private boolean canEdit(Map<String, Integer> values) {
        try {
            return PictureUserEditPolicy.allows(OemHdmiPortInfo.machineId(), values,
                    options.options(PicturePanelState.MODE));
        } catch (Exception | LinkageError unavailable) {
            Log.w(TAG, "OEM User picture mode not verified", unavailable);
            return false;
        }
    }

    /** RGB belongs to the user color-temperature profile, not the picture preset. */
    private boolean canEditScalar(String key, Map<String, Integer> values) {
        if (values == null || !"8054".equals(OemHdmiPortInfo.machineId())) return false;
        if (!COLOR.contains(key)) return canEdit(values);
        if (!Integer.valueOf(0).equals(values.get(PicturePanelState.TEMPERATURE))) return false;
        try {
            String[][] table = options.options(PicturePanelState.TEMPERATURE);
            if (!OemCustomerOptions.valid(table)) return false;
            for (String v : table[1]) if ("0".equals(v)) return true;
        } catch (Exception | LinkageError error) {
            Log.w(TAG, "Color-temperature User profile not verified", error);
        }
        return false;
    }

    /** Allow an expected change of the edited scalar, but no other picture keys. */
    static boolean otherKeysUnchanged(Map<String, Integer> before, Map<String, Integer> after, String edited) {
        if (before == null || after == null || edited == null
                || !before.keySet().equals(after.keySet())) return false;
        for (String key : before.keySet()) {
            if (!edited.equals(key) && !Objects.equals(before.get(key), after.get(key))) return false;
        }
        return true;
    }

    NativePicturePanel(NativeSections.Page host, PreferenceScreen screen, Page page) {
        this(host, screen, page, key -> stockOptions(host.requireContext(), key));
    }

    NativePicturePanel(NativeSections.Page host, PreferenceScreen screen, Page page, OptionsSource options) {
        this.options = options;
        context = host.requireContext();
        for (int i = 0; i < PicturePanelState.KEYS.length; i++) {
            String key = PicturePanelState.KEYS[i];
            if ((page == Page.PICTURE && !COLOR.contains(key))
                    || (page == Page.COLOR && COLOR.contains(key))
                    || (page == Page.MEMC && MEMC.equals(key))) {
                add(host, screen, key, PicturePanelState.TITLES[i],
                        page == Page.PICTURE ? pictureOrder(key) : 211 + i);
            }
        }
        if (page == Page.PICTURE) {
            add(host, screen, GAMMA, "Гамма", 126);
            Preference scope = new Preference(context);
            scope.setKey("projectorsoft_picture_video_pipeline_info");
            scope.setTitle("Регулировки видеосигнала");
            scope.setSummary("Яркость, контрастность, насыщенность, резкость, оттенок "
                    + "и гамма влияют на видео, но не на рабочий стол и интерфейс Android.");
            scope.setSelectable(false);
            scope.setOrder(112); // picture profile first, scalars after explanation
            screen.addPreference(scope);
        }
        refresh();
    }

    /** Frequently used signal-processing options first; gamma follows hue. */
    private static int pictureOrder(String key) {
        if (PicturePanelState.HDR.equals(key)) return 100;
        if (MEMC.equals(key)) return 101;
        if (PicturePanelState.MODE.equals(key)) return 110;
        if ("picture_brightness".equals(key)) return 120;
        if ("picture_contrast".equals(key)) return 121;
        if ("picture_saturation".equals(key)) return 122;
        if ("picture_sharpness".equals(key)) return 123;
        if ("picture_hue".equals(key)) return 125;
        return 150;
    }

    private void add(NativeSections.Page host, PreferenceScreen screen, String key, String title, int order) {
        Preference row;
        if (PicturePanelState.HDR.equals(key)) {
            row = new SwitchPreference(context);
        } else if (!SLIDER_CONFIG.containsKey(key)) {
            row = new ListPreference(context);
        } else {
            row = stockSliderRow(context);
            row.setOnPreferenceClickListener(item -> {
                showSlider(host, key, title);
                return true;
            });
        }
        row.setTitle(title);
        row.setOrder(order);
        row.setKey("projectorsoft_picture_read_" + key);
        row.setPersistent(false);
        if (row instanceof ListPreference) {
            ((ListPreference) row).setOnPreferenceChangeListener((pref, value) -> {
                if (value instanceof String) updateChoice(key, (String) value);
                return false; // refresh only after reading back OEM state
            });
        } else if (row instanceof SwitchPreference) {
            ((SwitchPreference) row).setOnPreferenceChangeListener((pref, value) -> {
                if (value instanceof Boolean) updateChoice(key, (Boolean) value ? "1" : "0");
                return false;
            });
        }
        screen.addPreference(row);
        rows.put(key, row);
    }

    private static Preference stockSliderRow(Context context) {
        // The original ProgressDialogPreference has its *own* onClick which can
        // intercept our custom popup. Use a plain preference with the exact
        // stock partner_progressbar_dialog.xml as the actual bottom editor.
        return new Preference(context);
    }

    /**
     * Real OEM consumer keys, not Factory PQ setters. Factory picture presets
     * protect scalar PQ controls AND gamma. HDR and MEMC remain independent.
     */
    private void updateChoice(String key, String selected) {
        Map<String, Integer> before = snapshot();
        Integer previous = before == null ? null :
                (GAMMA.equals(key) ? storedGamma() : before.get(key));
        if (!"8054".equals(OemHdmiPortInfo.machineId()) || previous == null || selected == null)
            return;
        if (GAMMA.equals(key) && !canEdit(before)) {
            // A disabled ListPreference alone is not a write barrier:
            // stale popup callbacks or programmatic changes must be blocked.
            Log.w(TAG, "Gamma write denied outside OEM User picture preset");
            refresh();
            return;
        }
        boolean toggle = PicturePanelState.HDR.equals(key);
        if (toggle && !("0".equals(selected) || "1".equals(selected))) return;
        if (!toggle) {
            String[][] choices;
            try { choices = options.options(key); }
            catch (Exception | LinkageError error) {
                Log.w(TAG, "Picture options unavailable for " + key, error);
                return;
            }
            if (!OemCustomerOptions.valid(choices)) return;
            boolean matched = false;
            for (String value : choices[1]) {
                if (selected.equals(value)) { matched = true; break; }
            }
            if (!matched) return;
        }
        // Gamma is User-only; HDR/MEMC and preset selection remain independent.
        // Color temperature remains independent of the picture preset.
        final int requested;
        try { requested = Integer.parseInt(selected); }
        catch (NumberFormatException invalid) { return; }
        if (requested == previous) return;
        if (!before.equals(snapshot())) return;
        try {
            if (!Settings.Global.putInt(context.getContentResolver(), key, requested)) {
                android.widget.Toast.makeText(context, "Настройка не применена",
                        android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            Log.i(TAG, "Picture OEM consumer change requested: " + key
                    + " " + previous + " -> " + requested);
            confirmationHandler.postDelayed(() -> {
                refresh();
                Integer result = GAMMA.equals(key) ? storedGamma()
                        : snapshot() == null ? null : snapshot().get(key);
                if (!Objects.equals(result, requested))
                    Log.w(TAG, "Picture OEM consumer readback not confirmed: " + key
                            + " requested=" + requested + " actual=" + result);
            }, 450);
        } catch (RuntimeException error) {
            Log.w(TAG, "Picture OEM consumer change failed: " + key, error);
            android.widget.Toast.makeText(context, "Не удалось изменить настройку",
                    android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    void start() {
        if (listening) return;
        listening = true;
        PicturePanelState.observe(this::observe, (key, error) -> Log.w(TAG, "PQ observer unavailable", error));
        try { observe(GAMMA); }
        catch (RuntimeException unavailable) { Log.w(TAG, "Gamma observer unavailable", unavailable); }
        refresh();
    }

    private void observe(String key) {
        context.getContentResolver().registerContentObserver(Settings.Global.getUriFor(key), false, listener);
    }

    void stop() {
        if (slider != null) slider.dismiss();
        if (!listening) return;
        listening = false;
        try { context.getContentResolver().unregisterContentObserver(listener); }
        catch (RuntimeException unavailable) { Log.w(TAG, "PQ observer teardown unavailable", unavailable); }
    }

    void refresh() {
        Map<String, Integer> snapshot = snapshot();
        // Own scalar changes are expected while the overlay is open. Only an
        // unrelated mode/input/HDR/temperature change invalidates the editor.
        if (slider != null && sliderSnapshot != null
                && !otherKeysUnchanged(sliderSnapshot, snapshot, sliderKey)) slider.dismiss();
        final boolean userModeVerified = canEdit(snapshot);
        for (Map.Entry<String, Preference> item : rows.entrySet()) {
            String key = item.getKey();
            Preference row = item.getValue();
            Integer value = snapshot == null ? null : snapshot.get(key);
            if (row instanceof ListPreference) {
                if (snapshot != null && GAMMA.equals(key))
                    value = storedGamma();
                renderList((ListPreference) row, key, value);
                if (GAMMA.equals(key) && !canEdit(snapshot)) {
                    ListPreference gamma = (ListPreference) row;
                    gamma.setEnabled(false);
                    CharSequence label = gamma.getEntry();
                    gamma.setSummary(label == null
                            ? "Доступна только в режиме «Пользовательский»"
                            : label.toString().replace("%", "%%")
                                + " · изменение только в режиме «Пользовательский»");
                }
            } else if (row instanceof SwitchPreference) {
                SwitchPreference hdr = (SwitchPreference) row;
                boolean known = value != null && (value == 0 || value == 1);
                hdr.setChecked(known && value == 1);
                hdr.setSummary(!known ? "Состояние недоступно"
                        : (value == 1 ? "Включён" : "Выключен"));
                hdr.setEnabled(known);
            } else {
                boolean editable = value != null && canEditScalar(key, snapshot);
                row.setSummary(value == null ? "Значение недоступно"
                        : String.valueOf(value));
                row.setEnabled(editable);
            }
        }
    }

    private Map<String, Integer> snapshot() {
        try { return PicturePanelState.read(key -> OemPictureState.readStored(context, key)); }
        catch (RuntimeException unavailable) { Log.w(TAG, "Stored picture state unavailable", unavailable); return null; }
    }

    private Integer storedGamma() {
        try { return Integer.valueOf(OemPictureState.readStored(context, GAMMA)); }
        catch (RuntimeException unavailable) { return null; }
    }

    private void renderList(ListPreference list, String key, Integer value) {
        String[][] table = null;
        try { table = options.options(key); }
        catch (Exception | LinkageError unavailable) { Log.d(TAG, "OEM picture labels unavailable for " + key, unavailable); }
        if (!OemCustomerOptions.valid(table)) {
            list.setEntries(new CharSequence[0]);
            list.setEntryValues(new CharSequence[0]);
            list.setValue(null);
            list.setSummary("Нет доступных вариантов");
            list.setEnabled(false);
            return;
        }
        list.setEntries(table[0]);
        list.setEntryValues(table[1]);
        String id = value == null ? null : value.toString();
        list.setValue(list.findIndexOfValue(id) >= 0 ? id : null);
        CharSequence label = list.getEntry();
        list.setSummary(label == null ? "Текущий режим недоступен"
                : label.toString().replace("%", "%%"));
        list.setEnabled(label != null);
    }

    private static String[][] stockOptions(Context context, String key) throws Exception {
        if (!"8054".equals(OemHdmiPortInfo.machineId())) return null;
        if (PicturePanelState.TEMPERATURE.equals(key)) return OemCustomerOptions.read(context, 0x7f0300ad, 0x7f0300ae);
        if (MEMC.equals(key)) return OemCustomerOptions.read(context, 0x7f030084, 0x7f030085);
        return OemCustomerOptions.forKey(context, key);
    }

    private void showSlider(NativeSections.Page host, String key, String title) {
        if (slider != null || !SLIDER_CONFIG.containsKey(key) || !"8054".equals(OemHdmiPortInfo.machineId())) return;
        try {
            Map<String, Integer> before = snapshot();
            Integer stored = before == null ? null : before.get(key);
            if (!canEditScalar(key, before)) {
                android.widget.Toast.makeText(context,
                        "Регулировка доступна только в пользовательском режиме",
                        android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            String configId = SLIDER_CONFIG.get(key);
            Class<?> type = Class.forName(CONFIG);
            Object cfg = type.getMethod("getInstance", Context.class).invoke(null, context);
            int min = ((Integer) type.getMethod("getMin", String.class).invoke(cfg, configId));
            int max = ((Integer) type.getMethod("getMax", String.class).invoke(cfg, configId));
            int actual = ((Integer) type.getMethod("getConfigValueInt", String.class).invoke(cfg, configId));
            if (stored == null || min >= max || stored < min || stored > max
                    || !before.equals(snapshot()) || !canEditScalar(key, snapshot())) {
                android.widget.Toast.makeText(context, "Текущее значение недоступно", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            View layout = LayoutInflater.from(context).inflate(0x7f0d0142, null, false);
            ProgressBar bar = layout.findViewById(0x7f0a013c);
            TextView name = layout.findViewById(0x7f0a013a);
            TextView value = layout.findViewById(0x7f0a013b);
            if (bar == null || name == null || value == null || host.getActivity() == null) return;
            bar.setMax(max);
            bar.setMin(min);
            bar.setProgress(stored);
            name.setText(title);
            value.setText(String.valueOf(stored));
            if (actual != stored)
                Log.w(TAG, "OEM PQ state not yet in sync for " + key
                        + ": stored=" + stored + " applied=" + actual);
            Dialog dialog = new Dialog(host.requireActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(layout);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnKeyListener((item, code, event) -> {
                if (code == KeyEvent.KEYCODE_DPAD_LEFT || code == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        int repeat = event.getRepeatCount();
                        int step = repeat > 15 ? 4 : repeat > 5 ? 2 : 1;
                        applySliderStep((code == KeyEvent.KEYCODE_DPAD_RIGHT ? 1 : -1) * step);
                    }
                    return true;
                }
                if (code == KeyEvent.KEYCODE_DPAD_CENTER) {
                    if (event.getAction() == KeyEvent.ACTION_UP) {
                        flushSlider();
                        dialog.dismiss();
                    }
                    return true;
                }
                return false;
            });
            View menu = host.requireActivity().findViewById(android.R.id.content);
            int visible = menu.getVisibility();
            dialog.setOnDismissListener(item -> {
                confirmationHandler.removeCallbacks(commitStep);
                confirmationHandler.removeCallbacks(confirmStep);
                flushSlider();
                menu.setVisibility(visible);
                slider = null;
                sliderSnapshot = null;
                sliderKey = null;
                sliderBar = null;
                sliderValue = null;
                waitingForBackend = false;
                refresh();
            });
            Window window = dialog.getWindow();
            if (window == null) return;
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            // Use the same compact bottom editor for video PQ and RGB.
            // MATCH_PARENT stretched the main Picture editor edge-to-edge,
            // unlike the original compact color-temperature control.
            window.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            dialog.show();
            int displayWidth = host.requireActivity().getResources()
                    .getDisplayMetrics().widthPixels;
            int dp = Math.round(host.requireActivity().getResources()
                    .getDisplayMetrics().density * 28f);
            int compactWidth = Math.min(displayWidth - 2 * dp,
                    Math.round(displayWidth * 0.66f));
            compactWidth = Math.max(1, compactWidth);
            window.setLayout(compactWidth, WindowManager.LayoutParams.WRAP_CONTENT);
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            params.y = dp; // inset above the bottom edge, not attached to it
            params.dimAmount = 0f;
            window.setAttributes(params);
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            sliderSnapshot = before;
            sliderKey = key;
            sliderBar = bar;
            sliderValue = value;
            sliderMin = min;
            sliderMax = max;
            waitingForBackend = false;
            localSliderPending = false;
            requestedValue = stored;
            slider = dialog;
            menu.setVisibility(View.INVISIBLE);
        } catch (Exception | LinkageError unavailable) {
            Log.w(TAG, "OEM picture slider unavailable", unavailable);
            if (slider != null) slider.dismiss();
            android.widget.Toast.makeText(context, "Регулятор недоступен", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Same system-provider route as the original OEM slider. This is an opt-in
     * candidate for device testing, NOT evidence of hardware/profile correctness.
     * No KuSettings Activity or Chinese UI controller is called.
     */
    /**
     * D-pad handling is immediate: repeat events update the visible value.
     * Write requests are coalesced to avoid waiting 250-500 ms for every press.
     * The OEM backend is verified in a separate readback loop.
     */
    private void applySliderStep(int direction) {
        if (slider == null || sliderKey == null || direction == 0) return;
        Map<String, Integer> state = snapshot();
        if (!otherKeysUnchanged(sliderSnapshot, state, sliderKey)
                || !canEditScalar(sliderKey, state)) {
            stopUnconfirmed("Контекст регулировки изменился");
            return;
        }
        int desired = Math.max(sliderMin, Math.min(sliderMax, requestedValue + direction));
        if (desired == requestedValue) return;
        requestedValue = desired;
        sliderBar.setProgress(desired);
        sliderValue.setText(String.valueOf(desired));
        localSliderPending = true;
        confirmationHandler.removeCallbacks(commitStep);
        confirmationHandler.postDelayed(commitStep, 70);
    }

    /** Flush the latest requested value; never overwrite another PQ preset. */
    private void flushSlider() {
        if (slider == null || sliderKey == null || !localSliderPending) return;
        confirmationHandler.removeCallbacks(commitStep);
        try {
            Map<String, Integer> state = snapshot();
            if (!otherKeysUnchanged(sliderSnapshot, state, sliderKey)
                    || !canEditScalar(sliderKey, state))
                throw new IllegalStateException("Picture context changed during edit");
            Integer original = sliderSnapshot.get(sliderKey);
            if (original == null || requestedValue < sliderMin || requestedValue > sliderMax)
                throw new IllegalStateException("Slider outside OEM bounds");
            if (!saveOriginal(sliderSnapshot, original))
                throw new IllegalStateException("Cannot journal original value");
            int desired = requestedValue;
            if (!Settings.Global.putInt(context.getContentResolver(), sliderKey, desired))
                throw new IllegalStateException("Settings.Global rejected scalar");
            localSliderPending = false;
            waitingForBackend = true;
            confirmations = 0;
            confirmationDeadline = SystemClock.uptimeMillis() + 8000;
            confirmationHandler.removeCallbacks(confirmStep);
            confirmationHandler.postDelayed(confirmStep, 200);
        } catch (Exception | LinkageError error) {
            Log.w(TAG, "Cannot apply picture scalar " + sliderKey, error);
            localSliderPending = false;
            stopUnconfirmed("Регулировка не применена");
        }
    }

    private int readMediaTekValue(String key) throws Exception {
        Class<?> type = Class.forName(CONFIG);
        Object config = type.getMethod("getInstance", Context.class).invoke(null, context);
        return (Integer) type.getMethod("getConfigValueInt", String.class)
                .invoke(config, SLIDER_CONFIG.get(key));
    }

    private boolean saveOriginal(Map<String, Integer> before, int original) {
        Integer mode = before.get(PicturePanelState.MODE);
        Integer temp = before.get(PicturePanelState.TEMPERATURE);
        Integer hdr = before.get(PicturePanelState.HDR);
        if (mode == null || temp == null || hdr == null) return false;
        // Audit record only: active HDMI is not authenticated; never auto-restore.
        String recordKey = sliderKey + "/mode=" + mode + "/temp=" + temp + "/hdr=" + hdr;
        SharedPreferences journal = context.getSharedPreferences(
                "projectorsoft_picture_candidate_originals", Context.MODE_PRIVATE);
        return journal.contains(recordKey)
                || journal.edit().putInt(recordKey, original).commit();
    }

    private void confirmSliderStep() {
        if (slider == null || !waitingForBackend || sliderKey == null) return;
        try {
            Map<String, Integer> now = snapshot();
            if (!otherKeysUnchanged(sliderSnapshot, now, sliderKey) || !canEditScalar(sliderKey, now))
                throw new IllegalStateException("Picture context changed");
            // A new D-pad step can be queued while the previous write is still
            // reaching TvServices. Defer completion until the last value settles.
            if (localSliderPending) {
                confirmationHandler.postDelayed(confirmStep, 200);
                return;
            }
            boolean confirmed = Objects.equals(now.get(sliderKey), requestedValue)
                    && readMediaTekValue(sliderKey) == requestedValue;
            confirmations = confirmed ? confirmations + 1 : 0;
            if (confirmations >= 2) {
                Log.i(TAG, "PQ user scalar accepted by MediaTek config: "
                        + sliderKey + "=" + requestedValue);
                waitingForBackend = false;
                sliderSnapshot = now;
                sliderBar.setProgress(requestedValue);
                sliderValue.setText(String.valueOf(requestedValue));
                refresh();
                return;
            }
            if (SystemClock.uptimeMillis() >= confirmationDeadline) {
                stopUnconfirmed("Не подтверждено службой MediaTek; проверьте значение в штатном меню");
                return;
            }
            confirmationHandler.postDelayed(confirmStep, 250);
        } catch (Exception | LinkageError failure) {
            Log.w(TAG, "Picture readback failed for " + sliderKey, failure);
            stopUnconfirmed("Не удалось подтвердить значение; проверьте штатные настройки");
        }
    }

    private void stopUnconfirmed(String message) {
        Log.w(TAG, "PQ user slider: " + message + " key=" + sliderKey
                + " requested=" + requestedValue);
        confirmationHandler.removeCallbacks(confirmStep);
        waitingForBackend = false;
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show();
        if (slider != null) slider.dismiss();
    }
}

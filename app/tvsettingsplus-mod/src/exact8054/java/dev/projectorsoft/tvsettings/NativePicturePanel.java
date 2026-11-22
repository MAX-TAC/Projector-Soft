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
            if ((page == Page.PICTURE && !COLOR.contains(key) && !MEMC.equals(key))
                    || (page == Page.COLOR && COLOR.contains(key))
                    || (page == Page.MEMC && MEMC.equals(key))) {
                add(host, screen, key, PicturePanelState.TITLES[i], 211 + i);
            }
        }
        if (page == Page.PICTURE) add(host, screen, GAMMA, "Гамма", 224);
        refresh();
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
        if (!SLIDER_CONFIG.containsKey(key)) row.setEnabled(false);
        screen.addPreference(row);
        rows.put(key, row);
    }

    private static Preference stockSliderRow(Context context) {
        try {
            return (Preference) Class.forName(ROOT + ".partnercustomizer.utils.ProgressDialogPreference")
                    .getConstructor(Context.class).newInstance(context);
        } catch (ReflectiveOperationException | LinkageError unavailable) {
            return new Preference(context);
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
        if (slider != null && sliderSnapshot != null
                && (!otherKeysUnchanged(sliderSnapshot, snapshot, sliderKey)
                    || (!waitingForBackend && !sliderSnapshot.equals(snapshot)))) slider.dismiss();
        final boolean userModeVerified = canEdit(snapshot);
        for (Map.Entry<String, Preference> item : rows.entrySet()) {
            String key = item.getKey();
            Preference row = item.getValue();
            Integer value = snapshot == null ? null : snapshot.get(key);
            if (row instanceof ListPreference) {
                if (snapshot != null && GAMMA.equals(key))
                    value = storedGamma();
                renderList((ListPreference) row, key, value);
            } else if (row instanceof SwitchPreference) {
                SwitchPreference hdr = (SwitchPreference) row;
                boolean known = value != null && (value == 0 || value == 1);
                hdr.setChecked(known && value == 1);
                hdr.setSummary(!known ? "Состояние недоступно"
                        : (value == 1 ? "Включён" : "Выключен") + " · только просмотр");
            } else {
                boolean editable = value != null && userModeVerified;
                row.setSummary(value == null ? "Значение недоступно"
                        : value + (editable ? " · пользовательский режим (тест)" : " · только просмотр"));
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
            list.setSummary("Штатные варианты недоступны");
            return;
        }
        list.setEntries(table[0]);
        list.setEntryValues(table[1]);
        String id = value == null ? null : value.toString();
        list.setValue(list.findIndexOfValue(id) >= 0 ? id : null);
        CharSequence label = list.getEntry();
        list.setSummary(label == null ? "Текущий режим недоступен"
                : label.toString().replace("%", "%%") + " · только просмотр");
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
            if (!canEdit(before)) {
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
            if (stored == null || stored != actual || min >= max || actual < min || actual > max
                    || !before.equals(snapshot()) || !canEdit(snapshot())) {
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
            bar.setProgress(actual);
            name.setText(title + " · тестовый обработчик OEM");
            value.setText(String.valueOf(actual));
            Dialog dialog = new Dialog(host.requireActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(layout);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnKeyListener((item, code, event) -> {
                if (code == KeyEvent.KEYCODE_DPAD_LEFT || code == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0)
                        applySliderStep(code == KeyEvent.KEYCODE_DPAD_RIGHT ? 1 : -1);
                    return true;
                }
                return code == KeyEvent.KEYCODE_DPAD_CENTER;
            });
            View menu = host.requireActivity().findViewById(android.R.id.content);
            int visible = menu.getVisibility();
            dialog.setOnDismissListener(item -> {
                confirmationHandler.removeCallbacks(confirmStep);
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
            window.setGravity(Gravity.BOTTOM);
            dialog.show();
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            sliderSnapshot = before;
            sliderKey = key;
            sliderBar = bar;
            sliderValue = value;
            sliderMin = min;
            sliderMax = max;
            waitingForBackend = false;
            slider = dialog;
            menu.setVisibility(View.INVISIBLE);
        } catch (Exception | LinkageError unavailable) {
            Log.w(TAG, "Read-only OEM picture slider unavailable", unavailable);
            if (slider != null) slider.dismiss();
            android.widget.Toast.makeText(context, "Просмотр недоступен", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Same system-provider route as the original OEM slider. This is an opt-in
     * candidate for device testing, NOT evidence of hardware/profile correctness.
     * No KuSettings Activity or Chinese UI controller is called.
     */
    private void applySliderStep(int direction) {
        if (slider == null || sliderKey == null || waitingForBackend || direction == 0) return;
        try {
            if (!"8054".equals(OemHdmiPortInfo.machineId())) throw new IllegalStateException("Wrong board");
            Map<String, Integer> before = snapshot();
            if (before == null || !before.equals(sliderSnapshot) || !canEdit(before))
                throw new IllegalStateException("Picture context changed");
            Integer original = before.get(sliderKey);
            if (original == null || original < sliderMin || original > sliderMax
                    || readMediaTekValue(sliderKey) != original)
                throw new IllegalStateException("OEM/global values disagree");
            // Candidate step of one, not yet calibrated against stock D-pad increment.
            int desired = original + direction;
            if (desired < sliderMin || desired > sliderMax) return;
            if (!saveOriginal(before, original))
                throw new IllegalStateException("Original value could not be saved");
            Map<String, Integer> atWrite = snapshot();
            if (!before.equals(atWrite) || !canEdit(atWrite))
                throw new IllegalStateException("Picture User context changed before write");
            if (!Settings.Global.putInt(context.getContentResolver(), sliderKey, desired))
                throw new IllegalStateException("Settings.Global rejected the value");
            requestedValue = desired;
            confirmations = 0;
            waitingForBackend = true;
            confirmationDeadline = SystemClock.uptimeMillis() + 5000;
            confirmationHandler.postDelayed(confirmStep, 250);
        } catch (Exception | LinkageError failure) {
            Log.w(TAG, "Picture edit refused for " + sliderKey, failure);
            stopUnconfirmed("Изменение не выполнено или контекст изменился");
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
            if (!otherKeysUnchanged(sliderSnapshot, now, sliderKey) || !canEdit(now))
                throw new IllegalStateException("Picture context changed");
            boolean confirmed = Objects.equals(now.get(sliderKey), requestedValue)
                    && readMediaTekValue(sliderKey) == requestedValue;
            confirmations = confirmed ? confirmations + 1 : 0;
            if (confirmations >= 2) {
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
        confirmationHandler.removeCallbacks(confirmStep);
        waitingForBackend = false;
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show();
        if (slider != null) slider.dismiss();
    }
}

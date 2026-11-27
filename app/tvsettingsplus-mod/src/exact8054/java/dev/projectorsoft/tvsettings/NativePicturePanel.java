package dev.projectorsoft.tvsettings;

import android.app.Dialog;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
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
                add(host, screen, key, PicturePanelState.TITLES[i], page == Page.PICTURE ? pictureOrder(key) : 211 + i);
            }
        }
        if (page == Page.PICTURE) add(host, screen, GAMMA, "Гамма", 126);
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
            row = new Preference(context);
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

    static boolean stableSnapshot(Map<String, Integer> before, Map<String, Integer> after) {
        return before != null && !before.isEmpty() && before.equals(after);
    }

    void refresh() {
        Map<String, Integer> snapshot = snapshot();
        if (slider != null && !stableSnapshot(sliderSnapshot, snapshot)) slider.dismiss();
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
                row.setSummary(value == null ? "Значение недоступно" : value + " · только просмотр");
                row.setEnabled(value != null && "8054".equals(OemHdmiPortInfo.machineId()));
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

    static int compactSliderWidth(int displayWidth, int bottomInsetPx) {
        return Math.max(1, Math.min(displayWidth - 2 * bottomInsetPx,
                Math.round(displayWidth * 0.66f)));
    }

    private void showSlider(NativeSections.Page host, String key, String title) {
        if (slider != null || !SLIDER_CONFIG.containsKey(key) || !"8054".equals(OemHdmiPortInfo.machineId())) return;
        try {
            Map<String, Integer> before = snapshot();
            Integer stored = before == null ? null : before.get(key);
            String configId = SLIDER_CONFIG.get(key);
            Class<?> type = Class.forName(CONFIG);
            Object cfg = type.getMethod("getInstance", Context.class).invoke(null, context);
            int min = ((Integer) type.getMethod("getMin", String.class).invoke(cfg, configId));
            int max = ((Integer) type.getMethod("getMax", String.class).invoke(cfg, configId));
            int actual = ((Integer) type.getMethod("getConfigValueInt", String.class).invoke(cfg, configId));
            if (stored == null || stored != actual || min >= max || actual < min || actual > max
                    || !stableSnapshot(before, snapshot())) {
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
            name.setText(title + " · только просмотр");
            value.setText(String.valueOf(actual));
            Dialog dialog = new Dialog(host.requireActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(layout);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnKeyListener((item, code, event) -> code == KeyEvent.KEYCODE_DPAD_LEFT
                    || code == KeyEvent.KEYCODE_DPAD_RIGHT || code == KeyEvent.KEYCODE_DPAD_CENTER);
            View menu = host.requireActivity().findViewById(android.R.id.content);
            int visible = menu.getVisibility();
            dialog.setOnDismissListener(item -> {
                menu.setVisibility(visible);
                slider = null;
                sliderSnapshot = null;
                refresh();
            });
            Window window = dialog.getWindow();
            if (window == null) return;
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            dialog.show();
            android.util.DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            int inset = Math.round(metrics.density * 28f);
            window.setLayout(compactSliderWidth(metrics.widthPixels, inset),
                    WindowManager.LayoutParams.WRAP_CONTENT);
            WindowManager.LayoutParams params = window.getAttributes();
            params.y = inset;
            window.setAttributes(params);
            sliderSnapshot = before;
            slider = dialog;
            menu.setVisibility(View.INVISIBLE);
        } catch (Exception | LinkageError unavailable) {
            Log.w(TAG, "Read-only OEM picture slider unavailable", unavailable);
            if (slider != null) slider.dismiss();
            android.widget.Toast.makeText(context, "Просмотр недоступен", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}

package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.SwitchPreference;


/** Retains controls from the owner-tested 2.28 build. */
final class ProjectorExtras {
    private static final String TAG = "TvSettingsPlusMOD";
    static final String GAME_MODE = "g_video__vid_game_mode";
    static final String GAME_MODE_MIRROR = "tv_picture_video_game_mode";
    static final String ALLM = "g_video__vid_allm_switch";
    static final String ALLM_MIRROR = "tv_picture_video_allm";
    static final String AISR = "tv_picture_video_aisr";
    static final String LIVE_TONE = "tv_picture_video_live_tone";
    private static final String KEY_GAME = "projectorsoft_extras_game_mode";
    private static final String KEY_ALLM = "projectorsoft_extras_allm";
    private static final String KEY_AISR = "projectorsoft_extras_aisr";
    private static final String KEY_LIVE = "projectorsoft_extras_live_tone";
    private ProjectorExtras() {}
    static void installSources(Context context, PreferenceGroup parent) {
        installArc(context, parent, 201);
        addSwitch(context, parent, KEY_GAME,
                "Игровой режим (низкая задержка)",
                "Режим изображения с уменьшенной задержкой.",
                250, GAME_MODE, GAME_MODE_MIRROR);
        addSwitch(context, parent, KEY_ALLM,
                "ALLM (авто-игровой режим)",
                "Источник сам просит игровой режим. Требует совместимую консоль.",
                251, ALLM, ALLM_MIRROR);
    }

    // ---------------- Picture ----------------

    /** AISR and Live Tone live on the picture screen, under video processing. */
    static void installPicture(Context context, PreferenceGroup parent) {
        addSwitch(context, parent, KEY_AISR,
                "AISR (улучшение детализации)",
                "Обработка детализации видеопотока.",
                226, AISR, null);
        addSwitch(context, parent, KEY_LIVE,
                "Live Tone",
                "Тональная обработка видеопотока.",
                227, LIVE_TONE, null);
    }

    static void installKeystoneReset(Context context, PreferenceGroup parent) {
        Preference row = new Preference(context);
        row.setKey("projectorsoft_keystone_reset_all");
        row.setTitle("Сбросить все коррекции трапеции");
        row.setSummary("Четыре угла и автокоррекция при включении и перемещении.");
        row.setOrder(300);
        row.setOnPreferenceClickListener(item -> {
            new android.app.AlertDialog.Builder(context)
                    .setTitle("Сбросить коррекцию?")
                    .setMessage("Четыре угла будут сброшены. Автокоррекция при включении и перемещении будет выключена.")
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton("Сбросить", (dialog, which) -> {
                        KeystoneResetState.Result result = resetKeystone(context);
                        String message = result == KeystoneResetState.Result.COMPLETE
                                ? "Коррекции трапеции сброшены"
                                : result == KeystoneResetState.Result.PARTIAL
                                ? "Сброс выполнен частично. Проверьте геометрию изображения."
                                : "Не удалось сбросить коррекцию";
                        feedback(context, message);
                        Log.i(TAG, "keystone reset result=" + result);
                    }).show();
            return true;
        });
        parent.addPreference(row);
    }

    private static KeystoneResetState.Result resetKeystone(Context context) {
        return KeystoneResetState.reset(new KeystoneResetState.Backend() {
            @Override public boolean resetDisplay() { return resetKeystoneNative(context); }
            @Override public boolean clear(String property) {
                try {
                    Class<?> props = Class.forName("android.os.SystemProperties");
                    java.lang.reflect.Method set = props.getMethod("set", String.class, String.class);
                    java.lang.reflect.Method get = props.getMethod("get", String.class, String.class);
                    return writeAndVerify(set, get, property);
                } catch (ReflectiveOperationException | RuntimeException refused) {
                    Log.w(TAG, "Keystone reset property failed: " + property, refused);
                    return false;
                }
            }
        });
    }

    private static boolean resetKeystoneNative(Context context) {
        String[] candidates = {
                "/system/priv-app/KeystoneCorrection/KeystoneCorrection.apk",
                "/system/app/KeystoneCorrection/KeystoneCorrection.apk",
                "/system/priv-app/TvCorrection/TvCorrection.apk"
        };
        for (String path : candidates) {
            if (!new java.io.File(path).exists()) continue;
            try {
                ClassLoader loader = new dalvik.system.DexClassLoader(
                        path, context.getCodeCacheDir().getAbsolutePath(), null,
                        ProjectorExtras.class.getClassLoader());
                Class<?> util = loader.loadClass("com.rjpj.tcorrection.KeystoneUtil");
                Object instance = util.getMethod("getInstance").invoke(null);
                if (instance == null) continue;
                util.getMethod("resetKeystone").invoke(instance);
                return true;
            } catch (Throwable error) {
                Log.w(TAG, "native keystone reset failed via " + path, error);
            }
        }
        return false;
    }
    private static boolean writeAndVerify(java.lang.reflect.Method set,
                                          java.lang.reflect.Method get,
                                          String key) {
        try {
            set.invoke(null, key, "0");
            Object after = get.invoke(null, key, "");
            String value = after == null ? "" : after.toString().trim();
            boolean ok = value.equals("0") || value.equals("0.0");
            if (!ok) Log.w(TAG, "keystone key did not take: " + key + " -> " + value);
            return ok;
        } catch (Throwable error) {
            Log.w(TAG, "keystone write failed: " + key, error);
            return false;
        }
    }
    // ---------------- HDMI EDID ----------------

    static final String EDID_KEY = "tv_input_hdmi_edid_version";
    // Index matches the OEM value 0..3 seen in the system settings.
    private static final String[] EDID_NAMES = {"HDMI 1.4", "HDMI 2.0", "Автоматически", "HDMI 2.1"};

    /**
     * HDMI compatibility (EDID) selector as a real control.
     *
     * The OEM hook only ever rendered a read-only status on the 8054 ("change
     * unavailable until both HDMI are checked"). The underlying key is a plain
     * global with four documented values, so it is offered as a list here.
     */
    static void installEdid(Context context, PreferenceGroup parent, int order) {
        if (parent == null) return;
        ListPreference list = new ListPreference(context);
        list.setKey("projectorsoft_edid_version");
        list.setTitle("Совместимость HDMI (EDID)");
        list.setOrder(order);
        list.setEntries(EDID_NAMES);
        String[] values = new String[EDID_NAMES.length];
        for (int i = 0; i < values.length; i++) values[i] = String.valueOf(i);
        list.setEntryValues(values);
        list.setPersistent(false);
        renderEdid(context, list);
        list.setOnPreferenceChangeListener((pref, value) -> {
            int index;
            try { index = Integer.parseInt(String.valueOf(value)); }
            catch (NumberFormatException bad) { return false; }
            if (index < 0 || index >= EDID_NAMES.length) return false;
            try {
                int actual = Settings.Global.getInt(context.getContentResolver(), EDID_KEY, -1);
                String before = actual >= 0 && actual < EDID_NAMES.length ? String.valueOf(actual) : null;
                if (!java.util.Objects.equals(list.getValue(), before)) {
                    feedback(context, "Значение изменилось. Выберите режим ещё раз.");
                } else if (!Settings.Global.putInt(context.getContentResolver(), EDID_KEY, index)
                        || Settings.Global.getInt(context.getContentResolver(), EDID_KEY, -1) != index) {
                    feedback(context, "Не удалось сохранить совместимость HDMI");
                }
            } catch (RuntimeException refused) {
                Log.w(TAG, "EDID write failed", refused);
                feedback(context, "Не удалось сохранить совместимость HDMI");
            }
            renderEdid(context, list);
            return false;
        });
        parent.addPreference(list);
    }
    private static void renderEdid(Context context, ListPreference list) {
        int saved = Settings.Global.getInt(context.getContentResolver(), EDID_KEY, -1);
        boolean known = saved >= 0 && saved < EDID_NAMES.length;
        list.setValue(known ? String.valueOf(saved) : null);
        list.setSummary(known ? EDID_NAMES[saved] : "Текущий режим не определён");
    }

    // ---------------- HDMI ARC ----------------

    static final String ARC_ENABLED = "hdmi_arc_control_enabled";

    /**
     * HDMI ARC on/off. Verified key on the 8054: Settings.Global
     * hdmi_arc_control_enabled (currently 1). eARC has no equivalent switch:
     * only informational g_audio__earc_info_* fields exist, so no eARC toggle
     * is offered rather than inventing one.
     */
    static void installArc(Context context, PreferenceGroup parent, int order) {
        if (parent == null) return;
        SwitchPreference arc = new SwitchPreference(context);
        arc.setKey("projectorsoft_arc_enabled");
        arc.setTitle("HDMI ARC");
        arc.setSummary("Возврат звука на ресивер по тому же HDMI-кабелю.");
        arc.setOrder(order);
        arc.setPersistent(false);
        arc.setChecked(Settings.Global.getInt(context.getContentResolver(), ARC_ENABLED, 1) == 1);
        arc.setOnPreferenceChangeListener((pref, value) -> {
            boolean on = value instanceof Boolean && (Boolean) value;
            boolean accepted = Settings.Global.putInt(context.getContentResolver(), ARC_ENABLED, on ? 1 : 0);
            int read = Settings.Global.getInt(context.getContentResolver(), ARC_ENABLED, -1);
            ((SwitchPreference) pref).setChecked(read == 1);
            if (!accepted || read != (on ? 1 : 0)) feedback(context, "Не удалось сохранить HDMI ARC");
            return false;
        });
        parent.addPreference(arc);
    }
    /** Toggle backed by one or two Settings.Global keys, with read-back check. */
    private static void addSwitch(Context context, PreferenceGroup parent, String key,
                                  String title, String summary, int order,
                                  String primary, String mirror) {
        if (parent == null) return;
        SwitchPreference row = new SwitchPreference(context);
        row.setKey(key);
        row.setTitle(title);
        row.setSummary(summary);
        row.setOrder(order);
        row.setPersistent(false);
        row.setChecked(Settings.Global.getInt(context.getContentResolver(), primary, 0) == 1);
        row.setOnPreferenceChangeListener((pref, value) -> {
            boolean on = value instanceof Boolean && (Boolean) value;
            int numeric = on ? 1 : 0;
            boolean ok = Settings.Global.putInt(context.getContentResolver(), primary, numeric);
            if (mirror != null) {
                ok &= Settings.Global.putInt(context.getContentResolver(), mirror, numeric);
            }
            int readBack = Settings.Global.getInt(context.getContentResolver(),
                    primary, Integer.MIN_VALUE);
            boolean matched = readBack == numeric && (mirror == null
                    || Settings.Global.getInt(context.getContentResolver(), mirror, Integer.MIN_VALUE) == numeric);
            if (!ok || !matched) feedback(context, "Настройка сохранена не полностью. Проверьте значение.");
            Log.i(TAG, key + "=" + numeric + " put=" + ok + " read=" + readBack);
            // Android reverts the switch when the listener returns false, so the
            // row must be set from the value we just read back. Without this the
            // toggle only appears to change after leaving and re-entering the page.
            if (pref instanceof SwitchPreference) {
                ((SwitchPreference) pref).setChecked(readBack == 1);
            }
            return false;
        });
        parent.addPreference(row);
    }

    private static void feedback(Context context, String message) {
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show();
    }

    static void refresh(Context context, androidx.preference.PreferenceScreen screen) {
        String[][] settings = {
            {"projectorsoft_arc_enabled", ARC_ENABLED},
            {KEY_GAME, GAME_MODE}, {KEY_ALLM, ALLM}, {KEY_AISR, AISR}, {KEY_LIVE, LIVE_TONE}
        };
        for (String[] setting : settings) {
            Preference row = screen.findPreference(setting[0]);
            if (row instanceof SwitchPreference)
                ((SwitchPreference) row).setChecked(Settings.Global.getInt(
                        context.getContentResolver(), setting[1], ARC_ENABLED.equals(setting[1]) ? 1 : 0) == 1);
        }
        Preference edid = screen.findPreference("projectorsoft_edid_version");
        if (edid instanceof ListPreference) renderEdid(context, (ListPreference) edid);
    }
}

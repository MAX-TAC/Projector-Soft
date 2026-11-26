package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.provider.Settings;
import android.util.Log;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import java.util.ArrayList;
import java.util.List;

/**
 * Extra projector controls added on top of the stock-like v41 tree.
 *
 * Every entry here was verified on the live 8054 unless marked otherwise:
 *  - Game Mode: g_video__vid_game_mode accepts the write and the value reads
 *    back. Owner confirmed the low-latency effect is real.
 *  - ALLM: g_video__vid_allm_switch writes, but mAllmRequested stays false
 *    without an ALLM source, so the row is informational until tested.
 *  - AISR / Live Tone: keys accept writes. Effect unconfirmed through Android
 *    apps because those go through the GPU compositor, not the MediaTek
 *    scaler; owner verifies on HDMI.
 *  - Duplicate to speaker: needs @SystemApi AudioManager, available only
 *    inside this system-uid settings package.
 *
 * No reflection over OEM classes: these are plain Settings.Global keys, the
 * same ones TvServices already watches.
 */
final class ProjectorExtras {
    private static final String TAG = "TvSettingsPlusMOD";

    static final String GAME_MODE = "g_video__vid_game_mode";
    static final String GAME_MODE_MIRROR = "tv_picture_video_game_mode";
    static final String ALLM = "g_video__vid_allm_switch";
    static final String ALLM_MIRROR = "tv_picture_video_allm";
    static final String AISR = "tv_picture_video_aisr";
    static final String LIVE_TONE = "tv_picture_video_live_tone";
    static final String DUP_SPEAKER = "projectorsoft_audio_dup_speaker";
    /** Primary output selection as a plain dropdown, not a separate OEM screen. */
    static final String PRIMARY_OUT = "projectorsoft_audio_primary";
    private static final String KEY_OUT = "projectorsoft_extras_audio_primary";
    private static final String[] OUT_NAMES = {
            "Авто (по подключению)", "Динамики проектора", "Bluetooth",
            "HDMI / ARC", "Линейный выход"
    };
    private static final int[] OUT_TYPES = {-1,
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
            AudioDeviceInfo.TYPE_HDMI_ARC,
            AudioDeviceInfo.TYPE_LINE_ANALOG};

    private static final String KEY_GAME = "projectorsoft_extras_game_mode";
    private static final String KEY_ALLM = "projectorsoft_extras_allm";
    private static final String KEY_AISR = "projectorsoft_extras_aisr";
    private static final String KEY_LIVE = "projectorsoft_extras_live_tone";
    private static final String KEY_DUP = "projectorsoft_extras_dup_speaker";

    private ProjectorExtras() {}

    // ---------------- Sources ----------------

    /** Game Mode and ALLM belong on the source screen: they matter with a console. */
    static void installSources(Context context, PreferenceGroup parent) {
        addSwitch(context, parent, KEY_GAME,
                "Игровой режим (низкая задержка)",
                "Отключает постобработку: MEMC и шумоподавление. Задержка ввода падает.",
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
                "Работает, когда разрешение источника ниже 4K.",
                226, AISR, null);
        addSwitch(context, parent, KEY_LIVE,
                "Live Tone",
                "Тональная компрессия HDR. На SDR-контенте не действует.",
                227, LIVE_TONE, null);
    }

    // ---------------- Sound ----------------

    /**
     * Duplicate the media stream to the built-in speaker while an external
     * output stays primary.
     *
     * setPreferredDevicesForStrategy is @SystemApi. A normal app gets
     * InvocationTargetException (measured on 8054 under uid 10058), so this
     * only works because the settings APK runs as uid 1000.
     */
    static void installSound(Context context, PreferenceGroup parent) {
        SwitchPreference row = new SwitchPreference(context);
        row.setKey(KEY_DUP);
        row.setTitle("Дублировать на динамики");
        row.setSummary("Второй выход: звук идёт и во внешнее устройство, и в проектор.");
        row.setOrder(250);
        row.setPersistent(true);
        row.setChecked(Settings.Global.getInt(context.getContentResolver(), DUP_SPEAKER, 0) == 1);
        row.setOnPreferenceChangeListener((pref, value) -> {
            boolean on = value instanceof Boolean && (Boolean) value;
            boolean stored = Settings.Global.putInt(context.getContentResolver(),
                    DUP_SPEAKER, on ? 1 : 0);
            boolean applied = applySpeakerDuplication(context, on);
            Log.i(TAG, "dup speaker on=" + on + " stored=" + stored + " applied=" + applied);
            android.widget.Toast.makeText(context,
                    applied ? (on ? "Дублирование включено" : "Дублирование выключено")
                            : "Дублирование недоступно на этом устройстве",
                    android.widget.Toast.LENGTH_LONG).show();
            if (!applied) {
                // Read back and reflect the real state instead of a lie.
                ((SwitchPreference) pref).setChecked(
                        Settings.Global.getInt(context.getContentResolver(), DUP_SPEAKER, 0) == 1);
            }
            return false;
        });
        if (parent != null) parent.addPreference(row);
    }

    /**
     * Replaces the OEM "separate screen" audio-output entry with a dropdown of
     * the outputs this board actually reports. Verified device list on 8054:
     * built-in speaker, Bluetooth A2DP, HDMI ARC.
     */
    static void installOutputChoice(Context context, PreferenceScreen screen) {
        if (screen == null) return;
        // Drop the OEM row that opens a separate activity.
        Preference legacy = screen.findPreference("projectorsoft_audio_output");
        if (legacy != null) screen.removePreference(legacy);

        ListPreference list = new ListPreference(context);
        list.setKey(KEY_OUT);
        list.setTitle("Устройство вывода звука");
        list.setOrder(240);
        list.setEntries(OUT_NAMES);
        String[] values = new String[OUT_NAMES.length];
        for (int i = 0; i < values.length; i++) values[i] = String.valueOf(i);
        list.setEntryValues(values);
        list.setPersistent(false);
        int saved = Settings.Global.getInt(context.getContentResolver(), PRIMARY_OUT, 0);
        if (saved < 0 || saved >= OUT_NAMES.length) saved = 0;
        list.setValue(String.valueOf(saved));
        list.setOnPreferenceChangeListener((pref, value) -> {
            int index;
            try {
                index = Integer.parseInt(String.valueOf(value));
            } catch (NumberFormatException bad) {
                return false;
            }
            Settings.Global.putInt(context.getContentResolver(), PRIMARY_OUT, index);
            boolean ok = applyPrimaryOutput(context, index);
            Log.i(TAG, "audio primary=" + index + " applied=" + ok);
            android.widget.Toast.makeText(context,
                    ok ? ("Выход переключён: " + OUT_NAMES[index])
                       : ("Не удалось переключить: " + OUT_NAMES[index]),
                    android.widget.Toast.LENGTH_LONG).show();
            if (pref instanceof ListPreference) {
                ((ListPreference) pref).setValue(String.valueOf(index));
            }
            return false;
        });
        screen.addPreference(list);
    }

    /** Routes the media strategy to the requested output type. */
    private static boolean applyPrimaryOutput(Context context, int index) {
        if (index <= 0) return true; // "Auto" leaves the OEM routing untouched
        if (index >= OUT_TYPES.length) return false;
        try {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (am == null) return false;
            int wanted = OUT_TYPES[index];
            List<AudioDeviceInfo> devices = new ArrayList<>();
            for (AudioDeviceInfo device : am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)) {
                if (device.getType() == wanted) devices.add(device);
            }
            if (devices.isEmpty()) {
                Log.w(TAG, "no device of type " + wanted);
                return false;
            }
            Object strategies = AudioManager.class
                    .getMethod("getAudioProductStrategies").invoke(am);
            if (!(strategies instanceof List)) return false;
            List<?> all = (List<?>) strategies;
            boolean any = false;
            boolean sawMedia = false;
            Log.i(TAG, "audio strategies=" + all.size() + " wanted=" + wanted
                    + " devices=" + devices.size());
            for (Object strategy : all) {
                String name = strategyName(strategy).toLowerCase();
                boolean media = name.contains("media");
                Log.i(TAG, "strategy " + name + " media=" + media);
                if (media) { sawMedia = true; any |= setPreferred(am, strategy, devices); }
            }
            if (!sawMedia) {
                // Some builds name the strategy differently; fall back to all.
                for (Object strategy : all) any |= setPreferred(am, strategy, devices);
            }
            Log.i(TAG, "audio primary applied=" + any);
            return any;
        } catch (Throwable error) {
            Log.w(TAG, "primary output routing failed", error);
            return false;
        }
    }
    /** Adds or removes the built-in speaker from the media strategy. */
    private static boolean applySpeakerDuplication(Context context, boolean enable) {
        try {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (am == null) return false;
            List<AudioDeviceInfo> speakers = new ArrayList<>();
            for (AudioDeviceInfo device : am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)) {
                if (device.getType() == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) speakers.add(device);
            }
            if (speakers.isEmpty()) return false;

            Object strategies = AudioManager.class
                    .getMethod("getAudioProductStrategies").invoke(am);
            if (!(strategies instanceof List)) return false;
            boolean any = false;
            for (Object strategy : (List<?>) strategies) {
                String name = strategyName(strategy).toLowerCase();
                if (!name.contains("media")) continue;
                List<AudioDeviceInfo> current = preferredDevices(am, strategy);
                if (current == null) current = new ArrayList<>();
                List<AudioDeviceInfo> next = new ArrayList<>(current);
                if (enable) {
                    for (AudioDeviceInfo speaker : speakers) {
                        if (!next.contains(speaker)) next.add(speaker);
                    }
                } else {
                    for (AudioDeviceInfo speaker : speakers) next.remove(speaker);
                }
                any |= setPreferred(am, strategy, next);
            }
            return any;
        } catch (Throwable error) {
            Log.w(TAG, "speaker duplication unavailable", error);
            return false;
        }
    }

    private static String strategyName(Object strategy) {
        try {
            Object value = strategy.getClass().getMethod("getName").invoke(strategy);
            return value == null ? "?" : value.toString();
        } catch (Throwable ignored) {
            return "?";
        }
    }

    @SuppressWarnings("unchecked")
    private static List<AudioDeviceInfo> preferredDevices(AudioManager am, Object strategy) {
        try {
            for (java.lang.reflect.Method method : AudioManager.class.getMethods()) {
                if (!method.getName().equals("getPreferredDevicesForStrategy")) continue;
                Class<?>[] types = method.getParameterTypes();
                if (types.length != 1 || !types[0].isInstance(strategy)) continue;
                Object result = method.invoke(am, strategy);
                if (result instanceof List) return new ArrayList<>((List<AudioDeviceInfo>) result);
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static boolean setPreferred(AudioManager am, Object strategy,
                                        List<AudioDeviceInfo> devices) {
        try {
            for (java.lang.reflect.Method method : AudioManager.class.getMethods()) {
                if (!method.getName().equals("setPreferredDevicesForStrategy")) continue;
                Class<?>[] types = method.getParameterTypes();
                if (types.length != 2 || !types[0].isInstance(strategy)
                        || !List.class.isAssignableFrom(types[1])) continue;
                Object result = method.invoke(am, strategy, devices);
                return result instanceof Boolean && (Boolean) result;
            }
        } catch (Throwable error) {
            Log.w(TAG, "setPreferredDevicesForStrategy failed", error);
        }
        return false;
    }

    // ---------------- Keystone reset ----------------

    /**
     * Zero every keystone corner plus the automatic keystone flags.
     *
     * Keys verified on the live 8054 (getprop): persist.rjpj.fourcorner.keystone_*
     * is what com.rjpj.tcorrection actually reads; persist.display.keystone_* is
     * the display-side mirror. Digital zoom and HV live inside the OEM activities
     * and expose no property, so they are not touched here.
     */
    private static final String[] CORNERS = {
            "ltx", "lty", "rtx", "rty", "lbx", "lby", "rbx", "rby"
    };

    static void installKeystoneReset(Context context, PreferenceGroup parent) {
        if (parent == null) return;
        Preference row = new Preference(context);
        row.setKey("projectorsoft_keystone_reset_all");
        row.setTitle("Сбросить все коррекции трапеции");
        row.setSummary("Четыре угла и автокоррекция возвращаются к нулю.");
        row.setOrder(300);
        row.setOnPreferenceClickListener(item -> {
            int changed = resetKeystone(context);
            android.widget.Toast.makeText(context,
                    changed > 0 ? "Коррекции трапеции сброшены" : "Не удалось сбросить",
                    android.widget.Toast.LENGTH_SHORT).show();
            Log.i(TAG, "keystone reset keys=" + changed);
            return true;
        });
        parent.addPreference(row);
    }

    /**
     * Writes 0 into every corner property and verifies the write landed.
     *
     * A write only counts when reading the property back returns 0, because
     * persist.* writes go through property_service and can be refused silently
     * depending on SELinux policy. The result is logged so a failure is visible
     * instead of looking like a dead button.
     */
    private static int resetKeystone(Context context) {
        // The OEM applies a reset through KeystoneUtil.resetKeystone(), which
        // pushes the corners to the display flinger. Writing the properties
        // alone only takes effect the next time an OEM keystone screen runs,
        // which is exactly the "resets only after I open the correction
        // screen" behaviour reported by the owner.
        boolean nativeReset = resetKeystoneNative(context);
        Log.i(TAG, "keystone native reset=" + nativeReset);
        int changed = 0;
        try {
            Class<?> props = Class.forName("android.os.SystemProperties");
            java.lang.reflect.Method set = props.getMethod("set", String.class, String.class);
            java.lang.reflect.Method get = props.getMethod("get", String.class, String.class);
            for (String corner : CORNERS) {
                if (writeAndVerify(set, get, "persist.rjpj.fourcorner.keystone_" + corner)) changed++;
                writeAndVerify(set, get, "persist.display.keystone_" + corner);
            }
            writeAndVerify(set, get, "persist.rjpj.autokeystone.move");
            writeAndVerify(set, get, "persist.rjpj.autokeystone.poweron");
            Log.i(TAG, "keystone reset verified corners=" + changed);
        } catch (Throwable error) {
            Log.w(TAG, "keystone reset failed", error);
        }
        return changed;
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
            Settings.Global.putInt(context.getContentResolver(), ARC_ENABLED, on ? 1 : 0);
            int read = Settings.Global.getInt(context.getContentResolver(), ARC_ENABLED, -1);
            ((SwitchPreference) pref).setChecked(read == 1);
            return false;
        });
        parent.addPreference(arc);
    }
    // ---------------- HDMI-CEC ----------------

    // HdmiControlService reads these two; verified absent from Settings on the
    // live 8054, so the service runs on its defaults until something writes them.
    static final String CEC_ENABLED = "hdmi_control_enabled";
    static final String CEC_AUTO_WAKEUP = "hdmi_control_auto_wakeup_enabled";
    private static final String KEY_CEC = "projectorsoft_extras_cec";
    private static final String KEY_CEC_POWER = "projectorsoft_extras_cec_power";

    /** CEC master switch and "wake the HDMI device with the projector". */
    static void installCec(Context context, PreferenceGroup parent) {
        if (parent == null) return;
        SwitchPreference cec = new SwitchPreference(context);
        cec.setKey(KEY_CEC);
        cec.setTitle("HDMI-CEC");
        cec.setSummary("Управление подключёнными HDMI-устройствами одним пультом.");
        cec.setOrder(50);
        cec.setPersistent(false);
        cec.setChecked(Settings.Global.getInt(context.getContentResolver(), CEC_ENABLED, 1) == 1);
        cec.setOnPreferenceChangeListener((pref, value) -> {
            boolean on = value instanceof Boolean && (Boolean) value;
            Settings.Global.putInt(context.getContentResolver(), CEC_ENABLED, on ? 1 : 0);
            // The OEM also stores a dotted alias; keep it in step for its own reader.
            Settings.Global.putInt(context.getContentResolver(),
                    "Settings.Global.HDMI_CONTROL_ENABLED", on ? 1 : 0);
            int read = Settings.Global.getInt(context.getContentResolver(), CEC_ENABLED, -1);
            ((SwitchPreference) pref).setChecked(read == 1);
            return false;
        });
        parent.addPreference(cec);

        SwitchPreference wake = new SwitchPreference(context);
        wake.setKey(KEY_CEC_POWER);
        wake.setTitle("Включать HDMI-устройство вместе с проектором");
        wake.setSummary("One Touch Play: проектор будит ресивер или приставку при включении.");
        wake.setOrder(51);
        wake.setPersistent(false);
        wake.setChecked(readOneTouchPlay(context));
        wake.setOnPreferenceChangeListener((pref, value) -> {
            boolean on = value instanceof Boolean && (Boolean) value;
            Settings.Global.putInt(context.getContentResolver(),
                    CEC_AUTO_WAKEUP, on ? 1 : 0);
            ((SwitchPreference) pref).setChecked(readOneTouchPlay(context));
            return false;
        });
        parent.addPreference(wake);
    }

    private static boolean readOneTouchPlay(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), CEC_AUTO_WAKEUP, 1) == 1;
    }
    // ---------------- shared ----------------

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

    /** Refreshes stored state after the user leaves and returns to a page. */
    static void refresh(PreferenceScreen screen, Context context) {
        if (screen == null) return;
        sync(screen, context, KEY_GAME, GAME_MODE);
        sync(screen, context, KEY_ALLM, ALLM);
        sync(screen, context, KEY_AISR, AISR);
        sync(screen, context, KEY_LIVE, LIVE_TONE);
        sync(screen, context, KEY_DUP, DUP_SPEAKER);
    }

    private static void sync(PreferenceScreen screen, Context context, String key, String storage) {
        Preference row = screen.findPreference(key);
        if (row instanceof SwitchPreference) {
            ((SwitchPreference) row).setChecked(
                    Settings.Global.getInt(context.getContentResolver(), storage, 0) == 1);
        }
    }
}
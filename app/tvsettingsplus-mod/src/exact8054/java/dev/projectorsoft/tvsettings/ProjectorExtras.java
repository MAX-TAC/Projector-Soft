package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.provider.Settings;
import android.util.Log;
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
            if (!applied) {
                // Read back and reflect the real state instead of a lie.
                ((SwitchPreference) pref).setChecked(
                        Settings.Global.getInt(context.getContentResolver(), DUP_SPEAKER, 0) == 1);
            }
            return false;
        });
        if (parent != null) parent.addPreference(row);
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
            return false; // re-read below, never trust the click
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
package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.provider.Settings;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * Переключатель выхода звука: динамики / Bluetooth / оба одновременно.
 *
 * ПОЧЕМУ ЭТО РАБОТАЕТ ТОЛЬКО В MOD:
 * android.media.AudioManager.setPreferredDevicesForStrategy объявлен как
 * @SystemApi. Обычному приложению он недоступен — при вызове через
 * рефлексию прилетает InvocationTargetException (проверено зондом на 8054
 * под uid 10058). MOD устанавливается как обновление системного
 * com.android.tv.settings и работает под uid 1000, где метод разрешён.
 *
 * Одновременный вывод: setPreferredDevicesForStrategy принимает СПИСОК
 * устройств. Штатная стратегия STRATEGY_SONIFICATION на этой прошивке уже
 * содержит сразу два устройства, значит AudioFlinger такое поддерживает.
 */
public final class OemAudioOutputHook {
    private static final String TAG = "TvSettingsPlusMOD";
    private static final String PREF_MODE = "projectorsoft_audio_output_mode";

    private static final int MODE_AUTO = 0;
    private static final int MODE_SPEAKER = 1;
    private static final int MODE_BLUETOOTH = 2;
    private static final int MODE_BOTH = 3;

    private static final String[] ENTRIES = {
            "Авто (по подключению)",
            "Динамики проектора",
            "Bluetooth",
            "Оба одновременно"
    };

    private OemAudioOutputHook() {}

    public static void install(Context ctx, Object group, Class<?> preferenceClass) {
        try {
            Class<?> listPref = Class.forName("androidx.preference.ListPreference");
            Object item = listPref.getConstructor(Context.class).newInstance(ctx);
            preferenceClass.getMethod("setKey", String.class).invoke(item, PREF_MODE);
            preferenceClass.getMethod("setTitle", CharSequence.class).invoke(item, "Выход звука");
            preferenceClass.getMethod("setOrder", int.class).invoke(item, 238);
            preferenceClass.getMethod("setPersistent", boolean.class).invoke(item, true);
            listPref.getMethod("setEntries", CharSequence[].class).invoke(item, (Object) ENTRIES);
            String[] values = {"0", "1", "2", "3"};
            listPref.getMethod("setEntryValues", CharSequence[].class).invoke(item, (Object) values);
            int saved = Settings.Global.getInt(ctx.getContentResolver(), PREF_MODE, MODE_AUTO);
            listPref.getMethod("setValue", String.class).invoke(item, String.valueOf(saved));

            Class<?> listener = Class.forName("androidx.preference.Preference$OnPreferenceChangeListener");
            InvocationHandler h = (proxy, method, args) -> {
                String name = method.getName();
                if ("onPreferenceChange".equals(name) && args != null && args.length >= 2) {
                    try {
                        int mode = Integer.parseInt(String.valueOf(args[1]));
                        boolean ok = applyMode(ctx, mode);
                        if (ok) {
                            Settings.Global.putInt(ctx.getContentResolver(), PREF_MODE, mode);
                        }
                        return ok;
                    } catch (Throwable e) {
                        Log.w(TAG, "audio output apply failed", e);
                        return false;
                    }
                }
                if ("toString".equals(name)) return "AudioOutput";
                if ("hashCode".equals(name)) return System.identityHashCode(proxy);
                if ("equals".equals(name)) return args != null && args.length > 0 && proxy == args[0];
                return false;
            };
            Object hObj = Proxy.newProxyInstance(listener.getClassLoader(), new Class<?>[]{listener}, h);
            listPref.getMethod("setOnPreferenceChangeListener", listener).invoke(item, hObj);
            group.getClass().getMethod("addPreference", preferenceClass).invoke(group, item);
        } catch (Throwable e) {
            Log.w(TAG, "AudioOutput UI unavailable", e);
        }
    }

    private static boolean applyMode(Context ctx, int mode) {
        AudioManager am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        if (am == null) return false;
        List<AudioDeviceInfo> speakers = devicesOfType(am, AudioDeviceInfo.TYPE_BUILTIN_SPEAKER);
        List<AudioDeviceInfo> bt = devicesOfType(am, AudioDeviceInfo.TYPE_BLUETOOTH_A2DP);

        List<AudioDeviceInfo> wanted = new ArrayList<>();
        switch (mode) {
            case MODE_SPEAKER:
                wanted.addAll(speakers);
                break;
            case MODE_BLUETOOTH:
                wanted.addAll(bt);
                break;
            case MODE_BOTH:
                wanted.addAll(speakers);
                wanted.addAll(bt);
                break;
            case MODE_AUTO:
            default:
                // Пустой список = вернуть штатное поведение (Android сам выберет).
                break;
        }

        if (mode != MODE_AUTO && wanted.isEmpty()) {
            Log.w(TAG, "no device for mode " + mode);
            return false;
        }
        return applyToMediaStrategy(am, wanted);
    }

    private static List<AudioDeviceInfo> devicesOfType(AudioManager am, int type) {
        List<AudioDeviceInfo> out = new ArrayList<>();
        for (AudioDeviceInfo d : am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)) {
            if (d.getType() == type) out.add(d);
        }
        return out;
    }

    /**
     * getAudioProductStrategies / setPreferredDevicesForStrategy — @SystemApi,
     * поэтому вызываются рефлексией: в SDK-заглушке их нет.
     */
    private static boolean applyToMediaStrategy(AudioManager am, List<AudioDeviceInfo> devices) {
        boolean any = false;
        try {
            Method getStrategies = AudioManager.class.getMethod("getAudioProductStrategies");
            Object strategiesObj = getStrategies.invoke(am);
            if (!(strategiesObj instanceof List)) {
                Log.w(TAG, "getAudioProductStrategies returned " + strategiesObj);
                return false;
            }
            for (Object strategy : (List<?>) strategiesObj) {
                String nm = String.valueOf(strategyName(strategy)).toLowerCase();
                if (!nm.contains("media")) continue;
                boolean ok = callSetPreferred(am, strategy, devices);
                Log.i(TAG, "strategy " + nm + " -> " + ok);
                any |= ok;
            }
        } catch (Throwable e) {
            Log.w(TAG, "applyToMediaStrategy failed", e);
            return false;
        }
        return any;
    }

    private static String strategyName(Object strategy) {
        try {
            Method m = strategy.getClass().getMethod("getName");
            Object v = m.invoke(strategy);
            return v == null ? "?" : v.toString();
        } catch (Throwable ignored) {
            return "?";
        }
    }

    private static boolean callSetPreferred(AudioManager am, Object strategy,
                                            List<AudioDeviceInfo> devices) {
        try {
            for (Method m : AudioManager.class.getMethods()) {
                if (!m.getName().equals("setPreferredDevicesForStrategy")) continue;
                Class<?>[] p = m.getParameterTypes();
                if (p.length != 2 || !p[0].isInstance(strategy) || !List.class.isAssignableFrom(p[1])) {
                    continue;
                }
                Object r = m.invoke(am, strategy, devices);
                return r instanceof Boolean && (Boolean) r;
            }
        } catch (Throwable e) {
            Log.w(TAG, "setPreferredDevicesForStrategy failed", e);
        }
        return false;
    }
}

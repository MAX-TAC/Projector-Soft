package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.provider.Settings;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Выход звука: выбор основного выхода + дополнительный вывод на динамики.
 *
 * Схема из двух независимых частей, потому что у проектора бывает
 * несколько выходов одновременно:
 *
 *  1. ОСНОВНОЙ выход — один из доступных:
 *       Авто / Динамики / Bluetooth / HDMI (ARC или eARC) / Линейный
 *  2. ДОПОЛНИТЕЛЬНО: дублировать звук на динамики проектора (вкл/выкл).
 *     Позволяет слышать звук и через саундбар, и через встроенные динамики.
 *
 * ПОЧЕМУ ЭТО РАБОТАЕТ ТОЛЬКО В MOD:
 * AudioManager.setPreferredDevicesForStrategy — @SystemApi. Обычному
 * приложению недоступен (проверено зондом на 8054 под uid 10058:
 * InvocationTargetException). MOD ставится как обновление системного
 * com.android.tv.settings и работает под uid 1000, где метод разрешён.
 *
 * Список устройств: setPreferredDevicesForStrategy принимает СПИСОК,
 * поэтому дополнительный вывод — это просто второе устройство в списке.
 */
public final class OemAudioOutputHook {
    private static final String TAG = "TvSettingsPlusMOD";
    private static final String PREF_PRIMARY = "projectorsoft_audio_primary";
    private static final String PREF_DUP_SPEAKER = "projectorsoft_audio_dup_speaker";

    private static final int PRIMARY_AUTO = 0;
    private static final int PRIMARY_SPEAKER = 1;
    private static final int PRIMARY_BLUETOOTH = 2;
    private static final int PRIMARY_HDMI = 3;
    private static final int PRIMARY_LINE = 4;

    private static final String[] PRIMARY_ENTRIES = {
            "Авто (по подключению)",
            "Динамики проектора",
            "Bluetooth",
            "HDMI / ARC",
            "Линейный выход"
    };
    private static final String[] PRIMARY_VALUES = {"0", "1", "2", "3", "4"};

    private OemAudioOutputHook() {}

    public static void install(Context ctx, Object group, Class<?> preferenceClass) {
        try {
            // 1) Основной выход
            Class<?> listPref = Class.forName("androidx.preference.ListPreference");
            Object primary = listPref.getConstructor(Context.class).newInstance(ctx);
            preferenceClass.getMethod("setKey", String.class).invoke(primary, PREF_PRIMARY);
            preferenceClass.getMethod("setTitle", CharSequence.class).invoke(primary, "Выход звука");
            preferenceClass.getMethod("setSummary", CharSequence.class)
                    .invoke(primary, availableSummary(ctx));
            preferenceClass.getMethod("setOrder", int.class).invoke(primary, 238);
            preferenceClass.getMethod("setPersistent", boolean.class).invoke(primary, true);
            listPref.getMethod("setEntries", CharSequence[].class)
                    .invoke(primary, (Object) PRIMARY_ENTRIES);
            listPref.getMethod("setEntryValues", CharSequence[].class)
                    .invoke(primary, (Object) PRIMARY_VALUES);
            int savedPrimary = Settings.Global.getInt(ctx.getContentResolver(),
                    PREF_PRIMARY, PRIMARY_AUTO);
            listPref.getMethod("setValue", String.class)
                    .invoke(primary, String.valueOf(savedPrimary));

            Class<?> changeListener = Class.forName(
                    "androidx.preference.Preference$OnPreferenceChangeListener");
            Object primaryCb = Proxy.newProxyInstance(changeListener.getClassLoader(),
                    new Class<?>[]{changeListener},
                    (proxy, method, args) -> {
                        String name = method.getName();
                        if ("onPreferenceChange".equals(name) && args != null && args.length >= 2) {
                            try {
                                int mode = Integer.parseInt(String.valueOf(args[1]));
                                Settings.Global.putInt(ctx.getContentResolver(), PREF_PRIMARY, mode);
                                return applyRouting(ctx);
                            } catch (Throwable e) {
                                Log.w(TAG, "primary output change failed", e);
                                return false;
                            }
                        }
                        return defaultProxy(proxy, method, args, "AudioPrimary");
                    });
            listPref.getMethod("setOnPreferenceChangeListener", changeListener)
                    .invoke(primary, primaryCb);
            group.getClass().getMethod("addPreference", preferenceClass).invoke(group, primary);

            // 2) Дополнительный вывод на динамики
            Class<?> switchPref = Class.forName("androidx.preference.SwitchPreferenceCompat");
            Object dup = switchPref.getConstructor(Context.class).newInstance(ctx);
            preferenceClass.getMethod("setTitle", CharSequence.class)
                    .invoke(dup, "Дублировать на динамики");
            preferenceClass.getMethod("setSummary", CharSequence.class)
                    .invoke(dup, "Второй выход: звук идёт и через внешнее устройство, и через проектор.");
            preferenceClass.getMethod("setOrder", int.class).invoke(dup, 239);
            preferenceClass.getMethod("setPersistent", boolean.class).invoke(dup, true);
            boolean dupOn = Settings.Global.getInt(ctx.getContentResolver(), PREF_DUP_SPEAKER, 0) == 1;
            switchPref.getMethod("setChecked", boolean.class).invoke(dup, dupOn);
            Object dupCb = Proxy.newProxyInstance(changeListener.getClassLoader(),
                    new Class<?>[]{changeListener},
                    (proxy, method, args) -> {
                        String name = method.getName();
                        if ("onPreferenceChange".equals(name) && args != null && args.length >= 2) {
                            boolean on = Boolean.TRUE.equals(args[1]);
                            Settings.Global.putInt(ctx.getContentResolver(),
                                    PREF_DUP_SPEAKER, on ? 1 : 0);
                            return applyRouting(ctx);
                        }
                        return defaultProxy(proxy, method, args, "AudioDup");
                    });
            preferenceClass.getMethod("setOnPreferenceChangeListener", changeListener)
                    .invoke(dup, dupCb);
            group.getClass().getMethod("addPreference", preferenceClass).invoke(group, dup);
        } catch (Throwable e) {
            Log.w(TAG, "AudioOutput UI unavailable", e);
        }
    }

    private static Object defaultProxy(Object proxy, java.lang.reflect.Method method,
                                       Object[] args, String tag) {
        String name = method.getName();
        if ("toString".equals(name)) return tag;
        if ("hashCode".equals(name)) return System.identityHashCode(proxy);
        if ("equals".equals(name)) return args != null && args.length > 0 && proxy == args[0];
        return false;
    }

    private static String availableSummary(Context ctx) {
        try {
            AudioManager am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
            if (am == null) return "";
            Map<String, Integer> seen = collectAvailable(am);
            StringBuilder sb = new StringBuilder("Подключено: ");
            boolean first = true;
            for (String name : seen.keySet()) {
                if (!first) sb.append(", ");
                sb.append(name);
                first = false;
            }
            return first ? "Нет доступных выходов" : sb.toString();
        } catch (Throwable e) {
            return "";
        }
    }

    /** Читаемый список того, что реально подключено прямо сейчас. */
    private static Map<String, Integer> collectAvailable(AudioManager am) {
        Map<String, Integer> out = new LinkedHashMap<>();
        for (AudioDeviceInfo d : am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)) {
            out.put(describe(d), d.getType());
        }
        return out;
    }

    private static String describe(AudioDeviceInfo d) {
        String name = d.getProductName() == null ? "" : d.getProductName().toString();
        String type;
        switch (d.getType()) {
            case AudioDeviceInfo.TYPE_BUILTIN_SPEAKER: type = "Динамики"; break;
            case AudioDeviceInfo.TYPE_BLUETOOTH_A2DP: type = "BT"; break;
            case AudioDeviceInfo.TYPE_BLUETOOTH_SCO: type = "BT-гарнитура"; break;
            case AudioDeviceInfo.TYPE_BLE_HEADSET: type = "BLE-наушники"; break;
            case AudioDeviceInfo.TYPE_BLE_SPEAKER: type = "BLE-колонка"; break;
            case AudioDeviceInfo.TYPE_HDMI: type = "HDMI"; break;
            case AudioDeviceInfo.TYPE_HDMI_ARC: type = "ARC"; break;
            case AudioDeviceInfo.TYPE_HDMI_EARC: type = "eARC"; break;
            case AudioDeviceInfo.TYPE_USB_DEVICE: type = "USB"; break;
            case AudioDeviceInfo.TYPE_USB_HEADSET: type = "USB-наушники"; break;
            case AudioDeviceInfo.TYPE_WIRED_HEADPHONES: type = "Наушники"; break;
            case AudioDeviceInfo.TYPE_WIRED_HEADSET: type = "Гарнитура"; break;
            case AudioDeviceInfo.TYPE_LINE_ANALOG: type = "Линейный"; break;
            case AudioDeviceInfo.TYPE_LINE_DIGITAL: type = "Линейный цифровой"; break;
            case AudioDeviceInfo.TYPE_DOCK: type = "Док"; break;
            case AudioDeviceInfo.TYPE_AUX_LINE: type = "AUX"; break;
            default: type = "тип " + d.getType(); break;
        }
        return name.isEmpty() ? type : type + " " + name;
    }

    /** Пересчитывает маршрут по сохранённым настройкам. */
    private static boolean applyRouting(Context ctx) {
        try {
            AudioManager am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
            if (am == null) return false;
            int primary = Settings.Global.getInt(ctx.getContentResolver(),
                    PREF_PRIMARY, PRIMARY_AUTO);
            boolean dup = Settings.Global.getInt(ctx.getContentResolver(),
                    PREF_DUP_SPEAKER, 0) == 1;

            List<AudioDeviceInfo> wanted = new ArrayList<>();
            List<AudioDeviceInfo> speakers = byType(am, AudioDeviceInfo.TYPE_BUILTIN_SPEAKER);

            switch (primary) {
                case PRIMARY_SPEAKER:
                    wanted.addAll(speakers);
                    break;
                case PRIMARY_BLUETOOTH:
                    wanted.addAll(byType(am, AudioDeviceInfo.TYPE_BLUETOOTH_A2DP));
                    wanted.addAll(byType(am, AudioDeviceInfo.TYPE_BLE_HEADSET));
                    wanted.addAll(byType(am, AudioDeviceInfo.TYPE_BLE_SPEAKER));
                    wanted.addAll(byType(am, AudioDeviceInfo.TYPE_BLUETOOTH_SCO));
                    break;
                case PRIMARY_HDMI:
                    wanted.addAll(byType(am, AudioDeviceInfo.TYPE_HDMI_EARC));
                    wanted.addAll(byType(am, AudioDeviceInfo.TYPE_HDMI_ARC));
                    wanted.addAll(byType(am, AudioDeviceInfo.TYPE_HDMI));
                    break;
                case PRIMARY_LINE:
                    wanted.addAll(byType(am, AudioDeviceInfo.TYPE_LINE_ANALOG));
                    wanted.addAll(byType(am, AudioDeviceInfo.TYPE_LINE_DIGITAL));
                    wanted.addAll(byType(am, AudioDeviceInfo.TYPE_AUX_LINE));
                    wanted.addAll(byType(am, AudioDeviceInfo.TYPE_WIRED_HEADPHONES));
                    wanted.addAll(byType(am, AudioDeviceInfo.TYPE_WIRED_HEADSET));
                    break;
                case PRIMARY_AUTO:
                default:
                    break;  // пустой список = штатное поведение
            }

            if (dup) {
                for (AudioDeviceInfo s : speakers) {
                    if (!wanted.contains(s)) wanted.add(s);
                }
            }

            if (primary != PRIMARY_AUTO && wanted.isEmpty()) {
                Log.w(TAG, "no device available for primary mode " + primary);
                return false;
            }
            Log.i(TAG, "routing: primary=" + primary + " dup=" + dup
                    + " devices=" + wanted.size());
            return applyToMediaStrategy(am, wanted);
        } catch (Throwable e) {
            Log.w(TAG, "applyRouting failed", e);
            return false;
        }
    }

    private static List<AudioDeviceInfo> byType(AudioManager am, int type) {
        List<AudioDeviceInfo> out = new ArrayList<>();
        for (AudioDeviceInfo d : am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)) {
            if (d.getType() == type) out.add(d);
        }
        return out;
    }

    private static boolean applyToMediaStrategy(AudioManager am, List<AudioDeviceInfo> devices) {
        boolean any = false;
        try {
            Object strategiesObj = AudioManager.class
                    .getMethod("getAudioProductStrategies").invoke(am);
            if (!(strategiesObj instanceof List)) return false;
            for (Object strategy : (List<?>) strategiesObj) {
                String nm = strategyName(strategy).toLowerCase();
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
            Object v = strategy.getClass().getMethod("getName").invoke(strategy);
            return v == null ? "?" : v.toString();
        } catch (Throwable ignored) {
            return "?";
        }
    }

    private static boolean callSetPreferred(AudioManager am, Object strategy,
                                            List<AudioDeviceInfo> devices) {
        try {
            for (java.lang.reflect.Method m : AudioManager.class.getMethods()) {
                if (!m.getName().equals("setPreferredDevicesForStrategy")) continue;
                Class<?>[] p = m.getParameterTypes();
                if (p.length != 2 || !p[0].isInstance(strategy)
                        || !List.class.isAssignableFrom(p[1])) continue;
                Object r = m.invoke(am, strategy, devices);
                return r instanceof Boolean && (Boolean) r;
            }
        } catch (Throwable e) {
            Log.w(TAG, "setPreferredDevicesForStrategy failed", e);
        }
        return false;
    }
}

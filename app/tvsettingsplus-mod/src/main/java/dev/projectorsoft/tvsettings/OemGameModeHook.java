package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * Игровой режим (низкая задержка) и ALLM.
 *
 * Зачем в разделе «Источники»: режим нужен при подключении консоли или ПК.
 * Для фильмов разница в задержке не важна.
 *
 * Game Mode — ручной переключатель: отключает постобработку
 * (MEMC, шумоподавление, часть масштабирования), за счёт чего падает
 * задержка ввода. Владелец подтвердил, что режим рабочий и разница
 * ощутима.
 *
 * ALLM — автоматический вариант того же: источник (консоль) сообщает,
 * что ему нужен игровой режим, и проектор переключается сам.
 * ALLM бессмысленен без Game Mode, потому что является лишь триггером.
 *
 * Ключи проверены на 8054: запись принимается TvServices,
 * значение читается обратно.
 */
public final class OemGameModeHook {
    private static final String TAG = "TvSettingsPlusMOD";

    /** OEM-ключ режима (CFG_VIDEO_VID_GAME_MODE). */
    private static final String KEY_GAME_MODE = "g_video__vid_game_mode";
    /** Зеркальный ключ, который использует пункт штатного меню. */
    private static final String KEY_GAME_MODE_UI = "tv_picture_video_game_mode";
    /** OEM-ключ ALLM (CFG_VIDEO_VID_ALLM_SWITCH). */
    private static final String KEY_ALLM = "g_video__vid_allm_switch";
    /** Зеркальный ключ ALLM. */
    private static final String KEY_ALLM_UI = "tv_picture_video_allm";

    private OemGameModeHook() {}

    public static void install(Context ctx, Object group, Class<?> preferenceClass) {
        try {
            // Game Mode
            int gameOn = Settings.Global.getInt(ctx.getContentResolver(), KEY_GAME_MODE, 0);
            Object gameSwitch = buildToggle(ctx, "Игровой режим (низкая задержка)",
                    "Отключает постобработку: MEMC, шумоподавление. Задержка падает.",
                    gameOn == 1, 255,
                    (proxy, method, args) -> {
                        String name = method.getName();
                        if ("onPreferenceChange".equals(name) && args != null && args.length >= 2) {
                            boolean on = Boolean.TRUE.equals(args[1]);
                            return writeBoth(ctx, KEY_GAME_MODE, KEY_GAME_MODE_UI, on);
                        }
                        return proxyDefault(proxy, method, args, "GameMode");
                    });
            if (gameSwitch != null) {
                group.getClass().getMethod("addPreference", preferenceClass).invoke(group, gameSwitch);
            }

            // ALLM
            int allmOn = Settings.Global.getInt(ctx.getContentResolver(), KEY_ALLM, 0);
            Object allmSwitch = buildToggle(ctx, "ALLM (авто-игровой режим)",
                    "Проектор сам включит игровой режим, когда источник попросит.",
                    allmOn == 1, 256,
                    (proxy, method, args) -> {
                        String name = method.getName();
                        if ("onPreferenceChange".equals(name) && args != null && args.length >= 2) {
                            boolean on = Boolean.TRUE.equals(args[1]);
                            return writeBoth(ctx, KEY_ALLM, KEY_ALLM_UI, on);
                        }
                        return proxyDefault(proxy, method, args, "Allm");
                    });
            if (allmSwitch != null) {
                group.getClass().getMethod("addPreference", preferenceClass).invoke(group, allmSwitch);
            }
        } catch (Throwable e) {
            Log.w(TAG, "GameMode UI unavailable", e);
        }
    }

    /** Пишет и OEM-ключ, и зеркальный, чтобы работали оба пути. */
    private static boolean writeBoth(Context ctx, String oem, String mirror, boolean on) {
        try {
            int v = on ? 1 : 0;
            boolean a = Settings.Global.putInt(ctx.getContentResolver(), oem, v);
            boolean b = Settings.Global.putInt(ctx.getContentResolver(), mirror, v);
            Log.i(TAG, "writeBoth " + oem + "=" + v + " ok=" + a + ", " + mirror + "=" + v + " ok=" + b);
            return a && b;
        } catch (Throwable e) {
            Log.w(TAG, "writeBoth failed", e);
            return false;
        }
    }

    private static Object proxyDefault(Object proxy, java.lang.reflect.Method method,
                                       Object[] args, String tag) {
        String name = method.getName();
        if ("toString".equals(name)) return tag;
        if ("hashCode".equals(name)) return System.identityHashCode(proxy);
        if ("equals".equals(name)) return args != null && args.length > 0 && proxy == args[0];
        return false;
    }

    private static Object buildToggle(Context ctx, String title, String summary,
                                      boolean checked, int order, InvocationHandler handler) {
        try {
            Class<?> switchPref = Class.forName("androidx.preference.SwitchPreferenceCompat");
            Object item = switchPref.getConstructor(Context.class).newInstance(ctx);
            Class<?> prefClass = Class.forName("androidx.preference.Preference");
            prefClass.getMethod("setTitle", CharSequence.class).invoke(item, title);
            if (summary != null) {
                prefClass.getMethod("setSummary", CharSequence.class).invoke(item, summary);
            }
            prefClass.getMethod("setOrder", int.class).invoke(item, order);
            prefClass.getMethod("setPersistent", boolean.class).invoke(item, false);
            switchPref.getMethod("setChecked", boolean.class).invoke(item, checked);

            Class<?> listener = Class.forName("androidx.preference.Preference$OnPreferenceChangeListener");
            Object proxy = Proxy.newProxyInstance(listener.getClassLoader(),
                    new Class<?>[]{listener}, handler);
            prefClass.getMethod("setOnPreferenceChangeListener", listener).invoke(item, proxy);
            return item;
        } catch (Throwable e) {
            Log.w(TAG, "buildToggle failed for " + title, e);
            return null;
        }
    }
}
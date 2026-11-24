package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * AISR (AI Super Resolution) и Live Tone.
 *
 * AISR — апскейл. Осмыслен только когда картинка НИЖЕ 4K: если источник
 * уже отдаёт 2160p, апскейлить нечего. Владелец на 8054 через приложения
 * (YouTube/HDRezka) разницы не увидел; ожидаемо, потому что приложения
 * идут через GPU-композитор, а не через MediaTek-скалер.
 *
 * Live Tone — тональная компрессия HDR. На SDR-контенте обрабатывать
 * нечего; на 8054 через YouTube HDR разница не наблюдалась.
 *
 * Оба ключа оставлены доступными в меню осознанно: на HDMI-входе
 * видеотракт другой, и там они могут проявиться. Пользователь проверит
 * сам. Если и на HDMI не дадут эффекта — пункты можно убрать.
 *
 * Ключи проверены на 8054: запись принимается, значение читается обратно.
 */
public final class OemPictureEnhanceHook {
    private static final String TAG = "TvSettingsPlusMOD";

    private static final String KEY_AISR = "tv_picture_video_aisr";
    private static final String KEY_LIVE_TONE = "tv_picture_video_live_tone";

    /** AISR уровни: 0 = выкл, далее усиление по возрастанию. */
    private static final String[] AISR_ENTRIES = {
            "Выключен", "Низкий", "Средний", "Высокий", "Максимальный"
    };
    private static final String[] AISR_VALUES = {"0", "1", "2", "3", "4"};

    private static final String[] ONOFF_ENTRIES = {"Выключен", "Включён"};
    private static final String[] ONOFF_VALUES = {"0", "1"};

    private OemPictureEnhanceHook() {}

    public static void install(Context ctx, Object group, Class<?> preferenceClass) {
        try {
            addList(ctx, group, preferenceClass,
                    "projectorsoft_aisr", "AISR (AI-улучшение детализации)",
                    "Работает при разрешении источника ниже 4K.",
                    KEY_AISR, AISR_ENTRIES, AISR_VALUES, 0, 225);

            addList(ctx, group, preferenceClass,
                    "projectorsoft_live_tone", "Live Tone",
                    "Тональная компрессия HDR. На SDR-контенте не действует.",
                    KEY_LIVE_TONE, ONOFF_ENTRIES, ONOFF_VALUES, 0, 226);
        } catch (Throwable e) {
            Log.w(TAG, "PictureEnhance UI unavailable", e);
        }
    }

    private static void addList(Context ctx, Object group, Class<?> preferenceClass,
                                String prefKey, String title, String summary,
                                String oemKey, String[] entries, String[] values,
                                int fallback, int order) throws Exception {
        Class<?> listPref = Class.forName("androidx.preference.ListPreference");
        Object item = listPref.getConstructor(Context.class).newInstance(ctx);
        preferenceClass.getMethod("setKey", String.class).invoke(item, prefKey);
        preferenceClass.getMethod("setTitle", CharSequence.class).invoke(item, title);
        preferenceClass.getMethod("setSummary", CharSequence.class).invoke(item, summary);
        preferenceClass.getMethod("setOrder", int.class).invoke(item, order);
        preferenceClass.getMethod("setPersistent", boolean.class).invoke(item, true);
        listPref.getMethod("setEntries", CharSequence[].class).invoke(item, (Object) entries);
        listPref.getMethod("setEntryValues", CharSequence[].class).invoke(item, (Object) values);

        int current = Settings.Global.getInt(ctx.getContentResolver(), oemKey, fallback);
        if (current < 0) current = fallback;
        listPref.getMethod("setValue", String.class).invoke(item, String.valueOf(current));

        Class<?> listener = Class.forName("androidx.preference.Preference$OnPreferenceChangeListener");
        InvocationHandler h = (proxy, method, args) -> {
            String name = method.getName();
            if ("onPreferenceChange".equals(name) && args != null && args.length >= 2) {
                try {
                    int v = Integer.parseInt(String.valueOf(args[1]));
                    boolean ok = Settings.Global.putInt(ctx.getContentResolver(), oemKey, v);
                    int back = Settings.Global.getInt(ctx.getContentResolver(), oemKey, Integer.MIN_VALUE);
                    Log.i(TAG, oemKey + "=" + v + " put=" + ok + " readback=" + back);
                    return ok && back == v;
                } catch (Throwable e) {
                    Log.w(TAG, oemKey + " write failed", e);
                    return false;
                }
            }
            if ("toString".equals(name)) return "PicEnhance:" + oemKey;
            if ("hashCode".equals(name)) return System.identityHashCode(proxy);
            if ("equals".equals(name)) return args != null && args.length > 0 && proxy == args[0];
            return false;
        };
        Object hObj = Proxy.newProxyInstance(listener.getClassLoader(), new Class<?>[]{listener}, h);
        listPref.getMethod("setOnPreferenceChangeListener", listener).invoke(item, hObj);
        group.getClass().getMethod("addPreference", preferenceClass).invoke(group, item);
    }
}
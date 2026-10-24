package dev.projectorsoft.tvsettings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Adds live OEM projector sections to stock Android TV MainFragment.
 * The preference screen itself stays Android TV / Leanback.
 *
 * Bridge limitation: OEM controllers live in a separate Activity backed by
 * FloatMenuManager. Selecting one of these rows currently launches that
 * existing activity; it does NOT embed FloatMenuManager in the AOSP right pane.
 * Camera/focus/keystone calibration behavior remains OEM-owned.
 */
public final class OemRootMenuHook {
    private static final String PREF_CLASS = "androidx.preference.Preference";
    private static final String ACTIVITY = "com.android.tv.settings.uicustom.MainActivity";
    private static final String SOURCE_ACTION = "dev.projectorsoft.settings.SOURCES";
    private static final String GENERAL_ACTION = "dev.projectorsoft.settings.GENERAL";
    private OemRootMenuHook() {}

    private static void insert(Object category, Class<?> clazz, Context context,
                               String key, String title, String summary,
                               String action, int order) throws Exception {
        // Duplicate-safe across fragment recreation and OEM flavor switching.
        Constructor<?> ctor = clazz.getConstructor(Context.class);
        Object preference = ctor.newInstance(context);
        clazz.getMethod("setKey", String.class).invoke(preference,key);
        clazz.getMethod("setTitle",CharSequence.class).invoke(preference,title);
        clazz.getMethod("setSummary",CharSequence.class).invoke(preference,summary);
        clazz.getMethod("setOrder",int.class).invoke(preference,order);
        Intent intent=new Intent(action);
        intent.setComponent(new ComponentName(context, ACTIVITY));
        clazz.getMethod("setIntent",Intent.class).invoke(preference,intent);
        // Category already belongs to MainFragment's preference tree.
        category.getClass().getMethod("addPreference",clazz).invoke(category,preference);
    }

    public static void install(Object fragment) {
        try {
            if (fragment == null) return;
            Object contextObj=fragment.getClass().getMethod("getContext").invoke(fragment);
            if (!(contextObj instanceof Context)) return;
            Context context=(Context)contextObj;
            context.getPackageManager().getActivityInfo(
                    new ComponentName(context, ACTIVITY),0);
            Class<?> preferenceClass=Class.forName(PREF_CLASS);
            Method find=fragment.getClass().getMethod("findPreference",CharSequence.class);
            Object category=find.invoke(fragment,"general_settings");
            if (category == null) {
                category=fragment.getClass().getMethod("getPreferenceScreen").invoke(fragment);
            }
            if (category==null) return;
            if (find.invoke(fragment,"projectorsoft_projection") != null) return;

            insert(category,preferenceClass,context,"projectorsoft_projection",
                    "Проекция", "Фокус, трапеция, установка проектора",
                    "android.settings.SETTINGS",200);
            insert(category,preferenceClass,context,"projectorsoft_sources",
                    "Источники", "HDMI, USB, CEC, источник при включении",
                    SOURCE_ACTION,201);
            insert(category,preferenceClass,context,"projectorsoft_picture",
                    "Изображение проектора", "Режим, цвет, контрастность, HDR и MEMC",
                    "android.settings.PICTURE_SETTINGS",202);
            insert(category,preferenceClass,context,"projectorsoft_sound",
                    "Звук проектора", "Звуковые режимы и дополнительные параметры",
                    "android.settings.SOUND_SETTINGS",203);
            insert(category,preferenceClass,context,"projectorsoft_general",
                    "Дополнительно: проектор", "Яркость источника света, питание и язык",
                    GENERAL_ACTION,204);
        } catch (Exception e) {
            android.util.Log.e("TvSettingsPlusMOD","Could not add OEM entries",e);
        }
    }

    public static String actionForSource() {return SOURCE_ACTION;}
    public static String actionForGeneral() {return GENERAL_ACTION;}
}

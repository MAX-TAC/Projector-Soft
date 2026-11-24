package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
    private static final String TAG="TvSettingsPlusMOD";
    private static final String PREF_CLASS = "androidx.preference.Preference";
    private static final String SOURCE_ACTION = "dev.projectorsoft.settings.SOURCES";
    private static final String GENERAL_ACTION = "dev.projectorsoft.settings.GENERAL";
    private OemRootMenuHook() {}

    private static void insert(Object fragment, Method find, Object category, Class<?> clazz, Context context,
                               String key, String groupedKey, String title, String summary,
                               String action, int order) throws Exception {
        Intent intent="projectorsoft_projection".equals(key)
                ? OemProjectionPanel.projectionIntent(context)
                : OemHardwareDelegatedSettings.mainActivityIntent(context,action);
        if(intent==null)return;
        if(groupedKey!=null && offersSameRoute(find.invoke(fragment,groupedKey),clazz,intent)) {
            Object old=find.invoke(fragment,key);
            if(old!=null)category.getClass().getMethod("removePreference",clazz).invoke(category,old);
            return;
        }
        if(find.invoke(fragment,key)!=null)return;
        Constructor<?> ctor = clazz.getConstructor(Context.class);
        Object preference = ctor.newInstance(context);
        clazz.getMethod("setKey", String.class).invoke(preference,key);
        clazz.getMethod("setTitle",CharSequence.class).invoke(preference,title);
        clazz.getMethod("setSummary",CharSequence.class).invoke(preference,summary);
        clazz.getMethod("setOrder",int.class).invoke(preference,order);
        CheckedPreferenceLaunch.attach(context,clazz,preference,intent,()->
                "projectorsoft_projection".equals(key) ? OemProjectionPanel.projectionIntent(context)
                        : OemHardwareDelegatedSettings.mainActivityIntent(context,action));
        // Category already belongs to MainFragment's preference tree.
        category.getClass().getMethod("addPreference",clazz).invoke(category,preference);
    }

    private static boolean offersSameRoute(Object preference,Class<?> type,Intent intent) {
        if(preference==null)return false;
        try {
            return Boolean.TRUE.equals(type.getMethod("isEnabled").invoke(preference))
                    && Boolean.TRUE.equals(type.getMethod("isVisible").invoke(preference))
                    && intent.filterEquals((Intent)type.getMethod("getIntent").invoke(preference));
        }catch(ReflectiveOperationException ex){
            return false;
        }
    }

    public static void install(Object fragment) {
        try {
            if (fragment == null) return;
            // The smali hook can fire from onCreateView, where the register holds a
            // View rather than the fragment. Resolve the real fragment first, or
            // every reflection call below throws and the root menu never appears.
            if (!OemInlinePreferencesHook.looksLikeFragment(fragment)) {
                fragment = OemInlinePreferencesHook.recoverFragment(fragment);
                if (fragment == null) {
                    Log.w(TAG, "root menu: no fragment available");
                    return;
                }
            }
            Object contextObj=fragment.getClass().getMethod("getContext").invoke(fragment);
            if (!(contextObj instanceof Context)) return;
            Context context=(Context)contextObj;
            Class<?> preferenceClass=Class.forName(PREF_CLASS);
            Method find=fragment.getClass().getMethod("findPreference",CharSequence.class);
            Object category=find.invoke(fragment,"general_settings");
            if (category == null) {
                category=OemInlinePreferencesHook.callDeep(fragment,"getPreferenceScreen");
            }
            if (category==null) return;
            boolean grouped = "8054".equals(OemHdmiPortInfo.machineId());
            insert(fragment,find,category,preferenceClass,context,"projectorsoft_projection",
                    grouped?"projectorsoft_calibration_oem":null,
                    "Проекция", "Фокус, трапеция, установка проектора",
                    "android.settings.SETTINGS",200);
            insert(fragment,find,category,preferenceClass,context,"projectorsoft_sources",
                    grouped?"projectorsoft_sources_oem":null,
                    "Источники", "HDMI, USB, CEC, источник при включении",
                    SOURCE_ACTION,201);
            if(!grouped)insert(fragment,find,category,preferenceClass,context,"projectorsoft_picture",null,
                    "Изображение проектора", "Режим, цвет, контрастность, HDR и MEMC",
                    "android.settings.PICTURE_SETTINGS",202);
            insert(fragment,find,category,preferenceClass,context,"projectorsoft_sound",
                    grouped?"projectorsoft_sound_oem":null,
                    "Звук проектора", "Звуковые режимы и дополнительные параметры",
                    "android.settings.SOUND_SETTINGS",203);
            insert(fragment,find,category,preferenceClass,context,"projectorsoft_general",null,
                    "Другие настройки проектора", "Штатные общие настройки",
                    GENERAL_ACTION,204);
        } catch (Exception e) {
            android.util.Log.e("TvSettingsPlusMOD","Could not add OEM entries",e);
        }
    }

    public static String actionForSource() {return SOURCE_ACTION;}
    public static String actionForGeneral() {return GENERAL_ACTION;}
}


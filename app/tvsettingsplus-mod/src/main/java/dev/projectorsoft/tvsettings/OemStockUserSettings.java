package dev.projectorsoft.tvsettings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/** Exposes only argument-free navigation paths found in the exact stock settings APK. */
final class OemStockUserSettings {
    private OemStockUserSettings() {}

    static boolean installAudioOutput(Context context,Object group,Class<?> pref) {
        String target = target("AUDIO_OUTPUT");
        if (target.isEmpty() || !"8054".equals(OemHdmiPortInfo.machineId())) return false;
        try {
            ComponentName component = new ComponentName(context,target);
            PackageManager pm = context.getPackageManager();
            ActivityInfo info = pm.getActivityInfo(component,0);
            if (!info.enabled || info.applicationInfo == null || !info.applicationInfo.enabled
                    || pm.getComponentEnabledSetting(component) > PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    || (info.permission != null && !info.permission.isEmpty()
                        && context.checkSelfPermission(info.permission) != PackageManager.PERMISSION_GRANTED)) return false;
            Object row = row(context,pref,"projectorsoft_audio_output","Аудиовыход",246);
            pref.getMethod("setIntent",Intent.class).invoke(row,new Intent().setComponent(component));
            return add(group,pref,row);
        } catch (Exception | LinkageError ex) {
            Log.w("TvSettingsPlusMOD","Stock audio-output screen unavailable",ex);
            return false;
        }
    }

    static boolean installSwitchOffTimer(Context context,Object group,Class<?> pref) {
        String target = target("TIMER_FRAGMENT");
        if (target.isEmpty() || !"8054".equals(OemHdmiPortInfo.machineId())) return false;
        try {
            Class<?> fragment = Class.forName(target,false,context.getClassLoader());
            if (!Class.forName("androidx.preference.PreferenceFragmentCompat").isAssignableFrom(fragment)) return false;
            fragment.getConstructor();
            Object row = row(context,pref,"projectorsoft_switch_off_timer","Таймер выключения",287);
            pref.getMethod("setFragment",String.class).invoke(row,target);
            return add(group,pref,row);
        } catch (Exception | LinkageError ex) {
            Log.w("TvSettingsPlusMOD","Stock switch-off timer screen unavailable",ex);
            return false;
        }
    }

    private static String target(String field) {
        try {
            return (String)Class.forName("dev.projectorsoft.tvsettings.Exact8054UserRoutes")
                    .getField(field).get(null);
        } catch (ReflectiveOperationException | LinkageError ex) {
            return "";
        }
    }

    private static Object row(Context context,Class<?> pref,String key,String title,int order) throws Exception {
        Object row = pref.getConstructor(Context.class).newInstance(context);
        pref.getMethod("setKey",String.class).invoke(row,key);
        pref.getMethod("setTitle",CharSequence.class).invoke(row,title);
        pref.getMethod("setOrder",int.class).invoke(row,order);
        return row;
    }

    private static boolean add(Object group,Class<?> pref,Object row) throws Exception {
        return Boolean.TRUE.equals(group.getClass().getMethod("addPreference",pref).invoke(group,row));
    }
}

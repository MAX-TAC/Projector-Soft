package dev.projectorsoft.tvsettings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;
import java.lang.reflect.Proxy;

/** Exposes only argument-free navigation paths found in the exact stock settings APK. */
final class OemStockUserSettings {
    private OemStockUserSettings() {}

    static boolean installAudioOutput(Context context,Object group,Class<?> pref) {
        return installActivity(context,group,pref,"AUDIO_OUTPUT","projectorsoft_audio_output","Аудиовыход",246);
    }

    static void installPictureFormat(Context context,Object group,Class<?> pref) {
        try {
            if (pictureFormatIntent(context)==null) return;
            Object item=row(context,pref,"projectorsoft_picture_format","Формат изображения HDMI",229);
            Class<?> listener=Class.forName("androidx.preference.Preference$OnPreferenceClickListener");
            Object callback=Proxy.newProxyInstance(listener.getClassLoader(),new Class<?>[]{listener},
                    (proxy,method,args) -> {
                        switch (method.getName()) {
                            case "onPreferenceClick":
                                try {
                                    Intent intent=pictureFormatIntent(context);
                                    if (intent!=null) {
                                        context.startActivity(intent);
                                        return true;
                                    }
                                } catch (Exception | LinkageError error) {
                                    Log.w("TvSettingsPlusMOD","Stock picture format unavailable",error);
                                }
                                Toast.makeText(context,"Формат изображения для текущего входа недоступен",
                                        Toast.LENGTH_SHORT).show();
                                return true;
                            case "hashCode": return System.identityHashCode(proxy);
                            case "equals": return proxy==args[0];
                            case "toString": return "Stock picture format";
                            default: return null;
                        }
                    });
            pref.getMethod("setOnPreferenceClickListener",listener).invoke(item,callback);
            add(group,pref,item);
        } catch (Exception | LinkageError error) {
            Log.w("TvSettingsPlusMOD","Stock picture format unavailable",error);
        }
    }

    private static Intent pictureFormatIntent(Context context) throws Exception {
        String machine=OemHdmiPortInfo.machineId();
        String route=target("PICTURE_FORMAT");
        if (!"8054".equals(machine) || route.isEmpty()) return null;
        Intent intent=stockActivityIntent(context,route);
        if (intent==null) return null;
        Class<?> utils=Class.forName(
                "com.android.tv.settings.partnercustomizer.tvsettingservice.util.PreferenceConfigUtils");
        if (!Boolean.TRUE.equals(utils.getMethod("isHDMISource",Context.class).invoke(null,context))) {
            return null;
        }
        Object screen=utils.getMethod("getInstance",Context.class).invoke(null,context);
        Object supported=utils.getMethod("getSupportScreenModes").invoke(screen);
        if (!(supported instanceof int[])) return null;
        Class<?> configClass=Class.forName("com.android.tv.settings.partnercustomizer.tvsettingservice.TVSettingConfig");
        Object config=configClass.getMethod("getInstance",Context.class).invoke(null,context);
        Object current=configClass.getMethod("getConfigValueInt",String.class)
                .invoke(config,"g_fusion_picture__pictrue_format");
        Class<?> exact=Class.forName("dev.projectorsoft.tvsettings.Exact8054UserRoutes");
        String[] labels=context.getResources().getStringArray(exact.getField("PICTURE_FORMAT_LABELS").getInt(null));
        String[] values=context.getResources().getStringArray(exact.getField("PICTURE_FORMAT_VALUES").getInt(null));
        return PictureFormat8054Policy.canOpen(machine,true,(int[])supported,labels,values,current)
                ? intent : null;
    }

    private static boolean installActivity(Context context,Object group,Class<?> pref,
                                           String route,String key,String title,int order) {
        String target=target(route);
        if (target.isEmpty() || !"8054".equals(OemHdmiPortInfo.machineId())) return false;
        try {
            Intent intent=stockActivityIntent(context,target);
            if (intent==null) return false;
            Object row=row(context,pref,key,title,order);
            pref.getMethod("setIntent",Intent.class).invoke(row,intent);
            return add(group,pref,row);
        } catch (Exception | LinkageError ex) {
            Log.w("TvSettingsPlusMOD","Stock screen unavailable: "+route,ex);
            return false;
        }
    }

    private static Intent stockActivityIntent(Context context,String target) throws PackageManager.NameNotFoundException {
        ComponentName component=new ComponentName(context,target);
        PackageManager pm=context.getPackageManager();
        ActivityInfo info=pm.getActivityInfo(component,0);
        if (!context.getPackageName().equals(info.packageName)
                || !info.enabled || info.applicationInfo==null || !info.applicationInfo.enabled
                || pm.getApplicationEnabledSetting(info.packageName)>PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                || pm.getComponentEnabledSetting(component)>PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                || (info.permission!=null && !info.permission.isEmpty()
                    && context.checkSelfPermission(info.permission)!=PackageManager.PERMISSION_GRANTED)) return null;
        return new Intent().setComponent(component);
    }

    static boolean installSwitchOffTimer(Context context,Object group,Class<?> pref) {
        if (installActivity(context,group,pref,"TIMER_ACTIVITY","projectorsoft_switch_off_timer",
                "Таймер выключения",287)) return true;
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

package dev.projectorsoft.tvsettings;
import android.content.*;
import android.content.pm.*;
import android.util.Log;

/** Checked OEM navigation, without invoking focus/motor/keystone/LED APIs. */
public final class OemProjectionPanel {
    private OemProjectionPanel() {}
    public static void install(Context context,Object category,Class<?> pref) {
        try {
            Intent intent=projectionIntent(context);
            if(intent!=null)addRow(context,category,pref,"projectorsoft_calibration_oem",
                    "Штатные настройки проектора","Положение, фокус и коррекция",intent,251);
        }catch(Exception ex){Log.w("TvSettingsPlusMOD","OEM projection menu unavailable",ex);}
        if ("8054".equals(OemHdmiPortInfo.machineId())) {
            // Exact stock KeystoneSettingsSecondaryController.gotoKeystoneCorrection intent.
            add(context,category,pref,"keystone","Коррекция трапеции · штатный экран",null,
                "com.android.tv.settings",
                "com.android.tv.settings.uicustom.fragment.projector.KeystoneCalibrationActivity",null,256);
            OemLightSourcePreference.install(context,category,pref);
            OemFocusPreferences.install(context,category,pref);
            OemMoveKeystonePreference.install(context,category,pref);
        } else {
            add(context,category,pref,"zoom","Цифровой зум · OEM",null,
                "com.rjpj.tcorrection","com.rjpj.tcorrection.DigitalZoomActivity",Intent.ACTION_MAIN,252);
            add(context,category,pref,"light","Яркость источника света · OEM",
                "Оригинальный экран с OEM ограничениями охлаждения",
                "com.coolux.source","com.coolux.source.activity.BrightActivity",Intent.ACTION_MAIN,253);
        }
    }

    static Intent projectionIntent(Context context) throws PackageManager.NameNotFoundException {
        if("8054".equals(OemHdmiPortInfo.machineId())) {
            String owner="com.my.settings";
            String name="com.my.settings.activity.ProjectionActivity";
            Intent route=new Intent("android.settings.PROJECTABOUT_SETTINGS").setPackage(owner);
            PackageManager pm=context.getPackageManager();
            ResolveInfo resolved=pm.resolveActivity(route,PackageManager.MATCH_DEFAULT_ONLY);
            ActivityInfo info=resolved==null ? null : resolved.activityInfo;
            if(info!=null && owner.equals(info.packageName) && name.equals(info.name)
                    && info.applicationInfo!=null
                    && (info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)!=0) {
                ComponentName component=new ComponentName(owner,name);
                if(accessible(context,pm,component,info))return route.setComponent(component);
            }
        }
        return OemHardwareDelegatedSettings.mainActivityIntent(context,"android.settings.SETTINGS");
    }

    private static boolean accessible(Context context,PackageManager pm,ComponentName component,ActivityInfo info) {
        boolean permission=info.permission==null || info.permission.isEmpty()
                || context.checkSelfPermission(info.permission)==PackageManager.PERMISSION_GRANTED;
        boolean owner=info.applicationInfo!=null && info.applicationInfo.uid==context.getApplicationInfo().uid;
        return Projection8054Policy.mayNavigate(
                info.enabled && pm.getComponentEnabledSetting(component)<=PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                info.applicationInfo!=null && info.applicationInfo.enabled
                    && pm.getApplicationEnabledSetting(component.getPackageName())<=PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                owner,info.exported,permission);
    }
    private static void add(Context context,Object category,Class<?> pref,String key,String title,String summary,
                            String pkg,String activity,String action,int order) {
        try {
            ComponentName component=new ComponentName(pkg,activity);
            PackageManager pm=context.getPackageManager();
            if(!accessible(context,pm,component,pm.getActivityInfo(component,0)))return;
            addRow(context,category,pref,"projectorsoft_projection_"+key,title,summary,
                    new Intent(action).setComponent(component),order);
        }catch(Exception ex){Log.w("TvSettingsPlusMOD","OEM projection entry unavailable "+key,ex);}
    }

    private static void addRow(Context context,Object category,Class<?> pref,String key,String title,
                               String summary,Intent intent,int order) throws Exception {
        Object item=pref.getConstructor(Context.class).newInstance(context);
        pref.getMethod("setKey",String.class).invoke(item,key);
        pref.getMethod("setTitle",CharSequence.class).invoke(item,title);
        pref.getMethod("setSummary",CharSequence.class).invoke(item,summary);
        pref.getMethod("setOrder",int.class).invoke(item,order);
        pref.getMethod("setIntent",Intent.class).invoke(item,intent);
        category.getClass().getMethod("addPreference",pref).invoke(category,item);
    }
}

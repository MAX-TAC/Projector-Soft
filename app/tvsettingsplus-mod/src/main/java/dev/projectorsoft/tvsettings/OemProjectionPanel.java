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
            installUnified(context,category,pref);
            // Exact stock KeystoneSettingsSecondaryController.gotoKeystoneCorrection intent.
            add(context,category,pref,"keystone","Коррекция трапеции · штатный экран",null,
                "com.android.tv.settings",
                "com.android.tv.settings.uicustom.fragment.projector.KeystoneCalibrationActivity",null,256);
        } else {
            add(context,category,pref,"zoom","Цифровой зум · OEM",null,
                "com.rjpj.tcorrection","com.rjpj.tcorrection.DigitalZoomActivity",Intent.ACTION_MAIN,252);
            add(context,category,pref,"light","Яркость источника света · OEM",
                "Оригинальный экран с OEM ограничениями охлаждения",
                "com.coolux.source","com.coolux.source.activity.BrightActivity",Intent.ACTION_MAIN,253);
        }
    }

    static void installUnified(Context context,Object category,Class<?> pref) {
        if (!"8054".equals(OemHdmiPortInfo.machineId()))return;
        addEditor(context,category,pref,"zoom","Цифровой зум",Projection8054Policy.DIGITAL_ZOOM,252);
        addEditor(context,category,pref,"four_corner","Коррекция четырёх углов",
                Projection8054Policy.FOUR_CORNER,254);
        addEditor(context,category,pref,"hv_keystone","Горизонтальная и вертикальная коррекция",
                Projection8054Policy.HORIZONTAL_VERTICAL,255);
        OemLightSourcePreference.install(context,category,pref);
        OemFocusPreferences.install(context,category,pref);
        OemMoveKeystonePreference.install(context,category,pref);
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

    private static Intent editorIntent(Context context,String action) {
        try {
            String machine=OemHdmiPortInfo.machineId();
            if (!"8054".equals(machine)) return null;
            String owner=Projection8054Policy.CORRECTION_PACKAGE;
            // The stock direct launchers use the action and NEW_TASK without extras.
            Intent intent=new Intent(action).setPackage(owner).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PackageManager pm=context.getPackageManager();
            ResolveInfo resolved=pm.resolveActivity(intent,PackageManager.MATCH_DEFAULT_ONLY);
            ActivityInfo info=resolved==null ? null : resolved.activityInfo;
            if (info==null || info.applicationInfo==null || info.packageName==null || info.name==null)
                return null;
            ComponentName component=new ComponentName(info.packageName,info.name);
            boolean permission=info.permission==null || info.permission.isEmpty()
                    || context.checkSelfPermission(info.permission)==PackageManager.PERMISSION_GRANTED;
            if (!Projection8054Policy.mayOpenStockEditor(machine,action,intent.getExtras()!=null,info.packageName,info.name,
                    (info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)!=0,
                    info.enabled && pm.getComponentEnabledSetting(component)<=PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    info.applicationInfo.enabled && pm.getApplicationEnabledSetting(owner)<=PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    info.applicationInfo.uid==context.getApplicationInfo().uid,info.exported,permission))
                return null;
            return intent.setComponent(component);
        } catch (Exception | LinkageError ex) {
            Log.w("TvSettingsPlusMOD","Stock projection editor unavailable: "+action,ex);
            return null;
        }
    }

    private static void addEditor(Context context,Object category,Class<?> pref,String key,String title,
                                  String action,int order) {
        if (editorIntent(context,action)==null) return;
        try {
            Object item=pref.getConstructor(Context.class).newInstance(context);
            pref.getMethod("setKey",String.class).invoke(item,"projectorsoft_projection_"+key);
            pref.getMethod("setTitle",CharSequence.class).invoke(item,title);
            pref.getMethod("setOrder",int.class).invoke(item,order);
            Class<?> listener=Class.forName("androidx.preference.Preference$OnPreferenceClickListener");
            Object callback=java.lang.reflect.Proxy.newProxyInstance(listener.getClassLoader(),
                    new Class<?>[]{listener},(proxy,method,args)-> {
                        switch(method.getName()){
                            case "onPreferenceClick":
                                Intent intent=editorIntent(context,action);
                                if (intent!=null) {
                                    try {context.startActivity(intent);return true;}
                                    catch (RuntimeException error) {
                                        Log.w("TvSettingsPlusMOD","Cannot open "+action,error);
                                    }
                                }
                                android.widget.Toast.makeText(context,"Штатный редактор недоступен",
                                        android.widget.Toast.LENGTH_SHORT).show();
                                return true;
                            case "hashCode":return System.identityHashCode(proxy);
                            case "equals":return args!=null && args.length==1 && proxy==args[0];
                            case "toString":return action;
                            default:return null;
                        }
                    });
            pref.getMethod("setOnPreferenceClickListener",listener).invoke(item,callback);
            category.getClass().getMethod("addPreference",pref).invoke(category,item);
        } catch (Exception | LinkageError error) {
            Log.w("TvSettingsPlusMOD","Projection row unavailable: "+action,error);
        }
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
        CheckedPreferenceLaunch.attach(context,pref,item,intent,()->intent);
        category.getClass().getMethod("addPreference",pref).invoke(category,item);
    }
}

package dev.projectorsoft.tvsettings;
import android.content.*;
import android.content.pm.*;
import android.util.Log;

/** Checked OEM navigation, without invoking focus/motor/keystone/LED APIs. */
public final class OemProjectionPanel {
    private OemProjectionPanel() {}
    public static void install(Context context,Object category,Class<?> pref) {
        add(context,category,pref,"settings","Установка, фокус и коррекция · OEM",
            "Штатный раздел проекции; доступные функции определяет OEM",
            "com.android.tv.settings","com.android.tv.settings.uicustom.MainActivity","android.settings.SETTINGS",251);
        add(context,category,pref,"zoom","Цифровой зум · OEM",
            "Масштаб изображения; это не яркость LED и не управление мотором",
            "com.rjpj.tcorrection","com.rjpj.tcorrection.DigitalZoomActivity",Intent.ACTION_MAIN,252);
        add(context,category,pref,"light","Яркость источника света · OEM",
            "Оригинальный экран с OEM ограничениями охлаждения",
            "com.coolux.source","com.coolux.source.activity.BrightActivity",Intent.ACTION_MAIN,253);
    }
    private static void add(Context context,Object category,Class<?> pref,String key,String title,String summary,
                            String pkg,String activity,String action,int order) {
        try {
            ComponentName component=new ComponentName(pkg,activity);
            ActivityInfo info=context.getPackageManager().getActivityInfo(component,0);
            int componentState=context.getPackageManager().getComponentEnabledSetting(component);
            int appState=context.getPackageManager().getApplicationEnabledSetting(pkg);
            boolean permission=info.permission==null||info.permission.isEmpty()
                ||context.checkSelfPermission(info.permission)==PackageManager.PERMISSION_GRANTED;
            if(!Projection8054Policy.mayNavigate(info.enabled&&componentState<=PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    info.applicationInfo!=null&&info.applicationInfo.enabled&&appState<=PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    context.getPackageName().equals(pkg),info.exported,permission))return;
            Object item=pref.getConstructor(Context.class).newInstance(context);
            pref.getMethod("setKey",String.class).invoke(item,"projectorsoft_projection_"+key);
            pref.getMethod("setTitle",CharSequence.class).invoke(item,title);
            pref.getMethod("setSummary",CharSequence.class).invoke(item,summary);
            pref.getMethod("setOrder",int.class).invoke(item,order);
            pref.getMethod("setIntent",Intent.class).invoke(item,new Intent(action).setComponent(component));
            category.getClass().getMethod("addPreference",pref).invoke(category,item);
        }catch(Exception ex){Log.w("TvSettingsPlusMOD","OEM projection entry unavailable "+key,ex);}
    }
}

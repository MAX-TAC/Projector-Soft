package dev.projectorsoft.tvsettings;
import android.content.*;
import android.content.pm.*;
import android.provider.Settings;
import android.util.Log;

/** Checked platform Intents. The original screen owns all setting changes. */
public final class OemStandardAndroidNavigation {
    private OemStandardAndroidNavigation() {}
    public static boolean install(Context context,Object category,Class<?> pref) {
        String[] actions={Settings.ACTION_WIFI_SETTINGS,Settings.ACTION_LOCALE_SETTINGS,
            Settings.ACTION_DATE_SETTINGS,Settings.ACTION_APPLICATION_SETTINGS};
        String[] titles={"Wi-Fi","Язык","Дата, время и часовой пояс","Приложения"};
        boolean inserted=false;
        for(int i=0;i<actions.length;i++) {
            Intent target=resolve(context,actions[i]);
            if(target==null)continue;
            try {
                Object item=pref.getConstructor(Context.class).newInstance(context);
                pref.getMethod("setKey",String.class).invoke(item,"projectorsoft_android_"+i);
                pref.getMethod("setTitle",CharSequence.class).invoke(item,titles[i]);
                pref.getMethod("setSummary",CharSequence.class).invoke(item,"Открыть системный экран Android TV");
                pref.getMethod("setOrder",int.class).invoke(item,290+i);
                pref.getMethod("setIntent",Intent.class).invoke(item,target);
                inserted=Boolean.TRUE.equals(category.getClass().getMethod("addPreference",pref).invoke(category,item))||inserted;
            }catch(Exception ex){Log.w("TvSettingsPlusMOD","System navigation entry unavailable",ex);}
        }
        return inserted;
    }
    private static Intent resolve(Context context,String action) {
        PackageManager manager=context.getPackageManager();
        for(String owner:new String[]{"com.android.tv.settings","com.android.settings"}) {
            try {
                Intent intent=new Intent(action).setPackage(owner);
                ResolveInfo resolved=manager.resolveActivity(intent,PackageManager.MATCH_DEFAULT_ONLY);
                if(resolved==null||resolved.activityInfo==null)continue;
                ActivityInfo info=resolved.activityInfo;
                if(!owner.equals(info.packageName)||info.name==null||info.applicationInfo==null)continue;
                ComponentName component=new ComponentName(info.packageName,info.name);
                boolean permission=info.permission==null||info.permission.isEmpty()
                    ||context.checkSelfPermission(info.permission)==PackageManager.PERMISSION_GRANTED;
                boolean system=(info.applicationInfo.flags&(ApplicationInfo.FLAG_SYSTEM|ApplicationInfo.FLAG_UPDATED_SYSTEM_APP))!=0;
                if(StandardAndroidNavigationPolicy.mayOpen(action,owner,system,
                        info.enabled&&manager.getComponentEnabledSetting(component)<=PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        info.applicationInfo.enabled&&manager.getApplicationEnabledSetting(owner)<=PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        context.getPackageName().equals(owner),info.exported,permission))return intent.setComponent(component);
            }catch(Exception ex){Log.w("TvSettingsPlusMOD","Platform settings handler unavailable: "+action,ex);}
        }
        return null;
    }
}

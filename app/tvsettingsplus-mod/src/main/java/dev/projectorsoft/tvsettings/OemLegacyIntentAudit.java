package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import java.util.List;

/**
 * Real Android package-manager audit for nine legacy implicit KuSettings
 * actions. It does not change preferred activities, enable/disable OEM
 * packages, or claim to intercept explicit ComponentName calls.
 */
public final class OemLegacyIntentAudit {
    private static final String TAG="TvSettingsPlusMOD";
    private static final String[] ACTIONS={
        "android.settings.SETTINGS",
        "android.settings.WIFI_SETTINGS",
        "android.settings.BLUETOOTH_SETTINGS",
        "android.settings.AUTO_BLUETOOTH",
        "android.settings.LAUGUAGE_SETTINGS",
        "android.settings.LAUGUAGE_LOCAL_SETTINGS",
        "android.settings.DATETIME_SETTINGS",
        "android.settings.ALLAPPS",
        "android.settings.PROJECTABOUT_SETTINGS"
    };
    private OemLegacyIntentAudit(){}

    @SuppressWarnings("deprecation")
    public static String collect(Context context){
        if(context==null)return "Проверка legacy маршрутов недоступна";
        StringBuilder report=new StringBuilder("LEGACY INTENT — KuSettings → TvSettingsPlus\n");
        try {
            PackageManager pm=context.getPackageManager();
            int confirmed=0;
            for(String action:ACTIONS){
                Intent request=new Intent(action);
                request.addCategory(Intent.CATEGORY_DEFAULT);
                List<ResolveInfo> handlers=pm.queryIntentActivities(
                    request,PackageManager.MATCH_DEFAULT_ONLY);
                boolean mod=false,ku=false;
                if(handlers!=null){
                    for(ResolveInfo item:handlers){
                        if(item==null||item.activityInfo==null)continue;
                        String packageName=item.activityInfo.packageName;
                        if(OemLegacyIntentPolicy.MOD.equals(packageName))mod=true;
                        if(OemLegacyIntentPolicy.KUSETTINGS.equals(packageName))ku=true;
                    }
                }
                ResolveInfo resolved=pm.resolveActivity(
                    request,PackageManager.MATCH_DEFAULT_ONLY);
                String preferred=resolved!=null && resolved.activityInfo!=null
                    ? resolved.activityInfo.packageName:null;
                OemLegacyIntentPolicy.State state=OemLegacyIntentPolicy.evaluate(
                    mod,ku,preferred);
                if(OemLegacyIntentPolicy.confirmedWithoutOldPackage(state))confirmed++;
                String shortAction=action.substring(action.lastIndexOf('.')+1);
                report.append(shortAction).append(": ")
                    .append(OemLegacyIntentPolicy.describe(state)).append("\n");
            }
            report.append("Подтверждено: ").append(confirmed)
                .append("/").append(ACTIONS.length).append(" системных переходов\n");
        }catch(Exception | LinkageError e){
            Log.w(TAG,"Cannot inspect legacy implicit intents",e);
            report.append("Android ограничил диагностику Intents. Достоверного вывода нет.\n");
        }
        report.append("Явные ссылки на com.my.settings / settingApps не перехватываются.");
        return report.toString();
    }
}

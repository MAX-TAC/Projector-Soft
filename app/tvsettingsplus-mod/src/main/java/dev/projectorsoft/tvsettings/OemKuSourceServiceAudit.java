package dev.projectorsoft.tvsettings;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;
import java.util.List;

/** Read-only observation: never start or stop fan/thermal services. */
public final class OemKuSourceServiceAudit {
    private static final String TAG="TvSettingsPlusMOD";
    private static final String PKG="com.coolux.source";
    private static final String SERVICE="com.coolux.source.service.TimeService";
    private static final String LEGACY_MONITOR="com.my.service.MySetTimeService";
    private OemKuSourceServiceAudit(){}

    @SuppressWarnings("deprecation")
    public static String inspect(Context context) {
        if(context==null)return OemKuSourceServicePolicy.describe(
                OemKuSourceServicePolicy.State.NOT_VERIFIABLE);
        try {
            ActivityManager manager=(ActivityManager)context.getSystemService(
                    Context.ACTIVITY_SERVICE);
            if(manager==null)return OemKuSourceServicePolicy.describe(
                    OemKuSourceServicePolicy.State.NOT_VERIFIABLE);
            List<ActivityManager.RunningServiceInfo> services=manager.getRunningServices(1000);
            boolean snapshotAvailable=services!=null && !services.isEmpty();
            boolean target=false,sourceVisible=false,monitorVisible=false;
            if(services!=null){
                for(ActivityManager.RunningServiceInfo item:services) {
                    if(item==null || item.service==null)continue;
                    String pkg=item.service.getPackageName();
                    String cls=item.service.getClassName();
                    if(PKG.equals(pkg)){
                        sourceVisible=true;
                        if(SERVICE.equals(cls))target=true;
                    }
                    if("com.my.settings".equals(pkg)&&LEGACY_MONITOR.equals(cls))
                        monitorVisible=true;
                }
            }
            OemKuSourceServicePolicy.State state=OemKuSourceServicePolicy.evaluate(
                snapshotAvailable,sourceVisible,target);
            String kuStatus=monitorVisible
                ? "KuSettings.MySetTimeService видна (заводской watchdog)"
                : "KuSettings.MySetTimeService не видна; отсутствие НЕ доказано";
            return OemKuSourceServicePolicy.describe(state)
                +"\n"+kuStatus+"\nСтатус RPM и тепловой защиты этим перечнем не подтверждается.";
        }catch(Exception e){
            Log.w(TAG,"Android blocked read-only OEM running-services inspection",e);
            return OemKuSourceServicePolicy.describe(
                OemKuSourceServicePolicy.State.NOT_VERIFIABLE);
        }
    }
}

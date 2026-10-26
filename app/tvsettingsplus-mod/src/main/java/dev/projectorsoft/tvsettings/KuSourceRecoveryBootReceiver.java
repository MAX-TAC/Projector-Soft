package dev.projectorsoft.tvsettings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Starts MOD source-safety recovery on BOOT_COMPLETED only when KuSettings,
 * the original owner of the monitor, was already deliberately disabled.
 * Does not disable packages, affect fan output or alter projector hardware.
 */
public final class KuSourceRecoveryBootReceiver extends BroadcastReceiver {
    private static final String TAG="TvSettingsPlusMOD";
    private static final String LEGACY="com.my.settings";

    static boolean needsReplacement(Context context) {
        try {
            PackageManager pm=context.getPackageManager();
            int explicit=pm.getApplicationEnabledSetting(LEGACY);
            if (explicit==PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    || explicit==PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                    || explicit==PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED)
                return true;
            ApplicationInfo app=pm.getApplicationInfo(LEGACY,0);
            return !app.enabled;
        }catch(PackageManager.NameNotFoundException e){
            return true;
        }catch(Exception e){
            Log.w(TAG,"Cannot determine legacy watchdog state; leaving OEM in control",e);
            return false;
        }
    }

    @Override public void onReceive(Context context,Intent intent) {
        if(intent==null || !Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()))return;
        if(!needsReplacement(context))return;
        try {
            Intent svc=new Intent(context,KuSourceRecoveryService.class);
            context.startService(svc);
        }catch(Exception e){
            Log.e(TAG,"Could not start replacement source watchdog; do not retire KuSettings",e);
        }
    }
}

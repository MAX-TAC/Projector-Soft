package dev.projectorsoft.tvsettings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import java.lang.reflect.Method;

/**
 * Starts MOD source-safety recovery on BOOT_COMPLETED only when KuSettings,
 * the original owner of the monitor, was already deliberately disabled.
 * Does not disable packages, affect fan output or alter projector hardware.
 */
public final class KuSourceRecoveryBootReceiver extends BroadcastReceiver {
    private static final String TAG="TvSettingsPlusMOD";
    private static final String LEGACY="com.my.settings";

    private static String machineId() {
        try {
            Class<?> properties=Class.forName("android.os.SystemProperties");
            Method get=properties.getMethod("get",String.class,String.class);
            Object value=get.invoke(null,"ro.rjpj.machine.id","");
            return value instanceof String ? (String)value : "";
        }catch(Exception e) {
            return "";
        }
    }

    /**
     * A missing/hidden PackageManager entry is NOT proof of package disablement.
     * Android 11+ app-visibility filtering can otherwise produce a false
     * NameNotFoundException and start a competing thermal-service monitor.
     */
    static boolean needsReplacement(Context context) {
        try {
            PackageManager pm=context.getPackageManager();
            int state=pm.getApplicationEnabledSetting(LEGACY);
            boolean allowed=KuSourceRecoveryActivationPolicy.automaticRecoveryAllowed(
                    machineId(),state);
            if(!allowed && KuSourceRecoveryActivationPolicy.explicitlyDisabled(state))
                Log.w(TAG,"Automatic source recovery unavailable on unvalidated board; restore KuSettings");
            return allowed;
        }catch(IllegalArgumentException e){
            Log.w(TAG,"Legacy KuSettings not queryable; replacement disabled",e);
            return false;
        }catch(Exception e){
            Log.w(TAG,"KuSettings state not verifiable; original watchdog assumed",e);
            return false;
        }
    }

    @Override public void onReceive(Context context,Intent intent) {
        if(intent==null || !Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()))return;
        // Always enforce no automatic screen blank/sleep at boot, irrespective
        // of whether KuSettings is enabled. Recovery service remains guarded.
        OemScreenTimeoutSafetyHook.ensureNever(context);
        if(!needsReplacement(context))return;
        try {
            Intent svc=new Intent(context,KuSourceRecoveryService.class);
            context.startService(svc);
        }catch(Exception e){
            Log.e(TAG,"Could not start replacement source watchdog; do not retire KuSettings",e);
        }
    }
}


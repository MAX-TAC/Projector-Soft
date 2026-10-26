package dev.projectorsoft.tvsettings;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import java.util.List;

/**
 * Compatibility replacement for KuSettings.MySetTimeService, not a replacement
 * for the thermal protections inside KuSourceRJ.TimeService.
 *
 * Starts ONLY if com.my.settings was disabled by an authorized user before
 * BOOT_COMPLETED, so it cannot race with the original KuSettings watchdog.
 * Restarts OEM TimeService only if a system-level running-services snapshot
 * can see unrelated processes, allowing a meaningful absence check.
 *
 * Requires device-level testing. Keep KuSettings installed as rollback.
 */
public final class KuSourceRecoveryService extends Service {
    private static final String TAG="TvSettingsPlusMOD";
    private static final String TARGET_PACKAGE="com.coolux.source";
    private static final String TARGET_SERVICE="com.coolux.source.service.TimeService";
    private static final long INITIAL_DELAY_MS=90000L;
    private static final long MIN_RESTART_INTERVAL_MS=60000L;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private long lastAttemptUptime=Long.MIN_VALUE;
    private boolean registered=false;
    private final BroadcastReceiver tick=new BroadcastReceiver() {
        @Override public void onReceive(Context context,Intent intent) {
            if(intent!=null && Intent.ACTION_TIME_TICK.equals(intent.getAction()))
                verifyAndRecover();
        }
    };
    private final Runnable initialCheck=new Runnable() {
        @Override public void run() {
            verifyAndRecover();
        }
    };

    @Override public IBinder onBind(Intent intent) { return null; }

    @Override public int onStartCommand(Intent intent,int flags,int startId) {
        if(!KuSourceRecoveryBootReceiver.needsReplacement(this)) {
            // Existing OEM KuSettings maintains this already. Do not duplicate.
            stopSelf(startId);
            return START_NOT_STICKY;
        }
        if(!registered) {
            registerReceiver(tick,new IntentFilter(Intent.ACTION_TIME_TICK));
            registered=true;
            handler.postDelayed(initialCheck,INITIAL_DELAY_MS);
        }
        return START_STICKY;
    }

    @SuppressWarnings("deprecation")
    private void verifyAndRecover() {
        if(!KuSourceRecoveryBootReceiver.needsReplacement(this)) {
            stopSelf();
            return;
        }
        try {
            ActivityManager am=(ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
            if(am==null)return;
            List<ActivityManager.RunningServiceInfo> services=am.getRunningServices(1000);
            if(services==null || services.isEmpty()){
                Log.w(TAG,"No reliable running services snapshot; recovery skipped");
                return;
            }
            boolean sawOtherPackages=false;
            for(ActivityManager.RunningServiceInfo current:services) {
                if(current.service==null)continue;
                if(TARGET_SERVICE.equals(current.service.getClassName()))return;
                if(!getPackageName().equals(current.service.getPackageName()))
                    sawOtherPackages=true;
            }
            if(!sawOtherPackages) {
                // Android 8+ may restrict service enumeration to own package.
                // Do not blindly restart KuSourceRJ every minute.
                Log.w(TAG,"OS filters external running services; cannot verify health");
                return;
            }
            long now=android.os.SystemClock.uptimeMillis();
            if(lastAttemptUptime!=Long.MIN_VALUE &&
                    now-lastAttemptUptime < MIN_RESTART_INTERVAL_MS)return;
            lastAttemptUptime=now;
            Intent target=new Intent();
            target.setComponent(new ComponentName(TARGET_PACKAGE,TARGET_SERVICE));
            ComponentName started=startService(target);
            if(started==null)
                Log.e(TAG,"OEM KuSourceRJ.TimeService could not be restarted");
            else
                Log.w(TAG,"Started missing OEM KuSourceRJ.TimeService using original component");
        }catch(Exception e) {
            Log.e(TAG,"Source recovery blocked, keep original KuSettings until verified",e);
        }
    }

    @Override public void onDestroy() {
        handler.removeCallbacks(initialCheck);
        if(registered){
            try{unregisterReceiver(tick);}catch(Exception ignored){}
            registered=false;
        }
        super.onDestroy();
    }
}

package dev.projectorsoft.tvsettings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

/**
 * Cross-package navigation without the OEM's uicustom hijack.
 *
 * The stock firmware rewrote standard Android settings actions so they resolve
 * to com.android.tv.settings.uicustom.MainActivity, i.e. the Chinese settings
 * app. Everything therefore landed in that menu no matter which entry was
 * pressed. This helper never references uicustom: it looks the target activity
 * up by explicit component and fails closed when the package is absent.
 *
 * No hard side effects live here (no CEC confirmation, no keystone reset);
 * it only starts activities that already exist on the device.
 */
public final class OemCrossActivity {
    private static final String TAG = "TvSettingsPlusMOD";

    private OemCrossActivity() {}

    /** Explicit component intent, or null when the target is missing/disabled. */
    public static Intent intentFor(Context context, String pkg, String activity) {
        if (context == null || pkg == null || activity == null) return null;
        try {
            ComponentName component = new ComponentName(pkg, activity);
            PackageManager pm = context.getPackageManager();
            ActivityInfo info = pm.getActivityInfo(component, 0);
            if (info == null || !info.enabled) return null;
            if (info.applicationInfo == null || !info.applicationInfo.enabled) return null;
            if (pm.getComponentEnabledSetting(component)
                    > PackageManager.COMPONENT_ENABLED_STATE_ENABLED) return null;
            if (pm.getApplicationEnabledSetting(pkg)
                    > PackageManager.COMPONENT_ENABLED_STATE_ENABLED) return null;
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setComponent(component);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return intent;
        } catch (Exception e) {
            Log.d(TAG, "cross activity unavailable: " + pkg + "/" + activity);
            return null;
        }
    }

    /** True when the package is installed and its launcher entry is resolvable. */
    public static boolean hasLauncher(Context context, String pkg) {
        if (context == null || pkg == null) return false;
        try {
            Intent probe = new Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_LAUNCHER).setPackage(pkg);
            ResolveInfo info = context.getPackageManager()
                    .resolveActivity(probe, PackageManager.MATCH_DEFAULT_ONLY);
            return info != null && info.activityInfo != null;
        } catch (Exception e) {
            return false;
        }
    }
}
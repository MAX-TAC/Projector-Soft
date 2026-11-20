package dev.projectorsoft.tvsettings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Re-resolve the current user's HOME handler instead of using a hard-coded launcher. */
final class HomeDestination {
    private HomeDestination() {}

    static Intent resolve(Context context) throws PackageManager.NameNotFoundException {
        PackageManager pm = context.getPackageManager();
        Intent home = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        List<ResolveInfo> results = pm.queryIntentActivities(home, PackageManager.MATCH_DEFAULT_ONLY);
        Set<ComponentName> verified = new LinkedHashSet<>();
        if (results != null) for (ResolveInfo result : results) {
            ComponentName component = candidate(context.getPackageName(), result);
            if (component == null) continue;
            try {
                if (CheckedPreferenceLaunch.accessible(context, component)) verified.add(component);
            } catch (PackageManager.NameNotFoundException removed) {
                // A disappearing candidate must not become a launcher fallback.
            }
        }
        ComponentName preferred = candidate(context.getPackageName(),
                pm.resolveActivity(home, PackageManager.MATCH_DEFAULT_ONLY));
        ComponentName target = select(preferred, verified);
        return target == null ? null : home.setComponent(target);
    }

    private static ComponentName candidate(String ownPackage, ResolveInfo info) {
        ActivityInfo activity = info == null ? null : info.activityInfo;
        if (activity == null || activity.applicationInfo == null || activity.name == null
                || activity.packageName == null || !activity.enabled || !activity.applicationInfo.enabled
                || !activity.exported || "android".equals(activity.packageName)
                || "com.my.settings".equals(activity.packageName) || ownPackage.equals(activity.packageName)) return null;
        return new ComponentName(activity.packageName, activity.name);
    }

    static ComponentName select(ComponentName preferred, Set<ComponentName> verified) {
        if (preferred != null && verified.contains(preferred)) return preferred;
        return verified.size() == 1 ? verified.iterator().next() : null;
    }
}

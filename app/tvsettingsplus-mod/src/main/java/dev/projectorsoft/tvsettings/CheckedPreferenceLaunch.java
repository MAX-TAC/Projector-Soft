package dev.projectorsoft.tvsettings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;
import java.lang.reflect.Proxy;

/** Consumes Preference clicks so an inaccessible explicit Activity cannot escape to AndroidX. */
final class CheckedPreferenceLaunch {
    interface Route { Intent resolve() throws Exception; }

    private CheckedPreferenceLaunch() {}

    static void attach(Context context, Class<?> pref, Object row, Intent initial, Route route)
            throws ReflectiveOperationException {
        Class<?> listener = Class.forName("androidx.preference.Preference$OnPreferenceClickListener");
        Object callback = Proxy.newProxyInstance(listener.getClassLoader(), new Class<?>[]{listener},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "onPreferenceClick":
                            if (!open(context, route)) {
                                Toast.makeText(context, "Экран настройки недоступен", Toast.LENGTH_SHORT).show();
                            }
                            return true;
                        case "toString": return "CheckedSettingsLaunch";
                        case "hashCode": return System.identityHashCode(proxy);
                        case "equals": return args != null && args.length == 1 && proxy == args[0];
                        default: return null;
                    }
                });
        pref.getMethod("setOnPreferenceClickListener", listener).invoke(row, callback);
        pref.getMethod("setIntent", Intent.class).invoke(row, initial);
    }

    static boolean open(Context context, Route route) {
        try {
            Intent intent = route.resolve();
            if (intent == null || !accessible(context, intent.getComponent())) return false;
            context.startActivity(intent);
            return true;
        } catch (Exception | LinkageError unavailable) {
            Log.w("TvSettingsPlusMOD", "Settings Activity unavailable", unavailable);
            return false;
        }
    }

    private static boolean accessible(Context context, ComponentName component)
            throws PackageManager.NameNotFoundException {
        if (component == null) return false;
        PackageManager pm = context.getPackageManager();
        ActivityInfo info = pm.getActivityInfo(component, 0);
        return info != null && component.getPackageName().equals(info.packageName)
                && component.getClassName().equals(info.name) && info.enabled
                && info.applicationInfo != null && info.applicationInfo.enabled
                && pm.getComponentEnabledSetting(component) <= PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                && pm.getApplicationEnabledSetting(info.packageName) <= PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                && (info.exported || info.applicationInfo.uid == context.getApplicationInfo().uid)
                && (info.permission == null || info.permission.isEmpty()
                    || context.checkSelfPermission(info.permission) == PackageManager.PERMISSION_GRANTED);
    }
}

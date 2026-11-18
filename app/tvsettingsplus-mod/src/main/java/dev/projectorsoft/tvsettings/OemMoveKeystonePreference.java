package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/** Calls the stock 8054 movement toggle with its real OEM widget dependencies. */
final class OemMoveKeystonePreference {
    private static final String TAG = "TvSettingsPlusMOD";
    private static final String PACKAGE = "com.rjpj.tcorrection";
    private static final String PROPERTY = "persist.rjpj.autokeystone.move";
    private static final String CONTROLLER =
            "com.android.tv.settings.uicustom.floatmenu.controller.projector.KeystoneSettingsSecondaryController";
    private static final String[] DEPENDENCIES = {
        "tsv_keystone_auto_screen", "tsv_keystone_auto_obstacle_avoidance", "tsv_keystone_vertical"
    };

    private OemMoveKeystonePreference() {}

    static void install(Context context,Object group,Class<?> pref) {
        try {
            if (!available(context)) return;
            String layout = (String)Class.forName("dev.projectorsoft.tvsettings.Exact8054ProjectionResources")
                    .getField("KEYSTONE_LAYOUT").get(null);
            int layoutId = context.getResources().getIdentifier(layout,"layout",context.getPackageName());
            if (layoutId == 0) return;
            Method read = Class.forName("android.os.SystemProperties").getMethod("get",String.class,String.class);
            String initial = readState(read);
            if (initial == null) return;

            Class<?> controllerClass = Class.forName(CONTROLLER);
            Object controller = controllerClass.getConstructor().newInstance();
            Method original = controllerClass.getDeclaredMethod("setMoveAutoKeystone",boolean.class);
            original.setAccessible(true);
            // The stock method needs these views but does not consult the host or attach lifecycle.
            // Keep their original widget types and layout attributes without installing UI listeners.
            View root = LayoutInflater.from(context).inflate(layoutId,null,false);
            for (String name : DEPENDENCIES) {
                int id = context.getResources().getIdentifier(name,"id",context.getPackageName());
                View stockView = id == 0 ? null : root.findViewById(id);
                Field field = controllerClass.getDeclaredField(name);
                if (!field.getType().isInstance(stockView)) return;
                field.setAccessible(true);
                field.set(controller,stockView);
            }

            Class<?> type = Class.forName("androidx.preference.SwitchPreference");
            Object row = type.getConstructor(Context.class).newInstance(context);
            Method checked = type.getMethod("setChecked",boolean.class);
            checked.invoke(row,"1".equals(initial));
            pref.getMethod("setKey",String.class).invoke(row,"projectorsoft_keystone_move");
            pref.getMethod("setTitle",CharSequence.class).invoke(row,"Автокоррекция трапеции при перемещении");
            pref.getMethod("setPersistent",boolean.class).invoke(row,false);
            pref.getMethod("setOrder",int.class).invoke(row,257);
            Class<?> listener = Class.forName("androidx.preference.Preference$OnPreferenceChangeListener");
            Object callback = Proxy.newProxyInstance(listener.getClassLoader(),new Class<?>[]{listener},
                    (proxy,method,args) -> {
                        switch (method.getName()) {
                            case "onPreferenceChange":
                                if (!(args[1] instanceof Boolean) || !available(context) || readState(read) == null) {
                                    return false;
                                }
                                try {
                                    boolean requested = (Boolean)args[1];
                                    original.invoke(controller,requested);
                                    String actual = readState(read);
                                    if ((requested ? "1" : "0").equals(actual)) return true;
                                    if (actual != null) checked.invoke(row,"1".equals(actual));
                                } catch (Exception ex) {
                                    Log.w(TAG,"Stock movement keystone callback failed",ex);
                                }
                                Toast.makeText(context,"Изменение не подтверждено",Toast.LENGTH_SHORT).show();
                                return false;
                            case "hashCode": return System.identityHashCode(proxy);
                            case "equals": return proxy == args[0];
                            case "toString": return "MoveKeystone";
                            default: return null;
                        }
                    });
            pref.getMethod("setOnPreferenceChangeListener",listener).invoke(row,callback);
            group.getClass().getMethod("addPreference",pref).invoke(group,row);
        } catch (Exception | LinkageError ex) {
            Log.w(TAG,"Stock movement keystone switch unavailable",ex);
        }
    }

    private static String readState(Method read) {
        try {
            String value = (String)read.invoke(null,PROPERTY,"");
            return "0".equals(value) || "1".equals(value) ? value : null;
        } catch (Exception ex) {
            return null;
        }
    }

    private static boolean available(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo app = pm.getApplicationInfo(PACKAGE,0);
            return "8054".equals(OemHdmiPortInfo.machineId()) && app.enabled
                    && (app.flags & ApplicationInfo.FLAG_SYSTEM) != 0
                    && pm.getPackageInfo(PACKAGE,0).getLongVersionCode() == 33
                    && pm.getApplicationEnabledSetting(PACKAGE) <= PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        } catch (Exception ex) {
            return false;
        }
    }
}

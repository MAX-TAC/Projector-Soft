package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

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

            OemProjectionSwitches.add(context, group, pref, "projectorsoft_keystone_move",
                    "Автокоррекция трапеции при перемещении", 257,
                    new ProjectionToggleState.Backend() {
                        @Override public String read() throws Exception {
                            return available(context) ? (String) read.invoke(null, PROPERTY, "") : null;
                        }
                        @Override public void write(boolean value) throws Exception {
                            original.invoke(controller, value);
                        }
                    });
        } catch (Exception | LinkageError ex) {
            Log.w(TAG,"Stock movement keystone switch unavailable",ex);
        }
    }

    private static boolean available(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo app = pm.getApplicationInfo(PACKAGE,0);
            return Projection8054Policy.mayUseOemToggle(OemHdmiPortInfo.machineId(),
                    (app.flags & ApplicationInfo.FLAG_SYSTEM) != 0,
                    app.enabled && pm.getApplicationEnabledSetting(PACKAGE)
                            <= PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    pm.getPackageInfo(PACKAGE, 0).getLongVersionCode(), 33);
        } catch (Exception ex) {
            return false;
        }
    }
}

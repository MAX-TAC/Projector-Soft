package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import java.lang.reflect.Method;

/** Native switches backed by the exact stock 8054 focus controller callbacks. */
final class OemFocusPreferences {
    private static final String TAG = "TvSettingsPlusMOD";
    private static final String PACKAGE = "com.hysd.screen";
    private static final String CONTROLLER =
            "com.android.tv.settings.uicustom.floatmenu.controller.projector.FocusSettingsSecondaryController";

    private OemFocusPreferences() {}

    private static boolean available(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(PACKAGE,0);
            return Projection8054Policy.mayUseOemToggle(OemHdmiPortInfo.machineId(),
                    (info.flags & ApplicationInfo.FLAG_SYSTEM) != 0,
                    info.enabled && pm.getApplicationEnabledSetting(PACKAGE)
                            <= PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    pm.getPackageInfo(PACKAGE, 0).getLongVersionCode(), 4);
        } catch (Exception ex) {
            return false;
        }
    }

    static void install(Context context,Object group,Class<?> pref) {
        if (!available(context)) return;
        add(context,group,pref,"persist.rjpj.autofocus.poweron","setPowerOnAutoFocus",
                "projectorsoft_focus_power_on","Автофокус при включении",254);
        add(context,group,pref,"persist.rjpj.autofocus.move","setMoveAutoFocus",
                "projectorsoft_focus_move","Автофокус при перемещении",255);
    }

    private static void add(Context context,Object group,Class<?> pref,String property,String callbackName,
                            String key,String title,int order) {
        try {
            Method read = Class.forName("android.os.SystemProperties").getMethod("get",String.class,String.class);
            Class<?> controllerClass = Class.forName(CONTROLLER);
            Method original = controllerClass.getDeclaredMethod(callbackName,boolean.class);
            original.setAccessible(true);
            // The exact build checks the public no-arg constructor chain.
            Object controller = controllerClass.getConstructor().newInstance();
            OemProjectionSwitches.add(context, group, pref, key, title, order,
                    new ProjectionToggleState.Backend() {
                        @Override public String read() throws Exception {
                            return available(context) ? (String) read.invoke(null, property, "") : null;
                        }
                        @Override public void write(boolean value) throws Exception {
                            original.invoke(controller, value);
                        }
                    });
        } catch (Exception ex) {
            Log.w(TAG,"Stock focus setting unavailable: "+key,ex);
        }
    }
}

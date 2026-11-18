package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

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
            return "8054".equals(OemHdmiPortInfo.machineId()) && info.enabled
                    && (info.flags & ApplicationInfo.FLAG_SYSTEM) != 0
                    && pm.getPackageInfo(PACKAGE,0).getLongVersionCode() == 4
                    && pm.getApplicationEnabledSetting(PACKAGE) <= PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
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
            String initial = (String) read.invoke(null,property,"");
            if (!"0".equals(initial) && !"1".equals(initial)) return;
            Class<?> controllerClass = Class.forName(CONTROLLER);
            Method original = controllerClass.getDeclaredMethod(callbackName,boolean.class);
            original.setAccessible(true);
            // The exact build checks the public no-arg constructor chain.
            Object controller = controllerClass.getConstructor().newInstance();
            Class<?> switchClass = Class.forName("androidx.preference.SwitchPreference");
            Object item = switchClass.getConstructor(Context.class).newInstance(context);
            Method checked = switchClass.getMethod("setChecked",boolean.class);
            pref.getMethod("setKey",String.class).invoke(item,key);
            pref.getMethod("setTitle",CharSequence.class).invoke(item,title);
            pref.getMethod("setPersistent",boolean.class).invoke(item,false);
            pref.getMethod("setOrder",int.class).invoke(item,order);
            checked.invoke(item,"1".equals(initial));
            Class<?> listener = Class.forName("androidx.preference.Preference$OnPreferenceChangeListener");
            Object callback = Proxy.newProxyInstance(listener.getClassLoader(),new Class<?>[]{listener},
                    (proxy,method,args) -> {
                        switch (method.getName()) {
                            case "onPreferenceChange":
                                if (!(args[1] instanceof Boolean) || !available(context)) return false;
                                try {
                                    String before = (String) read.invoke(null,property,"");
                                    if (!"0".equals(before) && !"1".equals(before)) return false;
                                    boolean requested = (Boolean) args[1];
                                    // On exact stock these two setters touch only controller-local state
                                    // and the corresponding OEM property. No attached view is needed.
                                    original.invoke(controller,requested);
                                    String after = (String) read.invoke(null,property,"");
                                    if ((requested ? "1" : "0").equals(after)) return true;
                                    if ("0".equals(after) || "1".equals(after)) checked.invoke(item,"1".equals(after));
                                } catch (Exception ex) {
                                    Log.w(TAG,"Stock focus callback unavailable",ex);
                                }
                                Toast.makeText(context,"Изменение не подтверждено",Toast.LENGTH_SHORT).show();
                                return false;
                            case "hashCode": return System.identityHashCode(proxy);
                            case "equals": return proxy == args[0];
                            case "toString": return key;
                            default: return null;
                        }
                    });
            pref.getMethod("setOnPreferenceChangeListener",listener).invoke(item,callback);
            group.getClass().getMethod("addPreference",pref).invoke(group,item);
        } catch (Exception ex) {
            Log.w(TAG,"Stock focus setting unavailable: "+key,ex);
        }
    }
}

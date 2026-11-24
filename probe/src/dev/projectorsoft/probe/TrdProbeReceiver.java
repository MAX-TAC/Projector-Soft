package dev.projectorsoft.probe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * TrdProbe — зонд для com.trd.framework.ITrdSystemService.
 *
 * Задача: выяснить, можно ли из обычного приложения получить доступ
 * к мотору, подсветке, температуре и др. через Binder-сервис.
 *
 * adb shell am broadcast -n dev.projectorsoft.probe/.TrdProbeReceiver \
 *     -a dev.projectorsoft.probe.TRD --es cmd info
 */
public class TrdProbeReceiver extends BroadcastReceiver {

    private static final String TAG = "TRDPROBE";
    private static final String SERVICE = "TrdSystemService";
    private static final String DESC = "com.trd.framework.ITrdSystemService";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        String cmd = intent == null ? null : intent.getStringExtra("cmd");
        if (cmd == null) cmd = "info";
        log("CMD=" + cmd);
        try {
            Object svc = getSystemServiceViaBinder();
            if (svc == null) { log("FAIL: could not obtain " + SERVICE); return; }
            log("GOT service: " + svc.getClass().getName());

            switch (cmd) {
                case "info":     dumpMethods(svc); break;
                case "motors":   callOrList(svc, "getMotorCtrl"); break;
                case "projector":callOrList(svc, "getProjectorCtrl"); break;
                case "backlight":callOrList(svc, "GetPanelBackLightCtrl"); break;
                case "panel":    callOrList(svc, "GetPanelDispCtrl"); break;
                case "comm":     callOrList(svc, "getCommunicationCtrl"); break;
                case "temp":     callOrList(svc, "getTemperatureCtrl"); break;
                case "sensor":   callOrList(svc, "getSensorCtrl"); break;
                case "system":   callOrList(svc, "getSystemCtrl"); break;
                default: log("UNKNOWN=" + cmd); break;
            }
        } catch (Throwable t) {
            log("ERROR: " + t);
        }
    }

    private static void log(String s) { Log.i(TAG, s); }

    /** Достаём ITrdSystemService через ServiceManager+"asInterface" по рефлексии. */
    private static Object getSystemServiceViaBinder() throws Exception {
        Class<?> smClass = Class.forName("android.os.ServiceManager");
        Method getService = smClass.getMethod("getService", String.class);
        Object binder = getService.invoke(null, SERVICE);
        if (binder == null) { log("ServiceManager returned null for " + SERVICE); return null; }

        Class<?> stubClass = Class.forName(DESC + "$Stub");
        Method asInterface = stubClass.getMethod("asInterface", Class.forName("android.os.IBinder"));
        return asInterface.invoke(null, binder);
    }

    private void dumpMethods(Object svc) {
        int n = 0;
        for (Method m : svc.getClass().getMethods()) {
            if (m.getDeclaringClass() == Object.class) continue;
            if (n++ > 200) { log("... (truncated)"); break; }
            StringBuilder sb = new StringBuilder("M ");
            sb.append(m.getReturnType().getSimpleName()).append(' ').append(m.getName()).append('(');
            Class<?>[] ps = m.getParameterTypes();
            for (int i = 0; i < ps.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(ps[i].getSimpleName());
            }
            sb.append(')');
            log(sb.toString());
        }
        log("methodCount=" + n);
    }

    private void callOrList(Object svc, String name) {
        try {
            Method target = null;
            for (Method m : svc.getClass().getMethods()) {
                if (m.getName().equals(name) && m.getParameterTypes().length == 0) { target = m; break; }
            }
            if (target == null) { log("no no-arg method " + name); return; }
            Object r = target.invoke(svc);
            log(name + "() -> " + (r == null ? "null" : r.getClass().getName()));
            if (r != null) {
                int n = 0;
                for (Method m : r.getClass().getMethods()) {
                    if (m.getDeclaringClass() == Object.class) continue;
                    if (n++ > 80) { log("... truncated"); break; }
                    log("  M " + m.getReturnType().getSimpleName() + " " + m.getName()
                            + "/" + m.getParameterTypes().length);
                }
            }
        } catch (Throwable t) { log(name + " error: " + t); }
    }
}

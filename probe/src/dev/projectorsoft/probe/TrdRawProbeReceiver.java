package dev.projectorsoft.probe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Parcel;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * TrdRawProbe — доступ к TrdSystemService через сырой Binder.transact.
 *
 * Классы com.trd.framework не видны обычному приложению, но IBinder доступен всем.
 * Перебираем transaction-коды и смотрим, какие отвечают без исключения.
 *
 * adb shell am broadcast -n dev.projectorsoft.probe/.TrdRawProbeReceiver \
 *     -a dev.projectorsoft.probe.TRDRAW --es cmd scan
 */
public class TrdRawProbeReceiver extends BroadcastReceiver {

    private static final String TAG = "TRDRAW";
    private static final String SERVICE = "TrdSystemService";
    private static final String DESC = "com.trd.framework.ITrdSystemService";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        String cmd = intent == null ? null : intent.getStringExtra("cmd");
        if (cmd == null) cmd = "scan";
        log("CMD=" + cmd);
        try {
            IBinder b = getBinder();
            if (b == null) { log("FAIL: no binder for " + SERVICE); return; }
            log("BINDER ok: " + b.getInterfaceDescriptor());
            switch (cmd) {
                case "scan": scan(b); break;
                case "names": callFirstStringMethod(b); break;
                default: log("UNKNOWN=" + cmd); break;
            }
        } catch (Throwable t) { log("ERROR: " + t); }
    }

    private static void log(String s) { Log.i(TAG, s); }

    private static IBinder getBinder() throws Exception {
        Class<?> sm = Class.forName("android.os.ServiceManager");
        Method m = sm.getMethod("getService", String.class);
        return (IBinder) m.invoke(null, SERVICE);
    }

    /** Перебираем transaction-коды 1..60 и смотрим, какие живые. */
    private void scan(IBinder b) {
        for (int code = 1; code <= 60; code++) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken(DESC);
                boolean ok = b.transact(code, data, reply, 0);
                int pos = reply.dataPosition();
                int size = reply.dataSize();
                String preview = "";
                if (size > pos) {
                    try { preview = reply.readString(); } catch (Throwable ignored) {}
                }
                log("code=" + code + " ok=" + ok + " replySize=" + size
                        + (preview != null && !preview.isEmpty() ? " str=" + shorten(preview) : ""));
            } catch (Throwable t) {
                log("code=" + code + " EX " + t.getClass().getSimpleName());
            } finally {
                data.recycle();
                reply.recycle();
            }
        }
    }

    private static String shorten(String s) {
        String one = s.replace('\n', ' ');
        return one.length() > 60 ? one.substring(0, 60) + "…" : one;
    }

    /** Код 1 обычно getInterfaceVersion/descriptor — пробуем безопасно. */
    private void callFirstStringMethod(IBinder b) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESC);
            b.transact(1, data, reply, 0);
            log("tx1 replySize=" + reply.dataSize());
        } catch (Throwable t) { log("tx1 error " + t); }
        finally { data.recycle(); reply.recycle(); }
    }
}

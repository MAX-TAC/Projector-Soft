package dev.projectorsoft.tvsettings;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Uses the 8054 customer-zero KuSourceRJ route and confirms its persisted mode. */
final class OemLightSourcePreference {
    private static final String TAG = "TvSettingsPlusMOD";
    private static final String BOARD = "8054";

    private OemLightSourcePreference() {}

    private static boolean stockRouteAvailable(Context context) {
        try {
            if (!BOARD.equals(OemHdmiPortInfo.machineId())
                    || Build.class.getField("A_RJ_CUSTOMER_ID").getInt(null) != 0) return false;
            PackageManager pm = context.getPackageManager();
            String owner = KuSourceRjBrightnessGateway.PACKAGE;
            ComponentName component = new ComponentName(owner,KuSourceRjBrightnessGateway.RECEIVER);
            ActivityInfo receiver = pm.getReceiverInfo(component,0);
            return receiver.enabled && receiver.applicationInfo != null
                    && receiver.applicationInfo.enabled
                    && (receiver.exported || receiver.applicationInfo.uid == context.getApplicationInfo().uid)
                    && (receiver.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0
                    && pm.getApplicationEnabledSetting(owner) <= PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    && pm.getComponentEnabledSetting(component) <= PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    && pm.getPackageInfo(owner,0).getLongVersionCode() == 41
                    && (receiver.permission == null || receiver.permission.isEmpty()
                        || context.checkSelfPermission(receiver.permission) == PackageManager.PERMISSION_GRANTED);
        } catch (Exception ex) {
            return false;
        }
    }

    static void install(Context context,Object group,Class<?> pref) {
        if (!stockRouteAvailable(context)) return;
        try {
            KuSourceRjBrightnessGateway gateway = new KuSourceRjBrightnessGateway(context,BOARD,
                    () -> stockRouteAvailable(context));
            LightSourceBrightnessController model = new LightSourceBrightnessController(
                    ProjectorCapabilities.from(BOARD,0,EnumSet.of(ProjectorCapabilities.Feature.LIGHT_SOURCE_MODE)),
                    gateway);
            LightSourceBrightnessController.Snapshot initial = model.refresh();
            if (!initial.enabled) return;
            Class<?> list = Class.forName("androidx.preference.ListPreference");
            Object item = list.getConstructor(Context.class).newInstance(context);
            List<LightSourceBrightnessController.Mode> modes = initial.choices;
            CharSequence[] titles = new CharSequence[modes.size()];
            CharSequence[] values = new CharSequence[modes.size()];
            Map<String,LightSourceBrightnessController.Mode> modeByValue = new HashMap<>();
            for (int i = 0; i < modes.size(); i++) {
                titles[i] = modes.get(i).title;
                values[i] = Integer.toString(modes.get(i).hardwareId);
                modeByValue.put(values[i].toString(),modes.get(i));
            }
            pref.getMethod("setKey",String.class).invoke(item,"projectorsoft_projection_light");
            pref.getMethod("setTitle",CharSequence.class).invoke(item,"Яркость источника света");
            pref.getMethod("setOrder",int.class).invoke(item,253);
            pref.getMethod("setPersistent",boolean.class).invoke(item,false);
            list.getMethod("setEntries",CharSequence[].class).invoke(item,(Object)titles);
            list.getMethod("setEntryValues",CharSequence[].class).invoke(item,(Object)values);
            Method setValue = list.getMethod("setValue",String.class);
            Method getEntry = list.getMethod("getEntry");
            Method summary = pref.getMethod("setSummary",CharSequence.class);
            Method enabled = pref.getMethod("setEnabled",boolean.class);
            setValue.invoke(item,Integer.toString(initial.currentId));
            summary.invoke(item,getEntry.invoke(item));
            Handler main = new Handler(Looper.getMainLooper());
            Class<?> listener = Class.forName("androidx.preference.Preference$OnPreferenceChangeListener");
            Object callback = Proxy.newProxyInstance(listener.getClassLoader(),new Class<?>[]{listener},
                    (proxy,method,args) -> {
                        switch (method.getName()) {
                            case "onPreferenceChange":
                                LightSourceBrightnessController.Snapshot current = model.refresh();
                                if (!current.enabled || current.pendingId != null) return false;
                                LightSourceBrightnessController.Mode requested = modeByValue.get(args[1]);
                                if (requested == null) return false;
                                if (!model.select(requested.hardwareId)) {
                                    Toast.makeText(context,"Не удалось выбрать режим",Toast.LENGTH_SHORT).show();
                                    return false;
                                }
                                if (model.currentSnapshot().pendingId == null) {
                                    summary.invoke(item,requested.title);
                                    return true;
                                }
                                enabled.invoke(item,false);
                                summary.invoke(item,"Ожидается подтверждение режима");
                                main.postDelayed(new Runnable() {
                                    private int attempts;
                                    @Override public void run() {
                                        try {
                                            LightSourceBrightnessController.Snapshot result = model.refresh();
                                            if (result.enabled && result.pendingId != null && ++attempts < 6) {
                                                main.postDelayed(this,500);
                                                return;
                                            }
                                            if (!result.enabled || result.pendingId != null) {
                                                model.abandonUnconfirmedRequest();
                                                result = model.currentSnapshot();
                                                Toast.makeText(context,"Режим не подтверждён",Toast.LENGTH_SHORT).show();
                                            }
                                            if (result.enabled) setValue.invoke(item,Integer.toString(result.currentId));
                                            summary.invoke(item,result.enabled ? getEntry.invoke(item) : "Режим недоступен");
                                            enabled.invoke(item,result.enabled);
                                        } catch (Exception ex) {
                                            Log.w(TAG,"Brightness readback unavailable",ex);
                                        }
                                    }
                                },500);
                                // Keep the current radio selection until OEM readback matches.
                                return false;
                            case "hashCode": return System.identityHashCode(proxy);
                            case "equals": return proxy == args[0];
                            case "toString": return "LightSourceMode";
                            default: return null;
                        }
                    });
            pref.getMethod("setOnPreferenceChangeListener",listener).invoke(item,callback);
            group.getClass().getMethod("addPreference",pref).invoke(group,item);
        } catch (Exception ex) {
            Log.w(TAG,"8054 inline light source unavailable",ex);
        }
    }
}

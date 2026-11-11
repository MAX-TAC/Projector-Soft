package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.os.HardwarePropertiesManager;
import android.os.PowerManager;
import android.util.Log;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * Read-only data from public Android APIs, without writing to OEM fan controls.
 * HardwarePropertiesManager can deny access to non-device-owner applications.
 * KuSettings, KuSourceRJ, calibration, and thermal protection are untouched.
 */
public final class OemThermalTelemetryHook {
    private static final String TAG = "TvSettingsPlusMOD";
    private static final String UNAVAILABLE = "Нет данных от Android";
    private static final String DENIED = "Доступ к датчикам запрещён Android";
    private OemThermalTelemetryHook() {}

    private static Object add(Context context, Object category, Class<?> preference,
                              String key, String title, String summary,
                              int order, boolean selectable) throws Exception {
        Object item = preference.getConstructor(Context.class).newInstance(context);
        preference.getMethod("setKey", String.class).invoke(item, key);
        preference.getMethod("setTitle", CharSequence.class).invoke(item, title);
        preference.getMethod("setSummary", CharSequence.class).invoke(item, summary);
        preference.getMethod("setOrder", int.class).invoke(item, order);
        preference.getMethod("setSelectable", boolean.class).invoke(item, selectable);
        Object added = category.getClass().getMethod("addPreference", preference).invoke(category, item);
        return Boolean.TRUE.equals(added) ? item : null;
    }

    private static String status(Context context) {
        try {
            PowerManager power = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return power == null ? UNAVAILABLE :
                    ThermalTelemetryPolicy.thermalState(power.getCurrentThermalStatus());
        } catch (RuntimeException | LinkageError error) {
            return UNAVAILABLE;
        }
    }

    private static HardwarePropertiesManager hardware(Context context) {
        Object service = context.getSystemService(Context.HARDWARE_PROPERTIES_SERVICE);
        return service instanceof HardwarePropertiesManager ? (HardwarePropertiesManager) service : null;
    }

    private static String temperature(Context context, int type) {
        try {
            HardwarePropertiesManager manager = hardware(context);
            return manager == null ? UNAVAILABLE : ThermalTelemetryPolicy.temperatures(
                    manager.getDeviceTemperatures(type, HardwarePropertiesManager.TEMPERATURE_CURRENT));
        } catch (SecurityException error) {
            return DENIED;
        } catch (RuntimeException | LinkageError error) {
            return UNAVAILABLE;
        }
    }

    private static String fans(Context context) {
        try {
            HardwarePropertiesManager manager = hardware(context);
            return manager == null ? UNAVAILABLE : ThermalTelemetryPolicy.fans(manager.getFanSpeeds());
        } catch (SecurityException error) {
            return DENIED;
        } catch (RuntimeException | LinkageError error) {
            return UNAVAILABLE;
        }
    }

    private static void update(Class<?> preference, Object item, String value) throws Exception {
        if (item != null) preference.getMethod("setSummary", CharSequence.class).invoke(item, value);
    }

    private static void refresh(Context context, Class<?> preference, Object state,
                                Object cpu, Object gpu, Object fan) throws Exception {
        update(preference, state, status(context));
        update(preference, cpu, temperature(context, HardwarePropertiesManager.DEVICE_TEMPERATURE_CPU));
        update(preference, gpu, temperature(context, HardwarePropertiesManager.DEVICE_TEMPERATURE_GPU));
        update(preference, fan, fans(context));
    }

    public static boolean install(Context context, Object category, Class<?> preference) {
        if (context == null || category == null || preference == null) return false;
        try {
            Object header = add(context, category, preference,
                    "projectorsoft_thermal_heading", "Температура и охлаждение",
                    "Только чтение. Нет управления оборотами или защитой от перегрева.",
                    290, false);
            if (header == null) return false;
            Object state = add(context, category, preference,
                    "projectorsoft_thermal_state", "Тепловое состояние Android",
                    "Получение показаний…", 291, false);
            Object cpu = add(context, category, preference,
                    "projectorsoft_cpu_temperature", "Температура CPU",
                    "Получение показаний…", 292, false);
            Object gpu = add(context, category, preference,
                    "projectorsoft_gpu_temperature", "Температура GPU",
                    "Получение показаний…", 293, false);
            Object fan = add(context, category, preference,
                    "projectorsoft_fan_rpm", "Скорость вентиляторов",
                    "Получение показаний…", 294, false);
            Object reload = add(context, category, preference,
                    "projectorsoft_refresh_thermal", "Обновить показания",
                    "Чтение по запросу, без фонового опроса", 295, true);
            refresh(context, preference, state, cpu, gpu, fan);
            if (reload != null) {
                Class<?> listener = Class.forName(
                        "androidx.preference.Preference$OnPreferenceClickListener");
                InvocationHandler callback = (proxy, method, args) -> {
                    String name = method.getName();
                    if ("onPreferenceClick".equals(name)) {
                        try {
                            refresh(context, preference, state, cpu, gpu, fan);
                        } catch (Exception error) {
                            Log.w(TAG, "Thermal refresh unavailable", error);
                        }
                        return true;
                    }
                    if ("hashCode".equals(name)) return System.identityHashCode(proxy);
                    if ("equals".equals(name)) return args != null && args.length == 1 && proxy == args[0];
                    if ("toString".equals(name)) return "ThermalTelemetryRefresh";
                    return false;
                };
                Object adapter = Proxy.newProxyInstance(
                        listener.getClassLoader(), new Class<?>[]{listener}, callback);
                preference.getMethod("setOnPreferenceClickListener", listener).invoke(reload, adapter);
            }
            return true;
        } catch (Exception | LinkageError error) {
            Log.w(TAG, "Thermal telemetry preferences unavailable", error);
            return false;
        }
    }
}

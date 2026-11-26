package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.os.HardwarePropertiesManager;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Locale;

/**
 * Read-only 8054 temperatures: SoC, LCD panel and LED light source.
 *
 * CPU comes from HardwarePropertiesManager, then from named thermal zones.
 * LCD and LED are not exposed by Android; they live behind the MediaTek/TRD
 * projector framework ({@code com.trd.android.api.HwTrd.getTemperatureCtrl()}
 * with {@code getTemperatueLCD()} / {@code getTemperatueLED()} /
 * {@code getTemperatueAMB()}). Those are reached by reflection because TRD is
 * not part of the public SDK. Every path is guarded: a missing sensor returns a
 * readable marker, never an exception or a fabricated number.
 *
 * Nothing here writes to thermal, fan or brightness controls.
 */
final class OemCpuTemperature {
    private static final String TAG = "TvSettingsPlusMOD";

    private OemCpuTemperature() {}

    // ---------------- public readings ----------------

    static String read(Context context) {
        if (!"8054".equals(OemHdmiPortInfo.machineId())) return "Недоступно";
        String cpu = readCpu(context);
        return cpu;
    }

    /** LCD panel temperature, or a marker when the sensor is absent. */
    static String readLcd(Context context) {
        if (!"8054".equals(OemHdmiPortInfo.machineId())) return "Недоступно";
        Double direct = viaTrd(context, "getTemperatueLCD");
        Log.i(TAG, "LCD via TRD=" + direct);
        if (direct != null) return display(direct);
        Double zone = fromZone("lcd", "panel", "lvds", "display");
        return zone != null ? display(zone) : "Датчик панели недоступен";
    }

    /** LED light-source temperature, or a marker when the sensor is absent. */
    static String readLed(Context context) {
        if (!"8054".equals(OemHdmiPortInfo.machineId())) return "Недоступно";
        Double direct = viaTrd(context, "getTemperatueLED");
        Log.i(TAG, "LED via TRD=" + direct);
        if (direct != null) return display(direct);
        Double zone = fromZone("led", "light", "lamp", "laser");
        return zone != null ? display(zone) : "Датчик LED недоступен";
    }

    /** Ambient/air temperature, or a marker when the sensor is absent. */
    static String readAmbient(Context context) {
        if (!"8054".equals(OemHdmiPortInfo.machineId())) return "Недоступно";
        Double direct = viaTrd(context, "getTemperatueAMB");
        if (direct != null) return display(direct);
        Double zone = fromZone("amb", "ambient", "env");
        return zone != null ? display(zone) : "Датчик воздуха недоступен";
    }

    // ---------------- CPU ----------------

    private static String readCpu(Context context) {
        // Prefer the official device-temperature API; GPU often reports the
        // same SoC sensor and is deliberately not shown as a separate metric.
        try {
            HardwarePropertiesManager h = (HardwarePropertiesManager)
                    context.getSystemService(Context.HARDWARE_PROPERTIES_SERVICE);
            if (h != null) {
                float[] values = h.getDeviceTemperatures(
                        HardwarePropertiesManager.DEVICE_TEMPERATURE_CPU,
                        HardwarePropertiesManager.TEMPERATURE_CURRENT);
                if (values != null) for (float value : values)
                    if (valid(value)) return display(value);
            }
        } catch (SecurityException | LinkageError blocked) {
            Log.d(TAG, "Android CPU temperature API inaccessible");
        } catch (RuntimeException error) {
            Log.d(TAG, "Android CPU temperature API returned no samples", error);
        }

        Double zone = fromZone("cpu", "soc", "ap_thermal", "mtktscpu");
        return zone != null ? display(zone) : "Датчик CPU недоступен";
    }

    // ---------------- TRD projector framework ----------------

    /**
     * Calls one ITemperatureCtrl getter through TRD.
     *
     * HwTrd may expose the controller statically or per instance, so both are
     * attempted; a failure on one shape must not poison the other.
     */
    private static Double viaTrd(Context context, String getter) {
        Object controller = temperatureController(context);
        if (controller == null) return null;
        try {
            Object value = controller.getClass().getMethod(getter).invoke(controller);
            return asTemperature(value);
        } catch (Throwable error) {
            Log.i(TAG, "TRD " + getter + " unavailable: " + error);
            return null;
        }
    }

    private static Object temperatureController(Context context) {
        // The TRD API lives inside /vendor/priv-app/trdmiddle/trdmiddle.apk, not
        // on the boot classpath, so a plain Class.forName cannot see it. Load it
        // through its own dex first.
        Object loaded = controllerFromApk(context);
        if (loaded != null) return loaded;
        Log.i(TAG, "TRD temperature controller not found via APK");
        // 1) Static accessor on HwTrd (the documented shape).
        try {
            Class<?> hw = Class.forName("com.trd.android.api.HwTrd");
            Object ctrl = hw.getMethod("getTemperatureCtrl").invoke(null);
            if (ctrl != null) return ctrl;
        } catch (Throwable ignored) {
        }
        // 2) Instance accessor: HwTrd may require an instance first.
        try {
            Class<?> hw = Class.forName("com.trd.android.api.HwTrd");
            Object instance = null;
            try {
                instance = hw.getMethod("getInstance").invoke(null);
            } catch (Throwable noSingleton) {
                try {
                    instance = hw.getConstructor().newInstance();
                } catch (Throwable noCtor) {
                    instance = null;
                }
            }
            if (instance != null) {
                Object ctrl = hw.getMethod("getTemperatureCtrl").invoke(instance);
                if (ctrl != null) return ctrl;
            }
        } catch (Throwable ignored) {
        }
        // 3) TrdManager as an alternative entry point.
        try {
            Class<?> manager = Class.forName("com.trd.android.api.TrdManager");
            for (String name : new String[]{"getInstance", "getDefault"}) {
                try {
                    Object instance = manager.getMethod(name).invoke(null);
                    if (instance == null) continue;
                    Object ctrl = manager.getMethod("getTemperatureCtrl").invoke(instance);
                    if (ctrl != null) return ctrl;
                } catch (Throwable skipped) {
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    /** TRD getters return int/float/Integer/String depending on firmware. */
    /** Loads HwTrd out of trdmiddle.apk and asks it for the temperature control. */
    private static Object controllerFromApk(Context context) {
        if (context == null) return null;
        String[] candidates = {
                "/vendor/priv-app/trdmiddle/trdmiddle.apk",
                "/system/priv-app/trdmiddle/trdmiddle.apk"
        };
        for (String path : candidates) {
            if (!new java.io.File(path).exists()) continue;
            try {
                ClassLoader loader = new dalvik.system.DexClassLoader(
                        path, context.getCodeCacheDir().getAbsolutePath(), null,
                        context.getClassLoader());
                Class<?> hw = loader.loadClass("com.trd.android.api.HwTrd");
                Object ctrl = hw.getMethod("getTemperatureCtrl").invoke(null);
                if (ctrl != null) return ctrl;
            } catch (Throwable error) {
                Log.i(TAG, "trdmiddle load failed from " + path + ": " + error);
            }
        }
        return null;
    }
    private static Double asTemperature(Object value) {
        if (value == null) return null;
        double numeric;
        if (value instanceof Number) {
            numeric = ((Number) value).doubleValue();
        } else {
            try {
                numeric = Double.parseDouble(value.toString().trim());
            } catch (NumberFormatException notANumber) {
                return null;
            }
        }
        // Some TRD builds report millidegrees.
        if (Math.abs(numeric) >= 1000d) numeric /= 1000d;
        return valid((float) numeric) ? numeric : null;
    }

    // ---------------- thermal zones ----------------

    private static Double fromZone(String... needles) {
        File base = new File("/sys/class/thermal");
        File[] zones = base.listFiles((dir, name) -> name.startsWith("thermal_zone"));
        if (zones == null) return null;
        for (File zone : zones) {
            String type = firstLine(new File(zone, "type"));
            if (type == null) continue;
            String name = type.toLowerCase(Locale.ROOT);
            boolean match = false;
            for (String needle : needles) if (name.contains(needle)) { match = true; break; }
            if (!match) continue;
            String raw = firstLine(new File(zone, "temp"));
            if (raw == null) continue;
            try {
                float v = Float.parseFloat(raw.trim());
                if (Math.abs(v) >= 1000f) v /= 1000f;
                if (valid(v)) return (double) v;
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private static String firstLine(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.readLine();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static boolean valid(float value) {
        return !Float.isNaN(value) && !Float.isInfinite(value)
                && value >= -20f && value <= 125f;
    }

    private static String display(double value) {
        return String.format(Locale.getDefault(), "%.1f °C", value);
    }
}
package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.os.HardwarePropertiesManager;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Locale;

/** Read-only 8054 CPU/SoC temperature. Never writes thermal or fan controls. */
final class OemCpuTemperature {
    private static final String TAG = "TvSettingsPlusMOD";
    private OemCpuTemperature() {}

    static String read(Context context) {
        if (!"8054".equals(OemHdmiPortInfo.machineId())) return "Недоступно";
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

        // MTK thermal zone names differ by firmware. Read only explicit CPU
        // / SoC types; never guess using an anonymous thermal_zone0.
        File base = new File("/sys/class/thermal");
        File[] zones = base.listFiles((dir, name) -> name.startsWith("thermal_zone"));
        if (zones != null) for (File zone : zones) {
            String type = firstLine(new File(zone, "type"));
            if (type == null) continue;
            String name = type.toLowerCase(Locale.ROOT);
            if (!(name.contains("cpu") || name.contains("soc") || name.contains("ap_thermal")
                    || name.contains("mtktscpu"))) continue;
            String raw = firstLine(new File(zone, "temp"));
            try {
                if (raw == null) continue;
                float v = Float.parseFloat(raw.trim());
                if (Math.abs(v) >= 1000f) v /= 1000f;
                if (valid(v)) return display(v);
            } catch (NumberFormatException ignored) { }
        }
        return "Датчик CPU недоступен";
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

    private static String display(float value) {
        return String.format(Locale.getDefault(), "%.1f °C", value);
    }
}

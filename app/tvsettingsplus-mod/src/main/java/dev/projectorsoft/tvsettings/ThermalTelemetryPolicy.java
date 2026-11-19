package dev.projectorsoft.tvsettings;

import java.util.Locale;

/** Pure Java, read-only formatting of temperatures, fan RPM, and Android thermal signals. */
public final class ThermalTelemetryPolicy {
    private static final Locale RU = new Locale("ru", "RU");
    private ThermalTelemetryPolicy() {}

    public static String temperatures(float[] samples) {
        return describe(samples, true);
    }

    public static String fans(float[] samples) {
        return describe(samples, false);
    }

    private static String describe(float[] samples, boolean temperature) {
        if (samples == null || samples.length == 0) return "Нет данных от Android";
        StringBuilder result = new StringBuilder();
        int shown = 0;
        for (int i = 0; i < samples.length && shown < 8; i++) {
            float value = samples[i];
            if (!Float.isFinite(value)) continue;
            // Also rejects Android's UNDEFINED_TEMPERATURE (-Float.MAX_VALUE).
            if (temperature && (value < -100f || value > 250f)) continue;
            if (!temperature && (value < 0f || value > 100000f)) continue;
            if (shown > 0) result.append("; ");
            result.append(temperature ? "Канал " : "Вентилятор ").append(i + 1).append(": ");
            if (temperature) result.append(String.format(RU, "%.1f °C", value));
            else result.append(Math.round(value)).append(" об/мин");
            shown++;
        }
        return shown == 0 ? "Нет достоверных показаний" : result.toString();
    }

    /** An Android thermal signal does not certify the OEM fan or overheating watchdog. */
    public static String thermalState(int status) {
        switch (status) {
            case 0: return "Android: тепловое ограничение не сообщается";
            case 1: return "Android: лёгкое тепловое ограничение";
            case 2: return "Android: умеренное тепловое ограничение";
            case 3: return "Android: сильное тепловое ограничение";
            case 4: return "Android: критическое тепловое ограничение";
            case 5: return "Android: аварийное тепловое ограничение";
            case 6: return "Android: порог теплового отключения";
            default: return "Нет данных о тепловом состоянии Android";
        }
    }
}

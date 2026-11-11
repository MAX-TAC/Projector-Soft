package dev.projectorsoft.tvsettings;

public final class ThermalTelemetryPolicyTest {
    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        check("Нет данных от Android".equals(ThermalTelemetryPolicy.temperatures(new float[0])), "empty");
        check("Нет достоверных показаний".equals(ThermalTelemetryPolicy.temperatures(
                new float[]{-Float.MAX_VALUE, Float.NaN, Float.POSITIVE_INFINITY})), "undefined readings");
        String c = ThermalTelemetryPolicy.temperatures(new float[]{43.25f, -Float.MAX_VALUE, 59.5f});
        check(c.contains("Канал 1: 43,3 °C") && c.contains("Канал 3: 59,5 °C")
                && !c.contains("Канал 2"), "temperature labels and invalid samples: " + c);
        check("Вентилятор 1: 0 об/мин; Вентилятор 2: 1450 об/мин".equals(
                ThermalTelemetryPolicy.fans(new float[]{0f,1450.2f})), "RPM/zero fan");
        check("Нет достоверных показаний".equals(ThermalTelemetryPolicy.fans(
                new float[]{Float.NaN, -8f, 100001f})), "bad RPM");
        check(ThermalTelemetryPolicy.thermalState(0).contains("не сообщается"), "thermal 0");
        check(ThermalTelemetryPolicy.thermalState(6).contains("отключения"), "thermal 6");
        check(ThermalTelemetryPolicy.thermalState(99).startsWith("Нет данных"), "unknown");
        System.out.println("PASS: read-only thermal formatting and safe missing-data handling");
    }
}

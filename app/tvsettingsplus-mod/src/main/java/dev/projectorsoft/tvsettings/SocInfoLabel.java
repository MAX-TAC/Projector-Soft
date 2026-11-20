package dev.projectorsoft.tvsettings;

/** Formats optional SoC identifiers reported by Android for the 8054 system page. */
final class SocInfoLabel {
    private SocInfoLabel() {}

    static String format(String machine, String manufacturer, String model) {
        if (!"8054".equals(machine)) return null;
        String name = present(manufacturer);
        String chip = present(model);
        if (chip == null) return name;
        if (name == null || chip.equalsIgnoreCase(name)
                || chip.regionMatches(true, 0, name + " ", 0, name.length() + 1)) {
            return chip;
        }
        return name + " " + chip;
    }

    private static String present(String field) {
        if (field == null) return null;
        String value = field.trim();
        if (value.isEmpty() || "unknown".equalsIgnoreCase(value) || "null".equalsIgnoreCase(value)
                || "n/a".equalsIgnoreCase(value) || "0".equals(value)) {
            return null;
        }
        return value;
    }
}

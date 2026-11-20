package dev.projectorsoft.tvsettings;

import java.lang.reflect.Field;
import java.util.Locale;

/** Reads the Settings-owned OEM instance only after stock code has constructed it. */
final class PanelCacheReadout {
    private static final String[] GETTERS = {
            "getPanelType", "getPanelVersion", "getPanelWidth", "getPanelHeight"
    };

    private PanelCacheReadout() {}

    static String[] read(String machine, ClassLoader loader, String managerName, String singletonName,
                         String displayName) {
        String[] result = new String[GETTERS.length];
        if (!"8054".equals(machine)) return result;
        try {
            Class<?> managerClass = Class.forName(managerName, false, loader);
            Field singleton = managerClass.getDeclaredField(singletonName);
            singleton.setAccessible(true);
            Object manager = singleton.get(null);
            if (manager == null) return result;
            Field displayField = managerClass.getDeclaredField(displayName);
            displayField.setAccessible(true);
            Object display = displayField.get(manager);
            if (display == null) return result;
            for (int i = 0; i < GETTERS.length; i++) {
                result[i] = readField(display, GETTERS[i], i >= 2);
            }
        } catch (ReflectiveOperationException | RuntimeException | LinkageError unavailable) {
            // No alternate initializer or file access when the Settings cache is absent.
        }
        return result;
    }

    private static String readField(Object display, String getter, boolean dimension) {
        try {
            Object value = display.getClass().getMethod(getter).invoke(display);
            if (!(value instanceof String)) return null;
            String text = ((String) value).trim();
            if (dimension) {
                int pixels = Integer.parseInt(text);
                return pixels > 0 ? pixels + " пкс" : null;
            }
            switch (text.toLowerCase(Locale.ROOT)) {
                case "":
                case "0":
                case "unknown":
                case "null":
                case "n/a":
                    return null;
                default:
                    return text;
            }
        } catch (ReflectiveOperationException | RuntimeException | LinkageError unavailable) {
            return null;
        }
    }
}

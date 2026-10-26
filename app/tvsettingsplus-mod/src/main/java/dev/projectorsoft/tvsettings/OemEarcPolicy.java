package dev.projectorsoft.tvsettings;

/**
 * Safety rules for OEM eARC. SourceSubMenuController disabling CEC writes
 * KEY_SOUND_EARC=1 and setEarcEnable(false), so the factory persisted
 * convention is 0=enabled, 1=disabled. Do not invert this mapping.
 */
public final class OemEarcPolicy {
    private OemEarcPolicy() {}

    public static boolean canExpose(boolean supported,boolean cecEnabled,
                                    String storedOemValue) {
        return supported && cecEnabled && ("0".equals(storedOemValue)
                                              || "1".equals(storedOemValue));
    }
    public static boolean isEnabled(String factoryValue) {
        if (!"0".equals(factoryValue) && !"1".equals(factoryValue))
            throw new IllegalArgumentException("Unknown OEM eARC value");
        return "0".equals(factoryValue);
    }
    public static int storedValue(boolean enabled) { return enabled?0:1; }
}

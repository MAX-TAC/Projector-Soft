package dev.projectorsoft.tvsettings;

/**
 * Fail-closed gate for transferring OEM thermal watchdog ownership.
 *
 * Values map to PackageManager.COMPONENT_ENABLED_STATE_* without importing
 * the Android framework, so it is regression-testable on a standard JVM.
 * A package that is absent, not queryable, implicitly default-enabled or
 * "disabled until used" must NOT silently activate a second watchdog.
 */
public final class KuSourceRecoveryActivationPolicy {
    public static final int DEFAULT=0;
    public static final int ENABLED=1;
    public static final int DISABLED=2;
    public static final int DISABLED_USER=3;
    public static final int DISABLED_UNTIL_USED=4;

    private KuSourceRecoveryActivationPolicy() {}

    public static boolean explicitlyDisabled(int componentEnabledState) {
        return componentEnabledState==DISABLED ||
               componentEnabledState==DISABLED_USER;
    }

    /**
     * The 8054 service lifecycle has not been established from bytecode and
     * hardware traces. A disabled KuSettings package alone is insufficient
     * authority to start a replacement thermal watchdog on that board.
     */
    public static boolean automaticRecoveryAllowed(String machineId,int componentEnabledState) {
        return "8052".equals(machineId) && explicitlyDisabled(componentEnabledState);
    }
}


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
     * The 8054 board runs the same KuSettings/KuSourceRJ pair as 8052: both APKs
     * are byte-identical apart from their signature (verified by hashing every
     * entry of vendor/reference/8052/APK/TvSettingsPlus.apk against the APK
     * pulled from the live 8054). The existing 8054 restriction was a
     * precaution against an unproven lifecycle, not evidence of a difference.
     *
     * Recovery is still gated on an authorized DISABLE of com.my.settings, so a
     * second watchdog can never race the OEM one. Flip ALLOW_8054 to false to
     * restore the conservative behaviour without touching callers.
     */
    private static final boolean ALLOW_8054 = true;

    public static boolean automaticRecoveryAllowed(String machineId,int componentEnabledState) {
        boolean enabledBoard = "8052".equals(machineId)
                || (ALLOW_8054 && "8054".equals(machineId));
        return enabledBoard && explicitlyDisabled(componentEnabledState);
    }
}


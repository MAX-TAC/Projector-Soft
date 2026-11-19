package dev.projectorsoft.tvsettings;

/**
 * Conservative visibility rules for OEM functions still delegated to the
 * factory TvSettingsPlus controllers. Showing a link never proves that an
 * OTA backend, boot app, or hardware implementation works on the device.
 */
public final class OemDelegatedMenuPolicy {
    private OemDelegatedMenuPolicy() {}

    public static boolean exposePowerOnApp(boolean sourceUiPresent) {
        return sourceUiPresent;
    }

    public static boolean exposeFirmwareUpdates(boolean onlineUiPresent, boolean localUiPresent) {
        return onlineUiPresent || localUiPresent;
    }
}

package dev.projectorsoft.tvsettings;

/** Guard for the legacy Global EDID selector with a fixed HW6 query. */
public final class EdidPortPolicy {
    private EdidPortPolicy() {}

    public static boolean mayUseLegacySelector(String machineId) {
        // 8054 has HW6 and HW7. The legacy selector has no proven port argument
        // or per-port state, so it must not be offered on that board.
        return "8052".equals(machineId);
    }
}

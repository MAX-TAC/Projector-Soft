package dev.projectorsoft.tvsettings;

import java.util.Map;

/**
 * User-only picture editing on exact 8054. The OEM's user preset has numeric
 * value 0; the dynamic customer mode table must advertise that value.
 *
 * This is only the preset guard. It does not prove the active HDMI/inputGroup,
 * HDR/PQ scope or a completed hardware write.
 */
final class PictureUserEditPolicy {
    private static final String OEM_USER_VALUE = "0";

    private PictureUserEditPolicy() {}

    static boolean allows(String machineId, Map<String, Integer> state, String[][] customerModes) {
        if (!"8054".equals(machineId) || state == null
                || !Integer.valueOf(0).equals(state.get(PicturePanelState.MODE))
                || !OemCustomerOptions.valid(customerModes)) return false;
        for (String value : customerModes[1]) {
            if (OEM_USER_VALUE.equals(value)) return true;
        }
        return false;
    }
}

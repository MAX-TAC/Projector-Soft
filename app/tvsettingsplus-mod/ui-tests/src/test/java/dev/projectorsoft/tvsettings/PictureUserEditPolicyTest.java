package dev.projectorsoft.tvsettings;

import static org.junit.Assert.*;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;

/** Reject factory-mode and missing-catalog writes before any OEM callback. */
public final class PictureUserEditPolicyTest {
    private static Map<String, Integer> state(int mode) {
        Map<String, Integer> values = new LinkedHashMap<>();
        values.put(PicturePanelState.MODE, mode);
        values.put(PicturePanelState.TEMPERATURE, 2);
        values.put(PicturePanelState.HDR, 0);
        values.put("picture_contrast", 50);
        return values;
    }

    private static final String[][] OEM_MODES = {
            {"Пользовательский", "Стандартный", "Кино"},
            {"0", "7", "9"}
    };

    @Test public void actualUserModeWithOemIdIsPotentiallyEditable() {
        assertTrue(PictureUserEditPolicy.allows("8054", state(0), OEM_MODES));
    }

    @Test public void noFactoryModeIsEditableEvenWithRealValues() {
        for (int mode : new int[]{2, 3, 7, 9, 10, 11, 14}) {
            assertFalse("factory " + mode,
                    PictureUserEditPolicy.allows("8054", state(mode), OEM_MODES));
        }
    }

    @Test public void missingOrMalformedOemUserCatalogFailsClosed() {
        assertFalse(PictureUserEditPolicy.allows("8054", state(0),
                new String[][]{{"Стандартный", "Кино"}, {"7", "9"}}));
        assertFalse(PictureUserEditPolicy.allows("8054", state(0),
                new String[][]{{"Пользовательский", "Дубликат"}, {"0", "0"}}));
        assertFalse(PictureUserEditPolicy.allows("8054", state(0), null));
        assertFalse(PictureUserEditPolicy.allows("8054", null, OEM_MODES));
        assertFalse(PictureUserEditPolicy.allows("8052", state(0), OEM_MODES));
    }

    @Test public void missingModeOrChangedModeInvalidatesWrite() {
        Map<String, Integer> stale = state(0);
        stale.remove(PicturePanelState.MODE);
        assertFalse(PictureUserEditPolicy.allows("8054", stale, OEM_MODES));
        stale.put(PicturePanelState.MODE, 7);
        assertFalse(PictureUserEditPolicy.allows("8054", stale, OEM_MODES));
    }
}

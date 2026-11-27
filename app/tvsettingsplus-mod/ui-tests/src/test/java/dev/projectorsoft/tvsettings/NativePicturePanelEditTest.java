package dev.projectorsoft.tvsettings;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

/** A lower preview is valid only while the observed PQ snapshot stays unchanged. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=33)
public final class NativePicturePanelEditTest {
    private Map<String,Integer> start() {
        Map<String,Integer> values = new LinkedHashMap<>();
        values.put(PicturePanelState.MODE, 1);
        values.put(PicturePanelState.TEMPERATURE, 2);
        values.put(PicturePanelState.HDR, 0);
        values.put("picture_contrast", 50);
        values.put("picture_brightness", 48);
        return values;
    }

    @Test public void changedContrastClosesThePreview() {
        Map<String,Integer> before=start(), after=start();
        after.put("picture_contrast", 51);
        assertFalse(NativePicturePanel.stableSnapshot(before, after));
    }

    @Test public void changedBrightnessInvalidatesThePreview() {
        Map<String,Integer> before=start(), after=start();
        after.put("picture_brightness", 49);
        assertFalse(NativePicturePanel.stableSnapshot(before, after));
    }

    @Test public void changedModeHdrOrTemperatureInvalidatesThePreview() {
        for (String key : new String[]{PicturePanelState.MODE, PicturePanelState.TEMPERATURE,
                PicturePanelState.HDR}) {
            Map<String,Integer> changed=start();
            changed.put(key,changed.get(key)+1);
            assertFalse(key,NativePicturePanel.stableSnapshot(start(),changed));
        }
    }

    @Test public void missingReadbackCannotPretendToBeStable() {
        assertFalse(NativePicturePanel.stableSnapshot(start(),null));
        assertFalse(NativePicturePanel.stableSnapshot(start(),Map.of()));
        assertTrue(NativePicturePanel.stableSnapshot(start(),start()));
    }
}

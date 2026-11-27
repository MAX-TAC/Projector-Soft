package dev.projectorsoft.tvsettings;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

/** Real v41 UI policy: contrast echo must not close the dialog; profile changes must. */
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

    @Test public void expectedContrastProviderEchoKeepsBottomEditor() {
        Map<String,Integer> before=start(), after=start();
        after.put("picture_contrast", 51);
        assertTrue(NativePicturePanel.otherKeysUnchanged(before, after, "picture_contrast"));
    }

    @Test public void changingBrightnessDuringContrastEditInvalidatesSession() {
        Map<String,Integer> before=start(), after=start();
        after.put("picture_brightness", 49);
        assertFalse(NativePicturePanel.otherKeysUnchanged(before, after, "picture_contrast"));
    }

    @Test public void changedModeHdrOrTemperatureStopsEdit() {
        for (String key : new String[]{PicturePanelState.MODE, PicturePanelState.TEMPERATURE,
                PicturePanelState.HDR}) {
            Map<String,Integer> changed=start();
            changed.put(key,changed.get(key)+1);
            assertFalse(key,NativePicturePanel.otherKeysUnchanged(start(),changed,"picture_contrast"));
        }
    }

    @Test public void sameProfileOnAnotherInputIsNotTheSameTarget() {
        String androidVideo = PictureEditContext.identify("AndroidVideo", start());
        String hdmiOne = PictureEditContext.identify("HDMI1", start());
        assertNotNull(androidVideo);
        assertNotEquals(androidVideo, hdmiOne);
        assertFalse(PictureEditContext.stable(androidVideo, hdmiOne, 50, 50));
        assertFalse(PictureEditContext.stable(androidVideo, androidVideo, 50, 49));
        assertNull(PictureEditContext.identify("unknown", start()));
    }

    @Test public void aChangedProviderValueAloneDoesNotConfirmApplication() {
        String source = PictureEditContext.identify("AndroidVideo", start());
        assertFalse(PictureEditContext.applied(source, source, 51, 51, 50));
        assertFalse(PictureEditContext.applied(source, source, 51, 51, null));
        assertFalse(PictureEditContext.applied(source, null, 51, 51, 51));
        assertTrue(PictureEditContext.applied(source, source, 51, 51, 51));
    }

    @Test public void missingReadbackCannotPretendToBeStable() {
        assertFalse(NativePicturePanel.otherKeysUnchanged(start(),null,"picture_contrast"));
        assertFalse(NativePicturePanel.otherKeysUnchanged(start(),Map.of(),"picture_contrast"));
        assertFalse(NativePicturePanel.otherKeysUnchanged(start(),start(),null));
    }
}

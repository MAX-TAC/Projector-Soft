package dev.projectorsoft.tvsettings;

import static org.junit.Assert.*;
import android.media.AudioDeviceInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Labels are based on output types, not paired Bluetooth devices or vendor XML. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public final class NativeAudioOutputReadoutTest {
    @Test public void speakerAndBluetoothAreDistinct() {
        assertEquals("Встроенные динамики",
                NativeAudioOutputReadout.label(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER, "Projetor"));
        assertEquals("Redmi TV Soundbar",
                NativeAudioOutputReadout.label(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP, "Redmi TV Soundbar"));
        assertEquals("Bluetooth A2DP",
                NativeAudioOutputReadout.label(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP, ""));
    }

    @Test public void hdmiArcAndEarcAreNotConfused() {
        assertEquals("HDMI ARC",
                NativeAudioOutputReadout.label(AudioDeviceInfo.TYPE_HDMI_ARC, "Unknown"));
        assertEquals("HDMI eARC",
                NativeAudioOutputReadout.label(AudioDeviceInfo.TYPE_HDMI_EARC, "Unknown"));
    }
}

package dev.projectorsoft.tvsettings;

import static org.junit.Assert.*;
import android.media.AudioDeviceInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Exact port matching and allowed device types for the guarded 8054 route test. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public final class NativeMediaRouteBridgeTest {
    @Test public void onlyConnectedSpeakerAndA2dpTypesAreCandidates() {
        assertTrue(NativeMediaRouteBridge.isCandidate(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER));
        assertTrue(NativeMediaRouteBridge.isCandidate(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP));
        for (int type : new int[]{
                AudioDeviceInfo.TYPE_HDMI,
                AudioDeviceInfo.TYPE_HDMI_ARC,
                AudioDeviceInfo.TYPE_HDMI_EARC,
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                AudioDeviceInfo.TYPE_USB_DEVICE,
                AudioDeviceInfo.TYPE_LINE_ANALOG}) {
            assertFalse("must not offer " + type, NativeMediaRouteBridge.isCandidate(type));
        }
    }

    @Test public void bluetoothRouteRequiresExactAddressNotOnlySameType() {
        NativeMediaRouteBridge.Output tv = new NativeMediaRouteBridge.Output(
                18, AudioDeviceInfo.TYPE_BLUETOOTH_A2DP, "AA:BB", "Soundbar");
        NativeMediaRouteBridge.Output same = new NativeMediaRouteBridge.Output(
                19, AudioDeviceInfo.TYPE_BLUETOOTH_A2DP, "AA:BB", "Soundbar");
        NativeMediaRouteBridge.Output different = new NativeMediaRouteBridge.Output(
                20, AudioDeviceInfo.TYPE_BLUETOOTH_A2DP, "CC:DD", "Other");
        assertTrue(tv.same(same));
        assertFalse(tv.same(different));
        assertFalse(tv.same(new NativeMediaRouteBridge.Output(
                2, AudioDeviceInfo.TYPE_BUILTIN_SPEAKER, "", "Speaker")));
    }

    @Test public void speakerRouteDoesNotRequirePhysicalAddress() {
        NativeMediaRouteBridge.Output speaker = new NativeMediaRouteBridge.Output(
                2, AudioDeviceInfo.TYPE_BUILTIN_SPEAKER, "", "Built-in");
        assertTrue(speaker.same(new NativeMediaRouteBridge.Output(
                8, AudioDeviceInfo.TYPE_BUILTIN_SPEAKER, "default", "Built-in")));
    }

    @Test public void nullBluetoothAddressNormalizesToEmpty() {
        NativeMediaRouteBridge.Output bt = new NativeMediaRouteBridge.Output(
                18, AudioDeviceInfo.TYPE_BLUETOOTH_A2DP, null, "A2DP");
        assertEquals("", bt.address);
    }
}

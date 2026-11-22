package dev.projectorsoft.tvsettings;

import static org.junit.Assert.*;
import android.media.AudioDeviceInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** No projectors or binder calls: exercise the same rollback path used by the UI. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public final class NativeMediaRouteTransactionTest {
    private static final NativeMediaRouteBridge.Output SPEAKER = new NativeMediaRouteBridge.Output(
            2, AudioDeviceInfo.TYPE_BUILTIN_SPEAKER, "", "Speaker");
    private static final NativeMediaRouteBridge.Output BT = new NativeMediaRouteBridge.Output(
            18, AudioDeviceInfo.TYPE_BLUETOOTH_A2DP, "AA:BB", "Soundbar");

    static final class FakePort implements NativeMediaRouteTransaction.Port {
        List<Object> preferred = new ArrayList<>();
        final List<NativeMediaRouteBridge.Output> devices = new ArrayList<>(
                Arrays.asList(SPEAKER, BT));
        NativeMediaRouteBridge.Output selected = BT;
        boolean throwAfterApply, denyApply;
        int writes, restores;

        @Override public List<Object> preferred() {
            return new ArrayList<>(preferred);
        }
        @Override public List<NativeMediaRouteBridge.Output> connectedBluetooth() {
            return devices.contains(BT) ? Collections.singletonList(BT) : Collections.emptyList();
        }
        @Override public NativeMediaRouteBridge.Output findConnected(NativeMediaRouteBridge.Output d) {
            return devices.contains(d) ? d : null;
        }
        @Override public boolean apply(NativeMediaRouteBridge.Output target) {
            writes++;
            if (denyApply) return false;
            preferred.clear();
            if (target != null) {
                preferred.add(target);
                selected = target;
            } else {
                selected = BT;
            }
            if (throwAfterApply) throw new IllegalStateException("partial binder write");
            return true;
        }
        @Override public boolean preferredIs(NativeMediaRouteBridge.Output target) {
            return preferred.size() == 1 && preferred.get(0) == target;
        }
        @Override public boolean preferredIsAutomatic() { return preferred.isEmpty(); }
        @Override public boolean routeIs(NativeMediaRouteBridge.Output target) {
            return selected == target;
        }
        @Override public boolean stillConnected(List<NativeMediaRouteBridge.Output> initial) {
            return devices.containsAll(initial);
        }
        @Override public boolean restore(List<Object> original) {
            restores++;
            preferred = new ArrayList<>(original);
            selected = original.isEmpty() ? BT : (NativeMediaRouteBridge.Output) original.get(0);
            return true;
        }
    }

    @Test public void speakerTransitionKeepsBtConnectedAndRestoresAutomaticMode() throws Exception {
        FakePort fake = new FakePort();
        NativeMediaRouteTransaction txn = new NativeMediaRouteTransaction(fake, SPEAKER);
        assertTrue(txn.targetAvailable());
        assertTrue(txn.apply());
        assertTrue(txn.confirmed());
        assertTrue(txn.bluetoothPreserved());
        assertTrue(fake.devices.contains(BT));
        assertTrue(txn.rollback());
        assertEquals(1, fake.restores);
        assertTrue(fake.preferred.isEmpty());
        assertSame(BT, fake.selected);
    }

    @Test public void previousExplicitPreferenceIsRestoredExactly() throws Exception {
        FakePort fake = new FakePort();
        fake.preferred.add(BT);
        NativeMediaRouteTransaction txn = new NativeMediaRouteTransaction(fake, SPEAKER);
        assertTrue(txn.apply());
        assertTrue(txn.confirmed());
        assertTrue(txn.rollback());
        assertEquals(Collections.singletonList(BT), fake.preferred);
        assertSame(BT, fake.selected);
    }

    @Test public void noOpAutomaticModeIsNotWritten() throws Exception {
        FakePort fake = new FakePort();
        NativeMediaRouteTransaction txn = new NativeMediaRouteTransaction(fake, null);
        assertTrue(txn.noChangeNeeded());
        assertEquals(0, fake.writes);
    }

    @Test public void disconnectedDeviceIsRejectedBeforeWrite() throws Exception {
        FakePort fake = new FakePort();
        fake.devices.remove(BT);
        NativeMediaRouteTransaction txn = new NativeMediaRouteTransaction(fake, BT);
        assertFalse(txn.targetAvailable());
        assertFalse(txn.apply());
        assertEquals(0, fake.writes);
        assertTrue(txn.rollback());
        assertEquals(0, fake.restores);
    }

    @Test public void disappearingBluetoothInvalidatesConfirmedSpeakerRoute() throws Exception {
        FakePort fake = new FakePort();
        NativeMediaRouteTransaction txn = new NativeMediaRouteTransaction(fake, SPEAKER);
        assertTrue(txn.apply());
        fake.devices.remove(BT);
        assertFalse(txn.bluetoothPreserved());
        assertFalse(txn.confirmed());
        assertTrue(txn.rollback());
        assertEquals(1, fake.restores);
    }

    @Test public void postWriteExceptionStillTriggersExactRollback() throws Exception {
        FakePort fake = new FakePort();
        fake.throwAfterApply = true;
        NativeMediaRouteTransaction txn = new NativeMediaRouteTransaction(fake, SPEAKER);
        try {
            txn.apply();
            fail("Simulated binder exception expected");
        } catch (IllegalStateException expected) {
            assertTrue(txn.rollback());
            assertTrue(fake.preferred.isEmpty());
            assertEquals(1, fake.restores);
        }
    }

    @Test public void rejectedWriteIsStillSafeToRollback() throws Exception {
        FakePort fake = new FakePort();
        fake.denyApply = true;
        NativeMediaRouteTransaction txn = new NativeMediaRouteTransaction(fake, SPEAKER);
        assertFalse(txn.apply());
        assertFalse(txn.confirmed());
        assertTrue(txn.rollback());
        assertEquals(1, fake.restores);
    }

    @Test public void routeMismatchNeverCountsAsSuccess() throws Exception {
        FakePort fake = new FakePort();
        NativeMediaRouteTransaction txn = new NativeMediaRouteTransaction(fake, SPEAKER);
        assertTrue(txn.apply());
        fake.selected = BT; // audio policy did not actually route to requested sink
        assertFalse(txn.confirmed());
        assertTrue(txn.rollback());
    }
}

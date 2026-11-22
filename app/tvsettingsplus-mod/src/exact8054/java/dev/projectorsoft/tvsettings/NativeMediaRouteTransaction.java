package dev.projectorsoft.tvsettings;

import java.util.ArrayList;
import java.util.List;

/** Testable one-shot state machine. Preserves the exact pre-existing preferred-device list. */
final class NativeMediaRouteTransaction {
    interface Port {
        List<Object> preferred() throws Exception;
        List<NativeMediaRouteBridge.Output> connectedBluetooth();
        NativeMediaRouteBridge.Output findConnected(NativeMediaRouteBridge.Output output);
        boolean apply(NativeMediaRouteBridge.Output output) throws Exception;
        boolean preferredIs(NativeMediaRouteBridge.Output output) throws Exception;
        boolean preferredIsAutomatic() throws Exception;
        boolean routeIs(NativeMediaRouteBridge.Output output);
        boolean stillConnected(List<NativeMediaRouteBridge.Output> bluetooth);
        boolean restore(List<Object> original) throws Exception;
        int mediaForce() throws Exception;
        boolean canControlA2dpMedia() throws Exception;
        void allowA2dpMedia(boolean allow) throws Exception;
        boolean mediaForceAllowsA2dp() throws Exception;
        boolean mediaForceDisallowsA2dp() throws Exception;
        default String diagnostic() { return "diagnostic unavailable"; }
    }

    private final Port port;
    private final NativeMediaRouteBridge.Output target;
    private final List<Object> original;
    private final List<NativeMediaRouteBridge.Output> bluetooth;
    private boolean attempted;
    private boolean forceAttempted;
    private final int forceBefore;
    // getForceUse(FOR_MEDIA): FORCE_NONE=0 and FORCE_NO_BT_A2DP=10 per Android 13.
    private static final int NONE = 0;
    private static final int NO_BT = 10;

    NativeMediaRouteTransaction(Port port, NativeMediaRouteBridge.Output target) throws Exception {
        this.port = port;
        this.target = target;
        // Capture before any write. An unreadable original is a hard stop.
        original = new ArrayList<>(port.preferred());
        bluetooth = new ArrayList<>(port.connectedBluetooth());
        forceBefore = port.mediaForce();
        if ((forceBefore != NONE && forceBefore != NO_BT)
                || !port.canControlA2dpMedia()) {
            throw new IllegalStateException("Cannot snapshot/restore MEDIA force policy safely");
        }
    }

    boolean noChangeNeeded() {
        return target == null && original.isEmpty() && forceBefore == NONE;
    }

    boolean targetAvailable() {
        return target == null || port.findConnected(target) != null;
    }

    boolean apply() throws Exception {
        if (attempted) return false;
        if (!targetAvailable()) return false;
        // A binder write may be partially applied before an exception is thrown.
        attempted = true;
        // An earlier explicit Speaker selection might have retained the A2DP
        // exclusion. Switching back to Bluetooth/Automatic must undo that first.
        if (target == null || target.type == android.media.AudioDeviceInfo.TYPE_BLUETOOTH_A2DP) {
            if (port.mediaForceDisallowsA2dp()) {
                forceAttempted = true;
                port.allowA2dpMedia(true);
            }
        }
        return port.apply(target);
    }

    boolean confirmed() throws Exception {
        if (!attempted || !port.stillConnected(bluetooth) || !targetAvailable())
            return false;
        if (target == null)
            return port.preferredIsAutomatic() && port.mediaForceAllowsA2dp();
        if (target.type == android.media.AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)
            return port.preferredIs(target) && port.routeIs(target)
                    && port.mediaForceAllowsA2dp();
        return port.preferredIs(target) && port.routeIs(target);
    }

    /**
     * Invoke only after preferred(Speaker) was accepted but real output is still
     * A2DP. An 8054 may ignore the preferred strategy while A2DP is active.
     */
    boolean trySpeakerForceFallback() throws Exception {
        if (!attempted || forceAttempted || target == null
                || target.type != android.media.AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                || !port.canControlA2dpMedia() || !port.preferredIs(target)) return false;
        forceAttempted = true; // mark before binder write, even if it throws
        port.allowA2dpMedia(false);
        return true;
    }

    boolean speakerForceApplied() throws Exception {
        return forceAttempted && port.mediaForceDisallowsA2dp();
    }

    boolean bluetoothPreserved() {
        return port.stillConnected(bluetooth);
    }

    boolean rollback() throws Exception {
        if (!attempted && !forceAttempted) return true;
        // Restore global force policy first, then previous preferred strategy.
        // A failure is reported instead of silently claiming success.
        // Force-use is updated by AudioService asynchronously. Even if the
        // immediate readback still equals the original value, an earlier
        // queued A2DP exclusion may apply AFTER the rollback. Queue the
        // compensating write in all paths that attempted a force change.
        int current = port.mediaForce();
        if (forceAttempted || current != forceBefore) {
            if (forceBefore != NONE && forceBefore != NO_BT) return false;
            port.allowA2dpMedia(forceBefore == NONE);
        }
        boolean preferenceRestored = port.restore(original);
        // Broker media policy update may be asynchronous; final readback is
        // performed in UI (and may report pending), not asserted immediately.
        return preferenceRestored;
    }

    boolean forceRestored() throws Exception {
        return port.mediaForce() == forceBefore;
    }

    int originalForce() { return forceBefore; }

    String diagnostic() {
        return port.diagnostic();
    }

    List<Object> originalPreference() {
        return new ArrayList<>(original);
    }
}

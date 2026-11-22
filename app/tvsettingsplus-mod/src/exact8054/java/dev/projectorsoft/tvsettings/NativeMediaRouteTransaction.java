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
    }

    private final Port port;
    private final NativeMediaRouteBridge.Output target;
    private final List<Object> original;
    private final List<NativeMediaRouteBridge.Output> bluetooth;
    private boolean attempted;

    NativeMediaRouteTransaction(Port port, NativeMediaRouteBridge.Output target) throws Exception {
        this.port = port;
        this.target = target;
        // Capture before any write. An unreadable original is a hard stop.
        original = new ArrayList<>(port.preferred());
        bluetooth = new ArrayList<>(port.connectedBluetooth());
    }

    boolean noChangeNeeded() {
        return target == null && original.isEmpty();
    }

    boolean targetAvailable() {
        return target == null || port.findConnected(target) != null;
    }

    boolean apply() throws Exception {
        if (attempted) return false;
        if (!targetAvailable()) return false;
        // A binder write may be partially applied before an exception is thrown.
        attempted = true;
        return port.apply(target);
    }

    boolean confirmed() throws Exception {
        if (!attempted || !port.stillConnected(bluetooth) || !targetAvailable())
            return false;
        return target == null ? port.preferredIsAutomatic()
                : port.preferredIs(target) && port.routeIs(target);
    }

    boolean bluetoothPreserved() {
        return port.stillConnected(bluetooth);
    }

    boolean rollback() throws Exception {
        return !attempted || port.restore(original);
    }

    List<Object> originalPreference() {
        return new ArrayList<>(original);
    }
}

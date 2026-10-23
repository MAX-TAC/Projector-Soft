package dev.projectorsoft.tvsettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Light-source brightness is a discrete OEM hardware setting, NOT the
 * Android picture-brightness slider.
 *
 * Headless controller for the right-hand TvSettingsPlus preference pane.
 * The runtime adapter must be backed by a VERIFIED vendor control path.
 * This class deliberately contains no guessed TRD/I2C constants or shell calls.
 */
public final class LightSourceBrightnessController {
    public static final int EXPECTED_MODE_COUNT = 4;

    public static final class Mode {
        public final int hardwareId;
        public final String title;

        public Mode(int hardwareId, String title) {
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("A mode must have a verified OEM title");
            }
            this.hardwareId = hardwareId;
            this.title = title;
        }
    }

    public interface VendorGateway {
        /** Must not synthesize IDs or titles from assumptions. */
        List<Mode> readSupportedModes() throws Exception;

        /** Reads actual active hardware mode, not an app preference. */
        int readCurrentModeId() throws Exception;

        /** Changes optical light-source hardware mode only. */
        void applyMode(int hardwareId) throws Exception;
    }

    public static final class Snapshot {
        public final boolean enabled;
        public final List<Mode> choices;
        public final Integer currentId;
        public final Integer pendingId;
        public final String error;

        private Snapshot(boolean enabled, List<Mode> choices, Integer currentId, Integer pendingId, String error) {
            this.enabled = enabled;
            this.choices = Collections.unmodifiableList(new ArrayList<>(choices));
            this.currentId = currentId;
            this.pendingId = pendingId;
            this.error = error;
        }
    }

    private final ProjectorCapabilities capabilities;
    private final VendorGateway gateway;
    private Snapshot snapshot = new Snapshot(false, Collections.<Mode>emptyList(), null, null, "Not loaded");
    private Integer pendingId = null;

    public LightSourceBrightnessController(ProjectorCapabilities capabilities, VendorGateway gateway) {
        this.capabilities = Objects.requireNonNull(capabilities, "capabilities");
        this.gateway = Objects.requireNonNull(gateway, "gateway");
    }

    /**
     * Never shows a writable three-option UI if the specific hardware/API is
     * not verified, the mode table isn't exactly three distinct OEM IDs,
     * or the hardware's currently active mode can't be queried.
     */
    public Snapshot refresh() {
        if (!capabilities.supports(ProjectorCapabilities.Feature.LIGHT_SOURCE_MODE)) {
            return snapshot = new Snapshot(false, Collections.<Mode>emptyList(), null, null,
                    "Light source API is not verified for this board");
        }
        try {
            List<Mode> modes = gateway.readSupportedModes();
            if (modes == null || modes.size() != EXPECTED_MODE_COUNT) {
                return snapshot = new Snapshot(false, Collections.<Mode>emptyList(), null, null,
                        "Expected exactly four verified OEM modes for 8052");
            }
            Set<Integer> unique = new HashSet<>();
            for (Mode mode : modes) {
                if (mode == null || mode.hardwareId < 0 || mode.hardwareId > 3 || !unique.add(mode.hardwareId)) {
                    return snapshot = new Snapshot(false, Collections.<Mode>emptyList(), null, null,
                            "Duplicate or null OEM brightness mode");
                }
            }
            for (int i=0;i<EXPECTED_MODE_COUNT;i++) {
                if (!unique.contains(i)) return snapshot = new Snapshot(false, Collections.<Mode>emptyList(), null, null,
                    "Required OEM brightness mode is missing: " + i);
            }
            int current = gateway.readCurrentModeId();
            if (!unique.contains(current)) {
                return snapshot = new Snapshot(false, Collections.<Mode>emptyList(), null, null,
                        "Current OEM brightness mode is not in the supported table");
            }
            if (pendingId != null && current == pendingId.intValue()) pendingId = null;
            return snapshot = new Snapshot(true, modes, current, pendingId, null);
        } catch (Exception e) {
            return snapshot = new Snapshot(false, Collections.<Mode>emptyList(), null, null,
                    "Failed reading OEM brightness state: " + e.getClass().getSimpleName());
        }
    }

    /**
     * Called only on user action in the right-hand pane.
     * An Android broadcast is asynchronous: success indicates dispatch, not
     * confirmation. Call refresh() after a delay and check pendingId.
     */
    public boolean select(int hardwareId) {
        if (!snapshot.enabled) return false;
        boolean available = false;
        for (Mode mode : snapshot.choices) {
            if (mode.hardwareId == hardwareId) available = true;
        }
        if (!available) return false;
        if (snapshot.currentId != null && snapshot.currentId == hardwareId) return true;
        if (pendingId != null) return false;
        try {
            gateway.applyMode(hardwareId);
            pendingId = hardwareId;
            snapshot = new Snapshot(true, snapshot.choices, snapshot.currentId, pendingId, null);
            return true;
        } catch (Exception e) {
            pendingId = null;
            refresh();
            return false;
        }
    }

    /** The right pane calls this if the OEM broadcast did not confirm in time. */
    public boolean abandonUnconfirmedRequest() {
        boolean confirmed = pendingId == null;
        pendingId = null;
        refresh();
        return confirmed;
    }

    public Snapshot currentSnapshot() { return snapshot; }
}

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
    public static final int EXPECTED_MODE_COUNT = 3;

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
        public final String error;

        private Snapshot(boolean enabled, List<Mode> choices, Integer currentId, String error) {
            this.enabled = enabled;
            this.choices = Collections.unmodifiableList(new ArrayList<>(choices));
            this.currentId = currentId;
            this.error = error;
        }
    }

    private final ProjectorCapabilities capabilities;
    private final VendorGateway gateway;
    private Snapshot snapshot = new Snapshot(false, Collections.<Mode>emptyList(), null, "Not loaded");

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
            return snapshot = new Snapshot(false, Collections.<Mode>emptyList(), null,
                    "Light source API is not verified for this board");
        }
        try {
            List<Mode> modes = gateway.readSupportedModes();
            if (modes == null || modes.size() != EXPECTED_MODE_COUNT) {
                return snapshot = new Snapshot(false, Collections.<Mode>emptyList(), null,
                        "Expected exactly three verified OEM modes");
            }
            Set<Integer> unique = new HashSet<>();
            for (Mode mode : modes) {
                if (mode == null || !unique.add(mode.hardwareId)) {
                    return snapshot = new Snapshot(false, Collections.<Mode>emptyList(), null,
                            "Duplicate or null OEM brightness mode");
                }
            }
            int current = gateway.readCurrentModeId();
            if (!unique.contains(current)) {
                return snapshot = new Snapshot(false, Collections.<Mode>emptyList(), null,
                        "Current OEM brightness mode is not in the supported table");
            }
            return snapshot = new Snapshot(true, modes, current, null);
        } catch (Exception e) {
            return snapshot = new Snapshot(false, Collections.<Mode>emptyList(), null,
                    "Failed reading OEM brightness state: " + e.getClass().getSimpleName());
        }
    }

    /**
     * Called by right-pane single-choice UI on user interaction only.
     * Read back the selected value before reporting success.
     * No shared preference is used as an authoritative hardware state.
     */
    public boolean select(int hardwareId) {
        if (!snapshot.enabled) return false;
        boolean available = false;
        for (Mode mode : snapshot.choices) {
            if (mode.hardwareId == hardwareId) available = true;
        }
        if (!available) return false;
        if (snapshot.currentId != null && snapshot.currentId == hardwareId) return true;
        try {
            gateway.applyMode(hardwareId);
            int actual = gateway.readCurrentModeId();
            if (actual != hardwareId) {
                refresh();
                return false;
            }
            snapshot = new Snapshot(true, snapshot.choices, actual, null);
            return true;
        } catch (Exception e) {
            refresh();
            return false;
        }
    }

    public Snapshot currentSnapshot() { return snapshot; }
}

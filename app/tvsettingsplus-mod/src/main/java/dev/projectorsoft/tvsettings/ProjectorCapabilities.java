package dev.projectorsoft.tvsettings;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Fail-closed capability model shared by 8052/8054.
 *
 * This class does not touch vendor hardware. UI adapters may show a control
 * only if both a physical device and a verified OEM implementation exist.
 * "Verified" means tested on the matching firmware/hardware, not just a
 * similarly named method present in a JADX listing.
 */
public final class ProjectorCapabilities {
    public enum Feature {
        INPUT_ANDROID, HDMI_1, HDMI_2,
        PICTURE_BRIGHTNESS, PICTURE_CONTRAST, PICTURE_COLOR,
        HDR_CONTROL, MEMC_CONTROL, GAME_LOW_LATENCY,
        AUDIO_EQUALIZER_5_BAND, AUDIO_OUTPUT,
        FOCUS_AUTO, FOCUS_MANUAL, KEYSTONE_AUTO, KEYSTONE_FOUR_CORNER,
        DIGITAL_ZOOM, ORIENTATION, OTA_CHECK, CPU_THERMAL_READ
    }

    private final String machineId;
    private final int physicalHdmiCount;
    private final Set<Feature> verified;

    private ProjectorCapabilities(String machineId, int physicalHdmiCount, Set<Feature> verified) {
        this.machineId = machineId;
        this.physicalHdmiCount = physicalHdmiCount;
        this.verified = Collections.unmodifiableSet(EnumSet.copyOf(verified));
    }

    public static ProjectorCapabilities from(String machineId, int reportedHdmiCount,
                                             Set<Feature> hardwareVerifiedFeatures) {
        Objects.requireNonNull(hardwareVerifiedFeatures, "hardwareVerifiedFeatures");
        String id = machineId == null ? "" : machineId.trim();
        int knownMaximum = "8052".equals(id) ? 1 : "8054".equals(id) ? 2 : 0;
        // No unsupported HDMI2 even if a vendor property misreports > 1 on 8052.
        // An absent port-count property does NOT prove a port is present.
        int physicalCount = Math.max(0, Math.min(knownMaximum, reportedHdmiCount));
        EnumSet<Feature> copied = EnumSet.noneOf(Feature.class);
        copied.addAll(hardwareVerifiedFeatures);
        return new ProjectorCapabilities(id, physicalCount, copied);
    }

    public boolean supports(Feature feature) {
        Objects.requireNonNull(feature, "feature");
        switch (feature) {
            case INPUT_ANDROID: return true;
            case HDMI_1: return physicalHdmiCount >= 1;
            case HDMI_2: return physicalHdmiCount >= 2 && "8054".equals(machineId);
            default: return verified.contains(feature);
        }
    }

    public String machineId() { return machineId; }
    public int hdmiPortCount() { return physicalHdmiCount; }
    public Set<Feature> verifiedFeatures() { return verified; }

    public Set<Feature> visibleControls() {
        EnumSet<Feature> result = EnumSet.noneOf(Feature.class);
        for (Feature f : Feature.values()) {
            if (supports(f)) result.add(f);
        }
        return Collections.unmodifiableSet(result);
    }
}

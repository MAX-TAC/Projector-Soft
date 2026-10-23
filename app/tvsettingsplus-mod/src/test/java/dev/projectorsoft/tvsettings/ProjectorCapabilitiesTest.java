package dev.projectorsoft.tvsettings;

import java.util.EnumSet;

public final class ProjectorCapabilitiesTest {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        ProjectorCapabilities unknown = ProjectorCapabilities.from("", 4,
                EnumSet.noneOf(ProjectorCapabilities.Feature.class));
        check(unknown.supports(ProjectorCapabilities.Feature.INPUT_ANDROID), "Android source");
        check(!unknown.supports(ProjectorCapabilities.Feature.HDMI_1), "Unknown device HDMI");
        check(!unknown.supports(ProjectorCapabilities.Feature.MEMC_CONTROL), "Unknown MEMC");

        ProjectorCapabilities rev8052 = ProjectorCapabilities.from("8052", 2,
                EnumSet.noneOf(ProjectorCapabilities.Feature.class));
        check(rev8052.supports(ProjectorCapabilities.Feature.HDMI_1), "8052 HDMI1");
        check(!rev8052.supports(ProjectorCapabilities.Feature.HDMI_2), "8052 HDMI2 phantom");
        check(!rev8052.supports(ProjectorCapabilities.Feature.HDR_CONTROL), "HDR not proven");
        check(!rev8052.supports(ProjectorCapabilities.Feature.AUDIO_EQUALIZER_5_BAND), "EQ not proven");
        check(!rev8052.supports(ProjectorCapabilities.Feature.FOCUS_AUTO), "focus not proven");

        ProjectorCapabilities rev8054 = ProjectorCapabilities.from("8054", 2,
                EnumSet.of(ProjectorCapabilities.Feature.FOCUS_AUTO));
        check(rev8054.supports(ProjectorCapabilities.Feature.HDMI_2), "8054 confirmed HDMI2");
        check(rev8054.supports(ProjectorCapabilities.Feature.FOCUS_AUTO), "8054 verified focus");
        check(!rev8054.supports(ProjectorCapabilities.Feature.KEYSTONE_AUTO), "keystone unverified");

        ProjectorCapabilities incomplete8054 = ProjectorCapabilities.from("8054", 1,
                EnumSet.noneOf(ProjectorCapabilities.Feature.class));
        check(!incomplete8054.supports(ProjectorCapabilities.Feature.HDMI_2), "Do not show unconfirmed HDMI2");

        ProjectorCapabilities unreal = ProjectorCapabilities.from("8053", 2,
                EnumSet.noneOf(ProjectorCapabilities.Feature.class));
        check(!unreal.supports(ProjectorCapabilities.Feature.HDMI_1), "8053 is not identified with D11");

        check(rev8054.visibleControls().contains(ProjectorCapabilities.Feature.FOCUS_AUTO), "UI capabilities");
        System.out.println("PASS: variant + vendor capability gating for 8052, 8054, unknown");
    }
}

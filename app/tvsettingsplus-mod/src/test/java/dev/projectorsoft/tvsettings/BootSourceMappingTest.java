package dev.projectorsoft.tvsettings;

import java.util.List;

/** Pure JVM guardrails for factory SourceOnBoot mappings. */
public final class BootSourceMappingTest {
    private static void check(boolean test,String message) {
        if(!test)throw new AssertionError(message);
    }
    public static void main(String[] args) {
        List<BootSourceMapping.Entry> one=
            BootSourceMapping.candidates(1,"HW5:HDMI 1",false);
        check(one.size()==3,"8052 must offer Android, exactly one HDMI, USB");
        check(one.get(0).id.equals("HOME"),"OEM HOME mapping");
        check(one.get(1).hdmiHardwarePort.equals("HW5"),"OEM hardware HDMI port");
        check(one.get(2).id.equals("USB"),"USB must remain boot option");
        check(BootSourceMapping.isKnownState(one,"com.mediatek.tvinput/.hdmi.HDMIInputService/HW5"),
              "Recognize original MediaTek HDMI input IDs");
        check(!BootSourceMapping.isKnownState(one,
              "com.mediatek.tvinput/.hdmi.HDMIInputService/HW6"),
              "8052 HDMI 2 must NEVER be selectable");
        check(!BootSourceMapping.isKnownState(one,"garbage"),"Reject unknown persisted state");

        List<BootSourceMapping.Entry> two=
            BootSourceMapping.candidates(2,"HW5:HDMI 1,HW7:HDMI 2",false);
        check(two.size()==4,"8054 could offer 2 HDMI only when board metadata confirms");
        check(two.get(2).hdmiHardwarePort.equals("HW7"),"Honor board-specific port map");
        check(BootSourceMapping.candidates(0,"HW5:HDMI 1",false).isEmpty(),
                "Do not use factory fallback four HDMI when board count unknown");
        check(BootSourceMapping.candidates(2,"HW5:HDMI 1",false).isEmpty(),
                "Missing board port metadata must hide controller");
        check(BootSourceMapping.candidates(2,"HW5:HDMI 1,HW5:HDMI 2",false).isEmpty(),
                "Duplicate hardware HDMI mapping must reject controller");
        check(BootSourceMapping.candidates(1,"bad:HDMI 1",false).isEmpty(),
                "Reject malformed hardware port");
        check(BootSourceMapping.candidates(1,"HW5:HDMI 1",true).size()==4,
                "AV should only be shown when hardware flags confirm");
        System.out.println("PASS BootSourceMapping: board-gated source-on-boot/8052/8054, no phantom HDMI");
    }
}

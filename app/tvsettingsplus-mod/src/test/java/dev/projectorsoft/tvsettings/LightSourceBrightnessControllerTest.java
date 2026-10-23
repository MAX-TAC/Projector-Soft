package dev.projectorsoft.tvsettings;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public final class LightSourceBrightnessControllerTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static final class Stub implements LightSourceBrightnessController.VendorGateway {
        int actual=20;
        int applied=-1;
        boolean throwOnApply=false;
        boolean noWriteEffect=false;
        List<LightSourceBrightnessController.Mode> modes=Arrays.asList(
                new LightSourceBrightnessController.Mode(10,"Mode A"),
                new LightSourceBrightnessController.Mode(20,"Mode B"),
                new LightSourceBrightnessController.Mode(30,"Mode C"));

        @Override public List<LightSourceBrightnessController.Mode> readSupportedModes() { return modes; }
        @Override public int readCurrentModeId() { return actual; }
        @Override public void applyMode(int id) throws Exception {
            if (throwOnApply) throw new IllegalStateException("HAL failed");
            applied=id;
            if (!noWriteEffect) actual=id;
        }
    }

    public static void main(String[] args) {
        Stub gateway = new Stub();
        ProjectorCapabilities unverified=ProjectorCapabilities.from("8052",1,
                EnumSet.noneOf(ProjectorCapabilities.Feature.class));
        LightSourceBrightnessController c=new LightSourceBrightnessController(unverified,gateway);
        check(!c.refresh().enabled,"Unknown HAL must not show writable UI");
        check(!c.select(30),"Unknown HAL must not write");
        check(gateway.applied == -1, "No hardware writes without capability");

        ProjectorCapabilities verified=ProjectorCapabilities.from("8052",1,
                EnumSet.of(ProjectorCapabilities.Feature.LIGHT_SOURCE_MODE));
        c=new LightSourceBrightnessController(verified,gateway);
        check(c.refresh().enabled,"Valid 3 OEM modes should be selectable");
        check(c.currentSnapshot().currentId==20,"Read actual mode from hardware");
        check(!c.select(100),"Unknown OEM ID must be rejected");
        check(c.select(10),"Switch to verified mode");
        check(c.currentSnapshot().currentId==10,"Read back selected hardware state");
        gateway.noWriteEffect=true;
        check(!c.select(30),"HAL pretending to accept command must fail readback");
        check(c.currentSnapshot().currentId==10,"Keep actual mode on failure");
        gateway.noWriteEffect=false;
        gateway.throwOnApply=true;
        check(!c.select(20),"HAL exception must be gracefully rejected");
        gateway.throwOnApply=false;
        gateway.modes=Arrays.asList(new LightSourceBrightnessController.Mode(10,"Duplicate"),
                new LightSourceBrightnessController.Mode(10,"Duplicate"),
                new LightSourceBrightnessController.Mode(30,"Three"));
        check(!c.refresh().enabled,"Duplicate backend mode ID must fail closed");
        gateway.modes=Arrays.asList(new LightSourceBrightnessController.Mode(10,"Only one"));
        check(!c.refresh().enabled,"Do not invent missing OEM modes");

        System.out.println("PASS LightSourceBrightnessController: mode list, readback, API gating, no optimistic UI");
    }
}

package dev.projectorsoft.tvsettings;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public final class LightSourceBrightnessControllerTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static final class Stub implements LightSourceBrightnessController.VendorGateway {
        int actual=1;
        int applied=-1;
        boolean throwOnApply=false;
        boolean noWriteEffect=false;
        List<LightSourceBrightnessController.Mode> modes=Arrays.asList(
                new LightSourceBrightnessController.Mode(0,"Повышенная"),
                new LightSourceBrightnessController.Mode(1,"Стандартная"),
                new LightSourceBrightnessController.Mode(2,"Тихая"),
                new LightSourceBrightnessController.Mode(3,"Высокогорная"));

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
        check(!c.select(3),"Unknown HAL must not write");
        check(gateway.applied == -1, "No hardware writes without capability");

        ProjectorCapabilities verified=ProjectorCapabilities.from("8052",1,
                EnumSet.of(ProjectorCapabilities.Feature.LIGHT_SOURCE_MODE));
        c=new LightSourceBrightnessController(verified,gateway);
        check(c.refresh().enabled,"Four verified OEM modes should be selectable");
        check(c.currentSnapshot().currentId==1,"Read actual mode from hardware");
        check(!c.select(100),"Unknown OEM ID must be rejected");
        check(c.select(0),"Dispatch verified OEM mode");
        check(c.currentSnapshot().currentId==1,"Do not optimistically update hardware state");
        check(c.currentSnapshot().pendingId==0,"Show pending OEM action");
        check(!c.select(2),"No second action during pending");
        check(c.refresh().enabled,"Deferred readback is permitted");
        check(c.currentSnapshot().currentId==0 && c.currentSnapshot().pendingId==null,"Confirm asynchronous readback");
        gateway.noWriteEffect=true;
        check(c.select(3),"Broadcast send may succeed even if HAL is broken");
        c.refresh();
        check(c.currentSnapshot().currentId==0 && c.currentSnapshot().pendingId==3,"No optimistic confirmation");
        check(!c.abandonUnconfirmedRequest(),"Unconfirmed event should time out and fail");
        check(c.currentSnapshot().currentId==0,"Keep hardware mode on timeout");
        gateway.noWriteEffect=false;
        gateway.throwOnApply=true;
        check(!c.select(1),"Broadcast dispatch failure must be rejected");
        gateway.throwOnApply=false;
        gateway.modes=Arrays.asList(new LightSourceBrightnessController.Mode(0,"Duplicate"),
                new LightSourceBrightnessController.Mode(0,"Duplicate"),
                new LightSourceBrightnessController.Mode(2,"Third"),
                new LightSourceBrightnessController.Mode(3,"Fourth"));
        check(!c.refresh().enabled,"Duplicate backend mode ID must fail closed");
        gateway.modes=Arrays.asList(new LightSourceBrightnessController.Mode(0,"Only one"));
        check(!c.refresh().enabled,"Do not invent missing OEM modes");

        System.out.println("PASS LightSourceBrightnessController: 4 OEM modes, deferred confirmation, capability gating, safe failure");
    }
}

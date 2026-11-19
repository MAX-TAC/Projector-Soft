package dev.projectorsoft.tvsettings;

public final class OemAcPowerBootPolicyTest {
    static final class Fake implements OemAcPowerBootPolicy.Gateway {
        Boolean state;
        int writes;
        boolean ignoreWrite;
        boolean failRead;
        Fake(Boolean initial) { state=initial; }
        @Override public Boolean read() throws Exception {
            if(failRead)throw new Exception("blocked");
            return state;
        }
        @Override public void write(boolean enabled) {writes++; if(!ignoreWrite)state=enabled;}
    }
    private static void ok(boolean v,String what) {
        if(!v)throw new AssertionError(what);
    }
    public static void main(String[] args) {
        Fake absent=new Fake(null);
        ok(!OemAcPowerBootPolicy.available(absent),"missing MtkUtil state");
        ok(!OemAcPowerBootPolicy.apply(absent,true),"do not change unknown state");
        ok(absent.writes==0,"no write if unknown");
        Fake factory=new Fake(false);
        ok(OemAcPowerBootPolicy.available(factory),"factory getter");
        ok(OemAcPowerBootPolicy.apply(factory,true),"confirmed write");
        ok(Boolean.TRUE.equals(factory.state) && factory.writes==1,"written to OEM");
        ok(OemAcPowerBootPolicy.apply(factory,true) && factory.writes==1,"idempotent");
        ok(!OemAcPowerBootPolicy.apply(factory,"true"),"reject nonboolean");
        Fake blocked=new Fake(false);
        blocked.ignoreWrite=true;
        ok(!OemAcPowerBootPolicy.apply(blocked,true),"reject mismatched readback");
        ok(blocked.writes==1 && Boolean.FALSE.equals(blocked.state),"not falsely checked");
        Fake error=new Fake(false);
        error.failRead=true;
        ok(!OemAcPowerBootPolicy.available(error),"getter exception");
        ok(!OemAcPowerBootPolicy.apply(error,true) && error.writes==0,"no unsafe write");
        System.out.println("PASS: vendor electricity-boot state verification, no phantom success");
    }
}

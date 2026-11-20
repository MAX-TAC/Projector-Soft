package dev.projectorsoft.tvsettings;

public final class Pic04ContrastStepTest {
    private static final class Fake implements Pic04ContrastStep.Port {
        String source="HDMI1", signal="SDR", profile="Standard", generation="read-7";
        boolean trusted=true, journalOk=true, providerOk=true, switchDuringRead;
        int mode=7,temp=2,hdr=0,global=40,mtk=40,min=0,max=100,step=2;
        int writes, savedBaseline=-1, reads;
        String savedContext;
        public Pic04ContrastStep.Reading read() {
            reads++;
            if (switchDuringRead && reads==2) generation="read-8";
            return new Pic04ContrastStep.Reading(source,signal,profile,generation,trusted,
                    mode,temp,hdr,global,mtk,min,max,step);
        }
        public boolean preserveOriginal(String context,int baseline) {
            if (!journalOk) return false;
            if (savedContext!=null) return savedContext.equals(context);
            savedContext=context;
            savedBaseline=baseline;
            return true;
        }
        public boolean submitGlobalContrast(int desired) {
            writes++;
            if (!providerOk) return false;
            global=desired;  // only provider echo: MediaTek remains at old value
            return true;
        }
    }
    private static void check(boolean ok,String message) {
        if (!ok) throw new AssertionError(message);
    }
    private static void status(Pic04ContrastStep.Result r,Pic04ContrastStep.Status expected) {
        check(r.status==expected,expected+" was expected, got "+r.status);
    }

    public static void main(String[] args) {
        Fake f=new Fake();
        Pic04ContrastStep c=new Pic04ContrastStep(f);
        status(c.step(1),Pic04ContrastStep.Status.PENDING);
        check(f.writes==1 && f.global==42 && f.mtk==40 && f.savedBaseline==40,
                "Stock key must be submitted only once; do not infer MediaTek completion");
        status(c.step(1),Pic04ContrastStep.Status.ALREADY_PENDING);
        status(c.verify(),Pic04ContrastStep.Status.PENDING);
        f.mtk=42;
        status(c.verify(),Pic04ContrastStep.Status.CONFIRMED_BACKEND_ONLY);
        check(f.writes==1 && f.savedBaseline==40,"journal must survive a confirmed step");
        status(c.step(-1),Pic04ContrastStep.Status.PENDING);
        check(f.writes==2 && f.global==40 && f.savedBaseline==40,"negative step reuses original");

        f=new Fake();f.trusted=false;
        status(new Pic04ContrastStep(f).step(1),Pic04ContrastStep.Status.UNKNOWN_CONTEXT);
        check(f.writes==0 && f.savedContext==null,"global and profile labels are insufficient");

        f=new Fake();f.source=null;
        status(new Pic04ContrastStep(f).step(1),Pic04ContrastStep.Status.UNKNOWN_CONTEXT);
        check(f.writes==0,"missing actual input must block provider");

        f=new Fake();f.switchDuringRead=true;
        status(new Pic04ContrastStep(f).step(1),Pic04ContrastStep.Status.CONTEXT_CHANGED);
        check(f.writes==0 && f.savedContext==null,"source generation drift must block");

        f=new Fake();f.mtk=39;
        status(new Pic04ContrastStep(f).step(1),Pic04ContrastStep.Status.VALUE_MISMATCH);
        check(f.writes==0,"ACFG mismatch must block");

        f=new Fake();f.step=0;
        status(new Pic04ContrastStep(f).step(1),Pic04ContrastStep.Status.INVALID_BOUNDS);
        check(f.writes==0,"unknown OEM increment must block");

        f=new Fake();f.global=100;f.mtk=100;
        status(new Pic04ContrastStep(f).step(1),Pic04ContrastStep.Status.OUT_OF_RANGE);
        check(f.writes==0,"out-of-range step must not clamp or write");

        f=new Fake();f.journalOk=false;
        status(new Pic04ContrastStep(f).step(-1),Pic04ContrastStep.Status.BASELINE_NOT_SAVED);
        check(f.writes==0,"no durable baseline means no provider write");

        f=new Fake();f.providerOk=false;
        c=new Pic04ContrastStep(f);
        status(c.step(1),Pic04ContrastStep.Status.PROVIDER_REFUSED);
        status(c.step(1),Pic04ContrastStep.Status.UNCONFIRMED_LOCKED);
        check(f.writes==1 && f.savedBaseline==40,"refused write cannot replay blindly");

        f=new Fake();c=new Pic04ContrastStep(f);
        status(c.step(1),Pic04ContrastStep.Status.PENDING);
        f.source="HDMI2";
        status(c.verify(),Pic04ContrastStep.Status.UNCONFIRMED_LOCKED);
        check(f.savedBaseline==40 && f.writes==1,"context changed after provider write: preserve baseline");
        status(c.step(1),Pic04ContrastStep.Status.UNCONFIRMED_LOCKED);

        f=new Fake();c=new Pic04ContrastStep(f);
        status(c.step(1),Pic04ContrastStep.Status.PENDING);
        c.detach();
        status(c.step(1),Pic04ContrastStep.Status.UNCONFIRMED_LOCKED);
        check(f.writes==1,"closing panel never auto-restores or repeats pending write");

        f=new Fake();
        status(new Pic04ContrastStep(f).step(2),Pic04ContrastStep.Status.INVALID_DIRECTION);
        check(f.writes==0,"only one explicit LEFT/RIGHT action");
        System.out.println("PASS PIC04 contract: one step, stable context, durable baseline, async readback, no blind restore");
    }
}

package dev.projectorsoft.tvsettings;

public final class SoundDelayStateTest {
    private static final class Fake implements SoundDelayState.Backend {
        int min=-2, max=14, applied=1, writes;
        String global="10", soundContext="standard";
        boolean readFails, refuse, sync, throwAfterWrite;

        @Override public SoundDelayState.Snapshot read() {
            if (readFails) throw new IllegalStateException("getter denied");
            return new SoundDelayState.Snapshot(min,max,global,applied,soundContext);
        }

        @Override public boolean writeProvider(int milliseconds) {
            writes++;
            if (refuse) return false;
            global=Integer.toString(milliseconds);
            if (throwAfterWrite) throw new IllegalStateException("ambiguous provider outcome");
            if (sync) applied=milliseconds/SoundDelayState.STEP_MS;
            return true;
        }
    }

    private static void check(boolean valid,String message) {
        if(!valid)throw new AssertionError(message);
    }

    public static void main(String[] args) {
        check(SoundDelayState.stockOffersSpdif(java.util.Arrays.asList("sound_spdif_delay"))
                && !SoundDelayState.stockOffersSpdif(java.util.Collections.emptyList()),"stock SPDIF availability");
        check(SoundDelayState.stockOffersSpeaker(java.util.Arrays.asList("sound_speaker_delay"),"true")
                && !SoundDelayState.stockOffersSpeaker(java.util.Arrays.asList("sound_speaker_delay"),"false")
                && !SoundDelayState.stockOffersSpeaker(java.util.Collections.emptyList(),"true"),
                "speaker requires both stock menu membership and factory show-all flag");
        Fake delay=new Fake();
        SoundDelayState.Snapshot displayed=delay.read();
        check(displayed.synchronizedValue() && displayed.minMs==-20 && displayed.maxMs==140,"OEM scaling");
        check(SoundDelayState.change(delay,displayed,11)==SoundDelayState.Write.STALE
                && delay.writes==0,"no off-step write");
        check(SoundDelayState.change(delay,displayed,150)==SoundDelayState.Write.STALE
                && delay.writes==0,"range");
        delay.max=20;
        check(SoundDelayState.change(delay,displayed,30)==SoundDelayState.Write.STALE
                && delay.writes==0,"range changed after page opened");
        delay.max=14;
        delay.soundContext="music";
        check(SoundDelayState.change(delay,displayed,30)==SoundDelayState.Write.STALE
                && delay.writes==0,"sound mode or output option changed");
        delay.soundContext="standard";
        delay.readFails=true;
        check(SoundDelayState.change(delay,displayed,30)==SoundDelayState.Write.STALE
                && delay.writes==0,"denied read prevents action");
        delay.readFails=false;
        delay.refuse=true;
        check(SoundDelayState.change(delay,displayed,30)==SoundDelayState.Write.REFUSED && delay.applied==1,
                "provider refused");
        delay.refuse=false;
        check(SoundDelayState.change(delay,displayed,30)==SoundDelayState.Write.PENDING && delay.applied==1,
                "provider readback alone cannot confirm application");
        check(SoundDelayState.change(delay,delay.read(),40)==SoundDelayState.Write.STALE
                && delay.writes==2,"do not write while awaiting OEM readback");
        delay.applied=3;
        check(SoundDelayState.observe(delay,displayed,30)==SoundDelayState.Write.APPLIED && delay.writes==2,
                "async confirmation only reads");
        delay.global="40";
        check(SoundDelayState.observe(delay,displayed,30)==SoundDelayState.Write.UNCERTAIN,
                "external edit supersedes pending confirmation");
        delay.global="30"; delay.sync=true;
        check(SoundDelayState.change(delay,delay.read(),10)==SoundDelayState.Write.APPLIED && delay.applied==1,
                "restore explicitly from latest OEM state");
        delay.throwAfterWrite=true;
        check(SoundDelayState.change(delay,delay.read(),20)==SoundDelayState.Write.UNCERTAIN
                && delay.writes==4 && delay.applied==1,"no replay after possible write");
        delay.global=null;
        check(!delay.read().synchronizedValue() && delay.read().storedMs==null,"missing is not zero");
        System.out.println("PASS delay scaling, bounds, stale state, async apply, refusal, explicit restore");
    }
}

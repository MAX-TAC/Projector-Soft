package dev.projectorsoft.tvsettings;

public final class CecAutoOff8054Test {
    private static final class Backend implements CecAutoOff8054.Backend {
        int cec=1, standby, writes;
        boolean reject, failAfterWrite, loseCec, readFails;
        int earc=1; // The auto-off route has no setter for related audio state.

        @Override public CecAutoOff8054.State read() throws Exception {
            if (readFails) throw new IllegalStateException("getter denied");
            return CecAutoOff8054.State.decode(cec,standby);
        }

        @Override public void setAutoOff(boolean enabled) throws Exception {
            writes++;
            if (!reject) standby=enabled?1:0;
            if (loseCec) cec=0;
            if (failAfterWrite) throw new IllegalStateException("remote failed after mutation");
        }
    }

    private static void check(boolean ok,String reason) {
        if (!ok) throw new AssertionError(reason);
    }

    public static void main(String[] args) throws Exception {
        Backend good=new Backend();
        check(CecAutoOff8054.apply(good,false,true)==CecAutoOff8054.Result.APPLIED
                && good.read().autoOff && good.earc==1 && good.cec==1,"verified stock setting only");
        check(CecAutoOff8054.apply(good,false,false)==CecAutoOff8054.Result.STALE
                && good.writes==1,"old open page cannot write");
        check(CecAutoOff8054.apply(good,true,false)==CecAutoOff8054.Result.APPLIED
                && !good.read().autoOff && good.writes==2,"explicit restoration from refreshed state");

        Backend missing=new Backend();
        missing.cec=0;
        check(CecAutoOff8054.apply(missing,false,true)==CecAutoOff8054.Result.STALE
                && missing.writes==0,"CEC off blocks the standby setting");
        missing.cec=1;
        missing.standby=-1;
        check(CecAutoOff8054.apply(missing,false,true)==CecAutoOff8054.Result.STALE
                && missing.writes==0,"unknown getter must not imply a valid snapshot");
        missing.readFails=true;
        check(CecAutoOff8054.apply(missing,false,true)==CecAutoOff8054.Result.STALE
                && missing.writes==0,"denied getter blocks setting");

        Backend refused=new Backend();
        refused.reject=true;
        check(CecAutoOff8054.apply(refused,false,true)==CecAutoOff8054.Result.REFUSED
                && refused.standby==0 && refused.writes==1,"ignored write is not confirmed");

        Backend partial=new Backend();
        partial.failAfterWrite=true;
        check(CecAutoOff8054.apply(partial,false,true)==CecAutoOff8054.Result.UNCERTAIN
                && partial.standby==1 && partial.writes==1,"no automatic replay after possible mutation");
        partial.failAfterWrite=false;
        partial.loseCec=true;
        check(CecAutoOff8054.apply(partial,true,false)==CecAutoOff8054.Result.UNCERTAIN
                && partial.writes==2,"CEC changed during operation");
        check(CecAutoOff8054.apply(partial,false,true)==CecAutoOff8054.Result.STALE
                && partial.writes==2,"reopened panel observes CEC off");
        System.out.println("PASS 8054 CEC auto-off: baseline, stale state, refusal, partial outcome, explicit restore");
    }
}

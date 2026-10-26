package dev.projectorsoft.tvsettings;

public final class KuSourceRecoveryPolicyTest {
    private static void check(boolean ok,String why) {
        if(!ok)throw new AssertionError(why);
    }
    public static void main(String[] args) {
        check(!KuSourceRecoveryPolicy.shouldRecover(false,true,false,-1,60000),
              "Must never replace a running KuSettings watchdog");
        check(!KuSourceRecoveryPolicy.shouldRecover(true,false,false,-1,60000),
              "Cannot assume service absence from Android filtered service list");
        check(!KuSourceRecoveryPolicy.shouldRecover(true,true,true,-1,60000),
              "Must not restart an already running thermal service");
        check(KuSourceRecoveryPolicy.shouldRecover(true,true,false,-1,60000),
              "Recover original thermal monitor only when old watchdog is absent");
        check(!KuSourceRecoveryPolicy.shouldRecover(true,true,false,101000,120000),
              "Rate limit retry attempts");
        check(KuSourceRecoveryPolicy.shouldRecover(true,true,false,101000,161000),
              "Permit retry after OEM minute cadence");
        System.out.println("PASS KuSourceRecoveryPolicy: no duplicate OEM watchdog, reliable snapshot, rate limit");
    }
}

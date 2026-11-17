package dev.projectorsoft.tvsettings;

/** Regression guards against Android 13 package-visibility false positives. */
public final class KuSourceRecoveryActivationPolicyTest {
    private static void check(boolean ok,String why) {
        if(!ok)throw new AssertionError(why);
    }
    public static void main(String[] args) {
        check(!KuSourceRecoveryActivationPolicy.explicitlyDisabled(0),
                "Default enabled state is not explicit retirement");
        check(!KuSourceRecoveryActivationPolicy.explicitlyDisabled(1),
                "Explicitly enabled OEM KuSettings must own source watchdog");
        check(KuSourceRecoveryActivationPolicy.explicitlyDisabled(2),
                "Disabled by management permits controlled replacement");
        check(KuSourceRecoveryActivationPolicy.explicitlyDisabled(3),
                "Disabled by user permits controlled replacement");
        check(!KuSourceRecoveryActivationPolicy.explicitlyDisabled(4),
                "Disabled until used can become enabled on demand");
        check(!KuSourceRecoveryActivationPolicy.explicitlyDisabled(-1),
                "Query error/unknown state must never activate watchdog");
        check(!KuSourceRecoveryActivationPolicy.explicitlyDisabled(77),
                "Unexpected package-manager state must fail closed");
        check(!KuSourceRecoveryActivationPolicy.automaticRecoveryAllowed("8054",2),
                "8054 must not transfer thermal watchdog ownership from package state alone");
        check(!KuSourceRecoveryActivationPolicy.automaticRecoveryAllowed("",3),
                "Unidentified board must fail closed");
        check(!KuSourceRecoveryActivationPolicy.automaticRecoveryAllowed("8052",0),
                "8052 default state retains OEM watchdog");
        check(KuSourceRecoveryActivationPolicy.automaticRecoveryAllowed("8052",2),
                "Existing 8052 guarded recovery path remains available");
        System.out.println("PASS KuSourceRecoveryActivationPolicy: explicit disable only");
    }
}


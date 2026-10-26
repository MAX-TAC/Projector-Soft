package dev.projectorsoft.tvsettings;

/** System-independent guardrails for a source thermal watchdog transition. */
public final class KuSourceRecoveryPolicy {
    private KuSourceRecoveryPolicy() {}
    public static boolean shouldRecover(boolean originalKuSettingsDisabled,
                                        boolean serviceSnapshotReliable,
                                        boolean sourceServiceRunning,
                                        long lastAttemptUptime,long nowUptime) {
        if(!originalKuSettingsDisabled || !serviceSnapshotReliable || sourceServiceRunning)
            return false;
        if(lastAttemptUptime<0)return true;
        return nowUptime>=lastAttemptUptime &&
            nowUptime-lastAttemptUptime>=60000L;
    }
}

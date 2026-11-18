package dev.projectorsoft.tvsettings;

/** Compare and verify the single stock standby-on-sleep setting. */
final class CecAutoOff8054 {
    enum Result { APPLIED, STALE, REFUSED, UNCERTAIN }

    static final class State {
        final boolean cecEnabled;
        final boolean autoOff;

        private State(boolean cecEnabled, boolean autoOff) {
            this.cecEnabled=cecEnabled;
            this.autoOff=autoOff;
        }

        static State decode(int cec, int standby) {
            if ((cec!=0 && cec!=1) || (standby!=0 && standby!=1)) return null;
            return new State(cec==1, standby==1);
        }
    }

    interface Backend {
        State read() throws Exception;
        void setAutoOff(boolean enabled) throws Exception;
    }

    private CecAutoOff8054() {}

    static Result apply(Backend backend, boolean expected, boolean requested) {
        State before;
        try {
            before=backend.read();
        } catch (Exception e) {
            return Result.STALE;
        }
        if (before==null || !before.cecEnabled || before.autoOff!=expected) return Result.STALE;
        if (requested==expected) return Result.APPLIED;
        try {
            backend.setAutoOff(requested);
            State after=backend.read();
            if (after==null || !after.cecEnabled) return Result.UNCERTAIN;
            return after.autoOff==requested ? Result.APPLIED : Result.REFUSED;
        } catch (Exception e) {
            // The remote call may have applied before reporting failure.
            return Result.UNCERTAIN;
        }
    }
}

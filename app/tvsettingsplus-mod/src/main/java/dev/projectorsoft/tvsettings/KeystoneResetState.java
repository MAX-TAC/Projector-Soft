package dev.projectorsoft.tvsettings;

/** Evaluates the existing OEM reset and all persisted values it touches. */
final class KeystoneResetState {
    interface Backend {
        boolean resetDisplay();
        boolean clear(String property);
    }

    enum Result { COMPLETE, PARTIAL, FAILED }

    private static final String[] CORNERS = {
        "ltx", "lty", "rtx", "rty", "lbx", "lby", "rbx", "rby"
    };

    private KeystoneResetState() {}

    static Result reset(Backend backend) {
        boolean display = backend.resetDisplay();
        int confirmed = 0;
        for (String corner : CORNERS) {
            if (backend.clear("persist.rjpj.fourcorner.keystone_" + corner)) confirmed++;
            if (backend.clear("persist.display.keystone_" + corner)) confirmed++;
        }
        if (backend.clear("persist.rjpj.autokeystone.move")) confirmed++;
        if (backend.clear("persist.rjpj.autokeystone.poweron")) confirmed++;
        if (display && confirmed == 18) return Result.COMPLETE;
        return display || confirmed > 0 ? Result.PARTIAL : Result.FAILED;
    }
}

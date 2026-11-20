package dev.projectorsoft.tvsettings;

/** A snapshot of OEM preference storage. A readback does not verify hardware motion. */
final class ProjectionToggleState {
    interface Backend {
        String read() throws Exception;
        void write(boolean value) throws Exception;
        default boolean canWrite(boolean value) throws Exception { return true; }
        default String restriction() throws Exception { return null; }
    }

    enum Result { CONFIRMED, STALE, UNAVAILABLE, OEM_REQUIRED, FAILED }

    private final Backend backend;
    private Boolean value;

    ProjectionToggleState(Backend backend) {
        this.backend = backend;
    }

    Boolean refresh() {
        try {
            String current = backend.read();
            if ("1".equals(current)) value = Boolean.TRUE;
            else if ("0".equals(current)) value = Boolean.FALSE;
            else value = null;
        } catch (Exception error) {
            value = null;
        }
        return value;
    }

    Boolean value() {
        return value;
    }

    String restriction() {
        try {
            return backend.restriction();
        } catch (Exception unavailable) {
            return "Не удалось проверить связанные настройки коррекции";
        }
    }

    Result change(boolean requested) {
        Boolean displayed = value;
        Boolean before = refresh();
        if (before == null) return Result.UNAVAILABLE;
        if (!before.equals(displayed)) return Result.STALE;
        if (before == requested) return Result.CONFIRMED;

        try {
            if (restriction() != null || !backend.canWrite(requested)) return Result.OEM_REQUIRED;
        } catch (Exception error) {
            return Result.OEM_REQUIRED;
        }

        boolean completed = false;
        try {
            backend.write(requested);
            completed = true;
        } catch (Exception error) {
            // The OEM may have partially applied a write before throwing.
        }
        Boolean after = refresh();
        return completed && after != null && after == requested ? Result.CONFIRMED : Result.FAILED;
    }
}

package dev.projectorsoft.tvsettings;

import java.util.Objects;

/**
 * PRODUCT-001: one PIC04 contrast step through the same provider route used by STOCK.
 *
 * Pure policy: its Port must be wired to a genuinely validated active input/profile
 * and a durable baseline journal BEFORE real Settings.Global writes are permitted.
 * No production Port exists yet. A Global key alone is NOT an input/profile proof.
 * PENDING/CONFIRMED_BACKEND are never claims of visible PQ change.
 */
public final class Pic04ContrastStep {
    public enum Status {
        PENDING, CONFIRMED_BACKEND_ONLY, ALREADY_PENDING,
        INVALID_DIRECTION, UNKNOWN_CONTEXT, INVALID_BOUNDS, VALUE_MISMATCH,
        CONTEXT_CHANGED, OUT_OF_RANGE, BASELINE_NOT_SAVED, PROVIDER_REFUSED,
        UNCONFIRMED_LOCKED
    }

    public static final class Reading {
        public final String source, signal, profile;
        public final String generation;
        public final boolean trusted;
        public final int mode, temperature, hdr;
        public final int global, mediaTek, min, max, increment;

        /**
         * Source/format/profile/generation MUST originate from a verified
         * live OEM backend, not labels, port-connected state or Settings.Global.
         */
        public Reading(String source, String signal, String profile, String generation,
                       boolean trusted, int mode, int temperature, int hdr,
                       int global, int mediaTek, int min, int max, int increment) {
            this.source=source;
            this.signal=signal;
            this.profile=profile;
            this.generation=generation;
            this.trusted=trusted;
            this.mode=mode;
            this.temperature=temperature;
            this.hdr=hdr;
            this.global=global;
            this.mediaTek=mediaTek;
            this.min=min;
            this.max=max;
            this.increment=increment;
        }

        boolean contextIsVerified() {
            return trusted && notBlank(source) && notBlank(signal)
                    && notBlank(profile) && notBlank(generation);
        }

        String scope() {
            return source+"|"+signal+"|"+profile+"|"+generation+"|"
                    +mode+"|"+temperature+"|"+hdr;
        }

        boolean same(Reading other) {
            return other!=null && contextIsVerified() && other.contextIsVerified()
                    && source.equals(other.source) && signal.equals(other.signal)
                    && profile.equals(other.profile) && generation.equals(other.generation)
                    && mode==other.mode && temperature==other.temperature && hdr==other.hdr
                    && global==other.global && mediaTek==other.mediaTek
                    && min==other.min && max==other.max && increment==other.increment;
        }

        boolean sameScope(Reading other) {
            return other!=null && contextIsVerified() && other.contextIsVerified()
                    && scope().equals(other.scope());
        }

        private static boolean notBlank(String value) {
            return value!=null && !value.trim().isEmpty() && !"UNKNOWN".equals(value);
        }
    }

    public interface Port {
        /** Should fail closed if the independent live source/profile cannot be validated. */
        Reading read();
        /** Durable atomic record of context + original, before any provider write. */
        boolean preserveOriginal(String context, int original);
        /** One Settings.Global.putInt(picture_contrast, value); never direct JNI/HIDL. */
        boolean submitGlobalContrast(int desired);
    }

    public static final class Result {
        public final Status status;
        public final Integer desired;
        private Result(Status status, Integer desired) {
            this.status=status;
            this.desired=desired;
        }
        static Result of(Status status) {return new Result(status,null);}
        static Result of(Status status, int desired) {return new Result(status,desired);}
    }

    private final Port port;
    private Reading initial;
    private Integer requested;
    private boolean locked;

    public Pic04ContrastStep(Port port) {
        this.port=Objects.requireNonNull(port,"port");
    }

    /**
     * Request one OEM-confirmed D-pad increment. No implicit clamping and no
     * second write while a provider/MediaTek acknowledgement is outstanding.
     */
    public Result step(int direction) {
        if (locked) return Result.of(Status.UNCONFIRMED_LOCKED);
        if (requested!=null) return Result.of(Status.ALREADY_PENDING);
        if (direction!=1 && direction!=-1) return Result.of(Status.INVALID_DIRECTION);
        try {
            Reading first=port.read();
            Reading second=port.read();
            if (first==null || second==null || !first.contextIsVerified()
                    || !second.contextIsVerified()) return Result.of(Status.UNKNOWN_CONTEXT);
            if (!first.same(second)) return Result.of(Status.CONTEXT_CHANGED);
            if (first.min>=first.max || first.increment<=0
                    || first.increment>first.max-first.min) return Result.of(Status.INVALID_BOUNDS);
            if (first.global!=first.mediaTek || first.global<first.min
                    || first.global>first.max) return Result.of(Status.VALUE_MISMATCH);
            long next=(long)first.global + (long)direction*first.increment;
            if (next<first.min || next>first.max) return Result.of(Status.OUT_OF_RANGE);
            if (!port.preserveOriginal(first.scope(),first.global))
                return Result.of(Status.BASELINE_NOT_SAVED);
            // Recheck after journal persistence to reduce but NOT eliminate a
            // race with source/profile changes. A trusted generation-aware OEM
            // getter/transition guard is still required for production.
            Reading preWrite=port.read();
            if (!first.same(preWrite)) return Result.of(Status.CONTEXT_CHANGED);
            initial=first;
            requested=(int)next;
            if (!port.submitGlobalContrast(requested)) {
                locked=true; // may be ambiguous even if provider returned false
                return Result.of(Status.PROVIDER_REFUSED,requested);
            }
            return Result.of(Status.PENDING,requested);
        } catch (RuntimeException ex) {
            locked=true; // fail closed; journal may exist
            return Result.of(Status.UNCONFIRMED_LOCKED);
        }
    }

    /** Call after a settling interval. Equality does NOT imply physical effect. */
    public Result verify() {
        if (locked) return Result.of(Status.UNCONFIRMED_LOCKED);
        if (requested==null || initial==null) return Result.of(Status.UNKNOWN_CONTEXT);
        try {
            Reading first=port.read();
            Reading second=port.read();
            if (first==null || second==null || !initial.sameScope(first)
                    || !initial.sameScope(second) || !first.same(second)) {
                locked=true;
                return Result.of(Status.UNCONFIRMED_LOCKED,requested);
            }
            if (first.global==requested && first.mediaTek==requested) {
                int acknowledged=requested;
                requested=null;
                initial=null;
                return Result.of(Status.CONFIRMED_BACKEND_ONLY,acknowledged);
            }
            // Both backend values may remain at the old value for a while;
            // async updates and timeouts belong to the UI adapter.
            return Result.of(Status.PENDING,requested);
        } catch (RuntimeException ex) {
            locked=true;
            return Result.of(Status.UNCONFIRMED_LOCKED);
        }
    }

    /** Abandon pending UI on Back without automatic writes or baseline deletion. */
    public void detach() {
        if (requested!=null) locked=true;
    }
}

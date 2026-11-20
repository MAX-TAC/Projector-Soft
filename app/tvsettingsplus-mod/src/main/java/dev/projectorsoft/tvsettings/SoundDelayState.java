package dev.projectorsoft.tvsettings;

import java.util.List;
import java.util.Objects;

/** STOCK delay preferences use milliseconds while the MediaTek config uses units of 10 ms. */
final class SoundDelayState {
    static final int STEP_MS=10;
    enum Write { APPLIED, PENDING, REFUSED, STALE, UNCERTAIN }

    interface Backend {
        Snapshot read() throws Exception;
        boolean writeProvider(int milliseconds) throws Exception;
    }

    static final class Snapshot {
        final int minMs, maxMs;
        final Integer storedMs, appliedMs;
        final Object soundContext;

        Snapshot(int min, int max, String stored, int applied, Object soundContext) {
            this.soundContext=soundContext;
            minMs=Math.multiplyExact(min,STEP_MS);
            maxMs=Math.multiplyExact(max,STEP_MS);
            Integer provider=parse(stored);
            int configMs=Math.multiplyExact(applied,STEP_MS);
            storedMs=provider!=null && allowed(provider) ? provider : null;
            appliedMs=allowed(configMs) ? configMs : null;
        }

        boolean allowed(int value) {
            return minMs<maxMs && (long)maxMs-minMs<=Integer.MAX_VALUE
                    && value>=minMs && value<=maxMs && value%STEP_MS==0;
        }

        boolean synchronizedValue() {
            return storedMs!=null && storedMs.equals(appliedMs);
        }

        boolean sameConfiguration(Snapshot other) {
            return other!=null && minMs==other.minMs && maxMs==other.maxMs
                    && Objects.equals(soundContext,other.soundContext);
        }
    }

    private SoundDelayState() {}

    static boolean stockOffersSpdif(List<?> audioOutput) {
        return audioOutput.contains("sound_spdif_delay");
    }

    static boolean stockOffersSpeaker(List<?> soundEffects,Object showAll) {
        return "true".equals(showAll) && soundEffects.contains("sound_speaker_delay");
    }

    static Write change(Backend backend, Snapshot displayed, int requested) {
        if (displayed==null || !displayed.synchronizedValue() || !displayed.allowed(requested))
            return Write.STALE;
        Snapshot before;
        try {
            before=backend.read();
        } catch (Exception e) {
            return Write.STALE;
        }
        if (!displayed.sameConfiguration(before) || !before.synchronizedValue()
                || !displayed.storedMs.equals(before.storedMs)) return Write.STALE;
        if (requested==before.storedMs) return Write.APPLIED;

        try {
            if (!backend.writeProvider(requested)) return Write.REFUSED;
            return observe(backend, before, requested);
        } catch (Exception e) {
            return Write.UNCERTAIN;
        }
    }

    static Write observe(Backend backend, Snapshot before, int requested) {
        try {
            Snapshot after=backend.read();
            if (!before.sameConfiguration(after) || after.storedMs==null || after.storedMs!=requested)
                return Write.UNCERTAIN;
            if (after.synchronizedValue()) return Write.APPLIED;
            return before.appliedMs.equals(after.appliedMs) ? Write.PENDING : Write.UNCERTAIN;
        } catch (Exception e) {
            return Write.UNCERTAIN;
        }
    }

    private static Integer parse(String value) {
        try {
            return value==null ? null : Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

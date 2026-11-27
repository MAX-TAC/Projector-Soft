package dev.projectorsoft.tvsettings;

import java.util.Map;
import java.util.Objects;

/** Identifies the source and PQ profile targeted by one lower slider. */
final class PictureEditContext {
    static String identify(String input, Map<String, Integer> state) {
        if (input == null || state == null) return null;
        input = input.trim();
        if (input.isEmpty() || input.length() > 80 || (input.contains("\n") || input.contains("|"))
                || "unknown".equalsIgnoreCase(input) || "none".equalsIgnoreCase(input)
                || "null".equalsIgnoreCase(input) || "-1".equals(input)) return null;
        Integer mode = state.get(PicturePanelState.MODE);
        Integer temperature = state.get(PicturePanelState.TEMPERATURE);
        Integer hdr = state.get(PicturePanelState.HDR);
        return mode == null || temperature == null || hdr == null ? null
                : input + "|" + mode + "/" + temperature + "/" + hdr;
    }

    static boolean stable(String expectedContext, String liveContext, Integer lastValue, Integer saved) {
        return expectedContext != null && expectedContext.equals(liveContext)
                && lastValue != null && Objects.equals(lastValue, saved);
    }

    static boolean applied(String expectedContext, String liveContext, int requested,
                           Integer saved, Integer config) {
        return stable(expectedContext, liveContext, requested, saved)
                && Objects.equals(requested, config);
    }

    private PictureEditContext() {}
}

package dev.projectorsoft.tvsettings;

/** Gate for launching the stock screen-format editor. No configuration is written here. */
final class PictureFormat8054Policy {
    private PictureFormat8054Policy() {}

    static boolean canOpen(String machine, boolean hdmi, int[] supported,
                           String[] labels, String[] values, Object current) {
        if (!"8054".equals(machine) || !hdmi || supported == null || labels == null
                || values == null || labels.length != values.length || !(current instanceof Integer)) {
            return false;
        }
        int usable = 0;
        boolean currentOffered = false;
        for (int i = 0; i < supported.length; i++) {
            int mode = supported[i];
            if (mode < 0) continue;
            String code = Integer.toString(mode);
            for (int j = 0; j < values.length; j++) {
                if (code.equals(values[j]) && labels[j] != null && !labels[j].trim().isEmpty()) {
                    for (int previous = 0; previous < i; previous++) {
                        if (supported[previous] == mode) return false;
                    }
                    usable++;
                    currentOffered |= mode == (Integer) current;
                    break;
                }
            }
        }
        return currentOffered && usable >= 2;
    }
}

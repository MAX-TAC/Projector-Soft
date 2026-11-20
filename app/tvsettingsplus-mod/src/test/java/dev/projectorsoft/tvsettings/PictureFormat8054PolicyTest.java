package dev.projectorsoft.tvsettings;

public final class PictureFormat8054PolicyTest {
    private static final String[] LABELS = {"OEM Wide", "OEM Normal", "OEM Native"};
    private static final String[] VALUES = {"1", "2", "4"};

    private static void check(boolean value) {
        if (!value) throw new AssertionError();
    }

    public static void main(String[] args) {
        // Both HDMI ports use the same OEM capability gate; no fabricated port is passed to a setter.
        check(PictureFormat8054Policy.canOpen("8054", true, new int[]{1, 2}, LABELS, VALUES, 1));
        check(PictureFormat8054Policy.canOpen("8054", true, new int[]{2, 4}, LABELS, VALUES, 4));
        check(!PictureFormat8054Policy.canOpen("8052", true, new int[]{1, 2}, LABELS, VALUES, 1));
        check(!PictureFormat8054Policy.canOpen(null, true, new int[]{1, 2}, LABELS, VALUES, 1));
        check(!PictureFormat8054Policy.canOpen("8054", false, new int[]{1, 2}, LABELS, VALUES, 1));
        check(!PictureFormat8054Policy.canOpen("8054", true, null, LABELS, VALUES, 1));
        check(!PictureFormat8054Policy.canOpen("8054", true, new int[0], LABELS, VALUES, 1));
        check(!PictureFormat8054Policy.canOpen("8054", true, new int[]{1, 99}, LABELS, VALUES, 1));
        check(!PictureFormat8054Policy.canOpen("8054", true, new int[]{1, 1}, LABELS, VALUES, 1));
        check(!PictureFormat8054Policy.canOpen("8054", true, new int[]{1, 2}, LABELS, VALUES, null));
        check(!PictureFormat8054Policy.canOpen("8054", true, new int[]{2, 4}, LABELS, VALUES, 1));
        check(!PictureFormat8054Policy.canOpen("8054", true, new int[]{1, 2}, new String[]{"x"}, VALUES, 1));
        check(!PictureFormat8054Policy.canOpen("8054", true, new int[]{1, 2},
                new String[]{"OEM Wide", "  ", "OEM Native"}, VALUES, 1));
        System.out.println("PASS OEM picture format route gated on HDMI, supported options and current config");
    }
}

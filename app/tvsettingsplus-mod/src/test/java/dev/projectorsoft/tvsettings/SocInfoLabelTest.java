package dev.projectorsoft.tvsettings;

import java.util.Objects;

public final class SocInfoLabelTest {
    private static void check(String expected, String board, String manufacturer, String model) {
        String actual = SocInfoLabel.format(board, manufacturer, model);
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError("expected " + expected + " but got " + actual);
        }
    }

    public static void main(String[] args) {
        check("MediaTek MT9660", "8054", " MediaTek ", " MT9660 ");
        check("MediaTek MT9660", "8054", "mediatek", "MediaTek MT9660");
        check("MT9660", "8054", "UNKNOWN", "MT9660");
        check("MediaTek", "8054", "MediaTek", null);
        check("MediaTek", "8054", "MediaTek", "MediaTek");
        check(null, "8054", "unknown", "0");
        check(null, "8054", "", "n/a");
        check(null, "8054", "null", null);
        check(null, "8052", "MediaTek", "MT9660");
        check(null, null, "MediaTek", "MT9660");
        System.out.println("PASS 8054 read-only SoC label");
    }
}

package dev.projectorsoft.tvsettings;

import java.util.Arrays;

public final class PanelCacheReadoutTest {
    public static final class Manager {
        private static Manager instance;
        private final Display display;

        Manager(Display display) {
            this.display = display;
        }

        public static Manager getInstance() {
            throw new AssertionError("must not initialize the stock singleton");
        }
    }

    public static final class Display {
        String type;
        String version;
        String width;
        String height;
        boolean failHeight;

        public String getPanelType() { return type; }
        public String getPanelVersion() { return version; }
        public String getPanelWidth() { return width; }
        public String getPanelHeight() {
            if (failHeight) throw new IllegalStateException("cache read failed");
            return height;
        }

        public void loadPanelInfo() { throw new AssertionError("must not load the cache"); }
        public void setPanelIni(String path) { throw new AssertionError("must not write"); }
    }

    private static String[] read(String machine, String owner, String field) {
        return PanelCacheReadout.read(machine, Manager.class.getClassLoader(), owner, field, "display");
    }

    private static void check(String machine, String... expected) {
        checkResult(read(machine, Manager.class.getName(), "instance"), expected);
    }

    private static void checkResult(String[] actual, String... expected) {
        if (!Arrays.equals(expected, actual)) throw new AssertionError(Arrays.toString(actual));
    }

    public static void main(String[] args) {
        check("8054", null, null, null, null);
        checkResult(read("8054", "absent.PanelManager", "instance"), null, null, null, null);
        checkResult(read("8054", Manager.class.getName(), "absent"), null, null, null, null);
        Manager.instance = new Manager(null);
        check("8054", null, null, null, null);

        Display display = new Display();
        Manager.instance = new Manager(display);
        check("8054", null, null, null, null);
        display.type = " Panel A ";
        display.version = "v2";
        display.width = "1920";
        display.height = "1080";
        check("8054", "Panel A", "v2", "1920 пкс", "1080 пкс");
        check("8052", null, null, null, null);
        check(null, null, null, null, null);

        display.type = "unknown";
        display.width = "0";
        display.failHeight = true;
        check("8054", null, "v2", null, null);

        display.version = "v3";
        display.width = "-42";
        display.failHeight = false;
        display.height = "not a size";
        check("8054", null, "v3", null, null);
        Manager.instance = null;
        check("8054", null, null, null, null);
        System.out.println("PASS 8054 panel cache readout without initialization or writes");
    }
}

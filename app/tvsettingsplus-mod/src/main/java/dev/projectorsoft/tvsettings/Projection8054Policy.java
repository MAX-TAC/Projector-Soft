package dev.projectorsoft.tvsettings;

/** Navigation eligibility only; never a capability claim for motors/calibration. */
public final class Projection8054Policy {
    static final String CORRECTION_PACKAGE="com.rjpj.tcorrection";

    private Projection8054Policy() {}

    static final String FOUR_CORNER="rjpj.action.keystone.FOUR_CORNER";
    static final String DIGITAL_ZOOM="rjpj.action.keystone.DIGITAL_ZOOM";
    static final String HORIZONTAL_VERTICAL="rjpj.action.keystone.HV";

    private static String stockEditor(String action) {
        if (FOUR_CORNER.equals(action)) return CORRECTION_PACKAGE+".FourCornerActivity";
        if (DIGITAL_ZOOM.equals(action)) return CORRECTION_PACKAGE+".DigitalZoomActivity";
        if (HORIZONTAL_VERTICAL.equals(action)) return CORRECTION_PACKAGE+".HVKeystoneActivity";
        return null;
    }

    static boolean mayOpenStockEditor(String machine,String action,boolean carriesExtras,
                                      String owner,String actualClass,boolean system,
                                      boolean activityEnabled,boolean appEnabled,boolean sameUid,
                                      boolean exported,boolean permissionGranted) {
        String expected=stockEditor(action);
        return "8054".equals(machine) && expected!=null && !carriesExtras && system
                && CORRECTION_PACKAGE.equals(owner) && expected.equals(actualClass)
                && mayNavigate(activityEnabled,appEnabled,sameUid,exported,permissionGranted);
    }

    static boolean canDisableMovement(String screen, String obstacle, String vertical) {
        return "0".equals(screen) && "0".equals(obstacle) && "0".equals(vertical);
    }

    static boolean mayUseOemToggle(String machine, boolean system, boolean enabled,
                                   long installedVersion, long expectedVersion) {
        return "8054".equals(machine) && system && enabled && installedVersion == expectedVersion;
    }

    public static boolean mayUseLegacyBrightness(String machine) {return "8052".equals(machine);}
    public static boolean mayNavigate(boolean activityEnabled,boolean appEnabled,boolean samePackage,
                                      boolean exported,boolean permissionGranted) {
        return activityEnabled&&appEnabled&&permissionGranted&&(samePackage||exported);
    }
}

package dev.projectorsoft.tvsettings;

/** Navigation eligibility only; never a capability claim for motors/calibration. */
public final class Projection8054Policy {
    private Projection8054Policy() {}
    public static boolean mayUseLegacyBrightness(String machine) {return "8052".equals(machine);}
    public static boolean mayNavigate(boolean activityEnabled,boolean appEnabled,boolean samePackage,
                                      boolean exported,boolean permissionGranted) {
        return activityEnabled&&appEnabled&&permissionGranted&&(samePackage||exported);
    }
}

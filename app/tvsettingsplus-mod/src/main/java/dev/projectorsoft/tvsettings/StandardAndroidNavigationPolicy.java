package dev.projectorsoft.tvsettings;
import java.util.Arrays;

/** No factory reset, reboot, OTA, package mutation or service actions. */
public final class StandardAndroidNavigationPolicy {
    private StandardAndroidNavigationPolicy() {}
    public static boolean mayOpen(String action,String owner,boolean systemApp,boolean activityEnabled,
                                  boolean appEnabled,boolean samePackage,boolean exported,boolean permission) {
        if(!Arrays.asList("android.settings.WIFI_SETTINGS","android.settings.LOCALE_SETTINGS",
                "android.settings.DATE_SETTINGS","android.settings.APPLICATION_SETTINGS").contains(action))return false;
        if(!"com.android.tv.settings".equals(owner)&&!"com.android.settings".equals(owner))return false;
        return systemApp&&Projection8054Policy.mayNavigate(activityEnabled,appEnabled,samePackage,exported,permission);
    }
}

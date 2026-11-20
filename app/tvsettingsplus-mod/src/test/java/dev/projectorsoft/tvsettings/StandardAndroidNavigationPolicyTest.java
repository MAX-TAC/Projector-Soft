package dev.projectorsoft.tvsettings;
public final class StandardAndroidNavigationPolicyTest {
    static void check(boolean value){if(!value)throw new AssertionError();}
    public static void main(String[] args) {
        for(String action:new String[]{"android.settings.WIFI_SETTINGS","android.settings.LOCALE_SETTINGS",
                "android.settings.DATE_SETTINGS","android.settings.APPLICATION_SETTINGS"}) {
            check(StandardAndroidNavigationPolicy.mayOpen(action,"com.android.tv.settings",true,true,true,true,false,true));
            check(StandardAndroidNavigationPolicy.mayOpen(action,"com.android.settings",true,true,true,false,true,true));
            check(!StandardAndroidNavigationPolicy.mayOpen(action,"com.my.settings",true,true,true,false,true,true));
            check(!StandardAndroidNavigationPolicy.mayOpen(action,"com.android.settings",false,true,true,false,true,true));
            check(!StandardAndroidNavigationPolicy.mayOpen(action,"com.android.settings",true,false,true,false,true,true));
            check(!StandardAndroidNavigationPolicy.mayOpen(action,"com.android.settings",true,true,false,false,true,true));
            check(!StandardAndroidNavigationPolicy.mayOpen(action,"com.android.settings",true,true,true,false,false,true));
            check(!StandardAndroidNavigationPolicy.mayOpen(action,"com.android.settings",true,true,true,false,true,false));
        }
        check(!StandardAndroidNavigationPolicy.mayOpen("android.intent.action.MASTER_CLEAR","com.android.settings",true,true,true,false,true,true));
        check(!StandardAndroidNavigationPolicy.mayOpen(null,"com.android.settings",true,true,true,false,true,true));
        System.out.println("PASS standard Android navigation action/owner/enable/export/permission allowlist");
    }
}

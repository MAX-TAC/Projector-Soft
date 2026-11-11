package dev.projectorsoft.tvsettings;

import java.util.Set;

/**
 * Safe boot-app selection sharing the original PowerOnAppSecondaryController
 * persisted property persist.trd.source_id. The allowed package names must
 * come from PackageManager launchable applications, never arbitrary UI text.
 */
public final class OemPowerOnAppPolicy {
    public interface Property {
        String read() throws Exception;
        boolean write(String value) throws Exception;
    }
    private OemPowerOnAppPolicy() {}

    public static boolean isPackageName(String value) {
        return value != null && value.matches(
                "[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)+");
    }

    public static boolean allowed(String value,Set<String> installedApps) {
        if("HOME".equals(value))return true;
        return installedApps!=null && isPackageName(value) && installedApps.contains(value);
    }

    public static boolean expose(String current,Set<String> installedApps) {
        return installedApps!=null && !installedApps.isEmpty()
                && allowed(current,installedApps);
    }

    public static boolean apply(Property property,Set<String> installedApps,Object nextValue) {
        if(property==null || !(nextValue instanceof String) || !allowed((String)nextValue,installedApps))
            return false;
        try {
            String current=property.read();
            if(!expose(current,installedApps))return false;
            String next=(String)nextValue;
            if(current.equals(next))return true;
            if(!property.write(next))return false;
            return next.equals(property.read());
        }catch(Exception e) {
            return false;
        }
    }
}

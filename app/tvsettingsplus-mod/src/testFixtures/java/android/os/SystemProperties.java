package android.os;
public final class SystemProperties {
    public static String value="0";
    public static int writes;
    public static boolean partial;
    public static boolean failRead;
    public static String get(String key,String fallback) {
        if(failRead) throw new IllegalStateException("property unavailable");
        return value;
    }
    public static void set(String key,String target) {
        writes++;
        value=target;
        if(partial) throw new IllegalStateException("setter failed after write");
    }
}

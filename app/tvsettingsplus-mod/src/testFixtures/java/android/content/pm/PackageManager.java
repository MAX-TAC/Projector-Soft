package android.content.pm;
import android.content.ComponentName;
public final class PackageManager {
    public int receiverLookups;
    public ActivityInfo getReceiverInfo(ComponentName name,int flags)throws NameNotFoundException {
        receiverLookups++;return new ActivityInfo();
    }
    public static final class NameNotFoundException extends Exception {}
}

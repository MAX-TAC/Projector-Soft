package android.content.pm;
import android.content.ComponentName;
public final class PackageManager {
    public int receiverLookups;
    public boolean receiverExported=true;
    public ActivityInfo getReceiverInfo(ComponentName name,int flags)throws NameNotFoundException {
        receiverLookups++;
        ActivityInfo receiver=new ActivityInfo();
        receiver.exported=receiverExported;
        return receiver;
    }
    public static final class NameNotFoundException extends Exception {}
}

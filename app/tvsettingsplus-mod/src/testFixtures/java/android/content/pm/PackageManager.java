package android.content.pm;
import android.content.ComponentName;
public final class PackageManager {
    public int receiverLookups;
    public static final int PERMISSION_GRANTED=0, COMPONENT_ENABLED_STATE_ENABLED=1;
    public final ActivityInfo target=new ActivityInfo();
    public boolean missing;
    public int state;
    public ActivityInfo getActivityInfo(ComponentName name,int flags)throws NameNotFoundException {
        if(missing) throw new NameNotFoundException();
        return target;
    }
    public int getApplicationEnabledSetting(String packageName){return state;}
    public int getComponentEnabledSetting(ComponentName component){return state;}
    public boolean receiverExported=true;
    public ActivityInfo getReceiverInfo(ComponentName name,int flags)throws NameNotFoundException {
        receiverLookups++;
        ActivityInfo receiver=new ActivityInfo();
        receiver.exported=receiverExported;
        return receiver;
    }
    public static final class NameNotFoundException extends Exception {}
}

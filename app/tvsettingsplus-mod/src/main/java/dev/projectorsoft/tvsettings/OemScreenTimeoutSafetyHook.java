package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.content.ContentResolver;
import android.provider.Settings;
import android.util.Log;

/**
 * Applies the requested Never/Never/no-dream policy on boot and Settings UI
 * entry. No touch to shutdown, power buttons, projector LEDs, fan HAL,
 * KuSettings or KuSourceRJ TimeService.
 */
public final class OemScreenTimeoutSafetyHook {
    private static final String TAG="TvSettingsPlusMOD";
    private OemScreenTimeoutSafetyHook(){}

    private static OemScreenTimeoutPolicy.Store store(Context context) {
        ContentResolver resolver=context.getContentResolver();
        return new OemScreenTimeoutPolicy.Store(){
            @Override public String read(OemScreenTimeoutPolicy.Table table,String key){
                return table==OemScreenTimeoutPolicy.Table.SYSTEM
                    ? Settings.System.getString(resolver,key)
                    : Settings.Secure.getString(resolver,key);
            }
            @Override public boolean write(OemScreenTimeoutPolicy.Table table,String key,int value){
                return table==OemScreenTimeoutPolicy.Table.SYSTEM
                    ? Settings.System.putInt(resolver,key,value)
                    : Settings.Secure.putInt(resolver,key,value);
            }
        };
    }

    /** Idempotent setting write; never reports success without readback. */
    public static boolean ensureNever(Context context) {
        if(context==null)return false;
        OemScreenTimeoutPolicy.Result result=
            OemScreenTimeoutPolicy.inspectOrApply(store(context),true);
        if(!result.isComplete())
            Log.w(TAG,"Could not enforce some projector inactivity settings: "+result.failed);
        return result.isComplete();
    }

    public static boolean allDisabled(Context context) {
        return context!=null &&
            OemScreenTimeoutPolicy.inspectOrApply(store(context),false).isComplete();
    }

    public static boolean installStatus(Context context,Object category,Class<?> preference) {
        if(context==null || category==null || preference==null)return false;
        try {
            Object item=preference.getConstructor(Context.class).newInstance(context);
            preference.getMethod("setKey",String.class).invoke(
                    item,"projectorsoft_screen_timeout_status");
            preference.getMethod("setTitle",CharSequence.class).invoke(
                    item,"Сон и заставка");
            preference.getMethod("setSummary",CharSequence.class).invoke(item,
                allDisabled(context)?"Отключены · экран не гаснет по таймеру":
                  "Не все параметры отключены · проверьте системные разрешения");
            preference.getMethod("setOrder",int.class).invoke(item,288);
            preference.getMethod("setSelectable",boolean.class).invoke(item,false);
            Object accepted=category.getClass().getMethod(
                    "addPreference",preference).invoke(category,item);
            return Boolean.TRUE.equals(accepted);
        }catch(Exception e){
            Log.w(TAG,"Cannot show inactivity status",e);
            return false;
        }
    }
}

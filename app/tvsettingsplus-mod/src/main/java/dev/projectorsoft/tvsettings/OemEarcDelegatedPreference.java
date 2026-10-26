package dev.projectorsoft.tvsettings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.lang.reflect.Method;

/**
 * Explicitly routes a complex eARC preference to the original sound controller.
 * Two OEM fragments assign different polarities to KEY_SOUND_EARC; this
 * launcher deliberately never reads/writes that setting or the audio HAL.
 *
 * This is a controlled compatibility bridge, not yet an inline audio switch.
 */
public final class OemEarcDelegatedPreference {
    private static final String TAG="TvSettingsPlusMOD";
    private static final String SOUND_PAGE="com.android.tv.settings.uicustom.MainActivity";
    private OemEarcDelegatedPreference() {}

    public static boolean install(Context context,Object category,Class<?> preferenceClass) {
        try {
            Class<?> config=Class.forName(
                "com.android.tv.settings.partnercustomizer.tvsettingservice.util.PreferenceConfigUtils");
            if(!Boolean.TRUE.equals(config.getMethod("isSupportEARC").invoke(null)))
                return false;
            ComponentName target=new ComponentName(context,SOUND_PAGE);
            context.getPackageManager().getActivityInfo(target,0);

            Object item=preferenceClass.getConstructor(Context.class).newInstance(context);
            preferenceClass.getMethod("setKey",String.class)
                    .invoke(item,"projectorsoft_earc_oem");
            preferenceClass.getMethod("setTitle",CharSequence.class)
                    .invoke(item,"HDMI eARC");
            preferenceClass.getMethod("setSummary",CharSequence.class)
                    .invoke(item,"Открыть заводские настройки аудиовыхода");
            preferenceClass.getMethod("setOrder",int.class).invoke(item,219);
            Intent intent=new Intent("android.settings.SOUND_SETTINGS");
            intent.setComponent(target); // Never resolves to KuSettings.
            preferenceClass.getMethod("setIntent",Intent.class).invoke(item,intent);
            Object inserted=category.getClass().getMethod("addPreference",preferenceClass)
                    .invoke(category,item);
            return Boolean.TRUE.equals(inserted);
        }catch(Exception e){
            Log.w(TAG,"OEM eARC sound page not available",e);
            return false;
        }
    }
}

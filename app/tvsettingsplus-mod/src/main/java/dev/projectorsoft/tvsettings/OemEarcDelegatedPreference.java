package dev.projectorsoft.tvsettings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * eARC entry on the sound screen.
 *
 * The OEM ships two fragments that assign different polarities to
 * KEY_SOUND_EARC, so this app must never write that key or touch the audio HAL
 * directly. It only opens the original sound controller that owns the polarity.
 *
 * The route is explicit: it used to point at com.android.tv.settings.uicustom
 * .MainActivity, which is the Chinese settings menu the firmware hijacks every
 * standard sound action into. That is gone; the entry now opens the stock
 * SoundActivity of this same package.
 */
public final class OemEarcDelegatedPreference {
    private static final String TAG="TvSettingsPlusMOD";
    private static final String SOUND_PAGE="com.android.tv.settings.device.sound.SoundActivity";
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
                    .invoke(item,"Открыть настройки аудиовыхода");
            preferenceClass.getMethod("setOrder",int.class).invoke(item,219);
            Intent intent=new Intent(Intent.ACTION_MAIN);
            intent.setComponent(target);
            CheckedPreferenceLaunch.attach(context,preferenceClass,item,intent,()->
                    OemCrossActivity.intentFor(context,"com.android.tv.settings",SOUND_PAGE));
            Object inserted=category.getClass().getMethod("addPreference",preferenceClass)
                    .invoke(category,item);
            return Boolean.TRUE.equals(inserted);
        }catch(Exception e){
            Log.w(TAG,"OEM eARC sound page not available",e);
            return false;
        }
    }
}
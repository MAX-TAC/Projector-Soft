package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.content.ContentResolver;
import android.provider.Settings;
import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * Native TV switch for the exact original OEM keypress sound handler.
 * SoundFragment writes Settings.System through
 * Constant.SETTINGS_KEY_SOUND_EFFECTS_ENABLED (0/1), with default 0.
 * Does not touch audio routing, volume, EQ DSP, eARC or Bluetooth.
 */
public final class OemKeyToneHook {
    private static final String TAG="TvSettingsPlusMOD";
    private OemKeyToneHook() {}

    private static String factoryKey() {
        try {
            Class<?> cls=Class.forName("com.android.tv.settings.uicustom.common.Constant");
            Field field=cls.getDeclaredField("SETTINGS_KEY_SOUND_EFFECTS_ENABLED");
            field.setAccessible(true);
            Object raw=field.get(null);
            String key=raw instanceof String?(String)raw:null;
            return key!=null && key.matches("[A-Za-z_][A-Za-z0-9_.]{2,90}")
                    ? key : null;
        }catch(Exception | LinkageError e) {
            Log.w(TAG,"OEM sound effect key unavailable",e);
            return null;
        }
    }

    public static boolean install(Context context,Object category,Class<?> preference) {
        if(context==null || category==null || preference==null)return false;
        try {
            String key=factoryKey();
            if(key==null)return false;
            ContentResolver resolver=context.getContentResolver();
            OemKeyTonePolicy.Store store=new OemKeyTonePolicy.Store() {
                @Override public String read() {
                    return Settings.System.getString(resolver,key);
                }
                @Override public boolean write(int value) {
                    return Settings.System.putInt(resolver,key,value);
                }
            };
            String initial=store.read();
            if(!OemKeyTonePolicy.valid(initial))return false;
            Class<?> toggle=Class.forName("androidx.preference.SwitchPreference");
            Object item=toggle.getConstructor(Context.class).newInstance(context);
            preference.getMethod("setKey",String.class).invoke(item,"projectorsoft_key_tone");
            preference.getMethod("setTitle",CharSequence.class).invoke(item,"Звук кнопок");
            preference.getMethod("setSummary",CharSequence.class).invoke(
                    item,"Звуковое сопровождение нажатий пульта и меню");
            preference.getMethod("setOrder",int.class).invoke(item,219);
            preference.getMethod("setPersistent",boolean.class).invoke(item,false);
            toggle.getMethod("setChecked",boolean.class)
                    .invoke(item,OemKeyTonePolicy.checked(initial));
            Class<?> callbackClass=Class.forName(
                    "androidx.preference.Preference$OnPreferenceChangeListener");
            InvocationHandler listener=(proxy,method,args)->{
                String name=method.getName();
                if("onPreferenceChange".equals(name)) {
                    if(args==null || args.length<2)return false;
                    boolean applied=OemKeyTonePolicy.apply(store,args[1]);
                    if(!applied)Log.w(TAG,"OEM key tone write was not verified");
                    return applied;
                }
                if("toString".equals(name))return "OemKeyToneSwitch";
                if("hashCode".equals(name))return System.identityHashCode(proxy);
                if("equals".equals(name))return args!=null && args.length>0 && proxy==args[0];
                return false;
            };
            Object callback=Proxy.newProxyInstance(callbackClass.getClassLoader(),
                    new Class<?>[]{callbackClass},listener);
            preference.getMethod("setOnPreferenceChangeListener",callbackClass)
                    .invoke(item,callback);
            return Boolean.TRUE.equals(
                    category.getClass().getMethod("addPreference",preference)
                        .invoke(category,item));
        }catch(Exception | LinkageError e) {
            Log.w(TAG,"OEM keypress sound control unavailable",e);
            return false;
        }
    }
}

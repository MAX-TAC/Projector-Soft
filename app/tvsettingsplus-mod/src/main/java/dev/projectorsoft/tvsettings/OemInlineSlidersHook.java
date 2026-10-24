package dev.projectorsoft.tvsettings;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Uses the exact Settings.Global keys and limits of original TvSettingsPlus
 * factory OEM TitleSeekBarView, but renders native Android TV SeekBarPreference.
 * No camera, motor, focus, keystone or LED PWM methods are invoked.
 */
public final class OemInlineSlidersHook {
    private static final String TAG = "TvSettingsPlusMOD";

    private static final class Spec {
        final String name;
        final String title;
        final String key;
        final int min;
        final int max;
        final int order;
        Spec(String name,String title,String key,int min,int max,int order) {
            this.name=name;
            this.title=title;
            this.key=key;
            this.min=min;
            this.max=max;
            this.order=order;
        }
    }

    // min/max read from stock layout XML; key from the original value
    // change listener, not guessed from button text.
    private static final Spec[] CONTROLS = {
        new Spec("picture_brightness","Яркость изображения","picture_brightness",0,100,220),
        new Spec("picture_contrast","Контрастность","picture_contrast",0,100,221),
        new Spec("picture_saturation","Насыщенность","picture_saturation",0,100,222),
        new Spec("picture_hue","Оттенок","picture_hue",-50,50,223),
        new Spec("picture_sharpness","Резкость","picture_sharpness",0,20,224),
        new Spec("picture_red_gain","Красный канал","picture_red_gain",-100,100,225),
        new Spec("picture_green_gain","Зелёный канал","picture_green_gain",-100,100,226),
        new Spec("picture_blue_gain","Синий канал","picture_blue_gain",-100,100,227),
        new Spec("sound_bass","Низкие частоты","sound_bass",0,100,228),
        new Spec("sound_treble","Высокие частоты","sound_treble",0,100,229)
    };

    private OemInlineSlidersHook() {}

    public static void install(Context context,Object category,Class<?> prefClass) {
        install(context,category,category,prefClass);
    }

    /** Native OEM sound and picture settings live in separate AOSP categories. */
    public static void install(Context context,Object pictureCategory,
                               Object soundCategory,Class<?> prefClass) {
        if (context == null || pictureCategory == null
                || soundCategory == null || prefClass == null) return;
        for (Spec spec:CONTROLS) {
            try {
                Object dest=spec.key.startsWith("sound_")?soundCategory:pictureCategory;
                attach(context,dest,prefClass,spec);
            } catch (Exception exception) {
                Log.w(TAG,"Skipping unavailable OEM slider "+spec.key,exception);
            }
        }
    }

    private static void attach(Context context,Object category,
                               Class<?> preferenceClass,Spec spec) throws Exception {
        ContentResolver resolver=context.getContentResolver();
        // Conservative capability gate: absent / malformed values remain hidden.
        String raw=Settings.Global.getString(resolver,spec.key);
        if (raw == null) return;
        final int initial;
        try { initial=Integer.parseInt(raw); }
        catch(NumberFormatException e) { return; }
        if (initial<spec.min || initial>spec.max) return;

        Class<?> bar=Class.forName("androidx.preference.SeekBarPreference");
        Object item=bar.getConstructor(Context.class).newInstance(context);
        preferenceClass.getMethod("setKey",String.class).invoke(item,"projectorsoft_"+spec.name);
        preferenceClass.getMethod("setTitle",CharSequence.class).invoke(item,spec.title);
        preferenceClass.getMethod("setOrder",int.class).invoke(item,spec.order);
        preferenceClass.getMethod("setPersistent",boolean.class).invoke(item,false);
        bar.getMethod("setMin",int.class).invoke(item,spec.min);
        bar.getMethod("setMax",int.class).invoke(item,spec.max);
        bar.getMethod("setSeekBarIncrement",int.class).invoke(item,1);
        bar.getMethod("setShowSeekBarValue",boolean.class).invoke(item,true);
        bar.getMethod("setUpdatesContinuously",boolean.class).invoke(item,false);
        bar.getMethod("setValue",int.class).invoke(item,initial);

        Class<?> listener=Class.forName("androidx.preference.Preference$OnPreferenceChangeListener");
        InvocationHandler callback=(proxy,method,args)-> {
            String name=method.getName();
            if ("onPreferenceChange".equals(name) && args!=null && args.length>=2) {
                if (!(args[1] instanceof Integer)) return false;
                int newValue=(Integer)args[1];
                if (newValue<spec.min || newValue>spec.max) return false;
                try {
                    // Original factory controllers use SettingsUtil.putGlobalValueInt
                    // with the exact same global key, which delegates to the
                    // platform Settings provider. We re-read instead of silently
                    // accepting a failed provider write.
                    if (!Settings.Global.putInt(resolver,spec.key,newValue)) return false;
                    return Settings.Global.getInt(resolver,spec.key,-99999)==newValue;
                } catch (Exception e) {
                    Log.w(TAG,"OEM seekbar write failed: "+spec.key,e);
                    return false;
                }
            }
            if ("toString".equals(name)) return "OemSlider:"+spec.key;
            if ("hashCode".equals(name)) return System.identityHashCode(proxy);
            if ("equals".equals(name)) return args!=null && args.length>0 && proxy==args[0];
            return false;
        };
        Object listenerObject=Proxy.newProxyInstance(listener.getClassLoader(),
                new Class<?>[]{listener},callback);
        preferenceClass.getMethod("setOnPreferenceChangeListener",listener)
                .invoke(item,listenerObject);
        category.getClass().getMethod("addPreference",preferenceClass).invoke(category,item);
    }
}

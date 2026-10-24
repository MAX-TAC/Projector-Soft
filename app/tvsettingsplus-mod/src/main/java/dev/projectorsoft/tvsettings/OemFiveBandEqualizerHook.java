package dev.projectorsoft.tvsettings;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * Native five-band equalizer backed by original OEM Settings.Global keys.
 *
 * Frequency/key pairs are from factory EqualizerDetailFragment.
 * Feature availability comes from original PartnerSettingsConfig and slider
 * bounds come from original partner_progressbar_dialog resources. Nothing
 * is synthesized on boards lacking native OEM EQ configuration.
 *
 * The actual sound effect activation and output path still need device tests.
 */
public final class OemFiveBandEqualizerHook {
    private static final String TAG="TvSettingsPlusMOD";
    private static final String GROUP="sound_equalizer_detail";
    private static final String[] KEYS={
        "sound_equalizer_detail_120hz",
        "sound_equalizer_detail_500hz",
        "sound_equalizer_detail_1500hz",
        "sound_equalizer_detail_5000hz",
        "sound_equalizer_detail_10000hz"
    };
    private static final String[] LABELS={"120 Гц","500 Гц","1,5 кГц","5 кГц","10 кГц"};

    private OemFiveBandEqualizerHook() {}

    @SuppressWarnings("unchecked")
    private static List<String> nativeAvailableBands() throws Exception {
        Class<?> config=Class.forName("com.mediatek.wwtv.tvcenter.util.PartnerSettingsConfig");
        Object list=config.getMethod("getSettingsList",String.class).invoke(null,GROUP);
        if (!(list instanceof List)) return null;
        List<String> result=new ArrayList<>();
        for (Object key:(List<?>)list) if (key instanceof String) result.add((String)key);
        return result;
    }

    /** Read real factory ProgressBar XML bounds, not guessed dB limits. */
    private static int[] originalBounds(Context context) throws Exception {
        int layout=context.getResources().getIdentifier("partner_progressbar_dialog",
                "layout","com.android.tv.settings");
        int progress=context.getResources().getIdentifier("dialog_progress_view",
                "id","com.android.tv.settings");
        if (layout==0||progress==0) return null;
        View view=LayoutInflater.from(context).inflate(layout,null,false);
        View item=view.findViewById(progress);
        if (!(item instanceof ProgressBar))return null;
        ProgressBar bar=(ProgressBar)item;
        int min=bar.getMin(),max=bar.getMax();
        if (min<0||max>200||max<=min)return null;
        return new int[]{min,max};
    }

    public static void install(Context ctx,Object group,Class<?> preferenceClass) {
        try {
            List<String> supported=nativeAvailableBands();
            int[] bounds=originalBounds(ctx);
            if (supported==null || supported.isEmpty() || bounds==null) return;
            ContentResolver resolver=ctx.getContentResolver();
            Class<?> seek=Class.forName("androidx.preference.SeekBarPreference");
            Class<?> listener=Class.forName("androidx.preference.Preference$OnPreferenceChangeListener");
            for (int i=0;i<KEYS.length;i++) {
                String key=KEYS[i];
                if (!supported.contains(key)) continue;
                int current=Settings.Global.getInt(resolver,key,0);
                if(current<bounds[0]||current>bounds[1])continue;
                Object item=seek.getConstructor(Context.class).newInstance(ctx);
                preferenceClass.getMethod("setKey",String.class).invoke(item,
                        "projectorsoft_eq_"+key);
                preferenceClass.getMethod("setTitle",CharSequence.class).invoke(item,LABELS[i]);
                preferenceClass.getMethod("setOrder",int.class).invoke(item,240+i);
                preferenceClass.getMethod("setPersistent",boolean.class).invoke(item,false);
                seek.getMethod("setMin",int.class).invoke(item,bounds[0]);
                seek.getMethod("setMax",int.class).invoke(item,bounds[1]);
                seek.getMethod("setSeekBarIncrement",int.class).invoke(item,1);
                seek.getMethod("setShowSeekBarValue",boolean.class).invoke(item,true);
                seek.getMethod("setUpdatesContinuously",boolean.class).invoke(item,false);
                seek.getMethod("setValue",int.class).invoke(item,current);
                final int lo=bounds[0],hi=bounds[1];
                InvocationHandler callback=(proxy,method,args)->{
                    String methodName=method.getName();
                    if ("onPreferenceChange".equals(methodName)&&args!=null&&args.length>=2) {
                        if (!(args[1] instanceof Integer))return false;
                        int value=(Integer)args[1];
                        if (value<lo||value>hi)return false;
                        try {
                            if (!Settings.Global.putInt(resolver,key,value))return false;
                            return Settings.Global.getInt(resolver,key,Integer.MIN_VALUE)==value;
                        } catch(Exception ex) {
                            Log.w(TAG,"EQ band update failed "+key,ex);
                            return false;
                        }
                    }
                    if ("toString".equals(methodName))return "OemEqualizer:"+key;
                    if ("hashCode".equals(methodName))return System.identityHashCode(proxy);
                    if ("equals".equals(methodName))return args!=null&&args.length>0&&proxy==args[0];
                    return false;
                };
                Object callbackObject=Proxy.newProxyInstance(listener.getClassLoader(),
                        new Class<?>[]{listener},callback);
                preferenceClass.getMethod("setOnPreferenceChangeListener",listener)
                        .invoke(item,callbackObject);
                group.getClass().getMethod("addPreference",preferenceClass).invoke(group,item);
            }
        } catch(Exception e) {
            Log.w(TAG,"OEM five-band EQ unavailable",e);
        }
    }
}

package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.util.Log;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;

/** Conditional shortcuts to the original editors; their settings stay under OEM ownership. */
final class OemAdvancedSoundRoutes {
    private static final String TAG="TvSettingsPlusMOD";

    private OemAdvancedSoundRoutes() {}

    static void install(Context context,Object group,Class<?> pref) {
        if (!"8054".equals(OemHdmiPortInfo.machineId())) return;
        add(context,group,pref,true,"dolby_dap","Dolby Audio Processing",
                "com.android.tv.settings.partnercustomizer.sound.advanced.AdvancedFragment",244);
        add(context,group,pref,false,"dts","DTS и выравнивание громкости",
                "com.android.tv.settings.partnercustomizer.sound.dts.DTSFragment",245);
    }

    private static List<?> settingsList(Method getGroup,String key) throws ReflectiveOperationException {
        Object result=getGroup.invoke(null,key);
        return result instanceof List ? (List<?>)result : Collections.emptyList();
    }

    private static boolean available(Context context,boolean dolby) throws ReflectiveOperationException {
        String board=OemHdmiPortInfo.machineId();
        if (!"8054".equals(board)) return false;
        Method getGroup=Class.forName("com.mediatek.wwtv.tvcenter.util.PartnerSettingsConfig")
                .getMethod("getSettingsList",String.class);
        List<?> soundEffects=settingsList(getGroup,"sound_effects");
        if (dolby) {
            Class<?> type=Class.forName("com.android.tv.settings.partnercustomizer.tvsettingservice.TVSettingConfig");
            Object config=type.getMethod("getInstance",Context.class).invoke(null,context);
            return config!=null && AdvancedSound8054Policy.dap(board,soundEffects,
                    type.getMethod("getDolbyAtmosType").invoke(config));
        }
        Class<?> type=Class.forName(
                "com.android.tv.settings.partnercustomizer.tvsettingservice.util.PreferenceConfigUtils");
        Object config=type.getMethod("getInstance",Context.class).invoke(null,context);
        return config!=null && AdvancedSound8054Policy.dts(board,soundEffects,
                type.getMethod("isAdvanceFunctionSupport",String.class).invoke(config,"sound_dts"),
                settingsList(getGroup,"sound_dts_vx"));
    }

    private static void add(Context context,Object group,Class<?> pref,boolean dolby,String key,
                            String title,String target,int order) {
        try {
            if (!available(context,dolby)) return;
            Class<?> fragment=Class.forName(target,false,context.getClassLoader());
            if (!Class.forName("androidx.preference.PreferenceFragmentCompat").isAssignableFrom(fragment)) return;
            fragment.getConstructor();
            Object item=pref.getConstructor(Context.class).newInstance(context);
            pref.getMethod("setKey",String.class).invoke(item,"projectorsoft_sound_"+key);
            pref.getMethod("setTitle",CharSequence.class).invoke(item,title);
            pref.getMethod("setOrder",int.class).invoke(item,order);
            pref.getMethod("setFragment",String.class).invoke(item,target);
            Class<?> listener=Class.forName("androidx.preference.Preference$OnPreferenceClickListener");
            Object callback=Proxy.newProxyInstance(listener.getClassLoader(),new Class<?>[]{listener},
                    (proxy,method,args)-> {
                        switch (method.getName()) {
                            case "onPreferenceClick":
                                try {if (available(context,dolby)) return false;}
                                catch (Exception | LinkageError error) {Log.w(TAG,"OEM audio support unavailable",error);}
                                android.widget.Toast.makeText(context,"Штатный раздел звука недоступен",
                                        android.widget.Toast.LENGTH_SHORT).show();
                                return true;
                            case "hashCode": return System.identityHashCode(proxy);
                            case "equals": return args!=null && args.length==1 && proxy==args[0];
                            case "toString": return target;
                            default: return null;
                        }
                    });
            pref.getMethod("setOnPreferenceClickListener",listener).invoke(item,callback);
            group.getClass().getMethod("addPreference",pref).invoke(group,item);
        } catch (Exception | LinkageError error) {
            Log.w(TAG,"OEM audio editor unavailable: "+key,error);
        }
    }
}

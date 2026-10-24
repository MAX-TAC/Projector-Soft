package dev.projectorsoft.tvsettings;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Inline EDID compatibility selector using the EXACT original
 * EdidSourceSecondaryController OEM hardware query and Settings.Global key.
 * Only shown while HDMI is the currently selected source. The manufacturer
 * checks this before enabling its EDID radio controls.
 */
public final class OemHdmiEdidHook {
    private static final String TAG="TvSettingsPlusMOD";
    private static final String KEY="tv_input_hdmi_edid_version";
    private static final int[] MODES={0,1,2,3};
    private static final String[] NAMES={"HDMI 1.4","HDMI 2.0","Автоматически","HDMI 2.1"};

    private OemHdmiEdidHook() {}

    private static int hardwareSupportedMax(Context context) throws Exception {
        Class<?> config=Class.forName(
          "com.android.tv.settings.partnercustomizer.tvsettingservice.util.PreferenceConfigUtils");
        Object preferences=config.getMethod("getInstance",Context.class).invoke(null,context);
        if(preferences==null)return -1;
        Object source=config.getMethod("isHDMISource",Context.class).invoke(preferences,context);
        if(!(source instanceof Boolean) || !((Boolean)source))return -1;
        Class<?> av=Class.forName("com.mediatek.twoworlds.tv.MtkTvAVMode");
        Class<?> data=Class.forName("com.mediatek.twoworlds.tv.model.MtkTvVideoInfoBase");
        Object avInstance=av.getMethod("getInstance").invoke(null);
        Object dataInstance=data.getConstructor().newInstance();
        Object info=av.getMethod("getVideoInfoData",int.class,data)
                    .invoke(avInstance,6,dataInstance);
        if(info==null)return -1;
        Object supported=data.getMethod("getU8EdidIndex").invoke(info);
        if(!(supported instanceof Integer))return -1;
        int max=(Integer)supported;
        return max>=0 && max<=3 ? max : -1;
    }

    /** Returns true only when a real native EDID preference is added. */
    public static boolean install(Context context,Object category,Class<?> preferenceClass) {
        try {
            final int hardwareMax=hardwareSupportedMax(context);
            if(hardwareMax<0)return false;

            ContentResolver resolver=context.getContentResolver();
            int actual=Settings.Global.getInt(resolver,KEY,0);
            int selected=actual<=hardwareMax && actual>=0 ? actual : hardwareMax;
            int length=hardwareMax+1;
            CharSequence[] labels=new CharSequence[length];
            CharSequence[] values=new CharSequence[length];
            Map<Integer,String> title=new HashMap<>();
            for(int i=0;i<length;i++){
                labels[i]=NAMES[i];
                values[i]=String.valueOf(MODES[i]);
                title.put(MODES[i],NAMES[i]);
            }
            Class<?> list=Class.forName("androidx.preference.ListPreference");
            Object item=list.getConstructor(Context.class).newInstance(context);
            preferenceClass.getMethod("setKey",String.class).invoke(item,"projectorsoft_hdmi_edid");
            preferenceClass.getMethod("setTitle",CharSequence.class).invoke(item,
                    "Совместимость HDMI (EDID)");
            preferenceClass.getMethod("setSummary",CharSequence.class).invoke(item,title.get(selected));
            preferenceClass.getMethod("setOrder",int.class).invoke(item,271);
            preferenceClass.getMethod("setPersistent",boolean.class).invoke(item,false);
            list.getMethod("setEntries",CharSequence[].class).invoke(item,(Object)labels);
            list.getMethod("setEntryValues",CharSequence[].class).invoke(item,(Object)values);
            list.getMethod("setValue",String.class).invoke(item,String.valueOf(selected));
            Class<?> listener=Class.forName("androidx.preference.Preference$OnPreferenceChangeListener");
            InvocationHandler callback=(proxy,method,args)->{
                String name=method.getName();
                if ("onPreferenceChange".equals(name)&&args!=null&&args.length>=2) {
                    try {
                        int mode=Integer.parseInt(String.valueOf(args[1]));
                        int currentMax=hardwareSupportedMax(context);
                        if(currentMax<0||mode<0||mode>currentMax)return false;
                        if(!Settings.Global.putInt(resolver,KEY,mode))return false;
                        if(Settings.Global.getInt(resolver,KEY,-99)!=mode)return false;
                        preferenceClass.getMethod("setSummary",CharSequence.class)
                                .invoke(item,title.get(mode));
                        return true;
                    }catch(Exception error){
                        Log.w(TAG,"EDID version not confirmed by OEM API",error);
                        return false;
                    }
                }
                if("toString".equals(name))return "NativeOemHdmiEdid";
                if("hashCode".equals(name))return System.identityHashCode(proxy);
                if("equals".equals(name))return args!=null&&args.length>0&&proxy==args[0];
                return false;
            };
            Object proxy=Proxy.newProxyInstance(listener.getClassLoader(),
                    new Class<?>[]{listener},callback);
            preferenceClass.getMethod("setOnPreferenceChangeListener",listener).invoke(item,proxy);
            Object accepted=category.getClass().getMethod("addPreference",preferenceClass)
                    .invoke(category,item);
            return Boolean.TRUE.equals(accepted);
        }catch(Exception e){
            Log.w(TAG,"OEM HDMI EDID controls unavailable",e);
            return false;
        }
    }
}

package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.content.ContentResolver;
import android.provider.Settings;
import android.util.Log;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/** Native TV UI for stock FontSizeSettingsSecondaryController scale values. */
public final class OemFontScaleHook {
    private static final String TAG="TvSettingsPlusMOD";
    private static final String SYSTEM_KEY="font_scale";
    private OemFontScaleHook() {}

    public static boolean install(Context context,Object category,Class<?> preference) {
        if(context==null || category==null || preference==null)return false;
        try {
            ContentResolver resolver=context.getContentResolver();
            OemFontScalePolicy.Store store=new OemFontScalePolicy.Store() {
                @Override public float read(){
                    return Settings.System.getFloat(resolver,SYSTEM_KEY,1.0f);
                }
                @Override public boolean write(float value){
                    return Settings.System.putFloat(resolver,SYSTEM_KEY,value);
                }
            };
            float current=store.read();
            int initial=OemFontScalePolicy.index(current);
            if(initial<0)return false;
            String[] titles=OemFontScalePolicy.labels();
            float[] values=OemFontScalePolicy.values();
            CharSequence[] display=new CharSequence[titles.length];
            CharSequence[] rawValues=new CharSequence[values.length];
            for(int i=0;i<titles.length;i++){
                display[i]=titles[i];
                rawValues[i]=Float.toString(values[i]);
            }
            Class<?> listType=Class.forName("androidx.preference.ListPreference");
            Object item=listType.getConstructor(Context.class).newInstance(context);
            preference.getMethod("setKey",String.class).invoke(
                    item,"projectorsoft_font_scale");
            preference.getMethod("setTitle",CharSequence.class).invoke(item,"Размер текста");
            preference.getMethod("setSummary",CharSequence.class).invoke(item,titles[initial]);
            preference.getMethod("setOrder",int.class).invoke(item,256);
            preference.getMethod("setPersistent",boolean.class).invoke(item,false);
            listType.getMethod("setEntries",CharSequence[].class).invoke(item,(Object)display);
            listType.getMethod("setEntryValues",CharSequence[].class)
                    .invoke(item,(Object)rawValues);
            listType.getMethod("setValue",String.class).invoke(item,Float.toString(values[initial]));
            Class<?> listenerType=Class.forName(
                    "androidx.preference.Preference$OnPreferenceChangeListener");
            InvocationHandler callback=(proxy,method,args)->{
                String name=method.getName();
                if("onPreferenceChange".equals(name)){
                    if(args==null || args.length<2)return false;
                    boolean success=OemFontScalePolicy.apply(store,args[1]);
                    if(success){
                        try {
                            float wanted=Float.parseFloat(String.valueOf(args[1]));
                            int index=OemFontScalePolicy.index(wanted);
                            if(index>=0)preference.getMethod("setSummary",CharSequence.class)
                                    .invoke(item,titles[index]);
                        }catch(Exception e){Log.w(TAG,"Could not update font summary",e);}
                    }
                    return success;
                }
                if("hashCode".equals(name))return System.identityHashCode(proxy);
                if("toString".equals(name))return "OemFontScalePreference";
                if("equals".equals(name))return args!=null && args.length>0 && proxy==args[0];
                return false;
            };
            Object listener=Proxy.newProxyInstance(listenerType.getClassLoader(),
                    new Class<?>[]{listenerType},callback);
            preference.getMethod("setOnPreferenceChangeListener",listenerType)
                    .invoke(item,listener);
            return Boolean.TRUE.equals(category.getClass().getMethod(
                    "addPreference",preference).invoke(category,item));
        }catch(Exception | LinkageError e){
            Log.w(TAG,"Font scale setting unavailable, OEM general page retained",e);
            return false;
        }
    }
}

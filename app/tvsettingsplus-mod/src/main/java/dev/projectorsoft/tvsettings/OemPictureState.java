package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import java.util.Map;

/** Keeps the provider state before the first MOD picture change for rollback. */
public final class OemPictureState {
    private OemPictureState() {}
    private static SharedPreferences saved(Context context) {
        return context.getSharedPreferences("projectorsoft_picture_rollback", Context.MODE_PRIVATE);
    }
    public static boolean write(Context context,String key,int value) {
        String old=Settings.Global.getString(context.getContentResolver(),key);
        if(old==null)return false;
        SharedPreferences state=saved(context);
        if(!state.contains(key)&&!state.edit().putString(key,old).commit())return false;
        return Settings.Global.putInt(context.getContentResolver(),key,value)
            && Integer.toString(value).equals(Settings.Global.getString(context.getContentResolver(),key));
    }
    public static boolean restore(Context context) {
        boolean complete=true;
        for(Map.Entry<String,?> item:saved(context).getAll().entrySet()) {
            String key=item.getKey();
            if(!key.startsWith("picture_")||!(item.getValue() instanceof String)) {
                complete=false;
                continue;
            }
            String value=(String)item.getValue();
            // Keep failed entries so a subsequent attempt can restore them.
            if(Settings.Global.putString(context.getContentResolver(),key,value)
                    && value.equals(Settings.Global.getString(context.getContentResolver(),key))) {
                if(!saved(context).edit().remove(key).commit())complete=false;
            } else complete=false;
        }
        return complete;
    }
    public static void install(Context context,Object category,Class<?> pref) throws Exception {
        Object item=pref.getConstructor(Context.class).newInstance(context);
        pref.getMethod("setKey",String.class).invoke(item,"projectorsoft_picture_restore");
        pref.getMethod("setTitle",CharSequence.class).invoke(item,"Восстановить изображение до изменений MOD");
        pref.getMethod("setSummary",CharSequence.class).invoke(item,"После восстановления заново откройте настройки изображения");
        pref.getMethod("setOrder",int.class).invoke(item,239);
        Class<?> listener=Class.forName("androidx.preference.Preference$OnPreferenceClickListener");
        Object callback=java.lang.reflect.Proxy.newProxyInstance(listener.getClassLoader(),new Class<?>[]{listener},
            (proxy,method,args)-> {
                if("onPreferenceClick".equals(method.getName())) {
                    boolean ok;
                    try {ok=restore(context);} catch(Exception ex){ok=false;}
                    pref.getMethod("setSummary",CharSequence.class).invoke(item,
                        ok?"Восстановлено. Заново откройте настройки изображения":"Восстановление не завершено. Повторите попытку");
                    return true;
                }
                if("hashCode".equals(method.getName()))return System.identityHashCode(proxy);
                if("equals".equals(method.getName()))return args!=null&&args.length>0&&proxy==args[0];
                return "PictureRestore";
            });
        pref.getMethod("setOnPreferenceClickListener",listener).invoke(item,callback);
        category.getClass().getMethod("addPreference",pref).invoke(category,item);
    }
}

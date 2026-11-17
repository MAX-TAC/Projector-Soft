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
    private static PictureStateTransaction transaction(Context context) {
        return new PictureStateTransaction(new PictureStateTransaction.Backend() {
            public String read(String key) {return Settings.Global.getString(context.getContentResolver(),key);}
            public boolean write(String key,String value) {return Settings.Global.putString(context.getContentResolver(),key,value);}
            // Global picture_mode alone cannot identify the active OEM HDMI/PQ profile.
            public String verifiedContext() {return null;}
        },new PictureStateTransaction.Journal() {
            public Map<String,String> entries() {
                Map<String,String> result=new java.util.LinkedHashMap<>();
                for(Map.Entry<String,?> e:saved(context).getAll().entrySet())
                    if(e.getValue() instanceof String)result.put(e.getKey(),(String)e.getValue());
                return result;
            }
            public boolean save(String key,String value) {return saved(context).edit().putString(key,value).commit();}
            public boolean remove(String key) {return saved(context).edit().remove(key).commit();}
        });
    }
    public static boolean write(Context context,String key,int value) {
        return transaction(context).write(key,value);
    }
    public static boolean restore(Context context) {return transaction(context).restore();}
    public static void install(Context context,Object category,Class<?> pref) throws Exception {
        Object item=pref.getConstructor(Context.class).newInstance(context);
        pref.getMethod("setKey",String.class).invoke(item,"projectorsoft_picture_restore");
        pref.getMethod("setTitle",CharSequence.class).invoke(item,"Сохранённые значения изображения");
        pref.getMethod("setSummary",CharSequence.class).invoke(item,"Восстановление недоступно: профиль изображения и HDMI ещё не определены");
        pref.getMethod("setOrder",int.class).invoke(item,239);
        pref.getMethod("setEnabled",boolean.class).invoke(item,false);
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

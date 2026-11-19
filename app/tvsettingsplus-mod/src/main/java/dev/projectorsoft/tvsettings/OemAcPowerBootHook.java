package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.util.Log;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Native TV switch for the already-existing factory electricity-boot control.
 * Delegates ALL hardware/power semantics to MtkUtil.isElectricityBoot() and
 * putElectricityBoot(boolean), rather than guessing properties or broadcasts.
 */
public final class OemAcPowerBootHook {
    private static final String TAG="TvSettingsPlusMOD";
    private OemAcPowerBootHook() {}

    private static OemAcPowerBootPolicy.Gateway factoryGateway() throws Exception {
        Class<?> api=Class.forName("com.android.tv.settings.uicustom.util.MtkUtil");
        Object singleton=api.getMethod("getInstance").invoke(null);
        if(singleton==null)throw new IllegalStateException("Factory MtkUtil unavailable");
        Method read=api.getMethod("isElectricityBoot");
        Method write=api.getMethod("putElectricityBoot",boolean.class);
        return new OemAcPowerBootPolicy.Gateway() {
            @Override public Boolean read() throws Exception {
                Object result=read.invoke(singleton);
                return result instanceof Boolean?(Boolean)result:null;
            }
            @Override public void write(boolean enabled) throws Exception {
                write.invoke(singleton,enabled);
            }
        };
    }

    public static boolean install(Context context,Object category,Class<?> preference) {
        if(context==null || category==null || preference==null)return false;
        try {
            OemAcPowerBootPolicy.Gateway gateway=factoryGateway();
            if(!OemAcPowerBootPolicy.available(gateway))return false;
            Boolean initial=gateway.read();
            if(initial==null)return false;
            Class<?> toggle=Class.forName("androidx.preference.SwitchPreference");
            Object item=toggle.getConstructor(Context.class).newInstance(context);
            preference.getMethod("setKey",String.class).invoke(
                    item,"projectorsoft_electricity_boot");
            preference.getMethod("setTitle",CharSequence.class).invoke(
                    item,"Включать после подачи питания");
            preference.getMethod("setSummary",CharSequence.class).invoke(
                    item,"Заводской режим автозапуска проектора при появлении питания");
            preference.getMethod("setOrder",int.class).invoke(item,261);
            preference.getMethod("setPersistent",boolean.class).invoke(item,false);
            toggle.getMethod("setChecked",boolean.class).invoke(item,initial.booleanValue());
            Class<?> callbackClass=Class.forName(
                    "androidx.preference.Preference$OnPreferenceChangeListener");
            InvocationHandler callback=(proxy,method,args)->{
                String name=method.getName();
                if("onPreferenceChange".equals(name)) {
                    if(args==null || args.length<2)return false;
                    boolean accepted=OemAcPowerBootPolicy.apply(gateway,args[1]);
                    if(!accepted)Log.w(TAG,"Factory electricity-boot value not confirmed");
                    return accepted;
                }
                if("toString".equals(name))return "OemAcPowerBootHook";
                if("hashCode".equals(name))return System.identityHashCode(proxy);
                if("equals".equals(name))return args!=null && args.length>0 && proxy==args[0];
                return false;
            };
            Object listener=Proxy.newProxyInstance(callbackClass.getClassLoader(),
                    new Class<?>[]{callbackClass},callback);
            preference.getMethod("setOnPreferenceChangeListener",callbackClass)
                    .invoke(item,listener);
            Object result=category.getClass().getMethod("addPreference",preference)
                    .invoke(category,item);
            return Boolean.TRUE.equals(result);
        }catch(Exception | LinkageError error) {
            Log.w(TAG,"Factory AC power-on control not available",error);
            return false;
        }
    }
}

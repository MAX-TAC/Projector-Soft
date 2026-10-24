package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * Complete OEM CEC toggle for boards without the OEM POWER CONFIRM dialog.
 * Preserves CompatibleApis, dependent auto-off reset, eARC disable and OEM
 * Settings.Global flag. Boards needing a power-confirmation dialog must
 * continue to use the original OEM SourceSubMenuController Activity.
 */
public final class OemHdmiCecHook {
    private static final String TAG="TvSettingsPlusMOD";
    private static final String COMPAT="com.mediatek.support.androidversioncompatiblelib.CompatibleApis";
    private static final String DATA="com.mediatek.wwtv.tvcenter.util.DataSeparaterUtil";
    private static final String PREFS="com.android.tv.settings.partnercustomizer.tvsettingservice.util.PreferenceConfigUtils";
    private static final String PREF_KEYS="com.android.tv.settings.partnercustomizer.tvsettingservice.util.PreferenceConfigUtilsBase";
    private static final String CONSTANT="com.android.tv.settings.uicustom.common.Constant";
    private static final String CEC_AUDIO="com.mediatek.twoworlds.tv.MtkTvCecAnBase";

    private OemHdmiCecHook() {}

    private static final class Bridge {
        final Context context;
        final Object compatible;
        final Method getCec, getOff, setCec, setOff;
        final String cecKey;
        final boolean earcSupported;
        final String earcKey;
        final Constructor<?> earcCtor;
        final Method disableEarc;
        Bridge(Context context) throws Exception {
            this.context=context;
            Class<?> data=Class.forName(DATA);
            Object singleton=data.getMethod("getInstance").invoke(null);
            if(singleton==null)throw new IllegalStateException("No OEM CEC data");
            Object requiresDialog=data.getMethod("isSupportPowerConfirmed").invoke(singleton);
            if(!(requiresDialog instanceof Boolean) || (Boolean)requiresDialog)
                throw new IllegalStateException("CEC requires original OEM power confirmation");
            Class<?> c=Class.forName(COMPAT);
            compatible=c.getConstructor(Context.class).newInstance(context);
            getCec=c.getMethod("isHdmiCecControlEnabled");
            getOff=c.getMethod("isHdmiCecAutoDeviceOffEnabled");
            setCec=c.getMethod("setHdmiCecControlEnabled",boolean.class);
            setOff=c.getMethod("setHdmiCecAutoDeviceOffEnabled",boolean.class);
            Object k=Class.forName(CONSTANT).getField("SETTINGS_KEY_HDMI_CEC").get(null);
            if(!(k instanceof String)||((String)k).isEmpty())
                throw new IllegalStateException("Missing factory HDMI CEC global key");
            cecKey=(String)k;
            Object supported=Class.forName(PREFS).getMethod("isSupportEARC").invoke(null);
            if(!(supported instanceof Boolean))
                throw new IllegalStateException("Unknown eARC support state");
            earcSupported=(Boolean)supported;
            if(earcSupported) {
                Class<?> type=Class.forName(CEC_AUDIO);
                earcCtor=type.getConstructor();
                disableEarc=type.getMethod("setEarcEnable",boolean.class);
                Object e=Class.forName(PREF_KEYS).getField("KEY_SOUND_EARC").get(null);
                if(!(e instanceof String) || ((String)e).isEmpty())
                    throw new IllegalStateException("No OEM eARC state key");
                earcKey=(String)e;
            } else {
                earcCtor=null; disableEarc=null; earcKey=null;
            }
        }
        boolean cec() throws Exception {
            return Boolean.TRUE.equals(getCec.invoke(compatible));
        }
        boolean off() throws Exception {
            return Boolean.TRUE.equals(getOff.invoke(compatible));
        }

        boolean setCecState(boolean requested) throws Exception {
            setCec.invoke(compatible,requested);
            if(cec()!=requested) return false;
            if(!requested) {
                // Mirror both OEM branches. Disabling HDMI CEC disables HDMI
                // auto device off and eARC, without leaving a stale DSP state.
                setOff.invoke(compatible,false);
                if(off()) return false;
                if(earcSupported) {
                    Object earc=earcCtor.newInstance();
                    disableEarc.invoke(earc,false);
                    if(!Settings.Global.putInt(context.getContentResolver(),earcKey,1))
                        return false;
                }
            }
            if(!Settings.Global.putInt(context.getContentResolver(),cecKey,requested?1:0))
                return false;
            return cec()==requested &&
                    Settings.Global.getInt(context.getContentResolver(),cecKey,-1)==(requested?1:0);
        }
        boolean setAutoOff(boolean requested) throws Exception {
            if(!cec()) return false;
            setOff.invoke(compatible,requested);
            return off()==requested;
        }
    }

    public static boolean install(Context context,Object category,Class<?> preferenceClass) {
        try {
            final Bridge bridge=new Bridge(context);
            final Object cec=makeSwitch(context,preferenceClass,
                    "projectorsoft_hdmi_cec","HDMI-CEC",
                    "Управление HDMI-устройствами; отключение CEC также отключит eARC",
                    bridge.cec(),274);
            final Object autoOff=makeSwitch(context,preferenceClass,
                    "projectorsoft_hdmi_auto_off","Выключать HDMI-устройства вместе с проектором",
                    "Доступно только при включённом HDMI-CEC",
                    bridge.off(),275);
            preferenceClass.getMethod("setEnabled",boolean.class)
                    .invoke(autoOff,bridge.cec());

            Class<?> callbackType=Class.forName("androidx.preference.Preference$OnPreferenceChangeListener");
            attach(preferenceClass,cec,callbackType,(proxy,method,args)->{
                String name=method.getName();
                if("onPreferenceChange".equals(name) && args!=null && args.length>=2) {
                    if(!(args[1] instanceof Boolean))return false;
                    boolean requested=(Boolean)args[1];
                    try {
                        if(!bridge.setCecState(requested))return false;
                        preferenceClass.getMethod("setEnabled",boolean.class)
                                .invoke(autoOff,requested);
                        if(!requested) {
                            Class.forName("androidx.preference.TwoStatePreference")
                                    .getMethod("setChecked",boolean.class).invoke(autoOff,false);
                        }
                        return true;
                    }catch(Exception e){
                        Log.w(TAG,"Native CEC write failed; OEM source controls preserved",e);
                        return false;
                    }
                }
                return objectMethod(proxy,name,args,"OemCec");
            });
            attach(preferenceClass,autoOff,callbackType,(proxy,method,args)->{
                String name=method.getName();
                if("onPreferenceChange".equals(name) && args!=null && args.length>=2) {
                    if(!(args[1] instanceof Boolean))return false;
                    try{return bridge.setAutoOff((Boolean)args[1]);}
                    catch(Exception e){
                        Log.w(TAG,"OEM CEC auto-device-off rejected",e);
                        return false;
                    }
                }
                return objectMethod(proxy,name,args,"OemCecAutoOff");
            });
            Method add=category.getClass().getMethod("addPreference",preferenceClass);
            boolean first=Boolean.TRUE.equals(add.invoke(category,cec));
            boolean second=Boolean.TRUE.equals(add.invoke(category,autoOff));
            return first && second;
        }catch(Exception e){
            // Never bypass the manufacturer's mandatory power confirmation.
            Log.i(TAG,"CEC uses original OEM settings screen on this board",e);
            return false;
        }
    }

    private static Object makeSwitch(Context context,Class<?> pref,
                                     String key,String title,String summary,
                                     boolean checked,int order) throws Exception {
        Class<?> type=Class.forName("androidx.preference.SwitchPreference");
        Object item=type.getConstructor(Context.class).newInstance(context);
        pref.getMethod("setKey",String.class).invoke(item,key);
        pref.getMethod("setTitle",CharSequence.class).invoke(item,title);
        pref.getMethod("setSummary",CharSequence.class).invoke(item,summary);
        pref.getMethod("setOrder",int.class).invoke(item,order);
        pref.getMethod("setPersistent",boolean.class).invoke(item,false);
        type.getMethod("setChecked",boolean.class).invoke(item,checked);
        return item;
    }

    private static void attach(Class<?> preference,Object item,Class<?> type,
                               InvocationHandler handler) throws Exception {
        Object proxy=Proxy.newProxyInstance(type.getClassLoader(),new Class<?>[]{type},handler);
        preference.getMethod("setOnPreferenceChangeListener",type).invoke(item,proxy);
    }
    private static Object objectMethod(Object proxy,String name,Object[] args,String text) {
        if("toString".equals(name))return text;
        if("hashCode".equals(name))return System.identityHashCode(proxy);
        if("equals".equals(name))return args!=null && args.length>0 && proxy==args[0];
        return false;
    }
}

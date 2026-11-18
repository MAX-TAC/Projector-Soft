package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.util.Log;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/** The one 8054 CEC control with a stock setter independent of main CEC and eARC. */
final class OemCecAutoOff8054 {
    static final String CEC_KEY="projectorsoft_cec_status_8054";
    static final String OFF_KEY="projectorsoft_cec_auto_off_8054";
    private static final String TAG="TvSettingsPlusMOD";

    private OemCecAutoOff8054() {}

    private static final class Bridge implements CecAutoOff8054.Backend {
        private final Object hdmi;
        private final Method getCec, getStandby, setStandby;

        Bridge(Context context) throws ReflectiveOperationException {
            hdmi=context.getSystemService("hdmi_control");
            if (hdmi==null) throw new IllegalStateException("HDMI control service unavailable");
            Class<?> manager=Class.forName("android.hardware.hdmi.HdmiControlManager");
            getCec=manager.getMethod("getHdmiCecEnabled");
            getStandby=manager.getMethod("getTvSendStandbyOnSleep");
            setStandby=manager.getMethod("setTvSendStandbyOnSleep",int.class);
        }

        @Override public CecAutoOff8054.State read() throws ReflectiveOperationException {
            int cec=(Integer)getCec.invoke(hdmi);
            int standby=(Integer)getStandby.invoke(hdmi);
            if (cec!=(Integer)getCec.invoke(hdmi)) return null;
            return CecAutoOff8054.State.decode(cec,standby);
        }

        @Override public void setAutoOff(boolean enabled) throws ReflectiveOperationException {
            setStandby.invoke(hdmi,enabled?1:0);
        }
    }

    static boolean install(Context context,Object group,Class<?> pref) {
        if (!"8054".equals(OemHdmiPortInfo.machineId())) return false;
        try {
            Object cec=pref.getConstructor(Context.class).newInstance(context);
            Class<?> switchType=Class.forName("androidx.preference.SwitchPreference");
            Object autoOff=switchType.getConstructor(Context.class).newInstance(context);
            pref.getMethod("setKey",String.class).invoke(cec,CEC_KEY);
            pref.getMethod("setTitle",CharSequence.class).invoke(cec,"HDMI-CEC");
            pref.getMethod("setOrder",int.class).invoke(cec,274);
            pref.getMethod("setSelectable",boolean.class).invoke(cec,false);
            pref.getMethod("setKey",String.class).invoke(autoOff,OFF_KEY);
            pref.getMethod("setTitle",CharSequence.class).invoke(autoOff,"Выключать HDMI-устройства вместе с проектором");
            pref.getMethod("setOrder",int.class).invoke(autoOff,275);
            pref.getMethod("setPersistent",boolean.class).invoke(autoOff,false);
            render(pref,cec,autoOff,read(context));

            Class<?> listener=Class.forName("androidx.preference.Preference$OnPreferenceChangeListener");
            Object callback=Proxy.newProxyInstance(listener.getClassLoader(),new Class<?>[]{listener},
                (proxy,method,args)->{
                    switch (method.getName()) {
                        case "hashCode": return System.identityHashCode(proxy);
                        case "equals": return proxy==args[0];
                        case "toString": return "CecAutoOff8054";
                        case "onPreferenceChange":
                            if (args==null || args.length<2 || !(args[1] instanceof Boolean) || !"8054".equals(OemHdmiPortInfo.machineId()))
                                return false;
                            try {
                                Bridge bridge=new Bridge(context);
                                boolean expected=(Boolean)switchType.getMethod("isChecked").invoke(autoOff);
                                CecAutoOff8054.Result result=CecAutoOff8054.apply(bridge,expected,(Boolean)args[1]);
                                render(pref,cec,autoOff,read(context));
                                if (result!=CecAutoOff8054.Result.APPLIED) {
                                    pref.getMethod("setSummary",CharSequence.class)
                                        .invoke(autoOff,"Изменение не подтверждено · проверьте состояние");
                                }
                                // render() always uses a fresh OEM getter, including if CEC changed.
                                return false;
                            } catch (Exception error) {
                                pref.getMethod("setEnabled",boolean.class).invoke(autoOff,false);
                                pref.getMethod("setSummary",CharSequence.class).invoke(autoOff,"Состояние недоступно");
                                Log.w(TAG,"Unable to confirm the HDMI auto-off setting",error);
                                return false;
                            }
                        default: return false;
                    }
                });
            pref.getMethod("setOnPreferenceChangeListener",listener).invoke(autoOff,callback);
            Method add=group.getClass().getMethod("addPreference",pref);
            boolean statusAdded=Boolean.TRUE.equals(add.invoke(group,cec));
            boolean switchAdded=Boolean.TRUE.equals(add.invoke(group,autoOff));
            return statusAdded || switchAdded;
        } catch (Exception e) {
            Log.w(TAG,"8054 HDMI auto-off unavailable",e);
            return false;
        }
    }

    private static CecAutoOff8054.State read(Context context) {
        try {
            return new Bridge(context).read();
        } catch (Exception error) {
            Log.w(TAG,"Cannot read HDMI control settings",error);
            return null;
        }
    }

    static void refresh(Context context,Object cec,Object autoOff) throws ReflectiveOperationException {
        if (cec==null || autoOff==null) return;
        render(Class.forName("androidx.preference.Preference"),cec,autoOff,read(context));
    }

    private static void render(Class<?> pref,Object cec,Object autoOff,CecAutoOff8054.State state)
            throws ReflectiveOperationException {
        Method summary=pref.getMethod("setSummary",CharSequence.class);
        summary.invoke(cec,state==null?"Состояние недоступно":state.cecEnabled?"Включён":"Выключен");
        pref.getMethod("setEnabled",boolean.class).invoke(autoOff,state!=null && state.cecEnabled);
        if (state!=null) autoOff.getClass().getMethod("setChecked",boolean.class).invoke(autoOff,state.autoOff);
        summary.invoke(autoOff,state==null?"Состояние недоступно"
                :state.cecEnabled?"Параметр ожидания при выключении проектора":"Доступно при включённом HDMI-CEC");
    }
}

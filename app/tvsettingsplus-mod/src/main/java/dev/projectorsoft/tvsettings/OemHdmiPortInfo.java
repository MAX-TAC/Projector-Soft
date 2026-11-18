package dev.projectorsoft.tvsettings;
import android.content.Context;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import java.lang.reflect.Proxy;
import java.util.*;

/** Read-only logical port inventory; no tune(), CEC, EDID, or property writes. */
public final class OemHdmiPortInfo {
    private OemHdmiPortInfo() {}
    public static String machineId() {
        try {return (String)Class.forName("android.os.SystemProperties").getMethod("get",String.class,String.class)
            .invoke(null,"ro.rjpj.machine.id","");}catch(Exception ex){return "";}
    }
    public static boolean install(Context context,Object category,Class<?> pref,View root) {
        if(!"8054".equals(machineId()))return false;
        try {
            int count=Build.class.getField("A_RJ_HDMI_PORT_COUNT").getInt(null);
            Object raw=Build.class.getField("A_RJ_HDMI_PORTS").get(null);
            Object manager=context.getSystemService(Context.TV_INPUT_SERVICE);
            TvInputManager tv=manager instanceof TvInputManager?(TvInputManager)manager:null;
            String metadata=raw instanceof String?(String)raw:null;
            List<Hdmi8054Policy.Port> ports=ports(tv,count,metadata);
            if(ports.isEmpty())return false;
            Map<String,Object> rows=new LinkedHashMap<>();
            for(int i=0;i<ports.size();i++) {
                Hdmi8054Policy.Port port=ports.get(i);
                Object row=entry(context,category,pref,port.hardware,port.label,status(tv,port),271+i,tv,count,metadata);
                if(row!=null)rows.put(port.hardware,row);
            }
            if(root!=null && tv!=null && !rows.isEmpty())observe(root,tv,pref,count,metadata,rows);
            return !rows.isEmpty();
        }catch(Exception ex){Log.w("TvSettingsPlusMOD","HDMI port inventory unavailable",ex);return false;}
    }

    private static List<Hdmi8054Policy.Port> ports(TvInputManager tv,int count,String metadata) {
        List<String> ids=new ArrayList<>();
        List<TvInputInfo> inputs=tv==null?null:tv.getTvInputList();
        if(inputs!=null)for(TvInputInfo input:inputs) {
            if(input!=null && input.getType()==TvInputInfo.TYPE_HDMI)ids.add(input.getId());
        }
        return Hdmi8054Policy.ports(machineId(),count,metadata,ids);
    }

    private static String status(TvInputManager tv,Hdmi8054Policy.Port port) {
        if(tv==null) return "Служба TV Input недоступна";
        if(port.inputId==null) return "Вход недоступен или неоднозначен · нажмите для обновления";
        try {
            switch(tv.getInputState(port.inputId)) {
                case TvInputManager.INPUT_STATE_CONNECTED: return "Подключён · нажмите для обновления";
                case TvInputManager.INPUT_STATE_CONNECTED_STANDBY: return "В режиме ожидания · нажмите для обновления";
                case TvInputManager.INPUT_STATE_DISCONNECTED: return "Отключён · нажмите для обновления";
                default: break;
            }
        } catch(RuntimeException ex) {
            Log.w("TvSettingsPlusMOD","HDMI input state unavailable",ex);
        }
        return "Состояние недоступно · нажмите для обновления";
    }

    private static void observe(View root,TvInputManager tv,Class<?> pref,int count,String metadata,
                                Map<String,Object> rows) throws ReflectiveOperationException {
        java.lang.reflect.Method summary=pref.getMethod("setSummary",CharSequence.class);
        Runnable refresh=()-> {
            if(!root.isAttachedToWindow())return;
            try {
                Map<String,String> statuses=new HashMap<>();
                for(Hdmi8054Policy.Port port:ports(tv,count,metadata))statuses.put(port.hardware,status(tv,port));
                for(Map.Entry<String,Object> row:rows.entrySet()) {
                    summary.invoke(row.getValue(),statuses.getOrDefault(row.getKey(),"Вход недоступен"));
                }
            }catch(Exception ex){Log.w("TvSettingsPlusMOD","HDMI refresh unavailable",ex);}
        };
        TvInputManager.TvInputCallback callback=new TvInputManager.TvInputCallback() {
            @Override public void onInputAdded(String inputId){refresh.run();}
            @Override public void onInputRemoved(String inputId){refresh.run();}
            @Override public void onInputStateChanged(String inputId,int state){refresh.run();}
        };
        View.OnAttachStateChangeListener lifecycle=new View.OnAttachStateChangeListener() {
            private boolean registered;
            @Override public void onViewAttachedToWindow(View view) {
                if(!registered)try {
                    tv.registerCallback(callback,new Handler(Looper.getMainLooper()));
                    registered=true;
                }catch(RuntimeException ex){Log.w("TvSettingsPlusMOD","HDMI events unavailable",ex);}
                refresh.run();
            }
            @Override public void onViewDetachedFromWindow(View view) {
                if(registered)try {
                    tv.unregisterCallback(callback);
                }catch(RuntimeException ex){Log.w("TvSettingsPlusMOD","Cannot unregister HDMI events",ex);}
                finally{registered=false;}
            }
        };
        root.addOnAttachStateChangeListener(lifecycle);
        if(root.isAttachedToWindow())lifecycle.onViewAttachedToWindow(root);
    }

    private static Object entry(Context context,Object group,Class<?> pref,String key,String title,String summary,
                                 int order,TvInputManager tv,int count,String metadata) throws Exception {
        Object item=pref.getConstructor(Context.class).newInstance(context);
        pref.getMethod("setKey",String.class).invoke(item,"projectorsoft_hdmi_info_"+key);
        pref.getMethod("setTitle",CharSequence.class).invoke(item,title);
        pref.getMethod("setSummary",CharSequence.class).invoke(item,summary);
        pref.getMethod("setOrder",int.class).invoke(item,order);
        pref.getMethod("setSelectable",boolean.class).invoke(item,tv!=null);
        if(tv!=null) {
            Class<?> listener=Class.forName("androidx.preference.Preference$OnPreferenceClickListener");
            Object callback=Proxy.newProxyInstance(listener.getClassLoader(),new Class<?>[]{listener},
                (proxy,method,args)-> {
                    switch(method.getName()) {
                        case "onPreferenceClick":
                            String current="Состояние недоступно";
                            try {
                                for(Hdmi8054Policy.Port port:ports(tv,count,metadata)) {
                                    if(key.equals(port.hardware)) {
                                        current=status(tv,port);
                                        break;
                                    }
                                }
                            } catch(RuntimeException ex) {
                                Log.w("TvSettingsPlusMOD","HDMI input state unavailable",ex);
                            }
                            pref.getMethod("setSummary",CharSequence.class).invoke(item,current);
                            return true;
                        case "hashCode": return System.identityHashCode(proxy);
                        case "equals": return proxy==args[0];
                        case "toString": return "HdmiPortStatus:"+key;
                        default: return null;
                    }
                });
            pref.getMethod("setOnPreferenceClickListener",listener).invoke(item,callback);
        }
        return Boolean.TRUE.equals(group.getClass().getMethod("addPreference",pref).invoke(group,item)) ? item : null;
    }
}

package dev.projectorsoft.tvsettings;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

/** Native HDMI rows open the stock LiveTV passthrough route for verified ports. */
public final class OemHdmiPortInfo {
    private static final Map<Object,WeakReference<Observer>> OBSERVERS = new WeakHashMap<>();

    private OemHdmiPortInfo() {}
    public static String machineId() {
        try {return (String)Class.forName("android.os.SystemProperties").getMethod("get",String.class,String.class)
            .invoke(null,"ro.rjpj.machine.id","");}catch(Exception ex){return "";}
    }
    public static boolean install(Context context,Object category,Class<?> pref) {
        if(!"8054".equals(machineId()))return false;
        try {
            int count=Build.class.getField("A_RJ_HDMI_PORT_COUNT").getInt(null);
            Object raw=Build.class.getField("A_RJ_HDMI_PORTS").get(null);
            Object manager=context.getSystemService(Context.TV_INPUT_SERVICE);
            TvInputManager tv=manager instanceof TvInputManager?(TvInputManager)manager:null;
            String metadata=raw instanceof String?(String)raw:null;
            List<Hdmi8054Policy.Port> ports=ports(tv,count,metadata);
            if(ports.isEmpty())return false;
            boolean added=false;
            for(int i=0;i<ports.size();i++) {
                Hdmi8054Policy.Port port=ports.get(i);
                Object row=entry(context,category,pref,port.hardware,port.label,status(tv,port),271+i,tv,count,metadata);
                added |= row != null;
            }
            return added;
        }catch(Exception ex){Log.w("TvSettingsPlusMOD","HDMI port inventory unavailable",ex);return false;}
    }

    static Map<String, String> bootPorts(Context context) throws Exception {
        Map<String, String> result = new LinkedHashMap<>();
        if (!"8054".equals(machineId())) return result;
        int count = Build.class.getField("A_RJ_HDMI_PORT_COUNT").getInt(null);
        Object metadata = Build.class.getField("A_RJ_HDMI_PORTS").get(null);
        Object manager = context.getSystemService(Context.TV_INPUT_SERVICE);
        if (!(manager instanceof TvInputManager)) throw new IllegalStateException("TV Input unavailable");
        TvInputManager tv = (TvInputManager) manager;
        List<TvInputInfo> installed = tv.getTvInputList();
        if (installed == null) throw new IllegalStateException("TV Input inventory unavailable");
        for (Hdmi8054Policy.Port port : ports(installed, count, metadata instanceof String ? (String) metadata : null)) {
            if (port.inputId != null) result.put(port.inputId, port.label);
        }
        return result;
    }

    static String currentStatus(Context context, String hardware) {
        try {
            if (!"8054".equals(machineId())) return "Вход недоступен";
            int count = Build.class.getField("A_RJ_HDMI_PORT_COUNT").getInt(null);
            Object metadata = Build.class.getField("A_RJ_HDMI_PORTS").get(null);
            TvInputManager tv = context.getSystemService(TvInputManager.class);
            for (Hdmi8054Policy.Port port : ports(tv, count, metadata instanceof String ? (String) metadata : null)) {
                if (port.hardware.equals(hardware)) return status(tv, port);
            }
        } catch (Exception unavailable) {
            Log.w("TvSettingsPlusMOD", "HDMI input state unavailable", unavailable);
        }
        return "Вход недоступен";
    }

    private static List<Hdmi8054Policy.Port> ports(TvInputManager tv,int count,String metadata) {
        return ports(tv == null ? null : tv.getTvInputList(), count, metadata);
    }

    private static List<Hdmi8054Policy.Port> ports(List<TvInputInfo> inputs,int count,String metadata) {
        List<String> ids=new ArrayList<>();
        if(inputs!=null)for(TvInputInfo input:inputs) {
            if(input!=null && input.getType()==TvInputInfo.TYPE_HDMI)ids.add(input.getId());
        }
        return Hdmi8054Policy.ports(machineId(),count,metadata,ids);
    }

    private static String status(TvInputManager tv,Hdmi8054Policy.Port port) {
        if(tv==null) return "Служба TV Input недоступна";
        if(port.inputId==null) return "Вход недоступен или неоднозначен";
        try {
            switch(tv.getInputState(port.inputId)) {
                case TvInputManager.INPUT_STATE_CONNECTED: return "Подключен";
                case TvInputManager.INPUT_STATE_CONNECTED_STANDBY:
                    // On MTK 8054 this state is returned for BOTH empty ports:
                    // it must NOT be displayed as evidence of a plugged-in cable.
                    return "Нет сигнала";
                case TvInputManager.INPUT_STATE_DISCONNECTED: return "Нет подключения";
                default: break;
            }
        } catch(RuntimeException ex) {
            Log.w("TvSettingsPlusMOD","HDMI input state unavailable",ex);
        }
        return "Состояние недоступно";
    }

    private static Intent liveTvIntent(Context context,Hdmi8054Policy.Port port) {
        if(port.inputId==null)return null;
        // Exact stock SourceFragment.switchToHDMI uses ACTION_VIEW with this passthrough URI.
        Intent intent=new Intent(Intent.ACTION_VIEW,TvContract.buildChannelUriForPassthroughInput(port.inputId))
                .setPackage(Hdmi8054Policy.LIVE_TV).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PackageManager pm=context.getPackageManager();
        ResolveInfo resolved=pm.resolveActivity(intent,PackageManager.MATCH_DEFAULT_ONLY);
        ActivityInfo info=resolved==null?null:resolved.activityInfo;
        if(info==null || info.applicationInfo==null || info.name==null
                || !Hdmi8054Policy.LIVE_TV.equals(info.packageName))return null;
        ComponentName component=new ComponentName(info.packageName,info.name);
        boolean system=(info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)!=0;
        boolean enabled=info.enabled && info.applicationInfo.enabled
                && pm.getComponentEnabledSetting(component)<=PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                && pm.getApplicationEnabledSetting(info.packageName)<=PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        boolean permission=info.permission==null || info.permission.isEmpty()
                || context.checkSelfPermission(info.permission)==PackageManager.PERMISSION_GRANTED;
        return Hdmi8054Policy.mayLaunch(info.packageName,system,enabled,info.exported,permission)
                ? intent.setComponent(component) : null;
    }

    static void bind(Object fragment,View root) {
        WeakReference<Observer> reference=OBSERVERS.get(fragment);
        Observer observer=reference==null ? null : reference.get();
        if(root==null || !"8054".equals(machineId())) {
            if(observer!=null)observer.bind(null);
            OBSERVERS.remove(fragment);
            return;
        }
        try {
            Method find=fragment.getClass().getMethod("findPreference",CharSequence.class);
            if(find.invoke(fragment,"projectorsoft_hdmi_info_HW6")==null
                    && find.invoke(fragment,"projectorsoft_hdmi_info_HW7")==null
                    && find.invoke(fragment,OemHdmiEdidHook.STATUS_KEY)==null
                    && find.invoke(fragment,OemHdmiAutoPlayHook.KEY)==null) {
                if(observer!=null)observer.bind(null);
                OBSERVERS.remove(fragment);
                return;
            }
            if(observer==null) {
                Context context=root.getContext();
                TvInputManager tv=context.getSystemService(TvInputManager.class);
                if(tv==null)return;
                int count=Build.class.getField("A_RJ_HDMI_PORT_COUNT").getInt(null);
                Object raw=Build.class.getField("A_RJ_HDMI_PORTS").get(null);
                Method summary=Class.forName("androidx.preference.Preference")
                        .getMethod("setSummary",CharSequence.class);
                observer=new Observer(context,fragment,find,summary,tv,count,raw instanceof String ? (String)raw : null);
                OBSERVERS.put(fragment,new WeakReference<>(observer));
            }
            observer.bind(root);
        }catch(Exception ex){Log.w("TvSettingsPlusMOD","HDMI view binding unavailable",ex);}
    }

    private static final class Observer extends HdmiViewLifecycle<View>
            implements View.OnAttachStateChangeListener, ViewTreeObserver.OnWindowFocusChangeListener {
        private final Context context;
        private final Object fragment;
        private final Method find;
        private final Method summary;
        private final TvInputManager tv;
        private final int count;
        private final String metadata;
        private final TvInputManager.TvInputCallback callback=new TvInputManager.TvInputCallback() {
            @Override public void onInputAdded(String id){inputChanged();}
            @Override public void onInputRemoved(String id){inputChanged();}
            @Override public void onInputStateChanged(String id,int state){inputChanged();}
        };

        Observer(Context context,Object fragment,Method find,Method summary,TvInputManager tv,int count,String metadata) {
            this.context=context;
            this.fragment=fragment;
            this.find=find;
            this.summary=summary;
            this.tv=tv;
            this.count=count;
            this.metadata=metadata;
        }

        @Override public void onViewAttachedToWindow(View root){attached(root);}
        @Override public void onViewDetachedFromWindow(View root){detached(root);}
        @Override boolean isAttached(View root){return root.isAttachedToWindow();}
        @Override void addListener(View root){root.addOnAttachStateChangeListener(this);}
        @Override void removeListener(View root){root.removeOnAttachStateChangeListener(this);}
        @Override public void onWindowFocusChanged(boolean focused){if(focused)windowFocused();}
        @Override void addFocusListener(View root){
            root.getViewTreeObserver().addOnWindowFocusChangeListener(this);
        }
        @Override void removeFocusListener(View root){
            ViewTreeObserver observer=root.getViewTreeObserver();
            if(observer.isAlive())observer.removeOnWindowFocusChangeListener(this);
        }
        @Override boolean subscribe(){
            try {
                tv.registerCallback(callback,new Handler(Looper.getMainLooper()));
                return true;
            }catch(RuntimeException ex){
                Log.w("TvSettingsPlusMOD","HDMI events unavailable",ex);
                return false;
            }
        }
        @Override void unsubscribe(){
            try {
                tv.unregisterCallback(callback);
            }catch(RuntimeException ex){
                Log.w("TvSettingsPlusMOD","Cannot unregister HDMI events",ex);
            }
        }
        @Override void refresh(){
            try {
                Map<String,String> statuses=new HashMap<>();
                for(Hdmi8054Policy.Port port:ports(tv,count,metadata))statuses.put(port.hardware,status(tv,port));
                for(String hardware:new String[]{"HW6","HW7"}) {
                    Object row=find.invoke(fragment,"projectorsoft_hdmi_info_"+hardware);
                    if(row!=null)summary.invoke(row,statuses.getOrDefault(hardware,"Вход недоступен"));
                }
                Object edid=find.invoke(fragment,OemHdmiEdidHook.STATUS_KEY);
                if(edid!=null)summary.invoke(edid,OemHdmiEdidHook.storedSummary(context));
                OemHdmiAutoPlayHook.refresh(find.invoke(fragment,OemHdmiAutoPlayHook.KEY));
                OemCecAutoOff8054.refresh(context,find.invoke(fragment,OemCecAutoOff8054.CEC_KEY),
                        find.invoke(fragment,OemCecAutoOff8054.OFF_KEY));
            }catch(Exception ex){Log.w("TvSettingsPlusMOD","HDMI refresh unavailable",ex);}
        }
    }

    private static Object entry(Context context,Object group,Class<?> pref,String key,String title,String summary,
                                 int order,TvInputManager tv,int count,String metadata) throws Exception {
        Object item=pref.getConstructor(Context.class).newInstance(context);
        pref.getMethod("setKey",String.class).invoke(item,"projectorsoft_hdmi_info_"+key);
        pref.getMethod("setTitle",CharSequence.class).invoke(item,title);
        pref.getMethod("setSummary",CharSequence.class).invoke(item,summary);
        pref.getMethod("setOrder",int.class).invoke(item,order);
        // A port without a live signal must not look selectable: the owner
        // asked for unconnected inputs to be inactive on the source screen.
        boolean connected = summary != null && summary.startsWith("Подключен");
        pref.getMethod("setEnabled",boolean.class).invoke(item,connected);
        pref.getMethod("setSelectable",boolean.class).invoke(item,connected && tv!=null);
        if(tv!=null) {
            Class<?> listener=Class.forName("androidx.preference.Preference$OnPreferenceClickListener");
            Object callback=Proxy.newProxyInstance(listener.getClassLoader(),new Class<?>[]{listener},
                (proxy,method,args)-> {
                    switch(method.getName()) {
                        case "onPreferenceClick":
                            String failure="Вход недоступен или неоднозначен";
                            try {
                                for(Hdmi8054Policy.Port port:ports(tv,count,metadata)) {
                                    if(!key.equals(port.hardware) || port.inputId==null)continue;
                                    failure="Штатный экран HDMI недоступен";
                                    Intent target=liveTvIntent(context,port);
                                    if(target!=null){
                                        context.startActivity(target);
                                        return true;
                                    }
                                    break;
                                }
                            } catch(RuntimeException ex) {
                                Log.w("TvSettingsPlusMOD","Cannot open HDMI input "+key,ex);
                            }
                            pref.getMethod("setSummary",CharSequence.class).invoke(item,failure);
                            return true;
                        case "hashCode": return System.identityHashCode(proxy);
                        case "equals": return proxy==args[0];
                        case "toString": return "HdmiPortSelect:"+key;
                        default: return null;
                    }
                });
            pref.getMethod("setOnPreferenceClickListener",listener).invoke(item,callback);
        }
        return Boolean.TRUE.equals(group.getClass().getMethod("addPreference",pref).invoke(group,item)) ? item : null;
    }
}

package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * RUIOS native "Source on power-on" selector.
 * Uses precisely the persisted property set by original
 * PowerOnSourceSecondaryController; does NOT switch the active input,
 * disable HDMI-CEC/eARC, or modify autofocus/keystone.
 */
public final class OemPowerOnSourceHook {
    private static final String TAG="TvSettingsPlusMOD";
    private static final String PROPERTY="persist.trd.source_id";
    private OemPowerOnSourceHook() {}

    private static String propGet() throws Exception {
        Class<?> cls=Class.forName("android.os.SystemProperties");
        return (String)cls.getMethod("get",String.class,String.class)
                .invoke(null,PROPERTY,"8054".equals(OemHdmiPortInfo.machineId()) ? "" : "HOME");
    }

    private static boolean propSet(String choice) throws Exception {
        Class<?> cls=Class.forName("android.os.SystemProperties");
        cls.getMethod("set",String.class,String.class).invoke(null,PROPERTY,choice);
        return choice.equals(propGet());
    }

    private static int boardCount() throws Exception {
        Field f=Build.class.getField("A_RJ_HDMI_PORT_COUNT");
        return f.getInt(null);
    }

    private static String boardPorts() throws Exception {
        Field f=Build.class.getField("A_RJ_HDMI_PORTS");
        Object val=f.get(null);
        return val instanceof String?(String)val:null;
    }

    private static boolean hasAv() throws Exception {
        return Build.class.getField("A_RJ_SUPPORT_SETTINGS_SHOW_AV").getInt(null)==1;
    }

    static Map<String,String> available(Context context) throws Exception {
        int count = boardCount();
        String metadata = boardPorts();
        Map<String,String> choices = new LinkedHashMap<>();
        List<BootSourceMapping.Entry> entries = BootSourceMapping.candidates(count,metadata,hasAv());
        TvInputManager tv = context.getSystemService(TvInputManager.class);
        if (entries.isEmpty() || tv == null) return choices;
        List<TvInputInfo> installed = tv.getTvInputList();
        if (installed == null) return choices;

        List<String> hdmiIds = new ArrayList<>();
        for (TvInputInfo input : installed) {
            if (input != null && input.getType() == TvInputInfo.TYPE_HDMI && input.getId() != null) {
                hdmiIds.add(input.getId());
            }
        }
        String machine = OemHdmiPortInfo.machineId();
        List<Hdmi8054Policy.Port> verifiedPorts = Hdmi8054Policy.ports(machine,count,metadata,hdmiIds);
        if ("8054".equals(machine) && verifiedPorts.isEmpty()) return choices;

        for (BootSourceMapping.Entry entry : entries) {
            if (entry.hdmiHardwarePort == null) {
                choices.put(entry.id,entry.title);
            } else if ("8054".equals(machine)) {
                for (Hdmi8054Policy.Port port : verifiedPorts) {
                    if (entry.hdmiHardwarePort.equals(port.hardware) && port.inputId != null) {
                        choices.put(port.inputId,port.label);
                    }
                }
            } else {
                for (String id : hdmiIds) {
                    if (id.endsWith("/"+entry.hdmiHardwarePort)) {
                        choices.put(id,entry.title);
                        break;
                    }
                }
            }
        }
        return choices;
    }

    /**
     * Installs no entry on unknown 8052 board layouts or missing hardware API.
     * This is stricter than the OEM fallback that assumes 4 HDMI ports.
     */
    public static boolean install(Context context,Object category,Class<?> preferenceClass) {
        try {
            Map<String,String> titleById = available(context);
            if (titleById.size() < 2) return false;
            String current = propGet();
            if (!titleById.containsKey(current)) return false;
            List<String> values = new ArrayList<>(titleById.keySet());
            List<String> labels = new ArrayList<>(titleById.values());
            String[] displayedValue = {current};
            Class<?> list=Class.forName("androidx.preference.ListPreference");
            Object item=list.getConstructor(Context.class).newInstance(context);
            preferenceClass.getMethod("setKey",String.class)
                    .invoke(item,"projectorsoft_poweron_source");
            preferenceClass.getMethod("setTitle",CharSequence.class)
                    .invoke(item,"Источник при включении");
            preferenceClass.getMethod("setOrder",int.class).invoke(item,273);
            preferenceClass.getMethod("setSummary",CharSequence.class)
                    .invoke(item,titleById.get(current));
            preferenceClass.getMethod("setPersistent",boolean.class)
                    .invoke(item,false);
            list.getMethod("setEntries",CharSequence[].class)
                    .invoke(item,(Object)labels.toArray(new CharSequence[0]));
            list.getMethod("setEntryValues",CharSequence[].class)
                    .invoke(item,(Object)values.toArray(new CharSequence[0]));
            list.getMethod("setValue",String.class).invoke(item,current);

            Class<?> callbackType=Class.forName(
                    "androidx.preference.Preference$OnPreferenceChangeListener");
            InvocationHandler callback=(proxy,method,args)->{
                String name=method.getName();
                if("onPreferenceChange".equals(name) && args!=null && args.length>=2) {
                    String requested=String.valueOf(args[1]);
                    if(!titleById.containsKey(requested))return false;
                    try {
                        if (!available(context).containsKey(requested)
                                || !displayedValue[0].equals(propGet())) {
                            Toast.makeText(context,"Источник изменился. Переоткройте настройки.",Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        if (!propSet(requested)) return false;
                        displayedValue[0] = requested;
                        preferenceClass.getMethod("setSummary",CharSequence.class)
                                .invoke(item,titleById.get(requested));
                        return true;
                    }catch(Exception ex){
                        Log.w(TAG,"Cannot set native source on boot",ex);
                        return false;
                    }
                }
                if("toString".equals(name))return "OemPowerOnSource";
                if("hashCode".equals(name))return System.identityHashCode(proxy);
                if("equals".equals(name))return args!=null&&args.length>0&&proxy==args[0];
                return false;
            };
            Object proxy=Proxy.newProxyInstance(callbackType.getClassLoader(),
                    new Class<?>[]{callbackType},callback);
            preferenceClass.getMethod("setOnPreferenceChangeListener",callbackType)
                    .invoke(item,proxy);
            Object accepted=category.getClass().getMethod("addPreference",preferenceClass)
                    .invoke(category,item);
            return Boolean.TRUE.equals(accepted);
        }catch(Exception e){
            Log.w(TAG,"Native source-on-boot preferences unavailable",e);
            return false;
        }
    }
}


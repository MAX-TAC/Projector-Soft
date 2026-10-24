package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.content.ContentResolver;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.os.Build;
import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
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
                .invoke(null,PROPERTY,"HOME");
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

    private static List<BootSourceMapping.Entry> available(Context context) throws Exception {
        List<BootSourceMapping.Entry> entries=BootSourceMapping.candidates(
                boardCount(),boardPorts(),hasAv());
        if(entries.isEmpty())return entries;
        Object manager=context.getSystemService(Context.TV_INPUT_SERVICE);
        if(!(manager instanceof TvInputManager))return java.util.Collections.emptyList();

        // Unlike factory fallback guesses, require a real MediaTek TV input.
        List<TvInputInfo> installed=((TvInputManager)manager).getTvInputList();
        if(installed==null)return java.util.Collections.emptyList();
        List<BootSourceMapping.Entry> actual=new ArrayList<>();
        for(BootSourceMapping.Entry option:entries) {
            if(option.hdmiHardwarePort==null) {
                actual.add(option);
                continue;
            }
            boolean found=false;
            for(TvInputInfo input:installed) {
                if(input.getType()==TvInputInfo.TYPE_HDMI && input.getId()!=null
                        && input.getId().endsWith("/"+option.hdmiHardwarePort)){
                    found=true;
                    break;
                }
            }
            if(found)actual.add(option);
        }
        return actual;
    }

    /**
     * Installs no entry on unknown 8052 board layouts or missing hardware API.
     * This is stricter than the OEM fallback that assumes 4 HDMI ports.
     */
    public static boolean install(Context context,Object category,Class<?> preferenceClass) {
        try {
            List<BootSourceMapping.Entry> candidates=available(context);
            if(candidates.size()<2)return false;
            String current=propGet();
            if(!BootSourceMapping.isKnownState(candidates,current))return false;

            List<String> labels=new ArrayList<>();
            List<String> values=new ArrayList<>();
            Map<String,String> titleById=new HashMap<>();
            Object tv=context.getSystemService(Context.TV_INPUT_SERVICE);
            List<TvInputInfo> tvInputs=((TvInputManager)tv).getTvInputList();
            for(BootSourceMapping.Entry entry:candidates) {
                String id=entry.id;
                if(entry.hdmiHardwarePort!=null) {
                    id=null;
                    for(TvInputInfo input:tvInputs) {
                        if(input.getType()==TvInputInfo.TYPE_HDMI && input.getId()!=null
                                && input.getId().endsWith("/"+entry.hdmiHardwarePort)) {
                            id=input.getId();
                            break;
                        }
                    }
                }
                if(id==null || titleById.containsKey(id))continue;
                labels.add(entry.title);
                values.add(id);
                titleById.put(id,entry.title);
            }
            if(!titleById.containsKey(current)) {
                // A known OEM legacy ID may differ from TvInputManager's registered ID.
                // Avoid an unselected UI and preserve the original OEM control.
                return false;
            }
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
                        List<BootSourceMapping.Entry> now=available(context);
                        if(!BootSourceMapping.isKnownState(now,requested))return false;
                        if(!propSet(requested))return false;
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

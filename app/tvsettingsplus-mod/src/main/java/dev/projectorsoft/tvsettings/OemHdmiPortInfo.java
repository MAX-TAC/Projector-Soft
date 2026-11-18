package dev.projectorsoft.tvsettings;
import android.content.Context;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.os.Build;
import android.util.Log;
import java.util.*;

/** Read-only logical port inventory; no tune(), CEC, EDID, or property writes. */
public final class OemHdmiPortInfo {
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
            List<String> ids=new ArrayList<>();
            if(manager instanceof TvInputManager) {
                List<TvInputInfo> inputs=((TvInputManager)manager).getTvInputList();
                if(inputs!=null)for(TvInputInfo input:inputs)
                    if(input.getType()==TvInputInfo.TYPE_HDMI)ids.add(input.getId());
            }
            List<Hdmi8054Policy.Port> ports=Hdmi8054Policy.ports(machineId(),count,raw instanceof String?(String)raw:null,ids);
            if(ports.isEmpty())return entry(context,category,pref,"unknown","HDMI · данные портов",
                "Соответствие входов не подтверждено. Используйте штатный раздел источников",271);
            boolean added=false;
            for(int i=0;i<ports.size();i++) {
                Hdmi8054Policy.Port port=ports.get(i);
                added=entry(context,category,pref,port.hardware,port.label,
                    port.inputId==null?"TV Input не найден или неоднозначен; используйте штатный раздел источников":
                    "Логический вход найден; сигнал и EDID проверяются в штатном разделе источников",271+i)||added;
            }
            return added;
        }catch(Exception ex){Log.w("TvSettingsPlusMOD","HDMI port inventory unavailable",ex);return false;}
    }
    private static boolean entry(Context context,Object group,Class<?> pref,String key,String title,String summary,int order) throws Exception {
        Object item=pref.getConstructor(Context.class).newInstance(context);
        pref.getMethod("setKey",String.class).invoke(item,"projectorsoft_hdmi_info_"+key);
        pref.getMethod("setTitle",CharSequence.class).invoke(item,title);
        pref.getMethod("setSummary",CharSequence.class).invoke(item,summary);
        pref.getMethod("setOrder",int.class).invoke(item,order);
        pref.getMethod("setSelectable",boolean.class).invoke(item,false);
        return Boolean.TRUE.equals(group.getClass().getMethod("addPreference",pref).invoke(group,item));
    }
}

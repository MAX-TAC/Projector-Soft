package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import java.lang.reflect.Field;

/**
 * Read-only OEM hardware/build information in Android TV's native menu.
 * Based on SystemInfoSecondaryController on stock RUIOS 8052.
 * Factory hides device/ambient temperature readouts; we do the same rather
 * than present dummy 25 C values as actual measurements.
 */
public final class OemSystemInfoHook {
    private static final String TAG="TvSettingsPlusMOD";

    private OemSystemInfoHook() {}

    private static String oemString(String field) {
        try {
            Field f=Build.class.getField(field);
            Object v=f.get(null);
            return v == null ? null : String.valueOf(v);
        }catch(Exception ex){return null;}
    }

    private static String firmwareVersion() {
        String time=oemString("A_RJ_BUILDTIME");
        String machine=oemString("A_RJ_MACHINE_ID");
        String rev=oemString("A_RJ_GITREV");
        if(time==null||machine==null||rev==null||
           time.length()<8||rev.length()<8)return Build.DISPLAY;
        String value=time.substring(0,8)+"_"+machine+"_"+rev.substring(0,8);
        try {
            String adapter=oemString("A_RJ_ADAPTER_ID");
            if("1".equals(adapter)) {
                Class<?> prop=Class.forName("android.os.SystemProperties");
                Object fpga=prop.getMethod("get",String.class,String.class)
                    .invoke(null,"rjpj.msg.fpga.version","");
                if(fpga instanceof String && !((String)fpga).isEmpty())
                    value+="_"+fpga;
            }
        }catch(Exception ex){Log.d(TAG,"FPGA version not accessible");}
        return value;
    }

    private static void entry(Object group,Class<?> prefClass,Context context,
                              String key,String title,String summary,int order) throws Exception {
        if(summary==null||summary.trim().isEmpty())return;
        Object p=prefClass.getConstructor(Context.class).newInstance(context);
        prefClass.getMethod("setKey",String.class).invoke(p,key);
        prefClass.getMethod("setTitle",CharSequence.class).invoke(p,title);
        prefClass.getMethod("setSummary",CharSequence.class).invoke(p,summary);
        prefClass.getMethod("setOrder",int.class).invoke(p,order);
        prefClass.getMethod("setSelectable",boolean.class).invoke(p,false);
        group.getClass().getMethod("addPreference",prefClass).invoke(group,p);
    }

    public static boolean install(Context context,Object category,Class<?> prefClass) {
        try {
            int count=0;
            String board=oemString("A_RJ_MACHINE_ID");
            if(board==null||board.isEmpty()) board=Build.MODEL;
            entry(category,prefClass,context,"projectorsoft_device_model",
                    "Модель устройства",board,281);
            count++;
            entry(category,prefClass,context,"projectorsoft_system_version",
                    "Версия прошивки",firmwareVersion(),282);
            count++;
            entry(category,prefClass,context,"projectorsoft_android_version",
                    "Android",Build.VERSION.RELEASE,283);
            count++;
            String hdmi=oemString("A_RJ_HDMI_PORT_COUNT");
            if(hdmi!=null && !hdmi.isEmpty() && !"0".equals(hdmi)) {
                entry(category,prefClass,context,"projectorsoft_hdmi_port_count",
                        "Количество HDMI по конфигурации платы",hdmi,284);
                count++;
            }
            return count>0;
        }catch(Exception e){
            Log.w(TAG,"OEM info unavailable",e);
            return false;
        }
    }
}

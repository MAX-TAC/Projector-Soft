package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Runtime adapter for Alpha8054Policy. Does not invoke vendor HAL or mutate
 * settings. Unknown platform properties are treated as missing, never guessed.
 */
public final class OemAlpha8054Gate {
    private static final String TAG="TvSettingsPlusMOD";
    private OemAlpha8054Gate() {}

    private static String readProperty(String key) {
        try {
            Class<?> type=Class.forName("android.os.SystemProperties");
            Method method=type.getMethod("get",String.class,String.class);
            Object value=method.invoke(null,key,"");
            return value instanceof String?(String)value:"";
        }catch(Exception e){return "";}
    }

    private static String readBuildField(String name) {
        try {
            Field field=Build.class.getField(name);
            Object value=field.get(null);
            return value==null?"":String.valueOf(value);
        }catch(Exception e){return "";}
    }

    private static Integer hdmiPorts() {
        try {
            Object value=Build.class.getField("A_RJ_HDMI_PORT_COUNT").get(null);
            return value instanceof Number?((Number)value).intValue():null;
        }catch(Exception e){return null;}
    }

    public static Alpha8054Policy.State evaluate() {
        return Alpha8054Policy.evaluate(
            readProperty("ro.rjpj.machine.id"),
            readBuildField("A_RJ_MACHINE_ID"),hdmiPorts(),Build.VERSION.RELEASE);
    }

    public static boolean allowNativeControls() {
        return Alpha8054Policy.allowExperimentalControls(evaluate());
    }

    public static String report() {
        return "RUIOS ID: "+readProperty("ro.rjpj.machine.id")
            +", Build ID: "+readBuildField("A_RJ_MACHINE_ID")
            +", HDMI: "+(hdmiPorts()==null?"не определено":hdmiPorts())
            +", Android: "+Build.VERSION.RELEASE
            +"\n"+Alpha8054Policy.status(evaluate());
    }

    public static void installStatus(Context context,Object category,Class<?> preferenceClass) {
        try {
            Object item=preferenceClass.getConstructor(Context.class).newInstance(context);
            preferenceClass.getMethod("setKey",String.class).invoke(
                    item,"projectorsoft_alpha_8054_status");
            preferenceClass.getMethod("setTitle",CharSequence.class).invoke(
                    item,"Альфа 8054 — совместимость");
            preferenceClass.getMethod("setSummary",CharSequence.class).invoke(
                    item,Alpha8054Policy.status(evaluate()));
            preferenceClass.getMethod("setOrder",int.class).invoke(item,279);
            preferenceClass.getMethod("setSelectable",boolean.class).invoke(item,false);
            category.getClass().getMethod("addPreference",preferenceClass).invoke(category,item);
        }catch(Exception e){
            Log.w(TAG,"Read-only 8054 alpha status not shown",e);
        }
    }
}

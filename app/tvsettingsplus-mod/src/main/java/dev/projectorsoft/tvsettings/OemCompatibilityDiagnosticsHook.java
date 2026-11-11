package dev.projectorsoft.tvsettings;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * Read-only built-in projector preflight for nontechnical field testers.
 * No settings are changed; it neither installs APKs nor disables KuSettings.
 */
public final class OemCompatibilityDiagnosticsHook {
    private static final String TAG="TvSettingsPlusMOD";
    private static final String OEM_PACKAGE="com.my.settings";
    private static final String SOURCE_PKG="com.coolux.source";
    private static final String SOURCE_RECEIVER="com.coolux.source.service.BrightReceiver";
    private OemCompatibilityDiagnosticsHook() {}

    private static String buildField(String name) {
        try {
            Field f=Build.class.getField(name);
            Object value=f.get(null);
            return value == null ? "не определено" : String.valueOf(value);
        } catch(Exception e) {return "не определено";}
    }

    private static String property(String name) {
        try {
            Class<?> cls=Class.forName("android.os.SystemProperties");
            Method get=cls.getMethod("get",String.class,String.class);
            Object value=get.invoke(null,name,"");
            return value instanceof String && !((String)value).isEmpty()
                    ? (String)value : "не определено";
        }catch(Exception e){return "не определено";}
    }

    private static Integer hdmiCount() {
        try {
            Field f=Build.class.getField("A_RJ_HDMI_PORT_COUNT");
            Object value=f.get(null);
            return value instanceof Integer ? (Integer)value : null;
        }catch(Exception e){return null;}
    }

    @SuppressWarnings("deprecation")
    private static String apkVersion(Context context) {
        try {
            PackageInfo pi=context.getPackageManager().getPackageInfo(context.getPackageName(),0);
            return pi.versionName+" (versionCode "+pi.versionCode+")";
        }catch(Exception e){return "Не удалось прочитать установленную версию";}
    }

    private static String legacyStatus(Context context) {
        try {
            int state=context.getPackageManager().getApplicationEnabledSetting(OEM_PACKAGE);
            return ProjectorCompatibilityPolicy.legacyPackageState(state);
        }catch(IllegalArgumentException e) {
            return "Пакет не найден или недоступен. Это НЕ доказывает, что он удалён";
        }catch(Exception e){
            return "Доступ к состоянию ограничен. Не отключайте KuSettings";
        }
    }

    @SuppressWarnings("deprecation")
    private static String brightnessReceiver(Context context) {
        try {
            ActivityInfo info=context.getPackageManager().getReceiverInfo(
                new ComponentName(SOURCE_PKG,SOURCE_RECEIVER),0);
            if(info==null)return "Не определён";
            return info.enabled && info.exported
                    ? "OEM BrightReceiver объявлен и доступен для внешнего вызова (работа не проверена)"
                    : "OEM BrightReceiver не экспортирован или отключён";
        }catch(Exception e){
            return "Не удаётся подтвердить наличие BrightReceiver";
        }
    }

    public static String collect(Context context) {
        String id=property("ro.rjpj.machine.id");
        Integer portCount=hdmiCount();
        StringBuilder sb=new StringBuilder(900);
        sb.append("TvSettingsPlus MOD — предварительная проверка\n\n");
        sb.append("Приложение: ").append(apkVersion(context)).append("\n");
        sb.append("Android: ").append(Build.VERSION.RELEASE).append("\n");
        sb.append("Сборка: ").append(Build.DISPLAY).append("\n");
        sb.append("RUIOS Machine ID: ").append(id).append("\n");
        sb.append("Build.A_RJ_MACHINE_ID: ").append(buildField("A_RJ_MACHINE_ID")).append("\n");
        sb.append("Порты HDMI по OEM: ").append(portCount==null?"не определено":portCount).append("\n");
        sb.append("OEM customer ID: ").append(buildField("A_RJ_CUSTOMER_ID")).append("\n\n");
        sb.append("СОВМЕСТИМОСТЬ БАЗЫ\n")
                .append(ProjectorCompatibilityPolicy.boardWarning(id,portCount))
                .append("\n\n");
        sb.append("СТАТУС OEM\n");
        sb.append("KuSettings: ").append(legacyStatus(context)).append("\n");
        sb.append("KuSourceRJ: ").append(brightnessReceiver(context)).append("\n");
        sb.append("ПРОВЕРКА СЛУЖБ (только чтение)\n");
        sb.append(OemKuSourceServiceAudit.inspect(context)).append("\n");
        sb.append("Вентиляторы и защита от перегрева: работа не подтверждена этим отчётом\n");
        sb.append("HDMI-CEC/eARC/MEMC/DSP: требуется аппаратное тестирование\n\n");
        sb.append("ВАЖНО: не отключайте KuSettings и не устанавливайте сборку 8052 ");
        sb.append("на проектор 8054. Диагностика ничего не изменяет.");
        return sb.toString();
    }

    public static boolean install(Context context,Object category,Class<?> preferenceClass) {
        try {
            Object item=preferenceClass.getConstructor(Context.class).newInstance(context);
            preferenceClass.getMethod("setKey",String.class)
                    .invoke(item,"projectorsoft_compatibility_diagnostics");
            preferenceClass.getMethod("setTitle",CharSequence.class)
                    .invoke(item,"Диагностика совместимости MOD");
            preferenceClass.getMethod("setSummary",CharSequence.class)
                    .invoke(item,"Модель, HDMI, KuSettings и OEM-службы — только чтение");
            preferenceClass.getMethod("setOrder",int.class).invoke(item,286);
            Class<?> callbackType=Class.forName(
                    "androidx.preference.Preference$OnPreferenceClickListener");
            InvocationHandler callback=(proxy,method,args)-> {
                String name=method.getName();
                if("onPreferenceClick".equals(name)){
                    String report=collect(context);
                    new AlertDialog.Builder(context)
                        .setTitle("Диагностика TvSettingsPlus")
                        .setMessage(report)
                        .setPositiveButton("Закрыть",(dialog,which)->dialog.dismiss())
                        .setNeutralButton("Копировать",(dialog,which)->{
                            try {
                                ClipboardManager clipboard=(ClipboardManager)
                                    context.getSystemService(Context.CLIPBOARD_SERVICE);
                                if(clipboard!=null)clipboard.setPrimaryClip(
                                    ClipData.newPlainText("TvSettingsPlus diagnostics",report));
                            }catch(Exception e){
                                Log.w(TAG,"Could not copy diagnostics",e);
                            }
                        }).show();
                    return true;
                }
                if("toString".equals(name))return "TvSettingsPlusReadOnlyDiagnostics";
                if("hashCode".equals(name))return System.identityHashCode(proxy);
                if("equals".equals(name))return args!=null&&args.length>0&&proxy==args[0];
                return false;
            };
            Object listener=Proxy.newProxyInstance(callbackType.getClassLoader(),
                new Class<?>[]{callbackType},callback);
            preferenceClass.getMethod("setOnPreferenceClickListener",callbackType)
                    .invoke(item,listener);
            Object added=category.getClass().getMethod("addPreference",preferenceClass)
                    .invoke(category,item);
            return Boolean.TRUE.equals(added);
        }catch(Exception e){
            Log.w(TAG,"Read-only compatibility diagnostics unavailable",e);
            return false;
        }
    }
}

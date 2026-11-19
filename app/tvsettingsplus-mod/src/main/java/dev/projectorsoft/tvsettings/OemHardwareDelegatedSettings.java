package dev.projectorsoft.tvsettings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import java.lang.reflect.Field;

/**
 * Navigation-only bridges for vendor functions with nontrivial HAL side
 * effects. Do not reproduce CEC confirmation, game-mode resetTrapezoid, or
 * full-screen focus / keystone calibration in an AOSP toggle.
 */
public final class OemHardwareDelegatedSettings {
    private static final String TAG="TvSettingsPlusMOD";
    private static final String ACTIVITY="com.android.tv.settings.uicustom.MainActivity";
    private static final String SOURCE_ACTION="dev.projectorsoft.settings.SOURCES";
    private static final String SYSTEM_ACTION="android.settings.DEVICE_INFO_SETTINGS";
    private OemHardwareDelegatedSettings() {}

    private static boolean hdmiExists() {
        try {
            Field field=Build.class.getField("A_RJ_HDMI_PORT_COUNT");
            int count=field.getInt(null);
            return count>0 && count<=4;
        }catch(Exception exception) {
            return false; // No phantom hardware links for unknown boards
        }
    }

    private static boolean add(Context context,Object category,Class<?> preferenceClass,
                               String key,String title,String summary,
                               String action,int order) {
        try {
            ComponentName component=new ComponentName(context,ACTIVITY);
            context.getPackageManager().getActivityInfo(component,0);
            Object item=preferenceClass.getConstructor(Context.class).newInstance(context);
            preferenceClass.getMethod("setKey",String.class).invoke(item,key);
            preferenceClass.getMethod("setTitle",CharSequence.class).invoke(item,title);
            preferenceClass.getMethod("setSummary",CharSequence.class).invoke(item,summary);
            preferenceClass.getMethod("setOrder",int.class).invoke(item,order);
            Intent intent=new Intent(action);
            intent.setComponent(component);
            preferenceClass.getMethod("setIntent",Intent.class).invoke(item,intent);
            Object inserted=category.getClass().getMethod("addPreference",preferenceClass)
                                    .invoke(category,item);
            return Boolean.TRUE.equals(inserted);
        }catch(Exception exception) {
            Log.w(TAG,"OEM function link unavailable: "+key,exception);
            return false;
        }
    }

    private static boolean hasOriginalControl(Context context,String id) {
        try {
            return context.getResources().getIdentifier(id,"id",context.getPackageName())!=0;
        }catch(Exception exception) {
            return false; // No invented controls on unknown OEM variants
        }
    }

    /**
     * Factory system page already contains local/online firmware update UI.
     * A navigation-only entry preserves RuiYunOta confirmation/download flow.
     * It neither starts the OTA service nor triggers an update directly.
     */
    public static boolean installSystemLinks(Context context,Object systemCategory,
                                            Class<?> preferenceClass) {
        if(context==null||systemCategory==null||preferenceClass==null)return false;
        boolean online=hasOriginalControl(context,"tciv_system_online_update");
        boolean local=hasOriginalControl(context,"tciv_system_local_update");
        if(!OemDelegatedMenuPolicy.exposeFirmwareUpdates(online,local))return false;
        return add(context,systemCategory,preferenceClass,
            "projectorsoft_firmware_update_oem","Обновление прошивки",
            "Оригинальный раздел OTA и локального обновления, без автозапуска",
            SYSTEM_ACTION,289);
    }

    public static boolean installSourceLinks(Context context,Object sourceCategory,
                                          Class<?> preferenceClass,boolean nativeCec,boolean nativeBootApp) {
        if(context==null||sourceCategory==null||preferenceClass==null)return false;
        // App-on-boot is a factory source-page feature even on a board with
        // zero confirmed HDMI ports. Do not hide it behind HDMI gating.
        boolean added=false;
        if(!nativeBootApp && OemDelegatedMenuPolicy.exposePowerOnApp(
                hasOriginalControl(context,"tciv_source_power_on_app"))) {
            added=add(context,sourceCategory,preferenceClass,
                "projectorsoft_boot_application_oem","Приложение при включении",
                "Заводской выбор приложения для автозапуска",
                SOURCE_ACTION,275);
        }
        if(!hdmiExists())return added;
        added=add(context,sourceCategory,preferenceClass,
            "projectorsoft_hdmi_game_oem","Игровой режим HDMI",
            "Заводская настройка с сохранением коррекции трапеции",
            SOURCE_ACTION,276);
        if(!nativeCec) {
            added=add(context,sourceCategory,preferenceClass,
                "projectorsoft_hdmi_cec_oem","HDMI-CEC · расширенные настройки",
                "Заводской экран подтверждения и управления HDMI-устройствами",
                SOURCE_ACTION,277) || added;
        }
        return added;
    }

    public static void installProjectionLinks(Context context,Object projectionCategory,
                                              Class<?> preferenceClass) {
        if(context==null||projectionCategory==null||preferenceClass==null)return;
        add(context,projectionCategory,preferenceClass,
            "projectorsoft_calibration_oem","Фокус и коррекция трапеции",
            "Оригинальные экраны автофокуса и калибровки четырёх углов",
            "android.settings.SETTINGS",251);
    }
}

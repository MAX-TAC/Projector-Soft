package dev.projectorsoft.tvsettings;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Native Bluetooth remote assistance in AOSP TV settings.
 *
 * KuSettings.MyAutoBluetoothActivity specifically selects HID class 1280 and
 * filters OEM names, then invokes CachedBluetoothDevice.connect(true).
 * TvSettingsPlus cannot claim parity without that vendor-name connection
 * sequence. Therefore this is intentionally manual, read-only bonded
 * status plus a link to the stock Android TV AddAccessoryActivity.
 * It does not initiate discovery, pair, connect or enable Bluetooth itself.
 */
public final class OemBluetoothRemoteHook {
    private static final String TAG="TvSettingsPlusMOD";
    private static final String TARGET="com.android.tv.settings.accessories.AddAccessoryActivity";
    private OemBluetoothRemoteHook(){}

    private static boolean hasPairingActivity(Context context) {
        try {
            context.getPackageManager().getActivityInfo(
                new ComponentName(context,TARGET),0);
            return true;
        }catch(Exception e){return false;}
    }

    private static final class Snapshot {
        final String summary;
        final String details;
        Snapshot(String summary,String details){
            this.summary=summary;this.details=details;
        }
    }

    private static Snapshot snapshot(Context context) {
        try {
            BluetoothManager manager=(BluetoothManager)
                context.getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter adapter=manager==null?BluetoothAdapter.getDefaultAdapter()
                :manager.getAdapter();
            if(adapter==null){
                String message=OemBluetoothRemotePolicy.summary(false,false,true,-1);
                return new Snapshot(message,message);
            }
            boolean enabled=adapter.isEnabled();
            if(!enabled){
                String message=OemBluetoothRemotePolicy.summary(true,false,true,0);
                return new Snapshot(message,message);
            }
            Set<BluetoothDevice> paired=adapter.getBondedDevices();
            if(paired==null){
                String message=OemBluetoothRemotePolicy.summary(true,true,true,-1);
                return new Snapshot(message,message);
            }
            List<String> found=new ArrayList<>();
            for(BluetoothDevice device:paired) {
                if(device==null)continue;
                try {
                    BluetoothClass cls=device.getBluetoothClass();
                    int major=cls==null?-1:cls.getMajorDeviceClass();
                    if(!OemBluetoothRemotePolicy.potentialRemote(major))continue;
                    found.add(OemBluetoothRemotePolicy.safeName(device.getName()));
                }catch(SecurityException error){
                    return new Snapshot(
                        OemBluetoothRemotePolicy.summary(true,true,false,0),
                        "Нет разрешения на просмотр сопряжённых Bluetooth-устройств.");
                }
            }
            Collections.sort(found,String.CASE_INSENSITIVE_ORDER);
            String summary=OemBluetoothRemotePolicy.summary(true,true,true,found.size());
            if(found.isEmpty())return new Snapshot(summary,
                "Сопряжённых устройств класса HID (0x500) не обнаружено. "
                +"Это не означает, что пульт отсутствует.");
            StringBuilder text=new StringBuilder(summary);
            text.append("\n\nРанее сопряжённые HID-устройства:");
            for(String title:found)text.append("\n• ").append(title);
            text.append("\n\nСопряжение не означает текущее подключение. "
               +"Автоматический OEM-поиск пульта пока остаётся в KuSettings.");
            return new Snapshot(summary,text.toString());
        }catch(SecurityException e){
            String message=OemBluetoothRemotePolicy.summary(true,true,false,-1);
            return new Snapshot(message,message);
        }catch(Exception | LinkageError e){
            Log.w(TAG,"Could not read paired Bluetooth remote status",e);
            return new Snapshot("Не удалось получить состояние Bluetooth",
                "Недоступны данные Bluetooth. Заводской обработчик остаётся включён.");
        }
    }

    public static boolean install(Context context,Object category,Class<?> preference) {
        if(context==null || category==null || preference==null)return false;
        try {
            Object item=preference.getConstructor(Context.class).newInstance(context);
            preference.getMethod("setKey",String.class).invoke(
                item,"projectorsoft_bluetooth_remote");
            preference.getMethod("setTitle",CharSequence.class).invoke(
                item,"Bluetooth-пульт и сопряжение");
            preference.getMethod("setSummary",CharSequence.class).invoke(
                item,"Сопряжённые HID-устройства и штатный поиск аксессуаров");
            preference.getMethod("setOrder",int.class).invoke(item,243);
            Class<?> listenerType=Class.forName(
                "androidx.preference.Preference$OnPreferenceClickListener");
            InvocationHandler handler=(proxy,method,args)->{
                String name=method.getName();
                if("onPreferenceClick".equals(name)){
                    Snapshot current=snapshot(context);
                    AlertDialog.Builder dialog=new AlertDialog.Builder(context)
                       .setTitle("Bluetooth-пульт")
                       .setMessage(current.details)
                       .setNegativeButton("Закрыть",(d,which)->d.dismiss());
                    if(hasPairingActivity(context)){
                        dialog.setPositiveButton("Открыть сопряжение",(d,which)->{
                            try {
                                Intent open=new Intent();
                                open.setComponent(new ComponentName(context,TARGET));
                                context.startActivity(open);
                            }catch(Exception e){
                                Log.w(TAG,"Android TV pairing Activity unavailable",e);
                            }
                        });
                    }
                    dialog.show();
                    return true;
                }
                if("toString".equals(name))return "BluetoothRemoteAssistant";
                if("hashCode".equals(name))return System.identityHashCode(proxy);
                if("equals".equals(name))return args!=null && args.length>0 && args[0]==proxy;
                return false;
            };
            Object listener=Proxy.newProxyInstance(listenerType.getClassLoader(),
                new Class<?>[]{listenerType},handler);
            preference.getMethod("setOnPreferenceClickListener",listenerType)
                .invoke(item,listener);
            return Boolean.TRUE.equals(category.getClass().getMethod(
                "addPreference",preference).invoke(category,item));
        }catch(Exception | LinkageError e){
            Log.w(TAG,"Could not add native Bluetooth assistance preference",e);
            return false;
        }
    }
}

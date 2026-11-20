package dev.projectorsoft.tvsettings;

/** UI-only representation of bonded Bluetooth HID-class accessories. */
public final class OemBluetoothRemotePolicy {
    public static final int HID_PERIPHERAL_MAJOR_CLASS=0x500;
    private OemBluetoothRemotePolicy(){}
    public static boolean potentialRemote(int majorClass){
        return majorClass==HID_PERIPHERAL_MAJOR_CLASS;
    }
    public static String safeName(String raw){
        if(raw==null || raw.trim().isEmpty())return "HID-устройство";
        StringBuilder out=new StringBuilder(64);
        for(int i=0;i<raw.length() && out.length()<64;i++){
            char c=raw.charAt(i);
            if(Character.isISOControl(c))out.append(' ');
            else out.append(c);
        }
        String sanitized=out.toString().trim();
        return sanitized.isEmpty()?"HID-устройство":sanitized;
    }
    public static String summary(boolean adapterAvailable,boolean enabled,
                                 boolean permissionGranted,int bondedRemoteCount){
        if(!adapterAvailable)return "Bluetooth-адаптер недоступен";
        if(!permissionGranted)return "Нельзя прочитать список сопряжённых устройств";
        if(!enabled)return "Bluetooth выключен; настройки подключения доступны в Android TV";
        if(bondedRemoteCount<0)return "Список сопряжённых HID-устройств недоступен";
        return "Сопряжено HID-устройств: "+bondedRemoteCount
              +" (подключение не подтверждено)";
    }

    /** BootBroadcastReceiver reads this Global setting with default 1. */
    public static String autoPairSettingSummary(String raw) {
        if(raw==null)return "OEM AutoPair: значение не задано (при чтении загрузчик использует 1).";
        if("0".equals(raw))return "OEM AutoPair: настройка выключена; состояние службы не проверено.";
        if("1".equals(raw))return "OEM AutoPair: настройка включена; работа службы не проверена.";
        return "OEM AutoPair: неизвестное значение; работа службы не проверена.";
    }
}


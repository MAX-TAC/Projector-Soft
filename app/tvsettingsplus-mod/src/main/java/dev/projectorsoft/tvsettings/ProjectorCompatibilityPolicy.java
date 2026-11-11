package dev.projectorsoft.tvsettings;

/**
 * Conservative release-compatibility summary for the signed 8052 APK.
 * A matching static identifier never implies verified runtime operation.
 */
public final class ProjectorCompatibilityPolicy {
    private ProjectorCompatibilityPolicy() {}

    public static String boardWarning(String firmwareId,Integer hdmiPorts) {
        if("8054".equals(firmwareId))
            return "ВНИМАНИЕ: ревизия 8054. Этот APK создан из прошивки 8052, несовместимость не исключена.";
        if(!"8052".equals(firmwareId))
            return "Ревизия не подтверждена. Не устанавливайте эту сборку как рабочую.";
        if(hdmiPorts==null || hdmiPorts.intValue()!=1)
            return "8052 определён, но количество HDMI не соответствует исходной базе. Нужна проверка.";
        return "Соответствует идентификаторам базы 8052. Работа на устройстве ещё не подтверждена.";
    }

    public static String legacyPackageState(int state) {
        switch(state) {
            case 0:return "Состояние по умолчанию: работоспособность OEM watchdog не проверена";
            case 1:return "Приложение явно включено";
            case 2:return "Приложение явно отключено: резервное охлаждение требует проверки";
            case 3:return "Отключено пользователем: резервное охлаждение требует проверки";
            case 4:return "Отключено до первого использования: не считать безопасным для удаления";
            default:return "Неизвестный PackageManager state: "+state;
        }
    }
}

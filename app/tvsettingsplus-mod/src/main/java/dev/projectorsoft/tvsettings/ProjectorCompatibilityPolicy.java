package dev.projectorsoft.tvsettings;

/**
 * Conservative release-compatibility summary for the signed 8052 APK.
 * A matching static identifier never implies verified runtime operation.
 */
public final class ProjectorCompatibilityPolicy {
    private ProjectorCompatibilityPolicy() {}

    public static String boardWarning(String firmwareId,Integer hdmiPorts) {
        if("8054".equals(firmwareId)) {
            if(hdmiPorts==null || hdmiPorts.intValue()!=2)
                return "8054 определён, но количество HDMI не подтверждено. Функции MOD нельзя считать совместимыми.";
            return "По архивному сравнению: заводской TvSettingsPlus из 8054.1.0.1 " +
                    "совпадал с 8052.1.0.4 по SHA-256. Текущая прошивка и аппаратные " +
                    "функции MOD на 8054 не проверены; KuSettings не отключать.";
        }
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

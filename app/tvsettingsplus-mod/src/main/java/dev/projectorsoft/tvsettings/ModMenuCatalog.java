package dev.projectorsoft.tvsettings;

import java.util.Arrays;
import java.util.List;

/** The eight product sections and their allowed, argument-free routes. */
final class ModMenuCatalog {
    static final String PACKAGE="com.android.tv.settings";
    static final String NETWORK="connectivity.NetworkActivity";
    static final String PAIRING="accessories.AddAccessoryActivity";
    static final String APPS="device.apps.AppsActivity";
    static final String ALL_APPS="device.apps.AllAppsActivity";
    static final String LIMITED="Управление пока недоступно";
    enum Page {
        NETWORK("Wi-Fi и сеть"), BLUETOOTH("Bluetooth"), SOURCES("Источники"),
        PROJECTION("Проекция"), PICTURE("Изображение"), SOUND("Звук"),
        APPS("Приложения"), SYSTEM("Система");
        final String title;
        Page(String title){this.title=title;}
    }

    enum Kind { GROUP, STOCK, BACKEND, READ, BLOCKED }
    static final class Row {
        final Kind kind;
        final String title;
        final String key;
        Row(Kind kind,String title,String key){this.kind=kind;this.title=title;this.key=key;}
    }

    private static Row group(String title){return new Row(Kind.GROUP,title,"");}
    private static Row stock(String title,String activity){return new Row(Kind.STOCK,title,activity);}
    private static Row backend(String title,String key){return new Row(Kind.BACKEND,title,key);}
    private static Row read(String title,String key){return new Row(Kind.READ,title,key);}
    private static Row off(String title){return new Row(Kind.BLOCKED,title,LIMITED);}

    static List<Row> rows(Page page) {
        switch(page) {
            case NETWORK:
                return Arrays.asList(stock("Wi-Fi · включение и выключение",NETWORK),
                    stock("Доступные сети",NETWORK),stock("Сохранённые сети",NETWORK),
                    stock("Добавить сеть","connectivity.AddWifiNetworkActivity"),
                    stock("Сканирование Wi-Fi",NETWORK),stock("Ethernet",NETWORK),
                    stock("DHCP и статический IP",NETWORK),stock("DNS",NETWORK),
                    stock("Прокси",NETWORK),stock("Сетевая информация",NETWORK));
            case BLUETOOTH:
                return Arrays.asList(read("Состояние Bluetooth","bluetooth"),
                    read("Сопряжённые устройства","paired"),stock("Поиск новых устройств",PAIRING),
                    stock("Добавить устройство",PAIRING),off("Отключить устройство"),off("Удалить сопряжение"),
                    backend("Состояние пульта","bluetooth_remote"),read("Диагностика автоподключения","autopair"));
            case SOURCES:
                return Arrays.asList(group("Выбрать источник"),backend("Android TV","home"),
                    backend("HDMI 1","hdmi_info_HW6"),backend("HDMI 2","hdmi_info_HW7"),
                    read("Состояние HDMI 1","HW6"),read("Состояние HDMI 2","HW7"),group("HDMI-CEC"),
                    backend("Управление CEC","cec_status_8054"),off("Включение устройств"),
                    backend("Выключение устройств","cec_auto_off_8054"),off("Дополнительные команды"),
                    backend("EDID / совместимость HDMI","hdmi_edid_status"),off("Игровой режим"),
                    backend("Автоматическое переключение HDMI","hdmi_autoplay"),
                    backend("Источник при включении","poweron_source"),
                    backend("Приложение при включении","poweron_app_native"));
            case PROJECTION:
                return Arrays.asList(group("Фокусировка"),off("Автофокус сейчас"),off("Фокус ближе"),
                    off("Фокус дальше"),backend("Автофокус при включении","focus_power_on"),
                    backend("Автофокус при перемещении","focus_move"),group("Геометрия"),
                    off("Автоматическая трапеция"),backend("Автокоррекция при перемещении","keystone_move"),
                    backend("Четыре угла","projection_four_corner"),
                    backend("Горизонтальная коррекция","projection_hv_keystone"),
                    backend("Вертикальная коррекция","projection_hv_keystone"),
                    backend("Цифровой зум","projection_zoom"),off("Смещение изображения"),
                    group("Положение проектора"),off("Передняя установка"),off("Задняя установка"),
                    off("Потолочная передняя установка"),off("Потолочная задняя установка"),
                    backend("Яркость источника света","projection_light"));
            case PICTURE:
                return Arrays.asList(read("Режим изображения","picture_mode"),read("Яркость изображения","picture_brightness"),
                    read("Контрастность","picture_contrast"),read("Насыщенность","picture_saturation"),
                    read("Резкость","picture_sharpness"),read("Цветовая температура","picture_color_temperature"),
                    off("Гамма"),read("HDR","picture_list_hdr"),read("MEMC","tv_picture_advance_video_mjc_effect"),
                    backend("Формат изображения HDMI","picture_format"),off("Подавление шума"),
                    off("Динамический контраст"),off("Восстановить настройки изображения"),
                    read("Оттенок","picture_hue"),read("Красный канал","picture_red_gain"),
                    read("Зелёный канал","picture_green_gain"),read("Синий канал","picture_blue_gain"));
            case SOUND:
                return Arrays.asList(backend("Режим звука","sound_mode"),group("Эквалайзер"),
                    off("Пресеты"),off("Полосы частот"),off("Сброс эквалайзера"),
                    backend("Низкие частоты","sound_bass"),backend("Высокие частоты","sound_treble"),
                    backend("Объёмное звучание","surround"),backend("Автоматическая громкость","auto_volume"),
                    backend("Аудиовыход","audio_output"),off("Цифровой формат"),off("HDMI ARC / eARC"),
                    backend("Задержка динамиков","sound_speaker_delay"),backend("Задержка SPDIF","sound_spdif_delay"),
                    backend("Системные звуки","key_tone"),off("Восстановить настройки звука"));
            case APPS:
                return Arrays.asList(stock("Установленные приложения",ALL_APPS),stock("Системные приложения",ALL_APPS),
                    stock("Разрешения",APPS),stock("Специальный доступ",APPS),stock("Хранилище приложений",ALL_APPS),
                    stock("Очистка кэша",ALL_APPS),stock("Управление приложениями",APPS));
            case SYSTEM:
                return Arrays.asList(group("Об устройстве"),backend("Модель","device_model"),
                    backend("Версия прошивки","system_version"),backend("Версия Android","android_version"),
                    backend("SoC","soc_info"),read("Информация о панели","panel"),off("Температура"),
                    backend("Состояние оборудования","thermal_monitor_status"),
                    stock("Дата и время","system.DateTimeActivity"),stock("Язык и ввод","system.LanguageActivity"),
                    stock("Учётные записи","accounts.AccountsActivity"),
                    stock("Хранилище","device.StorageResetActivity"),backend("Экран и интерфейс","font_scale"),
                    group("Питание"),backend("Включение при подаче питания","electricity_boot"),
                    backend("Таймер выключения","switch_off_timer"),backend("Сон и заставка","screen_timeout_status"),
                    stock("Конфиденциальность","enterprise.EnterprisePrivacySettingsActivity"),
                    stock("Специальные возможности","oemlink.AccessibilitySettingsActivity"),
                    new Row(Kind.BLOCKED,"Обновление прошивки","Ожидается отдельный проверенный экран RuiYunOta"),
                    stock("Для разработчиков","system.development.DevelopmentActivity"),
                    off("Сброс устройства"));
            default: throw new AssertionError(page);
        }
    }

    private ModMenuCatalog() {}
}

package dev.projectorsoft.tvsettings;

import android.app.Activity;
import java.util.List;

/** Root order and the matching Activity and preview fragment. */
enum NativePage {
    NETWORK("Wi-Fi и сеть",NetworkSettingsActivity.class,
            "com.android.tv.settings.connectivity.NetworkFragment"),
    BLUETOOTH("Bluetooth",BluetoothSettingsActivity.class,NativeBluetoothPage.class.getName()),
    SOURCES("Источники",SourcesSettingsActivity.class,NativeSections.Sources.class.getName()),
    PROJECTION("Проекция",ProjectionSettingsActivity.class,NativeSections.Projection.class.getName()),
    PICTURE("Изображение",PictureSettingsActivity.class,NativeSections.Picture.class.getName()),
    SOUND("Звук",SoundSettingsActivity.class,NativeSections.Sound.class.getName()),
    APPS("Приложения",AppsSettingsActivity.class,
            "com.android.tv.settings.device.apps.AppsFragment"),
    SYSTEM("Настройки устройства",SystemSettingsActivity.class,NativeSections.System.class.getName()),
    HDMI_CEC("HDMI-CEC",HdmiCecSettingsActivity.class,NativeSections.Cec.class.getName()),
    EQUALIZER("Эквалайзер и звуковые профили",EqualizerSettingsActivity.class,NativeSections.Equalizer.class.getName()),
    DELAY("Задержка звука",SoundDelaySettingsActivity.class,NativeSections.Delay.class.getName()),
    ABOUT("Об устройстве",DeviceAboutSettingsActivity.class,NativeSections.About.class.getName()),
    HARDWARE("Состояние оборудования",HardwareStatusSettingsActivity.class,NativeSections.Hardware.class.getName()),
    POWER("Питание",ProjectorPowerSettingsActivity.class,NativeSections.Power.class.getName()),
    REMOTE("Пульт управления",RemoteSettingsActivity.class,NativeSections.Remote.class.getName()),
    HDMI1_INFO("HDMI 1",HdmiOneStatusActivity.class,NativeSections.HdmiOne.class.getName()),
    HDMI2_INFO("HDMI 2",HdmiTwoStatusActivity.class,NativeSections.HdmiTwo.class.getName()),
    ACCESSORIES("Управление устройствами",BluetoothDevicesSettingsActivity.class,"com.android.tv.settings.accessories.AccessoriesFragment"),
    COLOR_TEMPERATURE("Цветовая температура",ColorTemperatureSettingsActivity.class,NativeSections.ColorTemperature.class.getName());

    static final List<NativePage> ROOT = List.of(NETWORK, BLUETOOTH, SOURCES, PROJECTION,
            PICTURE, SOUND, APPS, SYSTEM);

    final String title;
    final Class<? extends Activity> activity;
    final String fragment;

    NativePage(String title,Class<? extends Activity> activity,String fragment) {
        this.title=title;
        this.activity=activity;
        this.fragment=fragment;
    }
}

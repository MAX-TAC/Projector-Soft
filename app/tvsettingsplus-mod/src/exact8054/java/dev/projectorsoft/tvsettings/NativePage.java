package dev.projectorsoft.tvsettings;

import android.app.Activity;

/** Root order and the matching Activity and preview fragment. */
enum NativePage {
    NETWORK("Wi-Fi и сеть",NetworkSettingsActivity.class,
            "com.android.tv.settings.connectivity.NetworkFragment"),
    BLUETOOTH("Bluetooth",BluetoothSettingsActivity.class,NativeSections.Bluetooth.class.getName()),
    SOURCES("Источники",SourcesSettingsActivity.class,NativeSections.Sources.class.getName()),
    PROJECTION("Проекция",ProjectionSettingsActivity.class,NativeSections.Projection.class.getName()),
    PICTURE("Изображение",PictureSettingsActivity.class,NativeSections.Picture.class.getName()),
    SOUND("Звук",SoundSettingsActivity.class,NativeSections.Sound.class.getName()),
    APPS("Приложения",AppsSettingsActivity.class,
            "com.android.tv.settings.device.apps.AppsFragment"),
    SYSTEM("Система",SystemSettingsActivity.class,NativeSections.System.class.getName());

    final String title;
    final Class<? extends Activity> activity;
    final String fragment;

    NativePage(String title,Class<? extends Activity> activity,String fragment) {
        this.title=title;
        this.activity=activity;
        this.fragment=fragment;
    }
}

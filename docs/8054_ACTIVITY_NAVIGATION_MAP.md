# Карта Activity для 8054 v41

Компоненты выполняются в пакете `com.android.tv.settings`, если для OEM-редактора не указан другой владелец. Сокращение MOD в таблице — префикс class name `dev.projectorsoft.tvsettings.`. Для новых Activity в Manifest есть явный запуск, экспорт, штатная тема MainSettings и отсутствуют новые неявные фильтры. CheckedPreferenceLaunch проверяет разрешение, доступность и владельца. Back возвращает на вызывавший экран через штатный стек Android. Preview использует Fragment и исходный SettingsFragmentProvider, когда конвейер устройства поддерживает два столбца.

| Экран | Activity | Fragment или источник |
|---|---|---|
| Главный экран | com.android.tv.settings.MainSettings | MainFragment, flavor-resolved main_prefs*.xml |
| Wi-Fi и сеть | com.android.tv.settings.connectivity.NetworkActivity | connectivity.NetworkFragment, stock XML |
| Bluetooth | MOD BluetoothSettingsActivity | NativeBluetoothPage → штатный AccessoriesFragment. Категория сопряжённых устройств и одно штатное добавление. |
| Добавить устройство | com.android.tv.settings.accessories.AddAccessoryActivity | Штатное сопряжение |
| Источники | MOD SourcesSettingsActivity | NativeSections.Sources |
| HDMI-CEC | MOD HdmiCecSettingsActivity | NativeSections.Cec |
| Проекция | MOD ProjectionSettingsActivity | NativeSections.Projection |
| 4 угла, H/V, зум | com.rjpj.tcorrection FourCornerActivity / HVKeystoneActivity / DigitalZoomActivity | Исходный OEM-редактор, проверенный explicit intent |
| Изображение — корневой пункт v41 | **com.android.tv.settings.partnercustomizer.picture.PictureActivity** | Штатный OEM PictureFragment внутри Android TV Settings, подтверждённый ADB на живом 8054; собственные работающие пресеты/нижние редакторы Brightness/Contrast/Saturation/Hue/Sharpness, расширенная цветовая температура/RGB и MJC/MEMC. **Не com.my.settings**, не китайский GUI. |
| Цветовая температура и RGB | MOD ColorTemperatureSettingsActivity | NativeSections.ColorTemperature |
| Плавность движения (MEMC) | MOD MemcSettingsActivity | NativeSections.Memc → NativePicturePanel, пользовательский профиль для просмотра |
| Формат HDMI | com.android.tv.settings.partnercustomizer.picture.DisplayModeActivity | DisplayModeFragment и механизмы OEM текущего HDMI |
| Звук | MOD SoundSettingsActivity | NativeSections.Sound |
| EQ | MOD EqualizerSettingsActivity | NativeSections.Equalizer → NativeEqualizerPanel |
| Аудиовыход | com.android.tv.settings.device.sound.AudioOutputActivity | AudioOutputFragment и его DigitalOutputFragment |
| Задержки | MOD SoundDelaySettingsActivity | NativeSections.Delay |
| Приложения | com.android.tv.settings.device.apps.AppsActivity | device.apps.AppsFragment |
| Настройки устройства | MOD SystemSettingsActivity | NativeSections.System |
| Об устройстве, состояние, питание | MOD DeviceAboutSettingsActivity / HardwareStatusSettingsActivity / ProjectorPowerSettingsActivity | NativeSections.About / Hardware / Power |
| Таймер расписания | com.android.tv.settings.partnercustomizer.timer.TimerSetActivity | TimerFragment |
| Прочие штатные экраны | system.DateTimeActivity, system.LanguageActivity, accounts.AccountsActivity, device.StorageResetActivity, enterprise.EnterprisePrivacySettingsActivity, system.SecurityActivity, oemlink.AccessibilitySettingsActivity, system.development.DevelopmentActivity | Штатные Activity. Ссылка только при наличии и доступности компонента. |

Совместимые прямые MOD NetworkSettingsActivity и AppsSettingsActivity сохранены. Главный экран обращается к штатным Activity. Отдельный MOD-лаунчер исключён из Manifest. Редакторы мотора, сброса, режимов сна и домашнего экрана требуют отдельного контракта входа. Аппаратный статус всех маршрутов v41 — `RUNTIME_TEST_REQUIRED`. Полный Preview конкретной OEM-темы и фокус после переходов проверяются на устройстве.

BluetoothDevicesSettingsActivity, RemoteSettingsActivity и HdmiOneStatusActivity/HdmiTwoStatusActivity остаются совместимыми прямыми компонентами. Обычная навигация к ним не ведёт. Состояние входа находится в каждой строке HDMI. [Исправления v40](8054_POST_V39_REGRESSION.md)

## 21.11.2026 — аппаратно подтверждённый штатный редактор изображения

На проекторе владельца открыта оригинальная `com.android.tv.settings/.partnercustomizer.picture.PictureActivity`:
```text
mCurrentFocus=Window{8568e96 u0 com.android.tv.settings/com.android.tv.settings.partnercustomizer.picture.PictureActivity}
mFocusedApp=ActivityRecord{e953f35 u0 com.android.tv.settings/.partnercustomizer.picture.PictureActivity} t255
```
Пользователь подтвердил, что её нижние ползунки меняют реальное изображение и значения синхронизируются с отдельным китайским `com.my.settings`. Статическая исходная цепочка: `PictureFragment → PreferenceConfigUtils.updatePreferenceChanged → Settings.Global.putInt → TVMenuSettingsService.SettingObserver → TVSettingConfig`; китайский UI в ней отсутствует.

**Изменение маршрута:** `StockMainSettingsMenu.target(Picture)` запускает **эту же проверенную** PictureActivity. Главное дерево v41 и иконка Picture сохранены. Старые отдельные `PictureSettingsActivity`, `ColorTemperatureSettingsActivity`, `MemcSettingsActivity` оставлены для обратной совместимости/проверок; штатный PictureFragment уже имеет вложенные Advanced/Color/MJC. Корневая preview-область v41 пока использует `NativeSections.Picture` и может отображать старые read-only значения: preview должен быть отдельно сопоставлен с открываемым Activity на реальном экране. **Не делать из route CI вывод об аппаратной работе нового APK.**

Это не означает, что `com.my.settings` можно отключить: требуется независимо заменить boot/TIME_TICK recovery для `KuSourceRJ.TimeService`. Текущая экспериментальная ветка PR #5, содержащая прямой `Settings.Global.putInt` в собственном нижнем редакторе, **не включена** в этот более безопасный Picture route.

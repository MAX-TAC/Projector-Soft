# KuSettings: полный учёт 16 компонентов AndroidManifest (8052)

Дата: 2026-10-23. Источник: RUIOS.8052.1.0.4, оригинальный KuSettings.apk. Сопоставлено с TvSettingsPlus 8052 v2 и KuSetupWizard. Обнаружение класса/ресурса в APK не равно проверенной работоспособности на устройстве.

| № | Компонент KuSettings | Intent или служебное назначение | Чем заменён / что делаем | Реальная готовность |
|---:|---|---|---|---|
| 1 | `com.my.settings.SettingActivity` | Главный OEM экран; `android.settings.SETTINGS` | `TvSettingsPlus.MainSettings` + manifest alias | Intent есть; проверка пульта и UI ещё нужна |
| 2 | `com.my.settings.wifi.MyWifiActivity` | `WIFI_SETTINGS` | `NetworkActivity` и Wi-Fi AOSP | Intent есть, штатные функции имеются |
| 3 | `com.my.settings.bluetooth.MyBluetoothActivity` | `BLUETOOTH_SETTINGS` | `AddAccessoryActivity` как временный переход | Не вся функциональность списка/автоподключения перенесена |
| 4 | `com.my.settings.bluetooth.MyAutoBluetoothActivity` | `AUTO_BLUETOOTH` | `AddAccessoryActivity` через legacy alias | Поведение автоподключения не верифицировано |
| 5 | `com.my.settings.language.LanguageActivity` | `LAUGUAGE_SETTINGS` (орфография OEM) | `system.LanguageActivity` | Intent есть |
| 6 | `com.my.settings.language.LanguageLocalActivity` | `LAUGUAGE_LOCAL_SETTINGS` | `system.LanguageActivity` как временная совместимость | Отдельная региональная функциональность не доказана |
| 7 | `com.my.settings.time.DateTimeActivity` | `DATETIME_SETTINGS` | `system.DateTimeActivity` | Intent есть |
| 8 | `com.my.settings.time.MyZonePicker` | Выбор часового пояса, без отдельного Intent | `system.TimeZoneFragment` штатного TvSettingsPlus | Исходный пункт есть, точный OEM сценарий не испытан |
| 9 | `com.my.settings.activity.ScreenshotActivity` | `SCREENSHOT_SETTINGS` | НЕ переносить | По решению проекта исключён |
| 10 | `com.my.service.ScreenServices` | Периодическая отправка снимков в SystemUI | НЕ переносить | По решению проекта исключён |
| 11 | `com.my.settings.activity.ProjectionActivity` | `PROJECTABOUT_SETTINGS` | В v2 alias на `uicustom.MainActivity`; в целевой версии отдельная проекция | Только общий маршрут, точный экран не реализован |
| 12 | `com.my.settings.activity.AllAppActivity` | `ALLAPPS` | `device.apps.AllAppsActivity` | Intent есть, AOSP список есть |
| 13 | `com.my.service.BootupReceiver` | `BOOT_COMPLETED` запускает MySetTimeService | Не переносить как есть; собственный BootupReceiver у KuSourceRJ | Нужен тест cold boot |
| 14 | `com.my.service.MySetTimeService` | По минутному `TIME_TICK` проверяет и запускает `KuSourceRJ.TimeService` | Не переносить как есть; нужны тесты восстановления KuSourceRJ | Риск потери watchdog при отключении KuSettings |
| 15 | `androidx.startup.InitializationProvider` | Служебный AndroidX Startup provider | Не копировать отдельно: собственный provider присутствует в TvSettingsPlus | Типовой инфраструктурный компонент |
| 16 | `androidx.profileinstaller.ProfileInstallReceiver` | Установка/профилирование оптимизации AndroidX | Не требуется переносить OEM; использовать собственную инфраструктуру TvSettingsPlus | Не функция настроек |

## Отдельные APK и системные зависимости

- `KuSetupWizard` (`com.android.provision`) — отдельный пакет. Имеет собственные определения 7 946 общих классов. 14/15 DEX идентичны; разница в `classes12.dex` локализована до `WarningActivity`. В манифесте SetupWizard **не объявлены** старые KuSettings BootupReceiver/MySetTimeService/ScreenServices/SettingActivity. SetupWizard сохраняется.
- `KuSourceRJ` (`com.coolux.source`) — собственные BootupReceiver/TimeService, источник HDMI/Android, охлаждение и датчики. **Не отключать.**
- `services.jar` и `WFDSinkTest.apk` — Bluetooth legacy action; v2 регистрирует обработчик, но hardcoded проверка имени старого Activity в системных службах остаётся.
- `xtzm_setting.txt` — `settingApps: ["com.my.settings"]`; назначение потребителя не доказано. Это потенциальный критический blocker APK-only отказа.
- `whaletv_config.json` — белый список содержит `com.my.settings`, его имя упоминается в zxlauncher и zxstore. Назначение правил не подтверждено.
- AOSP TV/MTK OEM функции: реальные контроллеры изображения/источников/фокуса/трапеции уже есть в исходном TvSettingsPlus, но единая навигация, HDR/MEMC/эквалайзер/передача управления KuSourceRJ пока **не добавлены** в DEX.

## Вердикт

**Все 16 компонентов учтены. Нельзя утверждать, что все 16 уже перенесены.** Некоторые сознательно исключены, некоторые имеют лишь совместимый Intent-маршрут, а у загрузочного watchdog остаётся потенциальная аппаратная функция. Пакет KuSettings нельзя отключать до полевых испытаний, проверки settingApps и UI/TimeService.

Полное дерево меню: [TVSETTINGSPLUS_FULL_MENU_AND_PARITY_2026-10-23.md](TVSETTINGSPLUS_FULL_MENU_AND_PARITY_2026-10-23.md).

# TvSettingsPlus — этап миграции KuSettings (8052)

## Подтверждённая сборка
- CI: https://github.com/MAX-TAC/Projector-Soft/actions/runs/39935169058
- APK: `TvSettingsPlus-8052-v2-LEGACY-INTENTS-EXPERIMENTAL.apk` внутри GitHub Actions Artifacts.
- База: RUIOS.8052.1.0.4, `com.android.tv.settings`, versionCode 1 -> 2.
- Подпись: оригинальный APK и модифицированный APK используют один публичный AOSP platform test certificate (совпадение SHA256).
- Декодирование/пересборка ресурсов Apktool приводит к ошибкам имён и конфликтующим стилям; поэтому бинарный AndroidManifest изменён точечным AXML-редактором. Все DEX, resources.arsc, JNI, assets и остальные OEM данные побайтово совпадают с оригиналом.
- Новые intent compatibility aliases: `android.settings.SETTINGS`, `android.settings.WIFI_SETTINGS`, `android.settings.BLUETOOTH_SETTINGS`, `android.settings.AUTO_BLUETOOTH`, `android.settings.ALLAPPS`, `android.settings.DATETIME_SETTINGS`, `android.settings.LAUGUAGE_SETTINGS`.
- Старые OEM действия `android.settings.SCREENSHOT_SETTINGS` **не переносим**.
- Проверены `aapt dump badging`, `aapt dump xmltree`, `apksigner verify`, `zipalign`, ZIP binary preservation. На устройстве **не тестировалось**.

## Подтверждённая логика загрузки KuSettings
- `BootupReceiver` запускает `MySetTimeService` один раз по BOOT_COMPLETED при отсутствии процесса.
- `MySetTimeService` не синхронизирует часы. Каждую минуту через TIME_TICK проверяет `com.coolux.source.service.TimeService` и перезапускает при отсутствии.
- `KuSourceRJ` имеет отдельные `BootupReceiver` и `TimeService`; дублированные службы KuSettings **не переносим** до аппаратных тестов. Если контроль на случай падения TimeService нужен, реализовать отдельный fail-safe, не переносить старый сервис целиком.
- `ScreenServices` работает со скриншотами; **не переносить**.

## Требует проверки перед полным отключением
1. Разрешение старых implicit Intent после обновления APK в присутствии и при отключении KuSettings; AOSP activity-alias может открывать другой экран, чем OEM.
2. Обработка системной кнопки Bluetooth/поиска и launcher/menu-config маршрутов (включая `services.jar`, `xtzm_setting.txt`, `whaletv_config.json`).
3. Проверка `KuSourceRJ TimeService` после cold boot и аварийного прекращения работы. Для «полного отключения KuSettings» требуется доказать, что аппаратное охлаждение, яркость и источники не зависят от его ежеминутного watchdog.
4. Вся проекция: ToF, ручной/автофокус, 4 угла, калибровка, HDMI/USB различия 8052/8054.
5. Сохранять KuSetupWizard, TRD, RuiYunOta; они не являются частью удаляемого KuSettings.
6. Откат версии TvSettingsPlus и повторное включение KuSettings после аппаратного теста.

**Текущий пакет — функциональный эксперимент по маршрутизации Intent, не завершённый перенос всей прошивки. Запрещено считать KuSettings готовым к отключению.**

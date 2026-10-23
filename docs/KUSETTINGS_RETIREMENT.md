# KuSettings retirement plan — Android 13 / RUIOS 8052

**Цель:** после переноса функций отключить весь пакет `com.my.settings` без потери HDMI, фокуса, геометрии, звука и фоновых служб. Это не просто скрытие OEM GUI.

**Известные факты (статический аудит):**
- Пакет `com.my.settings`, versionCode 1, совместимая AOSP platform-подпись, shared UID 1000.
- Оригинальный `TvSettingsPlus` уже содержит `com.android.tv.settings.MainSettings`, `com.android.tv.settings.uicustom.MainActivity`, `...KeystoneCalibrationActivity`, `...DisplayAndSoundActivity`, `...InputsActivity`.
- `KuSettings` содержит `com.my.service.BootupReceiver`, `ScreenServices`, `MySetTimeService`; отключать весь пакет до аудита их событий и вызовов нельзя.
- `KuSourceRJ` имеет собственные `BootupReceiver`, `TimeService`, `ScreensaverReceiver`, `BrightReceiver`. Не путать независимую службу `KuSourceRJ` с выключаемым `KuSettings`.

## Матрица переноса интерфейса

| KuSettings компонент | Новая точка | Требуется до отключения |
|---|---|---|
| `SettingActivity` | `TvSettingsPlus.MainSettings` | Проверить общий `android.settings.SETTINGS`, вызовы с пульта/лаунчера |
| `MyWifiActivity` | Сеть и Интернет (штатный TvSettingsPlus) | Wi-Fi включение, сохранённые сети, Ethernet |
| `MyBluetoothActivity` | Пульты и аксессуары | Сопряжение и отключение, параметры пульта |
| `MyAutoBluetoothActivity` | Пульты и аксессуары + отдельный backend (при необходимости) | Исследовать автоподключение после перезагрузки |
| `LanguageActivity`, `LanguageLocalActivity` | Настройки устройства → Язык | Переключение системных локалей |
| `DateTimeActivity`, `MyZonePicker` | Об устройстве / Дата и время | Синхронизация и часовой пояс |
| `ProjectionActivity` | Проекция (через существующий uicustom) | HDMI/Android display, ориентация, трапеция и фокус |
| `AllAppActivity` | Приложения (AOSP) | Системные и пользовательские APK |
| `ScreenshotActivity` | Не переносить по согласованному требованию | Убедиться, что другие службы не требуют ScreenshotActivity |

## Установленные факты по коду OTA 8052 (23.10.2026)

1. `KuSettings/com.my.service.BootupReceiver` реагирует только на `BOOT_COMPLETED` и запускает `MySetTimeService` через `IntentUtils.startTime()`, если служба не обнаружена.
2. `MySetTimeService` по `TIME_TICK` (раз в минуту) вызывает `IntentUtils.startSourceService()`. Тот проверяет наличие `com.coolux.source.service.TimeService` и при необходимости запускает её через явный ComponentName. Системное время сервис **не синхронизирует**.
3. `KuSourceRJ` уже имеет собственный `BootupReceiver` и `TimeService`. Поэтому базовые BootupReceiver и MySetTimeService из KuSettings **не переносим как есть**. Резервный контроль доступности KuSourceRJ TimeService понадобится только если тесты с отключённым KuSettings выявят сбои после аварийного завершения OEM-службы.
4. `ScreenServices` выполняет периодический захват экрана через `com.android.systemui.screenshot.TakeScreenshotService`; перенос **запрещён по решению проекта**.
5. Старый `android.settings.AUTO_BLUETOOTH` обрабатывается `KuSettings/MyAutoBluetoothActivity`, а TvSettingsPlus без MOD его не объявляет. В framework `services.jar` и WFDSinkTest `PhoneWindowManager` распознаёт старое имя Activity для предотвращения повторного запуска; реальная навигация использует implicit Intent. Следовательно, добавляем совместимый Intent-фильтр в TvSettingsPlus, **а не** механически патчим имя в WFDSinkTest.
6. Полный read-only-скан обнаружил строки KuSettings в `system/etc/xtzm_setting.txt`, `system/menu_config_xml/whaletv_config.json` и `services.jar`. До полной ликвидации зависимости проверить назначение этих записей и runtime intent resolution. В рамках APK-проекта разделы system не переписывать.

Источники: [строгий аудит вызовов](../research/oem-apps/KuSettings_Behavior/startup_target_details.json), [полный скан OTA](../research/oem-apps/Full_OTA_KuReferences/README.md), [зависимости DEX](../research/migration-8052/DEX_DEPENDENCIES.md).

## Системная функциональность — критический путь

| OEM компонент | Статус | Как заменить |
|---|---|---|
| `BootupReceiver` | При BOOT_COMPLETED запускает MySetTimeService | Не копировать; KuSourceRJ имеет независимый BOOT_COMPLETED receiver |
| `MySetTimeService` | Каждую минуту проверяет KuSourceRJ TimeService и запускает при отсутствии | Не копировать; проверить устойчивость собственной службы KuSourceRJ без watchdog |
| `ScreenServices` | **Не переносить в TvSettingsPlus по решению проекта** | Проверить входящие вызовы и жизненный цикл; после отключения KuSettings не должен оставаться необходимым |
| `KuSourceRJ` | Сохраняется | Адаптер источников Android/HDMI, проверить косвенные зависимости от KuSettings |
| `RuiAutoFocus`, `KeystoneCorrection`, `trdservice`, `trdmiddle` | Сохраняются | Проверенные Intent/SDK/Binder, никакого сырого I²C до аппаратной верификации |
| `RuiYunOta`, `KuSetupWizard` | Сохраняются | OTA в «Об устройстве», SetupWizard не отключать |

## Обнаруженная зависимость Miracast

В `com.mediatek.miracast` (WFDSinkTest) класс `com.android.server.policy.PhoneWindowManager` при обработке кнопки содержит ссылку на `com.my.settings.bluetooth.MyAutoBluetoothActivity`. Прямых DEX-ссылок на `com.my.settings` в TvSettingsPlus и KuSourceRJ в статическом сканировании не было, но это не доказывает отсутствие runtime-зависимостей. Нельзя скрыть эту ссылку изменением одного TvSettingsPlus: нужен патч WFDSinkTest, совместимая подпись и проверка соответствия UX назначения кнопки. Скан и конкретный метод: `research/migration-8052/DEX_DEPENDENCIES.md`, `wfd-bluetooth-reference.json`.

`KuSetupWizard` содержит похожие внутренние классы `com.my.settings`; не отключать и не модифицировать без отдельной проверки жизненного цикла.

## Список не переносимых компонентов

- `com.my.service.ScreenServices` — **не переносить**, скриншоты не добавлять.
- `com.my.settings.activity.ScreenshotActivity` — **не переносить**, соответствующий экран не добавлять.
- Bluetooth speaker sink — не переносить.

**НЕ ГОТОВО:** 8052/8054 не подвергались аппаратным испытаниям MOD; нельзя считать `KuSettings` независимым только из-за отсутствия прямой текстовой ссылки на него в Java. Вызовы могут идти через Intent actions, shared UID, системные свойства и фоновые сервисы.

## Подготовленная инфраструктура

- `tools/audit_kusettings_migration.py` + `research/migration-8052/`: декомпиляция в GitHub Actions и публикация только статических метаданных;
- `tools/patch_axml_version.py`: изменение versionCode **без** пересборки ресурсов;
- `tools/repack_tvsettingsplus_dex.py`: сборка изменённых DEX с сохранением оригинальных бинарных ресурсов и JNI;
- `app/tvsettingsplus-mod/`: код определения доступности функций для 8052/8054, тесты на несуществующий HDMI2 и неподтверждённые HDR/MEMC/EQ;
- `.github/workflows/build-tvsettingsplus-smoke.yml`: экспериментальная сборка и проверка AOSP platform-подписи. Smoke APK не содержит готового переноса функций.

## Критерии окончательного отключения

1. Проверены все входящие вызовы `com.my.settings` (явные и неявные Intents, services, receiver, content providers).
2. Все нужные настройки работают из TvSettingsPlus, HDMI1/HDMI2 видны только на соответствующей ревизии.
3. Сохраняются фокус, трапеция, поворот, настройки картинки, аудио и OTA; MEMC/HDR/Game/EQ только при проверенных OEM API.
4. После установки и перезагрузки нет ошибок запуска аппаратных сервисов и `KuSourceRJ`.
5. Настройки по кнопке пульта и системным Intents открывают TvSettingsPlus, а не KuSettings.
6. Проверены откат `adb uninstall com.android.tv.settings` (только удаление обновления системного пакета) и повторное включение `KuSettings`. **Не выполнять эти операции вслепую без теста на устройстве.**
7. Только после успешного теста — отключение *всего пакета* `com.my.settings` для user 0. Удаление APK из /system в рамках этого проекта не требуется.


### Статический разбор двух системных конфигураций

- `system/system/etc/xtzm_setting.txt`, строка 3: `"settingApps": ["com.my.settings"]`. Это потенциальный пакет, выбираемый для открытия настроек **вне** TvSettingsPlus. Не доказано, что именно эта запись активно используется на 8052; требует трассировки потребителя `settingApps`.
- `system/system/menu_config_xml/whaletv_config.json`, строка 59: в `white_list` находится `com.my.settings` вместе с другими приложениями. Присутствие в white_list само по себе не означает обязательный запуск пакета.
- `system/framework/services.jar` содержит старый Bluetooth-маркер и `android.settings.AUTO_BLUETOOTH`. Его нельзя исправить простой пересборкой WFDSinkTest; APK-only маршрут — зарегистрировать действие в TvSettingsPlus, что реализовано экспериментально в подписанном v2.

Исходный [отчёт](../research/oem-apps/Full_OTA_KuReferences/inventory.json) и сборка [TvSettingsPlus v2](BUILD_STATUS_8052_v2.md).


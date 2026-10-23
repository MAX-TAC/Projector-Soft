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

## Системная функциональность — критический путь

| OEM компонент | Статус | Как заменить |
|---|---|---|
| `BootupReceiver` | Выясняем фактические действия и события из исходного DEX | Переносить только если обнаружена уникальная необходимая функция, не дублируемая Android/TRD/TvSettingsPlus/KuSourceRJ |
| `MySetTimeService` | Выясняем источники времени, периоды и побочные эффекты | Не переносить без доказанной необходимости; сравнить Android NetworkTimeUpdateService и TimeService KuSourceRJ |
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


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
| `BootupReceiver` | Не изучены все побочные эффекты | Только после точного аудита событий и выполняемых действий |
| `MySetTimeService` | Не изучены условия запуска | Использовать штатное Android время/синхронизацию при доказанной эквивалентности |
| `ScreenServices` | Имеет код скриншотов, возможны и другие функции | Исключить ненужные скриншоты, проверить все остальные публичные вызовы и регистрацию |
| `KuSourceRJ` | Сохраняется | Адаптер источников Android/HDMI, проверить косвенные зависимости от KuSettings |
| `RuiAutoFocus`, `KeystoneCorrection`, `trdservice`, `trdmiddle` | Сохраняются | Проверенные Intent/SDK/Binder, никакого сырого I²C до аппаратной верификации |
| `RuiYunOta`, `KuSetupWizard` | Сохраняются | OTA в «Об устройстве», SetupWizard не отключать |

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


# TvSettingsPlus MOD v16 — аппаратный HDMI-CEC и источник при включении

**Дата:** 24 октября 2026. **Исходная платформа:** RUIOS 8052, Android 13, MT9660. **Статус:** подписанный экспериментальный APK, без аппаратных испытаний.

[Сборка и проверки GitHub Actions](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40129257332) · [APK v16 — build artifact](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40129257332/artifacts/12573961443).

## Изменения относительно v14

### v15 — заводской источник при включении

В стандартную Android TV группу **«Источники · Проектор»** добавлен `ListPreference` «Источник при включении». Это **не** мгновенный переключатель активного источника.

- Использует оригинальный `persist.trd.source_id`, а не дополнительный MOD-only параметр.
- Варианты: Android TV (`HOME`), физические HDMI и USB (`USB`), AV только при заводском флаге поддержки.
- Для HDMI считывает `Build.A_RJ_HDMI_PORT_COUNT` и `Build.A_RJ_HDMI_PORTS`, извлекает реальные HW-порты и проверяет их регистрацию через `TvInputManager.getTvInputList()` (`TvInputInfo.TYPE_HDMI`).
- Не подставляет выдуманные HDMI2–HDMI4, когда параметры платы неполные или неизвестные. Нет совместимого выбора — контрол скрывается.
- После изменения системного свойства выполняется readback. Штатные действия HDMI-CEC, eARC, kеystone и активного источника остаются нетронутыми.

Чистая модель `BootSourceMapping` и `BootSourceMappingTest` проверяет 1-HDMI/2-HDMI схемы, дубликаты портов, некорректные данные и отсутствие фантомного HDMI2 у 8052. [Сборка v15](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40128787806) выполнена успешно.

### v16 — HDMI-CEC и автоматическое выключение HDMI-устройств

Два новых пункта в той же Android TV группе «Источники · Проектор»:

- **HDMI-CEC**: сохранение системного заводского ключа `Settings.Global.HDMI_CONTROL_ENABLED` (через исходный `Constant.SETTINGS_KEY_HDMI_CEC`) и изменение аппаратного CEC через `com.mediatek.support.androidversioncompatiblelib.CompatibleApis`.
- **Автоматически выключать HDMI-устройства вместе с проектором**: `CompatibleApis.setHdmiCecAutoDeviceOffEnabled`, доступно при включённом CEC.

**Ограничение, предотвращающее обход штатной защиты:** если `DataSeparaterUtil.isSupportPowerConfirmed()` сообщает необходимость OEM-диалога подтверждения питания, обе новые прямые настройки не добавляются; оригинальный `SourceSubMenuController` со штатным диалогом остаётся доступным через OEM раздел.

При отключении CEC повторены важные OEM-побочные действия: отключение Auto Device Off, а на устройствах с `PreferenceConfigUtils.isSupportEARC()` — отключение eARC через заводской `MtkTvCecAnBase.setEarcEnable(false)` и его исходный ключ `KEY_SOUND_EARC`. Переход считается подтверждённым только после чтения системного и нативного CEC-состояния.

**Это статически подготовленная интеграция на уровне APK.** Не подтверждена работа на физической плате, включая задержки применения HAL и особенности CEC-устройств. Для установки на 8054 использовать отдельный APK на базе оригинала 8054.

## Всё сохранённое в v16

- Телевизионный ярлык «Настройки» (`MAIN`, `LEANBACK_LAUNCHER`, `LAUNCHER`, штатные иконка и banner), открывает `TvSettingsPlus.MainSettings`.
- Android TV категории: изображение, звук, проекция и источники, плюс переходы к оригинальным OEM-вкладкам.
- 7 режимов изображения, цветовая температура, 10 заводских picture/RGB/Bass/Treble ползунков.
- Заводской **пятиполосный эквалайзер 120/500/1500/5000/10000 Гц**, без выдуманных полос, через штатный `Settings.Global` и gate `PartnerSettingsConfig`.
- HDR/MEMC только при наличии OEM состояния, четыре режима ориентации через `ProjectorManager`.
- EDID 1.4/2.0/Auto/2.1 с аппаратной проверкой MediaTek и только на HDMI-входе; HDMI hotplug autoplay с проверкой OEM свойства.
- Стандартная BrightReceiver логика для customer-0 с fallback; меню Bluetooth-колонки скрыто, Bluetooth-стек оставлен.
- **Без изменений:** калибровочные экраны автофокуса, ручной/автоматической трапеции, четырёх углов, штатные моторные JNI, OTA и службы источника/охлаждения.

## Статус подписанной сборки

GitHub Actions run **40129257332 — success**. Проверены Java/Android 13 compile, D8, smali-rebuild, `aapt`, AndroidManifest/launcher aliases, `zipalign`, `apksigner verify`, сравнение оригинальной подписи с заводским AOSP platform test-key и целостность неизменённых файлов. `com.android.tv.settings`, `versionCode=16`.

Как и в предыдущих версиях, из 16 исходных DEX заменены только `classes13.dex` и `classes14.dex`; дополнительный `classes17.dex` содержит наши Java-контроллеры. Исходные `resources.arsc`, остальные DEX и JNI сохранены.

## Что блокирует финальный релиз и полное отключение KuSettings

1. Прямые маршруты запуска `com.my.settings` в системе и OEM launcher, `settingApps`, `services.jar`, WFDSinkTest: APK TvSettingsPlus не может исправить их все. Девять Intent aliases не заменяют явно заданные component names. Это **не решено**.
2. `KuSettings.MySetTimeService` — дополнительный watchdog `KuSourceRJ.TimeService` раз в минуту. Собственный BootupReceiver KuSourceRJ присутствует, но без тестов отказа и перезагрузки нельзя гарантировать штатное восстановление. **KuSettings не отключать**.
3. Несколько аппаратных OEM действий (подтверждение CEC-питания, переключение HDMI low latency с `resetTrapezoid()`, eARC при иной конфигурации) остаются через оригинальный OEM экран того же TvSettingsPlus, а не все напрямую inlined.
4. Исходный `TvSettingsPlus.apk` ревизии **8054** отсутствует среди закреплённых `vendor/reference/8054/APK`, хотя в ранее исследованных firmware есть отличия HDMI/ToF/JNI. Отдельную сборку для 8054 пока нельзя честно объявить готовой.
5. Ни один модифицированный APK не запущен на физическом MT9660: тесты ART, пульта, показ ярлыка, HDMI, звука, SPDIF/eARC, ToF, автофокуса, термозащиты и возврата к стоку отсутствуют. CI подтверждает корректное построение ZIP/DEX и подпись, а не функциональную полноту или безопасность установки.

**Вывод:** v16 — реальный новый подписанный APK с портированным управлением CEC/источником старта. Это не полный финальный релиз для неподготовленного владельца.

## Исходники и проверка

- [BootSourceMapping.java](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/BootSourceMapping.java)
- [OemPowerOnSourceHook.java](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/OemPowerOnSourceHook.java)
- [OemHdmiCecHook.java](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/OemHdmiCecHook.java)
- [Native Android TV navigation](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/OemInlinePreferencesHook.java)
- [Board mapping regression tests](../app/tvsettingsplus-mod/src/test/java/dev/projectorsoft/tvsettings/BootSourceMappingTest.java)
- [OEM CEC backend audit](../research/oem-apps/Native_Controllers_8052/cec_initialization.json)
- [Power-on source handlers audit](../research/oem-apps/Native_Controllers_8052/poweron_handlers.json)
- [Read-only ADB diagnostics](../tools/collect_tvsettingsplus_adb_diagnostics.sh)
- [APK build workflow](../.github/workflows/build-tvsettingsplus-inline-v16.yml)

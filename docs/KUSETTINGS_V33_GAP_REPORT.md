# KuSettings — актуальная карта переноса (TvSettingsPlus MOD v33)

База: заводская 8052. Дата статического аудита 11.11.2026. Текущее состояние — программный этап без физического тестирования. В v33 добавлена runtime-проверка девяти implicit intent handlers через PackageManager. **Версия 8054 ещё не делается.**

## Из 16 AndroidManifest компонентов

| № | Компонент | Статус на v32 | Что осталось |
|---:|---|---|---|
| 1 | SettingActivity | Заменён вход в `MainSettings`; launcher alias | Проверка всех пультовых/системных вызовов, не все OEM страницы встроены |
| 2 | MyWifiActivity | AOSP NetworkActivity/алиас | Проверка Wi-Fi, Ethernet и возврата |
| 3 | MyBluetoothActivity | AOSP аксессуары + native HID status (v32) | Список соединений, управление ранее сопряжёнными устройствами |
| 4 | MyAutoBluetoothActivity | Частично: native HID status + AOSP pairing | **Критично:** OEM автопоиск пульта по именам и подключение `CachedBluetoothDevice.connect(true)` |
| 5 | LanguageActivity | AOSP язык + legacy Intent | Проверка системной локали |
| 6 | LanguageLocalActivity | AOSP язык, временная маршрутизация | Проверка отдельных региональных сценариев |
| 7 | DateTimeActivity | AOSP дата/время + legacy Intent | Проверка времени и авто-синхронизации |
| 8 | MyZonePicker | AOSP TimeZoneFragment | Проверка часового пояса и сохранения |
| 9 | ScreenshotActivity | **Исключён** | Не переносить |
| 10 | ScreenServices | **Исключён** | Не переносить |
| 11 | ProjectionActivity | OEM Projector таб + native orientation / calibration route | Не вся проекция нативная; проверка фокуса и трапеции |
| 12 | AllAppActivity | AOSP AllAppsActivity + legacy Intent | Проверка отображения системных приложений |
| 13 | BootupReceiver | Отдельный MOD BootReceiver (не дубль OEM) | Проверка cold boot и роли KuSetupWizard/KuSourceRJ |
| 14 | MySetTimeService | Подготовлен guarded recovery service; read-only audit v32 | **Критично:** эквивалентное восстановление KuSourceRJ.TimeService без ложных повторных запусков |
| 15 | AndroidX Startup provider | Собственная AOSP инфраструктура | Специальный перенос не нужен |
| 16 | ProfileInstallReceiver | AOSP инфраструктура | Специальный перенос не нужен |

## Ещё не закрытые зависимости вне этих 16 компонентов

1. Девять совместимых implicit Intent-ов проверяются в диагностике v33. Конкурирующие default handlers / явные `ComponentName(com.my.settings, ...)` остаются возможными. `xtzm_setting.txt` содержит `settingApps: ["com.my.settings"]`; исполнитель конфигурации неизвестен.
2. Системный `services.jar`/WFDSinkTest знает имя старой `MyAutoBluetoothActivity`; MOD зарегистрировал action, но явные проверки имени могут вести себя иначе.
3. `whaletv_config.json` включает `com.my.settings` в white_list. Это не доказывает необходимость запуска, нужно установить назначение.
4. OEM `KuSourceRJ.TimeService`, `KuSetupWizard`, `RuiAutoFocus`, `KeystoneCorrection`, `RuiYunOta` остаются заводскими сервисами (не относятся к удаляемому интерфейсу KuSettings).
5. В общих настройках часть безопасных контролов уже нативна: изображение/звук/EQ 5 полос/EDID/источник при включении/электропитание/размер текста; сложные CEC, eARC, Game и калибровка пока полагаются на OEM.
6. Сон и заставка скрыты в обеих разновидностях OEM General, Android idle policy v31. Это не замена отдельным энергополитикам KuSourceRJ или PowerManager OEM.

## Когда можно отключить KuSettings

Только после программного parity review, независимого теста загрузки и восстановления TimeService, реального Bluetooth-сопряжения/восстановления и проверки источников/фокуса/трапеции на целевом устройстве. До этого **KuSettings оставляем включённым**.

Связанные отчёты: `KUSETTINGS_16_COMPONENT_PARITY.md`, `KUSETTINGS_RETIREMENT.md`, `TVSETTINGSPLUS_V32_BLUETOOTH_KUSOURCE_DIAG.md`.

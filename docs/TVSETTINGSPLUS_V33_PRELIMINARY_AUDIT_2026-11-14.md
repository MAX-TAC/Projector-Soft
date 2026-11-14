# TvSettingsPlus MOD — предварительный аудит готовности v33 (14.11.2026)

**Объект:** рабочая ветка `feature/tvsettingsplus-thermal-telemetry`, Android 13 / база RUIOS 8052. Аудит проводился по фактически сохранённому Java-коду, сборочным скриптам, OEM metadata, документации и статусам GitHub Actions. **Это статическая ревизия, не полный аудит итоговой установленной APK.**

## Вердикт

**RELEASE BLOCKED** — запрещено объявлять `KuSettings` полностью заменённым или отключать `com.my.settings`. Последний успешный OEM APK build — v30 (11 ноября), workflow v31/v32/v33 на 14 ноября **queued**. Подписанная и проверенная v33 APK не подтверждена. Нельзя утверждать, что было выполнено полное программное тестирование v33.

## Что подтверждено источниками

| Область | Статус | Обоснование |
|---|---|---|
| 16 manifest components KuSettings | Инвентаризация проведена | `docs/KUSETTINGS_16_COMPONENT_PARITY.md`, реальная функциональная эквивалентность подтверждена не у всех |
| Wi-Fi/дата/язык/приложения | Есть AOSP экраны и intent-alias | Не проверены сценарии OEM региона и фактические вызовы кнопок |
| Нативные настройки яркости/изображения/звука/EQ/boot/font | Код внесён, v30 прошла сборку | Часть controls обёрнута штатными OEM API; работа HAL не тестировалась |
| Bluetooth remote | v32 реализован только readonly HID-list + pairing link | OEM MyAutoBluetoothActivity фильтрует имя и вызывает CachedBluetoothDevice.connect(true) — parity нет |
| Recovery KuSourceRJ.TimeService | Код guarded, работает только при отключённом KuSettings | Не доказана полнота ActivityManager.getRunningServices на Android 13; никакого аппаратного теста |
| Sleep/Screen-off | v31 скрывает два OEM интерфейса и пишет Never через System/Secure | Build ещё не завершён. AOSP/сторонние/OEM power UI отдельно не проверены. Нельзя гарантировать, что оптический движок никогда не гаснет |
| Запуск настроек системными Intent | Nine aliases и v33 PackageManager audit | Не перехватывает explicit ComponentName, `xtzm_setting.txt.settingApps` или OEM whitelist |
| Дизайн единой правой панели | Частично перенесён | `OemRootMenuHook` всё ещё открывает отдельную `uicustom.MainActivity`; аппаратные OEM экраны сохраняются |
| OEM APK integrity / signing | v30 verified, v33 pending | Нет подтверждённого `apksigner verify` / regression для v33 |
| Реальное устройство | NOT TESTED | Cold boot, BT pairing/reconnect, TimeService, HDMI, USB, фокус, трапеция, звук, OTA, откат |

## Конкретный дефект, найденный при проверке v33

`OemLegacyIntentPolicy.evaluate()` классифицировал режим **`preferredPackage=com.android.tv.settings` при видимых обоих обработчиках** как `COMPETING_HANDLERS`, не сообщая, что Android действительно выбрал TvSettingsPlus. Исправлено в коде и JVM-тестах: теперь `MOD_DEFAULT_WITH_COMPETITOR`, раздельный счётчик default route и реальной независимости от KuSettings. **После коммита требуется повторная CI-проверка.**

## Дефицит тестового покрытия до завершённого аудита

1. Подписанная APK v33: javac/D8, смали-патч всех ожидаемых классов, неизменность остальных 16 OEM DEX/resources, AndroidManifest alias/receiver, сертификат оригинала и результата, чистая установка/обновление и rollback.
2. Непосредственно на устройстве: boot/reboot, отсутствие ANR/crashes/VerifyError, правильные переходы пульта, все аппаратные секции, HDMI/CEC/eARC/EDID/источник, матрица/оптика, системные энергополитики и тепловой watchdog.
3. Bluetooth: поиск/сопряжение/повторное подключение OEM-пульта, сценарии после перезагрузки и потери связи.
4. TimeService: остановка/восстановление под защитой от ложных повторных запусков, наблюдение охлаждения и температур, не отключая заводской пакет на непроверенной системе.
5. Статический анализ оставшихся `services.jar`, WFDSinkTest, `xtzm_setting.txt` и `whaletv_config.json` на явные ссылки на `com.my.settings`.
6. Полная трассировка навигации (включая внешние Activity, ресиверы и системную кнопку) и ресурсы/локализации.

## Минимальные критерии готовности к адаптации 8054

- CI v33 или следующая ревизия полностью GREEN, реальные APK и результаты проверки подписи доступны.
- Проведены runtime smoke и rollback на базовой исследованной ревизии, с журналом.
- Нет незакрытых критических сервисных зависимостей KuSettings/KuSourceRJ; перенос OEM BT и recovery проверен.
- Зафиксированы расхождения двух портов HDMI, ToF и runtime capability matrix 8054.

**Решение:** не удалять и не отключать `KuSettings`, `KuSourceRJ`, `KuSetupWizard`; текущая ветка — экспериментальная разработка, а не финальный релиз.

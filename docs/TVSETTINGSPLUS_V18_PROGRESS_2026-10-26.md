# TvSettingsPlus MOD v18 — условная замена watchdog KuSettings для KuSourceRJ

**Дата:** 26 октября 2026. **База:** заводской `TvSettingsPlus.apk` RUIOS **8052** / Android 13 / MT9660. **Статус:** экспериментальная подписанная APK, без установки на реальное устройство.

[GitHub Actions v18 — success](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40332001444) · [APK и результаты сборки](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40332001444/artifacts/12655213059).

## 1. Причина появления нового кода

Аудит Java-кода заводских приложений обнаружил критическую зависимость:

- `KuSourceRJ` / `com.coolux.source.service.TimeService` — **компонент контроля температуры, вентиляторов и изменения мощности LED при перегреве**. На `TIME_TICK` он выполняет контроль, запускает OEM потоки обнаружения вентиляторов, превышения температуры и снижения яркости.
- `TimeService.onStartCommand` возвращает `2 = START_NOT_STICKY`. После аварийного завершения Android не обязан автоматически восстанавливать его.
- `KuSettings` / `com.my.service.MySetTimeService` раз в минуту проверяет наличие `KuSourceRJ.TimeService` и при необходимости запускает его явным `ComponentName(com.coolux.source, com.coolux.source.service.TimeService)`.
- Собственный `KuSourceRJ.BootupReceiver` запускает компоненты после включения, но **не гарантирует восстановления после аварии во время работы**.

Таким образом, простое отключение `com.my.settings` может убрать реальный механизм восстановления тепловой защиты. Это было блокером отказа от KuSettings, а не только UI-вопросом.

## 2. Новые компоненты MOD

Только в версии v18 дополнительно разработаны:

- `KuSourceRecoveryBootReceiver` — `BOOT_COMPLETED`; ничего не делает, если `com.my.settings` продолжает работать в штатном режиме. Если пользователь **до холодной загрузки намеренно отключил старый пакет**, запускает новый монитор.
- `KuSourceRecoveryService` — отдельный `START_STICKY` сервис, динамически регистрирующий `TIME_TICK`. Не занимается управлением вентиляторами/LED сам: оставляет аппаратную термозащиту в оригинальном `KuSourceRJ`. Пытается запустить строго исходный OEM `TimeService`, если он отсутствует, и только если проверка доступных процессов не выглядит ограниченной правами Android.
- `KuSourceRecoveryPolicy` — независимая проверяемая политика: **запрет конкуренции с включённым KuSettings; запрет рестарта при ненадёжной выборке служб; не трогать уже запущенный источник; ограничение повторов минимум минутой**.
- Все компоненты зарегистрированы в исходном **бинарном AndroidManifest.xml** без пересборки `resources.arsc`. Новые receiver/service не экспортированы другим приложениям.

**Ограничения реализации:** `getRunningServices` на Android 13 может скрывать чужие службы даже от системного приложения; старт чужой фоновой службы после `BOOT_COMPLETED` может ограничиваться системой; получение `TIME_TICK`, возврат после сна и `START_STICKY` требуют физической проверки. При ненадёжных данных монитор отказывается от запуска, а не создаёт потенциально опасную петлю. Поэтому это **экспериментальная защита, ещё не доказанная эквивалентом OEM сторожевого сервиса**.

## 3. Всё предыдущее оставлено в v18

- Native Android TV launcher alias, открывающий `com.android.tv.settings.MainSettings`.
- Пять ссылок к OEM страницам внутри TvSettingsPlus плюс встраиваемые Android TV группы «Изображение», «Звук», «Проекция», «Источники», «Система».
- Оригинальные режимы изображения и цвета, RGB/контраст/яркость картинки, bass/treble, OEM пять EQ полос **120 / 500 / 1500 / 5000 / 10000 Гц**.
- Параметры MEMC/HDR (при заводской поддержке); 4 положения проектора через ProjectorManager.
- EDID, автопереключение HDMI, источник после загрузки с проверкой реальных `TvInputInfo` и количества портов; аппаратный HDMI CEC, когда не требуется оригинальный диалог подтверждения питания.
- Условный мост источника яркости и сохранение штатного пути, скрытие пункта режима BT колонки.
- **Нет изменений** в полноэкранной калибровке фокуса, автофокусе, геометрии трапеции, 4 углах, драйверах моторного управления, заводских службах термозащиты/OTA.

## 4. Проверка v18

Успешно: Android 13 Java/D8, smali, 9 KuSettings-compatible intents, корректная регистрация приватных Watchdog Receiver/Service и BOOT action, aapt badging / manifest dump, Android platform test key и apksigner, целостность OEM файлов.

- `com.android.tv.settings`, `versionCode 18`.
- Из исходных 16 DEX изменены только `classes13.dex` и `classes14.dex`; добавлен `classes17.dex`, включая код мониторинга.
- `resources.arsc`, другие DEX, JNI и остальные файлы OEM проверены как неизменные.
- Дополнительная проверка подтверждает штатное разрешение `android.permission.RECEIVE_BOOT_COMPLETED` и в оригинале, и в обновлённом APK.
- JVM-тесты `KuSourceRecoveryPolicyTest` и `BootSourceMappingTest` проверяют отрицательные случаи и схемы 8052/8054.

**Это доказывает только корректную упаковку и статическую проверку**, не реальное восстановление вентилятора/термозащиты или функционирование интерфейса на MT9660.

## 5. Что ещё мешает готовому релизу

1. **KuSettings НЕ отключать** до подтверждения: после холодного старта с отключённым KuSettings (только на подготовленном тестовом устройстве с возможностью отката) запускается KuSourceRecoveryService; после аварийного завершения `KuSourceRJ.TimeService` монитор действительно восстановит его; контроль температуры/вентиляторов продолжает работать. Такие испытания нельзя заменять выводами CI.
2. Исправить системные явные ссылки `com.my.settings` в OEM launcher, `settingApps`, `services.jar`, WFDSinkTest. Девять Intent aliases покрывают только implicit intents; APK одной программой не заменит сторонние explicit components.
3. Для некоторых HDMI CEC/eARC/Game Mode OEM-подтверждений и side effects оставлены штатные компоненты TvSettingsPlus, ещё не интегрированные полностью в единый экран.
4. Нужен оригинальный `TvSettingsPlus.apk` **от 8054** и отдельная проверка подписи/разметки, прежде чем собирать версию под двух-HDMI плату и ToF.
5. Полный тест установки, remote keys, boot, autofocus/keystone, thermal, native EQ, EDID, HDMI/CEC, OTA и план отката на настоящем устройстве всё ещё впереди.

**Текущую v18 не передавать неподготовленному владельцу как финальную.** Она пригодна для дальнейшего анализа и подготовки **контролируемой** серии аппаратных испытаний.

## Исходники

- [Guards before boot](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/KuSourceRecoveryBootReceiver.java)
- [Thermal monitoring service](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/KuSourceRecoveryService.java)
- [Fail-closed policy](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/KuSourceRecoveryPolicy.java)
- [Policy JVM test](../app/tvsettingsplus-mod/src/test/java/dev/projectorsoft/tvsettings/KuSourceRecoveryPolicyTest.java)
- [APK Manifest binary updater](../tools/patch_axml_compat_aliases.py)
- [OEM TimeService static audit](../research/oem-apps/Native_Controllers_8052/lifecycle_telemetry.json)
- [AOSP System info](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/OemSystemInfoHook.java)
- [v18 build pipeline](../.github/workflows/build-tvsettingsplus-inline-v18.yml)

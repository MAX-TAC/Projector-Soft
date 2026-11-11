# TvSettingsPlus MOD v23 — встроенная диагностика и проверка общей базы 8052/8054

**Дата:** 11 ноября 2026. **Статус:** экспериментальный APK с проверками сборки; испытания на физическом проекторе ещё не проводились.

## Реальные новые функции v22–v23

- В стандартном Android TV меню `Система · Проектор` добавлен нажимаемый пункт **«Диагностика совместимости MOD»**. В отличие от обычной информации о системе, он открывает небольшой диалог со сводкой: версия самого TvSettingsPlus, версия Android, Build, RUIOS machine ID, аппаратный HDMI-count, OEM customer ID, состояние KuSettings и экспортированность `KuSourceRJ.BrightReceiver`.
- Кнопка **«Копировать»** переносит текст отчёта в буфер обмена Android. Пользователю не требуется ADB, root или ввод команд. Диалог ничего не меняет в системе.
- В отчёте **не говорится, что `KuSourceRJ.TimeService` запущен**, что вентилятор исправен или что HDR/MEMC/HDMI/eARC проверены: статические сведения и PackageManager не являются доказательством фактической работы HAL.
- Добавлен отдельный тестируемый `ProjectorCompatibilityPolicy`, который отвергает неопределённую ревизию/количество HDMI и распознаёт явное отключение KuSettings как состояние, при котором восстановление контроля охлаждения **ещё требует проверки**.
- Добавлены регрессионные JVM-тесты 8052/8054, неизвестного ID, числа HDMI и PackageManager states (0–4).
- Старые workflows v20–v22 отключены от автоматического запуска при каждой правке. Исторические сборки сохранены для ручного воспроизведения.

## Установлено по исходникам прошивок: заводской TvSettingsPlus общий

В прежнем независимом сравнении двух заводских APK:

| Образ | TvSettingsPlus APK SHA-256 |
|---|---|
| **RUIOS.8052.1.0.4** (актуальная тогда OTA) | `e4184a13fbf56fa069effc6a40ea50c0da7e9190321b389f59ba34700a80d0a1` |
| **RUIOS.8054.1.0.1** (заводской образ) | `e4184a13fbf56fa069effc6a40ea50c0da7e9190321b389f59ba34700a80d0a1` |

Их **16 исходных DEX также совпали**. Это подтверждает общий заводской APK и делает возможным общую кодовую ветку настроек.

Источники исходного исследования: [comparison.json](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/blob/tvsettingsplus-migration-v0/analysis/Latest_8052_OTA_vs_8054_Stock/comparison.json) и [Settings dispatcher and APK signatures](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/blob/global-firmware-map-8052-8054-20261021/analysis/Global_Firmware_Map_20261021/SETTINGS_DISPATCH_8052_RU.md).

**Чего эта информация НЕ доказывает:**

- что установленный сейчас на проекторе FlixTone D11 Master APK соответствует именно историческому `8054.1.0.1`;
- что модификации `classes13.dex`, `classes14.dex` и новый `classes17.dex` работают при отличающихся vendor/HAL/ToF драйверах 8054;
- что подключение двух HDMI, звук, MEMC, ToF и моторчики работают после установки MOD;
- что можно отключить старый `KuSettings` без потери `KuSourceRJ.TimeService` watchdog.

Поэтому **не маркировать 8052-сборку как проверенную и безопасную для 8054** на основании одного сравнения APK.

## Что уже сохранено от предыдущих версий

- Настоящий телевизионный ярлык «Настройки» → `com.android.tv.settings.MainSettings` с `MAIN` и `LEANBACK_LAUNCHER`.
- Пять переходов к заводским OEM разделам внутри того же пакета; native Android TV категории «Изображение», «Звук», «Проекция», «Источники», «Система».
- Семь режимов изображения, цветовые температуры, ползунки изображения/ RGB, HDR/MEMC с OEM-gating, пять частот заводского EQ **120/500/1500/5000/10000 Гц**, басы/высокие частоты.
- Положение проектора, яркость источника света с vendor branch, безопасное сохранение fullscreen фокуса и трапеции.
- HDMI EDID, HDMI hotplug autoplay, источник при включении, HDMI-CEC (только если не требуется OEM power confirmation), ссылки к заводским HDMI Game/eARC/calibration обработчикам вместо потери их побочных эффектов.
- Отдельный **условный** контроллер восстановления `KuSourceRJ.TimeService`; активируется только при явно отключённом KuSettings, без ложных положительных срабатываний из-за package visibility. **Аппаратно не проверен**.

## Что остаётся до финального выпуска для владельца

1. Проверить установленную прошивку и SHA оригинального APK на реальном 8054, перед любой установкой MOD.
2. Выполнить контролируемую проверку запуска и навигации: ярлык, пульт, все OEM вкладки, заводской DSP EQ, HDR/MEMC, HDMI/CEC/eARC, оптика и калибровка, перезагрузка и откат.
3. Подтвердить стабильный контроль `KuSourceRJ.TimeService` после сбоя/перезагрузки. **До этого KuSettings не отключать.**
4. Проверить точку входа Settings из штатного OEM лаунчера и оставшиеся `settingApps` / `services.jar` ссылки.
5. Не распространять текущие подписанные сборки неопытным пользователям как завершённую прошивку: успешная GitHub Actions CI — только **статическая** проверка подписанного APK.

## Исходные файлы

- [OemCompatibilityDiagnosticsHook](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/OemCompatibilityDiagnosticsHook.java)
- [ProjectorCompatibilityPolicy](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/ProjectorCompatibilityPolicy.java)
- [ProjectorCompatibilityPolicyTest](../app/tvsettingsplus-mod/src/test/java/dev/projectorsoft/tvsettings/ProjectorCompatibilityPolicyTest.java)
- [v23 workflow](../.github/workflows/build-tvsettingsplus-inline-v23.yml)

> **TvSettingsPlus MOD v31 — безопасность экрана:** скрыты заводские пункты сна/заставки. При загрузке и входе в настройки выставляется Never для Android screen/sleep timeouts и отключается Android Dream; статус виден только для чтения. [Отчёт v31](docs/TVSETTINGSPLUS_V31_SCREEN_SAFETY.md). KuSettings и управление охлаждением не изменены. Без адаптации под 8054.

> **TvSettingsPlus MOD v30 — программный этап:** native «Размер текста» 85/100/115/130% по заводским настройкам; сохранены native power/key tone/boot app. [Отчёт v30](docs/TVSETTINGSPLUS_V30_FONT_SCALE.md). 8054 только после завершения программных проверок.

> **TvSettingsPlus MOD v29 — программный этап переноса:** выбор приложения при включении теперь в нативном Android TV списке, а не только в OEM Activity; выбор разрешён лишь для реально запускаемых пакетов и проверяется обратным чтением. [Отчёт v29](docs/TVSETTINGSPLUS_V29_POWERON_APP.md). 8054 пока не адаптируется.

> **TvSettingsPlus MOD v28 (8052 software-stage):** native keypress sound toggle with exact OEM Settings.System key and verified readback; v27 electricity-boot toggle retained. [v28 report](docs/TVSETTINGSPLUS_V28_KEYTONE.md). No 8054 adaptation until software regression is complete.

> **TvSettingsPlus MOD v27 — продолжение переноса, без адаптации 8054:** нативный переключатель автозапуска после подачи питания с заводским MtkUtil и проверкой записи. [Отчёт v27](docs/TVSETTINGSPLUS_V27_NATIVE_POWER.md). KuSettings не отключать.

> **TvSettingsPlus v25 (экспериментально):** перенесены доступные из Android TV переходы к заводскому обновлению прошивки и выбору приложения при включении; [отчёт](docs/TVSETTINGSPLUS_V25_KUSETTINGS_ROUTES.md). KuSettings и оригинальные OTA/тепловые службы сохраняются.

> **TvSettingsPlus v24 (экспериментально, 11.11.2026):** новая read-only страница «Температура и охлаждение» (CPU/GPU/RPM + состояние Android). [Технический отчёт v24](docs/TVSETTINGSPLUS_V24_THERMAL_TELEMETRY.md). Защита охлаждения не меняется; KuSettings сохраняется.

# Projector-Soft

## Актуальная экспериментальная сборка TvSettingsPlus MOD v23 — 11.11.2026

- [**Подписанный APK v23 (GitHub Actions)**](https://github.com/MAX-TAC/Projector-Soft/actions/runs/43084120101/artifacts/13884564673) — выполнены Android 13 компиляция, оригинальная подписная идентичность платформы, OEM DEX repack, AndroidManifest, JVM-тесты и сверка неизменённых ресурсов.
- [**Отчёт v23**](docs/TVSETTINGSPLUS_V23_PROGRESS_2026-11-11.md) — новая read-only диагностика совместимости в меню «Система · Проектор», копирование сводки одной кнопкой, статическое сравнение исходного TvSettingsPlus APK 8052/8054.
- Старые сборки v2–v22 — **архивные**, приведены ниже для истории разработки. v23 не является финальной проверенной сборкой для владельца.
- Заводские APK **RUIOS.8052.1.0.4** и **RUIOS.8054.1.0.1** совпали побайтово: SHA-256 `e4184a13fbf56fa069effc6a40ea50c0da7e9190321b389f59ba34700a80d0a1`. Но аппаратная совместимость MOD на 8054 **не подтверждена** и оригинальный APK текущей установленной прошивки владельца ещё требуется сверить.
- **Не отключать KuSettings, не отключать термозащиту и не передавать экспериментальный APK неопытному владельцу без плана отката.**


## Архив: TvSettingsPlus MOD v19 (8052)

- [**Подписанный APK v19**](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40375223763/artifacts/12674936670) — успешно пройдены Java/D8, заводской DEX/repack, platform test-key и JVM-тесты безопасного запуска мониторинга.
- [**Отчёт о новых исправлениях и ограничениях v19**](docs/TVSETTINGSPLUS_V19_PROGRESS_2026-10-26.md).
- Исправлена критическая ложная активация резервного watchdog при ограниченной Android 13 видимости `KuSettings`; восстановление `KuSourceRJ.TimeService` теперь консервативнее и не полагается на присутствие посторонних служб.
- В Android TV «Система · Проектор» добавлена read-only строка о конфигурации контроля охлаждения. Всё, что было в v18, сохранено.
- **Сборка только для RUIOS 8052; не прошла испытаний на железе, НЕ готова для 8054, НЕ отключать KuSettings.**


## Архив: TvSettingsPlus MOD v18 (8052, 26.10.2026)

- [**TvSettingsPlus v18 — подписанный APK, GitHub Actions**](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40332001444/artifacts/12655213059).
- [**Отчёт v18: восстановление KuSourceRJ.TimeService и технические ограничения**](docs/TVSETTINGSPLUS_V18_PROGRESS_2026-10-26.md).
- Добавлен **условный** сервис контроля наличия `KuSourceRJ.TimeService`. При включённом штатном `KuSettings` он не работает; после намеренного отключения старого пакета и перезагрузки может восстанавливать исходную службу, если Android позволяет корректно контролировать её состояние.
- Все прежние OEM-настройки сохранены: 5-band EQ 120–10000 Гц, видео/RGB, HDMI CEC/EDID/boot-source, установка проектора, System Info, телевизионный ярлык.
- **Не финальный релиз:** нельзя отключать `KuSettings` до аппаратного теста поведения термозащиты/сторожевого сервиса. Не устанавливать базовый APK 8052 на ревизию 8054.


## Архив: TvSettingsPlus MOD v16 (RUIOS 8052)

- [**Подписанный APK v16 для 8052**](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40129257332/artifacts/12573961443) — прошёл статическую проверку CI, не тестировался на проекторе.
- [**Отчёт v16: HDMI-CEC, выбор источника при включении, eARC и оставшиеся зависимости**](docs/TVSETTINGSPLUS_V16_PROGRESS_2026-10-24.md).
- Заводской HDMI-CEC через `CompatibleApis`, с выключением eARC/автовыключения вместе с CEC; **при требовании OEM power-confirmation сохраняется штатный экран, прямой переключатель скрывается**.
- Источник при включении через `persist.trd.source_id` и реальные TV Input IDs, только для подтверждённых физических портов; три полноценные Android TV категории, пять полос EQ 120/500/1500/5000/10000 Гц, исходные ползунки изображения и звука, HDR/MEMC при поддержке, EDID, ярлык «Настройки».
- **НЕ ГОТОВО к передаче владельцу как финальная сборка.** Остаточные прямые ссылки на KuSettings, watchdog KuSourceRJ, HDMI конфигурации 8054 и аппаратные испытания остаются критическими.


## Архив: TvSettingsPlus MOD v13 (8052)

- [**Подписанный APK v13 — GitHub Actions Artifact**](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40119913189/artifacts/12570675887).
- [**Подробный отчёт о v13 и оставшихся системных зависимостях**](docs/TVSETTINGSPLUS_V13_PROGRESS_2026-10-24.md).
- Пять **настоящих** заводских полос EQ: **120/500/1500/5000/10000 Гц**; выбор источника совместимости HDMI EDID 1.4/2.0/Auto/2.1 с OEM-гейтами; 10 ползунков изображения/цвета/баса и ВЧ, HDR/MEMC, четыре положения проектора.
- Native Android TV группы «Изображение», «Звук», «Проекция», «HDMI», launcher alias «Настройки», маршруты к остальным штатным OEM экраном.
- **Не финальный для неподготовленного тестировщика.** Необходимо завершить HDMI CEC/eARC, связи с KuSettings, OEM watchdog и аппаратные испытания. Не устанавливать APK 8052 на 8054.


## Архив: TvSettingsPlus MOD v12 (8052)

- [**Подписанный APK v12 — GitHub Actions Artifact**](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40119416058/artifacts/12569253832).
- [**Отчёт об OEM-функциях и оставшихся проблемах v12**](docs/TVSETTINGSPLUS_V12_PROGRESS_2026-10-24.md).
- Настоящие 5 полос заводского эквалайзера: **120, 500, 1500, 5000, 10000 Гц**, исходные DSP ключи и проверки OEM `PartnerSettingsConfig`, в родных Android TV SeekBarPreferences.
- Три категории «Изображение», «Звук», «Проекция», все 10 заводских image/RGB/bass/treble ползунков, режимы изображения и звука, HDR/MEMC (при аппаратной поддержке), режимы установки.
- Штатный Android TV ярлык «Настройки» и доступ к заводским OEM-разделам при сохранении калибровки.
- **Не финальный.** Нужны корректные HDMI/CEC/EDID обработчики, закрытие связей с KuSettings, оригинальная 8054-база и аппаратные испытания. KuSettings пока не отключать.


## Архив: сборка TvSettingsPlus MOD v10 (8052)

- [Подписанный APK v10 — GitHub Actions Artifact](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40117262170/artifacts/12569741437) — только для будущих контролируемых испытаний.
- [Подробный отчёт v10](docs/TVSETTINGSPLUS_V10_PROGRESS_2026-10-24.md) — 10 настоящих OEM ползунков, HDR, MEMC, четыре режима установки проектора, TV ярлык, ранее перенесённые режимы/переключатели.
- В заводском APK изменены только 2 DEX из 16; фокус, трапеция, сервисы моторного управления и калибровочные экраны сохранены.
- **Ни одна версия MOD не испытана на физическом проекторе, 8054 пока без отдельной APK, KuSettings не отключать.**


## Архив: сборка: TvSettingsPlus MOD v7 (8052)

- [**APK v7, собран и подписан в GitHub Actions**](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40113864365/artifacts/12567458244).
- [**Подробный отчёт по v7, включая отсутствующие функции**](docs/TVSETTINGSPLUS_V7_PROGRESS_2026-10-24.md).
- Настоящий TV-ярлык «Настройки»: gear icon, TV banner, MAIN / LEANBACK_LAUNCHER, открывает com.android.tv.settings.MainSettings, не KuSettings.
- Актуальный AOSP MainFragment: пять переходов к OEM-разделам и **три прямых ListPreference** (изображение, цветовая температура, режим звука), **три SwitchPreference** (surround, HDMI eARC, автовыравнивание громкости при наличии OEM параметров).
- Статус: **не финальный**, не испытан на проекторе, не устанавливать на 8054, не отключать KuSettings. Остальные OEM функции ещё в процессе интеграции в AOSP UI.


Отдельный репозиторий кастомных Android TV приложений для проекторов Everycom CineLux, FlixTone D11 Master и совместимых 1LCD моделей.

## TvSettingsPlus MOD (в разработке)

Модифицируем **оригинальное** системное приложение `com.android.tv.settings`. Оставляем стандартные Android TV Settings и двухпанельный интерфейс; аппаратные функции подключаем через OEM/MTK API без перепрошивки. Не отключаем OEM-службы до проверки их зависимостей.

**База:** RUIOS.8052.1.0.4 (один HDMI). Для 8054 (два HDMI) нужен независимый аппаратный профиль.

### Текущая версия и аудит KuSettings

- [Полное дерево меню и статус переноса KuSettings](docs/TVSETTINGSPLUS_FULL_MENU_AND_PARITY_2026-10-23.md) — исходные Android TV пункты, целевое полное меню, контроль полного отказа от KuSettings.
- [Аудит оригинальных XML/DEX и KuSetupWizard](research/oem-apps/Menu_Inventory_8052/README.md)
- [Матрица всех 16 компонентов KuSettings](docs/KUSETTINGS_16_COMPONENT_PARITY.md) — перенесённые, частичные и сознательно исключённые функции. — реальные Activity, receivers, preference resources и внутренние классы.
- [Подписанный экспериментальный TvSettingsPlus v2](https://github.com/MAX-TAC/Projector-Soft/actions/runs/39948327567) — девять совместимых Intent-действий без изменения DEX/UI. Не установка для production.
- [Полный аудит упоминаний KuSettings в OTA](research/oem-apps/Full_OTA_KuReferences/README.md) — 85 APK и 2054 файлов, остаточные config/framework зависимости.

- [Диагностика реального запуска настроек, KuSettings и KuSourceRJ по ADB](docs/ADB_DIAGNOSTICS_AND_SETTINGS_LAUNCH.md) — одна команда для Termux, ZIP-отчёт без изменений проектора; [скрипт](tools/collect_tvsettingsplus_adb_diagnostics.sh).

### Содержимое

- [docs/APP_ARCHITECTURE.md](docs/APP_ARCHITECTURE.md) — архитектура меню и правила безопасной интеграции.
- [docs/DEPENDENCIES.md](docs/DEPENDENCIES.md) — карта компонентов, Binder, JNI и приложений.
- [docs/SOURCES.md](docs/SOURCES.md) — происхождение исходных бинарников.
- [research/oem-apps](research/oem-apps) — перенесённый статический анализ, сравнения 1/2 HDMI, аудит TV-служб.
- [tools](tools) — инструменты извлечения и исторические утилиты исследования.
- [GitHub Actions: extract-oem-8052](.github/workflows/extract-oem-8052.yml) — извлечение реальных `.apk` и `framework-res.apk` из проверенного OTA.
- `vendor/reference/8052/APK/` и `reports/8052/` — **генерируются workflow**, а не заменяются догадками/заглушками.

### Ключи и установка

Вспомогательная подпись AOSP OTA **testkey** не совпадает с APK **platform test key**. Перед сборкой и обновлением приложения нужно подтвердить сертификат оригинального APK; сохранение shared UID `android.uid.system` обязательно. Не следует пробовать `adb install -r` без этого сравнения и резервной копии.

### Разделение репозиториев

- [Everycom-Cinelux-Firmware](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware) хранит оригинальные OTA/PKG, разделы, AVB и firmware-исследования.
- **Projector-Soft** хранит приложения, API, зависимости, сборку и прикладные отчёты.

Оригинальные APK и иные производительские бинарники принадлежат соответствующим правообладателям. Публикация в открытом репозитории требует проверки прав на распространение.

## OEM brightness migration

[Light-source brightness migration — 4 modes, native receiver, inline right pane and calibration protection](docs/LIGHT_SOURCE_MODE_MIGRATION.md). The Java module compiles as Android 13 DEX, but the installable v2 APK does not yet include it.

## Экспериментальная TvSettingsPlus MOD v3 (8052)

- [Отчёт о переносе OEM-функций, реальных экранах и ограничениях](docs/OEM_MIGRATION_IMPLEMENTATION_REPORT_2026-10-23.md)
- [Аудит существующих заводских контроллеров](docs/OEM_MENU_CONTROLLER_AUDIT_2026-10-23.md)
- [Собранная и подписанная APK v3 (GitHub Actions Artifacts)](https://github.com/MAX-TAC/Projector-Soft/actions/runs/39997799927/artifacts/12519724074): исправление customer-0 яркости в правой панели, скрытие пункта BT-колонки, неизменность системных сервисов и калибровки. **Не тестировалась на железе, KuSettings не отключать.**

## Экспериментальная TvSettingsPlus MOD v4 (8052)

- [Отчёт о переносе OEM разделов в AOSP MainSettings](docs/TVSETTINGSPLUS_V4_IMPLEMENTATION_2026-10-24.md)
- [Успешно собранная и подписанная v4 APK](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40111124603/artifacts/12565973499): 5 новых пунктов в стандартном меню Android TV с переходом к штатным OEM-разделам внутри TvSettingsPlus. Это промежуточная навигация через OEM Activity, не окончательное встраивание контента в двухпанельный AOSP экран.
- **Не проверено на проекторе; KuSettings пока не отключать.**

# Projector-Soft

## Актуальная экспериментальная TvSettingsPlus MOD v13 (8052)

- [**Подписанный APK v13 — GitHub Actions Artifact**](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40119913189/artifacts/12570675887).
- [**Подробный отчёт о v13 и оставшихся системных зависимостях**](docs/TVSETTINGSPLUS_V13_PROGRESS_2026-10-24.md).
- Пять **настоящих** заводских полос EQ: **120/500/1500/5000/10000 Гц**; выбор источника совместимости HDMI EDID 1.4/2.0/Auto/2.1 с OEM-гейтами; 10 ползунков изображения/цвета/баса и ВЧ, HDR/MEMC, четыре положения проектора.
- Native Android TV группы «Изображение», «Звук», «Проекция», «HDMI», launcher alias «Настройки», маршруты к остальным штатным OEM экраном.
- **Не финальный для неподготовленного тестировщика.** Необходимо завершить HDMI CEC/eARC, связи с KuSettings, OEM watchdog и аппаратные испытания. Не устанавливать APK 8052 на 8054.


## Актуальная экспериментальная TvSettingsPlus MOD v12 (8052)

- [**Подписанный APK v12 — GitHub Actions Artifact**](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40119416058/artifacts/12569253832).
- [**Отчёт об OEM-функциях и оставшихся проблемах v12**](docs/TVSETTINGSPLUS_V12_PROGRESS_2026-10-24.md).
- Настоящие 5 полос заводского эквалайзера: **120, 500, 1500, 5000, 10000 Гц**, исходные DSP ключи и проверки OEM `PartnerSettingsConfig`, в родных Android TV SeekBarPreferences.
- Три категории «Изображение», «Звук», «Проекция», все 10 заводских image/RGB/bass/treble ползунков, режимы изображения и звука, HDR/MEMC (при аппаратной поддержке), режимы установки.
- Штатный Android TV ярлык «Настройки» и доступ к заводским OEM-разделам при сохранении калибровки.
- **Не финальный.** Нужны корректные HDMI/CEC/EDID обработчики, закрытие связей с KuSettings, оригинальная 8054-база и аппаратные испытания. KuSettings пока не отключать.


## Текущая экспериментальная сборка TvSettingsPlus MOD v10 (8052)

- [Подписанный APK v10 — GitHub Actions Artifact](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40117262170/artifacts/12569741437) — только для будущих контролируемых испытаний.
- [Подробный отчёт v10](docs/TVSETTINGSPLUS_V10_PROGRESS_2026-10-24.md) — 10 настоящих OEM ползунков, HDR, MEMC, четыре режима установки проектора, TV ярлык, ранее перенесённые режимы/переключатели.
- В заводском APK изменены только 2 DEX из 16; фокус, трапеция, сервисы моторного управления и калибровочные экраны сохранены.
- **Ни одна версия MOD не испытана на физическом проекторе, 8054 пока без отдельной APK, KuSettings не отключать.**


## Текущая экспериментальная сборка: TvSettingsPlus MOD v7 (8052)

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

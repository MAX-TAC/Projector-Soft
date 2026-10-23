# Projector-Soft

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

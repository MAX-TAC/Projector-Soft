# Отчёт о переносе OEM-функций в TvSettingsPlus MOD

**Дата:** 23 октября 2026. **База:** Android 13, RUIOS 8052; 8054 использован для сравнительного статического анализа. **Тип проверки:** анализ заводских APK, компиляция исходников и CI; установки на физический проектор не было.

## 1. Подтверждённые функциональные модули в заводском TvSettingsPlus

Проверен оригинальный TvSettingsPlus APK через JADX, инвентаризированы 24 OEM-контроллера и фрагмента (включая альтернативные фрагменты) и их UI-элементы. **Основные OEM-экраны уже существуют, нужно подключать их к общему Android TV меню, а не переписывать драйверы.**

| Блок | Фактически найдено | Что не следует переделывать |
|---|---|---|
| Яркость света | `BrightnessModeSettingsSecondaryController` уже имеет Eco/Standard/Highlight в правой панели; автоматический режим скрыт | Не подменять `MtkUtil.putLightMode` на `KuSourceRJ BrightReceiver` без проверки Customer-ID |
| Изображение | `PictureSubMenuController`, `PictureModeValueSecondaryController`, `ColorTempValueSecondaryController`, `AdvanceSettingsSecondaryController`: пять параметров изображения, RGB цветовая температура, HDR, MEMC | Оставить OEM API и только переставить/вывести понятные опции |
| Звук | `SoundSubMenuController`: пресеты, eARC, surround, auto volume; `SoundModeValueSecondaryController`: bass/treble | Не показывать 5-полосный EQ — нужный API не подтверждён |
| Источники | `SourceSubMenuController`: HDMI, USB, EDID, CEC, игровые/low-latency параметры, запуск при включении | Не показывать фантомный HDMI 2 в 8052 |
| Проекция | `ProjectorSubMenuController`, `FocusSettingsSecondaryController`, `KeystoneSettingsSecondaryController`, `ProjectionModeSecondaryController` | **Все** полноэкранные калибровочные изображения фокуса, трапеции, 4 углов сохранить |
| Система | `SystemSubMenuController`, `SystemInfoSecondaryController`: OTA, локальное обновление, сброс, температуры, версия/сеть | Не отключать RuiYunOta / сервисы термозащиты и фокусировки |

## 2. Обнаруженные сложности

### Ветка режима яркости

`GeneralSubMenuController` при `Build.A_RJ_CUSTOMER_ID==0` открывает **полноэкранный** `KuSourceRJ.BrightActivity`; при другом значении открывает штатный правый контроллер `BrightnessModeSettingsSecondaryController` с `MtkUtil.getLightMode/putLightMode`. Эти пути имеют разные аппаратные команды и нумерацию. В `KuSourceRJ` существуют отдельные четыре режима (0 Performance; 1 Standard; 2 Quiet; 3 High Altitude) и собственная термозащита.

Реализован исходный `LegacyBrightnessUiHook`: при наличии экспортированного BrightReceiver, доступного `persist.bright.model` и корректных OEM view IDs открывается существующая правая панель с переопределёнными командами через OEM `BrightReceiver`. Если любой признак отсутствует — оставляется заводской полноэкранный путь. Не менять напрямую PWM или свойства подсветки.

### Источники и Bluetooth-колонка

В заводских `SourceFragment` и `SourceSubMenuController` есть пункт `tciv_source_bluetooth_speaker`. Реализован UI-only `OemMenuVisibilityHook` для скрытия этого пункта без отключения Bluetooth-стека, сопряжения с пультом и других сервисов.

### Пересечения с KuSettings

Подтверждены старые Intent действий и остаточные ссылки `settingApps`, `whaletv_config.json`, `services.jar`. Девять совместимых Intent-фильтров в подписанной v2 не гарантируют, что лаунчер и системная кнопка откроют именно TvSettingsPlus; прямой вызов `com.my.settings` нужно устранить отдельно.

`KuSetupWizard` имеет собственные копии классов; сохранять без изменения. Отключение `KuSettings` остаётся **запрещённым**, пока не доказана устойчивость `KuSourceRJ.TimeService` после аварийного завершения и холодного старта.

## 3. Исходные модули и автоматические тесты

- `ProjectorCapabilities`: hardware feature gates 8052/8054.
- `MenuPresentationPolicy`: правые панели для обычных функций, исходные fullscreen для фокуса/трапеции.
- `LightSourceBrightnessController`, `KuSourceRjBrightnessGateway`, `LegacyBrightnessUiHook`: правопанельная ветка для customer 0 с возвращением к исходному OEM режиму при непригодности API.
- `OemMenuVisibilityHook`: отсутствие пункта Bluetooth-колонки в UI при неизменности сервисов.
- `patch_customer0_lightmode_smali.py`: ограниченные изменения только целевых OEM UI классов, без трогания калибровки.
- `compose_brightness_v3_apk.py`: формирование версии с новым DEX и исходными OEM ресурсами; платформа AOSP подписывается только после сверки сертификатов.
- `test-light-mode-android.yml`, `test-tvsettings-capabilities.yml`: компиляция Android 13/JVM-тесты.

## 4. Статус конкретных целей

| Цель | Состояние |
|---|---|
| Извлечь всю информацию из заводской OTA для экрана источников/изображения/звука/проекции | Выполнено для статически проверенных контроллеров 8052; есть отдельный diff 8054 |
| Сохранить полноэкранные изображения фокуса/трапеции | Архитектура и патч исключают их исходные классы из изменяемых |
| Перенести обычные значения в правые панели | Штатные OEM панельные контроллеры обнаружены; brightness/source-патчи подготовлены |
| Убрать пункт Bluetooth-колонки | UI-hook подготовлен, требуется пакетная и аппаратная проверка |
| Сделать единое главное меню внутри Android TV Settings | **Не завершено**: штатные OEM панели пока не привязаны ко всем элементам стандартного AOSP MainSettings |
| Сделать 5-полосный EQ | **Не завершено**: необходим реальный MTK DSP API, в текущих UI есть только bass/treble |
| Отказаться полностью от KuSettings | **Не подтверждено** до runtime аудита/ADB и безопасного теста после обновления |
| Получить рабочий v3 на обоих вариантах платы | **Не завершено**: v3 экспериментален и ориентирован на 8052; 8054 нужен свой базовый APK |
| Заводское восстановление, OTA, AVB, калибровка | Не менять заводские приложения и разделы |

## 5. Следующие критерии завершения

1. Собрать, статически проверить и подписать экспериментальный 8052 APK, убедиться, что нет изменений в `resources.arsc`, JNI, иных оригинальных файлах, не относящихся к затронутым DEX.
2. Проверить APK на проекторе с возможностью отката: пульт, открытие настроек из лаунчера, HDMI, профиль яркости, температурные ограничения, фокус/трапеция, OTA, звук, режим сна и cold boot.
3. Отладить главный AOSP экран так, чтобы все OEM функции стали доступны из **одного** меню и KuSettings не требовался.
4. Отдельно перенести подтверждённые изменения на OEM базу 8054, сохраняя variant-specific JNI и разные моторы.
5. После этого выполнить контролируемое тестовое отключение KuSettings, не удаляя системный APK до завершения стабильности.

**Не выдавать исследование UI и успешно скомпилированный модуль за готовую установку или проверенный на железе ROM.**

См. [детальный аудит контроллеров](OEM_MENU_CONTROLLER_AUDIT_2026-10-23.md), [исходники](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/), [настройки запуска](ADB_DIAGNOSTICS_AND_SETTINGS_LAUNCH.md).

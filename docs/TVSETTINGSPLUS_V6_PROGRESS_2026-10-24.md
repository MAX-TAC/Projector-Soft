# TvSettingsPlus MOD v6 — ярлык Android TV и первый прямой перенос OEM-режимов

**Дата:** 24 октября 2026. **Аппаратная база:** оригинальная RUIOS 8052 (Android 13). **Статус:** экспериментальный, без аппаратных испытаний.

[Успешная сборка и проверки](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40113383094) · [Подписанный APK v6](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40113383094/artifacts/12567821028).

## 1. Новый ярлык приложения

В бинарный AndroidManifest добавлен экспортированный `com.android.tv.settings.mod.SettingsLauncher`:

- `android.intent.action.MAIN`;
- категории `android.intent.category.LEANBACK_LAUNCHER` (Android TV) и `android.intent.category.LAUNCHER` (обычный Android);
- **целевой экран:** `com.android.tv.settings.MainSettings`, а не KuSettings;
- иконка-шестерёнка из заводских ресурсов `drawable/ic_settings_launcher_icon` (`0x7f08022e`);
- широкоформатный телевизионный баннер `drawable/ic_launcher_banner_settings` (`0x7f080142`);
- локализованное стандартное имя приложения: русский «Настройки» при русской локали.

APK-обновление само по себе **не может принудительно закрепить значок на главном экране**, этим управляет OEM-лаунчер. Статическая проверка подтвердила корректную регистрацию alias и ресурсов, но не гарантирует, что конкретный лаунчер покажет его без фильтра системных приложений. Возможна отдельная иконка рядом с уже зарегистрированной Activity штатных настроек.

## 2. Перенос настоящих аппаратных настроек в стандартное Android TV меню

В отличие от версии v4, которая добавила 5 ссылок на OEM-экран, в v6 код `OemInlinePreferencesHook` непосредственно добавляет в штатную `MainFragment` Android TV Settings группу **«Быстрые настройки проектора»** с тремя значимыми выборами:

| Пункт | Точные заводские варианты → OEM значения | Ключ состояния |
|---|---|---|
| Режим изображения | Пользовательский=0, Стандартный=7, Яркий=3, Спорт=2, Кино=9, Игра=10, Экономичный=11 | `Settings.Global.picture_mode` |
| Цветовая температура | Пользовательская=0, Холодная=1, Стандартная=2, Тёплая=3 | `Settings.Global.picture_color_temperature` |
| Режим звука | Стандартный=1, Яркий=2, Спорт=3, Кино=4, Музыка=5, Новости=6, Пользовательский=0 | `Settings.Global.sound_style` |

Числовые соответствия взяты **не предположительно**, а из заводских `PictureFragment.PICTURE_MODE_VALUES` и `SoundFragment.SOUND_MODE_VALUES`. Выбор передаётся той же настройке `Settings.Global`, которую используют OEM-классы через `SettingsUtil.putGlobalValueInt`. После записи значение проверяется обратным чтением. Если состояние вне ожидаемого списка, элемент не отображается.

Для интерфейса используются исходные AndroidX `ListPreference` самого `TvSettingsPlus`; внешние библиотеки AndroidX и сторонние ресурсы не подмешивались, чтобы не нарушить DEX/ресурсные зависимости. **Время и способ отображения диалога выбора штатным двухпанельным Android TV UI на реальном проекторе пока не проверены.**

## 3. Сохранено из v4/v5

- Пять маршрутов главного Android TV меню: Проекция, Источники, Изображение проектора, Звук проектора, Дополнительно.
- Яркость источника света: заводской `MtkUtil` режим для соответствующего customer ID и адаптер штатного `KuSourceRJ.BrightReceiver` для customer 0 с безопасным резервным полноэкранным экраном.
- Скрытие пункта Bluetooth-колонки без удаления Bluetooth-сервисов/профилей, необходимых для аксессуаров.
- Девять legacy Intent actions в AndroidManifest.
- Исходный fullscreen режим автофокуса, ручной/автоматической трапеции, коррекции четырёх углов, все калибровочные изображения, службы источников/термозащиты, OTA.

## 4. Техническая проверка v6

GitHub Actions run `40113383094` успешно выполнил компиляцию исходного Android 13 кода, сборку патчей заводских smali, APK-перепаковку, проверку `aapt`, сравнение сертификата с оригиналом и `apksigner verify`.

- `packageName = com.android.tv.settings`, `versionCode = 6`.
- Из оригинальных 16 DEX заменены только `classes13.dex` и `classes14.dex`, дополнительно включён `classes17.dex` с новым Java кодом.
- Оригинальные `resources.arsc`, другие DEX/JNI и ресурсы сохранены. Подписан совпадающим **AOSP platform test certificate**, как стоковый APK 8052.
- Собранная версия **только 8052**; нельзя устанавливать на 8054 без отдельного базового APK.
- Предыдущие исторические v3/v4/v5 workflow переведены в ручной режим, чтобы не создавать множество лишних билдов при каждой правке исходников.

## 5. Что пока НЕ завершено

1. Полное встраивание остальных OEM-контроллеров *вместо перехода* в отдельную OEM Activity: ползунки изображения и цвета, HDR/MEMC, HDMI/EDID/CEC, звук eARC/surround/auto-volume, оптическая геометрия и источник при включении.
2. Контроль всех системных вызовов `KuSettings`, включая OEM-лаунчер и `settingApps`, `services.jar` и `KuSourceRJ.TimeService`. Старый APK пока отключать **нельзя**.
3. Аппаратная проверка на проекторе: доступность нового ярлыка, реальная смена значений, навигация пультом, автофокус и трапеция, холодный запуск, откат.
4. Отдельная версия 8054 с проверкой HDMI 2 и ToF.

**Вывод:** v6 уже содержит три прямых, не зависящих от открытия внешней OEM Activity выбора в стандартном Android TV меню. Это реальный прогресс относительно v4, но все функции KuSettings ещё не перенесены. Пользователь не просил устанавливать экспериментальные промежуточные версии — до полной интеграции их не выдавать за релиз для тестера.

## Исходники и проверки

- [Android TV inline selectors](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/OemInlinePreferencesHook.java)
- [OEM root navigation](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/OemRootMenuHook.java)
- [Binary AndroidManifest launcher aliases](../tools/patch_axml_compat_aliases.py)
- [DEX OEM main fragment patch](../tools/patch_aosp_root_menu_smali.py)
- [Автосборка v6](../.github/workflows/build-tvsettingsplus-inline-v6.yml)
- [Методы из заводского APK](../research/oem-apps/Native_Controllers_8052/method_signatures.json)

# TvSettingsPlus MOD v10 — реальный прогресс по переносу OEM настроек

Дата: 24 октября 2026. Исходная платформа: **RUIOS 8052, Android 13**. Статус: **экспериментальная сборка без теста на проекторе**.

**Результат CI:** [run 40117262170](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40117262170) · [APK, исходная подписанная v10](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40117262170/artifacts/12569741437).

## Новые изменения v8–v10 относительно v7

### 10 настоящих ползунков Android TV

В `MainFragment` стандартного Android TV Settings, а не во внешней OEM Activity, добавлены `androidx.preference.SeekBarPreference`. Используются **настоящие значения и min/max** из оригинальных layout XML `TvSettingsPlus` и OEM listeners:

| Пункт | Реальный OEM ключ | min | max |
|---|---|---:|---:|
| Яркость изображения (не LED) | `picture_brightness` | 0 | 100 |
| Контрастность | `picture_contrast` | 0 | 100 |
| Насыщенность | `picture_saturation` | 0 | 100 |
| Оттенок | `picture_hue` | -50 | 50 |
| Резкость | `picture_sharpness` | 0 | 20 |
| Красный | `picture_red_gain` | -100 | 100 |
| Зелёный | `picture_green_gain` | -100 | 100 |
| Синий | `picture_blue_gain` | -100 | 100 |
| Бас | `sound_bass` | 0 | 100 |
| Высокие частоты | `sound_treble` | 0 | 100 |

Все пишут штатный `Settings.Global` через тот же ключ, что OEM `SettingsUtil.putGlobalValueInt`, выполняют readback и не подтверждают ошибочную запись. Отсутствующий/некорректный параметр скрывается. Не меняют GPIO, моторы, LED PWM и другие hardware services.

### Дополнительные прямые элементы

- HDR: `picture_list_hdr`, вкл/выкл, показывается только при существующем значении 0/1.
- MEMC: `tv_picture_advance_video_mjc_effect`, выбор 0 выкл, 1 низкий, 2 средний, 3 высокий, показывается только при заводском значении 0..3.
- **Установка проектора:** новый список четырёх вариантов непосредственно в Android TV: спереди на столе (0), сзади на столе (1), спереди под потолком (3), сзади под потолком (2). Значения и метод `ProjectorManager.getProjectorMirrorMode()/setProjectorMirrorMode(context,int)` взяты из заводского `ProjectionModeSecondaryController`. Список не показывается, если OEM метод не доступен/возвращает постороннее значение; результат изменения перепроверяется. Это не затрагивает калибровочную геометрию.

### Сохранено из предыдущих версий

- 3 списка режимов изображения, цветовой температуры, звука.
- 3 звуковых переключателя (surround/eARC/auto volume) при наличии заводских 0/1 значений.
- 5 ссылок AOSP MainFragment на штатные OEM разделы, которые пока открывают отдельную Activity того же TvSettingsPlus.
- Ярлык «Настройки» c `MAIN`/`LEANBACK_LAUNCHER`/`LAUNCHER`, заводскими icon/banner, прямым запуском `com.android.tv.settings.MainSettings`.
- Кастомизированный выбор световой яркости для customer-0 (только когда безопасен `KuSourceRJ BrightReceiver`; иначе заводской fullscreen fallback).
- Скрытие пункта Bluetooth-колонки из OEM раздела источников без отключения Bluetooth.
- 9 совместимых OEM Intent aliases.

## Техническая проверка v10

Run `40117262170` завершился success: Java Android 13 → D8, бинарный smali патч и сборка, 9 legacy actions, manifest launcher icon/banner, `aapt dump badging`, `zipalign`, подпись тем же AOSP platform test key, `apksigner verify`. `package com.android.tv.settings`, `versionCode 10`. Из 16 заводских DEX изменены только `classes13.dex` и `classes14.dex`, добавлен `classes17.dex`. Все остальные заводские ресурсы/DEX/JNI сохранены при проверке целостности.

**Эти проверки доказывают корректную структуру APK и не доказывают runtime работоспособность на OEM ART, управление с пульта или применение HDR/MEMC.** Нельзя отправлять владельцу v10 как финальный release и тем более отключать KuSettings до подтверждённого runtime тестирования.

## Что ещё осталось

- **Перестроить оставшиеся ссылки** HDMI, EDID, CEC, стартового источника, питания, информации/обновлений, цифрового зума и прочего в native AOSP контролы; сейчас они открывают заводской OEM Activity.
- Безопасно решить прямые вызовы `KuSettings` из `settingApps`, services.jar, OEM launcher и пульта; сохранить восстановление `KuSourceRJ.TimeService`.
- Окончательно устранить дублирование экрана настроек, не повреждая KuSetupWizard/Provision.
- Отдельная 8054-база с ToF/HDMI2 и аппаратные испытания.
- 5-полосного OEM эквалайзера по исходным данным нет, только bass/treble; нельзя выдавать фиктивные ползунки.

**Автофокус, ручная/автоматическая трапеция, редактор четырёх углов и их полноэкранные калибровочные экраны НЕ ИЗМЕНЯЛИСЬ.**

## Исходники, исследования, CI

- [Slider controller](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/OemInlineSlidersHook.java).
- [Brightness/modes/HDR and MEMC controls](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/OemInlinePreferencesHook.java).
- [Projector installation modes](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/OemProjectorOrientationHook.java).
- [Factory XML slider bounds](../research/oem-apps/Native_Controllers_8052/slider_layout_bounds.json).
- [Factory projector mode/source analysis](../research/oem-apps/Native_Controllers_8052/orientation_and_source.json).
- [APK build and signature](../.github/workflows/build-tvsettingsplus-inline-v10.yml).

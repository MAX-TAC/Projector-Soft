# Аудит фактически реализованных OEM-функций и план интеграции

Дата: 23.10.2026. Основание: декомпиляция **заводского TvSettingsPlus 8052**, сопоставление с KuSourceRJ, KuSettings и сравнением APK 8052/8054. Это **не означает**, что пункты уже объединены со стандартной иерархией Android TV Settings или испытаны на устройстве.

Вывод: большинство ранее запланированных OEM-контроллеров уже существует в заводском TvSettingsPlus с собственными правыми панелями. Основная работа — **подключение экранов к единой навигации**, устранение ошибочных прямых запусков OEM Fullscreen Activity, верная аппаратная маршрутизация и валидация 8054, а не повторная реализация регуляторов.

### Общее

| OEM контроллер в базовом TvSettingsPlus | Уже найденные элементы | Что делаем |
|---|---|---|
| `GeneralSubMenuController` | Язык, экран, подсветка, дата, заставка, питание. `tciv_general_date_time`, `tciv_general_font_size`, `tciv_general_language_settings`, `tciv_general_projection_brightness_mode`, `tciv_general_screensaver_settings`… | Использовать существующий контроллер, не создавать дубликат |
| `BrightnessModeSettingsSecondaryController` | Три видимые радиокнопки Эко/Стандарт/Повышенная; Авто скрыто. При customer ID 0 вместо панели вызывается внешний BrightActivity. `rjRadioGroup`, `rtv_general_projection_brightness_mode_auto`, `rtv_general_projection_brightness_mode_eco`, `rtv_general_projection_brightness_mode_highlight`, `rtv_general_projection_brightness_mode_standard` | Осторожно: customer ID определяет другой аппаратный путь |
| `LanguageSettingsSecondaryController` | Языки уже в OEM. `` | Использовать существующий контроллер, не создавать дубликат |
| `DateTimeSecondaryController` | Штатные дата/время. `` | Использовать существующий контроллер, не создавать дубликат |
| `TimeZoneSecondaryController` | Штатные часовые пояса. `` | Использовать существующий контроллер, не создавать дубликат |

### Изображение

| OEM контроллер в базовом TvSettingsPlus | Уже найденные элементы | Что делаем |
|---|---|---|
| `PictureSubMenuController` | Режим изображения, температура, сброс. `rjRadioGroup_color_temperature`, `rjRadioGroup_picture_mode`, `rjscrollView`, `rtv_color_temp_cool`, `rtv_color_temp_custom`… | Использовать существующий контроллер, не создавать дубликат |
| `PictureModeValueSecondaryController` | Пять ползунков: яркость картинки, контраст, насыщенность, резкость, оттенок. `tsbv_picture_brightness`, `tsbv_picture_contrast`, `tsbv_picture_hue`, `tsbv_picture_saturation`, `tsbv_picture_sharpness` | Использовать существующий контроллер, не создавать дубликат |
| `ColorTempValueSecondaryController` | Ползунки R/G/B. `tsbv_color_temp_blue`, `tsbv_color_temp_green`, `tsbv_color_temp_red` | Использовать существующий контроллер, не создавать дубликат |
| `AdvanceSettingsSecondaryController` | HDR On/Off, MEMC Off/Low/Medium/High + демо. `btn_mjc_demo`, `mjc_left`, `mjc_mid`, `mjc_right`, `rjRadioGroup_hdr`… | Использовать существующий контроллер, не создавать дубликат |

### Звук

| OEM контроллер в базовом TvSettingsPlus | Уже найденные элементы | Что делаем |
|---|---|---|
| `SoundSubMenuController` | Режимы звука, surround, eARC, автогромкость, системные звуки, сброс. `rjRadioGroup`, `rtv_sound_mode_custom`, `rtv_sound_mode_movie`, `rtv_sound_mode_music`, `rtv_sound_mode_news`… | Использовать существующий контроллер, не создавать дубликат |
| `SoundModeValueSecondaryController` | Низкие/высокие частоты (bass/treble), **не** 5-полосный EQ. `tsbv_sound_mode_value_bass`, `tsbv_sound_mode_value_treble` | Использовать существующий контроллер, не создавать дубликат |

### Источники

| OEM контроллер в базовом TvSettingsPlus | Уже найденные элементы | Что делаем |
|---|---|---|
| `SourceSubMenuController` | HDMI, USB, CEC, EDID, игровой режим, подключение/источник при включении, BT-колонка (исключить). `tciv_source_av`, `tciv_source_bluetooth_speaker`, `tciv_source_edid_version`, `tciv_source_hdmi`, `tciv_source_hdmi2`… | Скрыть неподдерживаемые входы/BT-колонку, сохранить CEC |
| `PowerOnSourceSecondaryController` | Начальный источник. `rjRadioGroup`, `rtv_source_power_on_av`, `rtv_source_power_on_hdmi1`, `rtv_source_power_on_hdmi2`, `rtv_source_power_on_hdmi3`… | Использовать существующий контроллер, не создавать дубликат |
| `PowerOnAppSecondaryController` | Приложение при старте. `listview_power_on_app` | Использовать существующий контроллер, не создавать дубликат |

### Проекция

| OEM контроллер в базовом TvSettingsPlus | Уже найденные элементы | Что делаем |
|---|---|---|
| `ProjectorSubMenuController` | Кнопки автофокус/трапеция, ручная калибровка, экран зума. `tciv_projector_auto_focus`, `tciv_projector_auto_keystone`, `tciv_projector_focus_settings`, `tciv_projector_four_point_keystone`, `tciv_projector_hv_keystone`… | Сохраняем штатную калибровку |
| `FocusSettingsSecondaryController` | Переключатели автофокус по включению/движению. `tsv_focus_accurate_focus`, `tsv_focus_move_auto`, `tsv_focus_power_on_auto`, `tv_focus_accurate_focus_tip` | Сохраняем штатную калибровку |
| `KeystoneSettingsSecondaryController` | Переключатели автотрапеции/препятствий; калибровка остается внешней. `tciv_keystone_auto_screen`, `tciv_keystone_correction`, `tsv_keystone_auto_obstacle_avoidance`, `tsv_keystone_auto_screen`, `tsv_keystone_move_auto`… | Сохраняем штатную калибровку |
| `ProjectionModeSecondaryController` | Установка: четыре положения, автоинверсия. `iv_mode_tips`, `rjRadioGroup`, `rtv_projector_mode_ceiling_front`, `rtv_projector_mode_ceiling_rear`, `rtv_projector_mode_front`… | Использовать существующий контроллер, не создавать дубликат |

### Система

| OEM контроллер в базовом TvSettingsPlus | Уже найденные элементы | Что делаем |
|---|---|---|
| `SystemSubMenuController` | Информация, локальное/OTA обновление, сброс. `tciv_system_factory_reset`, `tciv_system_info`, `tciv_system_local_update`, `tciv_system_online_update` | Использовать существующий контроллер, не создавать дубликат |
| `SystemInfoSecondaryController` | Аппаратные температуры, версии, MAC. `tcvv_system_ambient_temperature`, `tcvv_system_android_version`, `tcvv_system_device_model`, `tcvv_system_device_temperature`, `tcvv_system_mac_address`… | Использовать существующий контроллер, не создавать дубликат |


## Необходимо реализовать отдельно

| Элемент | Состояние | Решение |
|---|---|---|
| Трёхпунктовая яркость источника света | Есть в OEM правой панели, но при `Build.A_RJ_CUSTOMER_ID==0` открывается внешний BrightActivity | Для customer 0 нужен отдельный правопанельный адаптер к KuSourceRJ BrightReceiver; нельзя механически переключить на MtkUtil.putLightMode |
| 5-полосный эквалайзер | **Не найден** в проверенных панелях: имеется bass/treble | Не показывать 5 ползунков без OEM DSP API |
| HDR / MEMC | В исходном контроллере есть варианты и обработчики | Ограничивать аппаратной поддержкой/проверками, не обещать HDR/MEMC за одно лишь наличие UI |
| HDMI 2 | Исходные классы поддерживают несколько HDMI, но профиль 8052 — 1 порт | Gating по `ro.rjpj.machine.id` и аппаратной ревизии, 8054 только после проверки на устройстве |
| Bluetooth-колонка | Исходная панель источников содержит `tciv_source_bluetooth_speaker` | Не переносить/скрывать. Не удалять BT-профили, нужные для пульта |
| Экран фокуса, автофокус, ручная трапеция, авто-трапеция | Существующие OEM Activities выводят калибровку | Сохранить оригинальный fullscreen и обработку клавиш; не адаптировать в ползунки |
| Отключение KuSettings | Есть потенциальный watchdog KuSourceRJ и OEM settingApps | Не отключать до аппаратной проверки, журналов и переназначения всех launch paths |
| Перенос источников, изображения, звука, информации, фокуса | Контроллеры в 8052 уже есть | Использовать OEM native, не менять PWM, GPIO, моторы, TRD HAL |

## Специфический случай яркости

При обращении к пункту яркости в `GeneralSubMenuController`:
- если `Build.A_RJ_CUSTOMER_ID == 0`, через `FloatMenuManager.jumpToExternalActivity` открывается `com.coolux.source.activity.BrightActivity` (полноэкранный режим; OEM `BrightReceiver` и `persist.bright.model`);
- иначе вызывается `FloatMenuManager.openSecondaryInterface(R.layout.fragment_general_brightness_mode_settins, BrightnessModeSettingsSecondaryController.class)`, правопанельный режим `MtkUtil.getLightMode/putLightMode`. Видимых пунктов три: Eco, Standard, Highlight; Auto закодирован, но скрыт.

Эти API не взаимозаменяемы. Для правильного переноса необходимо выбрать *фактическую* ветку по customer ID и сохранить OEM-драйвер. Подготовленный `KuSourceRjBrightnessGateway` не должен подключаться вместо MtkUtil-контроллера на неподходящей ревизии.

## Источники
- [Inventory controllers](../research/oem-apps/TvSettings_Controller_8052/metadata.json)
- [Пересечение механизмов яркости](../research/oem-apps/Brightness_8052/overlap.json)
- [Сравнение прошивок](../research/oem-apps/Deep_APK_Comparison/README.md)
- [Код альтернативной правой панели яркости](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/LightSourceModePreferenceView.java)

## Ограничение результата
**Это результаты анализа, модели и подготовленные модули.** Новая объединённая Android TV навигация не внедрена в подписанный `TvSettingsPlus v2`, а проверка на физическом устройстве не выполнена. Ни один из перечисленных разрывов не следует считать закрытым без теста.

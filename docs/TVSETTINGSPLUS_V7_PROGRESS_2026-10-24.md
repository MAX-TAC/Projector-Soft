# TvSettingsPlus MOD v7 — Android TV launcher + заводские настройки в AOSP

**Дата:** 24 октября 2026. **База:** оригинальный APK RUIOS 8052 (Android 13). **Версия:** `com.android.tv.settings`, `versionCode=7`. **Статус:** экспериментальный, без установки и аппаратного тестирования.

[Успешная CI сборка](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40113864365) · [Собранный APK (GitHub Actions Artifact)](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40113864365/artifacts/12567458244)

## Реально изменённое по сравнению со стоком

| Блок | Что изменено НАМИ в APK | Оговорка |
|---|---|---|
| Ярлык «Настройки» | Добавлен `activity-alias` с `MAIN`, `LEANBACK_LAUNCHER`, `LAUNCHER`, target=`com.android.tv.settings.MainSettings`, иконкой-шестерёнкой `ic_settings_launcher_icon`, TV-баннером `ic_launcher_banner_settings`, локализованным названием | OEM-лаунчер может фильтровать системные приложения; создать закреплённый значок на главной странице через обновление APK нельзя |
| Главное меню Android TV | Добавлены 5 OEM-ссылок: Проекция, Источники, Изображение проектора, Звук проектора, Дополнительно | Это пока переходы к уже существующей OEM Activity внутри того же TvSettingsPlus |
| Режим изображения | **Новая штатная AndroidX ListPreference в MainFragment**: Пользовательский=0, Стандартный=7, Яркий=3, Спорт=2, Кино=9, Игра=10, Экономичный=11 | Параметр `Settings.Global.picture_mode`, сопоставлен с заводским PictureFragment |
| Цветовая температура | **Новая ListPreference в MainFragment**: Пользовательская=0, Холодная=1, Стандартная=2, Тёплая=3 | `Settings.Global.picture_color_temperature` |
| Режим звука | **Новая ListPreference в MainFragment**: Стандартный=1, Яркий=2, Спорт=3, Кино=4, Музыка=5, Новости=6, Пользовательский=0 | `Settings.Global.sound_style` |
| Объёмное звучание | **Новая SwitchPreference**, состояние через `sound_sound_surround` | Показывается только когда OEM системное значение уже существует и равно 0/1 |
| HDMI eARC | **Новая SwitchPreference**, OEM поле `PreferenceConfigUtilsBase.KEY_SOUND_EARC` (фактический `sound_earc`) | Скрывается, если OEM не создал состояние |
| Автоматическая громкость | **Новая SwitchPreference**, поле `KEY_SOUND_AUTO_VOLUME_CONTROL` (`sound_auto_volume_control`) | Скрывается при отсутствии состояния |
| Режим яркости источника света | Условно перенаправлен из полноэкранной BrightActivity во вторичную панель, не дублирует `MtkUtil` | Возможен штатный fullscreen fallback, зависящий от customer ID и доступности BrightReceiver |
| Bluetooth-колонка | Скрывается пункт её запуска в меню источников | Bluetooth-стек, сопряжение и пульт сохраняются |
| KuSettings-совместимость | Девять OEM Intent actions направлены к TvSettingsPlus | Другие обработчики KuSettings ещё могут выигрывать implicit Intent или явные вызовы |

В AOSP `MainFragment` теперь есть категория **«Быстрые настройки проектора»**, содержащая прямые элементы выбора. Прямые изменения 0/1 и режимов записываются через те же системные параметры, которые используют заводские OEM-фрагменты; перед подтверждением запись проверяется чтением назад. AndroidX предпочтения не сохраняются в собственных SharedPreferences вместо реального OEM состояния.

## Сохранено из исходного APK

- Фокус, автоматическая и ручная трапеция, редактор четырёх углов и **их полноэкранные калибровочные изображения**.
- OTA `RuiYunOta`, источники `KuSourceRJ`, термозащита, штатные сервисы пульта и HDMI/CEC.
- Не изменены системные разделы прошивки и другие OEM APK.

## Результат статической проверки

Run `40113864365`: успешно скомпилирован код Java под Android 13, перепакованы OEM smali, проверены бинарный AndroidManifest, девять legacy actions, launcher alias/icon/banner, package/versionCode и цифровая подпись. Для APK 8052 изменены только **2 оригинальных DEX: `classes13.dex` и `classes14.dex`**, добавлен `classes17.dex`. Оригинальные `resources.arsc`, другие DEX и JNI сохранены по данным автоматической проверки.

Это не гарантирует работоспособность на устройстве; runtime classloader, подтверждение применения HAL-команд, работа клавиш пульта и показ ярлыка в конкретном OEM-лаунчере ещё не проверены.

## Оставшиеся задачи до полного отказа от KuSettings

1. Перенос ползунков параметров изображения (яркость картинки, контраст, цвет, резкость и т. п.) и звука (bass/treble) в AOSP Preferences. Штатные контроллеры существуют; нельзя сымитировать их значения без аппаратного API.
2. HDR/MEMC: OEM коды, `picture_list_hdr`, `tv_picture_advance_video_mjc_effect` и значения найдены, но аппаратная поддержка текущей ревизии не подтверждена. Не показывать фиктивный включённый HDR/MEMC.
3. HDMI/CEC/EDID, источники при включении, цифровой зум, положение проектора, питание и системную информацию — связать с однотипным интерфейсом Android TV.
4. Важнее всего: проверить прямые вызовы из OEM launcher, `settingApps`, `services.jar`, `KuSourceRJ.TimeService`, чтобы отказ от KuSettings не оставил скрытых сбоев. **KuSettings пока не отключать.**
5. База 8054 отличается аппаратно: отдельная APK-сборка и контроль ToF/HDMI 2 до установки.

**Не отправлять v7 владельцу проектора как финальную.** Пользователь планирует испытания после завершения всех функций. Это промежуточный, технически проверенный APK.

## Исходники

- [Прямые режимы и звук](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/OemInlinePreferencesHook.java)
- [Пять OEM-ссылок](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/OemRootMenuHook.java)
- [Android TV launcher aliases](../tools/patch_axml_compat_aliases.py)
- [Бинарный патч AOSP MainFragment](../tools/patch_aosp_root_menu_smali.py)
- [Сборка и подпись APK](../.github/workflows/build-tvsettingsplus-inline-v7.yml)
- [Извлечённые звуковые/HDR/CEC ключи](../research/oem-apps/Native_Controllers_8052/extended_keys.json)

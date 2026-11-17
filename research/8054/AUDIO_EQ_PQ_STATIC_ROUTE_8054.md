# Звук, EQ и изображение 8054: точные статические маршруты

Исходники: оригинальные APK из backup ZIP SHA-256 `4f53f2224c98b4c832a19a591b9dd7f925bde28528b5674a09732c0f0e41d362`; TvSettingsPlus.apk SHA-256 `f8e0c6fe0f46e7eb8db651c31d85ef8b32a769b37e4dd0f96ed19a64725d74ee`; TvServices.apk SHA-256 `2934ca9334afd0a2af85786aa0593d27da216086c7eb9b0c9b801f412516b77d`. Воспроизведение: [инвентаризация точных smali callsite](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44121952362), [трассировка тел методов](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44122942100), [трассировка OEM записи и преобразования](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44123938819). Строки ниже относятся к apktool smali 8054, а не к исходникам MOD.

## Пять полос EQ

| OEM Global key | TvServices config ID | `initMapdata` `Map.put` | `initAdvancedAudioSubMap` `Map.put` |
|---|---|---:|---:|
| `sound_equalizer_detail_120hz` | `g_fusion_sound__equalizer_120hz` | 2041–2045 | 1421–1425 |
| `sound_equalizer_detail_500hz` | `g_fusion_sound__equalizer_500hz` | 2050–2054 | 1430–1434 |
| `sound_equalizer_detail_1500hz` | `g_fusion_sound__equalizer_1.5khz` | 2059–2063 | 1439–1443 |
| `sound_equalizer_detail_5000hz` | `g_fusion_sound__equalizer_5khz` | 2068–2072 | 1448–1452 |
| `sound_equalizer_detail_10000hz` | `g_fusion_sound__equalizer_10khz` | 2077–2081 | 1457–1461 |

`TVMenuSettingsService#initSoundStyleMap()V` кладёт те же пять пар в `soundStyleMap` (строки 4013–4053), поэтому смена звукового режима может затрагивать EQ. Точный эффект этого map при смене режима ещё не прослежен. Конструктор `TVMenuSettingsService#<init>()V` содержит все пять ключей в регистрах `v35..v39` (351–359), которые входят в `prefKeys` через `filled-new-array/range {v3 .. v161}` (606–610). `TVMenuSettingsService$1#run()` регистрирует `SettingObserver` для `prefKeys` на `Settings.Global.CONTENT_URI`; `SettingObserver#onChange` получает key, `formatSettingToConfigId` берёт config ID из `itemIds`, а `updateConfigData` в общем маршруте вызывает `TVSettingConfig#setConifg` → `MtkTvConfigBase#setConfigValue(String,int)`. Точные общие вызовы и оговорки о ветвлении описаны в `EDID_GLOBAL_ROUTE_8054.md`. Обратная синхронизация читает config и обновляет Global. Это статическая цепочка, не доказательство записи в DSP на конкретном устройстве.

В OEM `EqualizerDetailFragment#initPreferences()V` вызывает `PreferenceConfigUtils#createProgressDialogPreferenceTrans` для каждого ключа (smali 88–198). `ProgressBarVerticalFragment#onPreferenceChange` вызывает `PreferenceConfigUtils#updatePreferenceChanged` (1255–1257 и другие ветки). `PreferenceConfigUtils#updatePreferenceChanged`, smali 4608–4643, вызывает `onPreferenceValueChange`; если результат не sentinel `-1000`, передаёт `Preference#getKey()` и это целое в `putSettingValueInt`. Для `ProgressDialogPreference` `onPreferenceValueChange`, 3939–3982, преобразует String/Integer в целое, обновляет summary и возвращает то же целое; масштабирования здесь нет. `PreferenceConfigUtils#putSettingValueInt`, 297–329, вызывает `Settings.Global#putInt(resolver,key,value)` на строке 326 и игнорирует boolean результат. Это подтверждено [run 44125184745](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44125184745). `TVMenuSettingsService#reorganizeSettingValueIntToConfigValue`, 4385–4465, имеет особую ветку только для `g_audio__spdif` и возвращает входное целое для EQ config ID. `ProgressBarDialog#getACFGId` содержит EQ ключи и выбор индекса, но само совпадение строки в switch не подтверждает фактический путь записи.

## Соседние настройки

В `TVMenuSettingsService#initSoundStyleMap()V` точные пары: `sound_style → g_audio__sound_mode` (3968–3972), `sound_bass → g_audio__aud_bass` (3986–3990), `sound_treble → g_audio__aud_treble` (3995–3999), `sound_sound_surround → g_audio__aud_surround` (4004–4008). `initMapdata()` регистрирует `sound_style` и каждую полосу EQ в `itemIds`; `sound_bass`, `sound_treble`, `sound_sound_surround` также зарегистрированы. `picture_brightness` присутствует в `itemIds` и профильных picture map. Сопоставление всех режимов, шкал и побочных эффектов ещё не завершено.

## MOD, проверка и откат

`OemFiveBandEqualizerHook` в MOD v33 показывает полосу при наличии OEM `PartnerSettingsConfig` и границ из штатного layout. До исправления отсутствующий Global key подставлялся как `0`, что могло создать ложное подтверждение доступности. Коммит `3f69ae6b9b28658ed3444be218e068c4cc6bf209` требует существующее числовое значение в границах. Обработчик по-прежнему пишет `Settings.Global` и перечитывает **тот же ключ**; это не HAL/DSP readback. Откат к предыдущему исходнику возможен revert этого коммита; на устройстве записей не было.

CI: source-safety [44123469123](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44123469123), OEM capability [44123469415](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44123469415), v33 [44123468813](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44123468813) и exact-8054 APK [44123468782](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44123468782) завершились успешно. APK SHA-256 `fce8b271bf2bb5e7a69f717c71fee16b096799288a27942db33f77faed081034`, artifact ID `14346981499`; APK не устанавливался. Сборка не подтверждает физическую работу EQ.

Неизвестно: границы и возможное масштабирование в `ProgressBarDialog`/нижнем слое, профильный контекст при смене sound mode, достижимость generic observer branch, native/HAL и реальный DSP путь, поведение на динамиках/HDMI ARC. До разрешённого аппаратного теста прямые обращения к Factory GEQ/PEQ и калибровкам не выполнять. План теста: сохранить значения всех пяти Global key и sound mode, проверить отдельно Global и MediaTek config readback, затем аудиосигнал для каждой полосы и каждого выхода; откатить сохранённые значения в обратном порядке и сверить readback. Любая запись на проекторе требует разрешения владельца.


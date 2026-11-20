# Звук 8054: BUILT_UNTESTED

Кодовая оценка звуковой группы a53daf0 — BUILT_UNTESTED; аппаратное состояние RUNTIME_TEST_REQUIRED, не hardware verified. SoundPanelState.initial является только page-local reference baseline, не durable backup/rollback. Новый ручной device/rollback checklist: SOUND_DEVICE_CHECKLIST_8054.md. PR остаётся draft.

Первое изменение звука: `OemSoundValuePolicy` используется прямо в EQ callback. Проверяет существование OEM значения, диапазон из native layout, значение sound_style и совпадение текущего режима с режимом при открытии страницы. Смена режима требует переоткрытия страницы перед записью EQ. Отдельный JVM test покрывает валидное изменение, чужой режим, отсутствие/нечисловое/выходящее за шкалу состояние и неизвестный режим. Это защита устаревшего UI, не обнаружение transient изменения режима и возврата обратно и не DSP readback.

Маршрут Settings.Global → TvServices → config взят из сохранённой групповой карты. Новая декомпиляция не нужна. Далее: выровнять поддержку и ограничения bass/treble/surround по штатному UI, обновлять виджеты после sound mode и проверить групповые значения; eARC оставлять в штатном OEM обработчике до проверки согласованности CEC/API. Аппаратное состояние RUNTIME_TEST_REQUIRED.

## Групповой шаг после review

| UI | Global | MediaTek config | Значения |
|---|---|---|---|
| Режим | sound_style | g_audio__sound_mode | OEM enum 0..6, user=0 |
| Bass | sound_bass | g_audio__aud_bass | 0..100, штатный slider |
| Treble | sound_treble | g_audio__aud_treble | 0..100, штатный slider |
| Surround | sound_sound_surround | g_audio__aud_surround | 0/1 |
| EQ 120/500/1500/5000/10000 Hz | sound_equalizer_detail_* | g_fusion_sound__equalizer_120hz/500hz/1.5khz/5khz/10khz | min/max из native layout, не dB |

Источник пар — settings_routes_8054.json, существующие OEM UI enum/slider specs. Новый общий guard проверяет диапазон и сохранённое состояние для sound_style/bass/treble/surround, а также совпадение sound_style при открытии и перед записью. Те же условия для EQ уже реализованы. После смены sound_style ранее открытые dependent controls отвергают запись до переоткрытия страницы. OEM может ограничивать presets сильнее: это ещё не заявление об актуальности всех виджетов или слышимом эффекте.

Rollback звука: автоматический групповой откат ещё не реализован; до device test сохранить исходные sound_style/bass/treble/surround/EQ и проверить восстановление через штатный OEM обработчик в том же режиме/выходе. Смена режима может изменить набор значений — слепой replay не предлагается. На момент первого guard этапа rollback оставался IN_PROGRESS. Групповое обновление добавлено ниже; кодовая оценка текущего UI BUILT_UNTESTED. Автоматическое восстановление остаётся недоступным, ручная проверка вынесена в SOUND_DEVICE_CHECKLIST_8054.md; следующий source-only milestone — HDMI.

## 2026-11-18: групповое обновление UI

OemSoundPanel владеет только MOD sound category. Команда «Обновить настройки звука» перечитывает режим и девять значений, затем перестраивает только эту группу, заново создавая callbacks с актуальным sound_style. Если режим изменился во время чтения или отсутствует, native controls не создаются; OEM ссылка и повторное обновление остаются. Это явное обновление пользователем, без background observer и произвольной задержки ожидания OEM. Стабильность режима между двумя чтениями не доказывает атомарность всей группы/завершение DSP sync. При необходимости обновление повторяется.

«Исходные значения звука» показывает baseline первого успешного чтения текущей страницы. Повторные refresh/изменения его не заменяют. После уничтожения/нового открытия страницы baseline новый; durable снимок и автоматический replay не обещаны. Звук восстановляется через штатную SOUND_SETTINGS страницу с проверкой режима/выхода и исходными значениями; фактический rollback требует runtime теста. eARC остаётся в OEM UI, нет сырого toggle.

SoundPanelStateTest проверяет обновление текущих значений после смены режима, сохранность baseline при нескольких refresh, отсутствующие ключи, malformed/отсутствующий режим, смену режима между чтениями, immutable snapshots и явную page-local семантику при новом открытии. Android renderer проверяется API33 compile/APK build; D-pad focus после перестройки и реальный OEM переход ещё RUNTIME_TEST_REQUIRED.

Автоматическое восстановление ограничено неизвестным контекстом выхода/профиля. Это открытая зависимость звукового блока; исследование закрытого HAL ради неё не возобновляем. Следующий шаг после проверки этого пакета: аппаратный план ручного rollback в том же режиме/выходе и затем HDMI по готовым источникам HW6/HW7.

## Review checkpoint a53daf0
D-pad focus после removeAll/rebuild, explicit MainActivity SOUND_SETTINGS, работа после смены sound_style и доступность OEM/Refresh при частичном populateSound требуют реального 8054. JVM snapshot tests этого не доказывают. Ограничения динамиков/BT/ARC/eARC и последовательность ручного возврата зафиксированы отдельно в SOUND_DEVICE_CHECKLIST_8054.md. Проверки не выполнялись, разрешения на проектор нет. После документации этих границ следующий пакет — HDMI/EDID/CEC, без повторного закрытого HAL reverse.

## Цифровой формат в точном STOCK 8054

`AudioOutputFragment.getDigitalOutputEntries` сопоставляет `sound_spdif_type` с массивами от `PreferenceConfigUtils.getArrayIdsByCustomer`. `AudioOutputFragment.onPreferenceTreeClick` проверяет `PreferenceExUtils.isSupportSpeakerAndSPDIFFlag`, `getHdmiDetectType(AudioManager)`, `sound_speaker_status` и `sound_advanced_dolby_ap`. При speaker/SPDIF capability, отсутствии обнаруженного HDMI, speaker status 0 и Dolby AP 1 открывается `showVolumeTipsDialog`. Иначе открывается `DigitalOutputFragment`. В `DigitalOutputFragment.onPfTreeClick` выбранное значение из OEM-массива записывается через `PreferenceConfigUtils.putSettingValueInt` по ключу `sound_spdif_type`. Штатный getter возвращает 0 при отсутствии значения. [Трасса точного APK](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44718207615/job/136226904057)

По трассе TvServices `TVMenuSettingsService.itemIds` связывает этот ключ с `g_audio__spdif`. `reorganizeSettingValueIntToConfigValue` дополнительно записывает `external_surround_sound_enabled` и `nrdp_external_surround_sound_enabled` (1 для 4, 5 и 7, иначе 0). Переход через существующий `AudioOutputActivity` сохраняет предупреждение и OEM-массивы. Для нативного редактора нужны доказанный сигнал текущего выхода и подтверждение согласования ключа, config и обоих зависимых флагов при смене формата. На 8054 отдельно проверить динамики, SPDIF, ARC/eARC и появление предупреждения. Сырой автоматический replay связанных настроек не используется.

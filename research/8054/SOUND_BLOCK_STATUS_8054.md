# Звук: начат следующий блок

Изображение закрыто на уровне BUILT_UNTESTED по PICTURE_BLOCK_STATUS_8054.md. Полный откат и аппаратная работа не заявлены.

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

Rollback звука: автоматический групповой откат ещё не реализован; до device test сохранить исходные sound_style/bass/treble/surround/EQ и проверить восстановление через штатный OEM обработчик в том же режиме/выходе. Смена режима может изменить набор значений — слепой replay не предлагается. Статус блока IN_PROGRESS, RUNTIME_TEST_REQUIRED; следующий шаг — групповая актуализация UI, профильные ограничения и проверяемый rollback. HDMI начнётся после завершения этого блока.

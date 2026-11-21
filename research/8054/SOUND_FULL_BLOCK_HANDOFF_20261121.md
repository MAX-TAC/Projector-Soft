# SND-BLOCK-001 — пакетная миграция всего пользовательского блока «Звук» на exact-8054

Дата: 21.11.2026; база v41 PR #2 `fbb41e92...`. **Все SND01–SND13 исследуем одним блоком**, без отдельного PR на полосу эквалайзера. Владелец уже подтвердил, что родная `com.android.tv.settings.partnercustomizer.picture.PictureActivity` даёт рабочие аппаратные ползунки: для **звука** вначале найти аналогичные собственные `com.android.tv.settings` OEM Sound/AudioOutput/Dolby/DTS fragments, прежде чем дублировать backend.

## Истинная цель

Оставить v41 root («Звук», «Эквалайзер», «Аудиовыход», «Обработка звука», «Задержка», «Системные звуки»). Для всех доступных пользовательских функций переиспользовать работающий штатный Android TV OEM editor **без китайского `com.my.settings` UI/Controller**. Если OEM редактора нет — подключить существующий original Java/TVSettingConfig/Settings backend к MOD; не переизобретать HAL/13-band Factory PEQ. Если функция отсутствует на конкретной board capability — не создавать ложный элемент.

## A. Полный список из регистровой матрицы (не выдавать source status за hardware verified)

| ID | Функция | v41 статический статус | Наличие UI | Следующее действие |
|---|---|---|---|---|
| SND01 | Режим звука | WORKING_STATIC | EqualizerSettingsActivity | Сверить прямой Android TV OEM handler, физический output и подтверждение эффекта |
| SND02 | Низкие частоты | IMPLEMENTATION_PENDING | отдельной native страницы нет | Проверить оригинальную пользовательскую доступность и устранить gap |
| SND03 | Высокие частоты | IMPLEMENTATION_PENDING | отдельной native страницы нет | Проверить оригинальную пользовательскую доступность и устранить gap |
| SND04 | Surround | WORKING_STATIC | SoundSettingsActivity | Сверить прямой Android TV OEM handler, физический output и подтверждение эффекта |
| SND05 | Автогромкость | WORKING_STATIC | SoundSettingsActivity | Сверить прямой Android TV OEM handler, физический output и подтверждение эффекта |
| SND06 | Звуки интерфейса | WORKING_STATIC | SoundSettingsActivity | Сверить прямой Android TV OEM handler, физический output и подтверждение эффекта |
| SND07 | 5-полосный EQ | WORKING_STATIC | EqualizerSettingsActivity | Сверить прямой Android TV OEM handler, физический output и подтверждение эффекта |
| SND08 | 13-полосный PEQ | BLOCKED_SAFETY | отдельной native страницы нет | Не открывать заводской/опасный путь до отдельной capability/restore проверки |
| SND09 | Dolby DAP | OEM_SPECIALIZED_EDITOR | SoundSettingsActivity | Сверить штатный независимый Android TV редактор, оставить его native lifecycle |
| SND10 | DTS Loudness | OEM_SPECIALIZED_EDITOR | SoundSettingsActivity | Сверить штатный независимый Android TV редактор, оставить его native lifecycle |
| SND11 | ARC/eARC | BLOCKED_SAFETY | отдельной native страницы нет | Не открывать заводской/опасный путь до отдельной capability/restore проверки |
| SND12 | Задержка аудио | WORKING_STATIC | SoundDelaySettingsActivity | Сверить прямой Android TV OEM handler, физический output и подтверждение эффекта |
| SND13 | Bluetooth Speaker / A2DP sink | IMPLEMENTATION_PENDING | отдельной native страницы нет | Проверить оригинальную пользовательскую доступность и устранить gap |

## B. Подтверждённые оригинальные backend / owner, по которым должен работать весь этап

| Функция | Global / config | Условия и side effects |
|---|---|---|
| Sound mode | `sound_style → g_audio__sound_mode` (OEM enums 0–6, User 0) | Смена профиля может автоматически менять EQ, Bass/Treble и динамические DSP параметры |
| Bass / Treble | `sound_bass → g_audio__aud_bass`, `sound_treble → g_audio__aud_treble` | Следует сверить наличие соответствующего stock slider в оригинальном SoundFragment |
| Surround | `sound_sound_surround → g_audio__aud_surround` | Доступность зависит от OEM profile |
| EQ 5-band | `sound_equalizer_detail_120hz`, `_500hz`, `_1500hz`, `_5000hz`, `_10000hz` → `g_fusion_sound__equalizer_120hz/500hz/1.5khz/5khz/10khz` | В MOD v41 `NativeEqualizerPanel` пишет только для User при Global+config gate; реальный audible/DSP effect ещё не доказан |
| Digital output | `sound_spdif_type → g_audio__spdif`; возможно `sound_digital_output_to_device`, `sound_speaker_status`, `sound_earc` | Значения digital format 4/5/7 дополнительно устанавливают `external_surround_sound_enabled` и `nrdp_external_surround_sound_enabled` в original TvServices |
| SPDIF / Speaker delay | `sound_spdif_delay → g_audio__spdif_delay`, `sound_speaker_delay → g_audio__speaker_delay` | Provider хранит мс в масштабе ×10 относительно MTK config; stock editor шаг 10; speaker при `persist.sys.settings.all=true` и участии в `sound_effects` |
| Dolby/DTS | `partnercustomizer.sound.advanced.AdvancedFragment`, `partnercustomizer.sound.dts.DTSFragment` | Внутренние фрагменты `com.android.tv.settings`; OEM capability gate; не автоматически поддерживаются на каждом аудиовыходе |
| BT A2DP sink | Android BT profile + OEM | Не путать с парой пульта, не отключать HID/AutoPair |
| Factory PEQ13 | Только `Factory.PEQAdjustViewHolder`, 13-band get/set и saveAudioIni | **Не пользовательский 5-band эквалайзер**. Нельзя вскрывать/сохранять заводскую калибровку в пользовательском разделе |

Исходные материалы: [SOUND_BLOCK_STATUS](SOUND_BLOCK_STATUS_8054.md), [EQ route](AUDIO_EQ_PQ_STATIC_ROUTE_8054.md), [Dolby/DTS](AUDIO_ADVANCED_ROUTES_8054.md), [Delay](AUDIO_DELAY_8054.md), [SOUND_DEVICE_CHECKLIST](SOUND_DEVICE_CHECKLIST_8054.md).

## C. Единая диагностика от владельца на телефоне

Скрипт [`tools/collect_8054_audio_termux.sh`](../../tools/collect_8054_audio_termux.sh) запускается в Termux, где уже работает `adb shell`. Снимает **все** `sound_*/audio_*` Global keys, известные селекторы/задержки/EQ, аудиомаршруты из `dumpsys audio`, пакеты/intent/фокус OEM Android TV и KuSettings. Интерактивно запрашивает 6 страниц: основной Android TV Sound, Sound mode/EQ, AudioOutput, Advanced Dolby/DTS, Delay, оригинальная китайская Sound. Можно вводить `s` для отсутствующей страницы. Без автоматических изменений `settings put`, `am start`, `pm disable`, root. На телефоне появляется ZIP в Downloads или HOME Termux.

**Почему одноразового полного settings dump недостаточно:** один глобальный `sound_style` не доказывает исходный SoundFragment controller, доступность Dolby/DTS, физический ARC/eARC или независимость китайского `com.my.settings`; каждый экран надо совместить с `mCurrentFocus`.

## D. План кода после одного ZIP

1. По снимкам определить реальные `com.android.tv.settings` Sound/AudioOutput/EQ/Dolby/DTS Activities и штатный путь изменения, а также потребительские пункты, которых нет в v41.
2. **Одним PR по всему звуку:** переиспользовать уже рабочие штатные Sound/AudioOutput/Dolby/DTS фрагменты для контролов, в которых есть нативные OEM callbacks, оставив 8-раздельную оболочку v41. Точно сохранить диалоги/диапазоны/единицы.
3. Для Sound mode, Bass/Treble, EQ, Surround, Audio Delay, Digital Output сопоставить конкретный stock handler с `NativeEqualizerPanel`/`OemSoundPanel`/`OemSoundDelayPanel`; не добавлять redundant Android UI и не выполнять raw `Settings.Global` toggle вместо сложной OEM операции (eARC и Dolby).
4. Выполнить AndroidX/JVM/Exact-8054 APK CI. Проверить, что любой user path не запускает `com.my.settings`, но пакет пока не отключать из-за фоновых служб.
5. Отдельный разрешённый пользователем **один контролируемый sound-block аппаратный сеанс**: сохранить полный baseline в том же input/output, переключить User EQ на один шаг, проверить audible effect и readback, вернуть значения; проверить профили, Dolby/DTS и выходы только при наличии устройств и готовом plan rollback. Не проводить запись в этом диагностическом этапе.

## E. Статус и ограничения

Данный документ — статический план + pending owner diagnostics. **Настройки на 8054 не менялись**. Успех shell CI доказывает только скрипт/ZIP, не звук и не audio DSP. Вопросы про аппаратный маршрут и поддерживаемые выходы остаются открыты до файла от владельца.

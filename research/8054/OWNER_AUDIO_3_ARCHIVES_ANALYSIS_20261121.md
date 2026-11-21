# Реальные данные 8054: объединённый анализ трёх ADB-аудиоархивов — 21.11.2026

**Источник:** три ZIP, добровольно загруженные владельцем в беседу: `Projector_8054_AUDIO_20261121_125218.zip`, `...125334.zip`, `...125832.zip`. Это owner device observations, **не** заменяющий CI и не готовность к установке MOD. Оригинальные архивы с деталями Bluetooth не копировались в публичный GitHub; здесь приведена только обезличенная сводка.

## 1. Идентичность и доступные интерфейсы

- На устройстве `ro.rjpj.machine.id=8054`, Android 13/API33, `ro.rjpj.hdmi.port.count=2`, stock `com.android.tv.settings` APK расположен в `/system_ext/priv-app/TvSettingsPlus/TvSettingsPlus.apk`.
- `android.settings.SOUND_SETTINGS` разрешается в `com.android.tv.settings/.uicustom.MainActivity` — это **implicit Intent**, а не фактический экран при ручном входе через Settings.
- **Реальные foreground Activities:** `com.android.tv.settings/.device.sound.SoundActivity`, `com.android.tv.settings/.device.sound.AudioOutputActivity`, `com.android.tv.settings/.partnercustomizer.utils.ProgressDialogActivity` (нижний OEM редактор) и `mediatek.factorymenu.ui/mediatek.tvsetting.factory.ui.designmenu.DesignMenuActivity`, `...FactoryDolbyActivity`.
- В первом ZIP **все семь контрольных снимков 10–16** пришлись на MediaTek `DesignMenuActivity`; во втором ZIP на 10–12 был `SoundActivity`, на 13 `FactoryDolbyActivity`, на 14–16 `ProgressDialogActivity`. В третьем ZIP на 10–11 был `SoundActivity`, на 12 **`AudioOutputActivity`**, на 13 **`FactoryDolbyActivity`**, на 14–16 `ProgressDialogActivity`. Поэтому по имени снимка **нельзя утверждать**, что page 15 представлял китайский KuSettings Sound: фактически это Android OEM ProgressDialog.
- Перечисленные Android TV Sound/AudioOutput/ProgressDialog компоненты **не находятся в `com.my.settings`**. Это сильное основание для стратегии reuse уже имеющихся штатных обработчиков внутри MOD v41 без создания второго эквалайзера/цифрового формата.

## 2. Согласованные Global audio values (на всех capture)

| Ключ | Значение | Интерпретация |
|---|---|---|
| `sound_style` | `1` | Исходный OEM sound profile, по source registry стандартный; **не User**, для EQ mode-dependent |
| `sound_bass`, `sound_treble`, `sound_sound_surround` | `0`, `0`, `0` | Stored values, не DSP proof |
| `sound_equalizer_detail_120hz`, `_500hz`, `_1500hz`, `_5000hz`, `_10000hz` | `20`, `11`, `6`, `8`, `30` | OEM EQ values в Global, не напрямую dB, активность при sound_style=1 не установлена |
| `sound_spdif_type`, `sound_digital_output` | `7`, `7` | Stored digital format codes. Без OEM entries **не переводить цифру 7 в известный стандарт/кодек** |
| `sound_digital_output_to_device` | `0` | Stored route selector, физический активный route проверять через AudioPolicy |
| `sound_spdif_delay`, `sound_speaker_delay` | `0`, `0` | Stored values; original MediaTek config delay has ×10 relation |
| `sound_advanced_dolby_ap`, `sound_advanced_dolby_atmos` | `0`, `0` | Stored flags, не доказательство наличия или отсутствия лицензии |
| `sound_earc`, `sound_speaker_status` | `null`, `null` | **Ключи отсутствуют в Global**. Не приравнивать null к 0 либо отсутствию железной поддержки |
| `external_surround_sound_enabled`, `nrdp_external_surround_sound_enabled` | `1`, `1` | По исходному TvServices код цифрового формата `7` может выставлять эти флаги. Не означает работающий Dolby decode |
| `sound_effects_enabled` (Settings.System) | `0` | Системные звуки UI отключены |
| `persist.sys.settings.all` | Пустая property | Не `true`: штатный SoundFragment может скрывать speaker delay |

**Между тремя ZIP не выявлено изменения перечисленных Global values**. Диагностические скрипты ничего не писали.

## 3. Физический маршрут и HDMI ARC

`dumpsys audio / media.audio_policy` на момент сбора показывает **два доступных output устройства**: `Speaker` и подключённый `Bluetooth A2DP` (в реальных логах присутствует названная владельцем/устройством BT-звуковая панель). В policy встречаются selected BT A2DP и speaker; **нельзя считать** DSP/EQ MediaTek одинаковым на обоих выходах. Данные не дают доказательства, что все потоки только на одном конкретном выходе.

`dumpsys hdmi_control`:
- `mIsCecAvailable: false` при `hdmi_cec_enabled: 1` — хранимый CEC toggle не равен активному CEC соединению.
- `HDMI_IN port_id 2: arc=true`, остальные показанные HDMI_IN ports `arc=false` по service; необходима отдельная связь с **физическим** HDMI на D11. Не выводить eARC из одного этого поля.
- `mArcEstablished: false`, `mSystemAudioActivated: false`. **Активный ARC не установлен** на этом снимке.

## 4. Dolby: потребительская настройка не показана, но заводская страница есть

По прямому наблюдению владельца **нет опции включить Dolby** ни в китайском пользовательском меню, ни в Android TV пользовательском меню. При этом в MediaTek инженерном приложении реально открывается `mediatek.tvsetting.factory.ui.designmenu.FactoryDolbyActivity` (есть в двух отдельных ZIP).

Это **не** доказывает, что безопасно активировать Dolby в потребительском MOD: Factory UI может управлять DSP calibration, output mapping, лицензированием, test modes или системными флагами. `sound_advanced_dolby_ap=0`, `sound_advanced_dolby_atmos=0` не отвечают, есть ли рабочий decode/output. Без точного экрана, OEM callback, capability и restore **не создавать переключатель** Dolby или изменение Factory value в обычном UI.

Статически в `OemAdvancedSoundRoutes` уже есть stock `AdvancedFragment`/ `DTSFragment` capability gates; если OEM пользовательский SoundFragment скрывает их на 8054, MOD должен учитывать эту скрытую доступность. 13-band PEQ/GEQ остаётся **Factory-only** до самостоятельного consumer support доказательства.

## 5. Решение для всей группы SND01–SND13

**Пересмотреть реализацию звука одним целым вертикальным PR**, пользуясь `com.android.tv.settings/.device.sound.SoundActivity`, `/.device.sound.AudioOutputActivity`, оригинальным `ProgressDialogActivity`, и доступными штатными `SoundFragment`, `EqualizerDetailFragment`, `DigitalOutputFragment`, `AdvancedFragment`/ `DTSFragment` при доказанной доступности. Не дублировать действующие OEM callback, min/max, side effects и profile-dependent запреты.

Что ещё непроверено: реальный экран User EQ vs Standard, audible effect на speaker/BT, Dolby factory capability, настоящие списки цифровых форматов, delayed speaker control, зависимость `com.my.settings` только от GUI vs background, конкретное matching основного китайского Sound Activity.

## 6. Следующий безопасный owner test: один проход по инженерному Audio

Для сбора фактов создан [`tools/collect_8054_factory_audio_termux.sh`](../../tools/collect_8054_factory_audio_termux.sh) в `feature/8054-sound-batch-audit-20261121`. Пользователь **сам** открывает Factory → Audio root и каждый видимый подраздел (Dolby / DTS / PEQ / GEQ / Audio Output / Calibration / etc.). Скрипт по Enter записывает `mCurrentFocus`, отфильтрованные `sound_*`/`audio_*` значения и **скриншот PNG**; на `q` формирует ZIP. Этот сбор не требует подтверждения числовых изменений — **не нажимать Save/Write/Reset, не включать Dolby, не менять PEQ/factory gain/filter/Q**.

**Не запускать повторно шесть checkpoint звуковой диагностики:** три первоначальных архива сохранены и проанализированы.

Общий статус звукового модуля: `STOCK_UI_OBSERVED / FACTORY_DOLBY_UI_OBSERVED / MOD_AUDIO_REUSE_PROPOSED / HARDWARE_EFFECT_UNVERIFIED`.

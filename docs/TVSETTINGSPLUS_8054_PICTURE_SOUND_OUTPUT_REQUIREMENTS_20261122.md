# TvSettingsPlus MOD 8054 — утверждённые требования к изображению, звуку и выбору выхода

**Дата решения: 22.11.2026.** Автор требований: владелец устройства. **Статус: согласованная продуктовая спецификация**, а не подтверждение выполнения в APK/реальной аппаратуре. Применимость: заводская RUIOS 8054 (2×HDMI, Android 13), приложение com.android.tv.settings (TvSettingsPlus MOD). Следующее действие — исследования и код с обязательной аппаратной проверкой. Не менять Factory Menu, таблицы заводской калибровки, драйверы и уникальные ключи.

## 1. Изображение — согласованная модель

1. Пользователь может выбрать доступный OEM-пресет изображения: Кино, Стандартный, Яркий и другие, реально поддержанные 8054/типом сигнала (имена/индексы брать из stock OEM ресурсов + runtime capability, не брать все generic Factory resource modes).
2. Если выбран **любой заводской** пресет: все относящиеся к изображению регуляторы остаются **видимыми и отражают текущее прочитанное значение**, но **read-only** и не допускают запись, включая обход через нижний popup, D-pad, программный callback и прямой вызов адаптера. Запрет реализуется в UI **и** write-policy, а не только disabled widget.
3. Если выбран ровно OEM-пресет **«Пользовательский» (User)**: разрешить штатные пользовательские ползунки яркости, контраста, насыщенности, оттенка, резкости и доступные OEM регулировки RGB/температуры в пределах capability и безопасного input/profile/HDR scope. Признак User брать из реального OEM enum / resource mapping, а не определять по русской строке.
4. Вернуть **нижние горизонтальные полосы регулировки** из оригинального TvSettingsPlus (stock ProgressDialogPreference / partner_progressbar_dialog.xml), поверх текущей картинки, LEFT/RIGHT для изменения и BACK для закрытия, корректное D-pad focus. При заводском пресете их можно либо открыть только как read-only, либо не открывать, но видимые значения остаются всегда.
5. **Не делать автоматического перехода** из заводского пресета в User при попытке записи, не копировать заводские значения и не вызывать Factory setPictureModeDefaultCfg, NonLinear PQ или WB calibration setters. Требование приоритетно над предложенными ранее вариантами copy-on-edit.
6. При выборе пресета не выполнять отдельную серию putInt для всех ползунков. Сменить режим штатным consumer route (\`picture_mode\`) → дождаться OEM backend → перечитать все связанные значения в user UI. Потеря актуальности режима или источника — отказ записи.
7. Пока **не внедрять глобальное выравнивание изображения** Android HOME / HDMI1 / HDMI2, не делать mass replay на каждом переключении входа. Штатное поведение MediaTek сохраняется; изменения вводить только если физический тест подтвердит реальную проблему. SDR/HDR/DV контексты не смешивать.
8. Разрешить выбор пресета и показ его значений независимо от блокировки; другие независимые переключатели (HDR, MEMC) рассматривать по штатным ограничениям, не применять к ним автоматическую блокировку «только User» без OEM gating.

### Существующие подтверждения
- Stock Android TV PictureFragment → \`Settings.Global\` → TVMenuSettingsService → TVSettingConfig → MediaTek; profile switch asynchronously syncs dependent values. Подробнее: [PICTURE_BLOCK_STATUS_8054](../research/8054/PICTURE_BLOCK_STATUS_8054.md).
- Current MOD NativePicturePanel and original stock lower slider layout exist. Device test and per-source inputGroup scope remain unconfirmed.

## 2. Звук — согласованная модель

1. Показывать поддержанные заводские пресеты звука и их **читаемые пользовательские значения**, однако не допускать изменения bass/treble/Surround/5-band EQ в любом заводском режиме. Не пытаться извлекать/подделывать внутреннюю заводскую DSP-кривую, если пользовательские 5 Global keys не описывают её полностью; выводить лишь подтверждённые доступные параметры.
2. Только для OEM \`sound_style == User\` разрешить редактирование пользовательского EQ и регуляторов, после проверки возможностей маршрута. Для данного 8054 User в штатном enum отмечен индексом \`0\`, но код должен сверять stock support.
3. **Не копировать** заводской звуковой пресет в User и **не менять пресет автоматически** при первом касании ползунка.
4. Отдельные EQ-пресеты **на каждый аудиовыход пока не обязательны**. Допускается единый профиль, если есть доказанная обработка активного PCM тракта.
5. Не утверждать, что OEM DSP EQ действует на Bluetooth A2DP/HDMI ARC/eARC: владелец на живом 8054 подтвердил эффект на динамиках и отсутствие эффекта на Bluetooth-саундбаре. Это требует отдельной аудиоэффектной стратегии.
6. 13-band factory PEQ (MtkTvFApiAudio; saveAudioIni) **не использовать как пользовательский редактор** до доказательства safe per-user storage, обратимости, аудиомаршрута и отсутствия записи factory constants. Предпочтение stock 5-band EQ; альтернативы — системные Android AudioEffect либо интеграция отдельного проверенного эквалайзера.
7. При невозможности сделать общий EQ через существующую MediaTek DSP разрешается **отдельный пункт «Расширенный эквалайзер»**, открывающий **установленный пользователем** сторонний эквалайзер через проверенный exported launcher Intent. Отсутствует приложение — показать инструкцию по установке/поддержке; **никаких скрытых API, автоматической установки и обещаний глобального эффекта**. Проверить Android TV D-pad/focus/плееры/BT/eARC. Не использовать коммерческие APK без лицензии.

## 3. Аудиовыход — наиболее приоритетная новая функция

Встроить в меню «Звук» **«Устройство вывода»**, где перечисляются **реально доступные и разрешённые для медиа-аудио** выходы:
- **Встроенные динамики**, если есть физически и представлены в Android Audio Policy;
- **Bluetooth-саундбар**, с фактическим именем подключённого A2DP sink и значком Bluetooth; сопряжение ≠ подключение ≠ текущий output;
- **HDMI ARC/eARC**, если объявлен/разрешён конкретным MediaTek HAL и подтверждено реальное состояние handshake/audio route; название подключённого AVR/саундбара — только если оно действительно доступно через HDMI CEC/OEM.

Смена выбора:
- Не отключает/удаляет сопряжённый BT девайс и не требует дисконнекта саундбара; переключение на внутренние динамики должно быть возможно при сохранённом BT connection **если платформа допускает**.
- Не вмешивается в system alarms/calls отдельно от выбранной media strategy, если нет понятной OS политики.
- Предварительно проверяет capability и разрешения системного приложения. Android 13 media policy system APIs (AudioManager.getDevices(GET_DEVICES_OUTPUTS), getDevicesForAttributes(USAGE_MEDIA), AudioProductStrategy, setPreferredDevicesForStrategy) требуют \`MODIFY_AUDIO_ROUTING\` для write. \`setCommunicationDevice()\` — **не является** управлением обычным media output.
- Аудиовыход выбирается из устройств, которые audio policy действительно разрешает, не из всех имён Bluetooth bonded devices. Нужна разница \`connected / routed / preferred / available\`.
- После маршрутизации проверяет не только return boolean, но и \`AudioService\`/\`AudioPolicy\` readback, выход на реальном устройстве и сохранение/откат. При отказе сообщает причину и не меняет другие audio/CEC/eARC параметры.
- eARC может быть реализован как OEM proprietary TV audio route, а не обычное аудиоустройство в \`AudioManager.getDevices\`; **не обещать** унифицированный selector без проверки. eARC/CEC побочные действия должны учитывать текущие настройки и быть обратимыми, без raw setter только из-за наличия названия.
- Не включать автоматическую установку/настройку привилегий без OEM approval; bootloader unlocked не даёт \`MODIFY_AUDIO_ROUTING\` автоматически.

## 4. Что нам уже известно, а что ещё не проверено

| Возможность | Статус |
|---|---|
| Блокировка write у заводских пресетов изображения/звука (UI + backend guard) | **DESIGN_ACCEPTED**; тесты/реализация по новой политике требуются |
| Нижний OEM Picture slider | **EXISTING_STOCK_RESOURCE**, аппаратный D-pad/overlay ещё не проверен |
| Пользовательский 5-band EQ | **SOFTWARE_CHAIN_TRACED**, применение на BT/eARC не подтверждено |
| Список выходных устройств/имён | **AOSP API EXISTS**, проверка OEM permisssions / actual audio policy обязательна |
| Смена медиа-маршрута BT ↔ Speakers при активном A2DP | **UNVERIFIED_ON_8054**, преимущественное исследование |
| HDMI ARC/eARC как управляемый конечный вывод | **UNVERIFIED_ON_8054**, может потребоваться OEM-CEC аудио route |
| Единый EQ для всех маршрутов, в том числе YouTube/HDMI output | **NOT PROVEN**; audio effects/bypass/direct/offload зависит от DSP/Android HAL |
| Физический эффект профилей на входах HDMI | **NOT TESTED**, пользователь не требует менять scope до проверки |

## 5. План проверки, не меняющий projector

### Phase 1: stock firmware read-only
- Проверить \`com.android.tv.settings\` Manifest declared permissions и privileged grant XML для \`MODIFY_AUDIO_ROUTING\`, \`QUERY_AUDIO_STATE\`; не делать вывод из одной подписи APK.
- Разобрать \`vendor/etc/audio_policy_configuration.xml\` и включённые XML: devicePorts, mixPorts, routes, HDMI/SPDIF/ARC/eARC, speaker, A2DP provider. Android TV Audio Policy framework может получать BT endpoints динамически, а не перечислять их в vendor XML.
- Проверить доступные stock OEM controllers \`AudioOutputFragment\`, \`DigitalOutputFragment\`, \`SourceSubMenuController\` и \`sound_speaker_status\`, \`sound_earc\`; найти только доказанные user path, не экспериментировать с factory.
- После этого спроектировать native adapter и JVM tests (mock AudioManager/strategy/permission/route-change), без записи на устройство.

### Phase 2: owner-permitted live read-only ADB

**С подключёнными обоими саундбарами** (BT + HDMI eARC), не переключая существующий маршрут и не изменяя настройки:

\`\`\`bat
adb shell getprop ro.rjpj.machine.id
adb shell dumpsys audio > "%USERPROFILE%\Downloads\8054_audio.txt"
adb shell dumpsys media.audio_policy > "%USERPROFILE%\Downloads\8054_audio_policy.txt"
adb shell dumpsys media.audio_flinger > "%USERPROFILE%\Downloads\8054_audio_flinger.txt"
adb shell dumpsys package com.android.tv.settings > "%USERPROFILE%\Downloads\8054_tvsettings_package.txt"
adb shell dumpsys hdmi_control > "%USERPROFILE%\Downloads\8054_hdmi_control.txt"
\`\`\`

Обратить внимание на **granted: true** для permission, доступные output устройства и фактическую Android media strategy, текущие BT route, HDMI ARC/eARC and active CEC. Live dumps могут содержать BT MAC/профили/идентификаторы — не выкладывать без локальной проверки.

### Phase 3: one reversible routing test
- Только после baseline и подтверждённой rollback процедуры; выбрать media output штатным интерфейсом или ограниченным debug adapter, проверить sound output и OS route; восстановить baseline.
- Устройство может сохранять BT pairing, но аудиосессии некоторых приложений могут потребовать restart. Никаких \`svc bluetooth disable\`, принудительного \`disconnect\`, \`settings put global\`, \`cmd audio\` непроверенных команд.

### Phase 4: fallback equalizer
- Если OEM DSP не принимает внешний audio route, проверить существующие \`AudioEffect\` цепочки AOSP/MediaTek (только чтение).
- Рассмотреть **Poweramp Equalizer** (возможность per-device presets, advanced player tracking с permission; Android TV D-pad не доказан) и **Wavelet** (device-based profiles, Bluetooth support; совместимость Android TV и системным выводом не доказана), как **необязательные внешние** приложения.
- Главный путь: deep link/launch external EQ if installed; ручная установка; отсутствие вмешательства в system audio policy ради EQ.

### TV-first EQ вариант для удалённого пульта

Для запасного пункта «Расширенный эквалайзер» приоритет исследовательских кандидатов по **удобству управления с Android TV пульта**:

1. **SoundWave TV: Equalizer** (Google Play, `com.soundwave.tv`) — опубликовано описание интерфейса с D-pad и remote support; рекламируется TV EQ, но производитель предупреждает, что HDMI/BT и сторонние приложения могут ограничить эффект. Нужен тест на 8054: <https://play.google.com/store/apps/details?id=com.soundwave.tv>.
2. **Poweramp Equalizer** (`com.maxmpz.equalizer`) — многополосные и параметрические EQ, per-device preset assignments; на Android TV могут быть проблемы с фокусом/пультом, advanced player tracking требует дополнительных разрешений; прямой путь Google Play: <https://play.google.com/store/apps/details?id=com.maxmpz.equalizer>.
3. **Wavelet** (`com.pittvandewitt.wavelet`) — профили по типу/имени аудиоустройства, особенности Bluetooth и совместимости; UI для пульта не подтверждён: <https://play.google.com/store/apps/details?id=com.pittvandewitt.wavelet>.
4. **RootlessJamesDSP** (`me.timschneeberger.rootlessjamesdsp`) — открытый проект, альтернативная глобальная программная DSP, но требует первоначального Shizuku/ADB setup, не рассматривать как пользовательский вариант «просто установить» до проверки: <https://github.com/timschneeb/RootlessJamesDSP>.

Эти ссылки обозначают **кандидатов**, а не доказанную поддержку exact-8054, работы со всеми потоками или HDMI passthrough. Любой launcher внешнего EQ должен проверять наличие приложения/доступного activity, не устанавливать APK автоматически и сохранять D-pad/Back навигацию.

## 6. Acceptance criteria

1. Кино/Стандартный → значения PIC видны, любые edit callbacks немедленно отказывают без единой записи; User → нижний попап, D-pad, корректный readback и возврат; переключение пресета обновляет значения.
2. Звуковые заводские профили → видны реальные пользовательские параметры, write запрещён; User → разрешён 5-band EQ только на проверенном эффектом выходе.
3. Аудиовыход → список dynamically discovered, physical output icon+name, текущее состояние; BT paired/connected по-прежнему остаётся, даже когда выбран Speakers; после возвращения к BT звук звучит там.
4. eARC → доступен только если platform audio policy/OEM действительно поддерживает управляемый маршрут; нельзя ломать HDMI-CEC handshake.
5. Сторонний EQ → явно необязателен, запускается из MOD, D-pad доступен, эффект на BT проверен на том же тестовом фрагменте и проигрывателе. Нет гарантий для passthrough/offload без конкретного теста.
6. Все изменения gated на exact 8054, никаких заводских калибровок/SaveAudioIni, сбросов, замены system APK или flash без подтверждения.

## Source references

- [Verified 8054 HDMI, picture and audio API paths](../research/8054/MTK_DIRECT_CONSUMER_CONTROL_CONTRACT_20261122.md)
- [Sound block implementation](../research/8054/SOUND_BLOCK_STATUS_8054.md)
- [Audio DSP and equalizer static routes](../research/8054/AUDIO_EQ_PQ_STATIC_ROUTE_8054.md)
- [OEM Picture mode/slider scope](../research/8054/PICTURE_BLOCK_STATUS_8054.md)
- [AOSP Android 13 AudioManager: setPreferredDevicesForStrategy requires MODIFY_AUDIO_ROUTING](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android13-release/media/java/android/media/AudioManager.java)
- [Poweramp Equalizer capability statement](https://forum.powerampapp.com/files/category/5-poweramp-equalizer/)
- [Wavelet troubleshooting and per-device profiles](https://github.com/Pittvandewitt/Wavelet/blob/master/docs/Troubleshooting.md)

# 8054 — контракт прямого управления системными функциями из TvSettingsPlus MOD

**Дата:** 22.11.2026. **Стадия:** исследование конкретных пользовательских control paths (static evidence, без записи на реальный проектор). **Никакие изменения Factory UI APK, системного раздела и исходной прошивки не выполнялись.**

## Цель и принцип архитектуры

Пользовательское `TvSettingsPlus MOD` должно непосредственно обращаться к **существующим системным Android/MediaTek backend**, уже используемым заводским `com.android.tv.settings`, инженерным `mediatek.factorymenu.ui` и китайским `com.my.settings`. **Не направлять команды в Factory Menu Activity как в промежуточное звено и не запускать китайский UI.** Factory Menu — peer client MediaTek API; его актуальное состояние при следующем открытии/refresh должно считываться из общего backend, но live UI refresh этого экрана отдельный вопрос.

```text
TvSettingsPlus MOD (наши preference/page/UX)
            |
    Проверенный adapter + capability/permission/gating
            |-------- Android HdmiControlManager / TvInput / TvContract
            |-------- OEM Settings.Global -> TVMenuSettingsService -> TVSettingConfig
            |-------- обоснованный OEM MtkTvConfig/TVNative (только при необходимости)
            |
       OEM Android Services + MediaTek JNI/HAL/SoC
            ^
            |
     Factory.apk / KuSettings.apk / STOCK Settings
       (независимые клиенты, НЕ посредники)
```

Политика каждого адаптера: `probeCapability → readBackend → checkContext(input,signal,profile,output) → validateRequestedValue → applyViaVerifiedConsumerPath → awaitStableReadback → re-readDependentState`, с заранее заданными stop/rollback условиями. Графический label или одна переменная `Settings.Global` ещё не доказывают аппаратного эффекта.

## Подтверждённая карта требуемых пользовательских функций

| Функция | Factory.apk 8054 | Китайское/stock UI 8054 | Реальный backend / различие | Рекомендуемое подключение MOD | Проверка |
|---|---|---|---|---|---|
| **HDMI-CEC master ON/OFF** | В собственном `Factory.apk` не найдено доказанного пользовательского CEC-тумблера; поиск по собственным Factory UI классам и ресурсам не обнаружил соответствующего setter. | **STOCK** `SourceSubMenuController.lambda$initView$12`, `CompatibleApis.setHdmiCecControlEnabled`; китайский отдельный callback пока не трассирован. | `HdmiControlManager.setHdmiCecEnabled` + `Settings.Global.HDMI_CONTROL_ENABLED`; при OFF штатный контроллер также может выключать eARC/Auto Device Off и менять `sound_earc`; при ON может требоваться power confirmation. | Прямой Android/OEM CEC адаптер, НЕ Factory. До полного side effect/permission/rollback: статус без записи, как сейчас. | Проверка фактических CEC команд, eARC/auto-off, Android service readback и OEM consent. |
| **CEC: выключать HDMI-устройства при выключении проектора** | Factory UI путь не доказан. | **STOCK** `SourceSubMenuController.lambda$initView$13` и `SourceFragment.lambda$initView$9`. | `CompatibleApis.setHdmiCecAutoDeviceOffEnabled` → `HdmiControlManager.setTvSendStandbyOnSleep(int)`. | Уже имеется отдельный 8054 adapter `OemCecAutoOff8054` с getter/setter и gated readback. | Пара состояний CEC/auto-off и внешние устройства. |
| **HDMI1/HDMI2** | Factory использует `MtkTvFApiInputSource` и `TvInputSourceManager` для инженерных calibration экранов; это не consumer channel route. | **STOCK** `SourceFragment.switchToHDMI`. | `TvContract.buildChannelUriForPassthroughInput(id)` → `ACTION_VIEW` в `com.mediatek.wwtv.tvcenter`; на 8054 HW6=HDMI1, HW7=HDMI2. | Уже использован `OemHdmiPortInfo`; не заменять на Factory source-switch вызовы. | Физический вход, видео/звук, обратное переключение, UI readback. |
| **Пресеты изображения** | `PictureModeAdjustViewHolder` читает/пишет `g_video__picture_mode` через `MtkTvConfig`. | **STOCK** `PictureModeFragment` и `Settings.Global.picture_mode`; в KuSettings пользовательские sliders пишут связанные Global keys, ручное наблюдение владельца об общей картинке. | `TVMenuSettingsService` связывает provider с `TVSettingConfig / MtkTvConfigBase`; смена профиля инициирует `updateSettingDB` и асинхронную обратную синхронизацию зависимых параметров. | Consumer `picture_mode` setter/stock PreferenceConfigUtils, затем обновить **все** пользовательские sliders; не копировать factory default cfg writer. | Временные события provider/config, profile+source+HDR context, stable readback и эффект. |
| **Brightness/Contrast/Saturation/Hue/Sharpness** | `PictureModeAdjustViewHolder` применяет `MtkTvFApiDisplay.setPictureModeDefaultCfg(enumProfile, enumInput, enumPQType, value)` к заводским **значениям пресета**; `NonLinearAdjustViewHolder` меняет заводские кривые `get/setNonLinearPqCurve`. | **KuSettings** `PictureModeValueSecondaryController$1–3.onValueChanged` → `SettingsUtil.putGlobalValueInt`; **STOCK** `PictureFragment.onPreferenceChange → PreferenceConfigUtils.updatePreferenceChanged → Settings.Global.putInt`. | `TVMenuSettingsService.SettingObserver → updateConfigData → TVSettingConfig.setConifg → MtkTvConfigBase.setConfigValue`. | Consumer Global/stock OEM adapter со штатными bounds, source/mode guards, readback; factory setters **не использовать** для обычных ползунков. | Визуальный эффект, движение обоих UI, видимость HDMI2 и сохранение конкретного профиля. |
| **HDR On/Off** | В ресурсе Factory есть HDR10/HDR10Plus/HLG профили, **но прямой пользовательский HDR ON/OFF в Factory UI этим аудитом не подтверждён**. | **STOCK** `AdvanceSettingsSecondaryController$9/$10.onClick` пишет значения 1/0; MOD имеет `picture_list_hdr` только для чтения. | Consumer `Settings.Global.picture_list_hdr` (точную цепь до VPU и автоматическое HDR mode negotiation ещё предстоит проверить). | Не смешивать user HDR processing toggle с фактическим входным SDR/HDR метаданными. Native switch только после проверки OEM gating и контекста. | Поведение на SDR, HDR10 и смене HDMI, источник фактических video metadata. |
| **Dolby Vision** | Ресурс `str_arr_pic_mode_vals[5,6,13,14]`: Dovi Bright/Dark, Dolby Vision Vivid/Game; `MtkTvConfigTypeBase` содержит `ACFG_MSG_ENTER_DOLBY_VISION`, `ACFG_MSG_QUITE_DOLBY_VISION`, `g_video__vid_dolby_vision_global_dimming_switch`. | **STOCK** `PictureFragment.onResume` обновляет DV UI/доступность. | **Нет подтверждённого доступного consumer "force DV on/off" API на 8054.** Наличие строк не доказывает decoder/EDID/лицензию. | Capability/status диагностика, режим показывать только при подтверждённом DV-входе и OEM support; **не** создавать фиктивный DV toggle. | Проверка EDID source capabilities, текущего video HDR type, native source/HAL и воспроизведения совместимого тестового файла. |
| **MEMC/MJC эффект** | `FactoryMemcActivity`: `MtkTvConfig.getConfigValue("g_video__vid_mjc_effect")`; изменение `setConfigValueForAllElements("g_video__vid_mjc_effect",index)`; reset `setConfigValue("g_video__vid_mjc_effect",0,6)`. Ресурс Off/Low/Middle/High. | **STOCK** `MemcSettingsActivity`/user `partner_mjc.xml`, provider `tv_picture_advance_video_mjc_effect`; MOD пока read-only. | Factory `ForAllElements` potentially broader scope; argument `6` и all-elements семантика не подтверждены для пользовательского переключателя. | Consumer OEM MJC preference/backend, а не factory all-elements или reset; после выбора обновлять состояние. | Проверка режимов изображения, SDR/HDR, Game mode и физического результата. |
| **MFC** | `OtherOptionAdjustViewHolder` читает `MtkTvFApiDisplay.getMfcLevel`, пишет `setMfcLevel`; ресурс Off/Low/Mid/High/Bypass, но factory UI принудительно скрывает строку `text_factory_otheroption_mfc_val` в `findView`. | Отдельная consumer кнопка не доказана. | Не считать автоматически одним и тем же параметром MEMC/MJC. | Отдельное `FACTORY_ONLY / CAPABILITY_UNVERIFIED` исследование. | Разобрать MFC/MJC аппаратный scope; не включать до доказательств. |
| **Формат HDMI / Overscan** | `OverScanAdjustViewHolder`: `MtkTvAVModeBase.setCrntOverscan`, `Customize.setOverScan(inputType,timing,mode,edges)`, `updateOverscanIni`. | **STOCK** `DisplayModeActivity / DisplayModeFragment`, `PreferenceConfigUtils.getSupportScreenModes` и штатный `picture_format`. | Consumer screen mode отличается от factory HDMI PQ/timing calibration. | Использовать уже реализованный штатный `DisplayModeActivity`; не затирать factory Overscan значения. | Проверить сохранение картинки на HDMI1/2 и отсутствие нарушения геометрии. |
| **HDMI Game Mode** | Нет доказанного Factory UI переключателя в новом отчёте. | **STOCK** `SourceSubMenuController.lambda$initView$10` пишет `tv_picture_video_game_mode`. | В ветке ON вызывается `resetTrapezoid()`, отправляющий broadcast `com.rjpj.tcorrection.TCorrectionBroadcastReceiver` с `type=10`. | Только после анализа всех эффектов, включая сброс коррекции; пока неактивный MOD setter. | Read-only исходная геометрия, тест отдельно, подтверждение восстановления. |

## Самые существенные доказательства и ссылки

1. [FACTORY BACKEND CALLS](https://github.com/MAX-TAC/FlixTone_8054_Backup/blob/research/8054-factory-crossapp-audit-20261121/research/8054/mtk_factory_deep/FACTORY_BACKEND_CALLS_8054.csv), [FOCUS METHODS](https://github.com/MAX-TAC/FlixTone_8054_Backup/blob/research/8054-factory-crossapp-audit-20261121/research/8054/mtk_factory_deep/FOCUS_SCREEN_METHODS_8054.json), [ресурсы Factory](https://github.com/MAX-TAC/FlixTone_8054_Backup/blob/research/8054-factory-crossapp-audit-20261121/research/8054/mtk_factory_deep/FACTORY_RESOURCE_LABELS_8054.csv); exact-8054 SHA `2f0e2cbc133c97cb4b1383f7a4576fab78715dae72a8e590deb479a4196e0996`. Первый полный QA: [CI #44948646048](https://github.com/MAX-TAC/FlixTone_8054_Backup/actions/runs/44948646048).
2. [PICTURE_BLOCK_STATUS_8054.md](PICTURE_BLOCK_STATUS_8054.md) — Stock/KuSettings picture sliders, HDR, MEMC, picture mode, Async TvServices mapping.
3. [HDMI_BLOCK_STATUS_8054.md](HDMI_BLOCK_STATUS_8054.md) — exact SourceSubMenuController, CEC, eARC, AutoOff, Game Mode, source input.
4. [HDMI_PICTURE_FORMAT_8054.md](HDMI_PICTURE_FORMAT_8054.md) — Factory Overscan vs consumer DisplayMode.
5. [EDID_GLOBAL_ROUTE_8054.md](EDID_GLOBAL_ROUTE_8054.md) — exact EDID field, Global observer and unknown per-port routing.
6. [USER_SETTINGS_MIGRATION_TRACKER.md](USER_SETTINGS_MIGRATION_TRACKER.md) — реализованное vs read-only и deferred modules.
7. [AOSP HDMI-CEC](https://source.android.com/docs/devices/tv/hdmi-cec) — privileged system/priv-app HDMI-CEC access. `HdmiControlManager` методы на данной прошивке следует проверять в точном Android 13 framework с фактическими permission grants; сама разблокировка загрузчика **не даёт** приложению CEC permissions.

## Исправления предыдущих ошибочных подходов

- Нельзя вызывать `FactoryMenuActivity` для изменения CEC: **доказанного consumer CEC toggle в Factory UI нет**. Для CEC существует системный `HdmiControlService`. Если Factory Menu когда-либо отображает состояние CEC, оно может лишь переоткрывать/читать общий сервис; это нужно отдельно доказать.
- Нельзя копировать `setPictureModeDefaultCfg` из Factory как реализацию пользовательского контраста — эта функция регулирует заводское значение выбранного пресета для выбранного типа входа.
- Нельзя брать 77 Factory `str_arr_pic_mode_vals` и показывать их всем: многие — placeholder/другой формат SDR/DV/HDR10+/HLG; видимые режимы определяет OEM capability для текущего сигнала.
- Нельзя считать `MEMC`, `MJC`, `MFC` идентичными по одному названию. Сравнить exact config ID и аппаратную реакцию.
- Нельзя выполнять `FactoryDolbyActivity` при включении **Dolby Vision**: её код управляет **аудио** `CFG_AUD_DOLBY_CERT_MODE / CMPSS / DRC`.
- Нельзя принимать echoed provider value за подтверждение hardware-эффекта. Native route/readback и тест сигнала обязательны.

## Текущее состояние нашего MOD в прочитанной ветке

- `NativeSections.Cec` уже содержит `OemCecAutoOff8054` — **CEC master отображается только текстом**, изменение главного CEC disabled, Auto Device Off реализован отдельно с readback.
- `OemHdmiCecHook` legacy write разрешён только на старой 8052: для 8054 намеренно выключен из-за сторонних изменений eARC/autooff. **Не снимать gate, не копировать как есть.**
- `NativeSections.Picture/NativePicturePanel`: HDR и MEMC показываются без native-write, базовые ползунки имеют экспериментальный путь и требуют приёмочных тестов для точного input/profile scope; разделы существуют.
- `OemStockUserSettings.installPictureFormat` использует штатную `DisplayModeActivity`.
- `OemHdmiPortInfo` уже проверяет HW6/HW7 и формирует stock passthrough launch вместо Factory source mutation.

## Следующий конкретный план исследования (без отвлечения на full Factory UI dump)

**STAGE A — exact method-by-method tracing:**
1. **Picture Mode (PIC01)**: точная пара Stock/KuSettings callback → `picture_mode`; `TVMenuSettingsService.updateSettingDB` и все изменяемые поля Brightness/Contrast/Sat/Hue/Sharpness/Temp/Gamma/MJC по событию. Показать результат с сохранением profile и input context.
2. **MEMC (PIC12)**: `partner_mjc.xml`, enum values и `Settings.Global.tv_picture_advance_video_mjc_effect` mapping → `g_video__vid_mjc_effect`; нужен точный статический consumer-to-config chain (пока подтверждены endpoints отдельно). Выявить режимы, где отключён Game/VRR/HDR, если есть.
3. **CEC master (HDMI04)**: подтвердить прямой API method signature/permissions + все OEM side effects (EnergyCommonActivity confirm, eARC/AutoOff). Сделать безопасный `8054` adapter с save-original/status/readback, но не активировать write до аппаратной проверки.
4. **HDR (PIC11)**: установить с каким backend связан `picture_list_hdr`, какие условия видимости, SDR/HDR default и реальные режимы. Не путать HDR toggle vs DV present.
5. **Dolby Vision (PIC11 separate)**: static capability chain `PictureModeFragment → TVSettingConfig/MTK` + реальные коды входящего видеосигнала, EDID и HAL. Не обещать DV switch.
6. **Источники (HDMI01/02) и EDID**: использовать подтверждённый `TvContract` input launch, не factory `MtkTvFApiInputSource` calibration route. EDID port scope separately unresolved.
7. **После этого** аналогично проверить отсутствующие в Factory пользовательские переключатели оригинального KuSettings и переносить только проверенные backend с собственной UI оболочкой.

**STAGE B — на реальном 8054 (с согласия владельца, без записи сначала):**
- По одному и тому же снимку собрать активный input HW6/HW7/HOME, SDR/HDR, `picture_mode`, 5 user slider keys, `picture_list_hdr`, `tv_picture_advance_video_mjc_effect`, `dumpsys hdmi_control`, TV Input и MediaTek config read-only, где доступен.
- Снять baseline одновременно в китайском, STOCK и Factory UI без изменения. Сравнить после **одного отдельного согласованного** изменения через штатный consumer UI. Пресет в другом режиме — отдельный тест. Сверить все зависимые параметры и фактическую картинку.
- Не изменять factory preset defaults, PEQ, WB, NonLinear, Overscan, factory reset, ключи, UART, watchdog, motor/fan calibration.
- Для CEC измерить сопутствующие eARC/autooff до и после, т.к. простое обратное переключение CEC может не восстановить их автоматически. Не менять Game Mode в том же тесте.

**STAGE C — реализация:** каждый пункт `BackendContract` с `supports() / read() / apply() / verify() / stateDependencies() / rollbackNotes()`. Версия Android TV UI должна остаться единственным пользовательским входом; OEM сервисы остаются в системе. Сначала модульные тесты/host CI, затем ограниченный APK для аппарата.

## Критерий завершения, а не просто статического наличия метода

Функция готова к пользовательскому MOD только если доказаны (1) точная identity 8054, (2) настраиваемый пользовательский смысл, (3) адресат service и permission, (4) context/source/profile scope, (5) исходное состояние/readback, (6) все side effects, (7) подтверждение hardware effect, (8) понятный manual rollback.

Текущее состояние: **SOURCE_VERIFIED / BACKEND_MAP_PARTIALLY_PROVEN / DEVICE_WRITE_NOT_TESTED**.

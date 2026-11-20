# Карта обработчиков 8054

| Функция | Реальный обработчик | Ограничение и способ проверки |
|---|---|---|
| HDMI 1/2 и статус | OemHdmiPortInfo и Hdmi8054Policy → `TvInputManager`, проверенные board metadata HW6/HW7 и OEM LiveTV passthrough | Статус подключения подтверждается TV Input. Разрешение, частота и HDR для каждого порта ждут подтверждённого индивидуального getter. |
| Auto Off, CEC, EDID | OemCecAutoOff8054 → `HdmiControlManager.setTvSendStandbyOnSleep`; отдельный общий `tv_input_hdmi_edid_version` | Auto Off требует включённого CEC и readback. Основной CEC/eARC могут менять несколько параметров. EDID остаётся общим сохранённым значением. |
| Запуск и autoplay | NativeBootDestination → `persist.trd.source_id`; OemHdmiAutoPlayHook → `persist.rjpj.hdmiplug.autoplay` | Проверка свежего режима до записи и повторное чтение. Power-cycle проверяется отдельно. |
| Проекция | LightSourceBrightnessController / KuSourceRjBrightnessGateway, OemFocusPreferences, OemMoveKeystonePreference; редакторы OemProjectionPanel | Нет прямого PWM/мотора. Штатные проверки зависимостей и точный resolved component обязательны. |
| Picture PIC01–12 | OemPicturePanel → Settings.Global; TvServices → config → MediaTek JNI | Значения читаются со снимком mode/temp/HDR. Запись нельзя связать с подтверждённым входом и активным профилем. |
| Звуковой профиль и EQ | OemSoundPanel / OemFiveBandEqualizerHook → подтверждённые OEM keys → TvServices → MediaTek config | Точный диапазон и runtime capability. Смена профиля асинхронна. Подтверждение Global отделено от DSP readback. |
| Задержки, выход и эффекты | OemSoundDelayPanel → Global и TVSettingConfig; AudioOutputActivity сохраняет штатный цифровой формат и предупреждение; OemAdvancedSoundRoutes проверяет capability | Два отдельных диапазона. Состояние DSP, ARC/eARC и зависимые external surround flags проверяются по активному выходу. |
| Системные звуки | OemKeyToneHook → OEM Constant.SETTINGS_KEY_SOUND_EFFECTS_ENABLED в Settings.System | Один переключатель, readback ключа. |
| Питание и диагностика | OemAcPowerBootHook, штатный TimerSetActivity, OemScreenTimeoutSafetyHook.installStatus и OemThermalTelemetryHook | Статус сна читается без записи. Таймер расписания и countdown отдельны. Термозащита штатная. |

Для аппаратных значений перед записью требуется сохранить исходный контекст и значения; верификация и ручное восстановление проводятся в том же контексте. [Матрица 96 функций](../research/8054/FINAL_FUNCTION_PARITY_8054.csv)

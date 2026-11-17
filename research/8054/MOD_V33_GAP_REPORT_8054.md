# TvSettingsPlus MOD v33 — аудит функций на 8054

Основание: исходники Projector-Soft `ad258ff32a05754eed9b923b3e95bd0ce97f2369`, матрица 96 функций, исторический exact-8054 CI run 43900081008 и текущий exact-8054 CI run 44096540197 с C0/C1/C2 правками. Исторический run фиксирует checkout `ac71758d4dafe873f3193be020a623abd5e8b777`; сравнение до `ad258ff` показывает только два добавленных документа, без изменений исходников.

Статус BUILT_UNTESTED означает: соответствующий Java-класс входил в компиляцию exact-8054 CI run 44096540197 и достижим по статическим ссылкам от smali/Manifest точек интеграции, либо это проверенный в CI Intent alias. Он не доказывает запуск метода, запись в HAL или работу на проекторе. SOURCE_ONLY означает исходник без доказанной функциональной интеграции. STATIC_VERIFIED у SYS09 подтверждает лишь наличие Provision в инвентаре 8054. C0/C1/C2 правки собраны, но не установлены на устройство.

Итог матрицы: BUILT_UNTESTED 54; BLOCKED 10; NOT_IMPLEMENTED 30; SOURCE_ONLY 1; STATIC_VERIFIED 1. HARDWARE_VERIFIED 0.

## 96 функций

| ID | Функция | Текущий MOD (из handoff) | Класс в source | Статус |
|---|---|---|---|---|
| NAV01 | Открытие системных настроек | OemLegacyIntentAudit/aliases частично | OemLegacyIntentAudit | BUILT_UNTESTED |
| NAV02 | Wi-Fi / Ethernet | Intent-alias | — | BUILT_UNTESTED |
| NAV03 | Дата и время | Legacy alias | — | BUILT_UNTESTED |
| NAV04 | Часовой пояс | Legacy alias | — | BUILT_UNTESTED |
| NAV05 | Язык и локаль | Legacy alias | — | BUILT_UNTESTED |
| NAV06 | Список приложений | Legacy alias | — | BUILT_UNTESTED |
| NAV07 | Системный раздел и модель | OemSystemInfoHook: raw OEM ID | OemSystemInfoHook | BUILT_UNTESTED |
| NAV08 | Размер текста | OemFontScaleHook | OemFontScaleHook | BUILT_UNTESTED |
| NAV09 | Сон / заставка / таймаут | OemScreenTimeoutSafetyHook принудительно пишет Never при входе | OemScreenTimeoutSafetyHook | BUILT_UNTESTED |
| NAV10 | Factory reset | OEM link | — | BLOCKED |
| NAV11 | Обновление OEM OTA | OEM delegated links | — | BUILT_UNTESTED |
| NAV12 | Совместимые OEM Intent | OemLegacyIntentAudit | OemLegacyIntentAudit | BUILT_UNTESTED |
| NAV13 | Скриншоты OEM | Не переносится | — | NOT_IMPLEMENTED |
| NAV14 | Модель/диагностика платы | OemSystemInfoHook частично | OemSystemInfoHook | BUILT_UNTESTED |
| PIC01 | Режим изображения | OemInlinePreferencesHook пишет и перечитывает Global | OemInlinePreferencesHook | BUILT_UNTESTED |
| PIC02 | Цветовая температура | OemInlinePreferencesHook пишет и перечитывает Global | OemInlinePreferencesHook | BUILT_UNTESTED |
| PIC03 | Яркость изображения | OemInlineSlidersHook пишет и перечитывает Global | OemInlineSlidersHook | BUILT_UNTESTED |
| PIC04 | Контраст | OemInlineSlidersHook пишет и перечитывает Global | OemInlineSlidersHook | BUILT_UNTESTED |
| PIC05 | Насыщенность | OemInlineSlidersHook пишет и перечитывает Global | OemInlineSlidersHook | BUILT_UNTESTED |
| PIC06 | Резкость | OemInlineSlidersHook пишет и перечитывает Global | OemInlineSlidersHook | BUILT_UNTESTED |
| PIC07 | Оттенок / Hue | OemInlineSlidersHook пишет и перечитывает Global | OemInlineSlidersHook | BUILT_UNTESTED |
| PIC08 | Баланс красного R | OemInlineSlidersHook пишет и перечитывает Global | OemInlineSlidersHook | BUILT_UNTESTED |
| PIC09 | Баланс зелёного G | OemInlineSlidersHook пишет и перечитывает Global | OemInlineSlidersHook | BUILT_UNTESTED |
| PIC10 | Баланс синего B | OemInlineSlidersHook пишет и перечитывает Global | OemInlineSlidersHook | BUILT_UNTESTED |
| PIC11 | HDR | OemInlinePreferencesHook toggle | OemInlinePreferencesHook | BUILT_UNTESTED |
| PIC12 | MEMC/MJC | OemInlinePreferencesHook raw Global | OemInlinePreferencesHook | BUILT_UNTESTED |
| PIC13 | MEMC Demo | Не встроено | — | NOT_IMPLEMENTED |
| PIC14 | Overscan / масштаб входа | Нет | — | NOT_IMPLEMENTED |
| PIC15 | Тестовые картинки | Нет | — | NOT_IMPLEMENTED |
| PIC16 | Non-Linear PQ | Нет | — | BLOCKED |
| PIC17 | White Balance заводской | Нет | — | BLOCKED |
| PIC18 | Panel Info | Нет | — | NOT_IMPLEMENTED |
| SND01 | Режим звука | OemInlinePreferencesHook | OemInlinePreferencesHook | BUILT_UNTESTED |
| SND02 | Низкие частоты | OemInlineSlidersHook | OemInlineSlidersHook | BUILT_UNTESTED |
| SND03 | Высокие частоты | OemInlineSlidersHook | OemInlineSlidersHook | BUILT_UNTESTED |
| SND04 | Surround | OemInlinePreferencesHook | OemInlinePreferencesHook | BUILT_UNTESTED |
| SND05 | Автогромкость | OemInlinePreferencesHook | OemInlinePreferencesHook | BUILT_UNTESTED |
| SND06 | Звуки интерфейса | OemKeyToneHook | OemKeyToneHook | BUILT_UNTESTED |
| SND07 | 5-полосный EQ | OemFiveBandEqualizerHook raw Global | OemFiveBandEqualizerHook | BUILT_UNTESTED |
| SND08 | 13-полосный PEQ | В MOD отсутствует | — | NOT_IMPLEMENTED |
| SND09 | Dolby DAP | Не реализовано | — | NOT_IMPLEMENTED |
| SND10 | DTS Loudness | Не реализовано | — | NOT_IMPLEMENTED |
| SND11 | ARC/eARC | OemEarcDelegatedPreference OEM link | OemEarcDelegatedPreference | BUILT_UNTESTED |
| SND12 | Задержка аудио | Только policy enum (не UI реализация) | — | SOURCE_ONLY |
| SND13 | Bluetooth Speaker / A2DP sink | OemMenuVisibilityHook hides source UI | OemMenuVisibilityHook | BUILT_UNTESTED |
| HDMI01 | Активный источник HDMI 1 | OemRootMenuHook временно открывает OEM source page | OemRootMenuHook | BUILT_UNTESTED |
| HDMI02 | Активный источник HDMI 2 | OemRootMenuHook временно открывает OEM source page | OemRootMenuHook | BUILT_UNTESTED |
| HDMI03 | EDID version | OemHdmiEdidHook fixed 6 | OemHdmiEdidHook | BUILT_UNTESTED |
| HDMI04 | HDMI-CEC | OemHdmiCecHook existing | OemHdmiCecHook | BUILT_UNTESTED |
| HDMI05 | Game mode | OemHardwareDelegatedSettings delegated link | OemHardwareDelegatedSettings | BUILT_UNTESTED |
| HDMI06 | HDMI Plug autoplay | OemHdmiAutoPlayHook | OemHdmiAutoPlayHook | BUILT_UNTESTED |
| HDMI07 | Источник при старте | OemPowerOnSourceHook | OemPowerOnSourceHook | BUILT_UNTESTED |
| HDMI08 | Приложение при старте | OemPowerOnAppHook | OemPowerOnAppHook | BUILT_UNTESTED |
| HDMI09 | USB local media | OEM source link | — | BUILT_UNTESTED |
| HDMI10 | Аналоговые входы VGA/SCART/Tuner | Не переносить | — | BLOCKED |
| PRJ01 | Положение: Front/Rear/Ceiling | OemProjectorOrientationHook | OemProjectorOrientationHook | BUILT_UNTESTED |
| PRJ02 | Автофокус при включении | OEM delegated or not verified | — | NOT_IMPLEMENTED |
| PRJ03 | Автофокус при перемещении | OEM delegated or not verified | — | NOT_IMPLEMENTED |
| PRJ04 | Ручная фокусировка | OemHardwareDelegatedSettings OEM route | OemHardwareDelegatedSettings | BUILT_UNTESTED |
| PRJ05 | Автоматическая трапеция | OEM delegated or not verified | — | NOT_IMPLEMENTED |
| PRJ06 | 4-угольная коррекция | OemHardwareDelegatedSettings OEM route | OemHardwareDelegatedSettings | BUILT_UNTESTED |
| PRJ07 | Горизонтальная/вертикальная трапеция | OemHardwareDelegatedSettings OEM route | OemHardwareDelegatedSettings | BUILT_UNTESTED |
| PRJ08 | Обнаружение препятствия | OEM delegated or not verified | — | NOT_IMPLEMENTED |
| PRJ09 | Автоматическое выравнивание / экран | OEM delegated or not verified | — | NOT_IMPLEMENTED |
| PRJ10 | Цифровой зум | OEM delegated or not verified | — | NOT_IMPLEMENTED |
| PRJ11 | Оптическая яркость LED | LegacyBrightnessUiHook simulates customer=1 route + BrightReceiver | LegacyBrightnessUiHook | BUILT_UNTESTED |
| SYS01 | Питание при подключении сети | OemAcPowerBootHook | OemAcPowerBootHook | BUILT_UNTESTED |
| SYS02 | Автосопряжение Bluetooth пульта | OemBluetoothRemoteHook read-only pairing | OemBluetoothRemoteHook | BUILT_UNTESTED |
| SYS03 | Восстановление термослужбы | KuSourceRecoveryService guarded getRunningServices | KuSourceRecoveryService | BUILT_UNTESTED |
| SYS04 | Температура | OemThermalTelemetryHook only read | OemThermalTelemetryHook | BUILT_UNTESTED |
| SYS05 | Запуск источника после reboot | OemPowerOn* | — | NOT_IMPLEMENTED |
| SYS06 | Восстановление Bluetooth после reboot | Сохраняется | — | BUILT_UNTESTED |
| SYS07 | OEM Timer выключения | OemScreenTimeoutSafetyHook (не timer service) | OemScreenTimeoutSafetyHook | BUILT_UNTESTED |
| SYS08 | Кнопка Settings на пульте | OemLegacyIntentAudit | OemLegacyIntentAudit | BUILT_UNTESTED |
| SYS09 | Установленные/предустановочные приложения | Не изменено | — | STATIC_VERIFIED |
| FAC01 | Глубокая информация о чипе | Не переносится как UI; API исследован | — | NOT_IMPLEMENTED |
| FAC02 | Информация о панели и PQ | Не переносится как UI; API исследован | — | NOT_IMPLEMENTED |
| FAC03 | PEQ/GEQ | Не переносится как UI; API исследован | — | NOT_IMPLEMENTED |
| FAC04 | DTS/Dolby | Не переносится как UI; API исследован | — | NOT_IMPLEMENTED |
| FAC05 | System WatchDog / UART | Не переносится как UI; API исследован | — | BLOCKED |
| FAC06 | Factory PQ update | Не переносится как UI; API исследован | — | BLOCKED |
| FAC07 | Factory Execute Shell | Не переносится как UI; API исследован | — | BLOCKED |
| FAC08 | Установка CI/DRM ключей | Не переносится как UI; API исследован | — | BLOCKED |
| FAC09 | SSC/MIU/LVDS | Не переносится как UI; API исследован | — | BLOCKED |
| FAC10 | Factory Keystone Calib | Не переносится как UI; API исследован | — | BLOCKED |
| DBL01 | AirPlay/DLNA/Chromecast | Out of scope until TvSettings parity | — | NOT_IMPLEMENTED |
| DBL02 | Miracast | Out of scope until TvSettings parity | — | NOT_IMPLEMENTED |
| DBL03 | OEM store | Out of scope until TvSettings parity | — | NOT_IMPLEMENTED |
| DBL04 | OEM launcher | Out of scope until TvSettings parity | — | NOT_IMPLEMENTED |
| DBL05 | BT speaker source | Out of scope until TvSettings parity | — | NOT_IMPLEMENTED |
| DBL06 | Retail/demo | Out of scope until TvSettings parity | — | NOT_IMPLEMENTED |
| DBL07 | Auto OTA poll | Out of scope until TvSettings parity | — | NOT_IMPLEMENTED |
| DBL08 | OEM recommendations | Out of scope until TvSettings parity | — | NOT_IMPLEMENTED |
| BOOT01 | Ранняя надпись FULL HD | Out of scope until TvSettings parity | — | NOT_IMPLEMENTED |
| BOOT02 | Android bootanimation | Out of scope until TvSettings parity | — | NOT_IMPLEMENTED |
| BOOT03 | Стартовый звук | Out of scope until TvSettings parity | — | NOT_IMPLEMENTED |

## 47 Java-классов v33

| Класс | Статическая достижимость | JVM-тест |
|---|---|---|
| BootSourceMapping | Да, от smali/Manifest root | Есть |
| KuSourceRecoveryActivationPolicy | Да, от smali/Manifest root | Есть |
| KuSourceRecoveryBootReceiver | Да, от smali/Manifest root | Нет |
| KuSourceRecoveryPolicy | Да, от smali/Manifest root | Есть |
| KuSourceRecoveryService | Да, от smali/Manifest root | Нет |
| KuSourceRjBrightnessGateway | Да, от smali/Manifest root | Нет |
| LegacyBrightnessUiHook | Да, от smali/Manifest root | Нет |
| LightSourceBrightnessController | Да, от smali/Manifest root | Есть |
| LightSourceModePreferenceView | Не подтверждена | Нет |
| MenuPresentationPolicy | Не подтверждена | Есть |
| OemAcPowerBootHook | Да, от smali/Manifest root | Нет |
| OemAcPowerBootPolicy | Да, от smali/Manifest root | Есть |
| OemBluetoothRemoteHook | Да, от smali/Manifest root | Нет |
| OemBluetoothRemotePolicy | Да, от smali/Manifest root | Есть |
| OemCompatibilityDiagnosticsHook | Да, от smali/Manifest root | Нет |
| OemDelegatedMenuPolicy | Да, от smali/Manifest root | Есть |
| OemEarcDelegatedPreference | Да, от smali/Manifest root | Нет |
| OemEarcPolicy | Не подтверждена | Есть |
| OemFiveBandEqualizerHook | Да, от smali/Manifest root | Нет |
| OemFontScaleHook | Да, от smali/Manifest root | Нет |
| OemFontScalePolicy | Да, от smali/Manifest root | Есть |
| OemHardwareDelegatedSettings | Да, от smali/Manifest root | Нет |
| OemHdmiAutoPlayHook | Да, от smali/Manifest root | Нет |
| OemHdmiCecHook | Да, от smali/Manifest root | Нет |
| OemHdmiEdidHook | Да, от smali/Manifest root | Нет |
| OemInlinePreferencesHook | Да, от smali/Manifest root | Нет |
| OemInlineSlidersHook | Да, от smali/Manifest root | Нет |
| OemKeyToneHook | Да, от smali/Manifest root | Нет |
| OemKeyTonePolicy | Да, от smali/Manifest root | Есть |
| OemKuSourceServiceAudit | Да, от smali/Manifest root | Нет |
| OemKuSourceServicePolicy | Да, от smali/Manifest root | Есть |
| OemLaunchRouteHook | Да, от smali/Manifest root | Нет |
| OemLegacyIntentAudit | Да, от smali/Manifest root | Нет |
| OemLegacyIntentPolicy | Да, от smali/Manifest root | Есть |
| OemMenuVisibilityHook | Да, от smali/Manifest root | Нет |
| OemPowerOnAppHook | Да, от smali/Manifest root | Нет |
| OemPowerOnAppPolicy | Да, от smali/Manifest root | Есть |
| OemPowerOnSourceHook | Да, от smali/Manifest root | Нет |
| OemProjectorOrientationHook | Да, от smali/Manifest root | Нет |
| OemRootMenuHook | Да, от smali/Manifest root | Нет |
| OemScreenTimeoutPolicy | Да, от smali/Manifest root | Есть |
| OemScreenTimeoutSafetyHook | Да, от smali/Manifest root | Нет |
| OemSystemInfoHook | Да, от smali/Manifest root | Нет |
| OemThermalTelemetryHook | Да, от smali/Manifest root | Нет |
| ProjectorCapabilities | Да, от smali/Manifest root | Есть |
| ProjectorCompatibilityPolicy | Да, от smali/Manifest root | Есть |
| ThermalTelemetryPolicy | Да, от smali/Manifest root | Есть |

Новый `EdidPortPolicy.java` добавлен после v33 и скомпилирован exact-8054 CI; его чистая JVM-проверка прошла. Всего 48 Java-классов и 19 JVM-тестовых файлов.

## DEX, Manifest, ресурсы и сборка

- В v33 workflow компилируются все Java-классы через wildcard. Проверки `strings` подтверждают наличие маркеров DEX, но сами по себе не подтверждают вызов.
- Точки smali: `MainFragment#onCreateView` → `OemRootMenuHook.install` и `OemInlinePreferencesHook.install`; `uicustom.MainActivity#onCreate` → `OemLaunchRouteHook.apply`; исходные OEM brightness, source и general контроллеры патчатся отдельными fail-closed скриптами. CI ожидает восемь изменённых smali файлов.
- Manifest меняется скриптом совместимых activity-alias и регистрацией `KuSourceRecoveryBootReceiver`/`KuSourceRecoveryService`. Проверено в историческом CI, но конкретный AXML нового APK не исследован.
- Ресурсы, калибровочные классы и 8054 аппаратная совместимость требуют отдельного diff/repack аудита. В репозитории нет исходного AndroidManifest.xml или ресурсов MOD; за основу берётся оригинальный APK.
- CI run 44093893342 на commit `9fa9b8e` прошёл компиляцию C0/C1, smali patch, тесты, repack и platform test-key signing. Установка поверх stock, системные права/SELinux, реальные HDMI/звук/BT/оптика/термозащита и откат не проверены.

## Главные блокеры

1. `KuSourceRecoveryService` использует `ActivityManager.getRunningServices`; полнота видимости 8054 не доказана. Новая source-only защита запрещает автоматическую замену на 8054.
2. `OemHdmiEdidHook` использует фиксированный HW6 и глобальный `tv_input_hdmi_edid_version`; смысл для HW7 не доказан. Новая source-only защита скрывает этот selector на 8054.
3. Bluetooth-пульт: bonded HID и AOSP AddAccessory не заменяют OEM `CachedBluetoothDevice.connect(true)` и фильтры AutoPair.
4. Picture/sound/EQ: запись Settings.Global не равна подтверждённому MediaTek DSP/PQ readback.
5. Навигация: общие страницы всё ещё открывают отдельную OEM `uicustom.MainActivity`; единая двухпанельная UI не завершена.
6. Exact 8054 APK invoke graph, explicit ComponentName и аппаратные cold boot/rollback испытания отсутствуют.

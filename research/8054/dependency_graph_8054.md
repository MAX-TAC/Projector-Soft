# Карта зависимостей FlixTone 8054 — контроль доказательств

Карта объединяет handoff и выбранные точные smali invoke из оригинальных APK. Имена владельцев/backend в матрице остаются гипотезами; статический вызов не доказывает исполнение на устройстве.

Покрытие инвентаря: 88 APK, 1389 компонентов Manifest, 79 индикаторов DEX-строк, 96 функций.
Статусы: UNKNOWN 72; BLOCKED 10; PARTIAL_STATIC_TRACE 3; DEFERRED 11. Точных статических smali invoke 8054: 32; в Factory отчёте есть 4 прослеженных JADX-вызова.

Пустое exported в Manifest означает неизвестное значение. Каждая DEX-строка имеет статус DEX_STRING_ONLY.

| ID | Функция | Приоритет | Статус | Владелец (гипотеза) | Что проверить |
|---|---|---|---|---|---|
| NAV01 | Открытие системных настроек | P0 | UNKNOWN | KuSettings/com.my.settings + TvSettingsPlus/com.android.tv.settings | Искать explicit intents в 8054 services.jar и launcher |
| NAV02 | Wi-Fi / Ethernet | P1 | UNKNOWN | KuSettings.MyWifiActivity; TvSettingsPlus | Найти реальные explicit вызовы и original 8054 intent |
| NAV03 | Дата и время | P2 | UNKNOWN | KuSettings.DateTimeActivity | Проверить original 8054 intents |
| NAV04 | Часовой пояс | P2 | UNKNOWN | KuSettings.MyZonePicker | Проверить OEM отдельный picker 8054 |
| NAV05 | Язык и локаль | P2 | UNKNOWN | KuSettings.LanguageActivity/LanguageLocalActivity | Сохранить точное написание OEM action, дифф 8054 |
| NAV06 | Список приложений | P2 | UNKNOWN | KuSettings.AllAppActivity | Проверить сторонние вызовы списка |
| NAV07 | Системный раздел и модель | P1 | UNKNOWN | TvSettingsPlus.SystemInfoSecondaryController | Не подменять ro.product.model; зафиксировать только UI branding |
| NAV08 | Размер текста | P2 | UNKNOWN | TvSettingsPlus OEM font | Проверить рабочие диапазоны |
| NAV09 | Сон / заставка / таймаут | P1 | UNKNOWN | TvSettingsPlus / KuSourceRJ / Android | Не путать Android sleep с OEM power; проверить owner/observer |
| NAV10 | Factory reset | BLOCK | BLOCKED | TvSettingsPlus / Factory | Не реализовывать прямую команду |
| NAV11 | Обновление OEM OTA | P2 | UNKNOWN | RuiYunOta | Отделить auto jobs/receivers от ручной установки; USB cold boot не подтверждён |
| NAV12 | Совместимые OEM Intent | P0 | UNKNOWN | KuSettings 9 actions + TvSettingsPlus | Проверить явные ComponentName в 8054 |
| NAV13 | Скриншоты OEM | P2 | UNKNOWN | KuSettings.ScreenServices | Проверить 8054 triggers, отсутствие внешнего потребителя |
| NAV14 | Модель/диагностика платы | P2 | UNKNOWN | Factory.InfoViewHolder; Build | Различать аппаратную панель 1080p/4K decoder |
| PIC01 | Режим изображения | P1 | UNKNOWN | TvSettingsPlus.PictureSubMenuController/PictureModeValueSecondaryController/ColorTempValueSecondaryController | Подтвердить observer -> HAL на конкретной 8054; выяснить RGB ключи/границы/профиль |
| PIC02 | Цветовая температура | P1 | UNKNOWN | TvSettingsPlus.PictureSubMenuController/PictureModeValueSecondaryController/ColorTempValueSecondaryController | Подтвердить observer -> HAL на конкретной 8054; выяснить RGB ключи/границы/профиль |
| PIC03 | Яркость изображения | P1 | UNKNOWN | TvSettingsPlus.PictureSubMenuController/PictureModeValueSecondaryController/ColorTempValueSecondaryController | Подтвердить observer -> HAL на конкретной 8054; выяснить RGB ключи/границы/профиль |
| PIC04 | Контраст | P1 | UNKNOWN | TvSettingsPlus.PictureSubMenuController/PictureModeValueSecondaryController/ColorTempValueSecondaryController | Подтвердить observer -> HAL на конкретной 8054; выяснить RGB ключи/границы/профиль |
| PIC05 | Насыщенность | P1 | UNKNOWN | TvSettingsPlus.PictureSubMenuController/PictureModeValueSecondaryController/ColorTempValueSecondaryController | Подтвердить observer -> HAL на конкретной 8054; выяснить RGB ключи/границы/профиль |
| PIC06 | Резкость | P1 | UNKNOWN | TvSettingsPlus.PictureSubMenuController/PictureModeValueSecondaryController/ColorTempValueSecondaryController | Подтвердить observer -> HAL на конкретной 8054; выяснить RGB ключи/границы/профиль |
| PIC07 | Оттенок / Hue | P1 | UNKNOWN | TvSettingsPlus.PictureSubMenuController/PictureModeValueSecondaryController/ColorTempValueSecondaryController | Подтвердить observer -> HAL на конкретной 8054; выяснить RGB ключи/границы/профиль |
| PIC08 | Баланс красного R | P1 | UNKNOWN | TvSettingsPlus.PictureSubMenuController/PictureModeValueSecondaryController/ColorTempValueSecondaryController | Подтвердить observer -> HAL на конкретной 8054; выяснить RGB ключи/границы/профиль |
| PIC09 | Баланс зелёного G | P1 | UNKNOWN | TvSettingsPlus.PictureSubMenuController/PictureModeValueSecondaryController/ColorTempValueSecondaryController | Подтвердить observer -> HAL на конкретной 8054; выяснить RGB ключи/границы/профиль |
| PIC10 | Баланс синего B | P1 | UNKNOWN | TvSettingsPlus.PictureSubMenuController/PictureModeValueSecondaryController/ColorTempValueSecondaryController | Подтвердить observer -> HAL на конкретной 8054; выяснить RGB ключи/границы/профиль |
| PIC11 | HDR | P1 | UNKNOWN | AdvanceSettingsSecondaryController | Проверить реальный output HDR и MediaTek path |
| PIC12 | MEMC/MJC | P1 | UNKNOWN | FactoryMemcActivity, AdvanceSettingsSecondaryController | Установить взаимосвязь двух ключей и результат на 8054 |
| PIC13 | MEMC Demo | P3 | UNKNOWN | AdvanceSettingsSecondaryController | Доказать support, убрать если не работает |
| PIC14 | Overscan / масштаб входа | P2 | UNKNOWN | Factory.OverScanAdjustViewHolder | Различать factory calibration и user per-HDMI crop |
| PIC15 | Тестовые картинки | P3 | UNKNOWN | Factory.TestPatternViewHolder / TestImagePatternsActivity | Разработать watchdog восстановления паттерна |
| PIC16 | Non-Linear PQ | BLOCK | BLOCKED | Factory.NonLinearAdjustViewHolder | Не записывать calibration |
| PIC17 | White Balance заводской | BLOCK | BLOCKED | Factory.WBAdjustViewHolder | Не заменять RGB ползунок Factory WB API |
| PIC18 | Panel Info | P2 | UNKNOWN | Factory.PanelInfoViewHolder | Не трактовать как оптическое разрешение |
| SND01 | Режим звука | P1 | UNKNOWN | TvSettingsPlus.SoundSubMenuController / SoundModeValueSecondaryController | Проследить original handler Android 8054 -> HAL/observer, enum inversion |
| SND02 | Низкие частоты | P1 | UNKNOWN | TvSettingsPlus.SoundSubMenuController / SoundModeValueSecondaryController | Проследить original handler Android 8054 -> HAL/observer, enum inversion |
| SND03 | Высокие частоты | P1 | UNKNOWN | TvSettingsPlus.SoundSubMenuController / SoundModeValueSecondaryController | Проследить original handler Android 8054 -> HAL/observer, enum inversion |
| SND04 | Surround | P1 | UNKNOWN | TvSettingsPlus.SoundSubMenuController / SoundModeValueSecondaryController | Проследить original handler Android 8054 -> HAL/observer, enum inversion |
| SND05 | Автогромкость | P1 | UNKNOWN | TvSettingsPlus.SoundSubMenuController / SoundModeValueSecondaryController | Проследить original handler Android 8054 -> HAL/observer, enum inversion |
| SND06 | Звуки интерфейса | P1 | UNKNOWN | TvSettingsPlus.SoundSubMenuController / SoundModeValueSecondaryController | Проследить original handler Android 8054 -> HAL/observer, enum inversion |
| SND07 | 5-полосный EQ | P1 | UNKNOWN | TvSettingsPlus.EqualizerDetailFragment (partner) + Factory GEQ | Проверить фактический DSP, масштабы ползунков; заводской GEQ не автоматически равен Global |
| SND08 | 13-полосный PEQ | P2 | UNKNOWN | Factory.PEQAdjustViewHolder | Масштаб gain/Q, влияние sound mode, hardware support, выходы |
| SND09 | Dolby DAP | P3 | UNKNOWN | Factory.AdvancedEffectDapViewHolder | По фото DAP OFF, реальная поддержка неизвестна |
| SND10 | DTS Loudness | P3 | UNKNOWN | Factory.FactoryDTSActivity | Доказать support |
| SND11 | ARC/eARC | P1 | UNKNOWN | TvSettingsPlus.SoundSubMenuController, HDMI CEC | Реальный ARC/eARC на HW6/HW7, аудио sink + полярность |
| SND12 | Задержка аудио | P2 | UNKNOWN | TvSettingsPlus/MenuPresentationPolicy | Найти source controller exact |
| SND13 | Bluetooth Speaker / A2DP sink | P2 | UNKNOWN | RJBluetoothSpeaker + TvSettingsPlus WirelessSpeakerServices | Проверить shared Bluetooth services и receiver |
| HDMI01 | Активный источник HDMI 1 | P1 | UNKNOWN | KuSourceRJ / TvSettingsPlus.SourceSubMenuController / MtkTvInput.HDMIInputService | Различать HW6/HW7 с 4 зарегистрированными TV Input |
| HDMI02 | Активный источник HDMI 2 | P1 | UNKNOWN | KuSourceRJ / TvSettingsPlus.SourceSubMenuController / MtkTvInput.HDMIInputService | Различать HW6/HW7 с 4 зарегистрированными TV Input |
| HDMI03 | EDID version | P1 | PARTIAL_STATIC_TRACE | TvSettingsPlus SourceSubMenuController | Установить значение входного параметра 6, валидировать HW6/HW7, readback HAL |
| HDMI04 | HDMI-CEC | P1 | UNKNOWN | TvSettingsPlus SourceSubMenuController | Проверить включение и выключение с ARC |
| HDMI05 | Game mode | P1 | UNKNOWN | TvSettingsPlus SourceSubMenuController | Найти CALLSITE точно и rollback keystone |
| HDMI06 | HDMI Plug autoplay | P2 | UNKNOWN | TvSettingsPlus SourceSubMenuController | Проверить реальный reader свойства и hotplug |
| HDMI07 | Источник при старте | P1 | UNKNOWN | TvSettingsPlus.PowerOnSourceSecondaryController | Установить exact accepted values 8054 |
| HDMI08 | Приложение при старте | P1 | UNKNOWN | TvSettingsPlus.PowerOnAppSecondaryController | Проверить installed launchable apps/ package name and HOME |
| HDMI09 | USB local media | P2 | UNKNOWN | KuSourceRJ/ MultiMediaPlayer | Проверить intent consumers и handles before removing OEM player |
| HDMI10 | Аналоговые входы VGA/SCART/Tuner | BLOCK | BLOCKED | MtkTvInput | Проверить зависимости HDMI arbitration |
| PRJ01 | Положение: Front/Rear/Ceiling | P0 | PARTIAL_STATIC_TRACE | TvSettingsPlus / RuiAutoFocus / KeystoneCorrection | Восстановить точную реализацию 8054 и побочные действия, все Intent/HAL |
| PRJ02 | Автофокус при включении | P1 | UNKNOWN | TvSettingsPlus / RuiAutoFocus / KeystoneCorrection | Восстановить точную реализацию 8054 и побочные действия, все Intent/HAL |
| PRJ03 | Автофокус при перемещении | P1 | UNKNOWN | TvSettingsPlus / RuiAutoFocus / KeystoneCorrection | Восстановить точную реализацию 8054 и побочные действия, все Intent/HAL |
| PRJ04 | Ручная фокусировка | P0 | UNKNOWN | TvSettingsPlus / RuiAutoFocus / KeystoneCorrection | Восстановить точную реализацию 8054 и побочные действия, все Intent/HAL |
| PRJ05 | Автоматическая трапеция | P0 | UNKNOWN | TvSettingsPlus / RuiAutoFocus / KeystoneCorrection | Восстановить точную реализацию 8054 и побочные действия, все Intent/HAL |
| PRJ06 | 4-угольная коррекция | P0 | UNKNOWN | TvSettingsPlus / RuiAutoFocus / KeystoneCorrection | Восстановить точную реализацию 8054 и побочные действия, все Intent/HAL |
| PRJ07 | Горизонтальная/вертикальная трапеция | P1 | UNKNOWN | TvSettingsPlus / RuiAutoFocus / KeystoneCorrection | Восстановить точную реализацию 8054 и побочные действия, все Intent/HAL |
| PRJ08 | Обнаружение препятствия | P1 | UNKNOWN | TvSettingsPlus / RuiAutoFocus / KeystoneCorrection | Восстановить точную реализацию 8054 и побочные действия, все Intent/HAL |
| PRJ09 | Автоматическое выравнивание / экран | P1 | UNKNOWN | TvSettingsPlus / RuiAutoFocus / KeystoneCorrection | Восстановить точную реализацию 8054 и побочные действия, все Intent/HAL |
| PRJ10 | Цифровой зум | P1 | UNKNOWN | TvSettingsPlus / RuiAutoFocus / KeystoneCorrection | Восстановить точную реализацию 8054 и побочные действия, все Intent/HAL |
| PRJ11 | Оптическая яркость LED | P0 | UNKNOWN | KuSourceRJ.BrightReceiver, BrightUtils | Проверить 8054 customer/точные 4 режима, отказобезопасность |
| SYS01 | Питание при подключении сети | P1 | UNKNOWN | TvSettingsPlus.MtkUtil | Проверить cold AC without watchdog disable |
| SYS02 | Автосопряжение Bluetooth пульта | P0 | PARTIAL_STATIC_TRACE | KuSettings.MyAutoBluetoothActivity; AutoPair; TvSettingsPlus BluetoothReconnectService | На 8054 автоконнект / при перезагрузке |
| SYS03 | Восстановление термослужбы | P0 | UNKNOWN | KuSettings.MySetTimeService + KuSourceRJ.TimeService | На Android13 видимость other service не гарантирована |
| SYS04 | Температура | P0 | UNKNOWN | KuSourceRJ.TimeService / Android PowerManager | Проверить качество sensor data |
| SYS05 | Запуск источника после reboot | P1 | UNKNOWN | KuSourceRJ+TvSettingsPlus | Если не сработает должна быть возможность входа через ADB |
| SYS06 | Восстановление Bluetooth после reboot | P0 | UNKNOWN | TvSettingsPlus.BluetoothReconnectService | Взаимодействие с AutoPair |
| SYS07 | OEM Timer выключения | P1 | UNKNOWN | TvSettingsPlus.SwitchOffTimerService | Не деактивировать blanket sleep |
| SYS08 | Кнопка Settings на пульте | P1 | UNKNOWN | TvServices HotKeyService; KuSettings; TvSettingsPlus | services.jar, launcher, OEM hotkey config 8054 |
| SYS09 | Установленные/предустановочные приложения | P1 | UNKNOWN | Provision (не KuSetupWizard) | Не переносить 8052 KuSetupWizard assumptions |
| FAC01 | Глубокая информация о чипе | P2 | UNKNOWN | Factory.InfoViewHolder | Установить точные безопасность/поддержку на 8054 |
| FAC02 | Информация о панели и PQ | P2 | UNKNOWN | Factory.PanelInfoViewHolder | Установить точные безопасность/поддержку на 8054 |
| FAC03 | PEQ/GEQ | P2 | UNKNOWN | Factory.PEQAdjustViewHolder | Установить точные безопасность/поддержку на 8054 |
| FAC04 | DTS/Dolby | P2 | UNKNOWN | Factory.FactoryDolbyActivity/FactoryDTSActivity | Установить точные безопасность/поддержку на 8054 |
| FAC05 | System WatchDog / UART | BLOCK | BLOCKED | Factory.OtherOptionAdjustViewHolder | Установить точные безопасность/поддержку на 8054 |
| FAC06 | Factory PQ update | BLOCK | BLOCKED | Factory.FactoryPqUpdateActivity | Установить точные безопасность/поддержку на 8054 |
| FAC07 | Factory Execute Shell | BLOCK | BLOCKED | Factory.ExecuteShellActivity | Установить точные безопасность/поддержку на 8054 |
| FAC08 | Установка CI/DRM ключей | BLOCK | BLOCKED | Factory.FactoryInstallQdaActivity / FactoryKeyUpgradeActivity | Установить точные безопасность/поддержку на 8054 |
| FAC09 | SSC/MIU/LVDS | BLOCK | BLOCKED | Factory.SSCAdjustViewHolder | Установить точные безопасность/поддержку на 8054 |
| FAC10 | Factory Keystone Calib | BLOCK | BLOCKED | Factory/KeystoneCorrection | Установить точные безопасность/поддержку на 8054 |
| DBL01 | AirPlay/DLNA/Chromecast | DEFERRED | DEFERRED | FTAirplayWireless (com.android.toofifi) | Собрать точные package/backup/explicit deps |
| DBL02 | Miracast | DEFERRED | DEFERRED | WFDSinkTest (com.mediatek.miracast) | Собрать точные package/backup/explicit deps |
| DBL03 | OEM store | DEFERRED | DEFERRED | zxstore (zeasn.open.technical.tv.store) | Собрать точные package/backup/explicit deps |
| DBL04 | OEM launcher | DEFERRED | DEFERRED | zxlauncher (com.zeasn.launcher) | Собрать точные package/backup/explicit deps |
| DBL05 | BT speaker source | DEFERRED | DEFERRED | RJBluetoothSpeaker (com.rjpj.bluetoothspeaker) | Собрать точные package/backup/explicit deps |
| DBL06 | Retail/demo | DEFERRED | DEFERRED | RetailDisplay / TrdDreamPic | Собрать точные package/backup/explicit deps |
| DBL07 | Auto OTA poll | DEFERRED | DEFERRED | RuiYunOta | Собрать точные package/backup/explicit deps |
| DBL08 | OEM recommendations | DEFERRED | DEFERRED | TvServices RecommendationService | Собрать точные package/backup/explicit deps |
| BOOT01 | Ранняя надпись FULL HD | DEFERRED | DEFERRED | bootdata/mboot/tvservice? | Собрать точные package/backup/explicit deps |
| BOOT02 | Android bootanimation | DEFERRED | DEFERRED | Android bootanimation service | Собрать точные package/backup/explicit deps |
| BOOT03 | Стартовый звук | DEFERRED | DEFERRED | OEM audio startup controller unknown | Собрать точные package/backup/explicit deps |

## Следующая операция

Расширить exact invoke/Intent/Binder трассировку за пределы выбранных 32 вызовов, восстановить аргументы и ветвления из smali, затем проверить runtime только после разрешения владельца.

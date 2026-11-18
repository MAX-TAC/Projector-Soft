# Текущая доставка 96 функций 8054

DIRECT_MOD — код контрола/платформенной интеграции; DELEGATED_OEM — только штатная навигация; READ_ONLY — только информация; HIDDEN — прямой MOD-контрол скрыт; BLOCKED — запрещённый путь; NOT_IMPLEMENTED — реализации MOD нет. OEM ссылка не MIGRATED и не FEATURE_COMPLETE. Наличие отдельного подменю и аппаратуры надо подтвердить на устройстве. BUILT_UNTESTED относится к условному пути в APK, а не к переносу всей функции. — означает отсутствие активной реализации, не успешную сборку функции. Все hardware результаты отсутствуют.

DIRECT_MOD 22; DELEGATED_OEM 20; READ_ONLY 19; NOT_IMPLEMENTED 22; BLOCKED 11; HIDDEN 2

| ID | Функция | Доставка | Fallback | Код | Аппаратура | Следующая проверка |
|---|---|---|---|---|---|---|
| NAV01 | Открытие системных настроек | DIRECT_MOD | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | D-pad root navigation on8054 |
| NAV02 | Wi-Fi / Ethernet | DELEGATED_OEM | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Confirm target screen and Back navigation, no reset action |
| NAV03 | Дата и время | DELEGATED_OEM | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Confirm target screen and Back navigation, no reset action |
| NAV04 | Часовой пояс | DELEGATED_OEM | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Confirm target screen and Back navigation, no reset action |
| NAV05 | Язык и локаль | DELEGATED_OEM | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Confirm target screen and Back navigation, no reset action |
| NAV06 | Список приложений | DELEGATED_OEM | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Confirm target screen and Back navigation, no reset action |
| NAV07 | Системный раздел и модель | READ_ONLY | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Compare displayed values to original platform values |
| NAV08 | Размер текста | DIRECT_MOD | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Verify four OEM scale values and persistence |
| NAV09 | Сон / заставка / таймаут | DIRECT_MOD | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Verify Never/no dream policy versus OEM power timer |
| NAV10 | Factory reset | DELEGATED_OEM | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Confirm target screen and Back navigation, no reset action |
| NAV11 | Обновление OEM OTA | DELEGATED_OEM | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | No OTA work/actions in current milestone |
| NAV12 | Совместимые OEM Intent | DELEGATED_OEM | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Test Settings key routing without disabling KuSettings |
| NAV13 | Скриншоты OEM | NOT_IMPLEMENTED | — | — | — | Проверить 8054 triggers, отсутствие внешнего потребителя |
| NAV14 | Модель/диагностика платы | READ_ONLY | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Compare displayed values to original platform values |
| PIC01 | Режим изображения | READ_ONLY | — | SOURCE_ONLY | RUNTIME_TEST_REQUIRED | Establish the active input/PQ context and the stock callback/limits before direct writes |
| PIC02 | Цветовая температура | READ_ONLY | — | SOURCE_ONLY | RUNTIME_TEST_REQUIRED | Establish the active input/PQ context and the stock callback/limits before direct writes |
| PIC03 | Яркость изображения | READ_ONLY | — | SOURCE_ONLY | RUNTIME_TEST_REQUIRED | Establish the active input/PQ context and the stock callback/limits before direct writes |
| PIC04 | Контраст | READ_ONLY | — | SOURCE_ONLY | RUNTIME_TEST_REQUIRED | Establish the active input/PQ context and the stock callback/limits before direct writes |
| PIC05 | Насыщенность | READ_ONLY | — | SOURCE_ONLY | RUNTIME_TEST_REQUIRED | Establish the active input/PQ context and the stock callback/limits before direct writes |
| PIC06 | Резкость | READ_ONLY | — | SOURCE_ONLY | RUNTIME_TEST_REQUIRED | Establish the active input/PQ context and the stock callback/limits before direct writes |
| PIC07 | Оттенок / Hue | READ_ONLY | — | SOURCE_ONLY | RUNTIME_TEST_REQUIRED | Establish the active input/PQ context and the stock callback/limits before direct writes |
| PIC08 | Баланс красного R | READ_ONLY | — | SOURCE_ONLY | RUNTIME_TEST_REQUIRED | Establish the active input/PQ context and the stock callback/limits before direct writes |
| PIC09 | Баланс зелёного G | READ_ONLY | — | SOURCE_ONLY | RUNTIME_TEST_REQUIRED | Establish the active input/PQ context and the stock callback/limits before direct writes |
| PIC10 | Баланс синего B | READ_ONLY | — | SOURCE_ONLY | RUNTIME_TEST_REQUIRED | Establish the active input/PQ context and the stock callback/limits before direct writes |
| PIC11 | HDR | READ_ONLY | — | SOURCE_ONLY | RUNTIME_TEST_REQUIRED | Establish the active input/PQ context and the stock callback/limits before direct writes |
| PIC12 | MEMC/MJC | READ_ONLY | — | SOURCE_ONLY | RUNTIME_TEST_REQUIRED | Establish the active input/PQ context and the stock callback/limits before direct writes |
| PIC13 | MEMC Demo | NOT_IMPLEMENTED | — | — | — | Доказать support, убрать если не работает |
| PIC14 | Overscan / масштаб входа | NOT_IMPLEMENTED | — | — | — | Различать factory calibration и user per-HDMI crop |
| PIC15 | Тестовые картинки | NOT_IMPLEMENTED | — | — | — | Разработать watchdog восстановления паттерна |
| PIC16 | Non-Linear PQ | BLOCKED | — | — | — | Не записывать calibration |
| PIC17 | White Balance заводской | BLOCKED | — | — | — | Не заменять RGB ползунок Factory WB API |
| PIC18 | Panel Info | NOT_IMPLEMENTED | — | — | — | Не трактовать как оптическое разрешение |
| SND01 | Режим звука | DIRECT_MOD | DELEGATED_OEM | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | SOUND_DEVICE_CHECKLIST_8054.md: mode/output/focus, readback and audible effect |
| SND02 | Низкие частоты | DIRECT_MOD | DELEGATED_OEM | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | SOUND_DEVICE_CHECKLIST_8054.md: mode/output/focus, readback and audible effect |
| SND03 | Высокие частоты | DIRECT_MOD | DELEGATED_OEM | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | SOUND_DEVICE_CHECKLIST_8054.md: mode/output/focus, readback and audible effect |
| SND04 | Surround | DIRECT_MOD | DELEGATED_OEM | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | SOUND_DEVICE_CHECKLIST_8054.md: mode/output/focus, readback and audible effect |
| SND05 | Автогромкость | DIRECT_MOD | DELEGATED_OEM | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | SOUND_DEVICE_CHECKLIST_8054.md: mode/output/focus, readback and audible effect |
| SND06 | Звуки интерфейса | DIRECT_MOD | DELEGATED_OEM | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | SOUND_DEVICE_CHECKLIST_8054.md: mode/output/focus, readback and audible effect |
| SND07 | 5-полосный EQ | DIRECT_MOD | DELEGATED_OEM | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | SOUND_DEVICE_CHECKLIST_8054.md: mode/output/focus, readback and audible effect |
| SND08 | 13-полосный PEQ | NOT_IMPLEMENTED | — | — | — | Масштаб gain/Q, влияние sound mode, hardware support, выходы |
| SND09 | Dolby DAP | NOT_IMPLEMENTED | — | — | — | По фото DAP OFF, реальная поддержка неизвестна |
| SND10 | DTS Loudness | NOT_IMPLEMENTED | — | — | — | Доказать support |
| SND11 | ARC/eARC | DELEGATED_OEM | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Verify CEC/ARC/eARC and output-specific behavior |
| SND12 | Задержка аудио | DIRECT_MOD | DELEGATED_OEM | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Verify per-output min/max, pending readback and audio timing on 8054 |
| SND13 | Bluetooth Speaker / A2DP sink | HIDDEN | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Deferred speaker-mode scope; no service disable |
| HDMI01 | Активный источник HDMI 1 | DIRECT_MOD | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Confirm LiveTV resolves, actual port/signal, no-signal behavior and Back on 8054 |
| HDMI02 | Активный источник HDMI 2 | DIRECT_MOD | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Confirm LiveTV resolves, actual port/signal, no-signal behavior and Back on 8054 |
| HDMI03 | EDID version | READ_ONLY | DELEGATED_OEM | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Verify EDID target scope for both inputs, renegotiation and restore |
| HDMI04 | HDMI-CEC | DIRECT_MOD | DELEGATED_OEM | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Verify CEC availability, standby behavior, ARC/eARC and original power confirmation |
| HDMI05 | Game mode | DELEGATED_OEM | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Trace reset scope and restore correction before allowing a native toggle |
| HDMI06 | HDMI Plug autoplay | DIRECT_MOD | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Verify OEM autoplay state and startup side effects |
| HDMI07 | Источник при старте | DIRECT_MOD | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Verify exact8054 accepted IDs and cold boot per input |
| HDMI08 | Приложение при старте | DIRECT_MOD | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Verify target activity and persisted OEM property |
| HDMI09 | USB local media | DELEGATED_OEM | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Deferred player scope, navigation only |
| HDMI10 | Аналоговые входы VGA/SCART/Tuner | BLOCKED | — | — | — | Проверить зависимости HDMI arbitration |
| PRJ01 | Положение: Front/Rear/Ceiling | DELEGATED_OEM | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Confirm the original subcontrols, fullscreen routes and return path |
| PRJ02 | Автофокус при включении | DIRECT_MOD | DELEGATED_OEM | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Verify autofocus behavior separately for startup and movement |
| PRJ03 | Автофокус при перемещении | DIRECT_MOD | DELEGATED_OEM | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Verify autofocus behavior separately for startup and movement |
| PRJ04 | Ручная фокусировка | DELEGATED_OEM | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Confirm the original subcontrols, fullscreen routes and return path |
| PRJ05 | Автоматическая трапеция | DIRECT_MOD | DELEGATED_OEM | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Verify OEM view inflation and motion correction without changing manual correction |
| PRJ06 | 4-угольная коррекция | DELEGATED_OEM | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Confirm the original subcontrols, fullscreen routes and return path |
| PRJ07 | Горизонтальная/вертикальная трапеция | DELEGATED_OEM | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Confirm the original subcontrols, fullscreen routes and return path |
| PRJ08 | Обнаружение препятствия | DELEGATED_OEM | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Confirm the original subcontrols, fullscreen routes and return path |
| PRJ09 | Автоматическое выравнивание / экран | DELEGATED_OEM | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Confirm the original subcontrols, fullscreen routes and return path |
| PRJ10 | Цифровой зум | DELEGATED_OEM | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Verify stock digital zoom entry and Back/D-pad |
| PRJ11 | Оптическая яркость LED | DIRECT_MOD | DELEGATED_OEM | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Verify available customer profile, OEM readback, actual brightness and thermal behavior |
| SYS01 | Питание при подключении сети | DIRECT_MOD | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Verify power restoration behavior without modifying thermal services |
| SYS02 | Автосопряжение Bluetooth пульта | READ_ONLY | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Verify HID remote pairing and retained AutoPair/reconnect owners |
| SYS03 | Восстановление термослужбы | BLOCKED | — | — | — | Do not replace TimeService/source boot owner without proven transaction/rollback |
| SYS04 | Температура | READ_ONLY | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Compare sensor names/readings, no fan control |
| SYS05 | Запуск источника после reboot | BLOCKED | — | — | — | Do not replace TimeService/source boot owner without proven transaction/rollback |
| SYS06 | Восстановление Bluetooth после reboot | READ_ONLY | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Verify HID remote pairing and retained AutoPair/reconnect owners |
| SYS07 | OEM Timer выключения | DELEGATED_OEM | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Verify editor navigation, permitted schedule edit, power-off trigger and manual restore; separate from idle policy |
| SYS08 | Кнопка Settings на пульте | DELEGATED_OEM | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Verify physical Settings button resolution with KuSettings enabled |
| SYS09 | Установленные/предустановочные приложения | READ_ONLY | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Check inventory and OEM package visibility |
| FAC01 | Глубокая информация о чипе | NOT_IMPLEMENTED | — | — | — | Установить точные безопасность/поддержку на 8054 |
| FAC02 | Информация о панели и PQ | NOT_IMPLEMENTED | — | — | — | Установить точные безопасность/поддержку на 8054 |
| FAC03 | PEQ/GEQ | NOT_IMPLEMENTED | — | — | — | Установить точные безопасность/поддержку на 8054 |
| FAC04 | DTS/Dolby | NOT_IMPLEMENTED | — | — | — | Установить точные безопасность/поддержку на 8054 |
| FAC05 | System WatchDog / UART | BLOCKED | — | — | — | Установить точные безопасность/поддержку на 8054 |
| FAC06 | Factory PQ update | BLOCKED | — | — | — | Установить точные безопасность/поддержку на 8054 |
| FAC07 | Factory Execute Shell | BLOCKED | — | — | — | Установить точные безопасность/поддержку на 8054 |
| FAC08 | Установка CI/DRM ключей | BLOCKED | — | — | — | Установить точные безопасность/поддержку на 8054 |
| FAC09 | SSC/MIU/LVDS | BLOCKED | — | — | — | Установить точные безопасность/поддержку на 8054 |
| FAC10 | Factory Keystone Calib | BLOCKED | — | — | — | Установить точные безопасность/поддержку на 8054 |
| DBL01 | AirPlay/DLNA/Chromecast | NOT_IMPLEMENTED | — | — | — | Out of current scope |
| DBL02 | Miracast | NOT_IMPLEMENTED | — | — | — | Out of current scope |
| DBL03 | OEM store | NOT_IMPLEMENTED | — | — | — | Out of current scope |
| DBL04 | OEM launcher | NOT_IMPLEMENTED | — | — | — | Out of current scope |
| DBL05 | BT speaker source | HIDDEN | — | BUILT_UNTESTED | RUNTIME_TEST_REQUIRED | Deferred speaker-mode scope; no service disable |
| DBL06 | Retail/demo | NOT_IMPLEMENTED | — | — | — | Out of current scope |
| DBL07 | Auto OTA poll | NOT_IMPLEMENTED | — | — | — | Out of current scope |
| DBL08 | OEM recommendations | NOT_IMPLEMENTED | — | — | — | Out of current scope |
| BOOT01 | Ранняя надпись FULL HD | NOT_IMPLEMENTED | — | — | — | Out of current scope |
| BOOT02 | Android bootanimation | NOT_IMPLEMENTED | — | — | — | Out of current scope |
| BOOT03 | Стартовый звук | NOT_IMPLEMENTED | — | — | — | Out of current scope |

# Интеграционный аудит TvSettingsPlus MOD 8054

Объём этого этапа — текущий Draft PR #2 от базовой ревизии `1a698b4`, исходники AOSP/Leanback, мосты OEM и exact-сборка. Поиск точек записи и запуска охватил 80 production Java-файлов. Проверка сценариев по исходникам и предыдущим exact-трассам охватила 36 классов ниже. Тесты с Android-фикстурами выполняют выбранные production-обработчики и не моделируют физическое поведение проектора.

| Участок | Проверенные классы |
|---|---|
| Установка и состояние дерева | OemInlinePreferencesHook, MenuInstallation, OemRootMenuHook, OemPanelInfoHook, PanelCacheReadout, OemSystemInfoHook |
| Изображение и звук | OemPicturePanel, OemPictureState, OemInlineSlidersHook, OemSoundPanel, OemFiveBandEqualizerHook, OemSoundDelayPanel, OemAdvancedSoundRoutes |
| Проекция | OemProjectionPanel, OemProjectionSwitches, OemFocusPreferences, OemMoveKeystonePreference, OemLightSourcePreference |
| HDMI и запуск | OemHdmiPortInfo, OemHdmiEdidHook, OemCecAutoOff8054, OemHdmiCecHook, OemHdmiAutoPlayHook, OemPowerOnSourceHook, OemPowerOnAppHook |
| Питание и Android | OemAcPowerBootHook, OemScreenTimeoutSafetyHook, OemMenuVisibilityHook, OemStandardAndroidNavigation, OemBluetoothRemoteHook |
| Переходы и сохранение служб | OemStockUserSettings, OemHardwareDelegatedSettings, OemEarcDelegatedPreference, CheckedPreferenceLaunch, KuSourceRecoveryBootReceiver, KuSourceRecoveryService |

## Дефекты и регрессии

| Сценарий и причина | Исправление и проверка | Что проверить на устройстве |
|---|---|---|
| PIC18 уже показал данные, затем обновление Preference выбрасывает исключение. Обработчик раньше только логировал ошибку | `OemPanelInfoHook` скрывает родительскую категорию и строки при отказе. Production-тест подключает настоящий hook, показывает частичный кэш, затем заставляет `setSummary` вернуть ошибку и подтверждает скрытие | Реальное AndroidX-обновление, фокус и валидность полей OEM |
| Повторная привязка PIC18 к новому View до detach старого. Прежняя привязка оставалась зарегистрированной до старого detach | Одна актуальная привязка на Fragment и выбранный экран. Старый attach/focus listener снимается при смене View и при явном teardown. Production-тест проверяет duplicate bind, detach/attach, замену View и пустой холодный старт | Жизненный цикл в двухпанельном меню и повторное создание Fragment |
| Ранний отказ одного rollback-действия мог остановить снятие остальных | `MenuInstallation.close` проходит все действия в обратном порядке, сбрасывает маркер попытки. JVM проверяет ошибку посередине rollback, последующую повторную попытку и единственность успешной установки | Отказ AndroidX удалить конкретный Preference требует нового экрана или ручной диагностики |
| Между построением строки и кликом исчезает Activity, разрешение либо доступ по UID/exported | `CheckedPreferenceLaunch` выполняет повторную проверку explicit target и потребляет клик даже при отказе `startActivity`. Production-тест покрывает скрытый target, foreign/same UID, permission/state, недоступность и исключение запуска | Реальное разрешение alias, Back, D-pad и OEM init |
| Внешняя запись меняет HDMI autoplay, пока открыт MOD | Используется существующая модель snapshot/write/readback. Нажатие по устаревшему состоянию отменяется, недоступное чтение отключает строку, partial setter делает один вызов. Production-тест выполняет callback и повторное чтение | Hotplug, повторное согласование и фактическое переключение источника |
| Источник и приложение при включении делят `persist.trd.source_id`. Boot app повторно читал его уже после сравнения в UI, а отсутствие записи интерпретировалось как HOME | Ожидаемое значение передаётся в защищённую попытку записи. В 8054 отсутствие скрывает нативный селектор, fallback остаётся. Legacy default 8052 сохранён. JVM проверяет ноль записей при устаревшем состоянии | Экран со старым списком после изменения источника, пакет исчез до reboot, исходный владелец запуска |
| Параметры звука, EQ, громкость могли использовать актуальный профиль со старым локальным значением. Возврат из OEM оставлял текущий layout | Добавлено сравнение до и после записи, отказ устаревшему значению. По attach/focus сравниваются сохранённые звук, AVC и состояние громкости. При неизменном снимке строки не пересоздаются. При ошибке снимка остаются штатные действия и ручное обновление. Проверка через Android compile/D8 и JVM политик. UI-ветка обновления аудио не исполнялась в Android fixtures | D-pad при изменившемся снимке, реальное состояние динамиков/BT/ARC/eARC |
| Оптическая яркость перечитывала backend перед записью без сравнения с видимым режимом. Аналогичный пробел у питания, звуков кнопок и размера текста | Сравнение с видимым значением перед обращением к штатному setter. При отказе в оптике и питании перечитывается состояние. Неподтверждённый setter не повторяется. JVM тесты проверяют stale для питания, звуков кнопок и размера текста, модель оптики и существующий адаптер яркости | Ожидаемое включение, нагрев, доступность getter и обновление после реального OEM-редактора |

Прежние safeguards для PRJ02/03/05, CEC Auto Device Off и задержек звука проверены по цепочке открытия, callback и readback. PRJ05 сохраняет условие трёх подтверждённых нулей перед нативным выключением. Фоновая подмена режима, повторные аппаратные записи и автооткат не включены. Ключи и русские названия сверены по категориям. Обновление статического списка звука оставляет постоянные строки по идентичности. Снимок исходных значений действует только в пределах страницы.

## Операции записи и запуска

| Категория | Проверенный путь |
|---|---|
| Явный выбор пользователя | `Settings.Global` для звука/EQ/задержек и разрешённых legacy-путей изображения/CEC/EDID. `Settings.System` для размера текста и звуков кнопок. `SystemProperties.set` через reflection для HDMI autoplay и источника/приложения запуска. Android `AudioManager.setStreamVolume`. Штатные callbacks для фокуса/движения, питания и подтверждённой яркости, `HdmiControlManager.setTvSendStandbyOnSleep` для 8054. Каждое изменение проходит свой gate. |
| Штатная инициализация | Создание AndroidX rows, чтение OEM `getInstance` в существующих gateway и подписки для обновления. PIC18 читает уже существующий кэш. Выбор Android/LiveTV маршрута открывает экран и может менять активный вход только после нажатия. Legacy `SharedPreferences.commit` относится к существующему журналу изображения, активная запись PIC01–12 на 8054 закрыта. |
| Намеренная политика MOD | `ensureNever()` вызывается при построении встроенного меню, при скрытии Screensaver на OEM General (когда подтверждён ID) и из boot receiver. С отклонением исходного значения пишет System `screen_off_timeout=2147483647`, System и Secure `sleep_timeout=-1`, Secure `screensaver_enabled=0`. Идемпотентное сравнение с readback, при отказе фиксируется неполное состояние. График `TimerSetActivity` имеет собственное хранилище OEM. Совместную работу таймера и Android inactivity нужно проверить отдельно. |
| OEM-побочные действия | Вход в `KeystoneSettingsSecondaryController.initView` при отключённом движении пишет нули в зависимости obstacle/screen/vertical. PIC14 при подтверждении формата может снять freeze и временно погасить main видео. HDMI game enable вызывает reset geometry. Открытие и изменение таких экранов разделены в аппаратной матрице. |
| Службы и границы JNI | Точки запуска восстановления — существующие BootReceiver/Service. На 8054 автоматическое замещение закрыто policy. Legacy brightness broadcast и отдельный OEM mode request проверяются по различным маршрутам. ROM, HIDL, драйверы, shutdown, PWM/I²C и заводская калибровка остаются штатными. Фактический эффект ниже подтверждённого OEM API известен только после аппаратной проверки. |

Поиск проводился по Settings put, системным свойствам, ContentResolver update/insert/delete, reflection, broadcast, service, reset/calibration и нативным обёрткам. Изменения проекции, PQ, звукового профиля и источника из MOD при открытии страниц не добавлены. Предел проверки побочных действий при запуске сторонней OEM Activity указан в матрице.

## Контракты навигации

| Маршрут | Предусловия и обработка |
|---|---|
| Wi-Fi, язык, время, приложения | Android ACTION, поиск только в системных Settings, проверка владельца/компонента и параметров доступа. Повторная проверка при нажатии. |
| Bluetooth пульт | Проверенный explicit Settings pairing component, согласованная проверка AutoPair, отмена диалога закрывает маршрут. Чтение статуса не заменяет HID reconnect. |
| Изображение | PIC01–12 read-only, PIC14 — exact `DisplayModeActivity` с null Bundle и проверкой HDMI/режимов до запуска. 8052 сохраняет штатный переход. |
| Звук | Explicit `MainActivity` с OEM sound action, OEM AudioOutputActivity без extras и условные stock Dolby/DTS Fragment. Условия аудиовыхода и D-pad проверяются отдельно. |
| Проекция | Exact OEM Activity и известные actions FourCorner, HV и DigitalZoom. Проверка UID/permissions и доступности на клике; общий OEM fallback сохраняется. Открытие штатной панели коррекции рассматривается как отдельный сценарий с возможными записями. |
| HDMI | Источник LiveTV через verified full passthrough input ID и system package. Без повторного использования EDID query type в качестве порта. EDID/главный CEC/eARC остаются штатными. |
| Питание и устройство | Exact TimerSetActivity без extras, публичные Android/штатные read-only поля. В 8054 наличие расписания не отменяет необходимости отдельно проверять Android inactivity. |

## Сохранность и готовность

Exact pipeline сверяет оригинальный архив, scope smali patch, OEM resources/неизменённые DEX, alias Settings, shared UID и совпадение сертификата финального APK. Совпадение сертификата и versionCode не устанавливают право обновления фактически установленного APK. KuSettings, KuSourceRJ, TimeService, BootupReceiver, MySetTimeService, HID/reconnect, RuiAutoFocus, KeystoneCorrection, TvServices, LiveTV и Factory сохранены. Никаких операций на проекторе и ADB.

[Единая аппаратная матрица](DEVICE_TEST_MATRIX_8054.md) фиксирует условия допуска. Фактический PackageManager, возможность удаления обновления с возвратом STOCK и доступность ADB после отказа остаются блокерами. Статус готовности к установке — `NOT_READY_FOR_DEVICE_INSTALL`. Тесты с программным Android и Windows/Linux preflight mocks не означают аппаратной проверки.

## Итог CI

- `d1afa73` — lifecycle PIC18 и rollback, повторная проверка переходов, stale-защита HDMI/autoplay, общего boot property и звука.
- `e8d7361` — перечитывание звука по attach/focus. При неизменном снимке сохранены существующие Preference.
- `1501353` — stale-защита оптической яркости, питания, звуков кнопок и размера текста.
- `c3c7654` — слабые ссылки на View в проекции и задержках. Production-тест выполняет rebind и teardown проекции и подтверждает ноль записей.

Локально граф и генерация 96 статусов прошли. Пятнадцать host preflight-тестов прошли с имитацией ADB. JVM выполнял реальные production PIC18/Projection hooks, launch listener, HDMI autoplay callback и guard для неизвестного контекста изображения на Android-фикстурах. Smoke-тесты Android/D8, source-safety, Windows/Linux, OEM consistency, v33 и exact-8054 прошли. Две промежуточные exact сборки отменились по concurrency при обновлении кода. Готовность относится к финальному коммиту.

`SOURCE_AUDIT_PASS / CI_PASS / BUILT_UNTESTED`. Аппаратных проверок — 0. Установка — `NOT_READY_FOR_DEVICE_INSTALL`.

SHA-256 APK `f8f4c0c3654929ccaa98aa4f75555f96f52d3e85a99dd963f877bf11e0db8d40`. SHA-256 ZIP `49610b067014ab4832cc13f0711bd3fcaff60c9418d6c92f9793a2852bb45d1e`. Архив содержит provenance SHA и SHA256SUMS. [Exact APK](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44501022081/artifacts/14516084668) · [Exact log](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44501022081/job/135480505460) · [Android/D8/JVM](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44501022008) · [Source-safety, Windows и Linux](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44501022015) · [OEM](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44501022032) · [v33](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44501021957)

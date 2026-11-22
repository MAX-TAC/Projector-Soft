# 04. Полное техническое задание: TvSettingsPlus MOD v41 как замена KuSettings

**Заказчик подтвердил ТЗ 2026-11-20/21.** Данное ТЗ имеет приоритет над более ранними частными планами PIC04/HIDL. **Цель — законченный пользовательский продукт**, а не количество исследованных ELF, HIDL интерфейсов или созданных классов. Текущий код является экспериментом, а не готовым релизом.

## A. Продукт и границы

**Название:** TvSettingsPlus MOD для оригинальной Android TV / RUIOS 8054, с возможностью последующей адаптации на аппаратный профиль 8052.

**Пользовательский результат:** по кнопке **Settings** на пульте открываются настоящие стандартные настройки Android TV (`com.android.tv.settings.MainSettings`), встроенный эстетичный интерфейс v41 с восемью разделами; **все поддерживаемые обычным OEM-пользователем проекторные настройки** отображают актуальные значения и изменяют параметры **ровно тем же способом и с теми же эффектами**, что оригинальные KuSettings/TvSettingsPlus/OEM обработчики. Никакой пользовательский MOD пункт не открывает китайскую `com.my.settings` Activity или Fragment.

**Не задача:** переписать kernel, MediaTek HAL, калибровочный алгоритм, драйвер мотора, factory registers, систему OTA, AOSP Network/Bluetooth/Apps/Language целиком или использовать опасный «root-доступ по умолчанию». Не создавать отдельную поддельную Settings Activity, не менять без запроса внешний вид уже принятого v41.

**Что оставить штатным:** Android TV networking/Bluetooth pairing/apps/date/time/locale/accessibility; MediaTek TvServices, TV Input, TRD, KuSourceRJ, RuiAutoFocus, KeystoneCorrection, автофокус/ToF сервоприводы, panel/factory calibration, OTA updater, secure boot, штатная защита и термоконтроль. Их пользовательские действия должны быть представлены в MOD через свои UI handlers, где это реально можно безопасно сделать.

## B. Критерий полноты

1. Инвентаризовать все **пользовательские** функции китайского OEM меню и сравнить с v41: `PRESENT_WORKING`, `PRESENT_READONLY`, `DELEGATED_ANDROID`, `DELEGATED_CHINESE`, `MISSING`, `UNSUPPORTED_ON_8054`, `FACTORY_ONLY`.
2. Источник единого реестра: [96-пунктная матрица](https://github.com/MAX-TAC/Projector-Soft/blob/audit/8054-tvsettingsplus-20261117/research/8054/FINAL_FUNCTION_PARITY_8054.csv). Она шире UI ТЗ: содержит platform, factory, boot, telemetry и сервисы; она **не** означает, что пользователь обязан видеть 96 регуляторов. Все отсутствующие потребительские функции добавить.
3. **Для каждого user control** записать: original callback, storage/service key, actual setter/getter, value units/enum, диапазон, prerequisites, side effects, соответствующий MOD class/UI row, аппаратное испытание до/после/restore, KuSettings package dependency.
4. Не оставить ни одного режима `DELEGATED_CHINESE` для завершённых пользовательских настроек. Заводские инструменты не копировать в гражданский интерфейс.

## C. Обязательные разделы и пользовательские функции

### C1. Изображение (PIC)

Должны быть реализованы user-mode: **режим изображения, цветовая температура, гамма**, brightness, contrast, saturation, sharpness, hue, RGB controls, применимый HDR, MEMC/MJC modes, допустимые picture format/aspect/overscan; списки видимых OEM профилей и их реальные названия.

- В существующем `NativePicturePanel` сохранить нижнее OEM окно, название/значение, D-pad LEFT/RIGHT, Back, не закрывать диалог только из-за собственного ContentObserver эха.
- Из original code переносить **callback и prerequisites**, а не старый Activity/Fragment. Для обычных scalar settings при статически доказанном пути использовать `Settings.Global.putInt` → `TVMenuSettingsService` → `TVSettingConfig`; не вызывать `TVNativeWrapper` напрямую без причины.
- Пределы брать из оригинальных API `TVSettingConfig.getMin/getMax`, реальные `step` измерить на OEM. Не закладывать `0..100`, `+1` или fake `+2` как факт без измерения.
- Source/format/profile gating должен быть достаточным для безопасной записи; отдельно проверить, действительно ли OEM использует source-specific PQ и что происходит при HDMI/SDR/HDR/Mode switch. `TvInputManager.getInputState` недостаточен для **активного** входа. Не раздувать architecture сверх реального оригинального поведения, но не лишать пользователя возможности отката.
- HDR/MEMC/game/picture_mode/temp могут иметь побочные действия и нуждаются в отдельных smoke/restore сценариях.
- **Не включать** заводские RGB white-balance calibration, нелинейные PQ factory APIs, test patterns как обычный пользовательский reset.

### C2. Звук (SND)

Перенести/довести **режим звука**, bass/treble, Surround, automatic volume control, **5-band EQ** с OEM User profile, output format (speaker/HDMI ARC/eARC/SPDIF/BT там, где поддерживается), индивидуальные audio delays, system sounds, доступные Dolby/DTS consumer options.

- EQ output/profile должны проверяться: пользовательский пресет ≠ статические factory PEQ gains. Не обещать **13-полосный PEQ**, если у 8054 найден только Factory-калибровочный интерфейс.
- Параметры eARC/ARC/CEC взаимосвязаны; делать safe revert/readback по каждому, а не глобальный `Audio Reset`, который может поменять другие settings.
- Playback effects подтверждать **на слух/выходе**, а не только по Global/provider echo.

### C3. Источники / HDMI

Функции: Android TV home, HDMI1/2 selection, visible signal/source status, input labels, boot destination, boot app, HDMI hotplug autoplay, CEC power/Auto Device Off, HDMI-CEC/ARC/eARC, применимые EDID modes, Game mode/format/overscan при существовании на 8054.

- Использовать `OemHdmiPortInfo`, `TvInputManager`, `Hdmi8054Policy` и штатный LiveTV passthrough; `HW6/HW7` не смешивать с 8052.
- Хранимый EDID code не является proof, что display link его действительно renegotiated. На EDID/CEC отдельно отметить влияние на HDMI handshake, ARC и power.
- Game mode может вызывать resetTrapezoid; до первого независимого test необходимо сохранять геометрию и исключить неожиданный reset.

### C4. Проекция / световой источник

Положение/ориентация Front/Rear/Ceiling, brightness modes источника света, auto focus at boot/movement, manual focus, 4-corner keystone, H/V keystone, digital zoom, auto keystone, obstacle detect/screen fit только при подтверждённой board capability.

- **Не путать яркость LED со stored picture brightness** — разные hardware controls.
- Раздельно сохранять штатные OEM core (RuiAutoFocus/KeystoneCorrection/KuSourceRJ, motor/ToF/thermal) и переносить только пользовательский handler в MOD; не превращать чистый Fragment lifecycle в внезапный motor/factory reset.
- Возможен системный editor другого OEM package (кроме KuSettings) как временный fallback, но итоговая целевая форма пользователя — собственный пункт MOD, если реализуем безопасно.
- Любое движение линзы/мотор/LED/геометрия требуют привязки к актуальному состоянию, stop, bounds и ручного возврата. Никогда не экспериментировать с Factory calibration.
- **Отдельный обнаруженный старый путь:** `OemProjectionPanel.projectionIntent()` → `com.my.settings.activity.ProjectionActivity`. В финальном MOD это недопустимо; заменить после переноса работающего функционала.

### C5. Питание / система / поведение

Нужны обычные user-mode: AC power-on behavior, sleep/screen saver/idle, power-off timer/schedule, boot source/application, необходимая настройка кнопки Settings, font scale и разумные аппаратные информационные строки.

- Не путать OEM TimerSetActivity schedule, `power_sleep_timer` и `power_switch_off_timer`: у каждого отдельные условия и side effects.
- Android platform app management, language, timezone, date, networking, BT — **использовать штатные**, не рисовать дубль OEM; первичный `KuSetupWizard` сохранить.
- Factory reset/OTA/DRM/CI, инженерные/test/calibration функции отдельные, не выполнять в рамках автоматического преобразования user slider.

## D. Требования к реализации

| № | Требование | Критерий приёмки |
|---|---|---|
| D1 | Реальный MainSettings/MainFragment, Leanback/AOSP host | Кнопка Settings и вход из HOME/HDMI приводит к одному ожидаемому дереву, D-pad/Back/focus работают |
| D2 | Сохранить дизайн v41 | Все принятые меню/строки/нижние слайдеры на месте; новые элементы только по необходимости |
| D3 | Нет Chinese UI runtime | Проверен полный explicit/implicit Intent/ComponentName/Fragment маршрут; ни один user control не запускает `com.my.settings` Activity |
| D4 | Не копировать низкоуровневые алгоритмы | Для каждого control описан подтверждённый OEM/provider/backend; MediaTek HAL и motor drivers сохранены |
| D5 | Реальные диапазоны/единицы | Список режимов, min/max/step/enum сравнен с OEM и подтверждён на устройстве |
| D6 | Readback ≠ effect | Разделять successful write / provider echo / OEM getter / physical effect / reboot persistence |
| D7 | Отказ безопасен | При unavailable profile/permission/backend система не пишет неизвестные значения; понятное сообщение, нет бесконечных retries |
| D8 | Restore | Для каждого испытания до записи записано original value и manual restore, при контекстном конфликте нет слепого replay |
| D9 | Пакет совместим с exact-8054 | Original cert/manifest/privileged context, package version/dex/resources проверены в CI + на устройстве |
| D10 | CI + hardware qualification | Unit/Robolectric, exact APK hash/signature/DEX diff, owner-controlled real-projector smoke, не считать CI заменой hardware |
| D11 | Полный audit coverage | Все consumer пункты из 96 registry классифицированы и имеют статус реализации, test, service dependency |
| D12 | Нулевая зависимость от KuSettings UI | Все пользовательские функции работают с установленным, но **не открываемым** `com.my.settings`; результат M1 ниже |
| D13 | Вывод из эксплуатации пакета | Только после переноса обязательных receiver/service, cold boot/recovery/thermal/remote тестов и rollback; результат M2 |

## E. Независимость от KuSettings: отдельные контрольные точки

**M1. User-interface independence:** все пользовательские настройки доступны из TvSettingsPlus; старые китайские Activity/Fragment никогда не запускаются; `com.my.settings` **временно может оставаться установленным**, если его background services ещё нужны.

**M2. Package independence:** после полного dependency graph audit и миграции критичных background компонентов приложение функционирует при **временно отключённом KuSettings**. Прежде всего устранить `com.my.service.BootupReceiver → MySetTimeService → KuSourceRJ.TimeService` восстановление. Сохранить `KuSourceRJ.TimeService`, другие cooling/motor/TOF/CEC/AutoPair vendor owners. Новый MOD receiver/service на момент среза **отключён**, не испытывался; два одновременных watchdog не допускать. Проверить boot, crash recovery, TIME_TICK, вентилятор, Bluetooth пульт, сеть, HDMI, звук, источники, OEM schedule и термальную безопасность.

**M3. Removal candidate:** **только** после M2 + достоверный план отката и оценка system/AVB/OTA; фактическое удаление APK может потребовать firmware engineering. Отключение через PackageManager и удаление файла из /system — разные операции/риски. Целевое GUI-приложение не удалять до тестирования M1/M2.

[Трасса зависимостей](https://github.com/MAX-TAC/Projector-Soft/blob/audit/8054-tvsettingsplus-20261117/research/8054/KUSETTINGS_SERVICE_MIGRATION_8054.md) · [миграция](https://github.com/MAX-TAC/Projector-Soft/blob/audit/8054-tvsettingsplus-20261117/docs/8054_KUSETTINGS_MIGRATION.md).

## F. Порядок этапов (без обязательства тратить месяц на один ползунок)

1. **Inventory**: из оригинального Chinese и TvSettingsPlus составить actionable разницу по 96 registry + отсутствующим потребительским пунктам.
2. **Picture user slice**: scalar PIC03–10, режимы, температура, gamma, HDR/MEMC. Минимальная тестовая смена с ручным restore. Не возвращаться к NATIVE-005b, если backend уже понятен.
3. **Sound**: bass/treble/eq/profiles/output/delay/Surround/AVC; live readback/effect.
4. **HDMI**: source selection, boot/autoplay, CEC/EDID/game; особые тесты handshake и restore.
5. **Projection**: manual/auto focus, keystone/corners/zoom/orientation/light modes, без потери OEM limits и thermal controls.
6. **Power/system + legacy cleanup**: timers, settings hotkey, оставшиеся platform-friendly OEM user functions.
7. **M1 acceptance**: no Chinese GUI runtime and complete consumer parity; package remains installed if service dependency exists.
8. **M2**: перенести фоновые зависимости, провести controlled disable/reboot/recovery на exact 8054; вне обычного UI этапа.
9. **M3 / optional firmware project**: только после M2 и отдельного решения по full flashable ROM/AVB.

Нельзя держать все остальные пункты заблокированными из-за одного сложного RTL backend. Группировать по прямым готовым Android/OEM API. При необходимости спорные функции `DEFERRED_WITH_REASON` с конкретным evidence и задачей, а не пустым «не реализовано».

## G. План тестирования / требуемые действия владельца

Всё, что требует физического 8054, должно быть **прямо и конкретно** согласовано с владельцем. Агенту писать: на каком устройстве выполнить команду, что записать, какие значения считать правильными, как вернуть исходное состояние и когда остановиться.

До разрешения можно: читать GitHub, компилировать из isolated branches, проводить статический анализ, добавлять mock/unit tests, готовить read-only ADB скрипт и checklists. **Не** устанавливать/заменять APK на устройстве, не делать `adb shell settings put`, не делать Factory/I2C/root writes, `pm disable`, `fastboot unlock`, `dd of=`, OTA/flash/AVB changes, reboot, motor actions или EDID/CEC writes без отдельного осознанного разрешения.

Первый минимальный owner-controlled тест: **STOCK HDMI1/SDR/PIC04** — записать `picture_mode`, `picture_color_temperature`, `picture_list_hdr`, `picture_contrast`; в китайском оригинальном UI нажать RIGHT ровно раз, измерить настоящий шаг и Global after; штатным UI восстановить C0, повторить чтение и визуальную проверку. Не делать MOD hardware writes и не объявлять PIC03–10 готовыми по этому одному тесту.

[Подробная аппаратная матрица](https://github.com/MAX-TAC/Projector-Soft/blob/audit/8054-tvsettingsplus-20261117/research/8054/DEVICE_TEST_MATRIX_8054.md) · [STOCK preflight](https://github.com/MAX-TAC/Projector-Soft/blob/audit/8054-tvsettingsplus-20261117/research/8054/STOCK_PICTURE_DIAGNOSTICS_8054.md).

## H. Критерии завершения для другого ИИ

- По каждой функции: **Original OEM mapping VERIFIED**, **MOD handler IMPLEMENTED**, **CI PASS**, **owner device action TESTED**, **readback/physical EFFECT VERIFIED**, **restore PASS** и **Chinese UI dependency NONE**.
- Сохранённая `FINAL_FUNCTION_PARITY_8054.csv` не должна отмечать `HARDWARE_VERIFIED` без реальных доказательств. Стабильные commit, CI URL, exact APK SHA, device report/версия и ошибки должны быть воспроизводимы.
- Отдельные статусы `M1/M2/M3`; не выдавать успешный M1 за возможность удаления пакета.
- Другому ИИ предлагается **оспаривать** наш ТЗ только после фактической проверки исходников и ясно отделять подтверждённую необходимость от избыточной защиты.

**Ключевой приоритет:** закончить именно перенос всех работавших у производителя пользовательских функций в существующий v41, а не декомпилировать ради декомпиляции.

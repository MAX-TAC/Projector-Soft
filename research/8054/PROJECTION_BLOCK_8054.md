# Проекция 8054: OEM-навигация, не полный перенос

Кодовая оценка после группового CI: BUILT_UNTESTED; аппаратура RUNTIME_TEST_REQUIRED. Никакой OEM-ссылке не присваивается MIGRATED/FEATURE_COMPLETE. Реальные runtime критерии: original MainActivity/Activity включена вместе с приложением; внешний компонент exported; требуемое permission выдано. Не прошедшие проверку shortcuts не создаются. Общий OEM раздел доступен отдельно, но он не доказывает наличие каждого подменю.

## PRJ01–PRJ11

Общий точный entry G: explicit `com.android.tv.settings/com.android.tv.settings.uicustom.MainActivity`, action `android.settings.SETTINGS`; штатный раздел Projector. Существующий OEM маршрут сохраняется. Названия secondary controllers ниже взяты из handoff; они не выдаются за новый прослеженный HAL call.

| ID / назначение и наличие | OEM entry/API | Побочные действия | Стратегия / критерий доступности | RUNTIME_TEST_REQUIRED |
|---|---|---|---|---|
| PRJ01 Front/Rear/Ceiling, штатная установка | G; доказанный TvSettingsPlus ProjectorManager → Factory ApiService → setProjectorMirrorMode(int) | mirror/flip config, persistent property, I²C, display | OEM_DELEGATED через G; legacy MOD selector закрыт8054 | Исходный mode, ориентация, OEM возврат без калибровки |
| PRJ02 автофокус при старте | `OemFocusPreferences` вызывает проверенный штатный callback | мотор и датчики при старте | DIRECT_MOD · штатная доступность | Исходное значение; cold boot в отдельном тесте |
| PRJ03 автофокус при движении | `OemFocusPreferences`, отдельный штатный callback | сенсоры, автофокус, мотор | DIRECT_MOD · штатная доступность | Сохранение состояния и разрешённый movement test |
| PRJ04 ручной фокус, VAF2S/RuiAutoFocus присутствует | G → OEM focus UI; не запускать VAF2S/фокус broadcast напрямую | движение мотора и предельные положения | OEM_DELEGATED GROUP_ENTRY_ONLY | D-pad/пульт, границы и original focus; без raw motor commands |
| PRJ05 автотрапеция | `OemMoveKeystonePreference`, штатный callback и layout | геометрия и доступность зависимых функций | DIRECT_MOD · штатные зависимости | Исходная геометрия и откат без reset/calibrate |
| PRJ06 четыре угла | `rjpj.action.keystone.FOUR_CORNER` → `FourCornerActivity` | D-pad меняет выбранную точку, отдельный reset | DELEGATED_OEM · отдельный пункт, statically confirmed | Открытие/Back без правок, D-pad и возврат исходных точек |
| PRJ07 H/V трапеция | `rjpj.action.keystone.HV` → `HVKeystoneActivity` | геометрия/масштаб через штатный редактор | DELEGATED_OEM · отдельный пункт, statically confirmed | Проверить оси, Back и исходную геометрию |
| PRJ08 obstacle, зависит от OEM ToF/camera feature config | G → KeystoneSettingsSecondaryController | датчики, автоизменение геометрии | OEM_DELEGATED GROUP_ENTRY_ONLY; наличие приложения не доказывает датчик | Проверить доступность пункта/датчика, не включать скрытую функцию |
| PRJ09 auto screen alignment, зависит от OEM config | G → KeystoneSettingsSecondaryController | ToF/camera + автокоррекция | OEM_DELEGATED GROUP_ENTRY_ONLY | Наличие UI/датчика, baseline геометрии и штатный возврат |
| PRJ10 цифровой зум | `rjpj.action.keystone.DIGITAL_ZOOM` → `DigitalZoomActivity` | настройка масштаба через общие координаты коррекции | DELEGATED_OEM · отдельный пункт, statically confirmed | Штатные границы, один шаг, возврат, Back |
| PRJ11 оптическая яркость | Четыре режима KuSourceRJ для подтверждённого customer-0 | LED и тепловая реакция | DIRECT_MOD при штатной доступности; прочие профили остаются в OEM | Режим, яркость, температура и возврат в согласованном тесте |

Доказательства: handoff MANIFEST_COMPONENTS_8054.csv (RuiAutoFocus VAF2S/MainActivity; KeystoneCorrection FourCorner/DigitalZoom/HV; KuSourceRJ BrightActivity/BrightReceiver), SETTINGS_MIGRATION_MATRIX_8054.csv и SELECTED_SMALI_TRACE_FINDINGS_8054.md (PRJ01 exact chain). Manifest declaration не доказывает exported, support, запуск или безопасные side effects — это проверяется отдельными критериями выше. Для digital zoom неизвестный диапазон не заменяется выдуманным native slider.

## Точные точки входа PRJ06/07/10

Оригинальный `ProjectorSubMenuController` через `$5/$6/$7.onClick` вызывает `FloatMenuManager.jumpByAction(String)`. Он создаёт внутренний `Intent(action)` без extras (1883–1894). Внешняя обёртка передаёт этот Intent и параметры возврата в боковую панель: `menu_index`, `focus_id` и при необходимости состояние вторичного контроллера. Эти данные принадлежат навигации самой боковой панели.

Независимые штатные вызовы подтверждают прямой запуск. `KuSettings.IntentUtils.imageDigitalIntent` передаёт действие DIGITAL_ZOOM в двухаргументный `intentAction` (391–488). `KuSourceRJ.IntentUtils.fourPointIntent` и `keystoneIntent` используют тот же контракт для FOUR_CORNER и HV (317, 424). Оба варианта создают action Intent, добавляют `FLAG_ACTIVITY_NEW_TASK`, разрешают его с `MATCH_DEFAULT_ONLY` и вызывают `startActivity`. Этот маршрут обходится без extras и без подготовки через broadcast. Для каждого action точный Manifest KeystoneCorrection объявляет отдельный exported Activity и DEFAULT. [Трасса запуска и Manifest](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44397417775/job/135130932230)

`OemProjectionPanel` добавляет три отдельных пункта для 8054. До показа и при нажатии проверяет соответствие action → компоненту `com.rjpj.tcorrection`, системный пакет, enabled Activity и application, доступность для UID/exported и permission. При отказе остаётся общий штатный маршрут. `KeystoneCalibrationActivity` сохранён отдельным пунктом.

При открытии исследованные `onCreate` устанавливают layout, инициализируют виджеты и заголовок. `onResume → initData → checkDisplay` перечитывает восемь `persist.rjpj.fourcorner.keystone_*` и рисует редактор. Открытие не вызывает найденных setter/reset; Intent не несёт ID HDMI. Эти сведения не устанавливают аппаратную область хранения по источникам. FourCorner направляет D-pad в `setHorizontal/setVertical → KeystoneUtil.set*Offset`. DigitalZoom направляет D-pad в `setZoom → KeystoneUtil.adjustPixel` и использует те же сохранённые координаты. Отдельные команды редактирования и сброса принадлежат штатным экранам. Проверка Back/D-pad и возможных переходов на реальном дисплее остаётся обязательной. [Трасса жизненного цикла](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44396676096/job/135128331360)

PRJ01 остаётся на штатном маршруте из-за связанных mirror/flip, property и аппаратных действий. Для PRJ04 по-прежнему используется штатное ручное управление с пульта. Для PRJ08/09 найдены штатные `setAutoObstacleAvoidance` (689–705) и `setAutoScreen` (707–723): они сохраняют свойства, но доступность пунктов связана с движением/автокоррекцией и датчиками. Пока полный контракт не подтверждён, OEM-обработчики сохранены. Три новых пункта не меняют фокус, автотрапецию и оптическую яркость.

## Код и проверки

OemProjectionPanel только устанавливает checked explicit Intents; не startService/sendBroadcast/I²C/PWM/property writes. Projection8054PolicyTest проверяет внешний exported/permission, disabled Activity/application, same-package navigation и legacy brightness gate. Оригинальные KuSettings/KuSourceRJ/RuiAutoFocus/KeystoneCorrection/Factory/TvServices/TimeService/fan остаются включёнными. 8052 legacy поведение сохранено, но его hardware safety здесь не переутверждается.

Полная актуальная сводка 96 функций — DELIVERY_STATUS_8054.md / delivery_status_8054.json. DELEGATED_OEM означает только навигацию, HIDDEN означает скрытый прямой MOD control с отдельным fallback, NOT_IMPLEMENTED означает отсутствие реализации MOD, а не отсутствие функции в OEM firmware. source-only/отсутствующая функция не помечается успешно собранной; runtime verified 0.

## Короткий device/rollback checklist (пока не выполнялся)

1. Получить отдельное разрешение и сначала подготовить установочный/recovery пакет из следующего milestone. Сохранить вне приложения исходные projection values, фото геометрии/фокуса/ориентации/zoom/brightness/customer ID, активные OEM службы/температуры. Не считать Settings.Global полным calibration backup.
2. D-pad открыть G, Zoom, Brightness по одному; проверить доступность/отказ permissions и Back в MOD. Не менять значения при проверке навигации без отдельного согласования: OEM Activity сама может иметь side effects.
3. Для согласованной функции проверить original range и изменить один пользовательский шаг; вернуть original через тот же OEM UI и перепроверить геометрию/фокус/температуры. Не обращаться к factory reset/calibration, I²C/PWM или motor API. Не останавливать службы для теста отказа.
4. При отсутствии исходного state/понятного ручного возврата — stop test до изменения функции. UI/Intent eligibility не доказывает физический rollback. Результаты по каждому PRJxx остаются NOT_TESTED до реального теста.

Перед первым device test отдельный обязательный пакет: exact MOD + matching sha256 + original8054 APK + preflight PackageManager/signature/sharedUID/SELinux + recovery/D-pad path при нестарте меню. Ни установки, ни включения/отключения системных пакетов, ни записи Global этот документ не разрешает.

## Подтверждённый пакет проекции
Implementation db9818: source-safety 44251310586, Android/D8/JVM 44251310569, OEM 44251310610, v33 44251310583, exact-8054 44251310720 — success. APK SHA-256 d14b05f14a752d170a46956197059164693e6082b6c86db8be691cf67e029f8a, artifact 14404267004; не установлен. Тест настоящего brightness gateway подтвердил ноль receiver lookup/broadcast8054/unknown и сохранение8052 dispatch. Начат следующий STANDARD_ANDROID milestone (STANDARD_ANDROID_BLOCK_8054.md); пройденная сборка не означает аппаратного переноса PRJxx.

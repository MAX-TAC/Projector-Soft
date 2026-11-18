# HDMI / EDID / CEC 8054

Кодовый milestone: нативные строки выбора HDMI1/2 запускают штатный MediaTek LiveTV; аппаратное состояние RUNTIME_TEST_REQUIRED. Не hardware verified. После успешного CI статус этого milestone — BUILT_UNTESTED, PR draft.

## Доказательства и разные индексы

Handoff `00_START_HERE.md:3` и строки HDMI01/HDMI02 миграционной матрицы фиксируют плату с двумя портами: HW6:HDMI1, HW7:HDMI2. Это переданные исследования, не новый аппаратный тест. Проверка MOD требует ровно эту пару в `Build.A_RJ_HDMI_PORTS`, count=2, machine=8054 и сопоставляет с HDMI-type TV Input от `.hdmi.HDMIInputService` пакета `com.mediatek.tvinput`. HW5/HW8 могут быть зарегистрированы, но не становятся физическими HDMI3/4. Совпадение suffix без правильного owner, отсутствующий input и дубликаты не подтверждают вход.

| Обозначение | Смысл и граница |
|---|---|
| HDMI 1 / HW6 | physical label из board metadata, отдельно зарегистрированный TV Input |
| HDMI 2 / HW7 | physical label из board metadata, отдельно зарегистрированный TV Input |
| `getVideoInfoData(6, info)` | статически установленный OEM аргумент; не доказано, что это выбираемый физический HDMI-port index |
| `tv_input_hdmi_edid_version` | один Settings.Global key 0..3, без port argument в UI writes |
| `g_fusion_picture__hdmi_edid_version` | общий config ID из TvServices itemIds/observer, port routing неизвестен |

Нельзя автоматически заменить аргумент 6 на 7 для HDMI2. Наличие двух logical inputs не доказывает per-port EDID state, сигнал, активный источник или ARC/eARC-capability. Детальная уже полученная цепочка — EDID_GLOBAL_ROUTE_8054.md; повторной декомпиляции нет.

## Реализация и побочные действия

OemHdmiPortInfo проверяет board identity и TYPE_HDMI TV Input list. Две строки используют только однозначные HW6/HW7. При клике список перечитывается; отсутствующий или неоднозначный input блокирует запуск. Нет прямого tune(), записи Settings/properties, CEC API или EDID setter.

Legacy MOD EDID selector остаётся скрыт на 8054 (EdidPortPolicy). Добавлена явная ссылка в оригинальный раздел источников: нужный источник и EDID выбираются пользователем там. Это переход в source page, не автоматический выбор порта или direct EDID control.

Legacy MOD CEC разрешён только на исходной 8052; на 8054/unknown Bridge не создаётся и raw CEC/eARC операции не выполняются. Причина: текущий legacy transaction после смены CEC может выключить auto-device-off, вызвать setEarcEnable(false) и обновить Global eARC key; частичный отказ оставляет побочные изменения, а обратный transaction на 8054 не проверен. OEM CEC экран сохраняет подтверждения и управление зависимостями. eARC остаётся в OEM sound page; game mode в OEM source page (известен resetTrapezoid side effect). Критические службы не отключались. Boot-source/autoplay контролы этим milestone не переделываются и не считаются доказательством active input switching.

## Обновление состояния портов

Строки HDMI 1/2 перечитывают состояние TV Input при возвращении фокуса окну, в том числе если OEM отказал в регистрации callback. Срок жизни focus-listener привязан к текущему View. Это актуализирует состояние соединения и не помечает его как активный вход. JVM регрессия покрывает отказ регистрации, повторный attach, возврат фокуса и снятие подписок.

Штатный `SourceFragment.switchToHDMI(Context, String)` в TvSettingsPlus фильтрует TYPE_HDMI, берёт ID входа и передаёт `TvContract.buildChannelUriForPassthroughInput(id)` в ACTION_VIEW с пакетом `com.mediatek.wwtv.tvcenter` и NEW_TASK (smali 674–759). MOD повторяет этот переход из нативной строки с точным ID и разрешает только доступный системный LiveTV Activity для такого URI. Проверка разрешения и enabled/exported выполняется при клике. Ошибка запуска видна в строке. Запуск не доказывает активный видеовход. [Exact stock trace](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44357322703/job/134988833252)

## EDID, CEC и игровой режим

EDID в Android TV показывает одно состояние `Settings.Global` с точной меткой режима. Строка перечитывается при возврате фокуса. Индивидуальные переключатели HW6/HW7 требуют доказанного адресата EDID и чтения на внешнем источнике. Команды и границы наблюдения описаны в [EDID_GLOBAL_ROUTE_8054.md](EDID_GLOBAL_ROUTE_8054.md).

Основной `SourceSubMenuController.lambda$initView$12` сначала пишет `Settings.Global.HDMI_CONTROL_ENABLED` (1575–1577). При `DataSeparaterUtil.isSupportPowerConfirmed()` передаёт включение в `EnergyCommonActivity` с extra `EnergyCommonActivity=4`; `EnergyFragment.typeClicked(4)` вызывает включение CEC (127–137). Обычная ветка вызывает `CompatibleApis.setHdmiCecControlEnabled` → `HdmiControlManager.setHdmiCecEnabled`. При отключении с eARC capability OEM модифицирует `sound_earc=1` и вызывает `MtkTvCecAnBase.setEarcEnable(false)` (1622–1631), затем отключает Auto Device Off (1663). В OEM callback результаты этих операций не образуют транзакцию. Возврат CEC в ON сам по себе не доказывает возврат eARC, выхода или связанного поведения устройств. В MOD основной CEC отображается, запись требует проверки подтверждения и связанных состояний. Прежний редактор сохранён.

Отдельный `SourceSubMenuController.lambda$initView$13` (1677–1689) и `SourceFragment.lambda$initView$9` (530–542) вызывают только `CompatibleApis.setHdmiCecAutoDeviceOffEnabled(boolean)`. Тот вызывает `HdmiControlManager.setTvSendStandbyOnSleep(int)` (115). Для этого действия добавлен нативный 8054 switch. Перед записью считывается значение и проверяется, что CEC включён и UI не устарел; после setter требуется актуальный readback. При отказе, исключении или изменении CEC контрол перечитывается и не выполняет автоматическую компенсацию. Тест восстановления проверяет отдельное явное действие из заново прочитанного состояния. Проверка воздействия на реальное HDMI-устройство при выключении проектора остаётся аппаратной.

Игровой режим: штатный `SourceSubMenuController.lambda$initView$10` пишет `tv_picture_video_game_mode`, а при включении вызывает `resetTrapezoid()` (1525–1532). `resetTrapezoid` отправляет explicit broadcast в `com.rjpj.tcorrection.TCorrectionBroadcastReceiver` с `type=10` (1825–1873). Обработчик и набор изменяемых значений, область по входам и восстановление коррекции требуют отдельного подтверждения. Native game toggle не включён. [Stock EDID/CEC trace](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44363346572/job/135010388173)

## Тесты, откат, device-план

Hdmi8054PolicyTest: exact HW6/HW7 mapping, four registered inputs versus two physical ports, reverse metadata order, wrong labels/count, missing metadata, 8052 rejection, absent input, spoofed owner, duplicate registrations и CEC gate 8054/unknown/8052. API33 compile/общий JVM regression/exact-8054 build — одним пакетом subsystem. Это не проверка HDMI сигнала/handshake.

Откат исходников: revert subsystem commit. Новых device writes от read-only inventory нет. Для device tests после отдельного разрешения сохранить исходные active input, EDID, CEC/auto-off/eARC/аудиовыход и коррекцию. По одному физическому HDMI проверить OEM выбор → HW6/HW7, signal и EDID before/after только через OEM UI. Не считать показания фиксированного query(6) подтверждением HW7. При CEC изменении отдельно проверить auto-off и возврат eARC/выхода (включение CEC само по себе не обязано вернуть eARC). Game mode не переключать в общем EDID тесте. Возвращать исходные настройки через OEM UI и сверять signal/audio; не делать mass Global replay. Все hardware результаты пока NOT_TESTED.

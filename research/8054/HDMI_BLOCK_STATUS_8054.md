# HDMI / EDID / CEC 8054

Кодовый milestone: read-only проверка logical port inventory и OEM делегирование; аппаратное состояние RUNTIME_TEST_REQUIRED. Не hardware verified. После успешного CI статус этого milestone — BUILT_UNTESTED, PR draft.

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

OemHdmiPortInfo только читает board identity и TYPE_HDMI TV Input list. Показывает две read-only строки при валидной metadata, отдельно отмечает отсутствующий/неоднозначный input; при неверной metadata не выдумывает HDMI-порты. Нет tune(), property/Settings writes, CEC API или EDID setter.

Legacy MOD EDID selector остаётся скрыт на 8054 (EdidPortPolicy). Добавлена явная ссылка в оригинальный раздел источников: нужный источник и EDID выбираются пользователем там. Это переход в source page, не автоматический выбор порта или direct EDID control.

Legacy MOD CEC разрешён только на исходной 8052; на 8054/unknown Bridge не создаётся и raw CEC/eARC операции не выполняются. Причина: текущий legacy transaction после смены CEC может выключить auto-device-off, вызвать setEarcEnable(false) и обновить Global eARC key; частичный отказ оставляет побочные изменения, а обратный transaction на 8054 не проверен. OEM CEC экран сохраняет подтверждения и управление зависимостями. eARC остаётся в OEM sound page; game mode в OEM source page (известен resetTrapezoid side effect). Критические службы не отключались. Boot-source/autoplay контролы этим milestone не переделываются и не считаются доказательством active input switching.

## Обновление состояния портов

Строки HDMI 1/2 перечитывают состояние TV Input при возвращении фокуса окну, в том числе если OEM отказал в регистрации callback. Срок жизни focus-listener привязан к текущему View. Это актуализирует состояние соединения и не помечает его как активный вход. JVM регрессия покрывает отказ регистрации, повторный attach, возврат фокуса и снятие подписок.

Выбор HDMI1/2 требует подтверждённого штатного маршрута, который различает HW6/HW7. Сопоставление `TvInputInfo` само по себе не определяет аргументы OEM-команды переключения. Exact workflow трассирует только вызовы выбора источника в декодированных штатных TvSettingsPlus и KuSourceRJ. До подтверждения входного контракта строки остаются read-only. Отдельные блокеры EDID, CEC и game mode сохраняются.

## Тесты, откат, device-план

Hdmi8054PolicyTest: exact HW6/HW7 mapping, four registered inputs versus two physical ports, reverse metadata order, wrong labels/count, missing metadata, 8052 rejection, absent input, spoofed owner, duplicate registrations и CEC gate 8054/unknown/8052. API33 compile/общий JVM regression/exact-8054 build — одним пакетом subsystem. Это не проверка HDMI сигнала/handshake.

Откат исходников: revert subsystem commit. Новых device writes от read-only inventory нет. Для device tests после отдельного разрешения сохранить исходные active input, EDID, CEC/auto-off/eARC/аудиовыход и коррекцию. По одному физическому HDMI проверить OEM выбор → HW6/HW7, signal и EDID before/after только через OEM UI. Не считать показания фиксированного query(6) подтверждением HW7. При CEC изменении отдельно проверить auto-off и возврат eARC/выхода (включение CEC само по себе не обязано вернуть eARC). Game mode не переключать в общем EDID тесте. Возвращать исходные настройки через OEM UI и сверять signal/audio; не делать mass Global replay. Все hardware результаты пока NOT_TESTED.

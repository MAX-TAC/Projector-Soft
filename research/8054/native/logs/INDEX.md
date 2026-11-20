# Индекс журнала Native API — FlixTone exact-8054

**Рабочая ветка:** `research/8054-mediatek-native-api-20261120`  
**Отдельный Draft PR:** [#3](https://github.com/MAX-TAC/Projector-Soft/pull/3)  
**Основной MOD:** [PR #2](https://github.com/MAX-TAC/Projector-Soft/pull/2), v41 / `BUILT_UNTESTED` (не считать эти данные подтверждением установки на устройство).

## Записи фактически выполненных работ

| Дата | Журнал | Подтверждённые результаты | Открытый вопрос |
|---|---|---|---|
| 2026-11-20 | [Начальная ревизия полного дампа, CI, JNI и Factory](2026-11-20_INITIAL_NATIVE_AUDIT.md) | ZIP и SHA подтверждены, 1 283 ELF, конкретные JNI/config exports, 3 трассы | Машинный call edge / HIDL target / реальный PQ input context |
| 2026-11-20 | [Передача исследования и настройка журналов](2026-11-20_LOGGING_AND_HANDOFF.md) | AGENTS, HANDOFF, промпт нового чата, исправлен README, коммиты записаны | История до NATIVE-004 |\n| 2026-11-20 | [NATIVE-004: дизассемблирование и PLT/GOT](2026-11-20_NATIVE-004.md) | JNI BLX 0x7ae90 → GOT 0x7dff8 → `a_mtktvapi_config_set_value`; ABI signed16/string/int/signed16; CI SUCCESS | HIDL service receiver, значения -1 и 3; аппаратный эффект |

## Трассировки и статус

| ID | Функция/уровень | Состояние | Документ |
|---|---|---|---|
| 001 | Контраст изображения, OEM UI → TvServices → JNI Java | `JAVA_STATIC_TRACE_CONFIRMED` | [TRACE_001](../TRACE_001_PICTURE_CONTRAST.md) |
| 002 | Инвентаризация vendor JNI, HIDL, RPC, Factory, CEC, ToF/I²C | `EXACT_NATIVE_ELF_INVENTORIED` | [TRACE_002](../TRACE_002_NATIVE_ELF_BOUNDARIES.md) |
| 003 | Реальные JNI/config symbols в exact-8054 ELF | `EXACT_NATIVE_ELF_EXPORT_CONFIRMED` | [TRACE_003](../TRACE_003_MEDIATEK_CONFIG_JNI.md) |
| 004 | JNI `setConfigValue_native` → конкретный C wrapper callsite | **`NATIVE_CALL_EDGE_PROVEN_STATIC`** | [TRACE_004](../TRACE_004_CONFIG_JNI_TO_HIDL.md) · [ELF evidence](../generated/NATIVE_004_ELF_MACHINE_EVIDENCE_8054.md) |
| 005 | HIDL vtable/receiver; `inputGroup=-1`, `isUpdate=3`, HDMI/PQ | **`NEXT`** | Восстановить target C-wrapper `0x3f5cc blx r7` → BpHw/BnHw → service; выяснить значения sentinel/update |
| 006 | HDR + MEMC: OEM handler, config, native | `NOT_STARTED` | Исследовать раздельно с собственными trace ID |
| 007 | Звук: профили, EQ5, Factory PEQ13 (без записи) | `NOT_STARTED` | Разделять пользовательский и заводской путь |

## GitHub Actions (настоящее состояние)

| ID | Исход | Причина/подтверждение |
|---|---|---|
| [44792807063](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44792807063) | SUCCESS | Первый native ELF audit, EROFS, санированный отчёт |
| [44793339068](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44793339068) | FAILURE | Ошибка regex в Python: `unbalanced parenthesis`, не проблема native |
| [44793791027](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44793791027) | SUCCESS | Исправление regex, приоритетные экспорты JNI/config подтверждены |\n| [44798376982](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44798376982) | SUCCESS | Первый JNI disassembly; BLX 0x7ae90, отсутствует имя PLT в objdump |\n| [44798924086](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44798924086) | SUCCESS (collector false-negative) | Регулярные выражения нового PLT-сборщика ошибочно экранированы; ошибка фиксации вызова, не ELF |\n| [44799202257](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44799202257) | SUCCESS | После исправления regex: секции PLT и JMP_SLOT, слот 0x7dff8 |\n| [44799645224](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44799645224) | **SUCCESS / PROVEN_STATIC** | Машинно декодированный ARM PLT→GOT→`a_mtktvapi_config_set_value`; текстовый отчёт закоммичен |

## В каждом новом сеансе

1. Проверить текущий HEAD PR #3, реальные последние workflow runs и ссылку на свежую версию артефактов.
2. Прочитать [HANDOFF.md](../HANDOFF.md) и [AGENTS.md](../AGENTS.md).
3. Создать `logs/YYYY-MM-DD_TASK-ID.md`, записать гипотезы и стартовые SHA.
4. Выполнить следующую отдельную задачу (NATIVE-005) — vtable/HIDL receiver и семантика -1/3; после неё отдельные HDR/MEMC трассы.
5. Записать найденные и ненайденные вызовы, доказательства и границы проверки в TRACE.
6. Закоммитить TRACE, журнал и изменение этого индекса; проверить коммиты в PR.
7. Не объявлять `HARDWARE_EFFECT_VERIFIED` до измерений и разрешённого теста на самом проекторе.

Подробный формат журнала определён в [AGENTS.md](../AGENTS.md).

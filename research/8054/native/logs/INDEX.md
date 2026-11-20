# Индекс журнала Native API — FlixTone exact-8054

**Рабочая ветка:** `research/8054-mediatek-native-api-20261120`  
**Отдельный Draft PR:** [#3](https://github.com/MAX-TAC/Projector-Soft/pull/3)  
**Основной MOD:** [PR #2](https://github.com/MAX-TAC/Projector-Soft/pull/2), v41 / `BUILT_UNTESTED` (не считать эти данные подтверждением установки на устройство).

## Записи фактически выполненных работ

| Дата | Журнал | Подтверждённые результаты | Открытый вопрос |
|---|---|---|---|
| 2026-11-20 | [Начальная ревизия полного дампа, CI, JNI и Factory](2026-11-20_INITIAL_NATIVE_AUDIT.md) | ZIP и SHA подтверждены, 1 283 ELF, конкретные JNI/config exports, 3 трассы | Машинный call edge / HIDL target / реальный PQ input context |

## Трассировки и статус

| ID | Функция/уровень | Состояние | Документ |
|---|---|---|---|
| 001 | Контраст изображения, OEM UI → TvServices → JNI Java | `JAVA_STATIC_TRACE_CONFIRMED` | [TRACE_001](../TRACE_001_PICTURE_CONTRAST.md) |
| 002 | Инвентаризация vendor JNI, HIDL, RPC, Factory, CEC, ToF/I²C | `EXACT_NATIVE_ELF_INVENTORIED` | [TRACE_002](../TRACE_002_NATIVE_ELF_BOUNDARIES.md) |
| 003 | Реальные JNI/config symbols в exact-8054 ELF | `EXACT_NATIVE_ELF_EXPORT_CONFIRMED` | [TRACE_003](../TRACE_003_MEDIATEK_CONFIG_JNI.md) |
| 004 | JNI `setConfigValue_native` → реальный C wrapper callsite | `NOT_STARTED` | Создать `TRACE_004_...` по результатам |
| 005 | `inputGroup=-1`, `isUpdate=3`, адресация HDMI/PQ | `NOT_STARTED` | Создать `TRACE_005_...` по результатам |
| 006 | HDR + MEMC: OEM handler, config, native | `NOT_STARTED` | Исследовать раздельно с собственными trace ID |
| 007 | Звук: профили, EQ5, Factory PEQ13 (без записи) | `NOT_STARTED` | Разделять пользовательский и заводской путь |

## GitHub Actions (настоящее состояние)

| ID | Исход | Причина/подтверждение |
|---|---|---|
| [44792807063](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44792807063) | SUCCESS | Первый native ELF audit, EROFS, санированный отчёт |
| [44793339068](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44793339068) | FAILURE | Ошибка regex в Python: `unbalanced parenthesis`, не проблема native |
| [44793791027](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44793791027) | SUCCESS | Исправление regex, приоритетные экспорты JNI/config подтверждены |

## В каждом новом сеансе

1. Проверить текущий HEAD PR #3, реальные последние workflow runs и ссылку на свежую версию артефактов.
2. Прочитать [HANDOFF.md](../HANDOFF.md) и [AGENTS.md](../AGENTS.md).
3. Создать `logs/YYYY-MM-DD_TASK-ID.md`, записать гипотезы и стартовые SHA.
4. Выполнить статический анализ ровно одной недостающей функции/перехода.
5. Записать найденные и ненайденные вызовы, доказательства и границы проверки в TRACE.
6. Закоммитить TRACE, журнал и изменение этого индекса; проверить коммиты в PR.
7. Не объявлять `HARDWARE_EFFECT_VERIFIED` до измерений и разрешённого теста на самом проекторе.

Подробный формат журнала определён в [AGENTS.md](../AGENTS.md).

# Индекс журнала Native API — FlixTone exact-8054

**Рабочая ветка:** `research/8054-mediatek-native-api-20261120`  
**Отдельный Draft PR:** [#3](https://github.com/MAX-TAC/Projector-Soft/pull/3)  
**Основной MOD:** [PR #2](https://github.com/MAX-TAC/Projector-Soft/pull/2), v41 / `BUILT_UNTESTED` (не считать эти данные подтверждением установки на устройство).

## Записи фактически выполненных работ

| Дата | Журнал | Подтверждённые результаты | Открытый вопрос |
|---|---|---|---|
| 2026-11-20 | [Начальная ревизия полного дампа, CI, JNI и Factory](2026-11-20_INITIAL_NATIVE_AUDIT.md) | ZIP и SHA подтверждены, 1 283 ELF, конкретные JNI/config exports, 3 трассы | Машинный call edge / HIDL target / реальный PQ input context |
| 2026-11-20 | [Передача исследования и настройка журналов](2026-11-20_LOGGING_AND_HANDOFF.md) | AGENTS, HANDOFF, промпт нового чата, исправлен README, коммиты записаны | История до NATIVE-004 |
| 2026-11-20 | [NATIVE-004: дизассемблирование и PLT/GOT](2026-11-20_NATIVE-004.md) | JNI BLX 0x7ae90 → GOT 0x7dff8 → `a_mtktvapi_config_set_value`; ABI signed16/string/int/signed16; CI SUCCESS | HIDL service receiver, значения -1 и 3; аппаратный эффект |
| 2026-11-20 | [NATIVE-005: HIDL service → RPC](2026-11-20_NATIVE-005.md) | exact service ELF SHA, init/VINTF, config API HIDL proxy/stub, service BLX 0x29e48 → PLT 0x43490 → GOT 0x47fa0, [final CI](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44804034895) SUCCESS | C-wrapper vtable→BpHw→server implementation, -1 / 3 semantics |
| 2026-11-20 | [Product direction: нужен working PIC04, не абстрактный HIDL](2026-11-20_PRODUCT_DIRECTION_REVIEW.md) | Проанализированы TRACE_004/005, 96-функционная матрица, код v41; 0 app-файлов в PR #3 | **PRODUCT-001 / PIC04 — NEXT**; NATIVE-005b deferred |

## Трассировки и статус

| ID | Функция/уровень | Состояние | Документ |
|---|---|---|---|
| 001 | Контраст изображения, OEM UI → TvServices → JNI Java | `JAVA_STATIC_TRACE_CONFIRMED` | [TRACE_001](../TRACE_001_PICTURE_CONTRAST.md) |
| 002 | Инвентаризация vendor JNI, HIDL, RPC, Factory, CEC, ToF/I²C | `EXACT_NATIVE_ELF_INVENTORIED` | [TRACE_002](../TRACE_002_NATIVE_ELF_BOUNDARIES.md) |
| 003 | Реальные JNI/config symbols в exact-8054 ELF | `EXACT_NATIVE_ELF_EXPORT_CONFIRMED` | [TRACE_003](../TRACE_003_MEDIATEK_CONFIG_JNI.md) |
| 004 | JNI `setConfigValue_native` → конкретный C wrapper callsite | **`NATIVE_CALL_EDGE_PROVEN_STATIC`** | [TRACE_004](../TRACE_004_CONFIG_JNI_TO_HIDL.md) · [ELF evidence](../generated/NATIVE_004_ELF_MACHINE_EVIDENCE_8054.md) |
| 005 | HIDL proxy/stub, exact service, config setter service→RPC call | **`STATIC_SERVICE_TO_RPC_CALL_PROVEN / HIDL_HANDLER_EDGE_PARTIAL`** | [TRACE_005](../TRACE_005_CONFIG_HIDL_SERVICE_RPC.md) · [ELF evidence](../generated/NATIVE_005_HIDL_RECEIVER_EVIDENCE_8054.md) |
| 005b | C-wrapper concrete vtable→HIDL implementation, sender→RPC receiver, -1/3 semantics | **`DEFERRED`** | [Граница NATIVE-005 и следующий этап](../TRACE_005_CONFIG_HIDL_SERVICE_RPC.md#8-next-concrete-follow-up-native-005b--before-hdr--memc) |
| PRODUCT-001 | Пользовательская контрастность PIC04: контракт STOCK→MOD, контроль и откат | **`NEXT`** | [Решение](../PRODUCT_FOCUS_REVIEW_2026-11-20.md) · [Промпт](../PROMPT_PRODUCT_PIVOT.md) |
| 006 | HDR + MEMC: OEM handler, config, native | `NOT_STARTED` | Исследовать раздельно с собственными trace ID |
| 007 | Звук: профили, EQ5, Factory PEQ13 (без записи) | `NOT_STARTED` | Разделять пользовательский и заводской путь |

## GitHub Actions (настоящее состояние)

| ID | Исход | Причина/подтверждение |
|---|---|---|
| [44792807063](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44792807063) | SUCCESS | Первый native ELF audit, EROFS, санированный отчёт |
| [44793339068](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44793339068) | FAILURE | Ошибка regex в Python: `unbalanced parenthesis`, не проблема native |
| [44793791027](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44793791027) | SUCCESS | Исправление regex, приоритетные экспорты JNI/config подтверждены |
| [44798376982](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44798376982) | SUCCESS | Первый JNI disassembly; BLX 0x7ae90, отсутствует имя PLT в objdump |
| [44798924086](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44798924086) | SUCCESS (collector false-negative) | Регулярные выражения нового PLT-сборщика ошибочно экранированы; ошибка фиксации вызова, не ELF |
| [44799202257](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44799202257) | SUCCESS | После исправления regex: секции PLT и JMP_SLOT, слот 0x7dff8 |
| [44799645224](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44799645224) | **SUCCESS / PROVEN_STATIC** | Машинно декодированный ARM PLT→GOT→`a_mtktvapi_config_set_value`; текстовый отчёт закоммичен |
| [44801986683](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44801986683) | SUCCESS | Начальный HIDL/service ELF audit: 21 filename, 6 HIDL methods, 4 vtables |
| [44802499818](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44802499818) и [44802917321](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44802917321), [44803303478](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44803303478), [44803774847](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44803774847) | FAILURE (publication only) | Non-fast-forward при параллельных пушах результатов; устранено сериализацией workflow и rebase, не ошибка ELF |
| [44804034895](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44804034895) | **SUCCESS / SERVICE_CALL_PROVEN_STATIC** | HIDL transaction 3, exact service/import; ARM service BLX→PLT→GOT setter `0x29e48→0x43490→0x47fa0`; санированный отчёт в PR #3 |

## В каждом новом сеансе

1. Проверить текущий HEAD PR #3, реальные последние workflow runs и ссылку на свежую версию артефактов.
2. Прочитать [HANDOFF.md](../HANDOFF.md) и [AGENTS.md](../AGENTS.md).
3. Создать `logs/YYYY-MM-DD_TASK-ID.md`, записать гипотезы и стартовые SHA.
4. **Сменён приоритет по запросу владельца:** выполнить PRODUCT-001 / PIC04 (STOCK callback → MOD action → test/rollback); NATIVE-005b отложена до конкретного продуктового блокера. Подробности в PRODUCT_FOCUS_REVIEW и PROMPT_PRODUCT_PIVOT.
5. Записать найденные и ненайденные вызовы, доказательства и границы проверки в TRACE.
6. Закоммитить TRACE, журнал и изменение этого индекса; проверить коммиты в PR.
7. Не объявлять `HARDWARE_EFFECT_VERIFIED` до измерений и разрешённого теста на самом проекторе.

Подробный формат журнала определён в [AGENTS.md](../AGENTS.md).

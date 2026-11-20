# HANDOFF — exact-8054 MediaTek Native API

Срез: 2026-11-20. Перед работой перечитать актуальный HEAD PR #3 и GitHub Actions; сведения могут измениться.

## Цель владельца
Сохранить согласованный восьмираздельный интерфейс MainSettings / TvSettingsPlus MOD v41 и сделать реально работающими пользовательские PQ/HDR/MEMC, профили звука/EQ, источники/HDMI/CEC, проекцию и связанные переключатели. Сейчас UI почти готов, но аппаратное применение большинства настроек не проверено. Исследовать по одному пункту, а не писать новый UI вслепую.

## Репозитории и source of truth
- Projector-Soft: https://github.com/MAX-TAC/Projector-Soft
- Ветка исследований: research/8054-mediatek-native-api-20261120
- Draft PR #3: https://github.com/MAX-TAC/Projector-Soft/pull/3
- Разработка MOD v41 / BUILT_UNTESTED: https://github.com/MAX-TAC/Projector-Soft/pull/2 (не изменять без новой задачи).
- Исходная полная прошивка владельца: https://github.com/MAX-TAC/FlixTone_8054_Backup/releases/tag/FlixTone_8054_Backup
- Имя ZIP: FlixTone_8054_Backup.zip; 2 011 878 129 байт; SHA-256: 4f53f2224c98b4c832a19a591b9dd7f925bde28528b5674a09732c0f0e41d362.
- Содержимое: super.img с exact-8054 EROFS system_a / vendor_a / product_a / system_ext_a. Дамп 8052 как источник истины не использовать.

## Порядок изучения файлов
1. [Инструкция агенту](AGENTS.md) и [журнал/план](logs/INDEX.md).
2. [Первичный журнал действий](logs/2026-11-20_INITIAL_NATIVE_AUDIT.md).
3. [Контраст](TRACE_001_PICTURE_CONTRAST.md), [native ELF](TRACE_002_NATIVE_ELF_BOUNDARIES.md), [JNI config](TRACE_003_MEDIATEK_CONFIG_JNI.md).
4. [Генерируемые результаты](generated/NATIVE_API_CANDIDATES_8054.json), [ELF inventory](generated/ELF_LIBRARY_INVENTORY_8054.csv).
5. [Settings↔config ID](../settings_routes_8054.json), [PQ](../PICTURE_BLOCK_STATUS_8054.md), [EQ](../AUDIO_EQ_PQ_STATIC_ROUTE_8054.md), [EDID](../EDID_GLOBAL_ROUTE_8054.md).
6. [Меню v41](../../../docs/8054_FINAL_MENU_SPEC.md), [известные ограничения](../../../docs/8054_KNOWN_LIMITATIONS.md), [аппаратный тест](../../../docs/8054_PQ_EQ_V41_TEST.md).

## Подтверждённые результаты
- Дамп прошёл SHA-256 в Actions; извлечены четыре раздела. Просканировано 1 283 ELF .so: 919 system, 323 vendor, 6 product, 35 system_ext; 300 библиотек-кандидатов.
- Контраст: picture_contrast → g_video__contrast → TVMenuSettingsService / TVSettingConfig → MtkTvConfigBase → TVNativeWrapper.setConfigValue_native(-1, configId, value, 3). Это статический Java/smali маршрут.
- Оригинальный vendor_a/lib/libcom_mediatek_twoworlds_tv_jni.so, SHA-256 312fef3f17f656ac957a32dfee8187798dacac8f4df83e8219b1553b7248ab24, экспортирует Java_com_mediatek_twoworlds_tv_TVNative_setConfigValue_1native и getConfigValue_1native.
- **NATIVE-004 завершён на уровне машинного JNI→C вызова:** original JNI `0x56726 blx 0x7ae90` → ARM PLT `0x7ae90` → GOT `0x7dff8` (`R_ARM_JUMP_SLOT a_mtktvapi_config_set_value`), **`NATIVE_CALL_EDGE=PROVEN_STATIC`**. Отличный `...set_value_for_all_elems` слот — `0x7dffc`. Результат вычислен в [TRACE_004](TRACE_004_CONFIG_JNI_TO_HIDL.md) и [автоматическом ELF отчёте](generated/NATIVE_004_ELF_MACHINE_EVIDENCE_8054.md), [CI 44799645224](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44799645224).
- Factory HIDL, CEC HAL, ToF/TRD, I2C — разные интерфейсы. Нельзя приравнивать заводскую API к пользовательской и предполагать единый API для всего оборудования.
- Успешные runs: https://github.com/MAX-TAC/Projector-Soft/actions/runs/44792807063 и https://github.com/MAX-TAC/Projector-Soft/actions/runs/44793791027 . Ошибка run 44793339068 связана с Python regex и уже исправлена.

## NATIVE-004: достигнутый результат (2026-11-20)

- Отчёт: [TRACE_004_CONFIG_JNI_TO_HIDL.md](TRACE_004_CONFIG_JNI_TO_HIDL.md).
- Журнал реальных действий, команд и исправлений сборщика: [logs/2026-11-20_NATIVE-004.md](logs/2026-11-20_NATIVE-004.md); [индекс](logs/INDEX.md).
- Машинные данные: [NATIVE_004_ELF_MACHINE_EVIDENCE_8054.md](generated/NATIVE_004_ELF_MACHINE_EVIDENCE_8054.md). Финальный [run 44799645224](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44799645224) SUCCESS.
- Переход JNI→C setter **доказан** по ARM `BLX 0x7ae90`, PLT арифметике и `R_ARM_JUMP_SLOT` at `0x7dff8`. Это per-element API symbol `a_mtktvapi_config_set_value`, **не** `set_value_for_all_elems`.
- У аргументов в JNI: signed 16-bit `inputGroup`, UTF-строка ID, 32-bit value, signed 16-bit `isUpdate`. JNI преобразует `jstring` через `GetStringUTFChars` и возвращает статус, но null-ветки могут вернуть ноль без вызова C.
- В C-wrapper подтверждена виртуальная диспетчеризация `0x3f5cc blx r7` (vtable offset `+0x3c`), а в оригинальной HIDL `.so` — proxy/stub с типами `short, hidl_string, int, short`. **Виртуальный target/service implementation/transaction не установлены**. `inputGroup=-1`, `isUpdate=3` пока только передаваемые значения, не доказанная семантика.
- No runtime, no APK install, no hardware modifications. PR #2 неизменён; MOD v41 `BUILT_UNTESTED`.

## NATIVE-005: итоги статического анализа (2026-11-20)

- [TRACE_005_CONFIG_HIDL_SERVICE_RPC.md](TRACE_005_CONFIG_HIDL_SERVICE_RPC.md), [проверяемые инструкции, vtable/relocs](generated/NATIVE_005_HIDL_RECEIVER_EVIDENCE_8054.md), [журнал действий и реальных CI-сбоев](logs/2026-11-20_NATIVE-005.md).
- **Final run:** [44804034895](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44804034895) **SUCCESS**: SHA-verified original archive → EROFS → exact-8054 ELF → sanitized text output pushed to research branch. В workflow добавлены сериализация и rebase для устранения `non-fast-forward` при параллельной публикации исследований.
- Обнаружен настоящий `vendor_a/bin/hw/vendor.mediatek.api.mtktvapi@1.0-service`, SHA-256 `15360fa27a7039f57f6173cc28942d4458e9f4d0beeaa31620e71bc785f275f6`; статическая регистрация в `vendor_a/etc/init/vendor.mediatek.tv.mtktvapi@1.0-service.rc`, VINTF `vendor.mediatek.api.mtktvapi`, импорт `IMtkTvApiConfig::registerAsService`.
- Сервис связан с `vendor.mediatek.api.mtktvapi@1.0.so` и `libmediatek_tv_rpcwrapper.so`; его ELF импортирует `a_mtktvapi_config_set_value` (`R_ARM_JUMP_SLOT` GOT `0x47fa0`). **Реальная машинная ветка найдена:** service `0x29e48 BLX 0x43490`, ARM PLT `0x43490` → GOT `0x47fa0` → `a_mtktvapi_config_set_value`. Этот переход подтверждён программно с учётом ARM rotated immediate `#4,20 = 0x4000`.
- Оригинальный HIDL BpHw setter `0x24a230 movs r1,#3` → `0x24a232 blx r6` показывает transaction code `3` в Binder-запросе. **Не путать с payload `isUpdate=3`**. BnHw `onTransact` и code 1..15 switch есть, но конкретный маршрут к service `0x29e48` ещё **не** доказан.
- C-wrapper `0x3f5cc blx r7` с vtable +0x3c существует; `getMtkTvApiConfigService()` экспортирована в оригинальном ELF; **точный тип объекта и соответствие BpHw/Bs/серверной реализации не доказаны**. Простой vtable test `_ZTV +8+60` не дал надежной связи из-за возможного вторичного address point/релокаций.
- `inputGroup=-1` и `isUpdate=3`: знак и ширина `short` передаются корректно, но семантика активного входа, SDR/HDR, профиля PQ и сохранения **UNKNOWN**. `READBACK_CONFIRMED` и `HARDWARE_EFFECT_VERIFIED` остаются `NOT_TESTED`. MOD v41/PR #2 не менялись.

## Следующая конкретная задача: NATIVE-005b — связать разорванные edges

1. **Не начинать с нуля.** Читать TRACE_005 и ограниченный машинный отчёт; reuse exact SHA и тот же CI; не надо повторно искать архив.
2. Из C-wrapper проверить вызов `0x3f5a6 BLX 0x60090` через PLT/GOT, дальнейший `getMtkTvApiConfigService` → динамический HIDL объект → верный C++ vtable address point. Раскрыть, что реально лежит в `[vptr+0x3c]`. Не приравнивать простой `_ZTV+8+60` к нужному слоту без ABI-доказательств.
3. В service `vendor.mediatek.api.mtktvapi@1.0-service` восстановить **function owner** у `0x29e48` и входящую диспетчеризацию из HIDL setter (onTransact code 3); затем trace `libmediatek_tv_rpcwrapper.so` → `libmediatek_tv_rpccore.so`.
4. Искать **реального потребителя** signed `inputGroup=-1` и `isUpdate=3`. Если обработчик живёт за границей Android ELF/в firmware, явно зафиксировать предел статической проверки; не придумывать режимы HDMI или persistence.
5. Дополнительно получить точную DEX `TVNative.setConfigValue_native` declaration/Java status propagation. После выполнения NATIVE-005b отдельными трассами начать HDR и MEMC, не смешивая с Factory API.
6. Отдельный `logs/YYYY-MM-DD_NATIVE-005b.md`, TRACE, обновление INDEX/HANDOFF, коммит в PR #3. Никаких тестовых записей/установок APK без отдельного разрешения.

## Строгие ограничения
- Не менять main, MOD v41, заводскую прошивку и проектор в рамках статического исследования.
- Без разрешения владельца не делать settings put, setprop, root/су, запись I2C, EDID/HPD, firmware, fan/thermal, factory calibration/PEQ save, reset и установку APK.
- Публичному GitHub — только санированные отчёты, SHA, команды, минимальный дизассемблерный контекст; никакие сырые прошивки, ключи или индивидуальные секреты.
- Уровни фактов разделять: STATIC_KEY_MAP → JAVA_SMALI_CALLSITE → EXACT_NATIVE_ELF_EXPORT → NATIVE_CALL_EDGE → HIDL_RPC_RECEIVER → READBACK_CONFIRMED → HARDWARE_EFFECT_VERIFIED. Сейчас последнее не достигнуто.
- Результаты писать в файлы репозитория, не в чат, ответ заканчивать ссылками на конкретный коммит/PR. Нельзя обещать фоновую или будущую работу, только выполнять в текущем сеансе.

Результат следующего чата: NATIVE-005b с точным C-wrapper→HIDL и BnHw→service call edge, либо честным blocker, отдельным журналом, CI-ссылкой и commit SHA; никакой выдуманной семантики `-1/3` или hardware effect.

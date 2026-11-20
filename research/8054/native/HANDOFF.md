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
- vendor.mediatek.api.mtktvapi@1.0-cwrapper.so и libmediatek_tv_rpcwrapper.so экспортируют a_mtktvapi_config_set_value / get_value; JNI DT_NEEDED включает C-wrapper. Непосредственная машинная связь ещё НЕ ДОКАЗАНА.
- Factory HIDL, CEC HAL, ToF/TRD, I2C — разные интерфейсы. Нельзя приравнивать заводскую API к пользовательской и предполагать единый API для всего оборудования.
- Успешные runs: https://github.com/MAX-TAC/Projector-Soft/actions/runs/44792807063 и https://github.com/MAX-TAC/Projector-Soft/actions/runs/44793791027 . Ошибка run 44793339068 связана с Python regex и уже исправлена.

## Следующая задача: NATIVE-004 — JNI → C-wrapper → HIDL
1. Прочитать текущую ветку и материалы, создать logs/YYYY-MM-DD_NATIVE-004.md с целью, референсами и SHA.
2. Разобрать exact vendor JNI ELF через readelf -h -Ws -r -d, objdump/llvm-objdump -dr, PLT/GOT, при необходимости Ghidra/Rizin.
3. Найти реальный callsite функции Java_com_mediatek_twoworlds_tv_TVNative_setConfigValue_1native и точный C-wrapper target: a_mtktvapi_config_set_value или альтернативный.
4. Сопоставить Java JNI сигнатуры, jstring и ARM ABI, возвращаемые статусы, inputGroup=-1 и isUpdate=3; не гадать об активном HDMI.
5. Если найден реальный C target — проследить HIDL method/transaction/receiver и различить per-element vs all-elements.
6. Записать TRACE_004_CONFIG_JNI_TO_HIDL.md с проверяемыми адресами, командами, доказанными/неустановленными переходами; обновить журнал и logs/INDEX.md; сделать коммит в PR #3.
7. Далее отдельными задачами HDR picture_list_hdr → g_video__vid_hdr, MEMC tv_picture_advance_video_mjc_effect → g_video__vid_mjc_effect, RGB, sound_style/EQ5, HDMI/CEC, проекция и охлаждение.

## Строгие ограничения
- Не менять main, MOD v41, заводскую прошивку и проектор в рамках статического исследования.
- Без разрешения владельца не делать settings put, setprop, root/су, запись I2C, EDID/HPD, firmware, fan/thermal, factory calibration/PEQ save, reset и установку APK.
- Публичному GitHub — только санированные отчёты, SHA, команды, минимальный дизассемблерный контекст; никакие сырые прошивки, ключи или индивидуальные секреты.
- Уровни фактов разделять: STATIC_KEY_MAP → JAVA_SMALI_CALLSITE → EXACT_NATIVE_ELF_EXPORT → NATIVE_CALL_EDGE → HIDL_RPC_RECEIVER → READBACK_CONFIRMED → HARDWARE_EFFECT_VERIFIED. Сейчас последнее не достигнуто.
- Результаты писать в файлы репозитория, не в чат, ответ заканчивать ссылками на конкретный коммит/PR. Нельзя обещать фоновую или будущую работу, только выполнять в текущем сеансе.

Результат следующего чата: TRACE_004, журнал выполненных действий, обновлённый индекс и реальные GitHub commit SHA/URL.

# Журнал сеанса 2026-11-20 — исходная прошивка 8054, MediaTek Native

**Тип:** исследование / только чтение. **Исполнитель:** ChatGPT с доступом к GitHub connector. **Устройство:** FlixTone D11 Master 8054, Android TV 13, MT5873/MT9660 семейство (название семейства требует контекстного уточнения, не подменять board ID). **Проектор не подключался через ADB в этом сеансе.**

## Цели

1. Проверить заявление, что полный дамп 8054 уже существует.
2. Восстановить доступную цепочку MediaTek Java → JNI → native ELF.
3. Зафиксировать результаты в отдельной исследовательской ветке, не меняя MOD v41.
4. Организовать воспроизводимый сбор доказательств, без публикации исходных приватных бинарников.

## Хронология и фактические действия

| № | Действие | Источник/выполнение | Результат | Доказательство |
|---:|---|---|---|---|
| 01 | Прочитана актуальная ветка Draft PR #2 | `MAX-TAC/Projector-Soft`, базовый HEAD `fbb41e92a47a94a2a4b0f4122100e5a1cb43cfe5` | Исходники v41 не менялись | [PR #2](https://github.com/MAX-TAC/Projector-Soft/pull/2) |
| 02 | Найден полный оригинальный дамп | GitHub Release `MAX-TAC/FlixTone_8054_Backup` | ZIP 2 011 878 129 байт, SHA-256 `4f53f2224c98b4c832a19a591b9dd7f925bde28528b5674a09732c0f0e41d362` | [Release](https://github.com/MAX-TAC/FlixTone_8054_Backup/releases/tag/FlixTone_8054_Backup) |
| 03 | Проверены предыдущие материалы исследования | Сохранённый Factory-аудит, инструменты извлечения `super.img` / EROFS | Полные системные разделы `system_a/vendor_a/product_a/system_ext_a` уже использовались для анализа 88 APK; утверждение об отсутствии native-источников неверно | [Factory audit](https://github.com/MAX-TAC/FlixTone_8054_Backup/actions/runs/43968729249) |
| 04 | Создана изолированная исследовательская ветка | `research/8054-mediatek-native-api-20261120` от `fbb41e92...` | Изоляция от MOD, начало каталога | [Ветка](https://github.com/MAX-TAC/Projector-Soft/tree/research/8054-mediatek-native-api-20261120) |
| 05 | Записаны документация и Java-трасса | `README.md`, `TRACE_001_PICTURE_CONTRAST.md` | Подтверждена статическая цепочка `picture_contrast` → TvServices → `MtkTvConfigBase` → JNI, runtime не подтверждён | [TRACE_001](../TRACE_001_PICTURE_CONTRAST.md) |
| 06 | Добавлен native-аудитор и workflow | `tools/audit_mediatek_native_8054.py`, `.github/workflows/audit-8054-mediatek-native.yml` | Проверяет SHA-256 ZIP, извлекает разделы, читает ELF metadata / allowlisted names и записывает text-only результаты в ветку | [Workflow](https://github.com/MAX-TAC/Projector-Soft/blob/research/8054-mediatek-native-api-20261120/.github/workflows/audit-8054-mediatek-native.yml) |
| 07 | Первый полный запуск | Run `44792807063` | **SUCCESS**: 1 283 .so, 300 кандидатов, 62 с тематическими экспортами, 32 со строками из allowlist | [Run](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44792807063) |
| 08 | Зафиксированы конкретные файлы и SHA | `TRACE_002_NATIVE_ELF_BOUNDARIES.md` | `libcom_mediatek_twoworlds_tv_jni.so` и `mtktvapi@1.0-cwrapper.so`, Factory HIDL, CEC, I²C | [TRACE_002](../TRACE_002_NATIVE_ELF_BOUNDARIES.md) |
| 09 | Попытка повысить точность JNI-селектора | Run `44793339068` | **FAILURE** из-за `re.PatternError: unbalanced parenthesis` в новой Python regex, не ошибка прошивки | [Run](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44793339068) |
| 10 | Исправлена regex и запущена проверка | Коммит `92d5c4bc2e2f2d33ba6d8bc10351ca04283ff597`, run `44793791027` | **SUCCESS**: подтверждены native exports `TVNative_getConfigValue_1native`, `TVNative_setConfigValue_1native`, `a_mtktvapi_config_get_value`, `a_mtktvapi_config_set_value` | [Run](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44793791027) |
| 11 | Зафиксирована точная JNI-трасса и PR | `TRACE_003_MEDIATEK_CONFIG_JNI.md`, Draft PR #3 | Исследовательские материалы доступны для ревью; прямое машинное call edge JNI → C-wrapper **не доказано** | [TRACE_003](../TRACE_003_MEDIATEK_CONFIG_JNI.md) · [PR #3](https://github.com/MAX-TAC/Projector-Soft/pull/3) |

## Проверенные точные бинарники

- `vendor_a/lib/libcom_mediatek_twoworlds_tv_jni.so`, SHA-256 `312fef3f17f656ac957a32dfee8187798dacac8f4df83e8219b1553b7248ab24`.
- `vendor_a/lib/vendor.mediatek.api.mtktvapi@1.0-cwrapper.so`, SHA-256 `e470029a7e5d52ea25d17be2fb255e2dfe1fe75d6fe6cc527b3146001a3c8758`.
- `vendor_a/lib/libmediatek_tv_rpcwrapper.so`, SHA-256 `d8938a5733c5cacd32de03eef8dafd5ea16acb9d6386bfdc7974323782b8f62b`.
- `vendor_a/lib/vendor.mediatek.tv.mtktvfactory@1.0-imp.so`, SHA-256 `36ebefce2c24bf960d570d006977c2187130e0c6db73ca647e683fb104df3a86`.
- Полный список: [NATIVE_API_CANDIDATES_8054.json](../generated/NATIVE_API_CANDIDATES_8054.json).

## Границы достоверности

**Доказано:** найден дамп этой ревизии, SHA-256 архив проверен при запуске workflow, разделы успешно распакованы, в ELF реально присутствуют перечисленные экспортируемые символы, а в DEX ранее найдены точные Java→JNI сигнатуры.

**Не доказано:** непосредственный переход из конкретной JNI-функции к C-wrapper и HIDL transaction, семантика `inputGroup=-1` и `isUpdate=3`, физическое применение к активному HDMI/SDR/HDR/PQ-профилю, стабильный rollback при смене входа, слышимый эффект EQ.

**Никаких операций на проекторе:** ADB, `settings put`, `setprop`, установка APK, I²C, заводская калибровка, перепрошивка, сброс и изменения thermal/вентиляторов **не выполнялись**.

## Открытые задачи (не трактовать как выполненные)

1. `NATIVE-004`: disassembly + xref из `Java_com_mediatek_twoworlds_tv_TVNative_setConfigValue_1native` к `a_mtktvapi_config_set_value` или фактическому альтернативному пути, включая типы аргументов.
2. `NATIVE-005`: выяснить `inputGroup=-1` / `isUpdate=3` в оригинальном ELF/HIDL и его адресацию. Не утверждать смысл без доказательств.
3. `NATIVE-006`: точная цепочка HDR `picture_list_hdr` → `g_video__vid_hdr`, затем MEMC `tv_picture_advance_video_mjc_effect` → `g_video__vid_mjc_effect`.
4. `NATIVE-007`: аудио `sound_style`, EQ5, влияние `initSoundStyleMap`, различие GEQ5/Factory PEQ13 и контекст вывода.
5. `NATIVE-008`: gate на аппаратную проверку: лишь после разрешения владельца, readbacks и план восстановления.

## Правило дальнейших записей

Каждое действие: до исследования зафиксировать цель/предпосылку и исходный ref; после — точные бинарники и SHA, методы и сигнатуры, команды/версии инструментов, результаты и негативные проверки, выводы по уровням подтверждения, риск и необходимый следующий шаг. Не оставлять работу только в диалоге — сделать коммит, записать его SHA и соответствующий run/PR URL. Новые задачи ведутся в отдельных `logs/YYYY-MM-DD_TASK-ID.md`; общий [INDEX.md](INDEX.md) — точка входа.

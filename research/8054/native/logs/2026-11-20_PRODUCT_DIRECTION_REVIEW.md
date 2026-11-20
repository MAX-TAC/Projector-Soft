# 2026-11-20 — PRODUCT-DIRECTION-REVIEW: аудит вектора исследования

**Запрос владельца:** проверить текущий HANDOFF и понять, не избыточно ли NATIVE-005b уходит в ABI/vtable/HIDL/RPC вместо восстановления рабочих функций TvSettingsPlus MOD.
**Входной PR #3 HEAD при проверке:** `87a912e8a95d6599527a7f103128472031eda79a`; **MOD PR #2 HEAD:** `fbb41e92a47a94a2a4b0f4122100e5a1cb43cfe5`.

## Прочитанные источники
- `research/8054/native/HANDOFF.md`, `logs/INDEX.md`, `TRACE_004_CONFIG_JNI_TO_HIDL.md`, `TRACE_005_CONFIG_HIDL_SERVICE_RPC.md`, соответствующие отчёты и журналы;
- `research/8054/FINAL_FUNCTION_PARITY_8054.csv`, `research/8054/PICTURE_BLOCK_STATUS_8054.md`, `research/8054/DEVICE_TEST_MATRIX_8054.md`;
- `docs/8054_BACKEND_INTEGRATION_MAP.md`, `docs/8054_KNOWN_LIMITATIONS.md`;
- `list_pr_changed_filenames` PR #3: 23 изменённых файла, **0 изменений в `app/`**.

## Установленные результаты и интерпретация
- NATIVE-004/005 — подтверждённые статические исследования original 8054: JNI→C wrapper call edge и service→RPC callsite. Это существенные результаты, а не ошибочная декомпиляция.
- Следующий предложенный NATIVE-005b углубляет низкоуровневую диспетчеризацию и пытается понять inputGroup/update семантику. Для конечного пользователя пока нет работающего изменения в MOD; доказательства конкретного необходимого UI-вызова уже существуют на Java/Settings/TvServices уровне.
- Матрица v41 отмечает PIC01–12 как READ_ONLY; `NativePicturePanel` создаёт нижнее окно контраста, но не пишет значение. Для PIC04 известны OEM callback, Settings.Global `picture_contrast`, TvServices map `g_video__contrast`, JNI config setter.
- Успех Global запись и даже config getter не подтверждают HDMI/SDR/HDR/PQ profile scope. Поэтому нельзя мгновенно включить запись во всех контекстах без ограниченного теста и rollback.
- В ранее согласованной методике аппаратных тестов есть режимы/зависимости и предупреждение о побочном старте MOD через inactivity policy.

## Принятое проектное направление (рекомендация; аппаратный тест ещё не выполнялся)
- Отложить **NATIVE-005b** до конкретного воспроизводимого дефекта или недостающего OEM-контракта.
- Приоритет **PRODUCT-001 / PIC04 Contrast**: STOCK action → current OEM Java/Global/TvServices API → readback/effect/rollback → минимальная MOD интеграция.
- Не перерабатывать UI, не переходить к следующему параметру, пока текущий не получил честный статус и конкретный blocker.
- Сохранить все результаты TRACE_001..005 без удаления; новая цель дополняет их, а не отрицает.

## Записи GitHub
- `research/8054/native/PRODUCT_FOCUS_REVIEW_2026-11-20.md` — факты/обоснование/vertical slice, commit `b4e5b57869a746eb445e53162de498576a91cfb2`.
- `research/8054/native/PROMPT_PRODUCT_PIVOT.md` — подготовленный промпт, commit `206a8078afa0dc767244ea4bd45e61180df715c0`.
- `research/8054/native/HANDOFF.md` — добавлена заметка о новом продуктовом приоритете без удаления истории низкоуровневой работы, commit `8c7581ff3b0d98d031ed72427f56f96a1b6c378e`.
- Этот журнал и INDEX обновляются отдельно.

## Что не делалось
- Проектор не использовался; ADB не выполнялся; штатные значения не изменялись.
- Не запускалась сборка MOD v42, не вносились изменения в PR #2.
- Не выполнен ручной контрастный тест, нет оснований менять `BUILT_UNTESTED` на `WORKING_RUNTIME`.

## Следующая конкретная задача
`PRODUCT-001` — создать детальный OEM integration contract PIC04 и минимальный approved hardware test plan; затем отдельная реализация/проверка. Запись без разрешения владельца не запускать.

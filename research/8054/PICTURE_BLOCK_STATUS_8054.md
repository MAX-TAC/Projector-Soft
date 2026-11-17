# Изображение: граница реализации после review

Статус: BUILT_UNTESTED для MOD gate и OEM делегирования. RUNTIME_TEST_REQUIRED для фактической навигации и работы на 8054. Полный inline перенос и автоматический rollback не готовы.

| Путь | Текущая доступность |
|---|---|
| Штатная страница изображения | Ссылка PICTURE_SETTINGS на существующую com.android.tv.settings.uicustom.MainActivity с проверкой Activity |
| MOD mode/temperature/sliders/HDR/MEMC | Скрыты, поскольку PQ profile + HDMI context не установлен |
| Новые MOD picture writes | UNKNOWN блокируется до provider read и journal save |
| Replay сохранённых picture значений | Недоступен при неизвестном контексте; кнопка отключена |
| Старые UNKNOWN и legacy снимки | Сохраняются для диагностики, без replay/автоматического удаления/перезаписи |

Review 5944214002 P1 подтвердился: ранее UNKNOWN блокировал только restore. Теперь write также fail-closed. Core дополнительно не записывает picture_mode и picture_color_temperature как scalar значения. Даже known-context journal с чужим/старым UNKNOWN контекстом не перезаписывается новой записью.

Причина контекстного ограничения уже установлена по сохранённым smali, без нового reverse: TVMenuSettingsService#updateConfigDataChanged (5400–5412) на picture_mode вызывает updateSettingDB(itemIds), а на picture_color_temperature (5415–5427) — updateSettingDB(colorTempMap). Источник run 44123938819, TvServices SHA-256 2934ca9334afd0a2af85786aa0593d27da216086c7eb9b0c9b801f412516b77d. HDMI/PQ profile ownership остаётся неизвестным.

Тесты: OemPictureStateTest — повторное изменение, отсутствие original, partial restore/retry, восстановление логики с persisted journal, mode/input mismatch, UNKNOWN write без snapshot/provider mutation, journal failure и legacy quarantine. OemPictureAdapterTest компилирует настоящий OemPictureState.java с изолированными Android doubles и вызывает public write/restore при production verifiedContext=null; проверяет ноль provider writes и journal commits, сохранность UNKNOWN/legacy после нового Context. Это JVM тест adapter логики, не настоящий Android process/device test. Fixtures не включаются в production APK.

Новая проверка review correction: source-safety 44155206834 (core + production adapter + sound), общий Android/D8/JVM 44155206813, OEM 44155206851. v33 44155206940 и exact-8054 APK 44155208015 success на commit 4ab5dd5. APK SHA-256 063b65052e068b676a94ddf260087ef8569f78cb1687b802b319faf64d260fe6, artifact 14360098993. Ни одной записи на проекторе не было. Откат исходников — revert review correction commit; не replay неизвестного device snapshot.

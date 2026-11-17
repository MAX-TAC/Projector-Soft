# Изображение: состояние блока

Source state: BUILT_UNTESTED (предыдущая сборка); review correction P1/P2 ожидает новый CI. Android compile и transaction tests: source-safety run 44139714268 success, OEM run 44139714347 success. Аппаратное состояние RUNTIME_TEST_REQUIRED. Exact-8054 APK CI 44139714534 и v33 CI 44139714405 success. APK не установлен. Статус относится к MOD пользовательским picture controls, не к заводской калибровке/PQ.

## Режим и источник

В сохранённом оригинальном 8054 TvServices `TVMenuSettingsService#updateConfigDataChanged(String)`, smali 5400–5412, событие `picture_mode` вызывает `updateSettingDB(itemIds)`. При `picture_color_temperature`, 5415–5427, вызывается `updateSettingDB(colorTempMap)`. Общий `updateSettingDB` асинхронно перечитывает config в Global. Источник: run 44123938819, exact method bodies, APK SHA-256 2934ca9334afd0a2af85786aa0593d27da216086c7eb9b0c9b801f412516b77d. Это подтверждает пересинхронизацию связанных параметров при смене режима. Точное хранение профилей в закрытом backend и привязка к каждому HDMI-порту не установлены; отсутствие порта в Global key не доказывает независимость от входа.

## Ограничение восстановления

`OemPictureState` использует `PictureStateTransaction`: исходное значение сохраняется перед первой записью одним durable record с контекстом. Повторная запись не меняет original. Отсутствующее значение и отказ сохранения журнала блокируют запись. Для восстановления требуется совпадающий подтверждённый контекст режима И входа; неизвестный контекст и legacy records отклоняются. `picture_mode` никогда не воспроизводится обычным scalar restore, поскольку он запускает асинхронные групповые изменения. При частичной ошибке запись остаётся для повтора; успешные удаляются после readback. Совпадение readback означает только provider state.

В Android adapter verifiedContext пока возвращает null. Пункт «Сохранённые значения изображения» отключён с объяснением неизвестного профиля/HDMI. Даже прямой вызов restore возвращает false без записей. Полноценный откат изображения не заявлен. Существующие picture controls продолжают штатные Global записи; snapshot служит для диагностики. Полный откат не доказан и для HDR/MEMC, не охваченных snapshot.

## Тесты

`OemPictureStateTest` запускает тот же transaction core, что Android adapter: повторное изменение сохраняет original; отсутствие original; частичное восстановление и повтор; новый экземпляр с сохранённым journal (модель перезапуска приложения); смена picture mode; HDMI mismatch; неизвестный контекст; ошибка journal commit; legacy запись. Это JVM fake provider/journal, не Android process/device test. Android adapter проверяется компиляцией API33; аппаратная проверка остаётся отдельной.

Граница завершения: исходники и тестируемая защитная логика готовы, восстановление намеренно недоступно до установления контекста. Следующий пользовательский блок — звук: использовать готовую карту Settings/TvServices и OEM диапазоны, добавить групповые тесты, не исследовать закрытый HAL как условие переноса.



## Граница после review 5944214002

Прямые MOD list/slider/HDR/MEMC контролы скрыты до проверки контекста; пользовательский доступ — ссылка android.settings.PICTURE_SETTINGS на штатную com.android.tv.settings.uicustom.MainActivity (с проверкой наличия Activity). Это OEM делегирование, а не завершённый inline перенос. Отдельный mayEdit gate не позволяет показать прямые контролы; core блокирует UNKNOWN ещё до чтения provider/создания snapshot. Старые UNKNOWN/legacy records сохраняются без replay или автоматического удаления; даже при известном контексте journal с другим контекстом блокирует новую запись. picture_mode и picture_color_temperature делегируются из-за асинхронных групповых эффектов.

Production adapter test компилирует именно OemPictureState.java с изолированными Android test doubles и вызывает public write/restore: при реальном adapter verifiedContext=null нет provider writes и journal commits; старые UNKNOWN и legacy записи не меняются после нового Context. Это тест Android adapter логики, не устройства/реального SharedPreferences процесса.

Доступно: штатная OEM страница изображения. Скрыто: прямые MOD режимы, цветовая температура, sliders, HDR/MEMC. Отключено: replay snapshot. BUILT_UNTESTED относится к собранному MOD gate/делегированию; RUNTIME_TEST_REQUIRED относится к навигации/штатной пользовательской работе на устройстве. Полноценный откат не готов.

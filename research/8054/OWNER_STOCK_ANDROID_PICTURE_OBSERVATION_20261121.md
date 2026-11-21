# 2026-11-21 — реальный 8054: штатные Android TV Picture controls уже работают; новая стратегия reuse

**Источник:** прямое сообщение владельца работающего FlixTone D11 Master 8054 с наблюдениями штатного UI и чтением `adb shell settings get global`. **Это device observation владельца**, не независимый воспроизведённый испытателем APK CI run. Исходное приложение KuSettings **не отключалось**; нельзя заключить, что весь пакет безопасно удалить.

## 1. Что владелец реально сделал и увидел

1. Из китайских настроек изменил контраст, насыщенность и яркость примерно на 5 значений. Открыл **уже существующие штатные Android TV Settings** (не наш экспериментальный standalone UI), раздел «Экран и звук» → «Изображение».
2. Там уже есть пресеты Picture Mode и **работающие** нижние редакторы яркости, контраста, насыщенности, оттенка и резкости. Нажатие OK открывает нижний штатный ползунок; регулировка изменяет изображение и значение **в китайском меню**.
3. «Расширенные настройки»: **цветовая температура** с User/Cold/Standard/Warm и RGB Gain R/G/B с отдельными нижними редакторами.
4. Есть пункт, видимый пользователю как «MGC» (требуется точная UI-проверка spelling; source key `tv_picture_advance_video_mjc_effect`), с эффектами Off/Low/Medium/High и демо сравнения правой/левой части изображения. **Это motion interpolation/MJC/MEMC, не доказанная функция шумоподавления.** Демо в финальное пользовательское меню добавлять необязательно.
5. Актуальное чтение до/после пользовательских действий:
   ```text
   picture_mode=7
   picture_color_temperature=2
   picture_list_hdr=1
   picture_contrast=57
   picture_contrast=93
   picture_contrast=50
   picture_contrast=64
   ```
   **57→93→50→64 НЕ являются последовательностью одного OEM RIGHT шага**; между чтениями выполнены разные ручные регулировки. Параметр `picture_list_hdr=1` — **stored flag**, не независимое доказательство актуального SDR/HDR video.

## 2. Независимая статическая проверка адресации OEM Android TV

В исходном exact-8054 `com.android.tv.settings`:

```text
com.android.tv.settings.partnercustomizer.picture.PictureActivity
  → partnercustomizer.picture.PictureFragment
  → PictureFragment.onPreferenceChange / onPfChange
  → PreferenceConfigUtils.updatePreferenceChanged
  → PreferenceConfigUtils.putSettingValueInt
  → Settings.Global.putInt(resolver, key, int)
  → TVMenuSettingsService.SettingObserver
  → TVSettingConfig.setConifg / MtkTvConfigBase
  → MediaTek JNI / hardware backend
```

Этот **конкретный штатный Picture call chain НЕ обращается к `com.my.settings.SettingActivity`, не вызывает `com.my.settings` контроллер и не требует запуска китайской GUI Activity**. Другое меню видит изменения, поскольку provider/backend общие. Original `PictureFragment` обладает собственным lower `ProgressDialogPreference` и обработчиками, которые **уже физически работают** на устройстве при установленном KuSettings.

Кодовые указатели: [PICTURE_BLOCK_STATUS_8054.md](PICTURE_BLOCK_STATUS_8054.md), [PRODUCT-001 contract](https://github.com/MAX-TAC/Projector-Soft/blob/research/8054-mediatek-native-api-20261120/research/8054/native/PRODUCT_001_PIC04_INTEGRATION_CONTRACT.md), [TRACE_001](https://github.com/MAX-TAC/Projector-Soft/blob/research/8054-mediatek-native-api-20261120/research/8054/native/TRACE_001_PICTURE_CONTRAST.md). Native research documents находятся в независимой ветке research PR #3 (не в этой ветке PR #5).

## 3. Рекомендованное изменение реализации

**Новый продуктовый приоритет — reuse штатных работоспособных Android TV OEM Picture handlers вместо написания дубля.** Учесть, что **предыдущая задача от владельца требовала финальную интеграцию в existing v41 interface**, не создания второго китайского приложения:

1. Найти и проверить точные class/intent/manifest OEM stock `PictureActivity` и `PictureFragment` **в самом `com.android.tv.settings`**. Выяснить, можно ли непосредственно использовать их в v41 картине/advanced, не создавая лишнего щелчка.
2. **Наименее рискованный путь**: встроить штатный PictureFragment/PictureActivity как маршрут в v41; это Android TV native OEM экран, не запрещённый китайский `com.my.settings`. Не заменять контролы дублирующим `NativePicturePanel` writer до отдельной необходимости.
3. Если нужно сохранить **точно текущий v41 нижний dialog**, оставить его оболочку, но заменить callback на OEM `PreferenceConfigUtils` logic с точным оригинальным increment/side effects и source gates; доказать эквивалентность оригиналу. Это более сложный fallback, а не исходный приоритет.
4. Демо MJC и Factory PQ не обязательны. Реальные эффекты MJC, HDR, RGB, source switching проверять независимо.
5. Аудит на отсутствие `com.my.settings` Activity/Controller в **всех user entrypoints** обязателен, но возможность полного `pm disable com.my.settings` остаётся отдельной **M2**, потому что `BootupReceiver → MySetTimeService → KuSourceRJ.TimeService` обеспечивает OEM service recovery.

## 4. Влияние на PR #4 и PR #5

- PR #4 `Pic04ContrastStep` и более низкоуровневый контракт — полезный **исследовательский/fallback** вариант, а не основной продуктовый маршрут, если stock PictureFragment уже работает.
- PR #5 `NativePicturePanel` сейчас **экспериментально сам вызывает Settings.Global.putInt** с неподтверждённым `+1/-1` и собственным журналом. **Не рекомендуется устанавливать этот APK до выбора stock reuse route**. Это software candidate, хотя compile/UI/CI прошли.
- Базовый PR #2/v41 сохраняется. Не делать merge сомнительного direct scalar writer в stable branch. Следующая целевая реализация должна сохранить layout 8 разделов и использовать OEM Android TV Picture, а не китайское меню.

## 5. Приёмка после реализации reuse

- Android TV `PictureActivity/Fragment` открыт **из MOD v41** без `com.my.settings`; D-pad/OK/Back работают.
- Яркость/контраст/насыщенность/оттенок/резкость и RGB изменяют изображение и видимые stored values, preview и профили совпадают с работавшим STOCK.
- Профили picture mode/temperature/gamma и MJC сохранены; не включены factory-only Reset/демо как обязательные.
- Открытие штатной Android TV Picture Activity **не доказывает отключение KuSettings**. Тест его фона выполняется отдельно с безопасным восстановлением.
- Тестовое событие не содержит точной одиночной дельты RIGHT и не подтверждает конкретный device context: не делать из значений 57/93/50/64 такой вывод.

## 6. Только если нужен класс фактически запущенного Activity

Владелец может открыть работающий штатный экран «Изображение», оставить его на экране, затем **read-only**:
```sh
adb shell dumpsys activity activities | grep -E 'topResumedActivity|mResumedActivity|ResumedActivity|Hist #' | head -n 20
```
Это показывает часть task/Activity history; если вывод обрезан или не содержит актуального экрана — не угадывать по нему. Для контроля конкретного обработчика лучше независимый Manifest + live `dumpsys activity` по согласованию.

**No changes executed to projector during this document creation.**

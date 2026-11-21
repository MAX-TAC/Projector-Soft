# 08. Дополнение 21.11.2026 — рабочие штатные Picture controls уже существуют в Android TV Settings

**НОВОЕ НАБЛЮДЕНИЕ ВЛАДЕЛЬЦА НА РЕАЛЬНОМ FLIXTONE 8054:** исходные Android TV Settings → «Экран и звук» → «Изображение» позволяют регулировать яркость, контрастность, насыщенность, оттенок, резкость, цветовую температуру, усиление RGB и MJC/MEMC, а китайское OEM меню сразу отражает изменение. Это меняет продуктовый приоритет: **не переписывать то, что уже работает в исходном `com.android.tv.settings`.**

## A. Устройство и проверенный foreground component

Оригинальный вывод:
```
adb shell dumpsys window | grep -E 'mCurrentFocus|mFocusedApp'
mCurrentFocus=Window{8568e96 u0 com.android.tv.settings/com.android.tv.settings.partnercustomizer.picture.PictureActivity}
mFocusedApp=ActivityRecord{e953f35 u0 com.android.tv.settings/.partnercustomizer.picture.PictureActivity} t255
```

**Подтверждено на устройстве:** открыта **`com.android.tv.settings.partnercustomizer.picture.PictureActivity` внутри `com.android.tv.settings`**, а не `com.my.settings` и не китайская Activity. Проверена действующая нижняя регулировка; отдельное `com.my.settings` также видит результат. **Это не означает**, что весь KuSettings package не нужен для фоновых сервисов.

Ранее владелец получил provider readings:
```text
picture_mode=7
picture_color_temperature=2
picture_list_hdr=1
picture_contrast=57
after additional stock UI adjustments: contrast=93, then 50, then 64
```
Это несколько пользовательских шагов, **не** расчёт одиночного D-pad инкремента и **не** независимое SDR/HDR подтверждение.

## B. Подтверждённый исходниками маршрут управления

Original exact-8054 Android TV OEM:
```text
com.android.tv.settings.partnercustomizer.picture.PictureActivity
  → PictureFragment.onPreferenceChange / onPfChange
  → PreferenceConfigUtils.updatePreferenceChanged
  → Settings.Global.putInt(key,value)
  → TVMenuSettingsService.SettingObserver
  → TVSettingConfig.setConifg
  → MediaTek backend
```

Chinese `com.my.settings` GUI в этой цепочке отсутствует. Оба UI разделяют backend/provider. Источник: [OEM callsite audit](https://github.com/MAX-TAC/Projector-Soft/blob/audit/8054-tvsettingsplus-20261117/research/8054/PICTURE_BLOCK_STATUS_8054.md) и [PIC04 integration contract](https://github.com/MAX-TAC/Projector-Soft/blob/research/8054-mediatek-native-api-20261120/research/8054/native/PRODUCT_001_PIC04_INTEGRATION_CONTRACT.md).

**MJC (source key `tv_picture_advance_video_mjc_effect`) — режим обработки плавности/интерполяции движения, не подтверждённое «подавление шумов».** User-visible Off/Low/Medium/High полезны, демонстрационный split-screen MJC Demo не обязателен.

## C. Новое изменение в GitHub: отдельный Draft PR #7

[PR #7: direct owner-verified stock PictureActivity route](https://github.com/MAX-TAC/Projector-Soft/pull/7).

От базового PR #2 (`fbb41e92...`), **не** от PR #5 с пробным собственным writer:

- `StockMainSettingsMenu.target(Picture)` теперь возвращает `ComponentName("com.android.tv.settings","com.android.tv.settings.partnercustomizer.picture.PictureActivity")`.
- Корневые восемь строк, их порядок, иконки и все другие user routes не изменились.
- Запуск родной Android TV PictureActivity идёт прямо из `v41` root «Изображение», без открытия `com.my.settings` и без отдельного экспериментального custom slider.
- Robolectric regression `UnifiedMenuUiTest` проверяет строку класcа, пакет и отсутствие Chinese GUI target.
- На момент создания документа compilation/AndroidX/exact APK CI проверяются отдельно; **физический тест новой версии APK ещё НЕ проводился**.
- Root picture preview пока указывает на прежнюю `NativeSections.Picture` и может показывать только read-only значения. Для 2-pane Android TV следует проверить preview соответствие: после клика открывается работающий stock PictureActivity.

**Решение для следующего разработчика/ИИ:** перед добавлением ПОСТОЯННОГО собственного slider для Picture/Brightness/Contrast/Saturation/Hue/RGB/MJC убедиться, что stock Android TV OEM PictureActivity уже покрывает задачу. Иначе повторно создаются недостаточно проверенные права/профили/допуски/инкремент и баги ContentObserver.

## D. Что осталось и почему KuSettings нельзя удалить сразу

`com.my.settings` содержит отдельную цепочку `BootupReceiver → MySetTimeService → KuSourceRJ.TimeService` и потенциальные другие background dependencies. **Stock Android PictureActivity независима от китайского GUI, но всё приложение KuSettings пока не доказано удаляемым.**

Всё ещё требуется провести user parity аудит Sound, Sources/HDMI/CEC, Projection/Focus/Light, Power; по каждому искать уже работающий `com.android.tv.settings` stock экран или OEM backend **прежде** чем создавать двойной контроллер.

## E. Обязательный дополнительный вопрос независимому ИИ

**Найди другие штатные OEM Android TV Settings Activity/Fragments, которые уже реализуют корректные пользовательские функции без `com.my.settings`, и сопоставь их с MOD v41.** Предложи план reuse этих экранов, сохраняя единый встраиваемый UX, собственные разделы MOD и неизменную нижнюю platform backend архитектуру.

Скептическая оговорка: факт stock PictureActivity `com.android.tv.settings` и общей backend цепочки не доказывает, что она **не требует косвенных background сервисов**, случайно входящих в KuSettings. Это проверяется отдельно только в M2 с restore gate.

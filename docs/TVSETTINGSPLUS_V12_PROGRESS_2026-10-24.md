# TvSettingsPlus MOD v12 — статус переноса OEM-настроек

**Дата:** 24 октября 2026. **Целевая базовая прошивка:** RUIOS 8052 (Android 13, MT9660). **Статус:** техническая экспериментальная сборка, НЕ валидирована на физическом устройстве.

[Успешная проверка APK CI](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40119416058) · [Подписанный APK v12 (архив GitHub Actions)](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40119416058/artifacts/12569253832).

## Что реально включено в подписанный v12

1. Собственный launcher alias Android TV: `MAIN`, `LEANBACK_LAUNCHER`, `LAUNCHER`, русифицированное имя/штатный значок и TV banner. Ярлык открывает именно `com.android.tv.settings.MainSettings`, но размещение и отображение в OEM-лаунчере нужно испытать.
2. Пять совместимых входов в оригинальные OEM-разделы внутри самого TvSettingsPlus: Проекция, Источники, Изображение, Звук, Дополнительно. **Важно:** это пока отдельная OEM Activity внутри того же пакета, не fully inlined.
3. Прямо в AOSP MainFragment присутствуют **три отдельные тематические категории** `Изображение · Проектор`, `Звук · Проектор`, `Проекция · Проектор`, вместо смешанного блока быстрых настроек.
4. Изображение: 7 заводских пресетов, 4 температуры, ползунки яркости изображения (0–100), контраста (0–100), насыщенности (0–100), оттенка (−50…50), резкости (0–20), регулировки RGB (−100…100 для каждого). **Яркость изображения не означает мощность LED.**
5. Звук: 7 режимов звука, заводские Bass (0–100) и Treble (0–100). Кроме этого — **пять отдельных полос эквалайзера: 120 Гц, 500 Гц, 1,5 кГц, 5 кГц, 10 кГц**, каждая отдельным AOSP SeekBar. Поддержка подтверждается через заводской `PartnerSettingsConfig.getSettingsList("sound_equalizer_detail")`, а значения пишутся в реальные ключи `sound_equalizer_detail_*hz`, как оригинальный `ProgressBarDialog`. Штатные пределы прогресс-бара — 0–100, шаг 1.
6. Изображение: режимы HDR 0/1 и MEMC 0..3 при наличии исходных заводских значений. Наличие кода само по себе не доказывает физическую поддержку функций.
7. Проекция: четыре исходных режима положения (перед/зад, стол/потолок), управляемые штатным `ProjectorManager`. **Фокус и коррекция трапеции не переделывались.**
8. Для ветки яркости источника света с customer 0 добавлен условный мост к штатному термозащищённому `KuSourceRJ.BrightReceiver` с сохранением заводского fullscreen fallback. Для других customer ID остаётся оригинальный `MtkUtil`.
9. В исходном OEM-меню источников скрывается пункт Bluetooth-колонки; bluetooth-пульт и сам Bluetooth стек не затрагиваются.
10. Девять старых Settings Intent actions сохранены как совместимые aliases.

## Коррекции безопасности после анализа OEM обработчиков

Статический аудит показал:

- Включение HDMI Game Mode в оригинальном `SourceSubMenuController` вызывает `resetTrapezoid()`; простое изменение `Settings.Global` неполноценно. **Прямой переключатель низкой задержки в AOSP временно убран.** Оригинальный OEM-раздел остаётся доступным.
- HDMI-CEC вызывает `mCompatibleApis.setHdmiCecControlEnabled`, дополнительно затрагивает power-confirmation, Auto Device Off и eARC. **Нельзя заменить это одной настройкой из системного провайдера.**
- eARC использует OEM `MtkTvCecAnBase.setEarcEnable` и совместимость HDMI-CEC. Прямые упрощённые переключатели eARC, surround и auto-volume также **отключены** до переноса их соответствующих OEM обработчиков.
- EDID — HDMI capabilities handshake; заводское меню зависит от текущего HDMI-источника, данных `MtkTvAVMode.getVideoInfoData(...).getU8EdidIndex()` и отдельного `EdidSourceSecondaryController`. Пока оставить только через родной OEM раздел, не создавать некорректную отдельную запись.

## Что подтверждено автоматической проверкой

- GitHub Actions run `40119416058`: успех Java/Android 13 compile → D8, smali assembler, package integrity, AXML compatibility actions, aapt, zipalign, apksigner и совпадения с заводским AOSP platform test certificate.
- Пакет `com.android.tv.settings`, `versionCode=12`.
- Из 16 заводских DEX только `classes13.dex` и `classes14.dex` заменены, добавлен `classes17.dex`. Остальные DEX, JNI, ресурсы включая `resources.arsc` побайтово сверены с оригиналом.
- APK ориентирован **только на оригинал 8052**. Файла оригинального TvSettingsPlus от 8054 в текущей базе `vendor/reference` нет — нельзя устанавливать сборку 8052 как готовый MOD 8054.

## Не выполнено. До финального релиза требуется

1. Аппаратные испытания: работа native AndroidX preference с OEM ART, отображение меню и ярлыка, режимы/ползунки и звук DSP, возможность резервного восстановления.
2. Встроить remaining HDMI/CEC/EDID и связанные настройки источника *с сохранением всех аппаратных побочных эффектов*. Сейчас пользователь может добраться до них через отдельную **штатную OEM Activity** в том же TvSettingsPlus.
3. Закрыть ссылки на `KuSettings` в `settingApps`, OEM launcher, `services.jar` и обеспечить восстановление критического `KuSourceRJ.TimeService` (watchdog), прежде чем отключать KuSettings. **Сейчас не отключать.**
4. Собрать отдельно под 8054 с оригинальным базовым APK и проверить реальное количество HDMI, ToF, двигатели и драйверы. Подписанный 8052 APK не является межревизионным.
5. Принимать проект в статус «готовый финальный для тестера» только с достоверным APK, планом восстановления и проверкой ключевых системных вызовов.

**Важно:** данный отчёт не заявляет, что все меню перенесены или KuSettings стал ненужен. Наличие исходного DSP-эквалайзера и нового UI — сильный результат статического анализа, но не подтверждение аппаратной работоспособности на конкретном проекторе.

## Ссылки на изменённые модули

- [5-полосный OEM EQ](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/OemFiveBandEqualizerHook.java)
- [Тематические категории AOSP](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/OemInlinePreferencesHook.java)
- [Ползунки с заводскими пределами](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/OemInlineSlidersHook.java)
- [Проекция](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/OemProjectorOrientationHook.java)
- [Исследование реализации эквалайзера](../research/oem-apps/Native_Controllers_8052/eq_impl.json)
- [Заводские HDMI и EQ методы](../research/oem-apps/Native_Controllers_8052/hdmi_eq_audit.json)
- [Точные диапазоны OEM ползунков](../research/oem-apps/Native_Controllers_8052/slider_layout_bounds.json)
- [CI v12](../.github/workflows/build-tvsettingsplus-inline-v12.yml)

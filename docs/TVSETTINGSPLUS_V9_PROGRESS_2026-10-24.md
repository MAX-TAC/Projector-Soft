# TvSettingsPlus MOD v9: перенос оригинальных ползунков и HDR/MEMC

**Дата:** 2026-10-24. База: RUIOS 8052, Android 13. **Статус:** экспериментальная APK — собрана и подписана CI, на проекторе не проверялась.

[GitHub Actions: успешная сборка v9](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40116890354) · [APK и служебный отчёт](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40116890354/artifacts/12570440453)

## Что действительно добавлено НАМИ к исходному TvSettingsPlus

В стандартный Android TV MainFragment через native `androidx.preference` встраиваются:

| Реальный OEM элемент | Режим | Заводской диапазон / значения | Ключ |
|---|---|---|---|
| Яркость **изображения** (не светодиода) | Ползунок | 0–100 | `picture_brightness` |
| Контраст | Ползунок | 0–100 | `picture_contrast` |
| Насыщенность | Ползунок | 0–100 | `picture_saturation` |
| Оттенок | Ползунок | −50…50 | `picture_hue` |
| Резкость | Ползунок | 0–20 | `picture_sharpness` |
| Баланс красного | Ползунок | −100…100 | `picture_red_gain` |
| Баланс зелёного | Ползунок | −100…100 | `picture_green_gain` |
| Баланс синего | Ползунок | −100…100 | `picture_blue_gain` |
| Низкие частоты | Ползунок | 0–100 | `sound_bass` |
| Высокие частоты | Ползунок | 0–100 | `sound_treble` |
| HDR | Переключатель | 0/1 | `picture_list_hdr` |
| MEMC | Выбор | 0 выкл., 1 низкий, 2 средний, 3 высокий | `tv_picture_advance_video_mjc_effect` |

Эти значения установлены из **декомпилированных OEM контроллеров**, **точных XML min/max** и заводских обработчиков их изменений. Не использованы гипотетические шкалы. Для новых ползунков и HDR/MEMC **требуется наличие записанного заводского значения**; если OEM параметр отсутствует либо выходит за пределы диапазона — пункт скрывается. При записи выполняется контроль считыванием из Settings.Global. Наличие ключа и успешной записи **не доказывает аппаратное применение на данном физическом экземпляре**; HDR/MEMC особенно требуют проверки совместимости после установки.

Сохранено из v7: 3 OEM списка режимов изображения/цветовой температуры/звука; при поддержке OEM 3 переключателя surround/eARC/auto-volume; 5 ссылок с AOSP MainFragment на оригинальные OEM вкладки; Android TV ярлык «Настройки» со штатными иконкой и banner, 9 legacy Settings Intent aliases; brightness route customer-0 с заводским KuSourceRJ fallback; скрытие ссылки Bluetooth speaker mode.

**Полноэкранные калибровочные экраны автофокуса, трапеции и четырёх углов не изменялись.**

## Автоматическая проверка v9

GitHub Actions `40116890354` — success: Java/D8, smali patch/build, AndroidManifest (legacy actions, MAIN/LEANBACK_LAUNCHER и стандартный LAUNCHER, icon/banner), сертификат оригинальной platform test-key, aapt, zipalign/apksigner. Из 16 заводских DEX заменены только `classes13.dex` и `classes14.dex`; добавлен `classes17.dex` с нашим кодом. Заводские ресурсы, JNI и другие DEX остались нетронутыми. Это статические проверки APK, не доказательство его работы на MT9660.

## Что пока не переносилось целиком

- Разделы HDMI/EDID/CEC, источник при включении, ориентация проекции, питание и отдельные специфические OEM режимы пока доступны через существующую OEM Activity, а **не** непосредственно внутри Android TV двухпанельного интерфейса.
- Не реализован 5-полосный EQ: в OEM коде найдены лишь bass/treble.
- Не завершено безопасное отключение KuSettings: hardcoded `settingApps`, прямые вызовы кнопки и лаунчера, независимая watchdog-функция для `KuSourceRJ.TimeService`.
- Нет подтверждённой отдельной сборки для 8054, никаких тестов ADB и проверки работы пульта на физическом устройстве.

**Эту v9 НЕ отправлять владельцу как финальную прошивку для испытаний.** Проводим дальнейшую интеграцию по заводским API, затем аппаратное тестирование.

### Файлы

- [OemInlineSlidersHook.java](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/OemInlineSlidersHook.java) — 10 штатных ползунков
- [OemInlinePreferencesHook.java](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/OemInlinePreferencesHook.java) — режимы и HDR/MEMC
- [Статический аудит ключей и диапазонов](../research/oem-apps/Native_Controllers_8052/slider_layout_bounds.json)
- [GitHub workflow v9](../.github/workflows/build-tvsettingsplus-inline-v9.yml)

## Подтверждённое различие двух аппаратных режимов (дополнение к аудиту)

**Внимание: исходный TvSettingsPlus уже содержит собственный правопанельный контроллер яркости!** `BrightnessModeSettingsSecondaryController` предлагает Eco (внутренний ID 0), Standard (1), Highlight (2); Auto (3) скрыт. Он использует `MtkUtil.getLightMode()/putLightMode()`. Это **не та же шкала**, что `KuSourceRJ BrightReceiver` (0 Performance, 1 Standard, 2 Quiet, 3 High Altitude).

В `GeneralSubMenuController` есть проверка `Build.A_RJ_CUSTOMER_ID == 0`: при нуле производитель запускает полноэкранный `com.coolux.source.activity.BrightActivity`, а для других профилей использует уже существующую правую панель.

Для Android-модуля v3 разработан совместимый customer-0 hook, который сохраняет OEM `BrightReceiver` и заводской полноэкранный fallback. **Нельзя безусловно подменять `MtkUtil.putLightMode()` вызовом `BrightReceiver`**. Детали: [аудит 24 контроллеров](OEM_MENU_CONTROLLER_AUDIT_2026-10-23.md) и [сравнение методов](../research/oem-apps/Brightness_8052/overlap.json).


# Перенос яркости источника света и оформление OEM-настроек

**Платформа:** MT9660, оригинальная OTA 8052. Анализ сделан по APK `KuSourceRJ` и `TvSettingsPlus`, без запуска на оборудовании. Результат исследования: [Brightness_8052/oem_methods.json](../research/oem-apps/Brightness_8052/oem_methods.json).

## Что обнаружено в заводской прошивке

Стандартный `KuSourceRJ/BrightActivity` показывает **четыре** режима:

| OEM ID | OEM string resource | Название в нашем меню |
|---:|---|---|
| 0 | `performance_mode_text` | Повышенная яркость |
| 1 | `standard_model_text` | Стандартный |
| 2 | `quiet_text` | Тихий |
| 3 | `high_altitude_text` | Высокогорный |

Четвёртый пункт важен для управления тепловым режимом, не удалять без аппаратного обоснования.

`KuSourceRJ` содержит экспортированный `com.coolux.source.service.BrightReceiver`, который принимает:

- action: `com.action.brightReceiver`
- extra: `brightModel` (int, OEM ID 0..3)

Получив broadcast, этот OEM-получатель вызывает `BrightUtils.setBrightModel(Context,int)`. В отличие от прямого изменения системного свойства, штатная функция сохраняет `persist.bright.model`, выставляет внутренние таймеры режимов, запускает контроллер температурной коррекции `startThreadReduceNew` и корректирует PWM/подсветку по `getLedTem()`, типу платы и параметрам производителя. **Не переносить расчёты PWM в TvSettingsPlus, не писать `persist.bright.model` напрямую и не менять термозащиту.**

## Архитектура реализации

```text
TvSettingsPlus — правая панель / Изображение
  └─ Яркость источника света
       ◉ Повышенная яркость    (0)
       ○ Стандартный           (1)
       ○ Тихий                 (2)
       ○ Высокогорный          (3)
         │
         └─ LightSourceModePreferenceView (встраиваемый RadioGroup)
              └─ LightSourceBrightnessController (выбор, блокировки, подтверждение)
                   └─ KuSourceRjBrightnessGateway
                        ├─ explicit broadcast com.action.brightReceiver / brightModel
                        └─ чтение persist.bright.model (системное свойство)
                             ↓
                        KuSourceRJ.BrightReceiver
                             ↓
                        BrightUtils.setBrightModel
                             ↓
                        OEM температурный контроль и PWM/подсветка
```

Команда отправляется только при выборе пункта пользователем, подтверждается отложенным повторным чтением OEM-свойства. Если производитель не разрешает чтение свойства через скрытый API, интерфейс скрывается, а не показывает фиктивную выбранную настройку. При ошибке или неполном результате меню должно оставить реальное ранее выбранное значение.

- [`LightSourceBrightnessController.java`](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/LightSourceBrightnessController.java) — модель, безопасная для работы без железа.
- [`KuSourceRjBrightnessGateway.java`](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/KuSourceRjBrightnessGateway.java) — штатный OEM broadcast вместо ручных PWM.
- [`LightSourceModePreferenceView.java`](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/LightSourceModePreferenceView.java) — выбираемые радиопункты, **не полноэкранное окно**.
- [`MenuPresentationPolicy.java`](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/MenuPresentationPolicy.java) — явные правила, что выводится справа, а что сохраняет калибровочный экран.

## Что переносим и в каком формате

| Старое действие | Новое представление | Статус |
|---|---|---|
| Режим яркости источника света | **4 выбираемых пункта в правой панели**, без ползунка | Код модели, виджета и OEM-адаптера подготовлен; связка с исходным APK ещё не внедрена |
| Режим изображения | Список в правой панели | В исходном TvSettingsPlus имеются picture controllers; нужна интеграция |
| Контраст/цвет/насыщенность/резкость | Ползунки в правой панели | Исходные MTK методы требуют привязки к текущим widgets |
| HDR и MEMC | Выбираемые варианты в правой панели | В original `AdvanceSettingsSecondaryController` уже есть RadioTextView-контролы; в меню нужно правильно встроить и gate |
| Режим звука | Выбор пунктов в правой панели | Нужна проверка OEM аудио API |
| Эквалайзер | Панель с частотными ползунками | Только при подтверждённом DSP, иначе не отображать |
| Поворот/режим установки | Список вариантов в правой панели | Использовать существующий `ProjectionModeSecondaryController` |
| Автофокус | **Оригинальный полноэкранный калибровочный экран** | НЕ МЕНЯТЬ |
| Ручная фокусировка | **Оригинальный экран фокусировки** | НЕ МЕНЯТЬ до испытаний |
| Автоматическая трапеция | **Оригинальный полноэкранный калибровочный экран** | НЕ МЕНЯТЬ |
| Ручная трапеция / 4 угла | **Оригинальный полноэкранный калибровочный экран** | НЕ МЕНЯТЬ |

### Статус сборки и ограничения

Код Java **добавлен в Projector-Soft**, но последняя подписанная `TvSettingsPlus v2` всё ещё содержит старые DEX/UI и 9 legacy Intent aliases. Это не новая прошивка и не готовый установочный APK с новым меню.

Для практического включения нужно внедрить class DEX в оригинальный APK и привязать виджет к уже существующей правой панели TvSettingsPlus. **Не пересобирать весь `resources.arsc` через Apktool:** в оригинале есть конфликтующие стили и имена ресурсов, из-за которых стандартная пересборка ломается.

ADB-проверка по присланному ранее диагностическому скрипту нужна для подтверждения активной версии KuSourceRJ, наличия экспортированного BrightReceiver и прав чтения `persist.bright.model`, а также для финального теста после установки. Ревизия 8054 остаётся закрытой до сопоставления своего OEM API. До этого новый optical brightness feature для неё должен быть скрыт.

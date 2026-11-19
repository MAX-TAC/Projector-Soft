# HDMI ARC / eARC — статический аудит оригинального TvSettingsPlus 8052

**Дата:** 15.11.2026. **Исходник:** оригинальный `vendor/reference/8052/APK/TvSettingsPlus.apk` (Android 13 / RUIOS 8052). **Статус:** исходники декомпилированы на GitHub Actions, без установки/доступа к физическому проектору. Нельзя экстраполировать результат на 8054/FlixTone D11 без аппаратной проверки.

## Краткий вывод

**eARC имеет настоящий Java → MediaTek native вызов**, а не только UI. Однако две OEM-реализации sound menu **противоположно интерпретируют** ключ `Settings.Global.sound_earc`; плавающий OEM toggle сам не вызывает native API. Стандартные Android TV sound menu и отдельные eARC/CEC handlers напрямую вызывают `MtkTvCecAnBase.setEarcEnable(boolean)`. Физическая работоспособность конкретного HDMI порта и устойчивость линии ARC/eARC при паузе **не доказаны**.

## Точные файлы и поведение

| Код в TvSettingsPlus 8052 | Что делает |
|---|---|
| `partnercustomizer/tvsettingservice/util/PreferenceConfigUtils.java:isSupportEARC()`, строка 467 | `return new MtkTvCecAnBase().getEarcCaps();`: поддержка по MediaTek capability getter |
| `partnercustomizer/tvsettingservice/util/PreferenceConfigUtilsBase.java`, 216 | `KEY_SOUND_EARC = "sound_earc"` |
| `partnercustomizer/utils/SoundEARCReceiver.java`, 8–25 | Считывает `Settings.Global.sound_earc`, применяет `new MtkTvCecAnBase().setEarcEnable(value==0)` |
| Оригинальный `AndroidManifest.xml`, receiver line 467 | `SoundEARCReceiver` принимает `android.intent.action.BOOT_COMPLETED` (priority 1000) |
| `partnercustomizer/sound/EarcFragment.java`, 99–103, 113–123 | При выборе `0`: вызывает native eARC enable, включает HDMI-CEC; при `1` — disable; в `setEarcEnable` до HAL проверяет, отличается ли выбранное значение от сохранённого |
| `device/sound/SoundFragment.java`, 235–242, 264–273 | Стандартный экран Android TV также вызывает `setEarcEnable` при выборе параметра и включает CEC для `0` |
| `device/sound/SoundFragment.java`, 592–597 | Показывает пункт eARC только если `PreferenceConfigUtils.isSupportEARC()` возвращает `true` |
| `uicustom/floatmenu/controller/sound/SoundSubMenuController.java`, 40, 61, 197–202 | Плавающее меню считает `0` включённым, `1` выключенным. При переключении **только сохраняет Global**, без прямого `setEarcEnable` |
| `uicustom/fragment/sound/SoundFragment.java`, 46, 199–204 | Альтернативное меню считает **`1` включённым**, `0` выключенным: **полярность противоположная** основному драйверу и float menu |
| `uicustom/fragment/source/SourceFragment.java`, 209–212; `floatmenu/controller/source/SourceSubMenuController.java`, 349–352 | При отключении CEC (когда не нужна заводская dialog confirmation) сохраняют `sound_earc=1` и вызывают `setEarcEnable(false)` |
| `uicustom/common/Constant.java`, 40 | `SETTINGS_KEY_SOUND_SPEAKERS = "Settings.Global.HDMI_ARC_CONTROL_ENABLED"`; только наличие константы — НЕ доказательство работающей ARC линии; в decompiled TvSettingsPlus на этот literal нашлось одно упоминание, само объявление |

### Заводская таблица значений

- `Settings.Global.sound_earc = 0` означает **eARC enable** в драйверном `SoundEARCReceiver`, стандартном `EarcFragment` и native float menu.
- `Settings.Global.sound_earc = 1` означает **eARC disable**.
- Альтернативный `uicustom/fragment/sound/SoundFragment` использует обратное сопоставление при чтении и записи UI. Это доказанная несовместимость ветвей интерфейса.
- **Не писать `settings put global sound_earc ...` при диагностике**: одно сохранённое значение не гарантирует немедленное переключение HAL и может нарушить текущую маршрутизацию.

### Где именно применяется аппаратное состояние

- В eARC-настройках Android TV вызов MediaTek непосредственный.
- В плавающей версии меню звука выставляется только `Settings.Global`; соответствующий receiver зарегистрирован на `BOOT_COMPLETED`. Отдельный системный наблюдатель за настройкой вне APK возможен, но **не подтверждён** в этом аудите.
- В `EarcFragment` повторный выбор идентичного сохранённого значения **не вызывает** MediaTek API; если состояние драйвера разошлось с сохранённым значением, простое повторное нажатие не восстановит поток.

## Почему при паузе пропадает звук (гипотезы, не выводы)

1. Прекращение или повторное согласование ARC/eARC (HDMI-CEC/HAL; саундбар издаёт щелчок) после закрытия аудиопотока в MediaTek.
2. Аудиосервис переключает маршрут (HDMI ARC/eARC → динамики/другой sink) при смене состояния плеера.
3. Смена/отсутствие согласования кодека или passthrough после возобновления (`Auto/RAW` vs PCM).
4. Сам саундбар меняет вход/режим ожидания. Без снимков `dumpsys hdmi_control`, `dumpsys audio`, `dumpsys media.audio_policy` до/после паузы нельзя определить виновника.

## Проверка на FlixTone D11 Master и Everycom

- Подключать HDMI **ARC/eARC помеченный порт проектора** к **HDMI OUT (TV ARC/eARC)** саундбара; два HDMI-входа друг к другу не образуют ARC.
- Проверить физическую маркировку порта, включённый CEC и `sound_earc`; eARC capability/линк отобразится в MediaTek/AOSP diagnostics если служба его предоставляет.
- Сравнить три состояния одним сценарием: `playing`, `paused`, `resumed_no_sound` — штатная музыка/видео, без переключения аудионастроек между снимками.
- Для отделения проблемы формата временно выбрать PCM из обычного меню звука (изменение только в UI, с запоминанием прежнего режима) и повторить тест; не выполнять сбросов прошивки.
- При необходимости проверить воспроизведение при отключённом eARC, но включённом CEC, если устройство предоставляет явный ARC fallback.
- `dumpsys hdmi_control` может отсутствовать или не содержать поля vendor-MTK; пустой вывод **не доказывает отсутствия eARC**.

### Исследование

- Новый ограниченный аудит исходного APK: [GitHub Actions 43710386840](https://github.com/MAX-TAC/Projector-Soft/actions/runs/43710386840), artifact `8052-arc-earc-routing-audit` с Java-методами и manifest receiver.
- Предыдущая декомпиляция MediaTek calls: [GitHub Actions 40378960190](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40378960190).
- Инвентаризация реальных OEM callbacks: [GitHub Actions 40330275005](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40330275005).
- Метаданные: `research/oem-apps/Native_Controllers_8052/earc_native_control.json`, `earc_sound_callbacks.json`.

**Без ADB-логов и аппаратной проверки не объявлять работающим ни ARC, ни eARC на конкретном 8052/8054**, несмотря на наличие драйверных вызовов и интерфейсов.

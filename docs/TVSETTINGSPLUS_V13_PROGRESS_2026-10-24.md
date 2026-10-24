# TvSettingsPlus MOD v13 — встроенный EDID и штатный пятиполосный EQ

Дата: 24 октября 2026. Base: **RUIOS 8052, Android 13, MT9660**. APK **экспериментальный, на проекторе ещё не проверен**.

[Успешная GitHub Actions сборка 40119913189](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40119913189) · [Подписанный APK v13 и build report](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40119913189/artifacts/12570675887).

## Что перенесено в настоящее Android TV MainFragment

- Раздел «Изображение · Проектор»: 7 режимов, 4 цветовые температуры, 8 штатных видео/RGB ползунков с заводскими диапазонами, HDR и MEMC при наличии OEM значений.
- Раздел «Звук · Проектор»: 7 режимов звука, bass/treble и **пять настоящих полос OEM эквалайзера**: 120/500/1500/5000/10000 Гц, заводской диапазон 0–100 и шаг 1. Заводские ключи `sound_equalizer_detail_*hz`, реальные fusion sound ACFG идентификаторы `g_fusion_sound__equalizer_*`, обязательная проверка `PartnerSettingsConfig.getSettingsList("sound_equalizer_detail")`. Изменения в том же `Settings.Global` что исходные `EqualizerDetailFragment` и `ProgressBarDialog`. Не создавать фальшивые полосы на неподдерживаемой сборке.
- Раздел «Проекция · Проектор»: режимы установки перед/зад и стол/потолок через штатный `ProjectorManager`. Не трогать полноэкранные калибровки авто/ручного фокуса, трапеции и 4 углов.
- **Новый раздел «HDMI · Проектор»: «Совместимость HDMI (EDID)»**, заводские варианты 0=HDMI 1.4, 1=HDMI 2.0, 2=Автоматически, 3=HDMI 2.1. Включается **только** при активном HDMI-источнике (`PreferenceConfigUtils.isHDMISource`) и успешной проверке доступных аппаратных EDID версий через `MtkTvAVMode.getVideoInfoData(6, MtkTvVideoInfoBase).getU8EdidIndex()`. Неподдерживаемые версии не предлагаются. Применение — `Settings.Global.tv_input_hdmi_edid_version`, тот же API что заводской `EdidSourceSecondaryController`, с readback.
- Оставлены пять маршрутов в исходные OEM вкладки того же `TvSettingsPlus`, заводской Android TV launcher alias с gear icon/banner, девять совместимых Settings intent aliases, customer-0 brightness OEM bridge с fallback, скрыт только пункт режима BT колонки в OEM меню.

## Важные оговорки о HDMI

HDMI-CEC и eARC **нельзя безопасно перенести простым переключателем Settings.Global**. Заводской код `SourceSubMenuController` использует `mCompatibleApis.setHdmiCecControlEnabled`, `showCecEnergyDialog`, CEC auto-device-off, потенциально `MtkTvCecAnBase.setEarcEnable(false)`. Низкая задержка HDMI вызывает `resetTrapezoid()` на включении. Эти неполные direct toggles из MOD удалены, но исходные **рабочие OEM обработчики** остаются через вкладку «Источники». Настройка HDMI при старте также пока через OEM контроллер `PowerOnSourceSecondaryController`, который учитывает `Build.A_RJ_HDMI_PORT_COUNT`, `A_RJ_HDMI_PORTS`, `persist.trd.source_id` и реальные TV-input IDs.

## Технический результат

CI `40119913189`: success. Подписанный `com.android.tv.settings`, `versionCode=13`, тестовый совместимый platform certificate. Из 16 OEM DEX заменены только `classes13.dex` и `classes14.dex`; дополнен `classes17.dex`. Неизменённые OEM ресурсы, JNI, остальные DEX побайтово проверены. Проектор, экран/пульт, реальное поведение HAL, OTA и холодная загрузка пока не испытывались.

## До готового конечного тестового релиза не закрыто

1. Завершить HDMI-CEC, eARC, стартовый источник, питание и системные OEM настройки **через оригинальные аппаратные API**, не через фиктивные переключатели. Для пока не встроенных действий доступна оригинальная OEM вкладка внутри того же APK.
2. **KuSettings остаётся зависимостью:** `settingApps`, ссылки лаунчера, `services.jar`, второстепенные Intents и важный watchdog проверки `KuSourceRJ.TimeService`; нельзя отключать до замещения механизма восстановления и аппаратных испытаний.
3. Отдельная база 8054 должна использовать **свой оригинальный TvSettingsPlus APK**. Не применять 8052 APK к двух-HDMI проектору 8054 без доказанной совместимости.
4. Реальные тесты установки, выбора EDID, пяти полос звука, аппаратного декодирования/системы, пульта, отката (system privileged APK): **не выполнены**.

Версию v13 нельзя называть финальной безопасной версией для неподготовленного владельца. Результаты CI — статическая целостность APK, **не** runtime proof.

### Ссылки
- [Нативный EDID](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/OemHdmiEdidHook.java)
- [Эквалайзер 5 полос](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/OemFiveBandEqualizerHook.java)
- [Разделы и аудио/изображение](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/OemInlinePreferencesHook.java)
- [Аудит EDID](../research/oem-apps/Native_Controllers_8052/edid_poweron.json)
- [Аудит исходного эквалайзера](../research/oem-apps/Native_Controllers_8052/eq_dsp_path.json)
- [GitHub Actions v13](../.github/workflows/build-tvsettingsplus-inline-v13.yml)

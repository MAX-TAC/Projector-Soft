# TvSettingsPlus MOD v41 — первый аппаратный тест выбора аудиовыхода (8054)

Дата: 22.11.2026. Статус: **SOURCE IMPLEMENTED / UI TEST REQUIRED / HARDWARE NOT YET TESTED**.
Рабочая ветка: `feature/8054-v41-picture-sound-safety-20261122`, Draft PR #10.

## Доказательства: 8054 с работающим звуком

Реальная read-only диагностика владельца во время `ru.astroapps.hdrezka`:

- `dumpsys audio`: `AudioPlaybackConfiguration piid:119 deviceId:18 ... state:started usage=USAGE_MEDIA`, session 89.
- `dumpsys media.audio_policy`: `Speaker` (port 2), `BT A2DP Out / Redmi TV Soundbar` (port 18), `STRATEGY_MEDIA` (id 21) selects `AUDIO_DEVICE_OUT_BLUETOOTH_A2DP`.
- `dumpsys media.audio_flinger`: `Standby: no`, mixer output `AUDIO_DEVICE_OUT_BLUETOOTH_A2DP`; `0 Effect Chains` while track playing. Это прямое подтверждение A2DP media playback, но не глобальности OEM EQ.
- `settings global`: `picture_mode=0`, `sound_style=0`, Bass/Treble 50, пять EQ-band по 0.
- `dumpsys hdmi_control`: `mArcEstablished=false`. **ARC/eARC не выбран в первом тестовом picker**.

## Контракт реализации

`NativeMediaRouteBridge` (system UID / package `com.android.tv.settings`):
1. Проверяет `SDK_INT >= 33`, exact machine ID 8054, `checkSelfPermission(android.permission.MODIFY_AUDIO_ROUTING)` внутри процесса.
2. Через отражение находит ровно один `AudioProductStrategy`, поддерживающий `USAGE_MEDIA` и legacy `STREAM_MUSIC`. Numeric `21` не захардкожен.
3. Перечисляет **только реальные** `AudioManager.getDevices(GET_DEVICES_OUTPUTS)` и предлагает Speaker + подключённые A2DP. HDMI ARC/eARC, HDMI OUT, SPDIF, SCO, USB исключены из этого первого теста.
4. До записи копирует `AudioManager.getPreferredDevicesForStrategy` без модификации других стратегий или OEM settings.
5. Меняет media route через `setPreferredDevicesForStrategy` с `AudioDeviceAttributes(AudioDeviceInfo)`; при выборе Automatic удаляет override через `removePreferredDeviceForStrategy`.
6. Проверяет `getPreferredDevicesForStrategy` и публичный `getAudioDevicesForAttributes(USAGE_MEDIA)`. Для BT дополнительно сравнивает адрес. При переключении на Speaker проверяет сохранение подключённого BT A2DP.
7. В UI подтверждение реального звука через кнопку **«Звук работает»**; без подтверждения откат через 25 сек после успешного route readback. Время на первичный route-readback — 4.5 сек. При ошибке, disconnect или закрытии страницы — откат исходного списка устройств. Не изменяет `AudioManager.setStreamVolume`, `Settings.Global`, CEC, ARC, SPDIF, BT pairing.
8. Возможное отключение/падение процесса во время теста не позволяет гарантировать in-process timeout. Не тестировать без доступного ручного возврата к штатным настройкам.

## Сборка и допуск к установке

Только после AndroidX UI CI PASS на актуальном SHA, сборки **exact-8054** APK с v41, сравнения **сертификата подписи APK с исходным системным APK**, аудита DEX и возможности восстановления ранее установленной версии.

Текущий точный build workflow `.github/workflows/build-tvsettingsplus-8054-audit.yml` использует исходную прошивку 8054, восстанавливает оригинальный TvSettingsPlus APK, компилирует `src/main`+`src/exact8054`, собирает v41, проверяет подпись и публикует `TvSettingsPlus-8054-v41-MAINSETTINGS-EXPERIMENTAL.apk`. Следует запускать *на HEAD этой ветки*. **Не** считать более ранний v41 APK содержащим эти изменения. Исторический APK и hash из PR #2/#7 не подходят.

Перед заменой системного приложения сделать проверку `pm path`, `versionCode`, `SHA-256` установленного/кандидата и штатный rollback. Не делать `pm disable`, `pm clear`, `pm uninstall` без проверенного восстановления.

## Порядок аппаратного теста

1. Подключить Redmi TV Soundbar по BT; убедиться, что звук есть и BT не отваливается. Уменьшить громкость заранее: в дампе индексы Music BT=19, Speaker=25, это *разные* настройки устройств.
2. Запустить контент с непрерывным воспроизведением Media. При переключении в Settings приложение воспроизведения не должно останавливаться; если паузится, выбрать фоновой плеер или способ воспроизведения без потери focus.
3. Открыть TvSettingsPlus MOD v41 → **Звук → Устройство вывода звука** → **Встроенные динамики**. Должен появиться тестовый диалог с таймером. Если звук реально идёт из Speaker, нажать **«Звук работает»** до истечения 25 секунд. **Если звук не появился — не подтверждать**: автоматический откат или «Отменить и вернуть».
4. После успешного перехода проверить, что Redmi TV Soundbar остаётся именно подключённым, а не просто сопряжённым.
5. В том же меню выбрать **Redmi TV Soundbar**, подтвердить только когда звук на нём действительно слышен.
6. Отдельно проверить **Автоматически (Android AudioPolicy)**; при этом предпочтение media output должно исчезнуть и системная политика вернёт обычный выбор.
7. Отдельно негативный тест: снова выбрать Speaker и **не подтверждать** — через 25 секунд проверить восстановление прежнего preferred route.
8. Во всех стадиях прочитать AudioPolicy, AudioFlinger, Bluetooth connected devices и журналы. Ни Factory DSP, ни заводские изображения не трогать.

### Команды Termux после каждого шага

Выбрать метку `before`, `speaker`, `bluetooth`, `automatic`, `timeout` и подставить её вместо `STAGE` (это только имена файлов):

```bash
STAGE=speaker
DIR=/sdcard/Download/8054_route_check
mkdir -p "$DIR"
adb shell dumpsys audio > "$DIR/${STAGE}_audio.txt"
adb shell dumpsys media.audio_policy > "$DIR/${STAGE}_policy.txt"
adb shell dumpsys media.audio_flinger > "$DIR/${STAGE}_flinger.txt"
adb shell logcat -d -s TvSettingsPlusMOD:V AndroidRuntime:E '*:S' > "$DIR/${STAGE}_logcat.txt"
(cd "$DIR" && zip -q -r ../8054_route_check.zip .)
```

Полная диагностика содержит MAC-адрес BT и идентификаторы процессов: перед публичной публикацией их нужно обезличить.

### Критерии PASS

- При Speaker: `STRATEGY_MEDIA` указывает Speaker, `AudioFlinger` действительно пишет в Speaker; BT A2DP остаётся подключён; реальный звук слышен на Speaker; `getPreferredDevicesForStrategy` содержит Speaker.
- При Bluetooth: MEDIA указывает BT A2DP, звук слышен на Soundbar; `getPreferredDevicesForStrategy` содержит соответствующий адрес.
- При Automatic: `Preferred devices for strategy` снова не содержит MEDIA override (если исходно его не было).
- При timeout/cancel: возвращён ровно первоначальный preferred-device список; другие маршруты не изменились, устройство не перезагружалось, Bluetooth pairing не потерян.

Ключевой blocker: `MODIFY_AUDIO_ROUTING` может оказаться недоступным в runtime модифицированного APK несмотря на `granted=true` в dump общей системной учётной записи. Тогда picker покажет отказ и **не выполнит ни одной записи**.

## Экстренный возврат

Не подтверждать выбор, дождаться in-process timeout. Если приложение или его процесс закрылся до отката, открыть системное меню звука и переключить выход обратно на Bluetooth либо восстановить автоматическую AudioPolicy. Не применять force routing через shell с неизвестными кодами `service call`. В журнале искать `TvSettingsPlusMOD` и сообщения `MEDIA route rollback NOT verified`.

Дальнейшие тесты HDMI ARC/eARC и сторонних эквалайзеров не входят в эту проверку.

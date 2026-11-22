# TvSettingsPlus MOD v41 (8054) — первый безопасный пакет Picture/Sound

**22.11.2026.** Это source-only изменение на отдельной feature-ветке. Не является испытанием установленного APK, подтверждением маршрутизации BT↔speaker или готовым выпуском. Основа: требования `TVSETTINGSPLUS_8054_PICTURE_SOUND_OUTPUT_REQUIREMENTS_20261122.md` и пять read-only ADB дампов владельца.

## Подтверждённый baseline из 8054

- `media.audio_policy`: доступны `Speaker` и `BT A2DP Out (Redmi TV Soundbar)`; `STRATEGY_MEDIA` выбирает A2DP.
- `dumpsys audio`: в `Preferred devices for strategy` нет явного выбора. A2DP подключён, но это **не** доказательство успешного принудительного переключения на Speaker.
- `audio_flinger`: `0 Effect Chains` на момент простоя; системная библиотека Equalizer присутствует, глобальный эффект на BT/eARC не доказан.
- `hdmi_control`: CEC установлен в ON; `mArcEstablished=false`. Нельзя объявлять eARC как подключённый только по generic HDMI ARC port.
- `dumpsys package com.android.tv.settings`: системный UID 1000, `MODIFY_AUDIO_ROUTING: granted=true` в общем состоянии разрешений. Перед setter нужен `checkSelfPermission` внутри обновлённого APK. Пока маршрутизация не реализована.

## Состав изменений

1. **Picture:** `PictureUserEditPolicy` разрешает экспериментальную регулировку только при exact machine `8054`, стабильном `picture_mode=0` и наличии OEM User ID `0` в динамическом каталоге. Factory presets показывают текущее значение с пометкой «только просмотр», редактирование отключено. Guard вызывается также внутри callback, непосредственно перед `Settings.Global.putInt` и при подтверждении readback. Это НЕ решает неизвестную адресацию MediaTek `inputGroup=-1` и не разрешает развертывание editor в production.
2. **Sound:** `NativeEqualizerPanel` расширен с пяти полос до семи пользовательских регуляторов: Bass/Treble плюс пять OEM EQ полос. Все доступны только в `sound_style=User`, а их запись дополнительно ограничена единственным Media-маршрутом `TYPE_BUILTIN_SPEAKER`. При BT/eARC/неизвестном маршруте значения остаются видимыми, запись закрыта. Для Surround применена та же User-only policy, заводские пресеты read-only.
3. **Audio output (экспериментальная реализация):** пункт «Устройство вывода звука» открывает список подключённых Speaker / Bluetooth A2DP и режима Android AudioPolicy. `NativeMediaRouteBridge` получает MEDIA `AudioProductStrategy` динамически из Android 13, проверяет `MODIFY_AUDIO_ROUTING` в процессе, сохраняет исходный список preferred devices, назначает выбранный выход через `setPreferredDevicesForStrategy` и читает фактический Media route. `NativeMediaRoutePicker` запрещает повторные операции, требует подтверждения реального звука и откатывает маршрут при ошибке, отключении Bluetooth, уходе со страницы или тайм-ауте 25 секунд. Связь Bluetooth сама не разрывается. eARC отсутствует в первой активной версии списка.
4. Добавлены UI/JVM регрессионные тесты User/factory mapping, типизированных названий аудиоустройств и допускаемых Media outputs (только Speaker/A2DP). Полноценный интеграционный тест route setter/rollback возможен только на аппаратном 8054.

## Зафиксированные ограничения

- `picture_mode` / `sound_style` используют OEM consumer backend; автоматического copy-on-edit в User нет.
- UI источника и текущий HDMI/SDR/HDR profile до native write достоверно не установлен. Нет безопасного универсального rollback для PQ.
- Смена медиа-выхода без BT-disconnect **реализована в исходниках, не испытана аппаратно**. Нужен exact-8054 APK build и обратимый тест на физическом проекторе. Критично проверить `checkSelfPermission` уже в модифицированном APK, реальный звук на Speaker/BT, правильный readback Media route и отсутствие Bluetooth-disconnect.
- Native Picture mode picker по-прежнему read-only в данном пакете; открытие User через штатный OEM Android TV экран допускается. Отдельный нативный picker — следующая итерация после profile scope теста.
- Никакие Factory PEQ / PQ defaults / DSP calibration / China KuSettings Activity не используются как прокси; никакого отключения KuSettings, CEC, eARC, флэшинга или изменения оригинального APK.

## Прежде чем давать APK пользователю

- Дождаться успешной Android 13 AndroidX UI CI и exact-8054 APK build; привязать SHA коммита и SHA-256 APK.
- Подтвердить, что в собранном APK присутствуют все изменённые классы и правки интерфейса, и сохранена совместимая оригинальная подпись/UID.
- Через штатное Android TV меню владельцу выбрать профиль User и снять один read-only baseline `picture_mode`, `sound_style`, `media.audio_policy` при воспроизведении через Speaker.
- Отдельно разрешить один ручной шаг контраста в **штатном** UI и полный возврат. Сопоставить значения в HOME/HDMI1/HDMI2 и SDR/HDR. До завершения этого теста не объявлять NativePicturePanel write аппаратно безопасным.
- EQ Bass/Treble и каждую полосу тестировать на динамиках и BT одним и тем же сигналом с возвратом исходного значения. Нельзя считать Global/Config echo доказательством физического эффекта.
- Если целевой v41 stock PictureActivity используется как корневой маршрут в другой ветке, нельзя просто перенести его: это обойдёт собственную User-only политику. Нужна интеграция после теста.

## Проверка на 8054 с играющим Media

Перед установкой необходимы build+signature+APK hash и явный rollback исходного приложения. Пошаговый протокол: `docs/TVSETTINGSPLUS_8054_AUDIO_ROUTE_HARDWARE_ACCEPTANCE_20261122.md`.

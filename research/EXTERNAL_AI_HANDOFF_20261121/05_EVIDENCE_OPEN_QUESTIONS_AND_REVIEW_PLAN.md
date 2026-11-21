# 05. Матрица доказательств, противоречия, открытые вопросы и план независимого аудита

**Срез:** 2026-11-21. Этот документ намеренно скептический. Он отделяет результат статической декомпиляции и GitHub Actions от испытаний на настоящем проекторе. Если другой ИИ обнаружит несовместимость source/commit или неверную интерпретацию, итоговое заключение должно **исправить** наши предыдущие выводы, а не повторять их.

## 1. Шкала достоверности для заключения

| Код | Значение |
|---|---|
| `RELEASE_METADATA` | Реальное имя архива, digest/size/URL из GitHub Release. Ценность: provenance, не аппаратная функциональность |
| `STATIC_CONFIRMED` | Подтверждённая последовательность инструкций/ресурс/manifest/конфиг в определённом SHA source |
| `BUILD_TESTED` | GitHub Actions compile, unit, AndroidX/Robolectric, D8, APK packaging; это **не** физическая работа |
| `DEVICE_OBSERVED_OTHER` | Реальный ADB от **другого** 8052 (владелец не совпадает с owner 8054); нельзя переносить результат |
| `DEVICE_8054_VERIFIED` | Точная функция испытана с исходным значением, эффектом и возвратом на владельческом FlixTone 8054 |
| `UNVERIFIED` | Основание для решения недостаточно, даже если имена методов/функции выглядят логично |
| `DEFERRED` | Сознательно отложено, не закрыто и не потеряно |

**На срезе нет доказательства `DEVICE_8054_VERIFIED` для полного пользовательского переноса или безопасности отключения KuSettings.** В другом контексте могли выполняться ADB осмотры/электронные измерения владельца, но они не дают автоматического статуса полного аппаратного acceptance.

## 2. Claims / evidence matrix

| Утверждение | Уровень | Основание и границы |
|---|---|---|
| Существуют заводские 1-HDMI и 2-HDMI PKG, OTA 8052/8053 и owner dump 8054 | RELEASE_METADATA + STATIC_CONFIRMED | [firmware releases](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/releases); [dump release](https://github.com/MAX-TAC/FlixTone_8054_Backup/releases/tag/FlixTone_8054_Backup), SHA gates в Actions |
| Заводские PKG расшифровываются в EROFS/Android 13 | STATIC_CONFIRMED | [Original_Firmware/DECRYPTED_SUCCESS](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/blob/main/analysis/Original_Firmware/DECRYPTED_SUCCESS.md). Не означает валидный reflash |
| 8052 октября имеет некоторые бинарно одинаковые приложения/конфиги с factory 8054 сентября | STATIC_CONFIRMED | [Latest comparison](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/blob/latest-8052-vs-stock-8054-20261021/analysis/Latest_8052_OTA_vs_8054_Stock/README.md). Не переносить автоматически на актуальный live 8054 |
| 8053 — не проверенный hardware target D11 | UNVERIFIED compatibility | [OTA comparison](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/blob/main/analysis/RUIOS_OTA_Comparison/ENGINEERING_SUMMARY_RU.md) |
| A/B OTA metadata подписана AOSP RSA-2048 testkey | STATIC_CONFIRMED | [OTA signature report](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/blob/main/analysis/RUIOS_OTA_Signatures/README.md); не сравнивать с AVB |
| OEM AVB key RSA4096 не AOSP common RSA4096 test key | STATIC_CONFIRMED | [AVB comparison](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/blob/main/analysis/HDMI_Comparison/AVB.md) · [test key audit](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/blob/research-ota8052-avb-key-match-20261021/analysis/8052_Live_ADB_20261021/AVB_KEYS_RESEARCH_RU.md) |
| OEM MBOOT 8052 содержит comparator AVB-trusted key | STATIC_CONFIRMED | [Ghidra mboot](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/blob/research-ota8052-avb-key-match-20261021/analysis/AVB_Chain_RE_8052/README_RU.md); не доказан каждый boot path и не доказан 8054 |
| Другой реальный 8052 locked/green | DEVICE_OBSERVED_OTHER | [ADB report](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/blob/research-ota8052-avb-key-match-20261021/analysis/8052_Live_ADB_20261021/README_RU.md). Live 8054 boot states требуются отдельно |
| Custom v0 имеет два NONFLASHABLE overlays | BUILD_TESTED | [Build results](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/blob/custom-v0-staging-8052-8054-20261021/custom-v0/BUILD_STATUS_RU.md). Это не образ system/super и не OTA |
| Original exact 8054 OEM APK / Android+vendor доступен для исходного анализа | RELEASE_METADATA + STATIC_CONFIRMED | [exact dump](https://github.com/MAX-TAC/FlixTone_8054_Backup/releases/tag/FlixTone_8054_Backup) · [native inventory](https://github.com/MAX-TAC/Projector-Soft/blob/research/8054-mediatek-native-api-20261120/research/8054/native/README.md) |
| `Settings.Global.picture_contrast` → TvServices → `g_video__contrast` → JNI | STATIC_CONFIRMED | [PIC04 contract](https://github.com/MAX-TAC/Projector-Soft/blob/research/8054-mediatek-native-api-20261120/research/8054/native/PRODUCT_001_PIC04_INTEGRATION_CONTRACT.md) · [JNI trace](https://github.com/MAX-TAC/Projector-Soft/blob/research/8054-mediatek-native-api-20261120/research/8054/native/TRACE_004_CONFIG_JNI_TO_HIDL.md) |
| `NATIVE-005` нашёл независимый service→RPC setter edge | STATIC_CONFIRMED | [Trace 005](https://github.com/MAX-TAC/Projector-Soft/blob/research/8054-mediatek-native-api-20261120/research/8054/native/TRACE_005_CONFIG_HIDL_SERVICE_RPC.md); user function context/visual effect не следуют |
| MOD v41 MainSettings и 8 разделов компилируются в original APK environment | BUILD_TESTED | [v41 APK](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44769301913/artifacts/14645503299), [menu spec](https://github.com/MAX-TAC/Projector-Soft/blob/audit/8054-tvsettingsplus-20261117/docs/8054_FINAL_MENU_SPEC.md) |
| PR #4 policy в fake Port тестах пропускает один шаг | BUILD_TESTED | [CI](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44808298783) · [code review](https://github.com/MAX-TAC/Projector-Soft/blob/research/8054-mediatek-native-api-20261120/research/8054/native/PRODUCT_001_PR4_CODE_REVIEW.md) |
| PR #5 подключил прямой UI writer для PIC03–10 | STATIC_CONFIRMED + BUILD_TESTED | [source](https://github.com/MAX-TAC/Projector-Soft/blob/feature/8054-user-settings-migration-20261120/app/tvsettingsplus-mod/src/exact8054/java/dev/projectorsoft/tvsettings/NativePicturePanel.java) · [AndroidX](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44816235981) · [exact build](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44816462929). Не признан DEVICE_8054_VERIFIED |
| `com.my.settings` нуждается для boot/TimeService recovery | STATIC_CONFIRMED | [Ku dependencies](https://github.com/MAX-TAC/Projector-Soft/blob/audit/8054-tvsettingsplus-20261117/research/8054/KUSETTINGS_SERVICE_MIGRATION_8054.md); будущий MOD replacement disabled, а не реально запущен |
| KuSettings можно сейчас удалить | **НЕТ: UNVERIFIED/НЕ ДОПУЩЕНО** | UI и background service независимость не достигнуты; recovery/thermal/remote ещё проверять |

## 3. Какие старые заключения были уточнены или перестали быть актуальными

1. **«PKG не расшифрован»**: ранний high-entropy анализ из `Original_Firmware/CONCLUSIONS.md` устарел после найденного MSTAR_SECURE AES key и декодирования образа. Использовать [DECRYPTED_SUCCESS](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/blob/main/analysis/Original_Firmware/DECRYPTED_SUCCESS.md).
2. **«Оригинальный живой 8054 dump недоступен»**: более поздний release `FlixTone_8054_Backup.zip` с оригинальными разделами уже доступен; не возвращаться к предположениям, основанным только на 8052.
3. **«Всё MT9660, значит одинаково»**: Android reports `mt5873`; real 8052 vs 8054 имеют разные hardware identities, 8053 вообще не привязан к D11.
4. **«AOSP test OTA = подписать и прошить что угодно»**: OTA metadata и OEM AVB RSA4096 имеют отдельные ключи; mboot contains key validation. Подписанный OTA-контейнер не подтверждает Android загрузку.
5. **«KuSettings — только GUI»**: найден критичный boot/TIME_TICK → `KuSourceRJ.TimeService` recovery; раннее отключение опасно.
6. **«PR #4 — готовый contrast slider»**: PR #4 — чистая policy без `Port`/UI. PR #5 уже имеет прямой обработчик, но его аппаратное поведение не доказано и рискованно.
7. **«Нужно закончить всю JNI/HIDL архитектуру, прежде чем переносить один ползунок»**: это избыточный рабочий gate, если уже доказан OEM `Settings.Global` путь. Однако полный device safety/restore нельзя подменять одним provider echo.
8. **«Тест AndroidX означает работает на проекторе»**: это software/build, не field test. Исторические CI должны быть прочитаны с точным `head_sha`; один старый FAILED был исправлен на следующих коммитах.

## 4. Вопросы, на которые независимый ИИ должен ответить по источникам

### 4A. По прошивкам

1. Какая у реального FlixTone 8054 фактическая `RUIOS` версия, current slot, AVB `locked/green` и trust anchor? Не путать с прежним **8052** ADB.
2. На 8054 обнаружена ли независимая OTA 8054, или доступна только factory PKG + own backup? Где доказательство приёмки vendor OTA? В октябрьском исследовании публичный 8054 JSON возвращал 404 в проверенной схеме; не считать это доказательством полного отсутствия прошивок.
3. Если автор предложит flashable custom, где доказательства `mboot` acceptance, safe recovery, AVB key/rollback, права записи и защиты factory? Требовать точные байты/CI/ADB, а не универсальный рецепт.
4. Какие board-specific файлы идентичны у **актуальных** 8052/8054, а какие сохранять раздельно? Развести изменения по дате и плате.
5. Есть ли подтверждение, что real 8054 backup содержит **полный** набор для восстановительной прошивки? Один `super.img` и `boot_a/vbmeta` не доказывают, что factory/persist/BootROM восстановимы.

### 4B. По TvSettingsPlus MOD

6. Можно ли выполнить подавляющее большинство обычных Picture/Sound controls просто через OEM provider/Java backend без HIDL RE? Проанализировать точные оригинальные onValueChanged/onPreferenceChange и все условия, не только один вызов `putInt`.
7. Есть ли общий рабочий оригинальный contrast increment (не fake 2 / provisional 1)? Как его показать владельцу без опасного ADB записи?
8. Где точный источник **active main HDMI/SDR/HDR/profile** и необходимость проверок? Убедиться, что требования не стали искусственным вечным `UNKNOWN_CONTEXT`, но и не принять plugged input за active.
9. Не создаёт ли `NativePicturePanel` после миграции race между async `SettingObserver`, `refresh()`, bottom dialog, `readMediaTekValue`, `onDismiss` и восстановлением первоначального значения? Что подтверждает реальное OEM поведение?
10. Вспомогательные графические переходы в `com.android.tv.settings.uicustom` и `com.coolux.source` — достаточно ли independent от KuSettings? Отдельно `OemProjectionPanel.projectionIntent` на `com.my.settings`.
11. Насколько согласованы в PR #5 `SharedPreferences` journal, контекст HDMI, timeout и popup? Где rollback для multiple steps, смены источника или отключения электричества?
12. Что из существующих адаптеров звука реально пишет в DSP/backend, а что лишь `Settings.Global`/summary? Дать полный функциональный gap list по 96 строкам.
13. Какие Android privileged/signature rights точно требуются для обновления stock APK, какие OEM classes использует reflection, и не происходит ли implicit UI fallback в китайское меню?
14. Все consumer settings из старого меню представлены в v41? Есть ли дополнительные пункты, отсутствующие в current 96 registry (меню зависит от SKU/customer settings)?
15. Можно ли перенести `KuSettings.MySetTimeService` recovery в MOD или отдельный привилегированный service без Android 13 background restrictions и дублирующихся watchdog? Какие факты нужны с устройства?

## 5. Приоритет независимых проверок

| Очередь | Результат аудита | Что строго не делать в ходе исследования |
|---|---|---|
| P0 | Проверка правдивости входных SHA, релизов, branch heads, doc source/CI, conflicts и невыполненных гипотез | Не начинать с того, что исходные выводы верны |
| P1 | Доказательная таблица «каждый stock user control → storage/backend → MOD handler → статус» для v41 | Не считать 96 строк автоматически 96 consumer controls |
| P2 | Минимальный PIC03–PIC10 UI/provider integration review, отсутствие dangerous autowrites, реальный OEM шаг | Не ставить APK на owner device без разрешения |
| P3 | Звук/HDMI/проекция/питание: backlog реализации по группам и side effects | Не тянуть всё к голому HIDL без причины |
| P4 | M1 Chinese UI independence audit: все Activity/Intent/Fragment/fallback | Не удалять ещё действующие OEM services |
| P5 | Критические `KuSettings` receivers, boot recovery, HID/thermal lifecycle, M2 plan | Не `pm disable`, reboot/OTA/flash без отдельного разрешения |
| P6 | Отдельный независимый feasibility verdict по полноценной кастомной RUIOS | Не выдавать любой staging ZIP за полноценный ROM |

## 6. Рекомендуемые результаты ревизии другого ИИ

Другому ИИ следует выдать:

- **Независимый verdict**: 8052/8053/8054 прошивки и runtime lineage, техническая возможность полного custom ROM, безопасный путь распространения TvSettingsPlus.
- **Errata list**: каждая найденная фактическая ошибка, устаревший документ, неработающая ссылка, неполная проверка и альтернативная гипотеза с evidence.
- **98–100% исчерпывающий потребительский gap**: какой штатный OEM пункт ещё не перенесён, его конкретный getter/setter/side effects, код v41, приоритет и тест; то, что не поддерживается 8054, отдельно.
- **Executable plan**: ветки/PR, кодовые изменения, какие CI запустить, какой APK не ставить, как валидировать без hardware и какой **один** минимальный разрешённый device-test нужен дальше.
- **Отдельная оценка** по M1 UI-independence и M2/M3 package-removal readiness.
- Никаких «готово» по одному успешному компилятору; отчёт с ссылками на первичные SHA/классы/методы/Actions.

## 7. Минимальный read-only набор будущих owner observations (НЕ выполнять автоматически)

Уже документированы `preflight.py --picture-stock` и `DEVICE_TEST_MATRIX_8054.md`. Для первой проверки должен быть реальный контакт с проектором **только по разрешению пользователя**. При необходимости вернуть владельцу конкретные команды:

```sh
adb shell getprop ro.rjpj.machine.id
adb shell getprop ro.rjpj.yunota.version
adb shell getprop ro.build.fingerprint
adb shell getprop ro.boot.vbmeta.device_state
adb shell getprop ro.boot.verifiedbootstate
adb shell settings get global picture_mode
adb shell settings get global picture_color_temperature
adb shell settings get global picture_list_hdr
adb shell settings get global picture_contrast
```

Эти команды только **читают** и сами по себе не устанавливают активный HDMI, stock step, физическую контрастность, состояние служб и отсутствие нежелательных side effects. Для STOCK step необходим **отдельный ручной тест через оригинальный GUI**, запись C0/C1 и восстановление C0, без `adb shell settings put`.

[Аппаратная матрица](https://github.com/MAX-TAC/Projector-Soft/blob/audit/8054-tvsettingsplus-20261117/research/8054/DEVICE_TEST_MATRIX_8054.md) · [STOCK protocol](https://github.com/MAX-TAC/Projector-Soft/blob/audit/8054-tvsettingsplus-20261117/research/8054/STOCK_PICTURE_DIAGNOSTICS_8054.md) · [KuSettings dependency audit](https://github.com/MAX-TAC/Projector-Soft/blob/audit/8054-tvsettingsplus-20261117/research/8054/KUSETTINGS_DEPENDENCIES_8054.md).

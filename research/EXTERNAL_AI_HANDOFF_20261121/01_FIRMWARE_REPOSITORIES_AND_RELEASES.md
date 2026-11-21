# 01. Прошивки 8052 / 8053 / 8054: репозитории, релизы, доказанная совместимость

**Срез:** 2026-11-21. **Назначение:** независимому исследователю необходимо работать с конкретными архивами, а не отождествлять торговое имя MT9660, Linux `mt5873`, количество HDMI и номер RUIOS.

## 1. Три GitHub-репозитория

| Репозиторий | Назначение | Что считать первоисточником |
|---|---|---|
| [MAX-TAC/Everycom-Cinelux-Firmware](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware) | Заводские 1-HDMI/2-HDMI PKG, OTA 8052/8053, статические исследования, сравнения и эксперимент Custom v0 | [Заводские релизы](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/releases); исследования преимущественно в **отдельных ветках**, а не только в main |
| [MAX-TAC/FlixTone_8054_Backup](https://github.com/MAX-TAC/FlixTone_8054_Backup) | Снятый с настоящего FlixTone D11 Master 8054 резервный дамп и несколько исходных разделов, инструменты аудита | [FlixTone_8054_Backup release](https://github.com/MAX-TAC/FlixTone_8054_Backup/releases/tag/FlixTone_8054_Backup) |
| [MAX-TAC/Projector-Soft](https://github.com/MAX-TAC/Projector-Soft) | Изменения `TvSettingsPlus.apk`, декомпиляция Android/OEM, код MOD, исследовательские отчёты, Actions и APK-кандидаты | [PR #2](https://github.com/MAX-TAC/Projector-Soft/pull/2), [PR #3](https://github.com/MAX-TAC/Projector-Soft/pull/3), [PR #4](https://github.com/MAX-TAC/Projector-Soft/pull/4), [PR #5](https://github.com/MAX-TAC/Projector-Soft/pull/5) |

## 2. Реальные исходные образы — больше трёх файлов

Размеры и digest далее приведены по публичным metadata GitHub Releases. Публикация digest **не означает**, что архив повторно скачан и хеш вычислен при подготовке этого handoff. Там, где существующий Actions проверил SHA, об этом указано отдельно.

| № | Источник / релиз | Тип, реальная версия | Размер (байты) | SHA-256 | Статус |
|---|---|---|---:|---|---|
| 1 | [Original Firmware for 1 HDMI](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/releases/tag/Original_Firmware_for_1_HDMI_ports) · `4K.HDMI-.-20260813.1.rar` | Заводской PKG внутри RAR, **8052 / RUIOS.8052.1.0.1**, 1 HDMI, август 2026 | 1 122 636 304 | `bff981a9d440d1495bd6ab48a72fb13c4572de69fcbe81448fc117a3bba5f30f` | Получен, расшифрован и изучен офлайн |
| 2 | [Original Firmware for 2 HDMI](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/releases/tag/Original_Firmware) · `4KTOF.HDMI-.-DPLS.-09.30.rar` | Заводской PKG, **8054 / RUIOS.8054.1.0.1**, 2 HDMI, ToF-профиль, сентябрь 2026 | 1 301 025 233 | `170f745dc1711c2dae6410dd51f0a8bf5bb7c637e11b8dd3e5f59fbea8c388f3` | Получен, расшифрован и изучен офлайн; идентичность физической платы D11 не следует из номера 8054 |
| 3 | [OTA_Updates](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/releases/tag/OTA_Updates) · `RJMT9660_firmware_8052.zip` | Полная A/B OTA `RUIOS.8052.1.0.4`, build `eng.suhaix.20261012.025220`, октябрь 2026 | 851 685 795 | `bedea70fbf3f08d3c18e59d5f91e7d12828c77fac314c30fcc98332c32c88e35` | Декодирована, сравнивалась с заводским 8054; имеется отдельный живой ADB-отчёт 8052 |
| 4 | [OTA_Updates](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/releases/tag/OTA_Updates) · `RJMT9660_firmware_8053.zip` | A/B OTA `RUIOS.8053.1.0.0`, build `eng.luzhih.20260612.083454`, июнь 2026 | 841 648 271 | `9ca8276448e79587b82e55cec0cf3da55154b750774f608378f64619c84f556b` | Декодирована; **8053 не подтверждён как плата FlixTone D11 или универсальный профиль** |
| 5 | [Дамп реального 8054](https://github.com/MAX-TAC/FlixTone_8054_Backup/releases/tag/FlixTone_8054_Backup) · `FlixTone_8054_Backup.zip` | Дамп действующего FlixTone 8054, **не заводской установочный PKG**; включает `super.img` и др. | 2 011 878 129 | `4f53f2224c98b4c832a19a591b9dd7f925bde28528b5674a09732c0f0e41d362` | SHA проверялся в CI exact-8054; основа актуального исследования TvSettingsPlus |

В последнем релизе отдельно опубликованы: `boot_a.img` (78 643 200 байт, SHA `f1fc341533babecc66ab697df3943464a658c9ae8a03a035f2bf5a30529f1c63`), `dtbo_a.img` (8 388 608; `7b6face54f66968c533041d9ce38971eb7f5064d1357e46fb223a3fbe3cffded`), `vbmeta_a.img` (1 048 576; `0131481e6ac5533de54c3cc6a052d149ba135d2195fd31242f18bbb2a893eb19`), `vbmeta_system_a.img` (1 048 576; `d8b2856abd6920fb9ca25b9b487365386ca0e746e379d1275b92cc9a15d23e39`).

**Терминология:** в исторических отчётах слово «три прошивки» иногда относится к **каналам 8052, 8053 и 8054**, а не к количеству файлов. Для будущих кастомных аппаратных профилей планировались **8052 и 8054**, тогда как **8053 — сравнительный источник**, не подтверждённая целевая аппаратная платформа.

## 3. Форматы и ключевые технологические свойства

| Признак | Заводские PKG 8052/8054 | OTA 8052/8053 | Снятый дамп FlixTone 8054 |
|---|---|---|---|
| Container | `upgrade_image.pkg` в RAR; MStar Secure New, AES-256-CBC, LZ4 и OEM upgrade script | Android A/B payload, `CrAU` v2 | ZIP с полученными образами |
| Файловые системы | Android 13, EROFS, динамический `super`, разделы `system/vendor/product/system_ext` | Те же логические разделы восстанавливаются из payload | `super.img` 2 225 078 272 байта; извлечены `system_a/vendor_a/product_a/system_ext_a` |
| APK и native code | Раздельные OEM профили; ряд APK между старым 8052 и 8054 отличается | OTA 8052 и 8053 существенно различаются по составу/байтам | Точный 8054 backend и ELF; **1 283 ELF** библиотек, 300 кандидатов для native анализа |
| Подпись | OEM PKG authentication не повторена; Android vbmeta SHA256_RSA4096 | OTA metadata RSA2048/SHA256, AOSP testkey; **не** AVB-ключ | Опубликованы оригинальные vbmeta для сравнения; доверие/возможность own-key flash не доказаны |

Ссылки: [успешная расшифровка PKG](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/blob/main/analysis/Original_Firmware/DECRYPTED_SUCCESS.md), [сравнение HDMI профилей](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/blob/main/analysis/HDMI_Comparison/UNIVERSAL_ROM_ASSESSMENT_RU.md), [OTA engineering summary](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/blob/main/analysis/RUIOS_OTA_Comparison/ENGINEERING_SUMMARY_RU.md), [exact dump/native](https://github.com/MAX-TAC/Projector-Soft/blob/research/8054-mediatek-native-api-20261120/research/8054/native/README.md).

## 4. 8052 против 8054: устройство, версии, опасность кросс-прошивки

По документированному сравнению заводских систем:

| Свойство | 8052 (один HDMI) | 8054 (два HDMI) |
|---|---|---|
| `ro.rjpj.machine.id` | `8052` | `8054` |
| `ro.rjpj.hdmi.port.count` | 1 | 2 |
| `ro.rjpj.hdmi.ports` | `HW6:HDMI` | `HW6:HDMI1,HW7:HDMI2` |
| `ro.product.ota` | `V618RJEN` | `V618RJENHD` |
| `ro.rjpj.support.toffunction` | 0 | 1 |
| `ro.rjpj.yunota.url` | `mt9660/8052/8052_ota.json` | `mt9660/8054/8054_ota.json` |
| Первичный заводской boot, DTBO, OP-TEE, Linux/TV services | Собственный board profile | **Другие SHA; не смешивать** |

Сравнение **последней OTA 8052** с более старой factory 8054 показывает неожиданное: `board.ini`, `dtbo_mapping.ini` и конфигурации звука у этих двух сборок побайтно совпадают, хотя физические машины имеют разные SKU/HDMI. Это разрушает слишком простое предположение «каждое отличие board.ini = физическое отличие HDMI». При этом default config/gamma/PMU различаются, а совпадение config **не доказывает взаимозаменяемости устройств**.

Выпуск 8052 OTA содержит `TvSettingsPlus`, `KuSettings`, `KuSourceRJ`, `trdservice`, `trdmiddle`, `Factory` и `KeystoneCorrection` с APK/DEX, **побайтно идентичными** сентябрьскому 8054 factory (по соответствующему проверенному сравнительному отчёту); `RuiAutoFocus` отличается. **Не переносить этот вывод на все версии и на живой FlixTone backup автоматически.**

Источники: [сток 8052/8054](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/blob/stock-custom-architecture-8052-8054-20261021/analysis/Stock_8052_vs_8054_Board/README.md), [последняя 8052 vs 8054](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/blob/latest-8052-vs-stock-8054-20261021/analysis/Latest_8052_OTA_vs_8054_Stock/README.md).

## 5. Что известно с реально работающего 8052 и что нельзя переносить на 8054

Из анонимизированного отчёта ADB **другого владельца** на 8052, 2026-10-21:

- `ro.boot.vbmeta.device_state=locked`, `ro.boot.verifiedbootstate=green`; SELinux `Permissive`, userdebug, ADB без secure flag; наличие бинарника `su` **не доказывает root**.
- Активный A/B slot был `_b`; `update_engine` работал.
- Android EROFS `system/vendor/product/system_ext`, F2FS `/data`; присутствуют `super`, OEM разделы tvconfig, tvservice, eeprom, factory, persist.
- Показания Android thermal относятся к CPU/SoC, **не к вентилятору/LED/LCD**. Нулевой `fan` в HAL не означает остановку физического вентилятора.
- Стандартный implicit `android.settings.SETTINGS` в стоке разрешался в `com.my.settings`; `com.android.tv.settings.MainSettings` существовал отдельно.
- Все эти факты относятся **именно к проверенному 8052**. Live 8054 не следует объявлять аналогично locked/green без его собственных показаний.

[Оригинальный read-only report](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/blob/research-ota8052-avb-key-match-20261021/analysis/8052_Live_ADB_20261021/README_RU.md).

## 6. Ветвление исследований и воспроизводимость

В `Everycom-Cinelux-Firmware` отчёты содержатся **в разных ветках**. Для AVB-реверса: `research-ota8052-avb-key-match-20261021`; для архитектуры кастома: `stock-custom-architecture-8052-8054-20261021`; для Custom v0: `custom-v0-branding-debloat-ota-design-20261021`; для позднего PKG vs OTA сравнения: `latest-8052-vs-stock-8054-20261021`. Полный список файлов и ссылки собраны в [06_SOURCE_CATALOG.md](06_SOURCE_CATALOG.md).

**Запрещённый вывод:** один HDMI, один SoC или одинаковая строка `mt5873` не создают безопасного допуска к кросс-прошивке.

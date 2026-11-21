# Независимый технический handoff: FlixTone / Everycom RUIOS 8052–8054 и TvSettingsPlus MOD v41

**Дата среза:** 21 ноября 2026 года. **Язык:** русский. **Статус:** инженерное собрание доказательств и полное пользовательское ТЗ **для независимой перепроверки другим ИИ**; это не подтверждённая инструкция по прошивке или автоматической установке.

## Что находится внутри

| Файл | Содержание |
|---|---|
| [01_FIRMWARE_REPOSITORIES_AND_RELEASES.md](01_FIRMWARE_REPOSITORIES_AND_RELEASES.md) | Все 3 GitHub-репозитория; заводские PKG 1/2 HDMI, RUIOS OTA 8052/8053, дамп FlixTone 8054; версии, размеры и SHA-256, физические board профили |
| [02_CUSTOM_FIRMWARE_FEASIBILITY.md](02_CUSTOM_FIRMWARE_FEASIBILITY.md) | PKG AES extraction; Android A/B OTA; AVB RSA4096; MBOOT/OP-TEE и trust; Custom v0 NONFLASHABLE overlays, FLauncher, boot visuals, полный ROM/rollback verdict |
| [03_TVSETTINGSPLUS_MOD_TECHNICAL_STATE.md](03_TVSETTINGSPLUS_MOD_TECHNICAL_STATE.md) | Реальный Android TV Settings APK, оригинальная подпись, 8-раздельное дерево v41, 96 функций, история v2–v41, PR #2–#5, native PIC04 route, shortcomings |
| [04_PRODUCT_REQUIREMENTS_AND_ACCEPTANCE.md](04_PRODUCT_REQUIREMENTS_AND_ACCEPTANCE.md) | **Полное ТЗ заказчика**: перенос всех пользовательских настроек KuSettings в неизменённый по дизайну v41, требования по каждому разделу, приёмка, KuSettings M1/M2/M3 |
| [05_EVIDENCE_OPEN_QUESTIONS_AND_REVIEW_PLAN.md](05_EVIDENCE_OPEN_QUESTIONS_AND_REVIEW_PLAN.md) | Доказано/не доказано, errata исторических выводов, конфликтующие документы, вопросы для независимой проверки, необходимые безопасные user tests |
| [06_SOURCE_CATALOG.md](06_SOURCE_CATALOG.md) | **291 ссылка на реальные Git-файлы** исследований firmware, AVB, Custom v0, MOD v41, exact native и FlixTone auditing, с commit-pinned URLs |
| [07_INDEPENDENT_AI_REVIEW_TASK.md](07_INDEPENDENT_AI_REVIEW_TASK.md) | **Готовое техническое задание другому ИИ**: прочитать доказательства, опровергнуть ошибки, дать новые выводы и план |
| [evidence_manifest.json](evidence_manifest.json) | Машинно-читаемые версии/хеши ключевых релизов и PR; не эквивалент повторно посчитанным hash |
| [`FINAL_FUNCTION_PARITY_8054.csv`](https://github.com/MAX-TAC/Projector-Soft/blob/audit/8054-tvsettingsplus-20261117/research/8054/FINAL_FUNCTION_PARITY_8054.csv) | Канонический полный реестр 96 функций в исходном проекте; в офлайн ZIP вложен отдельно |
| [`USER_SETTINGS_MIGRATION_TRACKER.md`](https://github.com/MAX-TAC/Projector-Soft/blob/feature/8054-user-settings-migration-20261120/research/8054/USER_SETTINGS_MIGRATION_TRACKER.md) | Живой трекер перехода на пользовательские MOD обработчики; в офлайн ZIP вложен отдельно |

## С чего начинать другому ИИ

1. Прочитать **07_INDEPENDENT_AI_REVIEW_TASK.md** для корректной постановки задачи.
2. Затем **01 → 02 → 03 → 04 → 05**; все актуальные выводы и ограничения изложены здесь, а не только во внешних ссылках.
3. Для каждого спорного claim использовать **06_SOURCE_CATALOG.md** и реально открыть соответствующие GitHub source, PR patch, Action job logs, firmware/manifests.
4. Сверить `FINAL_FUNCTION_PARITY_8054.csv` (96 записей) и `USER_SETTINGS_MIGRATION_TRACKER.md` с **текущими** GitHub исходниками.
5. Выдать **собственное** заключение, список опровержений, реалистичную методику переноса и минимальные адресные запросы ADB с отдельным разрешением владельца.

## Главная цель проекта

**Существующий `com.android.tv.settings.MainSettings` / TvSettingsPlus MOD v41** уже имеет принятую структуру и внешний вид. Нужно подключить к его пунктам/ползункам/переключателям **все пользовательские OEM функции проектора**, которых сейчас нет или которые не работают; не открывать никакие китайские `com.my.settings` Activity/Fragment/Controller. **Не** дублировать стандартные Android Wi-Fi/BT/apps/about/date/time. MediaTek/TvServices/KuSourceRJ/Focus/Keystone/thermal остаются штатным backend. Затем, в отдельном независимом этапе, перенести нужные background receiver/services и добиться безопасного отключения KuSettings. Это не требует создания нового UX с нуля и **не** тождественно требованию немедленно создать полный кастомный flashable ROM.

**Важно:** для этого пакета не устанавливалось ПО на проектор, не делались новые ADB записи/flash/OTA и не отключались OEM services. Полный функциональный статус MOD и безопасность отключения KuSettings **не подтверждены аппаратно**.

## Ключевые первоисточники

| Направление | Репозиторий / вход |
|---|---|
| Заводские PKG + OTA | [Everycom-Cinelux-Firmware Releases](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/releases) |
| Оригинальный живой дамп 8054 | [FlixTone_8054_Backup Release](https://github.com/MAX-TAC/FlixTone_8054_Backup/releases/tag/FlixTone_8054_Backup) |
| Main MOD v41 | [Projector-Soft PR #2](https://github.com/MAX-TAC/Projector-Soft/pull/2) — source `fbb41e92` |
| Native research | [Projector-Soft PR #3](https://github.com/MAX-TAC/Projector-Soft/pull/3) — source `d1854db0` |
| PIC04 prototype | [Projector-Soft PR #4](https://github.com/MAX-TAC/Projector-Soft/pull/4) — source `4c3f68e5` |
| New user migration branch | [Projector-Soft PR #5](https://github.com/MAX-TAC/Projector-Soft/pull/5) — source `289ef297` |

## Факт и статус на дату среза

- **Пять конкретных firmware release assets**, структурно три RUIOS SKU 8052, 8053, 8054. **Не все пять совместимы с одним проектором.**
- Заводские PKG расшифрованы, Android EROFS исследован; штатный OTA testkey не снимает отдельный AVB/mboot trust blocker; flashable custom ROM **не создан**.
- В режиме Custom v0 опубликованы **два nonflashable overlay staging ZIP** и подготовлены дополнительные макеты; это не кастомная прошивка.
- **MOD v41** существует как exact-8054 подписанный APK с версией 41 и успешным CI; реальная работа всех пунктов на физическом 8054 ещё не подтверждена.
- **PR #4** — только тестируемая чистая PIC04 policy без Android UI; **PR #5** уже имеет кандидат прямых PIC03–PIC10 D-pad записей и успешный CI, но OEM step/active input/restore/hardware effect неизвестны.
- **KuSettings нельзя отключать сейчас:** доказана фоновая `BootupReceiver → MySetTimeService → KuSourceRJ.TimeService` цепочка, MOD replacement ещё disabled/unqualified.

## Статус документа и границы «полноты»

Это **полный тематический handoff** с исходными отчетами, архитектурой, требованиями, релизами, ошибками и source-index, **а не побайтовый архив** всех прошивок (в сумме несколько гигабайт) или всей старой переписки. Оригинальные firmware assets, большие Action artifacts и приложения останутся по ссылкам на GitHub; в ZIP находятся Markdown, машиночитаемые реестры и отобранные source docs, не закрытые бинарники, персональные идентификаторы или калибровки.

Исторические материалы могут противоречить друг другу после новых фактов; задача внешнего ИИ — это найти и рассудить по первичным данным. Документ **не обещает**, что все 291 файлов доступны вне GitHub при недоступной сети.

**Первый проверенный аппаратный шаг** — только отдельно разрешённый владельцем OEM STOCK один D-pad contrast step и ручной возврат. В этом handoff никаких write-команд по проектору нет.

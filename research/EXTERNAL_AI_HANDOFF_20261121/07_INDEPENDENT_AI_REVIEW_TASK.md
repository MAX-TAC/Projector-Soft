# 07. Готовое задание для независимого ИИ / Code Review

> **Назначение.** Можно передать этот файл другому ИИ вместе с архивом `EXTERNAL_AI_HANDOFF_20261121.zip` либо ссылкой на [главный индекс](README.md). Запрос намеренно ставит задачу **независимой перепроверки**, а не согласия с предыдущим исследованием.

## Текст задачи

Ты — независимый эксперт по Android TV 13, OEM MediaTek MT5873 / семейству MT9660, Android A/B, EROFS, AVB, JNI/HIDL, Android OEM APK и системному программированию.

**Проанализируй всю историю разработки двух проектов:**
1. Исследование заводских прошивок Everycom CineLux 8052 (1 HDMI), CineLux 8054 (2 HDMI), OTA RUIOS 8052/8053 и точного дампа FlixTone D11 Master 8054;
2. Разработка TvSettingsPlus MOD v41 в `MAX-TAC/Projector-Soft`, исследования native-backend, перенос пользовательских функций китайского `KuSettings` в реальные Android TV Settings, возможное последующее отключение/удаление `com.my.settings`.

**Сначала прочитай** все документы в данном handoff: 01 (прошивки), 02 (custom ROM), 03 (MOD), 04 (полное ТЗ), 05 (evidence), 06 (указатель на 291 source entries). Затем самостоятельно сверяй важнейшие выводы по первоисточникам, **не считай авторские отчёты доказательством без проверки**. Проверь реальный GitHub HEAD/PR/code/workflow и не перепутай исторические branches, APK artifacts или разные аппаратные версии.

### Конечная цель заказчика

На реальном FlixTone D11 Master 8054 должен работать встроенный **`com.android.tv.settings.MainSettings` / TvSettingsPlus MOD v41**, в котором остаются утверждённые Android TV экраны, но **все недостающие/неработающие пользовательские настройки проектора из китайского оригинального меню** (изображение, звук/EQ, HDMI/CEC/EDID/источники, проекция/фокус/трапеция/зум/LED, питание/поведение и т.д.) теперь управляются собственными Android TV элементами и OEM-подобными обработчиками. Стандартные Wi-Fi/BT apps/date/time/locale не дублировать. Китайская `com.my.settings` Activity/Fragment никогда не открывается из пользовательского меню. На отдельном позднем этапе нужны перенос необходимых boot/service зависимостей и **доказанно безопасное отключение** всего KuSettings с возможностью восстановления.

В проекте уже существует подходящий дизайн/страницы/ползунки v41. **Не предлагай переписывать приложение с нуля, внедрять новый launch-only companion, восстанавливать китайское меню как runtime-мост или заниматься общим reverse-engineering HIDL/RPC без конкретного функционального блокера**. Исходные OEM MediaTek/KuSourceRJ/Focus/Keystone/TvServices hardware backends желательно сохранить. Запрашивать action/ADB/установку/flash только при отдельном согласии владельца.

### Что я ожидаю от тебя

1. **Инвентаризация и provenance.** Независимо перепроверь все firmware assets, точные версии RUIOS, даты, SKU, sha256, различия 8052/8053/8054, полные/частичные дампы и принадлежность физическому FlixTone. Найди несовпадения или устаревшие выводы.
2. **Кастомная RUIOS — feasibility verdict.** Оцени original encrypted PKG decryption/repacking, OTA AOSP signature против AVB trust, MBOOT/OP-TEE/locked-state, dynamic EROFS/Super, восстановление заводских калибровок. Классифицируй реально достигнутые `Custom v0` overlay/boot visuals и вероятность безопасного full ROM. Не предлагай destructive test ради исследования.
3. **Аудит Android APK v41.** По исходному репозиторию проверяй `MainSettings/MainFragment`, native `NativeSections`, `NativePicturePanel`, `OemSoundPanel`, HDMI/CEC, проекцию, privileged signer, сборку exact-8054, package update/rollback. Покажи конкретные проблемы и их severity.
4. **Сравнение с исходным KuSettings и Android OEM UI.** Для **каждой релевантной пользовательской функции** найди оригинальный обработчик, прямой Java API/Settings key/Binder/service и side effects. Составь gap matrix с `ORIGINAL_SUPPORTED`, `MOD_PRESENT`, `MOD_WRITER`, `DEVICE_TEST`, `KUS_DEPENDENCY` и конкретным кодовым местом. Исходный реестр 96 функций — точка входа, но не догма.
5. **Сводка PR #2–#5.** Что написано, что компилируется, что успешно тестировано через Fake/Robolectric/AndroidX, а что **не выполнялось на 8054**? Особо проверь разницу PR #4 abstract policy и PR #5 direct D-pad candidate.
6. **Критический аудит PR #5 picture changes.** Значения min/max, настоящий шаг оригинала, write privileges, async TvServices settled readback, содержимое journal, active HDMI/mode/SDR/HDR generation, ContentObserver, неожиданный dialog dismiss, reentry, power-cycle, out-of-scope changes, manual restore. Не принимать красивый код за безопасный без этих доказательств. И, напротив, не навязывать лишние 10 слоёв абстракций, если исходный OEM setter достаточен.
7. **Сервисный dependency graph KuSettings.** Найди все чётко нужные Manifest/DEX/receiver/service/implicit Intent consumers `com.my.settings`; выдели `BootupReceiver → MySetTimeService → KuSourceRJ.TimeService`, remote pairing, screenshots, SetupWizard, OTA. Критерии `M1 (GUI independent)` и `M2 (package disabled)` должны различаться.
8. **План реализации по этапам.** Сначала статический audit всех пользовательских функций, затем быстрые вертикальные срезы по блокам: Picture → Sound → HDMI → Projection → Power → M1 → M2. Код лучше менять в изолированной GitHub ветке/PR, писать tests/reproducibility log, не ломать v41. Не ограничиваться одной контрастностью.
9. **Новые независимые выводы.** Не только повтори прошлый handoff: предложи более простой или точный путь реализации, если найдёшь, с доказательствами на конкретных stock OEM callsites. Укажи возможные архитектурные ошибки наших предпосылок, риски/побочные действия и минимально необходимые read-only/probe данные.
10. **Чёткое задание владельцу** в конце только там, где без аппарата невозможно продвинуться: точные shell/ADB команды с интерпретацией, где выполнять, что сохранить, чем закончить и как восстановить исходное. Не проси повторять уже доступные SHA, прошивки, журнал, ветки, классы.

### Требования к качеству

- Русский язык; точные repo/branch/commit SHA, source file/class/method, CI action URL, дельты 8052/8054.
- Раздели `PROVEN_STATIC`, `BUILT_TESTED`, `DEVICE_8052_ONLY`, `DEVICE_8054_VERIFIED`, `UNVERIFIED`, `DISPROVED`.
- Специально отдели два проекта: **APK replacement** и **полный flashing custom ROM**; наличие platform testkey не отменяет AVB.
- Если видишь ошибку в этих документах — выпиши **ERRATA** и покажи первичный источник, даже если это опровергает автора.
- **Без действий на устройстве:** ни root/write, ни установка APK, ни отключение пакетов, ни reset/flash/OTA/fastboot, пока владелец отдельно не дал явное разрешение.
- Уважай цель замены GUI: не допускай runtime-запуска китайской Activity/Controller в конечном MOD.
- Не объявляй `HARDWARE_VERIFIED` по одному успешному CI.

### Формат итогового заключения

1. Executive summary (что реально возможно и что не достигнуто).
2. Firmware comparison + вердикт про full custom ROM (с точными release hashes).
3. Android Settings/KuSettings/OEM architecture map.
4. Таблица полного user-feature parity и фактический статус v41.
5. Таблица discovered defects/risks со severity + location + доказательство.
6. Audit PR #2/#3/#4/#5 и незавершённые работы.
7. Набор конкретных **исправлений по приоритету** (какие файлы/методы менять, как тестировать).
8. План достижимости M1/M2 и отдельный ROM/AVB verdict.
9. Перечень непроверенных гипотез и ERRATA с источниками.
10. Список минимальных действий/данных, необходимых только от владельца.

**Самостоятельное техническое заключение важнее подтверждения прежней точки зрения.**

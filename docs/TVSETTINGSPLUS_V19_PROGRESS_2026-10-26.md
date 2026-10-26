# TvSettingsPlus MOD v19 — восстановление проекта и защита OEM термоконтроля

**Дата:** 26 октября 2026. **База:** заводской `TvSettingsPlus.apk` RUIOS **8052**, MT9660, Android 13. **Статус:** APK собрана/подписана CI, **без реальных испытаний на проекторе**.

- **Подтверждённая сборка:** [GitHub Actions 40375223763](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40375223763) — success.
- **APK и build report:** [v19 GitHub Actions artifact 12674936670](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40375223763/artifacts/12674936670) (срок хранения GitHub Actions ограничен).
- **Проверка pure JVM** при той же сборке: `KuSourceRecoveryActivationPolicyTest`, `KuSourceRecoveryPolicyTest` — success.

## Что нового в v19 по сравнению с v18

### 1. Устранена возможность конкуренции с KuSettings при ошибке видимости пакета

На Android 11+ `PackageManager.getApplicationInfo("com.my.settings")` может не вернуть приложение из-за правил package visibility, даже если оно реально установлено. В v18 отсутствие приложения трактовалось как причина включения нового thermal watchdog — потенциально **опасный ложный положительный результат**.

В v19 `KuSourceRecoveryBootReceiver.needsReplacement` использует только `PackageManager.getApplicationEnabledSetting("com.my.settings")` и отдельную тестируемую `KuSourceRecoveryActivationPolicy`.

- **Разрешение на попытку восстановления:** только `COMPONENT_ENABLED_STATE_DISABLED` (2) или `DISABLED_USER` (3).
- **Строгий отказ:** `DEFAULT` (0), `ENABLED` (1), `DISABLED_UNTIL_USED` (4), неизвестное значение и любая ошибка PackageManager.
- Код не удаляет и **не отключает** пакет KuSettings. Он лишь реагирует на подтверждённую внешнюю команду администрирования, **после** перезагрузки.

### 2. Исправлена недостоверная оценка списка Android-сервисов

В v18 `KuSourceRecoveryService` считал снимок сервисов пригодным, если система показывает **какой-либо чужой** сервис. Это не доказывает, что `com.coolux.source` вообще виден процессу TvSettingsPlus.

В v19:

- если точно запущен `com.coolux.source.service.TimeService` — **ничего не перезапускается**;
- если видна **другая служба из того же `com.coolux.source`**, но `TimeService` не найден, допускается ограниченная по времени попытка восстановления;
- если Android не показывает ни одной службы пакета `com.coolux.source`, монитор **не пытается угадать отсутствие TimeService** и не выполняет перезапуск;
- наличие службы случайного стороннего пакета больше не считается надёжным подтверждением.

Это всё ещё **не является доказанным механизмом контроля тепловой защиты**: Android 13 может частично скрывать список чужих сервисов, а фоновый запуск чужого компонента может быть заблокирован. Следовательно, KuSettings **нельзя отключать** только на основании этой логики.

### 3. Добавлено read-only состояние в стандартных настройках

В `Система · Проектор` добавлен пункт **«Контроль охлаждения (конфигурация)»**. Он различает подтверждённое отключение KuSettings, отсутствие такого отключения и недоступность информации. Пункт **не утверждает, что вентиляторы исправны или TimeService запущен** и не содержит кнопок опасного переключения.

### 4. Новые тесты и автоматическая сборка

Новый тест проверяет все состояния PackageManager (0/1/2/3/4, ошибочные и неизвестные). В CI сборке отдельно запускаются JVM-тесты watchdog перед декомпиляцией и обратной упаковкой OEM DEX.

Сохранены проверки original platform test certificate, AXML launcher и 9 legacy settings Intent aliases, декомпиляция/сборка smali, Android 13 D8, `apksigner verify`, сравнение `resources.arsc`, JNI и неизменённых DEX с оригинальным APK. **Тесты — статические и JVM; не заменяют аппаратную проверку.**

## Ранее реализованные функции, сохранённые в v19

- Android TV ярлык «Настройки», открывающий именно `com.android.tv.settings.MainSettings`, со штатной шестерёнкой и TV-баннером.
- Пять переходов из стандартных Settings в исходные OEM страницы того же TvSettingsPlus.
- Категории Android TV «Изображение», «Звук», «Проекция», «Источники» и «Система».
- Изображение: заводские режимы, температура цвета, ползунки яркости изображения/контраста/насыщенности/оттенка/резкости, RGB.
- Аудио: исходные режимы, Bass/Treble и **пять аппаратно обусловленных полос OEM эквалайзера 120/500/1500/5000/10000 Гц**.
- HDR/MEMC только при наличии заводских настроек; установка проектора; EDID и HDMI hotplug autoplay; выбор источника при включении с реальной картой HDMI-портов; аппаратно ограниченная интеграция CEC.
- Яркость **источника света** с учётом `A_RJ_CUSTOMER_ID` и fallback OEM; скрытие пункта Bluetooth-колонки без удаления BT-служб.
- Полноэкранные калибровочные изображения фокуса/трапеции, контроль температуры, заводские службы и OTA **не изменялись**.

## Что не позволяет объявить готовый финальный APK для владельца

1. **Ни один из MOD APK не проверен на настоящем проекторе**: AndroidX UI, remote keys, thermal/time tick, recovery после смерти TimeService, HDMI/CEC/EDID, EQ DSP, запуск, OTA, rollback.
2. **Все подписанные v2–v19** основаны на оригинальном TvSettingsPlus RUIOS **8052**. Нет отдельно собранной/проверенной **8054**-версии под аппарат с двумя HDMI и ToF; **не устанавливать 8052 APK на 8054**.
3. Прямые системные ссылки на `com.my.settings` внутри `settingApps`, `services.jar`, OEM launcher и WFDSinkTest не переназначены. Они не исчезают от добавления 9 `intent-filter` в TvSettingsPlus.
4. Для HDMI eARC, Game Mode (включая `resetTrapezoid()`), некоторых настроек питания и CEC с обязательным подтверждением доступны оригинальные OEM обработчики внутри APK, но это пока **не полностью нативные AOSP контролы**.
5. Новый recovery monitoring не доказан функционально эквивалентным оригинальному `KuSettings.MySetTimeService`, потому что `ActivityManager.getRunningServices` и ограничения Android 13 не дают достоверного основания для массового отключения KuSettings.

**Решение:** не отключать `KuSettings`, не распространять v19 неопытному владельцу как финальный релиз и не проверять экспериментальную сборку на 8054 до подготовки совместимого APK и плана отката.

## Изменённые исходники

- [Fail-closed activation policy](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/KuSourceRecoveryActivationPolicy.java)
- [Thermal watchdog boot receiver](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/KuSourceRecoveryBootReceiver.java)
- [Thermal watchdog recovery service](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/KuSourceRecoveryService.java)
- [Read-only OEM System information](../app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/OemSystemInfoHook.java)
- [Activation policy tests](../app/tvsettingsplus-mod/src/test/java/dev/projectorsoft/tvsettings/KuSourceRecoveryActivationPolicyTest.java)
- [v19 build pipeline](../.github/workflows/build-tvsettingsplus-inline-v19.yml)
- [Исходная зависимость](KUSETTINGS_RETIREMENT.md)

# TvSettingsPlus MOD v33 — проверка маршрутов KuSettings

**Программный этап на базе заводского TvSettingsPlus 8052, без адаптации под 8054.**

После переноса девяти KuSettings-совместимых intent-alias **не следует считать**, что все системные вызовы автоматически идут в MOD: до отключения KuSettings два приложения могут объявлять один и тот же action.

Добавлена реальная проверка в Android `PackageManager`, результат открывается внутри `Об устройстве · Проектор → Диагностика совместимости MOD`.

Для каждого из девяти старых действий (`SETTINGS`, `WIFI_SETTINGS`, `BLUETOOTH_SETTINGS`, `AUTO_BLUETOOTH`, `LAUGUAGE_SETTINGS`, `LAUGUAGE_LOCAL_SETTINGS`, `DATETIME_SETTINGS`, `ALLAPPS`, `PROJECTABOUT_SETTINGS`) проверяется:

- Видит ли Android обработчик в `com.android.tv.settings`;
- Видит ли Android конкурирующий обработчик в `com.my.settings`;
- Какой пакет возвращает `resolveActivity()` в этой версии прошивки.

В результате различаются **MOD выбран по умолчанию**, **KuSettings выбран по умолчанию**, **несколько обработчиков**, **MOD не обнаружен** и **неизвестно**. Число только положительно подтверждённых переходов указано в отчёте.

Диагностика **не меняет preferred activity**, не перехватывает явные вызовы `ComponentName(com.my.settings, ...)` и не отключает OEM-пакет. Системный `xtzm_setting.txt.settingApps` остаётся отдельным вопросом.

## Программные проверки

- `OemLegacyIntentPolicyTest` доказывает fail-closed классификацию.
- В workflow v33 AST-проверка сопоставляет девять action с исходным патчером бинарного AndroidManifest: нельзя случайно тестировать действия, которые приложение не объявляет.
- В CI остались все проверки v31/v32: отсутствие сна/заставки, Bluetooth HID, наблюдение за TimeService, целостность OEM ресурсов, D8, versionCode, platform signature и сборка APK.

**Состояние:** до успешного завершения GitHub Actions v33 статическое код-ревью не равнозначно успешной сборке. `KuSettings` оставлять включённым.

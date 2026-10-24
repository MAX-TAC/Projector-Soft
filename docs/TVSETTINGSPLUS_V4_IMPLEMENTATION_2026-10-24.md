# TvSettingsPlus MOD v4 — интеграция OEM-разделов в Android TV Settings

**24 октября 2026.** Экспериментальная прошивочная база RUIOS **8052**, Android 13.

## Что действительно изменилось в подписанном APK

[Успешная сборка v4 (GitHub Actions)](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40111124603) · [APK-артефакт](https://github.com/MAX-TAC/Projector-Soft/actions/runs/40111124603/artifacts/12565973499).

В отличие от v3, v4 модифицирует исходный `MainFragment` стандартного Android TV Settings и добавляет на главный экран **5 работающих маршрутов к уже имеющимся OEM-разделам**:

| Новый пункт главного меню | Что открывается |
|---|---|
| Проекция | `uicustom.MainActivity`, OEM вкладка 0, автофокус, коррекция трапеции, установка |
| Источники | `uicustom.MainActivity`, OEM вкладка 3, HDMI, USB, EDID, CEC, источник при запуске |
| Изображение проектора | `uicustom.MainActivity`, OEM вкладка 4, параметры изображения и цвет |
| Звук проектора | `uicustom.MainActivity`, OEM вкладка 5, звук, режимы, bass/treble, eARC |
| Дополнительно: проектор | `uicustom.MainActivity`, OEM вкладка 6, яркость источника света, язык, питание |

Новые Preference добавляются динамически через `OemRootMenuHook` в стандартную AOSP PreferenceCategory (`general_settings`) и при повторной инициализации не дублируются. Для «Источников» и «Общих» `OemLaunchRouteHook` запускает существующие вкладки OEM Activity; для изображения/звука/проекции используются штатные действия, уже понимаемые OEM Activity.

**Техническая оговорка:** это рабочий на уровне структуры APK мост навигации, а **не окончательное встраивание OEM контента внутрь правой панели AOSP**. Клик открывает существующую OEM Activity того же пакета. Это промежуточное решение, чтобы добиться доступа ко всем аппаратным разделам из стандартного Android TV Settings, не переделывая неиспытанные HAL/сервисы.

## Какие заводские компоненты изменены

Проверка CI подтвердила:

- Обновлены **6 smali-классов**, содержащиеся только в `classes13.dex` и `classes14.dex`:
  - `MainFragment` (новое главное меню Android TV);
  - `uicustom.MainActivity` (открытие нужного OEM раздела);
  - `BrightnessModeSettingsSecondaryController` (регулировка подсветки в правой панели для customer 0 при доступном OEM API);
  - `GeneralSubMenuController$5` (раздел яркости);
  - `SourceSubMenuController` и `SourceFragment` (скрытие пункта BT-колонки).
- Добавлен `classes17.dex` с нашим Java-модулем; из 16 исходных DEX только два заменены.
- Все остальные оригинальные DEX, `resources.arsc`, JNI, картинки, ресурсы и прочие файлы сохраняются побайтово (за исключением `AndroidManifest.xml` и подписи).
- `com.android.tv.settings`, `versionCode=4`; девять legacy Intent aliases.
- APK подписан тем же AOSP platform test сертификатом, что оригинал: `c8a2e9bccf597c2fb6dc66bee293fc13f2fc47ec77bc6b2b0d52c11f51192ab8`.
- Все шаги CI завершились успешно: компиляция Java, сборка smali/DEX, `aapt`, `apksigner verify`, проверка неизменности нецелевых файлов.

## Что не выполняли и не можем доказать из OTA

- Не запускали APK на проекторе: установка, DEX-verification на OEM ART, работа с пультом и фактическое открытие новых пунктов **не подтверждены**.
- Не переопределяли `settingApps` или запуск `KuSettings` из внешнего OEM лаунчера; само старое приложение **всё ещё необходимо оставить**.
- Не изменяли исходные процессы фокусировки, автофокуса, ручной/автоматической трапеции и калибровочные изображения. Они должны оставаться полноэкранными.
- Не создавали фантазийный 5-полосный эквалайзер: в существующих контроллерах найдены бас/высокие частоты; нет подтверждённого OEM API 5 полос.
- Не подтверждали аппаратные функции для 8054: новая APK собрана **только для 8052**.
- Основная незавершённая цель — настоящая **единая двухпанельная навигация** без переключения в OEM Activity. Для неё потребуется адаптация OEM контроллеров к `PreferenceFragmentCompat`/Leanback и проверка нужных API.

## Следующий тест с физическим устройством

После получения ранее запрошенного ADB-архива проверяем фактическую ревизию устройства, `A_RJ_CUSTOMER_ID`, маршрутизацию `android.settings.SETTINGS`, доступность `BrightReceiver`, состояние `KuSourceRJ.TimeService`. Только после подготовки безопасного отката можно тестировать установку модифицированного системного APK. **Не отключать `KuSettings` на основании одной успешной CI-сборки.**

## Изменённые файлы разработки

- `app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/OemRootMenuHook.java`
- `app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/OemLaunchRouteHook.java`
- `tools/patch_aosp_root_menu_smali.py`
- `.github/workflows/build-tvsettingsplus-oem-root-v4.yml`
- `tools/compose_brightness_v3_apk.py` — добавлен выбор версий 3/4.

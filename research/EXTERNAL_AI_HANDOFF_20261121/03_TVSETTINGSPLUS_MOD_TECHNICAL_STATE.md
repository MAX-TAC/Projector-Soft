# 03. TvSettingsPlus MOD v41: архитектура, исходники, PR, доказанный прогресс и текущие дефекты

**Срез:** 2026-11-21. **Целевой аппарат:** FlixTone D11 Master / Everycom CineLux, exact SKU `ro.rjpj.machine.id=8054` (две HDMI), Android 13/API 33. 8052 — исходник для сравнений, не окончательный runtime reference.

## 1. Цель приложения, которую нельзя подменять проектом другого типа

**Нужно изменить настоящий `com.android.tv.settings`, сохранить настоящий Android TV `MainSettings/MainFragment`** и сделать собственные элементы OEM управления. Не создавать отдельное чужое приложение с имитацией главного Settings, не открывать китайский `com.my.settings` как реализацию одной из настроек.

```text
Кнопка SETTINGS → реальный com.android.tv.settings.MainSettings
  → existing MOD native sections and Android platform screens
  → own MOD preference / dialog / D-pad handler
  → Android Settings.Global/System, штатные Java OEM/TvServices/сервисы
  → MediaTek/JNI/HAL/другие сохранённые OEM службы
  → физическая настройка
```

**Два разных старых интерфейса:** `com.my.settings` — отдельный KuSettings; `com.android.tv.settings.uicustom` / `partnercustomizer` — OEM расширения самого TvSettingsPlus APK. В конечном UX пользоваться старым китайским GUI нельзя, но нижние независимые API и аппаратные службы следует сохранить. Это подтверждённое архитектурное решение, [исходный контракт](https://github.com/MAX-TAC/Projector-Soft/blob/research/8054-mediatek-native-api-20261120/research/8054/native/RUNTIME_ARCHITECTURE_NO_CHINESE_UI.md).

## 2. Исходный пакет, компоновка и механизм сборки

- **Stock APK:** `system_ext_a/priv-app/TvSettingsPlus/TvSettingsPlus.apk`, SHA-256 `f8e0c6fe0f46e7eb8db651c31d85ef8b32a769b37e4dd0f96ed19a64725d74ee`.
- **Package:** `com.android.tv.settings`, системный privileged APK. Android 13, SDK33. Проверенный stock signing certificate SHA-256 `c8a2e9bccf597c2fb6dc66bee293fc13f2fc47ec77bc6b2b0d52c11f51192ab8` — AOSP platform testkey.
- **MOD source:** `app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/` (общие адаптеры/политики); `src/exact8054/java/.../` (нативные страницы/устройство); `ui-tests/` (реальный AndroidX Leanback host + Robolectric), `tools/` (patch/repack/audit).
- **Real APK process:** exact-8054 archive SHA gate → оригинальный TvSettingsPlus / framework-res → AndroidX Java compile и D8 → ограниченный patch исходного OEM Manifest/DEX/resources → repack/apksigner оригинальным совместимым cert identity → exact diff/manifest/signature checks. Успех сборки не гарантирует PackageManager runtime acceptance или аппаратную работу.
- В отчётах v39–v41 указаны **15 оригинальных неизменённых DEX**, с изменениями главным образом `classes13.dex`; не заявлять, что текущая отдельная branch автоматически сохранила этот property без нового exact build.
- Происходивший рост versionCode v33–v41 относится к экспериментальным APK-кандидатам, а **не** к установке на физический проектор.

[Исходная компоновка и changelog](https://github.com/MAX-TAC/Projector-Soft/blob/audit/8054-tvsettingsplus-20261117/docs/8054_RELEASE_CHANGELOG.md) · [точная инструкция сборки](https://github.com/MAX-TAC/Projector-Soft/blob/audit/8054-tvsettingsplus-20261117/.github/workflows/build-tvsettingsplus-8054-audit.yml) · [интеграционный аудит](https://github.com/MAX-TAC/Projector-Soft/blob/audit/8054-tvsettingsplus-20261117/research/8054/INTEGRATION_AUDIT_8054.md).

## 3. Существующее дерево v41 (уже согласованное; не переделывать без необходимости)

| MainSettings v41 | Состав и назначение |
|---|---|
| 1. Wi-Fi и сеть | Android `NetworkActivity/NetworkFragment`, Ethernet/Wi-Fi без дублирования OEM |
| 2. Bluetooth | Android платформа/сопряжение, информация и безопасные варианты управления |
| 3. Источники | Android TV/HDMI1/HDMI2, статусы, boot source/app, CEC Auto Off, autoplay, EDID readback |
| 4. Проекция | Яркость LED (4 OEM режима), автофокус, авто-трапеция, оригинальные 4-corner/HV/zoom редакторы до переноса logic |
| 5. Изображение | Picture mode/gamma, яркость/контраст/насыщенность/резкость/hue/RGB, цветовая температура, HDR/MEMC, нижний OEM-дизайн ползунков |
| 6. Звук | Профиль, доступные bass/treble/AVC/surround, EQ 5 полос, output, SPDIF/speaker delay, Dolby/DTS/eARC при наличии, системные звуки |
| 7. Приложения | Штатный Android TV `AppsActivity` |
| 8. Настройки устройства | About, hardware, power, language/time, font scale, original platform settings, реальная кнопка Settings |

Вторичный OEM функционал не должен возвращать нежелательные каналы/tuner, спутник, OLED, Retail/demo, factory-only панели или дублирующие Android пункты. UI уже в основном готов: нужно обеспечить **реальный setter и readback для каждого нужного пользовательского контрола**, а не переписывать оформление.

[Дерево v41](https://github.com/MAX-TAC/Projector-Soft/blob/audit/8054-tvsettingsplus-20261117/docs/8054_FINAL_MENU_SPEC.md) · [MainSettings integration](https://github.com/MAX-TAC/Projector-Soft/blob/audit/8054-tvsettingsplus-20261117/docs/8054_MAINSETTINGS_INTEGRATION.md).

## 4. Состояние 96 функций

Единый реестр: [`FINAL_FUNCTION_PARITY_8054.csv`](https://github.com/MAX-TAC/Projector-Soft/blob/audit/8054-tvsettingsplus-20261117/research/8054/FINAL_FUNCTION_PARITY_8054.csv).

Зафиксированный статус **на базовой v41 PR #2**:
- `DIRECT_MOD`: **22**, `DELEGATED_OEM`: **23**, `READ_ONLY`: **21**;
- `NOT_IMPLEMENTED`: **17**, `BLOCKED`: **11**, `HIDDEN`: **2**;
- 22 + 23 + 21 + 17 + 11 + 2 = **96**.
- **Все** перечисленные аппаратные сценарии в этой матрице имели `RUNTIME_TEST_REQUIRED`. `DIRECT_MOD` означает **код обработчика**, не физическое подтверждение. `DELEGATED_OEM` означает **ссылку на штатный экран**, а не перенос функции внутрь MOD.
- В числе 96 есть не пользовательские регулировки: Factory, AVB/boot, OTA, demos, platform. **Не нужно переносить опасные Factory операции в обычное меню**.
- Полный последующий трекер с группировкой: [`USER_SETTINGS_MIGRATION_TRACKER.md`](https://github.com/MAX-TAC/Projector-Soft/blob/feature/8054-user-settings-migration-20261120/research/8054/USER_SETTINGS_MIGRATION_TRACKER.md). Он описывает **задания**, не 96 завершённых функций.

## 5. Релизная/проектная история — факт против опыта

| Этап | Что сделано | Что не доказано |
|---|---|---|
| v2–v23 | Подготовлены original OEM menu hooks, JNI/Settings/vendor key analysis, source/sound/HDMI workflows, launcher entry, первые UI prototypes | Реальная универсальная hardware совместимость |
| v24–v33 | Диагностика тепла, EQ, brightness, native power, key tones, boot app, system font, Bluetooth/KuSource dependency, legacy Intents | Безопасность отключения KuSettings, все runtime API |
| v35–v38 | Android 13/Leanback native section host, отложенные неиспытанные recovery компоненты, отдельные native страницы | Полный user-flow/boot и действующий thermal fallback |
| v39 | Реальная интеграция в оригинальный MainSettings/MainFragment, 8-root tree | Поведение на реальном 8054 (вопросы меню/кнопок/фокуса) |
| v40 | По замечаниям v39 скорректированы Bluetooth/native source/CEC/EDID and page updates | Runtime test и физический эффект |
| **v41** | Picture native bottom panel, SOUND EQ, separated audio delay, 8-group MainSettings, exact-8054 signed APK + CI | **PIC01–12 базового v41 преимущественно READ_ONLY; аппаратное подтверждение отсутствует** |

### Верифицированный baseline v41

**v41 release APK**: `versionCode 41`, SHA-256 **`a93af51dda3ec723a1e895c935f7d68c31760b6f8d8169090e5d02b19256037e`**, source commit `a3ca1a2`. [Actions artifact](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44769301913/artifacts/14645503299); [changelog и предыдущие версии](https://github.com/MAX-TAC/Projector-Soft/blob/audit/8054-tvsettingsplus-20261117/docs/8054_RELEASE_CHANGELOG.md). Этот APK называется **BUILT_UNTESTED**, а не hardware verified.

**Важная оговорка:** `PR #2 HEAD fbb41e92...` — развитие базовой ветки, его HEAD может не совпадать с конкретной сборкой `a3ca1a2`. Разделять **APK artifact SHA** и **Git source commit**.

## 6. Текущие Draft PR (включая только что добавленные изменения)

| PR | HEAD на срезе | Смысл | Результат |
|---|---|---|---|
| [#2 — v41 native MOD](https://github.com/MAX-TAC/Projector-Soft/pull/2) | `fbb41e92a47a94a2a4b0f4122100e5a1cb43cfe5` | Исходный mod; стабильная база для изолированных экспериментов | Build/AndroidX tests, реальный аппарат НЕ протестирован |
| [#3 — MediaTek research](https://github.com/MAX-TAC/Projector-Soft/pull/3) | `d1854db0539c522f81f3c2ee355d08338197ae8a` | Настоящий 8054 dump, JNI/HIDL/rpc static evidence, PRODUCT-001 integration contract и code review | **STATIC only**, NATIVE-005b отложен |
| [#4 — Pic04ContrastStep](https://github.com/MAX-TAC/Projector-Soft/pull/4) | `4c3f68e5bda55dda9a38c232df2667898a456d97` | Pure-Java guarded one-step policy с Fake Port | [CI success](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44808298783), **никакого production Port и подключённого ползунка** |
| [#5 — user settings migration](https://github.com/MAX-TAC/Projector-Soft/pull/5) | `289ef297ca9e5f8319ca9b9a0501d000ec258368` | Прямой кандидат для восьми простых picture sliders в `NativePicturePanel`, Robolectric tests, 96-item tracker, CI build workflow | [UI success](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44816235981), [capabilities success](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44816235966), [exact APK CI success](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44816462929) на предыдущем HEAD `050f684...`, **hardware untested** |

**Не смешивать PR #4 и #5**: #4 — самостоятельная fail-closed policy без UI, #5 — интегрированный эксперимент с более слабым профилированием. Они не объединены и не создают «дважды проверенную» реализацию.

## 7. Подтверждённая статическая цепочка Picture/PQ

```text
Original user control: picture_contrast
    → Settings.Global.putInt("picture_contrast", value)
    → TVMenuSettingsService.SettingObserver
    → TVSettingConfig.setConifg("g_video__contrast", value)
    → MtkTvConfigBase.setConfigValue(configId, value)
    → TVNativeWrapper.setConfigValue_native(-1, configId, value, 3)
    → libcom_mediatek_twoworlds_tv_jni.so
    → imported a_mtktvapi_config_set_value (JNI BLX → PLT → GOT)
    → separate mtktvapi service / RPC import
    → hardware: NOT DEVICE-VERIFIED
```

Контрастный UI не должен использовать исходный `PictureModeValueSecondaryController` в runtime; он используется как **образец кода**, аналогичный путь реализуется в MOD.

Доказано по original exact-8054:
- `Settings.Global.picture_contrast` → `g_video__contrast` map;
- JNI `BLX 0x7ae90` → GOT `0x7dff8` оригинальной ARM32 `vendor_a/lib/libcom_mediatek_twoworlds_tv_jni.so`, SHA `312fef3f17f656ac957a32dfee8187798dacac8f4df83e8219b1553b7248ab24`;
- HIDL service separate call edge к RPC setter;
- **не доказаны** semantic `inputGroup=-1`, active HDMI/profile destination, real SDR/HDR effect, atomic rollback, stock D-pad increment.

[Static contract](https://github.com/MAX-TAC/Projector-Soft/blob/research/8054-mediatek-native-api-20261120/research/8054/native/PRODUCT_001_PIC04_INTEGRATION_CONTRACT.md) · [JNI machine trace](https://github.com/MAX-TAC/Projector-Soft/blob/research/8054-mediatek-native-api-20261120/research/8054/native/TRACE_004_CONFIG_JNI_TO_HIDL.md).

## 8. Где функциональность пока не переносится

**Изображение:** режим/температура/HDR/MEMC могут иметь режимные side effects; PIC03–10 кандидаты PR5, не product complete. `picture_mode` и `HDR` stored flags не обязательно идентифицируют реальный видеосигнал. Гамма и RGB не равнозначны заводской Factory WB.

**Звук:** пять полос EQ могут менять OEM User profile при проверке границ/Global+MediaTek; bass/treble, Dolby/DTS, ARC/eARC имеют profile/output-specific ограничения. Исходный общезвуковой reset затрагивает eARC/speaker state, поэтому нельзя добавлять фиктивный «Сброс EQ».

**HDMI:** `TvInputManager.getInputState()` определяет наличие input, **не активный physical HDMI**. EDID stored value и реально согласованный EDID на конкретном порту не идентичны; switching может привести к renegotiation. Game mode может сбрасывать трапецию.

**Проекция:** оригинальный `OemProjectionPanel.projectionIntent()` на 8054 **содержит явный путь к `com.my.settings.activity.ProjectionActivity`**. Это непосредственный **блокер финального требования «ни одного китайского GUI»**; PR #5 этот путь пока не удалил, потому что сначала требуется собственная замена. FourCorner/HV/zoom частично делегированы штатным редакторам. Motor/focus/LED APIs не вызывать вслепую.

**Система:** `KuSettings BootupReceiver → MySetTimeService → KuSourceRJ.TimeService` служит boot/TIME_TICK recovery для связанной с охлаждением службы. `KuSourceRecoveryBootReceiver` и `KuSourceRecoveryService` уже существуют в MOD source, но **disabled/no boot filter, не квалифицированы hardware**; раньше всего пакета `com.my.settings` отключать нельзя.

[Picture](https://github.com/MAX-TAC/Projector-Soft/blob/audit/8054-tvsettingsplus-20261117/research/8054/PICTURE_BLOCK_STATUS_8054.md) · [Sound](https://github.com/MAX-TAC/Projector-Soft/blob/audit/8054-tvsettingsplus-20261117/research/8054/SOUND_BLOCK_STATUS_8054.md) · [HDMI](https://github.com/MAX-TAC/Projector-Soft/blob/audit/8054-tvsettingsplus-20261117/research/8054/HDMI_BLOCK_STATUS_8054.md) · [Projection](https://github.com/MAX-TAC/Projector-Soft/blob/audit/8054-tvsettingsplus-20261117/research/8054/PROJECTION_BLOCK_8054.md) · [KuSettings](https://github.com/MAX-TAC/Projector-Soft/blob/audit/8054-tvsettingsplus-20261117/research/8054/KUSETTINGS_DEPENDENCIES_8054.md).

## 9. Критический независимый review PR #5 — не устанавливать до устранения рисков

`NativePicturePanel.java` в #5 **при нажатии LEFT/RIGHT действительно вызывает `Settings.Global.putInt`** для 8 picture keys; status уже не read-only. Однако:

1. Прирост `+1/-1` является предположением (в PR #4 пример `2` — также только mock), stock step 8054 не измерен.
2. Минимум и максимум берутся из `TVSettingConfig.getMin/getMax`; это реальные API, но не доказано, что они одинаковы для active input/profile и всех picture keys.
3. Journal в #5 сохраняет `key/mode/temperature/HDR` и original value **без идентичности HDMI**. Он **не является безопасной процедурой восстановления**, не автоматический rollback.
4. Обработчик ждёт 2 последовательных Global+ACFG совпадения в 250ms polling, timeout 5 s. Это не эквивалентное доказательство физического результата; возможно асинхронное замещение.
5. UI observer сохраняет окно при ожидаемом ключе, закрывает при другом изменении; зафиксированы только unit/Robolectric cases, не реальные input transition/hotplug.
6. Отсутствует гарантированное независимое active input/SDR/HDR/profile API; device permissions/visible picture effect ещё не подтверждены.
7. Наличие кодового пути к китайской `ProjectionActivity` и других делегированных OEM страниц означает, что полная GUI-миграция ещё не завершена.
8. **Производственный выпуск нельзя считать готовым на основании CI success**. Перед физическими записями нужен проверенный restore path и точный stock measurement.

Это реальные замечания к коду, не причина начинать заново переписывать весь MediaTek. Оптимальное направление — узкий vertical slice + тест, затем перенос **всех** функций группами.

## 10. Источники и технические опорные точки

- [96-function machine-readable](https://github.com/MAX-TAC/Projector-Soft/blob/audit/8054-tvsettingsplus-20261117/research/8054/FINAL_FUNCTION_PARITY_8054.csv)
- [UI tests](https://github.com/MAX-TAC/Projector-Soft/blob/feature/8054-user-settings-migration-20261120/app/tvsettingsplus-mod/ui-tests/src/test/java/dev/projectorsoft/tvsettings/NativePicturePanelEditTest.java)
- [Picture UI source](https://github.com/MAX-TAC/Projector-Soft/blob/feature/8054-user-settings-migration-20261120/app/tvsettingsplus-mod/src/exact8054/java/dev/projectorsoft/tvsettings/NativePicturePanel.java)
- [Source migration log](https://github.com/MAX-TAC/Projector-Soft/blob/feature/8054-user-settings-migration-20261120/research/8054/logs/2026-11-20_USER_SETTINGS_MIGRATION.md)
- [Review PR4](https://github.com/MAX-TAC/Projector-Soft/blob/research/8054-mediatek-native-api-20261120/research/8054/native/PRODUCT_001_PR4_CODE_REVIEW.md)
- [Original exact-8054 firmware](https://github.com/MAX-TAC/FlixTone_8054_Backup/releases/tag/FlixTone_8054_Backup)

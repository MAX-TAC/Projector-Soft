# Проекция 8054: OEM-навигация, не полный перенос

Кодовая оценка после группового CI: BUILT_UNTESTED; аппаратура RUNTIME_TEST_REQUIRED. Никакой OEM-ссылке не присваивается MIGRATED/FEATURE_COMPLETE. Реальные runtime критерии: original MainActivity/Activity включена вместе с приложением; внешний компонент exported; требуемое permission выдано. Не прошедшие проверку shortcuts не создаются. Общий OEM раздел доступен отдельно, но он не доказывает наличие каждого подменю.

## PRJ01–PRJ11

Общий точный entry G: explicit `com.android.tv.settings/com.android.tv.settings.uicustom.MainActivity`, action `android.settings.SETTINGS`; штатный раздел Projector. Существующий OEM маршрут сохраняется. Названия secondary controllers ниже взяты из handoff; они не выдаются за новый прослеженный HAL call.

| ID / назначение и наличие | OEM entry/API | Побочные действия | Стратегия / критерий доступности | RUNTIME_TEST_REQUIRED |
|---|---|---|---|---|
| PRJ01 Front/Rear/Ceiling, штатная установка | G; доказанный TvSettingsPlus ProjectorManager → Factory ApiService → setProjectorMirrorMode(int) | mirror/flip config, persistent property, I²C, display | OEM_DELEGATED через G; legacy MOD selector закрыт8054 | Исходный mode, ориентация, OEM возврат без калибровки |
| PRJ02 автофокус при старте, RuiAutoFocus service есть в Manifest | G → штатные настройки фокуса, FocusSettingsSecondaryController | запуск моторизованной фокусировки/датчиков при старте | OEM_DELEGATED GROUP_ENTRY_ONLY; только если OEM показывает пункт | Наличие toggle и возврат original, cold boot только в разрешённом тесте |
| PRJ03 автофокус при движении, зависимость OEM sensor config | G → штатные настройки фокуса | сенсоры/автофокус/мотор | OEM_DELEGATED GROUP_ENTRY_ONLY | Наличие пункта, исходный state, разрешённый movement test |
| PRJ04 ручной фокус, VAF2S/RuiAutoFocus присутствует | G → OEM focus UI; не запускать VAF2S/фокус broadcast напрямую | движение мотора и предельные положения | OEM_DELEGATED GROUP_ENTRY_ONLY | D-pad/пульт, границы и original focus; без raw motor commands |
| PRJ05 автотрапеция, KeystoneCorrection присутствует | G → KeystoneSettingsSecondaryController | коррекция/датчики, возможное изменение других projection значений | OEM_DELEGATED GROUP_ENTRY_ONLY | Проверить поддержку и original geometry; не reset/calibrate |
| PRJ06 четыре угла, FourCornerActivity есть в Manifest | G → штатный UI; прямой shortcut FourCornerActivity не добавлен | изменение геометрии; fullscreen calibration может иметь дополнительные эффекты | OEM_DELEGATED GROUP_ENTRY_ONLY, не прямой calibration launcher | Сохранить точки/изображение, убедиться в штатном способе возврата |
| PRJ07 H/V трапеция, HVKeystoneActivity есть в Manifest | G → штатный UI; прямой shortcut не добавлен | геометрия/масштаб/взаимодействие с автонастройкой | OEM_DELEGATED GROUP_ENTRY_ONLY | Наличие controls и возврат в original без factory reset |
| PRJ08 obstacle, зависит от OEM ToF/camera feature config | G → KeystoneSettingsSecondaryController | датчики, автоизменение геометрии | OEM_DELEGATED GROUP_ENTRY_ONLY; наличие приложения не доказывает датчик | Проверить доступность пункта/датчика, не включать скрытую функцию |
| PRJ09 auto screen alignment, зависит от OEM config | G → KeystoneSettingsSecondaryController | ToF/camera + автокоррекция | OEM_DELEGATED GROUP_ENTRY_ONLY | Наличие UI/датчика, baseline геометрии и штатный возврат |
| PRJ10 digital zoom, DigitalZoomActivity объявлена | explicit com.rjpj.tcorrection/.DigitalZoomActivity, ACTION_MAIN | пользовательский масштаб видео; диапазон ещё не получен | OEM_DELEGATED; shortcut только при enabled/exported/permission; иначе G | Прочитать реальный min/max/step в original UI, менять один шаг и вернуть; не путать с motor/LED |
| PRJ11 optical LED brightness, BrightActivity/BrightReceiver объявлены | explicit com.coolux.source/.activity.BrightActivity, ACTION_MAIN; original customer-specific brightness UI | LED/PWM/thermal timing, persist.bright.model | OEM_DELEGATED; shortcut только при access checks; MOD receiver gateway apply запрещён8054/unknown, LegacyBrightnessUiHook не подменяет customer route8054 | customer ID, штатные modes, actual brightness/temp и original return в разрешённом тесте |

Доказательства: handoff MANIFEST_COMPONENTS_8054.csv (RuiAutoFocus VAF2S/MainActivity; KeystoneCorrection FourCorner/DigitalZoom/HV; KuSourceRJ BrightActivity/BrightReceiver), SETTINGS_MIGRATION_MATRIX_8054.csv и SELECTED_SMALI_TRACE_FINDINGS_8054.md (PRJ01 exact chain). Manifest declaration не доказывает exported, support, запуск или безопасные side effects — это проверяется отдельными критериями выше. Для digital zoom неизвестный диапазон не заменяется выдуманным native slider.

## Код и проверки

OemProjectionPanel только устанавливает checked explicit Intents; не startService/sendBroadcast/I²C/PWM/property writes. Projection8054PolicyTest проверяет внешний exported/permission, disabled Activity/application, same-package navigation и legacy brightness gate. Оригинальные KuSettings/KuSourceRJ/RuiAutoFocus/KeystoneCorrection/Factory/TvServices/TimeService/fan остаются включёнными. 8052 legacy поведение сохранено, но его hardware safety здесь не переутверждается.

Полная актуальная сводка 96 функций — DELIVERY_STATUS_8054.md / delivery_status_8054.json. DELEGATED_OEM означает только навигацию, HIDDEN означает скрытый прямой MOD control с отдельным fallback, NOT_IMPLEMENTED означает отсутствие реализации MOD, а не отсутствие функции в OEM firmware. source-only/отсутствующая функция не помечается успешно собранной; runtime verified 0.

## Короткий device/rollback checklist (пока не выполнялся)

1. Получить отдельное разрешение и сначала подготовить установочный/recovery пакет из следующего milestone. Сохранить вне приложения исходные projection values, фото геометрии/фокуса/ориентации/zoom/brightness/customer ID, активные OEM службы/температуры. Не считать Settings.Global полным calibration backup.
2. D-pad открыть G, Zoom, Brightness по одному; проверить доступность/отказ permissions и Back в MOD. Не менять значения при проверке навигации без отдельного согласования: OEM Activity сама может иметь side effects.
3. Для согласованной функции проверить original range и изменить один пользовательский шаг; вернуть original через тот же OEM UI и перепроверить геометрию/фокус/температуры. Не обращаться к factory reset/calibration, I²C/PWM или motor API. Не останавливать службы для теста отказа.
4. При отсутствии исходного state/понятного ручного возврата — stop test до изменения функции. UI/Intent eligibility не доказывает физический rollback. Результаты по каждому PRJxx остаются NOT_TESTED до реального теста.

Следующий кодовый milestone — STANDARD_ANDROID система/NAV; без полного reverse низкоуровневых служб. Перед первым device test отдельный обязательный пакет: exact MOD + matching sha256 + original8054 APK + preflight PackageManager/signature/sharedUID/SELinux + recovery/D-pad path при нестарте меню. Ни установки, ни включения/отключения системных пакетов, ни записи Global этот документ не разрешает.

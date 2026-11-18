# KuSettings на 8054 · зависимости и миграция

Инвентарь 8054 фиксирует 16 компонентов пакета `com.my.settings`. Более ранний полный разбор 8052 полезен как указатель на методы. Данные о вызывающей стороне должны подтверждаться для 8054 реальным вызовом или штатным разрешением Intent. [История исходного аудита](../../docs/KUSETTINGS_16_COMPONENT_PARITY.md)

| Узел | Роль и использование MOD | Граница проверки |
|---|---|---|
| SettingActivity, Wi-Fi, LanguageActivity, LanguageLocalActivity, DateTimeActivity, MyZonePicker, AllAppActivity | Платформенные экраны и совместимые implicit маршруты MOD. Явные ComponentName старого пакета разрешаются в оригинальный Activity | Фактические default handlers и пульт требуют теста |
| MyBluetoothActivity, MyAutoBluetoothActivity | OEM pairing остаётся. Совместимые Intent и собственная панель не заменяют полный сценарий OEM автоподключения | Исторически есть ссылки в services.jar и WFDSinkTest; на 8054 литерал без достижимого вызова не считается доказательством |
| ProjectionActivity | `OemProjectionPanel` проверяет штатный implicit handler и использует OEM путь, если он разрешается. Подтверждённый KeystoneCalibrationActivity и другие собственные controls учитываются отдельно | Для FourCorner/DigitalZoom требуются точные вызовы и условия входа |
| BootupReceiver → MySetTimeService → KuSourceRJ.TimeService | Штатный watchdog связан с TIME_TICK и восстановлением TimeService. MOD replacement production gate закрыт на 8054 | Сохранить весь boot/thermal путь. Отсутствие службы в фильтрованном списке не является доказательством остановки |
| ScreenshotActivity и ScreenServices | Оригинальные компоненты вне миграции | Не переносить |
| AndroidX Startup provider и ProfileInstallReceiver | Служебная инфраструктура штатного APK | Отдельный перенос не требуется |
| KuSetupWizard | Отдельный пакет с собственной ролью первоначальной настройки | Без изменений |

Нативный SPDIF использует `PartnerSettingsConfig`, `Settings.Global` и `TvServices` из штатной TV цепочки. Он не использует Activity или сервисы KuSettings. При этом существующие внешние маршруты KuSettings и критические зависимости сохраняются; отключение пакета не оценивается по числу перенесённых пунктов. Handoff перечисляет ссылки на KuSettings в DEX и конфигурациях. Они остаются кандидатами на проверку потребителя, пока нет вызова и точки входа.

Следующие раздельные проверки: активные handlers после запуска из пульта; OEM pairing/reconnect; направление BootupReceiver → MySetTimeService → KuSourceRJ.TimeService и реакция после cold boot; фактический маршрут пользовательских projection Activity без обхода калибровки.

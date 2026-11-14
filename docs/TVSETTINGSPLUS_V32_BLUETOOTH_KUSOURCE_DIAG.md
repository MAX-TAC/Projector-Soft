# TvSettingsPlus MOD v32 — Bluetooth-пульт и диагностика KuSourceRJ

**Стадия:** перенос функций в общем TvSettingsPlus на заводской базе 8052. **Не адаптация 8054.**

## Перенесено в нативный интерфейс

- Пункт `Bluetooth · Проектор → Bluetooth-пульт и сопряжение` показывает ранее сопряжённые HID-устройства с major device class **1280**, как в оригинальном `MyAutoBluetoothActivity`.
- Перечень считывается через системный `BluetoothAdapter.getBondedDevices()`. Не объявляет устройства подключёнными, если они только сопряжены.
- Кнопка `Открыть сопряжение` открывает существующую `com.android.tv.settings.accessories.AddAccessoryActivity`. Никакого собственного сканирования/включения Bluetooth или вызова скрытого `CachedBluetoothDevice.connect(true)`.
- При ограничении `BLUETOOTH_CONNECT` сообщает, что данные недоступны, вместо «нет устройств». KuSettings продолжает выполнять OEM-поиск пульта.
- В `Об устройстве → Диагностика совместимости MOD` добавлено чтение состояний `KuSourceRJ.TimeService` и `KuSettings.MySetTimeService` через `ActivityManager.getRunningServices()` без запуска/остановки служб.
- Снимок служб может быть неполным на Android 13; только обнаружение точной службы даёт положительный статус. Отсутствие в снимке не считается доказательством остановки.

## Тестирование

- `OemBluetoothRemotePolicyTest`: OEM HID class, неподходящие категории, очистка имён, ограничение длины, отсутствие ложного утверждения о подключении.
- `OemKuSourceServicePolicyTest`: целевой процесс виден, возможная проблема, неполный/неизвестный снимок.
- CI дополнительно компилирует Android Java, пересобирает DEX, проверяет неизменность OEM APK, запускает JVM тесты, подписывает APK и проверяет смали-патч v31.

## Что ещё не перенесено

- OEM `MyAutoBluetoothActivity` ищет устройства класса 1280 и фильтрует имена по `com.my.constant.Constant.bluetoothName[1..3]`. Затем запускает `CachedBluetoothDevice.connect(true)`. Это **автоматическое подключение**, а не просто AOSP AddAccessoryActivity; полноценная замена ещё не реализована.
- `KuSettings.MySetTimeService` при минутном `TIME_TICK` поддерживает `KuSourceRJ.TimeService`. Без проверки на реальном устройстве нельзя выключать заводской watchdog.
- Старые ссылки `xtzm_setting.txt.settingApps = com.my.settings`, `services.jar` и имена оригинальных активностей в прошивке требуют отдельного анализа.
- Обновление, фокус, трапеция, eARC/CEC/Game: часть функций продолжает открывать оригинальные OEM страницы из TvSettingsPlus вместо полностью нативной правой панели.

**Не отключать KuSettings / KuSourceRJ.** Софтверные проверки не являются аппаратной проверкой пульта, оптики или защиты от перегрева.

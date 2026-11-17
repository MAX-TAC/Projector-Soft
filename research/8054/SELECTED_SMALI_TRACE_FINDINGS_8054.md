# Выборочная трассировка оригинальных APK 8052/8054

Источник: [read-only CI run 44097233248](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44097233248), commit `04f591e57101700d552509c2966c3cf097b1d11e`, инструмент `tools/trace_8054_selected_smali.js`. В CI распакованы оригинальные APK, девять тел методов извлечены из smali. Никакие методы не запускались на проекторе. Полный текст трасс — в артефакте `flix8054-exact-method-diff-text-only` этого run.

## HDMI EDID

`TvSettingsPlus: EdidSourceSecondaryController#getEdidVersion()I`, `smali_classes14/.../EdidSourceSecondaryController.smali:129-154`: инструкция 142 помещает `0x6` в первый аргумент `MtkTvAVModeBase#getVideoInfoData` (строка 144); затем читается `MtkTvVideoInfoBase#getU8EdidIndex` (149). `SourceSubMenuController#getEdidVersion()I` (`:300-344`) использует тот же аргумент `0x6` (строки 313-320).

Дополнительная [трассировка выбора EDID, run 44106214713](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44106214713): восемь `onClick(View)` в `EdidSourceSecondaryController$1..$4` и `EdidSourceFragment$1..$4` проверяют видимость соответствующей radio-строки, затем вызывают `SettingsUtil#putGlobalValueInt` с одним ключом `tv_input_hdmi_edid_version` и значениями 0, 1, 2, 3. В secondary controller это smali строки 57-74 каждой inner class; во fragment — 57-70. В этих обработчиках нет аргумента физического HDMI-порта. Следующий участок цепочки — точное имя MTK-конфигурации в TvServices — описан в `EDID_GLOBAL_ROUTE_8054.md`. Конечный observer/HAL и readback после hotplug остаются неизвестными. C1 gate остаётся.

## Проекция

`TvSettingsPlus: ProjectorManager#setProjectorMirrorMode(Context,int)`, `smali_classes14/.../ProjectorManager.smali:263-304`: создаёт явный `Intent` с компонентом `mediatek.factorymenu.ui/mediatek.tvsetting.factory.ui.ApiService`, extra `projector_mode_value=p2`, затем вызывает `Context#startService` (строки 275-290).

`Factory: ApiService#onStartCommand(Intent,int,int)`, `smali/.../ApiService.smali:53-100`: читает `projector_mode_value` с default 0 и вызывает `Factory.ProjectorManager#setProjectorMirrorMode(int)` (62-74).

`Factory: ProjectorManager#setProjectorMirrorMode(int)`, `smali/.../ProjectorManager.smali:285-428`: switch по mode 0..3; для каждой ветви выставляет `g_misc__mirror` и `g_misc__flip` через `MtkTvConfig#setConfigValue`, вызывает `i2c(boolean)`, записывает `persist.rjpj.projector.mode` и вызывает `MtkTvFApiDisplay#setMirrorMethod` (308-403). Mapping property по ветвям 0→"0", 1→"1", 2→"2", 3→"3"; набор mirror/flip и аргумент i2c различаются по ветвям. Точный низкоуровневый I²C register write в этой трассировке не раскрыт. Setter 8052 также выполнял `sendBroadcastSetKeytone(Context)` (строка 492), а 8054 setter этого вызова не содержит и имеет сигнатуру без Context. Совпадение имени `ProjectorManager` в двух APK не означает общий класс.

Следствие для MOD: текущий selector из 8052 запускал бы этот OEM путь. C2 скрывает его на 8054 и неизвестной плате; штатное OEM-меню доступно. Откат C2 — revert исходников после полного trace и одобренного теста с записанным исходным режимом. Писать I²C или заводские параметры без этого нельзя.

## Bluetooth пульт

`KuSettings: MyAutoBluetoothActivity#viewAutoBluetoothBak()V`, `smali_classes5/.../MyAutoBluetoothActivity.smali:644-790`: единственное смысловое изменение в этом методе — сравнение `sysOta` со строкой `V618RJENHD` через `String#contains` (8052, строка 652) заменено на `String#equals` (8054, та же строка). Ветви загружают разные изображения в `auto_bluetooth_bak` через Glide в зависимости от `sysOta` и locale. Этот diff **не** доказывает изменение подключения HID-пульта.

Дополнительная [точная трассировка KuSettings, run 44098788358](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44098788358): `MyAutoBluetoothActivity#refreshDeviceList()V` (`smali_classes5/.../MyAutoBluetoothActivity.smali:411-563`) получает cached devices, проверяет `BluetoothClass#getMajorDeviceClass()==0x500` (строки 453-463), затем `isAutoName` (470). Для неподключённого кандидата останавливает scan и планирует handler message 3 через `0x3a98` мс, вызывает `CachedBluetoothDevice#startPairing()` (548) и `#connect(true)` (551). Если устройство уже подключено, вызывает `connectFinsh()` (484). Точный источник вызова `refreshDeviceList` есть в activity handler/lifecycle, но Android runtime на 8054 ещё не проверен.

`isAutoName(String)` (`:348-409`) отвергает пустое имя, принимает `Remote` и `BT_Remote` без учёта регистра, либо имя, содержащее `Remote`/`remote` (362-404). `getIrBluetoothStatus()` (`:175-220`) проверяет adapter enabled и `getProfileConnectionState(4)==2`, затем вызывает `connectFinsh`; фактический профиль пульта на устройстве остаётся неизвестным. Цепочка AutoPair и её отдельный boot receiver разобраны в `BLUETOOTH_REMOTE_8054_STATIC_TRACE.md`; взаимодействие механизмов в runtime всё ещё неизвестно.

## trdservice

`TrdService#onCreate()V`: обе версии регистрируют Binder `TRDSERVICE`, инициализируют UI/message handler и hardware trd, запускают `TrdCheckMirror`. В 8052 после этого запускается дополнительный поток `checkSleep` (строки 1175-1185); в 8054 его нет, метод завершается на 1093. Отдельный diff добавил `IMotorCtrl` Binder/SDK. Это не доказательство связи с `KuSourceRJ.TimeService` или термозащитой. trdservice и KuSettings остаются включёнными.

## Открытые проверки

Manifest/ресурсный diff этих APK, точные register writes внутри `i2c`, EDID setter/двухпортовая маршрутизация, BT HID/AutoPair и runtime права/поведение остаются открытыми. Аппаратных тестов 0.

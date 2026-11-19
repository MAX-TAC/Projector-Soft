# PIC14 — формат изображения HDMI и заводской Overscan на 8054

Исходная плата FlixTone 8054, MT9660, Android TV 13. HDMI 1 зарегистрирован как HW6, HDMI 2 как HW7. Это идентификаторы TvInput, а query type 6 из EDID — отдельный параметр другого API. Ни один из них не подставляется в API формата без подтверждённого контракта.

## Заводской маршрут

`FactoryMenuActivity → OverScanAdjustViewHolder`. Редактор умеет запрашивать смену источника через `TvInputSourceManager.setInputSource`, обрабатывает completion/loss и получает тип текущего входа через `MtkTvFApiInputSource.getCurrentInputSourceType()`. Для HDMI callback сводит физический вход к `EnumInputSourceType.E_HDMI`. В `updateIni()` настраиваются четыре границы изображения. В `Customize.setOverScan` передаются тип входа, `EnumOverScanScreenMode`, left, right, top, bottom. Прямого аргумента HW6/HW7 нет. Структура `MtkTvFApiVideoTiming` в редакторе сама по себе не доказывает, что timing входит в ключ хранения.

Точная последовательность `updateIni` вызывает `MtkTvAVModeBase.setCrntOverscan(top,bottom,left,right)`, затем `Customize.setOverScan(...)`, затем `MtkTvAVModeBase.updateOverscanIni()`. Ветка `Customize` проверяет `Feature.isSupportOverScanTiming()` и наличие `getCrntOverscan()`, берёт `getTimingIdx()` и передаёт его в `MtkTvFApiDisplay.setOverScan(type,timingIdx,mode,left,right,top,bottom)`. `MtkTvFApiDisplayIniManager.buildKeys()` содержит секцию `HDMIOverscanSetting`. Индекс timing и тип HDMI входят в обнаруженный заводской путь. Модель зависимости от частоты/HDR и повторного применения на уровне закрытого кода остаётся неизвестной. Переходы `TVNativeWrapper → TVNative` ведут в JNI и не дают подтверждения фактической обрезки кадра. [Трасса Factory](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44478282439/job/135404015716)

## Потребительский маршрут

В точном TvSettingsPlus обнаружен `com.android.tv.settings.partnercustomizer.picture.DisplayModeFragment`. Он запрашивает `PreferenceConfigUtils.getSupportScreenModes() → MtkTvAVModeBase.getAllScreenMode()`. В LiveTV `SettingsPreferenceScreen` проверяет наличие `g_video__screen_mode`, а `TVContent.isHaveScreenMode()` проверяет видимость config. `MenuDataHelper.getSupportScreenModes` получает допустимые режимы, `MenuConfigManager.getScreenMode` читает `MtkTvConfigBase.getConfigValue`. В ветке `setValue(String,Object)` код выбора передаётся в `MtkTvAVModeBase.setScreenMode(int) → TVNativeWrapper.setScreenMode_native(int) → TVNative.setScreenMode_native(int)`. Этот setter не получает номер порта.

В TvServices `TVMenuSettingsService.NotifyCallbackHandler.notifyConfigValuechanged` принимает `g_video__screen_mode`, читает `g_fusion_picture__pictrue_format`, отражает значение в `picture_format` и отправляет штатное уведомление `NOTIFY_DOLBY_VISION` с флагом `DOLBY`. Это обратная синхронизация хранилища. Она не подтверждает применение кадрирования. [Трасса потребительского формата](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44478282439/job/135404015716)

Независимая адресация HW6/HW7 в найденных Java API отсутствует. Значение может относиться к текущему input/video context на стороне MediaTek. Переход устройства на другой вход между read/write нельзя исключить повторным чтением общего ключа. Раздельное хранение или общая настройка для обоих HDMI ниже JNI пока не установлены.

### Маршрут TvSettingsPlus и побочные действия

Штатный `DisplayModeActivity.createSettingsFragment()` создаёт `DisplayModeFragment` с null Bundle. `PictureActivity` также открывает этот Fragment по stock `hot_key=picture_format`. `DisplayModeFragment.onCreatePreferences` читает `TVSettingConfig.getConfigValueInt("g_fusion_picture__pictrue_format")`. Метод `getSupportPictureFormatArrValue` сопоставляет MediaTek `getAllScreenMode()` с OEM-массивами меток и кодов (`0x7f0300db` и `0x7f0300dc` в exact APK). Список зависит от результата OEM в текущем видеоконтексте. Фиксированный список 16:9/4:3/Native для обоих портов этой трассой не установлен. [Трасса штатного редактора](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44479103148/job/135406710836)

`DisplayModeFragment.onPreferenceTreeClick` сверяет ключ с OEM-опциями, пишет целое через `PreferenceConfigUtils.putSettingValueInt(...,"picture_format",...)` и возвращается назад. В TvServices key входит в `prefKeys` и `initMapdata`. `SettingObserver → updateConfigData` читает Global и ACFG, преобразует код, пропускает совпадения и передаёт `g_fusion_picture__pictrue_format` в `TVSettingConfig.setConifg(config,int) → MtkTvConfigBase.setConfigValue`. Ветка формата предварительно снимает freeze на `main`, если видео было заморожено, и вне CN временно включает `setVideoMute("main",true)`. После setter следует `setVideoMute("main",false)`. Эта цепочка подтверждает побочные вызовы, но не HPD, обновление HDMI-согласования или успех на экране. [Трасса наблюдателя и setter](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44115598445/job/134152662624)

### Доставка в MOD

В меню «Изображение» пункт «Формат изображения HDMI» ведёт к проверенному same-package `DisplayModeActivity` без extras. При сборке проверяются Manifest, тело штатного launcher, оба OEM-массива и сигнатура `isHDMISource(Context)`. При создании строки и повторно при нажатии проверяются 8054, доступность компонента, штатный тип HDMI, текущий код и наличие как минимум двух режимов OEM. Если первое открытие пришлось на неподтверждённый контекст, пункт появится при пересоздании страницы после входа в поддерживаемый контекст. MOD не вызывает setter и не пишет `picture_format` самостоятельно. Тот же OEM backend отвечает за изменения с обоих HDMI, точная адресация хранением не гарантируется. PIC14 классифицирован `DELEGATED_OEM` только в части формата. Нативная запись, пользовательский Overscan и пер-портовый откат не подтверждены.

Python fixtures проверяют отбор реальных инструкций, отбрасывание неисполняемой DEX-строки, проверку Manifest, no-extra маршрут и наличие обоих массивов. JVM проверяет отсутствие допустимого HDMI-контекста, неизвестную плату/8052, пустой или неподдерживаемый список, ошибку чтения, дубли и выход текущего кода за доступный список. Setter и частичное применение относятся к штатному редактору и проверяются на устройстве после разрешения. Автоматический replay в MOD отсутствует.

## Связь с геометрией

`DigitalZoomActivity` из KeystoneCorrection использует сохранённые координаты `persist.rjpj.fourcorner.keystone_*`, как FourCorner. Это геометрическая коррекция всей проекции. В вызовах потребительского формата не найдено прямого доступа к этим свойствам или вызовов сброса FourCorner/HV/автотрапеции. Изменение видеорежима, взаимодействие с HDR, профиль PQ и геометрия ниже JNI проверяются отдельно на обоих входах. [Трасса проекции](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44396676096/job/135128331360)

## Допуск пользовательского редактирования

Перед нативной записью нужны подтверждённый потребительский UI, полный диапазон OEM, достоверный getter, контекст текущего TV-входа и видеорежима, область адресации, поведение при переключении и способ подтвердить применение. Отдельно нужны реакции на hotplug, SDR/HDR, разрешение и частоту. При отсутствии адресации или при гонке смены источника нативная запись недопустима. Для неизвестного результата повторной записи и автоматического отката нет.

## Ручной возврат при будущих испытаниях

После отдельного разрешения сначала зафиксировать исходные HDMI-вход, режим сигнала и значение в подтверждённом штатном пользовательском интерфейсе. Зафиксировать видимые границы тестового изображения по каждому порту. Проверить чтение после переключения входа и возврата без правок. Только при известном способе возврата изменить один параметр штатным пользовательским интерфейсом, восстановить исходное значение на исходном входе и проверить границы кадра на обоих HDMI. Если доступен только Factory, проверка изменения пропускается. Не использовать Factory Overscan, сброс геометрии и правку координат, чтобы имитировать возврат.

## PIC18 — данные панели

Exact Factory `PanelInfoViewHolder.onCreate` получает type, version, width и height из `MtkTvFApiDisplay`. В TvSettingsPlus обнаружены те же getters: они возвращают кэш `MtkTvFApiDisplayBase.panelInfo`. `loadPanelInfo` заполняет его из INI по ключам `PANEL_TYPE/VERSION/WIDTH/HEIGHT`. Заполнение этого кэша в обычном процессе MOD и безопасный самостоятельный потребительский вызов загрузки не подтверждены. Оставляем PIC18 `NOT_IMPLEMENTED`. Размер из panel INI характеризует конфигурацию панели. Логический режим Android, активный HDMI-тайминг и оптическое разрешение требуют самостоятельных источников данных. [Exact Factory и TvSettingsPlus](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44479103148/job/135406710836)

## Контрольная сборка

Код `9bcc368`, статус `BUILT_UNTESTED / RUNTIME_TEST_REQUIRED`. Exact verifier подтвердил `DisplayModeActivity` по pinned stock APK. Android/D8/JVM, OEM, v33, source safety и host tests Windows/Linux прошли. Локально прошли шесть Python fixtures, генерация статусов и валидатор графа. Аппаратный запуск не выполнялся. [Exact-8054 APK](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44480820981/artifacts/14508651743) · [Android/D8/JVM](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44480820985) · [Source safety / Linux / Windows](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44480821022) · [OEM](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44480820983) · [v33](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44480821004)

SHA-256 APK `68d7aaa60701d94930c46067f989a12731d2fd57d24f81a175ceccd6653b4757`.

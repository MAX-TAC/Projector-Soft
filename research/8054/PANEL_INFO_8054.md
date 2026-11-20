# PIC18 — сведения о конфигурации панели на 8054

## Кэш и getter

В точных TvSettingsPlus и Factory присутствуют собственные DEX-копии `MtkTvFApiDisplayBase`. В `<clinit>` класса создаётся один `PanelInfo` на экземпляр загрузчика классов. На уровне Java UID не делает статические поля общими между APK/процессами. `MtkTvFApiDisplay.getInstance()` лениво вызывает конструктор singleton. Конструктор базового класса вызывает `loadPanelInfo()`, затем читает пределы RGB white balance через `MtkTvConfigBase.getMinMaxConfigValue`. Сам `loadPanelInfo()` читает INI и заполняет кэш. Вторая обнаруженная точка вызова — `setPanelIni()` после изменения INI и eMMC env. Этот путь не используется для диагностики.

`getPanelType()`, `getPanelVersion()`, `getPanelWidth()`, `getPanelHeight()` читают уже имеющееся состояние Java. Для width и height выполняется `Integer.toString`. Getter сам не выбирает конфигурацию и не проверяет её свежесть. `Factory.PanelInfoViewHolder.onCreate` выводит эти четыре значения в заводской экран. [Exact-8054, первичная трасса](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44490359718/job/135444112822)

## Формат данных

`getPanelIni()` выбирает `E_MTK_FAPI_INI_TYPE_PANEL` через `MtkTvFApiDisplayIniManager.getPath`. `loadPanelInfo` получает имена ключей для TYPE, VERSION, WIDTH и HEIGHT. INI читается через `IMtkTvFApiInformationWrapper.getIniParameter`. Тип и версия приходят строками. Ширина и высота преобразуются в десятичные `int`, явного верхнего предела аппаратного профиля в обработчике нет. При недоступном пути или пустых dimensions есть ранний отказ. Тип и версию можно заполнить до сбоя размеров, поэтому наличие одной строки не подтверждает полный профиль. Ошибка числового преобразования также возможна. Заполнение в конструкторе или по `setPanelIni` не гарантирует обновление кэша при возврате в меню.

Подтверждённые поля относятся к выбранной OEM-конфигурации. Проверка физической матрицы, входной HDMI-тайминг, разрешение Android и оптическое разрешение требуют отдельных источников.

## Граница изменений

Заводские Activity, загрузку Factory ради заполнения кэша, запись INI, `setPanelIni`, eMMC, настройки интерфейса панели, White Balance и PQ оставляем вне потребительского маршрута. Проверки используют распакованные копии прошивки на CI. ADB, установка и аппаратные испытания требуют отдельного разрешения.

## Подтверждённая точка входа в Settings

Точный `ProjectorManager.<init>` вызывает `MtkTvFApiDisplay.getInstance()` и хранит экземпляр в `mDisplay`. Это отдельно от процесса Factory. Штатный раздел установки проектора получает `ProjectorManager` при чтении текущего положения. Открытие общей страницы Android TV Settings само по себе не гарантирует создание объекта. У Activity `MainSettings` и штатного `MainActivity` нет отдельной записи процесса в переданном инвентаре, у `StatusActivity` есть `com.android.phone`, и этот компонент к PIC18 отношения не имеет. [Трасса конструктора и служб](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44490917070/job/135445999306)

Инициализация классов дисплея получает HIDL-клиенты Factory Display, Information и System. Чтение INI идёт по `MtkTvFApiInformationWrapper.getIniParameter → IMtkTvFApiInformation.a_hidl_mtktvfapi_info_get_ini_para`. Реальный opener ниже HIDL. Нет доказательства прямой доступности файла процессу Settings. Нахождение собственного DEX определения getter в TvSettingsPlus не подтверждает наличие или доступность результата.

## Выбор активного INI

`MtkTvFApiDisplayIniManager.<init>` создаёт приватный массив `iniPaths` и последовательно вызывает `buildPaths()`, `buildKeys()`, `updataPath()`. Последний заполняет `PANEL` в обеих ветках:

- При `MtkTvFApiCusdataIniManager.IsSupportDataSeparation() == true` используется `getCusIniPath(PANEL_NAME)` через customer/data index. Сборщик путей customer-INI получает data-index path от штатного `MtkTvFApiSystemBase.getDataIndexPath()`.
- При выключенном разделении данных используется `getData(CUSTOMER1, PANEL_NAME)`. Это конфигурационная ветка той же прошивки.

Точные ключи `buildKeys`: `IniVersion:Type`, `IniVersion:Ver`, `panel:m_wPanelWidth`, `panel:m_wPanelHeight`. Это разрешение пути по конфигурации, а не фиксированный путь в APK. Физический путь и ветка для запущенного экземпляра ещё требуют проверки. В доступной распаковке системных разделов не найден файл с именем по маске `*panel*.ini`, что само по себе не исключает другое имя или runtime-монтирование. Путь выбирает OEM, чтение выполняет HIDL-сервис. Файл из MOD не открывается. [Трасса `updataPath` и HIDL](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44492383982/job/135451029557)

## Интеграция PIC18

Раздел «Информация о панели» в «Об устройстве» обращается только к существующему `ProjectorManager.sInstance.mDisplay` в ClassLoader Settings. Exact verifier требует, чтобы чтение поля не вызывало активный static initializer, а четыре getter только читали кэш. Дополнительно verifier требует обе штатные ветки заполнения `PANEL`. MOD не вызывает `getInstance()`, `loadPanelInfo()` или HIDL и не поднимает заводские Activity или службы. Пассивное чтение без нового permission возможно, когда тот же процесс уже создал штатный singleton и в кэше есть опубликованные поля.

При первом открытии с пустым кэшем категория скрыта. При attach и возврате фокуса окна раздел повторно читает доступные поля, не обновляя INI. Если доступны только тип или версия, выводятся только эти поля. Размеры показываются в пикселях и помечены как конфигурация. Нулевые, отрицательные, нечисловые размеры, пустые строки и значения Android/OEM-заглушек скрыты. Текущий HDMI к выбору пути не относится. После перезапуска процесса доступность определяется заново штатным кэшем.

Пассивный reader был проверен на exact trial до нахождения второй фазы загрузки путей. Промежуточный кандидат исключал PIC18 до проверки всех writer. После обнаружения `updataPath()` обе ветки стали частью exact build gate и раздел включён в финальный исходник.

## FAC01 — безопасная часть диагностики

В «Об устройстве» добавлена строка «Чип (по данным Android)». Она использует публичные `Build.SOC_MANUFACTURER` и `Build.SOC_MODEL` на API 31+ для 8054. В точном пакете `vendor/build.prop` источник сообщает `MediaTek` и `mt5873`. Отображается значение текущей системы с этой атрибуцией. Маркировка микросхемы на плате остаётся отдельной аппаратной проверкой. Недоступные и заглушечные значения скрываются. PRJ/PIC, Factory Board/PQ и системная строка модели устройства через этот путь не меняются. [Android Build](https://developer.android.com/reference/android/os/Build#SOC_MODEL) · [Выборка exact vendor build.prop](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44490917070/job/135445999306)

## Приёмка после отдельного разрешения

1. Открыть «Об устройстве» после обычного запуска Settings. Проверить, что пустой, неполный или недоступный кэш не создаёт пустую категорию и не показывает ошибочные значения.
2. В том же процессе открыть штатный раздел положения проектора без изменения ориентации. После Back повторно открыть «Об устройстве». Зафиксировать доступные поля, их источник и реакцию на возврат фокуса. Не открывать Factory и не менять настройки панели для получения контрольных данных.
3. Сверить подписи и единицы прочитанных значений. Смена HDMI, логического режима Android и разрешения входного сигнала проверяется отдельно от конфигурации панели.
4. Проверить, что «Чип (по данным Android)» расположен рядом с данными системы, не дублирует строку модели устройства и скрывается при отсутствии обоих Android-полей.

Программный JVM-тест подставляет уже существующий объект, недоступные поля, отказ getter, незаполненный кэш и смену содержимого между чтениями. Вызов setter или initializer в тестовом объекте завершил бы тест ошибкой. Exact верификатор отдельно проверяет реальные getter, сигнатуру singleton, отсутствие активной инициализации держателя и две ветки пути в pinned APK. Реальная доступность активного INI, время прогрева кэша и D-pad/Back имеют статус `RUNTIME_TEST_REQUIRED`.

## Проверенная сборка

Код `d16ab5c`. PIC18 и FAC01 — `READ_ONLY / BUILT_UNTESTED`. Exact verifier подтвердил `ProjectorManager.sInstance.mDisplay` и две штатные ветки PANEL. Локальная генерация 96 статусов и валидатор графа прошли. В CI прошли JVM-тесты обоих модулей, Android/D8, OEM, v33, source-safety и Windows/Linux. [Exact-8054 APK](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44493132279/artifacts/14513208410) · [Лог exact verifier](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44493132279/job/135453532312) · [Android/JVM](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44493132135) · [Source-safety и host](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44493132327) · [OEM](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44493132285) · [v33](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44493132169)

SHA-256 APK `52cb7b7ead4974e414c9c12be1651bf7c4bbfb3d6f2c295c855b1e868c11c54a`.

# 8052: original APK menu inventory and KuSetupWizard component comparison

Static original OTA APK metadata only; v2 adds intent aliases but no DEX/UI screens.

## KuSetupWizard local class copies
KuSettings class definitions also present in KuSetupWizard: 7946 of 7946
Lcom/my/service/BootupReceiver;: True
Lcom/my/service/MySetTimeService;: True
Lcom/my/service/ScreenServices;: True
Lcom/my/settings/SettingActivity;: True
Lcom/my/settings/bluetooth/MyAutoBluetoothActivity;: True
KuSetupWizard declared old OEM components: []

## Intent actions present in KuSettings but not original TvSettingsPlus
- android.settings.ALLAPPS
- android.settings.AUTO_BLUETOOTH
- android.settings.BLUETOOTH_SETTINGS
- android.settings.DATETIME_SETTINGS
- android.settings.LAUGUAGE_LOCAL_SETTINGS
- android.settings.LAUGUAGE_SETTINGS
- android.settings.PROJECTABOUT_SETTINGS
- android.settings.SCREENSHOT_SETTINGS
- android.settings.SETTINGS
- android.settings.WIFI_SETTINGS

## KuSettings app components
- activity com.my.settings.SettingActivity => android.intent.action.MAIN,android.settings.SETTINGS
- activity com.my.settings.wifi.MyWifiActivity => android.settings.WIFI_SETTINGS
- activity com.my.settings.bluetooth.MyBluetoothActivity => android.settings.BLUETOOTH_SETTINGS
- activity com.my.settings.bluetooth.MyAutoBluetoothActivity => android.settings.AUTO_BLUETOOTH
- activity com.my.settings.language.LanguageActivity => android.settings.LAUGUAGE_SETTINGS
- activity com.my.settings.language.LanguageLocalActivity => android.settings.LAUGUAGE_LOCAL_SETTINGS
- activity com.my.settings.time.DateTimeActivity => android.settings.DATETIME_SETTINGS
- activity com.my.settings.time.MyZonePicker => 
- activity com.my.settings.activity.ScreenshotActivity => android.settings.SCREENSHOT_SETTINGS
- service com.my.service.ScreenServices => 
- activity com.my.settings.activity.ProjectionActivity => android.settings.PROJECTABOUT_SETTINGS
- activity com.my.settings.activity.AllAppActivity => android.settings.ALLAPPS
- receiver com.my.service.BootupReceiver => android.intent.action.BOOT_COMPLETED
- service com.my.service.MySetTimeService => 
- provider androidx.startup.InitializationProvider => 
- receiver androidx.profileinstaller.ProfileInstallReceiver => androidx.profileinstaller.action.INSTALL_PROFILE,androidx.profileinstaller.action.SKIP_FILE,androidx.profileinstaller.action.SAVE_PROFILE,androidx.profileinstaller.action.BENCHMARK_OPERATION

## TvSettingsPlus original preference resource nodes

### about_legal.xml (7 nodes)
- Юридическая информация [PreferenceScreen]
  - Авторские права [PreferenceScreen]
  - Лицензия [PreferenceScreen]
  - Условия использования [PreferenceScreen]
  - Лицензия WebView [PreferenceScreen]
  - Информация для потребителей [com.android.tv.settings.about.ConsumerInformationDialogPreference]
  - Реклама [Preference]

### about_legal_compat.xml (7 nodes)
- Юридическая информация [PreferenceScreen]
  - Авторские права [com.android.tv.settings.compat.TsPreference]
  - Лицензия [com.android.tv.settings.compat.TsPreference]
  - Условия использования [com.android.tv.settings.compat.TsPreference]
  - Лицензия WebView [com.android.tv.settings.compat.TsPreference]
  - Информация для потребителей [com.android.tv.settings.about.ConsumerInformationDialogPreferenceCompat]
  - Реклама [com.android.tv.settings.compat.TsPreference]

### accessibility.xml (7 nodes)
- Специальные возможности [PreferenceScreen]
  - Субтитры [Preference]
  - Контрастный текст [com.android.tv.settings.widget.SwitchWithSoundPreference]
  - Синтез речи [Preference]
  - Быстрое включение [Preference]
  - Аудиодескрипция [com.android.tv.settings.widget.SwitchWithSoundPreference]
  - Сервисы [PreferenceCategory]

### accessibility_compat.xml (7 nodes)
- Специальные возможности [PreferenceScreen]
  - Субтитры [com.android.tv.settings.compat.TsPreference]
  - Контрастный текст [com.android.tv.settings.compat.TsSwitchWithSoundPreference]
  - Синтез речи [com.android.tv.settings.compat.TsPreference]
  - Быстрое включение [com.android.tv.settings.compat.TsPreference]
  - Аудиодескрипция [com.android.tv.settings.compat.TsSwitchWithSoundPreference]
  - Сервисы [com.android.tv.settings.compat.TsPreferenceCategory]

### accessibility_service_compat.xml (2 nodes)
- PreferenceScreen [PreferenceScreen]
  - screen [com.android.tv.settings.compat.TsPreferenceCategory]

### accessibility_shortcut.xml (4 nodes)
- Быстрое включение [PreferenceScreen]
  - Быстрое включение [com.android.tv.settings.widget.SwitchWithSoundPreference]
  - Быстрое включение сервиса [Preference]
  - Когда эта функция включена, вы можете активировать специальные возможности, одновременно удерживая кнопки \"Назад\" и \"Вниз\" в течение тре [Preference]

### accessibility_shortcut_compat.xml (4 nodes)
- Быстрое включение [PreferenceScreen]
  - Быстрое включение [com.android.tv.settings.compat.TsSwitchWithSoundPreference]
  - Быстрое включение сервиса [com.android.tv.settings.compat.TsPreference]
  - Когда эта функция включена, вы можете активировать специальные возможности, одновременно удерживая кнопки \"Назад\" и \"Вниз\" в течение тре [com.android.tv.settings.compat.TsPreference]

### accessibility_shortcut_service.xml (1 nodes)
- Быстрое включение сервиса [PreferenceScreen]

### accessibility_shortcut_service_compat.xml (2 nodes)
- Быстрое включение сервиса [PreferenceScreen]
  - screen [com.android.tv.settings.compat.TsPreferenceCategory]

### account_preference.xml (4 nodes)
- PreferenceScreen [PreferenceScreen]
  - Синхронизировать [Preference]
  - Удалить аккаунт [Preference]
  - Выбрать приложения [PreferenceCategory]

### accounts.xml (3 nodes)
- Аккаунты и вход [PreferenceScreen]
  - Добавить аккаунт [com.android.settingslib.RestrictedPreference]
  - Этим устройством управляет ваша организация. [com.android.settingslib.widget.FooterPreference]

### adb_wireless_settings.xml (8 nodes)
- Отладка по Wi-Fi [PreferenceScreen]
  - adb_wireless_selection_option [PreferenceCategory]
    - Отключено [com.android.tv.settings.RadioPreference]
    - Включено [com.android.tv.settings.RadioPreference]
  - Подключить устройство с помощью кода подключения [Preference]
  - Название устройства [Preference]
  - IP-адрес и порт [Preference]
  - Подключенные устройства [PreferenceCategory]

### advanced_display.xml (3 nodes)
- Расширенные настройки экрана [PreferenceScreen]
  - Разрешить игровой режим [com.android.tv.settings.widget.SwitchWithSoundPreference]
  - Выбор формата [Preference]

### advanced_display_compat.xml (4 nodes)
- Расширенные настройки экрана [PreferenceScreen]
  - Разрешить игровой режим [com.android.tv.settings.compat.TsSwitchPreference]
  - Выбор формата [com.android.tv.settings.compat.TsPreference]
  - Разрешение [com.android.tv.settings.compat.TsPreference]

### advanced_sound.xml (5 nodes)
- Выбор форматов [PreferenceScreen]
  - advanced_sound_settings_option [PreferenceCategory]
    - "Автоматически: включить форматы, поддерживаемые выходным аудиоустройством " [com.android.tv.settings.RadioPreference]
    - Нет: никогда не использовать объемный звук [com.android.tv.settings.RadioPreference]
    - Вручную: настроить каждый формат, поддерживаемый на устройстве, независимо от поддержки на выходном аудиоустройстве [com.android.tv.settings.RadioPreference]

### advanced_volume_compat.xml (4 nodes)
- Выбор форматов [PreferenceScreen]
  - advanced_sound_settings_option [com.android.tv.settings.compat.TsPreferenceCategory]
  - surround_sound_format_info [com.android.tv.settings.compat.TsPreferenceCategory]
  - surround_sound_format_info_on_manual [com.android.tv.settings.compat.TsPreferenceCategory]

### alarms_and_reminders.xml (2 nodes)
- Будильники и напоминания [PreferenceScreen]
  - "Если вы разрешите приложениям устанавливать будильники и планировать на определенное время действия, то эти приложения будут работать в фон [Preference]

### alarms_and_reminders_compat.xml (2 nodes)
- Будильники и напоминания [PreferenceScreen]
  - "Если вы разрешите приложениям устанавливать будильники и планировать на определенное время действия, то эти приложения будут работать в фон [com.android.tv.settings.compat.TsPreference]

### all_apps.xml (5 nodes)
- Все приложения [PreferenceScreen]
  - Установленные приложения [PreferenceCategory]
  - Отключенные приложения [PreferenceCategory]
  - Показывать системные приложения [Preference]
  - Системные приложения [PreferenceCategory]

### all_apps_compat.xml (5 nodes)
- Все приложения [PreferenceScreen]
  - Установленные приложения [com.android.tv.settings.compat.TsPreferenceCategory]
  - Отключенные приложения [com.android.tv.settings.compat.TsPreferenceCategory]
  - Показывать системные приложения [com.android.tv.settings.compat.TsPreference]
  - Системные приложения [com.android.tv.settings.compat.TsPreferenceCategory]

### all_files_access.xml (2 nodes)
- Разрешить доступ к управлению всеми файлами [PreferenceScreen]
  - Приложение сможет читать, изменять и удалять все файлы на этом устройстве и любых подключенных носителях. Доступ к файлам будет осуществлять [Preference]

### app_list_disclosure_settings.xml (3 nodes)
- app_list_disclosure_settings [PreferenceScreen]
  - dashboard_tile_placeholder [PreferenceCategory]
  - Количество приложений является приблизительным и может не включать установки вне Google Play [com.android.settingslib.widget.FooterPreference]

### app_management_compat.xml (13 nodes)
- Приложения [PreferenceScreen]
  - version [com.android.tv.settings.compat.TsPreference]
  - open [com.android.tv.settings.compat.TsPreference]
  - forceStop [com.android.tv.settings.compat.TsRestrictedPreference]
  - uninstall [com.android.tv.settings.compat.TsRestrictedPreference]
  - enableDisable [com.android.tv.settings.compat.TsRestrictedPreference]
  - appStorage [com.android.tv.settings.compat.TsRestrictedPreference]
  - clearData [com.android.tv.settings.compat.TsRestrictedPreference]
  - clearCache [com.android.tv.settings.compat.TsRestrictedPreference]
  - clearDefaults [com.android.tv.settings.compat.TsRestrictedPreference]
  - notifications [com.android.tv.settings.compat.TsSwitchPreference]
  - permissions [com.android.tv.settings.compat.TsPreference]
  - licenses [com.android.tv.settings.compat.TsPreference]

### app_usage_access.xml (2 nodes)
- Доступ к истории использования [PreferenceScreen]
  - Эта настройка разрешает отслеживать данные об использовании других приложений, сведения об операторе связи, языковые настройки и другую инфо [Preference]

### app_usage_access_compat.xml (2 nodes)
- Экономия энергии [PreferenceScreen]
  - Эта настройка разрешает отслеживать данные об использовании других приложений, сведения об операторе связи, языковые настройки и другую инфо [com.android.tv.settings.compat.TsPreference]

### apps.xml (8 nodes)
- Приложения [PreferenceScreen]
  - Недавно открытые приложения [PreferenceCategory]
  - Все приложения [Preference]
  - Неиспользуемые приложения [Preference]
  - Разрешения [PreferenceCategory]
    - Разрешения приложений [Preference]
    - Специальный доступ [Preference]
  - Безопасность и ограничения [Preference]

### apps_compat.xml (7 nodes)
- Приложения [PreferenceScreen]
  - Недавно открытые приложения [com.android.tv.settings.compat.TsPreferenceCategory]
  - Все приложения [com.android.tv.settings.compat.TsPreference]
  - Неиспользуемые приложения [TsPreference]
  - Разрешения [com.android.tv.settings.compat.TsPreferenceCategory]
    - Разрешения приложений [com.android.tv.settings.compat.TsPreference]
  - Специальный доступ [com.android.tv.settings.compat.TsPreference]

### apps_x.xml (10 nodes)
- Приложения [PreferenceScreen]
  - Недавно открытые приложения [PreferenceCategory]
  - Все приложения [Preference]
  - Неиспользуемые приложения [Preference]
  - Разрешения [PreferenceCategory]
    - update [com.android.tv.twopanelsettings.slices.SlicePreference]
    - Разрешения приложений [Preference]
    - Специальный доступ [Preference]
  - Безопасность и ограничения [Preference]
  - overlay_security [com.android.tv.twopanelsettings.slices.SlicePreference]

### autofill_picker.xml (2 nodes)
- Выберите службу автозаполнения [PreferenceScreen]
  - Нет [com.android.tv.settings.RadioPreference]

### available_virtual_keyboard_compat.xml (1 nodes)
- Виртуальные клавиатуры [PreferenceScreen]

### bluetooth_accessory.xml (4 nodes)
- PreferenceScreen [PreferenceScreen]
  - Переименовать [Preference]
  - Отключить [Preference]
  - battery [Preference]

### caption.xml (11 nodes)
- Субтитры [PreferenceScreen]
  - Экран [com.android.tv.settings.widget.SwitchWithSoundPreference]
  - Параметры показа [PreferenceCategory]
    - Язык [ListPreference]
    - Размер текста [ListPreference]
  - Стиль титров [PreferenceCategory]
    - Белый на черном [com.android.tv.settings.RadioPreference]
    - Черный на белом [com.android.tv.settings.RadioPreference]
    - Желтый на черном [com.android.tv.settings.RadioPreference]
    - Желтый на синем [com.android.tv.settings.RadioPreference]
    - Другое [com.android.tv.settings.RadioPreference]

### caption_custom.xml (12 nodes)
- Персональные настройки [PreferenceScreen]
  - Шрифт [ListPreference]
  - Цвет текста [ListPreference]
  - Прозрачность текста [ListPreference]
  - Тип контура [ListPreference]
  - Цвет контура [ListPreference]
  - Показывать фон [SwitchPreference]
  - Цвет фона [ListPreference]
  - Прозрачность фона [ListPreference]
  - Показывать окно [SwitchPreference]
  - Цвет окна [ListPreference]
  - Прозрачность окна [ListPreference]

### channel_install_mode.xml (6 nodes)
- Режим установки каналов [PreferenceScreen]
  - Антенна [com.android.tv.settings.RadioPreference]
  - Кабельное ТВ [com.android.tv.settings.RadioPreference]
  - Предпочтительный спутник [com.android.tv.settings.RadioPreference]
  - Основной спутник [com.android.tv.settings.RadioPreference]
  - Спутник [com.android.tv.settings.RadioPreference]

### channel_scan_store_type.xml (7 nodes)
- PreferenceScreen [PreferenceScreen]
  - Все [com.android.tv.settings.RadioPreference]
  - Только зашифрованные каналы [com.android.tv.settings.RadioPreference]
  - Только бесплатные каналы [com.android.tv.settings.RadioPreference]
  - Все [com.android.tv.settings.RadioPreference]
  - Только цифровые каналы [com.android.tv.settings.RadioPreference]
  - Только радиоканалы [com.android.tv.settings.RadioPreference]

### channels.xml (10 nodes)
- Каналы [PreferenceScreen]
  - Цифровые и аналоговые каналы [Preference]
  - Аналоговые каналы [Preference]
  - Цифровой [Preference]
  - Антенна [Preference]
  - Кабельное ТВ [Preference]
  - Спутник [Preference]
  - Автоматическое обновление каналов [SwitchPreference]
  - Сообщение об обновлении канала [SwitchPreference]
  - Тип списка каналов [Preference]

### channels_and_inputs_vendor.xml (6 nodes)
- Каналы и входы [PreferenceScreen]
  - Каналы [Preference]
  - Каналы [Preference]
  - Канал [Preference]
  - Каналы [Preference]
  - Входы [Preference]

### channels_scan.xml (17 nodes)
- Каналы [PreferenceScreen]
  - Режим установки каналов [Preference]
  - Сканирование каналов [Preference]
  - Обновление параметров сканирования [Preference]
  - Сканирование аналоговых каналов вручную [Preference]
  - Сканирование одного ВЧ-канала [Preference]
  - Обновить сервисы вручную [Preference]
  - LCN [Preference]
  - Тип сканирования канала [Preference]
  - Тип сохранения каналов [Preference]
  - Выбрать предпочитаемую сеть [Preference]
  - Повторное сканирование спутников [Preference]
  - Добавление спутника [Preference]
  - Обновление спутника [Preference]
  - Точная настройка спутника вручную [Preference]
  - Настройка приложения \"%1$s\" [Preference]
  - Режим сканирования [Preference]

### date_time.xml (6 nodes)
- Дата и время [PreferenceScreen]
  - Автонастройка даты и времени [ListPreference]
  - Установить дату [com.android.tv.settings.system.LeanbackPickerDialogPreference]
  - Установить время [com.android.tv.settings.system.LeanbackPickerDialogPreference]
  - Выбрать часовой пояс [Preference]
  - 24-часовой формат [com.android.tv.settings.widget.SwitchWithSoundPreference]

### date_time_compat.xml (6 nodes)
- Дата и время [PreferenceScreen]
  - Автонастройка даты и времени [com.android.tv.settings.compat.TsListPreference]
  - Установить дату [com.android.tv.settings.compat.TsLeanbackPickerDialogPreference]
  - Установить время [com.android.tv.settings.compat.TsLeanbackPickerDialogPreference]
  - Выбрать часовой пояс [com.android.tv.settings.compat.TsPreference]
  - 24-часовой формат [com.android.tv.settings.compat.TsSwitchWithSoundPreference]

### daydream.xml (4 nodes)
- Заставка [PreferenceScreen]
  - Заставка [ListPreference]
  - Запуск [com.android.tv.settings.device.display.daydream.DreamTimePreference]
  - Запустить [Preference]

### daydream_compat.xml (4 nodes)
- Заставка [PreferenceScreen]
  - Заставка [com.android.tv.settings.compat.TsListPreference]
  - Запуск [com.android.tv.settings.compat.TsListPreference]
  - Запустить [com.android.tv.settings.compat.TsPreference]

### daydream_x.xml (3 nodes)
- Заставка [PreferenceScreen]
  - Запустить [Preference]
  - ambient_settings [com.android.tv.twopanelsettings.slices.SlicePreference]

### development_prefs.xml (67 nodes)
- Для разработчиков [PreferenceScreen]
  - Включить параметры для разработчиков [com.android.tv.settings.widget.SwitchWithSoundPreference]
  - @android:string/bugreport_screenshot_failure_toast [Preference]
  - Пароль для резервного копирования [PreferenceScreen]
  - Не выключать экран [com.android.tv.settings.widget.SwitchWithSoundPreference]
  - Проверка HDCP [ListPreference]
  - Включить журнал HCI Bluetooth [ListPreference]
  - Заводская разблокировка [com.android.settingslib.RestrictedSwitchPreference]
  - Работающие службы [PreferenceScreen]
  - Цветовой режим [com.android.tv.settings.system.development.ColorModePreference]
  - Отладка [PreferenceCategory]
    - Отладка по USB [com.android.tv.settings.widget.SwitchWithSoundPreference]
    - Отозвать доступ для отладки по USB [Preference]
    - Отладка по Wi-Fi [Preference]
    - Локальный терминальный доступ [com.android.tv.settings.widget.SwitchWithSoundPreference]
    - Выбрать приложение для фиктивных местоположений [PreferenceScreen]
    - Включить проверку атрибутов [com.android.tv.settings.widget.SwitchWithSoundPreference]
    - Выбрать приложение для отладки [PreferenceScreen]
    - Ждать подключения отладчика [com.android.tv.settings.widget.SwitchWithSoundPreference]
    - Проверять приложения при установке [com.android.tv.settings.widget.SwitchWithSoundPreference]
    - Размер буфера журнала [ListPreference]
    - Постоянно хранить данные журнала на устройстве [ListPreference]
  - Сети [PreferenceCategory]
    - Серт. беспроводн. мониторов [com.android.tv.settings.widget.SwitchWithSoundPreference]
    - Подробный журнал Wi‑Fi [com.android.tv.settings.widget.SwitchWithSoundPreference]
    - Не отключать мобильный интернет [com.android.tv.settings.widget.SwitchWithSoundPreference]
    - Конфигурация USB [ListPreference]
  - Ввод [PreferenceCategory]
    - Показывать нажатия [com.android.tv.settings.widget.SwitchWithSoundPreference]
    - Место касания [com.android.tv.settings.widget.SwitchWithSoundPreference]
  - Отрисовка [PreferenceCategory]
    - Показ. обнов. поверхности [com.android.tv.settings.widget.SwitchWithSoundPreference]
    - Показывать границы элементов [com.android.tv.settings.widget.SwitchWithSoundPreference]
    - Отразить интерфейс [com.android.tv.settings.widget.SwitchWithSoundPreference]
    - Анимация окон [ListPreference]
    - Анимация переходов [ListPreference]
    - Длительность анимации [ListPreference]
    - Эмуляция доп. экранов [ListPreference]
  - Аппаратное ускорение отрисовки [PreferenceCategory]
    - Показывать обнов. экрана [com.android.tv.settings.widget.SwitchWithSoundPreference]
    - Показ. аппаратные обновл. [com.android.tv.settings.widget.SwitchWithSoundPreference]
    - Отладка наложения [ListPreference]
    - Отладка операций усечения сложной формы [ListPreference]
    - Размытие на уровне окон [SwitchPreference]
    - Включить 4x MSAA [com.android.tv.settings.widget.SwitchWithSoundPreference]
    - Откл. аппарат. наложения [com.android.tv.settings.widget.SwitchWithSoundPreference]
    - Имитировать аномалию [ListPreference]
  - Мультимедиа [PreferenceCategory]
    - Откл. передачу аудио (USB) [com.android.tv.settings.widget.SwitchWithSoundPreference]
  - Аудио [PreferenceCategory]
    - Запись аудио [com.android.tv.settings.widget.SwitchWithSoundPreference]
    - Воспроизведение аудиозаписей [Preference]
    - Сохранение аудиозаписей [Preference]
    - Время до воспроизведения аудио [Preference]
    - Время до записи аудио [Preference]
    - Длительность пустой аудиозаписи [Preference]
  - Мониторинг [PreferenceCategory]
    - Строгий режим [com.android.tv.settings.widget.SwitchWithSoundPreference]
    - Учет времени работы HWUI [ListPreference]
    - Вкл. трассировку OpenGL [ListPreference]
  - Приложения [PreferenceCategory]
    - Вытеснение фоновых Activity [com.android.tv.settings.widget.SwitchWithSoundPreference]
    - Лимит фоновых процессов [ListPreference]
    - ANR в фоновом режиме [com.android.tv.settings.widget.SwitchWithSoundPreference]
    - Приложения в режиме ожидания [Preference]
    - Разрешить сохранение на внешние накопители [com.android.tv.settings.widget.SwitchWithSoundPreference]
    - Изменение размера в многооконном режиме [com.android.tv.settings.widget.SwitchWithSoundPreference]

### development_prefs_not_available.xml (2 nodes)
- Для разработчиков [PreferenceScreen]
  - Этому пользователю недоступны возможности разработчика [Preference]

### device.xml (20 nodes)
- Настройки устройства [PreferenceScreen]
  - Об устройстве [Preference]
  - Дата и время [Preference]
  - Язык [Preference]
  - Клавиатура [Preference]
  - Экран и звук [Preference]
  - Хранилище [Preference]
  - Главный экран [Preference]
  - Google [Preference]
  - Google Cast [Preference]
  - Заставка [Preference]
  - Режим энергосбережения [Preference]
  - Питание [Preference]
  - Таймер [Preference]
  - Для разработчиков [Preference]
  - Местоположение [Preference]
  - Использование и диагностика [Preference]
  - Специальные возможности [Preference]
  - Демонстрационный режим [Preference]
  - Перезагрузить [com.android.tv.settings.LongClickPreference]

### device_admin_settings.xml (2 nodes)
- Приложения администратора устройства [PreferenceScreen]
  - Нет доступных приложений администратора устройства [com.android.settingslib.widget.FooterPreference]

### device_info_settings.xml (20 nodes)
- Об устройстве [PreferenceScreen]
  - Руководства [Preference]
  - Обновление системы [Preference]
  - Доп. обновления системы [Preference]
  - Название устройства [Preference]
  - Сброс настроек [com.android.settingslib.RestrictedPreference]
  - Статус [Preference]
  - Руководство [Preference]
  - Юридическая информация [Preference]
  - Юридическая информация [Preference]
  - Отправить отзыв об устройстве [Preference]
  - Модель [Preference]
  - Версия ОС Android TV [Preference]
  - Исправление системы безопасности ОС Android TV [Preference]
  - Идентификатор оборудования [Preference]
  - Прошивка модуля связи [Preference]
  - Версия ядра [Preference]
  - Сборка ОС Android TV [Preference]
  - Статус SELinux [Preference]
  - general_info [com.android.tv.twopanelsettings.slices.EmbeddedSlicePreference]

### device_info_settings_vendor.xml (22 nodes)
- Об устройстве [PreferenceScreen]
  - Руководства [Preference]
  - Обновление системы [Preference]
  - Доп. обновления системы [Preference]
  - Название устройства [Preference]
  - Сброс [Preference]
  - Статус [Preference]
  - Руководство [Preference]
  - Юридическая информация [Preference]
  - Юридическая информация [Preference]
  - Отправить отзыв об устройстве [Preference]
  - Модель [Preference]
  - Версия ОС Android TV [Preference]
  - Исправление системы безопасности ОС Android TV [Preference]
  - Идентификатор оборудования [Preference]
  - Прошивка модуля связи [Preference]
  - Версия ядра [Preference]
  - Сборка ОС Android TV [Preference]
  - Статус SELinux [Preference]
  - Netflix ESN [com.android.tv.twopanelsettings.slices.EmbeddedSlicePreference]
  - Версия ПО [com.android.tv.twopanelsettings.slices.EmbeddedSlicePreference]
  - Срок работы телевизора [com.android.tv.twopanelsettings.slices.EmbeddedSlicePreference]

### device_info_settings_x.xml (20 nodes)
- Об устройстве [PreferenceScreen]
  - Руководства [Preference]
  - Обновление системы [com.android.tv.twopanelsettings.slices.CustomContentDescriptionRestrictedPreference]
  - Доп. обновления системы [Preference]
  - Название устройства [Preference]
  - Сброс настроек [com.android.tv.twopanelsettings.slices.CustomContentDescriptionRestrictedPreference]
  - Статус [Preference]
  - Руководство [Preference]
  - Юридическая информация [Preference]
  - Юридическая информация [Preference]
  - Отправить отзыв об устройстве [Preference]
  - Модель [Preference]
  - Версия ОС Android TV [Preference]
  - Исправление системы безопасности ОС Android TV [Preference]
  - Идентификатор оборудования [Preference]
  - Прошивка модуля связи [Preference]
  - Версия ядра [Preference]
  - Сборка ОС Android TV [Preference]
  - Статус SELinux [Preference]
  - general_info [com.android.tv.twopanelsettings.slices.EmbeddedSlicePreference]

### device_info_status.xml (9 nodes)
- Статус [PreferenceScreen]
  - Состояние батареи [Preference]
  - Уровень заряда батареи [Preference]
  - IP-адрес [Preference]
  - MAC-адрес устройства [Preference]
  - Адрес Bluetooth [Preference]
  - Серийный номер [Preference]
  - Время с момента включения [Preference]
  - Статус регистрации сервиса IMS [Preference]

### device_restricted.xml (9 nodes)
- Настройки устройства [PreferenceScreen]
  - Об устройстве [Preference]
  - Язык [Preference]
  - Дата и время [Preference]
  - Клавиатура [Preference]
  - Главный экран [Preference]
  - Google [Preference]
  - Специальные возможности [Preference]
  - Местоположение [Preference]

### device_two_panel.xml (14 nodes)
- Настройки устройства [PreferenceScreen]
  - Об устройстве [Preference]
  - Дата и время [Preference]
  - Язык [Preference]
  - Клавиатура [Preference]
  - Хранилище [Preference]
  - Главный экран [Preference]
  - Заставка [Preference]
  - Режим энергосбережения [Preference]
  - Для разработчиков [Preference]
  - Специальные возможности [Preference]
  - overlay_internal [com.android.tv.twopanelsettings.slices.SlicePreference]
  - Устройства [com.android.tv.twopanelsettings.slices.SlicePreference]
  - Перезагрузить [com.android.tv.settings.LongClickPreference]

### device_vendor.xml (15 nodes)
- Настройки устройства [PreferenceScreen]
  - Специальные возможности [Preference]
  - Об устройстве [Preference]
  - Дата и время [Preference]
  - Язык [Preference]
  - Клавиатура [Preference]
  - Хранилище [Preference]
  - Главный экран [Preference]
  - Заставка [com.android.tv.twopanelsettings.slices.SlicePreference]
  - Питание и энергопотребление [Preference]
  - Для разработчиков [Preference]
  - cast_settings [com.android.tv.twopanelsettings.slices.SlicePreference]
  - Системные звуки [SwitchPreference]
  - Демонстрационный режим [Preference]
  - Перезагрузить [com.android.tv.settings.LongClickPreference]

### device_x.xml (16 nodes)
- Настройки устройства [PreferenceScreen]
  - Специальные возможности [Preference]
  - Об устройстве [Preference]
  - Дата и время [Preference]
  - Язык [Preference]
  - Клавиатура [Preference]
  - Хранилище [Preference]
  - Главный экран [Preference]
  - Заставка [Preference]
  - Режим энергосбережения [Preference]
  - Для разработчиков [Preference]
  - cast_settings [com.android.tv.twopanelsettings.slices.SlicePreference]
  - assistant_broadcast [com.android.tv.twopanelsettings.slices.SlicePreference]
  - overlay_internal [com.android.tv.twopanelsettings.slices.SlicePreference]
  - Устройства [com.android.tv.twopanelsettings.slices.SlicePreference]
  - Перезагрузить [com.android.tv.settings.LongClickPreference]

### directory_access.xml (2 nodes)
- Доступ к каталогам [PreferenceScreen]
  - У этих приложений есть доступ к некоторым каталогам. [Preference]

### directory_access_details.xml (1 nodes)
- Доступ к каталогам [PreferenceScreen]

### display_and_sound.xml (2 nodes)
- Экран и звук [PreferenceScreen]
  - Экран и звук [Preference]

### display_sound.xml (11 nodes)
- Экран и звук [PreferenceScreen]
  - HDMI-CEC [com.android.tv.twopanelsettings.slices.SlicePreference]
  - Экран [PreferenceCategory]
  - Масштабирование текста [Preference]
  - Использовать частоту кадров контента [Preference]
  - Разрешение [Preference]
  - Расширенные настройки экрана [Preference]
  - Звук [PreferenceCategory]
  - Системные звуки [SwitchPreference]
  - default_audio_output_settings [com.android.tv.twopanelsettings.slices.SlicePreference]
  - Расширенные настройки звука [Preference]

### display_sound_compat.xml (9 nodes)
- Экран и звук [PreferenceScreen]
  - HDMI-CEC [com.android.tv.settings.compat.TsSlicePreference]
  - Экран [com.android.tv.settings.compat.TsPreferenceCategory]
  - Масштабирование текста [com.android.tv.settings.compat.TsPreference]
  - Использовать частоту кадров контента [com.android.tv.settings.compat.TsPreference]
  - Расширенные настройки экрана [com.android.tv.settings.compat.TsPreference]
  - Звук [com.android.tv.settings.compat.TsPreferenceCategory]
  - Системные звуки [com.android.tv.settings.compat.TsSwitchPreference]
  - Расширенные настройки звука [com.android.tv.settings.compat.TsPreference]

### display_sound_x.xml (13 nodes)
- Экран и звук [PreferenceScreen]
  - HDMI-CEC [com.android.tv.twopanelsettings.slices.SlicePreference]
  - Экран [PreferenceCategory]
  - display_resolution [com.android.tv.twopanelsettings.slices.EmbeddedSlicePreference]
  - Масштабирование текста [Preference]
  - hdr_and_color_format [com.android.tv.twopanelsettings.slices.EmbeddedSlicePreference]
  - Использовать частоту кадров контента [Preference]
  - Разрешение [Preference]
  - Расширенные настройки экрана [Preference]
  - Звук [PreferenceCategory]
  - Системные звуки [com.android.tv.settings.widget.SwitchWithSoundPreference]
  - default_audio_output_settings [com.android.tv.twopanelsettings.slices.SlicePreference]
  - Расширенные настройки звука [Preference]

### energy_saver.xml (3 nodes)
- Режим энергосбережения [PreferenceScreen]
  - Выключать экран [com.android.tv.settings.device.display.daydream.DreamTimePreference]
  - Разрешить отключение экрана [com.android.settingslib.RestrictedSwitchPreference]

### energy_saver_compat.xml (3 nodes)
- Режим энергосбережения [PreferenceScreen]
  - Выключать экран [com.android.tv.settings.compat.TsListPreference]
  - Разрешить отключение экрана [com.android.tv.settings.compat.TsRestrictedSwitchPreference]

### enterprise_privacy_settings.xml (26 nodes)
- Информация об управляемом устройстве [PreferenceScreen]
  - Какие данные доступны вашей организации [PreferenceCategory]
    - Данные, связанные с вашим рабочим аккаунтом, например почта и мероприятия в календаре [Preference]
    - Список приложений на устройстве [Preference]
    - Время работы и израсходованный трафик для каждого приложения [Preference]
    - Последний журнал сетевого трафика [Preference]
    - Последний отчет об ошибках [Preference]
    - Последний журнал безопасности [Preference]
  - Изменения, внесенные администратором организации [PreferenceCategory]
    - Установленные приложения [Preference]
    - Доступ к геоданным [Preference]
    - Доступ к микрофону [Preference]
    - Доступ к камере [Preference]
    - Приложения по умолчанию [Preference]
    - always_on_vpn_primary_user [Preference]
    - Постоянная VPN включена в вашем рабочем профиле [Preference]
    - Клавиатура по умолчанию [Preference]
    - Выбран глобальный прокси-сервер HTTP [Preference]
    - Надежные сертификаты в персональном профиле [Preference]
    - Надежные сертификаты в рабочем профиле [Preference]
  - Ваш доступ к устройству [PreferenceCategory]
    - Администратор может заблокировать устройство и сбросить пароль [Preference]
    - Администратор может удалить все данные с устройства [Preference]
    - Количество неудачных попыток ввода пароля, после которого данные на устройстве будут удалены [Preference]
    - Количество неудачных попыток ввода пароля, после которого данные рабочего профиля будут удалены [Preference]
  - "Чтобы предоставить доступ к рабочим данным, ваша организация может менять настройки и устанавливать ПО на устройство.

За дополнительной ин [com.android.settingslib.widget.FooterPreference]

### enterprise_set_default_apps_settings.xml (2 nodes)
- PreferenceScreen [PreferenceScreen]
  - dashboard_tile_placeholder [PreferenceCategory]

### font_scale.xml (2 nodes)
- Масштабирование текста [PreferenceScreen]
  - font_scale_group [PreferenceCategory]

### font_scale_compat.xml (1 nodes)
- Масштабирование текста [PreferenceScreen]

### free_up_storage.xml (3 nodes)
- Освободите место в хранилище [PreferenceScreen]
  - Очистите кеш [com.android.tv.settings.device.storage.ClearCachedDataPreference]
  - Удалите некоторые приложения [Preference]

### hdr_format_selection.xml (4 nodes)
- Выбор формата [PreferenceScreen]
  - hdr_format_selection_option [PreferenceCategory]
    - Автоматически [com.android.tv.settings.RadioPreference]
    - Вручную [com.android.tv.settings.RadioPreference]

### hdr_format_selection_compat.xml (4 nodes)
- Выбор формата [PreferenceScreen]
  - hdr_format_selection_option [com.android.tv.settings.compat.TsPreferenceCategory]
  - hdr_format_info [com.android.tv.settings.compat.TsPreferenceCategory]
  - hdr_format_info_on_manual [com.android.tv.settings.compat.TsPreferenceCategory]

### help_and_feedback.xml (3 nodes)
- Справка/отзыв [PreferenceScreen]
  - Отправить отзыв [Preference]
  - Справка [Preference]

### help_and_feedback_x.xml (4 nodes)
- Справка/отзыв [PreferenceScreen]
  - Отправить отзыв [Preference]
  - Справка [Preference]
  - help_center_info [Preference]

### input_options.xml (10 nodes)
- PreferenceScreen [PreferenceScreen]
  - Показать вход [CheckBoxPreference]
  - Название [PreferenceCategory]
    - name_default [com.android.tv.settings.RadioPreference]
    - Blu-ray [com.android.tv.settings.RadioPreference]
    - Кабельное ТВ [com.android.tv.settings.RadioPreference]
    - DVD-плеер [com.android.tv.settings.RadioPreference]
    - Игровая приставка [com.android.tv.settings.RadioPreference]
    - Aux [com.android.tv.settings.RadioPreference]
    - Введите название [com.android.tv.settings.RadioPreference]

### inputs.xml (8 nodes)
- Входы [PreferenceScreen]
  - connected_inputs [PreferenceCategory]
  - standby_inputs [PreferenceCategory]
  - disconnected_inputs [PreferenceCategory]
  - Consumer Electronic Control (CEC) [PreferenceCategory]
    - Управлять HDMI-устройствами [SwitchPreference]
    - Автомат. отключать устройство [SwitchPreference]
    - Автомат. включать телевизор [SwitchPreference]

### keyboard.xml (6 nodes)
- Клавиатура и автозаполнение [PreferenceScreen]
  - Клавиатура [PreferenceCategory]
  - Текущая клавиатура [ListPreference]
  - Управление клавиатурами [Preference]
  - Автозаполнение [PreferenceCategory]
    - Действующая служба автозаполнения [Preference]

### keyboard_compat.xml (6 nodes)
- Клавиатура и автозаполнение [PreferenceScreen]
  - Клавиатура [com.android.tv.settings.compat.TsPreferenceCategory]
  - Текущая клавиатура [com.android.tv.settings.compat.TsListPreference]
  - Управление клавиатурами [com.android.tv.settings.compat.TsPreference]
  - Автозаполнение [com.android.tv.settings.compat.TsPreferenceCategory]
    - Действующая служба автозаполнения [com.android.tv.settings.compat.TsPreference]

### language_compat.xml (1 nodes)
- Язык [PreferenceScreen]

### location_compat.xml (4 nodes)
- Местоположение [PreferenceScreen]
  - locationMode [com.android.tv.settings.compat.TsListPreference]
  - wifi_always_scan [com.android.tv.settings.compat.TsSwitchPreference]
  - recent_location_requests [com.android.tv.settings.compat.TsPreferenceCategory]

### main_prefs.xml (16 nodes)
- Настройки [PreferenceScreen]
  - Общие настройки [PreferenceCategory]
    - Сеть и интернет [Preference]
    - Канал [Preference]
    - Многоадресный режим [Preference]
    - Аккаунты и вход [Preference]
    - Аккаунты и вход [com.android.tv.twopanelsettings.slices.SlicePreference]
    - Звук [Preference]
    - Конфиденциальность [Preference]
    - Экран и звук [Preference]
    - Приложения [Preference]
    - Настройки устройства [Preference]
    - Пульты и аксессуары [Preference]
    - Пульты и аксессуары [Preference]
    - Пульты и аксессуары [com.android.tv.twopanelsettings.slices.SlicePreference]
    - Справка/отзыв [Preference]

### main_prefs_vendor.xml (19 nodes)
- Настройки [PreferenceScreen]
  - basic_mode_suggestion [PreferenceCategory]
    - basic_mode_exit [Preference]
  - Общие настройки [PreferenceCategory]
    - Каналы и входы [Preference]
    - Каналы и входы [com.android.tv.twopanelsettings.slices.SlicePreference]
    - Экран и звук [Preference]
    - Сеть и интернет [Preference]
    - Аккаунты и вход [Preference]
    - Аккаунты и вход [com.android.tv.twopanelsettings.slices.SlicePreference]
    - Аккаунты и вход [Preference]
    - Звук [Preference]
    - Конфиденциальность [Preference]
    - Приложения [Preference]
    - Настройки устройства [Preference]
    - Пульты и аксессуары [Preference]
    - Пульты и аксессуары [Preference]
    - Пульты и аксессуары [com.android.tv.twopanelsettings.slices.SlicePreference]
    - Справка/отзыв [Preference]

### main_prefs_x.xml (18 nodes)
- Настройки [PreferenceScreen]
  - basic_mode_suggestion [PreferenceCategory]
    - basic_mode_exit [Preference]
  - Общие настройки [PreferenceCategory]
    - Каналы и входы [com.android.tv.twopanelsettings.slices.SlicePreference]
    - Экран и звук [Preference]
    - Сеть и интернет [Preference]
    - Аккаунты и вход [Preference]
    - Аккаунты и вход [com.android.tv.twopanelsettings.slices.SlicePreference]
    - Аккаунты и вход [Preference]
    - Звук [Preference]
    - Конфиденциальность [Preference]
    - Приложения [Preference]
    - Настройки устройства [Preference]
    - Пульты и аксессуары [Preference]
    - Пульты и аксессуары [Preference]
    - Пульты и аксессуары [com.android.tv.twopanelsettings.slices.SlicePreference]
    - Справка/отзыв [Preference]

### manage_external_sources.xml (2 nodes)
- Установка неизвестных приложений [PreferenceScreen]
  - Личные данные и данные устройства более уязвимы для атак приложений, скачанных из неизвестных источников. Вы соглашаетесь с тем, что несете  [Preference]

### manage_high_power.xml (2 nodes)
- Экономия энергии [PreferenceScreen]
  - Оптимизация энергопотребления приложений [Preference]

### manage_high_power_compat.xml (2 nodes)
- Экономия энергии [PreferenceScreen]
  - Оптимизация энергопотребления приложений [com.android.tv.settings.compat.TsPreference]

### match_content_frame_rate.xml (5 nodes)
- Использовать частоту кадров контента [PreferenceScreen]
  - match_content_frame_rate_option [PreferenceCategory]
    - Согласовывать частоту кадров [com.android.tv.settings.RadioPreference]
    - Не согласовывать частоту кадров [com.android.tv.settings.RadioPreference]
    - Никогда [com.android.tv.settings.RadioPreference]

### match_content_rate_compat.xml (1 nodes)
- Использовать частоту кадров контента [PreferenceScreen]

### missing_storage.xml (3 nodes)
- PreferenceScreen [PreferenceScreen]
  - "Подключите диск заново, чтобы получить доступ к приложениям и другим данным на нем. Если это невозможно и диск недоступен, выберите \"Удали [Preference]
  - Не использовать как накопитель [Preference]

### missing_storage_compat.xml (3 nodes)
- PreferenceScreen [PreferenceScreen]
  - "Подключите диск заново, чтобы получить доступ к приложениям и другим данным на нем. Если это невозможно и диск недоступен, выберите \"Удали [Preference]
  - Не использовать как накопитель [Preference]

### multi_cast.xml (3 nodes)
- Многоадресный режим [PreferenceScreen]
  - Режим [PreferenceCategory]
  - Аудиовыход [PreferenceCategory]

### network.xml (16 nodes)
- Сеть и интернет [PreferenceScreen]
  - Политика конфиденциальности сети [com.android.tv.twopanelsettings.slices.EmbeddedSlicePreference]
  - Wi-Fi [SwitchPreference]
  - Показать все [Preference]
  - Другие варианты [PreferenceCategory]
    - Быстрое подключение [com.android.settingslib.RestrictedPreference]
    - Добавить сеть [com.android.settingslib.RestrictedPreference]
    - Экономия трафика [com.android.tv.twopanelsettings.slices.SlicePreference]
    - Передача данных и оповещения [com.android.tv.twopanelsettings.slices.SlicePreference]
    - Всегда искать сети [SwitchPreference]
    - Конфигурации вывода из спящего режима [com.android.tv.twopanelsettings.slices.EmbeddedSlicePreference]
  - Ethernet [PreferenceCategory]
    - Нет подключения [Preference]
    - Настройки прокси-сервера [Preference]
    - Настройки IP-адреса [Preference]
  - Диагностика сети [Preference]

### network_compat.xml (13 nodes)
- Сеть и интернет [PreferenceScreen]
  - Вы можете подключить устройство к интернету с помощью Wi-Fi, сети Ethernet или точки доступа на телефоне [com.android.tv.settings.compat.TsPreferenceCategory]
  - Wi-Fi [com.android.tv.settings.compat.TsSwitchPreference]
  - Показать все [com.android.tv.settings.compat.TsPreference]
  - Другие варианты [com.android.tv.settings.compat.TsPreferenceCategory]
    - Быстрое подключение [com.android.tv.settings.compat.TsRestrictedPreference]
    - Добавить сеть [com.android.tv.settings.compat.TsRestrictedPreference]
    - Всегда искать сети [com.android.tv.settings.compat.TsSwitchPreference]
  - Ethernet [com.android.tv.settings.compat.TsPreferenceCategory]
    - Нет подключения [com.android.tv.settings.compat.TsPreference]
    - Настройки прокси-сервера [com.android.tv.settings.compat.TsPreference]
    - Настройки IP-адреса [com.android.tv.settings.compat.TsPreference]
  - Диагностика сети [com.android.tv.settings.compat.TsPreference]

### network_x.xml (18 nodes)
- Сеть и интернет [PreferenceScreen]
  - Вы можете подключить устройство к интернету с помощью Wi-Fi, сети Ethernet или точки доступа на телефоне [Preference]
  - Wi-Fi [com.android.tv.settings.widget.SwitchWithSoundPreference]
  - Показать все [Preference]
  - Другие варианты [PreferenceCategory]
    - Быстрое подключение [com.android.settingslib.RestrictedPreference]
    - Добавить сеть [com.android.settingslib.RestrictedPreference]
    - Экономия трафика [com.android.tv.twopanelsettings.slices.SlicePreference]
    - Передача данных и оповещения [com.android.tv.twopanelsettings.slices.SlicePreference]
    - Всегда искать сети [com.android.tv.settings.widget.CustomContentDescriptionSwitchPreference]
  - Ethernet [PreferenceCategory]
    - Нет подключения [Preference]
    - Настройки прокси-сервера [Preference]
    - Настройки IP-адреса [Preference]
  - Диагностика сети [Preference]
  - Другое [PreferenceCategory]
    - Политика конфиденциальности сети [com.android.tv.twopanelsettings.slices.EmbeddedSlicePreference]
    - Конфигурации вывода из спящего режима [com.android.tv.twopanelsettings.slices.EmbeddedSlicePreference]

### normal_cast.xml (1 nodes)
- Обычный [PreferenceScreen]

### notification_access.xml (2 nodes)
- Доступ к уведомлениям [PreferenceScreen]
  - Эти приложения смогут читать все уведомления, в том числе содержащие личную информацию (например, имена контактов и сообщения). Они также по [Preference]

### notification_access_compat.xml (2 nodes)
- Доступ к уведомлениям [PreferenceScreen]
  - Эти приложения смогут читать все уведомления, в том числе содержащие личную информацию (например, имена контактов и сообщения). Они также по [com.android.tv.settings.compat.TsPreference]

### partner_accessibility_audio.xml (5 nodes)
- Аудио [PreferenceScreen]
  - Тип аудио [ListPreference]
  - Звуковое описание [SwitchPreference]
  - Для людей с нарушением зрения [Preference]
  - Для людей с нарушениями слуха [SwitchPreference]

### partner_advanced_video.xml (1 nodes)
- Расширенные настройки [PreferenceScreen]

### partner_audio_subtitle.xml (1 nodes)
- PreferenceScreen [PreferenceScreen]

### partner_black_pref.xml (1 nodes)
- PreferenceScreen [PreferenceScreen]

### partner_calibration.xml (4 nodes)
- Калибровка Dolby Vision PQ [PreferenceScreen]
  - Режим просмотра [ListPreference]
  - Калибровка конечным пользователем [Preference]
  - Время последнего изменения [Preference]

### partner_color_temperature.xml (5 nodes)
- Цветовая температура [PreferenceScreen]
  - Цветовая температура [ListPreference]
  - Усиление красного [com.android.tv.settings.partnercustomizer.utils.ProgressPreference]
  - Усиление зеленого [com.android.tv.settings.partnercustomizer.utils.ProgressPreference]
  - Усиление синего [com.android.tv.settings.partnercustomizer.utils.ProgressPreference]

### partner_date_time.xml (6 nodes)
- Дата и время [PreferenceScreen]
  - Автонастройка даты и времени [ListPreference]
  - Установить дату [com.android.tv.settings.system.LeanbackPickerDialogPreference]
  - Установить время [com.android.tv.settings.system.LeanbackPickerDialogPreference]
  - Выбрать часовой пояс [Preference]
  - 24-часовой формат [SwitchPreference]

### partner_dbx.xml (2 nodes)
- dbx-tv [PreferenceScreen]
  - @string/device_picture_reset_to_default [Preference]

### partner_device_3rd.xml (3 nodes)
- Настройки устройства [PreferenceScreen]
  - Звук [Preference]
  - Изображение [Preference]

### partner_dts.xml (4 nodes)
- DTS Virtual:X [PreferenceScreen]
  - Virtual:X [SwitchPreference]
  - TBHDX [SwitchPreference]
  - Ограничитель [SwitchPreference]

### partner_end_user_calibration.xml (14 nodes)
- Калибровка конечным пользователем [PreferenceScreen]
  - Сохранить [Preference]
  - Сбросить [Preference]
  - Tmax [com.android.tv.settings.partnercustomizer.picture.DialogPreference]
  - Tmin [com.android.tv.settings.partnercustomizer.picture.DialogPreference]
  - Гамма дисплея [com.android.tv.settings.partnercustomizer.picture.DialogPreference]
  - Rx [com.android.tv.settings.partnercustomizer.picture.DialogPreference]
  - Ry [com.android.tv.settings.partnercustomizer.picture.DialogPreference]
  - Gx [com.android.tv.settings.partnercustomizer.picture.DialogPreference]
  - Gy [com.android.tv.settings.partnercustomizer.picture.DialogPreference]
  - Bx [com.android.tv.settings.partnercustomizer.picture.DialogPreference]
  - By [com.android.tv.settings.partnercustomizer.picture.DialogPreference]
  - Wx [com.android.tv.settings.partnercustomizer.picture.DialogPreference]
  - Wy [com.android.tv.settings.partnercustomizer.picture.DialogPreference]

### partner_filmmaker.xml (3 nodes)
- Режим создания фильма [PreferenceScreen]
  - Режим создания фильма [SwitchPreference]
  - Автоматическое переключение [SwitchPreference]

### partner_mjc.xml (4 nodes)
- MJC [PreferenceScreen]
  - Эффекты [ListPreference]
  - Демонстрационный фрагмент [ListPreference]
  - Демоверсия [com.android.tv.settings.partnercustomizer.picture.DialogPreference]

### partner_network_wake_configs.xml (4 nodes)
- Конфигурации вывода из спящего режима [PreferenceScreen]
  - WoW [SwitchPreference]
  - WoL [SwitchPreference]
  - WoC [SwitchPreference]

### partner_oled.xml (4 nodes)
- OLED [PreferenceScreen]
  - Компенсация OLED [Preference]
  - Ручная настройка компенсации OFF-RS [Preference]
  - Сдвиг пикселей [SwitchPreference]

### partner_power.xml (6 nodes)
- Питание [PreferenceScreen]
  - Автовыключение [ListPreference]
  - Без изображения [Preference]
  - Таймер отключения [ListPreference]
  - Автопереход в спящий режим [Preference]
  - Standby light [SwitchPreference]

### partner_power_on_channel.xml (3 nodes)
- Канал при включении [PreferenceScreen]
  - Выберите режим [ListPreference]
  - Показать каналы [Preference]

### partner_retail_mode.xml (4 nodes)
- Демонстрационный режим [PreferenceScreen]
  - Демонстрационный режим [SwitchPreference]
  - Обмен сообщениями [ListPreference]
  - Образец качества изображения [ListPreference]

### partner_sound_speaker_devices_beyond.xml (1 nodes)
- Удалить сателлитные колонки [PreferenceScreen]

### partner_sound_wireless_speaker.xml (6 nodes)
- Беспроводные колонки [PreferenceScreen]
  - Беспроводные колонки [SwitchPreference]
  - Сателлитные колонки [PreferenceCategory]
    - Добавить сателлитные колонки [Preference]
  - Сабвуфер [PreferenceCategory]
    - Добавить сабвуфер [Preference]

### partner_speaker_device_info.xml (7 nodes)
- PreferenceScreen [PreferenceScreen]
  - Название [EditTextPreference]
  - Указать местоположение [Preference]
  - Установить громкость [com.android.tv.settings.partnercustomizer.utils.ProgressDialogPreference]
  - Удалить [Preference]
  - Перезапустить [Preference]
  - Версия [Preference]

### partner_speaker_location_setting.xml (1 nodes)
- Настройка расположения колонок [PreferenceScreen]

### partner_timer.xml (6 nodes)
- Таймер [PreferenceScreen]
  - Периодичность автовключения [ListPreference]
  - Время автовключения [Preference]
  - Канал при включении [Preference]
  - Периодичность автовыключения [ListPreference]
  - Время автовыключения [Preference]

### partner_visually.xml (8 nodes)
- Для людей с нарушением зрения [PreferenceScreen]
  - Динамик [SwitchPreference]
  - Наушники [SwitchPreference]
  - Громкость [com.android.tv.settings.partnercustomizer.utils.ProgressPreference]
  - Панорама и затухание [SwitchPreference]
  - Для людей с нарушением зрения [Preference]
  - Управление фейдером [ListPreference]
  - Улучшение диалогов AC4 [ListPreference]

### partner_visually_impaired_audio.xml (1 nodes)
- Для людей с нарушением зрения [PreferenceScreen]

### picture_in_picture.xml (2 nodes)
- Картинка в картинке [PreferenceScreen]
  - Когда приложение открыто или когда вы выходите из него, оно может создавать окно \"Картинка в картинке\". Так вы можете, например, продолжит [Preference]

### picture_in_picture_compat.xml (2 nodes)
- Картинка в картинке [PreferenceScreen]
  - Когда приложение открыто или когда вы выходите из него, оно может создавать окно \"Картинка в картинке\". Так вы можете, например, продолжит [com.android.tv.settings.compat.TsPreference]

### power_and_energy_vendor.xml (5 nodes)
- Питание и энергопотребление [PreferenceScreen]
  - Состояние при включении [com.android.tv.twopanelsettings.slices.SlicePreference]
  - Режим энергосбережения [Preference]
  - Питание [Preference]
  - Таймер [Preference]

### preview_display_vendor.xml (6 nodes)
- Экран и звук [PreferenceScreen]
  - Изображение [Preference]
  - Экран [Preference]
  - Звук [Preference]
  - Аудиовыход [Preference]
  - Custom Entries 1 [Preference]

### privacy.xml (14 nodes)
- Конфиденциальность [PreferenceScreen]
  - Настройки устройства [PreferenceCategory]
  - Местоположение [Preference]
  - Микрофон [Preference]
  - Камера [Preference]
  - Использование и диагностика [com.android.tv.twopanelsettings.slices.SlicePreference]
  - Реклама [Preference]
  - Настройки аккаунта [PreferenceCategory]
  - Google Ассистент [com.android.tv.twopanelsettings.slices.SlicePreference]
  - Платежи и покупки [com.android.tv.twopanelsettings.slices.SlicePreference]
  - Настройки приложений [PreferenceCategory]
  - Разрешения приложений [Preference]
  - Специальный доступ [Preference]
  - Безопасность и ограничения [Preference]

### privacy_compat.xml (5 nodes)
- Конфиденциальность [PreferenceScreen]
  - Настройки устройства [PreferenceCategory]
  - Местоположение [com.android.tv.settings.compat.TsPreference]
  - Микрофон [com.android.tv.settings.compat.TsPreference]
  - Камера [com.android.tv.settings.compat.TsPreference]

### privacy_x.xml (16 nodes)
- Конфиденциальность [PreferenceScreen]
  - Настройки устройства [PreferenceCategory]
  - Местоположение [Preference]
  - Микрофон [Preference]
  - Камера [Preference]
  - Использование и диагностика [com.android.tv.twopanelsettings.slices.SlicePreference]
  - Реклама [com.android.tv.twopanelsettings.slices.CustomContentDescriptionPreference]
  - Настройки аккаунта [PreferenceCategory]
  - Google Ассистент [com.android.tv.twopanelsettings.slices.SlicePreference]
  - Платежи и покупки [com.android.tv.twopanelsettings.slices.SlicePreference]
  - Настройки приложений [PreferenceCategory]
  - update [com.android.tv.twopanelsettings.slices.SlicePreference]
  - Разрешения приложений [Preference]
  - Специальный доступ [Preference]
  - Безопасность и ограничения [Preference]
  - overlay_security [com.android.tv.twopanelsettings.slices.SlicePreference]

### remotes_and_accessories.xml (2 nodes)
- Пульты и аксессуары [PreferenceScreen]
  - Добавить устройство [com.android.settingslib.RestrictedPreference]

### reset_options_vendor.xml (2 nodes)
- Сброс [PreferenceScreen]
  - Сброс настроек [Preference]

### resolution_selection.xml (2 nodes)
- Разрешение [PreferenceScreen]
  - resolution_selection_option [PreferenceCategory]

### resolution_selection_compat.xml (2 nodes)
- Разрешение [PreferenceScreen]
  - resolution_selection_option [com.android.tv.settings.compat.TsPreferenceCategory]

### security.xml (12 nodes)
- Безопасность и ограничения [PreferenceScreen]
  - Неизвестные источники [Preference]
  - Профиль с ограничен. доступом [PreferenceCategory]
    - Перейти в профиль с ограниченным доступом [Preference]
    - Выйти из профиля [Preference]
    - Разрешенные приложения [Preference]
    - Изменить PIN-код [Preference]
    - Создать профиль с ограниченным доступом [Preference]
    - Удалить профиль [Preference]
  - security_settings_device_admin_category [PreferenceCategory]
    - Приложения администратора устройства [Preference]
    - Информация об управляемом устройстве [Preference]

### sensor_compat.xml (5 nodes)
- Конфиденциальность [PreferenceScreen]
  - Настройки устройства [PreferenceCategory]
  - sensor_toggle [com.android.tv.settings.compat.TsSwitchPreference]
  - Показать все [com.android.tv.settings.compat.TsPreference]
  - open_permission_controller [com.android.tv.settings.compat.TsPreference]

### sound.xml (1 nodes)
- Звук [PreferenceScreen]

### special_app_access.xml (10 nodes)
- Специальный доступ [PreferenceScreen]
  - Экономия энергии [Preference]
  - Доступ к истории использования [Preference]
  - Доступ к уведомлениям [Preference]
  - Доступ ко всем файлам [Preference]
  - Поверх других приложений [Preference]
  - Изменение системных настроек [Preference]
  - Картинка в картинке [Preference]
  - Будильники и напоминания [Preference]
  - Включение экрана [Preference]

### special_app_access_compat.xml (8 nodes)
- Специальный доступ [PreferenceScreen]
  - Экономия энергии [com.android.tv.settings.compat.TsPreference]
  - Доступ к истории использования [com.android.tv.settings.compat.TsPreference]
  - Доступ к уведомлениям [com.android.tv.settings.compat.TsPreference]
  - Поверх других приложений [com.android.tv.settings.compat.TsPreference]
  - Изменение системных настроек [com.android.tv.settings.compat.TsPreference]
  - Картинка в картинке [com.android.tv.settings.compat.TsPreference]
  - Будильники и напоминания [com.android.tv.settings.compat.TsPreference]

### storage.xml (11 nodes)
- PreferenceScreen [PreferenceScreen]
  - Перенести данные в память этого устройства [Preference]
  - Извлечь [Preference]
  - Очистка и форматирование [Preference]
  - Приложения [com.android.tv.settings.device.storage.StoragePreference]
  - Фото и видео [com.android.tv.settings.device.storage.StoragePreference]
  - Аудио [com.android.tv.settings.device.storage.StoragePreference]
  - Загрузки [com.android.tv.settings.device.storage.StoragePreference]
  - Данные кеша [com.android.tv.settings.device.storage.StoragePreference]
  - Прочее [com.android.tv.settings.device.storage.StoragePreference]
  - Доступно [com.android.tv.settings.device.storage.StoragePreference]

### storage_compat.xml (11 nodes)
- PreferenceScreen [PreferenceScreen]
  - Перенести данные в память этого устройства [com.android.tv.settings.compat.TsPreference]
  - Извлечь [com.android.tv.settings.compat.TsPreference]
  - Очистка и форматирование [com.android.tv.settings.compat.TsPreference]
  - Приложения [com.android.tv.settings.compat.TsPreference]
  - Фото и видео [com.android.tv.settings.compat.TsPreference]
  - Аудио [com.android.tv.settings.compat.TsPreference]
  - Загрузки [com.android.tv.settings.compat.TsPreference]
  - Данные кеша [com.android.tv.settings.compat.TsPreference]
  - Прочее [com.android.tv.settings.compat.TsPreference]
  - Доступно [com.android.tv.settings.compat.TsPreference]

### storage_summary.xml (4 nodes)
- Хранилище [PreferenceScreen]
  - Хранилище устройства [PreferenceCategory]
  - Съемный накопитель [PreferenceCategory]
  - Освободите место в хранилище [Preference]

### storage_summary_compat.xml (3 nodes)
- Хранилище [PreferenceScreen]
  - Хранилище устройства [com.android.tv.settings.compat.TsPreferenceCategory]
  - Съемный накопитель [com.android.tv.settings.compat.TsPreferenceCategory]

### storage_summary_x.xml (6 nodes)
- Хранилище [PreferenceScreen]
  - suggestions [PreferenceCategory]
    - ambient_storage [com.android.tv.twopanelsettings.slices.EmbeddedSlicePreference]
  - Хранилище устройства [PreferenceCategory]
  - Съемный накопитель [PreferenceCategory]
  - Освободите место в хранилище [Preference]

### storage_uninstall_apps.xml (2 nodes)
- Все приложения [PreferenceScreen]
  - Установленные приложения [PreferenceCategory]

### system_alert_window.xml (2 nodes)
- Поверх других приложений [PreferenceScreen]
  - Разрешить показывать элементы интерфейса поверх других окон. Это может мешать вашей работе с приложениями или искажать их интерфейс. [Preference]

### system_alert_window_compat.xml (2 nodes)
- Поверх других приложений [PreferenceScreen]
  - Разрешить показывать элементы интерфейса поверх других окон. Это может мешать вашей работе с приложениями или искажать их интерфейс. [com.android.tv.settings.compat.TsPreference]

### tts_engine_settings.xml (4 nodes)
- PreferenceScreen [PreferenceScreen]
  - Язык [ListPreference]
  - %s [Preference]
  - Установка голосовых данных [Preference]

### tts_settings.xml (7 nodes)
- Синтез речи [PreferenceScreen]
  - Синтезатор речи по умолчанию [PreferenceCategory]
  - Настройки синтеза [Preference]
  - Общие [PreferenceCategory]
    - Скорость речи [ListPreference]
  - Прослушать пример [Preference]
  - Язык по умолчанию [Preference]

### turn_screen_on.xml (2 nodes)
- Разрешить приложению включать экран [PreferenceScreen]
  - Приложение сможет в любое время включать экран без явного согласия с вашей стороны [Preference]

### wifi_details.xml (9 nodes)
- PreferenceScreen [PreferenceScreen]
  - Подключение к Интернету [Preference]
  - IP-адрес [Preference]
  - MAC-адрес устройства [Preference]
  - Уровень сигнала [Preference]
  - Конфиденциальность [ListPreference]
  - Настройки прокси-сервера [com.android.settingslib.RestrictedPreference]
  - Настройки IP-адреса [com.android.settingslib.RestrictedPreference]
  - Удалить эту сеть [com.android.settingslib.RestrictedPreference]

### wifi_details_compat.xml (9 nodes)
- PreferenceScreen [PreferenceScreen]
  - Подключение к Интернету [com.android.tv.settings.compat.TsPreference]
  - IP-адрес [com.android.tv.settings.compat.TsPreference]
  - MAC-адрес устройства [com.android.tv.settings.compat.TsPreference]
  - Уровень сигнала [com.android.tv.settings.compat.TsPreference]
  - Конфиденциальность [ListPreference]
  - Настройки прокси-сервера [com.android.tv.settings.compat.TsRestrictedPreference]
  - Настройки IP-адреса [com.android.tv.settings.compat.TsRestrictedPreference]
  - Удалить эту сеть [com.android.tv.settings.compat.TsRestrictedPreference]

### write_settings.xml (2 nodes)
- Изменение системных настроек [PreferenceScreen]
  - Приложение сможет изменять системные настройки. [Preference]

### write_settings_compat.xml (2 nodes)
- Изменение системных настроек [PreferenceScreen]
  - Приложение сможет изменять системные настройки. [com.android.tv.settings.compat.TsPreference]

UI preference XML does not include Java-driven menus, uicustom floating menus or runtime hardware feature gates.
AOSP present class does not prove that every corresponding function was transferred from KuSettings.
ScreenServices and screenshot components are intentionally not ported; target 8054 requires independent testing.

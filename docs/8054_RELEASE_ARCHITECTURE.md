# Архитектура TvSettingsPlus 8054 v36

Девять Activity MOD наследуют оригинальный TvSettingsActivity и выбирают Fragment внутренним enum. Хост получает его через stock FlavorUtils → FeatureFactory → SettingsFragmentProvider. Контейнер, startup verification, тема из оригинального MainSettings, фокус, диалоги Leanback и переходы принадлежат штатному приложению. Выбор режима одной или двух панелей остаётся у оригинального flavor. Ни action, ни data, ни extras внешнего Intent не выбирают аппаратное действие.

Wi-Fi и приложения используют stock NetworkFragment и AppsFragment. Остальные разделы наследуют SettingsPreferenceFragment, создают настоящий PreferenceScreen в onCreatePreferences и связывают наблюдателей с onViewCreated/onDestroyView. Главный список передаёт штатному контейнеру имя Fragment. Доступен и отдельный запуск каждой Activity. Оболочка v35 и промежуточное копирование Preferences в TextView удалены.

Все 16 OEM DEX, штатные ресурсы, исходные Activity, Manifest handlers и подпись сохраняются. Новый DEX содержит только dev.projectorsoft.tvsettings. Тестовый API stock в компиляции и AndroidX-зависимости Gradle в него не входят. Прежний MainSettings остаётся доступен отдельно для проверки. Причина аппаратного сбоя v34 всё ещё требует AndroidRuntime Logcat.

Кнопки SETTINGS/TV_INPUT обрабатываются внутри активной MOD Activity. Глобальный владелец не менялся. Исходные keylayout exact показывают TV_INPUT для нескольких устройств. Без фактического input-device ID и foreground component нельзя доказать какой из них относится к пульту владельца.

[Трасса хоста, стандартных Fragment и переходов](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44691339983) · [Дерево](8054_RELEASE_MENU_TREE.md)

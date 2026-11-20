# v36 относительно v35

Артефакт v36 доступен в [Exact-8054](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44692995125/artifacts/14609353165). SHA-256 APK — `0f79bf8d4914a483ada782895ab75cf3effac831a47463bc4fa0ebaaf5883082`. [Отчёт и снимки тестовой среды Leanback](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44692995238/artifacts/14609895658).

- Штатные TvSettingsActivity, SettingsPreferenceFragment, Leanback, factory provider и theme. Девять Activity и восьмипунктовый корень. Ручной Layout и отражение UI через промежуточную таблицу удалены.
- Реальные Preference lifecycle, нативные списки, переключатели и ползунки. Network и Apps используют stock Fragment. Сохранён штатный MainSettings.
- HDMI standby различает изменение, отсутствие изменения, CEC OFF, устаревшее значение, отказ и неподтверждённое состояние. Источник и приложение при загрузке обслуживает один список на persist.trd.source_id.
- Автофокус при включении и движении переиспользует проверенный эквивалент двух штатных callbacks без создания контроллера. Критическая геометрия и motor gates сохранены. Добавлено mute медиа при наблюдаемом состоянии и наличии штатного разрешения.
- Sound использует штатные специализированные Fragment при поддержке, системный экран добавляет безопасную тепловую телеметрию. OEM Watchdog остаётся единственным активным владельцем.
- Проверки: ABI exact, оригинальный Manifest и 16 DEX, область определения новых классов, подпись, Leanback lifecycle на Android 13 и работа CEC listener с mock backend. Сравнение OEM оформления и аппаратных эффектов остаётся частью испытания владельца.

# Очистка пользовательского меню 8054

В v36 перечень входов задаёт NativeRootFragment. Это отделяет отображаемые пользовательские пункты от сохранённых OEM компонентов. Проверка наличия исходного XML и точных stock key ведётся по exact артефакту, универсальный ресурс сам по себе не подтверждает поддержку устройства.

| Что отсутствует в MOD | Известный исходный ресурс или узел | Зависимость и проверка |
|---|---|---|
| DVB, RF, LCN, кабель, спутник, DiSEqC | channels.xml, channels_scan.xml, channels_and_inputs_vendor.xml, channel_install_mode.xml; ChannelsActivity, ChannelsTwoPanalActivity, DiseqcTypeActivity | Строки отсутствуют в корне и в Sources. TvInputManager, TV Input services и LiveTV сохранены для HDMI. |
| OLED compensation, pixel shift, OFF-RS | partner_oled.xml; OEM PictureFragment / OLED gate | Исходные ресурсы и функции остаются в stock; собственного пункта LCD-проектора нет. |
| Retail, PQ/MEMC Demo | partner_retail_mode.xml, partner_mjc.xml; RetailModeActivity, RetailModeConfirmActivity | Демонстрационный режим исключён из дерева. Пользовательский MEMC сохранён как read-only там, где читается состояние. |
| HDMI 3/4, аналоговые входы | OEM SourceFragment/SourceSubMenuController и generic port metadata | В MOD только проверенные HW6/HW7. Дополнительные TV Input сервисы доступны stock. |
| Bluetooth speaker sink | tciv_source_bluetooth_speaker, OEM source menu | Нет пользовательской строки. Сопряжение и HID/AutoPair сохранены. |
| Заводские PQ/WB/UART | Отдельный Factory и vendor редакторы | Нет MOD вызовов заводских экранов, установок или калибровок. |
| Рекомендации и старые дубликаты | MainFragment suggestion container, OEM Settings/FloatMenu | Отдельный NativeRootFragment не создаёт эти строки. Старые классы и исходный MainSettings сохранены. |

Проверка — пройти восемь разделов новым ярлыком и сопоставить список с матрицей. Существующие компоненты физически не удалялись. Для возврата штатного дерева использовать отдельный MainSettings или подтверждённый штатный возврат к STOCK. [Тестовый протокол](8054_HARDWARE_TEST_PLAN.md)

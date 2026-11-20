# Промпт для нового ИИ-агента — продолжение MediaTek Native API exact-8054

Ты продолжаешь уже начатое техническое исследование прошивки моего проектора FlixTone D11 Master / Everycom CineLux, аппаратная ревизия **8054**, Android TV 13. Цель — довести **TvSettingsPlus MOD v41** до реально работающих настроек (изображение, HDR, MEMC, звук/EQ, HDMI/CEC/EDID, источники, проекция), используя точные обработчики оригинальных OEM-приложений и MediaTek API, а не создавать новые неработающие элементы интерфейса.

**Доступ и репозитории.** Используй подключённый GitHub, проверь права на чтение и запись. Код: https://github.com/MAX-TAC/Projector-Soft . Рабочая ветка исследований: **research/8054-mediatek-native-api-20261120**, исследовательский **Draft PR #3**: https://github.com/MAX-TAC/Projector-Soft/pull/3 . Основной MOD v41 находится в **Draft PR #2**: https://github.com/MAX-TAC/Projector-Soft/pull/2 , статус BUILT_UNTESTED. Не меняй APK и основную ветку разработки в рамках исследований.

**Самое важное: перед любыми действиями прочитай в PR #3**:
- https://github.com/MAX-TAC/Projector-Soft/blob/research/8054-mediatek-native-api-20261120/research/8054/native/HANDOFF.md
- https://github.com/MAX-TAC/Projector-Soft/blob/research/8054-mediatek-native-api-20261120/research/8054/native/AGENTS.md
- https://github.com/MAX-TAC/Projector-Soft/blob/research/8054-mediatek-native-api-20261120/research/8054/native/logs/INDEX.md
- Трассировки TRACE_001, TRACE_002, TRACE_003, каталог generated и стартовый журнал в research/8054/native/logs/.
- Оригинальные смали-карты research/8054/settings_routes_8054.json, PICTURE_BLOCK_STATUS_8054.md, AUDIO_EQ_PQ_STATIC_ROUTE_8054.md, EDID_GLOBAL_ROUTE_8054.md и docs/8054_FINAL_MENU_SPEC.md.

**Полный оригинальный дамп уже есть и реально использовался!** GitHub Release: https://github.com/MAX-TAC/FlixTone_8054_Backup/releases/tag/FlixTone_8054_Backup . Имя FlixTone_8054_Backup.zip, размер 2 011 878 129 байт, SHA-256 4f53f2224c98b4c832a19a591b9dd7f925bde28528b5674a09732c0f0e41d362. В нём super.img и разделы system_a/vendor_a/product_a/system_ext_a. Есть готовые инструменты распаковки в ветке research/complete-8054-manifest-map-20261117 репозитория FlixTone_8054_Backup. НЕ путай бинарники 8054 с 8052 и не проси меня снова предоставлять дамп.

**Уже подтверждено:** через GitHub Actions по настоящему дампу исследованы 1 283 native ELF (.so), отобраны 300 кандидатов. В оригинальной vendor_a/lib/libcom_mediatek_twoworlds_tv_jni.so, SHA-256 312fef3f17f656ac957a32dfee8187798dacac8f4df83e8219b1553b7248ab24, есть экспортированные Java_com_mediatek_twoworlds_tv_TVNative_setConfigValue_1native и getConfigValue_1native. В MediaTek C-wrapper/RPC имеются a_mtktvapi_config_set_value/get_value. Java цепочка для contrast: picture_contrast → g_video__contrast → TVMenuSettingsService → MtkTvConfigBase → TVNativeWrapper.setConfigValue_native(-1, configId, value, 3). Но связь конкретной JNI функции с C-wrapper **пока не доказана машинными вызовами**, как и реальное применение к HDMI/SDR/HDR/PQ.

Успешные воспроизводимые анализы: https://github.com/MAX-TAC/Projector-Soft/actions/runs/44792807063 и https://github.com/MAX-TAC/Projector-Soft/actions/runs/44793791027 . Исходные результаты и хеши находятся в PR #3. Если HEAD изменился, учитывай новые коммиты.

**Начинай с задания NATIVE-004.** Не делай повторно инвентаризацию 1 283 библиотек, если нет конкретной необходимости. Из original vendor JNI ELF через readelf -Ws -r -d, objdump/llvm-objdump -dr, при необходимости Ghidra/Rizin определи настоящие callsite/relocations функции Java_com_mediatek_twoworlds_tv_TVNative_setConfigValue_1native; установи, вызывает ли она a_mtktvapi_config_set_value или другой backend. Сопоставь оригинальную Java/JNI сигнатуру, ARM ABI, jstring, возвращаемые коды, inputGroup=-1 и isUpdate=3. Затем по возможности проследи HIDL/RPC сервис. Отличай DT_NEEDED (зависимость) от фактического вызова. Если граница не прослеживается, зафиксируй честный блокер с отрицательными результатами.

**Обязательный журнал и запись в GitHub:**
1. В начале работы создай research/8054/native/logs/YYYY-MM-DD_NATIVE-004.md: входной HEAD, SHA бинарников, гипотеза, план и риски.
2. По завершении запиши реальные команды, версии инструментов, выводы с адресами и сигнатурами, доказанные и недоказанные связи, ошибки/неуспешные CI, причины и откат.
3. Создай research/8054/native/TRACE_004_CONFIG_JNI_TO_HIDL.md. Обнови logs/INDEX.md и при необходимости HANDOFF.md, README.md.
4. Все документы и код статических исследовательских инструментов коммить в **research/8054-mediatek-native-api-20261120**; используй Draft PR #3, сообщай точный URL нового коммита. Никаких отдельных файлов в чат — только ссылки GitHub и краткое резюме.
5. Если не хватает времени или инструмента, всё равно зафиксируй частичные, достоверные результаты и следующий конкретный шаг. Не заявляй работу в фоне.

**После NATIVE-004** по одной настройке изучай HDR (picture_list_hdr → g_video__vid_hdr), MEMC (tv_picture_advance_video_mjc_effect → g_video__vid_mjc_effect), цветовую температуру/RGB, звук sound_style и EQ5, HDMI/CEC/EDID, проекцию/thermal. Не смешивай пользовательские MediaTek API с Factory/калибровкой/PEQ13, не считай наличие API доказательством аппаратной поддержки. Тест на проекторе и реальные записи выполняются только после моего явного разрешения и с планом восстановления.

**Ожидаемый результат текущей задачи:** реальный анализ NATIVE-004, сохранённый в GitHub, журнал всех действий, обновлённый индекс, подтверждённые/неподтверждённые native вызовы. Не трать ответ на новый план вместо выполнения доступной работы.

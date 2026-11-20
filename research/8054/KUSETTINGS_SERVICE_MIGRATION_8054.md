# KuSettings 8054 — перенос фоновых зависимостей

В v35 единственный действующий владелец восстановления — штатный KuSettings. Переиспользованы KuSourceRecoveryBootReceiver и KuSourceRecoveryService, зарегистрированные с enabled=false и exported=false. У MOD receiver отсутствует фильтр загрузки. Даже явное отключение KuSettings на 8054 не проходит KuSourceRecoveryActivationPolicy.

| Точная цепочка 8054 | Назначение и подготовленная замена |
|---|---|
| com.my.service.BootupReceiver.onReceive → com.my.utils.IntentUtils.startTime(Context) | Фильтрует BOOT_COMPLETED. startTime через явный Intent и startService запускает MySetTimeService. Приватный receiver MOD пока не подписан на загрузку. |
| MySetTimeService.onStartCommand → registerTimeTick() | Динамическая подписка TIME_TICK. MySetTimeService$1.onReceive вызывает IntentUtils.startSourceService. Сервис MOD уже содержит единственную подписку, отложенный снимок и снятие подписки при остановке. |
| IntentUtils.startSourceService → KuSourceRJ.TimeService | MOD знает точный целевой компонент. Для запуска нужны подтверждённая доступность и отсутствие TimeService, установленное по снимку, который видит сервисы самого KuSourceRJ. Попытки ограничены по частоте. При неоднозначности запуск пропускается. |
| KuSourceRJ.TimeService и собственные boot-компоненты | Остаются действующими. Термологика, мотор, датчики, fan HAL и калибровки принадлежат OEM. |

Состав компонентов установлен по точному Manifest. BOOT_COMPLETED, вызов startTime и динамический TIME_TICK подтверждены инструкциями 8054. Отсутствие службы в частичном результате getRunningServices не доказывает её остановку. Перед передачей владения нужны точные разрешения и видимость чужой службы, поведение после аварийного завершения и cold boot. [Трасса 8054](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44393750865/job/135118017636) · [Карта KuSettings](KUSETTINGS_DEPENDENCIES_8054.md)

BOOT_COMPLETED относится к исключениям из ограничений доставки неявных broadcast. Возможность запустить фоновый Service проверяется отдельно. Учитывать ограничения Android 13, targetSdk 30, original permissions и android.uid.system. Совместимая подпись и shared UID не устанавливают, что снимки служб полны. [Фоновые службы Android](https://developer.android.com/about/versions/oreo/background) · [Исключения для broadcast](https://developer.android.com/develop/background-work/background-tasks/broadcasts/broadcast-exceptions)

Включение недоступно в v35. Для отдельного испытания нужен новый согласованный вариант сборки, проверка единственного владельца, ограничение попыток до безопасного бюджета и подтверждённый путь возврата. Деактивация нового маршрута возвращает оба MOD-компонента в состояние v35. ScreenServices, ScreenshotActivity и Bluetooth speaker sink не переносятся. KuSetupWizard, RuiYunOta, trdservice/trdmiddle, RuiAutoFocus и KeystoneCorrection сохраняются.

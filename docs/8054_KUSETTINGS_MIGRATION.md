# KuSettings и фоновые службы 8054 v36

Полная цепочка exact 8054 и Android 13 ограничения приведены в предыдущем отчёте. В v36 остановка OEM KuSettings не является способом скрытия интерфейса. Существующие KuSourceRecoveryBootReceiver и KuSourceRecoveryService включены как исходный код и объявлены disabled/non-exported без BOOT фильтра. На 8054 программный activation gate также закрыт. Владелец восстановления при обычной эксплуатации — исходная цепочка KuSettings и KuSourceRJ. Сохранены TimeService, AutoPair, RuiAutoFocus, KeystoneCorrection и trdservice/trdmiddle.

Для отдельного испытания нужна сборка с согласованным возвратом к исходному владельцу, точное подтверждение visible и enabled состояния TimeService и возможность отличать остановку от неполной видимости. Сначала снять данные cold boot, затем согласовать переход на одного восстановителя и безопасный предел повторных запусков. Android UID и подпись сами по себе не подтверждают полноту running-services. Открытие MOD не включает watchdog и не меняет параметры сна.

[Точная цепочка Bootup → MySetTimeService → TimeService](../research/8054/KUSETTINGS_SERVICE_MIGRATION_8054.md)

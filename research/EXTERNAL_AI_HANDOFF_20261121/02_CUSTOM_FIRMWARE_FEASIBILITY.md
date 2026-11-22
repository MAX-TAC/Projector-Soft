# 02. Кастомная прошивка RUIOS: что исследовано, что реально возможно, что заблокировано

## СРОЧНАЯ КОРРЕКТИРОВКА 21.11.2026 — владелец реально разблокировал загрузчик FlixTone 8054

**Этот раздел имеет приоритет над более ранними оценками ниже.** На живом устройстве владелец в Windows Fastboot выполнил `fastboot flashing unlock`, получил `OKAY` и независимо подтвердил переход `fastboot getvar unlocked: no → yes`, `fastboot getvar secure: yes → no`. `fastboot getvar current-slot=a`, `slot-count=2`, `is-userspace=no`, версия MBOOT Fastboot `01.01.260905`. `fastboot flashing get_unlock_ability` вернул `invalid command` (этот запрос не поддержан), но **не** блокировал реальную операцию unlock; `fastboot oem unlock` тоже не поддерживается (и не требуется). **Важнейший прежний неопределённый аппаратный blocker — возможность unlock — снят реальным устройством.**

**Исправление старой интерпретации:** собственный OEM AVB RSA4096 private key не обязательно нужен для эксперимента с custom Android image в режиме `UNLOCKED`, поскольку нормальная AOSP Verified Boot state machine допускает загрузку OS не подписанного OEM root of trust. Этот вывод следует из официальной AOSP [Device state](https://source.android.com/docs/security/features/verifiedboot/device-state) и [Lock/unlock bootloader](https://source.android.com/docs/core/architecture/bootloader/locking_unlocking), но **точная реализация AVB/device tree/TEE на этом 8054 ещё не исследована после unlock**. `secure:no` — Fastboot флаг, **не** доказательство отключения всех уровней проверки или запуска произвольного `super.img`.

**Полный ROM теперь технически более реалистичен, чем ранее оценивалось**. Запрет на саму возможность custom flashing был необоснованным. Однако **успешная загрузка конкретного custom image, наличие рабочего recovery и обратимость ошибок пока не проверены**. В частности, из user listing `C:\Users\a804483\Downloads\d11_bk` видно 24 partition images (493 879 296 bytes): `boot_a/b`, `mboot_a/b`, `dtbo_a/b`, `vbmeta_a/b`, `vbmeta_system_a/b`, `optee_a`, `tvconfig_a`, `factory_a_a/b`, `persist`, `eeprom_a`, `private`, `tvcertificate`, `project_id` и др. **В этом каталоге нет `super.img`, `tvservice_a/b`, `linux_rootfs_*`, `userdata`;** отдельный FlixTone backup release мог содержать `super.img`, но это должно быть сведено в единый конкретный рестор-пакет с SHA/slot/калибровками, а не молчаливо считаться полным. Не публиковать в открытом GitHub `private`, `tvcertificate`, `eeprom`, `factory`, `persist`, `project_id`.

## 21.11.2026 — локальный `super.img` реально имеется, 2 225 078 272 bytes, SHA256

**Владелец подтвердил на Windows локальную резервную копию:** `C:\Users\a804483\Downloads\FlixTone_8054_Backup\super.img`, дата в dir 16.11.2026 17:38, точный размер **2 225 078 272 bytes**, что совпадает с physical super в `lpdump` и `fastboot getvar partition-size:super=0x84a00000`. `certutil -hashfile` показал `ac16996dddaa7c8f5e5429a0736f73d18a12df7dc6b58a4e89a475f5af0c5a02`. **Локальный файл не загружен в этот чат и его байты не проверялись моделью**. Совпадение размера — убедительный аргумент за RAW full super dump, но пока не проверены LP geometry/header, EROFS sig, CRC/metadata tables, независимая идентичность с текущей флеш-памятью и recovery.

**Следующий безопасный тест — прочесть 4-байтовые сигнатуры в файле средствами Windows PowerShell, без изменения eMMC:**
- offset 0: sparse `3A-FF-26-ED` или RAW (обычно не sparse)
- offsets 4096/8192: LP geometry `67-44-6C-61` (`0x616c4467`, little endian)
- offset 12288: LP metadata header `30-50-4C-41` (`0x414c5030`)
- EROFS: sector start×512 +1024 from live lpdump: system `1049600`, vendor `857736192`, product `1120928768`, system_ext `1198523392` — expected `E2-E1-F5-E0` (`0xE0F5E1E2`). These offsets are exact only if backup's LP layout matches live `lpdump`; mismatch needs offline lpdump before concluding corrupted backup. [LP format](https://android.googlesource.com/platform/system/core/+/master/fs_mgr/liblp/include/liblp/metadata_format.h), [EROFS](https://erofs.docs.kernel.org/en/latest/ondisk/core_ondisk.html).

**Corrected engineering statement:** AOSP permits flashing a factory `super.img` directly to physical super from bootloader fastboot when supported by device/flash utility, whereas individual `system_a/vendor_a/product_a/system_ext_a` logical partitions generally use fastbootd. Don't blindly assume `fastbootd` is the only restoration mechanism for a full physical super. [AOSP Implement dynamic partitions](https://source.android.com/docs/core/ota/dynamic_partitions/implement). Owner's bootloader `max-download-size=0x3df00000` is smaller than raw full-super, so transfer chunking/sparse support must be validated before a write. Do **not** flash or change active slot in this diagnostic phase.

## 21.11.2026, 22:24 — BOOT HAL 1.2, штатный Android A успешен, B bootable-but-not-successful

Владелец на exact-8054 выполнил read-only:
- `bootctl hal-info`: `android.hardware.boot@1.2::IBootControl`
- `bootctl get-number-slots=2`, `bootctl get-active-boot-slot=0` (A)
- `bootctl get-snapshot-merge-status=none`
- `bootctl is-slot-bootable 0` → `rc=0`; `is-slot-marked-successful 0` → `rc=0`
- `bootctl is-slot-bootable 1` → `rc=0`; `is-slot-marked-successful 1` → `rc=70`
- `ro.virtual_ab.enabled=true`, `ro.virtual_ab.compression.enabled=true`, `ro.virtual_ab.userspace.snapshots.enabled=true`, `ro.virtual_ab.io_uring.enabled=true`, `sys.boot_completed=1`, `vendor.mtk.boot_completed=1`. `init.svc.apexd-snapshotde=stopped` нормален сам по себе.
- `dmctl list devices -v` и `dmctl table system-verity` shell UID → Permission denied; не повторять без необходимости.

**Семантика exit 70 проверена по оригинальному AOSP `bootctl.cpp`:** `handle_return(Return<BoolResult>)` возвращает `EX_OK` лишь при `BoolResult::TRUE`, а `EX_SOFTWARE` (70) при `FALSE` или `INVALID_SLOT` либо IPC-ошибке. Для **валидного B (slot 1 при numberSlots=2) и без stderr** `rc=70` практически означает `isSlotMarkedSuccessful(B)=FALSE`; в сочетании с `isSlotBootable(B)=TRUE` нельзя утверждать, что B реально содержит полный Android. [AOSP bootctl.cpp](https://android.googlesource.com/platform/system/extras/+/d0655ccfbe939988f6ca32c508d9576cc7df4ee7/bootctl/bootctl.cpp#134), [Virtual A/B](https://source.android.com/docs/core/ota/virtual_ab).

**Проверенные статусы:** `A: bootable=true/successful=true/running`; `B: bootable=true/successful=false/no LP system_b extents in either LP metadata slot`. `Merge status=none` означает Boot HAL не сообщает текущий merge; это не тест вручную установленных images. Слот B нельзя использовать как безопасный fallback через set_active.

**Следующий этап — не новая серия повторных диагностик, а проверка доступности restore material:**
1. Сверить полный локальный набор original exact-8054 образов и их SHA256. В пользовательском `C:\Users\a804483\Downloads\d11_bk` 24 images суммарно 493 879 296 B, без `super.img`, `tvservice_b` и некоторых других. В repository release `MAX-TAC/FlixTone_8054_Backup` известен архив `FlixTone_8054_Backup.zip` размером 2 011 878 129 B; **состав должен быть проверен**, архив не равен автоматически полноценной emergency restore.
2. Получить **реальный** `super.img` того же exact build и желательно обе физические boot/vbmeta images + проверенный live recovery path; хранить private/cert/calibration backups локально, не публиковать.
3. Только потом решать, какие LP images, EROFS, VBMeta/AVB и SELinux пересобирать, и способен ли bootloader/fastbootd принимать images.
4. Никакого `set_active b`, `fastboot flashing lock`, `flash/erase`, заводского reset, отключения verity до выполнения restore prerequisites.

**Статус:** `OWNER_BOOTCTL_HAL_1_2_VERIFIED / A_SUCCESSFUL / B_BOOTABLE_NOT_SUCCESSFUL / NO_MERGE / FULL_RECOVERY_NOT_VERIFIED`.

## 21.11.2026 — LP metadata slot 1 / Virtual A/B и ограничения shell

**Новое аппаратное evidence, 22:32–22:33 local:** `lpdump --slot 1 /dev/block/by-name/super` успешно прочитан и **совпадает со slot 0**: metadata v10.2, 3 slots metadata (не boot slots), `virtual_ab_device`, все 4 logical A с прежними extents, logical `system_b/vendor_b/product_b/system_ext_b` **без extents**. Следовательно, сейчас обе LP metadata records не подтверждают готовую B system; `fastboot set_active b` **не использовать** для пробы.

`ro.virtual_ab.enabled=true`, `ro.virtual_ab.userspace.snapshots.enabled=true`, `ro.virtual_ab.compression.enabled=true`. Пустое значение `init.svc.snapuserd` означает лишь, что это init property не предоставило состояния службы. Не делать вывод «snapuserd не установлен» или «OTA выключено». Android 13 Virtual A/B compression uses COW/userspace snapshot and merge ([AOSP](https://source.android.com/docs/core/ota/virtual_ab)).

`bootctl get-current-slot` выдал `0`; следующие 4 команды `is-slot-bootable 0/1`, `is-slot-marked-successful 0/1` не дали видимого вывода. **Нельзя интерпретировать это как true/false** без проверки exit status отдельной командой и выявления Boot HAL behavior. По [AOSP bootctl source](https://android.googlesource.com/platform/system/extras/+/main/bootctl/bootctl.cpp) bool-check имеет значение return status; vendor build может отличаться. `dmctl list devices -v` и `dmctl table system-verity` оба возвращают `Failed to open device-mapper: Permission denied` с shell UID; это ожидаемое ограничение, **не** признак сломанной dm-verity. Не требовать root только ради чтения таблиц.

**Следующая минимальная диагностика без изменения устройства:**
```bat
adb shell "bootctl hal-info; bootctl get-number-slots; bootctl get-active-boot-slot; bootctl get-snapshot-merge-status"
adb shell "for n in 0 1; do echo SLOT=$n; bootctl is-slot-bootable $n; echo bootable_rc=$?; bootctl is-slot-marked-successful $n; echo successful_rc=$?; done"
adb shell "getprop | grep -Ei 'virtual_ab|snapshot|snapuserd|slot_suffix|boot_completed' "
adb shell "ls -l /dev/block/by-name | grep -E 'super|boot_[ab]|vbmeta_[ab]'"
```
**Предупреждение CMD shell quoting:** внутри команд в двойных кавычках переменные `$n`, `$?` раскрываются *на Android shell*, не cmd.exe. Код возврата 0 для bool в типичном AOSP означает true, ненулевой — false/ошибка, трактовать вместе со stderr. `get-active-boot-slot` может быть не поддержан vendor HAL: не ошибка пользовательского окружения. Не запускать state-changing `set-active`, `set-snapshot-merge-status`, `fastboot flash` и relock.

**Статус:** `UNLOCKED_ORANGE_STOCK_BOOT_OK / VIRTUAL_AB_COMPRESSION_TRUE / LP_SLOT0_SLOT1_IDENTICAL / EMPTY_B_EXTENTS / B_BOOTCTL_STATUS_UNKNOWN / DMCTL_SHELL_DENIED`.

## 21.11.2026 — LP metadata /dm mounts: Virtual A/B, но B logical partitions пусты

**Владелец получил реальный `lpdump --slot 0 /dev/block/by-name/super` на разблокированном, нормально загруженном Android 13 exact-8054.**

- LP metadata `10.2`, metadata slot count `3` (**не** три загрузочных слота), header flag `virtual_ab_device`; physical `super` size **2 225 078 272 B**.
- `group_oem_a` и `group_oem_b`: каждый max **2 220 883 968 B** (не могут быть одновременно заполнены до максимума).
- Реальные extents только у A: `system_a` 1 671 432×512=**855 773 184 B** (816,13 MiB); `vendor_a` 513 016×512=**262 664 192 B** (250,50 MiB); `product_a` 150 696×512=**77 156 352 B** (73,58 MiB); `system_ext_a` 170 496×512=**87 293 952 B** (83,25 MiB).
- Сумма A extents **1 282 887 680 B** (1 223,46 MiB). Простая разница `super−used` **942 190 592 B** (~898,54 MiB), **не** гарантированное свободное пространство для добавления — есть зарезервированные blocks/выравнивание/LP metadata. Предел `group_oem_a−used` **937 996 288 B** (~894,54 MiB).
- `system_b`, `vendor_b`, `product_b`, `system_ext_b` перечислены, но **не имеют extents** в LP metadata slot 0. Для Virtual A/B это штатная возможность ([AOSP overview](https://source.android.com/docs/core/ota/virtual_ab)). **Не полагаться на B как на заведомо рабочий fallback**; физические `boot_b` и `vbmeta_b` не обеспечивают полноценный Android B.
- Реальные mount points: `/dev/block/dm-4 /` (EROFS RO, System-as-root); `dm-5 /vendor` (EROFS RO), `dm-6 /product` (EROFS RO), `dm-7 /system_ext` (EROFS RO). Отдельно `dm-9 /mnt/vendor/tvservice` (ext4 RO), `dm-10 /mnt/vendor/factory` (ext4 RO), `dm-11 /mnt/vendor/linux_rootfs` (squashfs RO), `dm-12 /mnt/vendor/3rd` (squashfs RO), `dm-13 /mnt/vendor/bootdata` (ext4 RO); `dm-8 /data` F2FS RW.
- `/sys/block/dm-*/dm/name`: `dm-0..3` — `system_a/vendor_a/product_a/system_ext_a`, `dm-4..7` — соответствующие `*-verity`, `dm-9..13` — OEM отдельные `*-verity`. Такая схема показывает **активную runtime integrity mapping**: UNLOCKED/ORANGE не выключил слой проверки автоматически.
- `lpdump --slot 1` **ещё не проведён**, как и проверка `bootctl is-slot-bootable 1`/`is-slot-marked-successful 1` или snapshot merge state. Нельзя объявлять B usable/unusable только по slot 0, но slot 0 уже показывает, что physical Android B в текущем состоянии не гарантирован.

**Следующий безопасный read-only пакет:**
```bat
adb shell "lpdump --slot 1 /dev/block/by-name/super 2>&1"
adb shell "getprop ro.virtual_ab.enabled; getprop ro.virtual_ab.userspace.snapshots.enabled; getprop ro.virtual_ab.compression.enabled; getprop init.svc.snapuserd"
adb shell "command -v bootctl; bootctl get-current-slot; bootctl is-slot-bootable 0; bootctl is-slot-bootable 1; bootctl is-slot-marked-successful 0; bootctl is-slot-marked-successful 1"
adb shell "dmctl list devices -v 2>&1"
adb shell "dmctl table system-verity 2>&1"
```
**Никаких `bootctl set-active-boot-slot`, `fastboot set_active`, `fastboot flash`, `fastboot flashing lock` до доказанного full recovery.** Актуальный каталог local `d11_bk` содержит лишь 24 отдельных backup image, **не** `super.img` — наличие старой 8054 backup ZIP в отдельном release не доказывает, что она соответствует текущим блокам после unlock. Не публиковать личные калибровки, `tvcertificate`, `private`, `persist`, `eeprom`.

**Вывод по проекту:** для **самой пользовательской программы TvSettingsPlus** минимально рискованна подписанная APK-подмена в сохранённой OEM привилегированной системе, а не пересборка всего `super`. Цель полноценного custom firmware теперь обоснованна, но для изменения системного EROFS слоя потребуются корректные image build + LP метаданные + SELinux + AVB / dm-verity и проверяемый откат. [AOSP Dynamic partitions](https://source.android.com/docs/core/ota/dynamic_partitions/implement), [Virtual A/B](https://source.android.com/docs/core/ota/virtual_ab), [fastbootd](https://source.android.com/docs/core/architecture/bootloader/fastbootd).

## 21.11.2026 — аппаратно подтверждены AVB enforcing + dynamic partition/EROFS после unlock

**Данные из второго read-only ADB обхода после успешного загрузочного теста:**
- `ro.boot.veritymode=enforcing`, `ro.boot.vbmeta.hash_alg=sha256`, `ro.boot.vbmeta.digest=5d67f5b378cb61ff624fffb80ccb1ab435663efb9dc9bbecb7786e84d8148c2a`. `ro.boot.super_partition` не определено. Это **не** доказательство отключённой dm-verity в unlocked state.
- Физический `super -> /dev/block/mmcblk0p29`; `boot_a/b -> mmcblk0p24/p25`, `tvservice_a/b -> mmcblk0p12/p13`, `vbmeta_a/b -> mmcblk0p31/p32`, `vbmeta_system_a/b -> p33/p34`, `optee_a/b -> p35/p36`, `mboot_a/b -> p1/p2`. Два слота, активный `_a`.
- `/dev/block/mapper` реально содержит `system_a→dm-0`, `vendor_a→dm-1`, `product_a→dm-2`, `system_ext_a→dm-3`; отдельные `system-verity→dm-4`, `vendor-verity→dm-5`, `product-verity→dm-6`, `system_ext-verity→dm-7`. Также `userdata→dm-8`, `tvservice-verity→dm-9`, `factory-verity→dm-10`, `linux_rootfs-verity→dm-11`, `3rd-verity→dm-12`, `bootdata-verity→dm-13`.
- В `/proc/mounts` зафиксированы `/dev/block/dm-5 /vendor erofs ro`, `/dev/block/dm-6 /product erofs ro`, `/dev/block/dm-7 /system_ext erofs ro`. Предыдущий grep искал только `/system` (не `/`), поэтому отсутствие /system **не доказательство отсутствия system_a**: system-as-root нужно проверить через `/proc/mounts` отдельно.
- Названия device-mapper и реально смонтированные `-verity` dm nodes подтверждают активную схему device mapper integrity; точные load tables, tree semantics и реакция на порчу не испытывались. `enforcing` здесь OEM-свойство, и нельзя автоматически отождествлять его с единственным AOSP значением `eio`/`restart` error behavior.

**Решение:** перестать считать custom firmware невозможной; одновременно **не** утверждать, что unlocked state автоматически разрешает записать bytes в EROFS или любой vbmeta/super. Соблюдать backup/recovery gates и сначала прочитать `lpdump --slot 0 /dev/block/by-name/super` (если установлен) и фактические dm mount mappings. Для изменения rootfs нужна пересборка EROFS + учёт AVB; для целого `super.img` — понимание LP metadata и размера physical super. Прошивка отдельных logical images через **bootloader fastboot** не гарантируется: см. [AOSP dynamic partitions](https://source.android.com/docs/core/ota/dynamic_partitions/implement). Отдельно проверить наличие userspace fastbootd; никакого `fastboot flash`, erase, vbmeta disable, relock сейчас не выполнять.

**Статус:** `BOOTLOADER_UNLOCK_CONFIRMED / STOCK_BOOT_AVB_ORANGE / VERITY_MAP_ACTIVE / SUPER_LOGICAL_A_OBSERVED / EROFS_RO_CONFIRMED / CUSTOM_BOOT_UNTESTED`.

**РЕЗУЛЬТАТ СЛЕДУЮЩЕГО ТЕСТА — подтверждён владельцем, 21.11.2026:** после `fastboot flashing unlock` заводская Android 13 действительно загрузилась. Прочитаны `ro.boot.vbmeta.device_state=unlocked`, `ro.boot.verifiedbootstate=orange`, `ro.boot.slot_suffix=_a`, `ro.rjpj.machine.id=8054`, `ro.build.display.id=mt5873_cd-userdebug 13 TP1A.220624.021.A1 eng.suhaix.20260905.031903 test-keys`; `ro.boot.flash.locked` пустое/не экспортировано. По [официальной AOSP документации о device state](https://source.android.com/docs/security/features/verifiedboot/device-state) `orange` — штатное состояние boot после unlock; на обычной модели AVB bootloader продолжает boot OS, даже не подписанной OEM root-of-trust. **Новое аппаратное доказательство:** `STOCK_ANDROID_BOOTS_AFTER_UNLOCK / AVB_ORANGE_CONFIRMED`. **Не** является доказательством `CUSTOM_BOOT_SUCCESS`, отключения всех проверок MBOOT/OP-TEE или flashability произвольного `super`. `userdebug`/`test-keys` обозначают тип сборки/подпись ОС и **не** идентичны AVB root public key. Старый текст ниже о необходимости ещё проверить boot Android после unlock уже исполнен; любые дальнейшие шаги должны учитывать этот факт.

**Вопрос следующего read-only теста:** штатный Android по-прежнему загружается после unlock? AOSP обычно стирает user data на unlock; на этом OEM wipe **не подтверждён** до проверки. Только если устройство уже готово к обычному запуску, выполнить `fastboot reboot` и после Android boot прочитать `adb shell getprop ro.boot.vbmeta.device_state`, `ro.boot.verifiedbootstate`, `ro.boot.flash.locked`, `ro.boot.slot_suffix`, `ro.rjpj.machine.id`. Не проводить `fastboot flashing lock`, `fastboot flash`, стирание разделов или `--disable-verity/--disable-verification` без отдельных проверок и recoverable backup.

**Статус:** `OWNER_FASTBOOT_UNLOCK_CONFIRMED / OEM_AVB_IN_UNLOCKED_MODE_UNVERIFIED / MODIFIED_IMAGE_BOOT_UNTESTED / RECOVERY_UNPROVEN`.

**Срез:** 2026-11-21. **Не является инструкцией по прошивке.** Во всех имеющихся отчётах о Custom v0 содержатся офлайн-эксперименты, но **нет доказанной, безопасно устанавливаемой кастомной RUIOS**. Отдельная модернизация `TvSettingsPlus.apk` — существенно менее рискованный продуктовый путь.

## 1. Архитектура RUIOS

```text
BootROM / MBOOT(U-Boot) / OP-TEE / AVB
  ├── boot_a/b, dtbo_a/b, vbmeta_a/b, vbmeta_system_a/b
  ├── Android dynamic super → system/vendor/product/system_ext (EROFS)
  │     ├── TvSettingsPlus (com.android.tv.settings)
  │     ├── KuSettings (com.my.settings)
  │     ├── KuSourceRJ, RuiAutoFocus, KeystoneCorrection, OTA, launcher
  │     └── Android framework + MediaTek JNI/HAL
  ├── отдельные tvservice, tvconfig, linux_rootfs, bootdata
  └── persist / factory / eeprom / private / project_id / калибровки (не перезаписывать)
```

Семейство SoC в URL OTA обозначено `mt9660`, но у Android OTA `pre-device=mt5873`, инженерные описания платы используют `MT5873`. Это **не повод механически выбирать телефонные MediaTek flashing tools** и не доказательство полной совместимости плат. Ветка `vendor.mediatek...` и аппаратные фабричные библиотеки — отдельная подсистема относительно Android UI.

[Схема сервисов/прошивки](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/blob/stock-custom-architecture-8052-8054-20261021/analysis/Global_Firmware_Map_20261021/GLOBAL_COMPONENT_ARCHITECTURE_RU.md) · [сравнение железа](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/blob/stock-8052-8054-deep-audit-20261021/analysis/HDMI_Comparison/UNIVERSAL_ROM_ASSESSMENT_RU.md).

## 2. Заводской PKG: расшифровка не равна возможности прошивки

**Подтверждено офлайн:**

1. Оба заводских `upgrade_image.pkg` относятся к **MStar Secure New**; найден общедоступный стандартный AES-256-CBC ключ `MSTAR_SECURE/default`; расшифровка и LZ4 восстановление выполнены. Извлечены Android 13 логические разделы из `super`, Android EROFS, отдельные сервисные образы.
2. Извлечение и обратное шифрование на уровне AES — это только container transformation. У оригинального PKG присутствуют отдельные области с потенциальной аутентификацией, правила подписи и OEM private key **не подтверждены**.
3. В заводском upgrade-script присутствуют разрушительные операции: `gpt clean mmc 0`, `wipeuserdata`, `partition erase mmc 0 persist`, `mmc erase.p eeprom_a`. Слепой запуск оригинального, тем более чужого PKG может уничтожить настройки и калибровки.
4. Одинаковая GPT и общие 3944 файла между стоками 8052/8054 **не являются** разрешением на подмену boot/DTBO/PMU/панельных конфигов.

[PKG decrypt](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/blob/main/analysis/Original_Firmware/DECRYPTED_SUCCESS.md) · [Engineering verdict](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/blob/main/analysis/HDMI_Comparison/ENGINEERING_VERDICT.md) · [PKG reconstruct logs](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/tree/main/analysis/Original_Firmware/reconstructed).

## 3. OTA подпись, AVB и реальная граница доверия

| Уровень | Что доказано | Что **не** следует считать доказанным |
|---|---|---|
| Android APK signer | Значительная группа OEM/Android APK подписана AOSP **platform test-key**, fingerprint cert `C8A2E9BCCF597C2FB6DC66BEE293FC13F2FC47EC77BC6B2B0D52C11F51192AB8`. Возможна пересборка того же привилегированного APK с проверкой подписи. | Обновление обязательно установится поверх device APK, shared UID/privapp/SELinux не заблокируют runtime, либо это production-secret OEM key. |
| OTA 8052/8053 | Оригинальные `payload.bin` CrAU v2; RSA2048/SHA256 metadata signatures проверены AOSP `testkey.x509.pem` (не то же самое, что AVB). | Что OEM updater примет **кастомный OTA**, аппаратная база и slot верно выбраны, а загрузчик примет модифицированные разделы. |
| `vbmeta_a`, `vbmeta_system_a` | Заводские SHA256_RSA4096 (`algorithm=2`) и публичный AVB ключ с SHA256 **`dcf38badcd7784d2aa892739eba5e239a649362edb03586a455b3f8ded046cfe`**; ключ совпал в сравниваемых заводских 8052/8054 и в поздней OTA 8052. | Наличие закрытого ключа RSA4096, возможность подписать свои изменения под OEM root of trust. |
| AOSP test AVB key | Сравнён публичный AOSP RSA4096; его бинарный fingerprint **`7728e30f50bfa5cea165f473175a08803f6a8346642b5aa10913e9d9e6defef6`** **не совпадает** с OEM ключом. | Что другой тестовый ключ или альтернативный boot path гарантированно поддерживаются. |
| U-Boot mboot 8052 | Статический Ghidra-аудит нашёл код бинарного сравнения `AvbRSAPublicKey` 1032 байта с OEM ключом, AvbOps callback, `avb init 0`. На живом **8052** reported `locked/green`. | Что все ветки `avb_slot_verify` при каждом boot завершаются именно отказом для custom key; соответствующий живой 8054 режим пока отдельно не измерен. |
| OEM secure boot / OP-TEE | Есть образы и конфигурация, но цепь доверия сложнее Android AVB. | Возможность собственного MBOOT/OP-TEE или безопасного обхода подписи. |

Ключи **разных слоёв нельзя отождествлять**. Расшифровка AES PKG, подпись APK platform testkey, подпись OTA AOSP testkey и trust anchor AVB — четыре разные операции. Отдельно должен проверяться rollback index, enforcement и реальный метод восстановления.

Источники: [AVB sample comparison](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/blob/main/analysis/HDMI_Comparison/AVB.md), [OTA verification](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/blob/main/analysis/RUIOS_OTA_Comparison/ENGINEERING_SUMMARY_RU.md), [AVB key comparison](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/blob/research-ota8052-avb-key-match-20261021/analysis/8052_Live_ADB_20261021/AVB_KEYS_RESEARCH_RU.md), [mboot disassembly](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/blob/research-ota8052-avb-key-match-20261021/analysis/AVB_Chain_RE_8052/README_RU.md).

## 4. Две задуманные кастомные конфигурации, не один сомнительный «универсальный» образ

Исходный дизайн **Custom v0**: одна общая кодовая база и патчи пользовательских приложений, но **отдельные аппаратные baselines 8052 (1 HDMI) и 8054 (2 HDMI/ToF)**. 8053 не является целевой платой.

Планируемый набор изменений (часть **планы**, а не реализованные flashing images):
- заменить ZEASN `zxlauncher` на **FLauncher 0.18.0**, убрать `zxstore`, сохранить YouTube; в 8054 `preinstall_del` планировалось исключить Netflix и Prime Video;
- добавить собственный `org.cinelux.settingsshortcut`, запускающий **явный** `com.android.tv.settings.MainSettings`, не `android.settings.SETTINGS` (последний на стоке может попасть в KuSettings);
- сохранить `KuSetupWizard`, `KuSourceRJ`, `RuiAutoFocus`, `KeystoneCorrection`, `trdservice`, `trdmiddle`, `TvServices`, `SystemUI`, `Factory`, `RuiYunOta`, `AutoPair`, `update_engine`;
- **KuSettings оставить** до переноса `MySetTimeService` и зависимостей; не смешивать программный debloat и физическую калибровку;
- сохранить `boot`, `vendor`, `dtbo`, `optee`, `vbmeta`, заводские data/калибровки **по профилям**.

### Что действительно собиралось

[Custom v0 stage workflow SUCCESS](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/actions/runs/39626053724) подготовил **два NONFLASHABLE APK overlay ZIP**, а **не** EROFS images, `super.img`, `payload.bin`, USB BIN или заводской PKG:

| Профиль | Staging SHA256 (внутренний ZIP) | Результат |
|---|---|---|
| 8052 | `f23ec4625c92cee82c8b34656e78bbc1d9bc5b5ac5811e74ac1e6a0d3a4e9db7` | Файлы FLauncher/SettingsShortcut + manifest, removals |
| 8054 | `305e752332f88706a74f6b50f51bf966441651cc5e2a3ca837ff2867c164bb41` | То же, c 8054 stock APK hashes |

Исходный FLauncher 0.18.0 SHA256 `376fc528c880cdf7c7c04b2d3708c69f911144498218babb6300808b772af32e`; staging содержит лицензионные сведения и provenance. Разработанный shortcut подписан временным development key. **Не утверждать, что готовый APK уже заменил HOME на проекторе**.

Источники: [Custom v0 README](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/blob/custom-v0-staging-8052-8054-20261021/custom-v0/README_RU.md), [результат сборок](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/blob/custom-v0-staging-8052-8054-20261021/custom-v0/BUILD_STATUS_RU.md).

### Дополнительные идеи, без релизного внедрения

В ветках Custom v0 подготовлены исходники/макеты для раннего Android TV логотипа 1080p/4K, нового `bootanimation.zip` и бесшумного startup MP3; **финальная прошивка/flashable package не создана**. Есть идея read-only thermal screen с показом SoC/CPU, но без подтверждённого RPM или LCD/LED-показания; нельзя подменять ею оригинальный cooling policy.

[Boot visuals PREPARE](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/blob/custom-v0-branding-debloat-ota-design-20261021/custom-v0/BOOT_VISUALS_STAGE_RU.md) · [Thermal screen research](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/blob/custom-v0-readonly-thermal-ui-20261021/custom-v0/THERMAL_SCREEN_RU.md).

## 5. Лестница доступных способов доставки

| Путь | Оценка по имеющимся фактам |
|---|---|
| Модификация `TvSettingsPlus` APK с совпадающим platform signer, контролем installed version, permissions и обратимым удалением обновления | **Наиболее реалистичный текущий путь; CI built, device install/effect ещё проверять** |
| Sidecar обычное приложение/лаунчер без изменения system | Меньший риск для эксперимента, но целевой владелец хочет **реальный MainSettings**, поэтому не конечное решение |
| Изменение EROFS/super в копии оригинала, новое AVB/vbmeta | **Технически подготовительно исследуемо офлайн**, но аппаратная загрузка и trust anchor не установлены |
| Подписанный собственный A/B OTA | AOSP metadata signing возможен, **OEM/updater/AVB приёмка не доказана** |
| Repack/encrypt `upgrade_image.pkg` | AES доступен, OEM контейнерная подпись и восстановление не подтверждены; **риск крайне высокий** |
| Перенос image одной платы на другую (8052 ↔ 8054), flash без подписей и rescue | **Не поддерживается имеющимися данными; не делать** |

## 6. Отдельные риски полного кастома

- AVB boot failure/bricking; отсутствие гарантированного fastbootd/unlock и восстановительной процедуры.
- Уничтожение factory/eeprom/persist/калибровок штатным PKG script или неверными командами.
- Перепутанные DTBO/GPIO, motor/ToF, PMU/gamma/panel, HDMI/CEC, audio HAL и keylayout.
- Нарушение `KuSourceRJ.TimeService`, температурной безопасности и управления вентиляторами при debloat.
- Удаление OEM HOME до установки проверенного нового HOME + fallback, потеря Settings/пульта.
- Ошибки signing / shared UID / privileged permissions / SELinux / resource/DEX layout в TvSettingsPlus.
- Прерывание OTA A/B, invalid slot, rollback-index, mismatch `ro.product.ota`, factory wipe.
- Ложная уверенность в «root»: `su` существует, но не запускался на изученном 8052; сведения о 8054 актуализировать отдельно.

**Ни один из рисков не снимается только успешной офлайн-сборкой.**

## 7. Что требуется, чтобы изменить оценку «кастомная прошивка пока не установима»

Только после согласования с владельцем и наличия безопасного возврата:

1. Полная **read-only** идентификация конкретного 8054, его slot, AVB properties/key/signature, board configs и updater mode; расхождения с factory образами задокументировать.
2. Безопасно проверенный recovery/rescue, bootloader acceptance и подтверждённый rollback; **не** пробовать unlock ради сбора фактов.
3. Частный backup device-specific данных и независимая проверка его целостности, никаких private/factory/ключей в публичный GitHub.
4. Раздельные build profiles и полный offline EROFS/SELinux/xattrs/aapt/signature validation; проверка лимитов super.
5. Проверка OTA-приёмки только после шагов 1–4, без автоматического применения.
6. На **каждом** физическом SKU: вход, фокус, ToF, геометрия, LED/термозащита, звук/CEC/eARC, пульт, сеть, sleep, recovery и загрузка.

**Промежуточный инженерный вывод:** для требуемого пользовательского меню предпочтительнее развивать штатный TvSettingsPlus MOD и не блокировать его ожиданием кастомного PKG. Полный firmware custom остаётся отдельным возможным проектом после снятия AVB/rollback рисков.

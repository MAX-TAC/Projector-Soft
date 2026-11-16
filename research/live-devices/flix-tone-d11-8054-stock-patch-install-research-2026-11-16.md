# FlixTone D11 Master 8054 — RUIOS MOD на базе заводского Android 13

**Дата исследования:** 16.11.2026  
**Аппаратная база:** реальный FlixTone D11 Master, OEM ID 8054, Android 13 (`eng.suhaix.20260905.031903`), два HDMI.  
**Требование проекта:** модифицировать именно стоковую RUIOS, а не переносить LineageOS и не создавать Android с нуля. Работы ниже — локальная офлайн-разборка и сборки; прошивка физического устройства не выполнялась.

## 1. Подтверждённая загрузочная цепочка и ограничения

- Есть root через `su 0 id`, без Magisk; SELinux permissive.
- Фактический AVB статус `locked/green/enforcing`; запуск со штатными образами.
- RSA-4096 подписи реальных `vbmeta_a` и `vbmeta_system_a` проверены и действительны. Ключ SHA1 `883f885174dcadcce97fefad1bcbd11a5c0289e7`.
- `boot_a` и `dtbo_a` подтвердили подписанные хеши в vbmeta.
- Ключ **не совпадает** с 12 публичными RSA-тестовыми ключами из [NCC Group test_avb_key](https://github.com/nccgroup/test_avb_key) ([результат](https://github.com/MAX-TAC/FlixTone_8054_Backup/actions/runs/43900620711)). Это не исчерпывает все существующие ключи.
- Системный OTA-сертификат `otacerts.zip` — AOSP testkey RSA2048; он отличается от AVB RSA4096. Наличие OTA testkey не снимает проверку изменённого `system`.

## 2. Извлечение реального стокового Android 8054

Из `super.img` (2 225 078 272 байт) восстановлены все четыре активных динамических раздела EROFS и успешно проверены `fsck.erofs --extract`:

| Раздел | Размер, MiB | Количество обычных файлов | Количество APK |
|---|---:|---:|---:|
| `system_a` | 816.13 | 2502 | 64 |
| `vendor_a` | 250.50 | 1304 | 4 |
| `product_a` | 73.58 | 56 | 6 |
| `system_ext_a` | 83.25 | 95 | 14 |
| **Всего** | 1223.46 | 3957 | **88** |

Основные OEM APK в живой системе:
- `system/app`: `KuSettings`, `KuSourceRJ`, `RuiAutoFocus`, `trdservice`, `zxlauncher`, `zxstore`, `TrdFactoryTest`, `TrdDreamPic`, `FTAirplayWireless`, `RJBluetoothSpeaker`.
- `system/priv-app`: `KeystoneCorrection`, `RuiYunOta`.
- `vendor/priv-app`: `trdmiddle`.
- `system_ext/priv-app`: `TvSettingsPlus`, `Provision`, `TvServices`, `MtkTvInput`, `SystemUI`, `LiveTV`.
- `system/preinstall_del` содержит `AmazonPrimeVideo`, `Netfix`, `YouTube` APK, но само нахождение в этом каталоге **не доказывает установленный и работающий пакет**.

`TvSettingsPlus` родной для аппаратной версии 8054; не подменять его APK из 8052. В живой сборке установлены 16 DEX оригинального TvSettingsPlus. Его оригинальный `versionCode=1`, package `com.android.tv.settings`.

Техническая расшифровка APK и файлов выполнялась автоматически в [GitHub Actions 43897487647](https://github.com/MAX-TAC/FlixTone_8054_Backup/actions/runs/43897487647) и расширенном [43898418576](https://github.com/MAX-TAC/FlixTone_8054_Backup/actions/runs/43898418576).

## 3. Прорыв: OEM приложения подписаны общедоступной AOSP platform-парой

**Все 17 выбранных заводских системных APK с реального 8054** успешно прошли проверку `apksigner`, и их сертификат в точности равен AOSP `platform.x509.pem` (SHA-256 `c8a2e9bccf597c2fb6dc66bee293fc13f2fc47ec77bc6b2b0d52c11f51192ab8`).

Проверенные: `FTAirplayWireless`, `KuSettings`, `KuSourceRJ`, `RuiAutoFocus`, `TrdFactoryTest`, `trdservice`, `zxlauncher`, `zxstore`, `KeystoneCorrection`, `RuiYunOta`, `trdmiddle`, `Factory`, `LiveTV`, `Provision`, `SystemUI`, `TvServices`, `TvSettingsPlus`.

Доступный AOSP `platform.pk8` позволяет подписывать совместимые обновления этих APK с тем же сертификатом. Это **обходится без изменения подписанной EROFS-базы и AVB**: Android может устанавливать обновление системного пакета в `/data/app`, сохраняя неизменным оригинальный `/system_ext/priv-app` (конкретная приемка обновления и привилегий проверяется на устройстве отдельно). Важно сохранить package name, оригинальные разрешения, shared UID/manifest и повысить `versionCode`.

Именно этот вариант сейчас наиболее зрелый для RUIOS MOD: системное меню и другие OEM APK обновляются как приложения, нежелательные предустановленные компоненты отключаются после проверки зависимостей. Для полноценного неизменяемого `system.img` это пока не заменяет подписанный USB-образ.

## 4. Собран реальный экспериментальный TvSettingsPlus MOD v33 на базе 8054

[Успешная GitHub Actions сборка 43900081008](https://github.com/MAX-TAC/FlixTone_8054_Backup/actions/runs/43900081008) использовала точный OEM `TvSettingsPlus.apk` из сентябрьского `super.img` и существующий код MOD v33 из `Projector-Soft`.

- Исходный APK SHA256: `f8e0c6fe0f46e7eb8db651c31d85ef8b32a769b37e4dd0f96ed19a64725d74ee`, `versionCode=1`.
- Восемь OEM smali-классов изменены существующими правилами без ошибок (патчи источников, яркости, главного меню, сна). Незатронутые OEM DEX/ресурсы сохраняются компоновщиком.
- Java-классы MOD успешно скомпилированы для Android 13, пройдены все тестовые этапы workflow; package `com.android.tv.settings`, `versionCode=33`.
- APK подписан AOSP platform pair, `apksigner verify` успешен; сертификат по SHA256 совпадает с оригиналом.
- Экспериментальный APK SHA256: `a83bf9332f559ffaa27ad6b14bd670f0e09fd6f5612d14ede3a1ca05ebd7ef58`.
- Артефакт workflow: `tvsettingsplus-8054-v33-native-controls-experimental`.
- **Никаких установок на реальный D11 не выполнено. Не считать APK финальным или полностью заменяющим KuSettings**; аппаратная проверка 8054, полный перенос меню и сценарий отката ещё не завершены. Оригинальный `KuSettings` пока не отключать.

## 5. USB PKG: возможность аппаратно подтверждена, самоподписанный пакет пока не принят

В реальном `mboot_a.img` обнаружены `do_check_usb_auto_upgrade`, `check_usb_upgrade_file_available`, `do_mtkupgrade`, `mtkupgrade usb`, `usb start;mtkupgrade usb`, `usb_upgrade_pkg`.

Встроенная справка прямо описывает поведение `check_usb_auto_upgrade` при старте:
- `usb_auto_upgrade=disable_once` — пропускает один запуск;
- `usb_auto_upgrade=disable` — пропускает постоянно;
- `usb_auto_upgrade=NULL/enable/other` — разрешает проверку USB.

В снятом `uenv.img` `usb_auto_upgrade` отсутствует, `bootdelay=0`, `devicestate=lock`, `verify=n`. В `bootcmd` обычный `partition read...bootm`, нет явного `mtkupgrade`. **Код USB auto-upgrade есть, вызов конкретного раннего пути и работа с флешкой не воспроизведены**.

Загрузчик знает имя `/MTKUpgrade.bin`; установленный Android OTA клиент настроен на `upgrade_image.pkg` и `RJMT9660_firmware_8054.zip` (два потенциально разных update-механизма). Нельзя считать, что файл `upgrade_image.pkg` можно просто переименовать для USB Mboot без проверки формата и подписи.

Другие строки реального Mboot: `Verify PKG HASHSET...`, `Verifying Hash Integrity ...`, `This upgrade_image.pkg version is older than current version.`, `avb_slot_verify`, 24 вхождения `signature`, 25 `rsa`. Это **не позволяет сделать вывод о возможности самостоятельного подписания PKG**. Обратный инструмент `unixtract` специально поддерживает только распаковку, а старые MStar pack-tools не гарантируют совместимости с новым подписанным PKG.

`verify=n` — переменная U-Boot, а не доказательство отключённого Android AVB. По состоянию загруженной ОС AVB `green/locked/enforcing`.

## 6. Два направления RUIOS MOD

### A. Приоритетный и уже работающий офлайн путь — патчи системных APK

1. Закончить перенос всех KuSettings/KuSourceRJ меню на живую OEM базу `TvSettingsPlus` 8054, не заменяя аппаратный HAL.
2. Подписать обновления доступным AOSP platform pair и подтвердить package compatibility, permissions, native callbacks.
3. Установить обновлённое системное приложение в `/data/app` как обычное signed APK update (**после аппаратных тестов и плана отката**).
4. После проверки отдельно отключать `zxlauncher`, `zxstore` и иные нежелательные приложения, не отключая OEM устройства, сенсоры, мотор/фокус, CEC и TRD-сервисы.
5. Распространять пользовательский ZIP с APK, инструкцией, SHA и поэтапным откатом. **Это не ZIP, который принимает штатное OTA-меню Android; это пакет приложений для установки пользователем или через ADB.**

### B. Полная модифицированная RUIOS в качестве ZIP/PKG

1. Необходимо получить и статически разобрать исходный **оригинальный заводской PKG**, а также приемку PKG HASHSET в Mboot.
2. При прямой модификации `system_a` или `system_ext_a` на EROFS dm-verity-хеши изменятся. Нужен подписанный `vbmeta_system_a` под доверенным AVB RSA4096 или подтверждённая разблокировка/новый root-of-trust.
3. Для `payload.bin` также нужны корректная OTA metadata, подпись для update_engine и безопасный A/B slot state; testkey OTA не решает пункт AVB.
4. Изучить условия recovery/USB при холодном старте на UART **только пассивным наблюдением**, не подавать PKG на реальное устройство.
5. До записи нужен проверенный механический способ восстановления с контролем GPT, A/B, калибровок и OEM-уникальных разделов.

## 7. Что точно НЕ делать

- Не флешить PKG от 8052 или сентябрьскую/более новую PKG 8054 как тест: заводские скрипты ранее содержали очистку `persist`, `eeprom_a`, `userdata` и GPT.
- Не менять `devicestate`, `verify`, `vbmeta`, `boot`, `uenv` и разделы без восстановительного маршрута.
- Не применять `adb disable-verity`/ `adb remount -R` как безусловно безопасные команды на locked-AVB: стандартная AOSP OverlayFS требует проверки поддержки, отключает verity и может инициировать перезагрузку.
- Не удалять `KuSettings`, `KuSourceRJ`, `trdmiddle`, `trdservice`, `RuiAutoFocus` и `KeystoneCorrection` до проверки всех аппаратных вызовов.

## 8. Что сделано и что осталось

**Сделано:** full raw super backup, EROFS partitions extracted, 88 APK inventoried, USB auto-upgrade hook found in real Mboot, 17 OEM APK signatures verified against AOSP platform certificate, actual 8054-derived TvSettingsPlus MOD v33 successfully built and signed, OEM AVB key excluded from 12 commonly known public keys.

**Не подтверждено:** установка v33 на работающем D11, полный перенос KuSettings, работа PKG HASHSET с модифицированным PKG, разблокировка Fastboot, USB cold-boot recovery with a custom package, восстановление при повреждённом Mboot/AVB, приемка собственного `system` image locked загрузчиком.

**Инженерный вывод:** получать работающий RUIOS MOD сначала как обновления системных APK, продолжая в отдельной ветке исследовать полное USB/OTA-перепрошивание заводского Android 13.

# FlixTone D11 Master 8054 — проверка живых образов AVB (16.11.2026)

Источник: публичный [резервный релиз](https://github.com/MAX-TAC/FlixTone_8054_Backup/releases/tag/FlixTone_8054_Backup) владельца, локально загруженный `otacerts.zip`, `00_full_diagnostics.txt` и read-only GitHub Actions [первый](https://github.com/MAX-TAC/Projector-Soft/actions/runs/43881859597), [повторный с дополнительными проверками](https://github.com/MAX-TAC/Projector-Soft/actions/runs/43882139776). Все исследования проводились без команд прошивки.

## Подлинность файлов релиза

| Файл | Размер | SHA256 | Совпадает с GitHub API |
|---|---:|---|---|
| `vbmeta_a.img` | 1 048 576 | `0131481e6ac5533de54c3cc6a052d149ba135d2195fd31242f18bbb2a893eb19` | Да |
| `vbmeta_system_a.img` | 1 048 576 | `d8b2856abd6920fb9ca25b9b487365386ca0e746e379d1275b92cc9a15d23e39` | Да |
| `dtbo_a.img` | 8 388 608 | `7b6face54f66968c533041d9ce38971eb7f5064d1357e46fb223a3fbe3cffded` | Да |
| `boot_a.img` | 78 643 200 | `f1fc341533babecc66ab697df3943464a658c9ae8a03a035f2bf5a30529f1c63` | Да |

Размеры совпадают с GPT живого проектора. Контрольная сумма доказывает тождество скачанного файла байтам опубликованного релиза, но сами снимки уникальных заводских разделов ещё не получены.

## Результат avbtool

- `vbmeta_a`: `SHA256_RSA4096`, корректная внутренняя криптографическая подпись, `rollback_index=0`, `flags=0`.
- `vbmeta_system_a`: тот же алгоритм, корректная подпись, `rollback_index=0`, `flags=0`.
- Одинаковый AVB RSA-4096 public key, SHA1 `883f885174dcadcce97fefad1bcbd11a5c0289e7`, SHA256 бинарного AVB-ключа `dcf38badcd7784d2aa892739eba5e239a649362edb03586a455b3f8ded046cfe`.
- Проверено **несовпадение** modulus этого ключа с публичным `test/data/testkey_rsa4096_pub.pem` из LineageOS/AOSP external/avb test data.
- `boot_a.img`: Android boot image magic `ANDROID!`, AVB hash footer; фактический SHA256(salt + данные boot) совпадает с Hash descriptor в подписанном `vbmeta_a` (True).
- `dtbo_a.img`: Device Tree Overlay magic `d7b7ab1e`, AVB hash footer; фактический SHA256(salt + данные dtbo) совпадает с подписанным Hash descriptor (True).

### AVB chain descriptors в vbmeta_a

| Раздел | SHA1 доверенного публичного ключа |
|---|---|
| `vbmeta_system` | `883f885174dcadcce97fefad1bcbd11a5c0289e7` |
| `tvservice` | `d4df289869fa6b518601dce87190899fcd4c9926` |
| `bootdata` | `aec967cdccd487eabc11e667a8cad176255f27b0` |
| `linux_rootfs_a` | `880d057ef10eee5fd5415cd257c3da366f422861` |
| `3rd_a` | `47c7be71b30592a6411fdd54878b525d7f73037c` |
| `factory_a` | `fdf52feb0fec9bda3715741c1c5fffd5cec17870` |

`vbmeta_system_a` содержит dm-verity descriptors `system`, `system_ext` и `product`; `vbmeta_a` содержит vendor hashtree и boot/dtbo hashes. Эти проверки нельзя отключить только тем, что устройство имеет shell-root.

### OTA certificate

Прикреплённый `/system/etc/security/otacerts.zip` содержит `testkey.x509.pem`, self-signed Android RSA2048, fingerprint SHA256 `A4:0D:A8:0A:59:D1:70:CA:A9:50:CF:15:C1:8C:45:4D:47:A3:9B:26:98:9D:8B:64:0E:CD:74:5B:A7:1B:F5:DC`.

Это **не AVB RSA4096-ключ**, и в одиночку не доказывает возможность подписи изменённого A/B payload, принятия его `update_engine` или загрузки нового system.

## Перспектива установки кастома

- Кастомный Android 13 ROM **технически конструируем**, поскольку есть исходные factory images и root-доступ для резервного копирования.
- **Установка изменённого system/boot через штатное A/B OTA на locked/green** не доказана, так как нужен доверенный AVB RSA4096 private key либо подтверждённая альтернативная цепочка доверия/разблокировка.
- Подписанный только AOSP OTA testkey пакет не решает задачу AVB.
- Физическая USB-установка `upgrade_image.pkg` через mboot/U-Boot потенциально существует в семействе MT9660: [пример инструкции MT9660](https://bbs.cvbs.cn/forum.php?mod=viewthread&tid=2933) описывает вход в Mboot с удержанием Enter и команду `mtkupgrade usb upgrade_image.pkg`. **Нет доказательства**, что этот способ работает на FlixTone 8054; запускать чужой пакет нельзя. Более того, заводской script в нашей прошлой декомпиляции содержит команды очистки eeprom, persist и userdata.
- Для исследования bootloader/USB без записи требуется частный дамп `mboot_a`, `uenv`, при необходимости `mmcblk0boot0` и `super`, сохранение уникальных калибровок перед экспериментами и подтверждённое восстановление. Любой образ заводских уникальных разделов держать **приватно**.

## Следующие read-only шаги

1. Локально извлечь `mboot_a` (10 МиБ) и `uenv` (16 МиБ), искать команды загрузки, recovery и USB только в офлайн-копиях, без `setenv`/`saveenv` на устройстве.
2. Сделать локальную приватную резервную копию `eeprom_a`, `persist`, `project_id`, `tvcertificate`, `private` и `tvconfig_a` с SHA256; не загружать в общедоступные репозитории.
3. Считать `super` (2 225 078 272 байта) и восстановить `system`, `system_ext`, `product`, `vendor` для кастомизации на основе точной сентябрьской сборки.
4. Перед любым патчем AVB/разблокировкой убедиться в работающем восстановлении, правильном выборе A/B слота и отсутствии угрозы аппаратной калибровке.

**Итог**: исследованные исходные образы целы, криптографическая защита включена, ключи AVB не общедоступный AOSP testkey. Root открывает возможность полноценного анализа и получения резервных копий, но не гарантирует безопасную установку неподписанного system.

# FlixTone D11 Master 8054 — Разблокировка AVB и загрузчика (проверка 16.11.2026)

**Предмет:** только загрузчик, ключи AVB и возможность установки модифицированного **стокового** Android 13 (A/B OTA, USB PKG, ручная установка). **APK/LineageOS не исследуются.**

## Живые данные, проверенные офлайн

- Android 13, OEM 8054, MediaTek MT9660 / mt5873_cd; root \`su 0 id\` работает.
- \`ro.boot.vbmeta.device_state=locked\`, \`verifiedbootstate=green\`, \`veritymode=enforcing\`.
- \`uenv.img\`: \`devicestate=lock\`, \`verify=n\`, \`bootdelay=0\`. Установка \`verify=n\` в окружении НЕ означает отключение Android AVB.
- Подписи \`vbmeta_a\`, \`vbmeta_system_a\` действительно проверены, SHA256_RSA4096; root AVB public key:
  - SHA1: \`883f885174dcadcce97fefad1bcbd11a5c0289e7\`
  - SHA256 (AVB pubkey binary): \`dcf38badcd7784d2aa892739eba5e239a649362edb03586a455b3f8ded046cfe\`.
- Прямой поиск отпечатков ключа в открытом вебе не нашёл соответствующего **закрытого** PEM; ключ **не совпал с 12 общедоступными private keys** из [NCC Group test_avb_key](https://github.com/nccgroup/test_avb_key) ([проверка](https://github.com/MAX-TAC/FlixTone_8054_Backup/actions/runs/43900620711)). Это не доказательство отсутствия иных опубликованных приватных ключей.
- Без AVB-разблокировки или известного доверенного закрытого ключа модифицированные \`system\` / \`vendor\` / \`vbmeta\` в стандартной locked/green цепочке отвергаются; пользовательский root в Android это не меняет.

## Ключевой результат: работа с состоянием AVB скомпилирована в реальный Mboot

[Read-only исследование точного мbootа](https://github.com/MAX-TAC/FlixTone_8054_Backup/actions/runs/43902935064) (мboot SHA256 \`ebf9a0adef859f568d792d182f4ace08d026a27dc7f322bab735c67b07cd34c1\`) обнаружило следующие строки непосредственно в OEM U-Boot:

\`\`\`text
avb set-devicestate <0/1>  - AVB2.0 set-devicestate, 0=unlock/1=lock.
avb set-devicestate 0
avb set-devicestate 1
out_is_unlocked_from_env=%d, out_is_unlocked=%d
device_locked=%d
avb verify [slot_suffix] - run verification process using hash data
\`\`\`

Важное: \`avb set-devicestate 0\` **не выполнялось** на железе. Наличие help-string и команды — убедительное доказательство скомпилированного интерфейса, **но не разрешений, доступности UART или успешного сохранения состояния**.

Также встроены:
\`\`\`text
fastboot usb 0
Android Fastboot
reboot-fastboot
fastboot udp error: %d
\`\`\`

Однако \`get_unlock_ability\`, \`avb_custom_key\`, \`fastboot flashing unlock\` и \`avb is_unlocked\` **не найдены как строковые маркеры**. Невозможно строго заключить по отсутствию строк, что обработчиков нет. Обычный Fastboot без явной поддержки flashing unlock — не обещание возможности разблокировать.

### Ограничение — консоль Mboot может быть запаролена

В том же реальном Mboot обнаружены:
\`\`\`text
forbidden command [%s], please usb [ce] to unlock console
Console Unlock by Challenge-Response
[CE] unlock console.
\`\`\`

Это механизм **разблокировки командной консоли U-Boot**, отличный от изменения состояния **Android Verified Boot**. Неизвестно, заблокирована ли консоль на конкретном 8054 и распространяется ли блокировка на \`avb set-devicestate\`.

## Независимое подтверждение точной команды на другой MediaTek TV платформе

В публичном руководстве по TCL X11K/C8K/C7K и родственным моделям применялась **именно** \`avb set-devicestate 0\`:
[4PDA TCL thread](https://4pda.to/forum/index.php?showtopic=1080227&st=34540).

Описанный производителями другой платформы порядок включает доступ к U-Boot через UART, \`avb init mmc\`, \`avb set-devicestate 0\`, \`avb set-verity disable\` и \`saveenv\`. **Не запускать эту последовательность на FlixTone автоматически**: доступ, расположение AVB lock state и безопасность отличаются.

Близкий пример смены состояния через U-Boot: [Realme MStar Smart TV](https://4pda.to/forum/index.php?showtopic=1100132) использует схожий, но другой интерфейс \`avbab set_device_state\`.

Согласно AOSP:
- [Lock and unlock bootloader](https://source.android.com/docs/core/architecture/bootloader/locking_unlocking)
- [Verified Boot](https://android.googlesource.com/platform/external/avb/+/android17-release/README.md)

При locked state доверие к изменённой системе требует корректной подписи или разблокировки; переход обычно предполагает wipe userdata, и он не безрисковый.

## Вывод о путях для кастомного стокового ROM

| Механизм | Проверен | Статус |
|---|---|---|
| Получить доверенный RSA-4096 private key из \`vbmeta\` | невозможен криптографически | Нет |
| Найти известный общедоступный тестовый приватный ключ совпадающего modulus | 12 известных ключей + web поиск SHA | Совпадений нет |
| Перевести AVB в \`unlocked\` через команду OEM Mboot \`avb set-devicestate 0\` | код скомпилирован в mboot | Высокоприоритетный реальный путь, **runtime не проверен** |
| Выполнить \`fastboot flashing unlock\` | fastboot USB/UDP код присутствует, стандартный unlock endpoint не доказан | Неподтверждено |
| USB-auto-upgrade \`MTKUpgrade.bin\` / \`upgrade_image.pkg\` | бинарный handler в Mboot есть | Самоподписанный PKG и вызов автообновления не проверены |
| Модифицировать \`super\` под OEM stock Android 13 | raw \`super.img\` EROFS извлечён | Сборка возможна офлайн; запуск зависит от AVB |
| Сохранить locked и загрузить свои подписи | \`avb_custom_key\` маркера нет | Не подтверждено |

## Один решающий тест, пока только read-only

**Нужно получить доступ к UART Mboot**, желательно сначала полностью пассивным прослушиванием 3.3 В TTL (RX+GND), определить штатный лог и остановку загрузки. Если получен U-Boot prompt, допустимы только команды просмотра списка и состояния:

\`\`\`text
help
help avb
printenv devicestate
printenv bootdelay
printenv usb_auto_upgrade
\`\`\`

\`avb\` без аргументов может вывести справку, но лучше использовать \`help avb\`, если разрешено. Применять команды \`avb set-devicestate 0\`, \`avb set-verity disable\`, \`saveenv\`, \`setenv\`, \`mtkupgrade\`, \`fastboot flash\` или любые записи **до проверки восстановления** не следует.

Для **100% ответа** о разблокировке конкретного экземпляра требуется доказательство: доступная консоль + разрешённая команда смены состояния + фактический переход \`locked/green\` в \`unlocked/orange\`, либо подтверждённая ошибка запрета. Без этого утверждение «возможно/невозможно на 100%» было бы выдумкой.

**Практический приоритет:** UART Mboot вместо поиска случайного RSA private key, если ключ нельзя сопоставить с известными общественными образцами.

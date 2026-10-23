# Источники и разделение репозиториев

**Этот репозиторий**: только приложения, OEM API, справочные APK, адаптеры, сборка и диагностика.

**[Everycom-Cinelux-Firmware](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware)**: исходные OTA, PKG, разделы, AVB, boot и общий firmware audit. Архивы прошивок сюда не копируются.

- База: [OTA 8052 RUIOS.8052.1.0.4](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/releases/tag/OTA_Updates), файл RJMT9660_firmware_8052.zip, размер 851685795 байт, sha256 `bedea70fbf3f08d3c18e59d5f91e7d12828c77fac314c30fcc98332c32c88e35`.
- 8054: [заводская PKG 2×HDMI](https://github.com/MAX-TAC/Everycom-Cinelux-Firmware/releases/tag/Original_Firmware); **не путать с OTA 8053**.
- Переносимые исследования из оригинального репозитория размещены в `research/oem-apps/` без декомпилированных проприетарных исходников.
- `vendor/reference/8052/`: оригинальные APK из OTA, хэши, версия и сертификат — результат автоматической GitHub Actions проверки.

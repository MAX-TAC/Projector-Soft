# TvSettingsPlus MOD: техническая архитектура

Базовый APK: `com.android.tv.settings`, `/system_ext/priv-app/TvSettingsPlus/TvSettingsPlus.apk`, Android 13 SDK 33, versionCode 1, shared UID 1000 — подтверждено для 8052.

## UI
- Двухпанельные настройки Android TV; обычные настройки справа, без лишних полноэкранных окон.
- «Каналы и источники» объединяет Android TV, HDMI 1 (8052/8054) и HDMI 2 (только 8054 при проверенной функции).
- «Изображение и звук»: изображение, контраст/цвет/яркость, HDR, MEMC, Game Mode, звук, эквалайзер — только при реально доступном API.
- «Проекция»: фокус, автофокус, трапеция, угол/зум. Полноэкранный контроль допустим для ручной коррекции.
- «Об устройстве»: версии, OTA через RuiYunOta, CPU thermal (только чтение), охлаждение без выдуманных данных.
- Сохранить стандартные Android TV настройки Wi-Fi, приложений, аксессуаров и питания.

## Правила
1. Исключить скриншоты и режим Bluetooth-колонки.
2. Не удалять KuSettings; отключение UI только после проверки фоновых компонентов.
3. Не отключать KuSourceRJ, RuiAutoFocus, KeystoneCorrection, trdservice, trdmiddle, KuSetupWizard и аппаратные службы.
4. Адаптеры OEM API должны иметь проверку возможностей по ревизии, не вызывать моторы напрямую непроверенными командами.
5. При APK-обновлении не модифицировать AVB/разделы system. Сохранить packageName, sharedUserId, совместимую подпись и повысить versionCode.
6. Маркер прошивки test-keys и **OTA testkey не равны** подтверждённому platform test certificate APK. Сравнить APK certificate c AOSP platform.x509.pem до сборки/установки.

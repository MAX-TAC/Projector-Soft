# OEM пакеты и сервисы

| Файл APK | Пакет | Назначение |
|---|---|---|
| TvSettingsPlus | `com.android.tv.settings` | Основной APK модификации; штатные Android TV Settings |
| KuSettings | `com.my.settings` | OEM настройки, фоновые receivers и сервисы |
| KuSourceRJ | `com.coolux.source` | Источники TV/HDMI; зависим от вызовов KuSettings |
| RuiAutoFocus | `com.hysd.screen` | Мотор, фокус, ToF |
| KeystoneCorrection | `com.rjpj.tcorrection` | Трапеция и цифровой зум, JNI i2c_jni |
| trdservice | `com.trd.service` | TRDSERVICE, системная служба |
| trdmiddle | `com.trd.framework` | TrdSystemService, Binder и JNI |
| RuiYunOta | `com.rjpj.yunota` | OTA |
| KuSetupWizard | уточняется | Первая настройка/Provision |
| TvServices | уточняется | Аппаратные ТВ-службы |
| framework-res.apk | android | Ресурсы системного framework |

Исследования OEM функций и различий между платами находятся в `research/oem-apps`. Наличие имени класса/метода в JADX не гарантирует безопасность его вызова.

# RUIOS 8052: full read-only KuSettings reference audit

Scanned **2054** files including **85** APK files.
This is string/DEX reference inventory, not proof of runtime dependency.

| File | KuSettings | OEM service | AUTO_BLUETOOTH | Screenshots |
|---|---:|---:|---:|---:|
| system/system/app/KuSettings/KuSettings.apk | 2092 | 20 | 1 | 15 |
| system/system/app/KuSetupWizard/KuSetupWizard.apk | 2092 | 20 | 1 | 15 |
| system/system/etc/xtzm_setting.txt | 1 | 0 | 0 | 0 |
| system/system/framework/services.jar | 1 | 0 | 1 | 0 |
| system/system/menu_config_xml/whaletv_config.json | 1 | 0 | 0 | 0 |
| system_ext/app/WFDSinkTest/WFDSinkTest.apk | 1 | 0 | 1 | 0 |

Important: no arbitrary replacements. Android framework PhoneWindowManager may be runtime-loaded from services.jar, not WFDSinkTest.apk; check actual hosting process before patching an APK.

Do not disable KuSettings until all live intents have handlers, HDMI/focus/audio services tested, and a rollback plan exists.

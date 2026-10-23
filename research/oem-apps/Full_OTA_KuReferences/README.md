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

## Config consumer candidate locations (string evidence only)
- system/system/app/KuSettings/KuSettings.apk: projector_settings_action
- system/system/app/KuSetupWizard/KuSetupWizard.apk: projector_settings_action
- system/system/app/zxlauncher/zxlauncher.apk: whaletv_config_consumer
- system/system/app/zxstore/zxstore.apk: whaletv_config_consumer
- system/system/etc/xtzm_setting.txt: settings_config_consumer

## OEM config usages
- system/system/etc/xtzm_setting.txt: [{'line': 3, 'near_reference': '    "settingApps": ["com.my.settings"],'}]
- system/system/menu_config_xml/whaletv_config.json: [{'line': 59, 'near_reference': '  "white_list": ["zeasn.open.technical.tv.store","com.my.settings","com.mediatek.wwtv.mediaplayer","com.mediatek.miracast","com.android.toofifi","com.rjpj.bluetoothspeaker"],'}]

Important: no arbitrary replacements. Android framework PhoneWindowManager may be runtime-loaded from services.jar, not WFDSinkTest.apk; check actual hosting process before patching an APK.

Do not disable KuSettings until all live intents have handlers, HDMI/focus/audio services tested, and a rollback plan exists.

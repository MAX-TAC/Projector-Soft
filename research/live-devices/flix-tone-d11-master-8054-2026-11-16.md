# FlixTone D11 Master 8054: live ADB audit (2026-11-16)

Source: uploaded 00_full_diagnostics.txt, 01_hdmi_audio_logcat.txt, apk.zip and vendor audio policy XML. This report is sanitized. No device-unique IDs, certificates, raw logs, calibration images or user information are committed.

## Device identification
- Android 13 / SDK 33; MediaTek mt5873_cd; boot hardware mt9660.
- OEM profile 8054; two HDMI inputs HW6:HDMI1 and HW7:HDMI2.
- Build eng.suhaix.20260905.031903, userdebug/test-keys.
- OTA RUIOS.8054.1.0.0 with channel mt9660/8054/8054_ota.json. Existing reference PKG for 8054 was built later, 2026-09-30 (RUIOS.8054.1.0.1).
- Boot slot _a; Verified Boot green; vbmeta device_state locked; verity enforcing; SELinux runtime permissive.
- Root binary at /system/xbin/su but root privileges were not verified. Userdebug/test-keys and SELinux permissive do not imply unlocked bootloader.
- eMMC ~29.1 GiB. Super ~2.07 GiB; userdata ~25.81 GiB. A/B partitions include boot, vbmeta, dtbo, mboot, tvservice, tvconfig, bootdata, linux_rootfs.
- No raw partition image was dumped; only readable APKs and config files are available.

## HDMI ARC / eARC
- HDMI-CEC status at capture: mIsCecAvailable=false; mSystemAudioActivated=false; mArcEstablished=false; hdmi_cec_enabled=1.
- ARC capability map: {1=false, 2=true, 3=false, 4=false}; logical CEC port 2 is marked ARC-capable.
- CEC describes 4 logical ports while OEM profile describes 2 physical HDMI ports. History shows route selection to physical-address path 0x4000. Confirm actual HDMI labeling / mapping; do not presume a fault from a single snapshot.
- Vendor audio policy XML includes AUDIO_DEVICE_OUT_HDMI_ARC and an HDMI ARC output route.
- ARC output port lists AC3, E_AC3, DTS and DTS_HD but no PCM profile. This is a candidate for PCM routing investigation, not conclusive evidence PCM cannot be sent.
- During diagnostics AudioPolicyManager lists only Speaker as available output, and audio HAL reports ARC mode auto/type not specified. The capture may have occurred with no receiver connected; it is not a controlled failure reproduction.
- Filtered logcat contains only Parcel NULL-string messages. The old generic grep pattern 'arc' accidentally matched the substring in 'Parcel'; no trustworthy incident-level ARC log was collected.
- Needed: play/pause/resume snapshots with an actual receiver attached and meaningful logcat tags.

## Exact APK compare to GitHub 8052 reference
Reference branch: feature/tvsettingsplus-thermal-telemetry; path vendor/reference/8052/APK.
Matched byte-for-byte using Git blob SHA-1:
- KuSourceRJ.apk: 4c4cd22dad425e2d7a9d9511136fd8069cd16dd6
- RuiYunOta.apk: bd9453f0dead7d003207d5673b76a09c058d6ad8
- trdmiddle.apk: de4624d1ab74f696a0755695be61e8bc99dd3791

Different from the stored 8052 reference: TvSettingsPlus.apk, KuSettings.apk, KeystoneCorrection.apk, RuiAutoFocus.apk, trdservice.apk.

The following OEM services were running: com.coolux.source/.service.TimeService and com.my.settings/com.my.service.MySetTimeService. Do not disable KuSettings; do not install an unvalidated 8052 MOD on the 8054 hardware.

## Safe next steps
1. Check 'id; su -c id' in adb shell before attempting any read-only partition dump.
2. Capture dumpsys hdmi_control, dumpsys audio and dumpsys media.audio_policy in three states: sound working, pause, resume-silent.
3. Confirm which physical HDMI port supports ARC/eARC and whether the receiver reports ARC or eARC.
4. Compare changed OEM APK code paths for 8054 before applying the 8052 TvSettingsPlus MOD.
5. Keep sensitive partitions (eeprom_a, persist, tvcertificate, private, project_id, metadata, userdata) private; raw images are not equivalent to a signed installable factory PKG.

## September 8054 setup-package difference

The live build (2026-09-05) has /system_ext/priv-app/Provision/Provision.apk as package com.android.provision, with no KuSetupWizard in installed package list. In the previously analyzed factory 8054 PKG from 2026-09-30, KuSetupWizard replaced Provision. This is a material difference for first-boot setup and TvSettingsPlus/KuSettings migration; do not assume behavior parity across both 8054 builds.

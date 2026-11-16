# FlixTone D11 Master (8054): feasibility of custom firmware and USB OTA

**Date:** 2026-11-16  
**Evidence:** live ADB diagnostics supplied by device owner; live `RuiYunOta.apk` byte-identical to the GitHub 8052 reference; factory 8052/8054 PKG comparisons already documented in `MAX-TAC/Projector-Soft`; static JADX inspection of the factory updater conducted through GitHub Actions [43877130284](https://github.com/MAX-TAC/Projector-Soft/actions/runs/43877130284) and [43877534348](https://github.com/MAX-TAC/Projector-Soft/actions/runs/43877534348). No write / flash tests performed.

## Executive verdict

A stock-Android-13-based custom OS is a credible engineering goal, but **no safe installation path for modified OS images is yet established**. A/B local OTA definitely exists and invokes Android `UpdateEngine`. USB recovery at cold boot, bootloader unlocking and acceptance of third-party AVB-signatures are not demonstrated. The currently verified boot state is locked and green; modifying system images without solving AVB should be expected to fail.

The lowest-risk interim solution is a fully reversible custom UI and service stack on stock firmware, retaining all projector-critical OEM native services. The eventual modded OTA route is worth investigating only after determining signing keys and a verified stock recovery method.

## 1. Real device facts

- Profile `8054`, build `eng.suhaix.20260905.031903`, Android 13, SDK 33, `userdebug/test-keys`.
- `ro.boot.vbmeta.device_state=locked`, `ro.boot.verifiedbootstate=green`, `ro.boot.veritymode=enforcing`, `ro.boot.slot_suffix=_a`, `ro.secure=1`, `ro.debuggable=1`, `ro.adb.secure=0`.
- SELinux runtime is `Permissive`; this does **not** turn off Verified Boot.
- Android runtime is **32-bit** (`ro.product.cpu.abilist=armeabi-v7a,armeabi`, `ro.zygote=zygote32`), despite Armv8-capable hardware. A generic 64-bit Android/Lineage image cannot simply replace the installed system while retaining OEM 32-bit TV libraries.
- `update_engine` daemon and boot-control HAL are running. A/B partition pairs include `boot`, `dtbo`, `vbmeta`, `vbmeta_system`, `mboot`, `optee`, `tvservice`, `tvconfig`, `bootdata` and `linux_rootfs` plus dynamic `system`, `vendor`, `product`, `system_ext` via `super`.
- `ro.product.ab_ota_partitions` lists more than just Android userland. An OEM OTA could update critical bootloader/secure-OS/TV firmware, so not every official update is a harmless or automatically recoverable userland change.
- `su -c id` returned `su: invalid uid/gid '-c'`; this is an **argument-format error**, not an access-denied result. Test `su 0 id` or (from PC) `adb root` before concluding root unavailable. Neither test flashes firmware.
- A matching **September 5 factory restore image** has not been verified; earlier analyzed 8054 PKG was **September 30**, which differs and may change hardware initialization or configuration.

## 2. OTA via mounted USB / network, verified in the OEM updater

Live properties identify two factory update names:

```
ro.rjpj.yunota.zip=RJMT9660_firmware_8054.zip
ro.rjpj.yunota.bin=upgrade_image.pkg
```

Exact static evidence from `com.rjpj.yunota` (JADX source of the live-matching APK):

- `OTASystemLocalUpdateActivity` obtains filenames using `OTATools.getYunOTALocalZip()` and `.getYunOTALocalBin()` from system properties, listens for media mount/eject, scans USB storage and presents Install.
- `AbUpdate` constructs `new android.os.UpdateEngine()`; the local ZIP path is unzipped except for `payload.bin`, retrieves `payload_properties.txt` and offset/length from `META-INF/com/android/metadata`, and calls `UpdateEngine.applyPayload("file://"+zipPath, offset, length, props)`.
- Network direct HTTPS mode also calls `UpdateEngine.applyPayload` with a URL and offset/size/properties.
- `AbUpdate` has a constant `SKIP_VERIFY_PAYLOAD_BIN=true` and separately implements `verifyPayloadMetadata`, apparently omitted from the normal local-install transition. **This is not bypass of mandatory `update_engine` payload signature/integrity checks or of boot-time AVB.**
- The local UI searches both named file types, but the precise `upgrade_image.pkg` handling remains partly unresolved: one visible `AbUpdate` ZIP path rejects a non-ZIP filename; do not assert that `.pkg` is accepted by that same Android path without the missing OEM handler and proof.
- Prior inspection of publicly available 8052/8053 OTA showed signed A/B `payload.bin` (RSA-2048/SHA256 using a standard AOSP test certificate). That key does **not** establish that 8054 trusts the same payload, and is not the RSA4096 AVB key from the inspected factory PKGs. Compare live trust anchors before making signing claims.

## 3. Why an arbitrary custom OTA may fail

There are several independent acceptance/trust gates:

1. UI and filename / package metadata recognition (`RuiYunOta`).
2. Payload metadata, hash/signature, partition compatibility and allowed operations (`update_engine`).
3. Hardware-specific section and bootloader security checks for factory PKG, if a bootloader recovery is invoked (not yet inspected on the live board).
4. On reboot, the AVB root of trust verifies `vbmeta` and associated boot/dtbo/dynamic partitions. `green` + `locked` and enforcing dm-verity make altered unsigned images unsafe to test.
5. Slot switching and fallback may not protect shared non-A/B calibration areas or bootloader operations.

An encryption/decryption key for PKG, a public AVB key, and the OTA signing key are **not interchangeable**. To produce accepted OS changes, either a permitted unlock/custom trust root must be established or correctly trusted signing credentials must be available.

## 4. USB at cold power-on

The device has MStar/MediaTek `mboot_a`, `mboot_b`, `uenv` and factory `upgrade_image.pkg` naming. An early bootloader recovery loader **may** exist, but none of these facts proves its triggering key, USB file recognition or acceptance/signature rules on 8054. This must be researched from board-specific vendor instructions or harmless bootloader serial logs. Do **not** copy unrelated TV instructions such as `MstarUpgrade.bin` and hold Power: they are board-dependent.

The previously inspected full 8054 factory PKG install script included destructive steps such as `gpt clean mmc 0`, `wipeuserdata`, erase of `persist`, and `eeprom_a`. Blindly attempting a factory USB upgrade can erase focus/ToF/calibration and result in an unrecoverable board state. Keep a board-matched factory rescue package and backups before the first flash.

## 5. Unlock and recovery feasibility

`ro.oem_unlock_supported` and `ro.boot.flash.locked` were absent or empty in the supplied diagnostics. The presence of A/B partitions and `userdebug` does not prove that standard Fastboot is implemented. Before even entering bootloader/recovery, determine the supported command path, user-accessible reset/recovery and a return-to-stock procedure.

AOSP supports a read-only `fastboot flashing get_unlock_ability` query on implementations that provide Fastboot. This command is *not* a reason to invoke `fastboot flashing unlock`; unlocking can wipe user data, and on this projector it could be unsupported. Avoid `fastboot flash`, `fastboot erase`, `adb disable-verity`, `adb remount` and `dd of=/dev/block/...` during research.

## 6. Practical build paths, from safest to most experimental

**Path A — no flash, reversible:** Custom launcher, carefully selected third-party app removal/disable per user, improved TvSettingsPlus, and preserved OEM hardware services for ToF/focus, thermal/fan, keystone, HDMI and native MediaTek interfaces. Validate each change and rollback. This meets many usability goals without AVB implications.

**Path B — modified stock-based A/B OTA:** Keep exact 8054 kernel/device tree/vendor firmware and board-specific services; edit only a controlled, minimal userspace partition; rebuild filesystem and AVB chain with compatible trusted signatures, generate a correctly signed A/B payload, and test only after factory recovery is proven. Requires deciding trusted signing approach; not currently flashable.

**Path C — bootloader factory PKG:** Understand PKG verification, board identity checks, update script commands, preboot USB activation and recovery. A safe custom PKG is not proven. This can be more dangerous than A/B OTA due to unconditional partition wipes.

**Path D — unlocked Fastboot + custom ROM:** Only if bootloader has a supported unlock flow and device recovery. With 32-bit Android and proprietary MediaTek TV HALs, a full LineageOS port has materially greater engineering cost than a stock-based OS build.

## 7. Read-only owner tests next

Commands **inside `adb shell`** (none writes Android settings or firmware):

```sh
id
su --help 2>&1
su 0 id
getprop ro.oem_unlock_supported
getprop ro.boot.flash.locked
getprop ro.boot.vbmeta.device_state
getprop ro.boot.verifiedbootstate
getprop ro.product.ab_ota_partitions
ls -l /system/etc/security/otacerts.zip /vendor/etc/update_engine /system/etc/update_engine 2>&1
```

Optional from Windows CMD, *outside* the shell (restarts `adbd`, may briefly disconnect):

```
"C:\Program Files\platform-tools\adb.exe" -s PROJECTOR_IP:5555 root
```

Do not expose ADB on the public internet. If `su 0 id` or `adb root` reports UID 0, prioritize a controlled **read-only backup to the PC** of verified public/system images, and a separate private backup of device-unique critical partitions. No raw partition image should be published in a public repo.

## 8. Decision gates before any flash

- Have a matching restore image for the exact hardware revision and a confirmed recovery procedure.
- Verify bootloader unlock behavior or identify independently trusted signing keys for the current device.
- Compare live `vbmeta` public key with the factory PKG key (and any published AOSP AVB test key); compare OTA public-key trust anchor and payload signer.
- Preserve exact board hardware configs/ToF, factory calibration, EEPROM and thermal services.
- Offline test an updated image, including AVB signatures, payload metadata, partition sizes and explicit targeting of 8054.
- Test install on disposable matching hardware, never on the only working projector before rollback is demonstrated.

**Status: custom firmware technically plausible; custom ZIP installation on the locked live projector unverified.**
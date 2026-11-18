# TvSettingsPlus 8054: operator INSTALL / RECOVERY runbook

**INSTALL STATE: NOT_READY_FOR_DEVICE_INSTALL.** Host assets can be READY_FOR_OWNER_REVIEW after offline checks. Neither CI, matching test-key nor this runbook authorizes device actions. APK code remains BUILT_UNTESTED. Keep PR draft. No automatic install/recovery executable is shipped.

## 1. What is pinned and what is not

| Asset | Exact provenance |
|---|---|
| STOCK | f8e0c6fe0f46e7eb8db651c31d85ef8b32a769b37e4dd0f96ed19a64725d74ee; system_ext_a/priv-app/TvSettingsPlus from owner backup |
| MOD | cf5bba9d90935952ea5a8a832ea0bd9662374e5cc10c052d5cdb6698e0ba1246; code476a39ac9a693d8cb1c9c1b5d7b49911f6967477; exact build44252085197, artifact14403668693 |
| Docs checkpoint | c90a1ca7d3efadd6475ab65b1f01f9d9857808bf is docs-only above476a39a, NOT the APK build commit |
| Firmware backup | Release447166291/tag FlixTone_8054_Backup; ZIP2011878129 bytes, SHA2564f53f2224c98b4c832a19a591b9dd7f925bde28528b5674a09732c0f0e41d362 |

`provenance.json` has full links/pins. `manifest.json` records kit assembly commit separately from APK code commit. This kit reuses the original MOD bytes; it does not rebuild/re-sign them. Only the stock APK is extracted from system_ext; no factory/key/boot data is published. No device identity, installed permission grants, SELinux allowance or PackageManager acceptance follows from these offline assets.

Download the entire kit ZIP before its Actions TTL expires. Preserve the ZIP, extracted kit (BOTH APKs, SHA256SUMS, manifest, scripts, reports) and a separate owner firmware backup on two independent offline host media. Verify the ZIP's GitHub artifact digest against its artifact record, then verify all extracted files. Actions artifacts are temporary delivery, never permanent rollback storage. Do not proceed if any asset is missing/expired or any checksum differs. Never silently substitute a newer build.

## 2. Ordinary workstation prerequisites and HOST-only verification

Linux/macOS/Windows workstation: Python3.10+, Android platform-tools ADB, Android SDK build-tools `aapt` and `apksigner`, Java17. Tools are not installed by the operator scripts. Prefer Windows `aapt.exe` and SDK `lib/apksigner.jar` (the Python runner invokes Java); a .bat/.cmd SDK wrapper is supported with guarded arguments. Shell scripts are not required, and these host Python tools are not Termux/device programs.

From the extracted kit, host-only commands (Windows may use `py -3` instead of `python3`):

```text
python3 verify_files.py --kit .
python3 verify_offline.py --kit . --aapt /path/to/aapt --apksigner /path/to/apksigner
```

For Windows, supply normal executable/JAR paths instead. The verifier uses `apksigner verify --verbose --print-certs`, `aapt dump badging`, `aapt dump xmltree ... AndroidManifest.xml` and `aapt dump permissions`. It compares actual APK signer digest, package ID, versionCode/versionName, min/target SDK, sharedUserId, requested/declared permissions (including protection attributes) and standalone/split constraints. Raw outputs and JSON are saved on the host, under /tmp on Linux and local temp on Windows. STOP on any mismatch, missing metadata or failed signature/tool invocation. No privileged grants are inferred from permission requests or a familiar AOSP certificate.

Expected: `PASS complete kit checksums`; offline `PASS ... READY_FOR_OWNER_REVIEW`, while install state stays `NOT_READY_FOR_DEVICE_INSTALL`. Missing live identity is an explicit proof gap, not a PASS for installation. `READY_FOR_OWNER_REVIEW` only means the host kit can be inspected.

## 3. Read-only device preflight — separate owner permission first

Only after approval to collect diagnostics (not approval to install):

```text
python3 preflight.py --kit . --serial APPROVED_ADB_SERIAL --adb /path/to/adb --aapt /path/to/aapt --apksigner /path/to/apksigner
```

Do not run this now without permission. Choose the serial explicitly. No root, remount, Activity launch, reboot, package/settings mutation or on-device temporary output is performed. `adb pull` reads files onto the workstation. All diagnostic stdout is captured on the host; Linux output paths must stay under /tmp. Android `/tmp` is not used; if a future approved procedure needs device files, use permitted `/data/local/tmp`, not the Linux host path. Reports can contain serial/build/package data: keep them private and do not automatically upload them to GitHub.

The collector records machine strictly8054, API33, fingerprint/build/release, selected user (owner0 only), shell identity, SELinux enforcement, package path/splits/version/flags/enabled user state, launcher/settings resolution, actual dumpsys/signers when visible, critical package inventory (KuSettings, KuSourceRJ, TvServices, RuiAutoFocus, KeystoneCorrection, AutoPair) and TimeService/MySetTimeService observations. It also saves pm/cmd package help and privileged permission/SELinux labels. Absence/denied service observation is STOP, not evidence that cooling can be replaced. Package-enabled PASS is not service/fan health proof.

Live BASE APK and fixed built-in system_ext APK are pulled only when accessible. Both must match the pinned STOCK bytes before a first install; stock/mod/live then undergo the actual offline metadata/cert comparison. If paths differ, APKs are unreadable, active version/shared UID is ambiguous, splits exist, fingerprint is unreviewed or user/package/SELinux state differs: STOP and inspect, no mutation workaround. A familiar platform test-key against backup is never proof of the live installed identity.

Exit1 means diagnostic FAIL; exit2 means diagnostics may pass but installation readiness is still NOT_READY. Reboot transport, recovery and owner gates are deliberately not auto-approved. The preflight does not issue any mutation commands.

## 4. Two candidate recovery routes — both NOT_VERIFIED on this device

| Route | Preconditions/evidence needed before writing exact device-specific commands |
|---|---|
| A: remove ONLY the installed system-app update, expose built-in stock | Actual device help must support package-specific system-update removal; live package is system/updated-system as appropriate; built-in APK readable and equals pinned stock; shared UID/signer/state reviewed; explicit package restriction; data/reset behavior reviewed; ADB survives restart |
| B: MOD UI fails, use out-of-band host ADB with preserved STOCK APK | ADB independent of Settings UI, tested on stock after a separately approved reboot; actual host/device PackageInstaller syntax supported; signer/version/shared UID/permission/split conditions match; stock replacement acceptance/data behavior and fallback baseline reviewed |

These are distinct operator procedures (update removal versus replacement from preserved host APK), but BOTH depend on ADB and PackageManager. They are not independent against failure of those shared components. No validated recovery for loss of both is established. STOP if ADB is not reliably available after reboot or a recovery route is unsupported; do not invent root/flash recovery. Neither route is currently executable/verified, so installation remains NOT_READY.

[AOSP Android13 reference](https://android.googlesource.com/platform/frameworks/base/+/android-13.0.0_r24/services/core/java/com/android/server/pm/PackageManagerShellCommand.java) documents `uninstall-system-updates [<PACKAGE>]`: omitting the package affects all system-app updates. This is a source reference, NOT permission or proof that the projector implements it. Only read-only `pm help` and `cmd package help` are collected. No runnable removal/replacement command is emitted until actual device syntax AND all preconditions are reviewed and owner-approved. The package argument must never be omitted.

Default recovery must never be `pm uninstall --user 0`, `pm disable-user`, `pm clear`, generic `adb uninstall`, factory reset, remount/flash or stopping OEM services. Data preservation is NOT assumed for either candidate; external original-value records are mandatory. No private app data or calibration backup is fabricated from Settings.Global.

## 5. Staged opt-in smoke test, NOT executed

1. HOST stage: verify assets + scripts + reports, store both APKs offline. Review baseline and all STOP conditions.
2. Separately approved read-only device stage: preflight, compare live/build/signing/permissions/UID/SELinux, record stable ADB serial/boot ID. Separately approve a STOCK reboot transport check; rerun preflight and compare boot IDs/build/identity, never ask the script to reboot.
3. Only after recovery evidence and **separate installation approval**: operator uses reviewed device-specific procedure, immediately verifies package/version/flags/signer and HOME/Settings D-pad/Back. Stop on ANR/crash/missing settings entry; use approved out-of-band recovery, not a package-disable workaround.
4. Observe original TimeService/thermal/fan, BT remote, LED behavior; no temperature is invented and no services are stopped. Opening critical projection/focus/brightness/HDMI pages may itself have effects — test each risk class only with corresponding permission. No calibration or settings changes are implied by navigation approval.
5. Independently approve any OEM setting change. Save original input/output/mode and values outside the page-local sound baseline; change one user setting, return in OEM UI, compare Global and physical result. Speaker/BT/ARC/eARC results are separate. CEC may affect eARC/auto-off and connected devices; digital zoom is separate from optics/LED/motor.
6. Reboot check and rollback drill require their own agreed scope. After return to STOCK, verify actual APK/hash, settings entry/HOME/D-pad, service/thermal/remote/LED behavior and recorded baseline. Unsupported/failed recovery is FAIL, never a reason to flash or clear data by default.

Record each stage/route as PASS/FAIL/NOT_TESTED, with original/final state, commands actually approved/executed, timestamp and observed result. All device stages/rollback routes are currently NOT_TESTED. No script in this kit can turn that status into install authorization.

## Sources and scope

[Android apksigner documentation](https://developer.android.com/tools/apksigner) supports actual APK verification/certificate reporting. The AOSP reference above describes platform command intent; OEM behavior still requires live evidence. This kit is an operator review artifact, not a production installer, firmware flash tool or claim of feature completeness.

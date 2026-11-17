# TvSettingsPlus MOD: exact-8054 experimental build

**Status:** CI build passed with C0 TimeService, C1 EDID, C2 orientation safety gates and C3 read-only AutoPair diagnostics; hardware behavior unknown. [GitHub Actions run 44105161286](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44105161286), source commit `4e263422fe72e133477b73405c735561408c4501` on `audit/8054-tvsettingsplus-20261117`. Earlier C0/C1/C2 build: run 44096540197.

| Item | Verified value |
|---|---|
| Owner backup ZIP SHA-256 | `4f53f2224c98b4c832a19a591b9dd7f925bde28528b5674a09732c0f0e41d362` |
| Original TvSettingsPlus 8054 SHA-256 | `f8e0c6fe0f46e7eb8db651c31d85ef8b32a769b37e4dd0f96ed19a64725d74ee` |
| Package / versionCode | `com.android.tv.settings` / `33` (`aapt dump badging`) |
| Output APK SHA-256 | `6306bbab1aa4c87fb40e103d07e5f61bc34d66dde30717af7c6f10431931017b` |
| Output signer SHA-256 | `c8a2e9bccf597c2fb6dc66bee293fc13f2fc47ec77bc6b2b0d52c11f51192ab8` (AOSP platform test-key, matched to stock certificate) |
| Output artifact | `tvsettingsplus-8054-audit-source-experimental`, artifact ID `14337044931`, expires 2026-12-01 |

The run compiled all MOD Java against Android API 33, built DEX, patched the expected OEM smali/Manifest routes, repacked, signed and verified the APK. It ran JVM tests including the 8054 TimeService, EDID, orientation and Bluetooth diagnostic policies. Source safety run 44105161251, optical compile run 44105161255, OEM capability run 44105161455 and v33 board-parity run 44105161444 all passed on the same source commit. The CI result does not prove that Android PackageManager will accept an update over the installed stock APK, that shared UID and SELinux allow runtime calls, or that picture, sound, HDMI, Bluetooth, optics and fan behavior work on the physical projector.

**Rollback preparation:** retain original 8054 APK and its hash, current package/service state and an ADB access path. Do not install this candidate or disable KuSettings before a separately authorized hardware test plan and verified restore path. No device action occurred in this run.

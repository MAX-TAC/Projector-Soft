# ARCHITECTURE REQUIREMENT — independent TvSettingsPlus MOD, no Chinese settings runtime UI

**Owner clarification (2026-11-20):** final target is to RETIRE Chinese settings UI. STOCK is **reference for reverse-engineering and controlled comparisons only**, never a runtime mediator of migrated MOD controls. This document supersedes ambiguous wording `STOCK action → OEM handler → MOD handler` in research notes.

## Required runtime call chain for a migrated feature

```text
User / remote
   → TvSettingsPlus MOD (single agreed settings UI, native preference / slider)
   → OWN adapter/handler in MOD (copied/reimplemented OEM-equivalent logic, no stock Activity)
   → VERIFIED lower-level backend: Android Settings Provider / permitted OEM Java service /
     TvServices observer / Binder / platform API, as appropriate for this exact feature
   → MediaTek or vendor hardware service / HAL
   → projector hardware
```

**STOCK original Chinese UI/Controller is NOT in this runtime chain.** Static decompilation of stock handlers is allowed for discovery. Never implement `MOD → startActivity(com.my.settings.*)` or invoke an OEM *UI* screen as a substitute for a migrated preference. A cross-app service dependency must be inventoried explicitly; any backend still owned by the package being retired is a migration blocker, not a final implementation.

### Example PIC04 contrast

- Study original Chinese/Android TV callback **offline**: it writes `Settings.Global.picture_contrast` and TvServices observes/mirrors into `g_video__contrast`; confirm the other stock conditions and asynchronous handling.
- Implement MOD's **own** UI and click/step handler. When an authorized device test has established the correct effect/scope/rollback, invoke the confirmed backend directly from MOD (for this key potentially normal `Settings.Global.putInt`, subject to all guards), not `PictureModeValueSecondaryController` or the Chinese app.
- TvServices is an **independent required backend service**; retaining it does not mean retaining the Chinese UI. Shared backend services may remain installed, subject to actual package ownership.
- The current MOD v41 PIC04 is READ_ONLY and lacks runtime verification. Do not imply the above runtime chain is already implemented or safe on every HDMI/profile.

## Distinguish UI removal and whole-package retirement

**A. Migrated UX independence (product milestone):** all agreed consumer functions are handled within TvSettingsPlus MOD without Chinese UI Activity, fragments, or hidden proxy launches; Android platform system screens (Wi-Fi, apps, accessibility etc.) may still be delegated ONLY if explicitly agreed and identified as platform settings rather than the Chinese UI. OEM factory/calibration workflows are separate scope, not disguised migrated user settings.

**B. Whole Chinese package retirement (separate safety milestone):** remove / disable `com.my.settings` only after all external component dependencies have been inventoried and replaced/tested. A hidden background service still in the same package means full uninstall/disable is NOT safe. Do not mistake 'no UI dependency' for 'package independent'.

Exact-8054 evidence: `com.my.settings` boot path `com.my.service.BootupReceiver → MySetTimeService → KuSourceRJ.TimeService` periodically restores `KuSourceRJ.TimeService`. The latter is relevant for thermal/fan safety and returns `START_NOT_STICKY` in previous investigations. The prepared MOD recovery receiver/service are disabled and NOT hardware-qualified. See `research/8054/KUSETTINGS_SERVICE_MIGRATION_8054.md` and `docs/8054_KUSETTINGS_MIGRATION.md`. Therefore **do not disable/uninstall KuSettings now**.

Other retained owner components (`com.coolux.source` KuSourceRJ, RuiAutoFocus, KeystoneCorrection, TRD services, TvServices) are distinct from the retired GUI and must not be indiscriminately deleted.

## Acceptance tests per migrated setting

1. Static source proof of original exact-8054 STOCK callback, its direct *backend* contract, preconditions and side effects.
2. MOD implementation calls the backend from its own handler; no runtime `Intent`/`startActivity`/`ComponentName`/reflection leads into Chinese UI package for this user control.
3. Independent package-level static reference audit of MOD DEX/manifest/intent resolution. Note that a static scan cannot guarantee absence of runtime implicit resolution.
4. Separate authorized hardware test with original context (source/profile/output), readback and visible/audible effect, manual restoration.
5. Separate KuSettings system service retirement program: boot/cold boot, TimeService crash recovery, remote Bluetooth, HDMI/video/sound/thermal/watchdog checks and rollback. Do not gate basic MOD UI behavior on premature package removal.

## Direction for agent

Do PRODUCT-001 / PIC04 as an END-TO-END owner-independent feature. Preserve exact original OEM lower backend, but make the MOD OWN its user handler, never proxy the stock Chinese UI. Log every investigation/decision in GitHub. Research NATIVE-005b only for a real unresolved product blocker.

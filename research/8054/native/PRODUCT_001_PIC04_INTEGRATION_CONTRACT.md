# PRODUCT-001 — PIC04 «Контрастность»: контракт OEM→MOD (exact-8054)

**Date:** 2026-11-20. **Product milestone:** `CONTRACT_STATIC_CONFIRMED / MOD_RUNTIME_NOT_ENABLED / HARDWARE_NOT_TESTED`.  
**Start refs:** research PR #3 `b210ffc5c9094796fd0d560c1f3971ce6364cf54`; MOD v41 PR #2 `fbb41e92a47a94a2a4b0f4122100e5a1cb43cfe5`.  
**Scope:** **PIC04 only**. NATIVE-005b is **DEFERRED**. No app install or writes on projector were performed; this report is not evidence of actual physical contrast change.

## A. Architecture and isolation

**Target runtime:** `MOD NativePicturePanel / its own step handler → Android Settings.Global("picture_contrast") → independent TVMenuSettingsService.SettingObserver → TVSettingConfig.setConifg("g_video__contrast", value) → MtkTvConfigBase → MediaTek backend → hardware`.

Chinese `com.my.settings` UI/controller is **offline evidence only**. **Never** call `PictureModeValueSecondaryController` or launch Chinese Activity as the MOD's runtime bridge; old GUI is intended for retirement. Android TV OEM services and TvServices are a separate dependency; must retain them. Uninstalling/disabling the entire `com.my.settings` package is a *different* safety milestone because `MySetTimeService` recovers KuSourceRJ.TimeService; do not disable it during PIC04. See [runtime architecture](RUNTIME_ARCHITECTURE_NO_CHINESE_UI.md).

## B. Exact stock method and callback

| Level | Evidence and method (original 8054 static smali) | Consequence |
|---|---|---|
| Chinese reference | `PictureModeValueSecondaryController$1–3.onValueChanged` → `SettingsUtil.putGlobalValueInt("picture_contrast", int)` → `Settings.Global.putInt` (SettingsUtil smali ~103) | Chinese controller is not required by MOD at runtime; its write is to the same provider |
| Android TV stock | `PictureActivity.createSettingsFragment` → `partnercustomizer.picture.PictureFragment` → `PictureFragment.onPreferenceChange` (3527–3576) → `onPfChange` (~2003–2028) → `PreferenceConfigUtils.updatePreferenceChanged` (4612–4633) → `putSettingValueInt` (~326) → `Settings.Global.putInt(resolver, preference.key, value)` | Stock callback supplies **provider key and scalar int**; not `inputGroup` or explicit HDMI/profile ID |
| Alternative stock editor | `ProgressBarVerticalFragment.onPreferenceChange` (~1364) calls same `PreferenceConfigUtils.updatePreferenceChanged`; `createNewProgressPreferencesss` (~979–987) passes preference key/config ID | Avoid launching this fragment from MOD; its lifecycle has extra OEM effects |
| Observer | `TVMenuSettingsService.SettingObserver.onChange` (~171–176) → `updateConfigData` (~5033–5299) maps `picture_contrast` to `g_video__contrast` (`initMapdata` ~2361–2365) → `TVSettingConfig.setConifg` → `MtkTvConfigBase.setConfigValue(configId,value)` | **Asynchronous** Settings.Global→MediaTek apply; callback does not convey native result to writer |
| Native boundary (prior) | `TVNativeWrapper.setConfigValue_native(-1,id,value,3)`; NATIVE-004 confirms JNI BLX 0x56726 → PLT/GOT `a_mtktvapi_config_set_value`; NATIVE-005 confirms independent service→RPC import | Proves call machinery but **not input/profile address or video effect** |

Primary repo evidence: [PICTURE_BLOCK_STATUS](../PICTURE_BLOCK_STATUS_8054.md), [TRACE_001](TRACE_001_PICTURE_CONTRAST.md), [TRACE_004](TRACE_004_CONFIG_JNI_TO_HIDL.md) and [PRODUCT_FOCUS_REVIEW](PRODUCT_FOCUS_REVIEW_2026-11-20.md).

### OEM availability and incidental behavior (do not flatten)

- `PictureFragment.createPreferences` obtains `PartnerSettingsConfig.getSettingsList("picture_effects")`; list membership controls whether the preference is present. For contrast and saturation it checks `PreferenceConfigUtils.isVGASource` (~404 / ~449); `SaveValue.readWorldInputType == 16` is used by this gate. **VGA condition is not a definitive active-HDMI identifier.**
- `updateSomePrefEnabled` has `picture_mode=14` gating for **saturation**, not automatically proof of an equivalent contrast prohibition.
- `onPfChange` can issue BACK for a nonempty `mStartKey`. OEM's separate `isSupportPowerConfirmed/isNotFirstChange` branch may prompt on the first change; exact relevance for PIC04 must be checked on stock. `PictureFragment.onCreatePreferences` also invokes `disableAutoBackLight` and `updateOLEDVisible`; opening/modifying contrast **must not** silently reproduce those whole-fragment effects.
- `PictureFragment.onResume` refreshes summary/Dolby/availability. A provider write does not itself re-run OEM fragment lifecycle.
- When `picture_mode` or `picture_color_temperature` changes, `TVMenuSettingsService.updateSettingDB` / `TVAsyncExecutor.execute` may reread MediaTek config and write it back to Global later. An instant Global echo is not a completed operation.
- **Potential first-change prompt** (power behavior), auto-backlight, Dolby/HDR and source switching remain relevant observations in the single STOCK test. Do not infer other side effects as definitively caused by PIC04.

### Focused re-check of exact STOCK smali (existing successful CI logs)

Existing [run 44352714404](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44352714404), job `134972414645`, contains the complete selected `PictureModeValueSecondaryController$2.onValueChanged(I)V` at `smali_classes14/.../PictureModeValueSecondaryController$2.smali:38`. It stores the new integer in `contrastValue` via `access$202` and at lines **59–61** invokes `SettingsUtil.putGlobalValueInt(resolver, "picture_contrast", contrastValue)`. That util calls `Settings.Global.putInt` (smali 103), *discarding its boolean result*.

The original controller `initView` locates `TitleSeekBarView` for contrast at resource `0x7f0a04a5`, initialises it with `setValue(contrastValue)` (smali ~288–292), then installs `PictureModeValueSecondaryController$2` as its `OnValueChangeListener` (~334–340). In `initData`, `SettingsUtil.getGlobalValueInt(resolver,"picture_contrast",0x32)` uses **50 only as the missing-provider fallback value**, not as a documented minimum, maximum or D-pad step. These focused static observations confirm that a lightweight MOD-owned provider writer is architecturally possible without any Chinese UI runtime dependency. The internal `TitleSeekBarView` key increment was not derived from the reviewed log, so remains unknown until exact key behavior is measured.

## C. Original value range and step: verified vs missing

**Source of truth for actual value range on 8054**: `TVSettingConfig.getMin("g_video__contrast")`, `getMax("g_video__contrast")`. Current MOD already reflects both and reads `getConfigValueInt("g_video__contrast")`. It also compares the MediaTek returned integer to Global `picture_contrast` before opening. **No numeric min/max is verified for all active sources/profiles; do not hardcode `0..100`**. Validate `min <= current <= max` and `min < max`.

**Step:** stock D-pad increment for `ProgressDialogPreference`/the vertical editor was not proven numerically by static evidence in reviewed materials. Android standard ProgressBar max/min do not establish `keyProgressIncrement`; MOD uses a `ProgressBar`, *not a `SeekBar`*, inside `partner_progressbar_dialog.xml`. **No fixed `+1` assumption is authorized** until actual stock key press establishes displayed/ACFG delta. One stock single-step test should record `before, after, delta`. If delta is not stable/greater than zero or mode changes, stop.

Do not confuse `isUpdate=3` (native payload flag, semantics unknown) with HIDL transaction code 3. For PIC04 MOD should *not* call either directly: matching the stock provider path avoids exposing their parameter semantics to the product UI.

## D. Exact MOD v41 implementation and missing operations

Source PR #2 `app/tvsettingsplus-mod/src/exact8054/java/dev/projectorsoft/tvsettings/NativePicturePanel.java`:

| Existing (keep) | Required minimal change for permitted candidate |
|---|---|
| Current row `picture_contrast` uses existing `ProgressDialogPreference`; lower editor loads original layout `0x7f0d0142`, progress/name/value IDs `0x7f0a013c/013a/013b`; no Chinese GUI launch | Retain row, lower dialog, controls and Back; add **MOD-owned** contrast-specific D-pad step listener in candidate, not OEM old Activity/controller. |
| `showSlider` reads immutable provider snapshot, `TVSettingConfig.getMin/getMax/getConfigValueInt` and checks Global==ACFG; `getMin<getMax`, snapshot stable | Recheck immediately before each change, not only at open. Capture source + format + profile in same verified context; cancel on transition. |
| Dialog consumes LEFT/RIGHT/CENTER in `Dialog.setOnKeyListener` without writing. Other sliders are intentionally view-only | Wire LEFT/RIGHT exclusively for PIC04 to a capability-guarded handler. Return/Back must close without replaying possibly stale values. Avoid duplicate OEM `ProgressDialogPreference.onClick` dialogs. |
| `PicturePanelState.read` compares mode/temperature/HDR at start/end; observes 12 provider keys | This does **not** determine active TV input, real SDR/HDR payload or switching-in-progress; need an independent, validated source/profile provider before enabling an actual write. |
| `OemPictureState.mayEdit()=false`; `PictureStateTransaction.verifiedContext()=null` | **Do not flip global mayEdit to true**; implement a separate, PIC04-specific guarded pilot. Respect durable baseline journaling. |
| `showSlider` sets `ProgressBar` read-only, title ` · только просмотр`, hides parent menu and restores it on dismiss | Update bar/value only after settled backend acknowledgement, never after a successful `putInt` alone. If inconsistent, show error / stop editing / preserve journal. |

**No change should activate other PIC keys**. PIC03, PIC05, HDR, MEMC, RGB, reset, Factory API remain unchanged.

## E. Proposed precise PIC04 handler contract (software gate, not runtime proof)

1. `canStart` only on exact board 8054, stable mode/temp/HDR snapshot, **verified actual main input**, signal SDR/HDR, OEM user picture profile, allowed OEM picture_effects membership/VGA gating, and `min/max/current` from OEM. A Global key is a cache, **not an input/profile identifier**. Missing any source identifier → explicit `BLOCKED_CONTEXT_UNKNOWN`.
2. Read Global and `TVSettingConfig.getConfigValueInt` twice with a settling interval. Require both stable and equal; keep `baseline` scoped to the verified context. Do not guess the initial value.
3. On **one** LEFT/RIGHT press, compute new value from a **confirmed OEM increment** and clamp/deny at OEM min/max. No repeat/rapid queue until prior write settled.
4. Before write, repeat verified input/mode/signal/profile and backend reads; if mismatch, refuse without write. Persist a durable record `context + original value` in a **PIC04-only** journal; refusal to save journal must block the write.
5. Write exactly once via `Settings.Global.putInt(resolver, "picture_contrast", desired)` on permitted test build. This invokes the independent TvServices observer. **Never** call Chinese UI, `TVNativeWrapper`, MediaTek Factory/HIDL directly.
6. Treat `putInt=true` as provider admission only. Await settled repeated Global **and** MediaTek OEM getter values in the **same verified context** and compare to desired. Unknown timeout / mismatched values → `UNCONFIRMED`, disable further edits and retain baseline. **No blind automatic replay** if context changed.
7. Back closes existing bottom dialog; do not roll back automatically on Back or destroy. A separate, explicitly authorized manual STOCK restore using baseline is the safe fallback until multi-profile rollback evidence is established. Keep the journal until context + native readback confirm restored original.
8. No code may report `HARDWARE_EFFECT_VERIFIED` unless owner observes changed picture and restoration in the projector, not merely Global and ACFG agreement.

## F. Single minimal owner-controlled STOCK test (not executed)

**Current blocker: UNKNOWN active destination/profile, plus unverified true stock D-pad increment and actual effect.** One controlled **stock contrast edit with restoration** can assess all three without direct HAL/Factory writes.

**Before any action:** explicit separate owner approval; verify STOCK APK path/SHA and 8054/API33. Use already existing read-only `install-kit/preflight.py --picture-stock` to capture local snapshots; this tool writes only to the operator's computer (not projector). No custom ADB setter invented.

1. On a fixed source (prefer **HDMI1, steady SDR**), record time, visible source indication, picture mode, HDR/temperature and original stock contrast label/value **C0**. Run a read-only before snapshot. Record OEM min/max/config via already available authorized stock UI/Java if exposed; shell has **no verified MediaTek getter**.
2. Using **STOCK UI and remote only**, press RIGHT **once** and record the actual displayed delta (not presumed +1), any confirmation dialog or other side effects; wait until stable. Run after snapshot. Observe a static test image: did contrast **visibly** change? Distinguish subjective observation from objective config readback if unavailable.
3. Without switching source/mode mid-write, press LEFT once or use STOCK to explicitly restore **C0** in the **same** displayed context; wait and capture restoration snapshot. Confirm visual restoration. **If step or original value is ambiguous, do not start**, or stop further changes and restore manually in stock.
4. To determine scope safely, do **read-only observations only** after restoration by re-entering the stock panel on HDMI2 or a second picture mode, then returning to the original HDMI1/mode and checking C0 again. Do not edit the second source. If source/profile identity or stability cannot be determined from stock UI, record `UNKNOWN` and keep MOD write disabled.

Stop on unexpected input switching, auto mode reset, loss of original value, different after reading, no restore, user-visible warning or device instability. Do **not** use `settings put global`, `su`, service calls, installation, I²C, PQ Factory, EDID/HPD or power operations for this test.

Existing commands, no new execution: [STOCK_PICTURE_DIAGNOSTICS_8054.md](../STOCK_PICTURE_DIAGNOSTICS_8054.md). The exact one-shot ADB preflight command under the operator's machine is `python3 install-kit/preflight.py --picture-stock --serial APPROVED_SERIAL --out /tmp/stock8054-pic04-before`; use different output folders for after and restore. Before operating, check preflight's warning about side effects of installed MOD and don't install it as part of this experiment.

**Decision after test:** if no reliable HDMI/profile identifier or independent readback can be obtained, one stock success is **not** sufficient to enable general MOD writes: log `BLOCKED_CONTEXT_UNKNOWN`. If source/profile contract and apply/restore confirmed, enable **only PIC04** in a separate implementation branch based on actual PR #2, with device tests and rollback. Other 95 functions unaffected.

## G. Status and next owner action

- `STOCK_JAVA_ROUTE`: **STATIC_CONFIRMED**.
- `OEM_BOUNDS_API`: **STATIC_CONFIRMED**; numeric min/max **NOT_CAPTURED**.
- `OEM_STEP`: **UNKNOWN**.
- `GLOBAL_TO_NATIVE_SETTER`: **STATIC_CONFIRMED** (prior NATIVE-004).
- `MOD_LOWER_UI`: **BUILT_READ_ONLY**.
- `PIC04_STEP_POLICY`: **SOFTWARE_IMPLEMENTED / TESTED** in separate [Draft PR #4](https://github.com/MAX-TAC/Projector-Soft/pull/4), pure-Java `Pic04ContrastStep` with no production writer binding. [CI 44808298783](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44808298783) **SUCCESS** (Android source compilation and JVM policy test). It explicitly requires a trusted independent source/signal/profile/generation, OEM increment and durable baseline.
- `MOD_PIC04_WRITE`: **NOT WIRED / NOT ENABLED**. v41 lower dialog remains read-only in both PR #2 and the experimental PR #4; no compiled hardware candidate claiming working PIC04 has been produced.
- `INPUT_PROFILE_CONTEXT`: **UNVERIFIED**.
- `MEDIATEK_READBACK_RUNTIME`: **NOT_TESTED**.
- `HARDWARE_EFFECT_VERIFIED`: **NOT_TESTED**.

This is a **bounded product blocker**, not a reason to resume generic NATIVE-005b. Next interaction requested from owner: **one explicitly approved manual stock one-step contrast test**, with before/after/restore values and context as above. No privileged writes in this session.

All substantive actions and code provenance: [PRODUCT-001 session log](logs/2026-11-20_PRODUCT-001.md).

## H. Separate implementation branch — actual code and limits

- New branch based **exactly** on PR #2 HEAD `fbb41e92a47a94a2a4b0f4122100e5a1cb43cfe5`: [feature/8054-product-001-pic04-20261120](https://github.com/MAX-TAC/Projector-Soft/tree/feature/8054-product-001-pic04-20261120); isolated [Draft PR #4](https://github.com/MAX-TAC/Projector-Soft/pull/4) targeting `audit/8054-tvsettingsplus-20261117`.
- Production source [Pic04ContrastStep.java](https://github.com/MAX-TAC/Projector-Soft/blob/feature/8054-product-001-pic04-20261120/app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/Pic04ContrastStep.java) and [Pic04ContrastStepTest.java](https://github.com/MAX-TAC/Projector-Soft/blob/feature/8054-product-001-pic04-20261120/app/tvsettingsplus-mod/src/test/java/dev/projectorsoft/tvsettings/Pic04ContrastStepTest.java); CI workflow updated to compile pure Java policy and execute test.
- `Pic04ContrastStep.Port` is an injection-only boundary with `read(), preserveOriginal(), submitGlobalContrast()`. It does not call any Chinese UI, settings provider or native code itself. `Port` for actual device **not supplied or installed** because no verified independent main-input/profile contract exists. The policy refuses unknown context, mismatched Global/MediaTek, missing OEM increment, invalid limits, journal failure and concurrent pending changes. A provider echo yields `PENDING`, only stable double Global+MediaTek readback yields `CONFIRMED_BACKEND_ONLY`, never `HARDWARE_EFFECT_VERIFIED`. On change/failure it quarantines the transaction rather than blindly replaying a snapshot.
- [Successful GitHub Actions run #44808298783](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44808298783) contains `Android bridge compiled to classes.dex` and `PASS PIC04 contract: one step, stable context, durable baseline, async readback, no blind restore`. This confirms source compilation and policy tests, **not APK install, source detection or image effect**.
- The actual existing editor `NativePicturePanel` **was not changed**. PR #4 should not be merged as a completed feature; next step is the single owner-authorized STOCK test plus independent verified input context, then wire its working Port into existing lower slider and run Android/device tests. No edits to PR #2 or Chinese GUI.

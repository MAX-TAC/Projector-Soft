# PRODUCT FOCUS REVIEW — 2026-11-20 / Return from native reverse engineering to working user controls

**Scope:** advisory review requested by owner; no app code, firmware or projector was modified. Review basis: research PR #3 HEAD `87a912e8a95d6599527a7f103128472031eda79a` and MOD v41 PR #2 HEAD `fbb41e92a47a94a2a4b0f4122100e5a1cb43cfe5` at review start. Recheck current HEAD before implementing.

## Decision: DEFER NATIVE-005b, prioritize one working user-facing control

Existing NATIVE-004/005 findings are real and useful: machine edge JNI → a_mtktvapi_config_set_value proven; service-side a_mtktvapi_config_set_value callsite identified. Their work is not mistaken. But the next proposal in HANDOFF — additional C++ vtable dispatch, HIDL onTransact → implementation, RPC and sentinel analysis — is not yet connected to a concrete failure of MOD v41. PR #3 touches only research/scripts/reports, no `app/` paths. Product priority is to close one specific user setting with a verified OEM-equivalent behavior and explicit rollback.

### Evidence-based prioritization

| Item | What exact-8054 already provides | MOD v41 state / remaining evidence | Decision |
|---|---|---|---|
| PIC04 Contrast | Chinese UI and Android TV settings both write Settings.Global `picture_contrast`; TvServices observer maps to `g_video__contrast` and calls MtkTvConfigBase → confirmed native config setter. | `NativePicturePanel` renders read-only bottom editor. Original backend may already apply provider changes; destination per HDMI/SDR/profile, stable state and manual rollback not yet runtime confirmed. | **Pilot end-to-end vertical slice**. Do not require complete HAL reverse-engineering up front. |
| PIC03/PIC05 brightness/saturation | Same observed Settings.Global → TvServices native config pattern for known keys. | Both READ_ONLY; source, mode and availability caveats. | Follow pilot only after PIC04 validated. |
| PIC11 HDR / PIC12 MEMC | Static provider keys and config IDs exist. | READ_ONLY; may have special signal/profile dependencies. | Inspect each stock callback and permissions separately; do not assume all mimic PIC04. |
| SND user EQ | Global and MediaTek reads and edit gating exist. | Built but hardware effect untested; Bluetooth/ARC/speaker routes differ. | Separate owner-authorized one-band one-step test in User after safe UI preflight. |
| Native HIDL internals | JNI→C and service→RPC edges partly proven statically. | HIDL handler chain incomplete; no runtime check. | Archive present results; investigate further only if specific exposed OEM behavior cannot be explained or reproduced. |

### Why this shift

The product contract is: `STOCK user action` → correct OEM write/callback → observable applied behavior, persistence and rollback within the same input/profile/output. Knowing vtable address points is only necessary when this contract cannot be established through Java/DEX API and actual stock behavior. Discovery of an exported factory or HIDL symbol is not authorization to call it directly.

## NEXT TASK: PRODUCT-001 / PIC04 Contrast as vertical slice

**Phase 0: evidence audit, no device writes**
1. Recheck PR #2 and #3 current SHA; read `research/8054/PICTURE_BLOCK_STATUS_8054.md`, `research/8054/FINAL_FUNCTION_PARITY_8054.csv`, `docs/8054_PQ_EQ_V41_TEST.md`, `TRACE_001`, `TRACE_004` and exact MOD `NativePicturePanel`.
2. Extract exact stock onChange/click/write branches and all UI preconditions/side effects; verify that normal OEM contrast uses Settings.Global rather than Factory calibration or raw JNI.
3. Determine expected Global key, MediaTek config ID, OEM min/max, D-pad step, control availability by current mode/source, async readback settling, and how to detect input/profile/format changes.
4. Identify what is **already sufficient** to implement via stock provider/Java route; specify exactly which ONE unknown prevents safe pilot. Do not spend a new research cycle on C++ vtables without an evidence-based need.

**Phase 1: hardware-read-only baseline with owner agreement**
5. Supply minimal, non-invasive ADB reads and UI checks to establish version installed (STOCK vs MOD), input HDMI1/HDMI2/HOME, SDR/HDR, current picture mode and baseline `picture_contrast`. On the system-side MediaTek getter, use only proven available app/service API; don't invent an `adb shell` getter.
6. Check that opening a settings panel does not itself perform unexpected writes or change the video output. The current v41 launch can invoke Android inactivity `ensureNever` policy: preflight of app-install/startup side effects and recovery is required **before** new hardware install/test.

**Phase 2: controlled stock one-step test — ONLY after explicit owner authorization**
7. On one fixed known source/mode/profile, user manually increases STOCK contrast by exactly one OEM increment, waits until settled, records Global and MediaTek getter readback plus visible effect, then returns it using STOCK UI and confirms restored baseline.
8. If scope is ambiguous, getter differs, or rollback fails, stop, preserve evidence and precise blocker; do not enable MOD setter.

**Phase 3: MOD integration after adequate evidence**
9. In a new implementation branch based on current PR #2 (NOT in this research PR), wire the existing lower editor to the **same proven OEM-level action** with fresh read-before-write, capability, mode/source guards, consistent async UI refresh and explicit fault/rollback handling; avoid direct JNI/factory/HIDL call. Maintain existing v41 navigation/layout.
10. Add focused JVM/Android tests for allowed/blocked states, stale input/profile, readback not matching, and cancel/Back; avoid claiming software tests verify hardware.
11. Present a small instrumented APK candidate for separate owner-authorized hardware validation, do not claim WORKING_RUNTIME without a successful test and restoration.

**Required written result:** `research/8054/native/PRODUCT_001_PIC04_INTEGRATION_CONTRACT.md` (stock method, MOD adapter and missing runtime evidence); updated 96-function audit status for PIC04 *only after corresponding evidence*; dated session log and GitHub commit/PR URL. Other PIC/SND/CEC items must not be silently marked working.

## Completion contract for each further user function

- Exact STOCK handler and full entry context identified.
- Existing public/privileged Java/OEM route reused, only when validated.
- Every write paired with stable readback from the right backend and same context; don't mistake Global echo for a DSP/PQ effect.
- UX D-pad/Back and visual equality with agreed v41 menu retained.
- Approved test verifies actual projector behavior, durability where applicable, and manual restoration.
- Logs, failures, sources, verified SHA and commit URL recorded in GitHub.

## Scope boundaries

No production change, flashing, runtime `settings put`/`setprop`, Factory calibration, EDID/HPD, fan/thermal actuator, I²C writes or audio PEQ save without separate owner authorization. Do not re-run comprehensive 1,283-lib inventory or pursue generic HIDL internals by default. Owner explicitly wants working user controls rather than maximal understanding of low-level firmware.

**Important distinction:** this review is a proposed product priority, not proof that `Settings.Global.putInt(picture_contrast)` is safe on every input/profile. It must be verified through narrowly scoped OEM behavior and owner-approved hardware testing.

# REVIEW — PRODUCT-001 / PIC04, Research PR #3 vs Implementation PR #4

**Review at 2026-11-20.** Evidence base: research PR #3 commit `a0ac04d476c70c3b25934781d554c5b4e65bf1dc`, implementation PR #4 commit `4c3f68e5bda55dda9a38c232df2667898a456d97` (both independently verified via GitHub connector). No production modification, no projector use.

## Summary verdict

**Useful, honest preparatory work; not a complete user feature.** Research contract is specific, exact-8054 based, and avoids Chinese `com.my.settings` UI at runtime. PR #4 creates a pure-Java guarded one-step transaction policy with fake-port tests. Production `NativePicturePanel` and D-pad are **unchanged**, there is no live `Pic04ContrastStep.Port`, no Settings.Global writer wired to the MOD screen, and no trustworthy live source/profile provider. Hardware effect remains NOT TESTED. Do not merge or describe it as a working contrast setting.

## Source references

- [Contract](PRODUCT_001_PIC04_INTEGRATION_CONTRACT.md) and [2026-11-20 product journal](logs/2026-11-20_PRODUCT-001.md).
- [PR #4](https://github.com/MAX-TAC/Projector-Soft/pull/4), [`Pic04ContrastStep.java`](https://github.com/MAX-TAC/Projector-Soft/blob/4c3f68e5bda55dda9a38c232df2667898a456d97/app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings/Pic04ContrastStep.java), [`Pic04ContrastStepTest.java`](https://github.com/MAX-TAC/Projector-Soft/blob/4c3f68e5bda55dda9a38c232df2667898a456d97/app/tvsettingsplus-mod/src/test/java/dev/projectorsoft/tvsettings/Pic04ContrastStepTest.java).
- [Existing read-only lower dialog](https://github.com/MAX-TAC/Projector-Soft/blob/4c3f68e5bda55dda9a38c232df2667898a456d97/app/tvsettingsplus-mod/src/exact8054/java/dev/projectorsoft/tvsettings/NativePicturePanel.java), with `Dialog.setOnKeyListener` consuming LEFT/RIGHT/CENTER.
- [Successful compile/JVM run](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44808298783); job `verify` passed compilation and pure Java tests.

## What is genuinely tested

| Area | Actual evidence | Limitation |
|---|---|---|
| One-step bounds and direction | Pure-Java fake Port test exercises +1/-1 direction and simulated increment `2` | Increment `2` is a **test fixture**, not a verified OEM D-pad step |
| Input/profile guard | `Reading.contextIsVerified()` and `same()/sameScope()` compare caller supplied strings and generation | `trusted=true` is supplied by Fake/Port; **no independent real context provider exists**. No proof of HDMI1/2 or SDR/HDR detection |
| Provider/MediaTek agreement | Fake `global` changes before Fake `mtk`; result PENDING until both equal | Neither Settings.Global nor MediaTek getter was invoked; result only simulates backend progression |
| Durable baseline | `Port.preserveOriginal(scope,value)` required before call, tested false path | No real storage, atomicity/flush, retrieval or cleanup implementation. `scope()` includes `generation`—needs a defined meaning stable across edits |
| Safe failures | Protects pending requests, lost context, invalid bounds, provider failure, detach without blind restore | A recovery/rollback user workflow and UI timeout/feedback are not implemented |
| Hardware effects | No hardware test | No visual PQ effect, persistence or restoration verified |

## Focused review observations

1. The current production class has **zero integration** with `NativePicturePanel`; the existing dialog consumes Left/Right but does not write. This is the primary reason PIC04 is still not functional, not missing HIDL/RPC knowledge.
2. `Port.read()` must be given a **verified input, signal format, picture profile and generation**; otherwise `UNKNOWN_CONTEXT`. Requiring those things is safe, but inventing placeholders, hand-typing 'HDMI1' or accepting Global `picture_mode` alone will defeat its purpose. Determine what existing OEM API exposes; only add missing read-only instrumentation that resolves this concrete block.
3. The three synchronous calls to `Port.read()` in `step()` do not constitute a defined stable waiting interval. `verify()` similarly performs two immediate reads; the caller must implement scheduling/timeouts and handle async TvServices stabilization. Do not claim these checks alone solve the race.
4. Source/context change after a successful write intentionally locks the transaction and preserves the baseline. This is safe, but can leave the UI blocked; adapter needs a user-visible error and recovery instructions.
5. `preserveOriginal` is invoked on each new step; real implementation must preserve the original baseline across the **whole session**, not overwrite it with the previous intermediate value. Need persistence, restoration and reconciliation semantics.
6. Check that `generation` signifies **source/profile transition epoch**, not a timestamp changing every read or every provider value update; otherwise stable contexts will be rejected or subsequent steps disabled.
7. Add tests for `preWrite` context change after `preserveOriginal`, exceptions from read/journal/submit, async timeout and user-facing UI state once an adapter exists. Existing tests are meaningful but not exhaustive.
8. No Chinese GUI dependencies introduced in `Pic04ContrastStep`. The legitimate shared TvServices/MediaTek backend is separate from Chinese menu; disabling the whole `com.my.settings` package remains a separate blocked milestone due to TimeService recovery.

## Next action: time-boxed vertical slice

**A. In research PR #3, no hardware writes:** document the actual STOCK D-pad step/confirmed one-keypress delta and available exact-8054 independent input/profile APIs; provide the narrowest read-only ADB/stock UI protocol for owner approval. State any unknown without deeper generic vtable analysis.

**B. In implementation PR #4:** implement and test a real *read-only* Android/OEM `Port.read()` adapter and live context provider first, display its diagnostics without changing values; add proper UI scheduler and error reporting. Keep write disabled until owner approves and test demonstrates a stable input/profile.

**C. With explicit owner approval:** on same fixed input/mode perform exactly one STOCK contrast D-pad step, capture before/after/restore, range and delta; stop on mismatch. Do not assume `Fake.step=2` is real.

**D. After adequate proof:** wire only PIC04 to the **MOD-owned** D-pad handler through the original Settings.Global → TvServices route. Require baseline journal, independent backend readback, and manual rollback. No `com.my.settings` Activity/Controller invocation. Confirm actual picture change and restoration separately. Keep other PIC keys blocked until individually tested.

Do not revert the policy class; do not repeat low-level HIDL disassembly unless a specific user-control failure requires it. Deliver a functioning PIC04, not another unintegrated abstraction.

## Evidence/status

- Static Java→JNI native chain: confirmed previously; scope `inputGroup=-1` unknown.
- PR #4 compiles and policy tests pass: confirmed in GitHub Actions.
- Runtime API adapter, UI wiring, authenticated source/profile: NOT IMPLEMENTED.
- STOCK one-keypress test, config/native readback on device, visual effect/rollback: NOT TESTED.
- Final PIC04 status: `POLICY_IMPLEMENTED / UI_NOT_WIRED / HARDWARE_NOT_TESTED`.

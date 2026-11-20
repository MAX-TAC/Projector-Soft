# TRACE 005 — exact-8054: MediaTek config HIDL service → RPC boundary

**Date:** 2026-11-20 · **Task:** NATIVE-005 · **Branch:** `research/8054-mediatek-native-api-20261120` · **Draft PR:** [#3](https://github.com/MAX-TAC/Projector-Soft/pull/3).  
**Result:** original config service executable found and SHA-256 verified; exact HIDL proxy/stub, service-side RPC setter import and **one service machine callsite into the setter's PLT** identified. **Still NOT PROVEN:** particular C-wrapper vtable instance → BpHw receiver → corresponding implementation body/callsite and semantics of `inputGroup=-1`, `isUpdate=3`.  
**Read-only evidence:** [NATIVE_005_HIDL_RECEIVER_EVIDENCE_8054.md](generated/NATIVE_005_HIDL_RECEIVER_EVIDENCE_8054.md); NATIVE-004 previous [TRACE](TRACE_004_CONFIG_JNI_TO_HIDL.md). Original owner image only; no device writes or APK changes.

## 1. Exact sources, scope, tools

Owner `FlixTone_8054_Backup.zip`: SHA-256 `4f53f2224c98b4c832a19a591b9dd7f925bde28528b5674a09732c0f0e41d362`, verified **before** extracting exact `vendor_a` and `system_ext_a` from `super.img`. 8052 was not used.

| Original ELF | SHA-256 (verified/derived from exact bytes) | Role |
|---|---|---|
| `vendor_a/lib/libcom_mediatek_twoworlds_tv_jni.so` | `312fef3f17f656ac957a32dfee8187798dacac8f4df83e8219b1553b7248ab24` | JNI; setter call edge PROVEN in TRACE_004 |
| `vendor_a/lib/vendor.mediatek.api.mtktvapi@1.0-cwrapper.so` | `e470029a7e5d52ea25d17be2fb255e2dfe1fe75d6fe6cc527b3146001a3c8758` | C-wrapper / HIDL client |
| `vendor_a/lib/vendor.mediatek.api.mtktvapi@1.0.so` | `a48159c7c66e5e82a4d293bc1e59e878cb0cfafbd8f645d872332f10424711f7` | HIDL proxy/stub |
| `vendor_a/bin/hw/vendor.mediatek.api.mtktvapi@1.0-service` | `15360fa27a7039f57f6173cc28942d4458e9f4d0beeaa31620e71bc785f275f6` | Exact HIDL service executable |
| `vendor_a/lib/libmediatek_tv_rpcwrapper.so` | `d8938a5733c5cacd32de03eef8dafd5ea16acb9d6386bfdc7974323782b8f62b` | API setter provider on service side (normal linker candidate) |

Tools executed by GitHub Actions: GNU `readelf` 2.46, ARM `arm-linux-gnueabihf-objdump` 2.46, `c++filt`, Python SHA-256/ELF parsing; collector [tools/trace_8054_hidl_receiver.py](../../../tools/trace_8054_hidl_receiver.py). CI extracts in `/tmp`, verifies fixed ELF SHA values and commits only sanitized text. Logs and actual workflow outcomes: [NATIVE-005 journal](logs/2026-11-20_NATIVE-005.md).

## 2. Previously proven JNI → C-wrapper path (TRACE_004)

```text
OEM picture_contrast / g_video__contrast
 → TVMenuSettingsService → TVSettingConfig → MtkTvConfigBase
 → TVNativeWrapper.setConfigValue_native(-1, configId, value, 3)
 → exact JNI: 0x56726 BLX 0x7ae90 (PLT)
 → GOT 0x7dff8 [R_ARM_JUMP_SLOT a_mtktvapi_config_set_value]
 → vendor.mediatek.api.mtktvapi@1.0-cwrapper.so (normal provider)
```

This `NATIVE_CALL_EDGE` is fully proven static. The separate `a_mtktvapi_config_set_value_for_all_elems` is **not** the direct JNI call; its JUMP_SLOT is at `0x7dffc`.

## 3. C-wrapper → HIDL client, with explicit evidence boundary

In original C-wrapper `a_mtktvapi_config_set_value` at `0x3f584`:

```asm
3f5a6: blx 0x60090       ; call into service-object acquisition path (PLT label needs resolution)
3f5ba: ldr r1, [sp,#40] ; obtained interface object
3f5bc: cbz r1, 0x3f5e0 ; absent interface → default -1
3f5be: ldr r0, [r1,#0]  ; object's vtable pointer
3f5c0: ldr r7, [r0,#60] ; vtable slot +0x3c
3f5c2: strd r6,r5,[sp] ; int value and signed update mode
3f5ca: mov r2,r4        ; signed inputGroup
3f5cc: blx r7           ; INDIRECT virtual invocation
```

`getMtkTvApiConfigService()` is an actual **defined export** of the original C-wrapper (Thumb address `0x3d741`). Its body starts `0x3d740`, checks a cached pointer and has an acquire/lookup path. The C-wrapper imports `IMtkTvApiConfig::getService(..., bool)` and the generated HIDL shared object is an explicit `DT_NEEDED`. Together these identify the interface family. **The specific jump `0x60090` was not yet resolved to its PLT/GOT import, and dynamic object type after getService is not proven.**

The HIDL library exports all three forms:

- `BpHwMtkTvApiConfig::a_hidl_a_mtktvapi_config_set_value(short, hidl_string const&, int, short)`, Thumb `0x24ca29`.
- `BpHwMtkTvApiConfig::_hidl_a_hidl_a_mtktvapi_config_set_value(...)`, Thumb `0x24a129`.
- `BnHwMtkTvApiConfig::_hidl_a_hidl_a_mtktvapi_config_set_value(..., Parcel, ...)`, Thumb `0x24dc45`.

**Important limitation:** `_ZTV` symbols are present for `BpHwMtkTvApiConfig`, `BnHwMtkTvApiConfig`, `BsMtkTvApiConfig`, and `IMtkTvApiConfig`. A first attempt to interpret `vtable_address + 8 + 60` as the C-wrapper's slot produced zero/unmatched pointers for BpHw/BnHw/IMtk, and a different unlabelled function for Bs. Therefore **do not assert `0x3f5cc` calls the BpHw setter directly** on this incomplete simplistic vtable model. Multiple inheritance/secondary address points, relocations and implementation selection require a fuller ABI-aware reconstruction. A negative raw slot read does not refute the existence of the HIDL method.

## 4. HIDL transaction: a separate code 3

In the outbound `BpHwMtkTvApiConfig::_hidl_a_hidl_a_mtktvapi_config_set_value` body:

```asm
24a224: ldr r2, [r0,#0]
24a226: ldr r6, [r2,#0]
24a230: movs r1, #3
24a232: blx r6
```

This is the binder object's virtual dispatch with **transaction index/code `3`** passed in `r1`, observed inside the generated `config_set_value` method. Inbound `BnHwMtkTvApiConfig::onTransact(...)` exists at Thumb `0x24fa29` and begins with `subs r0,r1,#1; cmp r0,#14; tbb [pc,r0]`, a jump table over the first 15 transaction values. Exact mapping of jump-table entry 3 to `_hidl...config_set_value` needs disassembly/data verification.

**This HIDL transaction number `3` is distinct from the last `short isUpdate=3` passed as application payload. Identical numerical values do not imply identical semantics.**

## 5. Concrete HIDL service and RPC dependency

Original files:

- `vendor_a/etc/init/vendor.mediatek.tv.mtktvapi@1.0-service.rc`: registers `service tv-mtktvapi-1-0 /vendor/bin/hw/vendor.mediatek.api.mtktvapi@1.0-service`.
- `vendor_a/etc/vintf/manifest.xml`: includes `vendor.mediatek.api.mtktvapi` HAL entries with `default` instances. Multiple entries pertain to interfaces; do not interpret the number of `default` strings as a number of running service processes.
- The exact service ELF imports `IMtkTvApiConfig::registerAsService` (`R_ARM_JUMP_SLOT 0x47b10`), plus `a_mtktvapi_config_set_value` (`UND`, `R_ARM_JUMP_SLOT 0x47fa0`) and the distinct `...set_value_for_all_elems`.
- Its real `DT_NEEDED` includes both `vendor.mediatek.api.mtktvapi@1.0.so` and `libmediatek_tv_rpcwrapper.so`.

**New machine callsite in service (independent of HIDL handler identity):**

```asm
29e48: blx 0x43490   ; original service ELF .text into .plt
43490: add ip, pc, #0, 12
43494: add ip, ip, #4, 20    ; effective rotated immediate 0x4000
43498: ldr pc, [ip, #2824]! ; 0xB08
```

ARM PLT arithmetic: `(0x43490 + 8) + 0x0 + 0x4000 + 0xB08 = 0x47fa0`, which exactly matches `R_ARM_JUMP_SLOT a_mtktvapi_config_set_value` in this **service ELF**. Thus the service has **one statically located direct branch** into the imported RPC-named setter through PLT, and `libmediatek_tv_rpcwrapper.so` is present in its dependencies. This proves **the service→imported setter machine edge**, but **does not yet prove that the HIDL transaction for config_set_value reaches service instruction `0x29e48`**; its concrete method body is not exported with a recognizable config name and needs call-graph reconstruction.

The original `libmediatek_tv_rpcwrapper.so` has its own exported `a_mtktvapi_config_set_value` (disassembled in TRACE_004 at `0x76a14`); it links against `libmediatek_tv_rpccore.so`. A complete RPC message/receiver or hardware implementation has not been established from the direct PLT path alone. Runtime ELF interposition was not tested.

## 6. `inputGroup=-1` and `isUpdate=3`: what actually changed

| Question | Exact observed evidence | Status |
|---|---|---|
| First argument, `-1` | Java passes literal -1; JNI `ldrsh` sign-extends to C ABI; HIDL signature uses `short`; C-wrapper does not rewrite it in the observed dispatch sequence | **ABI confirmed; semantic meaning UNKNOWN** |
| Last argument, `3` | Java passes 3; JNI `ldrsh` → C/HIDL `short`; no meaning inferred | **ABI confirmed; update/persistence meaning UNKNOWN** |
| HIDL transaction code `3` | BpHw generated setter explicitly loads `r1=3` immediately before binder virtual call | **Observed static HIDL dispatch constant**, unrelated to payload `isUpdate` |
| `set_value` vs `set_value_for_all_elems` | JNI PLT/GOT chooses `set_value`; service has imports for both | **Entry resolved**, not complete scope/per-input semantics |
| HDMI input + SDR/HDR + PQ profile | No signal/source/profile context in the JNI wrapper; final receiver not decompiled | **UNKNOWN** |
| Return statuses / errors | JNI can return default zero without a C call for null string; C-wrapper absent-interface branch defaults -1 | **Partial**; native status-to-UI semantics not traced |
| Physical effect/readback | No running projector contacted | **NOT_TESTED** |

The names `set_value_for_all_elems` and `inputGroup=-1` are not interchangeable. A call to the ordinary setter with `-1` might still have special behavior in the RPC backend. **No mode, source, update or persistence semantics were established by static types or symbol names.**

## 7. Exact evidence levels, implementation decision

| Milestone | State |
|---|---|
| `STATIC_KEY_MAP` | CONFIRMED in prior smali, `picture_contrast → g_video__contrast` |
| `JAVA_SMALI_CALLSITE` | CONFIRMED in prior TRACE_001/004 |
| `EXACT_NATIVE_ELF_EXPORT` | CONFIRMED from owner firmware |
| `NATIVE_CALL_EDGE` | **PROVEN_STATIC**: JNI → C-wrapper setter; service also has a setter PLT caller |
| `HIDL_PROXY_STUB` | **EXACT_ELF_CONFIRMED**: BpHw/BnHw generated methods; txn code 3 in BpHw |
| `SERVICE_BINARY_REGISTRATION` | **STATIC_CONFIRMED**: init/VINTF + registration API import |
| `CWRAPPER_VTABLE_TO_BPHW` | **PARTIAL/UNRESOLVED**: selected runtime object's vptr not mapped to static concrete class |
| `HIDL_HANDLER_TO_SERVICE_CALLSITE` | **UNRESOLVED**: no proven handler → `0x29e48` edge |
| `RPC_RECEIVER/PERSISTENCE` | UNKNOWN |
| `READBACK_CONFIRMED` | NOT_TESTED |
| `HARDWARE_EFFECT_VERIFIED` | NOT_TESTED |

**No changes to MOD v41.** PQ editing remains restricted because the selected input/profile and true persistence semantics are not known. No Factory calibration, I²C, EDID/HPD, thermal/control, APK installation or hardware state changes performed.

## 8. Next concrete follow-up (NATIVE-005b / before HDR & MEMC)

1. In exact HIDL C-wrapper resolve `0x3f5a6 BLX 0x60090` via PLT/GOT, trace `getMtkTvApiConfigService` object acquisition to concrete `BpHw`/ `Bs` interface and vtable offset under Android ARM32 Itanium multi-inheritance. Do not assume `+8` is always the relevant vtable address point.
2. Use original service ELF `vendor.mediatek.api.mtktvapi@1.0-service` at `0x29e48`: recover containing function start, direct xrefs, incoming HIDL generated `BnHw` transaction 3 handler and registration path. The imported setter is from RPC-wrapper by normal dependency graph; verify exact callsite to the implementation.
3. Follow `libmediatek_tv_rpcwrapper.so` and `libmediatek_tv_rpccore.so` with a bounded, read-only RPC descriptor/call graph audit to discover how signed `-1` and `3` are consumed. If the actual receiver is non-Android TV firmware, record the boundary; do not invent its implementation.
4. Separately verify exact DEX declaration of `TVNative.setConfigValue_native` and Java error propagation; this report concentrates on native.
5. Only when source/profile and rollback semantics are proven, prepare a **separately authorized** OEM readback/one-step hardware test. Do not run it automatically. Then individual HDR and MEMC traces.

Reproduction and failures: [2026-11-20_NATIVE-005 journal](logs/2026-11-20_NATIVE-005.md).

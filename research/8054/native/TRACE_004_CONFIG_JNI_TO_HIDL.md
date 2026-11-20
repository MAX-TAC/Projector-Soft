# TRACE 004 — exact-8054: JNI `setConfigValue_native` → MediaTek config C-wrapper / HIDL boundary

**Date:** 2026-11-20. **Task:** NATIVE-004. **Method:** static, read-only, SHA-256-gated disassembly of owner's exact 8054.  
**Result:** `NATIVE_CALL_EDGE = PROVEN_STATIC` for `Java_com_mediatek_twoworlds_tv_TVNative_setConfigValue_1native` → imported `a_mtktvapi_config_set_value`; HIDL generated proxy/stub exists, but the concrete service implementation, active source/profile resolution, state persistence and visual effect are **NOT VERIFIED**.  
**PR:** [#3](https://github.com/MAX-TAC/Projector-Soft/pull/3). **Successful final run:** [44799645224](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44799645224).  
**Full bounded machine evidence:** [NATIVE_004_ELF_MACHINE_EVIDENCE_8054.md](generated/NATIVE_004_ELF_MACHINE_EVIDENCE_8054.md). No raw ELF attached.

## 1. Immutable provenance and tools

Archive `FlixTone_8054_Backup.zip`, original release [FlixTone_8054_Backup](https://github.com/MAX-TAC/FlixTone_8054_Backup/releases/tag/FlixTone_8054_Backup), 2,011,878,129 bytes; expected and CI-verified SHA-256 `4f53f2224c98b4c832a19a591b9dd7f925bde28528b5674a09732c0f0e41d362`. Its `super.img` yielded original `vendor_a`, `system_a`, `product_a`, `system_ext_a`. Not 8052.

| Exact source ELF path | SHA-256 verified by collector |
|---|---|
| `vendor_a/lib/libcom_mediatek_twoworlds_tv_jni.so` | `312fef3f17f656ac957a32dfee8187798dacac8f4df83e8219b1553b7248ab24` |
| `vendor_a/lib/vendor.mediatek.api.mtktvapi@1.0-cwrapper.so` | `e470029a7e5d52ea25d17be2fb255e2dfe1fe75d6fe6cc527b3146001a3c8758` |
| `vendor_a/lib/libmediatek_tv_rpcwrapper.so` | `d8938a5733c5cacd32de03eef8dafd5ea16acb9d6386bfdc7974323782b8f62b` |
| `vendor_a/lib/vendor.mediatek.api.mtktvapi@1.0.so` | `a48159c7c66e5e82a4d293bc1e59e878cb0cfafbd8f645d872332f10424711f7` |

All inspected objects report `Machine: ARM` (32-bit), **not AArch64**. Runner tools: GNU `readelf`, `arm-linux-gnueabihf-objdump`, `c++filt` version **2.46**. AAPCS32: [Arm ABI specification](https://github.com/ARM-software/abi-aa/blob/main/aapcs32/aapcs32.rst); JNI vtable indices: [Oracle JNI functions](https://docs.oracle.com/en/java/javase/25/docs/specs/jni/functions.html).

Actual executed collector: `python3 tools/trace_8054_config_jni.py --vendor-lib /tmp/flix8054_erofs/vendor_a_tree/lib --out /tmp/flix8054_native_report`. It invokes `readelf -h`, `readelf -d`, `readelf -WsW`, `readelf -rW`, `readelf -SW`, `arm-linux-gnueabihf-objdump -drw --disassemble=<symbol>`, `-j .plt --start-address ... --stop-address ...`, and `c++filt`. The firmware's archive hash is checked before extraction; all four ELF hashes are checked before disassembly.

## 2. OEM UI and Java precondition (established in TRACE_001, not re-run on hardware)

```text
STOCK picture_contrast (Settings.Global)
 → TVMenuSettingsService.SettingObserver / updateConfigData
 → TVSettingConfig.setConifg(configId, value)
 → MtkTvConfigBase.setConfigValue(configId, value)
 → TVNativeWrapper.setConfigValue_native(-1, configId, value, 3)
 → Java_com_mediatek_twoworlds_tv_TVNative_setConfigValue_1native
 → [machine call to a_mtktvapi_config_set_value proven BELOW]
```

For contrast `configId = g_video__contrast` from exact `TVMenuSettingsService.initMapdata`. This is a Java/smali static path plus actual native ELF export. The Java declaration's exact `static` vs instance form and complete DEX prototype were **not independently extracted in this run**; the JNI register usage below directly establishes the effective native argument layout and handling. Do not infer any input selection from the two Java literals alone.

## 3. JNI import, actual branch and PLT/GOT proof (new finding)

`readelf -WsW` in **the JNI ELF**:

```text
284: 00000000 0 FUNC GLOBAL DEFAULT UND a_mtktvapi_config_set_value
285: 00000000 0 FUNC GLOBAL DEFAULT UND a_mtktvapi_config_set_value_for_all_elems
834: 000566dd 104 FUNC GLOBAL DEFAULT 14 Java_com_mediatek_twoworlds_tv_TVNative_setConfigValue_1native
```

`readelf -rW`:

```text
0007dff8 00011c16 R_ARM_JUMP_SLOT 00000000 a_mtktvapi_config_set_value
0007dffc 00011d16 R_ARM_JUMP_SLOT 00000000 a_mtktvapi_config_set_value_for_all_elems
```

The specific exported JNI function contains `0x56726: blx 0x7ae90`. GNU objdump displays an unrelated nearest-symbol annotation for that branch; **this annotation must not be trusted as its actual callee**. The destination falls inside the verified `.plt` section `0x796b0..0x7c7b0`. Actual three-instruction ARM PLT stub:

```asm
7ae90: add ip, pc, #0
7ae94: add ip, ip, #12288       @ 0x3000
7ae98: ldr pc, [ip, #352]!     @ 0x160
```

With ARM-state PC `0x7ae90 + 8 = 0x7ae98`, the effective GOT slot is:

```text
0x7ae98 + 0 + 0x3000 + 0x160 = 0x7dff8
```

This is **exactly** `R_ARM_JUMP_SLOT` for `a_mtktvapi_config_set_value`, not the separate `...set_value_for_all_elems` GOT slot `0x7dffc`. Hence the **machine call edge is proven** even without a pretty `@plt` disassembler label. [CI computation and native excerpt](generated/NATIVE_004_ELF_MACHINE_EVIDENCE_8054.md#c3-independently-verified-arm-plt---got-resolution).

The native `DT_NEEDED` identifies `vendor.mediatek.api.mtktvapi@1.0-cwrapper.so`, which defines this imported C function at Thumb entry `0x3f585` (disassembly at `0x3f584`). This is the normal candidate provider for the resolved import. **The normal static linker route is established; potential runtime ELF symbol interposition or namespace overrides were not tested.** The similarly named RPC wrapper is a separate ELF and not the JNI library's direct `DT_NEEDED` dependency.

## 4. JNI ABI, parameter preservation and observable error paths

The function body (`0x566dc..0x56742`) reveals the following machine layout, independent of Java-source variable names:

| JNI entry and native machine evidence | Interpretation / limitation |
|---|---|
| `r0` saved at `[sp+20]`, `r1` at `[sp+16]` | `JNIEnv*` and Java object/class handle; static-vs-instance unresolved |
| `r2` saved at `[sp+12]` | first application argument; later `ldrsh` into `r0` → signed 16-bit `inputGroup` |
| `r3` saved at `[sp+8]` | Java `jstring` for config ID |
| `[sp+32]` | passed to native C wrapper as `r2`: 32-bit value |
| `[sp+36]` | `ldrsh` into `r3`: signed 16-bit `isUpdate` |
| `[JNIEnv vtable+676]` | `GetStringUTFChars`, JNI index 169 × 4 (0x2a4) |
| `[JNIEnv vtable+680]` | `ReleaseStringUTFChars`, JNI index 170 × 4 (0x2a8) |
| return register `r0` | on valid string path, the C setter return propagates through the JNI function |

At `0x5671a..0x56726` exact setter arguments are prepared:

```asm
5671a: ldrsh.w r0, [sp, #12]  ; signed inputGroup
5671e: ldr     r1, [sp, #0]   ; GetStringUTFChars result
56720: ldr     r2, [sp, #32]  ; config value
56722: ldrsh.w r3, [sp, #36]  ; signed isUpdate
56726: blx     0x7ae90        ; resolved above to config_set_value
```

The JNI body initializes a local status `0`. A null `jstring` branches directly to the return with that default (no C call); a null pointer from `GetStringUTFChars` skips the C setter. Therefore, **JNI return value 0 cannot automatically be equated with a completed hardware update**. On a successful native string-conversion path, `r0` from the imported setter is stored and returned. The complete OEM status enum and caller-side error handling are not established.

## 5. C-wrapper and HIDL dispatch boundary

Exact C-wrapper export `a_mtktvapi_config_set_value`: `0x3f585`, size 168 bytes. Disassembly `0x3f584..0x3f622` saves `r0..r3` into registers `r4,r7,r6,r5`, prepares an HIDL string and obtains a client/interface object. At `0x3f5be..0x3f5cc`:

```asm
3f5be: ldr  r0, [r1, #0]
3f5c0: ldr  r7, [r0, #60]    ; virtual dispatch slot
3f5c2: strd r6, r5, [sp]     ; value, mode on stack
3f5c6: add  r0, sp, #12
3f5c8: add  r3, sp, #48     ; prepared string
3f5ca: mov  r2, r4         ; inputGroup
3f5cc: blx  r7             ; indirect virtual dispatch
```

The function has a clear failure/default `-1` path when the required client object is absent (`0x3f5e0`), and returns a result from the dispatch wrapper when available (`0x3f5dc..0x3f602`). Do **not** assume all negative/positive return codes or the specific meaning of `-1` without the OEM enum and the actual service.

The exact generated HIDL library `vendor.mediatek.api.mtktvapi@1.0.so` exposes:

- `BpHwMtkTvApiConfig::a_hidl_a_mtktvapi_config_set_value(short, hidl_string const&, int, short)` at `0x24ca29`.
- `BpHwMtkTvApiConfig::_hidl_a_hidl_a_mtktvapi_config_set_value(..., short, hidl_string const&, int, short)` at `0x24a129`.
- `BnHwMtkTvApiConfig::_hidl_a_hidl_a_mtktvapi_config_set_value(..., Parcel const&, Parcel*, callback)` at `0x24dc45`.
- Separate `...set_value_for_all_elems` HIDL methods also exist; their existence does **not** mean JNI chooses them.

**Status of HIDL edge:** C-wrapper builds a compatible string/short/int/short call through a virtual method, and matching BpHw/BnHw generated methods are proven in the same original firmware. The exact **vtable target, Binder transaction code, service instance and concrete server implementation** have **not** been machine-followed and are therefore `PARTIAL / INFERRED`, not `HIDL_RPC_RECEIVER_CONFIRMED`. In particular, `libmediatek_tv_rpcwrapper.so` exports a *different implementation* of the same C symbol, but no call from this JNI entry to that RPC ELF was established.

## 6. Semantics, applicability and safety limits

- **`inputGroup=-1`:** the JNI machine code passes it signed via `ldrsh`; the meaning (active source vs global/all input/profile context vs sentinel) remains **UNKNOWN**. It is *not* proven to select HW6, HW7, HDMI1, HDMI2 or a particular SDR/HDR picture mode.
- **`isUpdate=3`:** passed unchanged as signed 16-bit at the JNI → C ABI boundary. Whether it requests persistence, apply-now, update scope or notification is **UNKNOWN**.
- **Per-element vs all-elements:** JNI resolved to `a_mtktvapi_config_set_value`, **not** `a_mtktvapi_config_set_value_for_all_elems`. This resolves *which function is called*, but the true scope of the per-element operation with `-1` is still unknown.
- `Settings.Global` write/read, MediaTek config readback, RPC service acknowledgement and physical PQ change are **distinct checkpoints**. No device/runtime readbacks were performed.
- Factory `a_mtktvfapi_disp_set_picture_param` and calibration methods are not the user config route. No Factory/EDID/HPD/I²C/PEQ/thermal writes have been attempted.
- The MOD v41 `NativePicturePanel` remains read-only/gated for ambiguous PQ context; **no MOD source edits were made** by this research.

## 7. Evidence levels

| Level | Status | What is actually proven |
|---|---|---|
| `STATIC_KEY_MAP` | CONFIRMED | `picture_contrast → g_video__contrast` |
| `JAVA_SMALI_CALLSITE` | CONFIRMED (prior) | TvServices → `TVNativeWrapper.setConfigValue_native(-1, id, value, 3)` |
| `EXACT_NATIVE_ELF_EXPORT` | CONFIRMED | Original 8054 JNI export, C setter import and HIDL ABI exports |
| `NATIVE_CALL_EDGE` | **PROVEN_STATIC (new)** | JNI `BLX 0x7ae90` → PLT/GOT `0x7dff8` → `a_mtktvapi_config_set_value` |
| `HIDL_RPC_RECEIVER` | PARTIAL | HIDL proxy/stub present; concrete receiver and exact virtual target not traced |
| `READBACK_CONFIRMED` | NOT_TESTED | No live config readback |
| `HARDWARE_EFFECT_VERIFIED` | NOT_TESTED | No physical display effect measured |

## 8. Next concrete stage — NATIVE-005, then HDR/MEMC

1. From this exact `0x3f5cc` indirect call, reconstruct interface object creation, vtable dispatch and `BpHwMtkTvApiConfig` target; identify HIDL transaction code, `BnHw` receiver and actual service implementation by scanning original `vendor_a` and system services; avoid guesses from demangled names.
2. Trace which implementation consumes the signed `-1` input-group sentinel and `3` update-mode flag, including input/profile mode and persistent store. Validate negative/error paths as part of the same contract.
3. Only after the resolver is established, create separate HDR `picture_list_hdr / g_video__vid_hdr` and MEMC `tv_picture_advance_video_mjc_effect / g_video__vid_mjc_effect` traces; do not silently reuse a generic setter assumption for modes with special handling.
4. Owner-authorized future runtime validation must record input, mode, original values, Global readback, MediaTek config readback, screen effect and restoration, with a stop condition on any unknown destination. **No writes until separate owner consent.**

For session actions, CI failures, commands and provenance, see [2026-11-20_NATIVE-004.md](logs/2026-11-20_NATIVE-004.md).

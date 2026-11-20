# TRACE 002 — MediaTek native ELF / JNI / HIDL boundaries (exact-8054)

**Source:** the owner's SHA-256-verified `FlixTone_8054_Backup.zip` from `MAX-TAC/FlixTone_8054_Backup`, `super.img` → EROFS `system_a/vendor_a/product_a/system_ext_a`.
**Initial completed GitHub Action:** [run 44792807063](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44792807063).
**Status:** `EXACT_NATIVE_ELF_INVENTORIED / STATIC_DEPENDENCIES / RUNTIME_UNVERIFIED`.

## New information from the actual native binaries

The full system dump was downloaded, hash-verified and unpacked on GitHub Actions; `readelf` and allowlisted `strings` scans were run on the extracted ARM ELF files. These are **real exact-8054 binary observations**, not borrowed SDK names.

| Measurement | Count |
|---|---:|
| Shared ELF libraries | **1,283** |
| system_a | 919 |
| vendor_a | 323 |
| product_a | 6 |
| system_ext_a | 35 |
| Filename-selected MediaTek/TV/audio/etc. candidates | 300 |
| Candidates with relevant dynamic symbols | 62 |
| Candidates with allowlisted JNI/config literals | 32 |

Full text-only evidence:
[ELF inventory](generated/ELF_LIBRARY_INVENTORY_8054.csv) ·
[Candidate symbols, SHA-256 and DT_NEEDED](generated/NATIVE_API_CANDIDATES_8054.json) ·
[Summary](generated/NATIVE_API_INVENTORY_8054.md).

## Native files and exact dependencies

### The TVNative JNI library

```text
/vendor/lib/libcom_mediatek_twoworlds_tv_jni.so
SHA256 312fef3f17f656ac957a32dfee8187798dacac8f4df83e8219b1553b7248ab24
size 646808 bytes
ELF: ARM
```

This library **exports JNI-style `Java_com_mediatek_twoworlds_tv_TVNative_...` entry points**. Its actual `DT_NEEDED` includes:

- `vendor.mediatek.api.mtktvapi@1.0-cwrapper.so` (HIDL client-side bridge candidate);
- `vendor.mediatek.custom.mtktvapicustom@1.0-cwrapper.so`;
- `libmediatek_tv_basic.so`;
- `libvendor_agent.so`;
- `libwebplatform_ca.so`.

This grounds a JNI/HIDL dependency chain for the **actual installed TVNative implementation**. A dependency is not proof that a specific `setConfigValue_native` call reaches that bridge: this exact call edge requires symbol/disassembly tracing.

### MediaTek TV API C wrapper and RPC implementation

```text
/vendor/lib/vendor.mediatek.api.mtktvapi@1.0-cwrapper.so
SHA256 e470029a7e5d52ea25d17be2fb255e2dfe1fe75d6fe6cc527b3146001a3c8758

/vendor/lib/libmediatek_tv_rpcwrapper.so
SHA256 d8938a5733c5cacd32de03eef8dafd5ea16acb9d6386bfdc7974323782b8f62b
```

The C wrapper exports `a_mtktvapi_config_set_value_for_all_elems` among numerous TV functions and depends on `vendor.mediatek.api.mtktvapi@1.0.so`. RPC wrapper exports `a_mtktvapi_config_set_str_value_ex`, as well as HDMI CEC and input-switching functions. **API symbols exist**, but their exact arguments, branch conditions and runtime service receiver need dedicated tracing.

### Factory HIDL implementation

```text
/vendor/lib/vendor.mediatek.tv.mtktvfactory@1.0-imp.so
SHA256 36ebefce2c24bf960d570d006977c2187130e0c6db73ca647e683fb104df3a86
```

Among observed *exported* symbols in the first inventory:

- `a_mtktvfapi_disp_set_picture_param` and `a_mtktvfapi_disp_set_picture_param_struct`;
- `a_mtktvfapi_aud_adv_set_peq_value`;
- `a_mtktvfapi_aud_adv_snd_set_dolby_dap_param`;
- `a_mtktvfapi_prphrl_get_hdmi_edid_version`;
- `a_mtktvfapi_prphrl_get_hdmi_edid_data`;
- `a_mtktvfapi_prphrl_enable_cec`;
- `a_mtktvfapi_disp_set_color_temperature`.

**Important:** these Factory/HIDL functions must not be substituted for ordinary per-profile TV settings without proof of equivalent semantics. The factory picture setter and general `g_video__contrast` config path may be distinct mechanisms.

### Projector-only and CEC paths

- `/system/lib/libfactoryi2c_jni.so`, SHA256 `effdc951747003547b8d2648f9bf5290a275c9e098210609c856c72b4699b642`: genuine exported `Java_mediatek_util_I2CDevice_{open,close,readRegister,writeRegister}`. This confirms the Factory JNI register access used by projector orientation, **not** that arbitrary I²C writes are safe.
- `/vendor/lib/hw/hdmi_cec.mt5873.so`, SHA256 `bbbcffba279b21257b3ab903df5157662a54a3711625ab543536a01fcafa31c1`: board-specific HDMI-CEC HAL, linked to `libmediatek_tv_rpcwrapper.so`, `libvendor_agent.so` and TV core libraries.
- `/system/lib/libtof_jni.so` depends on `vendor.trd.hardware.tof@1.0.so`, `vendor.trd.hardware.hwtrd@1.0.so` and TRD clients; projector sensors/focus may use **a different owner than TV/PQ**.

## Consolidated architectural hypothesis (not all edges confirmed)

```text
TvSettingsPlus OEM Chinese / Android TV UI
   -> Settings.Global (e.g. picture_contrast)
   -> TVMenuSettingsService (ContentObserver)
   -> TVSettingConfig / MtkTvConfigBase (Java)
   -> TVNative (native JNI method declaration)
   -> libcom_mediatek_twoworlds_tv_jni.so  [binary physically located]
      -> mtktvapi HIDL C wrapper [DT_NEEDED, call edge not yet proven]
      -> vendor mtktv API service / RPC / display pipeline [requires disassembly/runtime]
```

Separate branches: `Factory.apk → MtkTvFApi → mtktvfactory@1.0`; `HDMI-CEC HAL`; `TRD/ToF`. No universal API controlling all cooling/focus/video/sound has been established.

## Next exact, source-driven steps

1. Search **all** exported JNI names in `libcom_mediatek_twoworlds_tv_jni.so` for `getConfigValue_native` and `setConfigValue_native`. The first inventory capped reported symbols per library, so an absent match in that table **must not** be interpreted as missing from ELF.
2. Verify exact JNI signature, link references and call graph to `a_mtktvapi_config_*`; locate static HIDL service receiver if possible.
3. Analyze native target `inputGroup=-1` / update mode `3` and side effects from Java→native, not from assumptions about TV inputs.
4. Continue user-facing HDR → MEMC → RGB → audio EQ; never emit direct factory calls without OEM equivalent and rollback.
5. Keep implementation and device tests separate from this **read-only** research branch.

**Nothing was installed, flashed or changed on the projector.** 

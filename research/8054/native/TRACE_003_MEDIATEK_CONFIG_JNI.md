# TRACE 003 — exact TVNative JNI config entry points and MediaTek API symbols

**Firmware:** owner `FlixTone_8054_Backup.zip`, SHA-256 `4f53f2224c98b4c832a19a591b9dd7f925bde28528b5674a09732c0f0e41d362`.
**ELF verification:** [completed GitHub Actions run 44793791027](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44793791027).
**Artifact in repository:** [NATIVE_API_CANDIDATES_8054.json](generated/NATIVE_API_CANDIDATES_8054.json).

**Confidence:** exact ELF files and named exported symbols from genuine firmware — **CONFIRMED**; direct call edge between exported symbols — **NOT YET PROVEN**; runtime hardware effect — **UNTESTED**.

## 1. Java→JNI entry points definitely present in the original binary

**ELF** `vendor_a/lib/libcom_mediatek_twoworlds_tv_jni.so`,
SHA-256 `312fef3f17f656ac957a32dfee8187798dacac8f4df83e8219b1553b7248ab24`.

The complete priority-ordered readelf pass confirmed these **defined exports** (actual mangled JNI symbol names):

```text
Java_com_mediatek_twoworlds_tv_TVNative_getConfigValue_1native
Java_com_mediatek_twoworlds_tv_TVNative_setConfigValue_1native
Java_com_mediatek_twoworlds_tv_TVNative_getMinMaxConfigValue_1native
Java_com_mediatek_twoworlds_tv_TVNative_getCfgParserConfigValue_1native
Java_com_mediatek_twoworlds_tv_TVNative_setConfigValueForAllElements_1native
Java_com_mediatek_twoworlds_tv_TVNative_resetConfigValues_1native
Java_com_mediatek_twoworlds_tv_TVNative_getVideoInfoData_1native
```

**Meaning:** the exact Java-boundary methods traced from TvServices and `MtkTvConfigBase` have corresponding exported native entry points physically present in the owner's real image. The native ABI names alone do **not** reveal their C/C++ parameter sizes or target behavior; those remain subject to native disassembly and original Java method signatures.

**Actual `DT_NEEDED`** includes `vendor.mediatek.api.mtktvapi@1.0-cwrapper.so`, `vendor.mediatek.custom.mtktvapicustom@1.0-cwrapper.so`, `libvendor_agent.so`, `libmediatek_tv_basic.so`.

## 2. Matching MediaTek native API exported symbols

### RPC wrapper

`vendor_a/lib/libmediatek_tv_rpcwrapper.so`,
SHA-256 `d8938a5733c5cacd32de03eef8dafd5ea16acb9d6386bfdc7974323782b8f62b`.

Confirmed:

```text
a_mtktvapi_config_get_value
a_mtktvapi_config_set_value
a_mtktvapi_config_get_minmaxvalue
a_mtktvapi_config_get_str_value
a_mtktvapi_config_set_str_value
a_mtktvapi_config_set_str_value_ex
a_mtktvapi_config_set_value_for_all_elems
```

### HIDL client C wrapper

`vendor_a/lib/vendor.mediatek.api.mtktvapi@1.0-cwrapper.so`,
SHA-256 `e470029a7e5d52ea25d17be2fb255e2dfe1fe75d6fe6cc527b3146001a3c8758`.

Confirmed:

```text
a_mtktvapi_config_get_value
a_mtktvapi_config_set_value
a_mtktvapi_config_get_minmaxvalue
a_mtktvapi_config_set_value_for_all_elems
```

The JNI binary has `DT_NEEDED` for this C wrapper, whose `DT_NEEDED` in turn includes `vendor.mediatek.api.mtktvapi@1.0.so`.

**Narrowed architecture (known nodes and dependencies, not all edges):**

```text
OEM UI → Settings.Global.picture_contrast
   → TVMenuSettingsService
   → TVSettingConfig / MtkTvConfigBase
   → TVNative.setConfigValue_native( ... )  [Java signature traced]
   → Java_com_mediatek_twoworlds_tv_TVNative_setConfigValue_1native
      [ELF entry point CONFIRMED]
   ├─ DT_NEEDED: vendor.mediatek.api.mtktvapi@1.0-cwrapper.so
   │    └─ exported: a_mtktvapi_config_set_value [CONFIRMED]
   │         └─ DT_NEEDED: vendor.mediatek.api.mtktvapi@1.0.so
   └─ DT_NEEDED: libvendor_agent.so
```

The box above is a **dependency/candidate-call map**. It must not yet be described as a formally proven full C++ call graph. Direct xrefs from JNI into the exported config setter still require ELF disassembly/relocations, and the HIDL service-side dispatch is not yet followed.

## 3. Factory is a distinct API family, not always a replacement

The genuine `vendor_a/lib/vendor.mediatek.tv.mtktvfactory@1.0-imp.so` exports:

```text
a_mtktvfapi_disp_set_picture_param
a_mtktvfapi_disp_set_picture_param_struct
a_mtktvfapi_aud_adv_set_peq_value
a_mtktvfapi_aud_adv_set_peq_freq_value
a_mtktvfapi_aud_adv_set_peq_gain_value
a_mtktvfapi_aud_adv_set_peq_q_value
a_mtktvfapi_prphrl_get_hdmi_edid_version
a_mtktvfapi_prphrl_set_hdmi_edid_version
a_mtktvfapi_prphrl_enable_cec
```

The presence of Factory functions confirms native ABI availability. It **does not establish that a normal Android TV setting (contrast/HDR/MEMC) must call the Factory setter**. For normal user PQ the TvServices config observer chain is separately traced; substituting factory calibration paths may introduce different scope or persistence.

## 4. Implementer guidance

- Reuse the **original OEM Java/service-level path** from TvSettingsPlus as the initial integration target. Do not call raw exported JNI functions from custom preferences.
- For `picture_contrast`, the missing question is how native `inputGroup=-1` resolves active HDMI + SDR/HDR + picture profile and when readback becomes stable.
- Inspect JNI symbol disassembly and native cross-references next; bind inferred callsites to an exact binary SHA.
- A Java `Settings.Global.putInt` success **is not** proof of native config application; native config value readback **is not** proof of physical image change.
- Keep factory calibration, PEQ13, EDID HPD and I²C writes behind separate safety gates and no user-facing automatic writes.

**No projector connection or hardware modification was performed for this trace.**

# TRACE 001 — Picture contrast, exact-8054

**Provenance:** original user firmware (SHA-256 verified in [firmware archive](README.md)); recorded smali findings of original 8054 APK; MOD v41 at `fbb41e92a47a94a2a4b0f4122100e5a1cb43cfe5`. **Status:** `JAVA_STATIC_TRACE_CONFIRMED / NATIVE_ELF_PENDING / HARDWARE_UNVERIFIED`.

## User-facing function and identifiers

| Role | Exact identifier |
|---|---|
| User preference | Контрастность |
| Provider key | `Settings.Global.picture_contrast` |
| MediaTek config ID | `g_video__contrast` |
| TvServices mapping | `TVMenuSettingsService.initMapdata()V` near smali 2361–2365 |
| Android TV OEM UI | `partnercustomizer.picture.PictureFragment` / `ProgressBarVerticalFragment` |
| Chinese OEM UI | `PictureModeValueSecondaryController` |
| Java config access | `TVSettingConfig.getConfigValueInt(String)`, `MtkTvConfigBase.setConfigValue(String,int)` |
| Native call boundary | `TVNativeWrapper.getConfigValue_native(-1, configId)`, `TVNativeWrapper.setConfigValue_native(-1, configId, value, 3)` |

## Established paths

```text
Chinese OEM:
PictureModeValueSecondaryController$...onValueChanged
 -> SettingsUtil.putGlobalValueInt("picture_contrast", value)
 -> Settings.Global.putInt

Android-TV stock:
PictureActivity -> PictureFragment (or ProgressBarVerticalFragment)
 -> PictureFragment.onPreferenceChange/onPfChange
 -> PreferenceConfigUtils.updatePreferenceChanged
 -> PreferenceConfigUtils.onPreferenceValueChange
 -> PreferenceConfigUtils.putSettingValueInt
 -> Settings.Global.putInt

Observer in TvServices:
Settings.Global content URI
 -> TVMenuSettingsService$1 / SettingObserver.onChange
 -> TVMenuSettingsService.formatSettingToConfigId
 -> TVMenuSettingsService.updateConfigData
 -> TVSettingConfig.setConifg(configId, value)
 -> MtkTvConfigBase.setConfigValue(configId, value)
 -> TVNativeWrapper.setConfigValue_native(-1, configId, value, 3)
 -> native/HAL (exact implementation not yet inspected)
```

**Asynchrony:** `TVMenuSettingsService.updateSettingDB` performs reverse config→Global synchronization via `TVAsyncExecutor`. Provider readback immediately after write is not proof of final application.

**Stock preconditions/side effects:** OEM `PictureFragment.onCreatePreferences` can invoke `disableAutoBackLight`; picture mode affects editability; `isVGASource` gates some effects; power-related confirmation can occur. Do not launch a whole OEM fragment solely to reuse a child editor without inspecting lifecycle side effects.

## Current MOD v41 gap

`NativePicturePanel.showSlider()` reads both Global and `TVSettingConfig.getConfigValueInt("g_video__contrast")`, derives OEM `getMin/getMax`, draws OEM `partner_progressbar_dialog` but intercepts D-pad edit keys without a write. As of v41 it is **read-only**, not a functional stock contrast setter.

## Specific missing evidence

- Locate exact ELF library and native implementation behind `TVNativeWrapper.setConfigValue_native` and its `RegisterNatives` / JNI bridge / Binder/HIDL route.
- Determine what `inputGroup=-1` means during HDMI1/HW6↔HDMI2/HW7 and SDR/HDR/profile changes; Java-only traces do not prove its hardware target.
- Verify that edit, backend readback, visible change, and restoration act on the same input and picture profile.
- Do **not** replace OEM route with guessed direct JNI calls or bypass input-context safeguards.

## Reproduction and verification

1. Verify target board ID `8054` and source image, record current active input/signal/profile.
2. In stock UI, **manually** change contrast by one OEM step (owner-approved hardware test); sample Global and MediaTek config after settling.
3. Restore original value through stock UI in the same context and re-read.
4. Test two HDMI inputs and SDR/HDR separately; reject any ambiguous target or failed rollback.
5. Only then allow a gated MOD setter preserving OEM callbacks and correct lifecycle.

## Grounded research and source code

- [Image/PQ exact smali read/write](../PICTURE_BLOCK_STATUS_8054.md)
- [TvServices 200 config mappings](../settings_routes_8054.json)
- [Settings observer and native routing](../EDID_GLOBAL_ROUTE_8054.md)
- [MOD v41 picture panel](../../../app/tvsettingsplus-mod/src/exact8054/java/dev/projectorsoft/tvsettings/NativePicturePanel.java)
- [Original OEM controller audit](../../../docs/OEM_MENU_CONTROLLER_AUDIT_2026-10-23.md)

**No APK build, live projector writes or native ELF claims were made by creating this report.**

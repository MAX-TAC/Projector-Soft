# Exact-8054 MediaTek native/API audit

**Status: source located; Java/DEX mapping confirmed; native ELF audit not yet executed on the actual dump.**

## Provenance — corrected

The complete owner-supplied firmware image **does exist** and is publicly hosted as a GitHub release:

- Repository: [MAX-TAC/FlixTone_8054_Backup](https://github.com/MAX-TAC/FlixTone_8054_Backup).
- Release/tag: [FlixTone_8054_Backup](https://github.com/MAX-TAC/FlixTone_8054_Backup/releases/tag/FlixTone_8054_Backup).
- Archive: `FlixTone_8054_Backup.zip`, 2,011,878,129 bytes, SHA-256 `4f53f2224c98b4c832a19a591b9dd7f925bde28528b5674a09732c0f0e41d362`.
- Contained `super.img`: 2,225,078,272 bytes. Previously extracted partitions: `system_a`, `vendor_a`, `product_a`, `system_ext_a` (EROFS).
- Prior successful analysis: [Factory static audit Actions run](https://github.com/MAX-TAC/FlixTone_8054_Backup/actions/runs/43968729249); existing extraction tools in backup repo `tools/inspect_8054_draft_safely.py` and `tools/deep_audit_8054_stock.py`.
- Exact `Factory.apk`: `/system_ext/app/Factory/Factory.apk`; package `mediatek.factorymenu.ui`, SHA-256 `2f0e2cbc133c97cb4b1383f7a4576fab78715dae72a8e590deb479a4196e0996`.

Earlier wording that the owner's **full original dump is unavailable** was incorrect. In particular, `Factory.apk` contains no embedded `.so`, but the **system/vendor partitions** in the image provide the native-library search surface. Availability of a dump and successful extraction of exact native ELF bytes are distinct claims.

## What is already established (static exact-8054)

- 200 `Settings` to MediaTek config-ID mapping rows: [settings_routes_8054.json](../settings_routes_8054.json). These are mappings, **not** 200 verified working APIs.
- TvServices Java path: `TVMenuSettingsService.SettingObserver` → `updateConfigData` → `TVSettingConfig.setConifg(String,int)` → `MtkTvConfigBase.setConfigValue(String,int)`.
- Read path: `TVSettingConfig.getConfigValueInt(String)` → `MtkTvConfigBase.getConfigValue`.
- Exact Java JNI boundary for regular config writes: `TVNativeWrapper.setConfigValue_native(-1, configId, value, 3)`. This is a traced *wrapper invocation*, not a proven per-input hardware effect.
- Factory functions include MediaTek `MtkTvFApiAudio`, `MtkTvFApiDisplay`, `MtkTvFApiSystem`, `MtkTvFApiInputSource` and `vendor.mediatek.tv.mtktvfactory@1.0` HIDL client wrappers. Many functions are factory/calibration APIs and must not be exposed as ordinary user settings.

## Native-analysis method

1. Validate SHA-256 of the source archive **before** extraction.
2. Reuse **existing exact-8054** liblp + EROFS extraction tools from the backup repo, rather than substitute an unrelated MediaTek firmware.
3. Inventory `*.so` from system/vendor/product/system_ext; collect ELF architecture, DT_NEEDED, selected exported JNI/HIDL symbols, and *allowlisted* config key strings.
4. Trace from Java native declaration through exported JNI symbol or `RegisterNatives` / HIDL client where discoverable; classify static vs verified runtime.
5. **Do not** equate an exported symbol with reachability, hardware capability, or stable API surface. Stripped symbols, dynamic registration and Binder/HIDL calls require additional analysis.
6. Never publish raw system dumps, private partitions, device keys, identifiers, or entire binary strings tables in this *public* repo. Only sanitized textual evidence.
7. Prioritize user settings: PQ (contrast/HDR/MEMC/color), sound modes/EQ, HDMI/CEC; keep projector mechanisms and thermal control as separate owners until the routes prove otherwise.

## Files and status

- [TRACE_001_PICTURE_CONTRAST.md](TRACE_001_PICTURE_CONTRAST.md): completed exact-8054 **Java/static** trace, hardware context not yet verified.
- [tools/audit_mediatek_native_8054.py](../../../tools/audit_mediatek_native_8054.py): repeatable read-only native ELF inventory and JNI/HIDL candidate extraction.
- Planned workflow: verified owner release → partition extraction → sanitized native report in GitHub Actions.
- **No automatic APK changes, device writes, firmware flashing or live ADB execution.** Future claims of native verification must cite an actual completed run and observed ELF results.

## Source references

- [Factory audit results and PEQ/MediaTek architecture](https://github.com/MAX-TAC/FlixTone_8054_Backup/actions/runs/43968729249)
- [Exact-8054 selected smali](../SELECTED_SMALI_TRACE_FINDINGS_8054.md)
- [PQ and MediaTek JNI routes](../PICTURE_BLOCK_STATUS_8054.md)
- [EQ and sound mode map](../AUDIO_EQ_PQ_STATIC_ROUTE_8054.md)
- [EDID and TvServices observer](../EDID_GLOBAL_ROUTE_8054.md)

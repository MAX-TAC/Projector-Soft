# TvSettingsPlus MOD v41 R5 — Picture ordering, System cleanup and launcher experiment

**2026-11-22 — status: source change / CI and physical confirmation required**

## Evidence from owner
- On live 8054, setting saturation=0 changed the film/video in HDRezka to grayscale, while Android Home and application UIs stayed colored.
- Brightness, contrast and other native Picture scalars affect the video plane.
- Gamma demonstrably changes the video image; gamma and brightness should remain independent controls.
- The transparent in-pane interface from R4 now works.
- Settings button on the remote still launches the Chinese OEM settings.
- Chinese OEM settings have a launcher icon; MOD must have its own separate icon and test a reversible remote route without disabling Chinese OEM.

**Interpretation:** PQ consumer backend is applied to the MediaTek video-plane path and does not automatically affect SurfaceFlinger Android UI. Do not replay PQ into every view or force grayscale across Android. The HDR/inputGroup differences remain explicitly out of scope.

## R5 changes
- Picture order: HDR, MEMC first; Picture Mode and scalars follow; Gamma immediately after Hue; Color temperature link follows Gamma.
- Projection: `Размер текста` moved here (under the other projection/display controls).
- System: empty EnterprisePrivacy page removed; `Безопасность` uses white-tinted `ic_lock`; `Питание` uses white-tinted exact OEM `ic_settings_power` (ID 0x7f08022f).
- Android TV launcher: new exported **activity-alias** `com.android.tv.settings.mod.SettingsLauncher`, targeting the original `com.android.tv.settings.MainSettings`. It has explicit label `TvSettingsPlus MOD`, original exact 8054 Settings icon `0x7f08022e` and TV banner `0x7f080142`, and MAIN/LAUNCHER/LEANBACK_LAUNCHER intents.
- Optional **test-only implicit Android settings entry**: same alias handles action `android.settings.SETTINGS` with DEFAULT category, to test if this matches the owner's remote behavior. No OEM Chinese activity/alias is modified, disabled or replaced.

## Important remote limitation
KEYCODE_SETTINGS can be handled at the OEM input policy / launcher / KeyLayout layer, before the Android app receives it. An existing explicit KuSettings component launch **will not** be intercepted by an implicit Android Settings alias. A launcher alias is not a global key remapper; do NOT claim it changes the remote. Use one press + logcat to identify the actual route before trying any binary or keylayout modification.

## Read-only checks on projector
After verified R5 build and backup of installed v41 APK:

```sh
adb shell cmd package query-activities -a android.intent.action.MAIN -c android.intent.category.LEANBACK_LAUNCHER | grep -Ei 'SettingsLauncher|tv.settings' || true
adb shell am start -n com.android.tv.settings/com.android.tv.settings.mod.SettingsLauncher
adb shell am start -a android.settings.SETTINGS
```

On the phone/PC, save a trace of **one physical Settings press**:

```sh
adb logcat -c
# Press the physical Settings button once, then run:
adb shell dumpsys activity activities > /sdcard/Download/8054_settings_key_activities.txt
adb logcat -d -v threadtime > /sdcard/Download/8054_settings_key_logcat.txt
```

If it opens Chinese OEM settings, keep it; don't disable packages or remap keys blindly. Check startup ComponentName and key paths from logcat. Existing Chinese launcher remains an independent entry.

## Acceptance
- Test icon appears in TV and/or conventional launcher (launcher filtering may hide system aliases).
- Opening icon starts MOD MainSettings; Chinese icon still launches Chinese UI.
- All screens remain reachable and transparent.
- R5 Picture/System order matches above.
- Button behavior is *reported as confirmed only after physical test*. Revert possible via prior signed v41 APK.

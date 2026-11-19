# TvSettingsPlus MOD v28 — native button sound and power

**Status:** software-tested OEM settings migration, based on original 8052 factory APK. No 8054 alpha adaptation.

## Native audio setting

- Added **«Звук кнопок»** to `Звук · Проектор` as an Android TV switch.
- Original factory `SoundFragment` and `SoundSubMenuController` both read/write
  `Constant.SETTINGS_KEY_SOUND_EFFECTS_ENABLED` via `SettingsUtil.getSystemValueInt`
  and `putSystemValueInt`. The MOD resolves the original constant reflectively;
  never guesses or hardcodes its value.
- The original factory uses default **0** when the key is not set, retained here.
  Writes use `Settings.System.putInt` and verify readback before accepting the
  new checkbox state.
- Does **not** manipulate eARC, EQ DSP, Bluetooth, key sound assets or volume.
  If the OEM constant is absent or state malformed, the control is hidden.

## Native power setting (v27 preserved)

- `Питание · Проектор → Включать после подачи питания` still delegates to
  `MtkUtil.isElectricityBoot` / `putElectricityBoot` with readback verification.

## Test status and limits

- Added `OemKeyTonePolicyTest`: unset value, binary state, invalid state,
  failed write, unchanged readback, idempotence.
- Java 8 Android SDK build, D8 DEX build, pure JVM tests, OEM APK rebuild,
  platform certificate match, original resources/DEX checks run through CI.
- Stock KuSettings and KuSourceRJ remain enabled. Runtime behavior requires
  eventual hardware test; this is not a completed migration or an 8054 build.

## Audit source

GitHub Actions read-only JADX audit of stock KuSettings/TvSettingsPlus:
`audit-remaining-settings-for-mod.yml` (run 43090803452).
SoundFragment line ~217 and SoundSubMenuController line ~215 both use
the same OEM key and Settings.System numeric 0/1.

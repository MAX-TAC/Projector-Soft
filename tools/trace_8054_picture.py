"""Print exact stock smali bodies needed to verify the PIC03–05 route."""
import argparse
import re
from pathlib import Path


SETTINGS = {
    "PictureActivity": r"createSettingsFragment",
    "SettingsFragmentProviderImpl": r"newSettingsFragment",
    "PictureFragment": (
        r"onCreatePreferences|onPreferenceChange|onPfChange|onResume|createPreferences|"
        r"updateSomePrefEnabled|updateAllPictureValue|updatePicPreUI|.*lambda.*onPreferenceChange.*"
    ),
    "PreferenceConfigUtils": (
        r"putSettingValueInt|updatePreferenceChanged|onPreferenceValueChange|"
        r"createProgressDialogPreference(?:Trans)?|getACFGId"
    ),
    "SettingsUtil": r"putGlobalValueInt",
    "PictureModeValueSecondaryController": r"init.*|update.*|show.*",
    **{f"PictureModeValueSecondaryController${i}": r"onValueChanged" for i in (1, 2, 3)},
}
SERVICES = {
    "TVMenuSettingsService": r"updateConfigDataChanged|equalsCurrentAcfgValue|formatSettingToConfigId",
    "TVMenuSettingsService$SettingObserver": r"onChange",
    "TVSettingConfig": r"getConfigValueInt|setConifg",
    "MtkTvConfigBase": r"getConfigValue|setConfigValue|getMinMaxConfigValue",
}
METHOD_START = re.compile(r"^\.method .*?([^\s(]+)\(")
INPUT_CALL = re.compile(
    r"invoke-.*(?:->(?:getCurrentInputSource(?:Type)?|getCurrentSource(?:Type)?|"
    r"getCurrentInput|onInputSourceChanged|onSourceChanged)\(|"
    r"->a_hidl_mtktvfapi_inpsrc_get_current_input_source(?:_type)?\()",
    re.IGNORECASE,
)


def trace(root, selections):
    selected = set()
    for path in sorted(root.glob("smali*/**/*.smali")):
        mask = selections.get(path.stem)
        if mask is None and path.stem != "TVMenuSettingsService":
            continue
        if path.stem == "PictureFragment" and "partnercustomizer" not in path.parts:
            continue
        owner = path.relative_to(root).as_posix()
        method = None
        for line_number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
            start = METHOD_START.match(line)
            if start:
                name = start.group(1)
                method = line if mask is not None and re.fullmatch(mask, name) else None
                if method:
                    selected.add((path.stem, name))
                    print(f"PIC8054_METHOD {owner}:{line_number} {line}")
            elif method and not line.startswith((".line ", ".end method")) and line.strip():
                print(f"PIC8054_LINE {line_number} {line.strip()}")
            if line == ".end method":
                method = None
            if INPUT_CALL.search(line):
                print(f"PIC8054_INPUT {owner}:{line_number} {line.strip()}")
    return selected


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("settings", type=Path)
    parser.add_argument("services", type=Path)
    args = parser.parse_args()
    settings = trace(args.settings, SETTINGS)
    services = trace(args.services, SERVICES)
    required_settings = {
        ("PictureActivity", "createSettingsFragment"),
        ("PictureFragment", "onCreatePreferences"),
        ("PictureFragment", "onPreferenceChange"),
        ("PictureFragment", "onPfChange"),
        ("PictureFragment", "onResume"),
        ("PreferenceConfigUtils", "updatePreferenceChanged"),
        ("PreferenceConfigUtils", "putSettingValueInt"),
    }
    required_services = {
        ("TVMenuSettingsService$SettingObserver", "onChange"),
        ("TVMenuSettingsService", "updateConfigDataChanged"),
        ("TVSettingConfig", "getConfigValueInt"),
        ("TVSettingConfig", "setConifg"),
    }
    missing = (required_settings - settings) | (required_services - services)
    if missing:
        raise SystemExit(f"Missing required OEM methods: {sorted(missing)}")


if __name__ == "__main__":
    main()

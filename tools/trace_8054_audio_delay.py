"""Inspect stock audio delay and remaining settings entry points from exact smali."""
import re
import sys
from pathlib import Path


METHOD = re.compile(r"(?ms)^\.method [^\n]*?([^\s(]+)\([^\n]*\n.*?^\.end method$")
RELEVANT = re.compile(
    r"sound_(?:speaker|spdif)_delay|g_audio__(?:speaker|spdif)_delay|"
    r"->(?:getDigitalZoom|setDigitalZoom)|(?:FourCorner|DigitalZoom)Activity;|"
    r"tv_timer_power_off_timer_values|Lcom/my/(?:settings/activity/ProjectionActivity|service/MySetTimeService);"
)
EXACT = {
    "PreferenceConfigUtils": {"createNewProgressPreferencesss", "createProgressDialogPreferenceTrans",
                              "getSettingValueInt", "updatePreferenceChanged", "onPreferenceValueChange"},
    "ProgressBarVerticalFragment": {"onCreatePreferences", "onPreferenceChange"},
    "TVMenuSettingsService": {"reorganizeSettingValueIntToConfigValue", "updateConfigData"},
    "TVMenuSettingsService$2": {"run"},
    "TVMenuSettingsService$3": {"run"},
    "TVSettingConfig": {"getMin", "getMax", "getConfigValueInt"},
    "AudioOutputActivity": {"createSettingsFragment"},
}


def trace(root):
    for path in sorted(root.glob("smali*/**/*.smali")):
        source = path.read_text(encoding="utf-8")
        exact = EXACT.get(path.stem, set())
        if not exact and not RELEVANT.search(source):
            continue
        for match in METHOD.finditer(source):
            name, body = match[1], match[0]
            if name not in exact and not RELEVANT.search(body):
                continue
            lines = body.splitlines()
            start = source.count("\n", 0, match.start()) + 1
            print(f"SND8054_METHOD {root.name}/{path.relative_to(root)}:{start} {lines[0]}")
            # Compact large mapping and constructor methods around proven callsites.
            compact = len(lines) > 330 and name not in exact
            selected = set()
            if compact:
                for i, line in enumerate(lines):
                    if RELEVANT.search(line):
                        selected.update(range(max(1, i-2), min(len(lines), i+14)))
            for i, line in enumerate(lines[1:], 1):
                clean = line.strip()
                if clean and not clean.startswith((".line ", ".local ", ".end local ")):
                    if not compact or i in selected:
                        print(f"SND8054_LINE {start+i} {clean}")


if __name__ == "__main__":
    for directory in sys.argv[1:]:
        trace(Path(directory))

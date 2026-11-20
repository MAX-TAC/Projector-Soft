"""Collect narrow STOCK evidence for named PQ and EQ editor contracts."""
import re
import sys
from pathlib import Path

root = Path(sys.argv[1])
wanted = {
    "PictureModeFragment": r"onCreatePreferences|onPreferenceTreeClick|initPreferences|getEntries",
    "ColorTemperatureFragment": r"onCreatePreferences|onPreferenceChange|updateColorTempItemValue|initPreferences",
    "GammaFragment": r"onCreatePreferences|onPfTreeClick|initPreferences",
    "MJCFragment": r"onCreatePreferences|doItemInit|findAllPreferences|onPreferenceChange",
    "ProgressBarVerticalFragment": r"onCreatePreferences|onPreferenceChange|onStart|onResume|onCreateView|onDestroy",
    "ProgressDialogPreference": r"<init>|onClick|onBindViewHolder",
    "NewProgressPreference": r"onKey|onBindViewHolder|setValue|onProgress|lambda|onChange",
    "ProgressBarDialog": r".",
    "PictureResetActivity": r".",
    "AudioResetActivity": r".",
    "AudioResetActivity$ConfirmFragment": r".",
    "PictureResetActivity$ConfirmFragment": r".",
    "PictureResetFragment": r"onCreate|onPreference|reset",
    "AudioResetFragment": r"onCreate|onPreference|reset",
    "EqualizerDetailFragment": r"initPreferences|onPreferenceTreeClick|onResume|onCreate",
    "PreferenceConfigUtils": r"getArrayIdsByCustomer|createNewProgressPreferencesss|createProgressDialogPreference",
    "PartnerSettingsConfig": r"getSettingsList|getArray",
    "SoundModeFragment": r"onCreatePreferences|onPreferenceTreeClick|initPreferences|onPfTreeClick",
}
method = re.compile(r'(?ms)^\.method .*?^\.end method')
for path in sorted(root.glob('smali*/**/*.smali')):
    if path.stem not in wanted:
        continue
    data = path.read_text(encoding='utf-8')
    print('CLASS', path.relative_to(root), data.splitlines()[0])
    for match in method.finditer(data):
        s = match[0].splitlines()
        if re.search(wanted[path.stem], s[0]):
            print('METHOD', path.relative_to(root), data.count('\n',0,match.start())+1, s[0])
            for line in s[1:-1]:
                if line.strip() and not line.strip().startswith((".line", ".local", ".end local")):
                    print(line)

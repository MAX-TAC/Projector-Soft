"""Record the exact STOCK UI entry, fragment lifecycle and OEM entry points."""
import re
import sys
from pathlib import Path

CLASSES = {
    'com/android/tv/settings/MainSettings': r'.',
    'com/android/tv/settings/MainFragment': r'onCreate(View|Preferences)?|onViewCreated|onResume',
    'com/android/tv/settings/TvSettingsActivity': r'onCreate|onNewIntent|Fragment',
    'com/android/tv/settings/SettingsPreferenceFragment': r'onCreate|onViewCreated|Preference',
    'com/android/tv/settings/uicustom/MainActivity': r'onCreate|onNewIntent|initView|initData|switchPage',
    'com/android/tv/settings/uicustom/fragment/general/GeneralFragment': r'Layout|initView',
    'com/android/tv/settings/uicustom/fragment/general/GeneralFragment$4': r'onClick',
    'com/android/tv/settings/uicustom/floatmenu/FloatMenuService': r'on(StartCommand|Bind)|command',
    'com/android/tv/settings/uicustom/floatmenu/controller/general/GeneralSubMenuController$4': r'onClick',
}

root = Path(sys.argv[1])
for suffix, selectors in CLASSES.items():
    candidates = list(root.glob(f'smali*/{suffix}.smali'))
    if len(candidates) != 1:
        print('NOT FOUND', suffix, flush=True)
        continue
    data = candidates[0].read_text(encoding='utf-8')
    print('CLASS', candidates[0].relative_to(root), data.splitlines()[1])
    for body in re.findall(r'(?ms)^\.method .*?^\.end method', data):
        declaration = body.splitlines()[0]
        if re.search(selectors, declaration):
            print('\n'.join(line for line in body.splitlines()
                            if '.method ' in line or re.search(r'invoke-|new-instance|const[- /]|return|^    if-|^    iget|^    sget', line)))

for file in root.glob('smali*/**/GeneralSubMenuController.smali'):
    text = file.read_text(encoding='utf-8')
    print('OEM GENERAL', file.relative_to(root))
    for line in text.splitlines():
        if line.startswith('.field') or re.search(r'getId|findViewById|setOnClickListener|setOnKeyListener', line):
            print(line)

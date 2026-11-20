"""Report the original 8054 root resource resolver and runtime controllers."""
import re
import subprocess
import sys
from pathlib import Path

decoded, apk, aapt = sys.argv[1:]
root = Path(decoded)
types = {'MainFragment', 'MainSettings', 'PreferenceControllerFragment',
         'SuggestionQuickSettingPrefsContainer', 'SuggestionQuickSettingPrefsContainer$1',
         'MultiCastFragment', 'R$xml'}
for path in sorted(root.glob('smali*/com/android/tv/settings/**/*.smali')):
    if path.stem not in types:
        continue
    print('\n===', path.relative_to(root), flush=True)
    body = path.read_text(encoding='utf-8')
    if path.stem == 'R$xml':
        print('\n'.join(line for line in body.splitlines() if 'main_prefs' in line))
    elif path.stem == 'MultiCastFragment':
        print('\n'.join(line for line in body.splitlines() if re.search(
            r'\.method|Settings\$|putExtra|->start|[Mm]ulticast|[Mm]iracast|WifiP2p', line)))
    else:
        print(body)
names = subprocess.check_output([aapt, 'list', apk], text=True).splitlines()
for name in names:
    if re.search(r'/main_prefs(?:_vendor|_x)?\.xml$', name):
        print('EXACT_XML', name, flush=True)
        subprocess.run([aapt, 'dump', 'xmltree', apk, name], check=True)
res = subprocess.check_output([aapt, 'dump', 'resources', apk], text=True)
for line in res.splitlines():
    if re.search(r'xml/main_prefs|id/report_bug|string/(general_settings|bug_report)|drawable/ic_(?:wifi|network|app|system|sound|input|bluetooth|display)', line):
        print(line)

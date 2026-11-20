"""Trace the stock audio-format and timer controls outside general OEM menus."""
import re
import sys
from pathlib import Path

root = Path(sys.argv[1])
method_pattern = re.compile(r'(?ms)^\.method .*?^\.end method')
selected = {
    'AudioOutputFragment': r'onPreferenceTreeClick|getDigitalOutputEntries|onResume',
    'AudioOutputActivity': r'createSettingsFragment',
    'DigitalOutputFragment': r'onCreatePreferences|onPreference|onResume|SP[Dd][Ii][Ff]',
    'DigitalAudioFormatFragment': r'onCreatePreferences|onPreference|onResume',
    'TimerSetActivity': r'createSettingsFragment|onCreate',
    'TimerFragment': r'onCreatePreferences|onPreference|update|onResume',
    'PowerActivity': r'createSettingsFragment',
    'PowerFragment': r'onCreatePreferences|onPreference|createPreferences',
}
for file in sorted(root.glob('smali*/**/*.smali')):
    if file.stem not in selected and not re.search(r'(Digital|Spdif|SPDIF|SoundOutput).*Fragment',file.stem):
        continue
    context = file.read_text(encoding='utf-8')
    for match in method_pattern.finditer(context):
        body = match[0]
        declaration = body.splitlines()[0]
        if not re.search(selected.get(file.stem,r'onCreatePreferences|onPreference|onResume'),declaration):
            continue
        print('ROUTE',file.relative_to(root),context.count('\n',0,match.start())+1)
        print(body)

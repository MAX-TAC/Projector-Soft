"""Trace the stock audio-format, timer and two-panel list controls."""
import re
import sys
from pathlib import Path

root = Path(sys.argv[1])
method_pattern = re.compile(r'(?ms)^\.method .*?^\.end method')
selected = {
    'AudioOutputFragment': r'onPreferenceTreeClick|getDigitalOutputEntries|showVolumeTipsDialog|onResume',
    'AudioOutputActivity': r'createSettingsFragment',
    'DigitalOutputFragment': r'onCreatePreferences|onPreference|onResume|initPreferences|onPfTreeClick|finishActivity',
    'DigitalAudioFormatFragment': r'onCreatePreferences|onPreference|onResume',
    'TimerSetActivity': r'createSettingsFragment|onCreate',
    'TimerFragment': r'onCreatePreferences|onPreference|update|onResume',
    'PowerActivity': r'createSettingsFragment|onCreate|onResume',
    'PowerFragment': r'onCreatePreferences|onPreference|setupPreferences|setSleepTimerType|onResume|onPause',
    'TwoPanelListPreferenceDialogFragment': r'onNavigateToPreview|onCreate|onResume|Selection',
}
for file in sorted(root.glob('smali*/**/*.smali')):
    context = file.read_text(encoding='utf-8')
    if file.stem not in selected and not re.search(
            r'(Digital|Spdif|SPDIF|SoundOutput).*Fragment|TwoPanelListPreferenceDialogFragment\
    for match in method_pattern.finditer(context):
        body = match[0]
        declaration = body.splitlines()[0]
        if not re.search(selected.get(file.stem,r'onCreatePreferences|onPreference|onResume|onClick|onBindViewHolder'),declaration):
            continue
        print('ROUTE',file.relative_to(root),context.count('\n',0,match.start())+1)
        print(body)
, file.stem):
        if 'Lcom/android/tv/settings/partnercustomizer/power/PowerActivity;' in context:
            for match in method_pattern.finditer(context):
                if 'PowerActivity;' in match[0] and '->startActivity(' in match[0]:
                    print('POWER ENTRY', file.relative_to(root), context.count('\n', 0, match.start()) + 1)
                    print(match[0])
        continue
    for match in method_pattern.finditer(context):
        body = match[0]
        declaration = body.splitlines()[0]
        if not re.search(selected.get(file.stem,r'onCreatePreferences|onPreference|onResume'),declaration):
            continue
        print('ROUTE',file.relative_to(root),context.count('\n',0,match.start())+1)
        print(body)

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
power_activity = 'Lcom/android/tv/settings/partnercustomizer/power/PowerActivity;'
for file in sorted(root.glob('smali*/**/*.smali')):
    context = file.read_text(encoding='utf-8')
    names = selected.get(file.stem)
    if names is None and (
            re.search(r'(Digital|Spdif|SPDIF|SoundOutput).*Fragment', file.stem)
            or file.stem.startswith('TwoPanelListPreferenceDialogFragment$')):
        names = r'onCreatePreferences|onPreference|onResume|onClick|onBindViewHolder'
    if names is None and power_activity not in context:
        continue
    for match in method_pattern.finditer(context):
        body = match[0]
        if names is not None and re.search(names, body.splitlines()[0]):
            label = 'ROUTE'
        elif power_activity in body and '->startActivity(' in body:
            label = 'POWER ENTRY'
        else:
            continue
        print(label, file.relative_to(root), context.count('\n', 0, match.start()) + 1)
        print(body)

"""Trace candidate stock picture controls and verified Android destinations."""
import re
import subprocess
import sys
from pathlib import Path

decoded, apk, aapt = sys.argv[1:]
root = Path(decoded)
keys = re.compile(
    r'picture_gamma|end_user_calibration_gamma|dynamic_contrast|adaptive_luma|picture_list_hdr|'
    r'picture_(?:red|green|blue)_gain|tv_picture_advance_video_mjc_effect|disableAutoBackLight',
    re.IGNORECASE)
types = re.compile(
    r'PictureFragment|ProgressBarVerticalFragment|ColorTemperatureFragment|'
    r'AdvanceSettingsSecondaryController|Gamma|AdvancedPicture|MJCFragment|FlavorUtils')
for path in sorted(root.glob('smali*/**/*.smali')):
    if not types.search(path.stem):
        continue
    text = path.read_text(encoding='utf-8')
    for match in re.finditer(r'(?ms)^\.method .*?^\.end method', text):
        body = match[0]
        sig = body.splitlines()[0]
        if keys.search(body) or (path.stem == 'FlavorUtils' and re.search(r'\bgetFlavor\(', sig)):
            print('METHOD', path.relative_to(root), text.count('\n', 0, match.start()) + 1, sig)
            lines = body.splitlines()
            for i, line in enumerate(lines):
                if keys.search(line) or (path.stem == 'FlavorUtils' and re.search(r'invoke-|const|return', line)):
                    for neighbor in lines[max(1, i-1):min(len(lines), i+20)]:
                        if re.search(r'const|invoke-|return|if-|:[a-zA-Z]+_', neighbor):
                            print(neighbor)
resources = subprocess.check_output([aapt, 'dump', 'resources', apk], text=True)
for line in resources.splitlines():
    if 'config_settings_flavor' in line:
        print(line)
manifest = subprocess.check_output([aapt, 'dump', 'xmltree', apk, 'AndroidManifest.xml'], text=True)
for node in re.split(r'(?m)^    E: ', manifest):
    if node.startswith(('activity ', 'activity-alias ')) and re.search(
            r'About|HomeSettings|Dream|ScreenSaver|Screensaver|PowerActivity|AccessoriesActivity|Reset|DeviceName|MultiCast',
            node):
        print('MANIFEST', '\n'.join(line for line in node.splitlines()
                                    if re.search(r'android:name|android:exported|android:enabled|android:permission', line)))

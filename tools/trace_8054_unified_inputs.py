"""Identify exact 8054 key-layout and Settings listener evidence without inferring ownership."""
import re
import sys
from pathlib import Path

decoded, unpacked = map(Path, sys.argv[1:])
key_entry = re.compile(r'^\s*key\s+\S+\s+(?:SETTINGS|TV_INPUT)(?:\s|$)')
layouts = []
for folder in unpacked.glob('*_tree'):
    for path in folder.glob('**/usr/keylayout/*.kl'):
        for line in path.read_text(errors='replace').splitlines():
            if key_entry.search(line):
                layouts.append((path.relative_to(unpacked).as_posix(), line.strip()))
for path, match in sorted(set(layouts)):
    print('KEY_LAYOUT', path, match)
if not layouts:
    print('KEY_LAYOUT no matching stock label in extracted partitions')

owners = ('/uicustom/MainActivity.smali', '/uicustom/floatmenu/FloatMenuManager.smali',
          '/uicustom/floatmenu/FloatMenuService.smali')
pattern = re.compile(r'(?ms)^\.method .*?^\.end method')
for path in sorted(decoded.glob('smali*/**/*.smali')):
    if not path.as_posix().endswith(owners):
        continue
    for match in pattern.finditer(path.read_text(errors='replace')):
        method = match[0]
        first = method.splitlines()[0]
        if not re.search(r'onKey|dispatchKey|onStartCommand|onNewIntent|jumpToExternalActivity',first):
            continue
        print('UI_METHOD', path.relative_to(decoded), first)
        for line in method.splitlines():
            if re.search(r'getKeyCode|onKey|dispatchKey|startActivity|->showMenu|0xb0\b|0xb2\b|const-string',line):
                print(line.strip())

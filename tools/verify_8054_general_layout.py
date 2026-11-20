"""Identify the shipped layout containing the exact General Screensaver anchor."""
import re
import struct
import subprocess
import sys
import zipfile

apk = sys.argv[1]
anchor = '0x7f0a043a'
hits = []
with zipfile.ZipFile(apk) as source:
    for name in source.namelist():
        if not name.startswith('res/') or not name.endswith('.xml'):
            continue
        contents = source.read(name)
        if struct.pack('<I', int(anchor, 16)) not in contents:
            continue
        tree = subprocess.check_output(['aapt', 'dump', 'xmltree', apk, name], text=True)
        elements = []
        for line in tree.splitlines():
            node = re.match(r'^(\s*)E: ([^ ]+)', line)
            if node:
                level = len(node[1])
                while elements and elements[-1][0] >= level:
                    elements.pop()
                elements.append((level, node[2]))
            if 'android:id(' in line and anchor in line and len(elements) >= 2:
                hits.append((name, [tag for _, tag in elements]))
for hit in hits:
    print('8054 General anchor:', hit, flush=True)
if not hits:
    raise ValueError('Verified OEM General row missing')

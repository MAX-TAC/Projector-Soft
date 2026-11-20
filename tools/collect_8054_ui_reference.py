"""Collect stock binary-XML evidence for the native UI and excluded TV screens."""
import hashlib
import subprocess
import sys
import zipfile
from pathlib import Path

stock, output = Path(sys.argv[1]), Path(sys.argv[2])
output.mkdir(parents=True,exist_ok=True)
names = ('two_panel_settings_fragment', 'channels', 'channels_scan', 'channels_and_inputs_vendor',
         'channel_install_mode', 'partner_oled', 'partner_retail_mode', 'partner_mjc')
index = []
with zipfile.ZipFile(stock) as source:
    for name in names:
        candidates = [path for path in source.namelist()
                      if path.startswith('res/') and Path(path).stem == name]
        if not candidates:
            index.append(name + ' exact archive path not identified')
            continue
        for path in candidates:
            index.append(path + ' ' + hashlib.sha256(source.read(path)).hexdigest())
            dest = output / (path.removeprefix('res/').replace('/','_') + '.txt')
            dest.write_text(subprocess.check_output(
                ['aapt','dump','xmltree',str(stock),path],text=True),encoding='utf-8')
(output / 'index.txt').write_text('\n'.join(index) + '\n',encoding='utf-8')

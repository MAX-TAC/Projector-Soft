#!/usr/bin/env python3
from pathlib import Path
import re,sys,json
src=Path(sys.argv[1])/"sources"/"com/android/tv/settings/partnercustomizer/utils/ProgressBarDialog.java"
xml=Path(sys.argv[2]);dest=Path(sys.argv[3]);dest.parent.mkdir(parents=True,exist_ok=True)
s=src.read_text(errors="replace")
lines=s.splitlines()
for i,line in enumerate(lines):
 if re.search(r"setMax|setMin|setCurrentValue|progressBar.setProgress|mCurrentValue|progressBar.setMax|KEY_EQUALIZER",line):
  print(i+1,"\n","\n".join(lines[max(0,i-3):min(len(lines),i+4)]),flush=True)
for p in xml.rglob("partner_progressbar_dialog.xml"):
 print("Layout",p,"\n",p.read_text(errors="replace")[:9500],flush=True)
raw=re.findall(r"(?<![\w])(?:100|50|20|10|200|255)(?![\w])",s)
dest.write_text(json.dumps({"java_max_calls":re.findall(r"[\w.]+\.setMax\([^)]*\)",s),
 "java_min_calls":re.findall(r"[\w.]+\.setMin\([^)]*\)",s),
 "constants":sorted(set(raw)),"layout_found":any(xml.rglob("partner_progressbar_dialog.xml"))},indent=2)+"\n")

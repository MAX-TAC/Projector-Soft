#!/usr/bin/env python3
"""Inspect Apktool-decoded XML for exact OEM sliders min/max and resource IDs.
Resource file/attributes only; no SDK guess.
"""
from pathlib import Path
import json,re,sys,xml.etree.ElementTree as ET
root=Path(sys.argv[1]); out=Path(sys.argv[2]);out.parent.mkdir(parents=True,exist_ok=True)
android="{http://schemas.android.com/apk/res/android}"
records={}
for f in root.glob("res/layout*/*.xml"):
 try: tree=ET.parse(f)
 except (ET.ParseError,UnicodeError): continue
 sliders=[]
 for e in tree.iter():
  tag=e.tag
  if "TitleSeekBarView" not in tag:continue
  attrs={k.replace(android,"android:"):v for k,v in e.attrib.items()}
  sliders.append(attrs)
 if sliders:
  records[str(f.relative_to(root))]=sliders
print(json.dumps(records,ensure_ascii=False,indent=2)[:16000])
out.write_text(json.dumps(records,ensure_ascii=False,indent=2)+"\n")

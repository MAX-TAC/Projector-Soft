#!/usr/bin/env python3
"""Audit OEM screensaver and font size: numeric resources + relevant statements only."""
import json,sys,re,xml.etree.ElementTree as ET
from pathlib import Path
root=Path(sys.argv[1]);decompile=Path(sys.argv[2])
names={"rj_sleep_timeout_values","rj_screen_off_timeout_values",
       "rj_sleep_timeout_entries","rj_screen_off_timeout_entries"}
arrays={}
for p in root.glob("res/values*/*.xml"):
 if "public" in p.name:continue
 try:xml=ET.parse(p).getroot()
 except ET.ParseError:continue
 for item in xml:
  name=item.attrib.get("name")
  if name not in names:continue
  arrays.setdefault(name,[]).append({"source":str(p.relative_to(root)),
      "items":["".join(it.itertext()).strip() for it in item]})
controllers={}
for name in ("ScreensaverSecondaryController","FontSizeSettingsSecondaryController","SettingsUtil"):
 matches=list(decompile.rglob(name+".java"))
 preferred=[p for p in matches if ("uicustom/util" if name=="SettingsUtil"
            else "uicustom/floatmenu/controller/general") in str(p)]
 p=preferred[0] if len(preferred)==1 else matches[0] if len(matches)==1 else None
 if not p:
  controllers[name]={"found":False,"candidates":[str(p) for p in matches[:5]]}
  continue
 source=p.read_text(errors="replace").splitlines()
 if name.startswith("Screensaver"):
  lines=[(i+1,line.strip()) for i,line in enumerate(source) if
    (i+1 in range(85,134) or i+1 in range(142,163))
    and re.search(r"(?i)(setDream|getDream|curScreensaver|Integer.parseInt|SettingsUtil|screen_off_timeout|sleep_timeout|\.length|setValueText|setValueArrays|setCurValue)",line)]
 elif name=="SettingsUtil":
  lines=[(i+1,line.strip()) for i,line in enumerate(source) if
    re.search(r"(?i)(getSystemValueInt|putSystemValueInt|Settings.System|Settings.Secure|return.*getInt|return.*putInt)",line)]
 else:
  lines=[(i+1,line.strip()) for i,line in enumerate(source) if
    re.search(r"(?i)(font_scale|fontScale|setFont|float|0\\.\\d+|1\\.\\d+|Settings.System|onClick|setOn)",line)]
 controllers[name]={"found":True,"lines":lines[:120],"line_count":len(source)}
print(json.dumps({"arrays":arrays,"controllers":controllers},ensure_ascii=False,indent=2))

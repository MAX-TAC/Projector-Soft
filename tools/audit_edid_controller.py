#!/usr/bin/env python3
from pathlib import Path
import json,re,sys
root=Path(sys.argv[1])/"sources";out=Path(sys.argv[2]);out.parent.mkdir(parents=True,exist_ok=True)
targets=[
"com/android/tv/settings/uicustom/floatmenu/controller/source/EdidSourceSecondaryController.java",
"com/android/tv/settings/uicustom/floatmenu/controller/source/PowerOnSourceSecondaryController.java",
"com/android/tv/settings/uicustom/floatmenu/controller/source/SourceSubMenuController.java"
]
result={}
for rel in targets:
 p=root/rel
 if not p.exists():print("MISSING",rel);continue
 s=p.read_text(errors="replace")
 if "EdidSourceSecondaryController" in rel:
  print("FULL EDID",rel,"\n",s[:32000],flush=True)
 elif "PowerOnSourceSecondaryController" in rel:
  print("FULL POWERON SOURCE",rel,"\n",s[:15000],flush=True)
 result[rel]={"lines":len(s.splitlines()),
   "keys":sorted(set(re.findall(r'"([\w.:]{5,110})"',s)))[:150],
   "methods":sorted(set(re.findall(r"\b(?:set|get|put)[A-Z]\w+\(",s)))[:160]}
out.write_text(json.dumps(result,ensure_ascii=False,indent=2)+"\n")

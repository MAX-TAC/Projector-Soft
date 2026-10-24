#!/usr/bin/env python3
"""Extract checked key names and value mappings for sound/image advanced inline migration.
No decompiled Java source committed; output only names, constants, values.
"""
from pathlib import Path
import re,sys,json
root=Path(sys.argv[1])/"sources";out=Path(sys.argv[2]);out.parent.mkdir(parents=True,exist_ok=True)
targets=[
"com/android/tv/settings/partnercustomizer/tvsettingservice/util/PreferenceConfigUtilsBase.java",
"com/android/tv/settings/uicustom/floatmenu/controller/picture/AdvanceSettingsSecondaryController.java",
"com/android/tv/settings/uicustom/fragment/source/SourceFragment.java",
"com/android/tv/settings/uicustom/fragment/sound/SoundFragment.java",
]
result={}
for rel in targets:
 path=root/rel
 if not path.exists():
  print("MISSING",rel);continue
 text=path.read_text(errors="replace")
 fields=[]
 for ln in text.splitlines():
  if re.search(r"(?i)SOUND|HDR|MJC|MEMC|EARC|VOLUME|CEC|SOURCE|EDID|LATENCY|GAME",ln) and re.search(r"(?i)static final|SettingsUtil\\.get|SettingsUtil\\.put|switch|setOnTitleSwitch",ln):
   fields.append(ln.strip())
 relevant=sorted(set(re.findall(r'"([A-Za-z_][A-Za-z0-9_./]{2,90})"',text)))
 keys=[s for s in relevant if re.search(r"sound|hdr|mjc|memc|auto|earc|volume|source|edid|cec|latency",s,re.I)]
 print("\n==",rel,"==\n",'\n'.join(fields[:140]),flush=True)
 result[rel]={"fields":fields[:180],"literals":keys[:180]}
out.write_text(json.dumps(result,indent=2,ensure_ascii=False)+"\n")

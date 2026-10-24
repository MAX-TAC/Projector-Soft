#!/usr/bin/env python3
"""Extract OEM seekbar initial values, configured ranges and setter keys.
Print bounded decompiled method excerpts only into ephemeral CI log.
Persist concise metadata, not proprietary source.
"""
from pathlib import Path
import re,json,sys
root=Path(sys.argv[1])/"sources";out=Path(sys.argv[2]);out.parent.mkdir(parents=True,exist_ok=True)
targets=[
"com/android/tv/settings/uicustom/floatmenu/controller/picture/PictureModeValueSecondaryController.java",
"com/android/tv/settings/uicustom/floatmenu/controller/picture/ColorTempValueSecondaryController.java",
"com/android/tv/settings/uicustom/floatmenu/controller/sound/SoundModeValueSecondaryController.java",
"com/android/tv/settings/uicustom/view/TitleSeekBarView.java",
]
results={}
for name in targets:
 f=root/name
 if not f.exists():print("NOT_FOUND",name);continue
 lines=f.read_text(errors="replace").splitlines()
 blocks=[]
 for meth in ("initData","initView","setValue","onAttachedToWindow","init","onCreate"):
  for i,l in enumerate(lines):
   if not re.search(r"\b"+re.escape(meth)+r"\s*\(",l) or not re.search(r"\b(?:private|public|protected)\b",l):continue
   depth=0;begun=False;j=i
   for j in range(i,min(len(lines),i+150)):
    depth+=lines[j].count("{")-lines[j].count("}")
    if "{" in lines[j]:begun=True
    if begun and depth<=0:break
   body="\n".join(lines[i:j+1])
   print("\n----",name,meth,"line",i+1,"----\n",body[:13000],flush=True)
   blocks.append({"method":meth,"line":i+1,"oem_keys":sorted(set(re.findall(r'"((?:picture|sound|color|tv_)[^"]{0,90})"',body))),
                  "numeric_constants":sorted(set(re.findall(r"(?<![\w])(?:0|1|10|20|50|100|128|255)(?![\w])",body))),
                  "calls":sorted(set(re.findall(r"\b([\w$]+)\s*\(",body)))[:85]})
 results[name]=blocks
out.write_text(json.dumps(results,ensure_ascii=False,indent=2)+"\n")

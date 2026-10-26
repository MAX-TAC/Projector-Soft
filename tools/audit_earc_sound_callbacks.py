#!/usr/bin/env python3
"""Read selected OEM sound, eARC and CEC callbacks; emit code to CI logs only."""
import re,json,sys
from pathlib import Path
src=Path(sys.argv[1])/"sources";out=Path(sys.argv[2]);out.parent.mkdir(parents=True,exist_ok=True)
targets=[
"com/android/tv/settings/uicustom/floatmenu/controller/sound/SoundSubMenuController.java",
"com/android/tv/settings/uicustom/fragment/sound/SoundFragment.java",
"com/android/tv/settings/uicustom/floatmenu/controller/source/SourceSubMenuController.java"]
result={}
for rel in targets:
 p=src/rel
 if not p.exists():print("MISSING",rel);continue
 lines=p.read_text(errors="replace").splitlines()
 found=[]
 for i,line in enumerate(lines):
  if not re.search(r"\b(?:lambda\$|initCheck|initView|initListener|setEarc|isSupportEARC)[\w$]*\s*\(",line):continue
  if not re.search(r"(public|private|protected)",line):continue
  depth=0;start=False;j=i
  for j in range(i,min(i+160,len(lines))):
   depth+=lines[j].count("{")-lines[j].count("}")
   start|="{" in lines[j]
   if start and depth<=0:break
  b="\n".join(lines[i:j+1])
  if re.search(r"EARC|earc|surround|volume|setEarc|setHdmiCec|isSupport",b,re.I):
   print("OEM",rel,"line",i+1,"\n",b[:11500],flush=True)
   found.append({"method":line.strip(),"line":i+1,
     "calls":sorted(set(re.findall(r"\b([A-Za-z_]\w*)\(",b)))[:100],
     "key_literals":sorted(set(re.findall(r'"([^"]{3,90})"',b)))[:65]})
 result[rel]=found
out.write_text(json.dumps(result,indent=2,ensure_ascii=False)+"\n")

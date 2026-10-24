#!/usr/bin/env python3
"""Inspect precise callable OEM picture, color, sound APIs for true AOSP preference binding.
Print excerpts to CI logs; commit only safe method names and signature metadata.
"""
import json,re,sys
from pathlib import Path
root=Path(sys.argv[1])/"sources";out=Path(sys.argv[2]);out.parent.mkdir(parents=True,exist_ok=True)
targets=[
 ("com/android/tv/settings/uicustom/util/MtkUtil.java",("getLightMode","putLightMode","getPictureMode","setPictureMode","getColorTemperature","setColorTemperature","getSoundMode","setSoundMode")),
 ("com/android/tv/settings/uicustom/fragment/picture/PictureFragment.java",("initCheck","initListener","onRadioCheckedChanged","onCheckedChanged")),
 ("com/android/tv/settings/uicustom/fragment/sound/SoundFragment.java",("initCheck","initListener","onCheckedChanged")),
 ("com/android/tv/settings/uicustom/floatmenu/controller/picture/PictureModeValueSecondaryController.java",("initView","initCheck","initListener")),
 ("com/android/tv/settings/uicustom/floatmenu/controller/picture/ColorTempValueSecondaryController.java",("initView","initCheck","initListener")),
 ("com/android/tv/settings/uicustom/floatmenu/controller/sound/SoundModeValueSecondaryController.java",("initView","initCheck","initListener")),
 ("com/android/tv/settings/uicustom/floatmenu/controller/picture/AdvanceSettingsSecondaryController.java",("initView","initCheck","initListener")),
]
result={}
def method_blocks(s,name):
 lines=s.splitlines()
 found=[]
 for i,l in enumerate(lines):
  if not re.search(r"\b"+re.escape(name)+r"\s*\(",l):continue
  if not re.search(r"\b(?:public|protected|private)\b",l) or "abstract" in l:continue
  depth=0;begun=False
  for j in range(i,min(len(lines),i+175)):
   depth+=lines[j].count("{")-lines[j].count("}")
   if "{" in lines[j]:begun=True
   if begun and depth<=0:break
  text="\n".join(lines[i:j+1])
  found.append((i+1,text))
 return found[:3]
for rel,methods in targets:
 path=root/rel
 if not path.is_file():
  print("MISSING",rel);continue
 s=path.read_text(errors="replace");entries={}
 signatures=sorted(set(re.findall(r"(?m)^\s*(?:public|private|protected)\s+(?:static\s+)?[\w.$<>\[\],? ]+\s+\w+\([^)]*\)\s*(?:throws [^{]+)?\{",s)))
 for meth in methods:
  parts=method_blocks(s,meth)
  entries[meth]=[{"line":n,"declaration":b.splitlines()[0].strip(),"method_calls":sorted(set(re.findall(r"\b([\w$]+)\(",b)))[:60]} for n,b in parts]
  for n,b in parts:
   print("###",rel,"::",meth,"line",n,"\n",b[:11500],flush=True)
 print("###",rel,"ALL callable declarations\n","\n".join(signatures[:220]),flush=True)
 result[rel]={"methods":entries,"declarations":signatures[:300]}
out.write_text(json.dumps(result,indent=2,ensure_ascii=False)+"\n")

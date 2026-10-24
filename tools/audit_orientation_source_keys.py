#!/usr/bin/env python3
"""Inspect native orientation and HDMI-CEC controller actions; do not modify hardware.
Only metadata is committed; method bodies exist in build logs.
"""
import re,json,sys
from pathlib import Path
root=Path(sys.argv[1])/"sources";out=Path(sys.argv[2]);out.parent.mkdir(parents=True,exist_ok=True)
classes=[
"com/android/tv/settings/uicustom/floatmenu/controller/projector/ProjectionModeSecondaryController.java",
"com/android/tv/settings/uicustom/floatmenu/controller/source/SourceSubMenuController.java",
"com/android/tv/settings/uicustom/fragment/source/SourceFragment.java",
"com/android/tv/settings/uicustom/floatmenu/controller/projector/ProjectorSubMenuController.java"
]
records={}
for cls in classes:
 f=root/cls
 if not f.exists():
  print("MISSING",cls);continue
 lines=f.read_text(errors="replace").splitlines()
 res=[]
 for name in ("initData","initView","initCheck","onClick","onAttach","onCreate","initListener"):
  for i,line in enumerate(lines):
   if not re.search(r"\b"+re.escape(name)+r"\s*\(",line):continue
   if not re.search(r"\b(public|private|protected)\b",line):continue
   depth=0;begun=False;j=i
   for j in range(i,min(len(lines),i+170)):
    depth+=lines[j].count("{")-lines[j].count("}")
    if "{" in lines[j]:begun=True
    if begun and depth<=0:break
   body="\n".join(lines[i:j+1])
   print("=== "+cls+" "+name+" line "+str(i+1)+" ===\n"+body[:11500],flush=True)
   res.append({"method":name,"line":i+1,"keys":sorted(set(re.findall(r'"([\w.]{4,100})"',body)))[:100],
               "calls":sorted(set(re.findall(r"\b(\w+)\(",body)))[:90]})
 records[cls]=res
out.write_text(json.dumps(records,ensure_ascii=False,indent=2)+"\n")

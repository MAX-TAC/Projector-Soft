#!/usr/bin/env python3
"""Source at power-on: extract *only* original click handlers and settings calls.
Metadata json is retained; full excerpts only ephemeral CI output.
"""
import json,re,sys
from pathlib import Path
src=Path(sys.argv[1])/"sources"/"com/android/tv/settings/uicustom/floatmenu/controller/source/PowerOnSourceSecondaryController.java"
out=Path(sys.argv[2]);out.parent.mkdir(parents=True,exist_ok=True)
lines=src.read_text(errors="replace").splitlines();res={}
for method in ["lambda$initListener$1","lambda$initListener$2","lambda$initListener$3",
               "lambda$initListener$4","lambda$initListener$5","lambda$initListener$6",
               "lambda$initListener$7","lambda$initListener$8",
               "getHdmiInputId","initBoardConfig","systemToLocal"]:
 found=[]
 for i,line in enumerate(lines):
  if re.search(r"\b"+re.escape(method)+r"\s*\(",line) and re.search(r"\b(?:public|private|protected)\b",line):
   depth=0;start=False
   for j in range(i,min(i+95,len(lines))):
    depth+=lines[j].count("{")-lines[j].count("}")
    start |= "{" in lines[j]
    if start and depth<=0:break
   body="\n".join(lines[i:j+1])
   print("=== "+method+" L"+str(i+1)+" ===\n"+body[:8500],flush=True)
   found.append({"line":i+1,"signature":line.strip(),
       "properties":sorted(set(re.findall(r'"(persist\.[^"]+|HOME|USB|HW[0-9]+)"',body))),
       "calls":sorted(set(re.findall(r"\b([a-zA-Z_]\w*)\(",body)))[:100]})
 res[method]=found
out.write_text(json.dumps(res,ensure_ascii=False,indent=2)+"\n")

#!/usr/bin/env python3
"""Audit stock HDMI EDID/CEC actions and evidence of multiband EQ; commit metadata only.
Proprietary Java method excerpts are printed to CI logs, not saved.
"""
from pathlib import Path
import json,re,sys
root=Path(sys.argv[1]); output=Path(sys.argv[2]);output.parent.mkdir(parents=True,exist_ok=True)
src=root/"TvSettingsPlus"/"sources"
targets=["com/android/tv/settings/uicustom/floatmenu/controller/source/SourceSubMenuController.java",
"com/android/tv/settings/uicustom/fragment/source/SourceFragment.java",
"com/android/tv/settings/uicustom/util/MtkUtil.java"]
methods=["getEdidVersion","validateEdidVersion","lambda$initView$8","lambda$initView$9","lambda$initView$10","lambda$initView$11","lambda$initView$12","lambda$initView$13","updateEdidEnableState","initBoardConfig"]
results={}
for path in targets:
 p=src/path
 if not p.exists():continue
 lines=p.read_text(errors="replace").splitlines()
 records=[]
 for i,line in enumerate(lines):
  if not any(re.search(r"\b"+re.escape(m)+r"\s*\(",line) for m in methods):continue
  if not re.search(r"\b(public|private|protected)\b",line):continue
  depth=0;started=False;j=i
  for j in range(i,min(i+140,len(lines))):
   depth+=lines[j].count("{")-lines[j].count("}")
   if "{" in lines[j]:started=True
   if started and depth<=0:break
  body="\n".join(lines[i:j+1])
  print("\n==",path,"line",i+1,"\n",body[:12500],flush=True)
  records.append({"declaration":line.strip(),"line":i+1,
    "string_literals":sorted(set(re.findall(r'"([\w.:-]{4,120})"',body)))[:75],
    "calls":sorted(set(re.findall(r"\b([\w$]+)\(",body)))[:70]})
 results[path]=records
# Search for functional *named* EQ features across installed source packages.
findings=[]
for app in ("TvSettingsPlus","KuSettings","KuSourceRJ"):
 p=root/app/"sources"
 if not p.is_dir():continue
 for candidate in p.rglob("*.java"):
  name=candidate.name
  if not re.search(r"equaliz|audioeffect|bandlevel|eqsetting|graphic.eq",name,re.I):continue
  code=candidate.read_text(errors="replace")
  findings.append({"app":app,"class":str(candidate.relative_to(p)),
                   "methods":sorted(set(re.findall(r"\b(?:setBandLevel|setCenterFreq|setPreset|usePreset|setEqualizer|setBand|setEq)[\w]*",code)))})
results["eq_named_classes"]=findings[:100]
print("EQ matching classes:",findings[:100],flush=True)
output.write_text(json.dumps(results,ensure_ascii=False,indent=2)+"\n")

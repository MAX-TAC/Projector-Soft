#!/usr/bin/env python3
"""Static OEM brightness path comparison, output bounded method snippets only to CI logs.
Commit only API facts; never decompiled Java body.
"""
from pathlib import Path
import json,re,sys
base=Path(sys.argv[1]); out=Path(sys.argv[2]);report={}
targets={
"TvSettingsPlus":[
 ("com/android/tv/settings/uicustom/floatmenu/controller/general/BrightnessModeSettingsSecondaryController.java",("initData","initView","initCheck","initListener","onClick")),
 ("com/android/tv/settings/uicustom/fragment/general/BrightnessModeSettingsFragment.java",("initData","initView","onCheckedChanged")),
 ("com/android/tv/settings/uicustom/floatmenu/controller/general/GeneralSubMenuController.java",("getCurBrightnessModeResId","initData","onClick")),
 ],
"KuSourceRJ":[
 ("com/coolux/source/activity/BrightActivity.java",("init","initView","clickScreensaverModel")),
 ("com/coolux/source/service/BrightReceiver.java",("onReceive",)),
 ("com/coolux/source/utils/BrightUtils.java",("setBrightModel",)),
 ]}
for app,items in targets.items():
 for suffix,names in items:
  p=base/app/"sources"/suffix
  if not p.exists():
   print("MISSING",app,suffix);continue
  text=p.read_text(errors="replace").splitlines()
  meta={"methods":{},"identifiers":sorted(set(re.findall(r"\b(?:getLightMode|putLightMode|setBrightModel|setBacklight|setPwmValue|persist\.bright\.model|com\.action\.brightReceiver|TrdSystemManager)\b", "\n".join(text))))}
  for n in names:
   results=[]
   for i,line in enumerate(text):
    if not re.search(r"\b"+re.escape(n)+r"\s*\(",line):continue
    if not re.search(r"\b(?:public|private|protected)\b",line):continue
    braces=0;started=False;j=i
    while j<min(i+160,len(text)):
     if "{" in text[j]:started=True
     braces+=text[j].count("{")-text[j].count("}")
     if started and braces<=0:break
     j+=1
    body="\n".join(text[i:j+1])
    if len(body)>10000:body=body[:10000]
    print("\n###",app,suffix,n,"line",i+1,"\n"+body,flush=True)
    results.append({"at_line":i+1,"calls":sorted(set(re.findall(r"\b([\w$]+)\s*\(",body)))[:70],
                    "property_strings":sorted(set(re.findall(r'"(persist\.[^"]+|com\.action\.[^"]+)"',body)))[:30]})
   meta["methods"][n]=results[:2]
  report[app+"/"+suffix]=meta
out.parent.mkdir(parents=True,exist_ok=True)
out.write_text(json.dumps(report,ensure_ascii=False,indent=2)+"\n")

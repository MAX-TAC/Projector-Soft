#!/usr/bin/env python3
"""Inspect ProgressBarDialog scale and OEM audio EQ persistence.
Only method metadata retained in repo; bounded snippets shown in CI log.
"""
from pathlib import Path
import re,json,sys
root=Path(sys.argv[1])/"sources"
out=Path(sys.argv[2]);out.parent.mkdir(parents=True,exist_ok=True)
classes={
"com/android/tv/settings/partnercustomizer/utils/ProgressBarDialog.java":["setData","setProgressBar","initView","initViews","initData","getACFGId","setCurrentValue","onCreate","getCurrentValue","onKeyDown","setOnProgressChanged"],
"com/android/tv/settings/partnercustomizer/tvsettingservice/util/PreferenceConfigUtils.java":["createProgressDialogPreferenceTrans","createProgressDialogPreference","getSettingValueInt","putSettingValueInt","onPreferenceValueChange"]
}
result={}
for rel,names in classes.items():
 f=root/rel
 if not f.exists(): print("MISSING",rel);continue
 lines=f.read_text(errors="replace").splitlines()
 findings=[]
 print("FIELDS",rel,"\n","\n".join(lines[:65]),flush=True)
 for method in names:
  for i,line in enumerate(lines):
   if not re.search(r"\b"+re.escape(method)+r"\s*\(",line) or not re.search(r"\b(public|private|protected)\b",line):continue
   depth=0;started=False;j=i
   for j in range(i,min(len(lines),i+150)):
    depth+=lines[j].count("{")-lines[j].count("}")
    if "{" in lines[j]:started=True
    if started and depth<=0:break
   body="\n".join(lines[i:j+1])
   print("METHOD",rel,method,"line",i+1,"\n",body[:16000],flush=True)
   findings.append({"method":method,"line":i+1,"signature":line.strip(),"literals":sorted(set(re.findall(r'"([^"]{1,120})"',body)))[:80],
       "nums":sorted(set(re.findall(r"\b(?:100|50|10|12|15|20|30|120)\b",body)))})
 result[rel]=findings
out.write_text(json.dumps(result,ensure_ascii=False,indent=2)+"\n")

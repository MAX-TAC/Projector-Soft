#!/usr/bin/env python3
"""One-purpose method and constants audit of light-source brightness and projector UI.
No OEM source code is committed, only class/method/API/call metadata.
"""
from pathlib import Path
import argparse,re,json,collections
APP_METHODS={
 "KuSourceRJ":[
  ("com/coolux/source/activity/BrightActivity.java",["onCreate","init","findUI","initView","enterClick","onClick","onCheckedChanged","setBright","setLight"]),
  ("com/coolux/source/utils/BrightUtils.java",["setBrightModel","startReduceNew","startReduceNew518"]),
  ("com/coolux/source/service/BrightReceiver.java",["onReceive"]),
  ("com/coolux/source/utils/IntentUtils.java",["bright","customBright","setBright"]),
  ("com/coolux/source/utils/ImageUtils.java",["getBrightModel","defaultBrightModel","setBrightModel","getBacklight","setBacklight","setPwmValue","getPerformanceBright","getStandardBright","getQuietBright"]),
  ("com/coolux/source/utils/MyTimeUtils.java",["highBrightService","standerBrightService"]),
  ("com/coolux/source/activity/testcast/CustomBrightActivity.java",["onCreate","init","findUI","initView","enterClick","defaultBright","onClick","onCheckedChanged"]),
 ],
 "TvSettingsPlus":[
  ("com/android/tv/settings/uicustom/floatmenu/controller/picture/AdvanceSettingsSecondaryController.java",["initView","onClick","onCheckedChanged"]),
  ("com/android/tv/settings/uicustom/fragment/picture/PictureFragment.java",["onCreate","onViewCreated"]),
  ("com/android/tv/settings/uicustom/util/ProjectorManager.java",["setProjectorMirrorMode","setBright","setLight"]),
 ]
}
M=re.compile(r'\b(?:public|protected|private|static|final|synchronized|native)\b')
FUN=re.compile(r'\b([\w$]+)\s*\(')
STR=re.compile(r'"((?:[^"\\]|\\.){0,160})"')
def extract_method(content,name):
 lines=content.splitlines();results=[]
 for i,l in enumerate(lines):
  if name not in l or not re.search(r'\b'+re.escape(name)+r'\s*\(',l) or not M.search(l):continue
  if re.search(r'\b(?:if|else|return|throw|new)\b',l[:50]):continue
  depth=0;opened=False
  for j in range(i,min(len(lines),i+260)):
   if '{' in lines[j]:opened=True
   depth+=lines[j].count('{')-lines[j].count('}')
   if opened and depth<=0:break
  part='\n'.join(lines[i:j+1])
  if len(part)>14000:part=part[:14000]
  results.append((i+1,part))
 return results[:6]
def inspect(root):
 report={}
 for apk,entries in APP_METHODS.items():
  for suffix,names in entries:
   sources=root/apk/"sources"
   matches=[p for p in sources.rglob('*.java') if p.as_posix().endswith(suffix)] if sources.is_dir() else []
   if not matches:
    print("MISSING:",apk,suffix);continue
   for path in matches[:1]:
    text=path.read_text(errors="replace")
    key=apk+"/"+suffix
    record={"methods":[],"filtered_strings":sorted(set(x for x in STR.findall(text) if re.search(r'bright|high|low|eco|light|lamp|led|normal|mode|背光|亮度|节能|标准|高亮|省电',x,re.I)))[:180]}
    for method in names:
     for line,body in extract_method(text,method):
      calls=collections.Counter(re.findall(r'(?<![\w$])(?:[\w$.]+\.)?([\w$]+)\s*\(',body))
      literals=list(dict.fromkeys(STR.findall(body)))
      record["methods"].append({"name":method,"line":line,"called":calls.most_common(65),"strings":literals[:70],
                                 "body_lines":body.count("\n")+1})
      print("\n===== "+key+" :: "+method+" L"+str(line)+" =====\n"+body[:12500])
    report[key]=record
 return report
if __name__=="__main__":
 p=argparse.ArgumentParser();p.add_argument("jadx_root",type=Path);p.add_argument("output",type=Path);a=p.parse_args()
 result=inspect(a.jadx_root)
 a.output.parent.mkdir(parents=True,exist_ok=True)
 a.output.write_text(json.dumps(result,ensure_ascii=False,indent=2)+"\n")
 print("\nSaved metadata",a.output)

#!/usr/bin/env python3
"""Inspect AOSP TvSettingsPlus MainSettings navigation and OEM uicustom launcher.
Emit controlled method excerpts in CI only; commit method metadata without code.
"""
import json,re,sys
from pathlib import Path
root=Path(sys.argv[1])/"sources";out=Path(sys.argv[2]);out.parent.mkdir(parents=True,exist_ok=True)
targets={
"com/android/tv/settings/MainSettings.java":["onCreate","onCreatePreferences","onViewCreated","onPreferenceTreeClick","onResume","init","onStart"],
"com/android/tv/settings/uicustom/MainActivity.java":["onCreate","initView","onNewIntent","onResume","onKeyDown","initData","onClick"],
"com/android/tv/settings/uicustom/floatmenu/FloatMenuManager.java":["openSecondaryInterface","jumpToExternalActivity","init","show"],
"com/android/tv/settings/uicustom/fragment/general/GeneralFragment.java":["onCreate","onViewCreated","initView"],
"com/android/tv/settings/uicustom/fragment/picture/PictureFragment.java":["onCreate","onViewCreated","initView"],
"com/android/tv/settings/uicustom/fragment/source/SourceFragment.java":["onCreate","onViewCreated","initView"],
"com/android/tv/settings/uicustom/fragment/sound/SoundFragment.java":["onCreate","onViewCreated","initView"],
"com/android/tv/settings/uicustom/fragment/projector/ProjectorFragment.java":["onCreate","onViewCreated","initView"],
"com/android/tv/settings/uicustom/floatmenu/controller/general/GeneralSubMenuController.java":["initView","onClick"],
"com/android/tv/settings/uicustom/floatmenu/FloatMenuService.java":["onCreate","onStartCommand"]
}
def extract(source,name):
 lines=source.splitlines();found=[]
 for i,ln in enumerate(lines):
  if not re.search(r"\b"+re.escape(name)+r"\s*\(",ln):continue
  if not re.search(r"\b(public|protected|private)\b",ln) or "class " in ln:continue
  count=0;started=False;j=i
  for j in range(i,min(len(lines),i+140)):
   count+=lines[j].count("{")-lines[j].count("}")
   if "{" in lines[j]:started=True
   if started and count<=0:break
  body="\n".join(lines[i:j+1]);found.append((i+1,body))
 return found[:4]
results={}
for rel,methods in targets.items():
 p=root/rel
 if not p.is_file():
  # JADX obfuscation may use deobf path
  poss=list(root.rglob(rel.split("/")[-1]))
  p=poss[0] if poss else p
 if not p.exists():
  print("NOT FOUND",rel);continue
 s=p.read_text(errors="replace")
 app=results[rel]={"path":str(p.relative_to(root)),"methods":{},
   "resource_names":sorted(set(re.findall(r"R\.(?:xml|layout|string|id)\.([\w]+)",s)))[:160],
   "intent_literals":sorted(set(re.findall(r'"((?:android\.settings\.|com\.android\.tv\.|com\.coolux\.|com\.action\.)[^"]+)"',s)))[:80],
   "fragment_names":sorted(set(re.findall(r"\b(?:new |class |getClass\(\)|Class<)\s*([\w$]+(?:Fragment|Activity|Controller))",s)))[:50]}
 for method in methods:
  entries=extract(s,method)
  app["methods"][method]=[{"line":line,"body_lines":block.count("\n")+1,"calls":sorted(set(re.findall(r"\b([\w$]+)\s*\(",block)))[:65]} for line,block in entries]
  for line,block in entries:
   print("\n###",rel,method,"line",line,"\n",block[:10000],flush=True)
 print("FILE",rel,"resources",app["resource_names"],"literals",app["intent_literals"],flush=True)
out.write_text(json.dumps(results,indent=2,ensure_ascii=False)+"\n")

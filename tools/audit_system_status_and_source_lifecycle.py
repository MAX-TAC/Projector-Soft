#!/usr/bin/env python3
"""Inspect OEM system telemetry and KuSourceRJ boot/watchdog lifecycle.
Commit descriptive metadata, not decompiled source.
"""
from pathlib import Path
import json,re,sys
root=Path(sys.argv[1]);out=Path(sys.argv[2]);out.parent.mkdir(parents=True,exist_ok=True)
names={
"TvSettingsPlus":[
 "com/android/tv/settings/uicustom/floatmenu/controller/system/SystemInfoSecondaryController.java",
 "com/android/tv/settings/uicustom/floatmenu/controller/system/SystemSubMenuController.java",
 "com/android/tv/settings/partnercustomizer/tvsettingservice/TvBootupReceiver.java"],
"KuSourceRJ":[
 "com/coolux/source/service/TimeService.java",
 "com/coolux/source/receiver/BootupReceiver.java",
 "com/coolux/source/utils/IntentUtils.java"],
"KuSettings":[
 "com/my/service/MySetTimeService.java",
 "com/my/service/BootupReceiver.java",
 "com/my/utils/IntentUtils.java"]
}
info={}
for app,rels in names.items():
 for rel in rels:
  p=root/app/"sources"/rel
  if not p.exists():
   matches=list((root/app/"sources").rglob(rel.split("/")[-1]))
   if matches: p=matches[0]
  if not p.exists():
   print("MISSING",app,rel)
   continue
  s=p.read_text(errors="replace")
  print("FACTORY",app,str(p.relative_to(root/app/"sources")),"total",len(s.splitlines()),"\n",s[:28000],flush=True)
  mnames=re.findall(r"(?m)^\\s*(?:public|private|protected)\\s+(?:static\\s+)?[\\w.$<>\\[\\] ]+\\s+([A-Za-z_]\\w*)\\(",s)
  info[app+"/"+rel]={"lines":len(s.splitlines()),"methods":mnames[:110],
    "property_strings":sorted(set(re.findall(r'"((?:persist\\.|sys\\.|ro\\.|vendor\\.)[^"]{1,120})"',s)))[:80],
    "service_refs":sorted(set(re.findall(r'"([\\w.]+(?:Service|Receiver))"',s)))[:90]}
out.write_text(json.dumps(info,indent=2,ensure_ascii=False)+"\\n")

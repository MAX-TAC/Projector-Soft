#!/usr/bin/env python3
"""Read exact MediaTek CEC API initialization and settings, no source committed."""
from pathlib import Path
import json,re,sys
base=Path(sys.argv[1])/"sources";dest=Path(sys.argv[2]);dest.parent.mkdir(parents=True,exist_ok=True)
names=[
 "com/android/tv/settings/uicustom/floatmenu/controller/source/SourceSubMenuController.java",
 "com/android/tv/settings/uicustom/fragment/source/SourceFragment.java",
 "com/android/tv/settings/uicustom/common/Constant.java"]
report={}
for n in names:
 f=base/n
 if not f.exists():continue
 lines=f.read_text(errors="replace").splitlines()
 samples=[];out=[]
 for i,x in enumerate(lines):
  if i<135 or any(k in x for k in ("mCompatibleApis =", "showCecEnergyDialog(", "SETTINGS_KEY_HDMI_CEC", "isSupportPowerConfirmed")):
   if (i<135 and n.endswith("Constant.java") and not re.search("CEC|HDMI",x)):continue
   samples.append({"line":i+1,"snippet":x.strip()})
   if len(out)<230:out.append(str(i+1)+": "+x)
 print("CLASS",n,"\n"+"\n".join(out[:235]),flush=True)
 report[n]=samples[:300]
dest.write_text(json.dumps(report,ensure_ascii=False,indent=2)+"\n")

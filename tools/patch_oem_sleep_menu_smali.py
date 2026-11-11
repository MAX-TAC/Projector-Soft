#!/usr/bin/env python3
"""Hide ONLY factory inactivity submenu from OEM GeneralFragment and
GeneralSubMenuController; never delete OEM calibration, sleep, fan or source
classes. Fail closed when method or class layout differs from 8052 baseline.
"""
import argparse,json,re
from pathlib import Path
from patch_customer0_lightmode_smali import mode_dex

HOOK="Ldev/projectorsoft/tvsettings/OemMenuVisibilityHook;->hideSleepControls(Ljava/lang/Object;)V"

def locate(root, suffix):
    matches=[p for p in root.glob("smali*/**/*.smali")
             if p.is_file() and p.as_posix().endswith(suffix)]
    if len(matches)!=1:
        raise ValueError("Expected one stock OEM class "+suffix+", found "+str(matches))
    return matches[0]

def patch_one(path, source_register):
    original=path.read_text()
    blocks=list(re.finditer(r"(?ms)^\.method[^\n]*\n.*?^\.end method",original))
    target=[m for m in blocks if re.search(r"\binitView\(",m.group().splitlines()[0])]
    if len(target)!=1:
        raise ValueError("Expected one initView in "+str(path))
    match=target[0]
    body=match.group()
    if HOOK in body:raise ValueError("Hook already present")
    returns=list(re.finditer(r"(?m)^    return-void\s*$",body))
    if len(returns)!=1:raise ValueError("Cannot safely hook initView: "+str(path))
    insertion="    invoke-static {"+source_register+"}, "+HOOK+"\n"
    body=body[:returns[0].start()]+insertion+body[returns[0].start():]
    path.write_text(original[:match.start()]+body+original[match.end():])
    return {"class":str(path),"register":source_register}

def main():
    parser=argparse.ArgumentParser()
    parser.add_argument("decoded",type=Path)
    parser.add_argument("report",type=Path)
    args=parser.parse_args()
    targets=[
       ("com/android/tv/settings/uicustom/fragment/general/GeneralFragment.smali","p1"),
       ("com/android/tv/settings/uicustom/floatmenu/controller/general/GeneralSubMenuController.smali","p0")
    ]
    patched=[]
    for suffix,reg in targets:
        p=locate(args.decoded,suffix)
        patch_one(p,reg)
        patched.append(p)
    result={
      "edited_smali":[str(p.relative_to(args.decoded)) for p in patched],
      "edited_dex":sorted({mode_dex(p) for p in patched}),
      "hides":["tciv_general_screensaver_settings"],
      "protection":"original OEM sleep controllers and fan HAL unchanged"
    }
    args.report.parent.mkdir(parents=True,exist_ok=True)
    args.report.write_text(json.dumps(result,indent=2)+"\n")
    print(json.dumps(result,indent=2))
if __name__=="__main__":
    main()

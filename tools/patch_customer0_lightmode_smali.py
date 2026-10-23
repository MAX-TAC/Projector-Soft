#!/usr/bin/env python3
"""Patch only two TvSettingsPlus OEM menu smali classes for customer-0 light mode.
Requires apktool d -r output. Does NOT modify focus, keystone, setup, source services.
"""
from pathlib import Path
import argparse,re,json
def locate(root,name):
    matches=[p for p in root.glob("smali*/**/"+name) if p.is_file()]
    if len(matches)!=1:raise ValueError("Expected exactly one "+name+" smali, got "+str(matches))
    return matches[0]
def mode_dex(path):
    folder=next(p.name for p in path.parents if p.name=="smali" or p.name.startswith("smali_classes"))
    return "classes.dex" if folder=="smali" else "classes"+folder.rsplit("classes",1)[-1]+".dex"
def patch(root):
    target=locate(root,"BrightnessModeSettingsSecondaryController.smali")
    submenus=[p for p in root.glob("smali*/**/*.smali") if b'android.settings.BRIGHT_SETTINGS' in p.read_bytes()]
    candidates=[p for p in submenus if "GeneralSubMenuController$" in p.name]
    if len(candidates)!=1:raise ValueError("Wrong number of GeneralSubMenuController brightness listeners "+str(candidates))
    listener=candidates[0]
    src=listener.read_text()
    rx=re.compile(r"(?m)^(?P<indent>\s*)sget (?P<reg>[vp]\d+), Landroid/os/Build;->A_RJ_CUSTOMER_ID:I$")
    m=list(rx.finditer(src))
    if len(m)!=1:raise ValueError("Missing unique A_RJ_CUSTOMER_ID switch in brightness onClick")
    indent=m[0].group("indent");reg=m[0].group("reg")
    # The method is onClick(View), so p1 is the clicked view.
    replacement=(indent+"invoke-virtual {p1}, Landroid/view/View;->getContext()Landroid/content/Context;\n"
             +indent+"move-result-object "+reg+"\n"
             +indent+"invoke-static {"+reg+"}, Ldev/projectorsoft/tvsettings/LegacyBrightnessUiHook;->customerIdForMenu(Landroid/content/Context;)I\n"
             +indent+"move-result "+reg)
    new=rx.sub(lambda _:replacement,src,count=1)
    assert new!=src
    listener.write_text(new)

    old=target.read_text()
    parts=re.search(r"(?ms)^\.method [^\n]* initView\(\)V\n.*?^\.end method",old)
    if not parts:raise ValueError("Brightness controller initView method missing")
    block=parts.group()
    hits=list(re.finditer(r"(?m)^\s*invoke-\w+ \{p0\}, Lcom/android/tv/settings/uicustom/floatmenu/controller/general/BrightnessModeSettingsSecondaryController;->initListener\(\)V$",block))
    if len(hits)!=1:raise ValueError("Expected one initListener call; got "+str(len(hits)))
    insert='\n    invoke-static {p0}, Ldev/projectorsoft/tvsettings/LegacyBrightnessUiHook;->install(Ljava/lang/Object;)V'
    block=block[:hits[0].end()]+insert+block[hits[0].end():]
    target.write_text(old[:parts.start()]+block+old[parts.end():])
    # Hide only the obsolete Bluetooth-speaker SOURCE entry. Never disable BT services.
    visibility_hook="Ldev/projectorsoft/tvsettings/OemMenuVisibilityHook;->hideBluetoothSpeaker(Ljava/lang/Object;)V"
    extra=[]
    for cls,methods in [
        ("SourceSubMenuController.smali",("initView","onResume")),
        ("SourceFragment.smali",("initView","onReLoad"))
    ]:
        refs=[p for p in root.glob("smali*/**/"+cls)
              if p.is_file() and "/source/" in p.as_posix()]
        if len(refs)!=1:raise ValueError("Expected one "+cls+": "+str(refs))
        location=refs[0]; original=location.read_text()
        rebuilt=original; changed_count=0
        for method in methods:
            pattern=re.compile(r"(?ms)^\.method [^\n]* "+method+r"\([^\n]*\)[^\n]*\n.*?^\.end method")
            mm=list(pattern.finditer(rebuilt))
            if not mm:continue
            if len(mm)!=1:raise ValueError("Ambiguous "+method+" in "+cls)
            block=mm[0].group()
            hook="    invoke-static {p0}, "+visibility_hook+"\n"
            count=block.count("return-void")
            if count<1:raise ValueError("No return-void in "+cls+" "+method)
            block=block.replace("    return-void",hook+"    return-void")
            if hook not in block:raise ValueError("Hook not inserted into "+cls+" "+method)
            rebuilt=rebuilt[:mm[0].start()]+block+rebuilt[mm[0].end():]
            changed_count+=1
        if changed_count<1:raise ValueError("Cannot find init methods in "+cls)
        location.write_text(rebuilt)
        extra.append(location)
    changed=[target,listener]+extra
    return {"edited_smali":[str(p.relative_to(root)) for p in changed],
            "edited_dex":sorted({mode_dex(p) for p in changed}),
            "customer_0":"only redirects if receiver and actual OEM property usable; otherwise original BrightActivity path",
            "nonzero_customer":"retains original MtkUtil path",
            "calibration":"no focus or keystone smali edited"}
def main():
    ap=argparse.ArgumentParser()
    ap.add_argument("apktool_dir",type=Path);ap.add_argument("report",type=Path)
    args=ap.parse_args()
    info=patch(args.apktool_dir)
    args.report.parent.mkdir(parents=True,exist_ok=True)
    args.report.write_text(json.dumps(info,indent=2)+"\n")
    print(json.dumps(info,indent=2))
if __name__=="__main__":main()

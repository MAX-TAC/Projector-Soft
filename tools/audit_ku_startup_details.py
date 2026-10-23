#!/usr/bin/env python3
"""Constrained static audit for KuSettings startup service redirects and BT intents.
Print method excerpts in ephemeral CI log; commit only symbols/actions report.
"""
from pathlib import Path
import argparse, json, re, subprocess
METHODS=["startMyTimeService","startSourceTimeService","isServiceStart","isSourceServiceStart",
         "startTime","startSourceService"]
def block(src,name):
    lines=src.splitlines(); found=[]
    for i,ln in enumerate(lines):
        if not re.search(r"\b"+re.escape(name)+r"\s*\(",ln):continue
        if not re.search(r"\b(?:public|private|protected|static|final)\b",ln):continue
        depth=0; begun=False
        for j in range(i,min(i+110,len(lines))):
            depth+=lines[j].count("{")-lines[j].count("}")
            if "{" in lines[j]:begun=True
            if begun and depth<=0:break
        found.append((i+1,"\n".join(lines[i:j+1])))
    return found[:4]
def actions(apk):
    try: 
        o=subprocess.check_output(["aapt","dump","xmltree",str(apk),"AndroidManifest.xml"],text=True,errors="replace",timeout=30)
    except Exception as e:return {"error":str(e)}
    out=[]; comp=None; cur_action=None
    for l in o.splitlines():
        m=re.search(r"^\s*E:\s*(activity(?:-alias)?|receiver|service|intent-filter|action)\s",l)
        if m:
            tag=m.group(1)
            if tag in ("activity","activity-alias","receiver","service"):
                if comp:out.append(comp)
                comp={"kind":tag,"name":"","actions":[]}
            elif tag=="action":cur_action=comp
            continue
        if comp and "A: android:name(" in l:
            val=re.search(r'Raw: "([^"]+)"',l)
            if not val:continue
            v=val.group(1)
            if v.startswith("android.settings.") or v in ("android.intent.action.BOOT_COMPLETED","android.intent.action.TIME_TICK"):
                comp["actions"].append(v)
            elif not comp["name"] and ("." in v or v.startswith("com.")):
                comp["name"]=v
    if comp:out.append(comp)
    return [a for a in out if any(x in (a.get("name","")+" "+",".join(a["actions"])) for x in ("Bluetooth","settings","AUTO_BLUETOOTH","BOOT_COMPLETED","TIME_TICK")) or a["actions"]]
def main():
    p=argparse.ArgumentParser()
    p.add_argument("jdax",type=Path);p.add_argument("apks",type=Path);p.add_argument("out",type=Path)
    a=p.parse_args()
    root=a.jdax/"KuSettings"/"sources"
    f=next((x for x in root.rglob("IntentUtils.java") if x.as_posix().endswith("/utils/IntentUtils.java")),None)
    assert f,"KuSettings IntentUtils missing"
    source=f.read_text(errors="replace")
    report={"ku_start_service_methods":{},"manifest_filters":{}}
    for name in METHODS:
        vals=block(source,name)
        report["ku_start_service_methods"][name]=[]
        for n,s in vals:
            report["ku_start_service_methods"][name].append({
                "line":n,"literals":re.findall(r'"([^"\n]{0,150})"',s),
                "calls":list(dict.fromkeys(re.findall(r'[\w$.]+\.\s*([\w$]+)\(',s)))[:30]})
            print("\n### KuSettings IntentUtils",name,"source line",n,"\n",s[:9000])
    for name in ("KuSettings","KuSetupWizard","TvSettingsPlus","WFDSinkTest"):
        report["manifest_filters"][name]=actions(a.apks/(name+".apk"))
        print("\n### Manifest",name)
        print(json.dumps(report["manifest_filters"][name],ensure_ascii=False)[:9000])
    a.out.parent.mkdir(parents=True,exist_ok=True)
    a.out.write_text(json.dumps(report,ensure_ascii=False,indent=2)+"\n")
if __name__=="__main__":main()

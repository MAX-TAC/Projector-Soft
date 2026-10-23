#!/usr/bin/env python3
"""Targeted method and class-constant inspection for KuSettings retirement."""
import argparse,re,json
from pathlib import Path
def method(source,name,limit=400):
    lines=source.splitlines()
    rows=[]
    for i,ln in enumerate(lines):
        if (re.search(r"\b"+re.escape(name)+r"\s*\(",ln) and
            re.search(r"\b(public|protected|private|static)\b",ln) and
            "class " not in ln and not re.search(r"=\s*[^=]+[.]"+re.escape(name)+r"\(",ln)):
            # in JADX method headers may be one line; braces next line
            depth=0; started=False; end=i
            for end in range(i,min(i+limit,len(lines))):
                depth+=lines[end].count("{")-lines[end].count("}")
                if "{" in lines[end]:started=True
                if started and depth<=0:break
            rows.append((i+1,end+1,"\n".join(lines[i:end+1])))
    return rows[:8]
def inspect(root,out):
    results={}
    configs=[
       ("KuSettings","/utils/IntentUtils.java",["startTime","startSourceService","startTimeService","startScreen","startKuSetting"]),
       ("KuSetupWizard","/utils/IntentUtils.java",["startTime","startSourceService"]),
       ("KuSourceRJ","/utils/IntentUtils.java",["startTime","startSourceService"]),
       ("KuSourceRJ","/service/BootupReceiver.java",["defaultInit","startTime"]),
    ]
    for app,suf,meth in configs:
        code=root/app/"sources"
        src=next((p for p in code.rglob("*.java") if p.as_posix().endswith(suf)),None)
        if not src:continue
        t=src.read_text(errors="replace"); rows=[]
        for m in meth:
            for start,end,block in method(t,m):
                strs=re.findall(r'"([^"\n]{0,180})"',block)
                invokes=re.findall(r'(?:[\w$]+\.)+([\w$]+)\s*\(',block)
                r={"method":m,"line":start,"total_lines":end-start+1,"literals":strs[:35],"invocations":sorted(set(invokes))[:100]}
                rows.append(r)
                print("\n===== "+app+" "+suf+" "+m+" "+str(start)+".."+str(end))
                print(block[:6500])
        results[app+":"+suf]=rows
    f=next((p for p in (root/"WFDSinkTest"/"sources").rglob("PhoneWindowManager.java") if "/policy/" in str(p)),None)
    if f:
        ls=f.read_text(errors="replace").splitlines()
        hits=[]
        for i,ln in enumerate(ls):
            if "com.my.settings" in ln or "MyAutoBluetoothActivity" in ln or ("android.settings.SETTINGS" in ln and 2200<i<2700):
                context="\n".join(f"{q+1:5} {ls[q]}" for q in range(max(0,i-20),min(len(ls),i+22)))
                hits.append({"line":i+1,"content":ln.strip()[:400]})
                print("\n===== WFDSinkTest PhoneWindowManager line",i+1)
                print(context[:9000])
        results["WFD:inbound_KuSettings"]=hits
    out.parent.mkdir(parents=True,exist_ok=True)
    out.write_text(json.dumps(results,ensure_ascii=False,indent=2)+"\n")
    return results
if __name__=="__main__":
    p=argparse.ArgumentParser()
    p.add_argument("source",type=Path)
    p.add_argument("report",type=Path)
    a=p.parse_args()
    inspect(a.source,a.report)

#!/usr/bin/env python3
"""Extract scoped static behavior metadata and small diagnostic method excerpts.
Do not commit JADX generated sources or full method bodies to Git.
"""
import argparse, pathlib, re, json, collections
WATCH={
 "KuSettings": [
 ("service/BootupReceiver.java",["onReceive"]),
 ("service/MySetTimeService.java",["onStartCommand","onReceive","registerTimeTick","registerReceiver"]),
 ("service/ScreenServices.java",["onStartCommand","onBind","startShot","takeScreenShot"]),
 ("utils/IntentUtils.java",["enter","start","setting"]),
 ],
 "KuSetupWizard":[("service/BootupReceiver.java",["onReceive"]),("service/MySetTimeService.java",["onStartCommand","onReceive"])],
 "WFDSinkTest":[("server/policy/PhoneWindowManager.java",["interceptKeyBeforeDispatching"])],
 "TvSettingsPlus":[("tvsettingservice/TvBootupReceiver.java",["onReceive"])],
 "KuSourceRJ":[("service/BootupReceiver.java",["onReceive"]),("service/TimeService.java",["onStartCommand"])],
}
CALL_RE=re.compile(r'(?<![\w.])([\w$.]+)\s*\(', re.ASCII)
STRING_RE=re.compile(r'"((?:[^"\\]|\\.){0,150})"')
def grabmethod(txt,name,markers=()):
    lines=txt.splitlines(); results=[]
    for i,ln in enumerate(lines):
        if re.search(r'\b'+re.escape(name)+r'\s*\(',ln) and "class " not in ln and len(ln)<350:
            # only method declarations approximate: opening parens then braces with balance.
            pre=ln.strip()
            if not any(x in pre for x in ("public ","private ","protected ","void ","static ","synchronized ","final ")):continue
            start=i; end=min(len(lines),start+170)
            balance=0; begun=False
            for j in range(start,end):
                for c in lines[j]:
                    if c=='{':balance+=1;begun=True
                    elif c=='}':balance-=1
                if begun and balance<=0:
                    end=j+1;break
            block="\n".join(lines[start:end])
            if markers and not any(k in block for k in markers):continue
            invokes=collections.Counter(CALL_RE.findall(block))
            strs=[s for s in STRING_RE.findall(block) if len(s)<150]
            results.append({"line":i+1,"lines":end-start,"calls":invokes.most_common(35),
               "literals":list(dict.fromkeys(strs))[:40],
               "excerpt":block[:6500]})
    return results[:6]
def main():
    p=argparse.ArgumentParser()
    p.add_argument("root",type=pathlib.Path)
    p.add_argument("out",type=pathlib.Path)
    a=p.parse_args(); a.out.mkdir(parents=True,exist_ok=True)
    result={}
    for app,targets in WATCH.items():
        root=a.root/app/"sources"; items=[]
        for part,methods in targets:
            matches=[f for f in root.rglob("*.java") if f.as_posix().endswith(part)] if root.exists() else []
            for f in matches[:4]:
                source=f.read_text(errors="replace")
                funcs=[]
                for method in methods:
                    funcs += [{"method":method,**z} for z in grabmethod(source,method)]
                if funcs: items.append({"path":f.relative_to(root).as_posix(),"methods":funcs})
        result[app]=items
        print(f"\n## {app}: {len(items)} class files")
        for item in items:
            print("\n",item["path"])
            for x in item["methods"]:
                print("METHOD",x["method"],"line",x["line"],"total",x["lines"])
                print("CALLED",x["calls"][:22])
                print("LITERALS",x["literals"][:22])
                if app in ("KuSettings","KuSetupWizard","WFDSinkTest","TvSettingsPlus") and x["method"] in ("onReceive","onStartCommand","interceptKeyBeforeDispatching"):
                    print("EXCERPT\n",x["excerpt"][:5200])
    # NEVER persist method excerpts
    for app,items in result.items():
        for item in items:
            for met in item["methods"]:
                met.pop("excerpt",None)
    (a.out/"behavior_inventory.json").write_text(json.dumps(result,ensure_ascii=False,indent=2)+"\n")
    print("Saved behavior_inventory.json")
if __name__=="__main__":main()

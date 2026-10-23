#!/usr/bin/env python3
"""Static migration audit of OEM Settings dependencies; never executes OEM code."""
import argparse,collections,json,re,subprocess
from pathlib import Path

TARGETS=("TvSettingsPlus","KuSettings","KuSourceRJ","RuiAutoFocus","KeystoneCorrection","trdservice","trdmiddle","RuiYunOta")
TOKENS={
"KuSettings activity/services":re.compile(r"\b(?:SettingActivity|ScreenServices|MySetTimeService|BootupReceiver)\b",re.I),
"KuSettings package":re.compile(r"com[./]my[./](?:settings|service|utils)|com\.my\.settings",re.I),
"KuSourceRJ package":re.compile(r"com[./]coolux[./]source|com\.coolux\.source",re.I),
"RuiAutoFocus package":re.compile(r"com[./]hysd[./](?:screen|xymscreen|vafocus)|com\.hysd\.screen",re.I),
"Keystone package":re.compile(r"com[./]rjpj[./]tcorrection|com\.rjpj\.tcorrection",re.I),
"TRD SDK call sites":re.compile(r"\b(?:TrdSdk|TrdSystemManager|ProjectorCtrl|SystemCtrlImpl|MotorCtrl)\b",re.I),
"TRD Binder and SDK":re.compile(r"com[./]trd[./](?:framework|sdk)|ITrdSystemService|IProjectorCtrl|ITrdService",re.I),
"MTK display / picture":re.compile(r"MtkTvFApi(?:Display|Audio|InputSource|Information|Peripheral)",re.I),
"OEM focus and geometry":re.compile(r"(?i)(?:autofocus|keystone|fourcorner|motorctrl|tof|zoom)"),
"Picture features":re.compile(r"(?i)\b(?:memc|hdr10|hdr|game.?mode|equalizer|eqmode)\b"),
"System settings writes":re.compile(r"Settings\.(?:System|Global|Secure)\.(?:put|get)|ContentResolver",re.I),
"Service / receiver invokes":re.compile(r"\b(?:startService|startForegroundService|bindService|sendBroadcast|sendOrderedBroadcast)\s*\(",re.I),
}
PAT_CLASS=re.compile(r"(?m)\b(?:class|interface)\s+([A-Za-z_$][A-Za-z_$\d]*)")
PAT_METHOD=re.compile(r"(?m)^\s*(?:public|protected|private|static|final|synchronized|native|abstract|\s)+\s*[\w<>,.?@\[\]\s]+\s+([A-Za-z_$][A-Za-z_$\d]*)\s*\(")
def shell(*args):
    try:
        c=subprocess.run(args,stdout=subprocess.PIPE,stderr=subprocess.PIPE,text=True,errors="replace",timeout=60)
        return c.stdout if c.returncode==0 else ""
    except Exception:return ""
def manifest(apk):
    data=shell("aapt","dump","xmltree",str(apk),"AndroidManifest.xml")
    out=[]
    curr=None
    component_attrs=False
    kinds=("activity-alias","activity","receiver","service","provider")
    for line in data.splitlines():
        el=re.search(r"\bE:\s*([a-zA-Z0-9_-]+)",line)
        if el:
            kind=el.group(1)
            if kind in kinds:
                if curr:out.append(curr)
                curr={"type":kind,"name":"","exported":"","permission":"","enabled":""}
                component_attrs=True
            else:
                component_attrs=False
            continue
        if curr and component_attrs:
            m=re.search(r"A:\s+android:(name|exported|permission|enabled)\b[^=]*=\s*(.*)",line)
            if not m:continue
            val=m.group(2)
            hit=re.search(r'Raw:\s*"([^"]*)"',val)
            if not hit:hit=re.search(r'"([^"]+)"',val)
            v=hit.group(1) if hit else val.strip()[:90]
            if m.group(1) in ("enabled","exported") and not hit:
                if "0xffffffff" in v:v="true"
                elif "0x0" in v:v="false"
            curr[m.group(1)]=v
    if curr:out.append(curr)
    # Extract package/shared user, if available from aapt:
    badging=shell("aapt","dump","badging",str(apk))
    pkg=next((ln for ln in badging.splitlines() if ln.startswith("package:")),"")
    return {"package":pkg[:350],"components":out}
def main():
    p=argparse.ArgumentParser()
    p.add_argument("--jadx-dir",required=True,type=Path)
    p.add_argument("--apk-dir",required=True,type=Path)
    p.add_argument("--out",required=True,type=Path)
    o=p.parse_args()
    o.out.mkdir(exist_ok=True,parents=True)
    apps={}
    for target in TARGETS:
        base=o.jadx_dir/target/"sources"
        component=manifest(o.apk_dir/(target+".apk")) if (o.apk_dir/(target+".apk")).exists() else {}
        matches=collections.defaultdict(list)
        key_files=[]
        files=list(base.rglob("*.java")) if base.exists() else []
        for f in files:
            rel=f.relative_to(base).as_posix()
            if "/R.java" in "/"+rel or rel.endswith("/BuildConfig.java"):continue
            try:t=f.read_text(errors="replace")
            except OSError:continue
            own=("com/my/" in rel or "com/p001my/" in rel or
                 "com/android/tv/settings/uicustom/" in rel or
                 "com/coolux/source/" in rel or
                 "com/trd/" in rel or
                 "com/hysd/" in rel or "com/rjpj/" in rel)
            if own:
                nm=PAT_CLASS.search(t)
                methods=sorted(set(PAT_METHOD.findall(t)))
                if any(k in rel.lower() for k in ("setting","screen","focus","source","service","receiver","boot","audio","key","ota","activity")):
                    key_files.append({"path":rel,"class":nm.group(1) if nm else "", "methods":methods[:45]})
            # Don't count generic AndroidX / bundled libraries to reduce noise
            if not own and target != "TvSettingsPlus":continue
            if not own and "/com/android/tv/settings/" not in "/"+rel:continue
            for label,pat in TOKENS.items():
                hits=list(pat.finditer(t))
                if hits:
                    examples=[]
                    for mt in hits[:5]:
                        lineno=t.count("\n",0,mt.start())+1
                        examples.append(lineno)
                    matches[label].append({"file":rel,"hits":len(hits),"lines":examples})
        apps[target]={"java_files":len(files),"manifest":component,
                      "relevant_classes":key_files[:550],
                      "refs":dict(matches)}
    all_refs=[]
    for label,items in apps.get("TvSettingsPlus",{}).get("refs",{}).items():
        if label in ("KuSettings package","KuSettings activity/services","KuSourceRJ package","TRD Binder and SDK","TRD SDK call sites"):
            for it in items:all_refs.append({"caller":"TvSettingsPlus","category":label,**it})
    for label,items in apps.get("KuSourceRJ",{}).get("refs",{}).items():
        if label=="KuSettings package":
            for it in items:all_refs.append({"caller":"KuSourceRJ","category":label,**it})
    out={"source":"Original OTA 8052 APK; static JADX; not tested on hardware",
         "apps":apps,"cross_package_references":all_refs}
    (o.out/"audit.json").write_text(json.dumps(out,ensure_ascii=False,indent=2)+"\n")
    rows=["# KuSettings migration audit (8052)","",
          "Based on JADX + manifest inspection of original APKs, not dynamic execution.",
          "This is a **dependency inventory**, not proof that KuSettings can be disabled.",
          "",
          "## Exported / background components"]
    for k in TARGETS:
        a=apps[k]
        rows.extend(["",f"### {k}",f"Decompiled Java files: {a['java_files']}",""])
        rows+=["| Type | Name | Exported | Permission |","|---|---|---|---|"]
        for c in a["manifest"].get("components",[]):
            if k in ("KuSettings","KuSourceRJ","TvSettingsPlus") or c["type"] in ("receiver","service"):
                rows.append("| "+ " | ".join(str(c.get(field,"")).replace("|","/")[:130] for field in ("type","name","exported","permission"))+" |")
    rows+=["","## Cross-package references (possible links, not confirmed runtime calls)",
           "| Caller | Reference | File | Occurrences |","|---|---|---|---:|"]
    for r in all_refs:
        rows.append(f"| {r['caller']} | {r['category']} | \`{r['file']}\` | {r['hits']} |")
    rows+=["","## OEM API candidate files",""]
    for k in ("KuSettings","KuSourceRJ","TvSettingsPlus"):
        rows.append(f"### {k}")
        for v in apps[k]["relevant_classes"][:95]:
            rows.append("- \`"+v["path"]+"\`: "+", ".join(v["methods"][:13]))
    rows+=["","## Safety gates","",
        "1. Identify exact broadcasts, pending intents, boot receiver/service lifecycle, and references from other APKs before disabling KuSettings.",
        "2. Preserve KuSourceRJ, TRD services, focus/geometry, HDMI input routing, and SetupWizard.",
        "3. Confirm TvSettingsPlus original platform certificate; do not infer it from OTA testkey.",
        "4. Never write raw I2C or invoke motor APIs until vendor semantics and stops are validated.",
        "5. Modify existing TvSettingsPlus package; compare resource maps and compile before APK signing.",
        "6. First firmware test should validate navigation/UI and service references; require rollback procedure."]
    (o.out/"README.md").write_text("\n".join(rows)+"\n")
    print("Audited",len(apps),"packages; cross-package reference files",len(all_refs))
    for k,a in apps.items():print(k,len(a["manifest"].get("components",[])),a["java_files"])
if __name__=="__main__":main()

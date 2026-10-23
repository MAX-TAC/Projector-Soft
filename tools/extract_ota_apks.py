#!/usr/bin/env python3
"""Extract exact OEM APKs from the read-only unpacked 8052 OTA EROFS filesystems."""
from pathlib import Path
import argparse, hashlib, json, shutil, subprocess, zipfile, re, sys

NAMES = [
 "TvSettingsPlus", "KuSettings", "KuSourceRJ", "RuiAutoFocus",
 "KeystoneCorrection", "trdservice", "trdmiddle", "TvServices",
 "RuiYunOta", "KuSetupWizard", "Provision", "LiveTV",
 "Factory", "TvCenter", "TvInput", "FTAirplayWireless", "WFDSinkTest",
]
def digest(p):
    h=hashlib.sha256()
    with p.open("rb") as f:
        for block in iter(lambda:f.read(1024*1024),b""):h.update(block)
    return h.hexdigest()
def capture(args):
    try:
        p=subprocess.run(args,capture_output=True,text=True,errors="replace",timeout=90)
        return p.returncode, (p.stdout+"\n"+p.stderr).strip()
    except (OSError, subprocess.TimeoutExpired) as e:return -1,str(e)
def meta(p):
    d={"bytes":p.stat().st_size,"sha256":digest(p)}
    try:
        with zipfile.ZipFile(p) as z:
            d["is_zip"]=True
            d["dex"]=[x for x in z.namelist() if re.fullmatch(r"classes\d*\.dex",x)]
    except zipfile.BadZipFile:
        d["is_zip"]=False
    rc,txt=capture(["aapt","dump","badging",str(p)])
    for line in txt.splitlines():
        if line.startswith("package:"):d["package_line"]=line[:450];break
    rc,txt=capture(["apksigner","verify","--print-certs",str(p)])
    d["apksigner_return_code"]=rc
    for line in txt.splitlines():
        if "certificate SHA-256 digest:" in line:
            d["signer_sha256"]=line.split("digest:",1)[1].strip().replace(":","").lower()
            break
    return d
def locate(root,stem):
    paths=[p for p in root.rglob("*.apk") if p.stem.casefold()==stem.casefold()]
    if not paths:return None
    priority={"TvSettingsPlus":"system_ext","trdmiddle":"vendor"}
    prefix=priority.get(stem,"system")
    paths.sort(key=lambda p:(0 if prefix in p.parts or any(s.startswith(prefix+"_") for s in p.parts) else 1,len(p.parts),str(p)))
    return paths[0]
def main():
    ap=argparse.ArgumentParser()
    ap.add_argument("erofs_root",type=Path)
    ap.add_argument("output_root",type=Path)
    ap.add_argument("--code",default="8052")
    opts=ap.parse_args()
    root=opts.erofs_root.resolve()
    assert root.is_dir(), "Missing unpacked EROFS root: "+str(root)
    base=opts.output_root.resolve()
    dest=base/"vendor"/"reference"/opts.code/"APK"
    reports=base/"reports"/opts.code
    dest.mkdir(parents=True,exist_ok=True)
    reports.mkdir(parents=True,exist_ok=True)
    inventory={"machine_id":opts.code,"source":"Everycom-Cinelux-Firmware / OTA_Updates / RJMT9660_firmware_8052.zip","files":{},"missing":[]}
    for name in NAMES:
        src=locate(root,name)
        if src is None:inventory["missing"].append(name);continue
        size=src.stat().st_size
        inf=meta(src)
        inf["source_relative_path"]=src.relative_to(root).as_posix()
        if size<=95*1024*1024:
            dst=dest/src.name
            if dst.is_file() and digest(dst)!=inf["sha256"]:
                raise RuntimeError("Conflicting APK filename "+src.name)
            shutil.copy2(src,dst)
            inf["repo_relative_path"]=dst.relative_to(base).as_posix()
        else:inf["warning"]="Over GitHub single-file size limit, not committed"
        inventory["files"][name]=inf
        print(name,src,inf["bytes"],inf.get("signer_sha256"))
    framework=list(root.rglob("framework-res.apk"))
    if framework:
        f=sorted(framework,key=lambda p:len(p.parts))[0]
        dst=dest/"framework-res.apk"
        shutil.copy2(f,dst)
        info=meta(f);info["source_relative_path"]=f.relative_to(root).as_posix()
        info["repo_relative_path"]=dst.relative_to(base).as_posix()
        inventory["files"]["framework-res.apk"]=info
    # Framework and vendor service jars may provide missing classes while recompiling.
    candidates=[p for p in root.rglob("*.jar")
                if any(word in p.name.lower() for word in ("trd","mediatek","mstar","tvsetting","tvservice","projector"))]
    jars=dest.parent/"JAR"
    for f in sorted(candidates):
        if f.stat().st_size>40*1024*1024:continue
        target=jars/f.name
        if target.exists() and digest(target)!=digest(f):continue
        jars.mkdir(parents=True,exist_ok=True)
        shutil.copy2(f,target)
        inventory["files"]["jar:"+f.name]={"bytes":f.stat().st_size,"sha256":digest(f),
            "source_relative_path":f.relative_to(root).as_posix(),
            "repo_relative_path":target.relative_to(base).as_posix()}
    (reports/"inventory.json").write_text(json.dumps(inventory,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
    lines=["# OEM APK extraction — "+opts.code,"","Automated read-only extraction from verified OTA; no projector used.","",
           "| Component | APK/Package | Bytes | SHA256 |","|---|---|---:|---|"]
    for name,f in inventory["files"].items():
        line=f.get("package_line",f.get("repo_relative_path","not copied")).replace("|","/")
        lines.append(f"| {name} | {line[:120]} | {f['bytes']} | \`{f['sha256']}\` |")
    lines.extend(["","Missing / absent: "+(", ".join(inventory["missing"]) or "none"),"",
      "Signing SHA256 values in inventory are APK certificates, **not** OTA certificate fingerprints.",
      "No OEM code was run or decompiled by this script."])
    (reports/"README.md").write_text("\n".join(lines)+"\n",encoding="utf-8")
    if "TvSettingsPlus" not in inventory["files"]:
        sys.exit("ERROR: base TvSettingsPlus APK not found in image")
if __name__=="__main__":main()

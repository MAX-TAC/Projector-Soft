#!/usr/bin/env python3
"""Report references to KuSettings across every system/vendor/product APK/JAR/DEX/config.
Read-only OTA investigation; never publishes extracted proprietary sources.
"""
from pathlib import Path
import argparse, zipfile, hashlib,json,collections,re
TOKENS={
"ku_package":(b"com.my.settings",b"com/my/settings",b"com/p001my/settings",b"Lcom/my/settings",b"Lcom/p001my/settings"),
"ku_service":(b"com.my.service",b"com/my/service",b"com/p001my/service"),
"auto_bt":(b"android.settings.AUTO_BLUETOOTH",),
"screen_services":(b"ScreenServices",),
"ku_activities":(b"MyAutoBluetoothActivity",b"SettingActivity",b"ProjectionActivity"),
}
SCOPED={".apk",".jar",".dex",".vdex",".odex",".oat",".so",".xml",".rc",".prop",".json",".txt"}
def hashfile(p):
    h=hashlib.sha256()
    with p.open("rb") as f:
        for chunk in iter(lambda:f.read(1<<20),b""):h.update(chunk)
    return h.hexdigest()
def contents(blob):
    r={}
    for kind,variants in TOKENS.items():
        count=sum(blob.count(v) for v in variants)
        if count:r[kind]=count
    return r
def process(p):
    results={}
    if p.suffix.lower() in (".apk",".jar"):
        try:
            with zipfile.ZipFile(p) as f:
                for n in f.namelist():
                    if (re.fullmatch(r"classes\d*\.dex",n) or n=="AndroidManifest.xml" or
                        n.startswith("assets/") and n.endswith((".xml",".json",".properties"))):
                        hits=contents(f.read(n))
                        if hits:results[n]=hits
        except Exception as e:results["error"]={"zip_error":str(e)[:180]}
    else:
        if p.stat().st_size>200*1024*1024:return {}
        hits=contents(p.read_bytes())
        if hits:results["raw"]=hits
    return results
def main():
    ap=argparse.ArgumentParser()
    ap.add_argument("root",type=Path);ap.add_argument("out",type=Path)
    a=ap.parse_args();a.out.mkdir(parents=True,exist_ok=True)
    candidates=sorted(p for p in a.root.rglob("*") if p.is_file() and p.suffix.lower() in SCOPED)
    hits={};totals=collections.Counter();apps=0
    for p in candidates:
        rel=p.relative_to(a.root).as_posix()
        if p.suffix.lower()==".apk":apps+=1
        info=process(p)
        if not info:continue
        hits[rel]={"bytes":p.stat().st_size,"matches":info}
        for vals in info.values():
            if not isinstance(vals,dict):continue
            for k,v in vals.items():
                if isinstance(v,int):totals[k]+=v
    data={"scope":"All extracted 8052 EROFS system, system_ext, vendor, product readable files",
          "files_scanned":len(candidates),"apk_scanned":apps,"references":hits,"totals":dict(totals),
          "caveat":"DEX strings may contain dead code; inaccessible runtime ODEX may escape detection; no hardware execution."}
    (a.out/"inventory.json").write_text(json.dumps(data,ensure_ascii=False,indent=2)+"\n")
    rows=["# RUIOS 8052: full read-only KuSettings reference audit","",
          f"Scanned **{len(candidates)}** files including **{apps}** APK files.",
          "This is string/DEX reference inventory, not proof of runtime dependency.",
          "",
          "| File | KuSettings | OEM service | AUTO_BLUETOOTH | Screenshots |",
          "|---|---:|---:|---:|---:|"]
    for rel,entry in hits.items():
        counts=collections.Counter()
        for n,v in entry["matches"].items():
            if not isinstance(v,dict):continue
            for k,c in v.items():
                if isinstance(c,int):counts[k]+=c
        if any(counts.get(k) for k in ("ku_package","ku_service","auto_bt","screen_services")):
            rows.append(f"| {rel} | {counts['ku_package']} | {counts['ku_service']} | {counts['auto_bt']} | {counts['screen_services']} |")
    rows+=["","Important: no arbitrary replacements. Android framework PhoneWindowManager may be runtime-loaded from services.jar, not WFDSinkTest.apk; check actual hosting process before patching an APK.", "",
      "Do not disable KuSettings until all live intents have handlers, HDMI/focus/audio services tested, and a rollback plan exists."]
    (a.out/"README.md").write_text("\n".join(rows)+"\n")
    print("\n".join(rows[:110]))
if __name__=="__main__":main()

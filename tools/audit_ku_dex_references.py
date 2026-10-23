#!/usr/bin/env python3
"""Read DEX string tables of OEM APKs to identify possible KuSettings dependencies.

Structural static evidence, not proof of execution or call graph connectivity.
Does not decompile proprietary source, call vendor services, or modify APKs.
"""
import argparse
import json
import re
import struct
import zipfile
from pathlib import Path
from collections import Counter

TARGETS={
  "KuSettings":re.compile(r"(?i)(?:^L?com[/\.](?:p\d+)?my[/\.](?:settings|service|utils)|^com\.my\.settings$|^com\.my\.service$|^KuSettings$|^SettingActivity$)"),
  "KuSourceRJ":re.compile(r"(?i)(?:^L?com[/\.]coolux[/\.]source|^com\.coolux\.source$)"),
  "TRD":re.compile(r"(?i)(?:^L?com[/\.]trd[/\.](?:framework|service|sdk)|^com\.trd\.(?:framework|service)$)"),
  "Focus":re.compile(r"(?i)(?:^L?com[/\.]hysd[/\.]screen|^com\.hysd\.screen$)"),
  "Geometry":re.compile(r"(?i)(?:^L?com[/\.]rjpj[/\.]tcorrection|^com\.rjpj\.tcorrection$)"),
}
DEX_FILENAME=re.compile(r"classes\d*\.dex\Z")
def decode_uleb(data,off):
    num=0
    for shift in range(0,35,7):
        if off>=len(data):raise ValueError("Truncated dex string")
        v=data[off];off+=1
        num|=(v&0x7f)<<shift
        if not v&0x80:return num,off
    raise ValueError("Invalid uleb")
def strings(blob):
    if not blob.startswith(b"dex\n"):raise ValueError("Not a DEX file")
    count,ptr=struct.unpack_from("<II",blob,0x38)
    if count>1000000 or ptr+4*count>len(blob):raise ValueError("Invalid DEX string table")
    for i in range(count):
        offset=struct.unpack_from("<I",blob,ptr+i*4)[0]
        if offset>=len(blob):continue
        _,off=decode_uleb(blob,offset)
        end=blob.find(b"\0",off, min(len(blob),off+1500))
        if end<0:continue
        try:value=blob[off:end].decode("utf-8","replace")
        except Exception:continue
        yield value
def scan(apkdir):
    result={}
    for apk in sorted(apkdir.glob("*.apk")):
        item={"apk":apk.name,"dex_count":0,"signals":{}}
        hits={name:set() for name in TARGETS}
        with zipfile.ZipFile(apk) as z:
            for file in z.namelist():
                if not DEX_FILENAME.fullmatch(file):continue
                item["dex_count"]+=1
                for value in strings(z.read(file)):
                    for name,pat in TARGETS.items():
                        if pat.search(value) and len(value)<235 and "\n" not in value:
                            hits[name].add(value)
        for name,values in hits.items():
            if values:
                item["signals"][name]={"count":len(values),"samples":sorted(values)[:40]}
        result[apk.stem]=item
    return result

def main():
    p=argparse.ArgumentParser()
    p.add_argument("apk_directory",type=Path)
    p.add_argument("output_directory",type=Path)
    a=p.parse_args()
    a.output_directory.mkdir(exist_ok=True,parents=True)
    result=scan(a.apk_directory)
    (a.output_directory/"dex-package-references.json").write_text(json.dumps(result,ensure_ascii=False,indent=2)+"\n")
    rows=["# 8052 OEM DEX cross-package reference survey","",
      "DEX string-pool membership is not proof of a live Intent or service dependency.",
      "Absence of a literal reference does not exclude reflection, resource lookups, binder or implicit broadcasts.",
      "",
      "| APK | KuSettings strings | KuSource strings | TRD strings | Focus strings | Keystone strings |",
      "|---|---:|---:|---:|---:|---:|"]
    for name,v in result.items():
        get=lambda category: str(v["signals"].get(category,{}).get("count",0))
        rows.append("| "+name+" | "+" | ".join(get(x) for x in ("KuSettings","KuSourceRJ","TRD","Focus","Geometry"))+" |")
    rows+=["","## Potential inbound KuSettings refs outside KuSettings",""]
    for name,v in result.items():
        if name=="KuSettings":continue
        if "KuSettings" not in v["signals"]:continue
        rows.append("### "+name)
        for sig in v["signals"]["KuSettings"]["samples"][:40]:
            rows.append("- "+sig.replace("\x00",""))
    rows+=["","Do not disable KuSettings based solely on this survey. Verify explicit and implicit calls, BootupReceiver, MySetTimeService, ScreenServices and runtime traces."]
    (a.output_directory/"DEX_DEPENDENCIES.md").write_text("\n".join(rows)+"\n")
    print("Scanned",len(result),"APKs; external KuSettings references",sum("KuSettings" in v["signals"] for k,v in result.items() if k!="KuSettings"))

if __name__=="__main__":main()

#!/usr/bin/env python3
"""Repack modified TvSettingsPlus manifest and rebuilt DEX while retaining OEM resources, JNI and assets."""
from pathlib import Path
import argparse, zipfile, re, copy
DEX=re.compile(r"classes\d*\.dex$")
SIGN=(".RSA",".DSA",".EC",".SF",".MF")
def main():
    p=argparse.ArgumentParser()
    p.add_argument("original",type=Path)
    p.add_argument("rebuilt",type=Path)
    p.add_argument("output",type=Path)
    a=p.parse_args(); a.output.parent.mkdir(exist_ok=True,parents=True)
    with zipfile.ZipFile(a.original) as original,zipfile.ZipFile(a.rebuilt) as rebuilt:
        names={n for n in original.namelist() if DEX.fullmatch(n)}
        changed={n for n in rebuilt.namelist() if DEX.fullmatch(n)}
        if len(names)!=16 or changed!=names:raise ValueError(f"DEX mismatch original={names},rebuilt={changed}")
        o_manifest=original.read("AndroidManifest.xml")
        r_manifest=rebuilt.read("AndroidManifest.xml")
        if o_manifest==r_manifest:raise ValueError("Manifest unchanged, new aliases not applied")
        rarsc=rebuilt.read("resources.arsc")
        oarsc=original.read("resources.arsc")
        if rarsc!=oarsc:
            print("NOTE: apktool resources differ; keeping OEM original resources.arsc by design")
        with zipfile.ZipFile(a.output,"w",allowZip64=True) as result:
            for info in original.infolist():
                name=info.filename
                if name.startswith("META-INF/") and name.upper().endswith(SIGN):continue
                if DEX.fullmatch(name):data=rebuilt.read(name)
                elif name=="AndroidManifest.xml":data=r_manifest
                else:data=original.read(name)
                result.writestr(copy.copy(info),data)
        with zipfile.ZipFile(a.output) as result:
            assert result.read("resources.arsc")==oarsc
            assert result.read("AndroidManifest.xml")!=o_manifest
            for info in original.infolist():
                name=info.filename
                if name.startswith("META-INF/") or name in names or name=="AndroidManifest.xml":continue
                if result.read(name)!=original.read(name):raise AssertionError("Original OEM entry altered "+name)
            print("Original vendor resources, JNI, assets preserved; modified DEX+manifest merged")
if __name__=="__main__":main()

#!/usr/bin/env python3
"""Merge patched Apktool smali DEX back into ORIGINAL TvSettingsPlus APK.

The original binary resources.arsc, assets, native libraries, compiled manifest
and all unknown vendor files are preserved byte-for-byte. This avoids AAPT
renaming/dropping OEM resources. Requires successful apktool decode -r / build.
"""
import argparse
import copy
import re
import zipfile
from pathlib import Path
from patch_axml_version import change_version_code

DEX_PATTERN=re.compile(r"classes\d*\.dex\Z")
REMOVED_SIGNATURES=(".RSA",".DSA",".EC",".SF",".MF")

def names(z):
    return {n for n in z.namelist() if DEX_PATTERN.fullmatch(n)}

def assemble(original, rebuilt_smali, output, current, next_version):
    output.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(original) as src, zipfile.ZipFile(rebuilt_smali) as dex:
        baseline=names(src)
        patched=names(dex)
        if not baseline or not patched or baseline!=patched:
            raise ValueError(f"DEX sets do not match: orig={sorted(baseline)}, patched={sorted(patched)}")
        if "resources.arsc" not in src.namelist():
            raise ValueError("Missing OEM resources.arsc")
        if "AndroidManifest.xml" not in src.namelist():
            raise ValueError("Missing OEM manifest")
        new_manifest=change_version_code(src.read("AndroidManifest.xml"),current,next_version)
        modified=set(baseline)|{"AndroidManifest.xml"}
        with zipfile.ZipFile(output,mode="w",allowZip64=True) as dst:
            for info in src.infolist():
                name=info.filename
                if name.startswith("META-INF/") and name.upper().endswith(REMOVED_SIGNATURES):
                    continue
                if name=="AndroidManifest.xml":
                    payload=new_manifest
                elif name in baseline:
                    payload=dex.read(name)
                else:
                    payload=src.read(name)
                dst.writestr(copy.copy(info),payload)
        with zipfile.ZipFile(output) as out:
            for name in out.namelist():
                if name in modified:continue
                if src.read(name)!=out.read(name):
                    raise AssertionError("Vendor entry changed: "+name)
            if src.read("resources.arsc")!=out.read("resources.arsc"):
                raise AssertionError("OEM resources changed")
        print(f"Patched {len(baseline)} DEX archives, upgraded version {current}->{next_version}; other vendor bytes unchanged")

def main():
    p=argparse.ArgumentParser()
    p.add_argument("original",type=Path)
    p.add_argument("rebuilt_smali",type=Path)
    p.add_argument("output",type=Path)
    p.add_argument("--old",type=int,default=1)
    p.add_argument("--new",type=int,default=2)
    a=p.parse_args()
    assemble(a.original,a.rebuilt_smali,a.output,a.old,a.new)
if __name__=="__main__":main()

#!/usr/bin/env python3
"""Compose safe experimental MOD v3 from original OEM APK, rebuilt brightness DEX and new module dex.
Leaves every other original OEM APK ZIP entry (including all hardware services) unchanged.
"""
from pathlib import Path
import sys,re,zipfile,copy,json,argparse
sys.path.insert(0,str(Path(__file__).resolve().parent))
from patch_axml_compat_aliases import patch_manifest
from patch_axml_version import change_version_code
rx=re.compile(r"classes\d*\.dex$")
SIG=(".RSA",".DSA",".EC",".SF",".MF")
def main():
 p=argparse.ArgumentParser()
 p.add_argument("original",type=Path)
 p.add_argument("rebuilt",type=Path)
 p.add_argument("compiled_dex",type=Path)
 p.add_argument("patch_report",type=Path)
 p.add_argument("output",type=Path)
 p.add_argument("--version-code",type=int,default=3,choices=(3,4,5,6))
 a=p.parse_args()
 meta=json.loads(a.patch_report.read_text())
 modified=set(meta["edited_dex"])
 with zipfile.ZipFile(a.original) as src,zipfile.ZipFile(a.rebuilt) as fixed:
  original_names={x for x in src.namelist() if rx.fullmatch(x)}
  rebuilt_names={x for x in fixed.namelist() if rx.fullmatch(x)}
  if len(original_names)!=16 or original_names!=rebuilt_names or not modified.issubset(original_names):
   raise ValueError("Unexpected original/modified DEX file set")
  if not a.compiled_dex.read_bytes().startswith(b"dex\n"):
   raise ValueError("Bad Android classes17 DEX")
  manifest,actions=patch_manifest(src.read("AndroidManifest.xml"))
  manifest=change_version_code(manifest,2,a.version_code)
  a.output.parent.mkdir(parents=True,exist_ok=True)
  with zipfile.ZipFile(a.output,"w",allowZip64=True) as result:
   for i in src.infolist():
    name=i.filename
    if name.startswith("META-INF/") and name.upper().endswith(SIG):continue
    data=(manifest if name=="AndroidManifest.xml"
       else fixed.read(name) if name in modified
       else src.read(name))
    result.writestr(copy.copy(i),data)
   result.writestr("classes17.dex",a.compiled_dex.read_bytes(),compress_type=zipfile.ZIP_DEFLATED)
 with zipfile.ZipFile(a.original) as src,zipfile.ZipFile(a.output) as result:
  assert result.read("resources.arsc")==src.read("resources.arsc")
  assert result.read("AndroidManifest.xml")!=src.read("AndroidManifest.xml")
  for name in src.namelist():
   if name.startswith("META-INF/") or name in modified or name=="AndroidManifest.xml":continue
   if result.read(name)!=src.read(name):raise AssertionError("Changed untouched vendor entry "+name)
  assert result.read("classes17.dex").startswith(b"dex\n")
 print("PASS: merged",len(modified),"reassembled OEM DEX + 1 new module DEX,",
       len(actions),"legacy aliases; OEM resources/other dex intact")
 print("Modified DEX:",sorted(modified))
if __name__=="__main__":main()

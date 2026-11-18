"""Recheck the actual pinned APK archives and OEM edits on a host CI runner."""
import argparse
import difflib
import hashlib
import json
import re
import struct
import subprocess
import sys
import tempfile
import zipfile
import zlib
from pathlib import Path

from kit_common import MOD_SHA, STOCK_SHA, sha256


DEX = re.compile(r"classes\d*\.dex")
SIGNATURE = re.compile(r"META-INF/(?:MANIFEST\.MF|[^/]+\.(?:SF|RSA|DSA|EC))", re.I)


def dex_integrity(payload):
    return (payload.startswith(b"dex\n") and len(payload) >= 112
            and struct.unpack_from("<I",payload,32)[0] == len(payload)
            and payload[12:32] == hashlib.sha1(payload[32:]).digest()
            and struct.unpack_from("<I",payload,8)[0] == zlib.adler32(payload[12:]))


def manifest_inventory(path):
    text = path.read_text()
    kinds = ("activity", "activity-alias", "service", "receiver", "provider")
    return {kind: len(re.findall(r"(?m)^\s*E: " + kind + r"(?=\s|$)",text)) for kind in kinds}


def smali_inventory(root, module=False):
    result = {}
    for path in root.glob("smali*/**/*.smali"):
        relative = path.relative_to(root).as_posix()
        if relative.startswith("smali_classes17/") != module:
            continue
        body = path.read_text()
        # Reassembly can widen const-string and goto, or renumber labels by offset.
        # Preserve label relationships, method bodies, annotations and all constants.
        body = re.sub(r"\bconst-string/jumbo\b","const-string",body)
        body = re.sub(r"\bgoto/(?:16|32)\b","goto",body)
        def normalize_method(match):
            labels = {}
            return re.sub(r'"(?:\\.|[^"\\])*"|(?<!\S):[A-Za-z][\w]*\b',
                          lambda token: token[0] if token[0].startswith('"')
                          else labels.setdefault(token[0],f":label_{len(labels)}"),match[0])
        body = re.sub(r"(?ms)^\.method .*?^\.end method",normalize_method,body)
        result[relative] = hashlib.sha256(body.encode()).hexdigest()
    return result


def audit(args, report):
    if sha256(args.stock) != STOCK_SHA or sha256(args.mod) != MOD_SHA:
        raise ValueError("Audit input does not match pinned APK bytes")
    sys.path.insert(0,str(args.source/"tools"))
    from patch_axml_compat_aliases import patch_manifest
    from patch_axml_version import change_version_code
    with zipfile.ZipFile(args.stock) as stock, zipfile.ZipFile(args.mod) as mod:
        for archive in (stock,mod):
            if len(set(archive.namelist())) != len(archive.namelist()) or archive.testzip() is not None:
                raise ValueError("Duplicate entries or ZIP CRC failure")
        stock_names, mod_names = ({name for name in a.namelist() if not SIGNATURE.fullmatch(name)}
                                  for a in (stock,mod))
        if mod_names != stock_names | {"classes17.dex"}:
            raise ValueError("Unexpected added or removed archive payloads")
        expected_manifest = change_version_code(patch_manifest(stock.read("AndroidManifest.xml"))[0],2,33)
        if mod.read("AndroidManifest.xml") != expected_manifest:
            raise ValueError("Manifest has a delta outside the pinned source patches")
        for name in sorted(stock_names):
            if name != "AndroidManifest.xml" and not DEX.fullmatch(name) and stock.read(name) != mod.read(name):
                raise ValueError(f"Stock resource/native/asset payload changed: {name}")
        for archive,names in ((stock,stock_names),(mod,mod_names)):
            invalid = [name for name in names if DEX.fullmatch(name) and not dex_integrity(archive.read(name))]
            if invalid:
                raise ValueError(f"Invalid DEX checksum/header: {invalid}")
        report["changed_dex"] = sorted(name for name in stock_names if DEX.fullmatch(name)
                                      and stock.read(name) != mod.read(name))
        report["added_dex"] = ["classes17.dex"]
    report["manifest"] = {label: manifest_inventory(next(args.reports.glob(f"{label.upper()}-*-tree.txt")))
                          for label in ("stock","mod")}
    report["stock_oem_payload"] = "UNCHANGED_EXCEPT_REVIEWED_MANIFEST_AND_DEX"
    report["alignment"] = {}
    for label,path in (("stock",args.stock),("mod",args.mod)):
        run = subprocess.run([args.zipalign,"-c","-p","4",str(path)],capture_output=True,text=True)
        report["alignment"][label] = "PASS" if not run.returncode else "FAIL"
        (args.reports/f"{label}-zipalign.txt").write_text(run.stdout+run.stderr)
    if report["alignment"]["mod"] != "PASS":
        raise ValueError("MOD zipalign check failed")

    with tempfile.TemporaryDirectory(prefix="exact8054-audit-",dir="/tmp") as temp:
        work = Path(temp)
        baseline, actual = work/"expected", work/"actual"
        for apk,output in ((args.stock,baseline),(args.mod,actual)):
            subprocess.run(["java","-jar",str(args.apktool),"d","-f","-r",str(apk),"-o",str(output)],
                           check=True,timeout=240)
        edited = set()
        edited_dex = set()
        for tool in ("patch_customer0_lightmode_smali.py","patch_aosp_root_menu_smali.py","patch_oem_sleep_menu_smali.py"):
            proof = work/(tool+".json")
            subprocess.run([sys.executable,str(args.source/"tools"/tool),str(baseline),str(proof)],check=True)
            results = json.loads(proof.read_text())
            edited.update(results["edited_smali"])
            edited_dex.update(results["edited_dex"])
        report["reviewed_edited_oem_classes"] = sorted(edited)
        if len(edited) != 8 or set(report["changed_dex"]) != edited_dex:
            raise ValueError("Original DEX set does not match the eight reviewed patch sites")
        expected, observed = smali_inventory(baseline), smali_inventory(actual)
        changed = sorted(name for name in set(expected) | set(observed) if expected.get(name) != observed.get(name))
        report["pre_assembly_smali_differences"] = changed
        report["stock_class_count"] = len(expected)
        rebuilt = work/"expected.apk"
        # Assemble the reviewed stock edits using the exact build's Apktool version.
        # Comparing the output bytes handles assembler changes to instruction forms,
        # labels, and debug directives without allowing different executable code.
        subprocess.run(["java","-jar",str(args.apktool),"b",str(baseline),"-o",str(rebuilt)],
                       check=True,timeout=300)
        with zipfile.ZipFile(rebuilt) as expected_apk, zipfile.ZipFile(args.mod) as pinned_apk:
            different_dex = sorted(name for name in edited_dex
                                   if expected_apk.read(name) != pinned_apk.read(name))
        report["reproduced_oem_dex"] = sorted(edited_dex - set(different_dex))
        if different_dex and changed:
            first = changed[0]
            before = (baseline/first).read_text().splitlines() if (baseline/first).is_file() else []
            after = (actual/first).read_text().splitlines() if (actual/first).is_file() else []
            details = list(difflib.unified_diff(before,after,fromfile="expected/"+first,tofile="actual/"+first))
            (args.reports/"first-oem-difference.txt").write_text("\n".join(details),encoding="utf-8")
        if different_dex:
            raise ValueError(f"Reviewed stock patches did not reproduce DEX bytes: {different_dex}")
        added = smali_inventory(actual,module=True)
        if not added or any(not name.startswith("smali_classes17/dev/projectorsoft/tvsettings/") for name in added):
            raise ValueError("Added DEX classes leave the expected MOD namespace")
        report["mod_class_count"] = len(added)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    for name in ("stock","mod","source","reports","apktool"):
        parser.add_argument("--"+name,required=True,type=Path)
    parser.add_argument("--zipalign",default="zipalign")
    args = parser.parse_args()
    result = {"status":"FAIL","device_compatibility":"LIVE_PACKAGE_MANAGER_REQUIRED"}
    try:
        audit(args,result)
        result["status"] = "PASS"
    except (ValueError,OSError,subprocess.SubprocessError,StopIteration) as error:
        result["failure"] = str(error)
    (args.reports/"integration-audit.json").write_text(json.dumps(result,indent=2)+"\n")
    print("APK integration audit:",json.dumps({k:v for k,v in result.items() if k != "pre_assembly_smali_differences"}))
    return int(result["status"] != "PASS")


if __name__ == "__main__":
    sys.exit(main())

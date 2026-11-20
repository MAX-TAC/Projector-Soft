#!/usr/bin/env python3
"""NATIVE-005 exact-8054 read-only HIDL/vtable/receiver evidence collector.

Only run after the owner archive SHA-256 verification & EROFS extraction.
All published data is selected ELF symbols/relocations/instructions and
allowlisted service manifests; never dump raw strings or firmware binaries.
"""
from __future__ import annotations

import argparse
import hashlib
import pathlib
import re
import shutil
import struct
import subprocess
import xml.etree.ElementTree as ET

HASHES = {
    "vendor_a/lib/vendor.mediatek.api.mtktvapi@1.0-cwrapper.so":
        "e470029a7e5d52ea25d17be2fb255e2dfe1fe75d6fe6cc527b3146001a3c8758",
    "vendor_a/lib/vendor.mediatek.api.mtktvapi@1.0.so":
        "a48159c7c66e5e82a4d293bc1e59e878cb0cfafbd8f645d872332f10424711f7",
    "vendor_a/lib/libmediatek_tv_rpcwrapper.so":
        "d8938a5733c5cacd32de03eef8dafd5ea16acb9d6386bfdc7974323782b8f62b",
}
SET_NAME = "a_mtktvapi_config_set_value"
FUNC_RE = re.compile(r"config_set_value(?:_for_all_elems)?E")
ADDRESS_LINE = re.compile(r"^\s*[0-9a-f]+:", re.I)
REGEX_SERV = re.compile(r"mtktvapi|medIATEK.*tv|tvapi", re.I)
ALLOWED_RC = re.compile(r"vendor\.mediatek\.api\.mtktvapi|^\s*(service|interface)\s+[^\s]*(?:mtktv|tvapi)", re.I)


def call(argv: list[str], timeout=120, allow_error=False) -> str:
    p = subprocess.run(argv, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                       encoding="utf-8", errors="replace", check=False, timeout=timeout)
    if p.returncode and not allow_error:
        raise RuntimeError(f"{argv[0]} error {p.returncode}: {p.stderr[-240:]}")
    return p.stdout


def verify(path: pathlib.Path, expected: str):
    sha = hashlib.sha256()
    with path.open("rb") as fd:
        for chunk in iter(lambda: fd.read(4 * 1024 * 1024), b""):
            sha.update(chunk)
    if sha.hexdigest() != expected:
        raise RuntimeError("STOP: exact 8054 hash mismatch: " + path.name)


def elf_symbols(path: pathlib.Path):
    result = []
    for line in call(["readelf", "-WsW", str(path)], allow_error=True).splitlines():
        fields = line.split()
        if len(fields) >= 8 and fields[0].endswith(":"):
            result.append({"addr": fields[1], "size": fields[2], "type": fields[3],
                           "ndx": fields[6], "name": fields[-1], "line": line.strip()})
    return result


def demangle(value: str):
    if not value.startswith("_Z"):
        return value
    return call(["c++filt", value], allow_error=True).strip()


def selected_disassembly(path: pathlib.Path, symbol: str, cap=160):
    lines = call(["arm-linux-gnueabihf-objdump", "-drw",
                  "--disassemble=" + symbol, str(path)]).splitlines()
    insn_indices = [i for i, line in enumerate(lines) if ADDRESS_LINE.match(line)]
    if not insn_indices:
        return ["NO_INSTRUCTIONS / wrong dynamic symbol or disassembler mismatch"]
    selected = set(insn_indices[:30])
    for i in insn_indices:
        line = lines[i]
        if re.search(r"\b(?:blx?|bx|svc)\b", line) or re.search(
                r"\b(?:strh|ldrsh|ldr|str|cmp|movs?)\b[^;]*#(?:[0-9]+)", line):
            for k in range(max(0, i-2), min(len(lines), i+3)):
                if ADDRESS_LINE.match(lines[k]):
                    selected.add(k)
    ordered = sorted(selected)
    trunc = max(0, len(ordered)-cap)
    filtered = [re.sub(r"<[^>]{1,900}>", "<nearest-symbol-label-elided>", lines[i].rstrip())
                for i in ordered[:cap]]
    if trunc:
        filtered.append(f"[{trunc} additional selected lines not published; bounded excerpt]")
    return filtered


def service_candidates(root: pathlib.Path):
    paths = []
    for part in ("vendor_a", "system_ext_a"):
        tree = root / (part + "_tree")
        if not tree.is_dir():
            continue
        for folder in ("bin", "bin/hw", "lib", "lib/hw"):
            sub = tree / folder
            if not sub.is_dir():
                continue
            for path in sub.iterdir():
                if path.is_file() and REGEX_SERV.search(path.name):
                    if ".so" not in path.name or "mtktvapi" in path.name or "service" in path.name:
                        paths.append((part, path))
    return sorted(set(paths), key=lambda pair: str(pair[1]))[:70]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=pathlib.Path, default=pathlib.Path("/tmp/flix8054_erofs"))
    parser.add_argument("--out", type=pathlib.Path, default=pathlib.Path("/tmp/flix8054_native_report"))
    args = parser.parse_args()
    for x in ("readelf", "arm-linux-gnueabihf-objdump", "c++filt"):
        if shutil.which(x) is None:
            raise RuntimeError("STOP: missing " + x)
    for name, sha in HASHES.items():
        verify(args.root / (name.split("/")[0] + "_tree") / "/".join(name.split("/")[1:]), sha)

    cwrap = args.root / "vendor_a_tree/lib/vendor.mediatek.api.mtktvapi@1.0-cwrapper.so"
    hidl = args.root / "vendor_a_tree/lib/vendor.mediatek.api.mtktvapi@1.0.so"
    rows = [
        "# NATIVE-005: exact 8054 C-wrapper / HIDL vtable, proxy, server search",
        "",
        "Read-only machine observations from SHA-256-gated owner firmware.",
        "No claim of runtime Binder/HIDL reachability or physical display behavior.",
        "",
        "## Provenance and commands",
        "",
        "- Archive SHA-256 checked by workflow before EROFS extraction: "
        "`4f53f2224c98b4c832a19a591b9dd7f925bde28528b5674a09732c0f0e41d362`.",
    ]
    for name, sha in HASHES.items():
        rows.append("- `" + name + "`: `" + sha + "` (verified).")
    rows += ["- Tool versions: `" + call(["readelf", "--version"]).splitlines()[0] +
             "`; `" + call(["arm-linux-gnueabihf-objdump", "--version"]).splitlines()[0] + "`.",
             "- `readelf -h -WsW -rW -SW`; `arm-linux-gnueabihf-objdump -drw --disassemble=SYMBOL`; `c++filt`.",
             "",
             "## A. C-wrapper virtual dispatch boundary",
             "",
             "- `a_mtktvapi_config_set_value` imported by JNI; C-wrapper function at "
             "`0x3f584` (Thumb; symbol 0x3f585).",
             "- C-wrapper `0x3f5c0: ldr r7, [r0,#60]`, "
             "`0x3f5cc: blx r7` is a virtual call at byte offset **+60**.",
             "- The static HIDL client method corresponding to that virtual slot "
             "still requires proof from vtable/relocation.",
             "",
             "```asm"]
    rows += selected_disassembly(cwrap, SET_NAME, cap=110)
    rows += ["```", "", "## B. HIDL BpHw/BnHw exports, vtables and relocations", ""]
    syms = elf_symbols(hidl)
    targets = [s for s in syms if s["ndx"] != "UND" and
               FUNC_RE.search(s["name"]) and
               any(k in s["name"] for k in ("BpHwMtkTvApiConfig", "BnHwMtkTvApiConfig"))]
    rows += ["### Exact methods (demangled)", "", "```text"]
    for sym in targets[:16]:
        rows.append(f"{sym['addr']} size={sym['size']} {demangle(sym['name'])}")
    rows += ["```", "", "### Exact vtables / method relocation candidates", ""]
    vtables = [s for s in syms if "_ZTV" in s["name"] and "MtkTvApiConfig" in s["name"]]
    rows += ["```text"]
    rows += [s["line"] + "  " + demangle(s["name"]) for s in vtables[:16]] or ["NO_SYMBOL"]
    rows += ["```", "", "- Relocations naming config-set functions (avoid interpreting a "
             "relocation as a specific vtable edge without its section/offset):",
             "", "```text"]
    relocs = call(["readelf", "-rW", str(hidl)]).splitlines()
    relevant = [x.strip() for x in relocs
                if FUNC_RE.search(x) and
                ("BpHwMtkTvApiConfig" in x or "BnHwMtkTvApiConfig" in x)]
    rows += relevant[:35] or ["NO_RELOCATION_WITH_FUNCTION_NAME"]
    rows += ["```", ""]
    for keyword, title, cap in [
        ("BpHwMtkTvApiConfig34a_hidl_a_mtktvapi_config_set_value", "BpHw concrete virtual method", 80),
        ("BpHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_value", "BpHw outbound HIDL proxy implementation", 170),
        ("BnHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_value", "BnHw inbound HIDL parcel dispatch", 170),
    ]:
        chosen = [s for s in targets if keyword in s["name"]]
        rows += ["", "### " + title, ""]
        if not chosen:
            rows.append("SYMBOL_NOT_FOUND")
            continue
        sym = chosen[0]
        rows += ["- Exact function: `" + demangle(sym["name"]) + "`",
                 "- ELF address `0x" + sym["addr"] + "`; size " + sym["size"],
                 "", "```asm"]
        rows += selected_disassembly(hidl, sym["name"], cap=cap)
        rows += ["```"]

    rows += ["", "## C. Read-only search for exact-8054 HIDL service implementation",
             "", "### Executable/implementation-file candidates by allowlisted basename",
             "", "```text"]
    candidates = service_candidates(args.root)
    rows += [f"{part}: {p.relative_to(args.root / (part + '_tree'))}" for part,p in candidates] or ["NO_FILENAME_CANDIDATE"]
    rows += ["```", "", "### Defined/undefined exports suggesting concrete receiver (per ELF)", ""]
    for part,p in candidates:
        sym = elf_symbols(p)
        relevant = [x for x in sym if
                    re.search(r"(?:MtkTvApiConfigImpl|MtkTvApiImpl|IMtkTvApiConfig|MtkTvApiConfig|mtktvapi_config_set_value)", x["name"], re.I)
                    and ("MtkTvApiConfig" in x["name"] or "mtktvapi_config_set_value" in x["name"])]
        if relevant:
            rows += ["", "- `" + part + "/" + str(p.relative_to(args.root / (part+"_tree"))) + "`:",
                     "", "```text"]
            rows += [f"{z['ndx']} {z['type']} {z['addr']} {demangle(z['name'])}"
                     for z in relevant[:35]]
            rows += ["```"]
    rows += ["", "### Sanitized HIDL VINTF and init metadata", ""]
    for part in ("vendor_a", "system_ext_a"):
        tree = args.root / (part + "_tree")
        if not tree.is_dir():
            continue
        for base in ("etc/vintf", "etc/init"):
            directory = tree / base
            if not directory.is_dir():
                continue
            for path in sorted(directory.rglob("*")):
                if not path.is_file() or path.stat().st_size > 300_000:
                    continue
                if path.suffix == ".rc":
                    matched = [x.strip() for x in path.read_text(errors="replace").splitlines()
                               if ALLOWED_RC.search(x)]
                    if matched:
                        rows += [f"- `{part}/{path.relative_to(tree)}`:"]
                        rows += [f"  - `{x[:200]}`" for x in matched[:10]]
                elif path.suffix == ".xml":
                    try:
                        xm = ET.parse(path)
                    except ET.ParseError:
                        continue
                    for hal in xm.getroot().iter("hal"):
                        name = hal.findtext("name") or ""
                        if "mtktvapi" in name:
                            rows += [f"- `{part}/{path.relative_to(tree)}`: "
                                     f"`{name}` instances "
                                     f"`{[i.text for i in hal.findall('.//instance') if i.text]}`"]
    # Resolve specific vtable slot values from the SHA-verified ELF section bytes.
    # An Itanium C++ vtable contains offset-to-top and typeinfo before address
    # point (+8 on ARM32). Compare static function pointer to real dynsym value.
    rows += ["", "## C2. Raw vtable slot +60 from exact ELF .data.rel.ro",
             "", "The Itanium C++ vtable address point is vtable symbol + 8 on ARM32.",
             "Static pointers carry Thumb state in bit0; compare against exact ELF symbol values.",
             "", "| Vtable | Vtable address | pointer at address-point + 60 | matching exported method |",
             "|---|---|---|---|"]
    sections = []
    for line in call(["readelf", "-SW", str(hidl)]).splitlines():
        m = re.match(r"^\s*\[\s*\d+\]\s+(\S+)\s+\S+\s+([0-9a-f]+)\s+([0-9a-f]+)\s+([0-9a-f]+)", line)
        if m:
            sections.append((m.group(1), int(m.group(2), 16),
                             int(m.group(3), 16), int(m.group(4), 16)))
    blob = hidl.read_bytes()
    hidl_reloc_lines = call(["readelf", "-rW", str(hidl)]).splitlines()
    for sym in vtables:
        vaddr = int(sym["addr"], 16)
        addr = vaddr + 8 + 60
        sec = next((sec for sec in sections if sec[1] <= addr < sec[1]+sec[3]), None)
        target = None
        if sec:
            off = sec[2] + addr - sec[1]
            if off+4 <= len(blob):
                target = struct.unpack_from("<I", blob, off)[0]
        match = [z for z in syms if z["ndx"] != "UND" and z["type"] == "FUNC"
                 and target is not None and int(z["addr"],16) == target]
        table_name = demangle(sym["name"]).replace("vtable for ", "")
        target_str = f"0x{target:08x}" if target is not None else "NOT_READ"
        method = demangle(match[0]["name"]) if match else "NO_EXACT_FUNCTION_MATCH"
        rows.append(f"| `{table_name}` | `0x{vaddr:x}` | `{target_str}` | `{method}` |")
        slots = [line.strip() for line in hidl_reloc_lines if
                 re.match(r"^\s*" + f"{addr:08x}" + r"\b", line, re.I)]
        rows.append(f"  - vtable slot `0x{addr:x}` ELF relocation: " +
                    ("; ".join(slots) if slots else "NO_NAMED_RELOCATION_FOUND"))
    # Search the actual vtable object for the concrete BpHw set method;
    # offset +60 need not be the BpHw address point because C++ classes may
    # have multiple vtables / secondary subobjects and abstract slots.
    setter_vfunc = next((sym for sym in syms if
                         "BpHwMtkTvApiConfig34a_hidl_a_mtktvapi_config_set_valueE" in sym["name"]), None)
    rows += ["", "### Search full BpHw vtable object for the setter pointer", ""]
    if setter_vfunc:
        val = int(setter_vfunc["addr"], 16)
        hits = []
        for v in vtables:
            if "BpHwMtkTvApiConfigE" not in v["name"]:
                continue
            base = int(v["addr"], 16)
            size = int(v["size"])
            for a in range(base,base+size,4):
                sec = next((sec for sec in sections if sec[1] <= a < sec[1]+sec[3]), None)
                if sec:
                    off=sec[2]+a-sec[1]
                    if off+4 <= len(blob):
                        got = struct.unpack_from("<I",blob,off)[0]
                        if got == val:
                            hits.append(a)
        rows += ["- exact BpHw set function pointer \`0x" + f"{val:x}" +
                 "\` in this vtable object: " +
                 (", ".join(f"0x{a:x} (raw offset +0x{a-base:x})" for a in hits) or "NOT_FOUND")]
    else:
        rows.append("- exported BpHw setter not found")

    rows += ["", "## C3. Original HIDL service binary import and registration evidence", ""]
    service = args.root / "vendor_a_tree/bin/hw/vendor.mediatek.api.mtktvapi@1.0-service"
    if not service.is_file():
        rows += ["- SERVICE_EXECUTABLE_NOT_FOUND; cannot inspect service", ""]
    else:
        sh = hashlib.sha256(service.read_bytes()).hexdigest()
        rows.append("- Exact service ELF SHA-256: `" + sh + "`.")
        deps = call(["readelf", "-d", str(service)], allow_error=True)
        needed = re.findall(r"Shared library: \[([^\]]+)\]", deps)
        rows.append("- Selected DT_NEEDED: " +
                    ", ".join("`" + n + "`" for n in needed if
                              re.search("mtktv|mediatek_tv|rpc|hidl|vendor_agent", n, re.I)))
        exported = elf_symbols(service)
        for name in (SET_NAME, "a_mtktvapi_config_set_value_for_all_elems"):
            matching = [z for z in exported if z["name"].split("@@")[0] == name]
            rows.append("- Symbol `" + name + "`: " +
                        ("; ".join(f"{z['ndx']} {z['type']} {z['addr']}" for z in matching[:4])
                         or "NOT_FOUND"))
        other = [z for z in exported if z["ndx"] != "UND" and
                 re.search(r"(ConfigImpl|MtkTvApi.*Impl|MtkTvApiConfig|config_set_value)", z["name"])]
        rows.append("- Selected **defined** service methods: " +
                    str(len(other)) + " (absence is not proof of no implementation; symbols may be stripped).")
        rows += ["", "```text"]
        rows += [z["line"][:350] for z in other[:30]] or ["NO_MATCHING_EXPORTED_DEFINITION"]
        rows += ["```", ""]
        rows.append("- Relevant service relocations:")
        rows += ["", "```text"]
        rows += [line.strip() for line in call(["readelf", "-rW", str(service)]).splitlines()
                 if any(k in line for k in (SET_NAME, "registerAsService"))][:14] or ["NO_MATCH"]
        rows += ["```", ""]
        # Resolve the service's own imported backend setter and search its text
        # for calls to the exact PLT slot, independent of the misleading
        # nearest-symbol labels from GNU objdump.
        setter_relocs = [line.strip() for line in call(["readelf", "-rW", str(service)]).splitlines()
                         if re.search(r"\ba_mtktvapi_config_set_value\s*$", line)]
        rows += ["", "- Exact backend setter relocation(s):", "", "```text"]
        rows += setter_relocs or ["NOT_FOUND"]
        rows += ["```", ""]
        plt_dump = call(["arm-linux-gnueabihf-objdump", "-drw", "-j", ".plt", str(service)],
                        timeout=120, allow_error=True).splitlines()
        decoded_plts = []
        for k in range(len(plt_dump)-2):
            m0 = re.search(r"^\s*([0-9a-f]+):.*\badd\s+ip,\s*pc,\s*#(\d+)", plt_dump[k], re.I)
            if not m0:
                continue
            m1 = re.search(r"\badd\s+ip,\s*ip,\s*#(\d+)", plt_dump[k+1], re.I)
            m2 = re.search(r"\bldr\s+pc,\s*\[ip,\s*#(\d+)\]!", plt_dump[k+2], re.I)
            if m1 and m2:
                jump = int(m0.group(1),16)
                got = jump+8+int(m0.group(2))+int(m1.group(1))+int(m2.group(1))
                if any(line.lower().startswith(f"{got:08x} ") for line in setter_relocs):
                    decoded_plts.append((jump,got))
        rows.append("- Backend setter PLT decoded: " +
                    (", ".join(f"0x{plt:x} → GOT 0x{got:x}" for plt,got in decoded_plts)
                     or "NOT_FOUND"))
        # ARM PLT layout cross-check: 32-byte initial resolver header followed
        # by 16-byte stubs in the same order as R_ARM_JUMP_SLOT entries.
        # Treat this as a candidate only; validate its actual instructions.
        jump = [z.strip() for z in call(["readelf", "-rW", str(service)]).splitlines()
                if "R_ARM_JUMP_SLOT" in z]
        setter_indexes = [i for i,z in enumerate(jump)
                          if re.search(r"\ba_mtktvapi_config_set_value\s*$", z)]
        elf_sections = []
        for ln in call(["readelf", "-SW", str(service)]).splitlines():
            mm = re.match(r"^\s*\[\s*\d+\]\s+(\S+)\s+\S+\s+([0-9a-f]+)\s+([0-9a-f]+)\s+([0-9a-f]+)",ln)
            if mm:
                elf_sections.append((mm.group(1),int(mm.group(2),16),int(mm.group(4),16)))
        plt_info = next((x for x in elf_sections if x[0] == ".plt"),None)
        for ix in setter_indexes:
            rows.append(f"- Setter R_ARM_JUMP_SLOT index in service: {ix}")
            if plt_info:
                expected_plt=plt_info[1]+32+16*ix
                rows.append(f"- Index-based candidate PLT = 0x{plt_info[1]:x}+32+16*{ix} "
                            f"= 0x{expected_plt:x}; check actual jump-to-GOT below.")
                proof = call(["arm-linux-gnueabihf-objdump","-drw","-j",".plt",
                             f"--start-address=0x{expected_plt:x}",
                             f"--stop-address=0x{expected_plt+16:x}",str(service)],
                             allow_error=True)
                rows += ["", "```asm"] + proof.splitlines()[-9:] + ["```"]
                if (expected_plt, int(setter_relocs[0].split()[0],16)) in decoded_plts:
                    rows.append("- **PLT/GOT arithmetic CONFIRMED**")
                # Regardless of automatic decode, find candidate callers and
                # label as candidate, never assert if GOT check was not proven.
                if plt_info[1] <= expected_plt < plt_info[1]+plt_info[2]:
                    text_dis = call(["arm-linux-gnueabihf-objdump","-drw","-j",".text",
                                     str(service)],timeout=180,allow_error=True).splitlines()
                    direct = []
                    for k,line in enumerate(text_dis):
                        mm=re.search(r"\bblx?\s+([0-9a-f]+)\s+<",line,re.I)
                        if mm and int(mm.group(1),16)==expected_plt:
                            direct.append(" | ".join(z.strip().split("<",1)[0] for z in text_dis[max(0,k-3):k+2]))
                    rows += ["- Candidate service direct BL/BLX callsites into indexed PLT:",
                             "", "```text"]
                    rows += direct[:24] or ["NO_MATCH (indirect call possible)"]
                    rows += ["```"]

        if decoded_plts:
            text_dis = call(["arm-linux-gnueabihf-objdump", "-drw", "-j", ".text", str(service)],
                            timeout=180, allow_error=True).splitlines()
            targets = {a for a,b in decoded_plts}
            callsites = []
            for i, line in enumerate(text_dis):
                mm = re.search(r"\bblx?\s+([0-9a-f]+)\s+<", line, re.I)
                if mm and int(mm.group(1),16) in targets:
                    callsites += [" | ".join(z.strip().split("<",1)[0] for z in
                                  text_dis[max(0,i-3):i+2])]
            rows += ["- Service .text BL/BLX callsite to backend setter PLT "
                     "(unstripped function owner may not be available):", "", "```text"]
            rows += callsites[:20] or ["NO_STATIC_DIRECT_CALLSITE (possible indirect dispatch)"]
            rows += ["```"]
    rows += ["## C4. C-wrapper getService() exported helper and PLT indirection", ""]
    service_getter = next((z for z in elf_symbols(cwrap)
                           if z["name"].startswith("_Z") and "getMtkTvApiConfigService" in demangle(z["name"])
                           and z["ndx"] != "UND"), None)
    if service_getter:
        rows += ["- Service getter: " + demangle(service_getter["name"]), "", "```asm"]
        rows += selected_disassembly(cwrap, service_getter["name"], cap=120)
        rows += ["```"]
    else:
        rows += ["- Named `getMtkTvApiConfigService` not found under mangled name; "
                 "check plain C++ dynamic symbol.",
                 ""]
    rows += ["", "## C5. HIDL transaction number evidence", "",
             "Outbound BpHw setter `0x24a230 movs r1,#3` directly precedes "
             "`0x24a232 blx r6`, where r6 is obtained from the binder object's "
             "virtual slot. This strongly indicates **HIDL transaction code 3**.",
             "This numeric 3 belongs to the HIDL dispatch **and is not evidence** "
             "that the distinct Java/C argument `isUpdate=3` means persistence.",
             "Inbound BnHw onTransact dispatch should be separately cross-checked.", ""]
    inbound = next((z for z in syms if z["ndx"] != "UND" and
                    "BnHwMtkTvApiConfig" in z["name"] and "onTransact" in z["name"]), None)
    if inbound:
        rows += ["- Inbound onTransact: `" + demangle(inbound["name"]) + "`", "", "```asm"]
        rows += selected_disassembly(hidl, inbound["name"], cap=175)
        rows += ["```"]
    else:
        rows += ["- BnHw onTransact symbol unavailable for disassembly", ""]


    rows += ["", "## D. Evidence boundary (automatic, intentionally conservative)", "",
             "- The original JNI-to-C wrapper direct branch was already PROVEN_STATIC in TRACE_004.",
             "- HIDL BpHw/BnHw definitions and parcel-handling instructions are STATIC "
             "evidence, not proof that the instance is registered/running.",
             "- A matching service or implementation library filename/export is a "
             "candidate, not proof of a call edge.",
             "- `inputGroup=-1` / `isUpdate=3` are preserved as signed shorts at "
             "the C/HIDL boundary; the receiver semantics remain UNKNOWN unless separately derived.",
             "- Hardware PQ effect, source/profile routing, firmware-side update flags and "
             "persistent storage are NOT_VERIFIED without an actual receiver trace/readback.",
             "- No device operations, APK installation, Factory calls, I2C or EDID changes.",
             ""]
    args.out.mkdir(parents=True, exist_ok=True)
    output = args.out / "NATIVE_005_HIDL_RECEIVER_EVIDENCE_8054.md"
    output.write_text("\n".join(rows)+"\n", encoding="utf-8")
    print(f"NATIVE-005 evidence {output.stat().st_size} bytes; {len(targets)} HIDL methods; "
          f"{len(vtables)} vtables; {len(candidates)} service filenames")


if __name__ == "__main__":
    main()

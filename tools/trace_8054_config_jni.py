#!/usr/bin/env python3
"""NATIVE-004: bounded, reproducible, read-only exact-8054 ELF call-edge evidence.

Runs only after the owner's original archive has passed the fixed SHA-256 and
the original vendor_a EROFS has been extracted. Publishes text only; it neither
executes target code nor ships ELF bytes / arbitrary firmware strings.
"""
from __future__ import annotations

import argparse
import hashlib
import pathlib
import re
import shutil
import subprocess

EXPECTED = {
    "libcom_mediatek_twoworlds_tv_jni.so": "312fef3f17f656ac957a32dfee8187798dacac8f4df83e8219b1553b7248ab24",
    "vendor.mediatek.api.mtktvapi@1.0-cwrapper.so": "e470029a7e5d52ea25d17be2fb255e2dfe1fe75d6fe6cc527b3146001a3c8758",
    "libmediatek_tv_rpcwrapper.so": "d8938a5733c5cacd32de03eef8dafd5ea16acb9d6386bfdc7974323782b8f62b",
    "vendor.mediatek.api.mtktvapi@1.0.so": "a48159c7c66e5e82a4d293bc1e59e878cb0cfafbd8f645d872332f10424711f7",
}
JNI = "Java_com_mediatek_twoworlds_tv_TVNative_setConfigValue_1native"
SET = "a_mtktvapi_config_set_value"
ALL = "a_mtktvapi_config_set_value_for_all_elems"
GET = "a_mtktvapi_config_get_value"
FOCUS = ("config_set_value", "config_get_value", "setConfigValue_1native",
         "getConfigValue_1native", "config_set_value_for_all_elems")
CALL = re.compile(r"\b(?:bl|blx|b\.w|b|bx|blx\.w)\b", re.I)
ADDR = re.compile(r"^\s*[0-9a-f]+:", re.I)
RELOC = re.compile(r"R_ARM_(?:JUMP_SLOT|GLOB_DAT|RELATIVE|ABS32|REL32|CALL|THM_CALL)", re.I)


def run(args: list[str], timeout: int = 120) -> str:
    result = subprocess.run(args, text=True, encoding="utf-8", errors="replace",
                            stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                            timeout=timeout, check=False)
    if result.returncode:
        raise RuntimeError(f"command failed ({result.returncode}): {args[:4]}: "
                           + result.stderr[-600:])
    return result.stdout


def check_hash(path: pathlib.Path, expected: str) -> None:
    digest = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(4 * 1024 * 1024), b""):
            digest.update(chunk)
    if digest.hexdigest() != expected:
        raise RuntimeError("STOP: exact ELF digest mismatch for " + path.name)


def symbols_of(path: pathlib.Path) -> list[tuple[str, str]]:
    rows = []
    for line in run(["readelf", "-WsW", str(path)]).splitlines():
        fields = line.split()
        if len(fields) >= 8 and fields[0].endswith(":") and fields[3] in ("FUNC", "NOTYPE"):
            rows.append((fields[-1].split("@@", 1)[0], line.strip()))
    return rows


def focus_lines(lines: list[str], needle: tuple[str, ...] = FOCUS, limit: int = 42) -> list[str]:
    return [s.strip() for s in lines if any(x in s for x in needle)][:limit]


def disassemble(path: pathlib.Path, name: str) -> list[str]:
    command = ["arm-linux-gnueabihf-objdump", "-drw", "--disassemble=" + name, str(path)]
    output = run(command)
    # Truncate to a fixed number of lines, but never claim absence from truncated output.
    lines = output.splitlines()
    if len(lines) > 2400:
        raise RuntimeError("STOP: disassembly exceeded bounded output for " + path.name)
    return lines


def relevant_disassembly(lines: list[str], max_lines: int = 160) -> list[str]:
    """Function prologue + all callsites with adjacent argument setup."""
    positions = set()
    instructions = [i for i, x in enumerate(lines) if ADDR.match(x)]
    for i in instructions[:35]:
        positions.add(i)
    for i in instructions:
        s = lines[i]
        if CALL.search(s) or any(t in s for t in ("@plt", SET, GET, ALL, "getStringUTFChars", "ReleaseStringUTFChars")):
            for j in range(max(0, i - 7), min(len(lines), i + 5)):
                positions.add(j)
    selection = sorted(positions)
    if len(selection) > max_lines:
        selection = selection[:max_lines]
    return [lines[i].rstrip() for i in selection]


def named_calls(lines: list[str]) -> list[str]:
    calls = []
    for s in lines:
        if ADDR.match(s) and CALL.search(s):
            calls.append(s.strip())
    return calls[:120]


def cxx_demangle(name: str) -> str:
    if not name.startswith("_Z"):
        return name
    try:
        return run(["c++filt", name]).strip()
    except (RuntimeError, subprocess.TimeoutExpired):
        return name


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--vendor-lib", type=pathlib.Path, default=pathlib.Path("/tmp/flix8054_erofs/vendor_a_tree/lib"))
    ap.add_argument("--out", type=pathlib.Path, default=pathlib.Path("/tmp/flix8054_native_report"))
    args = ap.parse_args()
    for command in ("readelf", "arm-linux-gnueabihf-objdump", "c++filt"):
        if not shutil.which(command):
            raise RuntimeError("STOP: missing " + command)
    lib = args.vendor_lib
    for filename, digest in EXPECTED.items():
        check_hash(lib / filename, digest)
    result = [
        "# NATIVE-004 — machine evidence, exact-8054 (generated, read-only)",
        "",
        "This file is generated from **the SHA-256-verified owner firmware** on GitHub Actions.",
        "It contains selected relocation/symbol records and bounded disassembly excerpts,",
        "not a reconstructed call graph, implementation contract or runtime test.",
        "",
        "## Reproduction and tool versions",
        "",
        "\`\`\`sh",
        "python3 tools/trace_8054_config_jni.py --vendor-lib /tmp/flix8054_erofs/vendor_a_tree/lib --out /tmp/flix8054_native_report",
        "\`\`\`",
        "",
    ]
    for cmd in ("readelf", "arm-linux-gnueabihf-objdump", "c++filt"):
        result.append("- " + cmd + ": \`" + run([cmd, "--version"]).splitlines()[0] + "\`")
    result += [
        "",
        "## A. ELF inputs (contents are NOT published)",
        "",
        "| Firmware library | SHA-256 verified |",
        "|---|---|",
    ]
    for filename, digest in EXPECTED.items():
        result.append(f"| \`vendor_a/lib/{filename}\` | \`{digest}\` |")
    result += [
        "", "## B. Dynamic symbols and relocations",
        "",
        "The symbol section includes **UND imports** as well as definitions. An UND+PLT",
        "relocation and a branch from the specific JNI function establish a direct edge;",
        "DT_NEEDED by itself does not.",
    ]
    all_symbols: dict[str, list[tuple[str, str]]] = {}
    selected = [
        ("JNI", "libcom_mediatek_twoworlds_tv_jni.so"),
        ("C-wrapper", "vendor.mediatek.api.mtktvapi@1.0-cwrapper.so"),
        ("RPC-wrapper", "libmediatek_tv_rpcwrapper.so"),
        ("HIDL interface", "vendor.mediatek.api.mtktvapi@1.0.so"),
    ]
    for label, filename in selected:
        path = lib / filename
        symbols = symbols_of(path)
        all_symbols[filename] = symbols
        header = run(["readelf", "-h", str(path)])
        machine = next((x.strip() for x in header.splitlines() if "Machine:" in x), "Machine: UNKNOWN")
        deps = run(["readelf", "-d", str(path)])
        reloc = run(["readelf", "-rW", str(path)]).splitlines()
        focus_symbol_rows = [row for name, row in symbols if
                             (name == JNI or name in (SET, GET, ALL)
                              or any(x in name for x in ("config_set_valueE", "config_get_valueE",
                                 "config_set_value_for_all_elemsE", "_hidl_a_hidl_a_mtktvapi_config_set_valueE")))]
        # C++ HIDL symbols are long; limit print per library while preserving names.
        result += ["", f"### {label}: \`{filename}\`", "", f"- \`{machine}\`",
                   "- DT_NEEDED contains the following selected dependencies: " +
                   ", ".join("\`" + m + "\`" for m in re.findall(r"Shared library: \[([^]]+)\]", deps)
                             if "mtktv" in m or "hidl" in m or "rpc" in m) or "(none selected)",
                   "- Focused symbol rows (type, value, size, section, symbol):", "",
                   "\`\`\`text"]
        result += focus_symbol_rows[:45] or ["NO_MATCH"]
        result += ["\`\`\`", "", "- Relevant relocations:", "", "\`\`\`text"]
        matching_reloc = [r.strip() for r in reloc if any(x in r for x in (SET, GET, ALL))]
        result += matching_reloc[:45] or ["NO_MATCH"]
        result += ["\`\`\`"]
        if "HIDL interface" == label:
            hidl_names = [name for name, _ in symbols if "config_set_valueE" in name or "config_set_value_for_all_elemsE" in name]
            result += ["", "- ABI-derived C++ signatures from **exported** HIDL symbols:", "",
                       "\`\`\`text"]
            result += [cxx_demangle(n) for n in hidl_names[:12]] or ["NO_MATCH"]
            result += ["\`\`\`"]
    result += ["", "## C. Scoped machine disassembly", "",
               "Instruction addresses below are ELF virtual addresses, not runtime ASLR addresses.",
               "Raw calling instructions are shown as evidence; indirect register branches",
               "must not be assigned a target without a verified register/dataflow trace."]
    targets = [
        ("JNI setConfigValue", "libcom_mediatek_twoworlds_tv_jni.so",
         [JNI]),
        ("C-wrapper config set", "vendor.mediatek.api.mtktvapi@1.0-cwrapper.so",
         [SET]),
        ("RPC-wrapper config set (alternative)", "libmediatek_tv_rpcwrapper.so",
         [SET]),
    ]
    hidl_candidates = [name for name, _ in all_symbols["vendor.mediatek.api.mtktvapi@1.0.so"]
                       if "config_set_valueE" in name and ("BpHw" in name or "BnHw" in name)]
    targets.append(("HIDL proxy/stub config set", "vendor.mediatek.api.mtktvapi@1.0.so",
                    hidl_candidates[:4]))
    edge_confirmed = False
    for label, filename, names in targets:
        result += ["", "### " + label, ""]
        for name in names:
            lines = disassemble(lib / filename, name)
            calls = named_calls(lines)
            excerpt = relevant_disassembly(lines)
            if label.startswith("JNI") and any(SET in x and "@" in x for x in calls):
                edge_confirmed = True
            result += [f"- Symbol: \`{name}\`", "- Demangled: \`" + cxx_demangle(name) + "\`",
                       f"- Scoped instructions: {sum(bool(ADDR.match(s)) for s in lines)};",
                       f" call-like instructions: {len(calls)}",
                       "",
                       "\`\`\`asm"]
            result += excerpt or ["NO_SCOPED_DISASSEMBLY (review symbol/Thumb handling)"]
            result += ["\`\`\`"]
    result += ["", "## D. Automated interpretation (limited)", "",
               f"- Direct labelled JNI branch to C-wrapper setter via PLT in scoped disassembly: **{'OBSERVED' if edge_confirmed else 'NOT_OBSERVED'}**.",
               "- **NOT_OBSERVED is not proof of absence.** Inspect indirect BLX, interposition,",
               "  veneers and JNI wrappers manually before ruling out the edge.",
               "- The C-wrapper and RPC-wrapper both export similarly named functions, so",
               "  ownership cannot be inferred by symbol name alone.",
               "- JNI status mapping, JNI method static/instance declaration, inputGroup=-1,",
               "  isUpdate=3, HIDL service implementation, persisted config, and physical PQ",
               "  effect require additional callsites or device evidence.",
               "- Neither the projector nor native library functions were executed.",
               ""]
    args.out.mkdir(parents=True, exist_ok=True)
    target = args.out / "NATIVE_004_ELF_MACHINE_EVIDENCE_8054.md"
    target.write_text("\n".join(result) + "\n", encoding="utf-8")
    print(f"NATIVE-004 wrote {target}: {target.stat().st_size} bytes, JNI labelled edge={edge_confirmed}")


if __name__ == "__main__":
    main()

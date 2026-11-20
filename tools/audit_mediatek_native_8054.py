#!/usr/bin/env python3
"""Read-only, text-only native ELF inventory of the owner's exact-8054 EROFS.

Designed to run AFTER verified backup extraction in the existing backup repo.
No device connection, no write calls, no binary artifacts, no private partitions.
Reports candidate symbols and strings, NOT verified runtime functions.
"""
from __future__ import annotations

import argparse
import csv
import hashlib
import json
import pathlib
import re
import subprocess
from collections import Counter

PARTS = ("system_a", "vendor_a", "product_a", "system_ext_a")
NAME_RE = re.compile(
    r"(?:mediatek|mtk|tv|pq|picture|video|audio|sound|display|mjc|memc|hdmi|cec|"
    r"factory|hidl|thermal|fan|project|trd|native|jni)", re.I
)
SYMBOL_RE = re.compile(
    r"(?:^Java_(?:com_mediatek|vendor_mediatek|mediatek)|"
    r"TVNative|MtkTv|mtktv|setConfigValue|getConfigValue|"
    r"getVideoInfoData|RegisterNatives|setPeq|getPeq|"
    r"factory|hdmi_cec)", re.I
)
KEY_RE = re.compile(
    r"^(?:g_(?:video|audio|disp|fusion_sound|fusion_picture)__[A-Za-z0-9_]{3,85}"
    r"|Java_(?:com_mediatek|vendor_mediatek|mediatek)_[A-Za-z0-9_]{5,160}"
    r"|vendor\.mediatek\.tv\.[A-Za-z0-9_.@-]{3,120})$"
)
TARGET_CONFIG = {
    "g_video__contrast", "g_video__brightness", "g_video__vid_sat",
    "g_video__vid_hdr", "g_video__vid_mjc_effect",
    "g_video__clr_temp", "g_audio__sound_mode",
    "g_fusion_sound__equalizer_120hz",
    "g_fusion_picture__hdmi_edid_version",
}
MAX_KEY_STRINGS_PER_LIB = 80
MAX_SYMBOLS_PER_LIB = 120
# Prioritize the actual UI/config call path over broad, unrelated TV API names.
PRIORITY_SYMBOL_RE = re.compile(
    r"(?:^Java_com_mediatek_twoworlds_tv_TVNative_.*(?:[Cc]onfig|[Vv]ideoInfo)"
    r"|a_mtktvapi_config_(?:get|set)[A-Za-z0-9_]*"
    r"|a_mtktvfapi_.*(?:[Pp]eq|[Mm]jc|[Hh]dmi|[Cc]ec|[Pp]icture|[Cc]olor))",
    re.I,
)


def sha256_file(path: pathlib.Path) -> str:
    sha = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(4 * 1024 * 1024), b""):
            sha.update(chunk)
    return sha.hexdigest()


def execute(argv: list[str], timeout: int = 120) -> tuple[int, str]:
    try:
        run = subprocess.run(
            argv, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
            timeout=timeout, text=True, errors="replace", check=False
        )
        return run.returncode, run.stdout
    except (OSError, subprocess.TimeoutExpired):
        return 127, ""


def elf_metadata(path: pathlib.Path) -> tuple[str, list[str], list[str]]:
    code, head = execute(["readelf", "-h", str(path)])
    if code:
        return "not-ELF", [], []
    machine = next(
        (line.split(":", 1)[-1].strip()
         for line in head.splitlines() if "Machine:" in line), "unknown"
    )
    _, dynamic = execute(["readelf", "-d", str(path)])
    deps = sorted(set(re.findall(r"Shared library: \[([^]]+)\]", dynamic)))
    _, symbols = execute(["readelf", "-Ws", "--wide", str(path)], timeout=180)
    candidates = set()
    for line in symbols.splitlines():
        fields = line.split()
        if len(fields) < 8 or not fields[0].endswith(":"):
            continue
        name = fields[-1].split("@@", 1)[0]
        if fields[6] == "UND":
            continue
        if SYMBOL_RE.search(name):
            candidates.add(name[:240])
    ordered = sorted(candidates, key=lambda s: (
        0 if PRIORITY_SYMBOL_RE.search(s) else 1,
        0 if "setConfigValue" in s or "getConfigValue" in s else 1, s))
    return machine, deps, ordered[:MAX_SYMBOLS_PER_LIB]


def selected_strings(path: pathlib.Path) -> list[str]:
    # Only strict static key/descriptor/JNI-name allowlist; never dump arbitrary
    # strings from firmware where device credentials could otherwise appear.
    try:
        proc = subprocess.Popen(
            ["strings", "-a", "-n", "8", str(path)],
            stdout=subprocess.PIPE, stderr=subprocess.DEVNULL,
            text=True, errors="replace"
        )
    except OSError:
        return []
    results = []
    try:
        assert proc.stdout is not None
        for line in proc.stdout:
            candidate = line.strip()
            if KEY_RE.fullmatch(candidate) and candidate not in results:
                results.append(candidate)
            if len(results) >= MAX_KEY_STRINGS_PER_LIB:
                proc.terminate()
                break
        proc.communicate(timeout=60)
    except (subprocess.TimeoutExpired, OSError):
        proc.kill()
        proc.communicate()
    return results


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=pathlib.Path, default=pathlib.Path("/tmp/flix8054_erofs"))
    parser.add_argument("--out", type=pathlib.Path, default=pathlib.Path("/tmp/mtk_native_audit"))
    args = parser.parse_args()
    args.out.mkdir(parents=True, exist_ok=True)
    trees = [(part, args.root / (part + "_tree")) for part in PARTS]
    missing = [str(tree) for _, tree in trees if not tree.is_dir()]
    if missing:
        raise SystemExit("STOP: missing exact EROFS trees: " + ", ".join(missing))
    files = [
        (part, tree, file)
        for part, tree in trees
        for file in sorted(tree.rglob("*"))
        if file.is_file() and not file.is_symlink()
        and (file.name.endswith(".so") or re.search(r"\.so\.\d+$", file.name))
    ]
    if not files:
        raise SystemExit("STOP: no native libraries found in verified EROFS")
    inventory = []
    selected = []
    for part, tree, file in files:
        path = file.relative_to(tree).as_posix()
        candidate = bool(NAME_RE.search(file.name))
        size = file.stat().st_size
        inventory.append({
            "partition": part, "path": path, "size_bytes": size,
            "selected_for_analysis": str(candidate).lower()
        })
        if not candidate:
            continue
        machine, deps, symbols = elf_metadata(file)
        strings = selected_strings(file)
        selected.append({
            "partition": part, "path": path, "size_bytes": size,
            "sha256": sha256_file(file), "machine": machine,
            "dt_needed": deps, "static_candidate_exports": symbols,
            "allowlisted_key_strings": strings,
            "matched_target_config_keys": sorted(TARGET_CONFIG.intersection(strings)),
            "evidence_level": "STATIC_ELF_ONLY_NOT_RUNTIME"
        })
    with (args.out / "ELF_LIBRARY_INVENTORY_8054.csv").open("w", newline="", encoding="utf-8") as stream:
        csvw = csv.DictWriter(stream, fieldnames=list(inventory[0]))
        csvw.writeheader()
        csvw.writerows(inventory)
    (args.out / "NATIVE_API_CANDIDATES_8054.json").write_text(
        json.dumps(selected, indent=2, ensure_ascii=False) + "\n", encoding="utf-8"
    )
    counts = Counter(row["partition"] for row in inventory)
    with_exports = [r for r in selected if r["static_candidate_exports"]]
    with_config = [r for r in selected if r["allowlisted_key_strings"]]
    lines = [
        "# Exact-8054 native MediaTek ELF audit (static)",
        "",
        "## Source and confidence",
        "Original owner release `MAX-TAC/FlixTone_8054_Backup` SHA-256 checked "
        "by GitHub Actions before extraction of exact super.img/EROFS.",
        "This is a read-only *static* inventory of system/vendor/product/system_ext binaries. "
        "No runtime/JNI registration, Binder transaction, HAL effectiveness or physical "
        "feature availability is claimed.",
        "",
        "## Counts",
        f"- Native .so files: **{len(inventory)}**",
        f"- Filename-selected candidate libraries: **{len(selected)}**",
        f"- Candidates with relevant dynamic exported symbols: **{len(with_exports)}**",
        f"- Candidates containing strictly allowlisted key/JNI literals: **{len(with_config)}**",
        "- Partitions: " + ", ".join(f"{p}={counts[p]}" for p in PARTS),
        "",
        "## Candidate libraries (non-secret metadata)",
        "| Partition | Library | ELF machine | JNI/MediaTek exports | Config/JNI strings |",
        "|---|---|---|---:|---:|",
    ]
    for record in selected:
        lines.append(
            f"| {record['partition']} | `{record['path']}` | {record['machine']} | "
            f"{len(record['static_candidate_exports'])} | "
            f"{len(record['allowlisted_key_strings'])} |"
        )
    lines.extend([
        "", "## Next steps",
        "1. Resolve `TVNativeWrapper.setConfigValue_native` to the exact native "
        "registration or JNI export, including any HIDL/Binder hops.",
        "2. Follow the receiver of `inputGroup=-1`, `isUpdate=3` and "
        "establish whether it means active source or a broader profile scope.",
        "3. Repeat for HDR/MEMC and audio EQ in source-order, preserving exact "
        "type signatures and dependencies.",
        "4. Keep factory calibration/thermal actuator calls out of user MOD until "
        "individual firmware-specific contracts and rollback exist.",
        "",
        "### Artifacts",
        "- `ELF_LIBRARY_INVENTORY_8054.csv`: all discovered shared libraries.",
        "- `NATIVE_API_CANDIDATES_8054.json`: selected SHA-256, DT_NEEDED, "
        "static symbols and strictly allowlisted literal matches.",
        "",
        "**Do not import APIs into production MOD solely from this report.**",
    ])
    (args.out / "NATIVE_API_INVENTORY_8054.md").write_text(
        "\n".join(lines) + "\n", encoding="utf-8"
    )
    print(json.dumps({
        "libraries": len(inventory), "candidates": len(selected),
        "with_exports": len(with_exports), "with_strings": len(with_config),
        "out": str(args.out)
    }, ensure_ascii=False))


if __name__ == "__main__":
    main()

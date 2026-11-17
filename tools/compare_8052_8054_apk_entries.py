#!/usr/bin/env python3
"""Read-only ZIP entry comparison of original 8052/8054 APK pairs."""
import hashlib
import json
import sys
import zipfile
from pathlib import Path


def inventory(apk):
    entries = {}
    with zipfile.ZipFile(apk) as archive:
        for info in archive.infolist():
            if info.is_dir():
                continue
            if info.filename in entries:
                raise ValueError(f"duplicate APK entry {info.filename}: {apk}")
            data = archive.read(info)
            entries[info.filename] = {
                "sha256": hashlib.sha256(data).hexdigest(),
                "size": len(data),
            }
    return entries


def category(name):
    if name == "AndroidManifest.xml":
        return "manifest"
    if name == "resources.arsc" or name.startswith("res/"):
        return "resources"
    if name.startswith("classes") and name.endswith(".dex"):
        return "dex"
    if name.startswith("META-INF/"):
        return "signature"
    if name.startswith("lib/"):
        return "native_lib"
    return "other"


def compare(old, new):
    paths = sorted(set(old) | set(new))
    changed = []
    counts = {}
    for name in paths:
        before, after = old.get(name), new.get(name)
        if before == after:
            continue
        change = "ADDED" if before is None else "REMOVED" if after is None else "CHANGED"
        kind = category(name)
        counts.setdefault(kind, {"ADDED": 0, "REMOVED": 0, "CHANGED": 0})[change] += 1
        changed.append({
            "path": name,
            "category": kind,
            "change": change,
            "old_sha256": before["sha256"] if before else None,
            "new_sha256": after["sha256"] if after else None,
            "old_size": before["size"] if before else None,
            "new_size": after["size"] if after else None,
        })
    return {"old_entries": len(old), "new_entries": len(new),
            "category_counts": counts, "changed_entries": changed}


def main(config_file, output_dir):
    config = json.loads(Path(config_file).read_text(encoding="utf-8"))
    report = {"meaning": "Binary ZIP entry differences only; no decoded Manifest or resource semantics",
              "pairs": []}
    for pair in config["pairs"]:
        old_apk, new_apk = Path(pair["original_apk"]), Path(pair["target_apk"])
        old, new = inventory(old_apk), inventory(new_apk)
        item = {"apk": pair["apk"],
                "old_apk_sha256": hashlib.sha256(old_apk.read_bytes()).hexdigest(),
                "new_apk_sha256": hashlib.sha256(new_apk.read_bytes()).hexdigest(),
                **compare(old, new)}
        for key, actual in (("expected_original_sha256", item["old_apk_sha256"]),
                            ("expected_target_sha256", item["new_apk_sha256"])):
            if pair.get(key) and pair[key] != actual:
                raise ValueError(f"{pair['apk']}: {key} mismatch")
        report["pairs"].append(item)
        print("ENTRY_DIFF", pair["apk"], "entries", item["old_entries"],
              item["new_entries"], "changed", len(item["changed_entries"]),
              "by_category", json.dumps(item["category_counts"], sort_keys=True))
        for entry in item["changed_entries"]:
            if entry["category"] in ("manifest", "resources"):
                print("ENTRY_DETAIL", pair["apk"], entry["category"],
                      entry["change"], entry["path"])
    target = Path(output_dir)
    target.mkdir(parents=True, exist_ok=True)
    (target / "apk_entry_diff.json").write_text(
        json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    rows = ["# Original APK 8052 ↔ 8054: ZIP entry diff", "",
            "Binary entry hashes only. Changed Manifest/resources need a decoded semantic diff.",
            "", "| APK | 8052 entries | 8054 entries | Changed | Manifest binary | Resource entries changed |",
            "|---|---:|---:|---:|---|---:|"]
    for item in report["pairs"]:
        manifest = next((x["change"] for x in item["changed_entries"]
                         if x["path"] == "AndroidManifest.xml"), "SAME")
        resources = sum(1 for x in item["changed_entries"]
                        if x["category"] == "resources")
        rows.append(f"| {item['apk']} | {item['old_entries']} | {item['new_entries']} | "
                    f"{len(item['changed_entries'])} | {manifest} | {resources} |")
    rows.extend(["", "Exact paths and SHA-256 values are in `apk_entry_diff.json`.", ""])
    (target / "apk_entry_diff.md").write_text("\n".join(rows), encoding="utf-8")


def self_test():
    from tempfile import TemporaryDirectory
    with TemporaryDirectory(prefix="apk-entries-") as directory:
        old, new = Path(directory) / "a.apk", Path(directory) / "b.apk"
        with zipfile.ZipFile(old, "w") as archive:
            archive.writestr("AndroidManifest.xml", b"one")
            archive.writestr("res/layout/a.xml", b"same")
        with zipfile.ZipFile(new, "w") as archive:
            archive.writestr("AndroidManifest.xml", b"two")
            archive.writestr("res/layout/a.xml", b"same")
            archive.writestr("res/layout/b.xml", b"new")
        result = compare(inventory(old), inventory(new))
        assert len(result["changed_entries"]) == 2
        assert result["category_counts"]["manifest"]["CHANGED"] == 1
        assert result["category_counts"]["resources"]["ADDED"] == 1
        print("PASS APK entry diff self-test")


if __name__ == "__main__":
    if len(sys.argv) == 2 and sys.argv[1] == "--self-test":
        self_test()
    elif len(sys.argv) == 3:
        main(sys.argv[1], sys.argv[2])
    else:
        raise SystemExit("Usage: compare_8052_8054_apk_entries.py <config.json> <output-dir> | --self-test")

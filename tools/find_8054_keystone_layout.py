"""Resolve the exact stock layout for the movement callback's OEM widgets."""
import re
import subprocess
import sys
import zipfile
from pathlib import Path


WIDGETS = (
    "tsv_keystone_move_auto",
    "tsv_keystone_auto_screen",
    "tsv_keystone_auto_obstacle_avoidance",
    "tsv_keystone_vertical",
)


def aapt(*args):
    return subprocess.check_output(["aapt", "dump", *args], text=True)


def find_layout(apk):
    resources = aapt("--values", "resources", apk)
    ids = {
        name: value.lower()
        for value, name in re.findall(r"(?:spec )?resource (0x[0-9a-f]+) \S+:id/([\w]+)", resources)
    }
    if any(name not in ids for name in WIDGETS):
        return ""
    # Link actual XML paths (including obfuscated filenames) to layout names.
    by_path = {}
    name = None
    for line in resources.splitlines():
        if re.search(r"\bresource 0x", line):
            match = re.search(r"\bresource 0x[0-9a-f]+ \S+:layout/([\w]+)", line)
            name = match[1] if match else None
        if name:
            for path in re.findall(r'"(res/[^"]+\.xml)"', line):
                by_path[path] = name
    with zipfile.ZipFile(apk) as bundle:
        for path in bundle.namelist():
            match = re.fullmatch(r"res/layout(?:-[^/]+)?/(\w+)\.xml", path)
            if match:
                by_path.setdefault(path, match[1])

    candidates = set()
    for path, name in by_path.items():
        if "keystone" not in name.lower() and "projector" not in name.lower():
            continue
        try:
            xml = aapt("xmltree", apk, path)
        except subprocess.CalledProcessError:
            continue
        widget_ids = set(re.findall(r"android:id\([^)]*\)=@(0x[0-9a-f]+)", xml.lower()))
        if all(ids[widget] in widget_ids for widget in WIDGETS):
            candidates.add(name)
    print("Stock movement keystone layout candidates:", sorted(candidates))
    return next(iter(candidates)) if len(candidates) == 1 else ""


if __name__ == "__main__":
    apk, output = sys.argv[1:]
    layout = find_layout(apk)
    Path(output).write_text(
        "package dev.projectorsoft.tvsettings;\n"
        "public final class Exact8054ProjectionResources {\n"
        f'    public static final String KEYSTONE_LAYOUT = "{layout}";\n'
        "}\n", encoding="utf-8"
    )
    print("PRJ05 stock layout:", layout or "unavailable; retain OEM settings")

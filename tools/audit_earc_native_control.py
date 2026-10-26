#!/usr/bin/env python3
"""Inspect the *actual* OEM eARC source-to-HAL behavior (no guessed toggle).
Only bounded source excerpts into CI log; committed metadata is identifiers/calls.
"""
from pathlib import Path
import json,re,sys
src=Path(sys.argv[1])/"sources"
out=Path(sys.argv[2]);out.parent.mkdir(parents=True,exist_ok=True)
root=src/"com/android/tv/settings"
candidates=[]
for sub in ["partnercustomizer","uicustom"]:
    top=root/sub
    for f in top.rglob("*.java"):
        # Restrict by name OR containing eARC hardware control, avoid broad dump
        if re.search(r"sound|earc|audio|source",f.name,re.I):
            s=f.read_text(errors="replace")
            if re.search(r"setEarcEnable|KEY_SOUND_EARC|isSupportEARC",s):
                candidates.append(f)
report={}
for f in candidates:
    s=f.read_text(errors="replace");lines=s.splitlines(); excerpts=[]
    for i,ln in enumerate(lines):
        if not re.search(r"setEarcEnable|KEY_SOUND_EARC|isSupportEARC",ln):continue
        first=max(0,i-8);last=min(len(lines),i+14)
        print("=====",str(f.relative_to(src)),"line",i+1,"====\n",
              "\n".join(f"{n+1} {lines[n]}" for n in range(first,last)),flush=True)
        excerpts.append({"line":i+1,"statement":ln.strip()[:200],
                         "calls":re.findall(r"\b(?:get|set|is|put)\w+\(",ln)[:12]})
    report[str(f.relative_to(src))]=excerpts[:90]
print("eARC containing classes:",len(report))
out.write_text(json.dumps(report,indent=2,ensure_ascii=False)+"\n")

#!/usr/bin/env python3
"""Read-only source-level ARC/eARC follow-up audit for OEM 8052 TvSettingsPlus."""
from pathlib import Path
import json,re,sys
src=Path(sys.argv[1])/"sources"
out=Path(sys.argv[2])
out.parent.mkdir(parents=True,exist_ok=True)
patterns=("isSupportEARC(","setEarcEnable(","KEY_SOUND_EARC","sound_earc",
          "HDMI_ARC_CONTROL_ENABLED","SoundEARCReceiver","SpkOutputMode=",
          "setHdmiCecControlEnabled(","setSystemAudioMode(","setArcEnabled(")
allmatch={p:[] for p in patterns}
hits={}
for f in sorted(src.rglob("*.java")):
    try:s=f.read_text(errors="replace")
    except OSError:continue
    if not any(p in s for p in patterns):continue
    name=str(f.relative_to(src))
    lines=s.splitlines()
    for i,line in enumerate(lines):
        found=[p for p in patterns if p in line]
        if not found:continue
        for p in found:
            allmatch[p].append({"class":name,"line":i+1})
        # Capture bounded snippets for exact OEM on/off, hardware gate,
        # producer/consumer of eARC setting, and ARC routing.
        if (name.startswith("com/android/tv/settings/") or
            name.startswith("com/mediatek/")):
            if len(hits.get(name,[]))<30:
                lo=max(0,i-4);hi=min(len(lines),i+6)
                hits.setdefault(name,[]).append({
                    "line":i+1,
                    "excerpt":[f"{n+1} {lines[n].strip()[:250]}" for n in range(lo,hi)]
                })
result={"source":"8052 factory TvSettingsPlus.apk",
        "patterns":{p:{"count":len(v),"first":v[:80]} for p,v in allmatch.items()},
        "selected_source_excerpts":hits}
out.write_text(json.dumps(result,ensure_ascii=False,indent=2)+"\n")
print("SUMMARY")
for p,v in allmatch.items():
    print(p, len(v), "; ".join(x["class"]+":"+str(x["line"]) for x in v[:12]))
print("\nSPECIALIZED EVIDENCE")
for name,items in hits.items():
    if re.search("PreferenceConfigUtils|SoundEARCReceiver|EarcFragment|SoundSubMenuController|SoundFragment|SourceFragment|SoundOutput|Hdmi",name,re.I):
        print("\n###",name)
        for item in items:
            print("\n".join(item["excerpt"]))
            print("---")

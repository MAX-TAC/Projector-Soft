#!/usr/bin/env python3
from pathlib import Path
import json,re,sys
root=Path(sys.argv[1])/"sources"
out=Path(sys.argv[2]);out.parent.mkdir(parents=True,exist_ok=True)
base=root/"com/android/tv/settings/partnercustomizer/sound"
paths=[root/"com/android/tv/settings/partnercustomizer/sound/equalizerdetail/EqualizerDetailFragment.java"]
paths+=list(base.rglob("*Equaliz*.java"))
paths+=list(base.rglob("*Sound*.java"))[:30]
seen=set(); report={}
for path in paths:
 if path in seen or not path.exists():continue
 seen.add(path)
 src=path.read_text(errors="replace")
 print("\n###",path.relative_to(root),"source lines",len(src.splitlines()),"\n",src[:32000],flush=True)
 report[str(path.relative_to(root))]={"length":len(src),"extends":re.findall(r"class \w+ extends ([\w.]+)",src)[:3],
  "callables":sorted(set(re.findall(r"\b(?:set|get|apply|on|update|init)[A-Za-z0-9_]+\(",src)))[:150],
  "ints":sorted(set(re.findall(r"R\.id\.([A-Za-z0-9_]+)",src)))[:80],
  "strings":sorted(set(re.findall(r'"([^"]{3,100})"',src)))[:80]}
out.write_text(json.dumps(report,ensure_ascii=False,indent=2)+"\n")

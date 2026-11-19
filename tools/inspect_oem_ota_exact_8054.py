#!/usr/bin/env python3
"""Deep inspect selected read-only updater source sections; no runtime installation."""
from pathlib import Path
import sys
src=Path(sys.argv[1])/"sources/com/rjpj/yunota"
windows={
"AbUpdate.java":[(195,267),(390,550),(550,632)],
"OTASystemLocalUpdateActivity.java":[(30,116),(116,156),(211,259)],
"OTATools.java":[(75,115),(155,260)],
"Configs.java":[(1,20)],
}
for f,spans in windows.items():
 p=src/f
 print("\n==========",f,"exists",p.exists(),"==========",flush=True)
 if not p.exists():continue
 lines=p.read_text(errors="replace").splitlines()
 for a,b in spans:
  print("\n----",a,b,"----",flush=True)
  for i in range(a-1,min(b,len(lines))):
   print(f"{i+1}: {lines[i][:420]}",flush=True)

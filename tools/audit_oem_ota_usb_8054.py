#!/usr/bin/env python3
"""Read-only OEM updater flow inspection. Does NOT call/install/update any firmware."""
from pathlib import Path
import re,sys,json
root=Path(sys.argv[1])/"sources/com/rjpj/yunota"
out=Path(sys.argv[2])
files=["Configs.java","OTATools.java","AbUpdate.java","OTASystemLocalUpdateActivity.java",
       "OTASystemNetworkUpdateActivity.java","FileSelector.java","OTACheckService.java"]
pat=re.compile(r'applyPayload|applyUpdate|verify|Verify|MD5|md5|check|Check|signature|Sign|reboot|Reboot|systemUpdate|zip|Zip|pkg|Pkg|Bin|bin|local|Local|upgrade|Upgrade|UpdateEngine|metadata|payload|install|Install|SKIP_|UPDATE_|external|usb|USB|sdcard|sdCard|SDCard|file\.get|Environment|getYun',re.I)
result={}
for file in files:
 p=root/file
 if not p.exists():
  print("NOT FOUND",file,flush=True)
  continue
 lines=p.read_text(errors="replace").splitlines()
 hits=[n for n,line in enumerate(lines) if pat.search(line)]
 segments=[]
 for n in hits:
  a=max(0,n-5);b=min(len(lines),n+10)
  if segments and a<=segments[-1][1]:
   segments[-1]=(segments[-1][0],max(b,segments[-1][1]))
  else:segments.append((a,b))
 shown=0
 evidence=[]
 print("\n=== CLASS",file,"LINES",len(lines),"HITS",len(hits),"===",flush=True)
 for a,b in segments:
  if shown>420:break
  b=min(b,a+65)
  print("LINES",a+1,"-",b,flush=True)
  for i in range(a,b):
   print(f"{i+1}: {lines[i][:320]}",flush=True)
  shown+=(b-a)
  evidence.append({"from":a+1,"to":b,
      "matching_lines":[{"line":i+1,"text":lines[i][:350]} for i in range(a,b) if pat.search(lines[i])][:65]})
 result[file]=evidence
out.parent.mkdir(parents=True,exist_ok=True)
out.write_text(json.dumps(result,indent=2,ensure_ascii=False)+"\n")

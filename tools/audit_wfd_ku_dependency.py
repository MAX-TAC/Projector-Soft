#!/usr/bin/env python3
"""Report precise WFD -> KuSettings Bluetooth Activity call-site metadata (no source code)."""
import json,re,sys
from pathlib import Path
root=Path(sys.argv[1])
out=Path(sys.argv[2])
out.parent.mkdir(parents=True,exist_ok=True)
marker="com.my.settings.bluetooth.MyAutoBluetoothActivity"
results=[]
pattern=re.compile(r'(?m)^\s*(?:public|private|protected)\s+(?:static\s+|final\s+|synchronized\s+)*[\w<>\[\],.? ]+\s+(\w+)\s*\(')
for file in root.rglob("*.java"):
    text=file.read_text(errors="replace")
    pos=0
    while (pos:=text.find(marker,pos))>=0:
        before=text[:pos]
        fnames=list(pattern.finditer(before))
        method=fnames[-1].group(1) if fnames else "unresolved"
        section=text[max(0,pos-600):pos+900]
        symbols=sorted(set(re.findall(
            r"\b(?:setClassName|setComponent|setPackage|setAction|startActivity|startService|"
            r"sendBroadcast|resolveActivity|FLAG_ACTIVITY_[A-Z_]+|BluetoothAdapter|Intent)\b",
            section)))
        results.append({"relative_file":file.relative_to(root).as_posix(),
                        "method_guess":method,"line":text.count("\n",0,pos)+1,
                        "surrounding_api_symbols":symbols})
        pos+=len(marker)
payload={"marker":marker,"occurrences":results,
         "scope":"JADX static only; dynamic intent behavior must be confirmed"}
out.write_text(json.dumps(payload,ensure_ascii=False,indent=2)+"\n")
print(json.dumps(payload,ensure_ascii=False,indent=2))

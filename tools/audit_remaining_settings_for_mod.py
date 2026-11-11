#!/usr/bin/env python3
"""Extract call, key and field evidence for the remaining OEM settings migration.
No original decompiled Java is committed or uploaded.
"""
import json, re, sys
from pathlib import Path

root=Path(sys.argv[1])
targets={
 "KuSettings":[
  "com/my/settings/bluetooth/MyAutoBluetoothActivity.java",
  "com/my/settings/bluetooth/MyBluetoothActivity.java",
  "com/my/settings/language/LanguageLocalActivity.java",
  "com/my/settings/time/MyZonePicker.java",
 ],
 "TvSettingsPlus":[
  "com/android/tv/settings/partnercustomizer/tvsettingservice/BluetoothReconnectService.java",
  "com/android/tv/settings/partnercustomizer/tvsettingservice/BluetoothReconnectService$1.java",
  "com/android/tv/settings/uicustom/fragment/sound/SoundFragment.java",
  "com/android/tv/settings/uicustom/floatmenu/controller/sound/SoundSubMenuController.java",
  "com/android/tv/settings/uicustom/fragment/general/GeneralFragment.java",
  "com/android/tv/settings/uicustom/floatmenu/controller/general/GeneralSubMenuController.java",
  "com/android/tv/settings/system/TimeZoneFragment.java",
 ]}
pattern=re.compile(r"(?i)(onSwitchChange|Keypress|keyTone|key_tone|sound_effect|BluetoothReconnect|connectDevice|connectFinsh|autoConnect|connectGatt|connect\(|bluetooth|time_zone|timeZone|zonePicker|SCREEN_OFF_TIMEOUT|screensaver|font_scale|PowerOnUp|putElectricityBoot|Settings\.Global|Settings\.System|updateLocale|setTimeZone|locale|showScreenSaver)")
output={}
for package,paths in targets.items():
 for rel in paths:
  p=root/package/"sources"/rel
  if not p.exists():
   candidate=list((root/package/"sources").rglob(Path(rel).name))
   if len(candidate)!=1:
    output[package+":"+rel]={"found":False,"matches":len(candidate)}
    continue
   p=candidate[0]
  lines=p.read_text(errors="replace").splitlines()
  hits=[]
  for i,ln in enumerate(lines):
   if pattern.search(ln):
    excerpt=ln.strip()
    excerpt=re.sub(r'(?i)(bluetoothName|deviceName|address)=([A-F0-9:]{11,})',r'\1=[redacted]',excerpt)
    hits.append([i+1,excerpt[:440]])
  strings=sorted(set(re.findall(r'"([A-Za-z][\w.:-]{2,100})"', "\n".join(lines))))
  output[package+":"+rel]={"found":True,"line_count":len(lines),
     "matches":hits[:100],"literals":[x for x in strings if pattern.search(x)][:55]}
print(json.dumps(output,indent=2,ensure_ascii=False))

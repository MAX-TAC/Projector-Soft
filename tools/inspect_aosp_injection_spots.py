#!/usr/bin/env python3
from pathlib import Path
import sys,re
root=Path(sys.argv[1])/"sources"
targets=[
"com/android/tv/settings/MainSettings.java",
"com/android/tv/settings/MainFragment.java",
"com/android/tv/settings/SettingsPreferenceFragment.java",
"com/android/tv/settings/TvSettingsPreferenceFragment.java",
"com/android/tv/settings/device/DevicePrefFragment.java",
"com/android/tv/settings/device/displaysound/DisplaySoundFragment.java",
"com/android/tv/settings/uicustom/MainActivity.java",
"com/android/tv/twopanelsettings/TwoPanelSettingsFragment.java",
"com/android/tv/twopanelsettings/TwoPanelSettingsActivity.java"
]
for rel in targets:
 f=root/rel
 if not f.exists():
  print("MISSING",rel);continue
 s=f.read_text(errors="replace").splitlines()
 print("\n\n==============",rel,"lines",len(s),"==============")
 print("\n".join(f"{i+1}: {x}" for i,x in enumerate(s[:155])))

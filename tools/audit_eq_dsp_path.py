#!/usr/bin/env python3
"""Audit OEM equalizer bandwidth keys, range, audio HAL side effects and feature gating.
Stores no proprietary source, only signatures and static identifiers.
"""
from pathlib import Path
import json,re,sys
root=Path(sys.argv[1])/"sources"
out=Path(sys.argv[2]);out.parent.mkdir(parents=True,exist_ok=True)
rels=[
"com/android/tv/settings/partnercustomizer/tvsettingservice/util/PreferenceConfigUtilsBase.java",
"com/android/tv/settings/partnercustomizer/tvsettingservice/util/PreferenceConfigUtils.java",
"com/android/tv/settings/partnercustomizer/utils/ProgressDialogActivity.java",
"com/android/tv/settings/partnercustomizer/utils/ProgressDialogPreference.java",
"com/mediatek/wwtv/tvcenter/util/PartnerSettingsConfig.java",
"com/android/tv/settings/partnercustomizer/sound/equalizerdetail/EqualizerDetailFragment.java",
]
res={}
terms=re.compile(r"equaliz|sound_|progress|setSettingValueInt|updatePreferenceChanged|setBandLevel|setAudio|dsp|audio|setting",re.I)
for rel in rels:
 p=root/rel
 if not p.exists():print("MISSING",rel);continue
 lines=p.read_text(errors="replace").splitlines()
 found=[]
 for i,ln in enumerate(lines):
  if not terms.search(ln):continue
  if "class " in ln or i>0 and len(ln.strip())<10:continue
  if i<100 and "PreferenceConfigUtilsBase.java" in rel and not re.search(r"KEY_EQUALIZER|KEY_SOUND",ln):continue
  if any(w in rel for w in ("ProgressDialogActivity","ProgressDialogPreference","EqualizerDetailFragment")) and i<300:
   pass
  snippet="\n".join(lines[max(0,i-2):min(len(lines),i+4)])
  found.append({"at":i+1,"text":ln.strip()[:260]})
  if len(found)<=150:print("###",rel,"line",i+1,"\n",snippet,flush=True)
 if "ProgressDialogActivity" in rel or "ProgressDialogPreference" in rel:
  print("### Full selected class",rel,"\n","\n".join(lines[:230]),flush=True)
 res[rel]={"matches":found[:280],"totalLines":len(lines)}
out.write_text(json.dumps(res,ensure_ascii=False,indent=2)+"\n")

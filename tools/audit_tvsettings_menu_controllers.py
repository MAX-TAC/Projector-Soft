#!/usr/bin/env python3
"""Audits original OEM TvSettingsPlus menu controllers: factual metadata only.
No source decompiled OEM source code is committed; no code is modified.
"""
from pathlib import Path
import re,sys,json,collections
root,out=map(Path,sys.argv[1:])
names=[
"floatmenu/controller/general/BrightnessModeSettingsSecondaryController",
"floatmenu/controller/general/GeneralSubMenuController",
"floatmenu/controller/picture/PictureSubMenuController",
"floatmenu/controller/picture/PictureModeValueSecondaryController",
"floatmenu/controller/picture/ColorTempValueSecondaryController",
"floatmenu/controller/picture/AdvanceSettingsSecondaryController",
"floatmenu/controller/sound/SoundSubMenuController",
"floatmenu/controller/sound/SoundModeValueSecondaryController",
"floatmenu/controller/source/SourceSubMenuController",
"floatmenu/controller/source/PowerOnSourceSecondaryController",
"floatmenu/controller/source/PowerOnAppSecondaryController",
"floatmenu/controller/system/SystemSubMenuController",
"floatmenu/controller/system/SystemInfoSecondaryController",
"floatmenu/controller/projector/ProjectorSubMenuController",
"floatmenu/controller/projector/FocusSettingsSecondaryController",
"floatmenu/controller/projector/KeystoneSettingsSecondaryController",
"floatmenu/controller/projector/ProjectionModeSecondaryController",
"fragment/general/BrightnessModeSettingsFragment",
"fragment/general/GeneralFragment",
"fragment/picture/PictureFragment",
"fragment/sound/SoundFragment",
"fragment/projector/ProjectorFragment",
"fragment/source/SourceFragment",
"fragment/system/SystemFragment",
]
src=root/"sources"; docs={}
methodrx=re.compile(r"(?m)^\s*(?:public|private|protected)\s+(?:(?:static|final|synchronized|native|abstract)\s+)*[\w<>\[\].,$? ]+\s+([\w$]+)\s*\([^;{}]{0,300}\)\s*(?:throws [\w., ]+)?\{")
for target in names:
 path=next(iter(src.rglob(target.split("/")[-1]+".java")),None)
 if path is None:
  docs[target]={"present":False};continue
 text=path.read_text(errors="replace")
 methods=collections.Counter(methodrx.findall(text))
 ids=sorted(set(re.findall(r"R\.id\.([\w_]+)",text)))
 stringres=sorted(set(re.findall(r"R\.string\.([\w_]+)",text)))
 calls=sorted(set(re.findall(r"\b(?:[\w$.]+\.)?([\w$]+)\s*\(",text)))
 actions=sorted(set(re.findall(r'"((?:android\.settings\.|com\.action\.|persist\.|tv_)[^"]{0,120})"',text)))
 interesting=sorted(x for x in calls if re.search(r"bright|light|focus|key|mjc|hdr|sound|backlight|start|source|contrast|color|picture|saturation|sharp|display|game|ota|gamma|refresh|zoom|model|temp|power|fan|lumin|receiver",x,re.I))
 docs[target]={"present":True,"java_path":str(path.relative_to(src)),"line_count":len(text.splitlines()),"methods":dict(methods),"resource_ids":ids[:240],"strings":stringres[:180],"interesting_calls":interesting[:200],"actions_properties":actions[:70]}
 print(target,":",len(methods),"methods",len(ids),"ids",len(interesting),"interesting calls",flush=True)
out.parent.mkdir(parents=True,exist_ok=True)
out.write_text(json.dumps({"source":"Stock 8052 TvSettingsPlus DEX JADX metadata; not runtime proof","controllers":docs},ensure_ascii=False,indent=2)+"\n")
summary=["# TvSettingsPlus 8052: existing OEM controllers inventory","","Source: original 8052 TvSettingsPlus APK. Class names, methods, view IDs and property names only; no proprietary Java source reproduced.","","| Existing controller/fragment | Methods | OEM UI IDs | Example controls |","|---|---:|---:|---|"]
for n,v in docs.items():
 summary.append(f"| {n} | {len(v.get('methods',{}))} | {len(v.get('resource_ids',[]))} | {', '.join(v.get('resource_ids',[])[:11])} |")
summary+=["","These controllers already exist in the factory APK; their presence does not prove a new unified menu is installed.",
          "Preserve focus and keystone full-screen calibration flows. Light-source brightness must use original thermal-aware OEM action."]
(out.parent/"README.md").write_text("\n".join(summary)+"\n")

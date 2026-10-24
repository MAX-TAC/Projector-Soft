#!/usr/bin/env python3
"""Wire OEM settings entries to original AOSP MainFragment and OEM MainActivity.
Modifies only specific methods; all other classes retain their original resources.
"""
import argparse,json,re
from pathlib import Path
from patch_customer0_lightmode_smali import mode_dex

def find_class(root,name):
 matches=[x for x in root.glob("smali*/**/"+name) if x.is_file()]
 if len(matches)!=1:raise ValueError("Ambiguous smali class "+name+": "+repr(matches))
 return matches[0]

def hook_return_method(text,method_signature,return_verb,hook_call):
 # Supports primary smali method with method declaration + end marker,
 # and fails closed if its expected single return is not found.
 regex=re.compile(r"(?ms)^\.method[^\n]*"+re.escape(method_signature)+r"[^\n]*\n.*?^\.end method")
 found=list(regex.finditer(text))
 if len(found)!=1:raise ValueError("Expected 1 target method: "+method_signature+"; found "+str(len(found)))
 match=found[0]
 body=match.group()
 branches=list(re.finditer(r"(?m)^[ \t]*"+re.escape(return_verb)+r"\b[^\n]*$",body))
 if len(branches)!=1:
  raise ValueError("Expected exactly 1 "+return_verb+" in "+method_signature+", got "+str(len(branches)))
 insert="    "+hook_call+"\n"
 at=branches[0].start()
 patched=body[:at]+insert+body[at:]
 if patched==body:raise AssertionError("No method modification")
 return text[:match.start()]+patched+text[match.end():]

def main():
 p=argparse.ArgumentParser()
 p.add_argument("decoded",type=Path)
 p.add_argument("out",type=Path)
 a=p.parse_args()
 main_fragment=find_class(a.decoded,"MainFragment.smali")
 activity=find_class(a.decoded,"MainActivity.smali")
 if "/com/android/tv/settings/MainFragment.smali" not in main_fragment.as_posix():
  raise ValueError("Not original com.android.tv.settings.MainFragment")
 if "/com/android/tv/settings/uicustom/MainActivity.smali" not in activity.as_posix():
  raise ValueError("Not original OEM MainActivity")

 j=main_fragment.read_text()
 j=hook_return_method(j,
   "onCreateView(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;Landroid/os/Bundle;)Landroid/view/View;",
   "return-object",
   "invoke-static {p0}, Ldev/projectorsoft/tvsettings/OemRootMenuHook;->install(Ljava/lang/Object;)V")
 main_fragment.write_text(j)

 z=activity.read_text()
 z=hook_return_method(z,"onCreate(Landroid/os/Bundle;)V","return-void",
   "invoke-static {p0}, Ldev/projectorsoft/tvsettings/OemLaunchRouteHook;->apply(Ljava/lang/Object;)V")
 activity.write_text(z)
 info={"edited_smali":[str(main_fragment.relative_to(a.decoded)),str(activity.relative_to(a.decoded))],
       "edited_dex":sorted(set([mode_dex(main_fragment),mode_dex(activity)])),
       "features":["five Android TV MainFragment links to OEM projector pages",
                   "explicit OEM source and general routing",
                   "original OEM focus/keystone calibration classes untouched"]}
 a.out.parent.mkdir(parents=True,exist_ok=True)
 a.out.write_text(json.dumps(info,indent=2)+"\n")
 print(json.dumps(info,indent=2))
if __name__=="__main__":main()

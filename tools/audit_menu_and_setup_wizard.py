#!/usr/bin/env python3
"""Extract exact class definitions, manifest activities and XML preferences from original 8052 APKs.
Only metadata is published. No decompiled OEM code is committed.
"""
import argparse, collections, hashlib, json, re, struct, subprocess, xml.etree.ElementTree as ET, zipfile
from pathlib import Path
ANDROID="{http://schemas.android.com/apk/res/android}"
APPS=("KuSettings","KuSetupWizard","TvSettingsPlus","KuSourceRJ","WFDSinkTest")
def execute(*args):
 p=subprocess.run(args,capture_output=True,text=True,errors="replace",timeout=180)
 if p.returncode:raise RuntimeError(str(args)+"\n"+p.stderr[-1400:])
 return p.stdout
def class_definitions(apk):
 found=set()
 with zipfile.ZipFile(apk) as archive:
  for filename in archive.namelist():
   if not re.fullmatch(r"classes\d*\.dex",filename):continue
   d=archive.read(filename)
   if d[:4]!=b"dex\n":continue
   n,s=struct.unpack_from("<II",d,0x38)
   nt,ot=struct.unpack_from("<II",d,0x40)
   nc,oc=struct.unpack_from("<II",d,0x60)
   if max(s+4*n,ot+4*nt,oc+32*nc)>len(d):raise ValueError("Invalid DEX")
   def uleb(off):
    for i in range(5):
     ch=d[off];off+=1
     if ch<128:return off
    return off
   strings=[]
   for i in range(n):
    pos=uleb(struct.unpack_from("<I",d,s+4*i)[0])
    end=d.find(b"\0",pos,min(len(d),pos+1200))
    strings.append(d[pos:end].decode("utf8","replace"))
   types=[strings[struct.unpack_from("<I",d,ot+i*4)[0]] for i in range(nt)]
   for i in range(nc):
    idx=struct.unpack_from("<I",d,oc+32*i)[0]
    if idx<len(types):found.add(types[idx])
 return found
def components(apk):
 raw=execute("aapt","dump","xmltree",str(apk),"AndroidManifest.xml")
 stack=[];items=[];cur=None
 for ln in raw.splitlines():
  m=re.match(r"(\s*)E:\s+([\w-]+)",ln)
  if m:
   depth=len(m.group(1));tag=m.group(2)
   while stack and stack[-1][0]>=depth:stack.pop()
   stack.append((depth,tag))
   if tag in ("activity","activity-alias","service","receiver","provider"):
    cur={"type":tag,"name":"","actions":[],"exported":""};items.append(cur)
   continue
  v=re.search(r'A:\s+android:(\w+)\([^)]*\)=.*Raw: "([^"]+)"',ln)
  if v and cur:
   field,val=v.groups();tag=stack[-1][1] if stack else ""
   if field=="name" and tag==cur["type"]:cur["name"]=val
   if field=="name" and tag=="action":cur["actions"].append(val)
   if field=="exported" and tag==cur["type"]:cur["exported"]=val
 return items
def preferences(apk,scratch):
 execute("apktool","d","-f","-s",str(apk),"-o",str(scratch))
 strings={}
 for file in [scratch/"res/values/strings.xml",scratch/"res/values-ru/strings.xml"]:
  if file.is_file():
   try:
    for el in ET.parse(file).getroot():
     if el.tag=="string" and el.get("name"):strings[el.get("name")]="".join(el.itertext())[:140]
   except ET.ParseError:pass
 result=[]
 for file in sorted((scratch/"res/xml").glob("*.xml")):
  try:root=ET.parse(file).getroot()
  except ET.ParseError:continue
  nodes=[]
  def visit(node,level=0):
   tag=node.tag.split("}")[-1]
   if "Preference" in tag or "preference" in tag:
    title=node.get(ANDROID+"title","")
    if title.startswith("@string/"):title=strings.get(title[8:],title)
    nodes.append({"depth":level,"title":title,"key":node.get(ANDROID+"key",""),"tag":tag,
      "fragment":node.get(ANDROID+"fragment","")})
   for child in node:visit(child,level+1)
  visit(root)
  if nodes:result.append({"file":file.name,"nodes":nodes[:250]})
 return result
def main():
 a=argparse.ArgumentParser()
 a.add_argument("apkdir",type=Path);a.add_argument("tmp",type=Path);a.add_argument("out",type=Path)
 c=a.parse_args();c.out.mkdir(parents=True,exist_ok=True)
 d={};classes={}
 for app in APPS:
  apk=c.apkdir/(app+".apk")
  classes[app]=class_definitions(apk)
  d[app]={"class_count":len(classes[app]),"components":components(apk)}
  if app=="TvSettingsPlus":
   d[app]["uicustom_class_descriptors"]=sorted(x for x in classes[app] if x.startswith("Lcom/android/tv/settings/uicustom/") or x.startswith("Lcom/android/p002tv/settings/uicustom/"))
  if app in ("KuSettings","KuSetupWizard"):
   with zipfile.ZipFile(apk) as z:
    d[app]["dex_digests"]={n:hashlib.sha256(z.read(n)).hexdigest() for n in z.namelist() if re.fullmatch(r"classes\d*\.dex",n)}
  if app in ("TvSettingsPlus","KuSettings","KuSetupWizard"):
   d[app]["preferences"]=preferences(apk,c.tmp/app)
  print(app,"defined classes",len(classes[app]),"declared components",len(d[app]["components"]),flush=True)
 check=["Lcom/my/service/BootupReceiver;","Lcom/my/service/MySetTimeService;",
    "Lcom/my/service/ScreenServices;","Lcom/my/settings/SettingActivity;",
    "Lcom/my/settings/bluetooth/MyAutoBluetoothActivity;"]
 ku=classes["KuSettings"];setup=classes["KuSetupWizard"]
 d["KuSetupWizard_comparison"]={
   "ku_classes_defined_also_in_setup":len(ku&setup),
   "identical_dex_file_hashes":{n:val==d["KuSetupWizard"]["dex_digests"].get(n) for n,val in d["KuSettings"]["dex_digests"].items()},
   "ku_total_classes":len(ku),
   "explicitly_own_class_definitions":{name:name in setup for name in check},
   "ku_component_names_inside_setup_manifest":[x for x in d["KuSetupWizard"]["components"] if x["name"].startswith("com.my.service.") or x["name"]=="com.my.settings.SettingActivity"]
 }
 old={act for item in d["KuSettings"]["components"] if item["type"].startswith("activity") for act in item["actions"]}
 native={act for item in d["TvSettingsPlus"]["components"] if item["type"].startswith("activity") for act in item["actions"]}
 d["legacy_ku_actions_missing_from_original_tvsettings"]=sorted(old-native)
 (c.out/"menu_inventory.json").write_text(json.dumps(d,indent=2,ensure_ascii=False)+"\n")
 t=["# 8052: original APK menu inventory and KuSetupWizard component comparison","",
    "Static original OTA APK metadata only; v2 adds intent aliases but no DEX/UI screens.","",
    "## KuSetupWizard local class copies",
    "KuSettings class definitions also present in KuSetupWizard: "+str(len(ku&setup))+" of "+str(len(ku)),
    *[k+": "+str(k in setup) for k in check],
    "KuSetupWizard declared old OEM components: "+str(d["KuSetupWizard_comparison"]["ku_component_names_inside_setup_manifest"]),
    "","## Intent actions present in KuSettings but not original TvSettingsPlus",
    *["- "+x for x in d["legacy_ku_actions_missing_from_original_tvsettings"]],
    "","## KuSettings app components",
    *["- "+i["type"]+" "+i["name"]+" => "+",".join(i["actions"]) for i in d["KuSettings"]["components"]],
    "","## TvSettingsPlus original preference resource nodes"]
 for f in d["TvSettingsPlus"].get("preferences",[]):
  t.append("");t.append("### "+f["file"]+" ("+str(len(f["nodes"]))+" nodes)")
  for node in f["nodes"][:125]:
   t.append("  "*min(node["depth"],5)+"- "+(node["title"] or node["key"] or node["tag"])+" ["+node["tag"]+"]")
 t+=["","UI preference XML does not include Java-driven menus, uicustom floating menus or runtime hardware feature gates.",
   "AOSP present class does not prove that every corresponding function was transferred from KuSettings.",
   "ScreenServices and screenshot components are intentionally not ported; target 8054 requires independent testing."]
 (c.out/"README.md").write_text("\n".join(t)+"\n")
if __name__=="__main__":main()

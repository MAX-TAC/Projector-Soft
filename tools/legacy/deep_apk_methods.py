#!/usr/bin/env python3
"""Read-only analysis of OEM Android APKs for a one-vs-two HDMI comparison.
Never executes the APKs and never publishes reconstructed Java source.
Usage: python3 tools/audit_apk_deep.py /tmp/erofs /tmp/report/one.json one
"""
import sys,os,re,hashlib,subprocess,json,zipfile,shutil,collections
from pathlib import Path
root=Path(sys.argv[1]);output=Path(sys.argv[2]);variant=sys.argv[3]
output.parent.mkdir(parents=True,exist_ok=True)
apps={
 "KuSettings":"system_a/system/app/KuSettings/KuSettings.apk",
 "KeystoneCorrection":"system_a/system/priv-app/KeystoneCorrection/KeystoneCorrection.apk",
 "RuiAutoFocus":"system_a/system/app/RuiAutoFocus/RuiAutoFocus.apk",
 "KuSourceRJ":"system_a/system/app/KuSourceRJ/KuSourceRJ.apk",
 "trdservice":"system_a/system/app/trdservice/trdservice.apk",
 "trdmiddle":"vendor_a/priv-app/trdmiddle/trdmiddle.apk",
 "TvSettingsPlus":"system_ext_a/priv-app/TvSettingsPlus/TvSettingsPlus.apk",
 "zxlauncher":"system_a/system/app/zxlauncher/zxlauncher.apk",
 "zxstore":"system_a/system/app/zxstore/zxstore.apk",
 "RuiYunOta":"system_a/system/priv-app/RuiYunOta/RuiYunOta.apk",
 "Factory":"system_ext_a/app/Factory/Factory.apk",
 "TvServices":"system_ext_a/priv-app/TvServices/TvServices.apk",
 "LiveTV":"system_ext_a/priv-app/LiveTV/LiveTV.apk",
 "KuSetupWizard":"system_a/system/app/KuSetupWizard/KuSetupWizard.apk",
}
patterns={
 "native_load":r'(?:System\.)?loadLibrary\s*\(\s*["\x27]([^"\x27]{2,90})',
 "service_binder":r'\b(?:ServiceManager|HwBinder|IHwBinder|IBinder|IProjectorCtrl|ServiceConnection)\b',
 "hardware_references":r'(?:com\.trd\.|vendor\.trd\.|com\.mediatek\.|vendor\.mediatek\.|mstar\.hardware|I2CDevice|MotorJNI|MtkTv|TRDService)',
 "property_calls":r'\b(?:SystemProperties\.(?:get|set)|getprop|setprop)\b',
 "shell_process":r'\b(?:Runtime\.getRuntime\(\)\.exec|ProcessBuilder|su\s+-c)\b',
 "focus_calls":r'(?:\b[a-zA-Z_0-9]*(?:Focus|focus|Motor|motor|Tof|TOF)[a-zA-Z_0-9]*\s*\()',
 "keystone_calls":r'(?:\b[a-zA-Z_0-9]*(?:Keystone|keystone|Trapezoid|trapezoid|FourCorner)[a-zA-Z_0-9]*\s*\()',
 "hdmi_calls":r'(?:\b[a-zA-Z_0-9]*(?:Hdmi|HDMI|hdmi|InputSource|SourceSwitch)[a-zA-Z_0-9]*\s*\()',
}
suspect_routes=re.compile(r'(?i)(motor|focus|tof|hdmi|keystone|trapezoid|i2c|trd|tvsetting|inputsource|picture|brightness|audio|dolby|boot|uart|mstar)')
compiled={k:re.compile(v) for k,v in patterns.items()}
def call(args,timeout=200):
  try:
    p=subprocess.run(args,capture_output=True,text=True,errors='replace',timeout=timeout)
    return p.returncode,(p.stdout+'\n'+p.stderr)[-5000:]
  except Exception as e:return -1,str(e)
def sha(p):
  h=hashlib.sha256()
  with p.open('rb') as f:
    for b in iter(lambda:f.read(4*1024*1024),b''):h.update(b)
  return h.hexdigest()
def sanitize_java(s):
  s=re.sub(r'(?s)/\*.*?\*/','',s)
  s=re.sub(r'(?m)^\s*//.*$','',s)
  s=re.sub(r'\s+',' ',s).strip()
  return s
def extract_methods(s):
  found={}
  # Not a Java compiler; stable signatures with brace-balanced bodies.
  rx=re.compile(r'(?m)^[ \t]*(?:@Override[ \t]+)?(?:(?:public|private|protected|static|final|synchronized|native|abstract|volatile)\s+){0,6}[A-Za-z_$\w<>\[\].?, ]+\s+([A-Za-z_$][\w$]*)\s*\([^;{}]*?\)\s*(?:throws\s+[\w., ]+)?\{')
  for match in rx.finditer(s):
    head=s[match.start():match.end()].strip()
    name=match.group(1)
    if name in ('if','while','for','switch','catch','synchronized'):continue
    depth=1;i=match.end()
    while i<len(s) and depth:
      if s[i]=='{':depth+=1
      elif s[i]=='}':depth-=1
      i+=1
    if depth:continue
    canonical=sanitize_java(s[match.start():i])
    sig=re.sub(r'\s+',' ',head)
    key=name+'('+re.sub(r'^(.*?)\(','',sig).split(')',1)[0][:150]+')'
    found[key]={'digest':hashlib.sha256(canonical.encode()).hexdigest(),
       'tokens':len(canonical.split()),'name':name}
  return found
res={'variant':variant,'apps':{},'explanation':'Only hashes, metadata and derived indicators; no OEM decompiled source.'}
for app,rel in apps.items():
  p=root/rel
  if not p.is_file():
    res['apps'][app]={'missing':True,'expected_path':rel};continue
  row={'path':rel,'sha256':sha(p),'size':p.stat().st_size,'dex':{},'methods':{},
       'codefiles':{},'package':None,'app_version':None,'permissions':[],'features':{},
       'native_libraries':[], 'interesting_classes':[], 'manifest_extract':{},
       'api_usage_count':{},'referenced_service_strings':[], 'errors':[]}
  with zipfile.ZipFile(p) as z:
    for name in z.namelist():
      if re.fullmatch(r'classes\d*\.dex',name):
        b=z.read(name);row['dex'][name]={'sha256':hashlib.sha256(b).hexdigest(),'size':len(b)}
  rc,badging=call(['aapt','dump','badging',str(p)],timeout=40)
  match=re.search(r"package: name='([^']+)' versionCode='([^']*)' versionName='([^']*)'",badging)
  if match:row['package'],row['version_code'],row['app_version']=match.groups()
  row['permissions']=sorted(set(re.findall(r"uses-permission: name='([^']+)'",badging)))
  rc,manifest=call(['aapt','dump','xmltree',str(p),'AndroidManifest.xml'],timeout=45)
  if rc==0:
    lines=manifest.splitlines()
    row['manifest_extract']={
      'sharedUserId':next((x.strip()[:250] for x in lines if 'android:sharedUserId' in x),None),
      'debuggable':next((x.strip()[:180] for x in lines if 'android:debuggable' in x),None),
      'allowBackup':next((x.strip()[:180] for x in lines if 'android:allowBackup' in x),None),
      'exported_true_count':len(re.findall(r'android:exported.*(?:0xffffffff|true)',manifest))}
  else:row['errors'].append('aapt manifest '+manifest[-300:])
  if row['dex']:
    dst=Path('/tmp/jadxdeep')/(variant+'-'+app)
    cmd=['/tmp/jadx-bin/bin/jadx','--no-res','--no-imports','--threads-count','3',
       '-d',str(dst),str(p)]
    rc,log=call(cmd,timeout=420)
    row['jadx_exit_code']=rc
    row['jadx_warnings']=log[-1300:]
    java=list((dst/'sources').rglob('*.java')) if (dst/'sources').exists() else []
    row['java_class_count']=len(java)
    counts=collections.Counter();loads=set();strings=set()
    for j in java:
      relative=j.relative_to(dst/'sources').as_posix()
      code=j.read_text(errors='replace')
      row['codefiles'][relative]={'sha256':hashlib.sha256(sanitize_java(code).encode()).hexdigest(),
           'bytes':len(code)}
      row['methods'][relative]=extract_methods(code)
      for key,rx in compiled.items():
        matches=list(rx.finditer(code))
        counts[key]+=len(matches)
        if key=='native_load':
          loads.update(m.group(1) for m in matches)
      if suspect_routes.search(relative) and len(row['interesting_classes'])<140:
        row['interesting_classes'].append(relative)
      for m in re.finditer(r'["\x27]((?:com\.(?:trd|rjpj|my|coolux|mediatek|zeasn)\.|vendor\.|mstar\.|android\.hardware\.)[a-zA-Z0-9_.$@/-]{5,120})["\x27]',code):
        if len(strings)<200:strings.add(m.group(1))
    row['api_usage_count']=dict(counts)
    row['native_libraries']=sorted(loads)
    row['referenced_service_strings']=sorted(strings)
    shutil.rmtree(dst,ignore_errors=True)
  res['apps'][app]=row
  print(variant,app,'size',p.stat().st_size,'java classes',row.get('java_class_count',0),
     'methods',sum(len(x) for x in row.get('methods',{}).values()),flush=True)
output.write_text(json.dumps(res,ensure_ascii=False,indent=1),encoding='utf8')
print('Complete, bytes',output.stat().st_size,flush=True)

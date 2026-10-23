#!/usr/bin/env python3
"""Compare two extracted OEM Android roots: app methods, permissions, service & HAL setup.
This is static reverse engineering. NO proprietary source code is committed, run, or redistributed.
Usage: apk_deep_compare.py ROOT_ONE ROOT_TWO DEST
Requires jadx, aapt, readelf, strings, nm. Two EROFS roots include named partition subfolders.
"""
import sys,re,json,hashlib,subprocess,collections,shutil,tempfile,os
from pathlib import Path
a=Path(sys.argv[1]);b=Path(sys.argv[2]);dest=Path(sys.argv[3]);dest.mkdir(exist_ok=True,parents=True)
NAMES=['KuSettings','KeystoneCorrection','RuiAutoFocus','KuSourceRJ','trdservice','trdmiddle',
       'TvSettingsPlus','zxlauncher','zxstore','LiveTV','TvServices','Factory','KuSetupWizard','Provision','RuiYunOta']
def run(args,timeout=180):
 try:
  p=subprocess.run(args,capture_output=True,text=True,errors='replace',timeout=timeout)
  return p.returncode,p.stdout,p.stderr
 except Exception as e:return -1,'',str(e)
def sha_bytes(x):return hashlib.sha256(x).hexdigest()
def locate(root,name,ext='.apk'):
 return sorted(x for x in root.rglob(name+ext) if x.is_file())
def aapt_info(apk):
 if not apk:return {}
 code,s,err=run(['aapt','dump','badging',str(apk)],35)
 row={}
 for line in s.splitlines():
  if line.startswith('package:'):
   row['package_line']=line[:350]
   m=re.search(r"name='([^']+)'",line);row['package']=m.group(1) if m else ''
   m=re.search(r"versionCode='([^']+)'",line);row['version_code']=m.group(1) if m else ''
   m=re.search(r"versionName='([^']+)'",line);row['version_name']=m.group(1) if m else ''
  if line.startswith('uses-permission:'):
   m=re.search(r"name='([^']+)'",line)
   if m:row.setdefault('permissions',[]).append(m.group(1))
  if line.startswith('launchable-activity:'):
   row['launcher_activity']=line[:270]
 return row
def jadx_manifest(apk):
 code,s,err=run(['aapt','dump','xmltree',str(apk),'AndroidManifest.xml'],45)
 # record only manifest declaration metadata, not complete proprietary manifest
 keys=['activity','service','receiver','provider','uses-library','uses-permission','intent-filter','android:exported','android:permission','android:sharedUserId','android:debuggable']
 return {k:len(re.findall(r'\bE: '+re.escape(k)+r'\b|\bA: '+re.escape(k)+r'\b',s)) for k in keys}
def sources(apk,tmp):
 if not apk:return {'status':'missing','files':{},'error':''}
 shutil.rmtree(tmp,ignore_errors=True)
 code,so,se=run(['/tmp/jadx-bin/bin/jadx','--no-res','--no-imports','--threads-count','2',
                  '-d',str(tmp),str(apk)],240)
 found={}
 methods=collections.Counter();keywords=collections.defaultdict(set);imports=set()
 matcher=re.compile(r'\b(?:public|private|protected|static|final|synchronized|native|abstract)\s+[\w<>\[\].,? ]{2,120}\s+\w+\s*\([^\n]{0,220}\)\s*(?:\{|;)',re.MULTILINE)
 tags={
  'binder_hidl':r'\b(?:IHwBinder|HwBinder|ServiceManager|Binder|transact|asInterface|HwParcel)\b',
  'android_settings':r'\b(?:Settings\.System|Settings\.Global|Settings\.Secure|ContentResolver)\b',
  'vendor_tv':r'\b(?:MtkTv|MtkTvFApi|ITvInput|TvInputManager|IMtk|TvFApi|TrdService|TrdSystemService)\w*',
  'focus_tof':r'(?i)\b(?:focus|autofocus|motor|tof|distance|focusing)\w*',
  'keystone':r'(?i)\b(?:keystone|trapezoid|corner|warp|geometry)\w*',
  'hdmi':r'(?i)\b(?:hdmi|cec|tvinput|inputsource|input_source)\w*',
  'i2c':r'(?i)\b(?:i2c|settingsi2c|projectorctrl|gpio|sysfs)\w*',
  'network':r'\b(?:HttpURLConnection|OkHttpClient|Retrofit|WebView|Socket|URL|URLConnection)\b',
  'dynamic_exec':r'\b(?:Runtime\.getRuntime|ProcessBuilder|loadLibrary|System\.load)\b'
 }
 rx={k:re.compile(v) for k,v in tags.items()}
 for f in (tmp/'sources').rglob('*.java') if (tmp/'sources').exists() else []:
  rel=f.relative_to(tmp/'sources').as_posix()
  raw=f.read_text(errors='replace')
  # Strip comments, generated line hints, literal strings and whitespace to avoid false "changed" due to rebuilding.
  stripped=re.sub(r'/\*.*?\*/|//[^\n]*','',raw,flags=re.S)
  stripped=re.sub(r'"(?:\\.|[^"\\])*"','""',stripped)
  stripped=re.sub(r'\s+','',stripped)
  found[rel]={'struct_sha256':sha_bytes(stripped.encode()),'bytes':len(raw)}
  for match in matcher.finditer(raw):
   text=match.group().split('{')[0].strip()
   methods[text[:160]]+=1
  for key,pat in rx.items():
   if pat.search(raw):
    keywords[key].add(rel)
 status={'exit':code,'java_files':len(found),
  'methods_top':[k for k,_ in methods.most_common(30)],
  'categories':{k:sorted(v)[:80] for k,v in keywords.items()},
  'files':found,'warnings':(so+se)[-400:] if code else ''}
 shutil.rmtree(tmp,ignore_errors=True)
 return status
def summarize_diff(x,y):
 fa=x.get('files',{});fb=y.get('files',{})
 both=set(fa)&set(fb)
 matched=sum(fa[k]['struct_sha256']==fb[k]['struct_sha256'] for k in both)
 return {'common_decompiled_classes':len(both),'identical_normalized_classes':matched,
         'changed_classes':len(both)-matched,
         'classes_only_one':sorted(set(fa)-set(fb))[:100],
         'classes_only_two':sorted(set(fb)-set(fa))[:100],
         'changed_classes_sample':sorted(k for k in both if fa[k]['struct_sha256']!=fb[k]['struct_sha256'])[:160],
         'one_features':x.get('categories',{}),'two_features':y.get('categories',{})}
result={'apps':{},'notes':['Static structural diff of JADX output; decompilation can fail and changes can be compiler noise.','No original APK classes, source code, secret keys or binaries included in reports.']}
for name in NAMES:
 p=locate(a,name);q=locate(b,name)
 x=p[0] if p else None;y=q[0] if q else None
 meta={'present_one':bool(x),'present_two':bool(y),
  'apk_sha_identical': bool(x and y and sha_bytes(x.read_bytes())==sha_bytes(y.read_bytes())),
  'one':aapt_info(x),'two':aapt_info(y)}
 if x:meta['one']['manifest_counts']=jadx_manifest(x)
 if y:meta['two']['manifest_counts']=jadx_manifest(y)
 if x and y and meta['apk_sha_identical']:
  meta['structural_diff']={'status':'exactly_identical'}
 elif x and y:
  left=sources(x,Path('/tmp/app-decomp-one')/name)
  right=sources(y,Path('/tmp/app-decomp-two')/name)
  meta['structural_diff']=summarize_diff(left,right)
  meta['decompilation']={'one':{k:v for k,v in left.items() if k!='files'},
                        'two':{k:v for k,v in right.items() if k!='files'}}
 result['apps'][name]=meta
 print(name,'APK equal',meta['apk_sha_identical'],'one',meta['one'].get('version_name'),
       'two',meta['two'].get('version_name'),
       'classes',meta.get('structural_diff',{}).get('common_decompiled_classes','same/missing'),flush=True)
 # Persist partial reports to survive timeouts
 (dest/'apk_method_comparison.json').write_text(json.dumps(result,ensure_ascii=False,indent=1))
rows=['# One-HDMI vs two-HDMI: static code-level review of projector APKs','','Versions and method-level structural changes, not just whole-file checksums.',
      'These are approximate classes reconstructed by JADX, not original manufacturer sources.','','| App | 1 HDMI APK version | 2 HDMI APK version | Exact APK? | Decompiled classes same/changed |','|---|---|---|---|---|']
for name,meta in result['apps'].items():
 d=meta.get('structural_diff',{})
 numbers=('exactly identical' if meta['apk_sha_identical'] else str(d.get('identical_normalized_classes','?'))+'/'+str(d.get('changed_classes','?')))
 rows+=['| '+name+' | '+str(meta['one'].get('version_name','-'))+' | '+str(meta['two'].get('version_name','-'))+' | '+str(meta['apk_sha_identical'])+' | '+numbers+' |']
for name,meta in result['apps'].items():
 d=meta.get('structural_diff',{})
 rows+=['','## '+name,
        'APK versions: '+str(meta['one'].get('version_name','missing'))+' vs '+str(meta['two'].get('version_name','missing')),
        'Permissions only in 1 HDMI: '+', '.join(sorted(set(meta['one'].get('permissions',[]))-set(meta['two'].get('permissions',[])))),
        'Permissions only in 2 HDMI: '+', '.join(sorted(set(meta['two'].get('permissions',[]))-set(meta['one'].get('permissions',[]))))]
 if d.get('status')=='exactly_identical':rows+=['APK binaries match exactly.'];continue
 rows+=['Common decompiled classes: '+str(d.get('common_decompiled_classes')),
        'Identical normalized classes: '+str(d.get('identical_normalized_classes')),
        'Changed classes: '+str(d.get('changed_classes')),
        'Unique classes 1 HDMI: '+', '.join(d.get('classes_only_one',[])[:30]),
        'Unique classes 2 HDMI: '+', '.join(d.get('classes_only_two',[])[:30]),
        'Changed classes (first 40): '+', '.join(d.get('changed_classes_sample',[])[:40])]
(dest/'README.md').write_text('\n'.join(rows)+'\n')

#!/usr/bin/env python3
"""Static-only lightweight audit of native APIs used by projector control APKs.
Runs jadx decompilation in subprocess; does not execute manufacturer apps or ship sources.
"""
from pathlib import Path
import subprocess, re, json, collections, sys, shutil, zipfile
root=Path(sys.argv[1]); out=Path(sys.argv[2]);out.mkdir(parents=True,exist_ok=True)
targets={
 'KuSettings':'system_a/system/app/KuSettings/KuSettings.apk',
 'KeystoneCorrection':'system_a/system/priv-app/KeystoneCorrection/KeystoneCorrection.apk',
 'RuiAutoFocus':'system_a/system/app/RuiAutoFocus/RuiAutoFocus.apk',
 'KuSourceRJ':'system_a/system/app/KuSourceRJ/KuSourceRJ.apk',
 'trdservice':'system_a/system/app/trdservice/trdservice.apk',
 'trdmiddle':'vendor_a/priv-app/trdmiddle/trdmiddle.apk',
 'TvSettingsPlus':'system_ext_a/priv-app/TvSettingsPlus/TvSettingsPlus.apk',
 'zxlauncher':'system_a/system/app/zxlauncher/zxlauncher.apk'
}
patterns={
 'jni_load':r'\b(?:System\.loadLibrary|System\.load|loadLibrary)\s*\(',
 'native_methods':r'\bnative\s+(?:\w+\s+){1,5}\w+\s*\(',
 'runtime_exec':r'\b(?:Runtime\.getRuntime\(\)\.exec|ProcessBuilder)\s*\(',
 'hidl_binder':r'\b(?:HwBinder|IHwBinder|ServiceManager\.getService|bindService|IBinder|transact)\b',
 'mtk_api':r'\b(?:MtkTv|IMtk|TVAPI|mstar|MediaTek|IImgDisp|IMmDisp)\w*',
 'autofocus':r'\b(?:autoFocus|AutoFocus|tof|TOF|focusMotor|focusStep|setFocus|FocusController)\w*',
 'keystone':r'\b(?:keystone|Keystone|trapezoid|Trapezoid|fourCorner|FourCorner)\w*',
 'input_source':r'\b(?:hdmi|HDMI|Hdmi|TV_INPUT|InputSource|setSource)\w*',
 'hardware_bridge':r'\b(?:i2c|I2C|sysfs|/dev/|/proc/|setprop|getprop)\w*',
 'settings_provider':r'\b(?:Settings\.System|Settings\.Secure|Settings\.Global|ContentResolver)\b'
}
compiled={name:re.compile(p) for name,p in patterns.items()}
def run(argv,timeout=180):
    try:
        p=subprocess.run(argv,capture_output=True,text=True,errors='replace',timeout=timeout)
        return p.returncode,(p.stdout+'\n'+p.stderr)[-5500:]
    except Exception as e:return -1,str(e)
allresult={}
for name,path in targets.items():
    apk=root/path
    if not apk.is_file():
        allresult[name]={'error':'Not found: '+path}
        continue
    row={'path':path,'bytes':apk.stat().st_size,'classes_dex':[],'aapt':{},'pattern_occurrences':{},'code_files_sample':{},'library_loads':[],'permissions':[]}
    with zipfile.ZipFile(apk) as z:
        row['classes_dex']=[x for x in z.namelist() if re.fullmatch(r'classes\d*\.dex',x)]
    code,txt=run(['aapt','dump','badging',str(apk)],20)
    if code==0:
        for line in txt.splitlines():
            if line.startswith('package:'):row['aapt']['package']=line[:300]
            if line.startswith('launchable-activity:'):row['aapt']['launcher_activity']=line[:280]
            if line.startswith('uses-permission:') and len(row['permissions'])<80:
                row['permissions'].append(line[:220])
    else:row['aapt']['error']=txt[:600]
    xmlcode,manifest=run(['aapt','dump','xmltree',str(apk),'AndroidManifest.xml'],30)
    if xmlcode==0:
        row['manifest_counts']={label:manifest.count(label) for label in
            ('android:exported','android:permission','android:sharedUserId','android:debuggable',
             'android:allowBackup','android:usesCleartextTraffic')}
        row['exported_true_mentions']=len(re.findall(r'android:exported.*(?:0xffffffff|true)',manifest))
    target_dir=Path('/tmp/jadx')/name
    if row['classes_dex']:
        rc,logs=run(['/tmp/jadx-bin/bin/jadx','--no-res','--no-imports','--threads-count','2',
                   '--deobf','-d',str(target_dir),str(apk)],240)
        row['jadx_status']=rc;row['jadx_log_last']=logs[-1100:]
        counts=collections.Counter()
        examples=collections.defaultdict(list)
        loads=set()
        all_sources=list((target_dir/'sources').rglob('*.java')) if (target_dir/'sources').exists() else []
        row['decompiled_java_files']=len(all_sources)
        for f in all_sources:
            rel=f.relative_to(target_dir/'sources').as_posix()
            text=f.read_text(errors='replace')
            for kind,regex in compiled.items():
                matches=list(regex.finditer(text))
                if matches:
                    counts[kind]+=len(matches)
                    if len(examples[kind])<12:
                        examples[kind].append(rel)
            for m in re.finditer(r'(?:System\.)?loadLibrary\s*\(\s*["\x27]([\w.-]{2,90})',text):
                loads.add(m.group(1))
        row['pattern_occurrences']=dict(counts)
        row['code_files_sample']={k:v for k,v in examples.items()}
        row['library_loads']=sorted(loads)
        # delete proprietary decompilation; only reports preserved.
        shutil.rmtree(target_dir,ignore_errors=True)
    else:row['no_dex_note']='APK has no classes*.dex, code may be ODEX/VDEX; jadx source analysis skipped.'
    allresult[name]=row
    print('Analyzed',name, row.get('decompiled_java_files',0),
          'java classes; API patterns',row.get('pattern_occurrences',{}),flush=True)
(out/'apk_api_audit.json').write_text(json.dumps(allresult,ensure_ascii=False,indent=2))
lines=['# Static engineering overview of manufacturer projector APKs','',
'Source: official two-HDMI CineLux firmware. Decompiled code was inspected only to measure API usage.',
'No APK was executed and no decompiled proprietary source is published.',
'Counts indicate occurrences in reconstructed source, not necessarily active runtime execution.',
'','| APK | Package | DEX | Classes | JNI load | Binder/HIDL | MTK API | ToF/focus | Keystone | HDMI/source |',
'|---|---|---:|---:|---:|---:|---:|---:|---:|---:|']
for name,item in allresult.items():
    v=item.get('pattern_occurrences',{})
    pkg=item.get('aapt',{}).get('package','').split(' ')[1] if item.get('aapt',{}).get('package') else '?'
    lines.append('| '+name+' | '+pkg[:75]+' | '+str(len(item.get('classes_dex',[])))+
     ' | '+str(item.get('decompiled_java_files',0))+' | '+str(v.get('jni_load',0))+
     ' | '+str(v.get('hidl_binder',0))+' | '+str(v.get('mtk_api',0))+
     ' | '+str(v.get('autofocus',0))+' | '+str(v.get('keystone',0))+
     ' | '+str(v.get('input_source',0))+' |')
lines+=['','## Per-app findings','']
for name,item in allresult.items():
    lines+=['### '+name,
            'Package: '+item.get('aapt',{}).get('package','Unknown'),
            'Permissions: '+', '.join(item.get('permissions',[])[:25]),
            'Loaded JNI libraries: '+', '.join(item.get('library_loads',[])),'']
    for key,files in item.get('code_files_sample',{}).items():
        if key in ('jni_load','native_methods','runtime_exec','mtk_api','autofocus','keystone','input_source','hardware_bridge'):
            lines.append(key+': '+', '.join(files[:10]))
    lines+=['']
(out/'README.md').write_text('\n'.join(lines)+'\n',encoding='utf8')
print('Report saved',out/'README.md',flush=True)

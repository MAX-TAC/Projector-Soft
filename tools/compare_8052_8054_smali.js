#!/usr/bin/env node
// Read-only method-level comparison of apktool smali directories.
// Output contains code identifiers and instructions, never APK bytes.
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const os = require('os');

function files(dir) {
  const result = [];
  for (const entry of fs.readdirSync(dir,{withFileTypes:true})) {
    const p = path.join(dir,entry.name);
    if (entry.isDirectory()) result.push(...files(p));
    else if (entry.name.endsWith('.smali')) result.push(p);
  }
  return result;
}
function methods(root) {
  const map = new Map();
  for (const dir of fs.readdirSync(root).filter(x=>x==='smali'||x.startsWith('smali_classes'))) {
    for (const file of files(path.join(root,dir))) {
      const source = fs.readFileSync(file,'utf8');
      const declaration=source.split(/\r?\n/).find(x=>x.startsWith('.class '));
      const cls=declaration?.match(/L[^;]+;/)?.[0];
      if (!cls) throw new Error(`Missing .class in ${file}`);
      const lines = source.split(/\r?\n/);
      let current = null, body = [], start = 0;
      for (let n=0;n<lines.length;n++) {
        const line=lines[n];
        if (line.startsWith('.method ')) {
          if (current) throw new Error(`Nested method ${file}:${n+1}`);
          current=line.trim().split(/\s+/).at(-1);
          body=[line]; start=n+1;
        } else if (current) {
          body.push(line);
          if (line.startsWith('.end method')) {
            const key=cls+'->'+current;
            const normalized=body.filter(x=>!/^\s*\.(line|prologue|local|end local|restart local)\b/.test(x)).join('\n');
            const hash=crypto.createHash('sha256').update(normalized).digest('hex');
            if (map.has(key)) throw new Error(`Duplicate method ${key}`);
            const invokes=[];
            for (let i=0;i<body.length;i++) {
              const match=body[i].match(/^\s*(invoke-[\w/-]+)\s+\{[^}]*\},\s*(L[^;]+;->[\w$<>]+\([^)]*\)\S*)/);
              if (match) invokes.push({line:start+i,kind:match[1],target:match[2]});
            }
            map.set(key,{source_file:path.relative(root,file).replace(/\\/g,'/'),line:start,hash,invokes});
            current=null;body=[];
          }
        }
      }
      if (current) throw new Error(`Unclosed method ${file}:${start}`);
    }
  }
  return map;
}
function compare(a,b) {
  const all=new Set([...a.keys(),...b.keys()]);
  const changed=[];
  for (const key of [...all].sort()) {
    const before=a.get(key),after=b.get(key);
    if (!before || !after || before.hash!==after.hash)
      changed.push({method:key,change:!before?'ADDED':!after?'REMOVED':'CHANGED',
        before_hash:before?.hash||null,after_hash:after?.hash||null,
        before_file:before?.source_file||null,after_file:after?.source_file||null});
  }
  return changed;
}
function criticalCalls(map,apk) {
  const target=/^(Lcom\/coolux\/source\/|Lcom\/my\/settings\/|Lcom\/mediatek\/twoworlds\/|Lmediatek\/tvsetting\/factory\/ui\/|Lcom\/hysd\/screen\/|Lcom\/rjpj\/tcorrection\/|Landroid\/os\/SystemProperties;|Landroid\/provider\/Settings\$)/;
  const declaredClasses=new Set([...map.keys()].map(key=>key.split('->')[0]));
  const calls=[];
  for (const [method,info] of map) for (const invoke of info.invokes) {
    const targetClass=invoke.target.split('->')[0];
    if (target.test(invoke.target)) calls.push({apk,source_file:info.source_file,
      source_method:method,line_or_smali:invoke.line,call_kind:invoke.kind,
      target:invoke.target,target_declared_in_same_apk:declaredClasses.has(targetClass),
      arguments:null,confidence:'DEX_INVOKE_STATIC',
      runtime_test:'Required; static instruction only',rollback:'Original OEM APK'});
  }
  return calls;
}
function main(configFile,outputDir) {
  const config=JSON.parse(fs.readFileSync(configFile,'utf8'));
  fs.mkdirSync(outputDir,{recursive:true});
  const result={schema_version:1,meaning:'Static smali method hashes and invoke instructions; no hardware proof',packages:[]};
  for (const pair of config.pairs) {
    const originalSha=crypto.createHash('sha256').update(fs.readFileSync(pair.original_apk)).digest('hex');
    const targetSha=crypto.createHash('sha256').update(fs.readFileSync(pair.target_apk)).digest('hex');
    if (pair.expected_original_sha256 && originalSha!==pair.expected_original_sha256)
      throw new Error(`${pair.apk}: 8052 APK hash mismatch`);
    if (pair.expected_target_sha256 && targetSha!==pair.expected_target_sha256)
      throw new Error(`${pair.apk}: 8054 APK hash mismatch`);
    const original=methods(pair.original);
    const target=methods(pair.target);
    const changed=compare(original,target);
    result.packages.push({apk:pair.apk,original_sha256:originalSha,target_sha256:targetSha,
      original_methods:original.size,target_methods:target.size,changed_methods:changed,
      target_critical_invokes:criticalCalls(target,pair.apk)});
    for (const item of changed)
      console.log('DIFF_METHOD '+pair.apk+' '+item.change+' '+item.method);
    const selected=/ApiService|ProjectorManager|MySetTimeService|BootupReceiver|MyAutoBluetoothActivity|EdidSourceSecondaryController|PowerOnSourceSecondaryController|KeystoneSettingsSecondaryController/;
    for (const call of result.packages.at(-1).target_critical_invokes.filter(x=>selected.test(x.source_method)).slice(0,250))
      console.log('CALLSITE '+pair.apk+' '+call.source_method+' '+call.source_file+':'+call.line_or_smali+' '+call.call_kind+' '+call.target+' same_apk='+call.target_declared_in_same_apk);
    console.log(`${pair.apk}: ${original.size}/${target.size} methods, ${changed.length} changed, ${result.packages.at(-1).target_critical_invokes.length} critical invokes`);
  }
  fs.writeFileSync(path.join(outputDir,'exact_apk_method_diff.json'),JSON.stringify(result,null,2)+'\n');
  const md=['# Exact 8052 ↔ 8054 APK method diff (static)', '',
    'All methods originate from SHA-256 checked APKs. `DEX_INVOKE_STATIC` means an actual smali invoke instruction exists; it does not prove that the path executes on hardware. Runtime arguments and state ownership still need tracing.', '',
    '| APK | 8052 methods | 8054 methods | Changed | 8054 critical invokes |', '|---|---:|---:|---:|---:|',
    ...result.packages.map(p=>`| ${p.apk} | ${p.original_methods} | ${p.target_methods} | ${p.changed_methods.length} | ${p.target_critical_invokes.length} |`),
    '', 'Full changed method IDs and caller→target invoke instructions are in `exact_apk_method_diff.json`. No APK, DEX, calibration, key or personal data is included.', ''];
  fs.writeFileSync(path.join(outputDir,'exact_apk_method_diff.md'),md.join('\n'));
}
if (require.main===module) {
  if (process.argv[2]==='--self-test') {
    const temp=fs.mkdtempSync(path.join(os.tmpdir(),'apkdiff-fixture-'));
    try {
      const a=path.join(temp,'a','smali','com','example');
      const b=path.join(temp,'b','smali','com','example');
      fs.mkdirSync(a,{recursive:true});fs.mkdirSync(b,{recursive:true});
      const head='.class public Lcom/example/INotificationSideChannel$Stub$Proxy;\n.super Ljava/lang/Object;\n';
      fs.writeFileSync(path.join(a,'A.smali'),head+
        '.method public x()V\n    .locals 1\n    return-void\n.end method\n');
      fs.writeFileSync(path.join(b,'A.smali'),head+
        '.method public x()V\n    .locals 1\n    invoke-static {v0}, Lcom/coolux/source/service/TimeService;->start()V\n    return-void\n.end method\n'+
        '.method public y()V\n    .locals 0\n    return-void\n.end method\n');
      const before=methods(path.join(temp,'a')),after=methods(path.join(temp,'b'));
      if (compare(before,after).map(x=>x.change).join(',')!=='CHANGED,ADDED')
        throw new Error('compare self-test failed');
      if (criticalCalls(after,'fixture').length!==1)
        throw new Error('invoke parser self-test failed');
      console.log('PASS method and invoke parser self-test');
    } finally {
      const allowed=path.resolve(os.tmpdir())+path.sep;
      if (!path.resolve(temp).startsWith(allowed)) throw new Error('Unsafe fixture path');
      fs.rmSync(temp,{recursive:true,force:true});
    }
  } else main(process.argv[2],process.argv[3]);
}
module.exports={methods,compare,criticalCalls};

#!/usr/bin/env node
// Read-only focused smali trace: exact method bodies, never runtime proof.
const fs=require('fs');
const path=require('path');
const targets=[
  {apk:'TvSettingsPlus',cls:'Lcom/android/tv/settings/uicustom/floatmenu/controller/source/EdidSourceSecondaryController;',method:'getEdidVersion()I',versions:['8054']},
  {apk:'TvSettingsPlus',cls:'Lcom/android/tv/settings/uicustom/util/ProjectorManager;',method:'setProjectorMirrorMode(Landroid/content/Context;I)V',versions:['8054']},
  {apk:'KuSettings',cls:'Lcom/my/settings/bluetooth/MyAutoBluetoothActivity;',method:'viewAutoBluetoothBak()V',versions:['8052','8054']},
  {apk:'Factory',cls:'Lmediatek/tvsetting/factory/ui/ApiService;',method:'onStartCommand(Landroid/content/Intent;II)I',versions:['8054']},
  {apk:'Factory',cls:'Lmediatek/tvsetting/factory/ui/ProjectorManager;',method:'setProjectorMirrorMode(Landroid/content/Context;I)V',versions:['8052']},
  {apk:'Factory',cls:'Lmediatek/tvsetting/factory/ui/ProjectorManager;',method:'setProjectorMirrorMode(I)V',versions:['8054']},
  {apk:'trdservice',cls:'Lcom/trd/service/TrdService;',method:'onCreate()V',versions:['8052','8054']}
];
function extract(root,cls,method) {
  const relative=cls.slice(1,-1)+'.smali';
  let file;
  for(const dir of fs.readdirSync(root).filter(x=>x==='smali'||x.startsWith('smali_classes'))){
    const candidate=path.join(root,dir,relative);
    if(fs.existsSync(candidate)){if(file)throw new Error('Duplicate class '+cls);file=candidate;}
  }
  if(!file)throw new Error('Missing class '+cls+' in '+root);
  const lines=fs.readFileSync(file,'utf8').split(/\r?\n/);
  if(!lines.some(x=>x.startsWith('.class ')&&x.includes(cls)))
    throw new Error('Descriptor mismatch '+file);
  let start=-1,end=-1;
  for(let i=0;i<lines.length;i++){
    if(lines[i].startsWith('.method ')&&lines[i].trim().split(/\s+/).at(-1)===method){
      if(start!==-1)throw new Error('Duplicate method '+method);
      start=i;
    } else if(start!==-1&&end===-1&&lines[i].startsWith('.end method'))end=i;
  }
  if(start<0||end<start)throw new Error('Missing or unclosed method '+cls+'->'+method);
  return {source_file:path.relative(root,file).replace(/\\/g,'/'),start_line:start+1,
    lines:lines.slice(start,end+1).map((text,i)=>({line:start+i+1,text}))};
}
function main(configFile,outputDir){
  const config=JSON.parse(fs.readFileSync(configFile,'utf8'));
  const traces=[];
  for(const t of targets){
    const pair=config.pairs.find(x=>x.apk===t.apk);
    if(!pair)throw new Error('Missing pair '+t.apk);
    for(const version of t.versions){
      const trace=extract(pair[version==='8052'?'original':'target'],t.cls,t.method);
      traces.push({apk:t.apk,version,class_method:t.cls+'->'+t.method,...trace});
      console.log('TRACE_METHOD '+t.apk+' '+version+' '+t.cls+'->'+t.method+' '+trace.source_file+':'+trace.start_line);
      for(const line of trace.lines)console.log('TRACE_LINE '+line.line+' '+line.text);
    }
  }
  fs.mkdirSync(outputDir,{recursive:true});
  fs.writeFileSync(path.join(outputDir,'selected_smali_trace.json'),JSON.stringify({meaning:'Static exact smali only; arguments, branches and writes require interpretation',traces},null,2)+'\n');
}
if(require.main===module)main(process.argv[2],process.argv[3]);
module.exports={extract};

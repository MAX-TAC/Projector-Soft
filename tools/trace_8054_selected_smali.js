#!/usr/bin/env node
// Read-only focused smali trace: exact method bodies, never runtime proof.
const fs=require('fs');
const path=require('path');
const targets=[
  {apk:'TvSettingsPlus',cls:'Lcom/android/tv/settings/uicustom/floatmenu/controller/source/EdidSourceSecondaryController;',method:'getEdidVersion()I',versions:['8054']},
  {apk:'TvSettingsPlus',cls:'Lcom/android/tv/settings/uicustom/floatmenu/controller/source/SourceSubMenuController;',method:'getEdidVersion()I',versions:['8054']},
  {apk:'TvSettingsPlus',cls:'Lcom/android/tv/settings/uicustom/floatmenu/controller/source/EdidSourceSecondaryController$1;',method:'onClick(Landroid/view/View;)V',versions:['8054']},
  {apk:'TvSettingsPlus',cls:'Lcom/android/tv/settings/uicustom/floatmenu/controller/source/EdidSourceSecondaryController$2;',method:'onClick(Landroid/view/View;)V',versions:['8054']},
  {apk:'TvSettingsPlus',cls:'Lcom/android/tv/settings/uicustom/floatmenu/controller/source/EdidSourceSecondaryController$3;',method:'onClick(Landroid/view/View;)V',versions:['8054']},
  {apk:'TvSettingsPlus',cls:'Lcom/android/tv/settings/uicustom/floatmenu/controller/source/EdidSourceSecondaryController$4;',method:'onClick(Landroid/view/View;)V',versions:['8054']},
  {apk:'TvSettingsPlus',cls:'Lcom/android/tv/settings/uicustom/fragment/source/EdidSourceFragment$1;',method:'onClick(Landroid/view/View;)V',versions:['8054']},
  {apk:'TvSettingsPlus',cls:'Lcom/android/tv/settings/uicustom/fragment/source/EdidSourceFragment$2;',method:'onClick(Landroid/view/View;)V',versions:['8054']},
  {apk:'TvSettingsPlus',cls:'Lcom/android/tv/settings/uicustom/fragment/source/EdidSourceFragment$3;',method:'onClick(Landroid/view/View;)V',versions:['8054']},
  {apk:'TvSettingsPlus',cls:'Lcom/android/tv/settings/uicustom/fragment/source/EdidSourceFragment$4;',method:'onClick(Landroid/view/View;)V',versions:['8054']},
  {apk:'TvSettingsPlus',cls:'Lcom/android/tv/settings/uicustom/util/ProjectorManager;',method:'setProjectorMirrorMode(Landroid/content/Context;I)V',versions:['8054']},
  {apk:'KuSettings',cls:'Lcom/my/settings/bluetooth/MyAutoBluetoothActivity;',method:'viewAutoBluetoothBak()V',versions:['8052','8054']},
  {apk:'KuSettings',cls:'Lcom/my/settings/bluetooth/MyAutoBluetoothActivity;',method:'refreshDeviceList()V',versions:['8054']},
  {apk:'KuSettings',cls:'Lcom/my/settings/bluetooth/MyAutoBluetoothActivity;',method:'isAutoName(Ljava/lang/String;)Z',versions:['8054']},
  {apk:'KuSettings',cls:'Lcom/my/settings/bluetooth/MyAutoBluetoothActivity;',method:'getIrBluetoothStatus()V',versions:['8054']},
  {apk:'AutoPair',cls:'Lcom/mediatek/autopair/BootBroadcastReceiver;',method:'onReceive(Landroid/content/Context;Landroid/content/Intent;)V',versions:['8054']},
  {apk:'AutoPair',cls:'Lcom/mediatek/autopair/AutoPairBluetoothDevice;',method:'isAutoPairDevice(Landroid/bluetooth/BluetoothDevice;)Z',versions:['8054']},
  {apk:'AutoPair',cls:'Lcom/mediatek/autopair/AutoPairBluetoothDevice;',method:'isDeviceNameMatch(Ljava/lang/String;)Z',versions:['8054']},
  {apk:'AutoPair',cls:'Lcom/mediatek/autopair/AutoPairBluetoothDevice;',method:'createBond(Landroid/bluetooth/BluetoothDevice;)Z',versions:['8054']},
  {apk:'AutoPair',cls:'Lcom/mediatek/autopair/AutoPairBluetoothDevice;',method:'openConnection(Landroid/bluetooth/BluetoothDevice;)V',versions:['8054']},
  {apk:'AutoPair',cls:'Lcom/mediatek/autopair/AutoPairBluetoothDevice;',method:'startScan()V',versions:['8054']},
  {apk:'TvServices',cls:'Lcom/mediatek/tv/agent/TVMenuSettingsService;',method:'<init>()V',versions:['8054']},
  {apk:'TvServices',cls:'Lcom/mediatek/tv/agent/TVMenuSettingsService;',method:'initMapdata()V',versions:['8054']},
  {apk:'TvServices',cls:'Lcom/mediatek/tv/agent/TVMenuSettingsService;',method:'formatSettingToConfigId(Ljava/lang/String;)Ljava/lang/String;',versions:['8054']},
  {apk:'TvServices',cls:'Lcom/mediatek/tv/agent/TVMenuSettingsService;',method:'syncConfigDataToSetting()V',versions:['8054']},
  {apk:'TvServices',cls:'Lcom/mediatek/tv/agent/TVMenuSettingsService;',method:'updateConfigDataChanged(Ljava/lang/String;)V',versions:['8054']},
  {apk:'TvServices',cls:'Lcom/mediatek/tv/agent/TVMenuSettingsService;',method:'updateSettingDB(Ljava/util/Map;)V',versions:['8054']},
  {apk:'TvServices',cls:'Lcom/mediatek/tv/agent/TVMenuSettingsService;',method:'updateSettingDB(Ljava/util/Map;I)V',versions:['8054']},
  {apk:'TvServices',cls:'Lcom/mediatek/tv/agent/TVMenuSettingsService$2;',method:'run()V',versions:['8054']},
  {apk:'TvServices',cls:'Lcom/mediatek/tv/agent/TVMenuSettingsService$3;',method:'run()V',versions:['8054']},
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
    const pair=[...config.pairs,...(config.solo||[])].find(x=>x.apk===t.apk);
    if(!pair)throw new Error('Missing pair '+t.apk);
    for(const version of t.versions){
      const trace=extract(pair[version==='8052'?'original':'target'],t.cls,t.method);
      traces.push({apk:t.apk,version,class_method:t.cls+'->'+t.method,...trace});
      console.log('TRACE_METHOD '+t.apk+' '+version+' '+t.cls+'->'+t.method+' '+trace.source_file+':'+trace.start_line);
      const keyLines=trace.lines.filter(x=>x.text.includes('tv_input_hdmi_edid_version')).map(x=>x.line);
      const logged=trace.lines.length>400 && keyLines.length
        ? trace.lines.filter(x=>keyLines.some(n=>Math.abs(x.line-n)<=35))
        : trace.lines;
      if(logged.length<trace.lines.length)
        console.log('TRACE_WINDOW '+trace.lines.length+' total lines; showing EDID key context');
      for(const line of logged)console.log('TRACE_LINE '+line.line+' '+line.text);
    }
  }
  fs.mkdirSync(outputDir,{recursive:true});
  fs.writeFileSync(path.join(outputDir,'selected_smali_trace.json'),JSON.stringify({meaning:'Static exact smali only; arguments, branches and writes require interpretation',traces},null,2)+'\n');
}
if(require.main===module)main(process.argv[2],process.argv[3]);
module.exports={extract};

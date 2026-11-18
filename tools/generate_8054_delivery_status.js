// Current 8054 delivery inventory. A menu link never counts as a migrated control.
const fs=require('fs');
const root=process.argv[2]||'research/8054';
const graph=JSON.parse(fs.readFileSync(root+'/dependency_graph_8054.json','utf8'));
const states=new Set(['DIRECT_MOD','DELEGATED_OEM','READ_ONLY','HIDDEN','BLOCKED','NOT_IMPLEMENTED']);
const overrides={};
function set(ids,state,path,next,fallback=null){for(const id of ids.split(' '))overrides[id]={delivery:state,implementation:path,next_check:next,fallback};}
set('NAV01','DIRECT_MOD','OemRootMenuHook + Android TV MainFragment','D-pad root navigation on8054');
set('NAV02 NAV03 NAV04 NAV05 NAV06 NAV10','DELEGATED_OEM','OemStandardAndroidNavigation for NAV02-06; retained stock reset page NAV10, no reset action','Confirm target screen and Back navigation, no reset action');
set('NAV07 NAV14','READ_ONLY','OemSystemInfoHook','Compare displayed values to original platform values');
set('NAV08','DIRECT_MOD','OemFontScaleHook','Verify four OEM scale values and persistence');
set('NAV09','DIRECT_MOD','OemScreenTimeoutSafetyHook','Verify Never/no dream policy versus OEM power timer');
set('NAV11','DELEGATED_OEM','Existing OEM update page link, deferred scope','No OTA work/actions in current milestone');
set('NAV12','DELEGATED_OEM','Legacy intent aliases; compatibility diagnostics omitted from the normal 8054 menu','Test Settings key routing without disabling KuSettings');
set('PIC01 PIC02 PIC03 PIC04 PIC05 PIC06 PIC07 PIC08 PIC09 PIC10 PIC11 PIC12','HIDDEN','OemPictureState.mayEdit=false; OEM picture page is reachable','Establish verified PQ/input context before direct writes','DELEGATED_OEM');
set('SND01 SND02 SND03 SND04 SND05 SND06 SND07','DIRECT_MOD','OemSoundPanel / sound guards / OemKeyToneHook / OEM capability gates','SOUND_DEVICE_CHECKLIST_8054.md: mode/output/focus, readback and audible effect','DELEGATED_OEM');
set('SND11','DELEGATED_OEM','OEM SOUND_SETTINGS and stock AudioOutputActivity','Verify CEC/ARC/eARC and output-specific behavior');
set('SND12','NOT_IMPLEMENTED','OEM_ROUTE_UNCONFIRMED: speaker/SPDIF config keys only','Find the stock controller and range if available on 8054');
set('SND13 DBL05','HIDDEN','OemMenuVisibilityHook hides BT speaker entry only; service retained','Deferred speaker-mode scope; no service disable');
set('HDMI01 HDMI02','READ_ONLY','OemHdmiPortInfo HW6/HW7; OEM source screen','Confirm physical port identity, active selection and signal','DELEGATED_OEM');
set('HDMI03 HDMI04 HDMI05','DELEGATED_OEM','OEM source page; direct EDID/CEC gated on8054','EDID per-port routing, CEC/eARC side effects and keystone preservation');
set('HDMI06','DIRECT_MOD','OemHdmiAutoPlayHook','Verify OEM autoplay state and startup side effects');
set('HDMI07','DIRECT_MOD','OemPowerOnSourceHook; boot property only','Verify exact8054 accepted IDs and cold boot per input');
set('HDMI08','DIRECT_MOD','OemPowerOnAppHook','Verify target activity and persisted OEM property');
set('HDMI09','DELEGATED_OEM','OEM source USB page','Deferred player scope, navigation only');
set('PRJ01 PRJ04 PRJ06 PRJ07 PRJ08 PRJ09','DELEGATED_OEM','OemProjectionPanel: checked stock projection entry and original correction UI; manual focus uses remote keys','Confirm the original subcontrols, fullscreen routes and return path');
set('PRJ02 PRJ03','DIRECT_MOD','OemFocusPreferences: two stock callbacks and property readback','Verify autofocus behavior separately for startup and movement','DELEGATED_OEM');
set('PRJ05','DIRECT_MOD','OemMoveKeystonePreference: stock callback bound to original layout and property readback','Verify OEM view inflation and motion correction without changing manual correction','DELEGATED_OEM');
set('PRJ10','DELEGATED_OEM','Original projection menu; standalone DigitalZoomActivity launch is unconfirmed on 8054','Verify stock digital zoom entry and Back/D-pad');
set('PRJ11','DIRECT_MOD','OemLightSourcePreference: four KuSourceRJ modes for the verified customer-zero runtime path','Verify available customer profile, OEM readback, actual brightness and thermal behavior','DELEGATED_OEM');
set('SYS01','DIRECT_MOD','OemAcPowerBootHook','Verify power restoration behavior without modifying thermal services');
set('SYS02 SYS06','READ_ONLY','OemBluetoothRemoteHook / OEM state, no replacement reconnect service','Verify HID remote pairing and retained AutoPair/reconnect owners');
set('SYS03 SYS05','BLOCKED','8054 automatic recovery replacement disabled','Do not replace TimeService/source boot owner without proven transaction/rollback');
set('SYS04','READ_ONLY','OemThermalTelemetryHook','Compare sensor names/readings, no fan control');
set('SYS07','NOT_IMPLEMENTED','OEM_ROUTE_UNCONFIRMED: stock shutdown service and tip dialog only','Find an editable stock timer entry on 8054');
set('SYS08','DELEGATED_OEM','Existing OEM hotkey owner and MOD legacy Intent handlers','Verify physical Settings button resolution with KuSettings enabled');
set('SYS09','READ_ONLY','Package/compatibility diagnostics; app list remains stock','Check inventory and OEM package visibility');

const rows=graph.features.map(f=>{
  let record=overrides[f.id];
  if(!record)record={delivery:f.status==='BLOCKED'?'BLOCKED':'NOT_IMPLEMENTED',implementation:'No current MOD path demonstrated',next_check:f.status==='DEFERRED'?'Out of current scope':f.missing_verification,fallback:null};
  const implemented=['DIRECT_MOD','DELEGATED_OEM','READ_ONLY','HIDDEN'].includes(record.delivery);
  return {id:f.id,function:f.function,...record,build_state:implemented?'BUILT_UNTESTED':null,
    runtime_state:implemented?'RUNTIME_TEST_REQUIRED':null,feature_complete:false,
    scope:f.status==='DEFERRED'?'DEFERRED':'CURRENT',evidence_status:f.status};
});
if(rows.length!==96||new Set(rows.map(r=>r.id)).size!==96||rows.some(r=>!states.has(r.delivery)||!r.next_check))throw Error('Invalid delivery inventory');
const counts=rows.reduce((a,r)=>(a[r.delivery]=(a[r.delivery]||0)+1,a),{});
fs.writeFileSync(root+'/delivery_status_8054.json',JSON.stringify({meaning:'Current code delivery; no MIGRATED/FEATURE_COMPLETE claim. BUILT_UNTESTED refers to compiled conditional path, not hardware availability. Group OEM navigation does not prove individual feature presence. Null build/runtime state means no implemented feature to verify.',counts,features:rows},null,2)+'\n');
fs.writeFileSync(root+'/DELIVERY_STATUS_8054.md',[
  '# Текущая доставка 96 функций 8054','',
  'DIRECT_MOD — код контрола/платформенной интеграции; DELEGATED_OEM — только штатная навигация; READ_ONLY — только информация; HIDDEN — прямой MOD-контрол скрыт; BLOCKED — запрещённый путь; NOT_IMPLEMENTED — реализации MOD нет. OEM ссылка не MIGRATED и не FEATURE_COMPLETE. Наличие отдельного подменю и аппаратуры надо подтвердить на устройстве. BUILT_UNTESTED относится к условному пути в APK, а не к переносу всей функции. — означает отсутствие активной реализации, не успешную сборку функции. Все hardware результаты отсутствуют.', '',
  Object.entries(counts).map(([k,v])=>k+' '+v).join('; '),'',
  '| ID | Функция | Доставка | Fallback | Код | Аппаратура | Следующая проверка |','|---|---|---|---|---|---|---|',
  ...rows.map(r=>'| '+[r.id,r.function,r.delivery,r.fallback||'—',r.build_state||'—',r.runtime_state||'—',r.next_check].map(x=>String(x).replace(/\|/g,'/')).join(' | ')+' |'),''
].join('\n'));
console.log('PASS delivery inventory: '+JSON.stringify(counts)+'; 0 runtime verified, 0 feature complete');



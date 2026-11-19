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
set('PIC01 PIC02 PIC03 PIC04 PIC05 PIC06 PIC07 PIC08 PIC09 PIC10 PIC11 PIC12','READ_ONLY','OemPicturePanel native stored-value rows; editing BLOCKED_BY_RUNTIME','Establish the active input/PQ context and the stock callback/limits before direct writes');
set('SND01 SND02 SND03 SND04 SND05 SND06 SND07','DIRECT_MOD','OemSoundPanel / sound guards / OemKeyToneHook / OEM capability gates','SOUND_DEVICE_CHECKLIST_8054.md: mode/output/focus, readback and audible effect','DELEGATED_OEM');
set('SND08','NOT_IMPLEMENTED','No MOD editor; exact Factory PEQ uses sound mode, band index and saveAudioIni','Require a stock consumer editor or proven output/profile, units and restoration');
set('SND09','DELEGATED_OEM','Stock AdvancedFragment through sound_dap and the original Dolby Atmos capability set','Verify eligible Dolby type, native fragment navigation and output');
set('SND10','DELEGATED_OEM','Stock DTSFragment with isAdvanceFunctionSupport and sound_dts_truvolumehd in its group','Verify DTS loudness row, actual effect and manual restore for supported output');
set('SND11','DELEGATED_OEM','OEM SOUND_SETTINGS and stock AudioOutputActivity','Verify CEC/ARC/eARC and output-specific behavior');
set('SND12','DIRECT_MOD','Native speaker and SPDIF delay with exact OEM availability guards, provider and MediaTek readback','Verify per-output min/max, pending readback and audio timing on 8054','DELEGATED_OEM');
set('SND13 DBL05','HIDDEN','OemMenuVisibilityHook hides BT speaker entry only; service retained','Deferred speaker-mode scope; no service disable');
set('HDMI01 HDMI02','DIRECT_MOD','OemHdmiPortInfo HW6/HW7: verified input ID and stock LiveTV ACTION_VIEW passthrough','Confirm LiveTV resolves, actual port/signal, no-signal behavior and Back on 8054');
set('HDMI03','READ_ONLY','OemHdmiEdidHook displays the single stored Global code; OEM editor retained','Verify EDID target scope for both inputs, renegotiation and restore','DELEGATED_OEM');
set('HDMI04','DIRECT_MOD','OemCecAutoOff8054: main CEC status and native standby-on-sleep switch; main CEC remains OEM','Verify CEC availability, standby behavior, ARC/eARC and original power confirmation','DELEGATED_OEM');
set('HDMI05','DELEGATED_OEM','OEM source game mode; resetTrapezoid is part of enable','Trace reset scope and restore correction before allowing a native toggle');
set('HDMI06','DIRECT_MOD','OemHdmiAutoPlayHook','Verify OEM autoplay state and startup side effects');
set('HDMI07','DIRECT_MOD','OemPowerOnSourceHook; boot property only','Verify exact8054 accepted IDs and cold boot per input');
set('HDMI08','DIRECT_MOD','OemPowerOnAppHook','Verify target activity and persisted OEM property');
set('HDMI09','DELEGATED_OEM','OEM source USB page','Deferred player scope, navigation only');
set('PRJ01 PRJ04','DELEGATED_OEM','OemProjectionPanel: checked stock projection entry; manual focus uses OEM remote keys','Confirm placement and focus return through the stock UI');
set('PRJ08','DELEGATED_OEM','Stock projection entry; movement correction gates the obstacle toggle','Confirm sensor capability and behavior; stock keystone initialization clears dependent values when movement correction is off');
set('PRJ09','DELEGATED_OEM','Stock projection entry; customer 0 opens AutomaticAdmissionActivity instead of the stock switch','Confirm customer ID, sensor support and geometry restore; keep stock route');
set('PRJ02 PRJ03','DIRECT_MOD','OemFocusPreferences: two stock callbacks, stale-state gate and property readback on return','Verify autofocus behavior separately for startup and movement','DELEGATED_OEM');
set('PRJ05','DIRECT_MOD','OemMoveKeystonePreference: stock callback with original layout, stale-state gate and all-dependents-off guard for disabling','Verify storage and OEM fallback separately; active or unknown dependents require the stock UI','DELEGATED_OEM');
set('PRJ06','DELEGATED_OEM','Separate FourCornerActivity via the verified stock FOUR_CORNER action','Verify opening and return without geometry change, D-pad and manual restore');
set('PRJ07','DELEGATED_OEM','Separate HVKeystoneActivity via the verified stock HV action','Verify axis controls, D-pad/Back and restore shared geometry');
set('PRJ10','DELEGATED_OEM','Separate DigitalZoomActivity via the verified stock DIGITAL_ZOOM action','Verify zoom bounds, shared geometry, Back/D-pad and restore');
set('PRJ11','DIRECT_MOD','OemLightSourcePreference: four KuSourceRJ modes for the verified customer-zero runtime path','Verify available customer profile, OEM readback, actual brightness and thermal behavior','DELEGATED_OEM');
set('SYS01','DIRECT_MOD','OemAcPowerBootHook','Verify power restoration behavior without modifying thermal services');
set('SYS02 SYS06','READ_ONLY','OemBluetoothRemoteHook / OEM state, no replacement reconnect service','Verify HID remote pairing and retained AutoPair/reconnect owners');
set('SYS03 SYS05','BLOCKED','8054 automatic recovery replacement disabled','Do not replace TimeService/source boot owner without proven transaction/rollback');
set('SYS04','READ_ONLY','OemThermalTelemetryHook','Compare sensor names/readings, no fan control');
set('SYS07','DELEGATED_OEM','OemStockUserSettings opens exact-stock TimerSetActivity and TimerFragment power schedule','Verify editor navigation, permitted schedule edit, power-off trigger and manual restore; separate from idle policy');
set('SYS08','DELEGATED_OEM','Existing OEM hotkey owner and MOD legacy Intent handlers','Verify physical Settings button resolution with KuSettings enabled');
set('SYS09','READ_ONLY','Package/compatibility diagnostics; app list remains stock','Check inventory and OEM package visibility');

const rows=graph.features.map(f=>{
  let record=overrides[f.id];
  if(!record)record={delivery:f.status==='BLOCKED'?'BLOCKED':'NOT_IMPLEMENTED',implementation:'No current MOD path demonstrated',next_check:f.status==='DEFERRED'?'Out of current scope':f.missing_verification,fallback:null};
  const implemented=['DIRECT_MOD','DELEGATED_OEM','READ_ONLY','HIDDEN'].includes(record.delivery);
  const picturePreview=record.implementation.startsWith('OemPicturePanel ');
  return {id:f.id,function:f.function,...record,build_state:picturePreview?'SOURCE_ONLY':implemented?'BUILT_UNTESTED':null,
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



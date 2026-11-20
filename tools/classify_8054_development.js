// Development priority is independent of historical evidence confidence.
const fs=require('fs');
const root=process.argv[2]||'research/8054';
const graph=JSON.parse(fs.readFileSync(root+'/dependency_graph_8054.json','utf8'));
const critical=new Set(['NAV09','NAV10','SYS01','SYS02','SYS03','SYS04','SYS05','SYS06','SYS07','SYS08',
 'PRJ01','PRJ02','PRJ03','PRJ04','PRJ05','PRJ06','PRJ07','PRJ08','PRJ09','PRJ11','HDMI05','HDMI07']);
const standard=new Set(['NAV01','NAV02','NAV03','NAV04','NAV05','NAV06','NAV07','NAV08','NAV12','NAV14','SYS09','SND06']);
const rows=graph.features.map(f=>({id:f.id,function:f.function,
 category:critical.has(f.id)||f.id.startsWith('FAC')?'CRITICAL':standard.has(f.id)?'STANDARD_ANDROID':'USER_SETTING',
 scope:f.status==='DEFERRED'?'DEFERRED':f.status==='BLOCKED'?'BLOCKED':'IN_SCOPE',
 evidence_status:f.status,dependencies:f.backend_hint,
 implementation_status:'SEE_MOD_V33_GAP_REPORT',runtime_status:'RUNTIME_TEST_REQUIRED',
 completion_rule:critical.has(f.id)||f.id.startsWith('FAC')?'Detailed available call and side-effect review':
 standard.has(f.id)?'Android API/Intent; inspect OEM exceptions only':'Grouped UI to Settings/API to observer/config plus bounds and rollback; closed HAL not required'}));
if(rows.length!==96)throw Error('Expected 96 functions');
fs.writeFileSync(root+'/development_queue_8054.json',JSON.stringify({strategy:'2026-11-17 revised by owner',features:rows},null,2)+'\n');
console.log('Classified '+rows.length+' functions; evidence statuses preserved');

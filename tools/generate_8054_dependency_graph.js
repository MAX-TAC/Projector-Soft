#!/usr/bin/env node
// Converts the handoff inventory into an evidence-labelled work queue.
// A manifest declaration or DEX literal is never promoted to a runtime call.
const fs = require('fs');
const path = require('path');

function csv(file) {
  const input = fs.readFileSync(file, 'utf8').replace(/^\uFEFF/, '');
  const rows = [];
  let row = [], field = '', quoted = false;
  for (let i = 0; i < input.length; i++) {
    const c = input[i];
    if (quoted) {
      if (c === '"' && input[i + 1] === '"') { field += '"'; i++; }
      else if (c === '"') quoted = false;
      else field += c;
    } else if (c === '"') quoted = true;
    else if (c === ',') { row.push(field); field = ''; }
    else if (c === '\n') {
      row.push(field.replace(/\r$/, '')); field = '';
      if (row.some(Boolean)) rows.push(row);
      row = [];
    } else field += c;
  }
  if (quoted) throw new Error(`Unclosed CSV quote: ${file}`);
  if (field || row.length) { row.push(field); rows.push(row); }
  const header = rows.shift();
  return rows.map((values, i) => {
    if (values.length !== header.length)
      throw new Error(`${file}:${i + 2}: ${values.length} fields, expected ${header.length}`);
    return Object.fromEntries(header.map((key, n) => [key, values[n]]));
  });
}

function main(inputDir, outputDir) {
  const read = name => csv(path.join(inputDir, name));
  const apk = read('APK_INVENTORY_8054.csv');
  const components = read('MANIFEST_COMPONENTS_8054.csv');
  const literals = read('DEX_REFERENCE_INDICATORS_8054.csv');
  const matrix = read('SETTINGS_MIGRATION_MATRIX_8054.csv');
  const exactCalls = JSON.parse(fs.readFileSync(path.join(outputDir, 'exact_smali_calls_8054.json'), 'utf8')).calls;
  const features = matrix.map(item => ({
    id: item.id,
    section: item.section,
    function: item.function,
    priority: item.priority,
    owner_hint: item.owner,
    entry_point_hint: item.entry_point,
    backend_hint: item.original_backend,
    side_effects_hint: item.side_effects,
    source_file: null,
    class_method: null,
    line_or_smali: null,
    call_kind: null,
    target: null,
    arguments: null,
    precondition: null,
    confidence: 'UNKNOWN',
    status: item.priority === 'BLOCK' ? 'BLOCKED' :
            item.priority === 'DEFERRED' ? 'DEFERRED' : 'UNKNOWN',
    evidence_hint: item.evidence,
    evidence: [{kind: 'HANDOFF_MATRIX_ROW', source: 'SETTINGS_MIGRATION_MATRIX_8054.csv', row_id: item.id}],
    missing_verification: item.missing_verification,
    runtime_test: item.acceptance_test,
    rollback: item.rollback,
    risk: item.risk
  }));
  const partialTrace = {
    HDMI03: {
      source_file:'TvSettingsPlus smali_classes14/.../EdidSourceSecondaryController.smali, SourceSubMenuController.smali, EdidSourceSecondaryController$1..$4.smali, EdidSourceFragment$1..$4.smali; TvServices smali/com/mediatek/tv/agent/TVMenuSettingsService.smali',
      class_method:'EdidSourceSecondaryController#getEdidVersion; SourceSubMenuController#getEdidVersion; eight EDID radio onClick(View) handlers; TVMenuSettingsService#initMapdata/#syncConfigDataToSetting/#updateSettingDB/#updateConfigData; TVMenuSettingsService$1/$2/SettingObserver#run/onChange; TVSettingConfig#setConifg',
      line_or_smali:'getters:129-154 and 300-344; EDID onClick:57-74/57-70; TvServices Map.put:3051-3058, Global BASE_URI:205-207, observer:52-205, updateConfigData:4986-5376, config setter:472-508',
      call_kind:'DEX_INVOKE_CHAINS',
      target:'MtkTvAVModeBase#getVideoInfoData; MtkTvVideoInfoBase#getU8EdidIndex; SettingsUtil#putGlobalValueInt; TvServices itemIds Map#put; Settings.Global observer; TVSettingConfig#setConifg; MtkTvConfigBase#setConfigValue',
      arguments:'getVideoInfoData first argument 0x6; Global key tv_input_hdmi_edid_version values 0..3; itemIds maps key to g_fusion_picture__hdmi_edid_version; config to Global sync and generic Global observer to MTK setter; no physical port argument in UI handlers',
      precondition:'EDID membership in observed key array, dynamic generic-branch reachability, native/HAL effect, HW6/HW7 mapping and hotplug readback remain unverified',
      source_apk_sha256:'TvSettingsPlus f8e0c6fe0f46e7eb8db651c31d85ef8b32a769b37e4dd0f96ed19a64725d74ee; TvServices 2934ca9334afd0a2af85786aa0593d27da216086c7eb9b0c9b801f412516b77d'
    },
    PRJ01: {
      source_file:'TvSettingsPlus smali_classes14/.../ProjectorManager.smali; Factory smali/.../ApiService.smali and ProjectorManager.smali',
      class_method:'TvSettingsPlus.ProjectorManager#setProjectorMirrorMode(Context,int) -> Factory.ApiService#onStartCommand -> Factory.ProjectorManager#setProjectorMirrorMode(int)',
      line_or_smali:'TvSettingsPlus:263-304; Factory ApiService:53-100; Factory ProjectorManager:285-428',
      call_kind:'DEX_INVOKE_CHAIN',
      target:'explicit Factory ApiService; MtkTvConfig#setConfigValue; ProjectorManager#i2c; SystemProperties#set; MtkTvFApiDisplay#setMirrorMethod',
      arguments:'Intent extra projector_mode_value; Factory default 0; mode switch 0..3; g_misc__mirror, g_misc__flip, persist.rjpj.projector.mode',
      precondition:'Static path only; underlying I2C register writes and runtime permissions remain unverified',
      source_apk_sha256:'TvSettingsPlus f8e0c6fe0f46e7eb8db651c31d85ef8b32a769b37e4dd0f96ed19a64725d74ee; Factory 2f0e2cbc133c97cb4b1383f7a4576fab78715dae72a8e590deb479a4196e0996'
    },
    SYS02: {
      source_file:'KuSettings smali_classes5/com/my/settings/bluetooth/MyAutoBluetoothActivity.smali; AutoPair smali/com/mediatek/autopair/BootBroadcastReceiver.smali and AutoPairBluetoothDevice.smali',
      class_method:'MyAutoBluetoothActivity#refreshDeviceList; BootBroadcastReceiver#onReceive; AutoPairBluetoothDevice#startScan/#openConnection/#createBond',
      line_or_smali:'KuSettings:348-563; AutoPair receiver:268-452; AutoPair device:572-650,1722-1879,2234-2286,2741-2935',
      call_kind:'DEX_INVOKE_CHAINS',
      target:'CachedBluetoothDevice#startPairing/#connect(true); Context#startService(AutoPairService); BluetoothLeScanner#startScan; BluetoothConnector#openConnection',
      arguments:'KuSettings HID major 0x500 and Remote/BT_Remote name filter; AutoPair autopair_status default 1, BLE name filters from AutoPairConfig',
      precondition:'Static paths only; AutoPairConfig values, service interactions, runtime permissions and connection results unverified',
      source_apk_sha256:'AutoPair b3479fe8f873450b1953cedd21d5804a40a5c5cd8edf868cd15ea2b1d7ab5a6f; KuSettings extracted from backup ZIP 4f53f2224c98b4c832a19a591b9dd7f925bde28528b5674a09732c0f0e41d362 (per-APK hash not logged)'
    }
  };
  for (const feature of features) if (partialTrace[feature.id]) {
    Object.assign(feature, partialTrace[feature.id], {confidence:'DEX_INVOKE_STATIC',status:'PARTIAL_STATIC_TRACE'});
    feature.evidence.push({kind:'EXACT_SMALI_TRACE',
      source:feature.id==='SYS02' ? 'Projector-Soft Actions runs 44098788358 and 44103425107' :
             feature.id==='HDMI03' ? 'Projector-Soft Actions runs 44097233248, 44106214713, 44107731141, 44109768484, 44111398773, 44112728599, 44115028456 and 44115598445' :
             'Projector-Soft Actions run 44097233248',
      report:feature.id==='SYS02' ? 'BLUETOOTH_REMOTE_8054_STATIC_TRACE.md' :
             feature.id==='HDMI03' ? 'EDID_GLOBAL_ROUTE_8054.md' : 'SELECTED_SMALI_TRACE_FINDINGS_8054.md'});
  }
  const graph = {
    schema_version: 1,
    firmware: 'FlixTone D11 Master 8054 / MT9660 / Android TV 13',
    provenance: 'Handoff metadata, curated exact 8054 invokes from run 44094937995, selected method bodies from runs 44097233248, 44098788358, 44103425107, 44106214713 and 44107731141. Runtime behavior remains unverified.',
    evidence_rule: 'VERIFIED requires an exact APK hash, source method, line or smali instruction, call target and arguments, plus a reproducible read-only test.',
    apk_inventory: apk.map(x => ({partition:x.partition, apk_path:x.apk_path, package:x.package,
      cert_sha256:x.cert_sha256, shared_user_id:x.sharedUserId,
      manifest_counts: {activities:+x.activities, services:+x.services, receivers:+x.receivers, providers:+x.providers}})),
    manifest_components: components.map(x => ({package:x.package, apk_path:x.apk_path,
      type:x.component_type, name:x.name, exported_raw:x.exported || null,
      permission:x.permission || null, process:x.process || null, enabled_raw:x.enabled || null,
      confidence:'MANIFEST_DECLARATION_ONLY'})),
    dex_literal_indicators: literals.map((x,i) => ({id:`LIT${String(i+1).padStart(3,'0')}`,
      source_package:x.source_package, source_apk:x.source_apk, needle_name:x.needle_name,
      matched_literal:x.matched_literal, count:+x.count, confidence:'DEX_STRING_ONLY',
      call_kind:null, source_method:null})),
    decompiled_calls: [
      {id:'FACTORY_ORIENTATION_ENTRY', source_apk_sha256:'2f0e2cbc133c97cb4b1383f7a4576fab78715dae72a8e590deb479a4196e0996',
        source_file:'mediatek/tvsetting/factory/ui/ApiService.java',
        source_method:'ApiService#onStartCommand', line_or_smali:'JADX line 22', call_kind:'JAVA_INVOKE',
        target:'ProjectorManager#setProjectorMirrorMode', arguments:'Intent extra projector_mode_value, default 0',
        side_effects:'Delegates orientation transaction', confidence:'DECOMPILED_JAVA_CALLSITE',
        evidence:'SOURCE_Factory_JADX_Callsite_Report.md:3967-3980',
        runtime_test:'Read-only inspect service registration and current mode; no write without owner approval',
        rollback:'No action during static audit'},
      {id:'FACTORY_ORIENTATION_TRANSACTION', source_apk_sha256:'2f0e2cbc133c97cb4b1383f7a4576fab78715dae72a8e590deb479a4196e0996',
        source_file:'mediatek/tvsetting/factory/ui/ProjectorManager.java',
        source_method:'ProjectorManager#setProjectorMirrorMode', line_or_smali:'JADX lines 41-71', call_kind:'JAVA_INVOKE',
        target:'MtkTvConfig#setConfigValue; I2CDevice#writeRegister; SystemProperties#set; MtkTvFApiDisplay#setMirrorMethod',
        arguments:'mode 0..3; mirror/flip flags; I2C branch; persist.rjpj.projector.mode',
        side_effects:'Display orientation, persistent property and I2C writes; unsafe to replay without hardware validation',
        confidence:'DECOMPILED_JAVA_CALLSITE', evidence:'SOURCE_Factory_JADX_Callsite_Report.md:3988-4060',
        runtime_test:'Read current mode only; controlled orientation test requires owner approval and stock fallback',
        rollback:'Original Factory ApiService and recorded prior mode, only after authorized hardware test'},
      {id:'FACTORY_PEQ_WRITE', source_apk_sha256:'2f0e2cbc133c97cb4b1383f7a4576fab78715dae72a8e590deb479a4196e0996',
        source_file:'mediatek/tvsetting/factory/ui/designmenu/PEQAdjustViewHolder.java',
        source_method:'PEQAdjustViewHolder#onClick (JADX switch body; method signature must be checked)',
        line_or_smali:'JADX lines 363-364 and 399-400', call_kind:'JAVA_INVOKE',
        target:'MtkTvFApiAudio#setPeqFreq; MtkTvFApiAudio#saveAudioIni',
        arguments:'sound mode; band indices 0 and 12; short frequency value',
        side_effects:'Persists factory audio calibration; do not invoke', confidence:'DECOMPILED_JAVA_CALLSITE',
        evidence:'SOURCE_Factory_JADX_Callsite_Report.md:2239-2263',
        runtime_test:'Read-only PEQ capability and getter audit; no write test', rollback:'Private factory audio backup before any future authorized write'},
      {id:'FACTORY_WATCHDOG_WRITE', source_apk_sha256:'2f0e2cbc133c97cb4b1383f7a4576fab78715dae72a8e590deb479a4196e0996',
        source_file:'mediatek/tvsetting/factory/ui/designmenu/OtherOptionAdjustViewHolder.java',
        source_method:'OtherOptionAdjustViewHolder#method unknown', line_or_smali:'JADX lines 322,465',
        call_kind:'JAVA_INVOKE', target:'MtkTvFApiSystem#setWatchDog', arguments:'watchdogindex != 0',
        side_effects:'Changes system watchdog; do not invoke', confidence:'DECOMPILED_JAVA_CALLSITE',
        evidence:'SOURCE_Factory_JADX_Callsite_Report.md:3392-3431',
        runtime_test:'None; prohibited write', rollback:'No execution authorized'}
    ],
    exact_smali_calls: exactCalls,
    features
  };
  const ids = new Set();
  for (const feature of features) {
    if (!feature.id || ids.has(feature.id)) throw new Error(`Duplicate or empty feature ID ${feature.id}`);
    ids.add(feature.id);
  }
  for (const call of graph.decompiled_calls) {
    if (ids.has(call.id)) throw new Error(`Duplicate call ID ${call.id}`);
    ids.add(call.id);
    if (call.confidence === 'VERIFIED' && (!call.source_apk_sha256 ||
        !call.source_method || !call.line_or_smali || !call.target || !call.arguments))
      throw new Error(`Unsupported VERIFIED call ${call.id}`);
  }
  for (const call of exactCalls) {
    if (ids.has(call.id)) throw new Error(`Duplicate call ID ${call.id}`);
    ids.add(call.id);
    if (call.confidence !== 'DEX_INVOKE_STATIC' || !call.source_method ||
        !call.source_file || !call.line_or_smali || !call.call_kind || !call.target ||
        typeof call.target_declared_in_same_apk !== 'boolean' || call.arguments !== null)
      throw new Error(`Invalid static invoke evidence ${call.id}`);
  }
  if (apk.length !== 88 || components.length !== 1389 || literals.length !== 79 || features.length !== 96)
    throw new Error(`Unexpected handoff coverage ${apk.length}/${components.length}/${literals.length}/${features.length}`);
  for (const feature of features) {
    if (feature.status === 'VERIFIED' && (!feature.source_file || !feature.class_method ||
        !feature.line_or_smali || !feature.call_kind || !feature.target || !feature.arguments))
      throw new Error(`Unsupported VERIFIED: ${feature.id}`);
  }
  fs.mkdirSync(outputDir, {recursive:true});
  fs.writeFileSync(path.join(outputDir,'dependency_graph_8054.json'), JSON.stringify(graph,null,2)+'\n');
  const byStatus = Object.groupBy ? Object.groupBy(features, x => x.status) :
    features.reduce((a,x) => ((a[x.status] ||= []).push(x),a),{});
  const md = [
    '# Карта зависимостей FlixTone 8054 — контроль доказательств', '',
    'Карта объединяет handoff и выбранные точные smali invoke из оригинальных APK. Имена владельцев/backend в матрице остаются гипотезами; статический вызов не доказывает исполнение на устройстве.', '',
    `Покрытие инвентаря: ${apk.length} APK, ${components.length} компонентов Manifest, ${literals.length} индикаторов DEX-строк, ${features.length} функций.`,
    `Статусы: ${Object.entries(byStatus).map(([k,v])=>`${k} ${v.length}`).join('; ')}. Точных статических smali invoke 8054: ${exactCalls.length}; в Factory отчёте есть ${graph.decompiled_calls.length} прослеженных JADX-вызова.`, '',
    'Пустое exported в Manifest означает неизвестное значение. Каждая DEX-строка имеет статус DEX_STRING_ONLY.', '',
    '| ID | Функция | Приоритет | Статус | Владелец (гипотеза) | Что проверить |',
    '|---|---|---|---|---|---|',
    ...features.map(x=>`| ${x.id} | ${x.function.replace(/\|/g,'/')} | ${x.priority} | ${x.status} | ${x.owner_hint.replace(/\|/g,'/')} | ${x.missing_verification.replace(/\|/g,'/')} |`),
    '', '## Следующая операция', '',
    'Расширить exact invoke/Intent/Binder трассировку за пределы выбранных 32 вызовов, восстановить аргументы и ветвления из smali, затем проверить runtime только после разрешения владельца.', ''
  ].join('\n');
  fs.writeFileSync(path.join(outputDir,'dependency_graph_8054.md'),md);
  console.log(`Validated ${apk.length} APK, ${components.length} components, ${literals.length} literals, ${features.length} features; ${exactCalls.length} static invokes, 0 runtime-verified calls`);
}
if (require.main === module) {
  const [inputDir, outputDir] = process.argv.slice(2);
  if (!inputDir || !outputDir) throw new Error('Usage: node generate_8054_dependency_graph.js <handoff-dir> <output-dir>');
  main(inputDir, outputDir);
}
module.exports = {csv, main};

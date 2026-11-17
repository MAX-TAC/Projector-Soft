#!/usr/bin/env node
const fs = require('fs');
const file = process.argv[2];
if (!file) throw new Error('Usage: node validate_8054_graph.js <dependency_graph_8054.json>');
const graph = JSON.parse(fs.readFileSync(file,'utf8'));
const expected = {apk_inventory:88, manifest_components:1389,
  dex_literal_indicators:79, features:96};
for (const [key,count] of Object.entries(expected)) {
  if (!Array.isArray(graph[key]) || graph[key].length !== count)
    throw new Error(`${key}: expected ${count}, got ${graph[key]?.length}`);
}
const ids = new Set();
for (const entry of [...graph.features,...graph.dex_literal_indicators,...graph.decompiled_calls]) {
  if (!entry.id || ids.has(entry.id)) throw new Error(`Duplicate or empty ID ${entry.id}`);
  ids.add(entry.id);
  if (entry.confidence === 'VERIFIED' || entry.status === 'VERIFIED') {
    if (!entry.source_method && !entry.class_method)
      throw new Error(`${entry.id}: unsupported VERIFIED; missing source method`);
    for (const field of ['source_file','line_or_smali','call_kind','target','arguments']) {
      if (!entry[field]) throw new Error(`${entry.id}: unsupported VERIFIED; missing ${field}`);
    }
    if (!entry.evidence || !entry.runtime_test || !entry.rollback)
      throw new Error(`${entry.id}: VERIFIED requires evidence, test and rollback`);
  }
}
for (const item of graph.dex_literal_indicators) {
  if (item.confidence !== 'DEX_STRING_ONLY' || item.call_kind || item.source_method)
    throw new Error(`${item.id}: DEX literal cannot imply a call`);
}
for (const item of graph.manifest_components) {
  if (item.confidence !== 'MANIFEST_DECLARATION_ONLY')
    throw new Error(`Manifest component ${item.name} has unsupported confidence`);
}
console.log(`PASS: ${graph.apk_inventory.length} APK, ${graph.manifest_components.length} components, ${graph.features.length} features; no unsupported VERIFIED`);

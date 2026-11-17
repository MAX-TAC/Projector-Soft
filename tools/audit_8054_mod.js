#!/usr/bin/env node
// Conservative source/integration inventory. Hardware status is never inferred.
const fs = require('fs');
const path = require('path');
const {csv} = require('./generate_8054_dependency_graph');
const root = path.resolve(__dirname, '..');
const dir = path.join(root,'app/tvsettingsplus-mod/src/main/java/dev/projectorsoft/tvsettings');
const testDir = path.join(root,'app/tvsettingsplus-mod/src/test/java/dev/projectorsoft/tvsettings');
const input = process.argv[2];
const output = process.argv[3];
if (!input || !output) throw new Error('Usage: node audit_8054_mod.js <matrix.csv> <report.md>');
const sourceFiles = fs.readdirSync(dir).filter(x=>x.endsWith('.java')).sort();
const tests = new Set(fs.readdirSync(testDir).filter(x=>x.endsWith('Test.java')));
const baseline = sourceFiles.filter(x=>x !== 'EdidPortPolicy.java');
if (baseline.length !== 47) throw new Error(`Expected 47 v33 Java classes, got ${baseline.length}`);
const names = sourceFiles.map(x=>x.slice(0,-5));
const textByName = Object.fromEntries(names.map(n=>[n,fs.readFileSync(path.join(dir,n+'.java'),'utf8')]));
const roots = ['OemRootMenuHook','OemInlinePreferencesHook','OemLaunchRouteHook',
  'LegacyBrightnessUiHook','OemMenuVisibilityHook','KuSourceRecoveryBootReceiver'];
const reachable = new Set(roots), queue = [...roots];
while (queue.length) {
  const name = queue.shift();
  for (const candidate of names) {
    if (!reachable.has(candidate) && new RegExp(`\\b${candidate}\\b`).test(textByName[name])) {
      reachable.add(candidate);
      queue.push(candidate);
    }
  }
}
const matrix = csv(input);
const explicitStatus = {
  NAV02:'BUILT_UNTESTED', NAV03:'BUILT_UNTESTED', NAV04:'BUILT_UNTESTED',
  NAV05:'BUILT_UNTESTED', NAV06:'BUILT_UNTESTED', NAV11:'BUILT_UNTESTED',
  SND12:'SOURCE_ONLY', HDMI09:'BUILT_UNTESTED', SYS06:'BUILT_UNTESTED',
  SYS09:'STATIC_VERIFIED'
};
const status = row => {
  if (row.priority === 'BLOCK') return 'BLOCKED';
  if (row.priority === 'DEFERRED') return 'NOT_IMPLEMENTED';
  if (explicitStatus[row.id]) return explicitStatus[row.id];
  const found = names.filter(n=>new RegExp(`\\b${n}\\b`).test(row.current_mod));
  if (!found.length) return 'NOT_IMPLEMENTED';
  return found.some(n=>reachable.has(n) && n!=='EdidPortPolicy') ? 'BUILT_UNTESTED' : 'SOURCE_ONLY';
};
const table = matrix.map(row=>({row,found:names.filter(n=>new RegExp(`\\b${n}\\b`).test(row.current_mod)),status:status(row)}));
const counts = Object.entries(table.reduce((a,x)=>{
  a[x.status] = (a[x.status] || 0) + 1;
  return a;
},{}));
const md = [
  '# TvSettingsPlus MOD v33 — аудит функций на 8054', '',
  'Основание: исходники Projector-Soft `ad258ff32a05754eed9b923b3e95bd0ce97f2369`, матрица 96 функций и исторический успешный exact-8054 CI run 43900081008. Лог этого run фиксирует checkout `ac71758d4dafe873f3193be020a623abd5e8b777`; сравнение до `ad258ff` показывает только два добавленных документа, без изменений исходников.', '',
  'Статус BUILT_UNTESTED означает: соответствующий Java-класс входил в компиляцию исторического CI и достижим по статическим ссылкам от smali/Manifest точек интеграции, либо это проверенный в том CI Intent alias. Он не доказывает запуск метода, запись в HAL или работу на проекторе. SOURCE_ONLY означает исходник без доказанной функциональной интеграции. STATIC_VERIFIED у SYS09 подтверждает лишь наличие Provision в инвентаре 8054. Новые правки после `ad258ff` пока не собраны.', '',
  `Итог матрицы: ${counts.map(([k,v])=>`${k} ${v}`).join('; ')}. HARDWARE_VERIFIED 0; STATIC_VERIFIED 0.`, '',
  '## 96 функций', '',
  '| ID | Функция | Текущий MOD (из handoff) | Класс в source | Статус |',
  '|---|---|---|---|---|',
  ...table.map(({row,found,status})=>`| ${row.id} | ${row.function.replace(/\|/g,'/')} | ${row.current_mod.replace(/\|/g,'/')} | ${found.join(', ') || '—'} | ${status} |`),
  '', '## 47 Java-классов v33', '',
  '| Класс | Статическая достижимость | JVM-тест |', '|---|---|---|',
  ...baseline.map(file=>{
    const n=file.slice(0,-5);
    return `| ${n} | ${reachable.has(n)?'Да, от smali/Manifest root':'Не подтверждена'} | ${tests.has(n+'Test.java')?'Есть':'Нет'} |`;
  }),
  '', 'Новый `EdidPortPolicy.java` добавлен после v33: SOURCE_ONLY до нового CI. Всего локально 48 Java-классов и 19 JVM-тестовых файлов.', '',
  '## DEX, Manifest, ресурсы и сборка', '',
  '- В v33 workflow компилируются все Java-классы через wildcard. Проверки `strings` подтверждают наличие маркеров DEX, но сами по себе не подтверждают вызов.',
  '- Точки smali: `MainFragment#onCreateView` → `OemRootMenuHook.install` и `OemInlinePreferencesHook.install`; `uicustom.MainActivity#onCreate` → `OemLaunchRouteHook.apply`; исходные OEM brightness, source и general контроллеры патчатся отдельными fail-closed скриптами. CI ожидает восемь изменённых smali файлов.',
  '- Manifest меняется скриптом совместимых activity-alias и регистрацией `KuSourceRecoveryBootReceiver`/`KuSourceRecoveryService`. Проверено в историческом CI, но конкретный AXML нового APK не исследован.',
  '- Ресурсы, калибровочные классы и 8054 аппаратная совместимость требуют отдельного diff/repack аудита. В репозитории нет исходного AndroidManifest.xml или ресурсов MOD; за основу берётся оригинальный APK.',
  '- Исторический CI 8054 прошёл компиляцию, smali patch, тесты, repack и platform test-key signing. Установка поверх stock, системные права/SELinux, реальные HDMI/звук/BT/оптика/термозащита и откат не проверены.', '',
  '## Главные блокеры', '',
  '1. `KuSourceRecoveryService` использует `ActivityManager.getRunningServices`; полнота видимости 8054 не доказана. Новая source-only защита запрещает автоматическую замену на 8054.',
  '2. `OemHdmiEdidHook` использует фиксированный HW6 и глобальный `tv_input_hdmi_edid_version`; смысл для HW7 не доказан. Новая source-only защита скрывает этот selector на 8054.',
  '3. Bluetooth-пульт: bonded HID и AOSP AddAccessory не заменяют OEM `CachedBluetoothDevice.connect(true)` и фильтры AutoPair.',
  '4. Picture/sound/EQ: запись Settings.Global не равна подтверждённому MediaTek DSP/PQ readback.',
  '5. Навигация: общие страницы всё ещё открывают отдельную OEM `uicustom.MainActivity`; единая двухпанельная UI не завершена.',
  '6. Exact 8054 APK invoke graph, explicit ComponentName и аппаратные cold boot/rollback испытания отсутствуют.', ''
].join('\n');
fs.mkdirSync(path.dirname(output),{recursive:true});
fs.writeFileSync(output,md);
console.log(`Audited ${baseline.length} baseline classes, ${matrix.length} features; ${counts.map(x=>x.join('='))}`);

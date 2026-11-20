"""Trace widget ownership, HOME, CEC and movement in exact STOCK."""
import re
import sys
from pathlib import Path

root = Path(sys.argv[1])
classes = {
    'AccessoriesFragment': r'.',
    'AccessoriesFragment$1': r'onReceive',
    'BluetoothAccessoryFragment': r'onCreatePreferences|onPreferenceTreeClick|prepare|prepareArgs',
    'PowerOnSourceSecondaryController': r'initData|initView|initListener|onAttach|lambda',
    'PowerOnAppSecondaryController': r'initData|onAttach|lambda',
    'SourceFragment': r'switchToHome|switchToHDMI|initView|lambda',
    'SourceSubMenuController': r'initView|lambda',
    'CompatibleApis': r'Cec|Standby',
    'EdidSourceSecondaryController': r'initData|onAttach|lambda',
    'KeystoneSettingsSecondaryController': r'setMoveAutoKeystone|initListener',
}
p = re.compile(r'(?ms)^\.method .*?^\.end method')
for path in sorted(root.glob('smali*/**/*.smali')):
    content = None
    if path.stem in classes:
        content = path.read_text(encoding='utf-8')
        print('CLASS', path.relative_to(root), content.splitlines()[0])
        if path.stem.startswith('AccessoriesFragment'):
            for line in content.splitlines():
                if line.startswith('.field'):
                    print(line)
    elif path.stem.startswith('AccessoriesFragment$'):
        content = path.read_text(encoding='utf-8')
    if content is None:
        continue
    for m in p.finditer(content):
        decl = m[0].splitlines()[0]
        if re.search(classes.get(path.stem, r'onReceive|onBluetooth|isConnected'), decl):
            print('METHOD', path.relative_to(root), content.count('\n', 0, m.start()) + 1, decl)
            print('\n'.join(line for line in m[0].splitlines()[1:-1] if line.strip()
                            and not line.strip().startswith('.line')))

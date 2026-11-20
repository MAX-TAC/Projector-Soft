"""Integrate the 8054 menu into the verified stock root without changing its host."""
import argparse
import difflib
import hashlib
import json
import re
from pathlib import Path

BASE = 'smali_classes13/com/android/tv/settings/'
ROOT = BASE + 'MainFragment.smali'
AUX = BASE + 'SuggestionQuickSettingPrefsContainer.smali'
ACCESSORIES = BASE + 'accessories/AccessoriesFragment.smali'
BLUETOOTH = 'Ldev/projectorsoft/tvsettings/NativeBluetoothPage;'
BRIDGE = 'Ldev/projectorsoft/tvsettings/StockMainSettingsMenu;'
FRAGMENT = 'Landroidx/preference/PreferenceFragmentCompat;'
PREF = 'Landroidx/preference/Preference;'
GROUP = 'Landroidx/preference/PreferenceGroup;'
VIEW = 'onCreateView(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;Landroid/os/Bundle;)Landroid/view/View;'
SUPER_VIEW = ('    invoke-super {p0, p1, p2, p3}, '
              'Landroidx/leanback/preference/LeanbackPreferenceFragmentCompat;->' + VIEW)
ADD = f'    invoke-virtual {{v0, v1}}, {GROUP}->addPreference({PREF})Z'


def edit(text, signature, patch):
    pattern = re.compile(r'(?ms)^\.method [^\n]*\b' + re.escape(signature) + r'\n.*?^\.end method$')
    found = list(pattern.finditer(text))
    if len(found) != 1:
        raise ValueError('Expected one exact method: ' + signature)
    match = found[0]
    return text[:match.start()] + patch(match[0]) + text[match.end():]


def assert_once(code, token):
    if code.count(token) != 1:
        raise ValueError('Changed stock anchor: ' + token)


def root_view(code):
    assert_once(code, '    .locals 1')
    assert_once(code, '->updateChannelsAndInputs()V')
    assert_once(code, SUPER_VIEW)
    assert_once(code, '    return-object p0')
    assert_once(code, '    move-result-object p0')
    if '.catch' in code or not re.search(
            re.escape(SUPER_VIEW) + r'\n\s*\n    move-result-object p0\s+return-object p0', code):
        raise ValueError('MainFragment View return no longer matches stock register/lifecycle contract')
    before = code[:code.index(SUPER_VIEW)]
    if before.count('invoke-super') or re.search(r'(?m)^\s*(?:move[-/\w]*|iget[-/\w]*|const[-/\w]*|new-instance)\s+p0\b', before):
        raise ValueError('MainFragment receiver already modified before stock View invocation')
    return code.replace(SUPER_VIEW, f'    invoke-static {{p0}}, {BRIDGE}->install({FRAGMENT})V\n\n' + SUPER_VIEW)


def auxiliary(code, key, additions):
    assert_once(code, f'const-string v1, "{key}"')
    if code.count(f'{GROUP}->addPreference({PREF})Z') != additions or code.count(ADD) < 1:
        raise ValueError('Changed original add sites for ' + key)
    prefix = code[:code.index(ADD)]
    assert_once(prefix, f'const-string v1, "{key}"')
    assert_once(prefix, f'{FRAGMENT}->getPreferenceScreen()Landroidx/preference/PreferenceScreen;')
    return code.replace(ADD, f'    invoke-static {{v0, v1}}, {BRIDGE}->addAuxiliary({GROUP}{PREF})Z', 1)


def bluetooth_rows(code):
    get_screen = f'    invoke-virtual {{p0}}, {FRAGMENT}->getPreferenceScreen()Landroidx/preference/PreferenceScreen;'
    assert_once(code, '    .locals 11')
    assert_once(code, get_screen)
    assert_once(code, '    move-result-object v0')
    assert_once(code, f'{GROUP}->findPreference(')
    if code.count(f'{GROUP}->removeAll()V') != 2 or '.catch' in code:
        raise ValueError('Unexpected exact AccessoriesFragment.updateAccessories layout')
    return code.replace(get_screen, f'    invoke-static {{p0}}, {BLUETOOTH}->deviceRows({FRAGMENT}){GROUP}')


def modified_files(decoded):
    root = (decoded / ROOT).read_text(encoding='utf-8')
    aux = (decoded / AUX).read_text(encoding='utf-8')
    bt = (decoded / ACCESSORIES).read_text(encoding='utf-8')
    if BRIDGE in root or BRIDGE in aux or BLUETOOTH in bt:
        raise ValueError('Original stock root already patched')
    return {
        ROOT: edit(root, VIEW, root_view),
        ACCESSORIES: edit(bt, 'updateAccessories()V', bluetooth_rows),
        AUX: edit(edit(aux, 'showQuickSettings()V', lambda code: auxiliary(code, 'quick_settings', 3)),
                  'onSuggestionReady(Ljava/util/List;)V', lambda code: auxiliary(code, 'suggestions', 1)),
    }


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('decoded', type=Path)
    parser.add_argument('report', type=Path)
    parser.add_argument('diff', type=Path)
    args = parser.parse_args()
    changes = modified_files(args.decoded)
    evidence = {}
    diff = []
    for name, after in changes.items():
        path = args.decoded / name
        before = path.read_text(encoding='utf-8')
        evidence[name] = {'before_sha256': hashlib.sha256(before.encode()).hexdigest(),
                          'after_sha256': hashlib.sha256(after.encode()).hexdigest()}
        diff.extend(difflib.unified_diff(before.splitlines(True), after.splitlines(True),
                                        fromfile='stock/' + name, tofile='candidate/' + name))
        path.write_text(after, encoding='utf-8')
    args.diff.write_text(''.join(diff), encoding='utf-8')
    args.report.write_text(json.dumps({'edited_smali': list(changes), 'edited_dex': ['classes13.dex'],
                                      'main_settings': 'STOCK_ACTIVITY_WITH_UPDATED_MAIN_FRAGMENT',
                                      'evidence': evidence}, indent=2) + '\n', encoding='utf-8')


if __name__ == '__main__':
    main()

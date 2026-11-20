"""Verify that exact APK packaging leaves every stock component and payload intact."""
import re
import sys
import zipfile
from pathlib import Path

from patch_8054_unified_manifest import ACTIVITIES, DISABLED, PREFIX


def without_version(lines):
    return [line for line in lines if 'A: android:versionCode(' not in line]


def verify_manifest(stock, module):
    lines = module.splitlines()
    stock_lines = stock.splitlines()
    additions = {}
    for name in [name for name, _ in ACTIVITIES] + [name for _, name in DISABLED]:
        indexes = [i for i, line in enumerate(lines)
                   if f'A: android:name(0x01010003)="{PREFIX}{name}"' in line]
        if len(indexes) != 1:
            raise ValueError('Missing or duplicated MOD component ' + name)
        begin = indexes[0] - 1
        while begin>=0 and lines[begin].startswith('        '):
            begin-=1
        if not re.match(r'^      E: (activity|receiver|service) ', lines[begin]):
            raise ValueError('Wrong manifest component node for ' + name)
        end = begin + 1
        while end < len(lines) and lines[end].startswith('        '):
            end += 1
        additions[name] = '\n'.join(lines[begin:end])
        del lines[begin:end]
    if without_version(lines) != without_version(stock_lines):
        raise ValueError('Original manifest entries or filters changed')
    main = next(i for i, line in enumerate(stock_lines)
                if 'android:name(0x01010003)="com.android.tv.settings.MainSettings"' in line)
    theme = next((line.strip() for line in stock_lines[max(0,main-5):main+5]
                  if 'android:theme(' in line),None)
    if theme is None:
        raise ValueError('Missing stock MainSettings theme')
    for name, _ in ACTIVITIES:
        node = additions[name]
        if 'E: activity ' not in node or not re.search(r'android:exported.*0xffffffff',node):
            raise ValueError('Menu Activity not directly launchable: ' + name)
        if theme not in node:
            raise ValueError('Menu Activity does not share the stock theme: ' + name)
        if name == ACTIVITIES[0][0]:
            for value in ('android.intent.action.MAIN','android.intent.category.LAUNCHER',
                          'android.intent.category.LEANBACK_LAUNCHER'):
                if value not in node:
                    raise ValueError('Missing launcher contract: ' + value)
        elif 'E: intent-filter' in node:
            raise ValueError('Unexpected implicit route for ' + name)
    for kind, name in DISABLED:
        node = additions[name]
        for entry in ('enabled','exported'):
            if not re.search(r'android:' + entry + r'[^\n]*0x0$',node,re.M):
                raise ValueError('Recovery must remain dormant: ' + name)
        if ('E: ' + kind + ' ') not in node or 'E: intent-filter' in node:
            raise ValueError('Recovery must remain unregistered for implicit activation')
    print('PASS original MainSettings and all stock Manifest nodes; 9 new Activities; recovery dormant')


def verify_payload(stock, module):
    with zipfile.ZipFile(stock) as original, zipfile.ZipFile(module) as candidate:
        unchanged = {entry for entry in original.namelist()
                     if entry != 'AndroidManifest.xml' and not entry.startswith('META-INF/')}
        dex = {name for name in unchanged if re.fullmatch(r'classes\d*\.dex', name)}
        if len(dex) != 16 or any(original.read(entry) != candidate.read(entry) for entry in unchanged):
            raise ValueError('Unexpected delta in original 8054 DEX/resources')
        extra = {name for name in candidate.namelist() if name not in original.namelist()}
        if extra != {'classes17.dex'} or candidate.testzip():
            raise ValueError('Unexpected module archive entry or invalid ZIP')
        bytecode = candidate.read('classes17.dex')
        for name, _ in ACTIVITIES:
            descriptor = 'L' + (PREFIX + name).replace('.', '/') + ';'
            if descriptor.encode() not in bytecode:
                raise ValueError('DESCRIPTOR missing for ' + name)
    print('PASS all 16 original DEX and stock resources byte-identical')


if __name__ == '__main__':
    _, stock_apk, mod_apk, stock_tree, mod_tree = sys.argv
    verify_manifest(Path(stock_tree).read_text(encoding='utf-8'),
                    Path(mod_tree).read_text(encoding='utf-8'))
    verify_payload(stock_apk, mod_apk)

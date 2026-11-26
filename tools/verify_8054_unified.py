"""Verify that exact APK packaging leaves every stock component and payload intact."""
import re
import struct
import sys
import zipfile
from pathlib import Path

from patch_8054_unified_manifest import ACTIVITIES, DISABLED, PREFIX
from strip_8054_junk_activities import JUNK


def dex_classes(dex):
    def word(offset):
        return struct.unpack_from('<I',dex,offset)[0]

    strings, types = word(0x3c), word(0x44)
    count, definitions = word(0x60), word(0x64)
    result = set()
    for index in range(count):
        cls = word(definitions + 32*index)
        symbol = word(types + 4*cls)
        position = word(strings + 4*symbol)
        while dex[position] & 0x80:
            position += 1
        position += 1
        end = dex.index(0,position)
        result.add(dex[position:end].decode('ascii'))
    return result


def without_version(lines):
    return [line for line in lines if 'A: android:versionCode(' not in line]


def drop_junk_nodes(lines):
    """Return the tree without the deliberately removed junk activities."""
    drop = set()
    for index, line in enumerate(lines):
        match = re.search(r'android:name\(0x01010003\)="(com\.android\.tv\.settings\.[^"]+)"', line)
        alias = re.search(r'android:targetActivity\(0x01010202\)="(com\.android\.tv\.settings\.[^"]+)"', line)
        hit = (match and match.group(1) in JUNK) or (alias and alias.group(1) in JUNK)
        if not hit:
            continue
        indent = len(line) - len(line.lstrip())
        begin = index
        # Walk up to the element that owns this attribute: an "E:" line with a
        # smaller indent than the attribute itself.
        while begin > 0:
            previous = lines[begin - 1]
            previous_indent = len(previous) - len(previous.lstrip())
            if previous.lstrip().startswith('E:') and previous_indent < indent:
                break
            begin -= 1
        end = index + 1
        while end < len(lines):
            current = lines[end]
            current_indent = len(current) - len(current.lstrip())
            if current.lstrip().startswith('E:') and current_indent <= indent - 2:
                break
            end += 1
        drop.update(range(begin, end))
    return [line for index, line in enumerate(lines) if index not in drop]


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
    # New alias is a shortcut to the already-modified MainSettings. It must
    # not replace/disable original OEM activities or take the Chinese UI away.
    alias = 'com.android.tv.settings.mod.SettingsLauncher'
    matches = [i for i, line in enumerate(lines)
               if 'android:name(0x01010003)="' + alias + '"' in line]
    if len(matches) != 1:
        raise ValueError('Missing or duplicated TvSettingsPlus MOD launcher alias')
    alias_start = matches[0] - 1
    while alias_start >= 0 and lines[alias_start].startswith('        '):
        alias_start -= 1
    if not re.match(r'^      E: activity-alias ', lines[alias_start]):
        raise ValueError('MOD launcher is not an activity-alias')
    alias_end = alias_start + 1
    while alias_end < len(lines) and lines[alias_end].startswith('        '):
        alias_end += 1
    alias_node = '\n'.join(lines[alias_start:alias_end])
    for item in ('com.android.tv.settings.MainSettings',
                 'android.intent.action.MAIN',
                 'android.intent.category.LAUNCHER',
                 'android.intent.category.LEANBACK_LAUNCHER',
                 'TvSettingsPlus MOD', 'android:targetActivity',
                 'android:icon', 'android:banner',
                 '0x7f08022e', '0x7f080142'):
        if item not in alias_node:
            raise ValueError('MOD launcher alias missing ' + item)
    del lines[alias_start:alias_end]
    if without_version(lines) != without_version(drop_junk_nodes(stock_lines)):
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
        if 'E: intent-filter' in node:
            raise ValueError('Unexpected implicit route for ' + name)
    for kind, name in DISABLED:
        node = additions[name]
        for entry in ('enabled','exported'):
            if not re.search(r'android:' + entry + r'[^\n]*0x0$',node,re.M):
                raise ValueError('Recovery must remain dormant: ' + name)
        if ('E: ' + kind + ' ') not in node or 'E: intent-filter' in node:
            raise ValueError('Recovery must remain unregistered for implicit activation')
    print('PASS original MainSettings, stock components, exact MOD TV launcher alias, recovery dormant')


def verify_payload(stock, module):
    with zipfile.ZipFile(stock) as original, zipfile.ZipFile(module) as candidate:
        unchanged = {entry for entry in original.namelist()
                     if entry != 'AndroidManifest.xml' and not entry.startswith('META-INF/')}
        dex = {name for name in unchanged if re.fullmatch(r'classes\d*\.dex', name)}
        edited = 'classes13.dex'
        if len(dex) != 16 or any(original.read(entry) != candidate.read(entry)
                                 for entry in unchanged - {edited}):
            raise ValueError('Unexpected delta in original 8054 DEX/resources')
        before, after = original.read(edited), candidate.read(edited)
        if before == after or dex_classes(before) != dex_classes(after):
            raise ValueError('Invalid original MainFragment DEX delta')
        extra = {name for name in candidate.namelist() if name not in original.namelist()}
        if extra != {'classes17.dex'} or candidate.testzip():
            raise ValueError('Unexpected module archive entry or invalid ZIP')
        classes = dex_classes(candidate.read('classes17.dex'))
        if not classes or any(not name.startswith('Ldev/projectorsoft/tvsettings/') for name in classes):
            raise ValueError('Non-MOD class in module DEX; stock fixtures or libraries must not ship')
        for name, _ in ACTIVITIES:
            descriptor = 'L' + (PREFIX + name).replace('.', '/') + ';'
            if descriptor not in classes:
                raise ValueError('DESCRIPTOR missing for ' + name)
    print('PASS 15 original DEX and all stock resources byte-identical, classes13 DEX class set intact')


if __name__ == '__main__':
    _, stock_apk, mod_apk, stock_tree, mod_tree = sys.argv
    verify_manifest(Path(stock_tree).read_text(encoding='utf-8'),
                    Path(mod_tree).read_text(encoding='utf-8'))
    verify_payload(stock_apk, mod_apk)

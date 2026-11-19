"""Prove that the Settings-owned panel singleton can be inspected without constructing it."""
import re
import sys
from pathlib import Path

MANAGER = 'com.android.tv.settings.uicustom.util.ProjectorManager'
DISPLAY = 'Lcom/mediatek/twoworlds/factory/MtkTvFApiDisplay;'
BASE = 'Lcom/mediatek/twoworlds/factory/MtkTvFApiDisplayBase;'
INFO = 'Lcom/mediatek/twoworlds/factory/common/MtkTvFApiDisplayTypes$PanelInfo;'


def instructions(source, signature):
    hit = re.search(r'(?ms)^\.method [^\n]*' + re.escape(' ' + signature)
                    + r'\n(.*?)^\.end method$', source)
    if not hit:
        return None
    return [line.strip() for line in hit[1].splitlines()
            if line.strip() and not line.lstrip().startswith(('.', '#'))]


def discover(manager, display, base, ini):
    path = instructions(ini, 'updataPath()V') or []
    if (sum('->E_MTK_FAPI_INI_TYPE_PANEL:' in line for line in path) != 2
            or not any('->getCusIniPath(I)Ljava/lang/String;' in line for line in path)
            or not any('->getData(II)Ljava/lang/String;' in line for line in path)
            or '->updataPath()V' not in ini):
        return None
    descriptor = 'L' + MANAGER.replace('.', '/') + ';'
    if '.super Ljava/lang/Object;' not in manager or f'.super {BASE}' not in display:
        return None
    initial = instructions(manager, '<clinit>()V')
    if initial is not None and initial != ['return-void']:
        if len(initial) != 3 or not re.fullmatch(r'const/4 (v\d+), 0x0', initial[0]):
            return None
    fields = re.findall(r'(?m)^\.field (?:\w+ )*static (?:\w+ )*(\w+):'
                        + re.escape(descriptor) + r'(?: = null)?$', manager)
    if len(fields) != 1:
        return None
    singleton = fields[0]
    if initial not in (None, ['return-void']):
        reg = initial[0].split()[1].rstrip(',')
        if initial[1:] != [f'sput-object {reg}, {descriptor}->{singleton}:{descriptor}', 'return-void']:
            return None
    members = re.findall(r'(?m)^\.field (?:\w+ )*(\w+):' + re.escape(DISPLAY) + r'$', manager)
    if len(members) != 1:
        return None
    member = members[0]
    ctor = instructions(manager, '<init>()V') or []
    receiver = f'{descriptor}->{member}:{DISPLAY}'
    if not any(re.fullmatch(r'iput-object v\d+, p0, ' + re.escape(receiver), line) for line in ctor):
        return None
    if not any(DISPLAY + '->getInstance()' + DISPLAY in line for line in ctor):
        return None
    cached = instructions(base, 'getPanelInfo()' + INFO)
    if cached != [f'sget-object p0, {BASE}->panelInfo:{INFO}', 'return-object p0']:
        return None
    for prop, typ in [('Type', 'Ljava/lang/String;'), ('Version', 'Ljava/lang/String;'),
                      ('Width', 'I'), ('Height', 'I')]:
        signature = 'getPanel' + prop + '()Ljava/lang/String;'
        if instructions(display, signature) is not None:
            return None
        body = instructions(base, signature)
        expected = [f'invoke-direct {{p0}}, {BASE}->getPanelInfo(){INFO}', 'move-result-object p0']
        field = f'{INFO}->{prop.lower()}:{typ}'
        if typ == 'I':
            expected.extend([f'iget p0, p0, {field}',
                             'invoke-static {p0}, Ljava/lang/Integer;->toString(I)Ljava/lang/String;',
                             'move-result-object p0'])
        else:
            expected.append(f'iget-object p0, p0, {field}')
        expected.append('return-object p0')
        if body != expected:
            return None
    return singleton, member


def exact_file(decoded, name):
    paths = list(decoded.glob('smali*/**/' + name + '.smali'))
    return paths[0].read_text(encoding='utf-8') if len(paths) == 1 else ''


if __name__ == '__main__':
    decoded, output = map(Path, sys.argv[1:])
    route = discover(*(exact_file(decoded, name) for name in
                       ('ProjectorManager', 'MtkTvFApiDisplay', 'MtkTvFApiDisplayBase', 'MtkTvFApiDisplayIniManager')))
    if route is None:
        raise SystemExit('PIC18 stock passive cache contract not verified')
    singleton, member = route
    print(f'PIC18 verified passive cache and stock PANEL path resolver: {MANAGER}.{singleton}.{member}')
    output.write_text('package dev.projectorsoft.tvsettings;\n'
                      'public final class Exact8054PanelCache {\n'
                      f'    public static final String MANAGER = "{MANAGER}";\n'
                      f'    public static final String SINGLETON = "{singleton}";\n'
                      f'    public static final String DISPLAY = "{member}";\n'
                      '}\n', encoding='utf-8')

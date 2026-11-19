"""Print bounded exact-stock call sites for panel INI, panel cache and chip info."""
import re
import sys
from pathlib import Path

METHOD = re.compile(r'(?ms)^\.method [^\n]+\n.*?^\.end method$')
OWNER = ('ProjectorManager', 'MtkTvFApiDisplay', 'MtkTvFApiDisplayBase', 'MtkTvFApiDisplayTypes$PanelInfo',
         'MtkTvFApiCusdataIniManager', 'MtkTvFApiDisplayIniManager', 'MtkTvFApiIniManager',
         'MtkTvFApiDisplayWrapper', 'MtkTvFApiInformationWrapper', 'MtkTvFApiSystemWrapper',
         'MtkTvFApiManagerProxy', 'MtkTvFApiSystemBase', 'PanelInfoViewHolder', 'InfoViewHolder')
REF = re.compile(r'(?:ProjectorManager|MtkTvFApiDisplay);->getInstance\(|'
                 r'->(?:loadPanelInfo|getPanelIni|getPanelInfoByKey|getPanel(?:Type|Version|Width|Height)|'
                 r'getChip(?:ID|Id|Name|Type|Info)|init(?:TvFApi|Factory|DisplayIni)\w*)\(|'
                 r'->iniPaths:\[Ljava/lang/String;|->E_MTK_FAPI_INI_TYPE_PANEL:|'
                 r'->(?:panelInfo|E_MTK_FAPI_KEY_TYPE_PANEL_(?:TYPE|VERSION|WIDTH|HEIGHT)):|'
                 r'"[^"\n]*(?:[Pp]anel[^"\n]*\.ini|ro\.soc\.(?:model|manufacturer))[^"\n]*"')
OP = re.compile(r'^\s*(?:invoke-|const|move-result|if-|goto|[ais]get|[ais]put|new-instance|[a-z]+-switch|return|throw|:[a-z])')
SELECTED = re.compile(r'^(?:<init>|<clinit>|getInstance|init\w*|loadPanelInfo|getPanel\w+|getIniParameter|'
                      r'getCusIniPath|getPath|getData|buildPaths|buildKeys|read\w+|onCreate)$')
INIT = re.compile(r'->(?:init\w*|getInstance)\(')

def trace(root):
    count = 0
    for path in sorted(root.glob('smali*/**/*.smali')):
        count += 1
        owner = path.stem
        code = path.read_text(encoding='utf-8')
        relative = path.relative_to(root).as_posix()
        keyed = owner in OWNER
        if owner == 'MtkTvFApiDisplayIniManager':
            for field in code.splitlines():
                if field.startswith('.field ') and 'iniPaths:' in field:
                    print(f'PIC18_PATH_FIELD {root.name}/{relative}: {field}')
        lifecycle = bool(re.search(r'/(?:Factory.*Activity|DesignMenuActivity|[^/]*Application|TvSettingsApplication|MainSettings)\.smali$', relative))
        if not (keyed or lifecycle or REF.search(code)):
            continue
        for match in METHOD.finditer(code):
            signature, *body = match[0].splitlines()
            name = signature.split('(')[0].split()[-1]
            refs = any(REF.search(line) for line in body)
            around_init = lifecycle and name in ('onCreate', 'attachBaseContext', 'onStart')
            if not (refs or keyed and SELECTED.match(name) or around_init):
                continue
            if keyed and owner.endswith('IniManager') and name == 'buildKeys' and not refs:
                continue
            offset = code.count('\n', 0, match.start()) + 1
            print(f'PIC18_METHOD {root.name}/{relative}:{offset} {signature}')
            for i, line in enumerate(body, 1):
                if not OP.search(line):
                    continue
                if len(body) > 600 and (name in ('buildKeys', '<clinit>') or around_init):
                    if not (REF.search(line) or INIT.search(line) or name == 'buildKeys' and 'const-string' in line):
                        continue
                print(f'PIC18_LINE {offset+i} {line.strip()}')
    if not count:
        raise SystemExit(f'No decoded DEX in {root}')
    print(f'PIC18_SCANNED {root.name} {count}')

if __name__ == '__main__':
    for directory in sys.argv[1:]:
        trace(Path(directory))

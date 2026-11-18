"""Print only stock methods and real calls relevant to EDID and CEC on 8054."""
import re
import sys
from pathlib import Path


METHOD = re.compile(r"(?ms)^\.method [^\n]*?([^\s(]+)\([^\n]*\n.*?^\.end method$")
OWNERS = {
    'EdidSourceSecondaryController': r'getEdidVersion|init.*|set.*|.*lambda.*',
    'EdidSourceFragment': r'getEdidVersion|init.*|on.*|set.*|.*lambda.*',
    'SourceSubMenuController': r'.*(?:[Cc]ec|[Ee]did|[Aa]utoDeviceOff|[Gg]ame|[Cc]onfirm).*',
    'SourceFragment': r'.*(?:[Cc]ec|[Ee]did|[Aa]utoDeviceOff|[Gg]ame|[Cc]onfirm).*',
    'PreferenceConfigUtils': r'isHDMISource|isSupportEARC',
    'SaveValue': r'readWorldInputType',
    'MtkTvAVModeBase': r'getVideoInfoData',
    'MtkTvVideoInfoBase': r'getU8EdidIndex|setU8EdidIndex',
    'CompatibleApis': r'.*(?:CecControl|CecAutoDeviceOff).*',
    'MtkTvCecAnBase': r'.*[Ee]arc.*',
    'TVNativeWrapper': r'.*(?:VideoInfoData|[Ee]arc).*',
    'TVNative': r'.*(?:VideoInfoData|[Ee]arc).*',
    'DataSeparaterUtil': r'isSupportPowerConfirmed',
}
CALL = re.compile(
    r'invoke-.*->(?:getVideoInfoData|getU8EdidIndex|setU8EdidIndex|'
    r'isHdmiCecControlEnabled|setHdmiCecControlEnabled|isHdmiCecAutoDeviceOffEnabled|'
    r'setHdmiCecAutoDeviceOffEnabled|setEarcEnable|resetTrapezoid|isHDMISource)\('
)
EDID_KEY = 'tv_input_hdmi_edid_version'


def trace(root):
    for path in sorted(root.glob('smali*/**/*.smali')):
        relative = path.relative_to(root).as_posix()
        owner = path.stem.split('$')[0]
        mask = OWNERS.get(owner)
        if mask is None and not any(word in path.stem.lower()
                                    for word in ('cec', 'edid', 'game', 'earc')):
            # Restrict broad scanning to actual query callers and config consumers.
            raw = path.read_text(encoding='utf-8')
            if '->getU8EdidIndex(' not in raw and 'g_fusion_picture__hdmi_edid_version' not in raw:
                continue
        else:
            raw = path.read_text(encoding='utf-8')
        for match in METHOD.finditer(raw):
            name = match[1]
            body = match[0]
            relevant = mask is not None and re.fullmatch(mask, name)
            callsites = CALL.search(body)
            key_write = EDID_KEY in body and ('->putGlobalValueInt(' in body or '->putInt(' in body)
            config = 'g_fusion_picture__hdmi_edid_version' in body
            if not (relevant or key_write or callsites or config):
                continue
            if not (relevant or key_write or owner in OWNERS or
                    '->getU8EdidIndex(' in body or config):
                continue
            start = raw.count('\n', 0, match.start()) + 1
            lines = body.splitlines()
            print(f'HDMI_CONTROL {root.name}/{relative}:{start} {lines[0]}')
            focused = len(lines) > 260
            for i, line in enumerate(lines[1:], 1):
                line = line.strip()
                if not line or line.startswith(('.line ', '.local ', '.end local ')):
                    continue
                if focused and not (CALL.search(line) or EDID_KEY in line
                                    or 'g_fusion_picture__hdmi_edid_version' in line
                                    or re.search(r'->(?:setChecked|setVisibility|getString|getInt|putInt|setOnClickListener)\(', line)):
                    continue
                print(f'HDMI_CONTROL_LINE {start + i} {line}')


if __name__ == '__main__':
    for directory in sys.argv[1:]:
        trace(Path(directory))

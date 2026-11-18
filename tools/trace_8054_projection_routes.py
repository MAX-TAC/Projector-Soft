"""Trace original projection launchers and editor lifecycle on exact stock 8054."""
import re
import sys
from pathlib import Path

METHOD = re.compile(r"(?ms)^\.method [^\n]*?([^\s(]+)\([^\n]*\n.*?^\.end method$")
ROUTE = re.compile(r"FourCornerActivity|DigitalZoomActivity|HVKeystoneActivity|KeystoneCalibrationActivity|"
                   r"four_corner|digital_zoom|digitalZoom|FOUR_CORNER|DIGITAL_ZOOM|"
                   r"getDigitalZoom|setDigitalZoom|rjpj\.action\.keystone\.HV", re.I)
DETAIL = {
    'FourCornerActivity': r'on.*|init.*|.*(?:[Kk]eystone|[Pp]oint|[Rr]eset|[Kk]ey).*',
    'DigitalZoomActivity': r'on.*|init.*|.*(?:[Zz]oom|[Ss]cale|[Kk]ey).*',
    'HVKeystoneActivity': r'onCreate|onResume|onBackPressed|init.*',
    'TCorrectionBroadcastReceiver': r'onReceive',
    'KeystoneCalibrationActivity': r'on.*|init.*',
    'KeystoneSettingsSecondaryController': r'.*(?:[Cc]orrection|[Ff]our|[Zz]oom|[Oo]bstacle|[Ss]creen).*',
    'FloatMenuManager': r'jumpByAction|jumpToExternalActivity',
    'ProjectorFragment': r'access\$400|jumpByAction|initView|initData',
    'ProjectorSubMenuController': r'initView|initData',
    'IntentUtils': r'intentAction|fourPointIntent|imageDigitalIntent',
    'KeystoneUtil': r'(?:set.*Offset|setDigitalZoom|.*[Zz]oom.*)',
    'SoundFragment': r'createPreferences',
    'AdvancedFragment': r'onCreatePreferences',
    'DTSFragment': r'onCreatePreferences',
    'DTSStudioSoundFragment': r'onCreatePreferences',
    'ProjectionActivity': r'onCreate|init.*|onClick|.*lambda.*',
}
KEY = re.compile(r"invoke-|const-string|const-class|putExtra|get.*Extra|[is][gp]et|[a-z]-switch|return|if-|:cond|0x")


def trace(root):
    for path in sorted(root.glob("smali*/**/*.smali")):
        owner = path.stem.split('$')[0]
        mask = DETAIL.get(owner)
        raw = path.read_text(encoding="utf-8")
        if not mask and not ROUTE.search(raw):
            continue
        for m in METHOD.finditer(raw):
            name, body = m[1], m[0]
            selected = bool(mask and re.fullmatch(mask, name))
            if not selected and not (ROUTE.search(body) and
                    re.search(r"invoke-|const-class|const-string", body)):
                continue
            if not selected and owner in DETAIL and any(x in name for x in ('access$', '<init>')):
                continue
            start = raw.count("\n", 0, m.start())+1
            lines = body.splitlines()
            print(f"PRJ8054_METHOD {root.name}/{path.relative_to(root)}:{start} {lines[0]}")
            for i, line in enumerate(lines[1:], 1):
                clean=line.strip()
                if clean and not clean.startswith((".line ", ".local ", ".end local ")):
                    if len(lines)<300 or KEY.search(clean):
                        print(f"PRJ8054_LINE {start+i} {clean}")


if __name__ == "__main__":
    for folder in sys.argv[1:]:
        trace(Path(folder))

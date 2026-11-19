"""List exact 8054 picture-format and panel call sites from decoded OEM APKs."""
import re
import sys
from pathlib import Path

METHOD = re.compile(r"(?ms)^\.method [^\n]+\n.*?^\.end method$")
ASPECT = r"aspect[_. -]*ratio|overscan|just[_. -]*scan|screen[_. -]*fit|wide[_. -]*mode|screen[_. -]*mode|picture[_. -]*size|display[_. -]*format"
MATCH = re.compile(rf"{ASPECT}|FAPI_INI_TYPE_OVERSCAN|getPanel(?:Info|Resolution|Timing|Width|Height)|\b(?:get|set)(?:PEQ|Peq)\w*", re.I)
WIRING = re.compile(rf'^(?:invoke-\S+ .*|const-string(?:/jumbo)? .*)({ASPECT}|getPanel(?:Info|Resolution|Timing|Width|Height)|(?:get|set)(?:PEQ|Peq)\w*)', re.I)
OP = re.compile(r"^\s*(?:invoke-|const|move-result|if-|goto|[is]get|[is]put|[a-z]+-switch|return|:[a-z])")
FACTORY = {"OverScanAdjustViewHolder", "PanelInfoViewHolder"}
FRAMEWORK = ("android/", "androidx/", "com/google/", "kotlin/", "kotlinx/", "org/")

def trace(root):
    files = sorted(root.glob("smali*/**/*.smali"))
    if not files:
        raise ValueError(f"No decoded DEX below {root}")
    for source in files:
        path = source.relative_to(root)
        owner = source.stem.split("$")[0]
        if str(Path(*path.parts[1:])).replace("\\", "/").startswith(FRAMEWORK):
            continue
        factory_view = owner in FACTORY
        focus = MATCH.search(owner) is not None
        body = source.read_text(encoding="utf-8")
        if not (factory_view or focus or MATCH.search(body)):
            continue
        for match in METHOD.finditer(body):
            signature, *steps = match[0].splitlines()
            hits = [line for line in steps if WIRING.search(line.strip())]
            if not (hits or MATCH.search(signature) or factory_view or focus):
                continue
            number = body.count("\n", 0, match.start()) + 1
            print(f"PIC14_METHOD {root.name}/{path}:{number} {signature}")
            for i, line in enumerate(steps, 1):
                if OP.search(line):
                    if len(steps) <= 230 or line in hits or factory_view:
                        print(f"PIC14_LINE {number + i} {line.strip()}")
    print(f"PIC14_SCANNED {root.name} {len(files)}")

if __name__ == "__main__":
    for directory in sys.argv[1:]:
        trace(Path(directory))

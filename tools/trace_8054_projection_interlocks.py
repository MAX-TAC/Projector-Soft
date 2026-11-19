"""Select actual 8054 sensor/keystone call sites without running OEM software."""
import re
import sys
from pathlib import Path

METHOD = re.compile(r"(?ms)^\.method [^\n]*?([^\s(]+)\([^\n]*\n.*?^\.end method$")
REF = re.compile(r"persist\.rjpj\.(obstacle\.avoidance|screen\.alignment|autokeystone\.move)|"
                 r"->(setAutoObstacleAvoidance|setAutoScreen|setMoveAutoKeystone|setPEQ|getPEQ)\(|"
                 r"\b(?:peq13|PEQ13|PEQ_13|peq_13)\b")
CONTROLLER = re.compile(r"(^|\$)(?:init.*|on.*|setAuto.*|setMove.*|update.*|lambda.*)$")
BODY = re.compile(r"invoke-|const[-/ ]|if-|:[\w_]+|[ip]get|[is]put|[a-z]-switch|return|move-result")


def trace(root):
    for file in sorted(root.glob("smali*/**/*.smali")):
        owner = file.stem.split("$")[0]
        content = file.read_text(encoding="utf-8")
        editor = owner in ("KeystoneSettingsSecondaryController", "KeystoneSettingsFragment")
        if not editor and not REF.search(content):
            continue
        for m in METHOD.finditer(content):
            name, body = m[1], m[0]
            if not REF.search(body) and not (editor and CONTROLLER.search(name)):
                continue
            lines = body.splitlines()
            start = content.count("\n", 0, m.start()) + 1
            print(f"PRJ89_METHOD {root.name}/{file.relative_to(root)}:{start} {lines[0]}")
            for index, line in enumerate(lines[1:], 1):
                line = line.strip()
                if line and not line.startswith((".line ", ".local ", ".end local ")) and (
                        len(lines) < 260 or BODY.search(line)):
                    print(f"PRJ89_LINE {start+index} {line}")


if __name__ == "__main__":
    for directory in sys.argv[1:]:
        trace(Path(directory))

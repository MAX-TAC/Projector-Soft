"""Print executable stock source-selection call sites from exact 8054 smali."""
import re
import sys
from pathlib import Path


METHOD = re.compile(r'^\.method .*?([^\s(]+)\(')
SOURCE_CALL = re.compile(
    r'invoke-.*->(?:startActivity(?:ForResult)?|sendBroadcast(?:AsUser)?|'
    r'(?:[a-zA-Z0-9_]*(?:witch|elect|etCurrent|etMain)[a-zA-Z0-9_]*(?:Source|Input)[a-zA-Z0-9_]*)|'
    r'tune|setSource|setInput|setInputSource|putExtra|startService)\('
)


def trace(root, source_page):
    matches = 0
    for path in sorted(root.glob('smali*/**/*.smali')):
        relative = path.relative_to(root).as_posix()
        if source_page:
            if '/source/' not in relative and 'SourceActivity' not in path.stem:
                continue
            if '/uicustom/' not in relative and '/partnercustomizer/' not in relative:
                continue
        elif '/com/coolux/source/' not in relative:
            continue
        lines = path.read_text(encoding='utf-8').splitlines()
        selected = []
        for index, line in enumerate(lines):
            if METHOD.match(line):
                start = index
                selected = []
            elif SOURCE_CALL.search(line):
                selected.append(index)
            elif line == '.end method' and selected:
                matches += 1
                print(f'HDMI8054 {relative}:{start + 1} {lines[start]}')
                for index in range(start + 1, index):
                    instruction = lines[index].strip()
                    if instruction and not instruction.startswith(('.line ', '.local ', '.end local ')):
                        print(f'HDMI8054_LINE {index + 1} {instruction}')
    print(f'HDMI8054_COUNT {root.name} {matches}')


if __name__ == '__main__':
    trace(Path(sys.argv[1]), source_page=True)
    trace(Path(sys.argv[2]), source_page=False)

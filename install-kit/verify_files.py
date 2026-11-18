"""Verify the complete downloaded kit; checksums are not a substitute for a trusted source."""
import argparse
import re
import sys
from pathlib import Path
sys.dont_write_bytecode = True
from kit_common import sha256


def verify_files(kit):
    known = set()
    for line in (kit / 'SHA256SUMS').read_text(encoding='utf-8').splitlines():
        match = re.fullmatch(r'([0-9a-f]{64})  (.+)', line)
        if not match: raise ValueError('Malformed SHA256SUMS')
        relative = Path(match[2]); path = (kit / relative).resolve()
        if relative.is_absolute() or not path.is_relative_to(kit.resolve()) or match[2] in known:
            raise ValueError('Unsafe/duplicate checksum path')
        known.add(match[2])
        if not path.is_file() or sha256(path) != match[1]: raise ValueError('Checksum mismatch: ' + match[2])
    actual = {p.relative_to(kit).as_posix() for p in kit.rglob('*') if p.is_file() and p != kit/'SHA256SUMS'}
    if actual != known: raise ValueError('Unlisted or missing files in kit')
    return len(known)


def main():
    parser = argparse.ArgumentParser(description=__doc__); parser.add_argument('--kit', type=Path, default=Path(__file__).resolve().parent)
    args = parser.parse_args()
    try: print('PASS complete kit checksums: ' + str(verify_files(args.kit.resolve())) + ' files'); return 0
    except (OSError, ValueError) as exc: print('FAIL ' + str(exc)); return 1


if __name__ == '__main__': sys.exit(main())

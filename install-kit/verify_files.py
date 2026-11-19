"""Verify the complete downloaded kit; checksums are not a substitute for a trusted source."""
import argparse
import json
import re
import sys
from pathlib import Path
sys.dont_write_bytecode = True
from kit_common import apk_hashes, load_pins, sha256

OFFLINE_REPORT = 'reports/offline/offline-verification.json'
INTEGRATION_REPORT = 'reports/offline/integration-audit.json'
REQUIRED = ('RUNBOOK.md', 'RECOVERY.md', 'preflight.py', 'kit_common.py', 'verify_files.py', 'verify_offline.py')


def read_report(kit, relative):
    value = json.loads((kit / relative).read_text(encoding='utf-8'))
    if not isinstance(value, dict):
        raise ValueError('Invalid JSON object: ' + relative)
    return value


def verify_revision_reports(kit, pins):
    hashes = apk_hashes(pins)
    if any(sha256(kit / pins[label + '_file']) != digest for label, digest in hashes.items()):
        raise ValueError('APK bytes do not match revision')
    offline = read_report(kit, OFFLINE_REPORT)
    audit = read_report(kit, INTEGRATION_REPORT)
    for report in (offline, audit):
        if (report.get('status'), report.get('revision'), report.get('mod_code_commit')) != (
                'PASS', pins['revision'], pins['mod_code_commit']):
            raise ValueError('APK report is stale, missing or failed')
    if (audit.get('apk_sha256') != hashes
            or {label: offline['records'][label]['sha256'] for label in hashes} != hashes
            or offline.get('static_identity') != 'PASS'
            or offline.get('install_state') != 'NOT_READY_FOR_DEVICE_INSTALL'):
        raise ValueError('Reports do not match verified APKs or safety status')
    return offline


def verify_revision_kit(kit, revision):
    pins = load_pins(kit, revision)
    for name in (*REQUIRED, 'revisions/' + revision + '.json'):
        if not (kit / name).is_file():
            raise ValueError('Missing required kit file: ' + name)
    if read_report(kit, 'revisions/' + revision + '.json') != pins:
        raise ValueError('Bundled revision profile does not match selected revision')
    assembled = read_report(kit, 'manifest.json')
    if (assembled.get('provenance') != pins or assembled.get('integration_audit') != 'PASS'
            or assembled.get('verification') != OFFLINE_REPORT
            or assembled.get('device_actions_performed') is not False
            or assembled.get('install_state') != 'NOT_READY_FOR_DEVICE_INSTALL'
            or not re.fullmatch(r'[a-f0-9]{40}', str(assembled.get('assembly_commit')))):
        raise ValueError('Manifest does not match the selected revision and safety status')
    return verify_revision_reports(kit, pins)


def verify_files(kit):
    known = set()
    for line in (kit / 'SHA256SUMS').read_text(encoding='utf-8').splitlines():
        match = re.fullmatch(r'([0-9a-f]{64})  (.+)', line)
        if not match: raise ValueError('Malformed SHA256SUMS')
        relative = Path(match[2]); path = (kit / relative).resolve()
        if (relative.is_absolute() or '..' in relative.parts or relative.as_posix() != match[2]
                or not path.is_relative_to(kit.resolve()) or match[2] in known):
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

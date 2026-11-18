"""Verify pinned APK bytes and static identity using Android SDK tools. HOST ONLY."""
import argparse
import json
import re
import sys
import subprocess
from pathlib import Path
sys.dont_write_bytecode = True
from kit_common import PACKAGE, STOCK_SHA, MOD_SHA, output_dir, run, save_json, sha256


def parse_metadata(badging, tree, permissions, signer):
    package_line = next((line for line in badging.splitlines() if line.startswith('package:')), '')
    attrs = dict(re.findall(r"([\w]+)='([^']*)'", package_line))
    def sdk(name):
        m = re.search(r"^" + name + r":'([^']+)'", badging, re.M)
        return m[1] if m else None
    root = tree.split('\n    E:', 1)[0]
    uid = re.search(r'android:sharedUserId(?:\([^)]*\))?="([^"]+)"', root)
    # Compare declared permission attributes/protectionLevel, not XML source line numbers.
    records, record, depth = [], None, 0
    for line in tree.splitlines() + ['E: END']:
        element = re.match(r'(\s*)E: ([\w-]+)', line)
        if element and record is not None and len(element[1]) <= depth:
            records.append(tuple(sorted(record))); record = None
        if element and element[2] == 'permission':
            record = []; depth = len(element[1])
        attr = re.match(r'\s*A: ([^=(]+)(?:\([^)]*\))?=(.*)', line)
        if record is not None and attr:
            record.append(attr[1] + '=' + attr[2].split(' (Raw:')[0])
    certs = sorted(set(x.lower() for x in re.findall(r'Signer #\d+ certificate SHA-256 digest:\s*([0-9a-fA-F]{64})', signer)))
    requested = sorted(line.strip() for line in permissions.splitlines() if line.strip().startswith('uses-permission'))
    return dict(package=attrs.get('name'), version_code=attrs.get('versionCode'), version_name=attrs.get('versionName'),
                min_sdk=sdk('sdkVersion'), target_sdk=sdk('targetSdkVersion'),
                shared_user_id=uid[1] if uid else None, requested_permissions=requested,
                declared_permissions=sorted(records), cert_sha256=certs,
                standalone=not attrs.get('split') and 'E: uses-split ' not in tree and not re.search(r'\bA: split=', root))


def inspect(path, aapt, apksigner, destination, runner=run):
    outputs = {}
    commands = {'badging': [aapt, 'dump', 'badging', str(path)],
                'tree': [aapt, 'dump', 'xmltree', str(path), 'AndroidManifest.xml'],
                'permissions': [aapt, 'dump', 'permissions', str(path)],
                'signer': [apksigner, 'verify', '--verbose', '--print-certs', str(path)]}
    for label, command in commands.items():
        code, stdout, stderr = runner(command)
        (destination / (path.stem + '-' + label + '.txt')).write_text(stdout + '\n' + stderr, encoding='utf-8')
        if code:
            raise ValueError(f'{path.name}: {label} failed ({code})')
        outputs[label] = stdout
    return parse_metadata(**outputs)


def compare(records):
    failures = []
    stock = records['stock']
    required = ['package', 'version_code', 'version_name', 'min_sdk', 'target_sdk', 'shared_user_id', 'cert_sha256']
    for label, record in records.items():
        for key in required:
            if not record.get(key): failures.append(f'{label}: missing {key}')
        if record.get('package') != PACKAGE: failures.append(f'{label}: wrong package ID')
        if not record.get('requested_permissions'): failures.append(f'{label}: permission inventory missing')
        if not record.get('standalone'): failures.append(f'{label}: splits/dependencies unsupported')
        if len(record.get('cert_sha256', [])) != 1: failures.append(f'{label}: expected one actual APK signer')
        if label != 'stock':
            keys = required + ['requested_permissions', 'declared_permissions']
            if label == 'mod':
                keys = [key for key in keys if key not in ('version_code', 'version_name')]
            for key in keys:
                if record.get(key) != stock.get(key): failures.append(f'{label}: {key} mismatch')
    return failures


def assess_versions(records):
    result = dict(upgrade='BLOCKED_IDENTITY_OR_METADATA', rollback_A='NOT_VERIFIED_ON_8054',
                  rollback_B='BLOCKED_IDENTITY_OR_METADATA')
    if compare(records):
        return result
    try:
        stock, mod = (int(records[key]['version_code']) for key in ('stock', 'mod'))
        if min(stock,mod) <= 0:
            return result
    except (ValueError,TypeError):
        return result
    result['stock_version_code'] = stock
    result['mod_version_code'] = mod
    result['upgrade'] = ('STATIC_CANDIDATE_LIVE_PM_REQUIRED' if mod > stock
                         else 'BLOCKED_EXPECTED_VERSION_INCREASE')
    result['rollback_B'] = ('STANDARD_REPLACE_DOWNGRADE_BLOCKED' if mod > stock
                            else 'NOT_VERIFIED_ON_8054')
    return result


def verify(kit, destination, aapt='aapt', apksigner='apksigner', live=None, runner=run):
    report = dict(status='FAIL', readiness='NOT_READY', install_state='NOT_READY_FOR_DEVICE_INSTALL',
                  records={}, failures=[], live_identity='NOT_TESTED',
                  static_identity='NOT_VERIFIED', upgrade='NOT_VERIFIED',
                  rollback_A='NOT_VERIFIED_ON_8054', rollback_B='NOT_VERIFIED',
                  remaining_proofs=['live package identity/state', 'critical services', 'device rollback support/drill',
                                    'reliable out-of-band ADB after approved reboot', 'owner installation permission'])
    try:
        pins = json.loads((kit / 'provenance.json').read_text(encoding='utf-8-sig'))
        paths = {'stock': kit / pins['stock_file'], 'mod': kit / pins['mod_file']}
        if any(not path.resolve().is_relative_to(kit.resolve()) for path in paths.values()):
            raise ValueError('Asset path leaves kit directory')
        expected = {'stock': STOCK_SHA, 'mod': MOD_SHA}
        if pins['stock_sha256'] != STOCK_SHA or pins['mod_sha256'] != MOD_SHA:
            raise ValueError('Provenance pins differ from reviewed script pins')
        if live: paths['live'] = Path(live); expected['live'] = STOCK_SHA
        for label, path in paths.items():
            # Verify all bytes before invoking tools or accepting any metadata.
            if sha256(path) != expected[label]: raise ValueError(f'{label}: pinned SHA256 mismatch; STOP')
        for label, path in paths.items():
            report['records'][label] = inspect(path, aapt, apksigner, destination, runner)
            report['records'][label]['sha256'] = expected[label]
        report['failures'] = compare(report['records'])
        if live: report['live_identity'] = 'PASS' if not report['failures'] else 'FAIL'
        report['static_identity'] = 'FAIL' if report['failures'] else 'PASS'
        report.update(assess_versions(report['records']))
        if report['upgrade'] != 'STATIC_CANDIDATE_LIVE_PM_REQUIRED':
            report['failures'].append('Upgrade preliminary result: '+report['upgrade'])
        if not report['failures']: report.update(status='PASS', readiness='READY_FOR_OWNER_REVIEW')
    except (ValueError, OSError, KeyError, subprocess.SubprocessError) as exc:
        report['failures'].append(str(exc))
    save_json(destination / 'offline-verification.json', report)
    for label, record in report['records'].items():
        print('APK ' + label + ': ' + json.dumps({key: record.get(key) for key in
              ('sha256', 'package', 'version_code', 'min_sdk', 'target_sdk', 'shared_user_id', 'cert_sha256', 'standalone')}, sort_keys=True))
    print(f"{report['status']} offline assets; {report['readiness']}; NOT_READY_FOR_DEVICE_INSTALL")
    print('Upgrade/rollback: '+json.dumps({key: report[key] for key in
          ('static_identity','upgrade','rollback_A','rollback_B')},sort_keys=True))
    for failure in report['failures']: print('FAIL ' + failure)
    return report


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--kit', type=Path, default=Path(__file__).resolve().parent)
    parser.add_argument('--out', help='New host directory; on Linux must be under /tmp')
    parser.add_argument('--aapt', default='aapt'); parser.add_argument('--apksigner', default='apksigner')
    parser.add_argument('--live', type=Path, help='Previously pulled installed BASE APK, never an install action')
    args = parser.parse_args()
    try: destination = output_dir(args.out, 'tvsettings8054-offline-')
    except (OSError, ValueError) as exc: print('FAIL host output: '+str(exc)+' / NOT_READY'); return 1
    report = verify(args.kit.resolve(), destination, args.aapt, args.apksigner, args.live)
    print('Host report: ' + str(destination))
    return 0 if report['status'] == 'PASS' else 1


if __name__ == '__main__': sys.exit(main())

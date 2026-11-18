"""Finalize a host-side kit only after static verification; never declare device readiness."""
import argparse
import json
from pathlib import Path
from kit_common import STOCK_SHA, MOD_SHA, save_json, sha256
from verify_files import verify_files


def build(kit, commit):
    pins = json.loads((kit/'provenance.json').read_text(encoding='utf-8'))
    verification = json.loads((kit/'reports/offline/offline-verification.json').read_text(encoding='utf-8'))
    audit_path = kit/'reports/offline/integration-audit.json'
    audit = json.loads(audit_path.read_text(encoding='utf-8')) if audit_path.is_file() else {'status':'NOT_RUN'}
    ready = (audit['status'] == 'PASS' and verification['status'] == 'PASS' and sha256(kit/pins['stock_file']) == STOCK_SHA
             and sha256(kit/pins['mod_file']) == MOD_SHA)
    save_json(kit/'manifest.json', dict(provenance=pins, assembly_commit=commit,
        readiness='READY_FOR_OWNER_REVIEW' if ready else 'NOT_READY',
        install_state='NOT_READY_FOR_DEVICE_INSTALL', device_actions_performed=False,
        verification='reports/offline/offline-verification.json',
        integration_audit=audit['status'], compatibility={key: verification[key] for key in
            ('static_identity','upgrade','rollback_A','rollback_B')},
        note='Pinned MOD bytes are from code commit 3b3ec23. The menu retry fix in the assembly commit is not in this APK.'))
    files = sorted(p for p in kit.rglob('*') if p.is_file() and p != kit/'SHA256SUMS')
    (kit/'SHA256SUMS').write_text(''.join(sha256(p)+'  '+p.relative_to(kit).as_posix()+'\n' for p in files), encoding='utf-8')
    print('PASS final checksum manifest: '+str(verify_files(kit))+' files')
    print(('READY_FOR_OWNER_REVIEW' if ready else 'NOT_READY')+' / NOT_READY_FOR_DEVICE_INSTALL')


if __name__ == '__main__':
    parser=argparse.ArgumentParser(description=__doc__); parser.add_argument('kit',type=Path); parser.add_argument('--commit',required=True)
    args=parser.parse_args(); build(args.kit.resolve(),args.commit)

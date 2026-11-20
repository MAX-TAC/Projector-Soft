"""Finalize a host-side kit only after static verification; never declare device readiness."""
import argparse
import json
from pathlib import Path
from kit_common import apk_hashes, load_pins, save_json, sha256
from verify_files import verify_files, verify_revision_kit, verify_revision_reports


def build(kit, commit, revision=None):
    pins = load_pins(kit, revision)
    verification = json.loads((kit/'reports/offline/offline-verification.json').read_text(encoding='utf-8'))
    audit_path = kit/'reports/offline/integration-audit.json'
    audit = json.loads(audit_path.read_text(encoding='utf-8')) if audit_path.is_file() else {'status':'NOT_RUN'}
    if revision:
        verify_revision_reports(kit, pins)
    ready = (audit['status'] == 'PASS' and verification['status'] == 'PASS'
             and all(sha256(kit / pins[label + '_file']) == digest for label, digest in apk_hashes(pins).items()))
    statuses = dict(offline='OFFLINE_KIT_VERIFIED', stock='STOCK_DIAGNOSTICS_PREPARED',
                    rollback='ROLLBACK_NOT_VERIFIED', apk='BUILT_UNTESTED') if revision else {}
    save_json(kit/'manifest.json', dict(provenance=pins, assembly_commit=commit,
        readiness='READY_FOR_OWNER_REVIEW' if ready else 'NOT_READY',
        install_state='NOT_READY_FOR_DEVICE_INSTALL', device_actions_performed=False,
        verification='reports/offline/offline-verification.json',
        integration_audit=audit['status'], compatibility={key: verification[key] for key in
            ('static_identity','upgrade','rollback_A','rollback_B')},
        apk_state='FINAL_APK_STATIC_AUDIT_PASS / BUILT_UNTESTED' if ready else 'STATIC_AUDIT_INCOMPLETE',
        **({'statuses': statuses} if revision else {}),
        note='Unchanged exact CI APK from the pinned code commit.' if revision else
             'Unchanged exact CI APK from the pinned code commit, including the menu retry fix.'))
    files = sorted(p for p in kit.rglob('*') if p.is_file() and p != kit/'SHA256SUMS')
    (kit/'SHA256SUMS').write_text(''.join(sha256(p)+'  '+p.relative_to(kit).as_posix()+'\n' for p in files), encoding='utf-8')
    print('PASS final checksum manifest: '+str(verify_files(kit))+' files')
    if revision:
        verify_revision_kit(kit, revision)
    print(('READY_FOR_OWNER_REVIEW' if ready else 'NOT_READY')+' / NOT_READY_FOR_DEVICE_INSTALL')


if __name__ == '__main__':
    parser=argparse.ArgumentParser(description=__doc__); parser.add_argument('kit',type=Path); parser.add_argument('--commit',required=True)
    parser.add_argument('--revision')
    args=parser.parse_args(); build(args.kit.resolve(),args.commit,args.revision)

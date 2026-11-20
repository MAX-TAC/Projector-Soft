"""Fetch pinned GitHub CI inputs and stage only the reviewed APK bytes and host tools."""
import argparse
import json
import os
import shutil
import subprocess
import zipfile
from pathlib import Path

from kit_common import save_json, sha256, trusted_revision

ROOT = Path(__file__).resolve().parent
API = 'repos/MAX-TAC/Projector-Soft/actions'
EXACT_APK = 'TvSettingsPlus-8054-v33-NATIVE-CONTROLS-EXPERIMENTAL.apk'


def api(path):
    return json.loads(subprocess.check_output(['gh', 'api', API + path], text=True))


def artifact(source, run, artifact_id, digest, label):
    records = api(f'/runs/{run}/artifacts')['artifacts']
    record = next(item for item in records if item['id'] == artifact_id)
    if record['expired'] or record['digest'] != 'sha256:' + digest:
        raise ValueError(f'{label}: unexpected GitHub artifact metadata')
    archive = source / (label + '.zip')
    with archive.open('wb') as stream:
        subprocess.run(['gh', 'api', f'{API}/artifacts/{artifact_id}/zip'], stdout=stream, check=True)
    if sha256(archive) != digest:
        raise ValueError(f'{label}: downloaded ZIP SHA-256 mismatch')
    print(f'PASS {label} ZIP SHA-256 {digest}', flush=True)
    return archive, record


def extract_apk(archive, label, kit, pins):
    with zipfile.ZipFile(archive) as payload:
        apks = [name for name in payload.namelist() if name.endswith('.apk')]
        if label == 'mod':
            if len(apks) != 1 or Path(apks[0]).name != EXACT_APK:
                raise ValueError('Exact CI artifact does not contain the expected single MOD APK')
            member = apks[0]
        else:
            member = pins['stock_file']
            if apks.count(member) != 1:
                raise ValueError('Expected one pinned STOCK member in baseline artifact')
        destination = kit / pins[label + '_file']
        with payload.open(member) as source, destination.open('wb') as target:
            shutil.copyfileobj(source, target)
    if sha256(destination) != pins[label + '_sha256']:
        raise ValueError(f'{label}: extracted APK SHA-256 mismatch')
    print(f'PASS {label} APK SHA-256 {pins[label + "_sha256"]}', flush=True)


def stage(revision, source, kit):
    pins = trusted_revision(revision)
    source.mkdir()
    (kit / 'assets').mkdir(parents=True)
    (kit / 'reports').mkdir()
    (kit / 'revisions').mkdir()
    for label, run_id in {'exact': pins['build_run'], **pins['regression_runs']}.items():
        run = api(f'/runs/{run_id}')
        if run['conclusion'] != 'success' or run['head_sha'] != pins['mod_code_commit']:
            raise ValueError(f'{label}: CI success missing for exact MOD commit')
        print(f'PASS pinned CI {label} {run_id}', flush=True)

    transport = pins['stock_transport']
    for label, run, artifact_id, digest in (
            ('mod', pins['build_run'], pins['input_artifact_id'], pins['input_artifact_zip_digest']),
            ('stock', transport['run'], transport['artifact'], transport['zip_sha256'])):
        archive, record = artifact(source, run, artifact_id, digest, label)
        extract_apk(archive, label, kit, pins)
        save_json(kit / 'reports' / (label + '-input-artifact.json'),
                  dict(run=run, artifact=record['id'], verified_zip_sha256=digest))
    for name in ('kit_common.py', 'verify_files.py', 'verify_offline.py', 'preflight.py'):
        shutil.copyfile(ROOT / name, kit / name)
    for name in ('RUNBOOK.md', 'RECOVERY.md'):
        shutil.copyfile(ROOT / 'revisions' / revision / name, kit / name)
    shutil.copyfile(ROOT / 'revisions' / (revision + '.json'), kit / 'provenance.json')
    shutil.copyfile(kit / 'provenance.json', kit / 'revisions' / (revision + '.json'))
    with Path(os.environ['GITHUB_OUTPUT']).open('a', encoding='utf-8') as output:
        output.write('commit=' + pins['mod_code_commit'] + '\n')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('revision')
    parser.add_argument('source', type=Path)
    parser.add_argument('kit', type=Path)
    args = parser.parse_args()
    stage(args.revision, args.source, args.kit)

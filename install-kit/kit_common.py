"""Host-only helpers. No device mutation, installation or recovery commands."""
import hashlib
import json
import os
import platform
import re
import subprocess
import tempfile
from pathlib import Path

PACKAGE = 'com.android.tv.settings'
STOCK_SHA = 'f8e0c6fe0f46e7eb8db651c31d85ef8b32a769b37e4dd0f96ed19a64725d74ee'
MOD_SHA = '05f74576f6b777eb8c7b73699bdda47212280185fc1324ff45dc43166f9cbf3f'


def trusted_revision(revision):
    if not re.fullmatch(r'[a-f0-9]{7,40}', revision):
        raise ValueError('Invalid kit revision')
    path = Path(__file__).resolve().parent / 'revisions' / (revision + '.json')
    pins = json.loads(path.read_text(encoding='utf-8'))
    if (pins['revision'] != revision or not pins['mod_code_commit'].startswith(revision)
            or (pins['package'], pins['machine_id'], pins['api']) != (PACKAGE, '8054', '33')):
        raise ValueError('Invalid trusted kit profile')
    return pins


def load_pins(kit, revision):
    pins = json.loads((kit / 'provenance.json').read_text(encoding='utf-8-sig'))
    if not isinstance(pins, dict):
        raise ValueError('Invalid kit provenance')
    if revision is not None:
        if pins != trusted_revision(revision):
            raise ValueError('Kit provenance differs from selected revision')
    elif ('revision' in pins or pins.get('stock_sha256') != STOCK_SHA or pins.get('mod_sha256') != MOD_SHA):
        raise ValueError('Provenance differs from legacy pins; select the exact revision explicitly')
    return pins


def apk_hashes(pins):
    return {label: pins[label + '_sha256'] for label in ('stock', 'mod')}


def sha256(path):
    h = hashlib.sha256()
    with Path(path).open('rb') as stream:
        for block in iter(lambda: stream.read(8 * 1024 * 1024), b''):
            h.update(block)
    return h.hexdigest()


def run(args, timeout=90):
    if Path(args[0]).suffix.lower() == '.jar':
        args = ['java', '-jar'] + list(args)
    # Windows Android SDK wrappers are .bat/.cmd. cmd.exe needs robust quoting;
    # reject shell metacharacters and use list2cmdline only for trusted local tool paths.
    if os.name == 'nt' and Path(args[0]).suffix.lower() in ('.bat', '.cmd'):
        if any(re.search(r'[&|<>^%!"\r\n]', str(x)) for x in args):
            raise ValueError('Unsafe Windows SDK wrapper argument')
        command = 'cmd.exe /d /s /c "' + subprocess.list2cmdline([str(x) for x in args]) + '"'
    else:
        command = [str(x) for x in args]
    result = subprocess.run(command, capture_output=True, text=True,
                            encoding='utf-8', errors='replace', timeout=timeout, shell=False)
    return result.returncode, result.stdout, result.stderr


def output_dir(path=None, prefix='tvsettings8054-'):
    if path:
        directory = Path(path).resolve()
        if platform.system() == 'Linux' and not directory.is_relative_to(Path('/tmp')):
            raise ValueError('Linux diagnostic outputs must be under /tmp on the HOST')
        directory.mkdir(parents=True, exist_ok=False)
    else:
        directory = Path(tempfile.mkdtemp(prefix=prefix, dir='/tmp' if platform.system() == 'Linux' else None))
    return directory


def save_json(path, value):
    Path(path).write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')

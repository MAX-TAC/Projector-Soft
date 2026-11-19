import json
import shutil
import sys
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

import build_manifest
import kit_common
import preflight
from test_kit import BADGING, PERMS, SIGNER, TREE
from verify_files import INTEGRATION_REPORT, OFFLINE_REPORT, REQUIRED
from verify_offline import verify


class RevisionKitTests(unittest.TestCase):
    def setUp(self):
        temp = tempfile.TemporaryDirectory()
        self.addCleanup(temp.cleanup)
        self.root = Path(temp.name)
        self.kit = self.root / 'kit'
        (self.kit / 'assets').mkdir(parents=True)
        (self.kit / 'revisions').mkdir()
        (self.kit / 'reports/offline').mkdir(parents=True)
        self.revision = 'c3c7654'
        self.pins = kit_common.trusted_revision(self.revision)
        for label in ('mod', 'stock'):
            asset = self.kit / self.pins[label + '_file']
            asset.write_bytes(label.encode())
            self.pins[label + '_sha256'] = kit_common.sha256(asset)
        for name in REQUIRED:
            (self.kit / name).write_text('fixture', encoding='utf-8')
        for name in ('provenance.json', 'revisions/' + self.revision + '.json'):
            kit_common.save_json(self.kit / name, self.pins)
        trusted = patch('kit_common.trusted_revision', return_value=self.pins)
        trusted.start()
        self.addCleanup(trusted.stop)
        stock = patch('preflight.STOCK_SHA', self.pins['stock_sha256'])
        stock.start()
        self.addCleanup(stock.stop)
        assert verify(self.kit, self.kit / 'reports/offline', runner=self.sdk, revision=self.revision)['status'] == 'PASS'
        kit_common.save_json(self.kit / INTEGRATION_REPORT,
                             dict(status='PASS', revision=self.revision, mod_code_commit=self.pins['mod_code_commit'],
                                  apk_sha256=kit_common.apk_hashes(self.pins)))
        build_manifest.build(self.kit, 'a' * 40, self.revision)
        self.adb = []
        self.overrides = {}

    def sdk(self, args, *, badging=BADGING, tree=TREE, signer=None):
        if args[0] == 'apksigner':
            return 0, signer or SIGNER.replace('a' * 64, self.pins['signer_sha256']), ''
        self.assertEqual(args[:2], ['aapt', 'dump'])
        if args[2] == 'badging' and Path(args[3]).name.startswith('MOD'):
            badging = badging.replace("versionCode='1'", "versionCode='33'")
        return 0, {'badging': badging, 'xmltree': tree, 'permissions': PERMS}[args[2]], ''

    def runner(self, args):
        if args[0] != 'adb':
            return self.sdk(args)
        self.assertEqual(args[:3], ['adb', '-s', 'TEST-SERIAL'])
        command = args[3:]
        self.adb.append(command)
        if tuple(command) in self.overrides:
            value = self.overrides[tuple(command)]
            return value if isinstance(value, tuple) else (0, value, '')
        if command[0] in ('get-state', 'version', 'help'):
            self.assertEqual(len(command), 1)
            return 0, 'device', ''
        if command[0] == 'pull':
            self.assertEqual(command[1], preflight.BUILTIN)
            Path(command[2]).write_bytes(b'stock')
            return 0, 'pulled', ''
        self.assertEqual(command[0], 'shell')
        shell = command[1:]
        read_settings = [['settings', 'get', *item] for item in preflight.INACTIVITY]
        self.assertIn(shell, list(preflight.commands().values()) + read_settings)
        self.assertFalse(set(shell) & {'install', 'uninstall', 'uninstall-system-updates', 'root',
                                       'remount', 'reboot', 'reset', 'put', 'delete', 'disable', 'start'})
        if shell in read_settings:
            return 0, 'null' if shell[2:] == ['system', 'sleep_timeout'] else '30000', ''
        if shell[:2] == ['dumpsys', 'package']:
            return 0, ('pkgFlags=[ SYSTEM ]\nversionCode=1\n'
                       'User 0: installed=true hidden=false suspended=false enabled=0\n'
                       'sharedUser=SharedUserSetting{123 android.uid.system/1000}'), ''
        if 'resolve-activity' in shell:
            return 0, 'com.android.tv.settings/.MainActivity', ''
        responses = {
            'machine': '8054', 'api': '33', 'release': '13', 'user': '0',
            'shell_identity': 'uid=2000(shell)', 'selinux': 'Enforcing',
            'settings_path': 'package:' + preflight.BUILTIN,
            'source_services': '* ServiceRecord{abcd u0 com.coolux.source/.service.TimeService}',
            'pm_help': '  uninstall-system-updates [<PACKAGE>]',
        }
        name = next(key for key, value in preflight.commands().items() if shell == value)
        return 0, responses.get(name, 'captured evidence'), ''

    def main(self, *options, runner=None):
        destination = self.root / 'out'
        shutil.rmtree(destination, ignore_errors=True)
        args = ['preflight.py', '--kit', str(self.kit), '--out', str(destination), *options]
        with patch.object(sys, 'argv', args), patch.object(preflight, 'run', runner or self.runner), \
                patch.object(preflight, 'output_dir', return_value=destination):
            destination.mkdir()
            code = preflight.main()
        return code, destination

    def test_valid_offline_and_full_report_never_approve_install(self):
        self.assertEqual(self.main('--offline', '--revision', self.revision)[0], 0)
        self.assertFalse(self.adb)
        code, out = self.main('--revision', self.revision, '--serial', 'TEST-SERIAL')
        self.assertEqual(code, 2)
        report = json.loads((out / 'preflight.json').read_text(encoding='utf-8'))
        self.assertEqual(report['install_state'], 'NOT_READY_FOR_DEVICE_INSTALL')
        self.assertIn('NOT_VERIFIED', report['rollback_route_A'])
        self.assertEqual(report['apks']['builtin_sha256'], self.pins['stock_sha256'])
        self.assertEqual(report['inactivity']['system/sleep_timeout'], dict(raw='null', status='ABSENT'))
        self.assertFalse(report['device_changes'])

    def test_invalid_revision_missing_serial_and_apk_tampering_stop_before_adb(self):
        self.assertEqual(self.main('--revision', self.revision)[0], 1)
        self.assertEqual(self.main('--serial', 'TEST-SERIAL')[0], 1)
        self.assertEqual(self.main('--revision', '0000000', '--serial', 'TEST-SERIAL')[0], 1)
        for label in ('mod', 'stock'):
            asset = self.kit / self.pins[label + '_file']
            original = asset.read_bytes()
            for mode in ('missing', 'tampered'):
                with self.subTest(label=label, mode=mode):
                    if mode == 'missing':
                        asset.unlink()
                    else:
                        asset.write_bytes(b'other')
                    self.assertEqual(self.main('--revision', self.revision, '--serial', 'TEST-SERIAL')[0], 1)
                    asset.write_bytes(original)
        self.assertFalse(self.adb)

    def test_host_metadata_failures_never_call_adb(self):
        for changes in (
                {'badging': BADGING.replace('com.android.tv.settings', 'wrong.package')},
                {'badging': BADGING.replace("versionCode='1'", "versionCode='32'")},
                {'badging': BADGING.replace("versionCode='1'", "versionCode='1' split='feature'")},
                {'tree': TREE.replace('android:sharedUserId', 'other')},
                {'signer': SIGNER},
                {'tree': TREE.replace('0x01010003', '0x01010004').replace('settings.TEST', 'settings.OTHER')}):
            with self.subTest(changes=changes):
                def mismatch(args):
                    self.assertNotEqual(args[0], 'adb')
                    return self.sdk(args, **changes)
                self.assertEqual(self.main('--revision', self.revision, '--serial', 'TEST-SERIAL', runner=mismatch)[0], 1)

    def test_device_read_errors_and_updated_system_are_stop(self):
        for command, value in (
                (('get-state',), (1, '', 'FileNotFoundError')),
                (('shell', 'getprop', 'ro.rjpj.machine.id'), '8052'),
                (('shell', 'dumpsys', 'package', kit_common.PACKAGE), (0, 'Permission Denial', '')),
                (('shell', 'pm', 'path', kit_common.PACKAGE), ''),
                (('shell', 'pm', 'help'), (1, '', 'Permission denied')),
                (('shell', 'dumpsys', 'activity', 'services', 'com.coolux.source'), 'TimeService in stale description'),
                (('shell', 'settings', 'get', 'secure', 'sleep_timeout'), 'unknown'),
                (('shell', 'dumpsys', 'package', kit_common.PACKAGE), (
                     'pkgFlags=[ SYSTEM UPDATED_SYSTEM_APP ]\nversionCode=1\n'
                     'User 0: installed=true hidden=false suspended=false enabled=0\nsharedUser=system'))):
            with self.subTest(command=command):
                self.overrides = {command: value}
                self.assertEqual(self.main('--revision', self.revision, '--serial', 'TEST-SERIAL')[0], 1)

    def test_builtin_pull_failure_is_stop(self):
        def fail_builtin(args):
            if args[:5] == ['adb', '-s', 'TEST-SERIAL', 'pull', preflight.BUILTIN] and args[-1].endswith('BUILTIN-base.apk'):
                return 1, '', 'Permission denied'
            return self.runner(args)
        self.assertEqual(self.main('--revision', self.revision, '--serial', 'TEST-SERIAL', runner=fail_builtin)[0], 1)

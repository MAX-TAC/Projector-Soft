import copy
import json
import os
import subprocess
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch
import kit_common
import preflight
import verify_offline
import extract_stock
from verify_files import verify_files

BADGING = "package: name='com.android.tv.settings' versionCode='1' versionName='1.0'\nsdkVersion:'23'\ntargetSdkVersion:'33'\n"
TREE = '''N: android=http://schemas.android.com/apk/res/android
  E: manifest (line=1)
    A: package="com.android.tv.settings"
    A: android:sharedUserId(0x0101000b)="android.uid.system"
    E: uses-sdk (line=2)
    E: permission (line=3)
      A: android:name(0x01010003)="com.android.tv.settings.TEST"
      A: android:protectionLevel(0x01010009)=(type 0x11)0x2
    E: application (line=4)
'''
PERMS = "package: com.android.tv.settings\nuses-permission: name='android.permission.WRITE_SECURE_SETTINGS'\n"
SIGNER = 'Signer #1 certificate SHA-256 digest: ' + 'a' * 64


def picture_runner(overrides=None):
    calls = []
    responses = {
        ('get-state',): 'device',
        ('shell', 'id'): 'uid=2000(shell) gid=2000(shell)',
        ('shell', 'getprop', 'ro.rjpj.machine.id'): '8054',
        ('shell', 'getprop', 'ro.build.version.sdk'): '33',
        ('shell', 'pm', 'path', kit_common.PACKAGE): 'package:' + preflight.BUILTIN,
        ('shell', 'sha256sum', preflight.BUILTIN): kit_common.STOCK_SHA + '  ' + preflight.BUILTIN,
        ('shell', 'getprop', 'ro.build.fingerprint'): 'stock/fingerprint',
        ('shell', 'dumpsys', 'tv_input'): 'connected HW6, HW7; active source unknown',
        ('shell', 'dumpsys', 'display'): 'HDR capabilities; active format unknown',
        ('shell', 'dumpsys', 'package', 'com.mediatek.tv.service'): 'Package [com.mediatek.tv.service]',
        ('shell', 'dumpsys', 'activity', 'services', 'com.mediatek.tv.service'):
            '* ServiceRecord{abc u0 com.mediatek.tv.service/.agent.TVMenuSettingsService}',
        **{('shell', 'settings', 'get', 'global', key): str(value)
           for key, value in zip(preflight.PICTURE_KEYS, (7, 51, 45, 52))}
    }
    responses.update(overrides or {})

    def run(args):
        calls.append(args[3:])
        if args[:3] != ['adb', '-s', 'STOCK-SERIAL']:
            raise AssertionError(args)
        value = responses[tuple(args[3:])]
        if isinstance(value, list):
            value = value.pop(0)
        return 0, value + '\n', ''

    return run, calls, responses


class KitTests(unittest.TestCase):
    def record(self): return verify_offline.parse_metadata(BADGING, TREE, PERMS, SIGNER)

    def test_all_identity_mismatches_stop(self):
        original = self.record()
        self.assertEqual(verify_offline.compare({'stock': original, 'mod': copy.deepcopy(original)}), [])
        for key, replacement in [('package', 'other'), ('min_sdk', '24'),
                                 ('target_sdk', '34'), ('shared_user_id', 'other.uid'),
                                 ('cert_sha256', ['b'*64]), ('requested_permissions', []),
                                 ('declared_permissions', []), ('standalone', False)]:
            with self.subTest(key=key):
                modified = copy.deepcopy(original); modified[key] = replacement
                self.assertTrue(verify_offline.compare({'stock': original, 'mod': modified}))
        self.assertFalse(verify_offline.parse_metadata(BADGING.replace("versionCode='1'", "versionCode='1' split='feature'"), TREE, PERMS, SIGNER)['standalone'])
        self.assertTrue(verify_offline.compare({'stock': self.record(), 'live': self.record() | {'cert_sha256': []}}))

    def test_upgrade_and_rollback_have_independent_version_results(self):
        stock = self.record()
        mod = stock | {'version_code': '33', 'version_name': 'MOD'}
        records = {'stock': stock, 'mod': mod}
        self.assertEqual(verify_offline.compare(records), [])
        result = verify_offline.assess_versions(records)
        self.assertEqual(result['upgrade'], 'STATIC_CANDIDATE_LIVE_PM_REQUIRED')
        self.assertEqual(result['rollback_B'], 'STANDARD_REPLACE_DOWNGRADE_BLOCKED')
        self.assertEqual(result['rollback_A'], 'NOT_VERIFIED_ON_8054')
        self.assertEqual(verify_offline.assess_versions(records | {'mod': stock})['upgrade'],
                         'BLOCKED_EXPECTED_VERSION_INCREASE')
        self.assertEqual(verify_offline.assess_versions(records | {'mod': mod | {'cert_sha256': ['b'*64]}})['upgrade'],
                         'BLOCKED_IDENTITY_OR_METADATA')
        self.assertTrue(verify_offline.compare(records | {'live': mod}))

    def test_hash_mismatch_never_invokes_tools(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary); (root/'stock.apk').write_bytes(b'wrong'); (root/'mod.apk').write_bytes(b'wrong')
            pins = dict(stock_file='stock.apk', mod_file='mod.apk', stock_sha256=verify_offline.STOCK_SHA, mod_sha256=verify_offline.MOD_SHA)
            (root/'provenance.json').write_text(json.dumps(pins), encoding='utf-8')
            def forbidden(args): raise AssertionError('Tool invoked before pin check')
            report = verify_offline.verify(root, root, runner=forbidden)
            self.assertEqual(report['readiness'], 'NOT_READY')
            self.assertEqual(report['install_state'], 'NOT_READY_FOR_DEVICE_INSTALL')

    def test_package_state_fails_closed_and_help_is_not_recovery_proof(self):
        text = 'pkgFlags=[ SYSTEM UPDATED_SYSTEM_APP ]\nversionCode=1\nUser 0: installed=true hidden=false suspended=false enabled=0\nsharedUser=system'
        self.assertTrue(preflight.package_state(text, '0')['enabled_installed_user'])
        for changed in [text.replace('enabled=0', 'enabled=3'), text.replace('installed=true', 'installed=false'),
                        text.replace('hidden=false', 'hidden=true'), text.replace('suspended=false', ''), '']:
            self.assertFalse(preflight.package_state(changed, '0')['enabled_installed_user'])
        with tempfile.TemporaryDirectory() as temporary:
            calls = []
            def disconnected(args): calls.append(args); return 0, 'offline', ''
            report = preflight.collect(Path(temporary), 'EXPLICIT-SERIAL', Path(temporary), runner=disconnected)
            self.assertEqual(report['install_state'], 'NOT_READY_FOR_DEVICE_INSTALL')
            self.assertEqual(len(calls), 1)
            self.assertEqual(calls[0][-1], 'get-state')
            self.assertTrue(all(c[2] == 'EXPLICIT-SERIAL' for c in calls))

    def test_read_only_command_allowlist(self):
        allowed = {('getprop',), ('am', 'get-current-user'), ('id',), ('getenforce',), ('cat',),
                   ('pm', 'path'), ('dumpsys', 'package'), ('cmd', 'package', 'resolve-activity'),
                   ('pm', 'help'), ('cmd', 'package', 'help'), ('cmd', 'package', 'get-privapp-permissions'),
                   ('cmd', 'package', 'get-privapp-deny-permissions'), ('ls', '-lZ'), ('dumpsys', 'activity', 'services')}
        for command in preflight.commands().values():
            self.assertTrue(any(tuple(command[:len(prefix)]) == prefix for prefix in allowed), command)
            self.assertFalse(set(command) & {'install', 'uninstall', 'disable-user', 'clear', 'root', 'remount', 'reboot', 'put'})
            self.assertNotIn('>', ' '.join(command))

    def test_connected_collection_never_mutates_or_approves_install(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary); calls = []
            def device(args):
                calls.append(args); command = args[3:]
                if command == ['get-state']: return 0, 'device', ''
                if command[0] == 'pull': Path(command[2]).write_bytes(b'not pinned stock'); return 0, 'copied', ''
                if command[0] != 'shell': return 0, 'host adb help', ''
                shell = command[1:]
                values = {'ro.rjpj.machine.id': '8054', 'ro.build.version.sdk': '33', 'ro.build.fingerprint': 'test/fingerprint',
                          'ro.build.display.id': 'test/build'}
                if shell[0] == 'getprop': return 0, values.get(shell[1], 'test'), ''
                if shell == ['am', 'get-current-user']: return 0, '0', ''
                if shell == ['id']: return 0, 'uid=2000(shell)', ''
                if shell == ['getenforce']: return 0, 'Enforcing', ''
                if shell[:2] == ['pm', 'path']: return 0, 'package:' + preflight.BUILTIN, ''
                if shell[:2] == ['dumpsys', 'package']:
                    return 0, 'pkgFlags=[ SYSTEM ]\nversionCode=1\nUser 0: installed=true hidden=false suspended=false enabled=0\nsharedUser=system', ''
                if 'resolve-activity' in shell: return 0, 'com.android.tv.settings/.MainActivity', ''
                if shell[:3] == ['dumpsys', 'activity', 'services']: return 0, 'com.coolux.source.service.TimeService', ''
                return 0, 'uninstall-system-updates [<PACKAGE>]', ''
            result = preflight.collect(root, 'EXPLICIT', root, runner=device)
            self.assertEqual(result['readiness'], 'NOT_READY')
            self.assertEqual(result['install_state'], 'NOT_READY_FOR_DEVICE_INSTALL')
            self.assertIn('NOT_VERIFIED', result['rollback_route_A'])
            self.assertTrue(any(c[-1] == str(root/'LIVE-base.apk') for c in calls))
            allowed_shell = list(preflight.commands().values())
            self.assertTrue(all(c[3] in ('get-state', 'version', 'help', 'pull') or c[4:] in allowed_shell for c in calls))

    def test_picture_snapshot_only_uses_known_read_commands(self):
        runner, calls, allowed = picture_runner()
        with tempfile.TemporaryDirectory() as temporary:
            path = Path(temporary)
            report = preflight.collect_stock_picture('STOCK-SERIAL', path, runner=runner)
            self.assertEqual(set(map(tuple, calls)), set(allowed))
            self.assertEqual(report['status'], 'CAPTURED')
            self.assertEqual(report['settings_global']['picture_contrast'], 45)
            self.assertEqual(report['tvmenu_service_record'], 'OBSERVED')
            self.assertEqual(report['active_source'], 'UNVERIFIED')
            self.assertEqual(report['signal_format'], 'UNVERIFIED')
            self.assertEqual(report['mediatek_config'], 'NO_CONFIRMED_SHELL_INTERFACE')
            self.assertFalse(report['device_changes'])
            self.assertEqual(json.loads((path / 'stock-picture.json').read_text()), report)

    def test_picture_snapshot_stops_before_reads_on_wrong_device_or_apk(self):
        for command, value in {
            ('get-state',): 'offline',
            ('shell', 'id'): 'uid=0(root)',
            ('shell', 'getprop', 'ro.rjpj.machine.id'): '8052',
            ('shell', 'getprop', 'ro.build.version.sdk'): '32',
            ('shell', 'pm', 'path', kit_common.PACKAGE): 'package:/data/app/base.apk',
            ('shell', 'sha256sum', preflight.BUILTIN): '0' * 64 + '  ' + preflight.BUILTIN,
        }.items():
            with self.subTest(command=command), tempfile.TemporaryDirectory() as temporary:
                runner, calls, _ = picture_runner({command: value})
                report = preflight.collect_stock_picture('STOCK-SERIAL', Path(temporary), runner=runner)
                self.assertEqual(report['status'], 'STOP')
                self.assertEqual(tuple(calls[-1]), command)
                self.assertFalse(report['stock_verified'])

    def test_picture_snapshot_exposes_missing_values_mode_change_and_denied_dumps(self):
        overrides = {
            ('shell', 'settings', 'get', 'global', 'picture_mode'): ['7', '8'],
            ('shell', 'settings', 'get', 'global', 'picture_contrast'): 'null',
            ('shell', 'settings', 'get', 'global', 'picture_brightness'): '2147483648',
            ('shell', 'dumpsys', 'tv_input'): 'Permission Denial: dumping tv_input',
            ('shell', 'dumpsys', 'activity', 'services', 'com.mediatek.tv.service'):
                "Can't find service: activity",
        }
        runner, _, _ = picture_runner(overrides)
        with tempfile.TemporaryDirectory() as temporary:
            report = preflight.collect_stock_picture('STOCK-SERIAL', Path(temporary), runner=runner)
            self.assertEqual(report['status'], 'PARTIAL')
            self.assertIsNone(report['settings_global']['picture_mode'])
            self.assertIsNone(report['settings_global']['picture_contrast'])
            self.assertIsNone(report['settings_global']['picture_brightness'])
            self.assertEqual(report['commands']['tv_input']['status'], 'DENIED')
            self.assertEqual(report['commands']['tvservices_activity']['status'], 'UNAVAILABLE')
            self.assertEqual(report['tvmenu_service_record'], 'UNVERIFIED')

    def test_checksum_tampering_and_traversal(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary); asset = root/'stock.apk'; asset.write_bytes(b'original')
            (root/'SHA256SUMS').write_text(kit_common.sha256(asset)+'  stock.apk\n', encoding='utf-8')
            self.assertEqual(verify_files(root), 1)
            asset.write_bytes(b'changed')
            with self.assertRaises(ValueError): verify_files(root)
            (root/'SHA256SUMS').write_text('a'*64+'  ../outside.apk\n', encoding='utf-8')
            with self.assertRaises(ValueError): verify_files(root)

    def test_wrong_backup_never_extracts(self):
        with tempfile.TemporaryDirectory() as temporary:
            archive=Path(temporary)/'wrong.zip'; archive.write_bytes(b'not the owner backup')
            output=Path(temporary)/'stock.apk'
            with self.assertRaises(ValueError): extract_stock.extract(archive,output)
            self.assertFalse(output.exists())

    def test_linux_host_output_location(self):
        if os.name != 'nt':
            with self.assertRaises(ValueError): kit_common.output_dir('/var/invalid-preflight-location')

    @unittest.skipUnless(os.name == 'nt', 'Windows batch SDK wrapper test')
    def test_windows_sdk_wrapper_with_spaces(self):
        with tempfile.TemporaryDirectory(prefix='SDK tools ') as temporary:
            script = Path(temporary)/'sdk test.cmd'; script.write_text('@echo off\n@echo %~1\n', encoding='ascii')
            code, stdout, stderr = kit_common.run([str(script), 'argument with spaces'])
            self.assertEqual(code, 0, stderr); self.assertEqual(stdout.strip(), 'argument with spaces')


if __name__ == '__main__': unittest.main()

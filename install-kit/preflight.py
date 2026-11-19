"""Read-only ADB collection on an ordinary HOST. Run only after owner permission."""
import argparse
import json
import re
import sys
from pathlib import Path
sys.dont_write_bytecode = True
from kit_common import PACKAGE, STOCK_SHA, output_dir, run, save_json, sha256
from verify_offline import verify
from verify_files import read_report, verify_files, verify_revision_kit

CRITICAL = {'KuSettings': 'com.my.settings', 'KuSourceRJ': 'com.coolux.source',
            'TvServices': 'com.mediatek.tv.service', 'RuiAutoFocus': 'com.hysd.screen',
            'KeystoneCorrection': 'com.rjpj.tcorrection', 'AutoPair': 'com.mediatek.autopair'}
BUILTIN = '/system_ext/priv-app/TvSettingsPlus/TvSettingsPlus.apk'
EDID_KEY = 'tv_input_hdmi_edid_version'
PICTURE_KEYS = ('picture_mode', 'picture_brightness', 'picture_contrast', 'picture_saturation')
INACTIVITY = (('system', 'screen_off_timeout'), ('system', 'sleep_timeout'),
              ('secure', 'sleep_timeout'), ('secure', 'screensaver_enabled'))
IDENTITY = ('api', 'release', 'user', 'shell_identity', 'selinux', 'fingerprint', 'build')
TIME_SERVICE = re.compile(r'\bServiceRecord\{[^}\n]*\bcom\.coolux\.source/'
                          r'(?:com\.coolux\.source)?\.service\.TimeService(?=[\s}])')
DENIED = re.compile(r'permission denial|permission denied|securityexception', re.I)


def valid_serial(serial):
    return bool(serial and re.fullmatch(r'[A-Za-z0-9][A-Za-z0-9_.:%\[\]-]*', serial))


def commands():
    result = {
        'machine': ['getprop', 'ro.rjpj.machine.id'], 'fingerprint': ['getprop', 'ro.build.fingerprint'],
        'build': ['getprop', 'ro.build.display.id'], 'api': ['getprop', 'ro.build.version.sdk'],
        'release': ['getprop', 'ro.build.version.release'], 'user': ['am', 'get-current-user'],
        'shell_identity': ['id'], 'selinux': ['getenforce'], 'boot_id': ['cat', '/proc/sys/kernel/random/boot_id'],
        'settings_path': ['pm', 'path', PACKAGE], 'settings_package': ['dumpsys', 'package', PACKAGE],
        'settings_resolver': ['cmd', 'package', 'resolve-activity', '--brief', '-a', 'android.settings.SETTINGS'],
        'launcher_resolver': ['cmd', 'package', 'resolve-activity', '--brief', '-a', 'android.intent.action.MAIN',
                              '-c', 'android.intent.category.LEANBACK_LAUNCHER', PACKAGE],
        'pm_help': ['pm', 'help'], 'cmd_package_help': ['cmd', 'package', 'help'],
        'privapp_allowlist': ['cmd', 'package', 'get-privapp-permissions', PACKAGE],
        'privapp_denylist': ['cmd', 'package', 'get-privapp-deny-permissions', PACKAGE],
        'stock_selinux_label': ['ls', '-lZ', BUILTIN],
        'source_services': ['dumpsys', 'activity', 'services', 'com.coolux.source'],
        'settings_time_owner': ['dumpsys', 'activity', 'services', 'com.my.settings']}
    for label, package in CRITICAL.items():
        result['critical_' + label] = ['dumpsys', 'package', package]
    return result


def capture_adb(runner, adb, serial, destination, name, args):
    try:
        code, stdout, stderr = runner([adb, '-s', serial] + args)
    except Exception as exc:
        code, stdout, stderr = 1, '', type(exc).__name__
    (destination / (name + '.txt')).write_text(stdout + '\n' + stderr, encoding='utf-8')
    return dict(code=code, stdout=stdout.strip(), stderr=stderr.strip())


def collect_stock_picture(serial, destination, adb='adb', runner=run):
    return collect_stock_snapshot(serial, destination, adb, runner)


def collect_stock_snapshot(serial, destination, adb='adb', runner=run, *, hdmi=False):
    """Collect one exact STOCK snapshot. Optional dumps are evidence for manual review."""
    gates, commands_used = {}, {}

    def capture(name, args):
        result = capture_adb(runner, adb, serial, destination, name, args)
        output = result['stdout']
        text = output + '\n' + result['stderr']
        denied = re.search(r'permission denial|permission denied|securityexception', text, re.I)
        missing = re.search(r"can't find service|no such service|service .* not found", text, re.I)
        if denied:
            status = 'DENIED'
        elif result['code'] != 0 or not output or missing:
            status = 'UNAVAILABLE'
        else:
            status = 'OBSERVED'
        commands_used[name] = dict(args=args, exit_code=result['code'], status=status,
                                   file=name + '.txt')
        return output if status == 'OBSERVED' else None

    def require(name, args, eligible, reason):
        passed = eligible(capture(name, args))
        gates[name] = 'PASS' if passed else 'STOP'
        if not passed:
            print('STOP ' + name + ': ' + reason)
        return passed

    def setting_int(raw):
        if raw and re.fullmatch(r'-?\d{1,10}', raw):
            value = int(raw)
            if -(2**31) <= value < 2**31:
                return value
        return None

    baseline = dict.fromkeys((EDID_KEY,) if hdmi else PICTURE_KEYS)
    service_record = 'UNVERIFIED'

    stock = (
        require('connection', ['get-state'], lambda v: v == 'device', 'selected serial is unavailable')
        and require('shell_identity', ['shell', 'id'], lambda v: bool(v and re.match(r'uid=2000\b', v)),
                    'ordinary adb shell required')
        and require('machine', ['shell', 'getprop', 'ro.rjpj.machine.id'], lambda v: v == '8054',
                    'expected machine 8054')
        and require('api', ['shell', 'getprop', 'ro.build.version.sdk'], lambda v: v == '33',
                    'expected Android API 33')
        and require('settings_path', ['shell', 'pm', 'path', PACKAGE], lambda v: v == 'package:' + BUILTIN,
                    'expected a single built-in TvSettingsPlus APK')
        and require('settings_sha256', ['shell', 'sha256sum', BUILTIN],
                    lambda v: bool(v and v.split() == [STOCK_SHA, BUILTIN]),
                    'expected the exact STOCK hash; inspect settings_sha256.txt')
    )
    if stock:
        capture('fingerprint', ['shell', 'getprop', 'ro.build.fingerprint'])
        if hdmi:
            capture('tv_input', ['shell', 'dumpsys', 'tv_input'])
            capture('hdmi_control', ['shell', 'dumpsys', 'hdmi_control'])
            raw = capture(EDID_KEY, ['shell', 'settings', 'get', 'global', EDID_KEY])
            if raw in ('0', '1', '2', '3'):
                baseline[EDID_KEY] = int(raw)
        else:
            before = capture('picture_mode_before', ['shell', 'settings', 'get', 'global', 'picture_mode'])
            capture('tv_input', ['shell', 'dumpsys', 'tv_input'])
            capture('display', ['shell', 'dumpsys', 'display'])
            for key in PICTURE_KEYS[1:]:
                raw = capture(key, ['shell', 'settings', 'get', 'global', key])
                baseline[key] = setting_int(raw)
            after = capture('picture_mode_after', ['shell', 'settings', 'get', 'global', 'picture_mode'])
            if before == after:
                baseline['picture_mode'] = setting_int(after)
        capture('tvservices_package', ['shell', 'dumpsys', 'package', CRITICAL['TvServices']])
        services = capture('tvservices_activity',
                           ['shell', 'dumpsys', 'activity', 'services', CRITICAL['TvServices']])
        if services and re.search(r'ServiceRecord\{[^}\n]*com\.mediatek\.tv\.service/'
                                  r'(?:com\.mediatek\.tv)?\.agent\.TVMenuSettingsService(?:[\s}])',
                                  services):
            service_record = 'OBSERVED'

    if not stock:
        status = 'STOP'
    elif any(value is None for value in baseline.values()):
        status = 'PARTIAL'
    else:
        status = 'CAPTURED'
    result = dict(status=status, stock_verified=stock, gates=gates, commands=commands_used,
                  settings_global=baseline, active_source='UNVERIFIED', main_video_input='UNVERIFIED',
                  signal_format='UNVERIFIED', mediatek_config='NO_CONFIRMED_SHELL_INTERFACE',
                  mediatek_bounds='NO_CONFIRMED_SHELL_INTERFACE',
                  tvmenu_service_record=service_record,
                  device_changes=False, install_state='NOT_READY_FOR_DEVICE_INSTALL')
    if hdmi:
        result.update(edid_port_scope='UNVERIFIED', edid_sink_readback='NO_CONFIRMED_SHELL_INTERFACE',
                      cec_earc_state='UNVERIFIED')
    save_json(destination / ('stock-hdmi.json' if hdmi else 'stock-picture.json'), result)
    return result


def package_state(text, user):
    flags = re.search(r'(?:pkgFlags|flags)=\[([^\]]*)\]', text)
    line = re.search(r'^\s*User ' + re.escape(user) + r':([^\n]*)', text, re.M)
    enabled = bool(line and re.search(r'\binstalled=true\b', line[1])
                   and re.search(r'\benabled=[01]\b', line[1])
                   and re.search(r'\bhidden=false\b', line[1]) and re.search(r'\bsuspended=false\b', line[1]))
    versions = sorted(set(re.findall(r'\bversionCode=(\d+)', text)))
    return {'system': bool(flags and re.search(r'\bSYSTEM\b', flags[1])),
            'updated_system': bool(flags and re.search(r'\bUPDATED_SYSTEM_APP\b', flags[1])),
            'enabled_installed_user': enabled, 'version_codes': versions,
            'shared_uid_evidence': bool(re.search(r'sharedUser=|sharedUserId=', text))}


def collect(kit, serial, destination, adb='adb', aapt='aapt', apksigner='apksigner', runner=run, revision=None):
    checks, captured = [], {}
    package, inactivity, apks = {}, {}, {}
    def check(name, passed, detail):
        checks.append(dict(name=name, status='PASS' if passed else 'FAIL', detail=detail))
        print(('PASS ' if passed else 'FAIL ') + name + ': ' + detail)
    def capture(name, args):
        captured[name] = capture_adb(runner, adb, serial, destination, name, args)
        return captured[name]['code'] == 0
    def value(key):
        record = captured[key]
        if record['code'] != 0 or DENIED.search(record['stdout'] + '\n' + record['stderr']):
            return ''
        return record['stdout']

    connected = (valid_serial(serial) and capture('connection', ['get-state'])
                 and value('connection') == 'device')
    check('ADB_connection', connected, 'selected serial only; no device selection by guess')
    if connected:
        capture('machine', ['shell', 'getprop', 'ro.rjpj.machine.id'])
        connected = value('machine') == '8054'
        check('machine_8054', connected, captured['machine']['stdout'] or 'unavailable; STOP')
    if connected:
        for name in IDENTITY:
            capture(name, ['shell'] + commands()[name])
        user = value('user')
        check('API33', value('api') == '33', value('api') or 'unavailable')
        check('build_recorded', bool(value('fingerprint') and value('build')), 'Compare fingerprint/build to owner backup manually')
        check('owner_user0', user == '0', user or 'unknown')
        check('SELinux_Enforcing', value('selinux') == 'Enforcing', value('selinux') or 'unknown; never change enforcement')
        check('ordinary_ADB_shell', bool(re.match(r'uid=2000\b', value('shell_identity'))), 'Ordinary shell UID required')
        if revision:
            check('Android13', value('release') == '13', value('release') or 'unavailable')
    if connected and all(item['status'] == 'PASS' for item in checks):
        capture('adb_version', ['version'])
        capture('adb_help', ['help'])
        for name, command in commands().items():
            if name not in ('machine', *IDENTITY):
                capture(name, ['shell'] + command)
        state = package_state(value('settings_package'), user if user.isdigit() else 'INVALID')
        package = state
        check('system_package', state['system'], json.dumps(state))
        check('settings_user_enabled', state['enabled_installed_user'], 'installed/enabled/hidden/suspended for selected owner')
        check('shared_UID_visible', state['shared_uid_evidence'], 'Inspect raw package dump plus pulled APK sharedUserId')
        for key in ['settings_resolver', 'launcher_resolver']:
            check(key, bool(re.search(r'com\.android\.tv\.settings/[^\s]+', value(key))), 'Resolve only; no Activity started')
        for label in CRITICAL:
            data = value('critical_' + label)
            check('critical_' + label, bool(data) and package_state(data, user)['enabled_installed_user'],
                  'Package presence/enabled only; not proof of service or hardware health')
        service_dump = value('source_services')
        observed = (bool(TIME_SERVICE.search(service_dump)) if revision else
                    'com.coolux.source.service.TimeService' in service_dump)
        check('TimeService_observed', observed,
              'Missing/denied services observation is STOP, not permission to replace watchdog')
        paths = re.findall(r'^package:(/[^\r\n]+\.apk)$', value('settings_path'), re.M)
        path_only = len(paths) == 1 and value('settings_path') == 'package:' + paths[0]
        check('one_base_no_splits', path_only, f'{len(paths)} paths; split installs unsupported')
        if revision:
            check('stock_PM_state', path_only and paths[0] == BUILTIN and not state['updated_system']
                  and state['version_codes'] == ['1'], 'Expected active built-in STOCK v1 for owner')
            package['paths'] = paths
            for namespace, key in INACTIVITY:
                evidence = namespace + '_' + key
                capture(evidence, ['shell', 'settings', 'get', namespace, key])
                raw = value(evidence)
                valid = raw == 'null' or (bool(re.fullmatch(r'0|-?[1-9][0-9]{0,9}', raw))
                                          and -(2**31) <= int(raw) < 2**31)
                inactivity[namespace + '/' + key] = {'raw': raw or None,
                                                       'status': ('ABSENT' if raw == 'null' else
                                                                  'PRESENT' if valid else 'UNAVAILABLE')}
                check(evidence, valid, 'Owner 0 baseline only')
        live = destination / 'LIVE-base.apk'
        safe = (path_only and not {'.', '..', ''}.intersection(paths[0][1:].split('/'))
                and bool(re.fullmatch(r'/(?:system|system_ext|product|data)/[A-Za-z0-9_./+=@~-]+\.apk', paths[0])))
        pulled = safe and capture('pull_live', ['pull', paths[0], str(live)]) and live.is_file()
        check('live_APK_pulled', pulled, 'Read via ADB sync protocol, no shell output redirection')
        builtin = destination / 'BUILTIN-base.apk'
        builtin_ok = capture('pull_builtin', ['pull', BUILTIN, str(builtin)]) and builtin.is_file()
        apks = {'live_sha256': sha256(live) if pulled else None,
                'builtin_sha256': sha256(builtin) if builtin_ok else None}
        check('builtin_matches_backup', apks['builtin_sha256'] == STOCK_SHA,
              'Built-in original required for update-removal route; no assumption from /data update')
        if pulled:
            offline_dir = destination / 'apk-verification'; offline_dir.mkdir()
            report = verify(kit, offline_dir, aapt, apksigner, live, runner, revision)
            check('stock_mod_live_identity', report['status'] == 'PASS', 'Actual cert/package/version/SDK/sharedUID/permissions/splits, exact stock bytes before first install')
        else: check('stock_mod_live_identity', False, 'Cannot prove live identity without APK')
        help_text = value('pm_help') + '\n' + value('cmd_package_help')
        candidate = bool(re.search(r'^\s*uninstall-system-updates\s+[^\n]*PACKAGE', help_text, re.M))
        check('package_scoped_update_removal_help', candidate, 'Help candidate only; actual support/data behavior NOT_VERIFIED')
        for key in ['privapp_allowlist', 'privapp_denylist', 'stock_selinux_label']:
            check(key, captured[key]['code'] == 0 and bool(value(key)), 'Manual review required; no permission or SELinux changes')
    result = dict(readiness='NOT_READY', install_state='NOT_READY_FOR_DEVICE_INSTALL', checks=checks,
                  revision=revision, package=package, inactivity=inactivity, apks=apks,
                  device_changes=False, serial=serial,
                  rollback_route_A='NOT_VERIFIED: package-scoped system update removal',
                  rollback_route_B='STANDARD_REPLACE_DOWNGRADE_BLOCKED: STOCK v1 over MOD v33; no approved exception',
                  remaining_proofs=['actual rollback support and data behavior', 'ADB reliably available after separately approved reboot',
                                    'fingerprint/sharedUID/permissions/SELinux human review', 'owner opt-in installation permission'])
    # Even all diagnostic PASS results cannot substitute for device recovery evidence or owner approval.
    save_json(destination / 'preflight.json', result)
    print('NOT_READY / NOT_READY_FOR_DEVICE_INSTALL: recovery, reboot transport and owner proof remain mandatory')
    return result


def verify_host(kit, destination, aapt, apksigner, revision, runner=run):
    verify_files(kit)
    archived = verify_revision_kit(kit, revision) if revision else None
    if read_report(kit, 'manifest.json').get('integration_audit') != 'PASS':
        raise ValueError('Pinned archive integration audit did not pass')
    report = verify(kit, destination, aapt, apksigner, runner=runner, revision=revision)
    if report['status'] != 'PASS':
        raise ValueError('Host APK verification failed')
    if archived and archived['records'] != json.loads(json.dumps(report['records'])):
        raise ValueError('Archived manifest or signer metadata differs from fresh APK verification')
    return report


class PreflightArguments(argparse.ArgumentParser):
    def error(self, message):
        self.exit(1, 'STOP: ' + message + '\n')


def main():
    parser = PreflightArguments(description=__doc__)
    modes = parser.add_mutually_exclusive_group()
    modes.add_argument('--hdmi-stock', action='store_true', help='Read-only exact STOCK EDID and HDMI dumps')
    modes.add_argument('--picture-stock', action='store_true', help='Read-only exact STOCK picture snapshot; no install assets needed')
    modes.add_argument('--offline', action='store_true', help='Verify a complete kit without ADB')
    parser.add_argument('--serial', help='Explicit approved device; never auto-select')
    parser.add_argument('--revision', help='Exact pinned kit revision')
    parser.add_argument('--kit', type=Path, default=Path(__file__).resolve().parent)
    parser.add_argument('--out', help='New host directory; Linux /tmp only')
    parser.add_argument('--adb', default='adb'); parser.add_argument('--aapt', default='aapt'); parser.add_argument('--apksigner', default='apksigner')
    args = parser.parse_args()
    if (not args.offline and not valid_serial(args.serial)) or (args.offline and args.serial is not None):
        print('FAIL explicit serial / NOT_READY'); return 1
    if args.picture_stock or args.hdmi_stock:
        if args.revision is not None:
            parser.error('Stock-only snapshots do not select a kit revision')
        try:
            prefix = 'tvsettings8054-stock-hdmi-' if args.hdmi_stock else 'tvsettings8054-stock-picture-'
            destination = output_dir(args.out, prefix)
            result = collect_stock_snapshot(args.serial, destination, args.adb, hdmi=args.hdmi_stock)
        except (OSError, ValueError) as exc:
            print('STOP host output: ' + str(exc))
            return 1
        print(result['status'] + ': private local report: ' + str(destination))
        return {'CAPTURED': 0, 'STOP': 1, 'PARTIAL': 2}[result['status']]
    try:
        kit = args.kit.resolve()
        if args.out and Path(args.out).resolve().is_relative_to(kit):
            raise ValueError('Output must be outside the verified kit')
        destination = output_dir(args.out, 'tvsettings8054-preflight-')
        host = destination/'host-assets'; host.mkdir()
        verify_host(kit, host, args.aapt, args.apksigner, args.revision, run)
        if args.offline:
            print('OFFLINE_KIT_VERIFIED: ' + str(destination))
            return 0
        result = collect(kit, args.serial, destination, args.adb, args.aapt, args.apksigner, run, args.revision)
    except (OSError, ValueError, KeyError, TypeError) as exc:
        print('STOP: ' + str(exc) + ' / NOT_READY_FOR_DEVICE_INSTALL')
        return 1
    print('Private local report: ' + str(destination))
    return 1 if any(c['status'] == 'FAIL' for c in result['checks']) else 2  # 2 = diagnostics pass, install readiness still NOT_READY.


if __name__ == '__main__': sys.exit(main())

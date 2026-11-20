"""Generate the v39 function inventory from the pinned 96-item dependency graph."""
import csv
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1] / 'research' / '8054'
graph = json.loads((root / 'dependency_graph_8054.json').read_text(encoding='utf-8'))
previous = {row['id']: row for row in
            json.loads((root / 'delivery_status_8054.json').read_text(encoding='utf-8'))['features']}
contracts = {}


def declare(ids, area, activity, status, widget, adapter, getter='', setter='', bounds='', readback='', tests=''):
    for key in ids.split():
        if key in contracts:
            raise ValueError('Duplicate v39 scope '+key)
        contracts[key] = (area, activity, status, widget, adapter, getter, setter, bounds, readback, tests)


declare('NAV01','Главное','ModMainSettingsActivity','WORKING_STATIC','Preference → Fragment',
        'stock TvSettingsActivity/SettingsFragmentProvider','stock flavor','','','stock preview','UnifiedMenuUiTest')
declare('NAV02','Wi-Fi и сеть','NetworkSettingsActivity','WORKING_STATIC','stock NetworkFragment',
        'original Android NetworkFragment','stock','','OEM platform limits','stock UI','UnifiedMenuUiTest')
declare('NAV06 SYS09','Приложения','AppsSettingsActivity','WORKING_STATIC','stock AppsFragment',
        'Android TV app management','stock','','Android package state','stock UI','UnifiedMenuUiTest')
declare('NAV03 NAV04 NAV05','Система','SystemSettingsActivity','WORKING_STATIC','Preference → Activity',
        'stock DateTimeActivity / LanguageActivity','stock','','platform','stock Activity','UnifiedMenuUiTest')
declare('NAV07 NAV14 FAC01','Система','SystemSettingsActivity','READ_ONLY','Preference',
        'OemSystemInfoHook','Android Build/OEM fields','','','fresh value','SocInfoLabelTest')
declare('NAV08','Система','SystemSettingsActivity','WORKING_STATIC','ListPreference',
        'OemFontScaleHook','Settings.System font_scale','OEM float profile','0.85 / 1.0 / 1.15 / 1.3',
        'Settings.System readback','OemFontScalePolicyTest')
declare('NAV09','Система','SystemSettingsActivity','READ_ONLY','Preference',
        'OemScreenTimeoutSafetyHook.installStatus','Settings.System/Secure','','','status','OemScreenTimeoutPolicyTest / UnifiedMenuUiTest')
for n, key in enumerate(('picture_mode','picture_color_temperature','picture_brightness','picture_contrast',
                         'picture_saturation','picture_sharpness','picture_hue','picture_red_gain',
                         'picture_green_gain','picture_blue_gain','picture_list_hdr',
                         'tv_picture_advance_video_mjc_effect'),1):
    declare(f'PIC{n:02}','Изображение','PictureSettingsActivity','READ_ONLY','Preference',
            'OemPicturePanel','Settings.Global '+key,'write closed','active profile required',
            'bracket mode/color/HDR snapshot','PicturePanelStateTest / OemPicturePanelAdapterTest')
declare('PIC14','Изображение','PictureSettingsActivity','OEM_SPECIALIZED_EDITOR','Preference → Activity',
        'stock DisplayModeActivity via OemStockUserSettings','OEM GetSupportScreenModes and config',
        'stock editor','stock arrays for current HDMI','stock reader','PictureFormat8054PolicyTest')
declare('PIC18','Система','SystemSettingsActivity','READ_ONLY','PreferenceCategory',
        'PanelCacheReadout','existing OEM singleton cache','','','no initialization','PanelCacheReadoutTest')
for n, key in enumerate(('sound_style','sound_bass','sound_treble','sound_sound_surround',
                         'OEM KEY_SOUND_AUTO_VOLUME_CONTROL','OEM key-tone property',
                         'sound_equalizer_detail_120/500/1500/5000/10000hz'),1):
    declare(f'SND{n:02}','Звук','SoundSettingsActivity','WORKING_STATIC',
            'ListPreference / SwitchPreference / stock-rendered preference',
            'OemSoundPanel with original supported sound adapters','provider '+key,
            'guarded stock key / provider','OEM value table; EQ requires exact capability',
            'persisted value / current sound profile','SoundPanelStateTest / OemSoundValuePolicyTest')
declare('SND09 SND10','Звук','SoundSettingsActivity','OEM_SPECIALIZED_EDITOR','native Fragment',
        'OemAdvancedSoundRoutes','Dolby/DTS capability','stock Fragment','OEM capability','stock UI','AdvancedSound8054PolicyTest')
declare('SND12','Звук','SoundSettingsActivity','WORKING_STATIC','SeekBarPreference',
        'OemSoundDelayPanel','Global + TVSettingConfig','provider via TvServices','OEM min/max, step 10 ms',
        'synchronized Global and MediaTek config','SoundDelayStateTest')
declare('HDMI01 HDMI02','Источники','SourcesSettingsActivity','WORKING_STATIC','Preference → LiveTV',
        'OemHdmiPortInfo','TvInputManager + OEM physical metadata','explicit ACTION_VIEW','HW6 / HW7',
        're-resolve exact target at click, physical switching pending','Hdmi8054PolicyTest')
declare('HDMI03','Источники','SourcesSettingsActivity','READ_ONLY','Preference','OemHdmiEdidHook',
        'Settings.Global tv_input_hdmi_edid_version','','0–3 stored codes','stored value only','EdidPortPolicyTest')
declare('HDMI04','Источники','SourcesSettingsActivity','WORKING_STATIC','read-only CEC + SwitchPreference Auto Off',
        'OemCecAutoOff8054','HDMI manager CEC + standby','setTvSendStandbyOnSleep','0/1',
        'paired CEC/standby; main CEC setter closed','CecAutoOff8054Test / UnifiedMenuUiTest')
declare('HDMI06','Источники','SourcesSettingsActivity','WORKING_STATIC','SwitchPreference','OemHdmiAutoPlayHook',
        'persist.rjpj.hdmiplug.autoplay','stock property adapter','0/1','property reread','MenuIntegrationAdapterTest')
declare('HDMI07 HDMI08','Источники','SourcesSettingsActivity','WORKING_STATIC','one ListPreference',
        'NativeBootDestination','persist.trd.source_id','one guarded property write',
        'HOME / verified HW6-HW7 input ID / launchable package','compare baseline, revalidate options and readback','UnifiedMenuUiTest')
declare('PRJ02 PRJ03','Проекция','ProjectionSettingsActivity','WORKING_STATIC','SwitchPreference',
        'OemFocusPreferences','persist.rjpj.autofocus.poweron or .move','OEM SystemPropertiesUtil.set','0/1',
        'property reread, motor effect pending','ProjectionToggleStateTest / exact callback trace')
declare('PRJ05','Проекция','ProjectionSettingsActivity','WORKING_STATIC','SwitchPreference',
        'OemMoveKeystonePreference','persist.rjpj.autokeystone.move','stock callback','0/1, dependents off before disabling',
        'property reread','Projection8054PolicyTest / ProjectionToggleStateTest')
declare('PRJ06 PRJ07 PRJ10','Проекция','ProjectionSettingsActivity','OEM_SPECIALIZED_EDITOR','explicit editor',
        'OemProjectionPanel / KeystoneCorrection','stock getter','stock editor',
        'OEM bounds','stock editor','Projection8054PolicyTest')
declare('PRJ11','Проекция','ProjectionSettingsActivity','WORKING_STATIC','ListPreference',
        'OemLightSourcePreference','KuSourceRJ current mode','checked BrightReceiver','four OEM modes, customer 0',
        'OEM persisted mode, asynchronous bounded readback','Brightness8054AdapterTest')
declare('SYS01','Система','SystemSettingsActivity','WORKING_STATIC','SwitchPreference',
        'OemAcPowerBootHook','Factory user endpoint get','OEM putElectricityBoot','0/1','readback','OemAcPowerBootPolicyTest')
declare('SYS02 SYS06','Bluetooth','BluetoothSettingsActivity','READ_ONLY','Preference',
        'OemBluetoothRemoteHook / AutoPair','HID/AOSP Bluetooth and autopair_status','','','read-only status','OemBluetoothRemotePolicyTest')
declare('SYS04','Система','SystemSettingsActivity','READ_ONLY','Preference',
        'OemThermalTelemetryHook','Android thermal and HardwareProperties APIs','','','fresh permitted readings','ThermalTelemetryPolicyTest')
declare('SYS07','Система','SystemSettingsActivity','OEM_SPECIALIZED_EDITOR','stock timer',
        'OemStockUserSettings','stock TimerSetActivity or TimerFragment','OEM schedule','stock timer limits',
        'stock editor; effect requires hardware test','exact user-route validator')
declare('HDMI10','Источники','SourcesSettingsActivity','UNSUPPORTED_HARDWARE','hidden',
        'No analog/tuner controls in MOD')

new_activities = {
    'NAV01': 'com.android.tv.settings.MainSettings',
    'NAV02': 'com.android.tv.settings.connectivity.NetworkActivity',
    'NAV06': 'com.android.tv.settings.device.apps.AppsActivity',
    'SYS09': 'com.android.tv.settings.device.apps.AppsActivity',
    'NAV07': 'DeviceAboutSettingsActivity',
    'NAV14': 'DeviceAboutSettingsActivity',
    'FAC01': 'DeviceAboutSettingsActivity',
    'NAV09': 'ProjectorPowerSettingsActivity',
    'PIC18': 'DeviceAboutSettingsActivity',
    'PIC02': 'ColorTemperatureSettingsActivity',
    'PIC08': 'ColorTemperatureSettingsActivity',
    'PIC09': 'ColorTemperatureSettingsActivity',
    'PIC10': 'ColorTemperatureSettingsActivity',
    'SND01': 'EqualizerSettingsActivity',
    'SND07': 'EqualizerSettingsActivity',
    'SND12': 'SoundDelaySettingsActivity',
    'HDMI04': 'HdmiCecSettingsActivity',
    'SYS01': 'ProjectorPowerSettingsActivity',
    'SYS02': 'RemoteSettingsActivity',
    'SYS06': 'RemoteSettingsActivity',
    'SYS04': 'HardwareStatusSettingsActivity',
    'SYS07': 'ProjectorPowerSettingsActivity',
}
held_tone = ('SND02', 'SND03')
tone_check = 'Distinct config ID confirmed; identify DSP separation from the five-band EQ for each output and preset'
root_adapter = 'StockMainSettingsMenu in original MainFragment before super.onCreateView'
root_route = 'stock flavor, original MainSettings host and explicit section Activities'
root_test = 'UnifiedMenuUiTest production adapter + exact DEX round-trip'

safety = set('NAV10 HDMI05 SND08 SND11 PRJ04 PRJ08 PRJ09 SYS03 SYS05 FAC02 FAC03 FAC05 FAC06 FAC07 FAC08 FAC09 FAC10'.split())
excluded = set('NAV13 PIC13 PIC15 PIC17 SND13 DBL05 DBL06 DBL08'.split())
features=graph['features']
if len(features)!=96 or len({row['id'] for row in features})!=96 or set(contracts)-set(previous):
    raise ValueError('Expected the complete 96-function inventory and known contract IDs')

columns=('id','function','original_menu','original_activity_or_controller_hint','original_entry_point_hint',
         'original_backend_hint','new_section','new_activity','status','ui_control','adapter','getter','setter',
         'range_or_values','readback','availability','side_effects','dependencies','rollback',
         'software_tests','hardware_status','outstanding')
path=root / 'FINAL_FUNCTION_PARITY_8054.csv'
with path.open('w',encoding='utf-8',newline='') as out:
    writer=csv.writer(out,lineterminator='\n')
    writer.writerow(columns)
    for row in features:
        key=row['id']
        area, activity, status, widget, adapter, getter, setter, bounds, verify, tests=contracts.get(key,
            ('','', 'BLOCKED_SAFETY' if key in safety else 'IMPLEMENTATION_PENDING',
             'excluded' if key in excluded else 'not exposed','','','','','',''))
        activity = new_activities.get(key, activity)
        if key == 'NAV01':
            widget, adapter, getter, tests = 'Preference → Activity / Preview', root_adapter, root_route, root_test
        if key in held_tone:
            activity, status, widget, setter, verify = '', 'IMPLEMENTATION_PENDING', 'hidden', 'closed in v39', ''
        if not activity:
            outstanding='Excluded from v39 menu' if key in excluded else previous[key]['next_check']
        else:
            outstanding=previous[key]['next_check']
        if key in held_tone:
            outstanding = tone_check
        writer.writerow((key,row['function'],row['section'],row['owner_hint'],row['entry_point_hint'],
                         row['backend_hint'],area,activity,status,widget,adapter,getter,setter,bounds,verify,
                         row['precondition'] or ('Check platform or OEM capability' if activity else ''),
                         row['side_effects_hint'],row['target'] or row['owner_hint'],row['rollback'],
                         tests,'RUNTIME_TEST_REQUIRED' if activity else 'NOT_TESTED',outstanding))
print('PASS v39 parity: 96 unique rows')

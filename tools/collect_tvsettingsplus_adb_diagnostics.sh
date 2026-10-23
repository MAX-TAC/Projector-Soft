#!/data/data/com.termux/files/usr/bin/bash
# Read-only MT9660 RUIOS 8052/8054 diagnostic for TvSettingsPlus/KuSettings.
# Run on phone in Termux (not on projector). Uses already paired adb, no root.
set -Eeuo pipefail
umask 077
command -v adb >/dev/null || { echo "Missing adb: pkg install android-tools"; exit 2; }
command -v zip >/dev/null || { echo "Missing zip: pkg install zip"; exit 2; }
command -v unzip >/dev/null || { echo "Missing unzip: pkg install unzip"; exit 2; }
ADB_SERIAL="${ADB_SERIAL:-}"
DATE="$(date +%Y%m%d_%H%M%S)"
OUTPUT_DIR="$HOME/storage/downloads"
if [ ! -d "$OUTPUT_DIR" ] || [ ! -w "$OUTPUT_DIR" ]; then
  OUTPUT_DIR="$HOME"
  echo "Downloads unavailable: writing ZIP to Termux HOME" >&2
fi
WORK="$(mktemp -d "$HOME/tvsp_diag_XXXXXXXX")"
trap 'rm -rf "$WORK"' EXIT
FOLDER="TvSettingsPlus_Diagnostic_$DATE"
REPORT="$WORK/$FOLDER"
mkdir -p "$REPORT"/{packages,intents,system,activity,ui,status}
LOG="$REPORT/status/commands.log"
printf 'Run %s\n' "$DATE" > "$LOG"

DEVICES="$(adb devices 2>&1 || true)"
printf '%s\n' "$DEVICES" > "$REPORT/status/adb_devices.txt"
COUNT="$(printf '%s\n' "$DEVICES" | awk 'NR>1 && $2=="device" {n++} END{print n+0}')"
if [ "$COUNT" -lt 1 ]; then
  echo "ERROR: No connected authorized adb device; pair/connect ADB first." >&2
  printf '%s\n' "$DEVICES"
  exit 3
fi
if [ "$COUNT" -gt 1 ] && [ -z "$ADB_SERIAL" ]; then
  echo "ERROR: More than one device; set ADB_SERIAL=ip:port" >&2
  exit 3
fi
ADB=(adb)
if [ -n "$ADB_SERIAL" ]; then ADB+=(-s "$ADB_SERIAL"); fi

capture() {
  local target="$1" command="$2" rc=0
  printf '[RUN] %s\n' "$target"
  printf '%s\n' "$target :: $command" >> "$LOG"
  "${ADB[@]}" shell "$command" > "$REPORT/$target" 2>&1 || rc=$?
  printf '%s :: exit=%d\n' "$target" "$rc" >> "$LOG"
}
capture "status/properties.txt" 'for p in ro.product.model ro.product.device ro.build.display.id ro.build.version.release ro.build.version.sdk ro.build.tags ro.rjpj.machine.id ro.boot.verifiedbootstate ro.boot.flash.locked sys.boot_completed; do echo "$p=$(getprop "$p")"; done'
capture "status/package_help.txt" 'cmd package help 2>&1 | head -n 200'
capture "status/all_packages.txt" 'pm list packages -f'
capture "status/disabled_packages.txt" 'pm list packages --user 0 -d'
capture "status/enabled_packages.txt" 'pm list packages --user 0 -e'
capture "status/preferred_activities.txt" 'dumpsys package preferred-activities'
capture "status/package_home.txt" 'cmd package get-home-activities'

PACKAGES=(
  com.my.settings com.android.tv.settings com.android.provision
  com.coolux.source com.mediatek.miracast com.hysd.screen
  com.rjpj.tcorrection com.trd.service com.trd.framework
  com.rjpj.yunota com.android.systemui
)
for pkg in "${PACKAGES[@]}"; do
  capture "packages/$pkg.package.txt" "dumpsys package $pkg"
  capture "packages/$pkg.path.txt" "pm path $pkg"
done

ACTIONS=(
  android.settings.SETTINGS android.settings.WIFI_SETTINGS
  android.settings.BLUETOOTH_SETTINGS android.settings.AUTO_BLUETOOTH
  android.settings.ALLAPPS android.settings.DATETIME_SETTINGS
  android.settings.LAUGUAGE_SETTINGS android.settings.LAUGUAGE_LOCAL_SETTINGS
  android.settings.PROJECTABOUT_SETTINGS android.settings.SCREENSHOT_SETTINGS
)
for action in "${ACTIONS[@]}"; do
  capture "intents/$action.resolve.txt" "cmd package resolve-activity --user 0 --brief -a $action"
  capture "intents/$action.query.txt" "cmd package query-activities --user 0 -a $action"
  capture "intents/$action.pm.txt" "pm resolve-activity --brief -a $action"
done

# Explicit component resolution does NOT launch anything.
capture "intents/explicit_tvsettings.txt" 'cmd package resolve-activity --user 0 --brief -n com.android.tv.settings/com.android.tv.settings.MainSettings'
capture "intents/explicit_kusettings.txt" 'cmd package resolve-activity --user 0 --brief -n com.my.settings/com.my.settings.SettingActivity'
capture "intents/explicit_setupwizard.txt" 'cmd package resolve-activity --user 0 --brief -n com.android.provision/com.my.settings.wifi.ProvisionWifiActivity'

capture "activity/running_services.txt" 'dumpsys activity services'
capture "activity/kusource_services.txt" 'dumpsys activity services com.coolux.source'
capture "activity/kusettings_services.txt" 'dumpsys activity services com.my.settings'
capture "activity/current_activities.txt" 'dumpsys activity activities'
capture "activity/window_focus.txt" 'dumpsys window | grep -E "mCurrentFocus|mFocusedApp" | head -n 40'
capture "status/brightness_mode_property.txt" 'echo "persist.bright.model=$(getprop persist.bright.model)"; echo "ro.rjpj.machine.id=$(getprop ro.rjpj.machine.id)"'
capture "activity/brightness_receiver_resolution.txt" 'cmd package query-receivers --user 0 -a com.action.brightReceiver 2>&1'
capture "activity/brightness_receiver_manifest.txt" 'dumpsys package com.coolux.source | grep -E -C 6 "BrightReceiver|com.action.brightReceiver|brightModel" | head -n 180'
capture "activity/boot_source_service.txt" 'dumpsys package com.coolux.source | grep -E -C 3 "BOOT_COMPLETED|TimeService|BootupReceiver" | head -n 150'

capture "system/xtzm_setting.txt" 'if [ -r /system/etc/xtzm_setting.txt ]; then cat /system/etc/xtzm_setting.txt; else echo "not readable"; fi'
capture "system/whaletv_config.json" 'if [ -r /system/menu_config_xml/whaletv_config.json ]; then cat /system/menu_config_xml/whaletv_config.json; else echo "not readable"; fi'
capture "system/oem_paths.txt" 'ls -ld /system/app/KuSettings /system/app/KuSetupWizard /system_ext/priv-app/TvSettingsPlus /system/etc/xtzm_setting.txt /system/menu_config_xml/whaletv_config.json 2>&1'

cat <<'MSG'

OPTIONAL STEP (recommended):
On the PROJECTOR use the remote control to open Settings normally.
Leave the screen open, return to Termux on the phone and press Enter.
If you cannot do this, just press Enter to capture the current foreground app.
MSG
if [ -t 0 ]; then
  read -r -p "Press Enter to capture foreground activity: " _ || true
  capture "ui/foreground_window.txt" 'dumpsys window | grep -E "mCurrentFocus|mFocusedApp" | head -n 40'
  capture "ui/foreground_activity.txt" 'dumpsys activity activities'
  capture "ui/activity_top.txt" 'dumpsys activity top'
else
  echo "Noninteractive shell: skipped optional foreground capture." > "$REPORT/ui/README.txt"
fi

cat > "$REPORT/README_RU.txt" <<'MSG'
MT9660 TvSettingsPlus / KuSettings dependency snapshot.
Only read-only adb shell commands used.
No APK install, package disabling/enabling, reboot, root or app launch.
Folders:
intents = Android implicit/explicit settings activity resolution;
packages = package enabled state, exported receivers, services, permissions;
activity = running KuSourceRJ TimeService, KuSettings watchdog, current focus;
ui = active Settings screen after user optionally opened via remote;
system = currently installed OEM settingApps and launcher config;
status = hardware version, command diagnostics.
Some commands may be unsupported by the OEM shell; examine status/commands.log.
Check ZIP for device-specific identifiers or private data before forwarding.
MSG
OUT="$OUTPUT_DIR/$FOLDER.zip"
( cd "$WORK" && zip -q -r "$OUT" "$FOLDER" )
unzip -t "$OUT" | tail -n 2
printf '\nDONE: %s\n' "$OUT"

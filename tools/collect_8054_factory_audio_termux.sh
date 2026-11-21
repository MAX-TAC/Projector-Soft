#!/data/data/com.termux/files/usr/bin/bash
# Exact-8054 MediaTek Factory Audio UI. Read-only and owner-navigated.
set -euo pipefail
umask 077
command -v adb >/dev/null || { echo 'Install: pkg install android-tools' >&2; exit 2; }
command -v zip >/dev/null || { echo 'Install: pkg install zip' >&2; exit 2; }
ADB=(adb)
if [ -n "${ADB_SERIAL:-}" ]; then ADB+=(-s "$ADB_SERIAL"); fi
if ! "${ADB[@]}" get-state 2>/dev/null | grep -qx device; then
  echo 'Projector not connected/authorized over ADB' >&2
  "${ADB[@]}" devices >&2 || true
  exit 3
fi
OUT="$HOME/storage/downloads"
[ -d "$OUT" ] && [ -w "$OUT" ] || OUT="$HOME"
STAMP="$(date +%Y%m%d_%H%M%S)"
BASE="Projector_8054_FACTORY_AUDIO_${STAMP}"
TMP="$(mktemp -d "$HOME/factory_audio_XXXXXXXX")"
trap 'rm -rf "$TMP"' EXIT
D="$TMP/$BASE"
mkdir -p "$D"
cat > "$D/README_RU.txt" <<'TXT'
Owner navigates MediaTek Factory > Audio manually. This report includes
focused Activity, audio-only Settings Provider values, and screenshot per page.
No settings writes, factory setters, Save/Reset, root, reboot or app launches.
Screenshots may contain personal/device data: review before forwarding.
The presence of Dolby/PEQ controls in Factory does not prove that enabling
or saving them is safe or supported for consumer use.
TXT
"${ADB[@]}" shell 'echo machine_id=$(getprop ro.rjpj.machine.id); echo build=$(getprop ro.build.display.id); echo android=$(getprop ro.build.version.release); echo all_settings=$(getprop persist.sys.settings.all)' > "$D/00_board.txt" 2>&1 || true
"${ADB[@]}" shell 'dumpsys package mediatek.factorymenu.ui | grep -Ei "FactoryDolbyActivity|FactoryDTSActivity|PEQ|GEQ|Audio|Sound|designmenu" | head -n 100' > "$D/00_factory_manifest_audio.txt" 2>&1 || true
echo 'On the projector open MediaTek Factory > Audio. Do NOT change values or press Save/Reset.'
echo 'For each audio screen type a short label + Enter on your PHONE; q to finish.'
for n in $(seq 1 24); do
  printf '\nScreen %02d: navigate to a NEW factory audio page on projector.\n' "$n"
  read -r -p 'Page name (e.g. Audio root, Dolby, PEQ; q to finish): ' LABEL || LABEL=q
  case "$LABEL" in q|Q|quit|QUIT) break;; esac
  [ -n "$LABEL" ] || LABEL="screen_$n"
  NUM="$(printf '%02d' "$n")"
  printf '%s\n' "$LABEL" > "$D/${NUM}_name.txt"
  "${ADB[@]}" shell 'dumpsys window | grep -E "mCurrentFocus|mFocusedApp"; dumpsys activity activities | grep -E "topResumedActivity|mResumedActivity|ResumedActivity" | head -n 8' > "$D/${NUM}_focus.txt" 2>&1 || true
  "${ADB[@]}" shell 'settings list global | grep -Ei "^(sound_|audio_|external_surround_sound_enabled=|nrdp_external_surround_sound_enabled=)" | sort; echo --- SYSTEM ---; settings list system | grep -Ei "^(sound_effects_enabled=|volume_)" | sort' > "$D/${NUM}_audio_keys.txt" 2>&1 || true
  PNG="$D/${NUM}_screenshot.png"
  if "${ADB[@]}" exec-out screencap -p > "$PNG" 2>/dev/null && [ "$(od -An -N8 -tx1 "$PNG" | tr -d ' \n')" = '89504e470d0a1a0a' ]; then
    echo 'Screenshot captured.'
  else
    rm -f "$PNG"
    echo 'Screenshot unavailable; Activity and keys captured.'
  fi
  printf 'Captured: %s\n' "$LABEL"
done
ZIP="$OUT/$BASE.zip"
(cd "$TMP" && zip -qr "$ZIP" "$BASE")
echo "DONE: $ZIP"
echo 'Inspect screenshots for private information before sharing.'

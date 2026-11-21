#!/data/data/com.termux/files/usr/bin/bash
# Exact-8054 full audio block read-only audit. Run ON PHONE in Termux with ADB.
# Opens nothing and never writes to the projector. Navigating menus is manual.
set -euo pipefail
umask 077

for app in adb zip; do
    if ! command -v "$app" >/dev/null 2>&1; then
        echo "ERROR: $app is missing. In Termux run: pkg install android-tools zip" >&2
        exit 2
    fi
done

ADB=(adb)
if [ -n "${ADB_SERIAL:-}" ]; then
    ADB+=(-s "$ADB_SERIAL")
fi
if ! "${ADB[@]}" get-state 2>/dev/null | grep -qx device; then
    echo "ERROR: The projector is not connected/authorized over ADB." >&2
    "${ADB[@]}" devices >&2 || true
    exit 3
fi

OUTDIR="$HOME/storage/downloads"
if [ ! -d "$OUTDIR" ] || [ ! -w "$OUTDIR" ]; then
    OUTDIR="$HOME"
    echo "Termux Downloads unavailable; report will be stored in $HOME."
fi
STAMP="$(date +%Y%m%d_%H%M%S)"
REPORT="Projector_8054_AUDIO_${STAMP}"
WORK="$(mktemp -d "$HOME/audio_audit_XXXXXXXX")"
trap 'rm -rf "$WORK"' EXIT
mkdir -p "$WORK/$REPORT"
LOG="$WORK/$REPORT/00_session.txt"

cat > "$LOG" <<'TXT'
Exact-8054 full AUDIO block diagnosis, manually captured by the owner.
Only read-only ADB commands: settings list/get, getprop, dumpsys and pm/cmd query.
No settings put, app install/start, reboot, root, service stop, or package disable.
The operator is asked to NAVIGATE MENUS ONLY, WITHOUT EDITING any values.
Screens may be skipped by entering s. Values marked null were absent, not zero.
Not all physical audio routes can be inferred from the Settings provider.
Review the ZIP before uploading: audio outputs may reveal device names.
TXT

capture() {
    local name="$1" command="$2"
    local target="$WORK/$REPORT/$name"
    echo "Capturing: $name"
    if ! "${ADB[@]}" shell "$command" > "$target" 2>&1; then
        printf '\n[ADB command returned an error; partial output may follow]\n' >> "$target"
    fi
    # Redact Bluetooth/Wi-Fi MAC addresses from every captured text file.
    sed -E 's/([[:xdigit:]]{2}:){5}[[:xdigit:]]{2}/[MAC REDACTED]/g' "$target" > "${target}.tmp"
    mv "${target}.tmp" "$target"
}

capture "01_board.txt" 'for p in ro.rjpj.machine.id ro.build.display.id ro.build.version.release ro.build.version.sdk ro.rjpj.hdmi.port.count persist.sys.settings.all; do printf "%s=" "$p"; getprop "$p"; done; echo "TVSettings:"; pm path com.android.tv.settings'
capture "02_audio_app_activities.txt" 'echo "--- resolvers ---"; cmd package resolve-activity --user 0 --brief -a android.settings.SOUND_SETTINGS 2>&1; echo "--- OEM sound declarations ---"; dumpsys package com.android.tv.settings | grep -Ei "AudioOutputActivity|SoundActivity|SoundFragment|Equalizer|DTSFragment|AdvancedFragment|sound\.advanced|sound\.dts|uicustom\.MainActivity" | head -n 120'
capture "03_audio_routes.txt" 'echo "--- audio service ---"; dumpsys audio 2>&1 | grep -Ei "ConnectedDevices|AudioDevice|AUDIO_DEVICE_OUT|SPEAKER|A2DP|SPDIF|HDMI(_ARC|_EARC)?|mMode|volume.*stream|ActivePlayback" | head -n 130; echo "--- audio policy ---"; dumpsys media.audio_policy 2>&1 | grep -Ei "AUDIO_DEVICE_OUT|HDMI(_ARC|_EARC)?|SPEAKER|A2DP|SPDIF|Available output|Outputs dump|Audio patch" | head -n 110'
capture "04_hdmi_sound_context.txt" 'echo "--- HDMI/CEC ---"; dumpsys hdmi_control 2>&1 | grep -Ei "system audio|arc|earc|cec|mIsControlEnabled|mSystemAudio|audio system" | head -n 95'

SOUND_SNAPSHOT='echo "-- CURRENT ACTIVITY --"; dumpsys window | grep -E "mCurrentFocus|mFocusedApp" | head -n 5; echo "-- ACTIVITY STACK --"; dumpsys activity activities | grep -E "topResumedActivity|mResumedActivity|ResumedActivity|mCurrentFocus" | head -n 18; echo "-- OEM AUDIO KEYS IN SETTINGS.GLOBAL --"; settings list global | grep -Ei "^(sound_|audio_|external_surround_sound_enabled=|nrdp_external_surround_sound_enabled=|hdmi_control_enabled=|hdmi_cec_enabled=)" | sort; echo "-- SOUND SYSTEM KEYS --"; settings list system | grep -Ei "^(sound_effects_enabled=|volume_|mode_ringer=)" | sort; echo "-- SPECIFIC KEYS WITH NULL IF ABSENT --"; for k in sound_style sound_bass sound_treble sound_sound_surround sound_equalizer_detail_120hz sound_equalizer_detail_500hz sound_equalizer_detail_1500hz sound_equalizer_detail_5000hz sound_equalizer_detail_10000hz sound_digital_output_to_device sound_spdif_type sound_earc sound_speaker_status sound_spdif_delay sound_speaker_delay sound_advanced_dolby_ap sound_dap sound_dts sound_dts_vx sound_dts_truvolumehd external_surround_sound_enabled nrdp_external_surround_sound_enabled; do printf "%s=" "$k"; settings get global "$k"; done'

stage() {
    local file="$1" title="$2" answer=""
    printf '\n=== %s ===\n' "$title"
    printf '%s\n' "Open this screen on the PROJECTOR with the remote; DO NOT change any values."
    read -r -p "When ready press Enter. Type s + Enter to skip: " answer || answer="s"
    if [ "$answer" = "s" ] || [ "$answer" = "S" ]; then
        echo "SKIPPED $title" >> "$LOG"
        return
    fi
    printf 'CAPTURED %s\n' "$title" >> "$LOG"
    capture "$file" "$SOUND_SNAPSHOT"
}

echo
echo "This audit reads the whole sound block, not individual EQ bands."
echo "Keep volume/CEC/audio output unchanged. Navigation and Back are OK."
stage "10_android_sound_main.txt" "1/6 Android TV Settings → Screen & Sound → Sound (main sound page)"
stage "11_android_sound_profiles_equalizer.txt" "2/6 Sound → Sound mode / Equalizer (if available)"
stage "12_android_audio_output.txt" "3/6 Sound → Audio output / Digital audio format (if available)"
stage "13_android_sound_effects.txt" "4/6 Sound → Advanced / Dolby / DTS / Surround effects (if available)"
stage "14_android_audio_delay.txt" "5/6 Sound → Delay / SPDIF / Speaker delay (if available)"
stage "15_chinese_sound_main.txt" "6/6 Chinese KuSettings → Sound (main page, for comparison only)"
capture "16_final_audio_keys.txt" "$SOUND_SNAPSHOT"

cat > "$WORK/$REPORT/README_RU.txt" <<'TXT'
This ZIP contains a read-only diagnostic of the FULL 8054 audio block.
10-15 capture focused Android TV / OEM sound screens chosen by the owner.
Every capture lists all sound_ and audio_ Global keys plus exact known OEM
sound mode, bass, treble, 5 EQ bands, surround, SPDIF, eARC, delay and effects.
02 contains component declarations; 03 audio device routes; 04 HDMI ARC/CEC.
If a page did not exist, its snapshot was skipped, not marked broken.
Values are observations, not proof of DSP effect; no audio value was written.
Before sharing, inspect for any personal/device identifying fields.
TXT

ARCHIVE="$OUTDIR/$REPORT.zip"
( cd "$WORK" && zip -q -r "$ARCHIVE" "$REPORT" )
if command -v unzip >/dev/null 2>&1; then
    unzip -tq "$ARCHIVE" | tail -n 1
fi
printf '\nDONE: %s\n' "$ARCHIVE"
printf 'Send this ZIP and tell us whether you use built-in speakers, BT or HDMI ARC/eARC/SPDIF.\n'

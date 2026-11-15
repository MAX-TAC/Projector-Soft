# Read-only HDMI ARC/eARC state capture across three playback phases.
# Run on a Windows PC with adb and projector connected/authorized.
# No reboot, settings writes, root, remount, service stop or flash.
param(
  [string]$Serial = ""
)
$ErrorActionPreference="Stop"
$adb=(Get-Command adb -ErrorAction Stop).Source
$common=@()
if($Serial.Trim().Length -gt 0){$common=@("-s",$Serial)}
$state=(& $adb @common get-state 2>&1 | Out-String).Trim()
if($LASTEXITCODE -ne 0 -or $state -ne "device"){throw "ADB device not authorized or unavailable: $state"}
$stamp=Get-Date -Format "yyyyMMdd_HHmmss"
$folder=Join-Path (Get-Location).Path "HDMI_ARC_eARC_$stamp"
New-Item -ItemType Directory -Path $folder | Out-Null
$phases=@(
  @{Key="01_playing";Prompt="Start video/music. Confirm sound is audible. Press Enter to capture."},
  @{Key="02_paused";Prompt="Pause playback and wait for the soundbar click. Press Enter to capture."},
  @{Key="03_resumed";Prompt="Resume playback and verify whether sound is missing. Press Enter to capture."}
)
function Capture([string]$label,[string[]]$remote){
    $file=Join-Path $phaseDir "$label.txt"
    try{
      $result=& $adb @common shell @remote 2>&1 | Out-String
      $result | Set-Content -Encoding UTF8 -Path $file
    }catch{
      "Command failed: $($_.Exception.Message)" | Set-Content -Encoding UTF8 -Path $file
    }
}
$props=@(
 "ro.rjpj.machine.id","ro.build.display.id","ro.build.version.release",
 "ro.rjpj.hdmi.ports","ro.rjpj.hdmi.port.count",
 "ro.boot.verifiedbootstate","ro.boot.flash.locked"
)
$settings=@(
 "sound_earc","sound_spdif_type","sound_speaker_status",
 "HDMI_CONTROL_ENABLED","Settings.Global.HDMI_CONTROL_ENABLED",
 "HDMI_ARC_CONTROL_ENABLED","Settings.Global.HDMI_ARC_CONTROL_ENABLED"
)
foreach($stage in $phases){
  Read-Host $stage.Prompt | Out-Null
  $phaseDir=Join-Path $folder $stage.Key
  New-Item -ItemType Directory -Path $phaseDir | Out-Null
  Capture "hdmi_control" @("dumpsys","hdmi_control")
  Capture "audio" @("dumpsys","audio")
  Capture "audio_policy" @("dumpsys","media.audio_policy")
  Capture "audio_flinger" @("dumpsys","media.audio_flinger")
  $statusFile=Join-Path $phaseDir "settings_global.txt"
  foreach($key in $settings){
    $val=(& $adb @common shell settings get global $key 2>&1 | Out-String).Trim()
    "$key = $val" | Add-Content -Path $statusFile -Encoding UTF8
  }
  $propFile=Join-Path $phaseDir "hardware_properties.txt"
  foreach($prop in $props){
    $val=(& $adb @common shell getprop $prop 2>&1 | Out-String).Trim()
    "$prop = $val" | Add-Content -Path $propFile -Encoding UTF8
  }
  $lineDump=(& $adb @common shell logcat -d -t 2500 2>&1 | Out-String)
  $lineDump -split '\r?\n' |
    Select-String -Pattern '(?i)(hdmi|earc|cec|audio|spkoutput|mtktv|audiopolicy|audioflinger|alsa)' |
    ForEach-Object { $_.Line } |
    Set-Content -Encoding UTF8 -Path (Join-Path $phaseDir "filtered_logcat.txt")
  Write-Host "Captured $($stage.Key)"
}
@'
Read-only diagnosis, no Android state changes.
Before sharing inspect logcat for sensitive data, source IP/HDMI peer name,
serial/device identifiers or private app data.
On pause/resume record whether the soundbar shows ARC, eARC, PCM, Dolby
and whether the projector's internal speakers start playing.
'@ | Set-Content -Encoding UTF8 (Join-Path $folder "README_PRIVACY.txt")
$zip="$folder.zip"
Compress-Archive -Path $folder -DestinationPath $zip -CompressionLevel Optimal
Write-Host "READY: $zip"

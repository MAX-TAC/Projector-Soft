[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[A-Za-z0-9\[][A-Za-z0-9:._\-\[\]]{0,127}$')]
    [string]$Serial,

    [string]$OutputDirectory = (Join-Path $env:TEMP ('8054-v40-' + (Get-Date -Format 'yyyyMMdd-HHmmss')))
)

$adb = (Get-Command adb -CommandType Application -ErrorAction Stop).Source
New-Item -ItemType Directory -Path $OutputDirectory -Force -ErrorAction Stop | Out-Null
$report = Join-Path $OutputDirectory '8054-read-only.txt'

function Add-Section {
    param([string]$Title, [string[]]$AdbArguments)
    $output = @(& $adb -s $Serial @AdbArguments 2>&1 | ForEach-Object { $_.ToString() })
    $exitCode = $LASTEXITCODE
    @('', "[$Title]", "adb exit code $exitCode") + $output |
        Out-File -LiteralPath $report -Encoding utf8 -Append
    return $output
}

'FlixTone 8054 | read-only package and service diagnostics' |
    Out-File -LiteralPath $report -Encoding utf8
Add-Section -Title 'ADB connection' -AdbArguments @('get-state') | Out-Null
Add-Section -Title 'Installed APK path' -AdbArguments @('shell','pm','path','com.android.tv.settings') | Out-Null
Add-Section -Title 'Installed package and version' -AdbArguments @('shell','dumpsys','package','com.android.tv.settings') |
    Out-Null

$home = @(Add-Section -Title 'HOME default' -AdbArguments @(
    'shell','cmd','package','resolve-activity','--brief','-a','android.intent.action.MAIN','-c','android.intent.category.HOME'))
Add-Section -Title 'HOME registered candidates' -AdbArguments @(
    'shell','cmd','package','query-activities','--brief','-a','android.intent.action.MAIN','-c','android.intent.category.HOME') |
    Out-Null
foreach ($line in $home) {
    if ($line -match '^([A-Za-z0-9_.]+)/(?:[A-Za-z0-9_.$]+)$') {
        Add-Section -Title 'Resolved HOME package flags' -AdbArguments @('shell','dumpsys','package',$Matches[1]) |
            Out-Null
        break
    }
}

Add-Section -Title 'TV inputs and their states' -AdbArguments @('shell','dumpsys','tv_input') | Out-Null
Add-Section -Title 'HDMI-CEC service state' -AdbArguments @('shell','dumpsys','hdmi_control') | Out-Null
Add-Section -Title 'Stored CEC setting' -AdbArguments @('shell','settings','get','global','hdmi_control_enabled') | Out-Null
Add-Section -Title 'Stored EDID mode' -AdbArguments @('shell','settings','get','global','tv_input_hdmi_edid_version') | Out-Null
Add-Section -Title 'Bluetooth adapter and profiles' -AdbArguments @('shell','dumpsys','bluetooth_manager') | Out-Null

$properties = @(
    'ro.rjpj.machine.id',
    'persist.trd.source_id',
    'persist.rjpj.autokeystone.move',
    'persist.rjpj.screen.alignment',
    'persist.rjpj.obstacle.avoidance',
    'persist.rjpj.keystone.auto.vertical'
)
foreach ($property in $properties) {
    Add-Section -Title $property -AdbArguments @('shell','getprop',$property) | Out-Null
}
Add-Section -Title 'Recent app and Activity errors' -AdbArguments @(
    'logcat','-d','-t','500','-v','threadtime','-s','TvSettingsPlusMOD','ActivityTaskManager','AndroidRuntime') |
    Out-Null
Write-Output (Get-Item -LiteralPath $report | Select-Object -ExpandProperty FullName)

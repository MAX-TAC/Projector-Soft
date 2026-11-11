@echo off
setlocal EnableExtensions EnableDelayedExpansion
title TvSettingsPlus MOD - 8054 ALPHA1 preflight installer
set "EXPECTED_BASE=e4184a13fbf56fa069effc6a40ea50c0da7e9190321b389f59ba34700a80d0a1"
set "FILE=%~dp0TvSettingsPlus-8054-ALPHA1-v26.apk"

echo ================================================================
echo   TvSettingsPlus MOD 8054 ALPHA1 - STRICT PREINSTALL CHECK
echo ================================================================
echo Never disable KuSettings or KuSourceRJ.
if not exist "!FILE!" (
  echo ERROR: Put the APK in the same folder as this script.
  pause
  exit /b 1
)
where adb >nul 2>nul
if errorlevel 1 (
  echo ERROR: adb.exe is not in PATH. Install Android platform-tools.
  pause
  exit /b 1
)
adb get-state >nul 2>nul
if errorlevel 1 (
  echo ERROR: Connect and authorize exactly one projector over ADB.
  pause
  exit /b 1
)
set "ID="
for /f "usebackq delims=" %%I in (`adb shell getprop ro.rjpj.machine.id`) do set "ID=%%I"
echo Detected RUIOS machine ID: !ID!
if not "!ID!"=="8054" (
  echo BLOCKED: Firmware identity was not exactly 8054.
  echo Do not force install. Collect OEM diagnostics first.
  pause
  exit /b 1
)
set "ANDROID="
for /f "usebackq delims=" %%I in (`adb shell getprop ro.build.version.release`) do set "ANDROID=%%I"
if not "!ANDROID!"=="13" (
  echo BLOCKED: This alpha was compiled for Android 13 only.
  pause
  exit /b 1
)
set "DEVICE_PATH="
for /f "usebackq delims=" %%P in (`adb shell pm path com.android.tv.settings`) do (
  if not defined DEVICE_PATH (
    set "LINE=%%P"
    if /i "!LINE:~0,8!"=="package:" set "DEVICE_PATH=!LINE:~8!"
  )
)
if not defined DEVICE_PATH (
  echo BLOCKED: Stock com.android.tv.settings package not found.
  pause
  exit /b 1
)
echo Installed APK: !DEVICE_PATH!
echo !DEVICE_PATH!| findstr /B /C:"/system" >nul
if errorlevel 1 (
  echo BLOCKED: The installed Settings APK is not the original system app.
  echo An earlier MOD or a different build may already be installed.
  pause
  exit /b 1
)
set "DEVICE_SHA="
for /f "tokens=1" %%H in ('adb shell sha256sum "!DEVICE_PATH!" 2^>nul') do set "DEVICE_SHA=%%H"
echo Installed APK SHA-256: !DEVICE_SHA!
if /I not "!DEVICE_SHA!"=="!EXPECTED_BASE!" (
  echo BLOCKED: The current firmware Settings APK differs from the researched stock base.
  echo Do not overwrite it. Send the detected hash for analysis.
  pause
  exit /b 1
)
if not exist "%~dp0TvSettingsPlus-original-8054.apk" (
  echo Backing up original system APK...
  adb pull "!DEVICE_PATH!" "%~dp0TvSettingsPlus-original-8054.apk"
  if errorlevel 1 (
    echo BLOCKED: Could not save backup of original APK.
    pause
    exit /b 1
  )
)
echo.
echo Verified: original factory APK hash, RUIOS 8054 identity, Android 13.
echo This is an UNTESTED ALPHA, not a production release.
set "APPROVE="
set /p "APPROVE=Type YES to install this experimental Settings update: "
if /I not "!APPROVE!"=="YES" (
  echo Cancelled, nothing installed.
  exit /b 0
)
adb install -r "!FILE!"
if errorlevel 1 (
  echo INSTALL FAILED. OEM settings package was not changed by this script.
  pause
  exit /b 1
)
echo.
echo INSTALL FINISHED. Verify Settings launch and 8054 diagnostics.
echo Do NOT disable KuSettings. Reboot only after basic functions are checked.
pause
exit /b 0

# Аппаратный тест TvSettingsPlus 8054 v36

Артефакт v36 доступен в [Exact-8054](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44692995125/artifacts/14609353165). SHA-256 APK — `0f79bf8d4914a483ada782895ab75cf3effac831a47463bc4fa0ebaaf5883082`. [Отчёт и снимки тестовой среды Leanback](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44692995238/artifacts/14609895658).

Только после отдельного разрешения владельца. Используйте Windows PowerShell, ADB с явно выбранным serial и, для проверки сертификата, apksigner из Android SDK. Python не требуется. Сохраните исходное состояние, условия возврата к STOCK и устойчивый способ доступа к настройкам. Установка и ручной возврат требуют отдельного согласия.

## Проверка файла на ПК

Укажите локальный APK из Exact-8054 и сравните его SHA-256 с SHA256SUMS внутри артефакта и отчётом выпуска. Проверяемый SHA-256 сертификата STOCK и MOD — c8a2e9bccf597c2fb6dc66bee293fc13f2fc47ec77bc6b2b0d52c11f51192ab8. APK должен иметь пакет com.android.tv.settings, versionCode 36 и android.uid.system.

```powershell
$apk = 'C:\Downloads\TvSettingsPlus-8054-v36-NATIVE-SETTINGS-EXPERIMENTAL.apk'
Get-FileHash -LiteralPath $apk -Algorithm SHA256
& "$env:LOCALAPPDATA\Android\Sdk\build-tools\35.0.0\apksigner.bat" verify --print-certs $apk
```

## Этап A. Версия, запуск и диагностика

После отдельно разрешённой установки команда pm path должна показать активный APK. Сверьте versionCode=36 в выводе dumpsys. Команды ниже запускают UI, читают данные и сохраняют отчёт на ПК. Смена состояния аппаратных настроек в этот этап не входит.

```powershell
$serial = '<точный согласованный serial>'
$report = Join-Path $env:TEMP ('8054-v36-' + (Get-Date -Format yyyyMMdd-HHmmss))
New-Item -ItemType Directory -Path $report | Out-Null
$active = @(adb -s $serial shell pm path com.android.tv.settings | Where-Object { $_ -match '^package:/[^\r\n]+\.apk$' })
if ($LASTEXITCODE -ne 0 -or $active.Count -ne 1) { throw 'STOP: PackageManager не вернул один активный APK' }
$live = Join-Path $report 'installed-base.apk'
$livePath = $active[0].Trim().Substring(8)
adb -s $serial pull $livePath $live
if ($LASTEXITCODE -ne 0) { throw 'STOP: активный APK не прочитан' }
if ((Get-FileHash -LiteralPath $live -Algorithm SHA256).Hash -ne (Get-FileHash -LiteralPath $apk -Algorithm SHA256).Hash) {
    throw 'STOP: установленный APK отличается от файла выпуска'
}
adb -s $serial shell dumpsys package com.android.tv.settings > (Join-Path $report 'package.txt')
adb -s $serial shell am start -W -n 'com.android.tv.settings/com.android.tv.settings.MainSettings'
adb -s $serial shell am start -W -n 'com.android.tv.settings/dev.projectorsoft.tvsettings.ModMainSettingsActivity'
$pages = 'Network','Bluetooth','Sources','Projection','Picture','Sound','Apps','System'
foreach ($page in $pages) {
    Write-Host "Компонент для отдельной проверки: com.android.tv.settings/dev.projectorsoft.tvsettings.${page}SettingsActivity"
}
adb -s $serial shell am start -W -n 'com.android.tv.settings/dev.projectorsoft.tvsettings.SourcesSettingsActivity'
adb -s $serial shell dumpsys tv_input > (Join-Path $report 'tv-input.txt')
adb -s $serial shell dumpsys activity activities > (Join-Path $report 'activities.txt')
adb -s $serial logcat -d -v threadtime -s AndroidRuntime TvSettingsPlusMOD ActivityTaskManager > (Join-Path $report 'settings-logcat.txt')
```

Для каждого компонента из списка запустите тот же шаблон am start -W -n и нажмите Back после проверки. Смотрите реальный экран и ActivityTaskManager. Статус команды старта один не исключает падение на следующем кадре. Сверьте порядок восьми разделов, шрифты и фокус со штатным экраном, D-pad/OK/Back, preview, предупреждения и возврат из редакторов. Не выполняйте reset, очистку данных, аппаратную калибровку и операции выключения. При затемнении снимите logcat без его очистки. Держите список приложений, идентификаторы и отчёты локально.

## Этап B. По одному согласованному изменению

| Область | Исходное состояние, проверка и возврат |
|---|---|
| HDMI | Записать исходный вход, HW6/HW7 и сигнал. По одному выбрать HDMI 1, HDMI 2, Android TV. Сверить реальный разъём и вернуться к исходному. Запись EDID закрыта. |
| CEC | Сохранить статусы CEC, Auto Off и аудиовыход. Проверить один Auto Off и перечитать. Согласовать отдельный цикл standby. Вернуть прежнее значение. |
| Звук | Записать профиль, выход, громкость и исходную задержку. Изменить один доступный параметр, проверить звук и readback, вернуть в том же профиле. |
| Изображение | Сравнить read-only значения и штатный формат изображения. Проверку HDR/MEMC/PQ отдельно провести по STOCK-протоколу. |
| Фокус, геометрия и свет | Отдельное разрешение для каждого аппаратного действия. Снять кадр исходной геометрии и режим света. Проверить один OEM-редактор или одну настройку, вернуть в том же контексте. Без reset, калибровки и моторных команд вне редактора. |
| Запуск и питание | Записать исходные persist.trd.source_id, режим питания и доступность TimeService. Один согласованный reboot или цикл питания на отдельном сеансе. Проверить фактический режим и доступность управления, вернуть прежний выбор. |

## Возврат к STOCK

После согласования используйте штатную карточку Android TV Settings в списке системных приложений и её действие **Удалить обновления**, если оно доступно и относится к com.android.tv.settings. Владелец уже наблюдал этот возврат для предыдущего MOD. Удаление обновления может очистить данные. После возврата проверьте встроенный путь /system_ext/priv-app/TvSettingsPlus/TvSettingsPlus.apk, versionCode 1, штатный запуск и работу связи. При отсутствии пункта остановитесь. Не используйте отключение пакета, downgrade, reset или перепрошивку как подмену этой процедуры.

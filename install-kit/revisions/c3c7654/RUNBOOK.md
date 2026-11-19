# TvSettingsPlus 8054 c3c7654

Комплект предназначен для проверки на компьютере и последующей диагностики работающего **STOCK** с отдельного разрешения владельца. Статусы выпуска зафиксированы в `manifest.json`. Состояние APK — `BUILT_UNTESTED`. Возврат на 8054 имеет статус `ROLLBACK_NOT_VERIFIED`. Установка имеет статус `NOT_READY_FOR_DEVICE_INSTALL`.

| Поле | Значение |
|---|---|
| Устройство | FlixTone D11 Master, machine ID 8054, MT9660, Android 13, API 33 |
| STOCK | `assets/STOCK-TvSettingsPlus-8054.apk`, версия 1, SHA-256 `f8e0c6fe0f46e7eb8db651c31d85ef8b32a769b37e4dd0f96ed19a64725d74ee` |
| MOD | `assets/MOD-TvSettingsPlus-8054-c3c7654.apk`, версия 33, SHA-256 `f8f4c0c3654929ccaa98aa4f75555f96f52d3e85a99dd963f877bf11e0db8d40` |
| Коммит MOD | `c3c765468b321afe2fa078d8431ef840c01b2382` |
| Сборка MOD | [Exact-8054 44501022081, артефакт 14516084668](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44501022081/artifacts/14516084668) |
| Оригинал | [Резервная копия 8054 владельца](https://github.com/MAX-TAC/FlixTone_8054_Backup/releases/tag/FlixTone_8054_Backup), `system_ext_a/priv-app/TvSettingsPlus/TvSettingsPlus.apk` |
| Пакет и shared UID | `com.android.tv.settings`, `android.uid.system` |
| SHA-256 сертификата | `c8a2e9bccf597c2fb6dc66bee293fc13f2fc47ec77bc6b2b0d52c11f51192ab8` |

Оба входных ZIP проверяются в CI по SHA-256 перед извлечением. Из контрольного комплекта 9f8fc64 берутся только байты STOCK. Для текущего MOD CI заново проверяет APK, Manifest, разрешения, OEM DEX и ресурсы. `provenance.json` содержит пины обеих загрузок. `reports/offline` содержит исходные выводы SDK и отчёты. `RECOVERY.md` описывает варианты возврата и условия проверки.

## Проверка комплекта на компьютере

Сверьте SHA-256 скачанного ZIP со значением в сводке выпустившего его GitHub Actions. Проверяйте внешний SHA-256 до распаковки и запуска любого файла. Сохраните проверенный ZIP. Внутренний `SHA256SUMS` проверяет файлы после распаковки. Для проверки нужны Python 3.10+, Android SDK Build Tools с AAPT и apksigner и Java для apksigner. Перейдите в распакованный каталог. Ниже приведены примеры каталогов SDK. Укажите пути установленной версии.

Windows PowerShell

```powershell
Get-FileHash -Algorithm SHA256 -LiteralPath "C:\Downloads\tvsettingsplus-8054-diagnostics-c3c7654.zip"
$sdk = "$env:LOCALAPPDATA\Android\Sdk"
$bt = "$sdk\build-tools\35.0.0"
py -3 .\preflight.py --offline --kit . --revision c3c7654 --aapt "$bt\aapt.exe" --apksigner "$bt\lib\apksigner.jar"
```

Linux

```bash
sha256sum ~/Downloads/tvsettingsplus-8054-diagnostics-c3c7654.zip
sdk="$HOME/Android/Sdk"
bt="$sdk/build-tools/35.0.0"
python3 ./preflight.py --offline --kit . --revision c3c7654 --aapt "$bt/aapt" --apksigner "$bt/apksigner"
```

## Диагностика STOCK после отдельного разрешения

Используйте уже настроенный и доступный транспорт ADB. Укажите точный согласованный serial, включая порт, если это сетевой ADB. Запустите один из примеров со своего компьютера. Подставьте serial и новый путь для отчёта. На Linux путь должен быть под `/tmp`. Скрипт проверит комплект и APK локально перед первой командой ADB.

Windows PowerShell

```powershell
$serial = '<точный согласованный serial>'
$report = Join-Path $env:TEMP ("tvsettings8054-stock-" + (Get-Date -Format yyyyMMdd-HHmmss))
py -3 .\preflight.py --kit . --revision c3c7654 --serial "$serial" --out "$report" --adb "$sdk\platform-tools\adb.exe" --aapt "$bt\aapt.exe" --apksigner "$bt\lib\apksigner.jar"
$LASTEXITCODE
```

Linux

```bash
serial='<точный согласованный serial>'
report="/tmp/tvsettings8054-stock-$(date +%Y%m%d-%H%M%S)"
python3 ./preflight.py --kit . --revision c3c7654 --serial "$serial" --out "$report" --adb "$sdk/platform-tools/adb" --aapt "$bt/aapt" --apksigner "$bt/apksigner"
printf '%s\n' "$?"
```

Сохраните весь каталог локально. Для разбора нужны `preflight.json`, вывод PackageManager, данные подключения, состояние SELinux, TimeService и критических OEM-пакетов, сведения о разрешениях и результаты проверки обоих выгруженных APK. Справка PackageManager подтверждает только наличие подсказки. Диагностика читает состояние текущей загрузки и не проверяет восстановление ADB после перезагрузки. Перед публикацией выводов удалите идентификаторы устройства и проверьте, что в файлах нет личных данных.

| Код | Результат |
|---|---|
| 0 | Offline-проверка прошла. В отдельных прежних режимах `--picture-stock` и `--hdmi-stock` код означает, что снимок получен |
| 1 | Ошибка или STOP. Если сработала проверка комплекта, ADB ещё не вызывался |
| 2 | Полная диагностика завершилась с успешными проверками. Готовность к установке остаётся неподтверждённой. В отдельных прежних режимах код означает частичный снимок |

## Исходные значения и первый запуск MOD

`preflight.json` сохраняет исходное значение `system/screen_off_timeout`, `system/sleep_timeout`, `secure/sleep_timeout` и `secure/screensaver_enabled` для пользователя 0. Сохраняйте и статусы отсутствия ключей. `null` означает отсутствие значения в провайдере, а фактическое штатное значение может определяться настройками по умолчанию.

`OemScreenTimeoutSafetyHook.ensureNever()` может записать `2147483647` в `system/screen_off_timeout`, `-1` в оба `sleep_timeout` и `0` в `secure/screensaver_enabled`. Политика вызывается при загрузке и открытии соответствующих страниц. Изменение возможно без действий пользователя в меню и влияет на отключение экрана, сон или заставку при бездействии. Замена APK сама по себе не доказывает возврат значений провайдера. Автоматическое восстановление в комплект не входит.

## Состояние маршрутов

| ID | Что подтверждено статически |
|---|---|
| SYS07 | Штатный редактор расписания |
| SND12 | Условные нативные редакторы задержек |
| PIC14 | Штатная `DisplayModeActivity` для HDMI |
| PIC18 / FAC01 | Только чтение |
| PRJ06 / PRJ07 / PRJ10 | Отдельные OEM Activity |
| PRJ08 / PRJ09 | Сохранённые OEM-маршруты |
| PIC01–PIC12 | Только чтение |

После сбора диагностики проверьте отчёты совместно с сопровождающим проекта. Только затем можно принять решение о первом отдельном тесте.

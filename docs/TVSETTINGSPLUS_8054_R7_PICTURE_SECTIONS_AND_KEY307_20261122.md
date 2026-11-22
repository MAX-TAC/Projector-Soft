# TvSettingsPlus MOD v41 R7 — Picture slider geometry / three groups / remote Settings key 307

Date: 2026-11-22. Source branch: `feature/8054-v41-picture-sound-safety-20261122` (same branch as PR #10).

## Owner request / visual behavior
- Existing video-PQ editors (brightness, contrast, saturation, sharpness, hue) show a full-width bottom bar pressed against display bottom.
- RGB editor under Color Temperature shows a more compact control with sensible margins.
- Align **all** bottom picture sliders visually and leave actual MediaTek Global consumer read/write, User-only guard, fast DPAD and OK behavior intact.
- Picture screen must have exactly three STOCK-blue category headings, with Color Temperature directly after MEMC.

## R7 implementation
- `NativePicturePanel.showSlider`: previous `Dialog.Window.setLayout(MATCH_PARENT, WRAP_CONTENT)` stretched video PQ editor edge-to-edge. Use one `compactSliderWidth(displayWidth, 28dp)` helper: min(available width, 66% of display), gravity BOTTOM|CENTER_HORIZONTAL, 28dp bottom inset, no dim flag. The **same popup code** serves RGB and ordinary Picture scalar controls. Test ensures 1920px screen with 56px inset produces 1267px editor, not full width.
- `NativeSections.Picture`: group the SAME existing preferences into `PreferenceCategory` (blue style in the OEM Leanback theme), not new duplicate controls:
  1. **Обработка изображения** — HDR; MEMC.
  2. **Цвет и баланс белого** — link to Color Temperature (Standard / Warm / Cool / User, then User RGB).
  3. **Параметры видеотракта** — Picture Mode, video-only information, Brightness, Contrast, Saturation, Sharpness, Hue, Gamma (still User-only), optional HDMI picture-format.
- Existing `NativePicturePanel` references for refresh, Observers, protection of Factory modes and OEM consumer readback are reused; only visual containment changes.

## Remote key evidence from user's uploaded `8054_settings_route.zip`
Archive files:
- `standard_settings.txt`: `android.settings.SETTINGS` resolves to `com.my.settings/.SettingActivity`.
- `rj_settings.txt`: `android.settings.RJ_SETTINGS` resolves to `com.android.tv.settings/.uicustom.MainActivity`.
- `key_logcat.txt` timestamps:
  - `20:13:38.278` `TrdTvWindowMangerImpl interceptKeyBeforeQueueing keyCode:307`
  - `20:13:38.280` `trdservice KeyEventCtrlService interceptKeyBeforeQueueing keyCode:307 down:true`
  - `20:13:38.321` `ActivityManager Start proc com.android.tv.settings for service com.android.tv.settings.uicustom.floatmenu.FloatMenuService`
  - `20:13:38.779` `FloatMenuManager showMenu`
  - `20:13:38.924` `FloatMenuManager Loading initial sub menu for index: 0`
- `window.txt`: top visible `com.android.tv.settings` window has type `APPLICATION_OVERLAY`, not a new traditional settings Activity.
- Thus the remote button is **not** routed through standard `android.settings.SETTINGS`; adding an intent-filter to the launcher does not intercept the firmware policy's direct service startup.
- The source of direct `startService` in `PhoneWindowManager` / `TrdTvWindowMangerImpl` or service startup needs separate controlled binary/static investigation. Do not disable Chinese menu, remove FloatMenuService, edit vendor keylayout or redirect system global input without a verified restoration path.
- `KEYCODE 307` is a numeric diagnostic fact, not a claim about the Android public key name. Ownership is established by log chain; no firmware file modified for remote support in R7.

## Acceptance
- Visual: compact bar for brightness and RGB, both inset from bottom by same margin; image behind bar stays fully visible; no effect on OEM stored value or button repeat/OK.
- Picture list shows three blue headings and correct membership, no duplicate HDR, MEMC, Color Temperature or Gamma entries.
- Remote button remains a separate unresolved task despite definitive routing log. Don't claim remapping works.
- After CI pass, owner still needs on-projector visual validation.

# 8054 source safety changes (no device actions)

## C0 — TimeService ownership gate

`KuSourceRecoveryBootReceiver.needsReplacement` reads `ro.rjpj.machine.id` and the KuSettings enabled state. `KuSourceRecoveryActivationPolicy.automaticRecoveryAllowed` permits the existing guarded 8052 path only. On 8054 or an unidentified board it returns false, so the MOD does not start `KuSourceRecoveryService`; the original KuSettings remains the only known owner. This protects against treating a filtered Android 13 `getRunningServices` result as proof that TimeService stopped.

Test: `KuSourceRecoveryActivationPolicyTest` checks disabled states on 8054 and unknown boards. It is a JVM policy test, not an Android lifecycle or fan test. Rollback: revert the C0 commit in source; keep KuSettings enabled throughout. No service or package was disabled.

## C1 — two-HDMI EDID gate

`OemHdmiEdidHook` previously queried `getVideoInfoData(6, ...)` and wrote one `tv_input_hdmi_edid_version` Global key. The meaning of that pair for the second physical port HW7 is not proven. `EdidPortPolicy` now prevents the legacy selector from appearing on 8054 or an unknown board. The OEM source page remains available for settings and investigation.

Test: `EdidPortPolicyTest` checks 8052, 8054 and unknown identifiers. This is a JVM policy test; no EDID write or HDMI negotiation has been attempted. Rollback: revert the C1 commit in source and use the stock OEM source page until a port-aware implementation passes bytecode and hardware validation.

## C2 — legacy orientation gate

`OemProjectorOrientationHook` now reads the board ID before creating the MOD's orientation selector. `ProjectorCompatibilityPolicy.mayOfferLegacyOrientation` keeps the existing 8052 path and hides the selector on 8054 or an unknown board. The stock OEM projection link remains in the right pane. This is a source safety gate: no I²C, calibration, Flinger, or optical command was sent.

Test: `ProjectorCompatibilityPolicyTest` covers 8052, 8054 and an unknown ID; CI checks that the hook uses the gate. Rollback: revert the C2 source commit after tracing the exact 8054 TvSettingsPlus setter's arguments and side effects, and after an approved hardware test with a recorded original orientation. Factory's similarly named setter changed signature in 8054 and must not be confused with the TvSettingsPlus class.

## C3 — read-only OEM AutoPair setting

`OemBluetoothRemoteHook` now displays the current `Settings.Global.autopair_status` value in its existing Bluetooth dialog. The exact 8054 `AutoPair.BootBroadcastReceiver#onReceive` reads that key with default 1 and exits when the value is 0. The dialog explicitly says that this setting does not prove whether AutoPairService is running. It never changes the value, starts a scan, pairs, connects, or disables a package.

Test: `OemBluetoothRemotePolicyTest` covers absent, 0, 1 and unknown values. Exact APK build and Android source CI must pass; a live read requires owner permission. Rollback: revert C3 source changes; the stock KuSettings/AutoPair path remains enabled throughout.

## CI and remaining gates

`audit-8054-source-safety.yml` validates the dependency evidence, compiles all Java against Android API 33 and runs pure JVM tests. Source safety run 44105161251 and exact-8054 build run 44105161286 passed with C0/C1/C2/C3; see `BUILD_STATUS_8054_AUDIT_2026-11-17.md`. Installed package/update compatibility, TV Input HW6/HW7, Bluetooth remote, audio/PQ HAL, optics and TimeService/fan behavior remain open. No 8054 hardware parity claim follows from CI.

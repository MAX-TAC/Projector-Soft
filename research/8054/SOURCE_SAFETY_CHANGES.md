# 8054 source safety changes (no device actions)

## C0 — TimeService ownership gate

`KuSourceRecoveryBootReceiver.needsReplacement` reads `ro.rjpj.machine.id` and the KuSettings enabled state. `KuSourceRecoveryActivationPolicy.automaticRecoveryAllowed` permits the existing guarded 8052 path only. On 8054 or an unidentified board it returns false, so the MOD does not start `KuSourceRecoveryService`; the original KuSettings remains the only known owner. This protects against treating a filtered Android 13 `getRunningServices` result as proof that TimeService stopped.

Test: `KuSourceRecoveryActivationPolicyTest` checks disabled states on 8054 and unknown boards. It is a JVM policy test, not an Android lifecycle or fan test. Rollback: revert the C0 commit in source; keep KuSettings enabled throughout. No service or package was disabled.

## C1 — two-HDMI EDID gate

`OemHdmiEdidHook` previously queried `getVideoInfoData(6, ...)` and wrote one `tv_input_hdmi_edid_version` Global key. The meaning of that pair for the second physical port HW7 is not proven. `EdidPortPolicy` now prevents the legacy selector from appearing on 8054 or an unknown board. The OEM source page remains available for settings and investigation.

Test: `EdidPortPolicyTest` checks 8052, 8054 and unknown identifiers. This is a JVM policy test; no EDID write or HDMI negotiation has been attempted. Rollback: revert the C1 commit in source and use the stock OEM source page until a port-aware implementation passes bytecode and hardware validation.

## CI and remaining gates

`audit-8054-source-safety.yml` validates the dependency evidence, compiles all Java against Android API 33 and runs pure JVM tests. It does not repack or sign an APK. Exact 8054 APK rebuild, installed package/update compatibility, TV Input HW6/HW7, Bluetooth remote, audio/PQ HAL, optics and TimeService/fan behavior remain open. No 8054 hardware parity claim follows from this workflow.

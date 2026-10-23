# KuSettings migration audit (8052)

Based on JADX + manifest inspection of original APKs, not dynamic execution.
This is a **dependency inventory**, not proof that KuSettings can be disabled.

## Exported / background components

### TvSettingsPlus
Decompiled Java files: 5796

| Type | Name | Exported | Permission |
|---|---|---|---|
| provider | androidx.lifecycle.ProcessLifecycleInitializer | (type 0x12)0x0 |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | com.android.tv.settings.partnercustomizer.retailmode.RetailModeConfirmActivity | (type 0x12)0x0 |  |
| activity | com.android.tv.settings.partnercustomizer.EnergyCommonActivity | (type 0x12)0xffffffff |  |
| activity | com.android.tv.settings.partnercustomizer.sound.AudioResetActivity | (type 0x12)0x0 |  |
| activity | com.android.tv.settings.partnercustomizer.picture.PictureResetActivity | (type 0x12)0x0 |  |
| activity | com.android.tv.settings.partnercustomizer.inputs.InputsActivity | (type 0x12)0x0 |  |
| activity | com.android.tv.settings.partnercustomizer.power.PowerActivity | (type 0x12)0xffffffff |  |
| activity | com.android.tv.settings.partnercustomizer.power.PictureOffActivity | (type 0x12)0x0 |  |
| activity | com.android.tv.settings.partnercustomizer.vendor.channels.ChannelsActivity | (type 0x12)0xffffffff |  |
| activity | com.android.tv.settings.partnercustomizer.vendor.channels.DiseqcTypeActivity | (type 0x12)0xffffffff |  |
| activity | com.android.tv.settings.partnercustomizer.vendor.channels.ChannelsTwoPanalActivity | (type 0x12)0x0 |  |
| activity | com.android.tv.settings.partnercustomizer.vendor.channels.ChannelConfirmActivity | (type 0x12)0xffffffff |  |
| activity | com.android.tv.settings.partnercustomizer.retailmode.RetailModeActivity | (type 0x12)0x0 |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | com.android.tv.settings.partnercustomizer.timer.TimePickerActivity | (type 0x12)0x0 |  |
| activity | com.android.tv.settings.partnercustomizer.multicast.NormalCastActivity | (type 0x12)0x0 |  |
| activity | com.android.tv.settings.partnercustomizer.picture.DisplayModeActivity | (type 0x12)0xffffffff |  |
| activity | com.android.tv.settings.device.sound.AudioOutputActivity | (type 0x12)0xffffffff |  |
| activity | com.android.tv.settings.partnercustomizer.utils.ProgressDialogActivity | (type 0x12)0x0 |  |
| activity | com.android.tv.settings.partnercustomizer.picture.PictureConfirmActivity | (type 0x12)0x0 |  |
| activity | com.android.tv.settings.partnercustomizer.vendor.ResetActivity | (type 0x12)0xffffffff |  |
| activity | android.settings.CAPTIONING_SETTINGS | (type 0x12)0xffffffff |  |
| activity | com.android.tv.settings.partnercustomizer.date.DateTimeActivity | (type 0x12)0x0 |  |
| activity | com.android.tv.settings.partnercustomizer.DisplayAndSoundActivity | (type 0x12)0xffffffff |  |
| activity | com.android.tv.settings.partnercustomizer.sound.wireless.WirelessAddNoteActivity | (type 0x12)0x0 |  |
| activity | com.android.tv.settings.partnercustomizer.sound.wireless.WirelessNoteCommonActivity | (type 0x12)0x0 |  |
| activity | com.android.tv.settings.partnercustomizer.sound.wireless.SpeakerDevicesBeyondActivity | (type 0x12)0x0 |  |
| activity | com.android.tv.settings.partnercustomizer.sound.wireless.SpeakerDeviceBeyondTipsActivity | (type 0x12)0x0 |  |
| activity | com.android.tv.settings.partnercustomizer.sound.wireless.ResetSpeakerActivity | (type 0x12)0x0 |  |
| activity | com.android.tv.settings.partnercustomizer.sound.wireless.SelectListActivity | (type 0x12)0x0 |  |
| activity | com.android.tv.settings.partnercustomizer.sound.wireless.StatelliteSpeakerLayoutActivity | (type 0x12)0x0 |  |
| activity | com.android.tv.settings.partnercustomizer.sound.wireless.StatelliteSpeakerLayoutChangeTipsActivity | (type 0x12)0x0 |  |
| receiver | com.android.tv.settings.partnercustomizer.network.NetworkSliceBroadcastReceiver | (type 0x12)0x0 |  |
| provider | android.app.slice.category.SLICE | (type 0x12)0xffffffff |  |
| provider | com.android.tv.settings.partnercustomizer.CustomizedContentProvider | (type 0x12)0xffffffff |  |
| service | com.android.tv.settings.partnercustomizer.tvsettingservice.BluetoothReconnectService | (type 0x12)0xffffffff |  |
| service | com.android.tv.settings.partnercustomizer.switchofftimer.SwitchOffTimerService | (type 0x12)0xffffffff |  |
| service | com.android.tv.settings.partnercustomizer.tvsettingservice.WirelessSpeakerServices | (type 0x12)0xffffffff |  |
| receiver | android.intent.action.BOOT_COMPLETED | (type 0x12)0xffffffff |  |
| receiver | android.intent.action.BOOT_COMPLETED | (type 0x12)0xffffffff |  |
| receiver | android.intent.action.LOCALE_CHANGED | (type 0x12)0x0 |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | com.android.tv.settings.uicustom.fragment.projector.KeystoneCalibrationActivity | (type 0x12)0xffffffff |  |
| activity | com.android.tv.settings.uicustom.floatmenu.MenuJumpActivity |  |  |
| service | com.android.tv.settings.uicustom.floatmenu.FloatMenuService | (type 0x12)0xffffffff |  |
| activity | android.intent.category.LEANBACK_LAUNCHER | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff | android.permission.NETWORK_SETTINGS |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff | android.permission.NETWORK_SETTINGS |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | com.android.tv.settings.accounts.AccountSliceActivity | (type 0x12)0x0 |  |
| activity | com.android.tv.settings.accounts.AccountsActivity | (type 0x12)0x0 |  |
| activity | com.android.tv.settings.accounts.AccountsTrampolineActivity | (type 0x12)0x0 |  |
| activity-alias | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | com.android.tv.settings.accounts.AccountSyncActivity | (type 0x12)0xffffffff |  |
| activity | com.android.tv.settings.accounts.ManageUserServicesSliceActivity | (type 0x12)0x0 |  |
| activity-alias | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | com.android.tv.settings.accounts.RemoveAccountDialog | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | com.android.settings.SHORTCUT | (type 0x12)0xffffffff |  |
| activity | com.android.settings.SHORTCUT | (type 0x12)0xffffffff |  |
| activity | android.settings.MANAGE_APP_ALL_FILES_ACCESS_PERMISSION | (type 0x12)0xffffffff |  |
| activity | android.settings.USAGE_ACCESS_SETTINGS | (type 0x12)0xffffffff |  |
| activity | android.settings.PICTURE_IN_PICTURE_SETTINGS | (type 0x12)0xffffffff |  |
| activity | android.settings.action.MANAGE_OVERLAY_PERMISSION | (type 0x12)0xffffffff |  |
| activity | android.settings.REQUEST_SCHEDULE_EXACT_ALARM | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.os.storage.action.MANAGE_STORAGE | (type 0x12)0xffffffff |  |
| activity | com.android.tv.settings.device.storage.ResetActivity |  |  |
| activity | android.intent.category.LEANBACK_SETTINGS | (type 0x12)0xffffffff |  |
| activity-alias | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity-alias | android.intent.category.LEANBACK_SETTINGS | (type 0x12)0xffffffff |  |
| activity | com.android.settings.SHORTCUT | (type 0x12)0xffffffff |  |
| activity | com.android.tv.settings.device.display.ambient.AmbientSliceActivity | (type 0x12)0x0 |  |
| activity-alias | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | com.android.tv.settings.device.display.daydream.DaydreamActivity | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity-alias | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | com.android.settings.SHORTCUT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | com.android.tv.settings.system.development.AppPicker | (type 0x12)0x0 |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | com.android.tv.settings.autofill.AutofillPickerActivity | (type 0x12)0x0 |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | com.android.tv.settings.system.InputsActivity | (type 0x12)0xffffffff |  |
| activity | com.android.tv.settings.accessories.BluetoothAccessoryActivity | (type 0x12)0xffffffff | android.permission.BLUETOOTH_CONNECT |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff | android.permission.BLUETOOTH_CONNECT |
| receiver | android.bluetooth.device.action.PAIRING_REQUEST | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff | android.permission.BLUETOOTH_PRIVILEGED |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.VOICE_LAUNCH | (type 0x12)0xffffffff |  |
| activity | android.intent.category.VOICE_LAUNCH | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | com.android.settings.icon | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| service | com.android.tv.settings.users.UserSwitchListenerService | (type 0x12)0x0 |  |
| receiver | android.intent.action.BOOT_COMPLETED | (type 0x12)0xffffffff |  |
| receiver | com.google.android.tungsten.setupwraith.TV_SETTINGS_POST_SETUP | (type 0x12)0xffffffff |  |
| service | com.android.tv.settings.users.RestrictedProfilePinService | (type 0x12)0x0 |  |
| service | com.android.tv.settings.device.storage.SettingsStorageService$Impl | (type 0x12)0x0 |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0x0 |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0x0 |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0x0 |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0x0 |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0x0 |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff | android.permission.BLUETOOTH |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| provider | android.support.FILE_PROVIDER_PATHS | (type 0x12)0x0 |  |
| provider | com.android.settings.action.SUGGESTION_STATE_PROVIDER | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| receiver | com.android.tv.settings.REMOVE_WIFI | (type 0x12)0xffffffff | android.permission.READ_PRIVILEGED_PHONE_STATE |
| activity | com.android.tv.settings.accessories.BluetoothActionActivity | (type 0x12)0x0 |  |
| service | com.android.tv.settings.accessories.BluetoothDevicesService | (type 0x12)0x0 |  |
| provider | android.app.slice.category.SLICE | (type 0x12)0xffffffff |  |
| receiver | com.android.tv.settings.accessories.ConnectedDevicesSliceBroadcastReceiver | (type 0x12)0x0 |  |
| receiver | android.intent.action.BOOT_COMPLETED | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff | android.permission.DUMP |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| receiver | android.intent.action.GLOBAL_BUTTON | (type 0x12)0xffffffff |  |
| activity | com.android.settingslib.users.AvatarPickerActivity |  |  |
| receiver | androidx.remotecallback.ProviderRelayReceiver |  |  |
| activity | androidx.slice.compat.SlicePermissionActivity |  |  |

### KuSettings
Decompiled Java files: 3470

| Type | Name | Exported | Permission |
|---|---|---|---|
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.settings.WIFI_SETTINGS | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | com.my.settings.time.MyZonePicker |  |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| service | com.my.service.ScreenServices |  |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| receiver | android.intent.action.BOOT_COMPLETED | (type 0x12)0xffffffff |  |
| service | com.my.service.MySetTimeService |  |  |
| provider | androidx.profileinstaller.ProfileInstallerInitializer | (type 0x12)0x0 |  |
| receiver | androidx.profileinstaller.action.BENCHMARK_OPERATION | (type 0x12)0xffffffff | android.permission.DUMP |

### KuSourceRJ
Decompiled Java files: 3007

| Type | Name | Exported | Permission |
|---|---|---|---|
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| receiver | android.intent.action.BOOT_COMPLETED | (type 0x12)0xffffffff |  |
| receiver | com.action.screensaver | (type 0x12)0xffffffff |  |
| receiver | com.action.brightReceiver | (type 0x12)0xffffffff |  |
| service | com.coolux.source.service.TimeService |  |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| activity | android.intent.category.DEFAULT | (type 0x12)0xffffffff |  |
| provider | androidx.lifecycle.ProcessLifecycleInitializer | (type 0x12)0x0 |  |

### RuiAutoFocus
Decompiled Java files: 0

| Type | Name | Exported | Permission |
|---|---|---|---|
| service | com.hysd.client.VAF2S | (type 0x12)0xffffffff |  |

### KeystoneCorrection
Decompiled Java files: 0

| Type | Name | Exported | Permission |
|---|---|---|---|
| receiver | com.rjpj.tcorrection.TCorrectionBroadcastReceiver | (type 0x12)0xffffffff |  |

### trdservice
Decompiled Java files: 0

| Type | Name | Exported | Permission |
|---|---|---|---|
| service | com.trd.service.service | (type 0x12)0xffffffff |  |
| receiver | com.android.server.power.ACTION_PRE_SHUTDOWN | (type 0x12)0xffffffff |  |

### trdmiddle
Decompiled Java files: 0

| Type | Name | Exported | Permission |
|---|---|---|---|
| service | com.trd.framework.service | (type 0x12)0xffffffff |  |

### RuiYunOta
Decompiled Java files: 0

| Type | Name | Exported | Permission |
|---|---|---|---|
| service | .OTADownloadService |  |  |
| service | .OTACheckService |  |  |

## Cross-package references (possible links, not confirmed runtime calls)
| Caller | Reference | File | Occurrences |
|---|---|---|---:|

## OEM API candidate files

### KuSettings
- \`com/p001my/settingslib/RestrictedLockImageSpan.java\`: RestrictedLockImageSpan, draw, getDrawable, getSize, return, super
- \`com/p001my/settingslib/DeviceInfoUtils.java\`: for, formatKernelVersion, getBidiFormattedPhoneNumber, getFeedbackReporterPackage, getFormattedKernelVersion, getFormattedPhoneNumber, getFormattedPhoneNumbers, getMsvSuffix, getSecurityPatch, if, readLine
- \`com/p001my/settingslib/Utils.java\`: formatPercentage, getSingleSecurityTypeFromMultipleSecurityTypes, if, isAudioModeOngoingCall, isWifiOnly
- \`com/p001my/settingslib/RestrictedLockUtilsInternal.java\`: Proxy, checkForLockSetting, checkIfAccessibilityServiceDisallowed, checkIfApplicationIsSuspended, checkIfKeyguardFeaturesDisabled, checkIfMaximumTimeToLockIsSet, checkIfRemoteContactSearchDisallowed, checkIfRestrictionEnforced, checkIfUninstallBlocked, findEnforcedAdmin, for, getDeviceOwner, getManagedProfileId
- \`com/p001my/settingslib/WirelessUtils.java\`: if, isAirplaneModeOn, isRadioAllowed
- \`com/p001my/settingslib/RestrictedLockUtils.java\`: EnforcedAdmin, combine, createDefaultEnforcedAdminWithRestriction, equals, getDeviceOwner, getProfileOrDeviceOwner, getShowAdminSupportDetailsIntent, hashCode, if, isCurrentUserOrProfile, sendShowAdminSupportDetailsIntent, toString
- \`com/p001my/utils/ActivitysUtils.java\`: addActivity, destory, for, getInstance, if
- \`com/p001my/settings/LinkifyUtils.java\`: LinkifyUtils, if, linkify, onClick, updateDrawState
- \`com/p001my/settings/AirplaneModeEnabler.java\`: AirplaneModeEnabler, if, isAirplaneModeOn, onAirplaneModeChanged, onChanged, onRadioPowerStateChanged, setAirplaneModeInECM, setAirplaneModeOn, start, stop
- \`com/p001my/settings/C0950R.java\`: C0950R, array, attr, color, dimen, drawable, id, layout, mipmap, string, style, styleable, xml
- \`com/p001my/settings/MainActivity.java\`: onCreate, setContentView
- \`com/p001my/settings/ProxySelector.java\`: getDialogMetricsCategory, getMetricsCategory, if, initView, onClick, onCreateDialog, onCreateView, onFocusChange, switch, validate
- \`com/p001my/settings/DialogCreatable.java\`: getDialogMetricsCategory, onCreateDialog
- \`com/p001my/settings/WirelessSettings.java\`: if, isRadioAllowed
- \`com/p001my/settings/BaseActivity.java\`: 
- \`com/p001my/settings/Utils.java\`: confirmWorkProfileCredentials, convertCornerRadiusBitmap, createAccessibleSequence, createBitmap, createIconWithDrawable, createLocaleFromString, createPackageContextAsUser, enforceSameOwner, enforceSystemUser, for, forceCustomPadding, formatDateRange, formatIpAddresses
- \`com/p001my/settings/SettingActivity.java\`: enterClick, findUI, if, init, initView, isUpdataOtaRegister, isViewRemote, onClick, onCreate, onDestroy, onPause, onReceive, onResume
- \`com/p001my/service/BootupReceiver.java\`: if, onReceive
- \`com/p001my/service/MySetTimeService.java\`: if, onBind, onReceive, onStartCommand, registerReceiver, registerTimeTick
- \`com/p001my/service/ScreenServices.java\`: bindService, handleMessage, if, onBind, onServiceConnected, onServiceDisconnected, onStartCommand, run, setKeyStore, startShot, switch, synchronized, takeScreenShot
- \`com/p001my/settingslib/bluetooth/HearingAidDeviceManager.java\`: HearingAidDeviceManager, findMainDevice, for, getCachedDevice, getDeviceMode, getDeviceSide, getHiSyncId, if, initHearingAidDeviceIfNeeded, isValidHiSyncId, log, onHiSyncIdChanged, onProfileConnectionStateChangedIfProcessed
- \`com/p001my/settingslib/bluetooth/MapProfile.java\`: MapProfile, MapServiceListener, accessProfileEnabled, finalize, getConnectedDevices, getConnectionPolicy, getConnectionStatus, getDrawableResource, getNameResource, getOrdinal, getProfileId, getSummaryResourceForDevice, if
- \`com/p001my/settingslib/bluetooth/OppProfile.java\`: OppProfile, accessProfileEnabled, getConnectionPolicy, getConnectionStatus, getDrawableResource, getNameResource, getOrdinal, getProfileId, getSummaryResourceForDevice, isAutoConnectable, isEnabled, isProfileReady, setEnabled
- \`com/p001my/settingslib/bluetooth/VolumeControlProfile.java\`: VolumeControlProfile, VolumeControlProfileServiceListener, accessProfileEnabled, getConnectedDevices, getConnectionPolicy, getConnectionStatus, getDrawableResource, getNameResource, getOrdinal, getProfileId, getSummaryResourceForDevice, if, isAutoConnectable
- \`com/p001my/settingslib/bluetooth/BluetoothBroadcastUtils.java\`: 
- \`com/p001my/settingslib/bluetooth/PbapClientProfile.java\`: PbapClientProfile, PbapClientServiceListener, accessProfileEnabled, finalize, for, getConnectedDevices, getConnectionPolicy, getConnectionStatus, getDrawableResource, getNameResource, getOrdinal, getProfileId, getSummaryResourceForDevice
- \`com/p001my/settingslib/bluetooth/SapProfile.java\`: SapProfile, SapServiceListener, accessProfileEnabled, finalize, getConnectedDevices, getConnectionPolicy, getConnectionStatus, getDrawableResource, getNameResource, getOrdinal, getProfileId, getSummaryResourceForDevice, if
- \`com/p001my/settingslib/bluetooth/LocalBluetoothProfileManager.java\`: HeadsetStateChangeHandler, LocalBluetoothProfileManager, PanStateChangedHandler, StateChangedHandler, addHeadsetProfile, addPanProfile, addProfile, addServiceListener, callServiceConnectedListeners, callServiceDisconnectedListeners, for, getA2dpProfile, getA2dpSinkProfile
- \`com/p001my/settingslib/bluetooth/PanProfile.java\`: PanProfile, PanServiceListener, accessProfileEnabled, finalize, for, getConnectionPolicy, getConnectionStatus, getDrawableResource, getNameResource, getOrdinal, getProfileId, getSummaryResourceForDevice, if
- \`com/p001my/settingslib/bluetooth/BluetoothDeviceFilter.java\`: AllFilter, AudioFilter, BluetoothDeviceFilter, BondedDeviceFilter, ClassUuidFilter, NapFilter, PanuFilter, TransferFilter, UnbondedDeviceFilter, doesClassMatch, getFilter, if, matches
- \`com/p001my/settingslib/bluetooth/HfpClientProfile.java\`: HfpClientProfile, HfpClientServiceListener, accessProfileEnabled, finalize, getConnectedDevices, getConnectionPolicy, getConnectionStatus, getDrawableResource, getNameResource, getOrdinal, getProfileId, getSummaryResourceForDevice, if
- \`com/p001my/settingslib/bluetooth/LocalBluetoothProfile.java\`: accessProfileEnabled, getConnectionPolicy, getConnectionStatus, getDrawableResource, getNameResource, getOrdinal, getProfileId, getSummaryResourceForDevice, isAutoConnectable, isEnabled, isProfileReady, setEnabled
- \`com/p001my/settingslib/bluetooth/LocalBluetoothManager.java\`: LocalBluetoothManager, create, getBluetoothAdapter, getCachedDeviceManager, getContext, getEventManager, getForegroundActivity, getInstance, getProfileManager, if, isForegroundActivity, onBluetoothManagerInitialized, return
- \`com/p001my/settingslib/bluetooth/CsipDeviceManager.java\`: CsipDeviceManager, findMainDevice, for, getBaseGroupId, getCachedDevice, if, initCsipDeviceIfNeeded, isAtLeastT, isExistedGroupId, isValidGroupId, log, onGroupIdChanged, onProfileConnectionStateChangedIfProcessed
- \`com/p001my/settingslib/bluetooth/LocalBluetoothAdapter.java\`: LocalBluetoothAdapter, cancelDiscovery, disable, enable, getAddress, getBluetoothLeScanner, getBluetoothState, getBondedDevices, getConnectionState, getDiscoveryEndMillis, getInstance, getName, getProfileProxy
- \`com/p001my/settingslib/bluetooth/BluetoothCallback.java\`: onAclConnectionStateChanged, onActiveDeviceChanged, onAudioModeChanged, onBluetoothStateChanged, onConnectionStateChanged, onDeviceAdded, onDeviceBondStateChanged, onDeviceDeleted, onProfileConnectionStateChanged, onScanningStateChanged
- \`com/p001my/settingslib/bluetooth/HeadsetProfile.java\`: HeadsetProfile, HeadsetServiceListener, accessProfileEnabled, finalize, getActiveDevice, getAudioState, getConnectedDevices, getConnectionPolicy, getConnectionStatus, getDrawableResource, getNameResource, getOrdinal, getProfileId
- \`com/p001my/settingslib/bluetooth/LocalBluetoothLeBroadcastAssistant.java\`: LocalBluetoothLeBroadcastAssistant, accessProfileEnabled, addSource, buildMetadata, finalize, getAllSources, getConnectedDevices, getConnectionPolicy, getConnectionStatus, getDrawableResource, getNameResource, getOrdinal, getProfileId
- \`com/p001my/settingslib/bluetooth/HearingAidStatsLogUtils.java\`: HearingAidStatsLogUtils, getDeviceAddressToBondEntryMap, if, logHearingAidInfo, setBondEntryForDevice
- \`com/p001my/settingslib/bluetooth/PbapServerProfile.java\`: PbapServerProfile, PbapServiceListener, accessProfileEnabled, finalize, getConnectionPolicy, getConnectionStatus, getDrawableResource, getNameResource, getOrdinal, getProfileId, getSummaryResourceForDevice, if, isAutoConnectable
- \`com/p001my/settingslib/bluetooth/MapClientProfile.java\`: MapClientProfile, MapClientServiceListener, accessProfileEnabled, finalize, getConnectedDevices, getConnectionPolicy, getConnectionStatus, getDrawableResource, getNameResource, getOrdinal, getProfileId, getSummaryResourceForDevice, if
- \`com/p001my/settingslib/bluetooth/CsipSetCoordinatorProfile.java\`: CoordinatedSetServiceListener, CsipSetCoordinatorProfile, accessProfileEnabled, finalize, getConnectedDevices, getConnectionPolicy, getConnectionStatus, getDrawableResource, getGroupUuidMapByDevice, getNameResource, getOrdinal, getProfileId, getSummaryResourceForDevice
- \`com/p001my/settingslib/bluetooth/BluetoothEventManager.java\`: AclStateChangedHandler, ActiveDeviceChangedHandler, AdapterStateChangedHandler, AudioModeChangedHandler, BatteryLevelChangedHandler, BluetoothBroadcastReceiver, BluetoothEventManager, BondStateChangedHandler, ClassChangedHandler, ConnectionStateChangedHandler, DeviceFoundHandler, NameChangedHandler, ScanningStateChangedHandler
- \`com/p001my/settingslib/bluetooth/A2dpProfile.java\`: A2dpProfile, A2dpServiceListener, accessProfileEnabled, compare, finalize, for, getActiveDevice, getConnectableDevices, getConnectedDevices, getConnectionPolicy, getConnectionStatus, getDevicesByStates, getDrawableResource
- \`com/p001my/settingslib/bluetooth/LocalBluetoothLeBroadcast.java\`: BroadcastSettingsObserver, LocalBluetoothLeBroadcast, accessProfileEnabled, buildContentMetadata, finalize, for, generateRandomPassword, getAllBroadcastMetadata, getAppSourceName, getBroadcastCode, getConnectedDevices, getConnectionPolicy, getConnectionStatus
- \`com/p001my/settingslib/bluetooth/A2dpSinkProfile.java\`: A2dpSinkProfile, A2dpSinkServiceListener, accessProfileEnabled, finalize, getConnectedDevices, getConnectionPolicy, getConnectionStatus, getDrawableResource, getNameResource, getOrdinal, getProfileId, getSummaryResourceForDevice, if
- \`com/p001my/settingslib/bluetooth/LeAudioProfile.java\`: LeAudioProfile, LeAudioServiceListener, accessProfileEnabled, connect, disconnect, finalize, getActiveDevices, getAudioLocation, getConnectedDevices, getConnectedGroupLeadDevice, getConnectionPolicy, getConnectionStatus, getDrawableResource
- \`com/p001my/settingslib/bluetooth/CachedBluetoothDeviceManager.java\`: CachedBluetoothDeviceManager, addDevice, clearNonBondedDevices, clearNonBondedSubDevices, findDevice, for, getCachedDevicesCopy, getName, getSubDeviceSummary, if, isOngoingPairByCsip, isSubDevice, log
- \`com/p001my/settingslib/bluetooth/BluetoothUtils.java\`: buildAdvancedDrawable, buildBtRainbowDrawable, createBitmap, createIconWithDrawable, doesClassMatch, extraTagValue, for, generateExpressionWithTag, getBluetoothDrawable, getBooleanMetaData, getBtClassDrawableWithDescription, getBtDrawableWithDescription, getBtRainbowDrawableWithDescription
- \`com/p001my/settingslib/bluetooth/CachedBluetoothDevice.java\`: CachedBluetoothDevice, addMemberDevice, compareTo, connect, connectDevice, connectInt, connectProfile, describe, disconnect, ensurePaired, equals, fetchActiveDevices, fillData
- \`com/p001my/settingslib/bluetooth/HidProfile.java\`: HidHostServiceListener, HidProfile, accessProfileEnabled, finalize, getConnectionPolicy, getConnectionStatus, getDrawableResource, getHidClassDrawable, getNameResource, getOrdinal, getProfileId, getSummaryResourceForDevice, if
- \`com/p001my/settingslib/bluetooth/LocalBluetoothLeBroadcastMetadata.java\`: LocalBluetoothLeBroadcastMetadata, convertAudioCodecConfigToString, convertAudioContentToString, convertChannelToString, convertSubgroupToString, convertToBroadcastMetadata, convertToChannel, convertToConfigMetadata, convertToContentMetadata, convertToQrCodeString, convertToSubgroup, for, getBroadcastId
- \`com/p001my/settingslib/bluetooth/HidDeviceProfile.java\`: HidDeviceProfile, HidDeviceServiceListener, accessProfileEnabled, finalize, for, getConnectionPolicy, getConnectionStatus, getDrawableResource, getNameResource, getOrdinal, getProfileId, getSummaryResourceForDevice, if
- \`com/p001my/settingslib/bluetooth/HearingAidProfile.java\`: HearingAidProfile, HearingAidServiceListener, accessProfileEnabled, finalize, getActiveDevices, getConnectableDevices, getConnectedDevices, getConnectionPolicy, getConnectionStatus, getDeviceMode, getDeviceSide, getDevicesByStates, getDrawableResource
- \`com/p001my/settingslib/utils/ThreadUtils.java\`: ensureMainThread, getThreadExecutor, getUiThreadHandler, if, isMainThread, postOnBackgroundThread, postOnMainThread, postOnMainThreadDelayed
- \`com/p001my/settingslib/wifi/AccessPoint.java\`: AccessPoint, AccessPointProvisioningCallback, accept, clearConfig, compareTo, convertToQuotedString, equals, for, generateAverageSpeedForSsid, generateOpenNetworkConfig, getAppLabel, getBssid, getConfig
- \`com/p001my/settingslib/wifi/WifiUtils.java\`: InternetIconInjector, buildLoggingSummary, for, getIcon, getInternetIconResource, getMeteredLabel, getSpecificApSpeed, getVisibilityStatus, getWifiDetailsSettingsIntent, getWifiDialogIntent, getWifiTetherSummaryForConnectedDevices, if, isMeteredOverridden
- \`com/p001my/settingslib/wifi/WifiTracker.java\`: DBG, Multimap, Scanner, WifiListenerExecutor, WifiTracker, WifiTrackerNetworkCallback, clearAccessPointsAndConditionallyUpdate, conditionallyNotifyListeners, dump, evictOldScans, fetchScansAndConfigsAndUpdateAccessPoints, filterScanResultsByCapabilities, for
- \`com/p001my/settingslib/wifi/TimestampedScoredNetwork.java\`: TimestampedScoredNetwork, createFromParcel, describeContents, getScore, getUpdatedTimestampMillis, newArray, update, writeToParcel
- \`com/p001my/settingslib/widget/AdaptiveOutlineDrawable.java\`: AdaptiveOutlineDrawable, draw, getColor, getDefaultDisplayDensity, getDimensionPixelSize, getDrawable, getIntrinsicHeight, getIntrinsicWidth, if, init, super, switch
- \`com/p001my/settingslib/widget/AdaptiveIcon.java\`: AdaptiveConstantState, AdaptiveIcon, getChangingConfigurations, getConstantState, getDrawable, newDrawable, setBackgroundColor, setLayerInset, super, this
- \`com/p001my/settingslib/widget/AdaptiveIconShapeDrawable.java\`: AdaptiveIconShapeDrawable, inflate, init, setShape
- \`com/p001my/settingslib/wifi/wifitrackerlib/WifiEntry.java\`: ConnectActionListener, ConnectedInfo, ForgetActionListener, WifiEntry, accept, apply, canConnect, canDisconnect, canEasyConnect, canForget, canManageSubscription, canSetAutoJoinEnabled, canSetMeteredChoice
- \`com/p001my/settingslib/core/instrumentation/EventLogWriter.java\`: action, for, hidden, if, visible
- \`com/p001my/settingslib/core/instrumentation/Instrumentable.java\`: getMetricsCategory
- \`com/p001my/settingslib/core/instrumentation/MetricsFeatureProvider.java\`: MetricsFeatureProvider, action, for, getAttribution, getMetricsCategory, hidden, if, installLogWriters, logSettingsTileClick, logStartedIntent, logStartedIntentWithProfile, return, visible
- \`com/p001my/settingslib/core/instrumentation/LogWriter.java\`: action, hidden, visible
- \`com/p001my/settingslib/core/lifecycle/Lifecycle.java\`: Lifecycle, LifecycleProxy, addObserver, for, if, onAttach, onCreate, onCreateOptionsMenu, onDestroy, onLifecycleEvent, onOptionsItemSelected, onPause, onPrepareOptionsMenu
- \`com/p001my/settingslib/core/lifecycle/LifecycleObserver.java\`: 
- \`com/p001my/settingslib/core/lifecycle/events/OnStop.java\`: onStop
- \`com/p001my/settingslib/core/lifecycle/events/OnResume.java\`: onResume
- \`com/p001my/settingslib/core/lifecycle/events/OnPause.java\`: onPause
- \`com/p001my/settingslib/core/lifecycle/events/SetPreferenceScreen.java\`: setPreferenceScreen
- \`com/p001my/settingslib/core/lifecycle/events/OnDestroy.java\`: onDestroy
- \`com/p001my/settingslib/core/lifecycle/events/OnCreate.java\`: onCreate
- \`com/p001my/settingslib/core/lifecycle/events/OnSaveInstanceState.java\`: onSaveInstanceState
- \`com/p001my/settingslib/core/lifecycle/events/OnStart.java\`: onStart
- \`com/p001my/settingslib/core/lifecycle/events/OnPrepareOptionsMenu.java\`: onPrepareOptionsMenu
- \`com/p001my/settingslib/core/lifecycle/events/OnOptionsItemSelected.java\`: onOptionsItemSelected
- \`com/p001my/settingslib/core/lifecycle/events/OnCreateOptionsMenu.java\`: onCreateOptionsMenu
- \`com/p001my/settingslib/core/lifecycle/events/OnAttach.java\`: onAttach
- \`com/p001my/settings/language/LanguageLocalActivity.java\`: LocaleInfo, compareTo, constructAdapter, findUI, finish, for, getAllAssetLocales, getDatas, getDisplayName, getLabel, getLocale, getMyLocaleInfos, getView
- \`com/p001my/settings/language/LanguageUtils.java\`: getInstance, if, synchronized, updateLocale
- \`com/p001my/settings/language/LanguageActivity.java\`: enterClick, findUI, finish, for, getDatas, getLanguageBean, getLanguageData, getTypewritingDatas, if, init, initView, languageLocal, onActivityResult
- \`com/p001my/settings/language/InputMethodManagerUtils.java\`: for, getInput, getInstance, getPutLists, if, setInput, synchronized
- \`com/p001my/settings/bluetooth/MyAutoBluetoothActivityOld.java\`: askDisconnect, availableList, clickStatue, connectFinsh, enterClick, findUI, finish, flashScanDevices, for, getIrBluetoothStatus, getStatus, handleMessage, if
- \`com/p001my/settings/bluetooth/MyBlutoothAdapter.java\`: MyBlutoothAdapter, ViewHolder, for, getConnectionSummary, getCount, getItem, getItemId, getView, if, setDatas, setStatueView, switch
- \`com/p001my/settings/bluetooth/MyAutoBluetoothActivity.java\`: connectFinsh, findUI, finish, for, getIrBluetoothStatus, getStatus, handleMessage, if, init, initBtstatus, initView, isAutoName, onCreate
- \`com/p001my/settings/bluetooth/ProvisionBluetoothActivityOld.java\`: askDisconnect, availableList, clickStatue, connectFinsh, dispatchKeyEvent, enterClick, findUI, flashScanDevices, for, getIrBluetoothStatus, getStatus, handleMessage, if
- \`com/p001my/settings/bluetooth/Utils.java\`: Utils, formatPercentage, getLocalBtManager, if, isAudioModeOngoingCall, onBluetoothManagerInitialized, onShowError, showDisconnectDialog, showError
- \`com/p001my/settings/bluetooth/ProvisionBluetoothActivity.java\`: connectFinsh, dispatchKeyEvent, enterClick, findUI, for, getIrBluetoothStatus, getStatus, handleMessage, if, init, initBtstatus, initView, isAutoName
- \`com/p001my/settings/bluetooth/MyBluetoothActivity.java\`: addCachedDevices, addDevices, askDisconnect, autoBluttoth, autoScanning, availabeViewUI, availableList, bluetoothCloseDialog, clickStiwch, closeBlutooth, enterClick, enterFocus, findUI
- \`com/p001my/settings/bluetooth/MyAutoBlutoothAdapter.java\`: MyAutoBlutoothAdapter, ViewHolder, for, getConnectionSummary, getCount, getItem, getItemId, getView, if, setDatas, setStatueView, switch
- \`com/p001my/settings/utils/AndroidKeystoreAliasLoader.java\`: AndroidKeystoreAliasLoader, getCaCertAliases, getKeyCertAliases, if, while
- \`com/p001my/settings/network/SubscriptionUtil$$ExternalSyntheticLambda3.java\`: return, test
### KuSourceRJ
- \`com/trd/framework/ITrdSystemService.java\`: Proxy, Stub, asBinder, asInterface, attachInterface, getCommunicationCtrl, getDefaultImpl, getInterfaceDescriptor, getMotorCtrl, getProjectorCtrl, getSensorCtrl, getSystemCtrl, getTemperatureCtrl
- \`com/trd/sdk/ReConnectService.java\`: invoke, proxy, reconnect, return
- \`com/trd/common/enums/SourceItem.java\`: APP, AV1, HDMI1, HDMI2, HDMI3, HOME, SourceItem, for, fromInt, get, if
- \`com/trd/common/bean/TrdTvInputSourceInfo.java\`: TrdTvInputSourceInfo, createFromParcel, getExtraData, getId, getLabel, getParcel, newArray, toString, writeToParcel
- \`com/coolux/source/MainActivity.java\`: cancelFocus, enterClick, enterFocus, findUI, finish, hd2Focus, hdFocus, if, init, initView, loseFocus, onClick, onCreate
- \`com/coolux/source/C0537R.java\`: C0537R, color, dimen, drawable, id, layout, mipmap, raw, string, style, xml
- \`com/coolux/source/http/HttpTool.java\`: dealResponseResult, getInstance, getParamsEncry, httpStringPost, if, synchronized, while
- \`com/coolux/source/constant/ConstansUrl.java\`: 
- \`com/coolux/source/constant/Constans.java\`: 
- \`com/coolux/source/utils/IntentUtils.java\`: agingTest, agingVideoTest, audioVideoTest, authorizationIntent, autoApp, automaticAdmissionIntent, customBright, dlpTest, doStartApplicationWithPackageName, fanDetectionIntent, fourPointIntent, getInstance, highTemperatureIntent
- \`com/coolux/source/utils/JsonUtil.java\`: getInstance, if, isDataOk, isOk, return, synchronized, updateJson
- \`com/coolux/source/utils/WifiUtils.java\`: for, getInstance, getMacAddress, getWireMac, if, synchronized
- \`com/coolux/source/utils/VoiceUtils.java\`: closeVoices, getInstance, if, initVoices, isVoices, openVoices, synchronized
- \`com/coolux/source/utils/CodeUtils.java\`: createBarcode, createQrcode, for
- \`com/coolux/source/utils/UtilsTools.java\`: convertSecondsToTime, for, getAllAppInfos, getAllHours, getAppInfo, getAppVersionName, getInstance, getPageName, getTishiHour, getUtcTime, if, invisionSystemFiltersPak, isAllDigital
- \`com/coolux/source/utils/SharedPreferencesTest.java\`: defaultSavePerformance, getAgingAuto, getAgingKey, getAgingTest, getAgingTime, getAgingTimeString, getAudioTest, getAutoPak, getDefaultBright, getDefaultInit, getDeviceType, getDlpTest, getDustproofNum
- \`com/coolux/source/utils/ImageUtils.java\`: defaultBrightModel, defaultGlPerformanceValue, enterHdmi, getAutoFocus, getAutomaticTrapezoid, getBacklight, getBootSource, getBrightModel, getFanStatus, getHighFirstBright, getHighSecondBright, getHighThirdBright, getInstance
- \`com/coolux/source/utils/BrightUtils.java\`: getInstance, handleMessage, if, run, setBrightModel, startReduceNew, startReduceNew518, startThreadReduceNew, synchronized
- \`com/coolux/source/utils/ReflectionUtils.java\`: deviceType, for, getDeviceId, getFirmwareVersion, getHDMINum, getInstance, getModel, getSysModel, getSysOta, if, invokeStaticMethod, is618EN, isDPLSTOF
- \`com/coolux/source/utils/MyLog.java\`: m48d, m49e, m50i, m51v, m52w
- \`com/coolux/source/utils/MyTimeUtils.java\`: addMinuteSemih, getInstance, highBrightService, highSecondBrightService, highThirdBrightService, if, isReduceTime, setAgingTimeInit, standerBrightSecondService, standerBrightService, synchronized
- \`com/coolux/source/utils/ActivitysUtils.java\`: addActivity, destory, for, getInstance, if
- \`com/coolux/source/model/AppInfo.java\`: getAppIcon, getAppName, getPackname, getPkgSize, getUid, getVersion, isUserApp, setAppIcon, setAppName, setPackname, setPkgSize, setUid, setUserApp
- \`com/coolux/source/model/RsFwUpdatePackage.java\`: getCompletedLen, getDescribe, getFileSavePath, getFilename, getIsupdate, getMd5, getModelname, getSize, getStatus, getUrl, getVersion, setCompletedLen, setDescribe
- \`com/coolux/source/listenner/MyTagListenner.java\`: onTagComplete
- \`com/coolux/source/ilogic/UtilsLogicImpl.java\`: allApps, getHandler, handleMessage, if, run
- \`com/coolux/source/ilogic/SocaicalLogicImpl.java\`: getHandler, handleMessage, if, onTagComplete, postHttpUpdate, run
- \`com/coolux/source/ilogic/UtilsLogic.java\`: allApps
- \`com/coolux/source/ilogic/SocicalLogic.java\`: postHttpUpdate
- \`com/coolux/source/inter/Constant.java\`: 
- \`com/coolux/source/inter/DESEncryption.java\`: convertHexString, decodeBase64, decrypt, encodeBase64, encrypt, for, if, toHexString
- \`com/coolux/source/service/TimeService.java\`: UnsupportedOperationException, actualHighFirstBrightThread, actualHighSecondBrightThread, actualHighThirdBrightThread, actualIHighTemThread, actualStanderBrightSecondThread, actualStanderFirstBrightThread, brightReduce, defaultBright, defaultData, dustproof, fanDetection, handleMessage
- \`com/coolux/source/service/ScreensaverReceiver.java\`: if, onReceive, setScreensaverBright
- \`com/coolux/source/service/BrightReceiver.java\`: if, onReceive
- \`com/coolux/source/service/BootupReceiver.java\`: agingTest, defaultBright, defaultInit, if, onReceive, rebootStart
- \`com/coolux/source/courview/MainLayout.java\`: MainLayout, init, setClipChildren, setClipToPadding, super
- \`com/coolux/source/courview/CustomVideoView.java\`: CustomVideoView, if, onPause, onPlay, pause, setPlayPauseListener, start, super
- \`com/coolux/source/activity/MyTestActivity.java\`: findUI, init, initView, onCreate, setContentView
- \`com/coolux/source/activity/AuthorizationActivity.java\`: onCreate, onKeyDown, onPause, run, setContentView, shutDown
- \`com/coolux/source/activity/AllAppAutoActivity.java\`: findUI, finish, flashView, getAllAppsDatas, if, init, initView, onBackPressed, onCreate, onItemClick, onResume, onTagComplete, setAutoPak
- \`com/coolux/source/activity/AutomaticAdmissionActivity.java\`: admissionStart, enterClick, findUI, if, init, initView, onClick, onCreate, setContentView
- \`com/coolux/source/activity/ProjectModelActivity.java\`: enterClick, findUI, if, init, initView, onClick, onCreate, onDestroy, setContentView, setProjectModel, viewProjectMode
- \`com/coolux/source/activity/BootSoureActivityOld.java\`: clickSorce, enterClick, enterKey, findUI, getBootSource, if, init, initView, onActivityResult, onClick, onCreate, onDestroy, onKey
- \`com/coolux/source/activity/PoweronModeActivity.java\`: clickPoweronMode, enterClick, findUI, if, init, initView, onClick, onCreate, setContentView, viewPoweronMode
- \`com/coolux/source/activity/BootSoureActivity.java\`: clickSorce, enterClick, enterKey, findUI, getBootSource, if, init, initView, onActivityResult, onClick, onCreate, onDestroy, onKey
- \`com/coolux/source/activity/RestoreActivity.java\`: enterClick, findUI, getWindowManager, if, init, onClick, onCreate, onFocusChange, restoreDialog, setContentView
- \`com/coolux/source/activity/DustproofActivity.java\`: confirm, dispatchKeyEvent, findUI, if, init, initView, onCreate, setContentView
- \`com/coolux/source/activity/FanDetectionActivity.java\`: cancelTimer, findUI, getFanString, if, init, onCreate, onFinish, onKeyDown, onPause, onTick, setContentView, startTimer
- \`com/coolux/source/activity/BaseActivity.java\`: 
- \`com/coolux/source/activity/HighTemperatureActivity.java\`: cancelTimer, findUI, if, init, onCreate, onFinish, onKeyDown, onPause, onTick, setContentView, startTimer
- \`com/coolux/source/activity/KeystoneActivity.java\`: clickAutoFocus, clickAutomaticTrapezoid, clickObstacleAvoidance, clickPowerOnAutoFocus, clickPowerOnAutomaticTrapezoid, clickTofAutofocus, enterClick, enterKey, findUI, if, init, initView, isViewTofAutofocus
- \`com/coolux/source/activity/VoiceActivity.java\`: clickSound, enterClick, findUI, if, init, initView, onClick, onCreate, setContentView, viewSound
- \`com/coolux/source/activity/BrightActivity.java\`: clickScreensaverModel, enterClick, enterKey, findUI, if, init, initView, leftClick, onClick, onCreate, onKey, rightClick, setBrightModel
- \`com/coolux/source/adapter/AllAppAutoAdapter.java\`: AllAppAutoAdapter, ViewHolder, getCount, getItem, getItemId, getView, if, setDatas
- \`com/coolux/source/activity/testcast/InterfaceTestActivity.java\`: enterClick, findUI, flashView, fs0Value, fs2Value, glValue, handleMessage, if, init, initView, ledValue, onClick, onCreate
- \`com/coolux/source/activity/testcast/WiredSNActivity.java\`: barcodeString, findUI, init, initView, onCreate, setContentView
- \`com/coolux/source/activity/testcast/RebootNumActivity.java\`: findUI, if, init, initView, onClick, onCreate, rebootSet, setContentView
- \`com/coolux/source/activity/testcast/TestCastActivity.java\`: enterClick, findUI, finish, handleMessage, if, init, initView, isHysdFocus, onClick, onCreate, onDestroy, onPause, onResume
- \`com/coolux/source/activity/testcast/StandbyActivity.java\`: clickDevicetypeClose, clickDevicetypeOpen, clickHdmiNum, clickReduction, clickReductionClose, enterClick, findUI, if, init, initView, isSavePerformanceClose, isSavePerformanceOpen, isTimereduceBrightClose
- \`com/coolux/source/activity/testcast/AudioVideoActivity.java\`: findUI, finish, handleMessage, if, init, initPlayer, initView, isPass, onCompletion, onCreate, onDestroy, onError, onPause
- \`com/coolux/source/activity/testcast/WifiSNActivity.java\`: barcodeString, findUI, init, initView, onCreate, setContentView
- \`com/coolux/source/activity/testcast/PrintTestActivity.java\`: enterClick, findUI, finish, if, init, initView, onClick, onCreate, setContentView, stringView, uninstall
- \`com/coolux/source/activity/testcast/OtherActivity.java\`: clearAction, enterClick, findUI, if, init, initView, myAction, onClick, onCreate, onKey, onPause, onResume, setContentView
- \`com/coolux/source/activity/testcast/AgingActivity.java\`: dispatchKeyEvent, exit, findUI, finish, getCurrentBright, handleMessage, homeReceiverRegister, if, init, initPlayer, initView, onCompletion, onCreate
- \`com/coolux/source/activity/testcast/DlpActivity.java\`: dispatchKeyEvent, findUI, if, init, initView, onCreate, onDestroy, onPause, setContentView, viewImage
- \`com/coolux/source/activity/testcast/SetTimeActivity.java\`: findUI, if, init, initView, onClick, onCreate, setContentView, setTime
- \`com/coolux/source/activity/testcast/LeftRightVideoActivity.java\`: findUI, if, init, initPlayer, initView, onCompletion, onCreate, onDestroy, onError, onPause, onPlay, onPrepared, onResume
- \`com/coolux/source/activity/testcast/BrightTestActivity.java\`: enterClick, findUI, if, init, initView, onClick, onCreate, setContentView
- \`com/coolux/source/activity/testcast/InterfaceActivity.java\`: enterClick, findUI, finish, if, init, initView, jump, myHttp, onClick, onCreate, onDestroy, onReceive, onTagComplete
- \`com/coolux/source/activity/testcast/CustomBrightActivity.java\`: defaultBright, defaultBrightDialog, enterClick, enterKey, findUI, getWindowManager, if, init, initView, leftEnergyClick, leftHighlightClick, leftStandardClick, onClick
- \`com/coolux/source/activity/testcast/ClearTimeActivity.java\`: clear, clearSuccess, clearTime, findUI, handleMessage, if, init, initView, onClick, onCreate, setContentView, viewTime
- \`com/coolux/source/activity/testcast/AgingModelActivity.java\`: agingKeyModel, agingModel, enterClick, findUI, if, init, initView, onClick, onCreate, setContentView, viewAgingAuto, viewAgingKey
### TvSettingsPlus

## Safety gates

1. Identify exact broadcasts, pending intents, boot receiver/service lifecycle, and references from other APKs before disabling KuSettings.
2. Preserve KuSourceRJ, TRD services, focus/geometry, HDMI input routing, and SetupWizard.
3. Confirm TvSettingsPlus original platform certificate; do not infer it from OTA testkey.
4. Never write raw I2C or invoke motor APIs until vendor semantics and stops are validated.
5. Modify existing TvSettingsPlus package; compare resource maps and compile before APK signing.
6. First firmware test should validate navigation/UI and service references; require rollback procedure.

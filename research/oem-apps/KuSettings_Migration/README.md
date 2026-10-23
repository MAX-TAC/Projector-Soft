# KuSettings migration audit (8052)

Based on JADX + manifest inspection of original APKs, not dynamic execution.
This is a **dependency inventory**, not proof that KuSettings can be disabled.

## Exported / background components

### TvSettingsPlus
Decompiled Java files: 5796

| Type | Name | Exported | Permission |
|---|---|---|---|
| provider | androidx.startup.InitializationProvider | false |  |
| activity | com.android.tv.settings.partnercustomizer.timer.TimerActivity | true |  |
| activity | com.android.tv.settings.partnercustomizer.retailmode.RetailModeConfirmActivity | false |  |
| activity | com.android.tv.settings.partnercustomizer.EnergyCommonActivity | true |  |
| activity | com.android.tv.settings.partnercustomizer.sound.AudioResetActivity | false |  |
| activity | com.android.tv.settings.partnercustomizer.picture.PictureResetActivity | false |  |
| activity | com.android.tv.settings.partnercustomizer.inputs.InputsActivity | false |  |
| activity | com.android.tv.settings.partnercustomizer.power.PowerActivity | true |  |
| activity | com.android.tv.settings.partnercustomizer.power.PictureOffActivity | false |  |
| activity | com.android.tv.settings.partnercustomizer.vendor.channels.ChannelsActivity | true |  |
| activity | com.android.tv.settings.partnercustomizer.vendor.channels.DiseqcTypeActivity | true |  |
| activity | com.android.tv.settings.partnercustomizer.vendor.channels.ChannelsTwoPanalActivity | false |  |
| activity | com.android.tv.settings.partnercustomizer.vendor.channels.ChannelConfirmActivity | true |  |
| activity | com.android.tv.settings.partnercustomizer.retailmode.RetailModeActivity | false |  |
| activity | com.android.tv.settings.device.sound.SoundActivity | true |  |
| activity | com.android.tv.settings.partnercustomizer.picture.PictureActivity | true |  |
| activity | com.android.tv.settings.partnercustomizer.timer.TimePickerActivity | false |  |
| activity | com.android.tv.settings.partnercustomizer.multicast.NormalCastActivity | false |  |
| activity | com.android.tv.settings.partnercustomizer.picture.DisplayModeActivity | true |  |
| activity | com.android.tv.settings.device.sound.AudioOutputActivity | true |  |
| activity | com.android.tv.settings.partnercustomizer.utils.ProgressDialogActivity | false |  |
| activity | com.android.tv.settings.partnercustomizer.picture.PictureConfirmActivity | false |  |
| activity | com.android.tv.settings.partnercustomizer.vendor.ResetActivity | true |  |
| activity | com.android.tv.settings.partnercustomizer.timer.TimerSetActivity | true |  |
| activity | com.android.tv.settings.partnercustomizer.date.DateTimeActivity | false |  |
| activity | com.android.tv.settings.partnercustomizer.DisplayAndSoundActivity | true |  |
| activity | com.android.tv.settings.partnercustomizer.sound.wireless.WirelessAddNoteActivity | false |  |
| activity | com.android.tv.settings.partnercustomizer.sound.wireless.WirelessNoteCommonActivity | false |  |
| activity | com.android.tv.settings.partnercustomizer.sound.wireless.SpeakerDevicesBeyondActivity | false |  |
| activity | com.android.tv.settings.partnercustomizer.sound.wireless.SpeakerDeviceBeyondTipsActivity | false |  |
| activity | com.android.tv.settings.partnercustomizer.sound.wireless.ResetSpeakerActivity | false |  |
| activity | com.android.tv.settings.partnercustomizer.sound.wireless.SelectListActivity | false |  |
| activity | com.android.tv.settings.partnercustomizer.sound.wireless.StatelliteSpeakerLayoutActivity | false |  |
| activity | com.android.tv.settings.partnercustomizer.sound.wireless.StatelliteSpeakerLayoutChangeTipsActivity | false |  |
| receiver | com.android.tv.settings.partnercustomizer.network.NetworkSliceBroadcastReceiver | false |  |
| provider | com.android.tv.settings.partnercustomizer.TvSettingsSliceProvider | true |  |
| provider | com.android.tv.settings.partnercustomizer.CustomizedContentProvider | true |  |
| service | com.android.tv.settings.partnercustomizer.tvsettingservice.BluetoothReconnectService | true |  |
| service | com.android.tv.settings.partnercustomizer.switchofftimer.SwitchOffTimerService | true |  |
| service | com.android.tv.settings.partnercustomizer.tvsettingservice.WirelessSpeakerServices | true |  |
| receiver | com.android.tv.settings.partnercustomizer.utils.SoundEARCReceiver | true |  |
| receiver | com.android.tv.settings.partnercustomizer.tvsettingservice.TvBootupReceiver | true |  |
| receiver | com.android.tv.settings.partnercustomizer.reeciver.LanguageChangeReceiver | false |  |
| activity | com.android.tv.settings.uicustom.MainActivity | true |  |
| activity | com.android.tv.settings.uicustom.fragment.projector.KeystoneCalibrationActivity | true |  |
| activity | com.android.tv.settings.uicustom.floatmenu.MenuJumpActivity |  |  |
| service | com.android.tv.settings.uicustom.floatmenu.FloatMenuService | true |  |
| activity | com.android.tv.settings.MainSettings | true |  |
| activity | com.android.tv.settings.connectivity.setup.WifiSetupActivity | true |  |
| activity | com.android.tv.settings.connectivity.NetworkRequestDialogActivity | true | android.permission.NETWORK_SETTINGS |
| activity | com.android.tv.settings.connectivity.WifiConnectionActivity | true | android.permission.NETWORK_SETTINGS |
| activity | com.android.tv.settings.connectivity.EditProxySettingsActivity | true |  |
| activity | com.android.tv.settings.connectivity.EditIpSettingsActivity | true |  |
| activity | com.android.tv.settings.connectivity.AddWifiNetworkActivity | true |  |
| activity | com.android.tv.settings.accounts.AccountSliceActivity | false |  |
| activity | com.android.tv.settings.accounts.AccountsActivity | false |  |
| activity | com.android.tv.settings.accounts.AccountsTrampolineActivity | false |  |
| activity-alias | com.google.android.tv.settings.AccountActivity | true |  |
| activity | com.android.tv.settings.accounts.AccountSyncActivity | true |  |
| activity | com.android.tv.settings.accounts.ManageUserServicesSliceActivity | false |  |
| activity-alias | com.google.android.tv.settings.ManageUserServicesActivity | true |  |
| activity | com.android.tv.settings.accounts.RemoveAccountDialog | true |  |
| activity | com.android.tv.settings.accounts.AddAccountWithTypeActivity | true |  |
| activity | com.android.tv.settings.device.apps.AppsActivity | true |  |
| activity | com.android.tv.settings.device.apps.AllAppsActivity | true |  |
| activity | com.android.tv.settings.device.apps.specialaccess.AllFilesAccessActivity | true |  |
| activity | com.android.tv.settings.device.apps.specialaccess.AppUsageAccessActivity | true |  |
| activity | com.android.tv.settings.device.apps.specialaccess.PictureInPictureActivity | true |  |
| activity | com.android.tv.settings.device.apps.specialaccess.SystemAlertActivity | true |  |
| activity | com.android.tv.settings.device.apps.specialaccess.AlarmsAndRemindersActivity | true |  |
| activity | com.android.tv.settings.device.apps.AppManagementActivity | true |  |
| activity | com.android.tv.settings.device.StorageResetActivity | true |  |
| activity | com.android.tv.settings.device.storage.ResetActivity |  |  |
| activity | com.android.tv.settings.connectivity.NetworkActivity | true |  |
| activity-alias | com.android.tv.settings.oemlink.NetworkSettingsActivity | true |  |
| activity | com.android.tv.settings.system.SecurityActivity | true |  |
| activity-alias | com.android.tv.settings.users.RestrictedProfileActivityLauncherEntry | true |  |
| activity | com.android.tv.settings.device.displaysound.DisplaySoundActivity | true |  |
| activity | com.android.tv.settings.device.display.ambient.AmbientSliceActivity | false |  |
| activity-alias | com.google.android.tv.settings.AmbientActivity | true |  |
| activity | com.android.tv.settings.device.display.daydream.DaydreamActivity | true |  |
| activity | com.android.tv.settings.device.display.daydream.DaydreamVoiceAction | true |  |
| activity | com.android.tv.settings.system.CaptionSetupActivity | true |  |
| activity-alias | com.android.tv.settings.oemlink.AccessibilityCaptionActivity | true |  |
| activity | com.android.tv.settings.system.DateTimeActivity | true |  |
| activity | com.android.tv.settings.system.LocationActivity | true |  |
| activity | com.android.tv.settings.system.TextToSpeechActivity | true |  |
| activity | com.android.tv.settings.system.development.DevelopmentActivity | true |  |
| activity | com.android.tv.settings.system.development.AppPicker | false |  |
| activity | com.android.tv.settings.inputmethod.KeyboardActivity | true |  |
| activity | com.android.tv.settings.autofill.AutofillPickerActivity | false |  |
| activity | com.android.tv.settings.autofill.AutofillPickerTrampolineActivity | true |  |
| activity | com.android.tv.settings.system.AppLanguageActivity | true |  |
| activity | com.android.tv.settings.system.LanguageActivity | true |  |
| activity | com.android.tv.settings.system.InputsActivity | true |  |
| activity | com.android.tv.settings.accessories.BluetoothAccessoryActivity | true | android.permission.BLUETOOTH_CONNECT |
| activity | com.android.tv.settings.accessories.AddAccessoryActivity | true | android.permission.BLUETOOTH_CONNECT |
| receiver | com.android.tv.settings.accessories.BluetoothPairingRequest | true |  |
| activity | com.android.tv.settings.accessories.BluetoothPairingDialog | true | android.permission.BLUETOOTH_PRIVILEGED |
| activity | com.android.tv.settings.accessories.BluetoothDevicePickerActivity | true |  |
| activity | com.android.tv.settings.about.AboutActivity | true |  |
| activity | com.android.tv.settings.about.StatusActivity | true |  |
| activity | com.android.tv.settings.about.LicenseActivity | true |  |
| activity | com.android.tv.settings.name.DeviceNameSettingsActivity | true |  |
| activity | com.android.tv.settings.name.DeviceNameSuggestionActivity | true |  |
| activity | com.android.tv.settings.name.setup.DeviceNameFlowStartActivity | true |  |
| service | com.android.tv.settings.users.UserSwitchListenerService | false |  |
| receiver | com.android.tv.settings.users.UserSwitchListenerService$BootReceiver | true |  |
| receiver | com.android.tv.settings.device.storage.DiskReceiver | true |  |
| service | com.android.tv.settings.users.RestrictedProfilePinService | false |  |
| service | com.android.tv.settings.device.storage.SettingsStorageService$Impl | false |  |
| activity | com.android.tv.settings.device.storage.NewStorageActivity | false |  |
| activity | com.android.tv.settings.device.storage.UnmountActivity | false |  |
| activity | com.android.tv.settings.device.storage.FormatActivity | false |  |
| activity | com.android.tv.settings.device.storage.MigrateStorageActivity | false |  |
| activity | com.android.tv.settings.device.apps.MoveAppActivity | false |  |
| activity | com.android.tv.settings.accessories.BluetoothRequestPermissionActivity | true | android.permission.BLUETOOTH |
| activity | com.android.tv.settings.system.FallbackHome | true |  |
| activity | com.android.tv.settings.inputmethod.InputMethodAndSubtypeEnablerActivity | true |  |
| activity | com.android.tv.settings.device.apps.specialaccess.ExternalSourcesActivity | true |  |
| activity | com.android.tv.settings.privacy.NotificationAccessActivity | true |  |
| activity | com.android.tv.settings.privacy.SensorToggleActivity | true |  |
| activity | com.android.tv.settings.EmptyStubActivity | true |  |
| provider | androidx.core.content.FileProvider | false |  |
| provider | com.android.tv.settings.name.SuggestionContentProvider | true |  |
| activity | com.android.tv.settings.deviceadmin.ProfileOwnerAdd | true |  |
| activity | com.android.tv.settings.ActionDisabledByAdminDialog | true |  |
| activity | com.android.tv.settings.deviceadmin.DeviceAdminAdd | true |  |
| activity | com.android.tv.settings.slice.SliceActivity | true |  |
| activity | com.android.tv.settings.oemlink.AccessibilitySettingsActivity | true |  |
| activity | com.android.tv.settings.oemlink.AccessibilityServiceActivity | true |  |
| activity | com.android.tv.settings.oemlink.FactoryResetActivity | true |  |
| activity | com.android.tv.settings.vendor.VendorActivity | true |  |
| receiver | com.android.tv.settings.DeviceSettingBroadcastReceiver | true | android.permission.READ_PRIVILEGED_PHONE_STATE |
| activity | com.android.tv.settings.accessories.BluetoothActionActivity | false |  |
| service | com.android.tv.settings.accessories.BluetoothDevicesService | false |  |
| provider | com.android.tv.settings.accessories.ConnectedDevicesSliceProvider | true |  |
| receiver | com.android.tv.settings.accessories.ConnectedDevicesSliceBroadcastReceiver | false |  |
| receiver | com.android.tv.settings.BootReceiver | true |  |
| activity | com.android.tv.settings.RemoteBugreportActivity | true | android.permission.DUMP |
| activity | com.android.tv.settings.enterprise.EnterprisePrivacySettingsActivity | true |  |
| activity | com.android.tv.settings.device.apps.ConfirmationActivity | true |  |
| receiver | com.android.tv.settings.GlobalKeyReceiver | true |  |
| activity | com.android.settingslib.users.AvatarPickerActivity |  |  |
| receiver | androidx.remotecallback.ProviderRelayReceiver |  |  |
| activity | androidx.slice.compat.SlicePermissionActivity |  |  |

### KuSettings
Decompiled Java files: 3470

| Type | Name | Exported | Permission |
|---|---|---|---|
| activity | com.my.settings.SettingActivity | true |  |
| activity | com.my.settings.wifi.MyWifiActivity | true |  |
| activity | com.my.settings.bluetooth.MyBluetoothActivity | true |  |
| activity | com.my.settings.bluetooth.MyAutoBluetoothActivity | true |  |
| activity | com.my.settings.language.LanguageActivity | true |  |
| activity | com.my.settings.language.LanguageLocalActivity | true |  |
| activity | com.my.settings.time.DateTimeActivity | true |  |
| activity | com.my.settings.time.MyZonePicker |  |  |
| activity | com.my.settings.activity.ScreenshotActivity | true |  |
| service | com.my.service.ScreenServices |  |  |
| activity | com.my.settings.activity.ProjectionActivity | true |  |
| activity | com.my.settings.activity.AllAppActivity | true |  |
| receiver | com.my.service.BootupReceiver | true |  |
| service | com.my.service.MySetTimeService |  |  |
| provider | androidx.startup.InitializationProvider | false |  |
| receiver | androidx.profileinstaller.ProfileInstallReceiver | true | android.permission.DUMP |

### KuSourceRJ
Decompiled Java files: 3007

| Type | Name | Exported | Permission |
|---|---|---|---|
| activity | com.coolux.source.MainActivity | true |  |
| activity | com.coolux.source.activity.BootSoureActivity | true |  |
| activity | com.coolux.source.activity.BrightActivity | true |  |
| activity | com.coolux.source.activity.VoiceActivity | true |  |
| activity | com.coolux.source.activity.RestoreActivity | true |  |
| activity | com.coolux.source.activity.HighTemperatureActivity | true |  |
| activity | com.coolux.source.activity.FanDetectionActivity | true |  |
| activity | com.coolux.source.activity.AllAppAutoActivity | true |  |
| activity | com.coolux.source.activity.AuthorizationActivity | true |  |
| activity | com.coolux.source.activity.DustproofActivity | true |  |
| receiver | com.coolux.source.service.BootupReceiver | true |  |
| receiver | com.coolux.source.service.ScreensaverReceiver | true |  |
| receiver | com.coolux.source.service.BrightReceiver | true |  |
| service | com.coolux.source.service.TimeService |  |  |
| activity | com.coolux.source.activity.PoweronModeActivity | true |  |
| activity | com.coolux.source.activity.ProjectModelActivity | true |  |
| activity | com.coolux.source.activity.KeystoneActivity | true |  |
| activity | com.coolux.source.activity.AutomaticAdmissionActivity | true |  |
| activity | com.coolux.source.activity.MyTestActivity | true |  |
| activity | com.coolux.source.activity.testcast.TestCastActivity | true |  |
| activity | com.coolux.source.activity.testcast.InterfaceTestActivity | true |  |
| activity | com.coolux.source.activity.testcast.StandbyActivity | true |  |
| activity | com.coolux.source.activity.testcast.DlpActivity | true |  |
| activity | com.coolux.source.activity.testcast.InterfaceActivity | true |  |
| activity | com.coolux.source.activity.testcast.BrightTestActivity | true |  |
| activity | com.coolux.source.activity.testcast.AudioVideoActivity | true |  |
| activity | com.coolux.source.activity.testcast.LeftRightVideoActivity | true |  |
| activity | com.coolux.source.activity.testcast.AgingModelActivity | true |  |
| activity | com.coolux.source.activity.testcast.AgingActivity | true |  |
| activity | com.coolux.source.activity.testcast.RebootNumActivity | true |  |
| activity | com.coolux.source.activity.testcast.PrintTestActivity | true |  |
| activity | com.coolux.source.activity.testcast.CustomBrightActivity | true |  |
| activity | com.coolux.source.activity.testcast.OtherActivity | true |  |
| activity | com.coolux.source.activity.testcast.ClearTimeActivity | true |  |
| activity | com.coolux.source.activity.testcast.SetTimeActivity | true |  |
| activity | com.coolux.source.activity.testcast.WifiSNActivity | true |  |
| activity | com.coolux.source.activity.testcast.WiredSNActivity | true |  |
| provider | androidx.startup.InitializationProvider | false |  |

### RuiAutoFocus
Decompiled Java files: 1091

| Type | Name | Exported | Permission |
|---|---|---|---|
| service | com.hysd.client.VAF2S | true |  |

### KeystoneCorrection
Decompiled Java files: 449

| Type | Name | Exported | Permission |
|---|---|---|---|
| receiver | com.rjpj.tcorrection.TCorrectionBroadcastReceiver | true |  |

### trdservice
Decompiled Java files: 438

| Type | Name | Exported | Permission |
|---|---|---|---|
| service | com.trd.service.TrdService | true |  |
| receiver | com.trd.service.system.shutdown.PreShutdownReceiver | true |  |

### trdmiddle
Decompiled Java files: 568

| Type | Name | Exported | Permission |
|---|---|---|---|
| service | com.trd.framework.TrdSystemService | true |  |

### RuiYunOta
Decompiled Java files: 75

| Type | Name | Exported | Permission |
|---|---|---|---|
| service | .OTADownloadService |  |  |
| service | .OTACheckService |  |  |

## Cross-package references (possible links, not confirmed runtime calls)
| Caller | Reference | File | Occurrences |
|---|---|---|---:|
| TvSettingsPlus | KuSourceRJ package | \`com/android/tv/settings/uicustom/fragment/general/GeneralFragment.java\` | 2 |
| TvSettingsPlus | KuSourceRJ package | \`com/android/tv/settings/uicustom/floatmenu/controller/general/GeneralSubMenuController.java\` | 2 |
| TvSettingsPlus | KuSourceRJ package | \`com/android/tv/settings/uicustom/floatmenu/controller/projector/KeystoneSettingsSecondaryController.java\` | 2 |

## OEM API candidate files

### KuSettings
- \`com/my/settingslib/RestrictedLockImageSpan.java\`: RestrictedLockImageSpan, draw, getDrawable, getSize, return, super
- \`com/my/settingslib/DeviceInfoUtils.java\`: for, formatKernelVersion, getBidiFormattedPhoneNumber, getFeedbackReporterPackage, getFormattedKernelVersion, getFormattedPhoneNumber, getFormattedPhoneNumbers, getMsvSuffix, getSecurityPatch, if, readLine
- \`com/my/settingslib/Utils.java\`: formatPercentage, getSingleSecurityTypeFromMultipleSecurityTypes, if, isAudioModeOngoingCall, isWifiOnly
- \`com/my/settingslib/RestrictedLockUtilsInternal.java\`: Proxy, checkForLockSetting, checkIfAccessibilityServiceDisallowed, checkIfApplicationIsSuspended, checkIfKeyguardFeaturesDisabled, checkIfMaximumTimeToLockIsSet, checkIfRemoteContactSearchDisallowed, checkIfRestrictionEnforced, checkIfUninstallBlocked, findEnforcedAdmin, for, getDeviceOwner, getManagedProfileId
- \`com/my/settingslib/WirelessUtils.java\`: if, isAirplaneModeOn, isRadioAllowed
- \`com/my/settingslib/RestrictedLockUtils.java\`: EnforcedAdmin, combine, createDefaultEnforcedAdminWithRestriction, equals, getDeviceOwner, getProfileOrDeviceOwner, getShowAdminSupportDetailsIntent, hashCode, if, isCurrentUserOrProfile, sendShowAdminSupportDetailsIntent, toString
- \`com/my/utils/ActivitysUtils.java\`: addActivity, destory, for, getInstance, if
- \`com/my/settings/LinkifyUtils.java\`: LinkifyUtils, if, linkify, onClick, updateDrawState
- \`com/my/settings/AirplaneModeEnabler.java\`: AirplaneModeEnabler, if, isAirplaneModeOn, onAirplaneModeChanged, onChanged, onRadioPowerStateChanged, setAirplaneModeInECM, setAirplaneModeOn, start, stop
- \`com/my/settings/MainActivity.java\`: onCreate, setContentView
- \`com/my/settings/ProxySelector.java\`: getDialogMetricsCategory, getMetricsCategory, if, initView, onClick, onCreateDialog, onCreateView, onFocusChange, switch, validate
- \`com/my/settings/DialogCreatable.java\`: getDialogMetricsCategory, onCreateDialog
- \`com/my/settings/WirelessSettings.java\`: if, isRadioAllowed
- \`com/my/settings/BaseActivity.java\`: 
- \`com/my/settings/Utils.java\`: confirmWorkProfileCredentials, convertCornerRadiusBitmap, createAccessibleSequence, createBitmap, createIconWithDrawable, createLocaleFromString, createPackageContextAsUser, enforceSameOwner, enforceSystemUser, for, forceCustomPadding, formatDateRange, formatIpAddresses
- \`com/my/settings/SettingActivity.java\`: enterClick, findUI, if, init, initView, isUpdataOtaRegister, isViewRemote, onClick, onCreate, onDestroy, onPause, onReceive, onResume
- \`com/my/service/BootupReceiver.java\`: if, onReceive
- \`com/my/service/MySetTimeService.java\`: if, onBind, onReceive, onStartCommand, registerReceiver, registerTimeTick
- \`com/my/service/ScreenServices.java\`: bindService, handleMessage, if, onBind, onServiceConnected, onServiceDisconnected, onStartCommand, run, setKeyStore, startShot, switch, synchronized, takeScreenShot
- \`com/my/settingslib/bluetooth/HearingAidDeviceManager.java\`: HearingAidDeviceManager, findMainDevice, for, getCachedDevice, getDeviceMode, getDeviceSide, getHiSyncId, if, initHearingAidDeviceIfNeeded, isValidHiSyncId, log, onHiSyncIdChanged, onProfileConnectionStateChangedIfProcessed
- \`com/my/settingslib/bluetooth/MapProfile.java\`: MapProfile, MapServiceListener, accessProfileEnabled, finalize, getConnectedDevices, getConnectionPolicy, getConnectionStatus, getDrawableResource, getNameResource, getOrdinal, getProfileId, getSummaryResourceForDevice, if
- \`com/my/settingslib/bluetooth/OppProfile.java\`: OppProfile, accessProfileEnabled, getConnectionPolicy, getConnectionStatus, getDrawableResource, getNameResource, getOrdinal, getProfileId, getSummaryResourceForDevice, isAutoConnectable, isEnabled, isProfileReady, setEnabled
- \`com/my/settingslib/bluetooth/VolumeControlProfile.java\`: VolumeControlProfile, VolumeControlProfileServiceListener, accessProfileEnabled, getConnectedDevices, getConnectionPolicy, getConnectionStatus, getDrawableResource, getNameResource, getOrdinal, getProfileId, getSummaryResourceForDevice, if, isAutoConnectable
- \`com/my/settingslib/bluetooth/BluetoothBroadcastUtils.java\`: 
- \`com/my/settingslib/bluetooth/PbapClientProfile.java\`: PbapClientProfile, PbapClientServiceListener, accessProfileEnabled, finalize, for, getConnectedDevices, getConnectionPolicy, getConnectionStatus, getDrawableResource, getNameResource, getOrdinal, getProfileId, getSummaryResourceForDevice
- \`com/my/settingslib/bluetooth/SapProfile.java\`: SapProfile, SapServiceListener, accessProfileEnabled, finalize, getConnectedDevices, getConnectionPolicy, getConnectionStatus, getDrawableResource, getNameResource, getOrdinal, getProfileId, getSummaryResourceForDevice, if
- \`com/my/settingslib/bluetooth/LocalBluetoothProfileManager.java\`: HeadsetStateChangeHandler, LocalBluetoothProfileManager, PanStateChangedHandler, StateChangedHandler, addHeadsetProfile, addPanProfile, addProfile, addServiceListener, callServiceConnectedListeners, callServiceDisconnectedListeners, for, getA2dpProfile, getA2dpSinkProfile
- \`com/my/settingslib/bluetooth/PanProfile.java\`: PanProfile, PanServiceListener, accessProfileEnabled, finalize, for, getConnectionPolicy, getConnectionStatus, getDrawableResource, getNameResource, getOrdinal, getProfileId, getSummaryResourceForDevice, if
- \`com/my/settingslib/bluetooth/BluetoothDeviceFilter.java\`: AllFilter, AudioFilter, BluetoothDeviceFilter, BondedDeviceFilter, ClassUuidFilter, NapFilter, PanuFilter, TransferFilter, UnbondedDeviceFilter, doesClassMatch, getFilter, if, matches
- \`com/my/settingslib/bluetooth/HfpClientProfile.java\`: HfpClientProfile, HfpClientServiceListener, accessProfileEnabled, finalize, getConnectedDevices, getConnectionPolicy, getConnectionStatus, getDrawableResource, getNameResource, getOrdinal, getProfileId, getSummaryResourceForDevice, if
- \`com/my/settingslib/bluetooth/LocalBluetoothProfile.java\`: accessProfileEnabled, getConnectionPolicy, getConnectionStatus, getDrawableResource, getNameResource, getOrdinal, getProfileId, getSummaryResourceForDevice, isAutoConnectable, isEnabled, isProfileReady, setEnabled
- \`com/my/settingslib/bluetooth/LocalBluetoothManager.java\`: LocalBluetoothManager, create, getBluetoothAdapter, getCachedDeviceManager, getContext, getEventManager, getForegroundActivity, getInstance, getProfileManager, if, isForegroundActivity, onBluetoothManagerInitialized, return
- \`com/my/settingslib/bluetooth/CsipDeviceManager.java\`: CsipDeviceManager, findMainDevice, for, getBaseGroupId, getCachedDevice, if, initCsipDeviceIfNeeded, isAtLeastT, isExistedGroupId, isValidGroupId, log, onGroupIdChanged, onProfileConnectionStateChangedIfProcessed
- \`com/my/settingslib/bluetooth/LocalBluetoothAdapter.java\`: LocalBluetoothAdapter, cancelDiscovery, disable, enable, getAddress, getBluetoothLeScanner, getBluetoothState, getBondedDevices, getConnectionState, getDiscoveryEndMillis, getInstance, getName, getProfileProxy
- \`com/my/settingslib/bluetooth/BluetoothCallback.java\`: onAclConnectionStateChanged, onActiveDeviceChanged, onAudioModeChanged, onBluetoothStateChanged, onConnectionStateChanged, onDeviceAdded, onDeviceBondStateChanged, onDeviceDeleted, onProfileConnectionStateChanged, onScanningStateChanged
- \`com/my/settingslib/bluetooth/HeadsetProfile.java\`: HeadsetProfile, HeadsetServiceListener, accessProfileEnabled, finalize, getActiveDevice, getAudioState, getConnectedDevices, getConnectionPolicy, getConnectionStatus, getDrawableResource, getNameResource, getOrdinal, getProfileId
- \`com/my/settingslib/bluetooth/LocalBluetoothLeBroadcastAssistant.java\`: LocalBluetoothLeBroadcastAssistant, accessProfileEnabled, addSource, buildMetadata, finalize, getAllSources, getConnectedDevices, getConnectionPolicy, getConnectionStatus, getDrawableResource, getNameResource, getOrdinal, getProfileId
- \`com/my/settingslib/bluetooth/HearingAidStatsLogUtils.java\`: HearingAidStatsLogUtils, getDeviceAddressToBondEntryMap, if, logHearingAidInfo, setBondEntryForDevice
- \`com/my/settingslib/bluetooth/PbapServerProfile.java\`: PbapServerProfile, PbapServiceListener, accessProfileEnabled, finalize, getConnectionPolicy, getConnectionStatus, getDrawableResource, getNameResource, getOrdinal, getProfileId, getSummaryResourceForDevice, if, isAutoConnectable
- \`com/my/settingslib/bluetooth/MapClientProfile.java\`: MapClientProfile, MapClientServiceListener, accessProfileEnabled, finalize, getConnectedDevices, getConnectionPolicy, getConnectionStatus, getDrawableResource, getNameResource, getOrdinal, getProfileId, getSummaryResourceForDevice, if
- \`com/my/settingslib/bluetooth/CsipSetCoordinatorProfile.java\`: CoordinatedSetServiceListener, CsipSetCoordinatorProfile, accessProfileEnabled, finalize, getConnectedDevices, getConnectionPolicy, getConnectionStatus, getDrawableResource, getGroupUuidMapByDevice, getNameResource, getOrdinal, getProfileId, getSummaryResourceForDevice
- \`com/my/settingslib/bluetooth/BluetoothEventManager.java\`: AclStateChangedHandler, ActiveDeviceChangedHandler, AdapterStateChangedHandler, AudioModeChangedHandler, BatteryLevelChangedHandler, BluetoothBroadcastReceiver, BluetoothEventManager, BondStateChangedHandler, ClassChangedHandler, ConnectionStateChangedHandler, DeviceFoundHandler, NameChangedHandler, ScanningStateChangedHandler
- \`com/my/settingslib/bluetooth/A2dpProfile.java\`: A2dpProfile, A2dpServiceListener, accessProfileEnabled, compare, finalize, for, getActiveDevice, getConnectableDevices, getConnectedDevices, getConnectionPolicy, getConnectionStatus, getDevicesByStates, getDrawableResource
- \`com/my/settingslib/bluetooth/LocalBluetoothLeBroadcast.java\`: BroadcastSettingsObserver, LocalBluetoothLeBroadcast, accessProfileEnabled, buildContentMetadata, finalize, for, generateRandomPassword, getAllBroadcastMetadata, getAppSourceName, getBroadcastCode, getConnectedDevices, getConnectionPolicy, getConnectionStatus
- \`com/my/settingslib/bluetooth/A2dpSinkProfile.java\`: A2dpSinkProfile, A2dpSinkServiceListener, accessProfileEnabled, finalize, getConnectedDevices, getConnectionPolicy, getConnectionStatus, getDrawableResource, getNameResource, getOrdinal, getProfileId, getSummaryResourceForDevice, if
- \`com/my/settingslib/bluetooth/LeAudioProfile.java\`: LeAudioProfile, LeAudioServiceListener, accessProfileEnabled, connect, disconnect, finalize, getActiveDevices, getAudioLocation, getConnectedDevices, getConnectedGroupLeadDevice, getConnectionPolicy, getConnectionStatus, getDrawableResource
- \`com/my/settingslib/bluetooth/CachedBluetoothDeviceManager.java\`: CachedBluetoothDeviceManager, addDevice, clearNonBondedDevices, clearNonBondedSubDevices, findDevice, for, getCachedDevicesCopy, getName, getSubDeviceSummary, if, isOngoingPairByCsip, isSubDevice, log
- \`com/my/settingslib/bluetooth/BluetoothUtils.java\`: buildAdvancedDrawable, buildBtRainbowDrawable, createBitmap, createIconWithDrawable, doesClassMatch, extraTagValue, for, generateExpressionWithTag, getBluetoothDrawable, getBooleanMetaData, getBtClassDrawableWithDescription, getBtDrawableWithDescription, getBtRainbowDrawableWithDescription
- \`com/my/settingslib/bluetooth/CachedBluetoothDevice.java\`: CachedBluetoothDevice, addMemberDevice, compareTo, connect, connectDevice, connectInt, connectProfile, describe, disconnect, ensurePaired, equals, fetchActiveDevices, fillData
- \`com/my/settingslib/bluetooth/HidProfile.java\`: HidHostServiceListener, HidProfile, accessProfileEnabled, finalize, getConnectionPolicy, getConnectionStatus, getDrawableResource, getHidClassDrawable, getNameResource, getOrdinal, getProfileId, getSummaryResourceForDevice, if
- \`com/my/settingslib/bluetooth/LocalBluetoothLeBroadcastMetadata.java\`: LocalBluetoothLeBroadcastMetadata, convertAudioCodecConfigToString, convertAudioContentToString, convertChannelToString, convertSubgroupToString, convertToBroadcastMetadata, convertToChannel, convertToConfigMetadata, convertToContentMetadata, convertToQrCodeString, convertToSubgroup, for, getBroadcastId
- \`com/my/settingslib/bluetooth/HidDeviceProfile.java\`: HidDeviceProfile, HidDeviceServiceListener, accessProfileEnabled, finalize, for, getConnectionPolicy, getConnectionStatus, getDrawableResource, getNameResource, getOrdinal, getProfileId, getSummaryResourceForDevice, if
- \`com/my/settingslib/bluetooth/HearingAidProfile.java\`: HearingAidProfile, HearingAidServiceListener, accessProfileEnabled, finalize, getActiveDevices, getConnectableDevices, getConnectedDevices, getConnectionPolicy, getConnectionStatus, getDeviceMode, getDeviceSide, getDevicesByStates, getDrawableResource
- \`com/my/settingslib/utils/ThreadUtils.java\`: ensureMainThread, getThreadExecutor, getUiThreadHandler, if, isMainThread, postOnBackgroundThread, postOnMainThread, postOnMainThreadDelayed
- \`com/my/settingslib/wifi/AccessPoint.java\`: AccessPoint, AccessPointProvisioningCallback, accept, clearConfig, compareTo, convertToQuotedString, equals, for, generateAverageSpeedForSsid, generateOpenNetworkConfig, getAppLabel, getBssid, getConfig
- \`com/my/settingslib/wifi/WifiUtils.java\`: InternetIconInjector, buildLoggingSummary, for, getIcon, getInternetIconResource, getMeteredLabel, getSpecificApSpeed, getVisibilityStatus, getWifiDetailsSettingsIntent, getWifiDialogIntent, getWifiTetherSummaryForConnectedDevices, if, isMeteredOverridden
- \`com/my/settingslib/wifi/WifiTracker.java\`: DBG, Multimap, Scanner, WifiListenerExecutor, WifiTracker, WifiTrackerNetworkCallback, clearAccessPointsAndConditionallyUpdate, conditionallyNotifyListeners, dump, evictOldScans, fetchScansAndConfigsAndUpdateAccessPoints, filterScanResultsByCapabilities, for
- \`com/my/settingslib/wifi/TimestampedScoredNetwork.java\`: TimestampedScoredNetwork, createFromParcel, describeContents, getScore, getUpdatedTimestampMillis, newArray, update, writeToParcel
- \`com/my/settingslib/widget/AdaptiveOutlineDrawable.java\`: AdaptiveOutlineDrawable, draw, getColor, getDefaultDisplayDensity, getDimensionPixelSize, getDrawable, getIntrinsicHeight, getIntrinsicWidth, if, init, super, switch
- \`com/my/settingslib/widget/AdaptiveIcon.java\`: AdaptiveConstantState, AdaptiveIcon, getChangingConfigurations, getConstantState, getDrawable, newDrawable, setBackgroundColor, setLayerInset, super, this
- \`com/my/settingslib/widget/AdaptiveIconShapeDrawable.java\`: AdaptiveIconShapeDrawable, inflate, init, setShape
- \`com/my/settingslib/wifi/wifitrackerlib/WifiEntry.java\`: ConnectActionListener, ConnectedInfo, ForgetActionListener, WifiEntry, accept, apply, canConnect, canDisconnect, canEasyConnect, canForget, canManageSubscription, canSetAutoJoinEnabled, canSetMeteredChoice
- \`com/my/settingslib/core/instrumentation/EventLogWriter.java\`: action, for, hidden, if, visible
- \`com/my/settingslib/core/instrumentation/Instrumentable.java\`: getMetricsCategory
- \`com/my/settingslib/core/instrumentation/MetricsFeatureProvider.java\`: MetricsFeatureProvider, action, for, getAttribution, getMetricsCategory, hidden, if, installLogWriters, logSettingsTileClick, logStartedIntent, logStartedIntentWithProfile, return, visible
- \`com/my/settingslib/core/instrumentation/LogWriter.java\`: action, hidden, visible
- \`com/my/settingslib/core/lifecycle/Lifecycle.java\`: Lifecycle, LifecycleProxy, addObserver, for, if, onAttach, onCreate, onCreateOptionsMenu, onDestroy, onLifecycleEvent, onOptionsItemSelected, onPause, onPrepareOptionsMenu
- \`com/my/settingslib/core/lifecycle/LifecycleObserver.java\`: 
- \`com/my/settingslib/core/lifecycle/events/OnStop.java\`: onStop
- \`com/my/settingslib/core/lifecycle/events/OnResume.java\`: onResume
- \`com/my/settingslib/core/lifecycle/events/OnPause.java\`: onPause
- \`com/my/settingslib/core/lifecycle/events/SetPreferenceScreen.java\`: setPreferenceScreen
- \`com/my/settingslib/core/lifecycle/events/OnDestroy.java\`: onDestroy
- \`com/my/settingslib/core/lifecycle/events/OnCreate.java\`: onCreate
- \`com/my/settingslib/core/lifecycle/events/OnSaveInstanceState.java\`: onSaveInstanceState
- \`com/my/settingslib/core/lifecycle/events/OnStart.java\`: onStart
- \`com/my/settingslib/core/lifecycle/events/OnPrepareOptionsMenu.java\`: onPrepareOptionsMenu
- \`com/my/settingslib/core/lifecycle/events/OnOptionsItemSelected.java\`: onOptionsItemSelected
- \`com/my/settingslib/core/lifecycle/events/OnCreateOptionsMenu.java\`: onCreateOptionsMenu
- \`com/my/settingslib/core/lifecycle/events/OnAttach.java\`: onAttach
- \`com/my/settings/language/LanguageLocalActivity.java\`: LocaleInfo, compareTo, constructAdapter, findUI, finish, for, getAllAssetLocales, getDatas, getDisplayName, getLabel, getLocale, getMyLocaleInfos, getView
- \`com/my/settings/language/LanguageUtils.java\`: getInstance, if, synchronized, updateLocale
- \`com/my/settings/language/LanguageActivity.java\`: enterClick, findUI, finish, for, getDatas, getLanguageBean, getLanguageData, getTypewritingDatas, if, init, initView, languageLocal, onActivityResult
- \`com/my/settings/language/InputMethodManagerUtils.java\`: for, getInput, getInstance, getPutLists, if, setInput, synchronized
- \`com/my/settings/bluetooth/MyAutoBluetoothActivityOld.java\`: askDisconnect, availableList, clickStatue, connectFinsh, enterClick, findUI, finish, flashScanDevices, for, getIrBluetoothStatus, getStatus, handleMessage, if
- \`com/my/settings/bluetooth/MyBlutoothAdapter.java\`: MyBlutoothAdapter, ViewHolder, for, getConnectionSummary, getCount, getItem, getItemId, getView, if, setDatas, setStatueView, switch
- \`com/my/settings/bluetooth/MyAutoBluetoothActivity.java\`: connectFinsh, findUI, finish, for, getIrBluetoothStatus, getStatus, handleMessage, if, init, initBtstatus, initView, isAutoName, onCreate
- \`com/my/settings/bluetooth/ProvisionBluetoothActivityOld.java\`: askDisconnect, availableList, clickStatue, connectFinsh, dispatchKeyEvent, enterClick, findUI, flashScanDevices, for, getIrBluetoothStatus, getStatus, handleMessage, if
- \`com/my/settings/bluetooth/Utils.java\`: Utils, formatPercentage, getLocalBtManager, if, isAudioModeOngoingCall, onBluetoothManagerInitialized, onShowError, showDisconnectDialog, showError
- \`com/my/settings/bluetooth/ProvisionBluetoothActivity.java\`: connectFinsh, dispatchKeyEvent, enterClick, findUI, for, getIrBluetoothStatus, getStatus, handleMessage, if, init, initBtstatus, initView, isAutoName
- \`com/my/settings/bluetooth/MyBluetoothActivity.java\`: addCachedDevices, addDevices, askDisconnect, autoBluttoth, autoScanning, availabeViewUI, availableList, bluetoothCloseDialog, clickStiwch, closeBlutooth, enterClick, enterFocus, findUI
- \`com/my/settings/bluetooth/MyAutoBlutoothAdapter.java\`: MyAutoBlutoothAdapter, ViewHolder, for, getConnectionSummary, getCount, getItem, getItemId, getView, if, setDatas, setStatueView, switch
- \`com/my/settings/utils/AndroidKeystoreAliasLoader.java\`: AndroidKeystoreAliasLoader, getCaCertAliases, getKeyCertAliases, if, while
- \`com/my/settings/network/SubscriptionUtil$$ExternalSyntheticLambda3.java\`: return, test
- \`com/my/settings/network/SubscriptionUtil.java\`: C1DisplayInfo, apply, findAllSubscriptionsInGroup, for, get, getActiveSubscriptions, getAvailableSubscriptions, getDefaultDataSubscriptionId, getDefaultSmsSubscriptionId, getDefaultSubscriptionSelection, getDefaultVoiceSubscriptionId, getDisplayName, getFirstRemovableSubscription
### KuSourceRJ
- \`com/trd/framework/ITrdSystemService.java\`: Proxy, Stub, asBinder, asInterface, attachInterface, getCommunicationCtrl, getDefaultImpl, getInterfaceDescriptor, getMotorCtrl, getProjectorCtrl, getSensorCtrl, getSystemCtrl, getTemperatureCtrl
- \`com/trd/sdk/ReConnectService.java\`: invoke, proxy, reconnect, return
- \`com/trd/common/enums/SourceItem.java\`: APP, AV1, HDMI1, HDMI2, HDMI3, HOME, SourceItem, for, fromInt, get, if
- \`com/trd/common/bean/TrdTvInputSourceInfo.java\`: TrdTvInputSourceInfo, createFromParcel, getExtraData, getId, getLabel, getParcel, newArray, toString, writeToParcel
- \`com/coolux/source/MainActivity.java\`: cancelFocus, enterClick, enterFocus, findUI, finish, hd2Focus, hdFocus, if, init, initView, loseFocus, onClick, onCreate
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
- \`com/coolux/source/utils/MyLog.java\`: d, e, i, v, w
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
- \`com/android/tv/settings/BootReceiver.java\`: if, onReceive
- \`com/android/tv/settings/SystemSoundsPlayer.java\`: SoundPoolHandler, SoundPoolLoadCompleteListener, SystemSoundsPlayer, for, getSoundPoolIdForEffect, handleMessage, if, onLoadComplete, playSoundEffect, prepareSoundPool, querySoundEffectsEnabled, releaseSoundPool, super
- \`com/android/tv/settings/HasSettingsManager.java\`: getSettingsManager
- \`com/android/tv/settings/HotwordSwitchController.java\`: HotwordLoader, HotwordState, HotwordSwitchController, doInBackground, getLocalizedStringResource, getPreferenceKey, handlePreferenceTreeClick, if, init, isAvailable, onChange, onHotwordDisable, onHotwordEnable
- \`com/android/tv/settings/EmptyStubActivity.java\`: finish, onCreate
- \`com/android/tv/settings/PreferenceControllerFragment$$ExternalSyntheticLambda0.java\`: test
- \`com/android/tv/settings/SettingsFragmentProvider.java\`: newSettingsFragment
- \`com/android/tv/settings/SettingsPreferenceFragment.java\`: AnonymousClass1, for, getActivity, getPageId, getSettingsLifecycle, if, iteratePreferenceAndSetObserver, onArriveAtMainPanel, onAttach, onChanged, onCreate, onCreateAdapter, onCreateOptionsMenu
- \`com/android/tv/settings/TakeBugReportController.java\`: TakeBugReportController, getPreferenceKey, handlePreferenceTreeClick, if, isAvailable, super, updateState
- \`com/android/tv/settings/MainSettings.java\`: createSettingsFragment, isStartupVerificationRequired
- \`com/android/tv/settings/RestrictedSettingsFragment.java\`: RestrictedSettingsFragment, ensurePin, getActivity, getEmptyTextView, getEmptyView, getRestrictionEnforcedAdmin, hasChallengeSucceeded, if, initEmptyTextView, isRestrictedAndNotProviderProtected, isUiRestricted, isUiRestrictedByOnlyAdmin, onActivityCreated
- \`com/android/tv/settings/MainFragment.java\`: for, getActivity, getContext, getDrawableResource, getPageId, getPreferenceScreenResId, getProviderInfo, getStringResource, if, isRestricted, isWifiScanOptimisationEnabled, newInstance, onAttach
- \`com/android/tv/settings/LongClickPreference.java\`: LongClickPreference, onBindViewHolder, onLongClick, onPreferenceLongClick, setLongClickListener, super
- \`com/android/tv/settings/NopePreferenceController.java\`: NopePreferenceController, getPreferenceKey, isAvailable, super
- \`com/android/tv/settings/PreferenceControllerFragment.java\`: addPreferenceController, for, getOnePreferenceController, getPreferenceControllers, getPreferenceManager, getPreferenceScreenResId, if, onAttach, onCreate, onCreatePreferenceControllers, onCreatePreferences, onPreferenceTreeClick, onResume
- \`com/android/tv/settings/RemoteBugreportActivity.java\`: if, onClick, onCreate, onDismiss
- \`com/android/tv/settings/ActionDisabledByAdminDialog.java\`: finish, getAdminDetailsFromIntent, getRestrictionFromIntent, if, onCreate, onDismiss, onNewIntent
- \`com/android/tv/settings/SuggestionQuickSettingPrefsContainer.java\`: SuggestionQuickSettingPrefsContainer, for, hideQuickSettings, if, isRestricted, onCreate, onCreatePreferenceControllers, onCreatePreferences, onDestroy, onHotwordDisable, onHotwordEnable, onHotwordStateChanged, onPreferenceClick
- \`com/android/tv/settings/GlobalKeyReceiver.java\`: if, onReceive, sendPairingIntent
- \`com/android/tv/settings/EventLogTags.java\`: EventLogTags, writeExpDetDeviceAdminActivatedByUser, writeExpDetDeviceAdminDeclinedByUser, writeExpDetDeviceAdminUninstalledByUser, writeLockScreenType, writeSettingsLatency
- \`com/android/tv/settings/DeviceSettingBroadcastReceiver.java\`: doInBackground, if, onReceive, removeWifis, restartDevice, while
- \`com/android/tv/settings/ActionDisabledByAdminDialogHelper.java\`: ActionDisabledByAdminDialogHelper, getEnforcementAdminUserId, if, initializeDialogViews, maybeSetLearnMoreButton, onClick, prepareDialogBuilder, setAdminSupportDetails, setAdminSupportTitle, showAdminPolicies, switch, updateDialog
- \`com/android/tv/settings/TvSettingsActivity.java\`: AnonymousClass1, AnonymousClass2, accept, createSettingsFragment, finish, for, getAvailableFlavors, getSettingsManager, getSupportFragmentManager, getVisibleFragment, if, isStartupVerificationRequired, lambda$notifyUpdate$0
- \`com/android/tv/settings/RadioPreference.java\`: RadioPreference, clearOtherRadioPreferences, for, getRadioGroup, if, setRadioGroup, setWidgetLayoutResource, super, this
- \`com/android/tv/settings/RestrictedPreferenceAdapter.java\`: RestrictedPreferenceAdapter, adapt, getOriginalPreference, getPreference, if, isRestricted, isRestrictedByAdmin, replacePreference, setup, this, updatePreference, updateRestrictedPreference, while
- \`com/android/tv/settings/PreferenceUtils.java\`: for, if, resolveSystemActivityOrRemove
- \`com/android/tv/settings/TvSettingsApplication.java\`: getSystemSoundsPlayer, onCreate
- \`com/android/tv/settings/Manifest.java\`: 
- \`com/android/tv/settings/PreferenceExUtils.java\`: for, getHdmiDetectType, getSettingIntValue, getSettingStringValue, if, isSupportSpeakerAndSPDIFFlag, putSettingValue, putSettingValueInt, putSettingValueString, resolveSystemActivityOrRemove, setContext, setupListPreference, setupListPreferences
- \`com/android/tv/settings/BaseSettingsFragment.java\`: if, isUriValid, navigateBack, onPreferenceDisplayDialog, onPreferenceStartFragment, onPreferenceStartScreen, startImmersiveFragment, startPreferenceFragment
- \`com/android/tv/settings/overlay/FeatureFactory.java\`: getSettingsFragmentProvider
- \`com/android/tv/settings/overlay/FeatureFactoryImplTwoPanel.java\`: getSettingsFragmentProvider, if, newInstance, newSettingsFragment, onCreatePreviewFragment, onPreferenceStartInitialScreen, onPreferenceStartScreen, startPreferenceFragment
- \`com/android/tv/settings/overlay/FeatureFactoryImplX.java\`: 
- \`com/android/tv/settings/overlay/FeatureFactoryImpl.java\`: getSettingsFragmentProvider, newInstance, newSettingsFragment, onPreferenceStartInitialScreen, startPreferenceFragment
- \`com/android/tv/settings/overlay/FlavorUtils.java\`: getFeatureFactory, getFlavor, if, isTwoPanel, return, switch
- \`com/android/tv/settings/partnercustomizer/MtkPreferenceCategory.java\`: MtkPreferenceCategory, if, onBindViewHolder, super
- \`com/android/tv/settings/partnercustomizer/CustomizedContentProvider.java\`: delete, for, getAllLanguages, getTranslucent, getType, if, initLocales, insert, onCreate, query, update
- \`com/android/tv/settings/partnercustomizer/TvSettingsSliceProvider.java\`: call, createAudioSlice, createHelp, createNetWakeSlice, createNetflexESNSlice, createSampleSlice, createSoftwareVersionSlice, createTvLiftTime, getContext, getFirstSegment, getTvLifeTime, if, isMiscItemDisplay
- \`com/android/tv/settings/partnercustomizer/CommonActivity.java\`: AnonymousClass1, createSettingsFragment, finish, finishActivity, for, getCurRunningPackageName, getSupportFragmentManager, getWindow, getsInstance, if, onActivityResult, onCreate, onPreDraw
- \`com/android/tv/settings/partnercustomizer/EnergyCommonActivity.java\`: EnergyFragment, finish, getActivity, getContentResolver, if, newInstance, onBackPressed, onCreate, onCreateActions, onCreateGuidance, onGuidedActionClicked, onStop, run
- \`com/android/tv/settings/partnercustomizer/DisplayAndSoundActivity.java\`: createSettingsFragment
- \`com/android/tv/settings/accounts/RemoveAccountDialog.java\`: finish, for, if, newInstance, onCreate, onCreateActions, onCreateGuidance, onGuidedActionClicked, run
- \`com/android/tv/settings/accounts/AccountsFragment.java\`: for, getAuthImage, getAuthTitle, getTargetContext, if, isRestricted, onAccountsUpdate, onCreate, onCreatePreferences, onPreferenceClick, onResume, onStart, onStop
- \`com/android/tv/settings/accounts/AccountSyncFragment.java\`: accountExists, cancelSyncForEnabledProviders, createSyncStateSwitch, for, getActivity, getPageId, getPreferenceScreen, getToggleId, if, isSyncing, newInstance, onAccountsUpdate, onCreate
- \`com/android/tv/settings/accounts/AddAccountWithTypeActivity.java\`: finish, if, onActivityResult, onCreate, run, setResult, setResultAndFinish, startAccountTypePicker, startAccountsActivity, startActivity, startActivityForResult, startAddAccount
- \`com/android/tv/settings/accounts/AccountsSliceFragment.java\`: 
- \`com/android/tv/settings/accounts/AccountsActivity.java\`: createSettingsFragment, if
- \`com/android/tv/settings/accounts/AccountsTrampolineActivity.java\`: finish, getActivityToStart, if, onCreate, startActivity
- \`com/android/tv/settings/accounts/ManageUserServicesSliceActivity.java\`: getAvailableFlavors, getScreenTitle, getSliceUri, getString, if
- \`com/android/tv/settings/accounts/AccountSliceActivity.java\`: getAvailableFlavors, getScreenTitle, getSliceUri, getString
- \`com/android/tv/settings/accounts/AccountsUtil.java\`: getAccountsFragmentToLaunch, if, isAdminRestricted
- \`com/android/tv/settings/accounts/SyncStateSwitchPreference.java\`: SyncStateSwitchPreference, getAccount, getAuthority, if, isOneTimeSyncMode, notifyChanged, onBindViewHolder, onClick, setActive, setFailed, setOneTimeSyncMode, setPending, super
- \`com/android/tv/settings/accounts/AccountSyncActivity.java\`: createSettingsFragment, for, getArguments, if
- \`com/android/tv/settings/enterprise/CaCertsManagedProfilePreferenceController.java\`: CaCertsManagedProfilePreferenceController, getNumberOfCaCerts, getPreferenceKey, super
- \`com/android/tv/settings/enterprise/AdminActionPreferenceControllerBase.java\`: AdminActionPreferenceControllerBase, getAdminActionTimestamp, if, isAvailable, super, updateState
- \`com/android/tv/settings/enterprise/ApplicationListPreferenceController.java\`: ApplicationListPreferenceController, buildApplicationList, for, getPreferenceKey, if, isAvailable, onListOfAppsResult, super
- \`com/android/tv/settings/enterprise/FailedPasswordWipePreferenceControllerBase.java\`: FailedPasswordWipePreferenceControllerBase, getMaximumFailedPasswordsBeforeWipe, isAvailable, super, updateState
- \`com/android/tv/settings/enterprise/AdminGrantedLocationPermissionsPreferenceController.java\`: AdminGrantedLocationPermissionsPreferenceController, getPreferenceKey, super
- \`com/android/tv/settings/enterprise/AdminGrantedPermissionsPreferenceControllerBase.java\`: AdminGrantedPermissionsPreferenceControllerBase, handlePreferenceTreeClick, if, isAvailable, onNumberOfAppsResult, super, updateState
- \`com/android/tv/settings/enterprise/FailedPasswordWipeManagedProfilePreferenceController.java\`: FailedPasswordWipeManagedProfilePreferenceController, getMaximumFailedPasswordsBeforeWipe, getPreferenceKey, super
- \`com/android/tv/settings/enterprise/EnterpriseInstalledPackagesPreferenceController.java\`: EnterpriseInstalledPackagesPreferenceController, getPreferenceKey, if, isAvailable, onNumberOfAppsResult, super, updateState
- \`com/android/tv/settings/enterprise/ApplicationListFragment.java\`: AdminGrantedPermission, AdminGrantedPermissionCamera, AdminGrantedPermissionLocation, AdminGrantedPermissionMicrophone, buildApplicationList, getPreferenceScreenResId, newInstance, onCreatePreferenceControllers, super
- \`com/android/tv/settings/enterprise/BugReportsPreferenceController.java\`: BugReportsPreferenceController, getAdminActionTimestamp, getPreferenceKey, super
- \`com/android/tv/settings/enterprise/CaCertsCurrentUserPreferenceController.java\`: CaCertsCurrentUserPreferenceController, getNumberOfCaCerts, getPreferenceKey, super, updateState
- \`com/android/tv/settings/enterprise/EnterpriseSetDefaultAppsPreferenceController.java\`: EnterpriseSetDefaultAppsPreferenceController, for, getNumberOfEnterpriseSetDefaultApps, getPreferenceKey, isAvailable, super, updateState
- \`com/android/tv/settings/enterprise/EnterpriseSetDefaultAppsListFragment.java\`: getPageId, getPreferenceScreenResId, newInstance, onCreatePreferenceControllers
- \`com/android/tv/settings/enterprise/ImePreferenceController.java\`: ImePreferenceController, getPreferenceKey, isAvailable, super, updateState
- \`com/android/tv/settings/enterprise/AlwaysOnVpnManagedProfilePreferenceController.java\`: AlwaysOnVpnManagedProfilePreferenceController, getPreferenceKey, isAvailable, super
- \`com/android/tv/settings/enterprise/AlwaysOnVpnCurrentUserPreferenceController.java\`: AlwaysOnVpnCurrentUserPreferenceController, getPreferenceKey, isAvailable, super, updateState
- \`com/android/tv/settings/enterprise/AdminGrantedMicrophonePermissionPreferenceController.java\`: AdminGrantedMicrophonePermissionPreferenceController, getPreferenceKey, super
- \`com/android/tv/settings/enterprise/CaCertsPreferenceControllerBase.java\`: CaCertsPreferenceControllerBase, getNumberOfCaCerts, isAvailable, super, updateState
- \`com/android/tv/settings/enterprise/GlobalHttpProxyPreferenceController.java\`: GlobalHttpProxyPreferenceController, getPreferenceKey, isAvailable, super
- \`com/android/tv/settings/enterprise/SecurityLogsPreferenceController.java\`: SecurityLogsPreferenceController, getAdminActionTimestamp, getPreferenceKey, isAvailable, super
- \`com/android/tv/settings/enterprise/AdminGrantedCameraPermissionPreferenceController.java\`: AdminGrantedCameraPermissionPreferenceController, getPreferenceKey, super
- \`com/android/tv/settings/enterprise/EnterprisePrivacySettingsActivity.java\`: createSettingsFragment
- \`com/android/tv/settings/enterprise/EnterpriseSetDefaultAppsListPreferenceController.java\`: EnterpriseSetDefaultAppsListPreferenceController, buildAppList, buildSummaryString, createPreferences, for, getPreferenceKey, getTitle, if, isAvailable, lambda$buildAppList$0, run, super, switch
- \`com/android/tv/settings/enterprise/PreferenceCategoryController.java\`: PreferenceCategoryController, getPreferenceKey, if, isAvailable, setChildren, super, while
- \`com/android/tv/settings/enterprise/NetworkLogsPreferenceController.java\`: NetworkLogsPreferenceController, getAdminActionTimestamp, getPreferenceKey, isAvailable, super
- \`com/android/tv/settings/enterprise/PrivacySettingsEnterprisePreference.java\`: PrivacySettingsEnterprisePreference, createPreferenceControllers, getPreferenceScreenResId, getXmlResourcesToIndex
- \`com/android/tv/settings/enterprise/EnterprisePrivacySettingsFragment.java\`: getPageId, getPreferenceScreenResId, isPageEnabled, newInstance, onAttach, onCreatePreferenceControllers, onDetach
- \`com/android/tv/settings/enterprise/FailedPasswordWipeCurrentUserPreferenceController.java\`: FailedPasswordWipeCurrentUserPreferenceController, getMaximumFailedPasswordsBeforeWipe, getPreferenceKey, super
- \`com/android/tv/settings/enterprise/PrivacySettingsPreference.java\`: createPreferenceControllers, getPreferenceScreenResId, getXmlResourcesToIndex
- \`com/android/tv/settings/users/RestrictedProfilePinService.java\`: PinServiceImpl, deletePin, getPin, isPinCorrect, isPinSet, isSystemUser, onBind, onCreate, onDestroy, setPin
- \`com/android/tv/settings/users/AppRestrictionsFragment.java\`: AppLoadingTask, AppRestrictionsPreference, Listener, RestrictionsResultReceiver, addLocationAppRestrictionsPreference, apply, assertSafeToStartCustomActivity, doInBackground, findInArray, for, generateCustomActivityRequestCode, getActivity, getAppPreferenceGroup
- \`com/android/tv/settings/users/IRestrictedProfilePinService.java\`: Proxy, Stub, asBinder, asInterface, attachInterface, deletePin, getInterfaceDescriptor, if, isPinCorrect, isPinSet, onTransact, return, setPin
- \`com/android/tv/settings/users/RestrictionUtils.java\`: for, getRestrictions, if, setRestrictions, while
- \`com/android/tv/settings/users/UserSwitchListenerService.java\`: getBootUser, hasRestrictedProfile, if, onBind, onCreate, onDestroy, onReceive, onStartCommand, onUserCreatedOrDeleted, onUserSwitchComplete, setBootUser, stopSelf, switchUserNow
- \`com/android/tv/settings/users/RestrictedProfilePinServiceConnection.java\`: RestrictedProfilePinServiceConnection, bindPinService, getPinService, if, onBindingDied, onNullBinding, onServiceConnected, onServiceDisconnected, synchronized, unbindPinService, waitForService
- \`com/android/tv/settings/users/RestrictedProfilePinStorage.java\`: RestrictedProfilePinStorage, bind, checkPasswordLegacy, clearLockPassword, deletePin, deletePinInternal, findRestrictedProfileParent, getPinService, hasLockscreenSecurity, if, isPinCorrect, isPinCorrectInternal, isPinSet
- \`com/android/tv/settings/users/RestrictedProfilePinDialogFragment.java\`: deletePin, getDisablePinUntil, getPinDisabledUntil, isPinCorrect, isPinSet, newInstance, onAttach, onDetach, run, setDisablePinUntil, setPin, setPinDisabledUntil
- \`com/android/tv/settings/basic/BasicModeExitSuggestionInfoFragment.java\`: onCreateView, updateInfoFragment
- \`com/android/tv/settings/basic/BasicModeAccountsAndSignInInfoFragment.java\`: onCreateView, updateInfoFragment
- \`com/android/tv/settings/vendor/ChannelsAndInputFragment.java\`: onCreatePreferences, setPreferencesFromResource
- \`com/android/tv/settings/vendor/ResetOptionsFragment.java\`: if, onCreatePreferences, onPreferenceTreeClick, setPreferencesFromResource
- \`com/android/tv/settings/vendor/DisplayPreviewFragment.java\`: getDisplayManager, getPreferenceScreen, if, onCreatePreferences, onDisplayAdded, onDisplayChanged, onDisplayRemoved, removeResolutionPreference, return, setPreferencesFromResource, updateResolutionTitleDescription

## Safety gates

1. Identify exact broadcasts, pending intents, boot receiver/service lifecycle, and references from other APKs before disabling KuSettings.
2. Preserve KuSourceRJ, TRD services, focus/geometry, HDMI input routing, and SetupWizard.
3. Confirm TvSettingsPlus original platform certificate; do not infer it from OTA testkey.
4. Never write raw I2C or invoke motor APIs until vendor semantics and stops are validated.
5. Modify existing TvSettingsPlus package; compare resource maps and compile before APK signing.
6. First firmware test should validate navigation/UI and service references; require rollback procedure.

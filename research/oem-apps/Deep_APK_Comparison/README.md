# Everycom CineLux — deep OEM app comparison (1 vs 2 HDMI)

APK and Java method hashes are extracted without executing OEM code. Decompiled sources are NOT published.
Both original APKs may be from builds with different compiler flags; class/method diffs are indicative, not behavior proof.

| App | APK exact? | DEX exact? | Same classes | Changed classes | Changed methods | Added/removed methods |
|---|---|---|---:|---:|---:|---:|
| Factory | False | False | 1859 | 6 | 7 | 4 |
| KeystoneCorrection | False | False | 448 | 1 | 1 | 1 |
| KuSettings | False | False | 3463 | 7 | 7 | 0 |
| KuSetupWizard | Only two | — | — | — | — | — |
| KuSourceRJ | False | False | 2975 | 31 | 15 | 29 |
| LiveTV | False | False | 2243 | 3 | 3 | 0 |
| RuiAutoFocus | False | False | 1077 | 14 | 21 | 2 |
| RuiYunOta | True | True | 75 | 0 | 0 | 0 |
| TvServices | False | False | 473 | 1 | 1 | 0 |
| TvSettingsPlus | False | False | 5724 | 71 | 18 | 6 |
| trdmiddle | True | True | 567 | 1 | 1 | 0 |
| trdservice | False | False | 432 | 6 | 3 | 21 |
| zxlauncher | False | False | 3870 | 6313 | 6308 | 46980 |
| zxstore | False | False | 9703 | 959 | 1118 | 4798 |

## Factory

One HDMI: mediatek.factorymenu.ui version 1.0
Two HDMI: mediatek.factorymenu.ui version 1.0
Exact dex same: False
App permissions only in 1 HDMI: 
App permissions only in 2 HDMI: 
Native loaded 1 HDMI: com_mediatek_net_jni, com_mediatek_twoworlds_tv_jni
Native loaded 2 HDMI: com_mediatek_net_jni, com_mediatek_twoworlds_tv_jni, factoryi2c_jni
Method change counts: {"unchanged": 23294, "changed": 7, "added": 3, "removed": 1}

### Changed methods of likely hardware relevance (sample)

- changed com/android/tv/settings/partnercustomizer/tvsettingservice/TVSettingConfig.java :: getDefaultPowerSetting(android.content.Context context)
- changed com/android/tv/settings/partnercustomizer/tvsettingservice/TVSettingConfig.java :: getIntautoSleepValue(android.content.Context context)
- added mediatek/tvsetting/factory/ui/ProjectorManager.java :: i2c(boolean z)

### Added classes (sample)

- mediatek/util/I2CDevice.java

### Removed classes (sample)


## KeystoneCorrection

One HDMI: com.rjpj.tcorrection version 13
Two HDMI: com.rjpj.tcorrection version 13
Exact dex same: False
App permissions only in 1 HDMI: 
App permissions only in 2 HDMI: 
Native loaded 1 HDMI: i2c_jni
Native loaded 2 HDMI: i2c_jni
Method change counts: {"unchanged": 5096, "changed": 1, "added": 1, "removed": 0}

### Changed methods of likely hardware relevance (sample)

- added com/rjpj/tcorrection/KeystoneUtil.java :: getProjectorMirrorMode()
- changed com/rjpj/tcorrection/KeystoneUtil.java :: writeParcelToFlinger(int i)

### Added classes (sample)


### Removed classes (sample)


## KuSettings

One HDMI: None version None
Two HDMI: None version None
Exact dex same: False
App permissions only in 1 HDMI: 
App permissions only in 2 HDMI: 
Native loaded 1 HDMI: 
Native loaded 2 HDMI: 
Method change counts: {"unchanged": 47103, "changed": 7, "added": 0, "removed": 0}

### Changed methods of likely hardware relevance (sample)

- changed com/my/settings/bluetooth/MyAutoBluetoothActivity.java :: init()
- changed com/my/settings/bluetooth/MyAutoBluetoothActivity.java :: isAutoName(java.lang.String bluetoothName)
- changed com/my/settings/bluetooth/MyAutoBluetoothActivity.java :: viewAutoBluetoothBak()

### Added classes (sample)


### Removed classes (sample)


## KuSetupWizard

Package only available in two HDMI image.

## KuSourceRJ

One HDMI: None version None
Two HDMI: None version None
Exact dex same: False
App permissions only in 1 HDMI: 
App permissions only in 2 HDMI: 
Native loaded 1 HDMI: 
Native loaded 2 HDMI: 
Method change counts: {"unchanged": 38789, "changed": 15, "added": 27, "removed": 2}

### Changed methods of likely hardware relevance (sample)

- changed com/coolux/source/MainActivity.java :: hd2Focus()
- changed com/coolux/source/activity/BootSoureActivity.java :: enterClick(android.view.View view)
- changed com/coolux/source/activity/BootSoureActivity.java :: findUI()
- changed com/coolux/source/activity/BootSoureActivity.java :: init()
- changed com/coolux/source/activity/BootSoureActivity.java :: viewBootSource()
- removed com/coolux/source/activity/BootSoureActivity.java :: viewBootSource1()
- removed com/coolux/source/activity/BootSoureActivity.java :: viewBootSource2()
- added com/coolux/source/activity/BootSoureActivityOld.java :: clickSorce(int i)
- added com/coolux/source/activity/BootSoureActivityOld.java :: enterClick(android.view.View view)
- added com/coolux/source/activity/BootSoureActivityOld.java :: enterKey(android.view.View view, android.view.KeyEvent keyEvent)
- added com/coolux/source/activity/BootSoureActivityOld.java :: findUI()
- added com/coolux/source/activity/BootSoureActivityOld.java :: getBootSource()
- added com/coolux/source/activity/BootSoureActivityOld.java :: init()
- added com/coolux/source/activity/BootSoureActivityOld.java :: initView()
- added com/coolux/source/activity/BootSoureActivityOld.java :: onActivityResult(int i, int i2, android.content.Intent intent)
- added com/coolux/source/activity/BootSoureActivityOld.java :: onClick(android.view.View view)
- added com/coolux/source/activity/BootSoureActivityOld.java :: onCreate(android.os.Bundle bundle)
- added com/coolux/source/activity/BootSoureActivityOld.java :: onDestroy()
- added com/coolux/source/activity/BootSoureActivityOld.java :: onKey(android.view.View view, int i, android.view.KeyEvent keyEvent)
- added com/coolux/source/activity/BootSoureActivityOld.java :: onPause()
- added com/coolux/source/activity/BootSoureActivityOld.java :: onResume()
- added com/coolux/source/activity/BootSoureActivityOld.java :: setBootSource(int i)
- added com/coolux/source/activity/BootSoureActivityOld.java :: testAction()
- added com/coolux/source/activity/BootSoureActivityOld.java :: viewBootSource()
- added com/coolux/source/activity/BootSoureActivityOld.java :: viewBootSource1()
- added com/coolux/source/activity/BootSoureActivityOld.java :: viewBootSource2()
- added com/coolux/source/activity/KeystoneActivity.java :: clickTofAutofocus()
- changed com/coolux/source/activity/KeystoneActivity.java :: enterClick(android.view.View view)
- changed com/coolux/source/activity/KeystoneActivity.java :: findUI()
- changed com/coolux/source/activity/KeystoneActivity.java :: init()
- changed com/coolux/source/activity/KeystoneActivity.java :: initView()
- added com/coolux/source/activity/KeystoneActivity.java :: isViewTofAutofocus()
- changed com/coolux/source/activity/KeystoneActivity.java :: onCreate(android.os.Bundle bundle)
- added com/coolux/source/activity/KeystoneActivity.java :: viewTofAutofocus()
- changed com/coolux/source/activity/testcast/InterfaceActivity.java :: enterClick(android.view.View view)
- changed com/coolux/source/utils/ImageUtils.java :: defaultBrightModel()
- added com/coolux/source/utils/ImageUtils.java :: getSystemSource()
- added com/coolux/source/utils/ImageUtils.java :: getTofAutofocus()
- added com/coolux/source/utils/ImageUtils.java :: isDefaultPerformance()
- added com/coolux/source/utils/ImageUtils.java :: setTofAutofocus(java.lang.String str)
- changed com/coolux/source/utils/MyTimeUtils.java :: highBrightService(android.content.Context context)
- changed com/coolux/source/utils/ReflectionUtils.java :: getHDMINum(android.content.Context context)
- added com/coolux/source/utils/ReflectionUtils.java :: isDPLSTOF()
- changed com/coolux/source/utils/SharedPreferencesTest.java :: defaultSavePerformance()

### Added classes (sample)

- com/coolux/source/activity/BootSoureActivityOld.java

### Removed classes (sample)


## LiveTV

One HDMI: None version None
Two HDMI: None version None
Exact dex same: False
App permissions only in 1 HDMI: 
App permissions only in 2 HDMI: 
Native loaded 1 HDMI: com_mediatek_twoworlds_tv_jni
Native loaded 2 HDMI: com_mediatek_twoworlds_tv_jni
Method change counts: {"unchanged": 26940, "changed": 3, "added": 0, "removed": 0}

### Changed methods of likely hardware relevance (sample)


### Added classes (sample)


### Removed classes (sample)


## RuiAutoFocus

One HDMI: None version None
Two HDMI: None version None
Exact dex same: False
App permissions only in 1 HDMI: 
App permissions only in 2 HDMI: 
Native loaded 1 HDMI: com_mediatek_twoworlds_tv_jni, focusvahysd, ruicameraapi_jni
Native loaded 2 HDMI: com_mediatek_twoworlds_tv_jni, focusvahysd, ruicameraapi_jni
Method change counts: {"unchanged": 15915, "changed": 21, "added": 2, "removed": 0}

### Changed methods of likely hardware relevance (sample)

- changed com/hysd/ai/RuiCameraApi.java :: IsEndPositonBack()
- changed com/hysd/ai/RuiCameraApi.java :: IsEndPositonFront()
- changed com/hysd/ai/RuiCameraApi.java :: MotorBack()
- changed com/hysd/ai/RuiCameraApi.java :: MotorBackStep(int setRunStep)
- changed com/hysd/ai/RuiCameraApi.java :: MotorBackTofStep(int i, boolean z, int i2)
- changed com/hysd/ai/RuiCameraApi.java :: MotorFront()
- changed com/hysd/ai/RuiCameraApi.java :: MotorFrontStep(int setRunStep)
- changed com/hysd/ai/RuiCameraApi.java :: MotorFrontTofStep(int i, boolean z, int i2)
- changed com/hysd/ai/RuiCameraApi.java :: MotorStop()
- changed com/hysd/ai/RuiCameraApi.java :: RuiCameraApi(android.content.Context context)
- changed com/hysd/ai/RuiCameraApi.java :: startTof()

### Added classes (sample)


### Removed classes (sample)


## RuiYunOta

One HDMI: com.rjpj.yunota version 1.0
Two HDMI: com.rjpj.yunota version 1.0
Exact dex same: True
App permissions only in 1 HDMI: 
App permissions only in 2 HDMI: 
Native loaded 1 HDMI: 
Native loaded 2 HDMI: 
Method change counts: {"unchanged": 184, "changed": 0, "added": 0, "removed": 0}

### Changed methods of likely hardware relevance (sample)


### Added classes (sample)


### Removed classes (sample)


## TvServices

One HDMI: com.mediatek.tv.service version 1.3
Two HDMI: com.mediatek.tv.service version 1.3
Exact dex same: False
App permissions only in 1 HDMI: 
App permissions only in 2 HDMI: 
Native loaded 1 HDMI: com_mediatek_twoworlds_tv_jni
Native loaded 2 HDMI: com_mediatek_twoworlds_tv_jni
Method change counts: {"unchanged": 5472, "changed": 1, "added": 0, "removed": 0}

### Changed methods of likely hardware relevance (sample)


### Added classes (sample)


### Removed classes (sample)


## TvSettingsPlus

One HDMI: None version None
Two HDMI: None version None
Exact dex same: False
App permissions only in 1 HDMI: 
App permissions only in 2 HDMI: 
Native loaded 1 HDMI: com_mediatek_net_jni, com_mediatek_twoworlds_tv_jni
Native loaded 2 HDMI: com_mediatek_net_jni, com_mediatek_twoworlds_tv_jni, settingsi2c_jni
Method change counts: {"unchanged": 73517, "changed": 18, "added": 5, "removed": 1}

### Changed methods of likely hardware relevance (sample)

- changed com/android/tv/settings/partnercustomizer/tvsettingservice/TVSettingConfig.java :: getDefaultPowerSetting(android.content.Context context)
- changed com/android/tv/settings/partnercustomizer/tvsettingservice/TVSettingConfig.java :: getIntautoSleepValue(android.content.Context context)
- changed com/android/tv/settings/uicustom/floatmenu/controller/picture/AdvanceSettingsSecondaryController.java :: initCheck()
- changed com/android/tv/settings/uicustom/floatmenu/controller/picture/AdvanceSettingsSecondaryController.java :: initView()
- changed com/android/tv/settings/uicustom/floatmenu/controller/picture/AdvanceSettingsSecondaryController.java :: onClick(android.view.View view)
- changed com/android/tv/settings/uicustom/floatmenu/controller/picture/AdvanceSettingsSecondaryController.java :: onResume()
- changed com/android/tv/settings/uicustom/floatmenu/controller/projector/FocusSettingsSecondaryController.java :: initView()
- changed com/android/tv/settings/uicustom/floatmenu/controller/projector/ProjectionModeSecondaryController.java :: initListener()
- changed com/android/tv/settings/uicustom/floatmenu/controller/projector/ProjectionModeSecondaryController.java :: onCheckedChanged(com.android.tv.settings.uicustom.view.RadioTextView radioTextView, boolean z)
- changed com/android/tv/settings/uicustom/floatmenu/controller/source/SourceSubMenuController.java :: initData()
- added com/android/tv/settings/uicustom/floatmenu/controller/source/SourceSubMenuController.java :: initSource()
- changed com/android/tv/settings/uicustom/floatmenu/controller/source/SourceSubMenuController.java :: initView(android.view.View view)
- added com/android/tv/settings/uicustom/floatmenu/controller/source/SourceSubMenuController.java :: isTopActivity(android.content.Context context, java.lang.String str)
- changed com/android/tv/settings/uicustom/floatmenu/controller/source/SourceSubMenuController.java :: onResume()
- changed com/android/tv/settings/uicustom/floatmenu/controller/source/SourceSubMenuController.java :: onSwitchChangeListener(boolean z)
- added com/android/tv/settings/uicustom/floatmenu/controller/source/SourceSubMenuController.java :: resetTrapezoid()
- changed com/android/tv/settings/uicustom/floatmenu/controller/system/SystemSubMenuController.java :: factoryReset()
- changed com/android/tv/settings/uicustom/fragment/projector/ProjectorModeFragment.java :: initListener()
- changed com/android/tv/settings/uicustom/fragment/projector/ProjectorModeFragment.java :: onCheckedChanged(com.android.tv.settings.uicustom.view.RadioTextView radioTextView, boolean z)
- changed com/android/tv/settings/uicustom/fragment/system/SystemFragment.java :: factoryReset()
- added com/android/tv/settings/uicustom/util/ProjectorManager.java :: i2c(boolean z)
- added com/android/tv/settings/uicustom/util/ProjectorManager.java :: setProjectorMirrorMode(android.content.Context context, int i)
- removed com/android/tv/settings/uicustom/util/ProjectorManager.java :: setProjectorMirrorMode(int i)

### Added classes (sample)

- com/android/tv/settings/uicustom/I2CDevice.java

### Removed classes (sample)


## trdmiddle

One HDMI: com.trd.framework version 1.0
Two HDMI: com.trd.framework version 1.0
Exact dex same: True
App permissions only in 1 HDMI: 
App permissions only in 2 HDMI: 
Native loaded 1 HDMI: com_mediatek_twoworlds_tv_jni, tof_jni, trd_jni, trdmiddle
Native loaded 2 HDMI: com_mediatek_twoworlds_tv_jni, tof_jni, trd_jni, trdmiddle
Method change counts: {"unchanged": 6451, "changed": 1, "added": 0, "removed": 0}

### Changed methods of likely hardware relevance (sample)

- changed com/trd/framework/tof/TofCtrlService.java :: invokeTofDataListenersOnReceived(int i2, int i3, byte[] bArr)

### Added classes (sample)


### Removed classes (sample)


## trdservice

One HDMI: com.trd.service version 13
Two HDMI: com.trd.service version 13
Exact dex same: False
App permissions only in 1 HDMI: 
App permissions only in 2 HDMI: 
Native loaded 1 HDMI: rjpj_private_function
Native loaded 2 HDMI: rjpj_private_function
Method change counts: {"unchanged": 4855, "changed": 3, "added": 2, "removed": 19}

### Changed methods of likely hardware relevance (sample)

- removed com/trd/common/enums/MotorCtrlEnum.java :: MotorCtrlEnum(int i)
- removed com/trd/common/enums/MotorCtrlEnum.java :: get()
- removed com/trd/common/enums/MotorStatusEnum.java :: MotorStatusEnum(int i)
- removed com/trd/common/enums/MotorStatusEnum.java :: fromInt(int i)
- removed com/trd/common/enums/MotorStatusEnum.java :: get()
- removed com/trd/framework/IMotorCtrl.java :: GetMotorStatus()
- removed com/trd/framework/IMotorCtrl.java :: Proxy(android.os.IBinder iBinder)
- removed com/trd/framework/IMotorCtrl.java :: SetMotorCtrl(int i, int i2)
- removed com/trd/framework/IMotorCtrl.java :: asBinder()
- removed com/trd/framework/IMotorCtrl.java :: asInterface(android.os.IBinder iBinder)
- removed com/trd/framework/IMotorCtrl.java :: getDefaultImpl()
- removed com/trd/framework/ITrdSystemService.java :: getMotorCtrl()
- removed com/trd/sdk/TrdSystemManager.java :: getMotorCtrl()
- removed com/trd/sdk/impl/MotorCtrlImpl.java :: GetMotorStatus()
- removed com/trd/sdk/impl/MotorCtrlImpl.java :: MotorCtrlImpl()
- removed com/trd/sdk/impl/MotorCtrlImpl.java :: SetMotorCtrl(com.trd.common.enums.MotorCtrlEnum motorCtrlEnum, int i)
- removed com/trd/sdk/impl/MotorCtrlImpl.java :: getInstance()
- removed com/trd/sdk/impl/MotorCtrlImpl.java :: reconnect()
- removed com/trd/service/TrdService.java :: checkMirrorType()
- added com/trd/service/TrdService.java :: lambda$onCreate$0()
- added com/trd/service/TrdService.java :: lambda$onCreate$1()
- changed com/trd/service/TrdService.java :: onCreate()
- changed com/trd/service/communication/KeyEventCtrlService.java :: interceptKeyBeforeDispatching(android.view.KeyEvent keyEvent, int i)
- changed com/trd/service/ui/RJFocusDialog.java :: showFocus(int i)

### Added classes (sample)


### Removed classes (sample)

- com/trd/common/enums/MotorCtrlEnum.java
- com/trd/common/enums/MotorStatusEnum.java
- com/trd/framework/IMotorCtrl.java
- com/trd/sdk/MotorCtrl.java
- com/trd/sdk/impl/MotorCtrlImpl.java

## zxlauncher

One HDMI: None version None
Two HDMI: None version None
Exact dex same: False
App permissions only in 1 HDMI: 
App permissions only in 2 HDMI: 
Native loaded 1 HDMI: mmkv
Native loaded 2 HDMI: mmkv
Method change counts: {"unchanged": 71961, "changed": 6308, "added": 24802, "removed": 22178}

### Changed methods of likely hardware relevance (sample)

- removed android/support/v4/media/MediaBrowserCompat.java :: CallbackHandler(android.support.v4.media.MediaBrowserCompat.MediaBrowserServiceCallbackImpl callbackImpl)
- added android/support/v4/media/MediaBrowserCompat.java :: CallbackHandler(android.support.v4.media.MediaBrowserCompat.MediaBrowserServiceCallbackImpl mediaBrowserServiceCallbackImpl)
- removed android/support/v4/media/MediaBrowserCompat.java :: MediaBrowserCompat(android.content.Context context, android.content.ComponentName serviceComponent, android.support.v4.media.MediaBrowserCompat.ConnectionCallback callba)
- removed android/support/v4/media/MediaBrowserCompat.java :: MediaBrowserImplApi21(android.content.Context context, android.content.ComponentName serviceComponent, android.support.v4.media.MediaBrowserCompat.ConnectionCallback callba)
- removed android/support/v4/media/MediaBrowserCompat.java :: MediaBrowserImplApi23(android.content.Context context, android.content.ComponentName serviceComponent, android.support.v4.media.MediaBrowserCompat.ConnectionCallback callba)
- removed android/support/v4/media/MediaBrowserCompat.java :: MediaBrowserImplApi26(android.content.Context context, android.content.ComponentName serviceComponent, android.support.v4.media.MediaBrowserCompat.ConnectionCallback callba)
- removed android/support/v4/media/MediaBrowserCompat.java :: MediaBrowserImplBase(android.content.Context context, android.content.ComponentName serviceComponent, android.support.v4.media.MediaBrowserCompat.ConnectionCallback callba)
- added android/support/v4/media/MediaBrowserCompat.java :: MediaItem(@androidx.annotation.NonNull android.support.v4.media.MediaDescriptionCompat mediaDescriptionCompat, int i)
- added android/support/v4/media/MediaBrowserCompat.java :: getItem(@androidx.annotation.NonNull final java.lang.String str, @androidx.annotation.NonNull final android.support.v4.media.MediaBrowserCompat.ItemCallback i)
- added android/support/v4/media/MediaBrowserCompat.java :: getItem(@androidx.annotation.NonNull java.lang.String str, @androidx.annotation.NonNull android.support.v4.media.MediaBrowserCompat.ItemCallback itemCallback)
- added android/support/v4/media/MediaBrowserCompat.java :: handleMessage(@androidx.annotation.NonNull android.os.Message message)
- added android/support/v4/media/MediaBrowserCompat.java :: onChildrenLoaded(@androidx.annotation.NonNull java.lang.String str, @androidx.annotation.NonNull java.util.List<android.media.browse.MediaBrowser.MediaItem> list, @and)
- added android/support/v4/media/MediaBrowserCompat.java :: onChildrenLoaded(@androidx.annotation.NonNull java.lang.String str, @androidx.annotation.NonNull java.util.List<android.support.v4.media.MediaBrowserCompat.MediaItem> )
- added android/support/v4/media/MediaBrowserCompat.java :: onChildrenLoaded(@androidx.annotation.NonNull java.lang.String str, java.util.List<android.media.browse.MediaBrowser.MediaItem> list)
- added android/support/v4/media/MediaBrowserCompat.java :: onError(@androidx.annotation.NonNull java.lang.String str)
- added android/support/v4/media/MediaBrowserCompat.java :: onError(@androidx.annotation.NonNull java.lang.String str, @androidx.annotation.NonNull android.os.Bundle bundle)
- added android/support/v4/media/MediaBrowserCompat.java :: onError(@androidx.annotation.NonNull java.lang.String str, android.os.Bundle bundle)
- added android/support/v4/media/MediaBrowserCompat.java :: onSearchResult(@androidx.annotation.NonNull java.lang.String str, android.os.Bundle bundle, @androidx.annotation.NonNull java.util.List<android.support.v4.media.Medi)
- added android/support/v4/media/MediaBrowserCompat.java :: onServiceConnected(android.os.Messenger messenger, java.lang.String str, android.support.v4.media.session.MediaSessionCompat.Token token, android.os.Bundle bundle)
- added android/support/v4/media/MediaBrowserCompat.java :: onServiceConnected(final android.content.ComponentName componentName, final android.os.IBinder iBinder)
- removed android/support/v4/media/MediaBrowserCompat.java :: onServiceConnected(final android.content.ComponentName name, final android.os.IBinder binder)
- removed android/support/v4/media/MediaBrowserCompat.java :: onServiceConnected(final android.os.Messenger callback, final java.lang.String root, final android.support.v4.media.session.MediaSessionCompat.Token session, final andro)
- added android/support/v4/media/MediaBrowserCompat.java :: search(@androidx.annotation.NonNull final java.lang.String str, final android.os.Bundle bundle, @androidx.annotation.NonNull final android.support.v4.media.M)
- added android/support/v4/media/MediaBrowserCompat.java :: search(@androidx.annotation.NonNull java.lang.String str, android.os.Bundle bundle, @androidx.annotation.NonNull android.support.v4.media.MediaBrowserCompat.)
- added android/support/v4/media/MediaBrowserCompat.java :: sendCustomAction(@androidx.annotation.NonNull final java.lang.String str, final android.os.Bundle bundle, @androidx.annotation.Nullable final android.support.v4.media.)
- added android/support/v4/media/MediaBrowserCompat.java :: sendCustomAction(@androidx.annotation.NonNull java.lang.String str, android.os.Bundle bundle, @androidx.annotation.Nullable android.support.v4.media.MediaBrowserCompat)
- added android/support/v4/media/MediaBrowserCompat.java :: subscribe(@androidx.annotation.NonNull java.lang.String str, @androidx.annotation.NonNull android.os.Bundle bundle, @androidx.annotation.NonNull android.support)
- added android/support/v4/media/MediaBrowserCompat.java :: subscribe(@androidx.annotation.NonNull java.lang.String str, @androidx.annotation.NonNull android.support.v4.media.MediaBrowserCompat.SubscriptionCallback subsc)
- added android/support/v4/media/MediaBrowserCompat.java :: subscribe(@androidx.annotation.NonNull java.lang.String str, @androidx.annotation.Nullable android.os.Bundle bundle, @androidx.annotation.NonNull android.suppor)
- added android/support/v4/media/MediaBrowserCompat.java :: subscribe(@androidx.annotation.NonNull java.lang.String str, android.os.Bundle bundle, @androidx.annotation.NonNull android.support.v4.media.MediaBrowserCompat.)
- added android/support/v4/media/MediaBrowserCompat.java :: unsubscribe(@androidx.annotation.NonNull java.lang.String str)
- added android/support/v4/media/MediaBrowserCompat.java :: unsubscribe(@androidx.annotation.NonNull java.lang.String str, @androidx.annotation.NonNull android.support.v4.media.MediaBrowserCompat.SubscriptionCallback subsc)
- added android/support/v4/media/MediaBrowserCompat.java :: unsubscribe(@androidx.annotation.NonNull java.lang.String str, android.support.v4.media.MediaBrowserCompat.SubscriptionCallback subscriptionCallback)
- added android/support/v4/media/MediaDescriptionCompat.java :: setDescription(@androidx.annotation.Nullable java.lang.CharSequence charSequence)
- added android/support/v4/media/MediaDescriptionCompat.java :: setDescription(android.media.MediaDescription.Builder builder, @androidx.annotation.Nullable java.lang.CharSequence charSequence)
- added android/support/v4/media/MediaDescriptionCompat.java :: setExtras(@androidx.annotation.Nullable android.os.Bundle bundle)
- added android/support/v4/media/MediaDescriptionCompat.java :: setExtras(android.media.MediaDescription.Builder builder, @androidx.annotation.Nullable android.os.Bundle bundle)
- added android/support/v4/media/MediaDescriptionCompat.java :: setIconBitmap(@androidx.annotation.Nullable android.graphics.Bitmap bitmap)
- added android/support/v4/media/MediaDescriptionCompat.java :: setIconBitmap(android.media.MediaDescription.Builder builder, @androidx.annotation.Nullable android.graphics.Bitmap bitmap)
- added android/support/v4/media/MediaDescriptionCompat.java :: setIconUri(@androidx.annotation.Nullable android.net.Uri uri)
- added android/support/v4/media/MediaDescriptionCompat.java :: setIconUri(android.media.MediaDescription.Builder builder, @androidx.annotation.Nullable android.net.Uri uri)
- added android/support/v4/media/MediaDescriptionCompat.java :: setMediaId(@androidx.annotation.Nullable java.lang.String str)
- added android/support/v4/media/MediaDescriptionCompat.java :: setMediaId(android.media.MediaDescription.Builder builder, @androidx.annotation.Nullable java.lang.String str)
- added android/support/v4/media/MediaDescriptionCompat.java :: setMediaUri(@androidx.annotation.Nullable android.net.Uri uri)
- added android/support/v4/media/MediaDescriptionCompat.java :: setMediaUri(android.media.MediaDescription.Builder builder, @androidx.annotation.Nullable android.net.Uri uri)
- added android/support/v4/media/MediaDescriptionCompat.java :: setSubtitle(@androidx.annotation.Nullable java.lang.CharSequence charSequence)
- added android/support/v4/media/MediaDescriptionCompat.java :: setSubtitle(android.media.MediaDescription.Builder builder, @androidx.annotation.Nullable java.lang.CharSequence charSequence)
- added android/support/v4/media/MediaDescriptionCompat.java :: setTitle(@androidx.annotation.Nullable java.lang.CharSequence charSequence)
- added android/support/v4/media/MediaDescriptionCompat.java :: setTitle(android.media.MediaDescription.Builder builder, @androidx.annotation.Nullable java.lang.CharSequence charSequence)
- removed android/support/v4/media/MediaMetadataCompat.java :: Builder(android.support.v4.media.MediaMetadataCompat source)
- removed android/support/v4/media/MediaMetadataCompat.java :: Builder(android.support.v4.media.MediaMetadataCompat source, int maxBitmapSize)

### Added classes (sample)

- androidx/exifinterface/R.java
- androidx/exifinterface/media/ExifInterface.java
- com/android/media/api/service_api/utils/Consts1.java
- com/bumptech/glide/load/data/ParcelFileDescriptorRewinder.java
- com/bumptech/glide/load/model/stream/QMediaStoreUriLoader.java
- com/bumptech/glide/load/resource/bitmap/ImageReader.java
- com/bumptech/glide/load/resource/bitmap/ParcelFileDescriptorBitmapDecoder.java
- com/google/ads/interactivemedia/pal/zzb.java
- com/google/ads/interactivemedia/pal/zze.java
- com/google/ads/interactivemedia/pal/zzh.java
- com/google/ads/interactivemedia/pal/zzn.java
- com/google/android/exoplayer2/BuildConfig.java
- com/google/android/exoplayer2/R.java
- com/google/android/exoplayer2/offline/$$Lambda$DownloadManager$InternalHandler$NXQcmC9peGeDWV0s_8pBfzdJpS0.java
- com/google/android/exoplayer2/offline/$$Lambda$_14QHG018Z6p13d3hzJuGTWnNeo.java
- com/google/android/exoplayer2/source/dash/BaseUrlExclusionList.java
- com/google/android/exoplayer2/source/dash/BuildConfig.java
- com/google/android/exoplayer2/source/dash/DashChunkSource.java
- com/google/android/exoplayer2/source/dash/DashManifestStaleException.java
- com/google/android/exoplayer2/source/dash/DashMediaPeriod.java
- com/google/android/exoplayer2/source/dash/DashMediaSource.java
- com/google/android/exoplayer2/source/dash/DashSegmentIndex.java
- com/google/android/exoplayer2/source/dash/DashUtil.java
- com/google/android/exoplayer2/source/dash/DashWrappingSegmentIndex.java
- com/google/android/exoplayer2/source/dash/DefaultDashChunkSource.java
- com/google/android/exoplayer2/source/dash/EventSampleStream.java
- com/google/android/exoplayer2/source/dash/PlayerEmsgHandler.java
- com/google/android/exoplayer2/source/dash/R.java
- com/google/android/exoplayer2/source/dash/manifest/AdaptationSet.java
- com/google/android/exoplayer2/source/dash/manifest/BaseUrl.java
- com/google/android/exoplayer2/source/dash/manifest/DashManifest.java
- com/google/android/exoplayer2/source/dash/manifest/DashManifestParser.java
- com/google/android/exoplayer2/source/dash/manifest/Descriptor.java
- com/google/android/exoplayer2/source/dash/manifest/EventStream.java
- com/google/android/exoplayer2/source/dash/manifest/Period.java
- com/google/android/exoplayer2/source/dash/manifest/ProgramInformation.java
- com/google/android/exoplayer2/source/dash/manifest/RangedUri.java
- com/google/android/exoplayer2/source/dash/manifest/Representation.java
- com/google/android/exoplayer2/source/dash/manifest/SegmentBase.java
- com/google/android/exoplayer2/source/dash/manifest/ServiceDescriptionElement.java

### Removed classes (sample)

- com/android/media/api/service_api/net/BodyHttpLoggingInterceptor.java
- com/google/ads/interactivemedia/pal/AutoValue_NonceRequest.java
- com/google/ads/interactivemedia/pal/GoogleAdManagerSettings.java
- com/google/ads/interactivemedia/pal/zza.java
- com/google/ads/interactivemedia/pal/zzal.java
- com/google/ads/interactivemedia/pal/zzay.java
- com/google/ads/interactivemedia/pal/zzc.java
- com/google/ads/interactivemedia/pal/zzf.java
- com/google/ads/interactivemedia/pal/zzi.java
- com/google/ads/interactivemedia/pal/zzm.java
- com/google/android/exoplayer2/offline/$$Lambda$DownloadManager$InternalHandler$OH12G4NteYquO91EYFUVBFeiK8.java
- com/google/android/exoplayer2/offline/$$Lambda$PNiE7SuEFxRjAZH7pJpZIFOFjWg.java
- com/google/android/gms/common/PackageSignatureVerifier.java
- com/google/android/gms/common/PackageVerificationResult.java
- com/google/android/gms/common/api/OptionalModuleApi.java
- com/google/android/gms/common/internal/CallbackExecutor.java
- com/google/android/gms/common/internal/zzah.java
- com/google/android/gms/common/internal/zzal.java
- com/google/android/gms/common/internal/zzp.java
- com/google/android/gms/common/moduleinstall/InstallStatusListener.java
- com/google/android/gms/common/moduleinstall/ModuleAvailabilityResponse.java
- com/google/android/gms/common/moduleinstall/ModuleInstall.java
- com/google/android/gms/common/moduleinstall/ModuleInstallClient.java
- com/google/android/gms/common/moduleinstall/ModuleInstallIntentResponse.java
- com/google/android/gms/common/moduleinstall/ModuleInstallRequest.java
- com/google/android/gms/common/moduleinstall/ModuleInstallResponse.java
- com/google/android/gms/common/moduleinstall/ModuleInstallStatusCodes.java
- com/google/android/gms/common/moduleinstall/ModuleInstallStatusUpdate.java
- com/google/android/gms/common/moduleinstall/internal/ApiFeatureRequest.java
- com/google/android/gms/common/moduleinstall/internal/zaa.java
- com/google/android/gms/common/moduleinstall/internal/zaaa.java
- com/google/android/gms/common/moduleinstall/internal/zaab.java
- com/google/android/gms/common/moduleinstall/internal/zac.java
- com/google/android/gms/common/moduleinstall/internal/zad.java
- com/google/android/gms/common/moduleinstall/internal/zae.java
- com/google/android/gms/common/moduleinstall/internal/zaf.java
- com/google/android/gms/common/moduleinstall/internal/zag.java
- com/google/android/gms/common/moduleinstall/internal/zah.java
- com/google/android/gms/common/moduleinstall/internal/zaq.java
- com/google/android/gms/common/moduleinstall/internal/zar.java

## zxstore

One HDMI: None version None
Two HDMI: None version None
Exact dex same: False
App permissions only in 1 HDMI: 
App permissions only in 2 HDMI: 
Native loaded 1 HDMI: mmkv
Native loaded 2 HDMI: mmkv
Method change counts: {"unchanged": 99785, "changed": 1118, "added": 3930, "removed": 868}

### Changed methods of likely hardware relevance (sample)

- changed androidx/core/graphics/TypefaceCompat.java :: createFromResourcesFamilyXml(android.content.Context context, androidx.core.content.res.FontResourcesParserCompat.FamilyResourceEntry familyResourceEntry, android.content.res.Reso)
- changed androidx/core/graphics/TypefaceCompatBaseImpl.java :: findBestFont(T[] tArr, int i, androidx.core.graphics.TypefaceCompatBaseImpl.StyleExtractor<T> styleExtractor)
- changed androidx/core/view/ViewCompat.java :: getAvailableActionIdFromResources(android.view.View view, java.lang.CharSequence charSequence)
- changed androidx/core/widget/NestedScrollView.java :: findFocusableViewInBounds(boolean z, int i, int i2)
- changed androidx/leanback/widget/DetailsOverviewRowPresenter.java :: bindImageDrawable(androidx.leanback.widget.DetailsOverviewRowPresenter.ViewHolder viewHolder)
- removed androidx/leanback/widget/GuidedActionAdapterGroup.java :: focusToNextAction(androidx.leanback.widget.GuidedActionAdapter r7, androidx.leanback.widget.GuidedAction r8, long r9)
- added androidx/leanback/widget/GuidedActionAdapterGroup.java :: focusToNextAction(androidx.leanback.widget.GuidedActionAdapter r8, androidx.leanback.widget.GuidedAction r9, long r10)
- changed com/alibaba/fastjson/util/TypeUtils.java :: computeGetters(java.lang.Class<?> cls, com.alibaba.fastjson.annotation.JSONType jSONType, java.util.Map<java.lang.String, java.lang.String> map, java.util.Map<java.l)
- changed com/bumptech/glide/load/resource/bitmap/StreamBitmapDecoder.java :: decode(java.io.InputStream inputStream, int i, int i2, com.bumptech.glide.load.Options options)

### Added classes (sample)

- com/google/zxing/client/android/AmbientLightManager.java
- com/google/zxing/client/android/BeepManager.java
- com/google/zxing/client/android/BuildConfig.java
- com/google/zxing/client/android/DecodeFormatManager.java
- com/google/zxing/client/android/DecodeHintManager.java
- com/google/zxing/client/android/InactivityTimer.java
- com/google/zxing/client/android/Intents.java
- com/google/zxing/client/android/R.java
- com/google/zxing/client/android/camera/open/OpenCameraInterface.java
- com/google/zxing/integration/android/IntentIntegrator.java
- com/google/zxing/integration/android/IntentResult.java
- com/journeyapps/barcodescanner/BarcodeCallback.java
- com/journeyapps/barcodescanner/BarcodeEncoder.java
- com/journeyapps/barcodescanner/BarcodeResult.java
- com/journeyapps/barcodescanner/BarcodeView.java
- com/journeyapps/barcodescanner/CameraPreview.java
- com/journeyapps/barcodescanner/CaptureActivity.java
- com/journeyapps/barcodescanner/CaptureManager.java
- com/journeyapps/barcodescanner/CompoundBarcodeView.java
- com/journeyapps/barcodescanner/Decoder.java
- com/journeyapps/barcodescanner/DecoderFactory.java
- com/journeyapps/barcodescanner/DecoderResultPointCallback.java
- com/journeyapps/barcodescanner/DecoderThread.java
- com/journeyapps/barcodescanner/DecoratedBarcodeView.java
- com/journeyapps/barcodescanner/DefaultDecoderFactory.java
- com/journeyapps/barcodescanner/InvertedDecoder.java
- com/journeyapps/barcodescanner/MixedDecoder.java
- com/journeyapps/barcodescanner/RawImageData.java
- com/journeyapps/barcodescanner/RotationCallback.java
- com/journeyapps/barcodescanner/RotationListener.java
- com/journeyapps/barcodescanner/ScanContract.java
- com/journeyapps/barcodescanner/ScanIntentResult.java
- com/journeyapps/barcodescanner/ScanOptions.java
- com/journeyapps/barcodescanner/Size.java
- com/journeyapps/barcodescanner/SourceData.java
- com/journeyapps/barcodescanner/Util.java
- com/journeyapps/barcodescanner/ViewfinderView.java
- com/journeyapps/barcodescanner/camera/AutoFocusManager.java
- com/journeyapps/barcodescanner/camera/CameraConfigurationUtils.java
- com/journeyapps/barcodescanner/camera/CameraInstance.java

### Removed classes (sample)

- com/zeasn/ad/connector/BuildConfig.java
- com/zeasn/ad/connector/R.java
- com/zeasn/ad/connector/ZeasnAd.java
- com/zeasn/ad/connector/ZeasnAdSDK.java
- com/zeasn/ad/connector/ZeasnBaseAd.java
- com/zeasn/ad/connector/bean/AdBean.java
- com/zeasn/ad/connector/bean/ZeasnAdInfo.java
- com/zeasn/ad/connector/bean/ZeasnError.java
- com/zeasn/ad/connector/control/AdConstant.java
- com/zeasn/ad/connector/control/PluginAdControl.java
- com/zeasn/ad/connector/impl/AdType.java
- com/zeasn/ad/connector/impl/ErrType.java
- com/zeasn/ad/connector/impl/PluginAdInterface.java
- com/zeasn/ad/connector/impl/PluginAdListener.java
- com/zeasn/ad/connector/impl/PluginPlayerControl.java
- com/zeasn/ad/connector/impl/ZeasnAdKey.java
- com/zeasn/ad/connector/listener/ZeasnListener.java
- com/zeasn/ad/connector/listener/ZeasnOnVodListener.java
- com/zeasn/ad/connector/listener/ZeasnSkipListener.java
- com/zeasn/ad/connector/utils/LogUtil.java
- com/zeasn/ad/connector/utils/Observer.java
- com/zeasn/ad/connector/utils/ResourceUtils.java
- com/zeasn/ad/connector/utils/SharedPreferencesUtils.java
- com/zeasn/ad/connector/utils/Subject.java
- com/zeasn/dpapi/base/DateHolder.java
- com/zeasn/dpapi/base/interceptor/DateInterceptor.java
- com/zeasn/dpapi/base/interceptor/UserAgentInterceptor.java
- com/zeasn/plugin/BroadcastMonitor.java
- com/zeasn/plugin/BuildConfig.java
- com/zeasn/plugin/PluginInit.java
- com/zeasn/plugin/PluginLoad.java
- com/zeasn/plugin/PluginUpdate.java
- com/zeasn/plugin/ProxyContentProvider.java
- com/zeasn/plugin/ProxyService.java
- com/zeasn/plugin/R.java
- com/zeasn/plugin/ReceiverHookManager.java
- com/zeasn/plugin/SharedPreferencesUtils.java
- com/zeasn/plugin/StubActivity.java
- com/zeasn/plugin/Utils.java
- com/zeasn/plugin/base/Const.java

## Source limitations

This is code-level **static** evidence. It does not show a class actually runs, its effectiveness on hardware, or the reason a binary was rebuilt.
Dependency-safe removal and HDMI cross-compatibility require ADB runtime traces and hardware tests.


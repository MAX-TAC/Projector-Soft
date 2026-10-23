# Static engineering overview of manufacturer projector APKs

Source: official two-HDMI CineLux firmware. Decompiled code was inspected only to measure API usage.
No APK was executed and no decompiled proprietary source is published.
Counts indicate occurrences in reconstructed source, not necessarily active runtime execution.

| APK | Package | DEX | Classes | JNI load | Binder/HIDL | MTK API | ToF/focus | Keystone | HDMI/source |
|---|---|---:|---:|---:|---:|---:|---:|---:|---:|
| KuSettings | ? | 15 | 3470 | 0 | 81 | 13 | 53 | 14 | 28 |
| KeystoneCorrection | name='com.rjpj.tcorrection' | 1 | 449 | 1 | 18 | 0 | 12 | 86 | 1 |
| RuiAutoFocus | ? | 7 | 1091 | 5 | 764 | 11611 | 174 | 66 | 162 |
| KuSourceRJ | name='com.coolux.source' | 1 | 3007 | 0 | 296 | 0 | 103 | 30 | 190 |
| trdservice | name='com.trd.service' | 1 | 438 | 1 | 54 | 423 | 12 | 0 | 1 |
| trdmiddle | name='com.trd.framework' | 1 | 568 | 5 | 539 | 11679 | 74 | 61 | 159 |
| TvSettingsPlus | ? | 16 | 5796 | 4 | 1035 | 14510 | 205 | 1037 | 461 |
| zxlauncher | ? | 3 | 11007 | 19 | 539 | 1 | 270 | 0 | 72 |

## Per-app findings

### KuSettings
Package: Unknown
Permissions: 
Loaded JNI libraries: 

keystone: com/p001my/utils/IntentUtils.java, com/p001my/settings/C0950R.java, com/p001my/settings/SettingActivity.java, com/p001my/service/ScreenServices.java
mtk_api: com/p001my/service/ScreenServices.java, com/google/android/material/color/utilities/Cam16.java, androidx/core/content/res/CamColor.java
input_source: com/p001my/settingslib/bluetooth/LocalBluetoothLeBroadcastAssistant.java, com/p001my/settingslib/bluetooth/LocalBluetoothLeBroadcastMetadata.java, com/bumptech/glide/GlideBuilder.java, androidx/viewpager2/widget/ViewPager2.java, androidx/customview/widget/ExploreByTouchHelper.java, androidx/core/content/IntentSanitizer.java, androidx/core/view/ContentInfoCompat.java, androidx/core/view/ViewCompat.java, androidx/core/view/accessibility/AccessibilityRecordCompat.java, androidx/core/view/accessibility/AccessibilityNodeInfoCompat.java
autofocus: com/p001my/settings/widget/SwitchBar.java, com/google/android/material/navigation/NavigationBarItemView.java, com/google/android/material/timepicker/ClockFaceView.java, com/google/android/material/tabs/TabLayout.java, com/google/android/material/textfield/EndCompoundLayout.java, com/google/android/material/textfield/IconHelper.java, com/google/android/material/snackbar/BaseTransientBottomBar.java, com/google/android/material/search/SearchBar.java, com/google/android/material/search/SearchViewAnimationHelper.java, com/google/android/material/slider/RangeSlider.java

### KeystoneCorrection
Package: package: name='com.rjpj.tcorrection' versionCode='33' versionName='13' platformBuildVersionName='13' platformBuildVersionCode='33' compileSdkVersion='33' compileSdkVersionCodename='13'
Permissions: uses-permission: name='androidx.constraintlayout.widget.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION'
Loaded JNI libraries: i2c_jni

keystone: com/rjpj/tcorrection/KeystoneUtil.java, com/rjpj/tcorrection/HVKeystoneActivity.java, com/rjpj/tcorrection/TCorrectionBroadcastReceiver.java, com/rjpj/tcorrection/DigitalZoomActivity.java, com/rjpj/tcorrection/FourCornerActivity.java, com/rjpj/tcorrection/R.java
hardware_bridge: com/rjpj/tcorrection/KeystoneUtil.java, com/rjpj/tcorrection/I2CDevice.java
jni_load: com/rjpj/tcorrection/I2CDevice.java
native_methods: com/rjpj/tcorrection/I2CDevice.java
autofocus: androidx/appcompat/app/AlertController.java, androidx/appcompat/widget/ActionMenuPresenter.java, androidx/appcompat/widget/SearchView.java, androidx/appcompat/widget/AppCompatEmojiEditTextHelper.java, androidx/appcompat/widget/ListPopupWindow.java, androidx/fragment/app/Fragment.java, androidx/fragment/app/SpecialEffectsController.java, androidx/fragment/app/FragmentStateManager.java, androidx/core/widget/NestedScrollView.java
input_source: androidx/core/view/ViewCompat.java

### RuiAutoFocus
Package: Unknown
Permissions: uses-permission: name='android.permission.READ_EXTERNAL_STORAGE', uses-permission: name='android.permission.RECEIVE_BOOT_COMPLETED', uses-permission: name='android.permission.READ_EXTERNAL_STORAGE', uses-permission: name='android.permission.WRITE_EXTERNAL_STORAGE', uses-permission: name='android.permission.INTERNET', uses-permission: name='android.permission.WRITE_EXTERNAL_STORAGE', uses-permission: name='android.permission.READ_EXTERNAL_STORAGE', uses-permission: name='android.permission.ACCESS_NETWORK_STATE', uses-permission: name='android.permission.ACCESS_COARSE_LOCATION', uses-permission: name='android.permission.ACCESS_FINE_LOCATION', uses-permission: name='android.permission.WRITE_EXTERNAL_STORAGE', uses-permission: name='android.permission.DISABLE_KEYGUARD', uses-permission: name='android.permission.SYSTEM_ALERT_WINDOW', uses-permission: name='android.permission.SYSTEM_OVERLAY_WINDOW', uses-permission: name='android.permission.SYSTEM_ALERT_WINDOW', uses-permission: name='android.permission.SENSOR_INFO', uses-permission: name='android.permission.SENSOR_ENABLE', uses-permission: name='android.permission.ACTIVITY_RECOGNITION', uses-permission: name='android.permission.INTERACT_ACROSS_USERS', uses-permission: name='android.permission.INJECT_EVENTS', uses-permission: name='android.permission.INTERACT_ACROSS_USERS'
Loaded JNI libraries: com_mediatek_twoworlds_tv_jni, focusvahysd, ruicameraapi_jni

mtk_api: org/ini4j/ConfigParser.java, com/mediatek/twoworlds/factory/MtkTvFApiSystem.java, com/mediatek/twoworlds/factory/MtkTvFApiNetwork.java, com/mediatek/twoworlds/factory/MtkTvFApiCustomEventManager.java, com/mediatek/twoworlds/factory/MtkTvFApiPeripheralBase.java, com/mediatek/twoworlds/factory/MtkTvFApiDisplayBase.java, com/mediatek/twoworlds/factory/MtkTvFApiInformation.java, com/mediatek/twoworlds/factory/MtkTvFApiInputSource.java, com/mediatek/twoworlds/factory/MtkTvFApiTvBase.java, com/mediatek/twoworlds/factory/MtkTvFApiDisplay.java
runtime_exec: org/ini4j/Reg.java, com/trd/common/utils/FileUtil.java
input_source: com/mediatek/twoworlds/factory/MtkTvFApiPeripheralBase.java, com/mediatek/twoworlds/factory/MtkTvFApiDisplayBase.java, com/mediatek/twoworlds/factory/MtkTvFApiAudioBase.java, com/mediatek/twoworlds/factory/MtkTvFApiInputSourceBase.java, com/mediatek/twoworlds/p002tv/MtkTvRecordAdaptorBase.java, com/mediatek/twoworlds/p002tv/TVNativeWrapper.java, com/mediatek/twoworlds/p002tv/MtkTvInputSourceBase.java, com/mediatek/twoworlds/p002tv/TVNative.java, com/mediatek/twoworlds/factory/common/MtkTvFApiDisplayTypes.java, com/mediatek/twoworlds/factory/common/MtkTvFApiAudioTypes.java
hardware_bridge: com/mediatek/twoworlds/factory/MtkTvFApiPeripheralBase.java, com/trd/framework/ISystemCtrl.java, com/trd/sdk/impl/SystemCtrlImpl.java
jni_load: com/mediatek/twoworlds/p002tv/TVNativeCustom.java, com/mediatek/twoworlds/p002tv/TVNative.java, com/rui/cameraapi/MotorJNI.java, com/rui/cameraapi/PhioJNI.java, com/hysd/vafocus/HYSDVAF2.java
native_methods: com/mediatek/twoworlds/p002tv/TVNativeCustom.java, com/mediatek/twoworlds/p002tv/TVNative.java, com/rui/cameraapi/MotorJNI.java, com/rui/cameraapi/PhioJNI.java, com/hysd/vafocus/HYSDVAF2.java
autofocus: com/hysd/vafocus/HYSDVAF2.java, com/hysd/xymscreen/MainActivity.java, com/hysd/p001ai/AiTools.java, com/hysd/p001ai/RuiCameraApi.java, com/hysd/client/VAF2S.java, com/hysd/client/HYSDkeystone.java, com/trd/framework/IProjectorCtrl.java, com/trd/framework/ITofDataListener.java, com/trd/sdk/impl/TofCtrlImpl.java, android/support/v7/app/AlertController.java
keystone: com/hysd/vafocus/HYSDVAF2.java, com/hysd/xymscreen/MainActivity.java, com/hysd/p001ai/AiTools.java, com/hysd/p001ai/RuiCameraApi.java, com/hysd/client/ERRORCODE.java, com/hysd/client/VAF2S.java, com/hysd/client/HYSDkeystone.java, com/trd/framework/IProjectorCtrl.java, com/trd/sdk/ProjectorCtrl.java, com/trd/sdk/impl/ProjectorCtrlImpl.java

### KuSourceRJ
Package: package: name='com.coolux.source' versionCode='41' versionName='5.3' platformBuildVersionName='15' platformBuildVersionCode='35' compileSdkVersion='35' compileSdkVersionCodename='15'
Permissions: uses-permission: name='android.permission.CHANGE_NETWORK_STATE', uses-permission: name='android.permission.ACCESS_WIFI_STATE', uses-permission: name='android.permission.CHANGE_WIFI_STATE', uses-permission: name='android.permission.INTERNET', uses-permission: name='android.permission.ACCESS_NETWORK_STATE', uses-permission: name='android.permission.TV_INPUT_HARDWARE', uses-permission: name='android.permission.BIND_TV_INPUT', uses-permission: name='android.permission.WRITE_EPG_DATA', uses-permission: name='com.android.providers.tv.permission.WRITE_EPG_DATA', uses-permission: name='com.coolux.source.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION'
Loaded JNI libraries: 

autofocus: com/coolux/source/C0537R.java, com/coolux/source/utils/ImageUtils.java, com/coolux/source/activity/KeystoneActivity.java, com/google/android/material/navigation/NavigationBarItemView.java, com/google/android/material/timepicker/ClockFaceView.java, com/google/android/material/tabs/TabLayout.java, com/google/android/material/textfield/EndCompoundLayout.java, com/google/android/material/textfield/IconHelper.java, com/google/android/material/snackbar/BaseTransientBottomBar.java, com/google/android/material/search/SearchBar.java
keystone: com/coolux/source/C0537R.java, com/coolux/source/utils/IntentUtils.java, com/coolux/source/activity/KeystoneActivity.java, com/trd/framework/IProjectorCtrl.java, com/trd/sdk/ProjectorCtrl.java, com/trd/sdk/impl/ProjectorCtrlImpl.java
input_source: com/coolux/source/C0537R.java, com/coolux/source/MainActivity.java, com/coolux/source/constant/Constans.java, com/coolux/source/utils/ImageUtils.java, com/coolux/source/activity/BootSoureActivity.java, com/coolux/source/activity/BootSoureActivityOld.java, com/coolux/source/activity/testcast/StandbyActivity.java, com/coolux/source/activity/testcast/InterfaceActivity.java, com/trd/common/enums/SourceItem.java, com/trd/common/bean/TrdTvInputSourceInfo.java
runtime_exec: com/coolux/source/utils/UtilsTools.java, com/trd/common/utils/FileUtil.java

### trdservice
Package: package: name='com.trd.service' versionCode='33' versionName='13' platformBuildVersionName='13' platformBuildVersionCode='33' compileSdkVersion='33' compileSdkVersionCodename='13'
Permissions: uses-permission: name='android.permission.BLUETOOTH_CONNECT', uses-permission: name='android.permission.INTERNET', uses-permission: name='android.permission.ACCESS_WIFI_STATE', uses-permission: name='android.permission.READ_EXTERNAL_STORAGE', uses-permission: name='android.Manifest.permission.WRITE_DREAM_STATE', uses-permission: name='android.permission.HDMI_CEC', uses-permission: name='android.permission.ACCESS_TUNED_INFO', uses-permission: name='android.permission.SYSTEM_ALERT_WINDOW', uses-permission: name='androidx.constraintlayout.widget.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION'
Loaded JNI libraries: rjpj_private_function

mtk_api: com/mediatek/twoworlds/factory/MtkTvFApiInformation.java, com/mediatek/twoworlds/factory/MtkTvFApiInformationBase.java, com/mediatek/twoworlds/factory/MtkTvFApiEventManager.java, com/mediatek/twoworlds/factory/common/IMtkTvFApiEventListener.java, com/mediatek/twoworlds/factory/interfaces/IMtkTvFApiInformationWrapper.java, com/mediatek/twoworlds/factory/callbackservice/MtkTvFApiCallbackManagerImp.java, com/mediatek/twoworlds/factory/model/MtkTvFApiEventCommonObject.java, com/mediatek/twoworlds/factory/clientwrapper/MtkTvFApiManagerProxy.java, com/mediatek/twoworlds/factory/clientwrapper/MtkTvFApiInformationWrapper.java, com/trd/service/TrdService.java
jni_load: com/trd/service/jni/MotorJNI.java
native_methods: com/trd/service/jni/MotorJNI.java
runtime_exec: com/trd/service/savemac/SaveMacToUsb.java
autofocus: androidx/appcompat/app/AlertController.java, androidx/appcompat/widget/ActionMenuPresenter.java, androidx/appcompat/widget/SearchView.java, androidx/appcompat/widget/AppCompatEmojiEditTextHelper.java, androidx/appcompat/widget/ListPopupWindow.java, androidx/fragment/app/Fragment.java, androidx/fragment/app/SpecialEffectsController.java, androidx/fragment/app/FragmentStateManager.java, androidx/core/widget/NestedScrollView.java
input_source: androidx/core/view/ViewCompat.java

### trdmiddle
Package: package: name='com.trd.framework' versionCode='1' versionName='1.0' platformBuildVersionName='13' platformBuildVersionCode='33' compileSdkVersion='33' compileSdkVersionCodename='13'
Permissions: uses-permission: name='android.permission.BLUETOOTH_CONNECT', uses-permission: name='android.permission.INTERNET', uses-permission: name='android.permission.READ_EXTERNAL_STORAGE', uses-permission: name='android.Manifest.permission.WRITE_DREAM_STATE', uses-permission: name='android.permission.SYSTEM_ALERT_WINDOW'
Loaded JNI libraries: com_mediatek_twoworlds_tv_jni, tof_jni, trd_jni, trdmiddle

runtime_exec: p006e/C0062c.java
mtk_api: p006e/C0062c.java, p017p/C0088a.java, p017p/C0092e.java, p012k/BinderC0074a.java, com/mediatek/twoworlds/p003tv/MtkTvMdsBase.java, com/mediatek/twoworlds/p003tv/MtkTvCfgParser.java, com/mediatek/twoworlds/p003tv/MtkTvEventATSC.java, com/mediatek/twoworlds/p003tv/MtkTvSubtitleBase.java, com/mediatek/twoworlds/p003tv/MtkTvScanISDBBase.java, com/mediatek/twoworlds/p003tv/MtkTvConfigBase.java
keystone: p006e/C0062c.java, p010i/BinderC0072a.java, com/trd/framework/InterfaceC0044c.java, com/trd/framework/projector/TrapezoidUtil.java
input_source: p006e/C0062c.java, com/mediatek/twoworlds/p003tv/MtkTvRecordAdaptorBase.java, com/mediatek/twoworlds/p003tv/TVNativeWrapper.java, com/mediatek/twoworlds/p003tv/MtkTvInputSourceBase.java, com/mediatek/twoworlds/p003tv/TVNative.java, com/mediatek/twoworlds/factory/MtkTvFApiPeripheralBase.java, com/mediatek/twoworlds/factory/MtkTvFApiDisplayBase.java, com/mediatek/twoworlds/factory/MtkTvFApiAudioBase.java, com/mediatek/twoworlds/factory/MtkTvFApiInputSourceBase.java, com/mediatek/twoworlds/p003tv/common/MtkTvConfigTypeBase.java
jni_load: p009h/C0071a.java, com/mediatek/twoworlds/p003tv/TVNativeCustom.java, com/mediatek/twoworlds/p003tv/TVNative.java, com/trd/android/api/TrdManager.java
autofocus: p009h/C0071a.java, p015n/C0086b.java, p010i/BinderC0072a.java, com/trd/framework/InterfaceC0045d.java, com/trd/framework/TrdSystemService.java, com/trd/framework/InterfaceC0049h.java, com/trd/framework/InterfaceC0052k.java, com/trd/framework/InterfaceC0044c.java, com/trd/framework/tof/TofMessage.java, com/trd/framework/tof/TofCtrlService.java
native_methods: com/mediatek/twoworlds/p003tv/TVNativeCustom.java, com/mediatek/twoworlds/p003tv/TVNative.java, com/trd/framework/tof/TofController.java, com/trd/android/api/TrdManager.java
hardware_bridge: com/mediatek/twoworlds/factory/MtkTvFApiPeripheralBase.java

### TvSettingsPlus
Package: Unknown
Permissions: uses-permission: name='android.permission.WRITE_MEDIA_STORAGE', uses-permission: name='android.permission.MANAGE_EXTERNAL_STORAGE', uses-permission: name='android.permission.WRITE_SETTINGS', uses-permission: name='android.permission.WRITE_SECURE_SETTINGS', uses-permission: name='android.permission.REQUEST_DELETE_PACKAGES', uses-permission: name='android.permission.QUERY_ALL_PACKAGES', uses-permission: name='com.google.android.assistant.READ_HOTWORD_STATE', uses-permission: name='com.google.android.assistant.WRITE_HOTWORD_STATE', uses-permission: name='android.permission.MANAGE_SLICE_PERMISSIONS', uses-permission: name='com.netflix.ninja.permission.ESN', uses-permission: name='com.netflix.ninja.permission.ESN_RESPONSE', uses-permission: name='com.mediatek.tv.permission.BROADCAST', uses-permission: name='android.permission.TV_INPUT_HARDWARE', uses-permission: name='android.permission.BIND_TV_INPUT', uses-permission: name='android.permission.WRITE_EPG_DATA', uses-permission: name='com.android.providers.tv.permission.WRITE_EPG_DATA', uses-permission: name='android.permission.MANAGE_SENSOR_PRIVACY', uses-permission: name='android.permission.OBSERVE_SENSOR_PRIVACY', uses-permission: name='android.permission.START_ACTIVITIES_FROM_BACKGROUND', uses-permission: name='android.permission.SET_UNRESTRICTED_KEEP_CLEAR_AREAS', uses-permission: name='androidx.legacy.preference.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION', uses-permission: name='com.android.tv.twopanelsettings.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION', uses-permission: name='androidx.recyclerview.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION', uses-permission: name='androidx.preference.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION', uses-permission: name='androidx.appcompat.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION'
Loaded JNI libraries: com_mediatek_net_jni, com_mediatek_twoworlds_tv_jni, settingsi2c_jni

mtk_api: okio/Utf8.java, okio/Buffer.java, kotlin/UByte.java, kotlin/Triple.java, kotlin/TuplesKt.java, kotlin/ULong.java, kotlin/DeepRecursiveScopeImpl.java, kotlin/UInt.java, kotlin/NumbersKt__BigDecimalsKt.java, kotlin/UShort.java
input_source: okio/Pipe.java, com/mediatek/p005tv/p006ui/C2209R.java, com/mediatek/support/androidversioncompatiblelib/CompatibleApis.java, com/mediatek/support/sharecode/C2207R.java, com/mediatek/twoworlds/factory/MtkTvFApiPeripheralBase.java, com/mediatek/twoworlds/factory/MtkTvFApiDisplayBase.java, com/mediatek/twoworlds/factory/MtkTvFApiAudioBase.java, com/mediatek/twoworlds/factory/MtkTvFApiInputSourceBase.java, com/mediatek/twoworlds/p007tv/MtkTvRecordAdaptorBase.java, com/mediatek/twoworlds/p007tv/TVNativeWrapper.java
native_methods: com/mediatek/p004dm/DMServer.java, com/mediatek/net/MtkNetworkNative.java, com/mediatek/twoworlds/p007tv/TVNativeCustom.java, com/mediatek/twoworlds/p007tv/TVNative.java, com/android/p002tv/settings/uicustom/I2CDevice.java
jni_load: com/mediatek/net/MtkNetworkNative.java, com/mediatek/twoworlds/p007tv/TVNativeCustom.java, com/mediatek/twoworlds/p007tv/TVNative.java, com/android/p002tv/settings/uicustom/I2CDevice.java
keystone: com/mediatek/p005tv/p006ui/C2209R.java, com/mediatek/support/sharecode/C2207R.java, com/google/android/setupdesign/C2155R.java, com/google/android/setupcompat/C2138R.java, com/google/android/material/C1851R.java, com/airbnb/lottie/C1016R.java, com/android/settingslib/C1040R.java, com/android/wifitrackerlib/C1850R.java, com/android/settingslib/utils/C1116R.java, com/android/settingslib/collapsingtoolbar/C1081R.java
autofocus: com/mediatek/p005tv/p006ui/pindialog/Picker.java, com/mediatek/p005tv/p006ui/pindialog/PinPicker.java, com/google/android/setupdesign/view/CheckableLinearLayout.java, com/google/android/setupdesign/view/RichTextView.java, com/google/android/setupdesign/items/ExpandableSwitchItem.java, com/google/android/setupdesign/items/ItemViewHolder.java, com/google/android/setupdesign/accessibility/LinkAccessibilityHelper.java, com/google/android/material/navigation/NavigationBarItemView.java, com/google/android/material/timepicker/ClockFaceView.java, com/google/android/material/tabs/TabLayout.java
hardware_bridge: com/mediatek/twoworlds/factory/MtkTvFApiPeripheralBase.java, com/mediatek/twoworlds/aipq/clientwrapper/MtkTvAipqWrapper.java, com/android/p002tv/settings/uicustom/I2CDevice.java, com/android/p002tv/settings/uicustom/util/ProjectorManager.java, vendor/mediatek/p010tv/mtktvaipq/V1_0/IMtkTvAipq.java
runtime_exec: com/android/p002tv/settings/uicustom/util/MtkUtil.java

### zxlauncher
Package: Unknown
Permissions: 
Loaded JNI libraries: mmkv

input_source: okio/Pipe.java, com/zeasn/analytics/Bean.java, com/zeasn/launcher/base/bean/SeriesBean.java, com/zeasn/launcher/base/p016ui/BaseVideoDetailsActivity.java, com/zeasn/launcher/base/p016ui/dialog/BasePlayerNotInstallDialog.java, com/zeasn/saasapi/bean/VideoDetail.java, com/zeasn/saasapi/bean/DeeplinkSource.java, com/zeasn/saasapi/bean/ColumnContent.java, com/zeasn/saasapi/bean/VideoCategoryDetail.java, com/zeasn/saasapi/bean/Recommend.java
native_methods: org/chromium/base/JavaHandlerThread.java, org/chromium/base/TimeUtils.java, org/chromium/base/PowerMonitor.java, org/chromium/base/TraceEvent.java, org/chromium/base/MemoryPressureListener.java, org/chromium/base/FieldTrialList.java, org/chromium/base/PathService.java, org/chromium/base/ApplicationStatus.java, org/chromium/base/JavaExceptionReporter.java, org/chromium/base/SysUtils.java
jni_load: org/chromium/net/CronetEngine.java, org/chromium/base/library_loader/LibraryLoader.java, org/chromium/base/library_loader/Linker.java, org/chromium/base/library_loader/NativeLibraryPreloader.java, org/chromium/net/impl/CronetLibraryLoader.java, org/chromium/net/impl/VersionSafeCallbacks.java, com/tencent/mmkv/MMKV.java, com/google/android/exoplayer2/util/LibraryLoader.java, net/sqlcipher/database/SQLiteDatabase.java
autofocus: com/journeyapps/barcodescanner/camera/AutoFocusManager.java, com/journeyapps/barcodescanner/camera/CameraSettings.java, com/journeyapps/barcodescanner/camera/CameraConfigurationUtils.java, com/journeyapps/barcodescanner/camera/CameraManager.java, com/zeasn/tou_library/TouSettingActivity.java, com/zeasn/utils/CodeUtil.java, com/zeasn/widget/ZShapeButton.java, com/zeasn/widget/SelectorFactory.java, com/zeasn/widget/NetWorkView.java, com/zeasn/widget/ZImageView.java
runtime_exec: com/zeasn/utils/MyMacControl.java, com/zeasn/utils/NetWorkUtils.java, com/zeasn/utils/AppUtil.java, com/zeasn/utils/Utils.java, com/zeasn/live_data/LocalAppDataProvider.java, com/zeasn/p012c/download/install/ShellAppInstaller.java, com/zeasn/installer/xapi_lib/RomUtils.java, com/zeasn/installer/runnables/AndroidManualInstall.java, com/zeasn/installer/runnables/AndroidSilenceInstall.java, com/zeasn/saasapi/util/ShellUtils.java
hardware_bridge: com/zeasn/utils/NetWorkUtils.java, com/zeasn/installer/xapi_lib/RomUtils.java, com/qiniu/android/dns/local/AndroidDnsServer.java, com/android/media/api/service_api/utils/NetWorkUtils.java
mtk_api: com/zeasn/dpapi/brand/ZhiYuanMsd.java


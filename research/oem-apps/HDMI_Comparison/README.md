# One-HDMI vs two-HDMI: static code-level review of projector APKs

Versions and method-level structural changes, not just whole-file checksums.
These are approximate classes reconstructed by JADX, not original manufacturer sources.

| App | 1 HDMI APK version | 2 HDMI APK version | Exact APK? | Decompiled classes same/changed |
|---|---|---|---|---|
| KuSettings | 1.0 | 1.0 | False | 3463/7 |
| KeystoneCorrection | 13 | 13 | False | 448/1 |
| RuiAutoFocus | 4.2 | 4.2 | False | 1077/14 |
| KuSourceRJ | 4.9 | 5.3 | False | 2975/31 |
| trdservice | 13 | 13 | False | 432/6 |
| trdmiddle | 1.0 | 1.0 | True | exactly identical |
| TvSettingsPlus | 1.0 | 1.0 | False | 5724/71 |
| zxlauncher | 2.0.43 | 1.0.93 | False | 3907/6276 |
| zxstore | 4.1.52 | 4.2.01 | False | 9762/900 |
| LiveTV | 2.30.5 build 18321512 | 2.30.5 build 18321512 | False | 2244/2 |
| TvServices | 1.3 | 1.3 | False | 473/1 |
| Factory | 1.0 | 1.0 | False | 1860/5 |
| KuSetupWizard | - | 1.0 | False | ?/? |
| Provision | 13 | - | False | ?/? |
| RuiYunOta | 1.0 | 1.0 | True | exactly identical |

## KuSettings
APK versions: 1.0 vs 1.0
Permissions only in 1 HDMI: 
Permissions only in 2 HDMI: 
Common decompiled classes: 3470
Identical normalized classes: 3463
Changed classes: 7
Unique classes 1 HDMI: 
Unique classes 2 HDMI: 
Changed classes (first 40): androidx/core/app/AppLocalesStorageHelper.java, com/bumptech/glide/disklrucache/DiskLruCache.java, com/my/constant/Constant.java, com/my/settings/R.java, com/my/settings/bluetooth/MyAutoBluetoothActivity.java, com/my/utils/ReflectionUtils.java, kotlinx/coroutines/SupervisorKt.java

## KeystoneCorrection
APK versions: 13 vs 13
Permissions only in 1 HDMI: 
Permissions only in 2 HDMI: 
Common decompiled classes: 449
Identical normalized classes: 448
Changed classes: 1
Unique classes 1 HDMI: 
Unique classes 2 HDMI: 
Changed classes (first 40): com/rjpj/tcorrection/KeystoneUtil.java

## RuiAutoFocus
APK versions: 4.2 vs 4.2
Permissions only in 1 HDMI: 
Permissions only in 2 HDMI: 
Common decompiled classes: 1091
Identical normalized classes: 1077
Changed classes: 14
Unique classes 1 HDMI: 
Unique classes 2 HDMI: 
Changed classes (first 40): android/support/compat/R.java, android/support/constraint/R.java, android/support/coreui/R.java, android/support/coreutils/R.java, android/support/fragment/R.java, android/support/graphics/drawable/R.java, android/support/graphics/drawable/animated/R.java, android/support/v7/appcompat/R.java, com/hysd/ai/AiFileUtils.java, com/hysd/ai/AiTools.java, com/hysd/ai/RuiCameraApi.java, com/hysd/client/VAF2S.java, com/hysd/screen/R.java, com/hysd/xymscreen/MainActivity.java

## KuSourceRJ
APK versions: 4.9 vs 5.3
Permissions only in 1 HDMI: 
Permissions only in 2 HDMI: 
Common decompiled classes: 3006
Identical normalized classes: 2975
Changed classes: 31
Unique classes 1 HDMI: 
Unique classes 2 HDMI: com/coolux/source/activity/BootSoureActivityOld.java
Changed classes (first 40): androidx/activity/R.java, androidx/appcompat/R.java, androidx/constraintlayout/widget/R.java, androidx/coordinatorlayout/R.java, androidx/core/R.java, androidx/customview/R.java, androidx/drawerlayout/R.java, androidx/dynamicanimation/R.java, androidx/fragment/R.java, androidx/legacy/coreutils/R.java, androidx/lifecycle/runtime/R.java, androidx/lifecycle/viewmodel/R.java, androidx/loader/R.java, androidx/recyclerview/R.java, androidx/savedstate/R.java, androidx/transition/R.java, androidx/vectordrawable/R.java, androidx/vectordrawable/animated/R.java, androidx/viewpager/R.java, androidx/viewpager2/R.java, com/coolux/source/MainActivity.java, com/coolux/source/R.java, com/coolux/source/activity/BootSoureActivity.java, com/coolux/source/activity/KeystoneActivity.java, com/coolux/source/activity/testcast/InterfaceActivity.java, com/coolux/source/constant/Constans.java, com/coolux/source/utils/ImageUtils.java, com/coolux/source/utils/MyTimeUtils.java, com/coolux/source/utils/ReflectionUtils.java, com/coolux/source/utils/SharedPreferencesTest.java, com/google/android/material/R.java

## trdservice
APK versions: 13 vs 13
Permissions only in 1 HDMI: 
Permissions only in 2 HDMI: 
Common decompiled classes: 438
Identical normalized classes: 432
Changed classes: 6
Unique classes 1 HDMI: com/trd/common/enums/MotorCtrlEnum.java, com/trd/common/enums/MotorStatusEnum.java, com/trd/framework/IMotorCtrl.java, com/trd/sdk/MotorCtrl.java, com/trd/sdk/impl/MotorCtrlImpl.java
Unique classes 2 HDMI: 
Changed classes (first 40): com/trd/framework/ITrdSystemService.java, com/trd/sdk/TrdSystemManager.java, com/trd/service/TrdService.java, com/trd/service/communication/KeyEventCtrlService.java, com/trd/service/ui/RJFocusDialog.java, com/trd/service/ui/UICtrlImpl.java

## trdmiddle
APK versions: 1.0 vs 1.0
Permissions only in 1 HDMI: 
Permissions only in 2 HDMI: 
APK binaries match exactly.

## TvSettingsPlus
APK versions: 1.0 vs 1.0
Permissions only in 1 HDMI: 
Permissions only in 2 HDMI: 
Common decompiled classes: 5795
Identical normalized classes: 5724
Changed classes: 71
Unique classes 1 HDMI: 
Unique classes 2 HDMI: com/android/tv/settings/uicustom/I2CDevice.java
Changed classes (first 40): androidx/activity/R.java, androidx/appcompat/R.java, androidx/appcompat/resources/R.java, androidx/asynclayoutinflater/R.java, androidx/cardview/R.java, androidx/constraintlayout/widget/R.java, androidx/coordinatorlayout/R.java, androidx/core/R.java, androidx/customview/R.java, androidx/documentfile/R.java, androidx/drawerlayout/R.java, androidx/dynamicanimation/R.java, androidx/emoji2/R.java, androidx/emoji2/viewsintegration/R.java, androidx/fragment/R.java, androidx/leanback/R.java, androidx/leanback/preference/R.java, androidx/legacy/coreui/R.java, androidx/legacy/coreutils/R.java, androidx/legacy/preference/R.java, androidx/legacy/v4/R.java, androidx/lifecycle/extensions/R.java, androidx/lifecycle/process/R.java, androidx/lifecycle/runtime/R.java, androidx/lifecycle/service/R.java, androidx/lifecycle/viewmodel/R.java, androidx/lifecycle/viewmodel/savedstate/R.java, androidx/loader/R.java, androidx/media/R.java, androidx/mediarouter/R.java, androidx/preference/R.java, androidx/recyclerview/R.java, androidx/savedstate/R.java, androidx/slice/builders/R.java, androidx/slice/core/R.java, androidx/slice/view/R.java, androidx/startup/R.java, androidx/swiperefreshlayout/R.java, androidx/transition/R.java, androidx/vectordrawable/R.java

## zxlauncher
APK versions: 2.0.43 vs 1.0.93
Permissions only in 1 HDMI: android.permission.REQUEST_INSTALL_PACKAGES, android.permission.SYSTEM_ALERT_WINDOW, android.permission.WRITE_SECURE_SETTINGS
Permissions only in 2 HDMI: android.permission.BATTERY_STATS, android.permission.WRITE_MEDIA_STORAGE
Common decompiled classes: 10183
Identical normalized classes: 3907
Changed classes: 6276
Unique classes 1 HDMI: com/android/media/api/service_api/net/BodyHttpLoggingInterceptor.java, com/google/ads/interactivemedia/pal/AutoValue_NonceRequest.java, com/google/ads/interactivemedia/pal/GoogleAdManagerSettings.java, com/google/ads/interactivemedia/pal/zza.java, com/google/ads/interactivemedia/pal/zzal.java, com/google/ads/interactivemedia/pal/zzay.java, com/google/ads/interactivemedia/pal/zzc.java, com/google/ads/interactivemedia/pal/zzf.java, com/google/ads/interactivemedia/pal/zzi.java, com/google/ads/interactivemedia/pal/zzm.java, com/google/android/exoplayer2/offline/$$Lambda$DownloadManager$InternalHandler$OH12G4NteYquO91EYFUVBFeiK8.java, com/google/android/exoplayer2/offline/$$Lambda$PNiE7SuEFxRjAZH7pJpZIFOFjWg.java, com/google/android/gms/common/PackageSignatureVerifier.java, com/google/android/gms/common/PackageVerificationResult.java, com/google/android/gms/common/api/OptionalModuleApi.java, com/google/android/gms/common/internal/CallbackExecutor.java, com/google/android/gms/common/internal/zzah.java, com/google/android/gms/common/internal/zzal.java, com/google/android/gms/common/internal/zzp.java, com/google/android/gms/common/moduleinstall/InstallStatusListener.java, com/google/android/gms/common/moduleinstall/ModuleAvailabilityResponse.java, com/google/android/gms/common/moduleinstall/ModuleInstall.java, com/google/android/gms/common/moduleinstall/ModuleInstallClient.java, com/google/android/gms/common/moduleinstall/ModuleInstallIntentResponse.java, com/google/android/gms/common/moduleinstall/ModuleInstallRequest.java, com/google/android/gms/common/moduleinstall/ModuleInstallResponse.java, com/google/android/gms/common/moduleinstall/ModuleInstallStatusCodes.java, com/google/android/gms/common/moduleinstall/ModuleInstallStatusUpdate.java, com/google/android/gms/common/moduleinstall/internal/ApiFeatureRequest.java, com/google/android/gms/common/moduleinstall/internal/zaa.java
Unique classes 2 HDMI: androidx/exifinterface/R.java, androidx/exifinterface/media/ExifInterface.java, com/android/media/api/service_api/utils/Consts1.java, com/bumptech/glide/load/data/ParcelFileDescriptorRewinder.java, com/bumptech/glide/load/model/stream/QMediaStoreUriLoader.java, com/bumptech/glide/load/resource/bitmap/ImageReader.java, com/bumptech/glide/load/resource/bitmap/ParcelFileDescriptorBitmapDecoder.java, com/google/ads/interactivemedia/pal/zzb.java, com/google/ads/interactivemedia/pal/zze.java, com/google/ads/interactivemedia/pal/zzh.java, com/google/ads/interactivemedia/pal/zzn.java, com/google/android/exoplayer2/BuildConfig.java, com/google/android/exoplayer2/R.java, com/google/android/exoplayer2/offline/$$Lambda$DownloadManager$InternalHandler$NXQcmC9peGeDWV0s_8pBfzdJpS0.java, com/google/android/exoplayer2/offline/$$Lambda$_14QHG018Z6p13d3hzJuGTWnNeo.java, com/google/android/exoplayer2/source/dash/BaseUrlExclusionList.java, com/google/android/exoplayer2/source/dash/BuildConfig.java, com/google/android/exoplayer2/source/dash/DashChunkSource.java, com/google/android/exoplayer2/source/dash/DashManifestStaleException.java, com/google/android/exoplayer2/source/dash/DashMediaPeriod.java, com/google/android/exoplayer2/source/dash/DashMediaSource.java, com/google/android/exoplayer2/source/dash/DashSegmentIndex.java, com/google/android/exoplayer2/source/dash/DashUtil.java, com/google/android/exoplayer2/source/dash/DashWrappingSegmentIndex.java, com/google/android/exoplayer2/source/dash/DefaultDashChunkSource.java, com/google/android/exoplayer2/source/dash/EventSampleStream.java, com/google/android/exoplayer2/source/dash/PlayerEmsgHandler.java, com/google/android/exoplayer2/source/dash/R.java, com/google/android/exoplayer2/source/dash/manifest/AdaptationSet.java, com/google/android/exoplayer2/source/dash/manifest/BaseUrl.java
Changed classes (first 40): android/support/customtabs/ICustomTabsCallback.java, android/support/customtabs/ICustomTabsService.java, android/support/customtabs/IPostMessageService.java, android/support/customtabs/trusted/ITrustedWebActivityCallback.java, android/support/customtabs/trusted/ITrustedWebActivityService.java, android/support/v4/app/INotificationSideChannel.java, android/support/v4/app/RemoteActionCompatParcelizer.java, android/support/v4/graphics/drawable/IconCompatParcelizer.java, android/support/v4/media/AudioAttributesCompatParcelizer.java, android/support/v4/media/AudioAttributesImplApi21Parcelizer.java, android/support/v4/media/AudioAttributesImplApi26Parcelizer.java, android/support/v4/media/AudioAttributesImplBaseParcelizer.java, android/support/v4/media/MediaBrowserCompat.java, android/support/v4/media/MediaDescriptionCompat.java, android/support/v4/media/MediaMetadataCompat.java, android/support/v4/media/RatingCompat.java, android/support/v4/media/session/IMediaControllerCallback.java, android/support/v4/media/session/IMediaSession.java, android/support/v4/media/session/MediaControllerCompat.java, android/support/v4/media/session/MediaSessionCompat.java, android/support/v4/media/session/ParcelableVolumeInfo.java, android/support/v4/media/session/PlaybackStateCompat.java, android/support/v4/os/IResultReceiver.java, android/support/v4/os/ResultReceiver.java, androidx/activity/ComponentActivity.java, androidx/activity/ImmLeaksCleaner.java, androidx/activity/OnBackPressedCallback.java, androidx/activity/OnBackPressedDispatcher.java, androidx/activity/OnBackPressedDispatcherOwner.java, androidx/activity/R.java, androidx/activity/contextaware/ContextAware.java, androidx/activity/contextaware/ContextAwareHelper.java, androidx/activity/contextaware/OnContextAvailableListener.java, androidx/activity/result/ActivityResult.java, androidx/activity/result/ActivityResultCallback.java, androidx/activity/result/ActivityResultCaller.java, androidx/activity/result/ActivityResultLauncher.java, androidx/activity/result/ActivityResultRegistry.java, androidx/activity/result/ActivityResultRegistryOwner.java, androidx/activity/result/IntentSenderRequest.java

## zxstore
APK versions: 4.1.52 vs 4.2.01
Permissions only in 1 HDMI: android.permission.INTERACT_ACROSS_USERS
Permissions only in 2 HDMI: android.permission.CAMERA, android.permission.WRITE_MEDIA_STORAGE
Common decompiled classes: 10662
Identical normalized classes: 9762
Changed classes: 900
Unique classes 1 HDMI: com/zeasn/ad/connector/BuildConfig.java, com/zeasn/ad/connector/R.java, com/zeasn/ad/connector/ZeasnAd.java, com/zeasn/ad/connector/ZeasnAdSDK.java, com/zeasn/ad/connector/ZeasnBaseAd.java, com/zeasn/ad/connector/bean/AdBean.java, com/zeasn/ad/connector/bean/ZeasnAdInfo.java, com/zeasn/ad/connector/bean/ZeasnError.java, com/zeasn/ad/connector/control/AdConstant.java, com/zeasn/ad/connector/control/PluginAdControl.java, com/zeasn/ad/connector/impl/AdType.java, com/zeasn/ad/connector/impl/ErrType.java, com/zeasn/ad/connector/impl/PluginAdInterface.java, com/zeasn/ad/connector/impl/PluginAdListener.java, com/zeasn/ad/connector/impl/PluginPlayerControl.java, com/zeasn/ad/connector/impl/ZeasnAdKey.java, com/zeasn/ad/connector/listener/ZeasnListener.java, com/zeasn/ad/connector/listener/ZeasnOnVodListener.java, com/zeasn/ad/connector/listener/ZeasnSkipListener.java, com/zeasn/ad/connector/utils/LogUtil.java, com/zeasn/ad/connector/utils/Observer.java, com/zeasn/ad/connector/utils/ResourceUtils.java, com/zeasn/ad/connector/utils/SharedPreferencesUtils.java, com/zeasn/ad/connector/utils/Subject.java, com/zeasn/dpapi/base/DateHolder.java, com/zeasn/dpapi/base/interceptor/DateInterceptor.java, com/zeasn/dpapi/base/interceptor/UserAgentInterceptor.java, com/zeasn/plugin/BroadcastMonitor.java, com/zeasn/plugin/BuildConfig.java, com/zeasn/plugin/PluginInit.java
Unique classes 2 HDMI: com/google/zxing/client/android/AmbientLightManager.java, com/google/zxing/client/android/BeepManager.java, com/google/zxing/client/android/BuildConfig.java, com/google/zxing/client/android/DecodeFormatManager.java, com/google/zxing/client/android/DecodeHintManager.java, com/google/zxing/client/android/InactivityTimer.java, com/google/zxing/client/android/Intents.java, com/google/zxing/client/android/R.java, com/google/zxing/client/android/camera/open/OpenCameraInterface.java, com/google/zxing/integration/android/IntentIntegrator.java, com/google/zxing/integration/android/IntentResult.java, com/journeyapps/barcodescanner/BarcodeCallback.java, com/journeyapps/barcodescanner/BarcodeEncoder.java, com/journeyapps/barcodescanner/BarcodeResult.java, com/journeyapps/barcodescanner/BarcodeView.java, com/journeyapps/barcodescanner/CameraPreview.java, com/journeyapps/barcodescanner/CaptureActivity.java, com/journeyapps/barcodescanner/CaptureManager.java, com/journeyapps/barcodescanner/CompoundBarcodeView.java, com/journeyapps/barcodescanner/Decoder.java, com/journeyapps/barcodescanner/DecoderFactory.java, com/journeyapps/barcodescanner/DecoderResultPointCallback.java, com/journeyapps/barcodescanner/DecoderThread.java, com/journeyapps/barcodescanner/DecoratedBarcodeView.java, com/journeyapps/barcodescanner/DefaultDecoderFactory.java, com/journeyapps/barcodescanner/InvertedDecoder.java, com/journeyapps/barcodescanner/MixedDecoder.java, com/journeyapps/barcodescanner/RawImageData.java, com/journeyapps/barcodescanner/RotationCallback.java, com/journeyapps/barcodescanner/RotationListener.java
Changed classes (first 40): android/support/v4/media/session/MediaSessionCompat.java, androidx/activity/R.java, androidx/appcompat/R.java, androidx/appcompat/app/AppCompatDelegateImpl.java, androidx/appcompat/app/TwilightManager.java, androidx/appcompat/app/WindowDecorActionBar.java, androidx/appcompat/resources/R.java, androidx/appcompat/view/SupportMenuInflater.java, androidx/appcompat/view/menu/MenuPopup.java, androidx/appcompat/widget/ActionBarContainer.java, androidx/appcompat/widget/ActionMenuPresenter.java, androidx/appcompat/widget/ActionMenuView.java, androidx/appcompat/widget/ActivityChooserView.java, androidx/appcompat/widget/AppCompatDrawableManager.java, androidx/appcompat/widget/AppCompatSpinner.java, androidx/appcompat/widget/AppCompatTextHelper.java, androidx/appcompat/widget/AppCompatTextViewAutoSizeHelper.java, androidx/appcompat/widget/DropDownListView.java, androidx/appcompat/widget/LinearLayoutCompat.java, androidx/appcompat/widget/Toolbar.java, androidx/asynclayoutinflater/R.java, androidx/browser/R.java, androidx/browser/trusted/TrustedWebActivityDisplayMode.java, androidx/cardview/R.java, androidx/collection/SimpleArrayMap.java, androidx/concurrent/futures/AbstractResolvableFuture.java, androidx/constraintlayout/solver/ArrayLinkedVariables.java, androidx/constraintlayout/solver/LinearSystem.java, androidx/constraintlayout/solver/SolverVariable.java, androidx/constraintlayout/solver/widgets/Chain.java, androidx/constraintlayout/solver/widgets/ConstraintWidget.java, androidx/constraintlayout/solver/widgets/ConstraintWidgetContainer.java, androidx/constraintlayout/widget/ConstraintLayout.java, androidx/constraintlayout/widget/R.java, androidx/coordinatorlayout/R.java, androidx/coordinatorlayout/widget/CoordinatorLayout.java, androidx/core/R.java, androidx/core/app/JobIntentService.java, androidx/core/graphics/BitmapKt.java, androidx/core/graphics/PathParser.java

## LiveTV
APK versions: 2.30.5 build 18321512 vs 2.30.5 build 18321512
Permissions only in 1 HDMI: 
Permissions only in 2 HDMI: 
Common decompiled classes: 2246
Identical normalized classes: 2244
Changed classes: 2
Unique classes 1 HDMI: 
Unique classes 2 HDMI: 
Changed classes (first 40): com/mediatek/wwtv/tvcenter/nav/TurnkeyUiMainActivity.java, com/mediatek/wwtv/tvcenter/util/DataSeparaterUtil.java

## TvServices
APK versions: 1.3 vs 1.3
Permissions only in 1 HDMI: 
Permissions only in 2 HDMI: 
Common decompiled classes: 474
Identical normalized classes: 473
Changed classes: 1
Unique classes 1 HDMI: 
Unique classes 2 HDMI: 
Changed classes (first 40): com/mediatek/wwtv/tvcenter/util/DataSeparaterUtil.java

## Factory
APK versions: 1.0 vs 1.0
Permissions only in 1 HDMI: 
Permissions only in 2 HDMI: 
Common decompiled classes: 1865
Identical normalized classes: 1860
Changed classes: 5
Unique classes 1 HDMI: 
Unique classes 2 HDMI: mediatek/util/I2CDevice.java
Changed classes (first 40): com/android/tv/settings/partnercustomizer/tvsettingservice/TVSettingConfig.java, com/mediatek/wwtv/tvcenter/util/DataSeparaterUtil.java, mediatek/tvsetting/factory/ui/ApiService.java, mediatek/tvsetting/factory/ui/ProjectorManager.java, mediatek/tvsetting/factory/ui/factorymenu/FactoryMenuActivity.java

## KuSetupWizard
APK versions: missing vs 1.0
Permissions only in 1 HDMI: 
Permissions only in 2 HDMI: android.permission.ACCESS_FINE_LOCATION, android.permission.ACCESS_NETWORK_STATE, android.permission.ACCESS_WIFI_STATE, android.permission.BACKUP, android.permission.BLUETOOTH, android.permission.BLUETOOTH_ADMIN, android.permission.BLUETOOTH_CONNECT, android.permission.BLUETOOTH_PRIVILEGED, android.permission.BLUETOOTH_SCAN, android.permission.CHANGE_COMPONENT_ENABLED_STATE, android.permission.CHANGE_NETWORK_STATE, android.permission.CHANGE_WIFI_STATE, android.permission.CLEAR_APP_USER_DATA, android.permission.CONNECTIVITY_INTERNAL, android.permission.DELETE_CACHE_FILES, android.permission.DUMP, android.permission.FORCE_STOP_PACKAGES, android.permission.GET_ACCOUNTS, android.permission.GET_PACKAGE_SIZE, android.permission.INJECT_EVENTS, android.permission.INTERNET, android.permission.MANAGE_ACCOUNTS, android.permission.MANAGE_USERS, android.permission.MASTER_CLEAR, android.permission.MOUNT_UNMOUNT_FILESYSTEMS, android.permission.OVERRIDE_WIFI_CONFIG, android.permission.QUERY_ALL_PACKAGES, android.permission.READ_CONTACTS, android.permission.READ_EXTERNAL_STORAGE, android.permission.READ_PHONE_STATE, android.permission.RECEIVE_BOOT_COMPLETED, android.permission.RECOVERY, android.permission.SET_PREFERRED_APPLICATIONS, android.permission.SET_TIME, android.permission.SET_TIME_ZONE, android.permission.SYSTEM_ALERT_WINDOW, android.permission.TETHER_PRIVILEGED, android.permission.WAKE_LOCK, android.permission.WRITE_EXTERNAL_STORAGE, android.permission.WRITE_MEDIA_STORAGE, android.permission.WRITE_SECURE_SETTINGS, android.permission.WRITE_SETTINGS, com.android.provision.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION
Common decompiled classes: None
Identical normalized classes: None
Changed classes: None
Unique classes 1 HDMI: 
Unique classes 2 HDMI: 
Changed classes (first 40): 

## Provision
APK versions: 13 vs missing
Permissions only in 1 HDMI: android.permission.DISPATCH_PROVISIONING_MESSAGE, android.permission.MASTER_CLEAR, android.permission.WRITE_SECURE_SETTINGS, android.permission.WRITE_SETTINGS
Permissions only in 2 HDMI: 
Common decompiled classes: None
Identical normalized classes: None
Changed classes: None
Unique classes 1 HDMI: 
Unique classes 2 HDMI: 
Changed classes (first 40): 

## RuiYunOta
APK versions: 1.0 vs 1.0
Permissions only in 1 HDMI: 
Permissions only in 2 HDMI: 
APK binaries match exactly.

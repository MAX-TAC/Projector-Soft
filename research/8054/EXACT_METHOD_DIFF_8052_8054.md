# Original APK 8052 ↔ 8054: method-level smali diff

Source: [successful CI run 44094937995](https://github.com/MAX-TAC/Projector-Soft/actions/runs/44094937995), original APK pairs, `tools/compare_8052_8054_smali.js`. Method bodies compared after stripping debug directives. `CHANGED`, `ADDED`, `REMOVED` describe static method signatures and instructions; none proves runtime execution.

| APK | 8052 methods | 8054 methods | Changed method IDs |
|---|---:|---:|---:|
| TvSettingsPlus | 98195 | 98195 | 3 |
| KuSettings | 61024 | 61024 | 3 |
| RuiAutoFocus | 21925 | 21920 | 40 |
| KeystoneCorrection | 6423 | 6422 | 2 |
| Factory | 31164 | 31163 | 9 |
| trdservice | 6233 | 6260 | 41 |

The diff proves the Factory orientation method signature changed from `setProjectorMirrorMode(Context,int)` to `setProjectorMirrorMode(int)` and 8054 `ApiService#onStartCommand` invokes the new signature. This is Factory's class, distinct from TvSettingsPlus's homonymous class. A focused trace in run 44097233248 shows KuSettings's changed Bluetooth view method changes only an image-selection version comparison; it does not prove a pairing change. The 8054 trdservice no longer starts a `checkSleep` thread in `onCreate` and adds an `IMotorCtrl` Binder interface and SDK. Optical and keystone method changes prevent blind 8052 porting.

## All changed method IDs

```text
DIFF_METHOD TvSettingsPlus CHANGED Lcom/android/tv/settings/partnercustomizer/tvsettingservice/TVSettingConfig;->getDefaultPowerSetting(Landroid/content/Context;)I
DIFF_METHOD TvSettingsPlus CHANGED Lcom/android/tv/settings/partnercustomizer/tvsettingservice/TVSettingConfig;->getIntautoSleepValue(Landroid/content/Context;)I
DIFF_METHOD TvSettingsPlus CHANGED Lcom/mediatek/wwtv/tvcenter/util/DataSeparaterUtil;->getValueAutoSleep()I
DIFF_METHOD KuSettings CHANGED Lcom/my/settings/bluetooth/MyAutoBluetoothActivity;->viewAutoBluetoothBak()V
DIFF_METHOD KuSettings CHANGED Lcom/my/utils/IntentUtils;->airplayIntent(Landroid/content/Context;)V
DIFF_METHOD KuSettings CHANGED Lcom/my/utils/ReflectionUtils;->getReleaseVersion()Ljava/lang/String;
DIFF_METHOD RuiAutoFocus CHANGED Lcom/hysd/ai/AiFileUtils;->CheckHysdFiles(Z)I
DIFF_METHOD RuiAutoFocus REMOVED Lcom/hysd/ai/AiFileUtils;->clearAllCalibrationFiles()Ljava/lang/String;
DIFF_METHOD RuiAutoFocus REMOVED Lcom/hysd/ai/AiFileUtils;->getFileMd5AndDelete(Ljava/lang/String;)Ljava/lang/String;
DIFF_METHOD RuiAutoFocus CHANGED Lcom/hysd/ai/RuiCameraApi;-><init>(Landroid/content/Context;)V
DIFF_METHOD RuiAutoFocus CHANGED Lcom/hysd/ai/RuiCameraApi;->IsEndPositonBack()Z
DIFF_METHOD RuiAutoFocus CHANGED Lcom/hysd/ai/RuiCameraApi;->IsEndPositonFront()Z
DIFF_METHOD RuiAutoFocus CHANGED Lcom/hysd/ai/RuiCameraApi;->MotorBack()I
DIFF_METHOD RuiAutoFocus CHANGED Lcom/hysd/ai/RuiCameraApi;->MotorBackStep(I)I
DIFF_METHOD RuiAutoFocus CHANGED Lcom/hysd/ai/RuiCameraApi;->MotorBackTofStep(IZI)I
DIFF_METHOD RuiAutoFocus CHANGED Lcom/hysd/ai/RuiCameraApi;->MotorFront()I
DIFF_METHOD RuiAutoFocus CHANGED Lcom/hysd/ai/RuiCameraApi;->MotorFrontStep(I)I
DIFF_METHOD RuiAutoFocus CHANGED Lcom/hysd/ai/RuiCameraApi;->MotorFrontTofStep(IZI)I
DIFF_METHOD RuiAutoFocus CHANGED Lcom/hysd/ai/RuiCameraApi;->MotorStop()I
DIFF_METHOD RuiAutoFocus CHANGED Lcom/hysd/ai/RuiCameraApi;->startTof()I
DIFF_METHOD RuiAutoFocus CHANGED Lcom/hysd/client/VAF2S;->focusfloatview()V
DIFF_METHOD RuiAutoFocus CHANGED Lcom/hysd/client/VAF2S;->onCreate()V
DIFF_METHOD RuiAutoFocus CHANGED Lcom/hysd/client/VAF2S;->onStartCommand(Landroid/content/Intent;II)I
DIFF_METHOD RuiAutoFocus CHANGED Lcom/hysd/xymscreen/MainActivity$10;->onClick(Landroid/view/View;)V
DIFF_METHOD RuiAutoFocus CHANGED Lcom/hysd/xymscreen/MainActivity$11;->onClick(Landroid/view/View;)V
DIFF_METHOD RuiAutoFocus REMOVED Lcom/hysd/xymscreen/MainActivity$13;-><init>(Lcom/hysd/xymscreen/MainActivity;)V
DIFF_METHOD RuiAutoFocus REMOVED Lcom/hysd/xymscreen/MainActivity$13;->onClick(Landroid/view/View;)V
DIFF_METHOD RuiAutoFocus CHANGED Lcom/hysd/xymscreen/MainActivity$1;->handleMessage(Landroid/os/Message;)V
DIFF_METHOD RuiAutoFocus CHANGED Lcom/hysd/xymscreen/MainActivity$2;->onClick(Landroid/view/View;)V
DIFF_METHOD RuiAutoFocus CHANGED Lcom/hysd/xymscreen/MainActivity$3;->onClick(Landroid/view/View;)V
DIFF_METHOD RuiAutoFocus CHANGED Lcom/hysd/xymscreen/MainActivity$9;->onClick(Landroid/view/View;)V
DIFF_METHOD RuiAutoFocus CHANGED Lcom/hysd/xymscreen/MainActivity$CalibrationBroadcastReceiver;->onReceive(Landroid/content/Context;Landroid/content/Intent;)V
DIFF_METHOD RuiAutoFocus CHANGED Lcom/hysd/xymscreen/MainActivity$RunCmdAccGryCaliSaveThread;->run()V
DIFF_METHOD RuiAutoFocus CHANGED Lcom/hysd/xymscreen/MainActivity$RunCmdAllStepThread;->run()V
DIFF_METHOD RuiAutoFocus CHANGED Lcom/hysd/xymscreen/MainActivity$RunCmdCurStepThread;->run()V
DIFF_METHOD RuiAutoFocus CHANGED Lcom/hysd/xymscreen/MainActivity$RunCmdMotorTofSaveThread;->run()V
DIFF_METHOD RuiAutoFocus ADDED Lcom/hysd/xymscreen/MainActivity;->access$700(Landroid/content/Context;Ljava/lang/String;)Z
DIFF_METHOD RuiAutoFocus REMOVED Lcom/hysd/xymscreen/MainActivity;->access$700(Lcom/hysd/xymscreen/MainActivity;)Landroid/widget/Button;
DIFF_METHOD RuiAutoFocus REMOVED Lcom/hysd/xymscreen/MainActivity;->access$800(Landroid/content/Context;Ljava/lang/String;)Z
DIFF_METHOD RuiAutoFocus ADDED Lcom/hysd/xymscreen/MainActivity;->access$800(Lcom/hysd/xymscreen/MainActivity;I)V
DIFF_METHOD RuiAutoFocus REMOVED Lcom/hysd/xymscreen/MainActivity;->access$900(Lcom/hysd/xymscreen/MainActivity;I)V
DIFF_METHOD RuiAutoFocus CHANGED Lcom/hysd/xymscreen/MainActivity;->onCreate(Landroid/os/Bundle;)V
DIFF_METHOD RuiAutoFocus CHANGED Lcom/hysd/xymscreen/MainActivity;->onKeyDown(ILandroid/view/KeyEvent;)Z
DIFF_METHOD RuiAutoFocus CHANGED Lcom/hysd/xymscreen/MainActivity;->onStart()V
DIFF_METHOD RuiAutoFocus CHANGED Lcom/hysd/xymscreen/MainActivity;->updateDataFast()V
DIFF_METHOD RuiAutoFocus CHANGED Lcom/hysd/xymscreen/MainActivity;->updateDataSlow()V
DIFF_METHOD KeystoneCorrection REMOVED Lcom/rjpj/tcorrection/KeystoneUtil;->getProjectorMirrorMode()I
DIFF_METHOD KeystoneCorrection CHANGED Lcom/rjpj/tcorrection/KeystoneUtil;->writeParcelToFlinger(I)V
DIFF_METHOD Factory CHANGED Lcom/android/tv/settings/partnercustomizer/tvsettingservice/TVSettingConfig;->getDefaultPowerSetting(Landroid/content/Context;)I
DIFF_METHOD Factory CHANGED Lcom/android/tv/settings/partnercustomizer/tvsettingservice/TVSettingConfig;->getIntautoSleepValue(Landroid/content/Context;)I
DIFF_METHOD Factory CHANGED Lcom/mediatek/wwtv/tvcenter/util/DataSeparaterUtil;->getValueAutoSleep()I
DIFF_METHOD Factory CHANGED Lmediatek/tvsetting/factory/ui/ApiService;->onCreate()V
DIFF_METHOD Factory CHANGED Lmediatek/tvsetting/factory/ui/ApiService;->onStartCommand(Landroid/content/Intent;II)I
DIFF_METHOD Factory REMOVED Lmediatek/tvsetting/factory/ui/ProjectorManager;->sendBroadcastSetKeytone(Landroid/content/Context;)V
DIFF_METHOD Factory ADDED Lmediatek/tvsetting/factory/ui/ProjectorManager;->setProjectorMirrorMode(I)V
DIFF_METHOD Factory REMOVED Lmediatek/tvsetting/factory/ui/ProjectorManager;->setProjectorMirrorMode(Landroid/content/Context;I)V
DIFF_METHOD Factory CHANGED Lmediatek/tvsetting/factory/ui/factorymenu/OtherOptionForFactory;->onKeyDown(ILandroid/view/KeyEvent;)Z
DIFF_METHOD trdservice ADDED Lcom/trd/common/enums/MotorCtrlEnum;->$values()[Lcom/trd/common/enums/MotorCtrlEnum;
DIFF_METHOD trdservice ADDED Lcom/trd/common/enums/MotorCtrlEnum;-><clinit>()V
DIFF_METHOD trdservice ADDED Lcom/trd/common/enums/MotorCtrlEnum;-><init>(Ljava/lang/String;II)V
DIFF_METHOD trdservice ADDED Lcom/trd/common/enums/MotorCtrlEnum;->get()I
DIFF_METHOD trdservice ADDED Lcom/trd/common/enums/MotorCtrlEnum;->valueOf(Ljava/lang/String;)Lcom/trd/common/enums/MotorCtrlEnum;
DIFF_METHOD trdservice ADDED Lcom/trd/common/enums/MotorCtrlEnum;->values()[Lcom/trd/common/enums/MotorCtrlEnum;
DIFF_METHOD trdservice ADDED Lcom/trd/common/enums/MotorStatusEnum;->$values()[Lcom/trd/common/enums/MotorStatusEnum;
DIFF_METHOD trdservice ADDED Lcom/trd/common/enums/MotorStatusEnum;-><clinit>()V
DIFF_METHOD trdservice ADDED Lcom/trd/common/enums/MotorStatusEnum;-><init>(Ljava/lang/String;II)V
DIFF_METHOD trdservice ADDED Lcom/trd/common/enums/MotorStatusEnum;->fromInt(I)Lcom/trd/common/enums/MotorStatusEnum;
DIFF_METHOD trdservice ADDED Lcom/trd/common/enums/MotorStatusEnum;->get()I
DIFF_METHOD trdservice ADDED Lcom/trd/common/enums/MotorStatusEnum;->valueOf(Ljava/lang/String;)Lcom/trd/common/enums/MotorStatusEnum;
DIFF_METHOD trdservice ADDED Lcom/trd/common/enums/MotorStatusEnum;->values()[Lcom/trd/common/enums/MotorStatusEnum;
DIFF_METHOD trdservice ADDED Lcom/trd/framework/IMotorCtrl$Stub$Proxy;-><init>(Landroid/os/IBinder;)V
DIFF_METHOD trdservice ADDED Lcom/trd/framework/IMotorCtrl$Stub$Proxy;->GetMotorStatus()I
DIFF_METHOD trdservice ADDED Lcom/trd/framework/IMotorCtrl$Stub$Proxy;->SetMotorCtrl(II)V
DIFF_METHOD trdservice ADDED Lcom/trd/framework/IMotorCtrl$Stub$Proxy;->asBinder()Landroid/os/IBinder;
DIFF_METHOD trdservice ADDED Lcom/trd/framework/IMotorCtrl$Stub;->asInterface(Landroid/os/IBinder;)Lcom/trd/framework/IMotorCtrl;
DIFF_METHOD trdservice ADDED Lcom/trd/framework/IMotorCtrl$Stub;->getDefaultImpl()Lcom/trd/framework/IMotorCtrl;
DIFF_METHOD trdservice ADDED Lcom/trd/framework/IMotorCtrl;->GetMotorStatus()I
DIFF_METHOD trdservice ADDED Lcom/trd/framework/IMotorCtrl;->SetMotorCtrl(II)V
DIFF_METHOD trdservice ADDED Lcom/trd/framework/ITrdSystemService$Stub$Proxy;->getMotorCtrl()Lcom/trd/framework/IMotorCtrl;
DIFF_METHOD trdservice ADDED Lcom/trd/framework/ITrdSystemService;->getMotorCtrl()Lcom/trd/framework/IMotorCtrl;
DIFF_METHOD trdservice ADDED Lcom/trd/sdk/MotorCtrl;->GetMotorStatus()Lcom/trd/common/enums/MotorStatusEnum;
DIFF_METHOD trdservice ADDED Lcom/trd/sdk/MotorCtrl;->SetMotorCtrl(Lcom/trd/common/enums/MotorCtrlEnum;I)V
DIFF_METHOD trdservice ADDED Lcom/trd/sdk/TrdSystemManager;->getMotorCtrl()Lcom/trd/sdk/MotorCtrl;
DIFF_METHOD trdservice ADDED Lcom/trd/sdk/impl/MotorCtrlImpl;-><clinit>()V
DIFF_METHOD trdservice ADDED Lcom/trd/sdk/impl/MotorCtrlImpl;-><init>()V
DIFF_METHOD trdservice ADDED Lcom/trd/sdk/impl/MotorCtrlImpl;->GetMotorStatus()Lcom/trd/common/enums/MotorStatusEnum;
DIFF_METHOD trdservice ADDED Lcom/trd/sdk/impl/MotorCtrlImpl;->SetMotorCtrl(Lcom/trd/common/enums/MotorCtrlEnum;I)V
DIFF_METHOD trdservice ADDED Lcom/trd/sdk/impl/MotorCtrlImpl;->getInstance()Lcom/trd/sdk/MotorCtrl;
DIFF_METHOD trdservice ADDED Lcom/trd/sdk/impl/MotorCtrlImpl;->reconnect()V
DIFF_METHOD trdservice REMOVED Lcom/trd/service/TrdService$$ExternalSyntheticLambda1;-><init>(Lcom/trd/service/TrdService;)V
DIFF_METHOD trdservice REMOVED Lcom/trd/service/TrdService$$ExternalSyntheticLambda1;->run()V
DIFF_METHOD trdservice REMOVED Lcom/trd/service/TrdService;->$r8$lambda$JOVqU1KvZI0prAJ77-TIo4aMGSQ(Lcom/trd/service/TrdService;)V
DIFF_METHOD trdservice REMOVED Lcom/trd/service/TrdService;->checkSleep()V
DIFF_METHOD trdservice REMOVED Lcom/trd/service/TrdService;->lambda$onCreate$1()V
DIFF_METHOD trdservice CHANGED Lcom/trd/service/TrdService;->onCreate()V
DIFF_METHOD trdservice CHANGED Lcom/trd/service/communication/KeyEventCtrlService;->interceptKeyBeforeDispatching(Landroid/view/KeyEvent;I)I
DIFF_METHOD trdservice CHANGED Lcom/trd/service/ui/RJFocusDialog;->showFocus(I)V
DIFF_METHOD trdservice CHANGED Lcom/trd/service/ui/UICtrlImpl$4;->run()V
```

## Limits

The six pairs cover critical but not all 88 APK. Manifest and resource diffs are still open. Exact invoke extraction for selected methods is recorded in `exact_smali_calls_8054.json`; its `target_declared_in_same_apk` field must not be interpreted as a cross-process call. Arguments, branches, backend effects, hardware behavior, and rollback remain to be traced.

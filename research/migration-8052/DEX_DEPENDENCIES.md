# 8052 OEM DEX cross-package reference survey

DEX string-pool membership is not proof of a live Intent or service dependency.
Absence of a literal reference does not exclude reflection, resource lookups, binder or implicit broadcasts.

| APK | KuSettings strings | KuSource strings | TRD strings | Focus strings | Keystone strings |
|---|---:|---:|---:|---:|---:|
| Factory | 0 | 0 | 0 | 1 | 2 |
| KeystoneCorrection | 0 | 0 | 13 | 0 | 8 |
| KuSettings | 469 | 2 | 0 | 0 | 0 |
| KuSetupWizard | 469 | 2 | 0 | 0 | 0 |
| KuSourceRJ | 0 | 172 | 73 | 1 | 0 |
| LiveTV | 0 | 0 | 0 | 0 | 0 |
| RuiAutoFocus | 0 | 0 | 74 | 17 | 2 |
| RuiYunOta | 0 | 0 | 0 | 0 | 0 |
| TvServices | 0 | 0 | 0 | 0 | 0 |
| TvSettingsPlus | 0 | 3 | 0 | 1 | 2 |
| WFDSinkTest | 1 | 2 | 1 | 1 | 0 |
| framework-res | 0 | 0 | 0 | 0 | 0 |
| trdmiddle | 0 | 0 | 52 | 0 | 0 |
| trdservice | 0 | 0 | 76 | 0 | 2 |

## Potential inbound KuSettings refs outside KuSettings

### KuSetupWizard
- Lcom/my/service/BootupReceiver;
- Lcom/my/service/MySetTimeService$1;
- Lcom/my/service/MySetTimeService;
- Lcom/my/service/ScreenServices$1;
- Lcom/my/service/ScreenServices$2;
- Lcom/my/service/ScreenServices$3;
- Lcom/my/service/ScreenServices$4$1;
- Lcom/my/service/ScreenServices$4;
- Lcom/my/service/ScreenServices;
- Lcom/my/settings/AirplaneModeEnabler$1;
- Lcom/my/settings/AirplaneModeEnabler$OnAirplaneModeChangedListener;
- Lcom/my/settings/AirplaneModeEnabler;
- Lcom/my/settings/BaseActivity;
- Lcom/my/settings/DialogCreatable;
- Lcom/my/settings/LinkifyUtils$1;
- Lcom/my/settings/LinkifyUtils$OnClickListener;
- Lcom/my/settings/LinkifyUtils;
- Lcom/my/settings/MainActivity;
- Lcom/my/settings/ProxySelector$1;
- Lcom/my/settings/ProxySelector$2;
- Lcom/my/settings/ProxySelector$3;
- Lcom/my/settings/ProxySelector$4;
- Lcom/my/settings/ProxySelector;
- Lcom/my/settings/R$array;
- Lcom/my/settings/R$attr;
- Lcom/my/settings/R$color;
- Lcom/my/settings/R$dimen;
- Lcom/my/settings/R$drawable;
- Lcom/my/settings/R$id;
- Lcom/my/settings/R$layout;
- Lcom/my/settings/R$mipmap;
- Lcom/my/settings/R$string;
- Lcom/my/settings/R$style;
- Lcom/my/settings/R$styleable;
- Lcom/my/settings/R$xml;
- Lcom/my/settings/R;
- Lcom/my/settings/SettingActivity$1;
- Lcom/my/settings/SettingActivity$2;
- Lcom/my/settings/SettingActivity;
- Lcom/my/settings/Utils;
### WFDSinkTest
- com.my.settings.bluetooth.MyAutoBluetoothActivity

Do not disable KuSettings based solely on this survey. Verify explicit and implicit calls, BootupReceiver, MySetTimeService, ScreenServices and runtime traces.

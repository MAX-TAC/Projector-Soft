package dev.projectorsoft.tvsettings;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import com.android.tv.settings.SettingsPreferenceFragment;

/** Each section owns its stock-rendered tree, observers and checked user actions. */
public final class NativeSections {
    private static final String OWN="com.android.tv.settings";

    private NativeSections() {}

    public abstract static class Page extends SettingsPreferenceFragment {
        abstract NativePage section();
        abstract void populate(PreferenceScreen screen);
        @Override public int getPageId(){return 0;}

        @Override public void onCreatePreferences(Bundle state,String rootKey){
            PreferenceScreen screen=getPreferenceManager().createPreferenceScreen(requireContext());
            screen.setTitle(section().title);
            setPreferenceScreen(screen);
            guarded("page-"+section(),()->populate(screen));
            if(screen.getPreferenceCount()==0)
                info(screen,"availability","Параметры недоступны","Служба или профиль 8054 недоступны");
        }

        @Override public void onViewCreated(View view,Bundle state){
            super.onViewCreated(view,state);
            OemInlinePreferencesHook.bind(this,view);
        }

        @Override public void onDestroyView(){
            OemInlinePreferencesHook.bind(this,null);
            super.onDestroyView();
        }

        @Override public void onResume(){
            super.onResume();
            if(section()==NativePage.SOURCES)guarded("cec-resume",()->OemCecAutoOff8054.refresh(requireContext(),
                    findPreference(OemCecAutoOff8054.CEC_KEY),findPreference(OemCecAutoOff8054.OFF_KEY)));
        }

        protected PreferenceCategory group(PreferenceScreen screen,String key,String label){
            PreferenceCategory category=new PreferenceCategory(requireContext());
            category.setKey(key);
            category.setTitle(label);
            screen.addPreference(category);
            return category;
        }

        protected void info(PreferenceGroup group,String key,String label,String value){
            Preference row=new Preference(requireContext());
            row.setKey(key);
            row.setTitle(label);
            row.setSummary(value);
            row.setSelectable(false);
            group.addPreference(row);
        }

        protected void action(PreferenceGroup group,String key,String label,CheckedPreferenceLaunch.Route route){
            Context context=requireContext();
            guarded(key,()->{
                Intent intent=route.resolve();
                if(intent==null || !CheckedPreferenceLaunch.accessible(context,intent.getComponent()))return;
                Preference row=new Preference(context);
                row.setTitle(label);
                row.setKey(key);
                row.setOnPreferenceClickListener(item->{
                    if(!CheckedPreferenceLaunch.open(context,route))
                        android.widget.Toast.makeText(context,"Экран недоступен",android.widget.Toast.LENGTH_SHORT).show();
                    return true;
                });
                group.addPreference(row);
            });
        }

        protected void stock(PreferenceGroup group,String key,String label,String name){
            action(group,key,label,()->new Intent().setComponent(new ComponentName(OWN,OWN+"."+name)));
        }
    }

    public static final class Bluetooth extends Page {
        @Override NativePage section(){return NativePage.BLUETOOTH;}
        @Override void populate(PreferenceScreen screen){
            Context context=requireContext();
            guarded("bluetooth-adapter",()->{
                BluetoothManager manager=context.getSystemService(BluetoothManager.class);
                BluetoothAdapter adapter=manager==null?null:manager.getAdapter();
                info(screen,"bt-status","Состояние Bluetooth",adapter==null?"Нет данных":
                        adapter.isEnabled()?"Включён":"Выключен");
                if(adapter!=null) {
                    StringBuilder devices=new StringBuilder();
                    for(android.bluetooth.BluetoothDevice device:adapter.getBondedDevices()) {
                        if(devices.length()>0)devices.append(", ");
                        String name=device.getName();
                        devices.append(name==null?"Без названия":name);
                    }
                    info(screen,"bt-paired","Сопряжённые устройства",
                            devices.length()==0?"Сохранённых устройств нет":devices.toString());
                }
            });
            stock(screen,"bt-add","Добавить устройство","accessories.AddAccessoryActivity");
            OemBluetoothRemoteHook.install(context,screen,Preference.class);
            guarded("bluetooth-autopair",()->info(screen,"bt-auto","Автоматическое подключение пульта",
                OemBluetoothRemotePolicy.autoPairSettingSummary(Settings.Global.getString(
                        context.getContentResolver(),"autopair_status"))));
        }
    }

    public static final class Sources extends Page {
        @Override NativePage section(){return NativePage.SOURCES;}
        @Override void populate(PreferenceScreen screen){
            Context context=requireContext();
            PreferenceCategory inputs=group(screen,"inputs","Выбор источника");
            action(inputs,"home","Android TV",()->home(context));
            guarded("hdmi-ports",()->OemHdmiPortInfo.install(context,inputs,Preference.class));
            if(inputs.getPreferenceCount()==0)info(inputs,"inputs-unavailable","Источники","Нет подтверждённого маршрута");
            guarded("hdmi-cec",()->OemCecAutoOff8054.install(context,screen,Preference.class));
            guarded("hdmi-edid",()->OemHdmiEdidHook.install(context,screen,Preference.class));
            guarded("hdmi-autoplay",()->OemHdmiAutoPlayHook.install(context,screen,Preference.class));
            guarded("boot-destination",()->NativeBootDestination.install(context,screen));
        }

        private Intent home(Context context){
            Intent intent=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
            ResolveInfo info=context.getPackageManager().resolveActivity(intent,0);
            if(info==null || info.activityInfo==null || info.activityInfo.applicationInfo==null
                    || (info.activityInfo.applicationInfo.flags&ApplicationInfo.FLAG_SYSTEM)==0)return null;
            String pkg=info.activityInfo.packageName;
            if("android".equals(pkg) || "com.my.settings".equals(pkg) || OWN.equals(pkg))return null;
            return intent.setComponent(new ComponentName(pkg,info.activityInfo.name));
        }
    }

    public static final class Projection extends Page {
        @Override NativePage section(){return NativePage.PROJECTION;}
        @Override void populate(PreferenceScreen screen){
            OemProjectionPanel.installUnified(requireContext(),screen,Preference.class);
        }
    }

    public static final class Picture extends Page {
        @Override NativePage section(){return NativePage.PICTURE;}
        @Override void populate(PreferenceScreen screen){
            guarded("picture-read",()->OemPicturePanel.install(requireContext(),screen,Preference.class));
            OemStockUserSettings.installPictureFormat(requireContext(),screen,Preference.class);
        }
    }

    public static final class Sound extends Page {
        @Override NativePage section(){return NativePage.SOUND;}
        @Override void populate(PreferenceScreen screen){
            PreferenceCategory group=group(screen,"projectorsoft_sound_category","Звук");
            guarded("sound",()->OemSoundPanel.installUnified(requireContext(),group,Preference.class));
            guarded("mute",()->mute(screen));
        }

        private void mute(PreferenceScreen screen){
            Context context=requireContext();
            AudioManager audio=context.getSystemService(AudioManager.class);
            if(audio==null || audio.isVolumeFixed() || context.checkSelfPermission(
                    android.Manifest.permission.MODIFY_AUDIO_SETTINGS)!=android.content.pm.PackageManager.PERMISSION_GRANTED)return;
            int stream=AudioManager.STREAM_MUSIC;
            SwitchPreference row=new SwitchPreference(context);
            row.setKey("sound_mute");
            row.setTitle("Без звука · медиа");
            row.setPersistent(false);
            row.setChecked(audio.isStreamMute(stream));
            row.setOnPreferenceChangeListener((item,value)->{
                guarded("mute-change",()->{
                    if(!(value instanceof Boolean)||audio.isVolumeFixed())return;
                    boolean initial=row.isChecked();
                    boolean current=audio.isStreamMute(stream);
                    if(current==initial && current!=(Boolean)value)
                        audio.adjustStreamVolume(stream,(Boolean)value?AudioManager.ADJUST_MUTE:AudioManager.ADJUST_UNMUTE,0);
                    boolean actual=audio.isStreamMute(stream);
                    row.setChecked(actual);
                    row.setSummary(actual==(Boolean)value?null:"Запрошенное состояние не подтверждено");
                });
                return false;
            });
            screen.addPreference(row);
        }
    }

    public static final class System extends Page {
        @Override NativePage section(){return NativePage.SYSTEM;}
        @Override void populate(PreferenceScreen screen){
            Context context=requireContext();
            PreferenceCategory about=group(screen,"about","Об устройстве");
            OemSystemInfoHook.install(context,about,Preference.class);
            OemPanelInfoHook.install(context,about,Preference.class);
            info(about,"kernel","Ядро",java.lang.System.getProperty("os.version","Нет данных"));
            String machine=OemHdmiPortInfo.machineId();
            info(about,"hardware","Аппаратная ревизия",machine.isEmpty()?"Нет данных":machine);
            stock(screen,"time","Дата и время","system.DateTimeActivity");
            stock(screen,"language","Язык и ввод","system.LanguageActivity");
            stock(screen,"accounts","Учётные записи","accounts.AccountsActivity");
            stock(screen,"storage","Хранилище","device.StorageResetActivity");
            OemFontScaleHook.install(context,screen,Preference.class);
            PreferenceCategory power=group(screen,"power","Питание");
            OemAcPowerBootHook.install(context,power,Preference.class);
            OemStockUserSettings.installSwitchOffTimer(context,power,Preference.class);
            OemScreenTimeoutSafetyHook.installStatus(context,power,Preference.class);
            if(power.getPreferenceCount()==0)screen.removePreference(power);
            stock(screen,"privacy","Конфиденциальность","enterprise.EnterprisePrivacySettingsActivity");
            stock(screen,"security","Безопасность","system.SecurityActivity");
            stock(screen,"accessibility","Специальные возможности","oemlink.AccessibilitySettingsActivity");
            stock(screen,"developer","Для разработчиков","system.development.DevelopmentActivity");
        }
    }

    private static void guarded(String stage,ProjectorMenuSections.Work block){
        ProjectorMenuSections.optional("native-"+stage,block);
    }
}

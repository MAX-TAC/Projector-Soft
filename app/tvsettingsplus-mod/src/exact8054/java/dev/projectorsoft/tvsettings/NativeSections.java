package dev.projectorsoft.tvsettings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewTreeObserver;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
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

        protected void link(PreferenceGroup group, NativePage page, int order) {
            Context context = requireContext();
            Preference row = new Preference(context);
            row.setKey("projectorsoft_page_" + page.name());
            row.setTitle(page.title);
            row.setOrder(order);
            row.setFragment(page.fragment);
            row.setOnPreferenceClickListener(item -> {
                if (!CheckedPreferenceLaunch.open(context, () -> new Intent(context, page.activity)))
                    android.widget.Toast.makeText(context,"Экран недоступен",android.widget.Toast.LENGTH_SHORT).show();
                return true;
            });
            group.addPreference(row);
        }

        protected void stock(PreferenceGroup group,String key,String label,String name){
            action(group,key,label,()->new Intent().setComponent(new ComponentName(OWN,OWN+"."+name)));
        }
    }

    public static final class Remote extends Page {
        @Override NativePage section(){return NativePage.REMOTE;}
        @Override void populate(PreferenceScreen screen){
            Context context = requireContext();
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
            action(inputs,"home","Android TV",()->HomeDestination.resolve(context));
            guarded("hdmi-ports", () -> OemHdmiPortInfo.install(context, inputs, Preference.class));
            if(inputs.getPreferenceCount()==0)info(inputs,"inputs-unavailable","Источники","Нет подтверждённого маршрута");
            guarded("boot-destination",()->NativeBootDestination.install(context,screen));
            link(screen, NativePage.HDMI_CEC, 200);
            guarded("hdmi-autoplay",()->OemHdmiAutoPlayHook.install(context,screen,Preference.class));
            Preference autoplay = screen.findPreference(OemHdmiAutoPlayHook.KEY);
            if (autoplay != null) autoplay.setTitle("Автопереключение HDMI");
            guarded("hdmi-edid",()->OemHdmiEdidHook.install(context,screen,Preference.class));
            Preference edid = screen.findPreference(OemHdmiEdidHook.STATUS_KEY);
            if (edid != null) edid.setOrder(290);
        }

        @Override public void onResume(){
            super.onResume();
            PreferenceScreen screen=getPreferenceScreen();
            if (screen != null) {
                NativeBootDestination.refresh(screen);
                PreferenceCategory inputs = screen.findPreference("inputs");
                if (inputs != null && inputs.findPreference("home") == null)
                    action(inputs, "home", "Android TV", () -> HomeDestination.resolve(requireContext()));
            }
        }

    }

    public abstract static class PortStatus extends Page {
        abstract String hardware();
        @Override void populate(PreferenceScreen screen) {
            info(screen, "projectorsoft_hdmi_info_" + hardware(), "Подключение",
                    OemHdmiPortInfo.currentStatus(requireContext(), hardware()));
        }
        @Override public void onResume() {
            super.onResume();
            Preference row = findPreference("projectorsoft_hdmi_info_" + hardware());
            if (row != null) row.setSummary(OemHdmiPortInfo.currentStatus(requireContext(), hardware()));
        }
    }

    public static final class HdmiOne extends PortStatus {
        @Override NativePage section() { return NativePage.HDMI1_INFO; }
        @Override String hardware() { return "HW6"; }
    }

    public static final class HdmiTwo extends PortStatus {
        @Override NativePage section() { return NativePage.HDMI2_INFO; }
        @Override String hardware() { return "HW7"; }
    }

    public static final class Cec extends Page {
        @Override NativePage section(){return NativePage.HDMI_CEC;}
        @Override void populate(PreferenceScreen screen){
            guarded("hdmi-cec",()->OemCecAutoOff8054.install(requireContext(),screen,Preference.class));
        }
        @Override public void onResume(){
            super.onResume();
            guarded("cec-resume",()->OemCecAutoOff8054.refresh(requireContext(),
                    findPreference(OemCecAutoOff8054.CEC_KEY),findPreference(OemCecAutoOff8054.OFF_KEY)));
        }
    }

    public static final class Projection extends Page {
        @Override NativePage section(){return NativePage.PROJECTION;}
        @Override void populate(PreferenceScreen screen){
            Context context = requireContext();
            PreferenceCategory light = group(screen, "projection_light", "Источник света");
            OemLightSourcePreference.install(context, light, Preference.class);
            if (light.getPreferenceCount() == 0) screen.removePreference(light);
            PreferenceCategory focus = group(screen, "projection_focus", "Фокусировка");
            OemFocusPreferences.install(context, focus, Preference.class);
            if (focus.getPreferenceCount() == 0) screen.removePreference(focus);
            PreferenceCategory geometry = group(screen, "projection_geometry", "Коррекция изображения");
            OemMoveKeystonePreference.install(context, geometry, Preference.class);
            Preference movement = geometry.findPreference("projectorsoft_keystone_move");
            if (movement != null) movement.setOrder(200);
            OemProjectionPanel.installEditors(context, geometry, Preference.class);
            if (geometry.getPreferenceCount() == 0) screen.removePreference(geometry);
        }
    }

    public abstract static class PictureRows extends Page {
        private NativePicturePanel panel;
        abstract NativePicturePanel.Page picturePage();
        @Override void populate(PreferenceScreen screen) {
            panel = new NativePicturePanel(this, screen, picturePage());
        }
        @Override public void onStart() {
            super.onStart();
            if (panel != null) panel.start();
        }
        @Override public void onResume() {
            super.onResume();
            if (panel != null) panel.refresh();
        }
        @Override public void onStop() {
            if (panel != null) panel.stop();
            super.onStop();
        }
    }

    public static final class Picture extends PictureRows {
        @Override NativePage section(){return NativePage.PICTURE;}
        @Override NativePicturePanel.Page picturePage() { return NativePicturePanel.Page.PICTURE; }
        @Override void populate(PreferenceScreen screen) {
            super.populate(screen);
            link(screen, NativePage.COLOR_TEMPERATURE, 228);
            link(screen, NativePage.MEMC, 229);
            OemStockUserSettings.installPictureFormat(requireContext(), screen, Preference.class);
        }
    }

    public static final class ColorTemperature extends PictureRows {
        @Override NativePage section() { return NativePage.COLOR_TEMPERATURE; }
        @Override NativePicturePanel.Page picturePage() { return NativePicturePanel.Page.COLOR; }
    }

    public static final class Memc extends PictureRows {
        @Override NativePage section() { return NativePage.MEMC; }
        @Override NativePicturePanel.Page picturePage() { return NativePicturePanel.Page.MEMC; }
    }

    public static final class Sound extends Page {
        @Override NativePage section(){return NativePage.SOUND;}
        @Override void populate(PreferenceScreen screen){
            Context context = requireContext();
            NativeAudioOutputReadout.install(context, screen);
            link(screen, NativePage.EQUALIZER, 200);
            OemStockUserSettings.installAudioOutput(context, screen, Preference.class);
            PreferenceCategory effects = group(screen, "sound_processing", "Обработка звука");
            effects.setOrder(300);
            guarded("sound-processing", () -> OemInlinePreferencesHook.populateSoundProcessing(
                    context, effects, Preference.class));
            OemAdvancedSoundRoutes.install(context, effects, Preference.class);
            if (effects.getPreferenceCount() == 0) screen.removePreference(effects);
            link(screen, NativePage.DELAY, 400);
            if (OemKeyToneHook.install(context, screen, Preference.class)) {
                Preference tone = screen.findPreference("projectorsoft_key_tone");
                if (tone != null) tone.setOrder(500);
            }
        }
        @Override public void onResume() {
            super.onResume();
            if (getPreferenceScreen() != null)
                NativeAudioOutputReadout.refresh(requireContext(), getPreferenceScreen());
        }
        @Override public void onStop() {
            NativeMediaRoutePicker.cancelOnStop();
            super.onStop();
        }
    }

    public static final class Equalizer extends Page {
        private NativeEqualizerPanel panel;
        private final ViewTreeObserver.OnWindowFocusChangeListener eqFocus = focused -> {
            if (focused && panel != null) panel.refresh();
        };
        @Override NativePage section(){return NativePage.EQUALIZER;}
        @Override void populate(PreferenceScreen screen){
            panel = new NativeEqualizerPanel(requireContext(), screen);
        }
        @Override public void onViewCreated(View view, Bundle state) {
            super.onViewCreated(view, state);
            view.getViewTreeObserver().addOnWindowFocusChangeListener(eqFocus);
        }
        @Override public void onDestroyView() {
            View view = getView();
            if (view != null && view.getViewTreeObserver().isAlive())
                view.getViewTreeObserver().removeOnWindowFocusChangeListener(eqFocus);
            super.onDestroyView();
        }
        @Override public void onStart() {
            super.onStart();
            if (panel != null) panel.start();
        }
        @Override public void onResume() {
            super.onResume();
            if (panel != null) panel.refresh();
        }
        @Override public void onStop() {
            if (panel != null) panel.stop();
            super.onStop();
        }
    }

    public static final class Delay extends Page {
        @Override NativePage section(){return NativePage.DELAY;}
        @Override void populate(PreferenceScreen screen){
            PreferenceCategory controls = group(screen, "projectorsoft_sound_category", null);
            OemSoundDelayPanel.install(requireContext(), controls, Preference.class);
            if ("8054".equals(OemHdmiPortInfo.machineId())
                    && controls.findPreference("projectorsoft_sound_speaker_delay") == null)
                info(controls, "speaker_delay_unavailable", "Задержка динамиков", OemSoundDelayPanel.speakerUnavailableReason());
            if (controls.getPreferenceCount() == 0) screen.removePreference(controls);
        }
    }

    public static final class System extends Page {
        @Override NativePage section(){return NativePage.SYSTEM;}
        @Override void populate(PreferenceScreen screen){
            Context context = requireContext();
            link(screen, NativePage.ABOUT, 0);
            stock(screen,"time","Дата и время","system.DateTimeActivity");
            stock(screen,"language","Язык и ввод","system.LanguageActivity");
            stock(screen,"accounts","Учётные записи","accounts.AccountsActivity");
            stock(screen,"storage","Хранилище","device.StorageResetActivity");
            link(screen, NativePage.POWER, 20);
            stock(screen,"privacy","Конфиденциальность","enterprise.EnterprisePrivacySettingsActivity");
            stock(screen,"security","Безопасность","system.SecurityActivity");
            stock(screen,"accessibility","Специальные возможности","oemlink.AccessibilitySettingsActivity");
            stock(screen,"developer","Для разработчиков","system.development.DevelopmentActivity");
            OemFontScaleHook.install(context,screen,Preference.class);
        }
    }

    public static final class About extends Page {
        @Override NativePage section(){return NativePage.ABOUT;}
        @Override void populate(PreferenceScreen screen){
            Context context = requireContext();
            OemSystemInfoHook.install(context,screen,Preference.class);
            OemPanelInfoHook.install(context,screen,Preference.class);
            info(screen,"kernel","Ядро",java.lang.System.getProperty("os.version","Нет данных"));
            String machine = OemHdmiPortInfo.machineId();
            info(screen,"hardware","Аппаратная ревизия",machine.isEmpty()?"Нет данных":machine);
            link(screen, NativePage.HARDWARE, 400);
        }
    }

    public static final class Hardware extends Page {
        @Override NativePage section(){return NativePage.HARDWARE;}
        @Override void populate(PreferenceScreen screen){
            OemThermalTelemetryHook.install(requireContext(),screen,Preference.class);
        }
    }

    public static final class Power extends Page {
        @Override NativePage section(){return NativePage.POWER;}
        @Override void populate(PreferenceScreen screen){
            Context context = requireContext();
            OemAcPowerBootHook.install(context,screen,Preference.class);
            OemStockUserSettings.installSwitchOffTimer(context,screen,Preference.class);
            OemScreenTimeoutSafetyHook.installStatus(context,screen,Preference.class);
        }
    }

    private static void guarded(String stage,ProjectorMenuSections.Work block){
        ProjectorMenuSections.optional("native-"+stage,block);
    }
}

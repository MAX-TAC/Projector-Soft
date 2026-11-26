package dev.projectorsoft.tvsettings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
                boolean available = false;
                try {
                    Intent intent=route.resolve();
                    available = intent != null
                            && CheckedPreferenceLaunch.accessible(context,intent.getComponent());
                } catch (Exception | LinkageError missing) {
                    android.util.Log.d("TvSettingsPlusMOD", "Stock shortcut unavailable: " + key, missing);
                }
                Preference row=new Preference(context);
                row.setTitle(label);
                row.setKey(key);
                setSystemIcon(row, key);
                row.setEnabled(available);
                if (!available) row.setSummary("Недоступно в этой версии прошивки");
                row.setOnPreferenceClickListener(item->{
                    if(!CheckedPreferenceLaunch.open(context,route))
                        android.widget.Toast.makeText(context,"Экран недоступен",android.widget.Toast.LENGTH_SHORT).show();
                    return true;
                });
                group.addPreference(row);
            });
        }

        /**
         * Launcher row that starts a plain HOME intent. A component-less HOME
         * intent has no component for CheckedPreferenceLaunch.accessible(), so
         * the generic action() helper always disabled it. This one resolves at
         * click time instead.
         */
        protected void homeAction(PreferenceGroup group, String key, String label) {
            Context context = requireContext();
            Preference row = new Preference(context);
            row.setTitle(label);
            row.setKey(key);
            setSystemIcon(row, key);
            row.setEnabled(true);
            row.setOnPreferenceClickListener(item -> {
                try {
                    context.startActivity(HomeDestination.homeIntent(context));
                } catch (RuntimeException noHome) {
                    android.widget.Toast.makeText(context,
                            "Не удалось открыть рабочий стол",
                            android.widget.Toast.LENGTH_SHORT).show();
                }
                return true;
            });
            group.addPreference(row);
        }
        protected void link(PreferenceGroup group, NativePage page, int order) {
            Context context = requireContext();
            Preference row = new Preference(context);
            row.setKey("projectorsoft_page_" + page.name());
            row.setTitle(page.title);
            row.setOrder(order);
            row.setFragment(page.fragment);
            if (page == NativePage.POWER) setSystemIcon(row, "power");
            // Let stock Leanback/TwoPanelSettings resolve Preference.fragment.
            // Activity-based navigation creates an extra opaque window.
            group.addPreference(row);
        }

        protected void stock(PreferenceGroup group,String key,String label,String name){
            action(group,key,label,()->new Intent().setComponent(new ComponentName(OWN,OWN+"."+name)));
        }

        protected void stockFragment(PreferenceGroup group,String key,String label,String className) {
            Preference row = new Preference(requireContext());
            row.setKey(key);
            row.setTitle(label);
            row.setFragment(OWN + "." + className);
            setSystemIcon(row, key);
            group.addPreference(row);
        }

        private void setSystemIcon(Preference row, String key) {
            // Reuse the exact 8054 OEM drawables, not an unthemed Android icon.
            String drawable;
            switch (key) {
                case "about_stock": drawable = "ic_devices_other"; break;
                case "time": drawable = "ic_access_time"; break;
                case "language": drawable = "ic_language"; break;
                case "location": drawable = "ic_location_on"; break;
                case "accounts": drawable = "ic_accounts_and_sign_in"; break;
                case "storage": drawable = "ic_storage"; break;
                case "power": drawable = "ic_settings_power"; break;
                case "privacy": drawable = "ic_lock"; break;
                case "security": drawable = "ic_lock"; break;
                case "accessibility": drawable = "ic_accessibility_new"; break;
                case "developer": drawable = "ic_settings_developeroptions"; break;
                default: return;
            }
            int resource = requireContext().getResources().getIdentifier(drawable, "drawable", OWN);
            android.graphics.drawable.Drawable icon = resource != 0
                    ? requireContext().getDrawable(resource)
                    : requireContext().getDrawable(android.R.drawable.ic_menu_preferences);
            if (icon != null) {
                icon = icon.mutate();
                icon.setTint(android.graphics.Color.WHITE);
                row.setIcon(icon);
            }
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
            // One entry: the system launcher. Listing the two installed
            // launchers separately was not what the owner wants here.
            homeAction(inputs, "home", "Android TV");
            guarded("hdmi-ports", () -> OemHdmiPortInfo.install(context, inputs, Preference.class));
            if(inputs.getPreferenceCount()==0)info(inputs,"inputs-unavailable","Источники","Нет подтверждённого маршрута");
            guarded("boot-destination",()->NativeBootDestination.install(context,screen));
            // Everything below the boot source is grouped under one heading so
            // the screen has two clear blocks.
            PreferenceCategory extras = group(screen, "sources_extras",
                    "Дополнительные настройки");
            extras.setOrder(500);
            link(extras, NativePage.HDMI_CEC, 200);
            guarded("hdmi-autoplay",()->OemHdmiAutoPlayHook.install(
                    context,extras,Preference.class));
            Preference autoplay = extras.findPreference(OemHdmiAutoPlayHook.KEY);
            if (autoplay != null) autoplay.setTitle("Автопереключение HDMI");
            guarded("hdmi-edid",()->OemHdmiEdidHook.install(
                    context,extras,Preference.class));
            Preference edid = extras.findPreference(OemHdmiEdidHook.STATUS_KEY);
            if (edid != null) edid.setOrder(290);
            ProjectorExtras.installArc(context, extras, 285);

            ProjectorExtras.installSources(context, extras);
        }

        @Override public void onResume(){
            super.onResume();
            PreferenceScreen screen=getPreferenceScreen();
            if (screen != null) {
                NativeBootDestination.refresh(screen);
                PreferenceCategory inputs = screen.findPreference("inputs");
                if (inputs != null && inputs.findPreference("home") == null)
                    homeAction(inputs, "home", "Android TV");
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
            ProjectorExtras.installCec(requireContext(), screen);
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
            ProjectorExtras.installKeystoneReset(context, geometry);
            if (geometry.getPreferenceCount() == 0) screen.removePreference(geometry);
            // Font scaling is a display/projection preference, not a system-security setting.
            OemFontScaleHook.install(context, screen, Preference.class);
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
            link(screen, NativePage.COLOR_TEMPERATURE, 130);
            OemStockUserSettings.installPictureFormat(requireContext(), screen, Preference.class);

            // PreferenceCategory uses the stock blue section-heading style.
            // Move the EXISTING preferences rather than constructing parallel
            // controls: NativePicturePanel retains its observers and handlers.
            PreferenceCategory processing = group(screen,
                    "projectorsoft_picture_group_processing", "Обработка изображения");
            processing.setOrder(0);
            ProjectorExtras.installPicture(requireContext(),processing);
            PreferenceCategory color = group(screen,
                    "projectorsoft_picture_group_color", "Цвет и баланс белого");
            color.setOrder(1);
            PreferenceCategory video = group(screen,
                    "projectorsoft_picture_group_video", "Видеопоток (не влияет на Android)");
            video.setOrder(2);

            move(screen, processing, "projectorsoft_picture_read_picture_list_hdr");
            move(screen, processing,
                    "projectorsoft_picture_read_tv_picture_advance_video_mjc_effect");

            // The color-temperature page (Standard/Warm/Cool/User) follows
            // directly after MEMC, independently of the Picture Mode preset.
            move(screen, color, "projectorsoft_page_COLOR_TEMPERATURE");

            String[] videoKeys = {
                "projectorsoft_picture_read_picture_mode",
                "projectorsoft_picture_video_pipeline_info",
                "projectorsoft_picture_read_picture_brightness",
                "projectorsoft_picture_read_picture_contrast",
                "projectorsoft_picture_read_picture_saturation",
                "projectorsoft_picture_read_picture_sharpness",
                "projectorsoft_picture_read_picture_hue",
                "projectorsoft_picture_read_picture_gamma",
                "projectorsoft_picture_format"
            };
            for (String key : videoKeys) move(screen, video, key);
        }

        private void move(PreferenceScreen screen, PreferenceCategory destination,
                          String key) {
            Preference item = screen.findPreference(key);
            if (item == null) return; // optional stock picture format
            if (!screen.removePreference(item)) {
                android.util.Log.w("TvSettingsPlusMOD",
                        "Picture section item is not in root: " + key);
                return;
            }
            destination.addPreference(item);
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
            // NativeAudioOutputReadout used to add a second, read-only
            // "Устройство вывода звука" row above the real dropdown. Removed.
            link(screen, NativePage.EQUALIZER, 200);
            // Audio output switching and speaker duplication were removed:
            // setPreferredDevicesForStrategy returns false on this board, so the
            // controls only reported failure to the user.
            guarded("sound-processing", () -> OemInlinePreferencesHook.populateSoundProcessing(
                    context, screen, Preference.class));
            OemAdvancedSoundRoutes.install(context, screen, Preference.class);
            // SPDIF delay already lives on the OEM digital-output screen; do not
            // expose a second editor for the same setting on the Sound root.
            if (OemKeyToneHook.install(context, screen, Preference.class)) {
                Preference tone = screen.findPreference("projectorsoft_key_tone");
                if (tone != null) tone.setOrder(500);
            }
        }
        @Override public void onResume() {
            super.onResume();
            ProjectorExtras.refresh(getPreferenceScreen(), requireContext());
        }
        @Override public void onStop() {
            NativeMediaRoutePicker.cancelOnStop();
            super.onStop();
        }
    }

    public static final class Equalizer extends Page {
        // Only the AudioFlinger-level equalizer is offered. The old OEM five-band
        // panel (NativeEqualizerPanel) wrote g_fusion_sound__* keys that reach the
        // MediaTek DSP only, so bass/treble did nothing over Bluetooth or HDMI.
        private BtEqualizerPanel btPanel;
        @Override NativePage section(){return NativePage.EQUALIZER;}
        @Override void populate(PreferenceScreen screen){
            btPanel = new BtEqualizerPanel(requireContext());
            btPanel.install(screen);
        }
        @Override public void onStart() {
            super.onStart();
            if (btPanel != null) btPanel.start();
        }
        @Override public void onResume() {
            super.onResume();
            if (btPanel != null && getPreferenceScreen() != null)
                btPanel.refresh(getPreferenceScreen());
        }
        @Override public void onStop() {
            if (btPanel != null) btPanel.stop();
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
            // Actual OEM AboutFragment (verified in stock classes13.dex).
            // It stays inside MainSettings so the background remains visible.
            stockFragment(screen,"about_stock","Об устройстве","about.AboutFragment");
            // Device rename already exists inside the genuine AboutFragment.
            // CPU is injected into the bottom of About via its own lifecycle.
            stock(screen,"time","Дата и время","system.DateTimeActivity");
            link(screen, NativePage.LANGUAGE_INPUT, 15);
            stock(screen,"location","Местоположение","system.LocationActivity");
            stock(screen,"accounts","Учётные записи","accounts.AccountsActivity");
            stock(screen,"storage","Хранилище","device.StorageResetActivity");
            // Power moved to the main screen at the owner request.
            // Empty EnterprisePrivacy screen is not relevant on this exact 8054.
            stock(screen,"security","Безопасность","system.SecurityActivity");
            stock(screen,"accessibility","Специальные возможности","oemlink.AccessibilitySettingsActivity");
            stock(screen,"developer","Для разработчиков","system.development.DevelopmentActivity");
        }
    }

    /**
     * Language and input: two rows, mirroring a normal Android TV layout.
     * The stock LanguageActivity is only the language picker, so it is reached
     * from the first row instead of standing in for the whole screen.
     */
    public static final class LanguageInput extends Page {
        @Override NativePage section(){return NativePage.LANGUAGE_INPUT;}
        @Override void populate(PreferenceScreen screen){
            stock(screen,"language_system","Язык системы","system.LanguageActivity");
            stock(screen,"keyboard_input","Клавиатура и способы ввода","inputmethod.KeyboardActivity");
        }
    }
    /**
     * Full-page power-on application picker with real icons. Opened from the
     * "Приложение…" row on the Sources screen. Selecting a package stores it in
     * persist.trd.source_id, which is what the OEM boot path reads.
     */
    public static final class BootApp extends Page {
        @Override NativePage section(){return NativePage.BOOT_APP;}
        @Override void populate(PreferenceScreen screen){
            Context context = requireContext();
            String current = NativeBootDestination.bootSource();
            java.util.Map<String, String> apps;
            try {
                apps = new java.util.TreeMap<>(OemPowerOnAppHook.candidates(context));
            } catch (RuntimeException unavailable) {
                info(screen, "bootapp_unavailable", "Приложения",
                        "Список приложений недоступен");
                return;
            }
            if (apps.isEmpty()) {
                info(screen, "bootapp_empty", "Приложения",
                        "Нет установленных приложений с ярлыком");
                return;
            }
            int order = 0;
            for (java.util.Map.Entry<String, String> entry : apps.entrySet()) {
                final String pkg = entry.getKey();
                Preference row = new Preference(context);
                row.setKey("projectorsoft_bootapp_" + pkg);
                row.setTitle(entry.getValue());
                row.setOrder(order++);
                // Always show a real launcher icon so the list reads like a picker.
                try {
                    android.content.pm.PackageManager pm = context.getPackageManager();
                    row.setIcon(pm.getApplicationIcon(pkg));
                } catch (android.content.pm.PackageManager.NameNotFoundException noIcon) {
                    // leave the row without an icon
                }
                boolean selected = pkg.equals(current);
                row.setSummary(selected ? "Выбрано для включения" : null);
                row.setOnPreferenceClickListener(item -> {
                    NativeBootDestination.setBootSource(pkg);
                    android.widget.Toast.makeText(context,
                            "Приложение при включении: " + entry.getValue(),
                            android.widget.Toast.LENGTH_SHORT).show();
                    return true;
                });
                screen.addPreference(row);
            }
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
            // Live temperatures live here rather than only in the stock About
            // fragment, which is a separate activity the bridge cannot reach.
            info(screen, "projectorsoft_temp_cpu", "Температура CPU", OemCpuTemperature.read(context));
            link(screen, NativePage.HARDWARE, 400);
        }
        @Override public void onResume() {
            super.onResume();
            PreferenceScreen screen = getPreferenceScreen();
            if (screen == null) return;
            Context context = requireContext();
            Preference cpu = screen.findPreference("projectorsoft_temp_cpu");
            if (cpu != null) cpu.setSummary(OemCpuTemperature.read(context));
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
            // "Сон экрана и заставка" removed at the owner request: the screen
            // policy already forces screen-never-off, and this row only
            // restated it. The safety hook still runs at boot elsewhere.
        }
    }

    private static void guarded(String stage,ProjectorMenuSections.Work block){
        ProjectorMenuSections.optional("native-"+stage,block);
    }
}

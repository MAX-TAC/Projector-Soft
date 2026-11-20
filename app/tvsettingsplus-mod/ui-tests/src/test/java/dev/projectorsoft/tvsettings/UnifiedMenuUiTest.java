package dev.projectorsoft.tvsettings;

import android.content.Intent;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Looper;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import com.android.tv.settings.MainSettings;
import com.android.tv.settings.MainFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=33)
public class UnifiedMenuUiTest {
    @Test public void stockMainSettingsHostInstallsVisibleRootAndRetainsControllerPreferences() {
        MainSettings main = Robolectric.buildActivity(MainSettings.class).setup().get();
        MainFragment fragment = (MainFragment) main.getSupportFragmentManager().findFragmentById(android.R.id.content);
        PreferenceScreen screen = fragment.getPreferenceScreen();
        PreferenceCategory active = screen.findPreference(StockMainSettingsMenu.CATEGORY);
        assertEquals("Общие настройки", active.getTitle());
        assertNotNull(fragment.getListView().getAdapter());
        String[] expected = {"Wi-Fi и сеть", "Bluetooth", "Источники", "Проекция",
                "Изображение", "Звук", "Приложения", "Настройки устройства"};
        assertEquals(expected.length, active.getPreferenceCount());
        assertEquals(expected.length + 1, fragment.getListView().getAdapter().getItemCount());
        for (int i = 0; i < expected.length; i++) {
            Preference row = active.getPreference(i);
            assertEquals(expected[i], row.getTitle());
            assertEquals(NativePage.ROOT.get(i).fragment, row.getFragment());
            assertNotNull(row.getOnPreferenceClickListener());
        }
        assertEquals("com.android.tv.settings.connectivity.NetworkActivity",
                StockMainSettingsMenu.target(main, NativePage.NETWORK).getClassName());
        assertEquals("com.android.tv.settings.device.apps.AppsActivity",
                StockMainSettingsMenu.target(main, NativePage.APPS).getClassName());
        assertNotNull(screen.findPreference("take_bug_report"));
        assertNotNull(screen.findPreference("channel"));
        assertFalse(screen.findPreference("general_settings").isVisible());
        assertFalse(screen.findPreference("quick_settings").isVisible());
        StockMainSettingsMenu.install(fragment);
        assertSame(active, screen.findPreference(StockMainSettingsMenu.CATEGORY));
        PreferenceCategory late = new PreferenceCategory(main);
        late.setKey("suggestions");
        assertTrue(StockMainSettingsMenu.addAuxiliary(screen, late));
        assertFalse(late.isVisible());
    }

    @Test public void settingsKeyInsideSectionTargetsOriginalMainSettings() {
        SourcesSettingsActivity activity = Robolectric.buildActivity(SourcesSettingsActivity.class).setup().get();
        assertTrue(activity.onKeyUp(KeyEvent.KEYCODE_SETTINGS,
                new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SETTINGS)));
        assertEquals(StockMainSettingsMenu.STOCK_MAIN,
                shadowOf(activity).getNextStartedActivity().getComponent().getClassName());
    }

    @Test public void rootStartsAndBuildsOrderedNativePreviewRoutes(){
        ModMainSettingsActivity root=Robolectric.buildActivity(ModMainSettingsActivity.class).setup().get();
        NativeRootFragment fragment=(NativeRootFragment)root.getSupportFragmentManager().findFragmentById(android.R.id.content);
        assertNotNull(fragment.getView());
        assertNotNull(fragment.getListView().getAdapter());
        PreferenceScreen screen=fragment.getPreferenceScreen();
        String[] titles={"Wi-Fi и сеть","Bluetooth","Источники","Проекция","Изображение","Звук","Приложения","Настройки устройства"};
        assertEquals(titles.length,screen.getPreferenceCount());
        for(int i=0;i<titles.length;i++){
            Preference row=screen.getPreference(i);
            assertEquals(titles[i],row.getTitle().toString());
            assertEquals(NativePage.ROOT.get(i).fragment,row.getFragment());
            assertFalse(row.getFragment().contains("uicustom"));
            assertNull(row.getIntent());
        }
    }

    @Test public void standaloneSectionsUseLeanbackLifecycleAndIgnoreExternalExtras(){
        Intent external=new Intent("android.intent.action.FACTORY_RESET")
                .putExtra("volumeUuid","unexpected").putExtra("screen_off_timeout",3);
        for(NativePage page:NativePage.values()){
            ActivityController<? extends android.app.Activity> lifecycle=Robolectric.buildActivity(page.activity,external).setup();
            ModSettingsActivity activity=(ModSettingsActivity)lifecycle.get();
            LeanbackPreferenceFragmentCompat fragment=(LeanbackPreferenceFragmentCompat)
                    activity.getSupportFragmentManager().findFragmentById(android.R.id.content);
            assertEquals(page.fragment,fragment.getClass().getName());
            assertEquals(page.title,fragment.getPreferenceScreen().getTitle().toString());
            assertNotNull(fragment.getListView().getAdapter());
            assertFalse(fragment.getArguments().containsKey("volumeUuid"));
            assertNull(shadowOf(activity).getNextStartedActivity());
            assertNull(Settings.System.getString(activity.getContentResolver(),"screen_off_timeout"));
            assertNull(Settings.Secure.getString(activity.getContentResolver(),"sleep_timeout"));
            assertNull(Settings.Secure.getString(activity.getContentResolver(),"screensaver_enabled"));
            int count=fragment.getPreferenceScreen().getPreferenceCount();
            lifecycle.pause().resume();
            shadowOf(Looper.getMainLooper()).idle();
            assertEquals(count,fragment.getPreferenceScreen().getPreferenceCount());
            activity.onBackPressed();
            assertTrue(activity.isFinishing());
            lifecycle.pause().stop().destroy();
        }
    }

    @Test public void foregroundMediaInputLaunchesOnlyNativeSources(){
        ModMainSettingsActivity activity=Robolectric.buildActivity(ModMainSettingsActivity.class).setup().get();
        assertTrue(activity.onKeyUp(KeyEvent.KEYCODE_TV_INPUT,new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_TV_INPUT)));
        assertEquals(SourcesSettingsActivity.class.getName(),
                shadowOf(activity).getNextStartedActivity().getComponent().getClassName());
    }

    @Test public void oneBootPropertyHasOneCurrentDestinationAndVerifiedReadback() throws Exception {
        Map<String,String> choices=Map.of("HOME","Android TV","id/HW6","HDMI 1","app.video","Приложение");
        class Property implements OemPowerOnAppPolicy.Property {
            String value="HOME";
            int writes;
            boolean changes=true;
            @Override public String read(){return value;}
            @Override public boolean write(String next){writes++;if(changes)value=next;return true;}
        }
        Property store=new Property();
        assertEquals(NativeBootDestination.Result.CONFIRMED,
                NativeBootDestination.apply(store,choices,choices,"HOME","id/HW6"));
        assertEquals("id/HW6",store.value);
        assertEquals(NativeBootDestination.Result.STALE,
                NativeBootDestination.apply(store,choices,choices,"HOME","app.video"));
        assertEquals(NativeBootDestination.Result.UNSUPPORTED,
                NativeBootDestination.apply(store,choices,Map.of("id/HW6","HDMI 1"),"id/HW6","app.video"));
        assertEquals(NativeBootDestination.Result.UNSUPPORTED,
                NativeBootDestination.apply(store,choices,Map.of("HOME","Android TV","app.video","Приложение"),
                        "id/HW6","app.video"));
        assertEquals(1,store.writes);
        store.changes=false;
        assertEquals(NativeBootDestination.Result.UNCONFIRMED,
                NativeBootDestination.apply(store,choices,choices,"id/HW6","app.video"));
        assertEquals("id/HW6",store.value);
    }

    @Test public void sourcesRefreshesBootDestinationOnReturnWithoutRecreatingRow() {
        ActivityController<SourcesSettingsActivity> lifecycle =
                Robolectric.buildActivity(SourcesSettingsActivity.class).setup();
        SourcesSettingsActivity activity = lifecycle.get();
        NativeSections.Sources fragment = (NativeSections.Sources)
                activity.getSupportFragmentManager().findFragmentById(android.R.id.content);
        PreferenceScreen screen = fragment.getPreferenceScreen();
        NativeBootDestination.Row previous = screen.findPreference(NativeBootDestination.KEY);
        if (previous != null) screen.removePreference(previous);
        BootProperty property = new BootProperty();
        NativeBootDestination.Row row = new NativeBootDestination.Row(activity, property,
                () -> Map.of("HOME", "Android TV", "id/HW7", "HDMI 2"));
        screen.addPreference(row);
        row.refresh();
        assertEquals("Android TV", row.getSummary());
        lifecycle.pause();
        property.value = "id/HW7";
        lifecycle.resume();
        assertSame(row, screen.findPreference(NativeBootDestination.KEY));
        assertEquals("id/HW7", row.getValue());
        assertEquals("HDMI 2", row.getSummary());
        assertEquals(0, property.writes);
    }

    @Test public void bootDestinationRecoversItsOptionsAfterReadFailure() {
        android.content.Context context = org.robolectric.RuntimeEnvironment.getApplication();
        PreferenceScreen screen = new PreferenceManager(context).createPreferenceScreen(context);
        BootProperty property = new BootProperty();
        java.util.Map<String, String> choices = new java.util.LinkedHashMap<>();
        choices.put("HOME", "Android TV");
        choices.put("id/HW6", "HDMI 1");
        NativeBootDestination.Row row = new NativeBootDestination.Row(context, property, () -> choices);
        screen.addPreference(row);
        property.failRead = true;
        row.refresh();
        assertFalse(row.isEnabled());
        assertNull(row.getValue());
        assertEquals(0, row.getEntryValues().length);
        choices.remove("id/HW6");
        choices.put("id/HW7", "HDMI 2");
        property.failRead = false;
        row.refresh();
        assertTrue(row.isEnabled());
        assertFalse(row.isPersistent());
        assertArrayEquals(new CharSequence[]{"HOME", "id/HW7"}, row.getEntryValues());
        assertEquals("HOME", row.getValue());
        assertEquals(0, property.writes);
    }

    @Test public void openBootDestinationDialogChecksOriginalStateAndStillAvailableOptions() {
        android.content.Context context = org.robolectric.RuntimeEnvironment.getApplication();
        PreferenceScreen screen = new PreferenceManager(context).createPreferenceScreen(context);
        BootProperty property = new BootProperty();
        java.util.Map<String, String> choices = new java.util.LinkedHashMap<>();
        choices.put("HOME", "Android TV");
        choices.put("id/HW6", "HDMI 1");
        NativeBootDestination.Row row = new NativeBootDestination.Row(context, property, () -> choices);
        screen.addPreference(row);
        row.refresh();

        assertFalse(row.callChangeListener("id/HW6"));
        row.onClick();
        property.value = "id/HW6";
        row.refresh();
        assertFalse(row.callChangeListener("HOME"));
        assertEquals(0, property.writes);
        assertEquals("id/HW6", row.getValue());

        row.onClick();
        choices.put("id/HW7", "HDMI 2");
        row.refresh();
        assertFalse(row.callChangeListener("id/HW7"));
        assertEquals(0, property.writes);

        row.onClick();
        choices.remove("id/HW7");
        assertFalse(row.callChangeListener("id/HW7"));
        assertEquals(0, property.writes);

        row.onClick();
        assertFalse(row.callChangeListener("HOME"));
        assertEquals(1, property.writes);
        assertEquals("HOME", row.getValue());
        assertEquals("Android TV", row.getSummary());
    }

    private static final class BootProperty implements OemPowerOnAppPolicy.Property {
        String value = "HOME";
        boolean failRead;
        int writes;

        @Override public String read() throws Exception {
            if (failRead) throw new IllegalStateException("Property unavailable");
            return value;
        }

        @Override public boolean write(String next) {
            writes++;
            value = next;
            return true;
        }
    }

    @Test public void nativeCecWidgetUsesFreshStateWithFalseListenerReturn(){
        HdmiCecSettingsActivity activity=Robolectric.buildActivity(HdmiCecSettingsActivity.class).setup().get();
        NativeSections.Cec fragment=(NativeSections.Cec)
                activity.getSupportFragmentManager().findFragmentById(android.R.id.content);
        PreferenceScreen screen=fragment.getPreferenceScreen();
        class Device implements CecAutoOff8054.Backend {
            int cec=1, setting, writes;
            @Override public CecAutoOff8054.State read(){return CecAutoOff8054.State.decode(cec,setting);}
            @Override public void setAutoOff(boolean value){writes++;setting=value?1:0;}
        }
        Device device=new Device();
        assertTrue(OemCecAutoOff8054.install(activity,screen,Preference.class,()->device));
        SwitchPreference row=screen.findPreference(OemCecAutoOff8054.OFF_KEY);
        assertTrue(row.isEnabled());
        assertFalse(row.callChangeListener(true));
        assertTrue(row.isChecked());
        assertEquals(1,device.writes);
        assertFalse(row.callChangeListener(true));
        assertEquals(1,device.writes);
        device.cec=0;
        assertFalse(row.callChangeListener(false));
        assertFalse(row.isEnabled());
        assertEquals(1,device.writes);
    }

    @Test public void soundRoutesKeepEqAndDelayOnSeparateScreensWithoutMediaVolume() {
        SoundSettingsActivity activity = Robolectric.buildActivity(SoundSettingsActivity.class).setup().get();
        NativeSections.Sound fragment = (NativeSections.Sound)
                activity.getSupportFragmentManager().findFragmentById(android.R.id.content);
        PreferenceScreen screen = fragment.getPreferenceScreen();
        Preference equalizer = screen.findPreference("projectorsoft_page_EQUALIZER");
        Preference delay = screen.findPreference("projectorsoft_page_DELAY");
        assertEquals(NativePage.EQUALIZER.fragment, equalizer.getFragment());
        assertEquals(NativePage.DELAY.fragment, delay.getFragment());
        assertTrue(equalizer.getOrder() < delay.getOrder());
        assertNull(screen.findPreference("projectorsoft_media_volume"));
        assertNull(screen.findPreference("sound_mute"));
        assertNull(screen.findPreference("projectorsoft_sound_mode"));

        EqualizerSettingsActivity eq = Robolectric.buildActivity(EqualizerSettingsActivity.class).setup().get();
        NativeSections.Equalizer eqPage = (NativeSections.Equalizer)
                eq.getSupportFragmentManager().findFragmentById(android.R.id.content);
        assertNull(eqPage.findPreference("projectorsoft_media_volume"));
        assertNull(eqPage.findPreference("projectorsoft_page_DELAY"));
    }

    @Test public void directHdmiStatusDoesNotInventAnActivePortOrLaunchLiveTv() {
        HdmiOneStatusActivity one = Robolectric.buildActivity(HdmiOneStatusActivity.class).setup().get();
        HdmiTwoStatusActivity two = Robolectric.buildActivity(HdmiTwoStatusActivity.class).setup().get();
        NativeSections.HdmiOne first = (NativeSections.HdmiOne)
                one.getSupportFragmentManager().findFragmentById(android.R.id.content);
        NativeSections.HdmiTwo second = (NativeSections.HdmiTwo)
                two.getSupportFragmentManager().findFragmentById(android.R.id.content);
        Preference hw6 = first.findPreference("projectorsoft_hdmi_info_HW6");
        Preference hw7 = second.findPreference("projectorsoft_hdmi_info_HW7");
        assertFalse(hw6.isSelectable());
        assertFalse(hw7.isSelectable());
        assertEquals("Вход недоступен", hw6.getSummary());
        assertEquals("Вход недоступен", hw7.getSummary());
        assertNull(shadowOf(one).getNextStartedActivity());
        assertNull(shadowOf(two).getNextStartedActivity());
    }

    @Test public void fontScaleSummaryRendersLiteralPercent() {
        SystemSettingsActivity activity = Robolectric.buildActivity(SystemSettingsActivity.class).setup().get();
        NativeSections.System page = (NativeSections.System)
                activity.getSupportFragmentManager().findFragmentById(android.R.id.content);
        Preference row = page.findPreference("projectorsoft_font_scale");
        assertNotNull(row);
        assertTrue(row.getSummary().toString().contains("100%"));
        assertFalse(row.getSummary().toString().contains("%%"));
    }

    @Test public void rgbHasItsOwnReadOnlyColorTemperatureScreen() {
        PictureSettingsActivity activity = Robolectric.buildActivity(PictureSettingsActivity.class).setup().get();
        NativeSections.Picture picture = (NativeSections.Picture)
                activity.getSupportFragmentManager().findFragmentById(android.R.id.content);
        assertNotNull(picture.findPreference("projectorsoft_page_COLOR_TEMPERATURE"));
        assertNull(picture.findPreference("projectorsoft_picture_read_picture_red_gain"));

        ColorTemperatureSettingsActivity colors = Robolectric.buildActivity(ColorTemperatureSettingsActivity.class)
                .setup().get();
        NativeSections.ColorTemperature fragment = (NativeSections.ColorTemperature)
                colors.getSupportFragmentManager().findFragmentById(android.R.id.content);
        for (String key : new String[]{"picture_color_temperature", "picture_red_gain", "picture_green_gain",
                "picture_blue_gain"}) {
            Preference row = fragment.findPreference("projectorsoft_picture_read_" + key);
            assertNotNull(row);
            assertFalse(row.isSelectable());
            assertNull(row.getOnPreferenceChangeListener());
        }
        assertNull(fragment.findPreference("projectorsoft_picture_read_picture_brightness"));
        assertNull(Settings.Global.getString(colors.getContentResolver(), "picture_red_gain"));
    }

    @Test @GraphicsMode(GraphicsMode.Mode.NATIVE)
    public void captureLeanbackFixtureScreensAt720p() throws Exception {
        capture("root",MainSettings.class);
        capture("sources",SourcesSettingsActivity.class);
        capture("projection",ProjectionSettingsActivity.class);
        capture("picture",PictureSettingsActivity.class);
        capture("sound",SoundSettingsActivity.class);
    }

    private void capture(String label,Class<? extends Activity> type) throws Exception {
        Activity activity=Robolectric.buildActivity(type).setup().get();
        View content=activity.getWindow().getDecorView();
        content.measure(View.MeasureSpec.makeMeasureSpec(1280,View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(720,View.MeasureSpec.EXACTLY));
        content.layout(0,0,1280,720);
        shadowOf(Looper.getMainLooper()).idle();
        File folder=new File("build/ui-evidence");
        assertTrue(folder.isDirectory() || folder.mkdirs());
        Bitmap image=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        content.draw(new Canvas(image));
        try(FileOutputStream out=new FileOutputStream(new File(folder,label+"-leanback-fixture.png"))){
            assertTrue(image.compress(Bitmap.CompressFormat.PNG,100,out));
        } finally {
            image.recycle();
        }
    }
}

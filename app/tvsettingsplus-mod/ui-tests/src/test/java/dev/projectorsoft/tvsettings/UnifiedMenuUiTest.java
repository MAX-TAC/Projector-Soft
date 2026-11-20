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
    @Test public void rootStartsAndBuildsOrderedNativePreviewRoutes(){
        ModMainSettingsActivity root=Robolectric.buildActivity(ModMainSettingsActivity.class).setup().get();
        NativeRootFragment fragment=(NativeRootFragment)root.getSupportFragmentManager().findFragmentById(android.R.id.content);
        assertNotNull(fragment.getView());
        assertNotNull(fragment.getListView().getAdapter());
        PreferenceScreen screen=fragment.getPreferenceScreen();
        String[] titles={"Wi-Fi и сеть","Bluetooth","Источники","Проекция","Изображение","Звук","Приложения","Система"};
        assertEquals(titles.length,screen.getPreferenceCount());
        for(int i=0;i<titles.length;i++){
            Preference row=screen.getPreference(i);
            assertEquals(titles[i],row.getTitle().toString());
            assertEquals(NativePage.values()[i].fragment,row.getFragment());
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
        assertEquals(1,store.writes);
        store.changes=false;
        assertEquals(NativeBootDestination.Result.UNCONFIRMED,
                NativeBootDestination.apply(store,choices,choices,"id/HW6","app.video"));
        assertEquals("id/HW6",store.value);
    }

    @Test public void nativeCecWidgetUsesFreshStateWithFalseListenerReturn(){
        SourcesSettingsActivity activity=Robolectric.buildActivity(SourcesSettingsActivity.class).setup().get();
        NativeSections.Sources fragment=(NativeSections.Sources)
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

    @Test @GraphicsMode(GraphicsMode.Mode.NATIVE)
    public void captureLeanbackFixtureScreensAt720p() throws Exception {
        capture("root",ModMainSettingsActivity.class);
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

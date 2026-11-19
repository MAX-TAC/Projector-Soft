package dev.projectorsoft.tvsettings;

import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowSystemProperties;

import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=33)
public class UnifiedMenuUiTest {
    @Test public void rootOrderAndAllEightExplicitActivityRoutes() {
        Activity activity=Robolectric.buildActivity(ModMainSettingsActivity.class).setup().get();
        String[] titles={"Wi-Fi и сеть","Bluetooth","Источники","Проекция","Изображение","Звук","Приложения","Система"};
        for(int i=0;i<titles.length;i++){
            TextView item=activity.findViewById(100+i);
            assertEquals(titles[i],item.getText().toString());
            assertTrue(item.isFocusable());
            assertTrue(item.performClick());
            Intent route=shadowOf(activity).getNextStartedActivity();
            assertEquals(ModSettingsActivity.activityFor(ModMenuCatalog.Page.values()[i]).getName(),
                    route.getComponent().getClassName());
            assertNull(route.getExtras());
        }
        assertNull(activity.findViewById(108));
    }

    @Test public void actualLifecycleOpensEverySectionAndNeverObeysUntrustedExtras() {
        ShadowSystemProperties.override("ro.rjpj.machine.id","8054");
        Intent crafted=new Intent("android.intent.action.FACTORY_RESET").putExtra("sleep_timeout",99)
                .putExtra("component","com.my.settings").putExtra("hdmi","HW7");
        for(ModMenuCatalog.Page page:ModMenuCatalog.Page.values()) {
            ActivityController<? extends Activity> controller=Robolectric
                    .buildActivity(ModSettingsActivity.activityFor(page),crafted).setup();
            Activity activity=controller.get();
            List<String> labels=new ArrayList<>();
            gather(activity.findViewById(android.R.id.content),labels);
            for(ModMenuCatalog.Row row:ModMenuCatalog.rows(page)) {
                assertEquals(page+" item "+row.title,1,labels.stream().filter(label->label.equals(row.title)).count());
            }
            assertNull(shadowOf(activity).getNextStartedActivity());
            assertNull(Settings.System.getString(activity.getContentResolver(),"screen_off_timeout"));
            assertNull(Settings.Secure.getString(activity.getContentResolver(),"sleep_timeout"));
            assertNull(Settings.Secure.getString(activity.getContentResolver(),"screensaver_enabled"));
            TextView content=activity.findViewById(1000);
            assertEquals(100+page.ordinal(),content.getNextFocusLeftId());
            assertEquals(1000,activity.findViewById(100+page.ordinal()).getNextFocusRightId());
            content.requestFocus();
            controller.pause().resume();
            assertEquals(content.getId(),activity.getCurrentFocus().getId());
            activity.onBackPressed();
            assertTrue(activity.isFinishing());
            controller.pause().stop().destroy();
        }
    }

    @Test public void unsafeProjectorOperationsAreNonClickable() {
        for(Class<? extends Activity> type:new Class[]{SourcesSettingsActivity.class,ProjectionSettingsActivity.class,
                PictureSettingsActivity.class,SoundSettingsActivity.class,SystemSettingsActivity.class}) {
            Activity activity=Robolectric.buildActivity(type).setup().get();
            ModMenuCatalog.Page page=((ModSettingsActivity)activity).page();
            int id=1000;
            for(ModMenuCatalog.Row row:ModMenuCatalog.rows(page)){
                if(row.kind==ModMenuCatalog.Kind.GROUP)continue;
                View view=activity.findViewById(id++);
                if(row.kind==ModMenuCatalog.Kind.BLOCKED || row.kind==ModMenuCatalog.Kind.READ)
                    assertFalse(row.title,view.isClickable());
            }
        }
    }

    private void gather(View view,List<String> labels){
        if(view instanceof TextView)labels.add(((TextView)view).getText().toString().split("\n",2)[0]);
        if(view instanceof ViewGroup) {
            ViewGroup group=(ViewGroup)view;
            for(int i=0;i<group.getChildCount();i++)gather(group.getChildAt(i),labels);
        }
    }
}

package dev.projectorsoft.tvsettings;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Looper;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroupAdapter;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowSystemProperties;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=33)
public class ProjectorMenuUiTest {
    private Context context;
    private Activity activity;
    private PreferenceScreen screen;
    private ProjectorMenuSections sections;

    @Before public void createScreen() throws Exception {
        activity=Robolectric.buildActivity(Activity.class).setup().get();
        context=new ContextThemeWrapper(activity,androidx.appcompat.R.style.Theme_AppCompat);
        screen=new PreferenceManager(context).createPreferenceScreen(context);
        sections=new ProjectorMenuSections(context,screen);
    }

    public static class Host {
        private final Context context;
        private final PreferenceScreen screen;
        Host(Context context,PreferenceScreen screen){this.context=context;this.screen=screen;}
        public Context getContext(){return context;}
        public PreferenceScreen getPreferenceScreen(){return screen;}
    }

    @Test public void productionHostProvidesSixVisibleNonemptyCategoriesAndReopens() {
        ShadowSystemProperties.override("ro.rjpj.machine.id","8054");
        Host host=new Host(context,screen);
        OemInlinePreferencesHook.populate(host);
        OemInlinePreferencesHook.populate(host);
        List<String> keys=visibleKeys();
        for(String name:new String[]{"picture","sound","placement","hdmi","power","system"}) {
            String key="projectorsoft_"+name+"_category";
            assertTrue(key,keys.contains(key));
            PreferenceCategory category=screen.findPreference(key);
            assertNotNull(category);
            assertTrue(key,category.getPreferenceCount()>0);
        }
        assertFalse(keys.contains("projectorsoft_picture_mode"));
    }

    @Test public void optionalGetterAndUnavailableActivityKeepOtherRows() {
        sections.add("picture","Picture",1,true,group->{
            ProjectorMenuSections.optional("oem-getter",()->{throw new SecurityException("getter denied");});
            row(group,"readout");
            Preference missing=row(group,"missing-activity");
            Intent intent=new Intent().setComponent(new ComponentName("unavailable","Settings"));
            CheckedPreferenceLaunch.attach(context,Preference.class,missing,intent,()->intent);
        });
        sections.add("sound","Sound",2,true,group->row(group,"sound-mode"));
        Preference missing=screen.findPreference("missing-activity");
        assertTrue(missing.getOnPreferenceClickListener().onPreferenceClick(missing));
        assertNull(shadowOf(activity).getNextStartedActivity());
        assertTrue(visibleKeys().contains("readout"));
        assertTrue(visibleKeys().contains("sound-mode"));
    }

    @Test public void reflectionFailureRollsBackOnlyCurrentSectionAndRetryWorks() {
        sections.add("broken","Broken",1,true,group->{
            row(group,"partially-installed");
            group.getClass().getMethod("missingOemApi");
        });
        sections.add("optional","Optional",2,false,group->{});
        sections.add("empty","Required",3,true,group->{
            row(group,"hidden").setVisible(false);
        });
        assertEquals(java.util.Arrays.asList("empty","empty_availability"),visibleKeys());
        assertNull(screen.findPreference("partially-installed"));
        sections.add("broken","Recovered",1,true,group->row(group,"new-row"));
        sections.add("broken","Duplicate",1,true,group->fail("reopen must not duplicate"));
        assertTrue(visibleKeys().contains("new-row"));
    }

    @Test public void stockViewEntryIsVisibleFocusableAndStartsOnlyExplicitAlias() {
        ComponentName target=new ComponentName(activity.getPackageName(),OemGeneralMenuBridge.ALIAS);
        ActivityInfo info=new ActivityInfo();
        info.packageName=target.getPackageName();
        info.name=target.getClassName();
        info.enabled=true;
        info.applicationInfo=activity.getApplicationInfo();
        info.exported=true;
        shadowOf(activity.getPackageManager()).addOrUpdateActivity(info);
        LinearLayout list=new LinearLayout(activity);
        list.setOrientation(LinearLayout.VERTICAL);
        View original=new View(activity);
        list.addView(original);
        activity.setContentView(list);
        Runnable launch=()->assertTrue(OemGeneralMenuBridge.open(activity,false));
        OemGeneralMenuBridge.addEntry(original,launch);
        OemGeneralMenuBridge.addEntry(original,launch);
        Button entry=(Button)list.getChildAt(0);
        assertSame(original,list.getChildAt(1));
        assertEquals(2,list.getChildCount());
        assertTrue(entry.isShown());
        assertTrue(entry.isFocusable());
        assertTrue(entry.performClick());
        ShadowActivity shadow=shadowOf(activity);
        Intent actual=shadow.getNextStartedActivity();
        assertEquals(OemGeneralMenuBridge.ACTION,actual.getAction());
        assertEquals(target,actual.getComponent());
    }

    private List<String> visibleKeys() {
        PreferenceGroupAdapter adapter=new PreferenceGroupAdapter(screen);
        shadowOf(Looper.getMainLooper()).idle();
        List<String> keys=new ArrayList<>();
        for(int i=0;i<adapter.getItemCount();i++)keys.add(adapter.getItem(i).getKey());
        return keys;
    }

    private Preference row(Object group,String key) {
        Preference item=new Preference(context);
        item.setKey(key);
        item.setTitle(key);
        assertTrue(((PreferenceCategory)group).addPreference(item));
        return item;
    }
}

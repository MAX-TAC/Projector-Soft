package dev.projectorsoft.tvsettings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.view.View;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.SwitchPreference;

public final class MenuIntegrationAdapterTest {
    private static final String PANEL="projectorsoft_panel_configuration";

    public static final class Manager {
        private static Manager instance;
        private final Display display=new Display();
        public static Manager getInstance(){throw new AssertionError("Factory initialization");}
    }

    public static final class Display {
        private String width="1920";
        public String getPanelType(){return "Panel A";}
        public String getPanelVersion(){throw new IllegalStateException("unavailable");}
        public String getPanelWidth(){return width;}
        public String getPanelHeight(){return "0";}
    }

    public static final class Fragment {
        private final PreferenceGroup group;
        Fragment(Context context){group=new PreferenceGroup(context);}
        public Preference findPreference(CharSequence key){return group.find(key);}
    }

    private static void check(boolean value,String message) {
        if (!value) throw new AssertionError(message);
    }

    private static void panelLifecycle(Context context) {
        Fragment fragment=new Fragment(context);
        View first=new View(context);
        first.setAttached(true);
        OemPanelInfoHook.install(context,fragment.group,Preference.class);
        OemPanelInfoHook.bind(fragment,first);
        OemPanelInfoHook.bind(fragment,first);
        Preference panel=fragment.findPreference(PANEL);
        Preference width=fragment.findPreference(PANEL+"_width");
        check(!panel.visible && first.count()==1 && first.getViewTreeObserver().count()==1,"cold/duplicate bind");
        Manager.instance=new Manager();
        first.getViewTreeObserver().focus();
        check(panel.visible && width.visible && width.summary.contains("1920")
                && !fragment.findPreference(PANEL+"_version").visible,"wake up with a partial OEM cache");
        width.rejectSummary=true;
        first.getViewTreeObserver().focus();
        check(!panel.visible && !width.visible,"render refusal must hide previously published data");
        width.rejectSummary=false;
        View replacement=new View(context);
        replacement.setAttached(true);
        Manager.instance.display.width="1280";
        OemPanelInfoHook.bind(fragment,replacement);
        check(first.count()==0 && first.getViewTreeObserver().count()==0
                && width.visible && width.summary.contains("1280"),"replacement must release old View");
        replacement.setAttached(false);
        Manager.instance=null;
        replacement.getViewTreeObserver().focus();
        check(panel.visible && replacement.getViewTreeObserver().count()==0,"detached stale observer");
        replacement.setAttached(true);
        check(!panel.visible,"reopen must not retain previous cache");
        OemPanelInfoHook.bind(fragment,null);
        check(replacement.count()==0 && replacement.getViewTreeObserver().count()==0,"teardown leaked");
    }

    private static void launch(Context context) throws Exception {
        Preference row=new Preference(context);
        Intent intent=new Intent("settings").setComponent(new ComponentName("vendor","Screen"));
        context.manager.target.packageName="vendor";
        context.manager.target.name="Screen";
        context.manager.target.applicationInfo.uid=33;
        CheckedPreferenceLaunch.attach(context,Preference.class,row,intent,()->intent);
        check(row.click() && context.starts==1,"valid explicit route");
        context.manager.target.exported=false;
        check(row.click() && context.starts==1,"private different-UID component");
        context.manager.target.applicationInfo.uid=context.app.uid;
        check(row.click() && context.starts==2,"private same-UID component");
        context.manager.target.permission="restricted";
        context.granted=false;
        check(row.click() && context.starts==2,"revoked permission");
        context.granted=true;
        context.manager.state=2;
        check(row.click() && context.starts==2,"component disabled after installing row");
        context.manager.state=0;
        context.manager.missing=true;
        check(row.click() && context.starts==2,"missing target");
        context.manager.missing=false;
        context.failLaunch=true;
        check(row.click() && context.starts==2,"launch failure must be consumed");
        context.failLaunch=false;
    }

    private static void autoplay(Context context) throws Exception {
        PreferenceGroup group=new PreferenceGroup(context);
        check(OemHdmiAutoPlayHook.install(context,group,Preference.class),"install HDMI auto switch");
        SwitchPreference row=(SwitchPreference) group.find(OemHdmiAutoPlayHook.KEY);
        SystemProperties.value="1";
        check(!row.change(true) && row.checked && SystemProperties.writes==0,"stale value must refuse any write");
        SystemProperties.partial=true;
        check(!row.change(false) && !row.checked && SystemProperties.writes==1,"partial write readback without retry");
        SystemProperties.failRead=true;
        OemHdmiAutoPlayHook.refresh(row);
        check(!row.enabled && SystemProperties.writes==1,"unavailable read disables and writes nothing");
        SystemProperties.failRead=false;
        OemHdmiAutoPlayHook.refresh(row);
        check(row.enabled && !row.checked,"refresh renders real state");
    }

    public static void main(String[] args) throws Exception {
        Context context=new Context();
        panelLifecycle(context);
        launch(context);
        autoplay(context);
        System.out.println("PASS production PIC18 lifecycle, checked navigation and HDMI autoplay");
    }
}

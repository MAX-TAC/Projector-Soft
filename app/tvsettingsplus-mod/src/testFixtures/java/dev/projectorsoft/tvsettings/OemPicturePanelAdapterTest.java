package dev.projectorsoft.tvsettings;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.view.View;
import androidx.preference.Preference;
import java.util.HashMap;
import java.util.Map;

public final class OemPicturePanelAdapterTest {
    private static final String CONTRAST="picture_contrast";
    private static final String CONTRAST_ROW="projectorsoft_picture_read_"+CONTRAST;

    public static final class Fragment {
        final Map<String,Preference> rows=new HashMap<>();
        public Preference findPreference(CharSequence key) {return rows.get(key.toString());}
    }

    private static void check(boolean ok,String message) {
        if(!ok)throw new AssertionError(message);
    }

    public static void main(String[] args) {
        Context context=new Context();
        ContentResolver resolver=context.getContentResolver();
        resolver.rejected=PicturePanelState.MODE;
        Fragment fragment=new Fragment();
        fragment.rows.put(OemPicturePanel.STATUS,new Preference());
        Preference contrast=new Preference();
        fragment.rows.put(CONTRAST_ROW,contrast);
        View first=new View(context);
        first.setAttached(true);
        Settings.Global.values.put(CONTRAST,"40");
        OemPicturePanel.bind(fragment,first);
        OemPicturePanel.bind(fragment,first);
        first.setAttached(true);
        int subscriptions=PicturePanelState.KEYS.length-1;
        check(first.count()==1 && first.getViewTreeObserver().count()==1
                && resolver.observers.size()==subscriptions && resolver.registrations==subscriptions,
                "same view must register once, remaining keys must survive a failed registration");
        check("40".equals(contrast.summary),"attach did not read initial value");

        Settings.Global.values.put(CONTRAST,"41");
        resolver.notifyChange(CONTRAST);
        check("41".equals(contrast.summary),"observer did not refresh remaining key");

        View replacement=new View(context);
        replacement.setAttached(true);
        OemPicturePanel.bind(fragment,replacement);
        first.setAttached(false);
        check(first.count()==0 && first.getViewTreeObserver().count()==0
                && resolver.observers.size()==subscriptions && resolver.registrations==subscriptions*2,
                "recreating the view must remove old listeners and keep the replacement observers");

        replacement.setAttached(false);
        check(resolver.observers.isEmpty() && replacement.getViewTreeObserver().count()==0,
                "detaching must remove partial registrations and the focus listener");
        Settings.Global.values.put(CONTRAST,"42");
        resolver.notifyChange(CONTRAST);
        check("41".equals(contrast.summary),"detached screen received a callback");
        replacement.setAttached(true);
        check("42".equals(contrast.summary) && resolver.registrations==subscriptions*3,
                "reopening must read the latest value and re-register once");

        Settings.Global.values.put(CONTRAST,"43");
        replacement.getViewTreeObserver().focus();
        check("43".equals(contrast.summary),"returning focus did not refresh provider values");
        OemPicturePanel.bind(fragment,null);
        check(replacement.count()==0 && replacement.getViewTreeObserver().count()==0
                && resolver.observers.isEmpty(),"screen teardown leaked a listener");
        check(Settings.Global.writes==0,"picture readout wrote to the provider");
        System.out.println("PASS Picture panel: partial registration, rebind, reopen, focus, and teardown");
    }
}

package dev.projectorsoft.tvsettings;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Opens the correct OEM sub-page for AOSP root preference bridges.
 * Does not modify the calibration activities or their rendering.
 */
public final class OemLaunchRouteHook {
    private OemLaunchRouteHook() {}

    public static void apply(Object activityObj) {
        if (!(activityObj instanceof Activity)) return;
        Activity a=(Activity)activityObj;
        Intent intent=a.getIntent();
        if (intent==null) return;
        String action=intent.getAction();
        int page;
        String fieldName;
        if (OemRootMenuHook.actionForSource().equals(action)) {
            page=3;
            fieldName="item_source";
        } else if (OemRootMenuHook.actionForGeneral().equals(action)) {
            page=6;
            fieldName="item_general";
        } else {
            // PICTURE, SOUND, SETTINGS and other actions already handled by OEM code.
            return;
        }
        try {
            Method switchPage=null;
            for (Method m=a.getClass().getDeclaredMethods()) {
                if (!m.getName().equals("switchPage")) continue;
                Class<?>[] params=m.getParameterTypes();
                if (params.length==2 && params[0]==int.class &&
                        !params[1].isPrimitive()) {
                    switchPage=m;
                    break;
                }
            }
            if (switchPage==null) return;
            switchPage.setAccessible(true);
            switchPage.invoke(a,page,null);
            Field focus=a.getClass().getDeclaredField(fieldName);
            focus.setAccessible(true);
            Object f=focus.get(a);
            if (f instanceof View) ((View)f).requestFocus();
        } catch (Exception ex) {
            android.util.Log.w("TvSettingsPlusMOD","OEM page route unavailable",ex);
        }
    }
}

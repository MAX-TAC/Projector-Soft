package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.view.View;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * UI-only hiding of the legacy Bluetooth speaker INPUT in OEM source menus.
 * Does not touch Bluetooth HCI, pairing, A2DP sink services or remote transport.
 */
public final class OemMenuVisibilityHook {
    private OemMenuVisibilityHook() {}

    public static void hideBluetoothSpeaker(Object controller) {
        View root=findRoot(controller);
        if (root==null) return;
        Context ctx=root.getContext();
        int id=ctx.getResources().getIdentifier("tciv_source_bluetooth_speaker",
                "id","com.android.tv.settings");
        if (id==0) return;
        View item=root.findViewById(id);
        if (item==null) return;
        item.setVisibility(View.GONE);
        item.setEnabled(false);
        item.setFocusable(false);
        item.setOnClickListener(null);
    }

    private static View findRoot(Object controller) {
        if (controller==null) return null;
        Class<?> cls=controller.getClass();
        while(cls!=null) {
            for (String fieldName:new String[] {"rootView","mRootView","view"}) {
                try {
                    Field f=cls.getDeclaredField(fieldName);
                    f.setAccessible(true);
                    Object obj=f.get(controller);
                    if (obj instanceof View) return (View)obj;
                } catch (Exception ignored) {}
            }
            cls=cls.getSuperclass();
        }
        try {
            Method getView=controller.getClass().getMethod("getView");
            Object result=getView.invoke(controller);
            if (result instanceof View) return (View)result;
        } catch(Exception ignored){}
        return null;
    }
}

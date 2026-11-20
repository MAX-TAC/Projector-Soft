package dev.projectorsoft.tvsettings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.lang.reflect.Field;

/** The exact 8054 OEM General list links to its existing Android TV Settings host. */
public final class OemGeneralMenuBridge {
    private static final String TAG="TvSettingsPlusMOD";
    // Stable across OEM FloatMenu recreation so its focus_id can select this row.
    // STOCK view resources use the 0x7f package; framework resources use 0x01.
    private static final int ROW=0x7e080054;
    private static final String FLOAT_CONTROLLER="com.android.tv.settings.uicustom.floatmenu.controller.general.GeneralSubMenuController";
    private static final String FLOAT_MANAGER="com.android.tv.settings.uicustom.floatmenu.FloatMenuManager";
    static final String ALIAS="com.android.tv.settings.mod.SettingsLauncher";
    static final String ACTION="dev.projectorsoft.settings.OPEN_PROJECTOR_MENU";
    static final String TITLE="Настройки проектора (MOD)";

    private OemGeneralMenuBridge() {}

    public static void install(Object controller) {
        if(controller==null || !"8054".equals(OemHdmiPortInfo.machineId()))return;
        ProjectorMenuSections.optional("oem-general-anchor",()->{
            Class<?> type=controller.getClass();
            Field field=type.getDeclaredField("tciv_general_screensaver_settings");
            field.setAccessible(true);
            View anchor=(View)field.get(controller);
            if(anchor==null)throw new IllegalStateException("OEM screensaver row unavailable");
            Context context=anchor.getContext();
            // Keep inactivity protection independent of the UI and retain the visible
            // stock row. Its unsafe editor is replaced by read-only status on this build.
            ProjectorMenuSections.optional("oem-general-inactivity",
                    ()->OemScreenTimeoutSafetyHook.ensureNever(context));
            anchor.setOnClickListener(view->{
                boolean guarded=OemScreenTimeoutSafetyHook.allDisabled(context);
                Toast.makeText(context,guarded
                        ? "Заставка отключена политикой защиты от бездействия"
                        : "Проверка защиты от бездействия не пройдена",Toast.LENGTH_LONG).show();
            });
            boolean floating=FLOAT_CONTROLLER.equals(type.getName());
            addEntry(anchor,()->{
                if(!open(context,floating)) {
                    Toast.makeText(context,"Настройки проектора недоступны",Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    static void addEntry(View anchor,Runnable open) {
        if(!(anchor.getParent() instanceof LinearLayout))
            throw new IllegalStateException("Unsupported OEM General row layout");
        LinearLayout list=(LinearLayout)anchor.getParent();
        if(list.getOrientation()!=LinearLayout.VERTICAL)
            throw new IllegalStateException("OEM General list is not vertical");
        if(list.findViewById(ROW)!=null)return;
        Button button=new Button(anchor.getContext());
        button.setId(ROW);
        button.setText(TITLE);
        button.setAllCaps(false);
        button.setFocusable(true);
        button.setOnClickListener(view->open.run());
        list.addView(button,list.indexOfChild(anchor),
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        Log.i(TAG,"menu stage=oem-general-entry-ready");
    }

    static boolean open(Context context,boolean floating) {
        ComponentName target=new ComponentName(context.getPackageName(),ALIAS);
        if(!floating) {
            return CheckedPreferenceLaunch.open(context,()->new Intent(ACTION).setComponent(target));
        }
        try {
            if(!CheckedPreferenceLaunch.accessible(context,target))return false;
            Class<?> manager=Class.forName(FLOAT_MANAGER);
            Object current=manager.getMethod("getInstance",Context.class).invoke(null,context);
            manager.getMethod("jumpToExternalActivity",Intent.class).invoke(current,
                    new Intent(ACTION).setComponent(target));
            return true;
        } catch(Exception | LinkageError failure) {
            Log.w(TAG,"menu stage=oem-float-launch",failure);
            return false;
        }
    }
}

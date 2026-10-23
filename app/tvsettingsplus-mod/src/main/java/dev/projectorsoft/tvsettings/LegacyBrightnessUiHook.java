package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Optional compatibility hook for OEM customer 0 only.
 * Runs INSIDE the existing TvSettingsPlus right-hand brightness panel.
 * For customers !=0 the untouched MtkUtil OEM implementation remains active.
 * If the KuSourceRJ receiver/property cannot be validated, the legacy
 * fullscreen BrightActivity path remains active; never replace with dead UI.
 */
public final class LegacyBrightnessUiHook {
    private static final String PACKAGE = "com.android.tv.settings";
    private static final String ID_ECO = "rtv_general_projection_brightness_mode_eco";
    private static final String ID_STANDARD = "rtv_general_projection_brightness_mode_standard";
    private static final String ID_HIGHLIGHT = "rtv_general_projection_brightness_mode_highlight";
    private static final String ID_AUTO = "rtv_general_projection_brightness_mode_auto";
    private static final String ID_GROUP = "rjRadioGroup";
    private static final int MAX_READBACK_TRIES = 5;
    private static final long POLL_MS = 800L;
    private LegacyBrightnessUiHook() {}

    private static int rawCustomerId() {
        try {
            Field field = Build.class.getField("A_RJ_CUSTOMER_ID");
            return field.getInt(null);
        } catch (Exception e) { return 0; }
    }
    private static String firmwareId() {
        try {
            Class<?> cls=Class.forName("android.os.SystemProperties");
            Method get=cls.getMethod("get",String.class,String.class);
            return (String)get.invoke(null,"ro.rjpj.machine.id","");
        } catch (Exception e) { return ""; }
    }
    private static boolean sourceAvailable(Context ctx) {
        try {
            String id=firmwareId();
            if (!"8052".equals(id) && !"8054".equals(id)) return false;
            // Fail back to original fullscreen UI if any OEM radio widget is missing.
            for (String viewName:new String[] {ID_ECO,ID_STANDARD,ID_HIGHLIGHT,ID_AUTO,ID_GROUP}) {
                if (ctx.getResources().getIdentifier(viewName,"id",PACKAGE)==0) return false;
            }
            KuSourceRjBrightnessGateway gateway=new KuSourceRjBrightnessGateway(ctx,id);
            if (gateway.readSupportedModes().size()!=4) return false;
            return gateway.readCurrentModeId()>=0;
        } catch (Exception e) { return false; }
    }
    /** Replaces only the local Build customer-ID read in the stock brightness menu onClick. */
    public static int customerIdForMenu(Context context) {
        int id=rawCustomerId();
        if (id==0 && sourceAvailable(context)) return 1; // use existing right pane + hook
        return id; // original fullscreen fallback, or unmodified MtkUtil branch
    }

    /** Layout is original OEM right pane; no activity, full-screen UI or resources created. */
    public static void install(Object controller) {
        if (controller==null || rawCustomerId()!=0) return;
        View root=findControllerRoot(controller);
        if (root==null) return;
        Context context=root.getContext();
        if (!sourceAvailable(context)) return;
        final KuSourceRjBrightnessGateway gateway=new KuSourceRjBrightnessGateway(context,firmwareId());
        final LightSourceBrightnessController model=new LightSourceBrightnessController(
                ProjectorCapabilities.from(firmwareId(),0,
                    EnumSet.of(ProjectorCapabilities.Feature.LIGHT_SOURCE_MODE)),gateway);
        if (!model.refresh().enabled) return;
        final Map<View,Integer> modes=new HashMap<>();
        addChoice(root,ID_ECO,2,modes);
        addChoice(root,ID_STANDARD,1,modes);
        addChoice(root,ID_HIGHLIGHT,0,modes);
        View altitude=addChoice(root,ID_AUTO,3,modes);
        if (altitude!=null) changeLabel(altitude,"Высокогорный");
        if (altitude!=null) altitude.setVisibility(View.VISIBLE);
        if (modes.size()!=4) return; // OEM layout not recognized; don't intercept UI
        updateCheck(root,model.currentSnapshot().currentId,modes);

        for (Map.Entry<View,Integer> entry:modes.entrySet()) {
            final int nextMode=entry.getValue();
            final View button=entry.getKey();
            button.setOnClickListener(v-> {
                if (model.currentSnapshot().pendingId!=null) return;
                if (nextMode==0 || nextMode==3) {
                    Toast.makeText(v.getContext(),"Режим может увеличить нагрев проектора",Toast.LENGTH_LONG).show();
                }
                if (!model.select(nextMode)) {
                    Toast.makeText(v.getContext(),"Не удалось переключить режим",Toast.LENGTH_SHORT).show();
                    return;
                }
                Integer current=model.currentSnapshot().currentId;
                updateCheck(root,current,modes);
                if (model.currentSnapshot().pendingId!=null) poll(root,model,modes,0);
            });
        }
    }
    private static boolean changeLabel(View view,String value) {
        if (view instanceof TextView) {
            ((TextView)view).setText(value);
            return true;
        }
        if (view instanceof ViewGroup) {
            ViewGroup container=(ViewGroup)view;
            for (int i=0;i<container.getChildCount();i++) {
                if (changeLabel(container.getChildAt(i),value)) return true;
            }
        }
        return false;
    }
    private static void poll(View root,LightSourceBrightnessController model,
                             Map<View,Integer> modes,int attempt) {
        root.postDelayed(()-> {
            if (!root.isAttachedToWindow()) return;
            LightSourceBrightnessController.Snapshot s=model.refresh();
            if (s.pendingId==null) {
                updateCheck(root,s.currentId,modes);
            } else if (attempt<MAX_READBACK_TRIES) {
                poll(root,model,modes,attempt+1);
            } else {
                model.abandonUnconfirmedRequest();
                updateCheck(root,model.currentSnapshot().currentId,modes);
                Toast.makeText(root.getContext(),"Проектор не подтвердил переключение режима",
                        Toast.LENGTH_LONG).show();
            }
        },POLL_MS);
    }
    private static View addChoice(View root,String name,int id,Map<View,Integer> modes) {
        int viewId=root.getResources().getIdentifier(name,"id",PACKAGE);
        if (viewId==0) return null;
        View v=root.findViewById(viewId);
        if (v!=null) modes.put(v,id);
        return v;
    }
    private static void updateCheck(View root,Integer current,Map<View,Integer> modes) {
        if (current==null) return;
        int groupId=root.getResources().getIdentifier(ID_GROUP,"id",PACKAGE);
        View group=root.findViewById(groupId);
        for (Map.Entry<View,Integer> e:modes.entrySet()) {
            if (e.getValue().equals(current)) {
                try {
                    if (group!=null) {
                        Method check=group.getClass().getMethod("check",int.class);
                        check.invoke(group,e.getKey().getId());
                        return;
                    }
                } catch (Exception ignored) {}
            }
        }
    }
    private static View findControllerRoot(Object c) {
        Class<?> type=c.getClass();
        while(type!=null) {
            try {
                Field f=type.getDeclaredField("rootView");
                f.setAccessible(true);
                Object obj=f.get(c);
                return obj instanceof View ? (View)obj : null;
            } catch (Exception ignored) { type=type.getSuperclass(); }
        }
        return null;
    }
}

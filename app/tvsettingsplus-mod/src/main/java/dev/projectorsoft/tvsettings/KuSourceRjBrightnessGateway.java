package dev.projectorsoft.tvsettings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * OEM-preserving adapter for RUIOS 8052 light-source modes.
 *
 * BrightReceiver performs the original BrightUtils.setBrightModel logic:
 * persistent mode, thermal/PWM limits and timing. Never write PWM, TRD or
 * persist.bright.model from the settings app.
 *
 * IMPORTANT: This class is source-level implementation; it is NOT yet wired
 * into the signed TvSettingsPlus APK until its DEX/menu integration passes CI.
 */
public final class KuSourceRjBrightnessGateway
        implements LightSourceBrightnessController.VendorGateway {
    public static final String PACKAGE = "com.coolux.source";
    public static final String RECEIVER = "com.coolux.source.service.BrightReceiver";
    public static final String ACTION = "com.action.brightReceiver";
    public static final String EXTRA_MODE = "brightModel";
    public static final String PROPERTY_MODE = "persist.bright.model";

    private static final List<LightSourceBrightnessController.Mode> MODES_8052 =
            Collections.unmodifiableList(Arrays.asList(
                    new LightSourceBrightnessController.Mode(0, "Повышенная яркость"),
                    new LightSourceBrightnessController.Mode(1, "Стандартный"),
                    new LightSourceBrightnessController.Mode(2, "Тихий"),
                    new LightSourceBrightnessController.Mode(3, "Высокогорный")
            ));

    private final Context context;
    private final String firmwareId;

    public KuSourceRjBrightnessGateway(Context context, String firmwareId) {
        this.context = Objects.requireNonNull(context, "context").getApplicationContext();
        this.firmwareId = firmwareId;
    }

    private void requireReceiver() throws PackageManager.NameNotFoundException {
        ComponentName receiver = new ComponentName(PACKAGE, RECEIVER);
        // A receiver from another APK cannot be called without being exported.
        ActivityInfo info = context.getPackageManager().getReceiverInfo(receiver, 0);
        if (info == null || !info.enabled || !info.exported) {
            throw new IllegalStateException("Brightness receiver unavailable/not exported");
        }
    }

    @Override
    public List<LightSourceBrightnessController.Mode> readSupportedModes() throws Exception {
        // 8054 must be audited separately: do not assume 8052 brightness IDs apply.
        if (!"8052".equals(firmwareId)) return Collections.emptyList();
        requireReceiver();
        return MODES_8052;
    }

    @Override
    public int readCurrentModeId() throws Exception {
        // SystemProperties is hidden in SDK builds. Reflection may fail under
        // hidden-API policy: fail closed instead of fabricating a selected row.
        Class<?> cls=Class.forName("android.os.SystemProperties");
        Method get=cls.getDeclaredMethod("get",String.class,String.class);
        String raw=(String)get.invoke(null,PROPERTY_MODE,"");
        if (raw == null || !raw.matches("[0-3]")) {
            throw new IllegalStateException("OEM property unreadable or invalid");
        }
        return Integer.parseInt(raw);
    }

    @Override
    public void applyMode(int hardwareId) throws Exception {
        if (hardwareId < 0 || hardwareId > 3) {
            throw new IllegalArgumentException("Non-OEM brightness mode");
        }
        if (!"8052".equals(firmwareId)) throw new IllegalStateException("Unknown board");
        requireReceiver();
        Intent intent=new Intent(ACTION);
        intent.setComponent(new ComponentName(PACKAGE, RECEIVER));
        intent.putExtra(EXTRA_MODE,hardwareId);
        context.sendBroadcast(intent);
        // sendBroadcast is asynchronous. Controller refresh must verify state.
    }
}

package dev.projectorsoft.tvsettings;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.util.Log;

/**
 * Removes the OEM two-panel layout's opaque full-screen gray background while
 * retaining the dark backgrounds of the right-side preference panels.
 * Does not change global dim settings or the launcher.
 */
final class NativeOverlayWindow {
    // Exact stock res/layout/two_panel_settings_fragment.xml root:
    // com.android.tv.twopanelsettings.TwoPanelSettingsRootView id=0x7f0a04d9.
    private static final int TWO_PANEL_ROOT = 0x7f0a04d9;

    private NativeOverlayWindow() {}

    static void apply(Activity activity) {
        if (activity == null) return;
        try {
        Window window = activity.getWindow();
        if (window == null) return;
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setDimAmount(0f);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // Vendor MainSettings uses a transparent overlay surface; preserve alpha
        // instead of allowing the theme to force an opaque background.
        window.setFormat(PixelFormat.TRANSLUCENT);
        ViewGroup content = activity.findViewById(android.R.id.content);
        if (content == null) return;
        content.setBackgroundColor(Color.TRANSPARENT);
        View root = content.findViewById(TWO_PANEL_ROOT);
        if (root != null) root.setBackgroundColor(Color.TRANSPARENT);
        // The initial two-panel layout can be attached after Activity.onCreate.
        content.post(() -> {
            if (!activity.isFinishing() && !activity.isDestroyed()) {
                View panelRoot = content.findViewById(TWO_PANEL_ROOT);
                if (panelRoot != null) panelRoot.setBackgroundColor(Color.TRANSPARENT);
            }
        });
        } catch (RuntimeException | LinkageError error) {
            // Presentation failure must never crash the system Settings app.
            Log.w("TvSettingsPlusMOD", "Transparent settings overlay unavailable", error);
        }
    }
}

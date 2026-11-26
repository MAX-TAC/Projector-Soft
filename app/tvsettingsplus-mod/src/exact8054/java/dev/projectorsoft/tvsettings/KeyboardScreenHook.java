package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.provider.Settings;
import android.util.Log;

/**
 * Helpers for the current default keyboard.
 *
 * The stock keyboard screen is not used any more (it listed a duplicate
 * physical keyboard and always linked the AOSP keyboard), so this class only
 * answers "which IME is default" and "how do I open its settings".
 */
final class KeyboardScreenHook {
    private static final String TAG = "TvSettingsPlusMOD";

    private KeyboardScreenHook() { }

    /** Label of the current default IME, falling back to a neutral word. */
    static String currentKeyboardName(Context context) {
        android.view.inputmethod.InputMethodInfo info = currentKeyboardInfo(context);
        if (info == null) return "Клавиатура";
        try {
            CharSequence label = context.getPackageManager()
                    .getApplicationLabel(context.getPackageManager()
                            .getApplicationInfo(info.getPackageName(), 0));
            if (label != null && label.length() > 0) return label.toString();
        } catch (Throwable ignored) {
        }
        return "Клавиатура";
    }

    static android.view.inputmethod.InputMethodInfo currentKeyboardInfo(Context context) {
        try {
            String defaultId = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.DEFAULT_INPUT_METHOD);
            android.view.inputmethod.InputMethodManager manager =
                    (android.view.inputmethod.InputMethodManager)
                            context.getSystemService(Context.INPUT_METHOD_SERVICE);
            java.util.List<android.view.inputmethod.InputMethodInfo> all =
                    manager == null ? null : manager.getInputMethodList();
            if (all == null || defaultId == null) return null;
            for (android.view.inputmethod.InputMethodInfo candidate : all) {
                if (defaultId.equals(candidate.getId())) return candidate;
            }
        } catch (Throwable unavailable) {
            Log.w(TAG, "current keyboard lookup failed", unavailable);
        }
        return null;
    }

    /** Opens the settings activity declared by the current IME, if any. */
    static boolean openCurrentKeyboardSettings(Context context) {
        android.view.inputmethod.InputMethodInfo info = currentKeyboardInfo(context);
        if (info == null) return false;
        String activity = info.getSettingsActivity();
        if (activity == null || activity.isEmpty()) return false;
        try {
            context.startActivity(new Intent(Intent.ACTION_MAIN)
                    .setComponent(new ComponentName(info.getPackageName(), activity))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            return true;
        } catch (RuntimeException unavailable) {
            Log.w(TAG, "keyboard settings not launched", unavailable);
            return false;
        }
    }
}
package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.provider.Settings;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helpers for the default keyboard.
 *
 * The stock keyboard screen is not used (it listed a duplicate physical
 * keyboard and a fixed AOSP settings entry), so this class answers "which IME
 * is default", lists the installed ones, switches the default, and opens the
 * settings activity of the current IME.
 */
final class KeyboardScreenHook {
    private static final String TAG = "TvSettingsPlusMOD";

    private KeyboardScreenHook() { }

    /** Id of the current default IME, or an empty string. */
    static String currentKeyboardId(Context context) {
        try {
            String id = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.DEFAULT_INPUT_METHOD);
            return id == null ? "" : id;
        } catch (Throwable unavailable) {
            Log.w(TAG, "default IME unreadable", unavailable);
            return "";
        }
    }

    /** Every keyboard installed on the device. */
    static List<android.view.inputmethod.InputMethodInfo> installedKeyboards(Context context) {
        try {
            android.view.inputmethod.InputMethodManager manager =
                    (android.view.inputmethod.InputMethodManager)
                            context.getSystemService(Context.INPUT_METHOD_SERVICE);
            List<android.view.inputmethod.InputMethodInfo> all =
                    manager == null ? null : manager.getInputMethodList();
            return all == null ? Collections.<android.view.inputmethod.InputMethodInfo>emptyList()
                    : new ArrayList<>(all);
        } catch (Throwable unavailable) {
            Log.w(TAG, "keyboard list unavailable", unavailable);
            return Collections.emptyList();
        }
    }

    static android.view.inputmethod.InputMethodInfo currentKeyboardInfo(Context context) {
        String id = currentKeyboardId(context);
        for (android.view.inputmethod.InputMethodInfo candidate : installedKeyboards(context)) {
            if (candidate.getId().equals(id)) return candidate;
        }
        return null;
    }

    /** Human label of one keyboard. */
    static String labelOf(Context context, android.view.inputmethod.InputMethodInfo info) {
        if (info == null) return "Клавиатура";
        try {
            if (info.getServiceInfo() != null && info.getServiceInfo().applicationInfo != null) {
                CharSequence label = info.loadLabel(context.getPackageManager());
                if (label != null && label.length() > 0) return label.toString();
            }
            CharSequence label = context.getPackageManager()
                    .getApplicationLabel(context.getPackageManager()
                            .getApplicationInfo(info.getPackageName(), 0));
            if (label != null && label.length() > 0) return label.toString();
        } catch (Throwable ignored) {
        }
        return "Клавиатура";
    }

    /** Label of the current default keyboard. */
    static String currentKeyboardName(Context context) {
        android.view.inputmethod.InputMethodInfo info = currentKeyboardInfo(context);
        return info == null ? "Клавиатура" : labelOf(context, info);
    }

    /**
     * Makes the given IME the default one.
     *
     * Settings.Secure is written directly: setInputMethod() needs the token of
     * a live IME connection, which a settings screen does not hold.
     */
    static boolean setDefaultKeyboard(Context context, String id) {
        if (id == null || id.isEmpty()) return false;
        try {
            Settings.Secure.putString(context.getContentResolver(),
                    Settings.Secure.DEFAULT_INPUT_METHOD, id);
            String read = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.DEFAULT_INPUT_METHOD);
            if (id.equals(read)) return true;
        } catch (Throwable unavailable) {
            Log.w(TAG, "keyboard not switched", unavailable);
        }
        return false;
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
package dev.projectorsoft.tvsettings;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

/**
 * Cleans the stock keyboard screen.
 *
 * The factory KeyboardActivity lists a duplicate "Физическая клавиатура" entry
 * (the OEM trd_keyinput adds a second one) and an "Клавиатура Android (AOSP):
 * настройки" link that always points at the AOSP keyboard even when another IME
 * is the default. The owner wants the physical rows gone and the AOSP row
 * replaced by a link to the CURRENT default keyboard's own settings, so a user
 * who installs their own keyboard can configure it.
 */
final class KeyboardScreenHook {
    private static final String TAG = "TvSettingsPlusMOD";
    private static boolean installed;

    private KeyboardScreenHook() { }

    static void install(Context context) {
        if (context == null) return;
        if (!"8054".equals(OemHdmiPortInfo.machineId())) return;
        if (installed) return;
        try {
            Application app = (Application) context.getApplicationContext();
            if (app == null) return;
            app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                @Override public void onActivityCreated(Activity activity, Bundle state) { }
                @Override public void onActivityStarted(Activity activity) { }
                @Override public void onActivityResumed(Activity activity) { onScreen(activity); }
                @Override public void onActivityPaused(Activity activity) { }
                @Override public void onActivityStopped(Activity activity) { }
                @Override public void onActivitySaveInstanceState(Activity activity, Bundle state) { }
                @Override public void onActivityDestroyed(Activity activity) { }
            });
            installed = true;
        } catch (Throwable unavailable) {
            Log.w(TAG, "keyboard screen hook not installed", unavailable);
        }
    }

    private static void onScreen(Activity activity) {
        if (!(activity instanceof FragmentActivity)) return;
        if (!activity.getClass().getName().endsWith("inputmethod.KeyboardActivity")) return;
        try {
            FragmentManager manager = ((FragmentActivity) activity).getSupportFragmentManager();
            manager.registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
                @Override public void onFragmentResumed(FragmentManager fm, Fragment fragment) {
                    if (fragment instanceof PreferenceFragmentCompat) {
                        rewrite((PreferenceFragmentCompat) fragment);
                    }
                }
            }, false);
        } catch (Throwable unavailable) {
            Log.w(TAG, "keyboard fragment hook failed", unavailable);
        }
    }

    /** Removes the physical rows and repoints the AOSP row at the current IME. */
    private static void rewrite(PreferenceFragmentCompat fragment) {
        PreferenceScreen screen;
        try {
            screen = fragment.getPreferenceScreen();
        } catch (RuntimeException unavailable) {
            return;
        }
        if (screen == null) return;
        Context context = fragment.requireContext();
        String name = currentKeyboardName(context);
        for (int index = screen.getPreferenceCount() - 1; index >= 0; index--) {
            Preference row = screen.getPreference(index);
            if (row == null) continue;
            CharSequence title = row.getTitle();
            String text = title == null ? "" : title.toString();
            if (text.contains("Физическая") || text.contains("физическая")) {
                screen.removePreference(row);
                continue;
            }
            if (text.contains("AOSP")) {
                row.setTitle("Настройки: " + name);
                row.setSummary(null);
                row.setOnPreferenceClickListener(item -> {
                    if (!openCurrentKeyboardSettings(context)) {
                        android.widget.Toast.makeText(context,
                                "Настройки этой клавиатуры недоступны",
                                android.widget.Toast.LENGTH_SHORT).show();
                    }
                    return true;
                });
            }
        }
    }

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

    private static android.view.inputmethod.InputMethodInfo currentKeyboardInfo(Context context) {
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
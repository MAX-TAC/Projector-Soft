package dev.projectorsoft.tvsettings;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Add one read-only live CPU row to the *original* Android TV AboutFragment.
 * Does not replace AboutFragment, its controllers or native device-name editor.
 * App lifecycle coverage also catches the original separate AboutActivity.
 */
final class StockAboutTemperatureBridge {
    static final String KEY = "projectorsoft_stock_about_cpu_temperature";
    private static final String OEM_ABOUT = "com.android.tv.settings.about.AboutFragment";
    private static final String TAG = "TvSettingsPlusMOD";
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final Set<FragmentActivity> OBSERVED =
            Collections.newSetFromMap(new WeakHashMap<FragmentActivity, Boolean>());
    private static boolean registered;
    private static WeakReference<PreferenceFragmentCompat> current = new WeakReference<>(null);

    private static final Runnable POLL = new Runnable() {
        @Override public void run() {
            PreferenceFragmentCompat page = current.get();
            if (page == null || !page.isResumed()) return;
            PreferenceScreen screen = page.getPreferenceScreen();
            if (screen == null) return;
            Preference cpu = screen.findPreference(KEY);
            if (cpu == null) return;
            cpu.setSummary(OemCpuTemperature.read(page.requireContext()));
            MAIN.postDelayed(this, 2000);
        }
    };

    private StockAboutTemperatureBridge() {}

    static synchronized void install(Activity activity) {
        if (activity == null || !"8054".equals(OemHdmiPortInfo.machineId())) return;
        if (!registered) {
            activity.getApplication().registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                @Override public void onActivityCreated(Activity a, Bundle state) {
                    observe(a);
                }
                @Override public void onActivityStarted(Activity a) { }
                @Override public void onActivityResumed(Activity a) { observe(a); }
                @Override public void onActivityPaused(Activity a) { }
                @Override public void onActivityStopped(Activity a) { }
                @Override public void onActivitySaveInstanceState(Activity a, Bundle state) { }
                @Override public void onActivityDestroyed(Activity a) {
                    if (a instanceof FragmentActivity) OBSERVED.remove((FragmentActivity) a);
                }
            });
            registered = true;
        }
        observe(activity);
    }

    private static void observe(Activity activity) {
        if (!(activity instanceof FragmentActivity)) return;
        FragmentActivity host = (FragmentActivity) activity;
        if (!OBSERVED.add(host)) return;
        host.getSupportFragmentManager().registerFragmentLifecycleCallbacks(
                new FragmentManager.FragmentLifecycleCallbacks() {
                    @Override public void onFragmentResumed(FragmentManager manager, Fragment f) {
                        if (OEM_ABOUT.equals(f.getClass().getName())
                                && f instanceof PreferenceFragmentCompat)
                            bind((PreferenceFragmentCompat) f);
                    }
                    @Override public void onFragmentPaused(FragmentManager manager, Fragment f) {
                        if (current.get() == f) {
                            MAIN.removeCallbacks(POLL);
                            current.clear();
                        }
                    }
                    @Override public void onFragmentDestroyed(FragmentManager manager, Fragment f) {
                        if (current.get() == f) {
                            MAIN.removeCallbacks(POLL);
                            current.clear();
                        }
                    }
                }, true);
        for (Fragment f : host.getSupportFragmentManager().getFragments())
            if (OEM_ABOUT.equals(f.getClass().getName())
                    && f instanceof PreferenceFragmentCompat && f.isResumed())
                bind((PreferenceFragmentCompat) f);
    }

    private static Preference thermalRow(PreferenceFragmentCompat fragment, String key,
                                         String title, int order) {
        Preference row = new Preference(fragment.requireContext());
        row.setKey(key);
        row.setTitle(title);
        row.setSelectable(false);
        row.setOrder(order);
        return row;
    }

    private static void bind(PreferenceFragmentCompat fragment) {
        try {
            PreferenceScreen screen = fragment.getPreferenceScreen();
            if (screen == null) return;
            Preference cpu = screen.findPreference(KEY);
            if (cpu == null) {
                screen.addPreference(thermalRow(fragment, KEY, "Температура CPU", Integer.MAX_VALUE - 2));
            }
            MAIN.removeCallbacks(POLL);
            current = new WeakReference<>(fragment);
            MAIN.post(POLL);
        } catch (RuntimeException error) {
            Log.w(TAG, "Stock About CPU insertion unavailable", error);
        }
    }
}

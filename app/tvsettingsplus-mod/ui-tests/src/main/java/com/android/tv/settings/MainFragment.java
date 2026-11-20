package com.android.tv.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import dev.projectorsoft.tvsettings.StockMainSettingsMenu;

/** Fixture for stock-created Preferences and the verified hook before the stock super call. */
public final class MainFragment extends SettingsPreferenceFragment {
    @Override public int getPageId() {
        return 0;
    }

    @Override public void onCreatePreferences(Bundle state, String key) {
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(requireContext());
        setPreferenceScreen(screen);
        PreferenceCategory general = new PreferenceCategory(requireContext());
        general.setTitle("Общие настройки");
        general.setKey("general_settings");
        screen.addPreference(general);
        for (String name : new String[]{"network", "applications", "device_pref", "channel",
                "remotes_and_accessories", "display_and_sound", "retail", "oled"}) {
            Preference row = new Preference(requireContext());
            row.setKey(name);
            general.addPreference(row);
        }
        PreferenceCategory quick = new PreferenceCategory(requireContext());
        quick.setKey("quick_settings");
        screen.addPreference(quick);
        Preference bug = new Preference(requireContext());
        bug.setKey("take_bug_report");
        quick.addPreference(bug);
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        StockMainSettingsMenu.install(this);
        return super.onCreateView(inflater, container, state);
    }
}

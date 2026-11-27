package com.android.tv.settings.partnercustomizer.inputs;

import android.os.Bundle;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import com.android.tv.settings.SettingsPreferenceFragment;

/** Provides the stock fragment shape for navigation tests. */
public final class InputsFragment extends SettingsPreferenceFragment {
    @Override public int getPageId() { return 0; }

    @Override public void onCreatePreferences(Bundle state, String rootKey) {
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(requireContext());
        screen.setTitle("Входы");
        for (String key : new String[]{"hdmi_control", "tv_auto_on", "device_auto_off"}) {
            SwitchPreference toggle = new SwitchPreference(requireContext());
            toggle.setKey(key);
            screen.addPreference(toggle);
        }
        setPreferenceScreen(screen);
    }
}

package com.android.tv.settings.accessories;

import android.os.Bundle;
import com.android.tv.settings.SettingsPreferenceFragment;

/** Test contract for the original 8054 accessory management fragment. */
public final class AccessoriesFragment extends SettingsPreferenceFragment {
    @Override public int getPageId() { return 0; }
    @Override public void onCreatePreferences(Bundle state, String key) {
        setPreferenceScreen(getPreferenceManager().createPreferenceScreen(requireContext()));
    }
}

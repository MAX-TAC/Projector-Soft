package com.android.tv.settings.device.apps;

import android.os.Bundle;
import androidx.preference.PreferenceScreen;
import com.android.tv.settings.SettingsPreferenceFragment;

/** Test double for the existing stock AppsFragment. */
public final class AppsFragment extends SettingsPreferenceFragment {
    @Override public int getPageId(){return 0;}
    @Override public void onCreatePreferences(Bundle state,String root){
        PreferenceScreen screen=getPreferenceManager().createPreferenceScreen(requireContext());
        screen.setTitle("Приложения");
        setPreferenceScreen(screen);
    }
}

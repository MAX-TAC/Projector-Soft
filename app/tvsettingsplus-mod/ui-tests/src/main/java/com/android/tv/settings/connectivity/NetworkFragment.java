package com.android.tv.settings.connectivity;

import android.os.Bundle;
import androidx.preference.PreferenceScreen;
import com.android.tv.settings.SettingsPreferenceFragment;

/** Test double for the existing OEM screen. AndroidX still renders its tree. */
public final class NetworkFragment extends SettingsPreferenceFragment {
    @Override public int getPageId(){return 0;}
    @Override public void onCreatePreferences(Bundle state,String root){
        PreferenceScreen screen=getPreferenceManager().createPreferenceScreen(requireContext());
        screen.setTitle("Wi-Fi и сеть");
        setPreferenceScreen(screen);
    }
}

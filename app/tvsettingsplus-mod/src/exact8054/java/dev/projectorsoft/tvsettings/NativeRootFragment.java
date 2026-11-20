package dev.projectorsoft.tvsettings;

import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.tv.settings.SettingsPreferenceFragment;

public final class NativeRootFragment extends SettingsPreferenceFragment {
    @Override public int getPageId(){return 0;}

    @Override public void onCreatePreferences(Bundle state,String rootKey) {
        PreferenceScreen screen=getPreferenceManager().createPreferenceScreen(requireContext());
        screen.setTitle("Настройки");
        setPreferenceScreen(screen);

        for(NativePage page:NativePage.values()){
            Preference row=new Preference(requireContext());
            row.setKey(page.name());
            row.setTitle(page.title);
            row.setFragment(page.fragment);
            // The stock container previews and navigates to this fragment.
            // The section's separate Activity remains available for explicit entry.
            screen.addPreference(row);
        }
    }
}

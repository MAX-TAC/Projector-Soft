package com.android.tv.settings;

import androidx.fragment.app.Fragment;
import com.android.tv.settings.overlay.FlavorUtils;

/** JVM fixture for the verified stock Activity/provider ABI. Excluded from the APK. */
public final class MainSettings extends TvSettingsActivity {
    @Override protected Fragment createSettingsFragment() {
        return FlavorUtils.getFeatureFactory(this).getSettingsFragmentProvider()
                .newSettingsFragment(MainFragment.class.getName(), null);
    }

    @Override protected boolean isStartupVerificationRequired() {
        return true;
    }
}

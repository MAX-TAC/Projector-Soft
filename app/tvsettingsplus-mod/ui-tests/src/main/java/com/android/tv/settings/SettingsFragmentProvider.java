package com.android.tv.settings;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

public interface SettingsFragmentProvider {
    Fragment newSettingsFragment(String name,Bundle arguments);
}

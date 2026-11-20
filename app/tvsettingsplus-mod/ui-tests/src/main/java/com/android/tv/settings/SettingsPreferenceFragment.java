package com.android.tv.settings;

import androidx.leanback.preference.LeanbackPreferenceFragmentCompat;

/** Compile-time contract. The signed APK resolves this class from stock classes13.dex. */
public abstract class SettingsPreferenceFragment extends LeanbackPreferenceFragmentCompat {
    public abstract int getPageId();
}

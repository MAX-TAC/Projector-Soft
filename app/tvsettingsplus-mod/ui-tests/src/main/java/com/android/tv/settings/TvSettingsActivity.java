package com.android.tv.settings;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

/** Build-time API shape and test host. Excluded from production D8 inputs. */
public abstract class TvSettingsActivity extends FragmentActivity {
    protected abstract Fragment createSettingsFragment();
    protected boolean isStartupVerificationRequired(){return false;}

    @Override protected void onCreate(Bundle state){
        setTheme(androidx.leanback.R.style.Theme_Leanback);
        super.onCreate(state);
        if(state==null)getSupportFragmentManager().beginTransaction()
            .replace(android.R.id.content,createSettingsFragment()).commitNow();
    }
}

package dev.projectorsoft.tvsettings;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import androidx.fragment.app.Fragment;
import com.android.tv.settings.TvSettingsActivity;
import com.android.tv.settings.overlay.FlavorUtils;

/** Enters the original 8054 SettingsFragmentProvider with an internally selected page. */
public abstract class ModSettingsActivity extends TvSettingsActivity {
    abstract NativePage page();

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        NativeOverlayWindow.apply(this);
        // The stock TvSettingsActivity already slides the right-hand pane.
        // Prevent a second inconsistent diagonal Activity transition.
        overridePendingTransition(0, 0);
    }

    @Override protected void onResume() {
        super.onResume();
        NativeOverlayWindow.apply(this);
    }

    @Override protected Fragment createSettingsFragment() {
        NativePage current=page();
        String name=current==null?NativeRootFragment.class.getName():current.fragment;
        return FlavorUtils.getFeatureFactory(this).getSettingsFragmentProvider()
                .newSettingsFragment(name,new Bundle());
    }

    @Override protected boolean isStartupVerificationRequired() {
        // Preserve the same startup verification as original MainSettings.
        return true;
    }

    @Override public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override public boolean onKeyUp(int code,KeyEvent event) {
        if(code==KeyEvent.KEYCODE_SETTINGS) {
            if(page()!=null)startActivity(new Intent().setClassName(this,StockMainSettingsMenu.STOCK_MAIN)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP));
            return true;
        }
        if(code==KeyEvent.KEYCODE_TV_INPUT) {
            if(page()!=NativePage.SOURCES)startActivity(new Intent(this,SourcesSettingsActivity.class));
            return true;
        }
        return super.onKeyUp(code,event);
    }
}

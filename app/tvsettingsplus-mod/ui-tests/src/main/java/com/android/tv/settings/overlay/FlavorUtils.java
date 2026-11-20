package com.android.tv.settings.overlay;

import android.content.Context;
import androidx.fragment.app.Fragment;

/** Test fixture for the stock provider. Actual OEM panel transitions run only on the target APK. */
public final class FlavorUtils {
    private FlavorUtils(){}
    public static FeatureFactory getFeatureFactory(Context context) {
        return ()->(name,args)->Fragment.instantiate(context,name,args);
    }
}

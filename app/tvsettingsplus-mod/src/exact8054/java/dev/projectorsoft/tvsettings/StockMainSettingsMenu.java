package dev.projectorsoft.tvsettings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;

/** Contributes the projector links to the original MainFragment's PreferenceScreen. */
public final class StockMainSettingsMenu {
    static final String CATEGORY = "projectorsoft_general_settings";
    static final String STOCK_MAIN = "com.android.tv.settings.MainSettings";
    private static final String PACKAGE = "com.android.tv.settings";
    private static final String TAG = "TvSettingsPlusMOD";

    private StockMainSettingsMenu() {}

    public static void install(PreferenceFragmentCompat fragment) {
        PreferenceScreen screen = fragment.getPreferenceScreen();
        if (screen == null) return;
        PreferenceCategory added = null;
        try {
            Preference header = screen.findPreference("general_settings");
            if (!(header instanceof PreferenceCategory)) return;
            if (screen.findPreference(CATEGORY) != null) {
                hideOriginal(screen);
                return;
            }
            Context context = screen.getContext();
            added = new PreferenceCategory(context);
            added.setKey(CATEGORY);
            added.setTitle(header.getTitle());
            added.setOrder(header.getOrder());
            if (!screen.addPreference(added)) return;
            for (NativePage page : NativePage.ROOT) {
                if (page == NativePage.APPS) {
                    Preference apps = screen.findPreference("applications");
                    if (apps != null && !apps.isVisible()) continue;
                }
                Preference row = new Preference(context);
                row.setKey("projectorsoft_root_" + page.name());
                row.setTitle(page.title);
                row.setOrder(page.ordinal());
                row.setFragment(page.fragment);
                row.setIcon(icon(context, screen, page));
                CheckedPreferenceLaunch.Route route = () -> new Intent().setComponent(target(context, page));
                row.setOnPreferenceClickListener(item -> {
                    if (!CheckedPreferenceLaunch.open(context, route))
                        Toast.makeText(context, "Экран недоступен", Toast.LENGTH_SHORT).show();
                    return true;
                });
                added.addPreference(row);
            }
            screen.setTitle("Настройки Android TV");
            hideOriginal(screen);
        } catch (RuntimeException | LinkageError failure) {
            if (added != null) screen.removePreference(added);
            Log.e(TAG, "Unable to install the projector tree in stock MainFragment", failure);
        }
    }

    private static void hideOriginal(PreferenceScreen root) {
        for (int i = 0; i < root.getPreferenceCount(); i++) {
            Preference row = root.getPreference(i);
            if (!CATEGORY.equals(row.getKey())) row.setVisible(false);
        }
    }

    public static boolean addAuxiliary(PreferenceGroup parent, Preference child) {
        if (parent.findPreference(CATEGORY) != null) child.setVisible(false);
        return parent.addPreference(child);
    }

    static ComponentName target(Context context, NativePage page) {
        if (page == NativePage.NETWORK)
            return new ComponentName(PACKAGE, PACKAGE + ".connectivity.NetworkActivity");
        if (page == NativePage.APPS)
            return new ComponentName(PACKAGE, PACKAGE + ".device.apps.AppsActivity");
        return new ComponentName(context, page.activity);
    }

    private static Drawable icon(Context context, PreferenceScreen screen, NativePage page) {
        switch (page) {
            case NETWORK: return stockIcon(screen, "network");
            case APPS: return stockIcon(screen, "applications");
            case SYSTEM: return stockIcon(screen, "device_pref");
            case BLUETOOTH:
                int icon = context.getResources().getIdentifier("ic_bluetooth", "drawable", PACKAGE);
                return icon == 0 ? stockIcon(screen, "remotes_and_accessories") : context.getDrawable(icon);
            default: return new MenuIcon(context, page);
        }
    }

    private static Drawable stockIcon(PreferenceScreen screen, String key) {
        Preference row = screen.findPreference(key);
        return row == null ? null : row.getIcon();
    }

    /** Scalable outline glyphs in the stock preference icon slot. */
    private static final class MenuIcon extends Drawable {
        private final NativePage page;
        private final int size;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();

        MenuIcon(Context context, NativePage page) {
            this.page = page;
            size = Math.round(24 * context.getResources().getDisplayMetrics().density);
            android.content.res.TypedArray a = context.obtainStyledAttributes(
                    new int[]{android.R.attr.colorControlNormal});
            paint.setColor(a.getColor(0, android.graphics.Color.WHITE));
            a.recycle();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.8f);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
        }

        @Override public void draw(Canvas canvas) {
            int state = canvas.save();
            canvas.translate(getBounds().left, getBounds().top);
            canvas.scale(getBounds().width() / 24f, getBounds().height() / 24f);
            switch (page) {
                case SOURCES:
                    canvas.drawRoundRect(6, 5, 22, 19, 2, 2, paint);
                    line(canvas, 2, 12, 16, 12);
                    line(canvas, 12, 8, 16, 12, 12, 16);
                    break;
                case PROJECTION:
                    line(canvas, 3, 5, 21, 7, 19, 18, 5, 20, 3, 5);
                    canvas.drawCircle(12, 12, 2, paint);
                    break;
                case PICTURE:
                    canvas.drawRoundRect(3, 4, 21, 20, 2, 2, paint);
                    line(canvas, 4, 17, 9, 12, 13, 16, 17, 12, 20, 16);
                    canvas.drawCircle(15, 9, 1, paint);
                    break;
                case SOUND:
                    line(canvas, 3, 9, 7, 9, 12, 5, 12, 19, 7, 15, 3, 15, 3, 9);
                    canvas.drawArc(10, 5, 22, 19, -47, 94, false, paint);
                    break;
                default:
                    break;
            }
            canvas.restoreToCount(state);
        }

        private void line(Canvas canvas, int... points) {
            path.reset();
            path.moveTo(points[0], points[1]);
            for (int i = 2; i < points.length; i += 2) path.lineTo(points[i], points[i + 1]);
            canvas.drawPath(path, paint);
        }

        @Override public int getIntrinsicWidth() { return size; }
        @Override public int getIntrinsicHeight() { return size; }
        @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); }
        @Override public void setColorFilter(ColorFilter filter) { paint.setColorFilter(filter); }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }
}

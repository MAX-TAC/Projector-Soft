package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.Equalizer;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceGroup;
import androidx.preference.SeekBarPreference;

/** Keeps the saved preset and the confirmed Android output-mix state separate. */
final class BtEqualizerPanel {
    private static final String TAG = "TvSettingsPlusMOD";
    static final String KEY_PRESET = "projectorsoft_eq_preset";
    static final String KEY_BAND = "projectorsoft_eq_band_";
    private static final String[] NAMES = {
        "Выключен", "Ровный", "Бас", "Высокие", "Голос", "Пользовательский"
    };
    static final int OFF = 0;
    static final int CUSTOM = NAMES.length - 1;
    static final int BANDS = 5;
    private static final int[][] CURVES = {
        {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {1200, 600, 0, 0, 0},
        {0, 0, 0, 600, 1200}, {-300, 0, 900, 900, 300}
    };

    interface Effect {
        int bandCount();
        int min();
        int max();
        int frequency(int band);
        boolean controlled();
        boolean enabled();
        boolean enable();
        void setLevel(int band, int level);
        int level(int band);
        void release();
    }

    interface Factory { Effect open(); }

    static final class Session {
        private final Factory factory;
        private Effect effect;
        private int applied = OFF;
        private int[] levels;

        Session(Factory factory) { this.factory = factory; }

        boolean apply(int preset, int[] requested) {
            if (preset == OFF) return close();
            if (preset < OFF || preset > CUSTOM || requested == null || requested.length != BANDS)
                return false;
            try {
                if (effect == null) effect = factory.open();
                if (effect == null || !effect.controlled() || effect.bandCount() != BANDS
                        || effect.min() >= effect.max()) {
                    close();
                    return false;
                }
                int[] wanted = requested.clone();
                for (int i = 0; i < BANDS; i++)
                    wanted[i] = Math.max(effect.min(), Math.min(effect.max(), wanted[i]));
                if (matches(preset, wanted)) return true;
                for (int i = 0; i < BANDS; i++) effect.setLevel(i, wanted[i]);
                if (!effect.enable()) {
                    close();
                    return false;
                }
                applied = preset;
                levels = wanted;
                if (matches(preset, wanted)) return true;
            } catch (RuntimeException | LinkageError unavailable) {
                Log.w(TAG, "Output-mix equalizer unavailable", unavailable);
            }
            close();
            return false;
        }

        private boolean matches(int preset, int[] wanted) {
            if (applied != preset || !effect.controlled() || !effect.enabled()) return false;
            for (int i = 0; i < BANDS; i++)
                if (effect.level(i) != wanted[i]) return false;
            return true;
        }

        boolean active(int preset) {
            try { return effect != null && levels != null && matches(preset, levels); }
            catch (RuntimeException | LinkageError unavailable) { return false; }
        }

        private boolean close() {
            Effect previous = effect;
            effect = null;
            levels = null;
            applied = OFF;
            if (previous != null) {
                try { previous.release(); }
                catch (RuntimeException | LinkageError unavailable) {
                    Log.w(TAG, "Equalizer release failed", unavailable);
                    return false;
                }
            }
            return true;
        }
    }

    private static final class OutputMix implements Effect {
        private final Equalizer equalizer = new Equalizer(0, 0);
        private boolean enabledHere;

        @Override public int bandCount() { return equalizer.getNumberOfBands(); }
        @Override public int min() { return equalizer.getBandLevelRange()[0]; }
        @Override public int max() { return equalizer.getBandLevelRange()[1]; }
        @Override public int frequency(int band) { return equalizer.getCenterFreq((short) band); }
        @Override public boolean controlled() { return equalizer.hasControl(); }
        @Override public boolean enabled() { return equalizer.getEnabled(); }
        @Override public boolean enable() {
            enabledHere = equalizer.setEnabled(true) == AudioEffect.SUCCESS;
            return enabledHere;
        }
        @Override public void setLevel(int band, int level) { equalizer.setBandLevel((short) band, (short) level); }
        @Override public int level(int band) { return equalizer.getBandLevel((short) band); }
        @Override public void release() {
            try { if (enabledHere && equalizer.hasControl()) equalizer.setEnabled(false); }
            finally { equalizer.release(); }
        }
    }

    private static final Session SESSION = new Session(OutputMix::new);
    private final Context context;
    private final SeekBarPreference[] rows = new SeekBarPreference[BANDS];
    private ListPreference presetRow;

    BtEqualizerPanel(Context context) { this.context = context; }

    static void restore(Context context) {
        if (!"8054".equals(OemHdmiPortInfo.machineId())) return;
        restoreSaved(context, SESSION);
    }

    static boolean restoreSaved(Context context, Session session) {
        int saved = storedPreset(context);
        return session.apply(saved, curve(context, saved));
    }

    void start() { restore(context); }

    void install(PreferenceGroup parent) {
        presetRow = new ListPreference(context);
        presetRow.setKey(KEY_PRESET);
        presetRow.setTitle("Пресет");
        presetRow.setOrder(100);
        presetRow.setEntries(NAMES);
        String[] values = new String[NAMES.length];
        for (int i = 0; i < values.length; i++) values[i] = String.valueOf(i);
        presetRow.setEntryValues(values);
        presetRow.setPersistent(false);
        presetRow.setOnPreferenceChangeListener((pref, value) -> changePreset(value));
        parent.addPreference(presetRow);

        for (int i = 0; i < rows.length; i++) {
            final int band = i;
            SeekBarPreference row = new SeekBarPreference(context);
            row.setKey(KEY_BAND + band);
            row.setOrder(110 + band);
            row.setPersistent(false);
            row.setSeekBarIncrement(100);
            row.setUpdatesContinuously(false);
            row.setOnPreferenceChangeListener((pref, value) -> changeBand(band, value));
            rows[i] = row;
            parent.addPreference(row);
        }
        refresh();
    }

    private boolean changePreset(Object value) {
        int requested;
        try { requested = Integer.parseInt(String.valueOf(value)); }
        catch (NumberFormatException invalid) { return false; }
        if (requested < OFF || requested > CUSTOM || !"8054".equals(OemHdmiPortInfo.machineId()))
            return false;
        commit(KEY_PRESET, requested, requested, curve(context, requested));
        return false;
    }

    private boolean changeBand(int band, Object value) {
        if (!(value instanceof Integer) || storedPreset(context) != CUSTOM || !SESSION.active(CUSTOM))
            return false;
        int desired = (Integer) value;
        try {
            if (desired < SESSION.effect.min() || desired > SESSION.effect.max()) return false;
        } catch (RuntimeException | LinkageError unavailable) {
            refresh();
            return false;
        }
        int[] curve = curve(context, CUSTOM);
        curve[band] = desired;
        commit(KEY_BAND + band, desired, CUSTOM, curve);
        return false;
    }

    private void commit(String key, int value, int preset, int[] requested) {
        boolean applied = SESSION.apply(preset, requested);
        boolean saved = applied && write(key, value);
        if (!saved) {
            if (applied) restore(context);
            Toast.makeText(context, "Применение эквалайзера не подтверждено", Toast.LENGTH_SHORT).show();
        }
        refresh();
    }

    private boolean write(String key, int value) {
        try {
            return Settings.Global.putInt(context.getContentResolver(), key, value)
                    && Settings.Global.getInt(context.getContentResolver(), key, Integer.MIN_VALUE) == value;
        } catch (RuntimeException unavailable) {
            Log.w(TAG, "Cannot save equalizer settings", unavailable);
            return false;
        }
    }

    void refresh() {
        int saved = storedPreset(context);
        boolean active = SESSION.active(saved);
        presetRow.setValue(String.valueOf(saved));
        presetRow.setSummary(NAMES[saved] + (saved != OFF && !active ? " · применение не подтверждено" : ""));
        presetRow.setEnabled("8054".equals(OemHdmiPortInfo.machineId()));
        for (int i = 0; i < rows.length; i++) {
            SeekBarPreference row = rows[i];
            row.setEnabled(false);
            row.setVisible(active);
            if (!active) continue;
            try {
                row.setTitle(String.format(java.util.Locale.getDefault(), "%,d Гц",
                        SESSION.effect.frequency(i) / 1000));
                row.setMin(SESSION.effect.min());
                row.setMax(SESSION.effect.max());
                row.setValue(SESSION.levels[i]);
                row.setSummary(String.format(java.util.Locale.getDefault(), "%+.1f дБ",
                        SESSION.levels[i] / 100f));
                row.setEnabled(saved == CUSTOM);
            } catch (RuntimeException | LinkageError unavailable) {
                row.setVisible(false);
            }
        }
    }

    private static int storedPreset(Context context) {
        int value = read(context, KEY_PRESET, OFF);
        return value < OFF || value > CUSTOM ? OFF : value;
    }

    private static int[] curve(Context context, int preset) {
        if (preset != CUSTOM) return CURVES[preset];
        int[] values = new int[BANDS];
        for (int i = 0; i < BANDS; i++) values[i] = read(context, KEY_BAND + i, 0);
        return values;
    }

    private static int read(Context context, String key, int fallback) {
        try { return Settings.Global.getInt(context.getContentResolver(), key, fallback); }
        catch (RuntimeException unreadable) { return fallback; }
    }
}

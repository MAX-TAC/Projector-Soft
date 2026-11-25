package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.audiofx.Equalizer;
import android.provider.Settings;
import android.util.Log;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceGroup;
import androidx.preference.SeekBarPreference;

/**
 * AudioFlinger-level equalizer that works on every output, including Bluetooth.
 *
 * Why this exists: the OEM five-band panel wrote g_fusion_sound__* keys that
 * configure the MediaTek DSP only; over Bluetooth nothing changed because the
 * BT stream leaves AudioFlinger before that DSP. android.media.audiofx.Equalizer
 * binds to an AudioFlinger session instead, so it applies to whatever output is
 * active. Verified audible on the 8054 with a BT soundbar.
 *
 * UI shape requested by the owner: no on/off switch. One preset list with
 * "Выключен" first plus five presets, and five band sliders that are editable
 * only while "Пользовательский" is selected. In every other preset the sliders
 * display that preset's curve read-only.
 */
final class BtEqualizerPanel {
    private static final String TAG = "TvSettingsPlusMOD";

    static final String KEY_PRESET = "projectorsoft_eq_preset";
    static final String KEY_BAND = "projectorsoft_eq_band_";
    static final String[] BAND_LABELS = {"60 Гц", "230 Гц", "910 Гц", "3,6 кГц", "14 кГц"};

    // Preset order matters: index 0 is "off", the last is the editable one.
    private static final String[] PRESET_NAMES = {
            "Выключен", "Ровный", "Бас", "Высокие", "Голос", "Пользовательский"
    };
    private static final short[][] PRESET_BANDS = {
            {0, 0, 0, 0, 0},                 // Выключен (also silences the effect)
            {0, 0, 0, 0, 0},                 // Ровный
            {1200, 600, 0, 0, 0},            // Бас
            {0, 0, 0, 600, 1200},            // Высокие
            {-300, 0, 900, 900, 300},        // Голос
            null                             // Пользовательский — from stored bands
    };
    private static final int CUSTOM = PRESET_NAMES.length - 1;
    private static final int OFF = 0;

    private final Context context;
    private AudioTrack track;
    private Equalizer equalizer;
    private ListPreference presetRow;
    private final SeekBarPreference[] bandRows = new SeekBarPreference[BAND_LABELS.length];

    BtEqualizerPanel(Context context) {
        this.context = context;
    }

    /** Builds the preset list and the five band sliders. */
    void install(PreferenceGroup parent) {
        if (parent == null) return;

        presetRow = new ListPreference(context);
        presetRow.setKey(KEY_PRESET);
        presetRow.setTitle("Пресет");
        presetRow.setOrder(100);
        presetRow.setEntries(PRESET_NAMES);
        String[] values = new String[PRESET_NAMES.length];
        for (int i = 0; i < values.length; i++) values[i] = String.valueOf(i);
        presetRow.setEntryValues(values);
        presetRow.setPersistent(false);
        int current = storedPreset();
        presetRow.setValue(String.valueOf(current));
        presetRow.setSummary(PRESET_NAMES[current]);
        presetRow.setOnPreferenceChangeListener((pref, value) -> {
            int index;
            try {
                index = Integer.parseInt(String.valueOf(value));
            } catch (NumberFormatException bad) {
                return false;
            }
            Settings.Global.putInt(context.getContentResolver(), KEY_PRESET, index);
            ((ListPreference) pref).setValue(String.valueOf(index));
            ((ListPreference) pref).setSummary(PRESET_NAMES[index]);
            applyPreset(index);
            syncRows(index);
            return false;
        });
        parent.addPreference(presetRow);

        int defaultLevel = 0;
        for (int band = 0; band < BAND_LABELS.length; band++) {
            SeekBarPreference row = new SeekBarPreference(context);
            row.setKey(KEY_BAND + band);
            row.setTitle(BAND_LABELS[band]);
            row.setOrder(110 + band);
            row.setPersistent(false);
            row.setMin(-1500);
            row.setMax(1500);
            row.setShowSeekBarValue(true);
            row.setValue(storedBand(band, defaultLevel));
            row.setEnabled(current == CUSTOM);
            final int index = band;
            row.setOnPreferenceChangeListener((pref, value) -> {
                if (!(value instanceof Integer)) return false;
                int level = (Integer) value;
                Settings.Global.putInt(context.getContentResolver(), KEY_BAND + index, level);
                // Moving a slider only makes sense in the custom preset.
                Settings.Global.putInt(context.getContentResolver(), KEY_PRESET, CUSTOM);
                if (presetRow != null) {
                    presetRow.setValue(String.valueOf(CUSTOM));
                    presetRow.setSummary(PRESET_NAMES[CUSTOM]);
                }
                applyCustom();
                return false;
            });
            bandRows[band] = row;
            parent.addPreference(row);
        }

        // Reflect the saved preset on the sliders at first paint.
        syncRows(current);
    }

    /** Called from the page onStart: keep a silent session only when not off. */
    void start() {
        if (storedPreset() != OFF) ensureSession();
    }

    /** Called from onStop: never keep an audio track alive in the background. */
    void stop() {
        if (equalizer != null) {
            try { equalizer.setEnabled(false); } catch (Throwable ignored) { }
            try { equalizer.release(); } catch (Throwable ignored) { }
            equalizer = null;
        }
        if (track != null) {
            try { track.stop(); } catch (Throwable ignored) { }
            try { track.release(); } catch (Throwable ignored) { }
            track = null;
        }
    }

    /** Re-reads stored state so the page never shows a stale preset. */
    void refresh(PreferenceGroup parent) {
        if (parent == null) return;
        int current = storedPreset();
        ListPreference list = parent.findPreference(KEY_PRESET);
        if (list != null) {
            list.setValue(String.valueOf(current));
            list.setSummary(PRESET_NAMES[current]);
        }
        syncRows(current);
    }

    // ---------------- rows ----------------

    /** Sliders are editable only in the custom preset; elsewhere they mirror it. */
    private void syncRows(int preset) {
        boolean editable = preset == CUSTOM;
        short[] curve = presetBandsSafe(preset);
        for (int band = 0; band < bandRows.length; band++) {
            SeekBarPreference row = bandRows[band];
            if (row == null) continue;
            row.setEnabled(editable);
            if (!editable && curve != null && band < curve.length) {
                row.setValue(curve[band]);
            }
        }
    }

    private short[] presetBandsSafe(int preset) {
        if (preset < 0 || preset >= PRESET_BANDS.length) return PRESET_BANDS[0];
        if (PRESET_BANDS[preset] != null) return PRESET_BANDS[preset];
        short[] custom = new short[BAND_LABELS.length];
        for (int band = 0; band < custom.length; band++) {
            custom[band] = (short) storedBand(band, 0);
        }
        return custom;
    }

    private int storedPreset() {
        int value = Settings.Global.getInt(context.getContentResolver(), KEY_PRESET, OFF);
        return (value < 0 || value >= PRESET_NAMES.length) ? OFF : value;
    }

    private int storedBand(int band, int fallback) {
        return Settings.Global.getInt(context.getContentResolver(), KEY_BAND + band, fallback);
    }

    // ---------------- engine ----------------

    private synchronized void ensureSession() {
        if (track != null && equalizer != null) return;
        try {
            int rate = 48000;
            int buffer = AudioTrack.getMinBufferSize(rate,
                    AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
            if (buffer <= 0) buffer = 4096;
            AudioTrack created = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(rate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                            .build())
                    .setBufferSizeInBytes(buffer * 4)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build();
            created.play();
            track = created;
            final AudioTrack reference = created;
            Thread pump = new Thread(() -> {
                short[] silence = new short[2048];
                while (true) {
                    AudioTrack current = track;
                    if (current == null || current != reference) break;
                    if (current.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) break;
                    current.write(silence, 0, silence.length);
                }
            });
            pump.setDaemon(true);
            pump.start();
            Thread.sleep(300);
            equalizer = new Equalizer(0, created.getAudioSessionId());
            Log.i(TAG, "eq session ready bands=" + equalizer.getNumberOfBands());
        } catch (Throwable error) {
            Log.w(TAG, "eq session failed", error);
            stop();
        }
    }

    private void applyPreset(int preset) {
        if (preset == OFF) {
            if (equalizer != null) {
                try { equalizer.setEnabled(false); } catch (Throwable error) {
                    Log.w(TAG, "eq disable failed", error);
                }
            }
            return;
        }
        if (preset == CUSTOM) {
            applyCustom();
            return;
        }
        ensureSession();
        if (equalizer == null) return;
        short[] curve = PRESET_BANDS[preset];
        try {
            for (short band = 0; band < equalizer.getNumberOfBands() && band < curve.length; band++) {
                equalizer.setBandLevel(band, curve[band]);
            }
            equalizer.setEnabled(true);
        } catch (Throwable error) {
            Log.w(TAG, "eq preset failed", error);
        }
    }

    private void applyCustom() {
        ensureSession();
        if (equalizer == null) return;
        try {
            for (short band = 0; band < equalizer.getNumberOfBands() && band < BAND_LABELS.length; band++) {
                equalizer.setBandLevel(band, (short) storedBand(band, 0));
            }
            equalizer.setEnabled(true);
        } catch (Throwable error) {
            Log.w(TAG, "eq custom failed", error);
        }
    }
}
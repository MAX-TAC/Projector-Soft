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
import androidx.preference.SwitchPreference;

/**
 * Android-level equalizer that works on every output, including Bluetooth.
 *
 * Why this exists next to the stock five-band panel: the OEM equalizer writes
 * g_fusion_sound__equalizer_* which configures the MediaTek DSP. On 8054 that
 * path is silent over Bluetooth because the BT stream leaves AudioFlinger
 * before the DSP (measured: Bluetooth "SRC_DATA_READY" while the OEM keys were
 * written with no audible change). android.media.audiofx.Equalizer binds to an
 * AudioFlinger session instead, so it applies to whatever output is active.
 *
 * Verified on 8054 with a BT soundbar: band 0 (+12 dB) and band 4 (+12 dB)
 * were clearly audible.
 *
 * The effect needs a live AudioTrack session, so this panel keeps a silent
 * stream alive while the page is resumed and releases it on stop.
 */
final class BtEqualizerPanel {
    private static final String TAG = "TvSettingsPlusMOD";

    private static final String PREF_ENABLED = "projectorsoft_bt_eq_enabled";
    private static final String PREF_PRESET = "projectorsoft_bt_eq_preset";
    private static final String PREF_BAND = "projectorsoft_bt_eq_band_";

    static final String KEY_ENABLED = "projectorsoft_bt_eq_switch";
    static final String KEY_PRESET = "projectorsoft_bt_eq_preset_row";
    static final String[] BAND_KEYS = {
            "projectorsoft_bt_eq_band_row_0", "projectorsoft_bt_eq_band_row_1",
            "projectorsoft_bt_eq_band_row_2", "projectorsoft_bt_eq_band_row_3",
            "projectorsoft_bt_eq_band_row_4"
    };
    static final String[] BAND_LABELS = {"60 Гц", "230 Гц", "910 Гц", "3,6 кГц", "14 кГц"};

    private static final String[] PRESET_NAMES = {
            "Ровный", "Бас", "Высокие", "Голос", "Пользовательский"
    };
    private static final short[][] PRESETS = {
            {0, 0, 0, 0, 0},
            {1200, 600, 0, 0, 0},
            {0, 0, 0, 600, 1200},
            {-300, 0, 900, 900, 300},
            null
    };

    private final Context context;
    private AudioTrack track;
    private Equalizer equalizer;

    BtEqualizerPanel(Context context) {
        this.context = context;
    }

    /** Builds the rows. Sliders are plain rows that open the stock dialog. */
    void install(PreferenceGroup parent) {
        if (parent == null) return;
        boolean on = Settings.Global.getInt(context.getContentResolver(), PREF_ENABLED, 0) == 1;

        SwitchPreference toggle = new SwitchPreference(context);
        toggle.setKey(KEY_ENABLED);
        toggle.setTitle("Эквалайзер для любых выходов");
        toggle.setSummary("Работает и на Bluetooth. Независим от штатных полос выше.");
        toggle.setOrder(300);
        toggle.setPersistent(false);
        toggle.setChecked(on);
        toggle.setOnPreferenceChangeListener((pref, value) -> {
            boolean enable = value instanceof Boolean && (Boolean) value;
            Settings.Global.putInt(context.getContentResolver(), PREF_ENABLED, enable ? 1 : 0);
            applyEnabled(enable);
            return false;
        });
        parent.addPreference(toggle);

        ListPreference preset = new ListPreference(context);
        preset.setKey(KEY_PRESET);
        preset.setTitle("Пресет");
        preset.setEntries(PRESET_NAMES);
        preset.setEntryValues(new String[]{"0", "1", "2", "3", "4"});
        preset.setOrder(301);
        preset.setPersistent(false);
        preset.setValue(String.valueOf(Settings.Global.getInt(
                context.getContentResolver(), PREF_PRESET, 0)));
        preset.setOnPreferenceChangeListener((pref, value) -> {
            int index;
            try {
                index = Integer.parseInt(String.valueOf(value));
            } catch (NumberFormatException bad) {
                return false;
            }
            Settings.Global.putInt(context.getContentResolver(), PREF_PRESET, index);
            Settings.Global.putInt(context.getContentResolver(), PREF_ENABLED, 1);
            applyPreset(index);
            return false;
        });
        parent.addPreference(preset);
    }

    /** Called from the page's onStart: open the session only when enabled. */
    void start() {
        if (Settings.Global.getInt(context.getContentResolver(), PREF_ENABLED, 0) == 1) {
            ensureSession();
        }
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

    /** Re-reads the kept values so the page never shows a stale switch. */
    void refresh(PreferenceGroup parent) {
        if (parent == null) return;
        SwitchPreference toggle = parent.findPreference(KEY_ENABLED);
        if (toggle != null) {
            toggle.setChecked(Settings.Global.getInt(
                    context.getContentResolver(), PREF_ENABLED, 0) == 1);
        }
        ListPreference preset = parent.findPreference(KEY_PRESET);
        if (preset != null) {
            preset.setValue(String.valueOf(Settings.Global.getInt(
                    context.getContentResolver(), PREF_PRESET, 0)));
        }
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
            Log.i(TAG, "BT eq session ready bands=" + equalizer.getNumberOfBands()
                    + " range=" + equalizer.getBandLevelRange()[0]
                    + ".." + equalizer.getBandLevelRange()[1]);
            applyPreset(Settings.Global.getInt(context.getContentResolver(), PREF_PRESET, 0));
        } catch (Throwable error) {
            Log.w(TAG, "BT eq session failed", error);
            stop();
        }
    }

    private void applyEnabled(boolean enable) {
        if (enable) {
            ensureSession();
            if (equalizer != null) {
                try { equalizer.setEnabled(true); } catch (Throwable error) {
                    Log.w(TAG, "eq enable failed", error);
                }
            }
        } else if (equalizer != null) {
            try { equalizer.setEnabled(false); } catch (Throwable error) {
                Log.w(TAG, "eq disable failed", error);
            }
        }
    }

    private void applyPreset(int index) {
        ensureSession();
        if (equalizer == null) return;
        if (index < 0 || index >= PRESETS.length) return;
        short[] bands = PRESETS[index];
        if (bands == null) return;
        try {
            for (short band = 0; band < equalizer.getNumberOfBands() && band < bands.length; band++) {
                equalizer.setBandLevel(band, bands[band]);
            }
            equalizer.setEnabled(true);
            Settings.Global.putInt(context.getContentResolver(), PREF_ENABLED, 1);
        } catch (Throwable error) {
            Log.w(TAG, "eq preset failed", error);
        }
    }
}
package dev.projectorsoft.tvsettings;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioDeviceCallback;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SeekBarPreference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** Owns a stable set of preferences while TvServices switches and republishes profiles. */
final class NativeEqualizerPanel {
    private static final String TAG = "TvSettingsPlusMOD";
    static final String MODE = "sound_style";
    static final String[] KEYS = OemFiveBandEqualizerHook.KEYS;
    private static final String[] CONFIG = {"g_fusion_sound__equalizer_120hz", "g_fusion_sound__equalizer_500hz",
            "g_fusion_sound__equalizer_1.5khz", "g_fusion_sound__equalizer_5khz", "g_fusion_sound__equalizer_10khz"};
    private static final String[] OUTPUT = {"sound_digital_output_to_device", "sound_spdif_type", "sound_earc"};
    private static final long[] DELAYS = {0, 150, 200, 300, 450, 600};

    interface Backend {
        Snapshot read() throws Exception;
        boolean write(String key, int value) throws Exception;
        default String[][] profiles() throws Exception {
            return new String[][]{OemInlinePreferencesHook.soundTitles(),
                    Arrays.stream(OemInlinePreferencesHook.soundValues()).mapToObj(String::valueOf).toArray(String[]::new)};
        }
    }

    static final class Band {
        final boolean supported;
        final Integer stored, applied;
        final int min, max;

        Band(boolean supported, Integer stored, Integer applied, int min, int max) {
            this.supported = supported;
            this.stored = stored;
            this.applied = applied;
            this.min = min;
            this.max = max;
        }
        boolean inRange() { return stored != null && min < max && stored >= min && stored <= max; }
        boolean coherent() { return supported && inRange() && stored.equals(applied); }
        Object signature() { return Arrays.asList(supported, stored, applied, min, max); }
    }

    static final class Snapshot {
        final String profile, appliedProfile;
        final List<String> output;
        final List<Band> bands;

        Snapshot(String profile, String appliedProfile, List<String> output, List<Band> bands) {
            this.profile = profile;
            this.appliedProfile = appliedProfile;
            this.output = output;
            this.bands = bands;
        }
        boolean profileReady() { return OemSoundValuePolicy.validProfile(profile) && profile.equals(appliedProfile); }
        boolean same(Snapshot other) {
            if (other == null || !Objects.equals(profile, other.profile)
                    || !Objects.equals(appliedProfile, other.appliedProfile) || !output.equals(other.output)) return false;
            for (int i = 0; i < KEYS.length; i++)
                if (!bands.get(i).signature().equals(other.bands.get(i).signature())) return false;
            return true;
        }
        boolean matches(String key, int value) {
            if (MODE.equals(key)) return profileReady() && String.valueOf(value).equals(profile);
            int i = Arrays.asList(KEYS).indexOf(key);
            return i >= 0 && bands.get(i).coherent() && bands.get(i).stored == value;
        }
    }

    private final Context context;
    private final Backend backend;
    private final ContentResolver resolver;
    private final AudioManager audio;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ListPreference mode;
    private final boolean profileOptionsKnown;
    private final BandRow[] rows = new BandRow[KEYS.length];
    private boolean active, polling, ready, audioRegistered;
    private Snapshot shown, previous;
    private int attempt, equalReads;
    private String pendingKey, feedback;
    private int pendingValue;

    private final Runnable poll = this::sample;
    private final ContentObserver observer = new ContentObserver(main) {
        @Override public void onChange(boolean selfChange) { refresh(); }
    };
    private final AudioDeviceCallback audioDevices = new AudioDeviceCallback() {
        @Override public void onAudioDevicesAdded(AudioDeviceInfo[] devices) { refresh(); }
        @Override public void onAudioDevicesRemoved(AudioDeviceInfo[] devices) { refresh(); }
    };

    NativeEqualizerPanel(Context context, PreferenceGroup group) {
        this(context, group, new OemBackend(context));
    }

    NativeEqualizerPanel(Context context, PreferenceGroup group, Backend backend) {
        this.context = context;
        this.backend = backend;
        resolver = context.getContentResolver();
        audio = context.getSystemService(AudioManager.class);
        mode = new ListPreference(context);
        mode.setTitle("Звуковой профиль");
        mode.setKey("projectorsoft_sound_mode");
        mode.setPersistent(false);
        mode.setOrder(213);
        String[][] profiles = null;
        try { profiles = backend.profiles(); }
        catch (Exception | LinkageError unavailable) { Log.w(TAG, "OEM profiles unavailable", unavailable); }
        profileOptionsKnown = OemCustomerOptions.valid(profiles);
        if (profileOptionsKnown) {
            mode.setEntries(profiles[0]);
            mode.setEntryValues(profiles[1]);
        } else {
            mode.setEntries(new CharSequence[0]);
            mode.setEntryValues(new CharSequence[0]);
        }
        mode.setOnPreferenceChangeListener((pref, value) -> {
            if (value instanceof String && mode.findIndexOfValue((String) value) >= 0
                    && OemSoundValuePolicy.validProfile((String) value))
                change(MODE, Integer.parseInt((String) value));
            return false;
        });
        group.addPreference(mode);
        for (int i = 0; i < rows.length; i++) {
            final String key = KEYS[i];
            BandRow row = new BandRow(context);
            row.setKey("projectorsoft_eq_" + key);
            row.setTitle(OemFiveBandEqualizerHook.LABELS[i]);
            row.setOrder(240 + i);
            row.setPersistent(false);
            row.setSeekBarIncrement(1);
            row.setUpdatesContinuously(false);
            row.setOnPreferenceChangeListener((pref, value) -> {
                if (value instanceof Integer) change(key, (Integer) value);
                return false;
            });
            rows[i] = row;
            group.addPreference(row);
        }
        render(read(), false);
    }

    void start() {
        if (active) return;
        active = true;
        observe(MODE);
        for (String key : KEYS) observe(key);
        for (String key : OUTPUT) observe(key);
        if (audio != null) try {
            audio.registerAudioDeviceCallback(audioDevices, main);
            audioRegistered = true;
        } catch (RuntimeException unavailable) {
            Log.w(TAG, "Audio device callback unavailable", unavailable);
        }
        refresh();
    }

    void stop() {
        if (!active) return;
        active = false;
        polling = ready = false;
        pendingKey = null;
        main.removeCallbacks(poll);
        try { resolver.unregisterContentObserver(observer); }
        catch (RuntimeException unavailable) { Log.w(TAG, "Sound observer teardown unavailable", unavailable); }
        if (audioRegistered) {
            audio.unregisterAudioDeviceCallback(audioDevices);
            audioRegistered = false;
        }
    }

    private void observe(String key) {
        try { resolver.registerContentObserver(Settings.Global.getUriFor(key), false, observer); }
        catch (RuntimeException unavailable) { Log.w(TAG, "Cannot observe sound state", unavailable); }
    }

    void refresh() {
        if (!active || polling) return;
        ready = false;
        polling = true;
        attempt = equalReads = 0;
        previous = null;
        render(shown, false);
        main.post(poll);
    }

    private Snapshot read() {
        try { return backend.read(); }
        catch (Exception | LinkageError unavailable) {
            Log.w(TAG, "Sound state unavailable", unavailable);
            return null;
        }
    }

    private void sample() {
        if (!active) return;
        Snapshot current = read();
        equalReads = current != null && current.same(previous) ? equalReads + 1 : 0;
        previous = current;
        boolean applied = pendingKey == null || current != null && current.matches(pendingKey, pendingValue);
        boolean settled = attempt >= 3 && equalReads >= 2 && applied;
        if (!settled && attempt < DELAYS.length - 1) {
            render(current, false);
            main.postDelayed(poll, DELAYS[++attempt]);
            return;
        }
        polling = false;
        ready = settled;
        if (!applied) feedback = "Применение звука не подтверждено";
        pendingKey = null;
        render(current, ready);
        if (feedback != null) {
            Toast.makeText(context, feedback, Toast.LENGTH_SHORT).show();
            feedback = null;
        }
    }

    private void change(String key, int requested) {
        if (!active || !ready || shown == null || !profileOptionsKnown) return;
        Snapshot fresh = read();
        if (!shown.same(fresh) || !fresh.profileReady()) {
            feedback = "Звуковой профиль или выход изменился";
            refresh();
            return;
        }
        int i = Arrays.asList(KEYS).indexOf(key);
        if (!MODE.equals(key)) {
            if (i < 0) return;
            Band band = fresh.bands.get(i);
            if (!band.coherent() || !OemSoundValuePolicy.acceptsBandChange(String.valueOf(band.stored), requested,
                    band.min, band.max, shown.profile, fresh.profile)) return;
        }
        if (fresh.matches(key, requested)) return;
        try {
            if (backend.write(key, requested)) {
                pendingKey = key;
                pendingValue = requested;
            } else {
                feedback = "Изменение звука отклонено";
            }
        } catch (Exception | LinkageError unavailable) {
            Log.w(TAG, "Sound change failed", unavailable);
            feedback = "Изменение звука не подтверждено";
        }
        refresh();
    }

    private void render(Snapshot snapshot, boolean stable) {
        shown = snapshot;
        String profile = snapshot == null ? null : snapshot.profile;
        mode.setValue(OemSoundValuePolicy.validProfile(profile) ? profile : null);
        CharSequence name = mode.getEntry();
        mode.setSummary(name == null ? "Профиль недоступен" : name);
        boolean profileReady = stable && profileOptionsKnown && name != null && snapshot != null && snapshot.profileReady();
        mode.setEnabled(profileReady);
        for (int i = 0; i < rows.length; i++) {
            Band band = snapshot == null ? null : snapshot.bands.get(i);
            BandRow row = rows[i];
            if (band != null && band.supported && band.inRange()) {
                row.setMax(band.max);
                row.setMin(band.min);
                row.setValue(band.stored);
                row.display(true);
                row.setSummary(profileReady && !"0".equals(profile) ? "Редактируется в пользовательском профиле" : null);
            } else {
                row.display(false);
                row.setSummary(band != null && !band.supported
                        ? "Полоса не поддерживается текущей конфигурацией" : "Значение недоступно");
            }
            row.setEnabled(profileReady && band != null && band.coherent()
                    && OemSoundValuePolicy.acceptsBandChange(String.valueOf(band.stored), band.stored,
                            band.min, band.max, profile, profile));
        }
    }

    private static final class BandRow extends SeekBarPreference {
        private boolean show;
        BandRow(Context context) { super(context); }
        void display(boolean value) {
            if (show == value) return;
            show = value;
            setShowSeekBarValue(value);
            notifyChanged();
        }
        @Override public void onBindViewHolder(PreferenceViewHolder holder) {
            super.onBindViewHolder(holder);
            int id = getContext().getResources().getIdentifier("seekbar", "id", getContext().getPackageName());
            View bar = id == 0 ? null : holder.findViewById(id);
            if (bar != null) bar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private static final class OemBackend implements Backend {
        private final Context context;
        private final ContentResolver resolver;

        OemBackend(Context context) {
            this.context = context;
            resolver = context.getContentResolver();
        }

        @Override public Snapshot read() throws Exception {
            if (!"8054".equals(OemHdmiPortInfo.machineId())) return null;
            List<String> supported = OemFiveBandEqualizerHook.nativeAvailableBands();
            if (supported == null) return null;
            String profile = Settings.Global.getString(resolver, MODE);
            List<String> output = output();
            Class<?> type = Class.forName("com.android.tv.settings.partnercustomizer.tvsettingservice.TVSettingConfig");
            Object config = type.getMethod("getInstance", Context.class).invoke(null, context);
            Method min = type.getMethod("getMin", String.class), max = type.getMethod("getMax", String.class);
            Method get = type.getMethod("getConfigValueInt", String.class);
            String current = String.valueOf(get.invoke(config, "g_audio__sound_mode"));
            List<Band> bands = new ArrayList<>();
            for (int i = 0; i < KEYS.length; i++) {
                Integer value = Settings.Global.getInt(resolver, KEYS[i], Integer.MIN_VALUE);
                boolean offered = supported.contains(KEYS[i]);
                if (!offered) {
                    bands.add(new Band(false, null, null, 0, 0));
                    continue;
                }
                try {
                    int low = (Integer) min.invoke(config, CONFIG[i]);
                    int high = (Integer) max.invoke(config, CONFIG[i]);
                    int applied = (Integer) get.invoke(config, CONFIG[i]);
                    bands.add(new Band(true, value == Integer.MIN_VALUE ? null : value, applied, low, high));
                } catch (ReflectiveOperationException | RuntimeException unavailable) {
                    bands.add(new Band(true, null, null, 0, 0));
                }
            }
            if (!Objects.equals(profile, Settings.Global.getString(resolver, MODE)) || !output.equals(output()))
                return null;
            return new Snapshot(profile, current, output, bands);
        }

        private List<String> output() {
            List<String> result = new ArrayList<>();
            for (String key : OUTPUT) result.add(Settings.Global.getString(resolver, key));
            return result;
        }

        @Override public String[][] profiles() throws Exception {
            return OemCustomerOptions.forKey(context, MODE);
        }

        @Override public boolean write(String key, int value) {
            return Settings.Global.putInt(resolver, key, value);
        }
    }
}

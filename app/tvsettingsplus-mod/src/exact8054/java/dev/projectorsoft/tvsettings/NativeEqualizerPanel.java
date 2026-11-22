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
import java.util.function.BooleanSupplier;

/** Owns a stable set of preferences while TvServices switches and republishes profiles. */
final class NativeEqualizerPanel {
    private static final String TAG = "TvSettingsPlusMOD";
    static final String MODE = "sound_style";
    static final String[] KEYS = {"sound_bass", "sound_treble",
            "sound_equalizer_detail_120hz", "sound_equalizer_detail_500hz",
            "sound_equalizer_detail_1500hz", "sound_equalizer_detail_5000hz",
            "sound_equalizer_detail_10000hz"};
    private static final String[] CONFIG = {"g_audio__aud_bass", "g_audio__aud_treble",
            "g_fusion_sound__equalizer_120hz", "g_fusion_sound__equalizer_500hz",
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
            if (!profileReady()) return false;
            if (MODE.equals(key)) return String.valueOf(value).equals(profile);
            int i = Arrays.asList(KEYS).indexOf(key);
            return "0".equals(profile) && i >= 0 && bands.get(i).coherent() && bands.get(i).stored == value;
        }
    }

    private final Context context;
    private final Backend backend;
    private final ContentResolver resolver;
    private final AudioManager audio;
    private final BooleanSupplier speakerRouteProbe;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ListPreference mode;
    private final boolean profileOptionsKnown;
    private final BandRow[] rows = new BandRow[KEYS.length];
    private boolean active, polling, ready, audioRegistered;
    private Snapshot shown, previous, pendingContext;
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
        this(context, group, backend, null);
    }

    /** RouteProbe is injectable for tests; production always queries Android AudioManager. */
    NativeEqualizerPanel(Context context, PreferenceGroup group, Backend backend,
                         BooleanSupplier routeProbe) {
        this.context = context;
        this.backend = backend;
        resolver = context.getContentResolver();
        audio = context.getSystemService(AudioManager.class);
        speakerRouteProbe = routeProbe == null
                ? () -> NativeAudioOutputReadout.speakerOnly(audio) : routeProbe;
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
            row.setTitle(i == 0 ? "Низкие частоты" : i == 1 ? "Высокие частоты"
                    : OemFiveBandEqualizerHook.LABELS[i - 2]);
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
        pendingContext = null;
        main.removeCallbacks(poll);
        try { resolver.unregisterContentObserver(observer); }
        catch (RuntimeException unavailable) { Log.w(TAG, "Sound observer teardown unavailable", unavailable); }
        if (audioRegistered) {
            audioRegistered = false;
            try { audio.unregisterAudioDeviceCallback(audioDevices); }
            catch (RuntimeException unavailable) { Log.w(TAG, "Audio device callback teardown unavailable", unavailable); }
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
        // Keep visible, last stable controls during the short TvServices sync.
        // The previous code disabled every widget on each observer callback.
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
        if (pendingKey != null && current != null
                && (!pendingContext.output.equals(current.output)
                    || !MODE.equals(pendingKey) && !pendingContext.profile.equals(current.profile))) {
            pendingKey = null;
            pendingContext = null;
            feedback = "Звуковой профиль или выход изменился. Применение не подтверждено";
        }
        boolean applied = pendingKey == null || current != null && current.matches(pendingKey, pendingValue);
        boolean settled = attempt >= 3 && equalReads >= 2 && applied;
        if (!settled && attempt < DELAYS.length - 1) {
            render(current, false);
            main.postDelayed(poll, DELAYS[++attempt]);
            return;
        }
        polling = false;
        ready = current != null && OemSoundValuePolicy.validProfile(current.profile);
        if (!applied) {
            Log.w(TAG, "Sound backend did not settle: key=" + pendingKey
                    + " requested=" + pendingValue
                    + " global=" + (current == null ? null : current.profile)
                    + " OEM=" + (current == null ? null : current.appliedProfile));
            feedback = "Применение звука не подтверждено";
        }
        pendingKey = null;
        pendingContext = null;
        render(current, ready);
        if (feedback != null) {
            Toast.makeText(context, feedback, Toast.LENGTH_SHORT).show();
            feedback = null;
        }
    }

    private boolean speakerRoute() {
        try { return speakerRouteProbe.getAsBoolean(); }
        catch (RuntimeException unavailable) { return false; }
    }

    private void change(String key, int requested) {
        if (!active || polling || shown == null || !profileOptionsKnown) return;
        // OEM tone controls configure the internal-speaker profile even when
        // media currently plays over Bluetooth. They must not be represented
        // as a Bluetooth-wide EQ, but editing the saved User profile is valid.
        Snapshot fresh = read();
        if (fresh == null || !Objects.equals(shown.profile, fresh.profile)
                || !Objects.equals(shown.output, fresh.output)) {
            feedback = "Звуковой профиль или выход изменился";
            refresh();
            return;
        }
        int i = Arrays.asList(KEYS).indexOf(key);
        if (!MODE.equals(key)) {
            if (i < 0) return;
            Band band = fresh.bands.get(i);
            if (!band.supported || !band.inRange()
                    || !OemSoundValuePolicy.acceptsBandChange(String.valueOf(band.stored), requested,
                    band.min, band.max, shown.profile, fresh.profile)) return;
        }
        if (fresh.matches(key, requested)) return;
        try {
            if (backend.write(key, requested)) {
                Log.i(TAG, "Sound Global edit requested: " + key + "=" + requested
                        + ", mode=" + fresh.profile + ", OEM mode=" + fresh.appliedProfile);
                pendingKey = key;
                pendingValue = requested;
                pendingContext = fresh;
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
        mode.setSummary(name == null ? "Профиль недоступен"
                : !speakerRoute() && "0".equals(profile)
                    ? name + " · настройки для динамиков" : name);
        boolean profileReady = profileOptionsKnown && name != null && snapshot != null
                && OemSoundValuePolicy.validProfile(profile);
        mode.setEnabled(profileReady && active && !polling);
        // The DSP effect is output-dependent, but User profile controls are
        // still editable while BT is active for future speaker playback.
        for (int i = 0; i < rows.length; i++) {
            Band band = snapshot == null ? null : snapshot.bands.get(i);
            BandRow row = rows[i];
            if (band != null && band.supported && band.inRange()) {
                row.setMax(band.max);
                row.setMin(band.min);
                row.setValue(band.stored);
                row.display(true);
                row.setSummary(null);
            } else {
                row.display(false);
                row.setSummary(band != null && !band.supported
                        ? "Полоса не поддерживается текущей конфигурацией" : "Значение недоступно");
            }
            row.setEnabled(profileReady && active && !polling
                    && band != null && band.supported && band.inRange()
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
            // Keep Bass/Treble independent from optional five-band EQ resources.
            if (supported == null) supported = java.util.Collections.emptyList();
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
                // Bass/Treble are separate native OEM controls. The five EQ
                // bands still require their own exact capability discovery.
                boolean offered = i < 2 || supported.contains(KEYS[i]);
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

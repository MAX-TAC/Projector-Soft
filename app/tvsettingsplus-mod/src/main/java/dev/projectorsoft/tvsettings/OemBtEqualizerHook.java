package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.audiofx.Equalizer;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * Рабочий эквалайзер для ЛЮБОГО выхода (включая Bluetooth).
 *
 * Почему не OEM-ключи: проверено на 8054 — OEM-эквалайзер
 * (g_fusion_sound__equalizer_*) применяется только к динамикам,
 * потому что настраивает MediaTek DSP, а BT-поток уходит в кодек
 * раньше DSP.
 *
 * android.media.audiofx.Equalizer работает на уровне AudioFlinger
 * и потому действует на любой выход. Проверено на 8054 с
 * BT-саундбаром Redmi: бас и высокие слышны отчётливо.
 *
 * Полосы: 60 / 230 / 910 / 3600 / 14000 Гц, диапазон -1500..1500 мБ.
 */
public final class OemBtEqualizerHook {
    private static final String TAG = "TvSettingsPlusMOD";
    private static final String PREF_ENABLED = "projectorsoft_bt_eq_enabled";
    private static final String PREF_PRESET = "projectorsoft_bt_eq_preset";
    private static final String PREF_BAND_PREFIX = "projectorsoft_bt_eq_band_";

    /** Состояние живёт в процессе с открытым AudioTrack. */
    private static volatile AudioTrack sTrack;
    private static volatile Equalizer sEq;

    private static final String[] PRESET_NAMES = {
            "Ровный", "Бас", "Высокие", "Голос", "Свой"
    };
    private static final short[][] PRESETS = {
            {0, 0, 0, 0, 0},
            {1200, 600, 0, 0, 0},
            {0, 0, 0, 600, 1200},
            {-300, 0, 900, 900, 300},
            null
    };

    private OemBtEqualizerHook() {}

    // ---------- движок ----------

    /**
     * Эквалайзер работает, пока есть живой AudioTrack. Создаём
     * беззвучный поток: PCM нулей, чтобы не мешать пользователю.
     */
    private static synchronized void ensureSession(Context ctx) {
        if (sTrack != null && sEq != null) return;
        try {
            int sr = 48000;
            int minBuf = AudioTrack.getMinBufferSize(sr,
                    AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT);
            if (minBuf <= 0) minBuf = 4096;
            AudioTrack t = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sr)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                            .build())
                    .setBufferSizeInBytes(minBuf * 4)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build();
            t.play();
            sTrack = t;
            final AudioTrack ref = t;
            Thread th = new Thread(() -> {
                short[] silence = new short[2048];
                while (true) {
                    AudioTrack cur = sTrack;
                    if (cur == null || cur != ref) break;
                    if (cur.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) break;
                    cur.write(silence, 0, silence.length);
                }
            });
            th.setDaemon(true);
            th.start();
            Thread.sleep(300);
            sEq = new Equalizer(0, t.getAudioSessionId());
            Log.i(TAG, "BT EQ session ready, bands=" + sEq.getNumberOfBands()
                    + " range=" + sEq.getBandLevelRange()[0] + ".." + sEq.getBandLevelRange()[1]);
        } catch (Throwable e) {
            Log.w(TAG, "BT EQ session failed", e);
        }
    }

    private static synchronized void applyPreset(Context ctx, int presetIndex) {
        ensureSession(ctx);
        if (sEq == null) return;
        try {
            short[] bands = (presetIndex >= 0 && presetIndex < PRESETS.length)
                    ? PRESETS[presetIndex] : null;
            if (bands == null) return;
            for (short b = 0; b < sEq.getNumberOfBands() && b < bands.length; b++) {
                sEq.setBandLevel(b, bands[b]);
            }
            Settings.Global.putInt(ctx.getContentResolver(), PREF_PRESET, presetIndex);
        } catch (Throwable e) {
            Log.w(TAG, "BT EQ preset failed", e);
        }
    }

    private static synchronized void setEnabled(Context ctx, boolean on) {
        if (on) {
            ensureSession(ctx);
            if (sEq != null) {
                try { sEq.setEnabled(true); } catch (Throwable e) { Log.w(TAG, "eq on", e); }
            }
        } else {
            if (sEq != null) {
                try { sEq.setEnabled(false); } catch (Throwable e) { Log.w(TAG, "eq off", e); }
            }
        }
        Settings.Global.putInt(ctx.getContentResolver(), PREF_ENABLED, on ? 1 : 0);
    }

    private static synchronized void setBand(Context ctx, int band, int millibels) {
        ensureSession(ctx);
        if (sEq == null) return;
        try {
            if (band < 0 || band >= sEq.getNumberOfBands()) return;
            short[] range = sEq.getBandLevelRange();
            short v = (short) Math.max(range[0], Math.min(range[1], millibels));
            sEq.setBandLevel((short) band, v);
            Settings.Global.putInt(ctx.getContentResolver(), PREF_BAND_PREFIX + band, v);
        } catch (Throwable e) {
            Log.w(TAG, "BT EQ band failed", e);
        }
    }

    // ---------- UI ----------

    public static void install(Context ctx, Object group, Class<?> preferenceClass) {
        try {
            LayoutInflater inflater = LayoutInflater.from(ctx);

            // 1. Переключатель эквалайзера
            Object toggle = buildSwitch(ctx, inflater,
                    "Эквалайзер (работает и на Bluetooth)",
                    Settings.Global.getInt(ctx.getContentResolver(), PREF_ENABLED, 0) == 1,
                    240,
                    (btn, isChecked) -> setEnabled(ctx, isChecked));
            if (toggle != null) {
                group.getClass().getMethod("addPreference", preferenceClass).invoke(group, toggle);
            }

            // 2. Пресеты
            Class<?> listPref = Class.forName("androidx.preference.ListPreference");
            Object preset = listPref.getConstructor(Context.class).newInstance(ctx);
            preferenceClass.getMethod("setKey", String.class).invoke(preset, PREF_PRESET);
            preferenceClass.getMethod("setTitle", CharSequence.class).invoke(preset, "Пресет эквалайзера");
            preferenceClass.getMethod("setOrder", int.class).invoke(preset, 241);
            preferenceClass.getMethod("setPersistent", boolean.class).invoke(preset, true);
            listPref.getMethod("setEntries", CharSequence[].class).invoke(preset, (Object) PRESET_NAMES);
            String[] values = new String[PRESET_NAMES.length];
            for (int i = 0; i < values.length; i++) values[i] = String.valueOf(i);
            listPref.getMethod("setEntryValues", CharSequence[].class).invoke(preset, (Object) values);
            int currentPreset = Settings.Global.getInt(ctx.getContentResolver(), PREF_PRESET, 0);
            listPref.getMethod("setValue", String.class).invoke(preset, String.valueOf(currentPreset));

            Class<?> listener = Class.forName("androidx.preference.Preference$OnPreferenceChangeListener");
            InvocationHandler cb = (proxy, method, args) -> {
                String name = method.getName();
                if ("onPreferenceChange".equals(name) && args != null && args.length >= 2) {
                    try {
                        int idx = Integer.parseInt(String.valueOf(args[1]));
                        setEnabled(ctx, true);
                        applyPreset(ctx, idx);
                        return true;
                    } catch (Throwable e) { return false; }
                }
                if ("toString".equals(name)) return "BtEqPreset";
                if ("hashCode".equals(name)) return System.identityHashCode(proxy);
                if ("equals".equals(name)) return args != null && args.length > 0 && proxy == args[0];
                return false;
            };
            Object cbObj = Proxy.newProxyInstance(listener.getClassLoader(), new Class<?>[]{listener}, cb);
            listPref.getMethod("setOnPreferenceChangeListener", listener).invoke(preset, cbObj);
            group.getClass().getMethod("addPreference", preferenceClass).invoke(group, preset);

            // 3. Пять полос вручную
            String[] labels = {"60 Гц", "230 Гц", "910 Гц", "3,6 кГц", "14 кГц"};
            for (int i = 0; i < 5; i++) {
                final int bandIndex = i;
                int saved = Settings.Global.getInt(ctx.getContentResolver(), PREF_BAND_PREFIX + i, 0);
                Object slider = buildBandSlider(ctx, inflater, labels[i], saved, 242 + i,
                        (bar, progress) -> {
                            int mb = progress - 1500;  // 0..3000 -> -1500..1500
                            setBand(ctx, bandIndex, mb);
                        });
                if (slider != null) {
                    group.getClass().getMethod("addPreference", preferenceClass).invoke(group, slider);
                }
            }
        } catch (Throwable e) {
            Log.w(TAG, "BT EQ UI unavailable", e);
        }
    }

    // ---------- helpers ----------

    private interface OnToggle { void apply(Object button, boolean checked); }
    private interface OnSlide { void apply(Object bar, int progress); }

    private static Object buildSwitch(Context ctx, LayoutInflater inflater, String title,
                                      boolean checked, int order, OnToggle cb) {
        try {
            Class<?> prefClass = Class.forName("androidx.preference.Preference");
            Object pref = prefClass.getConstructor(Context.class).newInstance(ctx);
            prefClass.getMethod("setTitle", CharSequence.class).invoke(pref, title);
            prefClass.getMethod("setOrder", int.class).invoke(pref, order);
            prefClass.getMethod("setPersistent", boolean.class).invoke(pref, false);

            int layout = ctx.getResources().getIdentifier("preference_widget_switch",
                    "layout", "androidx.preference");
            final View view;
            if (layout != 0) {
                view = inflater.inflate(layout, null, false);
            } else {
                LinearLayout ll = new LinearLayout(ctx);
                ll.setOrientation(LinearLayout.HORIZONTAL);
                TextView tv = new TextView(ctx);
                tv.setText(title);
                Switch sw = new Switch(ctx);
                sw.setChecked(checked);
                ll.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                ll.addView(sw);
                view = ll;
            }
            final Switch target = findSwitch(view);
            if (target != null) {
                target.setChecked(checked);
                target.setOnCheckedChangeListener((btn, isChecked) -> cb.apply(btn, isChecked));
            }
            prefClass.getMethod("setOnPreferenceClickListener",
                    Class.forName("androidx.preference.Preference$OnPreferenceClickListener"));
            return pref;
        } catch (Throwable e) {
            Log.w(TAG, "buildSwitch failed", e);
            return null;
        }
    }

    private static Switch findSwitch(View v) {
        if (v instanceof Switch) return (Switch) v;
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) {
                Switch s = findSwitch(g.getChildAt(i));
                if (s != null) return s;
            }
        }
        return null;
    }

    private static Object buildBandSlider(Context ctx, LayoutInflater inflater, String title,
                                          int millibels, int order, OnSlide cb) {
        try {
            Class<?> seek = Class.forName("androidx.preference.SeekBarPreference");
            Object item = seek.getConstructor(Context.class).newInstance(ctx);
            Class<?> prefClass = Class.forName("androidx.preference.Preference");
            prefClass.getMethod("setTitle", CharSequence.class).invoke(item, title);
            prefClass.getMethod("setOrder", int.class).invoke(item, order);
            prefClass.getMethod("setPersistent", boolean.class).invoke(item, false);
            seek.getMethod("setMin", int.class).invoke(item, 0);
            seek.getMethod("setMax", int.class).invoke(item, 3000);
            seek.getMethod("setSeekBarIncrement", int.class).invoke(item, 50);
            seek.getMethod("setShowSeekBarValue", boolean.class).invoke(item, false);
            seek.getMethod("setUpdatesContinuously", boolean.class).invoke(item, true);
            seek.getMethod("setValue", int.class).invoke(item, millibels + 1500);

            Class<?> listener = Class.forName("androidx.preference.Preference$OnPreferenceChangeListener");
            InvocationHandler h = (proxy, method, args) -> {
                String name = method.getName();
                if ("onPreferenceChange".equals(name) && args != null && args.length >= 2) {
                    if (!(args[1] instanceof Integer)) return false;
                    cb.apply(null, (Integer) args[1]);
                    return true;
                }
                if ("toString".equals(name)) return "BtEqBand:" + title;
                if ("hashCode".equals(name)) return System.identityHashCode(proxy);
                if ("equals".equals(name)) return args != null && args.length > 0 && proxy == args[0];
                return false;
            };
            Object hObj = Proxy.newProxyInstance(listener.getClassLoader(), new Class<?>[]{listener}, h);
            seek.getMethod("setOnPreferenceChangeListener", listener).invoke(item, hObj);
            return item;
        } catch (Throwable e) {
            Log.w(TAG, "buildBandSlider failed", e);
            return null;
        }
    }
}

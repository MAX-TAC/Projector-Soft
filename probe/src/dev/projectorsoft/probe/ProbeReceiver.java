package dev.projectorsoft.probe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.audiofx.Equalizer;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * AudioRouteProbe — диагностический зонд для FlixTone D11 (8054).
 *
 * Скрытые API (AudioProductStrategy) вызываются через рефлексию,
 * потому что их нет в публичном android.jar.
 *
 * adb shell am broadcast -a dev.projectorsoft.probe.CMD --es cmd status
 */
public class ProbeReceiver extends BroadcastReceiver {

    private static final String TAG = "PROBE";
    private static volatile AudioTrack sTrack;
    private static volatile Equalizer sEq;

    @Override
    public void onReceive(Context ctx, Intent intent) {
        String cmd = intent == null ? null : intent.getStringExtra("cmd");
        if (cmd == null) cmd = "status";
        log("CMD=" + cmd);
        switch (cmd) {
            case "status":      dumpStatus(ctx); break;
            case "eq_on":       eqEnable(true); break;
            case "eq_off":      eqEnable(false); break;
            case "eq_bass":     eqBand(0, 1200); break;
            case "eq_treble":   eqBand(4, 1200); break;
            case "eq_flat":     eqFlat(); break;
            case "eq_info":     eqInfo(); break;
            case "out_speaker": preferOutput(ctx, new int[]{AudioDeviceInfo.TYPE_BUILTIN_SPEAKER}); break;
            case "out_bt":      preferOutput(ctx, new int[]{AudioDeviceInfo.TYPE_BLUETOOTH_A2DP}); break;
            case "out_both":    preferOutput(ctx, new int[]{AudioDeviceInfo.TYPE_BUILTIN_SPEAKER, AudioDeviceInfo.TYPE_BLUETOOTH_A2DP}); break;
            case "tone_start":  toneStart(ctx); break;
            case "tone_stop":   toneStop(); break;
            default: log("UNKNOWN_CMD=" + cmd); break;
        }
    }

    private static void log(String msg) { Log.i(TAG, msg); }

    // ---------------- рефлексия для скрытого API ----------------

    /** Возвращает список AudioProductStrategy через рефлексию. */
    private static List<Object> productStrategies(AudioManager am) {
        List<Object> out = new ArrayList<>();
        try {
            Method m = AudioManager.class.getMethod("getAudioProductStrategies");
            Object list = m.invoke(am);
            if (list instanceof List) {
                for (Object o : (List<?>) list) if (o != null) out.add(o);
            }
        } catch (Throwable t) { log("reflect getAudioProductStrategies: " + t); }
        return out;
    }

    /** getId() / getName() стратегии через рефлексию. */
    private static String stratInfo(Object st) {
        String id = "?", name = "?";
        try {
            Method gid = st.getClass().getMethod("getId");
            Object v = gid.invoke(st);
            if (v != null) id = v.toString();
        } catch (Throwable ignored) {}
        try {
            Method gname = st.getClass().getMethod("getName");
            Object v = gname.invoke(st);
            if (v != null) name = v.toString();
        } catch (Throwable ignored) {}
        return id + " name=" + name;
    }

    private static List<AudioDeviceInfo> preferredFor(AudioManager am, Object st) {
        List<AudioDeviceInfo> out = new ArrayList<>();
        try {
            for (Method m : AudioManager.class.getMethods()) {
                if (!m.getName().equals("getPreferredDevicesForStrategy")) continue;
                Class<?>[] p = m.getParameterTypes();
                if (p.length != 1 || !p[0].isInstance(st)) continue;
                Object r = m.invoke(am, st);
                if (r instanceof List) {
                    for (Object o : (List<?>) r) if (o instanceof AudioDeviceInfo) out.add((AudioDeviceInfo) o);
                }
                break;
            }
        } catch (Throwable t) { log("reflect getPreferredDevicesForStrategy: " + t); }
        return out;
    }

    private static boolean setPreferred(AudioManager am, Object st, List<AudioDeviceInfo> devs) {
        try {
            for (Method m : AudioManager.class.getMethods()) {
                if (!m.getName().equals("setPreferredDevicesForStrategy")) continue;
                Class<?>[] p = m.getParameterTypes();
                if (p.length != 2 || !p[0].isInstance(st) || !List.class.isAssignableFrom(p[1])) continue;
                Object r = m.invoke(am, st, devs);
                return r instanceof Boolean && (Boolean) r;
            }
            log("setPreferredDevicesForStrategy: no matching method");
        } catch (Throwable t) { log("reflect setPreferred: " + t); }
        return false;
    }

    // ---------------- команды ----------------

    private void dumpStatus(Context ctx) {
        AudioManager am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        log("--- OUTPUTS ---");
        for (AudioDeviceInfo d : am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)) {
            log("OUT id=" + d.getId() + " type=" + d.getType() + " name=" + d.getProductName());
        }
        log("--- STRATEGIES ---");
        List<Object> strategies = productStrategies(am);
        log("strategyCount=" + strategies.size());
        for (Object st : strategies) {
            StringBuilder sb = new StringBuilder();
            for (AudioDeviceInfo d : preferredFor(am, st)) {
                sb.append(d.getId()).append("/").append(d.getType()).append(" ");
            }
            log("STRATEGY " + stratInfo(st) + " preferred=[" + sb.toString().trim() + "]");
        }
        log("--- EQ ---");
        log("eq=" + (sEq == null ? "null" : "enabled=" + sEq.getEnabled() + " bands=" + sEq.getNumberOfBands()));
    }

    private void eqInfo() {
        if (sEq == null) { log("EQ not created — run tone_start first"); return; }
        try {
            short[] range = sEq.getBandLevelRange();
            log("EQ bandLevelRange=" + range[0] + ".." + range[1]
                    + " (mB)  numBands=" + sEq.getNumberOfBands());
            for (short b = 0; b < sEq.getNumberOfBands(); b++) {
                log("EQ band" + b + " center=" + (sEq.getCenterFreq(b) / 1000) + "Hz level="
                        + sEq.getBandLevel(b) + "mB");
            }
        } catch (Throwable t) { log("EQ info error: " + t); }
    }

    private void eqEnable(boolean on) {
        try {
            if (sEq == null) {
                if (sTrack == null) { log("ERROR: no AudioTrack. Run tone_start first."); return; }
                int session = sTrack.getAudioSessionId();
                log("creating Equalizer on session " + session);
                sEq = new Equalizer(0, session);
            }
            sEq.setEnabled(on);
            log("EQ enabled=" + sEq.getEnabled());
        } catch (Throwable t) { log("EQ enable error: " + t); }
    }

    private void eqBand(int bandIndex, int millibels) {
        try {
            if (sEq == null) eqEnable(true);
            if (sEq == null) { log("EQ still null"); return; }
            sEq.setBandLevel((short) bandIndex, (short) millibels);
            log("EQ band" + bandIndex + " = " + sEq.getBandLevel((short) bandIndex) + "mB");
        } catch (Throwable t) { log("EQ band error: " + t); }
    }

    private void eqFlat() {
        try {
            if (sEq == null) eqEnable(true);
            if (sEq == null) { log("EQ still null"); return; }
            for (short b = 0; b < sEq.getNumberOfBands(); b++) sEq.setBandLevel(b, (short) 0);
            log("EQ all bands = 0");
        } catch (Throwable t) { log("EQ flat error: " + t); }
    }

    private void preferOutput(Context ctx, int[] types) {
        AudioManager am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        try {
            List<AudioDeviceInfo> wanted = new ArrayList<>();
            for (AudioDeviceInfo d : am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)) {
                for (int t : types) if (d.getType() == t) wanted.add(d);
            }
            if (wanted.isEmpty()) { log("no matching device for requested types"); return; }
            log("want " + wanted.size() + " device(s)");
            for (Object st : productStrategies(am)) {
                String nm = stratInfo(st).toLowerCase();
                if (!nm.contains("media")) continue;
                boolean ok = setPreferred(am, st, wanted);
                log("setPreferred -> ok=" + ok + " for " + stratInfo(st));
            }
            dumpStatus(ctx);
        } catch (Throwable t) { log("preferOutput error: " + t); }
    }

    private void toneStart(Context ctx) {
        try {
            if (sTrack != null && sTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                log("tone already running");
                return;
            }
            int sr = 48000;
            int minBuf = AudioTrack.getMinBufferSize(sr,
                    android.media.AudioFormat.CHANNEL_OUT_STEREO,
                    android.media.AudioFormat.ENCODING_PCM_16BIT);
            if (minBuf <= 0) minBuf = 4096;
            AudioTrack t = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
                    .setAudioFormat(new android.media.AudioFormat.Builder()
                            .setEncoding(android.media.AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sr)
                            .setChannelMask(android.media.AudioFormat.CHANNEL_OUT_STEREO)
                            .build())
                    .setBufferSizeInBytes(minBuf * 4)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build();
            t.play();
            sTrack = t;
            log("tone started session=" + t.getAudioSessionId());

            final int sampleRate = sr;
            final AudioTrack ref = t;
            Thread th = new Thread(new Runnable() {
                @Override public void run() {
                    short[] pcm = makeNoise(sampleRate);
                    while (ref.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                        ref.write(pcm, 0, pcm.length);
                    }
                }
            });
            th.setDaemon(true);
            th.start();

            Thread.sleep(500);
            eqEnable(false);
            eqInfo();
        } catch (Throwable t) { log("tone error: " + t); }
    }

    private static short[] makeNoise(int sr) {
        int n = sr;
        short[] out = new short[n * 2];
        java.util.Random r = new java.util.Random(42);
        double b0 = 0, b1 = 0, b2 = 0;
        for (int i = 0; i < n; i++) {
            double w = r.nextDouble() * 2 - 1;
            b0 = 0.99765 * b0 + w * 0.0990460;
            b1 = 0.96300 * b1 + w * 0.2965164;
            b2 = 0.57000 * b2 + w * 1.0526913;
            double v = (b0 + b1 + b2 + w * 0.1848) * 0.15;
            if (v > 1) v = 1; if (v < -1) v = -1;
            short s = (short) (v * 32000);
            out[i * 2] = s;
            out[i * 2 + 1] = s;
        }
        return out;
    }

    private void toneStop() {
        try {
            if (sEq != null) { sEq.setEnabled(false); sEq.release(); sEq = null; }
            if (sTrack != null) { sTrack.stop(); sTrack.release(); sTrack = null; }
            log("tone stopped");
        } catch (Throwable t) { log("stop error: " + t); }
    }
}

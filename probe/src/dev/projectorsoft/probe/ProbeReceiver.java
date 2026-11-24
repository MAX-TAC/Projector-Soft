package dev.projectorsoft.probe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.audiofx.Equalizer;
import android.os.Build;
import android.util.Log;

/**
 * AudioRouteProbe — диагностический зонд для проектора FlixTone D11 (8054).
 *
 * Задачи:
 *  1. Проверить, влияет ли android.media.audiofx.Equalizer на BT-выход.
 *  2. Проверить переключение выхода через setPreferredDevicesForStrategy.
 *  3. Проверить одновременный вывод (SPEAKER + BT).
 *
 * Управление (только чтение состояния + явные действия по команде):
 *   adb shell am broadcast -a dev.projectorsoft.probe.CMD --es cmd status
 *   adb shell am broadcast -a dev.projectorsoft.probe.CMD --es cmd eq_on
 *   adb shell am broadcast -a dev.projectorsoft.probe.CMD --es cmd eq_off
 *   adb shell am broadcast -a dev.projectorsoft.probe.CMD --es cmd eq_bass
 *   adb shell am broadcast -a dev.projectorsoft.probe.CMD --es cmd eq_treble
 *   adb shell am broadcast -a dev.projectorsoft.probe.CMD --es cmd out_speaker
 *   adb shell am broadcast -a dev.projectorsoft.probe.CMD --es cmd out_bt
 *   adb shell am broadcast -a dev.projectorsoft.probe.CMD --es cmd out_both
 *   adb shell am broadcast -a dev.projectorsoft.probe.CMD --es cmd tone_start
 *   adb shell am broadcast -a dev.projectorsoft.probe.CMD --es cmd tone_stop
 */
public class ProbeReceiver extends BroadcastReceiver {

    private static final String TAG = "PROBE";

    // Общий AudioTrack, чтобы у эффектов была живая сессия.
    private static volatile AudioTrack sTrack;
    private static volatile Equalizer sEq;

    @Override
    public void onReceive(Context ctx, Intent intent) {
        String cmd = intent == null ? null : intent.getStringExtra("cmd");
        if (cmd == null) cmd = "status";
        log("CMD=" + cmd);

        switch (cmd) {
            case "status":      dumpStatus(ctx); break;
            case "eq_on":       eqEnable(ctx, true); break;
            case "eq_off":      eqEnable(ctx, false); break;
            case "eq_bass":     eqBand(ctx, 0, 1200); break;
            case "eq_treble":   eqBand(ctx, 4, 1200); break;
            case "eq_flat":     eqAllBands(ctx, 0); break;
            case "eq_info":     eqInfo(); break;
            case "out_speaker": preferOutput(ctx, new int[]{AudioDeviceInfo.TYPE_BUILTIN_SPEAKER}); break;
            case "out_bt":      preferOutput(ctx, new int[]{AudioDeviceInfo.TYPE_BLUETOOTH_A2DP}); break;
            case "out_both":    preferOutput(ctx, new int[]{AudioDeviceInfo.TYPE_BUILTIN_SPEAKER, AudioDeviceInfo.TYPE_BLUETOOTH_A2DP}); break;
            case "tone_start":  toneStart(ctx); break;
            case "tone_stop":   toneStop(); break;
            default: log("UNKNOWN_CMD=" + cmd); break;
        }
    }

    // ---------- утилиты ----------

    private static void log(String msg) {
        Log.i(TAG, msg);
    }

    private void dumpStatus(Context ctx) {
        AudioManager am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        log("--- OUTPUTS ---");
        for (AudioDeviceInfo d : am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)) {
            log("OUT id=" + d.getId() + " type=" + d.getType() + " name=" + d.getProductName());
        }
        log("--- DEFAULT (USAGE_MEDIA) ---");
        AudioDeviceInfo def = am.getCommunicationDevice();
        log("commDevice=" + (def == null ? "null" : def.getId() + "/" + def.getType()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            java.util.List<android.media.AudioProductStrategy> strategies =
                    am.getAudioProductStrategies();
            for (android.media.AudioProductStrategy st : strategies) {
                java.util.List<AudioDeviceInfo> devs = am.getPreferredDevicesForStrategy(st);
                StringBuilder sb = new StringBuilder();
                for (AudioDeviceInfo d : devs) sb.append(d.getId()).append("/").append(d.getType()).append(" ");
                log("STRATEGY id=" + st.getId() + " name=" + st.getName() + " preferred=[" + sb + "]");
            }
        }
        log("--- EQ ---");
        log("eq=" + (sEq == null ? "null" : "present, enabled=" + sEq.getEnabled()));
    }

    private void eqInfo() {
        if (sEq == null) { log("EQ not created — run tone_start first"); return; }
        try {
            short[] range = sEq.getBandLevelRange();
            log("EQ bandLevelRange=" + range[0] + ".." + range[1]);
            log("EQ numBands=" + sEq.getNumberOfBands());
            for (short b = 0; b < sEq.getNumberOfBands(); b++) {
                int[] f = sEq.getBandFreqRange(b);
                log("EQ band" + b + " centerFreq=" + sEq.getCenterFreq(b) / 1000 + "Hz range="
                        + f[0] / 1000 + ".." + f[1] / 1000 + "Hz level=" + sEq.getBandLevel(b));
            }
        } catch (Throwable t) { log("EQ info error: " + t); }
    }

    private void eqEnable(Context ctx, boolean on) {
        try {
            if (sTrack == null) { log("ERROR: no AudioTrack. Run tone_start or play media first."); return; }
            if (sEq == null) {
                int session = sTrack.getAudioSessionId();
                log("creating Equalizer on session " + session);
                sEq = new Equalizer(0, session);
            }
            sEq.setEnabled(on);
            log("EQ enabled=" + sEq.getEnabled());
        } catch (Throwable t) { log("EQ enable error: " + t); }
    }

    private void eqBand(Context ctx, int bandIndex, int millibels) {
        try {
            if (sEq == null) { eqEnable(ctx, true); }
            if (sEq == null) { log("EQ still null"); return; }
            sEq.setBandLevel((short) bandIndex, (short) millibels);
            log("EQ band" + bandIndex + " = " + sEq.getBandLevel((short) bandIndex));
        } catch (Throwable t) { log("EQ band error: " + t); }
    }

    private void eqAllBands(Context ctx, int millibels) {
        try {
            if (sEq == null) { eqEnable(ctx, true); }
            if (sEq == null) { log("EQ still null"); return; }
            for (short b = 0; b < sEq.getNumberOfBands(); b++) sEq.setBandLevel(b, (short) millibels);
            log("EQ all bands set to " + millibels);
        } catch (Throwable t) { log("EQ all error: " + t); }
    }

    private void preferOutput(Context ctx, int[] types) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) { log("needs API 31+"); return; }
        AudioManager am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        try {
            java.util.List<AudioDeviceInfo> wanted = new java.util.ArrayList<>();
            for (AudioDeviceInfo d : am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)) {
                for (int t : types) if (d.getType() == t) wanted.add(d);
            }
            if (wanted.isEmpty()) { log("no matching device for types"); return; }
            java.util.List<android.media.AudioProductStrategy> strategies = am.getAudioProductStrategies();
            for (android.media.AudioProductStrategy st : strategies) {
                if (st.getName() != null && st.getName().toLowerCase().contains("media")) {
                    boolean ok = am.setPreferredDevicesForStrategy(st, wanted);
                    log("setPreferredDevicesForStrategy(media," + wanted.size() + ") ok=" + ok);
                }
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
            int buf = AudioTrack.getMinBufferSize(sr, android.media.AudioFormat.CHANNEL_OUT_STEREO,
                    android.media.AudioFormat.ENCODING_PCM_16BIT);
            if (buf <= 0) buf = 4096;
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
                    .setBufferSizeInBytes(buf * 4)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build();
            t.play();
            sTrack = t;
            log("tone started, session=" + t.getAudioSessionId());
            final int sampleRate = sr;
            Thread th = new Thread(() -> {
                short[] pcm = makeNoise(sampleRate);
                AudioTrack tt = sTrack;
                while (tt != null && tt.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                    tt.write(pcm, 0, pcm.length);
                }
            });
            th.setDaemon(true);
            th.start();
            // Создаём EQ сразу на этой сессии
            Thread.sleep(500);
            eqEnable(ctx, false);
            eqInfo();
        } catch (Throwable t) { log("tone error: " + t); }
    }

    private static short[] makeNoise(int sr) {
        // Пинк-подобный шум: слышно на любых частотах, хорошо для оценки EQ.
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

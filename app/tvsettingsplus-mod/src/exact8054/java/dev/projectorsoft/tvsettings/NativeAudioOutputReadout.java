package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.util.Log;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import java.util.ArrayList;
import java.util.List;

/**
 * Non-mutating Android 13 media route readout. Uses the public API 33
 * getAudioDevicesForAttributes(USAGE_MEDIA), not Bluetooth bonded devices.
 * ARC/eARC is never inferred from a generic vendor policy port alone.
 */
final class NativeAudioOutputReadout {
    static final String KEY = "projectorsoft_media_output_status";
    private static final String TAG = "TvSettingsPlusMOD";
    private static final AudioAttributes MEDIA =
            new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build();

    private NativeAudioOutputReadout() {}

    static void install(Context context, PreferenceScreen screen) {
        Preference row = new Preference(context);
        row.setKey(KEY);
        row.setTitle("Устройство вывода · диагностика");
        row.setOrder(195);
        row.setPersistent(false);
        row.setOnPreferenceClickListener(item -> {
            refresh(context, screen);
            return true;
        });
        screen.addPreference(row);
        refresh(context, screen);
    }

    static void refresh(Context context, PreferenceScreen screen) {
        Preference row = screen.findPreference(KEY);
        if (row == null) return;
        AudioManager audio = context.getSystemService(AudioManager.class);
        if (audio == null || !"8054".equals(OemHdmiPortInfo.machineId())) {
            row.setSummary("Аудиомаршрут недоступен");
            row.setEnabled(false);
            return;
        }
        try {
            List<AudioDeviceInfo> routed = audio.getAudioDevicesForAttributes(MEDIA);
            AudioDeviceInfo[] available = audio.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
            List<String> activeNames = new ArrayList<>();
            if (routed != null) for (AudioDeviceInfo device : routed) {
                if (device != null) activeNames.add(name(device));
            }
            List<String> availableNames = new ArrayList<>();
            if (available != null) for (AudioDeviceInfo device : available) {
                if (device != null && isMediaCandidate(device)) availableNames.add(name(device));
            }
            row.setSummary("Media: " + (activeNames.isEmpty() ? "не определён" : String.join(", ", activeNames))
                    + "\nДоступны: " + (availableNames.isEmpty() ? "нет данных" : String.join(", ", availableNames))
                    + "\nНажмите для обновления. Переключение пока не разрешено.");
        } catch (RuntimeException | LinkageError error) {
            Log.w(TAG, "Media output readout unavailable", error);
            row.setSummary("Аудиомаршрут недоступен для чтения");
        }
    }

    static boolean speakerOnly(AudioManager audio) {
        if (audio == null) return false;
        try {
            List<AudioDeviceInfo> routed = audio.getAudioDevicesForAttributes(MEDIA);
            return routed != null && routed.size() == 1 && routed.get(0) != null
                    && routed.get(0).getType() == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER;
        } catch (RuntimeException | LinkageError unavailable) {
            return false;
        }
    }

    private static boolean isMediaCandidate(AudioDeviceInfo device) {
        switch (device.getType()) {
            case AudioDeviceInfo.TYPE_BUILTIN_SPEAKER:
            case AudioDeviceInfo.TYPE_BLUETOOTH_A2DP:
            case AudioDeviceInfo.TYPE_HDMI_ARC:
            case AudioDeviceInfo.TYPE_HDMI_EARC:
                return true;
            default:
                return false;
        }
    }

    static String label(int type, String productName) {
        if (type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) return "Встроенные динамики";
        if (type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)
            return productName == null || productName.trim().isEmpty()
                    ? "Bluetooth A2DP" : productName;
        if (type == AudioDeviceInfo.TYPE_HDMI_ARC) return "HDMI ARC";
        if (type == AudioDeviceInfo.TYPE_HDMI_EARC) return "HDMI eARC";
        return "Аудиоустройство (" + type + ")";
    }

    private static String name(AudioDeviceInfo device) {
        CharSequence product = device.getProductName();
        return label(device.getType(), product == null ? null : product.toString());
    }
}

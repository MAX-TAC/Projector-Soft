package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Android 13 system-UID-only AudioProductStrategy MEDIA routing.
 *
 * No setCommunicationDevice(), setBluetoothA2dpOn(), hidden AudioSystem forceUse,
 * Settings.Global sound_speaker_status writes, or Bluetooth disconnect.
 * Fail closed if any system method, permission, or exact strategy is unavailable.
 */
final class NativeMediaRouteBridge implements NativeMediaRouteTransaction.Port {
    static final String PERMISSION = "android.permission.MODIFY_AUDIO_ROUTING";
    static final AudioAttributes MEDIA =
            new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build();

    static final class Output {
        final int id;
        final int type;
        final String address;
        final String label;

        Output(int id, int type, String address, String label) {
            this.id = id;
            this.type = type;
            this.address = address == null ? "" : address;
            this.label = label;
        }

        boolean same(Output other) {
            return other != null && type == other.type
                    && (type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                    || address.equals(other.address));
        }
    }

    private final Context context;
    private final AudioManager audio;
    private final Object mediaStrategy;
    private final Class<?> strategyClass;
    private final Class<?> deviceAttributesClass;
    private final Method getPreferred;
    private final Method setPreferred;
    private final Method removePreferred;
    private final Constructor<?> deviceAttributesConstructor;
    private final Method preferredType;
    private final Method preferredAddress;

    private NativeMediaRouteBridge(Context context, AudioManager audio, Object strategy,
                                   Class<?> strategyClass, Class<?> deviceAttributesClass)
            throws Exception {
        this.context = context;
        this.audio = audio;
        this.mediaStrategy = strategy;
        this.strategyClass = strategyClass;
        this.deviceAttributesClass = deviceAttributesClass;
        getPreferred = AudioManager.class.getMethod("getPreferredDevicesForStrategy", strategyClass);
        setPreferred = AudioManager.class.getMethod("setPreferredDevicesForStrategy",
                strategyClass, List.class);
        removePreferred = AudioManager.class.getMethod("removePreferredDeviceForStrategy",
                strategyClass);
        deviceAttributesConstructor = deviceAttributesClass.getConstructor(AudioDeviceInfo.class);
        preferredType = deviceAttributesClass.getMethod("getType");
        preferredAddress = deviceAttributesClass.getMethod("getAddress");
    }

    static NativeMediaRouteBridge open(Context context) throws Exception {
        if (Build.VERSION.SDK_INT < 33 || !"8054".equals(OemHdmiPortInfo.machineId()))
            throw new IllegalStateException("Unsupported firmware");
        if (context.checkSelfPermission(PERMISSION) != PackageManager.PERMISSION_GRANTED)
            throw new SecurityException("MODIFY_AUDIO_ROUTING not granted to this APK");
        AudioManager audio = context.getSystemService(AudioManager.class);
        if (audio == null) throw new IllegalStateException("AudioManager unavailable");

        Class<?> productStrategy = Class.forName("android.media.audiopolicy.AudioProductStrategy");
        Class<?> attributes = Class.forName("android.media.AudioDeviceAttributes");
        Method strategies = productStrategy.getMethod("getAudioProductStrategies");
        Method supports = productStrategy.getMethod("supportsAudioAttributes", AudioAttributes.class);
        Method legacy = productStrategy.getMethod("getAudioAttributesForLegacyStreamType", int.class);
        Object found = null;
        int matches = 0;
        Object value = strategies.invoke(null);
        if (!(value instanceof List)) throw new IllegalStateException("Product strategies unavailable");
        for (Object candidate : (List<?>) value) {
            if (candidate == null) continue;
            if (Boolean.TRUE.equals(supports.invoke(candidate, MEDIA))
                    && legacy.invoke(candidate, AudioManager.STREAM_MUSIC) instanceof AudioAttributes) {
                found = candidate;
                matches++;
            }
        }
        if (matches != 1 || found == null)
            throw new IllegalStateException("Cannot uniquely identify MEDIA strategy: " + matches);
        return new NativeMediaRouteBridge(context, audio, found, productStrategy, attributes);
    }

    private void requirePermission() {
        if (context.checkSelfPermission(PERMISSION) != PackageManager.PERMISSION_GRANTED)
            throw new SecurityException("MODIFY_AUDIO_ROUTING permission changed");
    }

    List<Output> outputs() {
        List<Output> outputs = new ArrayList<>();
        for (AudioDeviceInfo d : audio.getDevices(AudioManager.GET_DEVICES_OUTPUTS)) {
            if (d == null || !d.isSink() || !isCandidate(d.getType())) continue;
            CharSequence name = d.getProductName();
            outputs.add(new Output(d.getId(), d.getType(), d.getAddress(),
                    NativeAudioOutputReadout.label(d.getType(),
                            name == null ? null : name.toString())));
        }
        return outputs;
    }

    static boolean isCandidate(int type) {
        // eARC is deliberately not a writable target in the first device test.
        return type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                || type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP;
    }

    @Override public Output findConnected(Output selected) {
        if (selected == null) return null;
        for (Output output : outputs()) {
            // Port ID is ephemeral; while switching require the same connected port.
            if (output.id == selected.id && output.same(selected)) return output;
        }
        return null;
    }

    @Override public List<Object> preferred() throws Exception {
        requirePermission();
        Object value = getPreferred.invoke(audio, mediaStrategy);
        if (!(value instanceof List))
            throw new IllegalStateException("Preferred MEDIA list unavailable");
        return new ArrayList<>((List<?>) value);
    }

    @Override public boolean preferredIs(Output target) throws Exception {
        List<Object> list = preferred();
        return list.size() == 1 && sameAttributes(list.get(0), target);
    }

    @Override public boolean preferredIsAutomatic() throws Exception {
        return preferred().isEmpty();
    }

    @Override public boolean routeIs(Output target) {
        if (target == null) return false;
        List<AudioDeviceInfo> routed = audio.getAudioDevicesForAttributes(MEDIA);
        if (routed == null || routed.size() != 1 || routed.get(0) == null) return false;
        AudioDeviceInfo current = routed.get(0);
        return current.isSink()
                && current.getType() == target.type
                && (target.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                        || Objects.equals(current.getAddress(), target.address));
    }

    @Override public boolean stillConnected(List<Output> initiallyConnectedBluetooth) {
        List<Output> now = outputs();
        for (Output bt : initiallyConnectedBluetooth) {
            boolean present = false;
            for (Output d : now) {
                if (bt.id == d.id && bt.same(d)) { present = true; break; }
            }
            if (!present) return false;
        }
        return true;
    }

    @Override public List<Output> connectedBluetooth() {
        List<Output> result = new ArrayList<>();
        for (Output d : outputs())
            if (d.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP) result.add(d);
        return result;
    }

    @Override public boolean apply(Output selected) throws Exception {
        requirePermission();
        if (selected != null) {
            AudioDeviceInfo connected = null;
            for (AudioDeviceInfo d : audio.getDevices(AudioManager.GET_DEVICES_OUTPUTS)) {
                if (d != null && d.isSink() && d.getId() == selected.id
                        && d.getType() == selected.type
                        && (selected.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                                || Objects.equals(d.getAddress(), selected.address))) {
                    connected = d;
                    break;
                }
            }
            if (connected == null) return false;
            Object attributes = deviceAttributesConstructor.newInstance(connected);
            return Boolean.TRUE.equals(setPreferred.invoke(audio, mediaStrategy,
                    Collections.singletonList(attributes)));
        }
        // Automatic routing: no preferred override at all.
        return Boolean.TRUE.equals(removePreferred.invoke(audio, mediaStrategy));
    }

    @Override public boolean restore(List<Object> original) throws Exception {
        requirePermission();
        if (original == null) throw new IllegalArgumentException("Original preference not captured");
        if (original.isEmpty()) {
            // Removing an already cleared preference can return false on some builds.
            if (preferred().isEmpty()) return true;
            removePreferred.invoke(audio, mediaStrategy);
            return preferred().isEmpty();
        }
        for (Object item : original)
            if (!deviceAttributesClass.isInstance(item)) return false;
        if (!Boolean.TRUE.equals(setPreferred.invoke(audio, mediaStrategy, original)))
            return false;
        return samePreferred(original, preferred());
    }

    boolean samePreferred(List<Object> first, List<Object> second) throws Exception {
        if (first.size() != second.size()) return false;
        for (int i = 0; i < first.size(); i++) {
            Object a = first.get(i), b = second.get(i);
            if (!Objects.equals(preferredType.invoke(a), preferredType.invoke(b))
                    || !Objects.equals(preferredAddress.invoke(a), preferredAddress.invoke(b)))
                return false;
        }
        return true;
    }

    private boolean sameAttributes(Object attributes, Output target) throws Exception {
        return target != null
                && ((Integer) preferredType.invoke(attributes)).intValue() == target.type
                && (target.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                    || Objects.equals(preferredAddress.invoke(attributes), target.address));
    }
}

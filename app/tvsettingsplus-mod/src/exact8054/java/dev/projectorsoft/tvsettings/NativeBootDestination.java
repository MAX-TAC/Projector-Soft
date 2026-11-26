package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.util.Log;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceScreen;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

/** A single confirmed destination for the shared OEM persist.trd.source_id property. */
final class NativeBootDestination {
    private static final String TAG = "TvSettingsPlusMOD";
    static final String KEY = "projectorsoft_boot_destination";
    enum Result { CONFIRMED, STALE, UNSUPPORTED, UNCONFIRMED }

    private NativeBootDestination() {}

    /** Human label for a package, falling back to the package id. */
    private static String appLabel(android.content.Context context, String pkg) {
        try {
            android.content.pm.PackageManager pm = context.getPackageManager();
            CharSequence label = pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0));
            if (label != null && label.length() > 0) return label.toString();
        } catch (Throwable ignored) {
        }
        return pkg;
    }
    /** Current power-on source (package, HOME, or HW id). */
    static String bootSource() {
        try {
            Object value = Class.forName("android.os.SystemProperties")
                    .getMethod("get", String.class, String.class)
                    .invoke(null, "persist.trd.source_id", "");
            return value == null ? "" : value.toString();
        } catch (Throwable unavailable) {
            Log.w(TAG, "boot source unreadable", unavailable);
            return "";
        }
    }

    /** Stores the chosen power-on source. */
    static boolean setBootSource(String value) {
        try {
            Class.forName("android.os.SystemProperties")
                    .getMethod("set", String.class, String.class)
                    .invoke(null, "persist.trd.source_id", value);
            return true;
        } catch (Throwable unavailable) {
            Log.w(TAG, "boot source not stored", unavailable);
            return false;
        }
    }
    private static Map<String, String> choices(Context context) throws Exception {
        if (!"8054".equals(OemHdmiPortInfo.machineId())) return Collections.emptyMap();
        Map<String, String> result = new LinkedHashMap<>();
        result.put("HOME", "Android TV");
        Map<String, String> ports = OemHdmiPortInfo.bootPorts(context);
        if (ports.isEmpty()) {
            // The OEM Build fields are not always populated, and the owner
            // asked to pick a boot source even with nothing plugged in, so
            // fall back to the declared port list from the system property.
            ports = declaredPorts();
        }
        result.putAll(ports);
        // Applications get their own screen with icons, so here only the
        // entry point to that picker is offered.
        result.put("projectorsoft_boot_app", "Приложение…");
        return result;
    }

    /** Reads ro.rjpj.hdmi.ports ("HW6:HDMI1,HW7:HDMI2") into id->label. */
    private static Map<String, String> declaredPorts() {
        Map<String, String> result = new LinkedHashMap<>();
        try {
            String raw = (String) Class.forName("android.os.SystemProperties")
                    .getMethod("get", String.class, String.class)
                    .invoke(null, "ro.rjpj.hdmi.ports", "");
            if (raw != null) for (String part : raw.split(",")) {
                String[] pair = part.trim().split(":");
                if (pair.length == 2 && !pair[0].isEmpty())
                    result.put(pair[0], pair[1]);
            }
        } catch (Throwable error) {
            Log.w(TAG, "declared HDMI ports unreadable", error);
        }
        return result;
    }

    static Result apply(OemPowerOnAppPolicy.Property backend, Map<String, String> displayedChoices,
                        Map<String, String> freshChoices, String before, String requested) throws Exception {
        if (!displayedChoices.containsKey(requested) || !freshChoices.containsKey(requested))
            return Result.UNSUPPORTED;
        String current = backend.read();
        if (before == null || !before.equals(current)) return Result.STALE;
        if (!freshChoices.containsKey(before)) return Result.UNSUPPORTED;
        if (requested.equals(current)) return Result.CONFIRMED;
        boolean completed = backend.write(requested);
        return completed && requested.equals(backend.read()) ? Result.CONFIRMED : Result.UNCONFIRMED;
    }

    static void install(Context context, PreferenceScreen screen) throws Exception {
        if (!"8054".equals(OemHdmiPortInfo.machineId())) return;
        Row row = new Row(context, OemPowerOnAppHook.property(), () -> choices(context));
        screen.addPreference(row);
        row.refresh();
    }

    static void refresh(PreferenceScreen screen) {
        Row row = screen.findPreference(KEY);
        if (row != null) row.refresh();
    }

    static final class Row extends ListPreference {
        private final OemPowerOnAppPolicy.Property backend;
        private final Callable<Map<String, String>> loadChoices;
        private Map<String, String> offered = Collections.emptyMap();
        private Map<String, String> dialogChoices = Collections.emptyMap();
        private String dialogBaseline;

        Row(Context context, OemPowerOnAppPolicy.Property backend, Callable<Map<String, String>> loadChoices) {
            super(context);
            this.backend = backend;
            this.loadChoices = loadChoices;
            setKey(KEY);
            setTitle("Источник при включении");
            setOrder(190);
            setPersistent(false);
            setOnPreferenceChangeListener((item, value) -> change(value));
        }

        @Override protected void onClick() {
            dialogChoices = offered;
            dialogBaseline = getValue();
            super.onClick();
        }

        void refresh() {
            try {
                render(loadChoices.call());
            } catch (Exception e) {
                unavailable("Не удалось прочитать источник включения");
                Log.w(TAG, "Cannot read boot destination", e);
            }
        }

        private boolean change(Object value) {
            Map<String, String> displayed = dialogChoices;
            String before = dialogBaseline;
            dialogChoices = Collections.emptyMap();
            dialogBaseline = null;
            try {
                // "Приложение…" opens the icon picker instead of being stored.
                if ("projectorsoft_boot_app".equals(value)) {
                    try {
                        getContext().startActivity(new android.content.Intent(
                                getContext(), BootAppPickerSettingsActivity.class)
                                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK));
                    } catch (RuntimeException unavailable) {
                        Log.w(TAG, "boot app picker unavailable", unavailable);
                    }
                    render(loadChoices.call());
                    return false;
                }
                Map<String, String> fresh = loadChoices.call();
                if (value instanceof String && before != null && fresh.size() >= 2) {
                    try {
                        Result result = apply(backend, displayed, fresh, before, (String) value);
                        if (result != Result.CONFIRMED) Log.w(TAG, "Boot destination not applied: " + result);
                    } catch (Exception failedWrite) {
                        Log.w(TAG, "Boot destination change failed; reading actual state", failedWrite);
                    }
                }
                render(fresh);
            } catch (Exception e) {
                unavailable("Не удалось подтвердить сохранённое значение");
                Log.w(TAG, "Cannot confirm boot destination", e);
            }
            // The displayed value comes from the property readback.
            return false;
        }

        private void render(Map<String, String> fresh) throws Exception {
            offered = new LinkedHashMap<>(fresh);
            setEntries(offered.values().toArray(new CharSequence[0]));
            setEntryValues(offered.keySet().toArray(new CharSequence[0]));
            String current = backend.read();
            // An unset property used to disable the whole row, which is why the
            // owner could never open it. Show "not set" but keep it selectable.
            boolean known = current != null && !current.isEmpty()
                    && fresh.containsKey(current);
            if (known) {
                setValue(current);
                setSummary(offered.get(current).replace("%", "%%"));
            } else if (current != null && current.contains(".")) {
                // A stored package is a chosen application: show its label.
                setValue(null);
                setSummary("Приложение · " + appLabel(getContext(), current));
            } else {
                setSummary("Не задан · сейчас Android TV");
            }
            setEnabled(fresh.size() >= 2);
        }

        private void unavailable(String reason) {
            setValue(null);
            setSummary(reason);
            setEnabled(false);
        }
    }
}

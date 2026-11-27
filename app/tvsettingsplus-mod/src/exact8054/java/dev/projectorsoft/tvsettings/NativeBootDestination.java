package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.util.Log;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceScreen;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/** A single confirmed destination for the shared OEM persist.trd.source_id property. */
final class NativeBootDestination {
    private static final String TAG = "TvSettingsPlusMOD";
    static final String KEY = "projectorsoft_boot_destination";
    enum Result { CONFIRMED, STALE, UNSUPPORTED, UNCONFIRMED }

    private NativeBootDestination() {}

    private static Map<String, String> choices(Context context) throws Exception {
        if (!"8054".equals(OemHdmiPortInfo.machineId())) return Collections.emptyMap();
        Map<String, String> result = new LinkedHashMap<>();
        result.put("HOME", "Android TV");
        result.putAll(OemHdmiPortInfo.bootPorts(context));
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
            if (current == null || current.isEmpty()) {
                unavailable("Источник включения не задан. Изменение недоступно");
            } else if (!fresh.containsKey(current)) {
                unavailable("Сохранённый источник не совпадает с доступными вариантами");
            } else {
                setValue(current);
                setSummary(offered.get(current).replace("%", "%%"));
                setEnabled(fresh.size() >= 2);
            }
        }

        private void unavailable(String reason) {
            setValue(null);
            setSummary(reason);
            setEnabled(false);
        }
    }
}

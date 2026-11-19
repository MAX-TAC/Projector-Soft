package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

/** Resynchronizes the stored autofocus and movement-keystone states when the menu returns. */
final class OemProjectionSwitches implements View.OnAttachStateChangeListener,
        ViewTreeObserver.OnWindowFocusChangeListener {
    private static final String TAG = "TvSettingsPlusMOD";
    private static final String GROUP = "projectorsoft_placement_category";
    private static final WeakHashMap<Object, WeakReference<OemProjectionSwitches>> PANELS =
            new WeakHashMap<>();
    private final List<Row> rows = new ArrayList<>();
    private WeakReference<View> root = new WeakReference<>(null);
    private ViewTreeObserver observer;

    static void add(Context context, Object group, Class<?> pref, String key, String title,
                    int order, ProjectionToggleState.Backend backend) throws Exception {
        ProjectionToggleState state = new ProjectionToggleState(backend);
        if (state.refresh() == null) return;
        WeakReference<OemProjectionSwitches> previous = PANELS.get(group);
        OemProjectionSwitches panel = previous == null ? null : previous.get();
        if (panel == null) panel = new OemProjectionSwitches();
        if (panel.addRow(context, group, pref, key, title, order, state)) {
            PANELS.put(group, new WeakReference<>(panel));
        }
    }

    static void bind(Object fragment, View root) {
        if (fragment == null || !"8054".equals(OemHdmiPortInfo.machineId())) return;
        try {
            Object group = fragment.getClass().getMethod("findPreference", CharSequence.class)
                    .invoke(fragment, GROUP);
            WeakReference<OemProjectionSwitches> reference = PANELS.get(group);
            OemProjectionSwitches panel = reference == null ? null : reference.get();
            if (panel != null) panel.rebind(root);
        } catch (Exception error) {
            Log.w(TAG, "Projection switch view unavailable", error);
        }
    }

    private boolean addRow(Context context, Object group, Class<?> pref, String key, String title,
                           int order, ProjectionToggleState state) throws Exception {
        Class<?> type = Class.forName("androidx.preference.SwitchPreference");
        Object item = type.getConstructor(Context.class).newInstance(context);
        Row row = new Row(item, state, type.getMethod("setChecked", boolean.class),
                pref.getMethod("setEnabled", boolean.class), pref.getMethod("setSummary", CharSequence.class));
        pref.getMethod("setKey", String.class).invoke(item, key);
        pref.getMethod("setTitle", CharSequence.class).invoke(item, title);
        pref.getMethod("setPersistent", boolean.class).invoke(item, false);
        pref.getMethod("setOrder", int.class).invoke(item, order);
        row.render();
        Class<?> listener = Class.forName("androidx.preference.Preference$OnPreferenceChangeListener");
        Object callback = Proxy.newProxyInstance(listener.getClassLoader(), new Class<?>[]{listener},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "onPreferenceChange":
                            if (!(args[1] instanceof Boolean)) return false;
                            ProjectionToggleState.Result result = state.change((Boolean) args[1]);
                            try {
                                row.render();
                            } catch (ReflectiveOperationException error) {
                                Log.w(TAG, "Projection switch render failed", error);
                            }
                            for (Row other : rows) {
                                if (other != row) refresh(other);
                            }
                            if (result != ProjectionToggleState.Result.CONFIRMED) {
                                String message = "Изменение не подтверждено";
                                if (result == ProjectionToggleState.Result.STALE) {
                                    message = "Настройка уже изменилась. Проверьте новое значение";
                                } else if (result == ProjectionToggleState.Result.OEM_REQUIRED) {
                                    message = "Для выключения откройте штатные настройки проектора";
                                }
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                            }
                            // This preference is non-persistent: only the OEM callback writes.
                            return result == ProjectionToggleState.Result.CONFIRMED;
                        case "hashCode": return System.identityHashCode(proxy);
                        case "equals": return proxy == args[0];
                        case "toString": return key;
                        default: return null;
                    }
                });
        pref.getMethod("setOnPreferenceChangeListener", listener).invoke(item, callback);
        if (!Boolean.TRUE.equals(group.getClass().getMethod("addPreference", pref).invoke(group, item))) {
            return false;
        }
        rows.add(row);
        return true;
    }

    private void rebind(View view) {
        View previous = root.get();
        if (view == previous) return;
        if (previous != null) {
            onViewDetachedFromWindow(previous);
            previous.removeOnAttachStateChangeListener(this);
        }
        root = new WeakReference<>(view);
        if (view == null) return;
        view.addOnAttachStateChangeListener(this);
        if (view.isAttachedToWindow()) onViewAttachedToWindow(view);
    }

    @Override public void onViewAttachedToWindow(View view) {
        if (view != root.get() || observer != null) return;
        observer = view.getViewTreeObserver();
        observer.addOnWindowFocusChangeListener(this);
        refresh();
    }

    @Override public void onViewDetachedFromWindow(View view) {
        if (view != root.get() || observer == null) return;
        if (observer.isAlive()) observer.removeOnWindowFocusChangeListener(this);
        else if (view.getViewTreeObserver().isAlive()) {
            view.getViewTreeObserver().removeOnWindowFocusChangeListener(this);
        }
        observer = null;
    }

    @Override public void onWindowFocusChanged(boolean focused) {
        if (focused && observer != null) refresh();
    }

    private void refresh() {
        for (Row row : rows) refresh(row);
    }

    private static void refresh(Row row) {
        row.state.refresh();
        try {
            row.render();
        } catch (ReflectiveOperationException error) {
            Log.w(TAG, "Projection switch refresh failed", error);
        }
    }

    private static final class Row {
        private final Object item;
        private final ProjectionToggleState state;
        private final Method checked;
        private final Method enabled;
        private final Method summary;

        Row(Object item, ProjectionToggleState state, Method checked, Method enabled, Method summary) {
            this.item = item;
            this.state = state;
            this.checked = checked;
            this.enabled = enabled;
            this.summary = summary;
        }

        void render() throws ReflectiveOperationException {
            Boolean value = state.value();
            enabled.invoke(item, value != null);
            summary.invoke(item, value == null ? "Текущее значение недоступно" : null);
            if (value != null) checked.invoke(item, value);
        }
    }
}

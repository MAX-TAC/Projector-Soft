package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import java.lang.reflect.Method;
import java.util.WeakHashMap;

/** Presentation of the existing Settings-owned panel configuration. */
final class OemPanelInfoHook {
    private static final String TAG = "TvSettingsPlusMOD";
    private static final String KEY = "projectorsoft_panel_configuration";
    private static final String[] FIELDS = {"type", "version", "width", "height"};
    private static final String[] TITLES = {
            "Тип панели", "Версия панели", "Ширина панели по конфигурации", "Высота панели по конфигурации"
    };
    private static final WeakHashMap<View, Boolean> BOUND = new WeakHashMap<>();

    private OemPanelInfoHook() {}

    static void install(Context context, Object group, Class<?> pref) {
        if (!"8054".equals(OemHdmiPortInfo.machineId())) return;
        try {
            Class.forName("dev.projectorsoft.tvsettings.Exact8054PanelCache", false, context.getClassLoader());
            Class<?> category = Class.forName("androidx.preference.PreferenceCategory");
            Class<?> preferenceGroup = Class.forName("androidx.preference.PreferenceGroup");
            Method add = preferenceGroup.getMethod("addPreference", pref);
            Object panel = category.getConstructor(Context.class).newInstance(context);
            pref.getMethod("setTitle", CharSequence.class).invoke(panel, "Информация о панели");
            pref.getMethod("setKey", String.class).invoke(panel, KEY);
            pref.getMethod("setOrder", int.class).invoke(panel, 290);
            Method visible = pref.getMethod("setVisible", boolean.class);
            visible.invoke(panel, false);
            for (int i = 0; i < FIELDS.length; i++) {
                Object item = pref.getConstructor(Context.class).newInstance(context);
                pref.getMethod("setTitle", CharSequence.class).invoke(item, TITLES[i]);
                pref.getMethod("setKey", String.class).invoke(item, KEY + "_" + FIELDS[i]);
                pref.getMethod("setSelectable", boolean.class).invoke(item, false);
                visible.invoke(item, false);
                add.invoke(panel, item);
            }
            add.invoke(group, panel);
        } catch (Exception | LinkageError unavailable) {
            Log.d(TAG, "Panel configuration unavailable");
        }
    }

    static void bind(Object fragment, View root) {
        if (root == null || BOUND.containsKey(root) || !"8054".equals(OemHdmiPortInfo.machineId())) return;
        try {
            Method find = fragment.getClass().getMethod("findPreference", CharSequence.class);
            Object panel = find.invoke(fragment, KEY);
            if (panel == null) return;
            Object[] rows = new Object[FIELDS.length];
            for (int i = 0; i < FIELDS.length; i++) {
                rows[i] = find.invoke(fragment, KEY + "_" + FIELDS[i]);
                if (rows[i] == null) return;
            }
            Class<?> pref = Class.forName("androidx.preference.Preference");
            Binding binding = new Binding(root.getContext().getClassLoader(), panel, rows,
                    pref.getMethod("setVisible", boolean.class), pref.getMethod("setSummary", CharSequence.class));
            BOUND.put(root, true);
            root.addOnAttachStateChangeListener(binding);
            if (root.isAttachedToWindow()) binding.onViewAttachedToWindow(root);
        } catch (Exception | LinkageError unavailable) {
            Log.d(TAG, "Panel configuration view unavailable");
        }
    }

    private static final class Binding implements View.OnAttachStateChangeListener,
            ViewTreeObserver.OnWindowFocusChangeListener {
        private final ClassLoader loader;
        private final Object panel;
        private final Object[] rows;
        private final Method visible;
        private final Method summary;
        private ViewTreeObserver observer;

        Binding(ClassLoader loader, Object panel, Object[] rows, Method visible, Method summary) {
            this.loader = loader;
            this.panel = panel;
            this.rows = rows;
            this.visible = visible;
            this.summary = summary;
        }

        @Override public void onViewAttachedToWindow(View view) {
            if (observer != null) return;
            observer = view.getViewTreeObserver();
            observer.addOnWindowFocusChangeListener(this);
            refresh();
        }

        @Override public void onViewDetachedFromWindow(View view) {
            if (observer == null) return;
            if (observer.isAlive()) observer.removeOnWindowFocusChangeListener(this);
            observer = null;
        }

        @Override public void onWindowFocusChanged(boolean focused) {
            if (focused && observer != null) refresh();
        }

        private void refresh() {
            try {
                Class<?> exact = Class.forName("dev.projectorsoft.tvsettings.Exact8054PanelCache", false, loader);
                String[] values = PanelCacheReadout.read(OemHdmiPortInfo.machineId(), loader,
                        (String) exact.getField("MANAGER").get(null),
                        (String) exact.getField("SINGLETON").get(null),
                        (String) exact.getField("DISPLAY").get(null));
                boolean any = false;
                for (int i = 0; i < rows.length; i++) {
                    summary.invoke(rows[i], values[i]);
                    visible.invoke(rows[i], values[i] != null);
                    any |= values[i] != null;
                }
                visible.invoke(panel, any);
            } catch (Exception | LinkageError unavailable) {
                Log.d(TAG, "Panel configuration refresh unavailable");
            }
        }
    }
}

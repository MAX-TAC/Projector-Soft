package dev.projectorsoft.tvsettings;

/** One HDMI subscription for the currently attached menu view. Main thread only. */
abstract class HdmiViewLifecycle<V> {
    private V view;
    private boolean subscribed;

    final void bind(V next) {
        if (view == next) return;
        if (view != null) {
            detached(view);
            removeListener(view);
        }
        view = next;
        if (next != null) {
            addListener(next);
            if (isAttached(next)) attached(next);
        }
    }

    final void attached(V candidate) {
        if (candidate != view || subscribed || !isAttached(candidate)) return;
        subscribed = subscribe();
        refresh();
    }

    final void detached(V candidate) {
        if (candidate != view) return;
        if (subscribed) {
            subscribed = false;
            unsubscribe();
        }
    }

    final void inputChanged() {
        if (view != null && subscribed && isAttached(view)) refresh();
    }

    abstract boolean isAttached(V candidate);
    abstract void addListener(V candidate);
    abstract void removeListener(V candidate);
    abstract boolean subscribe();
    abstract void unsubscribe();
    abstract void refresh();
}

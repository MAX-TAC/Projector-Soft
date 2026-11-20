package dev.projectorsoft.tvsettings;

/** One HDMI subscription for the currently attached menu view. Main thread only. */
abstract class HdmiViewLifecycle<V> {
    private V view;
    private boolean active;
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
        if (candidate != view || active || !isAttached(candidate)) return;
        active = true;
        addFocusListener(candidate);
        subscribed = subscribe();
        refresh();
    }

    final void detached(V candidate) {
        if (candidate != view || !active) return;
        active = false;
        removeFocusListener(candidate);
        if (subscribed) {
            subscribed = false;
            unsubscribe();
        }
    }

    final void inputChanged() {
        if (view != null && subscribed && isAttached(view)) refresh();
    }

    final void windowFocused() {
        if (active && isAttached(view)) refresh();
    }

    abstract void addFocusListener(V candidate);
    abstract void removeFocusListener(V candidate);
    abstract boolean isAttached(V candidate);
    abstract void addListener(V candidate);
    abstract void removeListener(V candidate);
    abstract boolean subscribe();
    abstract void unsubscribe();
    abstract void refresh();
}

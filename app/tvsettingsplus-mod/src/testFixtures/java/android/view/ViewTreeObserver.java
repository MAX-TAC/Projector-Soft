package android.view;

import java.util.ArrayList;
import java.util.List;

public final class ViewTreeObserver {
    public interface OnWindowFocusChangeListener {void onWindowFocusChanged(boolean focused);}
    private final List<OnWindowFocusChangeListener> listeners=new ArrayList<>();
    public boolean isAlive() {return true;}
    public void addOnWindowFocusChangeListener(OnWindowFocusChangeListener listener) {listeners.add(listener);}
    public void removeOnWindowFocusChangeListener(OnWindowFocusChangeListener listener) {listeners.remove(listener);}
    public int count() {return listeners.size();}
    public void focus() {
        for(OnWindowFocusChangeListener listener:new ArrayList<>(listeners))listener.onWindowFocusChanged(true);
    }
}

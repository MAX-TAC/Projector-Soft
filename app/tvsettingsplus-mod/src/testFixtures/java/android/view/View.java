package android.view;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

public final class View {
    public interface OnAttachStateChangeListener {
        void onViewAttachedToWindow(View view);
        void onViewDetachedFromWindow(View view);
    }

    private final Context context;
    private final ViewTreeObserver tree=new ViewTreeObserver();
    private final List<OnAttachStateChangeListener> listeners=new ArrayList<>();
    private boolean attached;

    public View(Context context) {this.context=context;}
    public Context getContext() {return context;}
    public ViewTreeObserver getViewTreeObserver() {return tree;}
    public boolean isAttachedToWindow() {return attached;}
    public void addOnAttachStateChangeListener(OnAttachStateChangeListener listener) {listeners.add(listener);}
    public void removeOnAttachStateChangeListener(OnAttachStateChangeListener listener) {listeners.remove(listener);}
    public int count() {return listeners.size();}

    public void setAttached(boolean attached) {
        this.attached=attached;
        for(OnAttachStateChangeListener listener:new ArrayList<>(listeners)){
            if(attached)listener.onViewAttachedToWindow(this);
            else listener.onViewDetachedFromWindow(this);
        }
    }
}

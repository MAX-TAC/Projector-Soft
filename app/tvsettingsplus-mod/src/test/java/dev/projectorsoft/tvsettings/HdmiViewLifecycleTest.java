package dev.projectorsoft.tvsettings;

public final class HdmiViewLifecycleTest {
    private static final class View {
        boolean attached;
        boolean listening;
        boolean focusListening;
        View(boolean attached){this.attached=attached;}
    }

    private static final class Observer extends HdmiViewLifecycle<View> {
        int subscriptions;
        int attempts;
        boolean reject;
        int unsubscriptions;
        int refreshes;
        boolean active;

        @Override boolean isAttached(View view){return view.attached;}
        @Override void addListener(View view){
            require(!view.listening,"duplicate view listener");
            view.listening=true;
        }
        @Override void removeListener(View view){
            require(view.listening,"missing view listener");
            view.listening=false;
        }
        @Override void addFocusListener(View view){
            require(!view.focusListening,"duplicate window focus listener");
            view.focusListening=true;
        }
        @Override void removeFocusListener(View view){
            require(view.focusListening,"missing window focus listener");
            view.focusListening=false;
        }
        @Override boolean subscribe(){
            attempts++;
            if(reject)return false;
            require(!active,"duplicate TV callback");
            active=true;
            subscriptions++;
            return true;
        }
        @Override void unsubscribe(){
            require(active,"duplicate TV unsubscribe");
            active=false;
            unsubscriptions++;
        }
        @Override void refresh(){refreshes++;}
    }

    private static void require(boolean value,String message) {
        if(!value)throw new AssertionError(message);
    }

    private static void recreateViewWithExistingRows() {
        Observer observer=new Observer();
        View first=new View(true);
        observer.bind(first);
        observer.bind(first);
        observer.attached(first);
        require(observer.subscriptions==1,"same view registered twice");

        first.attached=false;
        observer.detached(first);
        observer.detached(first);
        observer.inputChanged();
        require(observer.unsubscriptions==1 && observer.refreshes==1,"detached view receives updates");
        View replacement=new View(false);
        observer.bind(replacement);
        require(!first.listening && replacement.listening && !observer.active,"replacement not staged");
        replacement.attached=true;
        observer.attached(replacement);
        observer.inputChanged();
        require(observer.subscriptions==2 && observer.refreshes==3,"replacement missed HDMI changes");
    }

    private static void replaceBeforeOldDetachAndReattach() {
        Observer observer=new Observer();
        View first=new View(true);
        View replacement=new View(true);
        observer.bind(first);
        observer.bind(replacement);
        observer.detached(first);
        require(observer.active && observer.subscriptions==2 && observer.unsubscriptions==1,
                "old destroy disturbed replacement subscription");
        replacement.attached=false;
        observer.detached(replacement);
        replacement.attached=true;
        observer.attached(replacement);
        require(observer.active && observer.subscriptions==3 && observer.unsubscriptions==2,
                "reattach did not subscribe");
        observer.bind(null);
        require(!observer.active && !replacement.listening && observer.unsubscriptions==3,
                "final release left a listener or subscription");
    }

    private static void refreshOnFocusWhenTvCallbacksUnavailable() {
        Observer observer=new Observer();
        observer.reject=true;
        View first=new View(true);
        observer.bind(first);
        observer.attached(first);
        require(observer.attempts==1 && first.focusListening,"repeated attach after rejected callback");
        observer.inputChanged();
        observer.windowFocused();
        require(observer.refreshes==2,"window focus must refresh when TV callback was rejected");
        observer.bind(null);
        observer.windowFocused();
        require(!first.focusListening && observer.unsubscriptions==0 && observer.refreshes==2,
                "detached focus listener leaked after a rejected callback");
    }

    public static void main(String[] args) {
        recreateViewWithExistingRows();
        replaceBeforeOldDetachAndReattach();
        refreshOnFocusWhenTvCallbacksUnavailable();
        System.out.println("PASS HDMI lifecycle: recreate, replace, reattach, and unsubscribe without duplicate callbacks");
    }
}

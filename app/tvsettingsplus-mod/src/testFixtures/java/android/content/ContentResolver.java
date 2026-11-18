package android.content;

import android.database.ContentObserver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ContentResolver {
    public final Map<String,List<ContentObserver>> observers=new HashMap<>();
    public String rejected;
    public int registrations;
    public void registerContentObserver(String uri,boolean descendants,ContentObserver observer) {
        if(uri.equals(rejected))throw new SecurityException("observer rejected");
        observers.computeIfAbsent(uri, ignored->new ArrayList<>()).add(observer);
        registrations++;
    }

    public void unregisterContentObserver(ContentObserver observer) {
        for(List<ContentObserver> registered:observers.values())registered.remove(observer);
        observers.values().removeIf(List::isEmpty);
    }

    public void notifyChange(String key) {
        List<ContentObserver> listeners=observers.get(key);
        if(listeners!=null)for(ContentObserver observer:new ArrayList<>(listeners))observer.onChange(false);
    }
}

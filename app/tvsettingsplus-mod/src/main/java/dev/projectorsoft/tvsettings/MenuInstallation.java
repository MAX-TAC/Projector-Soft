package dev.projectorsoft.tvsettings;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

/** One main-thread installation per tree. Failed attempts undo only their own rows. */
final class MenuInstallation {
    private final WeakHashMap<Object,Boolean> screens=new WeakHashMap<>();

    Attempt begin(Object screen) {
        if(screens.containsKey(screen))return null;
        screens.put(screen,false);
        return new Attempt(screen);
    }

    final class Attempt implements AutoCloseable {
        private final Object screen;
        private final List<Runnable> undo=new ArrayList<>();
        private boolean committed;
        Attempt(Object screen){this.screen=screen;}
        void track(Runnable remove){undo.add(remove);}
        void commit(){committed=true;screens.put(screen,true);}
        @Override public void close(){
            if(committed)return;
            try {
                for(int i=undo.size()-1;i>=0;i--)undo.get(i).run();
            } finally {
                screens.remove(screen);
            }
        }
    }
}

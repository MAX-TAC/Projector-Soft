package dev.projectorsoft.tvsettings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

/** Main-thread transactions for independent sections in one preference tree. */
final class MenuInstallation {
    private final WeakHashMap<Object,Set<String>> screens=new WeakHashMap<>();

    Attempt begin(Object screen) {
        return begin(screen,"");
    }

    Attempt begin(Object screen,String key) {
        Set<String> sections=screens.computeIfAbsent(screen,ignored->new HashSet<>());
        return sections.add(key) ? new Attempt(screen,key) : null;
    }

    final class Attempt implements AutoCloseable {
        private final Object screen;
        private final String key;
        private final List<Runnable> undo=new ArrayList<>();
        private boolean committed;
        Attempt(Object screen,String key){this.screen=screen;this.key=key;}
        void track(Runnable remove){undo.add(remove);}
        void commit(){committed=true;}
        @Override public void close(){
            if(committed)return;
            try {
                RuntimeException failure = null;
                for (int i = undo.size() - 1; i >= 0; i--) {
                    try { undo.get(i).run(); }
                    catch (RuntimeException error) {
                        if (failure == null) failure = error;
                    }
                }
                if (failure != null) throw failure;
            } finally {
                Set<String> sections=screens.get(screen);
                sections.remove(key);
                if(sections.isEmpty())screens.remove(screen);
            }
        }
    }
}

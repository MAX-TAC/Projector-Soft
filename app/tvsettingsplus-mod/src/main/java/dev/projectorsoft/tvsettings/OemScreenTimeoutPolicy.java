package dev.projectorsoft.tvsettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Projector screen safety policy based on verified OEM 8052 value arrays.
 * Not a thermal/fan controller: affects only Android inactivity and dream
 * settings. Both System and Secure sleep_timeout namespaces are handled
 * because the stock ScreensaverSecondaryController reads one and writes the
 * other.
 */
public final class OemScreenTimeoutPolicy {
    public enum Table { SYSTEM, SECURE }

    public interface Store {
        String read(Table table,String key) throws Exception;
        boolean write(Table table,String key,int value) throws Exception;
    }

    public static final int SCREEN_OFF_NEVER=Integer.MAX_VALUE;
    public static final int SLEEP_NEVER=-1;
    public static final int DREAM_DISABLED=0;

    public static final class Entry {
        public final Table table;
        public final String key;
        public final int expected;
        Entry(Table table,String key,int expected) {
            this.table=table;this.key=key;this.expected=expected;
        }
    }
    public static final class Result {
        public final int updated;
        public final int unchanged;
        public final List<String> failed;
        Result(int updated,int unchanged,List<String> failed) {
            this.updated=updated;
            this.unchanged=unchanged;
            this.failed=Collections.unmodifiableList(new ArrayList<>(failed));
        }
        public boolean isComplete() {return failed.isEmpty();}
    }
    private static final Entry[] ENTRIES={
        new Entry(Table.SYSTEM,"screen_off_timeout",SCREEN_OFF_NEVER),
        // This OEM reads sleep_timeout through Settings.System:
        new Entry(Table.SYSTEM,"sleep_timeout",SLEEP_NEVER),
        // ...but its setter uses Settings.Secure:
        new Entry(Table.SECURE,"sleep_timeout",SLEEP_NEVER),
        // Android DreamService screensaver switch.
        new Entry(Table.SECURE,"screensaver_enabled",DREAM_DISABLED)
    };
    private OemScreenTimeoutPolicy() {}

    public static Entry[] entries(){return ENTRIES.clone();}

    public static Result inspectOrApply(Store store,boolean apply) {
        List<String> failures=new ArrayList<>();
        int updated=0,unchanged=0;
        for(Entry spec:ENTRIES) {
            String name=spec.table+"."+spec.key;
            try {
                if(store==null)throw new IllegalStateException("No settings backend");
                String existing=store.read(spec.table,spec.key);
                String target=Integer.toString(spec.expected);
                if(target.equals(existing)){
                    unchanged++;
                    continue;
                }
                if(!apply || !store.write(spec.table,spec.key,spec.expected)
                        || !target.equals(store.read(spec.table,spec.key))) {
                    failures.add(name);
                }else {
                    updated++;
                }
            }catch(Exception error){
                failures.add(name);
            }
        }
        return new Result(updated,unchanged,failures);
    }
}

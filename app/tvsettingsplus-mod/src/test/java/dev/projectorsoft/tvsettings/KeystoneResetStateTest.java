package dev.projectorsoft.tvsettings;

import java.util.HashSet;
import java.util.Set;

public final class KeystoneResetStateTest {
    static final class Backend implements KeystoneResetState.Backend {
        boolean display = true;
        String refused;
        boolean refuseAll;
        int nativeCalls;
        Set<String> visited = new HashSet<>();
        public boolean resetDisplay() { nativeCalls++; return display; }
        public boolean clear(String property) {
            visited.add(property);
            return !refuseAll && !property.equals(refused);
        }
    }

    static void check(boolean condition) {
        if (!condition) throw new AssertionError();
    }

    public static void main(String[] args) {
        Backend backend = new Backend();
        check(KeystoneResetState.reset(backend) == KeystoneResetState.Result.COMPLETE);
        check(backend.nativeCalls == 1 && backend.visited.size() == 18);
        for (String key : backend.visited) {
            Backend partial = new Backend();
            partial.refused = key;
            check(KeystoneResetState.reset(partial) == KeystoneResetState.Result.PARTIAL);
            check(partial.visited.size() == 18);
        }
        backend.display = false;
        check(KeystoneResetState.reset(backend) == KeystoneResetState.Result.PARTIAL);
        backend.refuseAll = true;
        check(KeystoneResetState.reset(backend) == KeystoneResetState.Result.FAILED);
        backend.display = true;
        check(KeystoneResetState.reset(backend) == KeystoneResetState.Result.PARTIAL);
        System.out.println("PASS OEM reset, all 18 writes, every partial failure and failed display reset");
    }
}

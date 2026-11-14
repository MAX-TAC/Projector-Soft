package dev.projectorsoft.tvsettings;

import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

public final class OemScreenTimeoutPolicyTest {
    static final class Fake implements OemScreenTimeoutPolicy.Store {
        final Map<String,String> values=new HashMap<>();
        final Set<String> denied=new HashSet<>();
        final Set<String> ineffective=new HashSet<>();
        int writeCount;
        String id(OemScreenTimeoutPolicy.Table t,String k){return t+"."+k;}
        @Override public String read(OemScreenTimeoutPolicy.Table table,String key){
            return values.get(id(table,key));
        }
        @Override public boolean write(OemScreenTimeoutPolicy.Table table,String key,int value){
            String id=id(table,key);
            writeCount++;
            if(denied.contains(id))return false;
            if(!ineffective.contains(id))values.put(id,Integer.toString(value));
            return true;
        }
    }
    static void check(boolean ok,String message){
        if(!ok)throw new AssertionError(message);
    }
    public static void main(String[] args){
        OemScreenTimeoutPolicy.Entry[] entries=OemScreenTimeoutPolicy.entries();
        check(entries.length==4,"four exact Android keys");
        check(entries[0].expected==Integer.MAX_VALUE,"OEM Never for display");
        check(entries[1].expected==-1 && entries[2].expected==-1,"OEM Never sleep");
        check(entries[3].expected==0,"Dream disabled");
        check(entries[1].table==OemScreenTimeoutPolicy.Table.SYSTEM
           && entries[2].table==OemScreenTimeoutPolicy.Table.SECURE,"OEM mixed getter/setter");

        Fake fresh=new Fake();
        OemScreenTimeoutPolicy.Result dry=
            OemScreenTimeoutPolicy.inspectOrApply(fresh,false);
        check(!dry.isComplete() && dry.failed.size()==4 && fresh.writeCount==0,
              "missing settings are NOT treated as disabled");
        OemScreenTimeoutPolicy.Result applied=
            OemScreenTimeoutPolicy.inspectOrApply(fresh,true);
        check(applied.isComplete() && applied.updated==4 && fresh.writeCount==4,
              "initialize Never even when raw settings absent");
        check(fresh.values.get("SYSTEM.screen_off_timeout").equals("2147483647"),"screen on");
        check(fresh.values.get("SYSTEM.sleep_timeout").equals("-1"),"OEM read");
        check(fresh.values.get("SECURE.sleep_timeout").equals("-1"),"system sleep");
        check(fresh.values.get("SECURE.screensaver_enabled").equals("0"),"dream off");
        OemScreenTimeoutPolicy.Result second=
            OemScreenTimeoutPolicy.inspectOrApply(fresh,true);
        check(second.isComplete() && second.unchanged==4 && fresh.writeCount==4,
             "idempotent, no redundant writes");

        Fake blocked=new Fake();
        blocked.denied.add("SECURE.sleep_timeout");
        OemScreenTimeoutPolicy.Result partial=
            OemScreenTimeoutPolicy.inspectOrApply(blocked,true);
        check(!partial.isComplete() && partial.failed.size()==1 && partial.updated==3,
             "partial failure is reported, other keys still attempted");
        blocked.denied.clear();
        // The previous partial write already stored dream-disabled (0).
        // Reset it to 1 so the test actually exercises a rejected readback,
        // rather than testing the idempotent no-write path.
        blocked.values.put("SECURE.screensaver_enabled","1");
        blocked.ineffective.add("SECURE.screensaver_enabled");
        check(!OemScreenTimeoutPolicy.inspectOrApply(blocked,true).isComplete(),
              "no phantom success when provider ignores update");
        check(!OemScreenTimeoutPolicy.inspectOrApply(null,true).isComplete(),
              "null backend fails closed");
        // Policy must never write any brightness, power HAL, projector or fan properties.
        for(String k:fresh.values.keySet()){
            check(k.contains("timeout")||k.contains("screensaver_enabled"),
                  "unexpected write outside inactivity settings");
        }
        System.out.println("PASS: Never/Never/No Dream, OEM namespaces, readback and no thermal writes");
    }
}

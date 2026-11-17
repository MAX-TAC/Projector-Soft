package dev.projectorsoft.tvsettings;
import java.util.*;
public final class SoundPanelStateTest {
    static void check(boolean value){if(!value)throw new AssertionError();}
    public static void main(String[] args) {
        Map<String,String> provider=new HashMap<>();provider.put("sound_style","1");provider.put("sound_bass","50");
        SoundPanelState state=new SoundPanelState();check(state.refresh(provider::get));
        provider.put("sound_style","0");provider.put("sound_bass","70");check(state.refresh(provider::get));
        check("50".equals(state.initial().get("sound_bass"))&&"1".equals(state.initial().get("sound_style")));
        check("70".equals(state.current().get("sound_bass"))&&"0".equals(state.current().get("sound_style")));
        check(state.current().get("sound_treble")==null); // Absent state is never invented.
        int[] reads={0};check(!state.refresh(key->{if("sound_style".equals(key))return ++reads[0]==1?"0":"4";return provider.get(key);}));
        check("0".equals(state.current().get("sound_style"))); // Rejected refresh preserves last accepted UI snapshot.
        provider.remove("sound_style");check(!state.refresh(provider::get));
        provider.put("sound_style","bad");check(!state.refresh(provider::get));
        try {state.initial().put("sound_bass","99");throw new AssertionError();}catch(UnsupportedOperationException expected){}
        SoundPanelState reopened=new SoundPanelState();provider.put("sound_style","0");check(reopened.refresh(provider::get));
        check("70".equals(reopened.initial().get("sound_bass"))); // Clearly page-local, not durable rollback.
        System.out.println("PASS sound panel refresh, immutable baseline, mode transition, absent/malformed state and reopen");
    }
}

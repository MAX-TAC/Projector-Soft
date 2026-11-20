package dev.projectorsoft.tvsettings;
import java.util.*;

/** Page-local provider snapshots; never replays a DSP profile. */
public final class SoundPanelState {
    public interface Reader {String read(String key);}
    public static final String[] KEYS={"sound_style","sound_bass","sound_treble","sound_sound_surround",
        "sound_equalizer_detail_120hz","sound_equalizer_detail_500hz","sound_equalizer_detail_1500hz",
        "sound_equalizer_detail_5000hz","sound_equalizer_detail_10000hz"};
    private Map<String,String> initial,current;
    public boolean refresh(Reader reader) {
        String mode=reader.read("sound_style");
        if(mode==null||!mode.matches("[0-6]"))return false;
        Map<String,String> next=new LinkedHashMap<>();
        next.put("sound_style",mode);
        for(String key:KEYS)if(!"sound_style".equals(key))next.put(key,reader.read(key));
        if(!mode.equals(reader.read("sound_style")))return false;
        current=Collections.unmodifiableMap(next);
        if(initial==null)initial=current;
        return true;
    }
    public Map<String,String> initial(){return initial==null?Collections.emptyMap():initial;}
    public Map<String,String> current(){return current==null?Collections.emptyMap():current;}
}

package dev.projectorsoft.tvsettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Pure, testable source-to-property mapping from the factory
 * PowerOnSourceSecondaryController. Never expose HDMI ports beyond board count.
 */
public final class BootSourceMapping {
    public static final class Entry {
        public final String title;
        public final String id;
        public final String hdmiHardwarePort;
        Entry(String title,String id,String port) {
            this.title=title; this.id=id; this.hdmiHardwarePort=port;
        }
    }

    private BootSourceMapping() {}

    /**
     * The OEM defaults to HW5/HW6/HW7/HW8 if A_RJ_HDMI_PORTS is missing.
     * For an experimental MOD we deliberately fail closed when board metadata
     * is not precise. This prevents an imaginary second HDMI on 8052.
     */
    public static List<Entry> candidates(int count,String boardPorts,boolean hasAv) {
        if (count < 1 || count > 4 || boardPorts == null)
            return Collections.emptyList();
        String[] parts=boardPorts.split(",");
        if (parts.length < count)
            return Collections.emptyList();
        Set<String> seen=new HashSet<>();
        List<Entry> result=new ArrayList<>();
        result.add(new Entry("Android TV","HOME",null));
        if (hasAv) result.add(new Entry("AV","com.mediatek.tvinput/.composite.CompositeInputService/HW2",null));
        for(int i=0;i<count;i++) {
            String[] split=parts[i].trim().split(":",-1);
            if(split.length!=2 || !split[0].trim().matches("HW[0-9]+")
                    || split[1].trim().isEmpty() || !seen.add(split[0].trim()))
                return Collections.emptyList();
            result.add(new Entry(split[1].trim(),null,split[0].trim()));
        }
        result.add(new Entry("USB","USB",null));
        return Collections.unmodifiableList(result);
    }

    public static boolean isKnownState(List<Entry> entries,String stored) {
        if(stored==null)return false;
        for(Entry entry:entries) {
            if(stored.equals(entry.id))return true;
            if(entry.hdmiHardwarePort!=null && stored.endsWith("/"+entry.hdmiHardwarePort))
                return true;
        }
        return false;
    }
}

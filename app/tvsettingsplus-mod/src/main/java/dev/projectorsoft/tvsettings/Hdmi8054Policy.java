package dev.projectorsoft.tvsettings;
import java.util.*;

/** Board port identity and eligibility for the stock LiveTV launch. */
public final class Hdmi8054Policy {
    public static final class Port {
        public final String label,hardware,inputId;
        private Port(String label,String hardware,String inputId) {
            this.label=label;this.hardware=hardware;this.inputId=inputId;
        }
    }
    static final String LIVE_TV = "com.mediatek.wwtv.tvcenter";
    private Hdmi8054Policy() {}

    static boolean mayLaunch(String owner,boolean system,boolean enabled,boolean exported,boolean permission) {
        return LIVE_TV.equals(owner) && system && enabled && exported && permission;
    }

    public static boolean mayUseLegacyCec(String machine) {return "8052".equals(machine);}
    public static List<Port> ports(String machine,int count,String metadata,List<String> hdmiInputs) {
        if(!"8054".equals(machine)||count!=2||metadata==null||hdmiInputs==null)return Collections.emptyList();
        String[] entries=metadata.split(",",-1);
        Set<String> actual=new HashSet<>();
        for(String entry:entries)actual.add(entry.trim());
        if(entries.length!=2||actual.size()!=2||!actual.contains("HW6:HDMI1")||!actual.contains("HW7:HDMI2"))
            return Collections.emptyList();
        List<Port> result=new ArrayList<>();
        for(int index=0;index<2;index++) {
            String hardware="HW"+(6+index),found=null;
            int matches=0;
            for(String id:hdmiInputs) {
                if(id!=null&&(id.equals("com.mediatek.tvinput/.hdmi.HDMIInputService/"+hardware)
                    ||id.equals("com.mediatek.tvinput/com.mediatek.tvinput.hdmi.HDMIInputService/"+hardware))) {
                    found=id;matches++;
                }
            }
            result.add(new Port("HDMI "+(index+1),hardware,matches==1?found:null));
        }
        return Collections.unmodifiableList(result);
    }
}

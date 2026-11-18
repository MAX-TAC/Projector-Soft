package dev.projectorsoft.tvsettings;
import java.util.*;
public final class Hdmi8054PolicyTest {
    private static final String BASE="com.mediatek.tvinput/.hdmi.HDMIInputService/";
    static void check(boolean value){if(!value)throw new AssertionError();}
    public static void main(String[] args) {
        List<String> ids=Arrays.asList(BASE+"HW5",BASE+"HW6",BASE+"HW7",BASE+"HW8");
        List<Hdmi8054Policy.Port> ports=Hdmi8054Policy.ports("8054",2,"HW6:HDMI1,HW7:HDMI2",ids);
        check(ports.size()==2&&"HDMI 1".equals(ports.get(0).label)&&"HW6".equals(ports.get(0).hardware));
        check((BASE+"HW7").equals(ports.get(1).inputId)); // HW5/HW8 registrations do not imply physical ports.
        check(Hdmi8054Policy.ports("8054",4,"HW6:HDMI1,HW7:HDMI2",ids).isEmpty());
        check(Hdmi8054Policy.ports("8054",2,"HW6:HDMI2,HW7:HDMI1",ids).isEmpty());
        check(Hdmi8054Policy.ports("8054",2,"HW6:HDMI1,HW6:HDMI2",ids).isEmpty());
        check(Hdmi8054Policy.ports("8054",2,null,ids).isEmpty());
        check(Hdmi8054Policy.ports("8052",2,"HW6:HDMI1,HW7:HDMI2",ids).isEmpty());
        ports=Hdmi8054Policy.ports("8054",2,"HW7:HDMI2,HW6:HDMI1",Arrays.asList(BASE+"HW6"));
        check(ports.size()==2&&ports.get(1).inputId==null); // Missing HDMI2 is not guessed.
        ports=Hdmi8054Policy.ports("8054",2,"HW6:HDMI1,HW7:HDMI2",Arrays.asList("other/.Input/HW6",BASE+"HW7"));
        check(ports.get(0).inputId==null); // Suffix alone cannot prove the TV input owner.
        ports=Hdmi8054Policy.ports("8054",2,"HW6:HDMI1,HW7:HDMI2",Arrays.asList(BASE+"HW6",BASE+"HW6",BASE+"HW7"));
        check(ports.get(0).inputId==null); // Ambiguous duplicate registrations fail closed.
        check(!Hdmi8054Policy.mayUseLegacyCec("8054")&&!Hdmi8054Policy.mayUseLegacyCec(null)
            &&!Hdmi8054Policy.mayUseLegacyCec("")&&Hdmi8054Policy.mayUseLegacyCec("8052"));
        check(Hdmi8054Policy.mayLaunch(Hdmi8054Policy.LIVE_TV,true,true,true,true));
        check(!Hdmi8054Policy.mayLaunch("other.tv",true,true,true,true));
        check(!Hdmi8054Policy.mayLaunch(Hdmi8054Policy.LIVE_TV,false,true,true,true));
        check(!Hdmi8054Policy.mayLaunch(Hdmi8054Policy.LIVE_TV,true,false,true,true));
        check(!Hdmi8054Policy.mayLaunch(Hdmi8054Policy.LIVE_TV,true,true,false,true));
        check(!Hdmi8054Policy.mayLaunch(Hdmi8054Policy.LIVE_TV,true,true,true,false));
        System.out.println("PASS HDMI8054 HW6/HW7 identity, metadata, missing/duplicate/spoofed inputs, legacy CEC gate");
    }
}

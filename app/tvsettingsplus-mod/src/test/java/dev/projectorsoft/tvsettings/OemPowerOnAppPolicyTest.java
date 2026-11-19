package dev.projectorsoft.tvsettings;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class OemPowerOnAppPolicyTest {
    static final class Fake implements OemPowerOnAppPolicy.Property {
        String value;
        boolean permit=true;
        boolean stuck=false;
        int writes;
        Fake(String initial){value=initial;}
        public String read(){return value;}
        public boolean write(String target){
            writes++;
            if(!permit)return false;
            if(!stuck)value=target;
            return true;
        }
    }
    private static void ok(boolean b,String message){if(!b)throw new AssertionError(message);}
    public static void main(String[] args){
        Set<String> apps=new HashSet<>(Arrays.asList("org.example.player","com.example.tv"));
        ok(OemPowerOnAppPolicy.isPackageName("org.example.player"),"real package");
        ok(!OemPowerOnAppPolicy.isPackageName("../etc") 
           && !OemPowerOnAppPolicy.isPackageName("HOME"),"reject malformed");
        ok(OemPowerOnAppPolicy.expose("HOME",apps),"no startup app");
        ok(OemPowerOnAppPolicy.expose("com.example.tv",apps),"installed app");
        ok(!OemPowerOnAppPolicy.expose("USB",apps),"unknown source fallback");
        ok(!OemPowerOnAppPolicy.expose("com.example.missing",apps),"uninstalled app");
        ok(!OemPowerOnAppPolicy.expose("HOME",new HashSet<String>()),"no candidates");
        Fake f=new Fake("HOME");
        ok(OemPowerOnAppPolicy.apply(f,apps,"com.example.tv"),"vendor property set");
        ok(f.writes==1 && "com.example.tv".equals(f.value),"read back");
        ok(OemPowerOnAppPolicy.apply(f,apps,"com.example.tv") && f.writes==1,"idempotent");
        ok(!OemPowerOnAppPolicy.apply(f,apps,"com.my.settings")
           && !OemPowerOnAppPolicy.apply(f,apps,"com.evil.unknown"),"not whitelisted");
        ok(OemPowerOnAppPolicy.apply(f,apps,"HOME"),"restore Android TV");
        Fake blocked=new Fake("HOME");
        blocked.permit=false;
        ok(!OemPowerOnAppPolicy.apply(blocked,apps,"org.example.player"),"reject denial");
        Fake stuck=new Fake("HOME");
        stuck.stuck=true;
        ok(!OemPowerOnAppPolicy.apply(stuck,apps,"org.example.player"),"reject no readback");
        Fake hdmi=new Fake("com.mediatek.input/HDMI1");
        ok(!OemPowerOnAppPolicy.apply(hdmi,apps,"org.example.player")
           && hdmi.writes==0,"do not override unknown OEM source");
        System.out.println("PASS: launchable-package whitelist, source fallback and verified writes");
    }
}

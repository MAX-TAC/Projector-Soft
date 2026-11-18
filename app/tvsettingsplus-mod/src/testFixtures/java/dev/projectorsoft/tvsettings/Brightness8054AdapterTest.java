package dev.projectorsoft.tvsettings;
import android.content.Context;
public final class Brightness8054AdapterTest {
    static void check(boolean value){if(!value)throw new AssertionError();}
    public static void main(String[] args)throws Exception {
        for(String machine:new String[]{"8054","",null}) {
            Context context=new Context();
            KuSourceRjBrightnessGateway gateway=new KuSourceRjBrightnessGateway(context,machine);
            for(int mode=0;mode<=3;mode++) {
                try {gateway.applyMode(mode);throw new AssertionError("Unverified brightness write accepted");}
                catch(IllegalStateException expected){}
            }
            check(context.broadcasts==0&&context.manager.receiverLookups==0);
        }
        Context legacy=new Context();
        new KuSourceRjBrightnessGateway(legacy,"8052").applyMode(1);
        check(legacy.broadcasts==1&&legacy.manager.receiverLookups==1); // Dispatch positive control; not a HAL assertion.
        Context inline=new Context();
        boolean[] customerRoute={false};
        KuSourceRjBrightnessGateway candidate=new KuSourceRjBrightnessGateway(inline,"8054",()->customerRoute[0]);
        check(candidate.readSupportedModes().isEmpty());
        customerRoute[0]=true;
        check(candidate.readSupportedModes().size()==4);
        candidate.applyMode(3);
        customerRoute[0]=false;
        try {candidate.applyMode(2);throw new AssertionError("Stale route accepted");}
        catch(IllegalStateException expected){}
        check(inline.broadcasts==1&&inline.manager.receiverLookups==2);
        System.out.println("PASS production brightness gateway: no receiver lookup/broadcast for8054/unknown; legacy dispatch retained");
    }
}

package dev.projectorsoft.tvsettings;

public final class OemEarcPolicyTest {
    private static void eq(boolean expected,boolean actual) {
        if(expected!=actual)throw new AssertionError("expected "+expected+" actual "+actual);
    }
    public static void main(String[] args) {
        eq(false,OemEarcPolicy.canExpose(false,true,"0"));
        eq(false,OemEarcPolicy.canExpose(true,false,"0"));
        eq(false,OemEarcPolicy.canExpose(true,true,null));
        eq(false,OemEarcPolicy.canExpose(true,true,"-1"));
        eq(false,OemEarcPolicy.canExpose(true,true,"2"));
        eq(true,OemEarcPolicy.canExpose(true,true,"0"));
        eq(true,OemEarcPolicy.canExpose(true,true,"1"));
        eq(true,OemEarcPolicy.isEnabled("0"));
        eq(false,OemEarcPolicy.isEnabled("1"));
        if(OemEarcPolicy.storedValue(true)!=0 || OemEarcPolicy.storedValue(false)!=1)
            throw new AssertionError("wrong factory eARC polarity");
        try {
            OemEarcPolicy.isEnabled("unknown");
            throw new AssertionError("invalid eARC flag must be rejected");
        }catch(IllegalArgumentException expected){}
        System.out.println("PASS OEM eARC supported+CEC+factory-state gating and polarity");
    }
}

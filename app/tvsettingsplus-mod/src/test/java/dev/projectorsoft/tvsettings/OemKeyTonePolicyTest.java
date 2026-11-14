package dev.projectorsoft.tvsettings;

public final class OemKeyTonePolicyTest {
    private static class Fake implements OemKeyTonePolicy.Store {
        String raw;
        boolean permit=true;
        boolean stuck=false;
        int writes=0;
        Fake(String r){raw=r;}
        public String read(){return raw;}
        public boolean write(int v){
            writes++;
            if(!permit)return false;
            if(!stuck)raw=Integer.toString(v);
            return true;
        }
    }
    private static void check(boolean yes,String why) {
        if(!yes)throw new AssertionError(why);
    }
    public static void main(String[] args) {
        check(OemKeyTonePolicy.valid(null),"OEM absent == default off");
        check(OemKeyTonePolicy.valid("0") && OemKeyTonePolicy.valid("1"),"OEM 0/1");
        check(!OemKeyTonePolicy.valid("2") && !OemKeyTonePolicy.valid("true"),"invalid state");
        check(!OemKeyTonePolicy.checked(null),"unset default off");
        check(OemKeyTonePolicy.checked("1"),"on");
        Fake original=new Fake(null);
        check(OemKeyTonePolicy.apply(original,true),"write on when setting absent");
        check("1".equals(original.raw) && original.writes==1,"persist 1");
        check(OemKeyTonePolicy.apply(original,true) && original.writes==1,"no duplicate writes");
        check(OemKeyTonePolicy.apply(original,false),"turn off");
        check("0".equals(original.raw) && original.writes==2,"persist 0");
        check(!OemKeyTonePolicy.apply(original,"true"),"reject non-Boolean");
        Fake denied=new Fake("0");
        denied.permit=false;
        check(!OemKeyTonePolicy.apply(denied,true) && "0".equals(denied.raw),"denied write");
        Fake mismatched=new Fake("0");
        mismatched.stuck=true;
        check(!OemKeyTonePolicy.apply(mismatched,true),"no false readback");
        Fake invalid=new Fake("3");
        check(!OemKeyTonePolicy.apply(invalid,true) && invalid.writes==0,"unknown state, no write");
        check(!OemKeyTonePolicy.apply(null,true),"null backend");
        System.out.println("PASS: factory sound-effects key with default, state gate and verified writes");
    }
}

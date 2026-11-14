package dev.projectorsoft.tvsettings;

public final class OemFontScalePolicyTest {
    private static final class Fake implements OemFontScalePolicy.Store {
        float value;
        boolean permit=true,stuck=false;
        int writes=0;
        Fake(float initial){value=initial;}
        @Override public float read(){return value;}
        @Override public boolean write(float next){
            writes++;
            if(!permit)return false;
            if(!stuck)value=next;
            return true;
        }
    }
    private static void ok(boolean b,String why){if(!b)throw new AssertionError(why);}
    public static void main(String[] args){
        float[] values=OemFontScalePolicy.values();
        ok(values.length==4 && OemFontScalePolicy.labels().length==4,"exact OEM scale set");
        ok(OemFontScalePolicy.index(0.85f)==0,"small");
        ok(OemFontScalePolicy.index(1.0f)==1,"normal");
        ok(OemFontScalePolicy.index(1.15f)==2,"large");
        ok(OemFontScalePolicy.index(1.3f)==3,"extra large");
        ok(OemFontScalePolicy.index(0.92f)<0
           && OemFontScalePolicy.index(Float.NaN)<0
           && OemFontScalePolicy.index(Float.POSITIVE_INFINITY)<0,"reject unknown");
        Fake store=new Fake(1.0f);
        ok(OemFontScalePolicy.apply(store,"1.15"),"write exact 1.15");
        ok(store.writes==1 && OemFontScalePolicy.index(store.value)==2,"write verified");
        ok(OemFontScalePolicy.apply(store,"1.15") && store.writes==1,"idempotent");
        ok(OemFontScalePolicy.apply(store,"0.85"),"set small");
        ok(!OemFontScalePolicy.apply(store,"2.0"),"no unsupported scale");
        Fake denied=new Fake(1.0f);
        denied.permit=false;
        ok(!OemFontScalePolicy.apply(denied,"1.3"),"fail closed on deny");
        Fake stuck=new Fake(1.0f);
        stuck.stuck=true;
        ok(!OemFontScalePolicy.apply(stuck,"1.3"),"fail on readback");
        Fake unknown=new Fake(1.1f);
        ok(!OemFontScalePolicy.apply(unknown,"1.0") && unknown.writes==0,"unknown original");
        ok(!OemFontScalePolicy.apply(null,"1.0")
           && !OemFontScalePolicy.apply(store,1.0f),"type check");
        values[0]=12f;
        ok(OemFontScalePolicy.index(0.85f)==0,"defensive clone");
        System.out.println("PASS: four exact OEM font scales and fail-closed store writes");
    }
}

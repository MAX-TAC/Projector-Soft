package dev.projectorsoft.tvsettings;
public final class OemKuSourceServicePolicyTest {
    private static void ok(boolean x,String m){if(!x)throw new AssertionError(m);}
    public static void main(String[] args){
        ok(OemKuSourceServicePolicy.evaluate(true,true,true)
            ==OemKuSourceServicePolicy.State.RUNNING_VISIBLE,"observed target");
        ok(OemKuSourceServicePolicy.evaluate(true,false,true)
            ==OemKuSourceServicePolicy.State.RUNNING_VISIBLE,"observed outranks source flag");
        ok(OemKuSourceServicePolicy.evaluate(true,true,false)
            ==OemKuSourceServicePolicy.State.POSSIBLY_STOPPED,"other OEM services visible");
        ok(OemKuSourceServicePolicy.evaluate(true,false,false)
            ==OemKuSourceServicePolicy.State.NOT_VERIFIABLE,"unrelated processes not proof");
        ok(OemKuSourceServicePolicy.evaluate(false,true,false)
            ==OemKuSourceServicePolicy.State.NOT_VERIFIABLE,"empty snapshot not proof");
        for(OemKuSourceServicePolicy.State s:OemKuSourceServicePolicy.State.values())
            ok(!OemKuSourceServicePolicy.describe(s).isEmpty(),"safe status "+s);
        ok(OemKuSourceServicePolicy.describe(OemKuSourceServicePolicy.State.NOT_VERIFIABLE)
            .contains("не подтверждено"),"never claim stopped on missing");
        System.out.println("PASS: read-only service visibility evidence and conservative state");
    }
}

package dev.projectorsoft.tvsettings;
public final class OemLegacyIntentPolicyTest {
    private static void ok(boolean value,String text){
        if(!value)throw new AssertionError(text);
    }
    public static void main(String[] args){
        OemLegacyIntentPolicy.State mod=OemLegacyIntentPolicy.evaluate(
            true,false,OemLegacyIntentPolicy.MOD);
        ok(mod==OemLegacyIntentPolicy.State.MOD_DEFAULT,"mod preferred");
        ok(OemLegacyIntentPolicy.confirmedWithoutOldPackage(mod),"only proven route");
        OemLegacyIntentPolicy.State old=OemLegacyIntentPolicy.evaluate(
            true,true,OemLegacyIntentPolicy.KUSETTINGS);
        ok(old==OemLegacyIntentPolicy.State.OEM_DEFAULT,"old settings preferred");
        ok(!OemLegacyIntentPolicy.confirmedWithoutOldPackage(old),"not yet migrated");
        OemLegacyIntentPolicy.State selectedDespiteCompetition=
            OemLegacyIntentPolicy.evaluate(true,true,OemLegacyIntentPolicy.MOD);
        ok(selectedDespiteCompetition==OemLegacyIntentPolicy.State.MOD_DEFAULT_WITH_COMPETITOR,
            "Android selects MOD while KuSettings also handles the action");
        ok(OemLegacyIntentPolicy.chosenByAndroid(selectedDespiteCompetition),
            "count actual default even with competitor");
        ok(!OemLegacyIntentPolicy.confirmedWithoutOldPackage(selectedDespiteCompetition),
            "not yet independent of KuSettings");
        ok(OemLegacyIntentPolicy.evaluate(true,true,null)
            ==OemLegacyIntentPolicy.State.COMPETING_HANDLERS,"unresolved competing handlers");
        ok(OemLegacyIntentPolicy.chosenByAndroid(mod),"selected MOD without competitor");
        ok(OemLegacyIntentPolicy.evaluate(false,true,OemLegacyIntentPolicy.KUSETTINGS)
            ==OemLegacyIntentPolicy.State.MOD_NOT_VISIBLE,"mod handler missing");
        ok(OemLegacyIntentPolicy.evaluate(true,false,null)
            ==OemLegacyIntentPolicy.State.DEFAULT_UNKNOWN,"no resolved default");
        for(OemLegacyIntentPolicy.State state:OemLegacyIntentPolicy.State.values())
            ok(OemLegacyIntentPolicy.describe(state)!=null
               && !OemLegacyIntentPolicy.describe(state).isEmpty(),"status "+state);
        System.out.println("PASS: old/new explicit intent handler evidence, no false migration claims");
    }
}

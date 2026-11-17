package dev.projectorsoft.tvsettings;

public final class EdidPortPolicyTest {
    private static void check(boolean ok,String reason) {
        if(!ok)throw new AssertionError(reason);
    }
    public static void main(String[] args) {
        check(EdidPortPolicy.mayUseLegacySelector("8052"),"Keep existing single-port route");
        check(!EdidPortPolicy.mayUseLegacySelector("8054"),"Never write shared EDID key for unverified two-port mapping");
        check(!EdidPortPolicy.mayUseLegacySelector(null),"Unknown board must fail closed");
        check(!EdidPortPolicy.mayUseLegacySelector(""),"Missing board ID must fail closed");
        System.out.println("PASS EdidPortPolicy: 8054 legacy EDID selector hidden");
    }
}

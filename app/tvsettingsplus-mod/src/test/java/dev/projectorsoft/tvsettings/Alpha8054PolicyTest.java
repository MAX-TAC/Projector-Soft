package dev.projectorsoft.tvsettings;

public final class Alpha8054PolicyTest {
    private static void require(boolean ok,String what) {
        if(!ok)throw new AssertionError(what);
    }
    public static void main(String[] args) {
        Alpha8054Policy.State yes=Alpha8054Policy.evaluate("8054","8054",2,"13");
        require(yes==Alpha8054Policy.State.CANDIDATE,"exact 8054");
        require(Alpha8054Policy.allowExperimentalControls(yes),"candidate allows UI");
        require(Alpha8054Policy.evaluate("","8054",2,"13")==yes,"build fallback");
        require(Alpha8054Policy.evaluate("8054","",2,"13")==yes,"property fallback");
        require(Alpha8054Policy.evaluate("8052","8054",2,"13")
                ==Alpha8054Policy.State.CONFLICTING_IDENTIFIERS,"conflicting board signals");
        require(Alpha8054Policy.evaluate("8052","8052",1,"13")
                ==Alpha8054Policy.State.DIFFERENT_BOARD,"8052 not alpha");
        require(Alpha8054Policy.evaluate("8054","8054",1,"13")
                ==Alpha8054Policy.State.HDMI_UNCONFIRMED,"HDMI1 mismatch");
        require(Alpha8054Policy.evaluate("8054","8054",null,"13")
                ==Alpha8054Policy.State.HDMI_UNCONFIRMED,"HDMI missing");
        require(Alpha8054Policy.evaluate("8054","8054",2,"14")
                ==Alpha8054Policy.State.UNSUPPORTED_ANDROID,"Android 14");
        require(Alpha8054Policy.evaluate("не определено",null,2,"13")
                ==Alpha8054Policy.State.UNKNOWN_BOARD,"unknown");
        for(Alpha8054Policy.State state:Alpha8054Policy.State.values()) {
            require(!Alpha8054Policy.status(state).isEmpty(),"status: "+state);
            require(Alpha8054Policy.allowExperimentalControls(state)
                    ==(state==Alpha8054Policy.State.CANDIDATE),"fail closed "+state);
        }
        System.out.println("PASS: 8054 alpha runtime gate fails closed on bad metadata");
    }
}

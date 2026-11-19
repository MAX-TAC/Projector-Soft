package dev.projectorsoft.tvsettings;
public final class Projection8054PolicyTest {
    static void check(boolean value){if(!value)throw new AssertionError();}
    private static final String OWNER=Projection8054Policy.CORRECTION_PACKAGE;
    private static final String FOUR=OWNER+".FourCornerActivity";

    private static boolean eligible(String model,boolean extras,String owner,String activity,
                                    boolean system,boolean component,boolean app,boolean sameUid,
                                    boolean export,boolean grant) {
        return Projection8054Policy.mayOpenStockEditor(model,Projection8054Policy.FOUR_CORNER,extras,owner,activity,
                system,component,app,sameUid,export,grant);
    }

    private static void testStockEditor() {
        check(eligible("8054",false,OWNER,FOUR,true,true,true,false,true,true));
        check(eligible("8054",false,OWNER,FOUR,true,true,true,true,false,true));
        check(!eligible("8052",false,OWNER,FOUR,true,true,true,false,true,true));
        check(!eligible(null,false,OWNER,FOUR,true,true,true,false,true,true));
        check(!eligible("8054",true,OWNER,FOUR,true,true,true,false,true,true));
        check(!eligible("8054",false,"other",FOUR,true,true,true,false,true,true));
        check(!eligible("8054",false,OWNER,null,true,true,true,false,true,true));
        check(!eligible("8054",false,OWNER,OWNER+".DigitalZoomActivity",true,true,true,false,true,true));
        check(!eligible("8054",false,OWNER,FOUR,false,true,true,false,true,true));
        check(!eligible("8054",false,OWNER,FOUR,true,false,true,false,true,true));
        check(!eligible("8054",false,OWNER,FOUR,true,true,false,false,true,true));
        check(!eligible("8054",false,OWNER,FOUR,true,true,true,false,false,true));
        check(!eligible("8054",false,OWNER,FOUR,true,true,true,true,true,false));
        check(eligible("8054",false,OWNER,FOUR,true,true,true,false,true,true));
        check(Projection8054Policy.mayOpenStockEditor("8054",Projection8054Policy.DIGITAL_ZOOM,
                false,OWNER,OWNER+".DigitalZoomActivity",true,true,true,false,true,true));
        check(Projection8054Policy.mayOpenStockEditor("8054",Projection8054Policy.HORIZONTAL_VERTICAL,
                false,OWNER,OWNER+".HVKeystoneActivity",true,true,true,false,true,true));
        check(!Projection8054Policy.mayOpenStockEditor("8054","unknown",false,OWNER,FOUR,
                true,true,true,false,true,true));
    }

    public static void main(String[] args) {
        check(!Projection8054Policy.mayUseLegacyBrightness("8054"));
        check(!Projection8054Policy.mayUseLegacyBrightness(null));
        check(!Projection8054Policy.mayUseLegacyBrightness(""));
        check(Projection8054Policy.mayUseLegacyBrightness("8052"));
        check(Projection8054Policy.mayNavigate(true,true,false,true,true));
        check(Projection8054Policy.mayNavigate(true,true,true,false,true));
        check(!Projection8054Policy.mayNavigate(true,true,false,false,true));
        check(!Projection8054Policy.mayNavigate(false,true,true,true,true));
        check(!Projection8054Policy.mayNavigate(true,false,true,true,true));
        check(!Projection8054Policy.mayNavigate(true,true,false,true,false));
        testStockEditor();
        System.out.println("PASS projection OEM eligibility, permission/exported/disabled gates and 8054 brightness write gate");
    }
}

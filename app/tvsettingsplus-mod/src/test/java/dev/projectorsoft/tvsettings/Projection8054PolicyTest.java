package dev.projectorsoft.tvsettings;
public final class Projection8054PolicyTest {
    static void check(boolean value){if(!value)throw new AssertionError();}
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
        System.out.println("PASS projection OEM eligibility, permission/exported/disabled gates and 8054 brightness write gate");
    }
}

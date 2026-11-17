package dev.projectorsoft.tvsettings;
public final class OemSoundValuePolicyTest {
    static void check(boolean value){if(!value)throw new AssertionError();}
    public static void main(String[] args) {
        check(OemSoundValuePolicy.acceptsBandChange("50",60,0,100,"0","0"));
        check(!OemSoundValuePolicy.acceptsBandChange("50",60,0,100,"0","4"));
        check(!OemSoundValuePolicy.acceptsBandChange(null,60,0,100,"0","0"));
        check(!OemSoundValuePolicy.acceptsBandChange("bad",60,0,100,"0","0"));
        check(!OemSoundValuePolicy.acceptsBandChange("201",60,0,100,"0","0"));
        check(!OemSoundValuePolicy.acceptsBandChange("50",101,0,100,"0","0"));
        check(!OemSoundValuePolicy.acceptsBandChange("50",60,0,100,null,null));
        check(!OemSoundValuePolicy.acceptsBandChange("50",60,0,100,"7","7"));
        check(!OemSoundValuePolicy.acceptsBandChange("50",60,100,0,"0","0"));
        System.out.println("PASS sound EQ value, scale, capability and stale-mode guards");
    }
}

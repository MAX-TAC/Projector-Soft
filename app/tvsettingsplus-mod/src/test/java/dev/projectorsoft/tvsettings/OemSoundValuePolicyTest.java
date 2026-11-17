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
        for(String key:new String[]{"sound_style","sound_bass","sound_treble","sound_sound_surround"}) {
            int max="sound_style".equals(key)?6:"sound_sound_surround".equals(key)?1:100;
            check(OemSoundValuePolicy.acceptsControlChange(key,"0",max,"0","0"));
            check(!OemSoundValuePolicy.acceptsControlChange(key,"0",max+1,"0","0"));
            check(!OemSoundValuePolicy.acceptsControlChange(key,null,0,"0","0"));
            check(!OemSoundValuePolicy.acceptsControlChange(key,"0",0,"0","4"));
        }
        check(!OemSoundValuePolicy.acceptsControlChange("sound_unknown","0",0,"0","0"));
        System.out.println("PASS sound EQ value, scale, capability and stale-mode guards");
    }
}

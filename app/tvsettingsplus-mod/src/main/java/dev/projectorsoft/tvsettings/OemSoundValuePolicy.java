package dev.projectorsoft.tvsettings;

/** Validates native EQ scale and rejects edits from an obsolete sound-mode page. */
public final class OemSoundValuePolicy {
    private OemSoundValuePolicy() {}
    public static boolean acceptsControlChange(String key,String existing,int requested,
                                               String openedMode,String currentMode) {
        int max;
        if("sound_style".equals(key))max=6;
        else if("sound_bass".equals(key)||"sound_treble".equals(key))max=100;
        else if("sound_sound_surround".equals(key))max=1;
        else return false;
        return acceptsValueChange(existing,requested,0,max,openedMode,currentMode);
    }
    public static boolean acceptsBandChange(String existing,int requested,int min,int max,
                                            String openedMode,String currentMode) {
        return "0".equals(currentMode) && acceptsValueChange(existing, requested, min, max, openedMode, currentMode);
    }

    static boolean validProfile(String profile) {
        return profile != null && profile.matches("[0-7]");
    }

    private static boolean acceptsValueChange(String existing,int requested,int min,int max,
                                              String openedMode,String currentMode) {
        if(min<0||max>200||max<=min||requested<min||requested>max
                ||openedMode==null||!openedMode.equals(currentMode)||existing==null)return false;
        try {
            int old=Integer.parseInt(existing),mode=Integer.parseInt(currentMode);
            return old>=min&&old<=max&&mode>=0&&mode<=6;
        } catch(NumberFormatException ex) {return false;}
    }
}

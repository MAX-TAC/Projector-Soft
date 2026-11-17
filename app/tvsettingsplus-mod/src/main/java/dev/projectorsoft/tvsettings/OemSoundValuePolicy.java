package dev.projectorsoft.tvsettings;

/** Validates native EQ scale and rejects edits from an obsolete sound-mode page. */
public final class OemSoundValuePolicy {
    private OemSoundValuePolicy() {}
    public static boolean acceptsBandChange(String existing,int requested,int min,int max,
                                            String openedMode,String currentMode) {
        if(min<0||max>200||max<=min||requested<min||requested>max
                ||openedMode==null||!openedMode.equals(currentMode)||existing==null)return false;
        try {
            int old=Integer.parseInt(existing),mode=Integer.parseInt(currentMode);
            return old>=min&&old<=max&&mode>=0&&mode<=6;
        } catch(NumberFormatException ex) {return false;}
    }
}

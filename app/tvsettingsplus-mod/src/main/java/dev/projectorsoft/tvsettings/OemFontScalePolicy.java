package dev.projectorsoft.tvsettings;

/**
 * Verified stock FontSizeSettingsSecondaryController choices.
 * The OEM writes Settings.System "font_scale" 0.85,1.0,1.15,1.3.
 */
public final class OemFontScalePolicy {
    public interface Store {
        float read() throws Exception;
        boolean write(float value) throws Exception;
    }
    private static final float[] VALUES={0.85f,1.0f,1.15f,1.3f};
    private static final String[] LABELS={"Маленький (85%)","Средний (100%)",
            "Большой (115%)","Очень большой (130%)"};
    private OemFontScalePolicy(){}

    public static float[] values(){return VALUES.clone();}
    public static String[] labels(){return LABELS.clone();}

    public static int index(float value) {
        if(!Float.isFinite(value))return -1;
        for(int i=0;i<VALUES.length;i++)
            if(Math.abs(VALUES[i]-value)<0.0001f)return i;
        return -1;
    }

    public static boolean apply(Store store,Object proposal) {
        if(store==null || !(proposal instanceof String))return false;
        try {
            float target=Float.parseFloat((String)proposal);
            if(index(target)<0)return false;
            float before=store.read();
            if(index(before)<0)return false;
            if(Math.abs(before-target)<0.0001f)return true;
            if(!store.write(target))return false;
            return index(store.read())==index(target);
        }catch(Exception e) {
            return false;
        }
    }
}

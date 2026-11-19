package dev.projectorsoft.tvsettings;

/** Exact stock SoundFragment key-tone state: absent system setting defaults to 0. */
public final class OemKeyTonePolicy {
    public interface Store {
        String read() throws Exception;
        boolean write(int value) throws Exception;
    }
    private OemKeyTonePolicy() {}

    public static boolean valid(String raw) {
        return raw == null || "0".equals(raw) || "1".equals(raw);
    }

    public static boolean checked(String raw) {
        return "1".equals(raw);
    }

    public static boolean apply(Store store,Object requested) {
        if(store==null || !(requested instanceof Boolean))return false;
        try {
            String before=store.read();
            if(!valid(before))return false;
            boolean current=checked(before);
            boolean next=((Boolean)requested).booleanValue();
            if(current==next)return true; // avoid writing unnecessary setting
            if(!store.write(next?1:0))return false;
            return String.valueOf(next?1:0).equals(store.read());
        }catch(Exception ignored){
            return false;
        }
    }
}

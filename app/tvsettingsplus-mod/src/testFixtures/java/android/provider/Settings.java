package android.provider;
import android.content.ContentResolver;
import java.util.*;
public final class Settings {
    public static final class Global {
        public static final Map<String,String> values=new HashMap<>();
        public static int writes;
        public static String getString(ContentResolver resolver,String key){return values.get(key);}
        public static boolean putString(ContentResolver resolver,String key,String value){writes++;values.put(key,value);return true;}
    }
}

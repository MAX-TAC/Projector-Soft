package android.content;
public final class Context {
    public static final int MODE_PRIVATE=0;
    private static final SharedPreferences saved=new SharedPreferences();
    public ContentResolver getContentResolver(){return new ContentResolver();}
    public SharedPreferences getSharedPreferences(String name,int mode){return saved;}
}

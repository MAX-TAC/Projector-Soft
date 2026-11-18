package android.content;
public final class Context {
    public static final int MODE_PRIVATE=0;
    private static final SharedPreferences saved=new SharedPreferences();
    public int broadcasts;
    public final android.content.pm.PackageManager manager=new android.content.pm.PackageManager();
    public Context getApplicationContext(){return this;}
    public android.content.pm.PackageManager getPackageManager(){return manager;}
    public void sendBroadcast(Intent intent){broadcasts++;}
    public ContentResolver getContentResolver(){return new ContentResolver();}
    public SharedPreferences getSharedPreferences(String name,int mode){return saved;}
}

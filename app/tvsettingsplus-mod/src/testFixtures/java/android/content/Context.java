package android.content;
public final class Context {
    public static final int MODE_PRIVATE=0;
    private static final SharedPreferences saved=new SharedPreferences();
    private final ContentResolver resolver=new ContentResolver();
    public int broadcasts, starts;
    public boolean failLaunch, granted=true;
    public final android.content.pm.ApplicationInfo app=new android.content.pm.ApplicationInfo();
    public ClassLoader getClassLoader(){return getClass().getClassLoader();}
    public android.content.pm.ApplicationInfo getApplicationInfo(){return app;}
    public int checkSelfPermission(String permission){return granted?0:-1;}
    public void startActivity(Intent intent){
        if(failLaunch) throw new SecurityException("cannot start");
        starts++;
    }
    public final android.content.pm.PackageManager manager=new android.content.pm.PackageManager();
    public Context getApplicationContext(){return this;}
    public android.content.pm.PackageManager getPackageManager(){return manager;}
    public void sendBroadcast(Intent intent){broadcasts++;}
    public ContentResolver getContentResolver(){return resolver;}
    public SharedPreferences getSharedPreferences(String name,int mode){return saved;}
}

package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.util.Log;
import java.lang.reflect.Method;

/** Attach each section to the real AndroidX screen before it receives children. */
final class ProjectorMenuSections {
    private static final String TAG="TvSettingsPlusMOD";
    private static final MenuInstallation INSTALLATION=new MenuInstallation();

    interface Work { void run() throws Exception; }
    interface Check { boolean run() throws Exception; }
    interface Populate { void run(Object category) throws Exception; }

    final Class<?> preference;
    private final Context context;
    private final Object screen;
    private final Class<?> categoryType;
    private final Method add;
    private final Method remove;

    ProjectorMenuSections(Context context,Object screen) throws ReflectiveOperationException {
        this.context=context;
        this.screen=screen;
        ClassLoader loader=screen.getClass().getClassLoader();
        preference=Class.forName("androidx.preference.Preference",false,loader);
        categoryType=Class.forName("androidx.preference.PreferenceCategory",false,loader);
        add=screen.getClass().getMethod("addPreference",preference);
        remove=screen.getClass().getMethod("removePreference",preference);
    }

    void add(String key,String title,int order,boolean required,Populate populate) {
        MenuInstallation.Attempt attempt=INSTALLATION.begin(screen,key);
        if(attempt==null)return;
        String stage="create";
        try(MenuInstallation.Attempt transaction=attempt) {
            Object category=categoryType.getConstructor(Context.class).newInstance(context);
            preference.getMethod("setKey",String.class).invoke(category,key);
            preference.getMethod("setTitle",CharSequence.class).invoke(category,title);
            preference.getMethod("setOrder",int.class).invoke(category,order);
            stage="attach";
            transaction.track(()->remove(category,key));
            if(!Boolean.TRUE.equals(add.invoke(screen,category)))
                throw new IllegalStateException("PreferenceScreen rejected category");
            stage="populate";
            populate.run(category);
            stage="visibility";
            int count=(Integer)categoryType.getMethod("getPreferenceCount").invoke(category);
            Method get=categoryType.getMethod("getPreference",int.class);
            Method visible=preference.getMethod("isVisible");
            for(int i=0;i<count;i++) {
                if(Boolean.TRUE.equals(visible.invoke(get.invoke(category,i)))) {
                    transaction.commit();
                    Log.i(TAG,"menu section="+key+" stage=ready");
                    return;
                }
            }
            if(!required)return;
            Object status=preference.getConstructor(Context.class).newInstance(context);
            preference.getMethod("setKey",String.class).invoke(status,key+"_availability");
            preference.getMethod("setTitle",CharSequence.class).invoke(status,
                    "Параметры сейчас недоступны");
            preference.getMethod("setSelectable",boolean.class).invoke(status,false);
            if(!Boolean.TRUE.equals(categoryType.getMethod("addPreference",preference).invoke(category,status)))
                throw new IllegalStateException("Category rejected availability row");
            transaction.commit();
            Log.i(TAG,"menu section="+key+" stage=read-only-status");
        } catch(Exception | LinkageError problem) {
            Log.w(TAG,"menu section="+key+" stage="+stage,problem);
        }
    }

    static void optional(String key,Work work) {
        available(key,()->{work.run();return true;});
    }

    static boolean available(String key,Check check) {
        try {return check.run();}
        catch(Exception | LinkageError problem) {
            Log.w(TAG,"menu stage="+key,problem);
            return false;
        }
    }

    private void remove(Object category,String key) {
        try {remove.invoke(screen,category);}
        catch(ReflectiveOperationException error) {
            throw new IllegalStateException("menu section="+key+" stage=rollback",error);
        }
    }
}

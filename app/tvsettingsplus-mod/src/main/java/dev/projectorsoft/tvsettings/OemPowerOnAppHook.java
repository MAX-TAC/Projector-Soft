package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Actual native Android TV boot-app selector, not an OEM page link.
 * Mirrors the factory PowerOnAppSecondaryController's persisted
 * persist.trd.source_id property with HOME for no startup application.
 * Input options are limited to currently installed, launchable packages.
 */
public final class OemPowerOnAppHook {
    private static final String TAG="TvSettingsPlusMOD";
    private static final String PROPERTY="persist.trd.source_id";
    private OemPowerOnAppHook() {}

    private static OemPowerOnAppPolicy.Property property() throws Exception {
        Class<?> cls=Class.forName("android.os.SystemProperties");
        Method getter=cls.getMethod("get",String.class,String.class);
        Method setter=cls.getMethod("set",String.class,String.class);
        return new OemPowerOnAppPolicy.Property() {
            @Override public String read() throws Exception {
                Object value=getter.invoke(null,PROPERTY,"HOME");
                return value instanceof String?(String)value:null;
            }
            @Override public boolean write(String value) throws Exception {
                setter.invoke(null,PROPERTY,value);
                return true;
            }
        };
    }

    private static Map<String,String> candidates(Context context) {
        PackageManager pm=context.getPackageManager();
        Map<String,String> result=new HashMap<>();
        for(String category:new String[]{
                Intent.CATEGORY_LEANBACK_LAUNCHER,Intent.CATEGORY_LAUNCHER}) {
            Intent query=new Intent(Intent.ACTION_MAIN);
            query.addCategory(category);
            List<ResolveInfo> activities=pm.queryIntentActivities(query,0);
            if(activities==null)continue;
            for(ResolveInfo info:activities) {
                if(info==null || info.activityInfo==null)continue;
                String pkg=info.activityInfo.packageName;
                if(!OemPowerOnAppPolicy.isPackageName(pkg)
                        || pkg.equals(context.getPackageName())
                        || "com.my.settings".equals(pkg)
                        || "com.coolux.source".equals(pkg))continue;
                try {
                    if(pm.getLeanbackLaunchIntentForPackage(pkg)==null
                            && pm.getLaunchIntentForPackage(pkg)==null)continue;
                    CharSequence label=info.loadLabel(pm);
                    String title=label==null?pkg:label.toString().trim();
                    if(title.isEmpty())title=pkg;
                    result.put(pkg,title);
                }catch(RuntimeException error) {
                    Log.d(TAG,"Skipping nonlaunchable app",error);
                }
            }
        }
        return result;
    }

    public static boolean install(Context context,Object category,Class<?> preference) {
        if(context==null || category==null || preference==null)return false;
        try {
            Map<String,String> nameByPackage=candidates(context);
            Set<String> apps=Collections.unmodifiableSet(
                    new HashSet<>(nameByPackage.keySet()));
            OemPowerOnAppPolicy.Property prop=property();
            String current=prop.read();
            if(!OemPowerOnAppPolicy.expose(current,apps))return false;
            List<String> names=new ArrayList<>(apps);
            Collections.sort(names,(a,b)-> {
                int c=nameByPackage.get(a).compareToIgnoreCase(nameByPackage.get(b));
                return c!=0?c:a.compareTo(b);
            });
            List<CharSequence> titles=new ArrayList<>();
            List<CharSequence> values=new ArrayList<>();
            titles.add("Не запускать приложение");
            values.add("HOME");
            for(String pkg:names) {
                titles.add(nameByPackage.get(pkg));
                values.add(pkg);
            }
            Class<?> listType=Class.forName("androidx.preference.ListPreference");
            Object item=listType.getConstructor(Context.class).newInstance(context);
            preference.getMethod("setKey",String.class).invoke(
                    item,"projectorsoft_poweron_app_native");
            preference.getMethod("setTitle",CharSequence.class).invoke(
                    item,"Приложение при включении");
            preference.getMethod("setOrder",int.class).invoke(item,274);
            preference.getMethod("setPersistent",boolean.class).invoke(item,false);
            String summary="HOME".equals(current)?"Не запускать приложение":nameByPackage.get(current);
            preference.getMethod("setSummary",CharSequence.class).invoke(item,summary);
            listType.getMethod("setEntries",CharSequence[].class)
                    .invoke(item,(Object)titles.toArray(new CharSequence[0]));
            listType.getMethod("setEntryValues",CharSequence[].class)
                    .invoke(item,(Object)values.toArray(new CharSequence[0]));
            listType.getMethod("setValue",String.class).invoke(item,current);
            Class<?> listenerType=Class.forName(
                    "androidx.preference.Preference$OnPreferenceChangeListener");
            InvocationHandler callback=(proxy,method,args)->{
                String methodName=method.getName();
                if("onPreferenceChange".equals(methodName)) {
                    if(args==null || args.length<2)return false;
                    boolean success=OemPowerOnAppPolicy.apply(prop,apps,args[1]);
                    if(success) {
                        String chosen=String.valueOf(args[1]);
                        String label="HOME".equals(chosen)
                                ? "Не запускать приложение":nameByPackage.get(chosen);
                        try {
                            preference.getMethod("setSummary",CharSequence.class)
                                    .invoke(item,label);
                        }catch(Exception e){Log.w(TAG,"Could not refresh boot-app label",e);}
                    }else {
                        Log.w(TAG,"Factory boot-app property write not confirmed");
                    }
                    return success;
                }
                if("toString".equals(methodName))return "OemPowerOnAppNative";
                if("hashCode".equals(methodName))return System.identityHashCode(proxy);
                if("equals".equals(methodName))
                    return args!=null && args.length>0 && proxy==args[0];
                return false;
            };
            Object callbackObject=Proxy.newProxyInstance(listenerType.getClassLoader(),
                    new Class<?>[]{listenerType},callback);
            preference.getMethod("setOnPreferenceChangeListener",listenerType)
                    .invoke(item,callbackObject);
            return Boolean.TRUE.equals(category.getClass().getMethod(
                    "addPreference",preference).invoke(category,item));
        }catch(Exception | LinkageError e){
            Log.w(TAG,"Native startup app selector unavailable, OEM fallback retained",e);
            return false;
        }
    }
}

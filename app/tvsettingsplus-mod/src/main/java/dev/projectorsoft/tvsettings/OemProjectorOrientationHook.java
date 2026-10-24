package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.util.Log;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Native right-pane projection placement without disturbing focus,
 * keystone, motor drivers or calibration images.
 *
 * The values and API below are from original 8052
 * ProjectionModeSecondaryController: 0=front, 1=rear,
 * 2=ceiling rear, 3=ceiling front.
 */
public final class OemProjectorOrientationHook {
    private static final String TAG="TvSettingsPlusMOD";
    private static final String OEM="com.android.tv.settings.uicustom.util.ProjectorManager";

    private static final String[] LABELS={
        "Спереди на столе","Сзади на столе",
        "Спереди под потолком","Сзади под потолком"
    };
    private static final int[] IDS={0,1,3,2};

    private OemProjectorOrientationHook() {}

    private static final class Gateway {
        final Object manager;
        final Method getter;
        final Method setter;
        final Context context;
        Gateway(Context context) throws Exception {
            this.context=context;
            Class<?> cls=Class.forName(OEM);
            this.manager=cls.getMethod("getInstance").invoke(null);
            if (manager==null)throw new IllegalStateException("OEM ProjectorManager not initialized");
            this.getter=cls.getMethod("getProjectorMirrorMode");
            this.setter=cls.getMethod("setProjectorMirrorMode",Context.class,int.class);
        }
        int get() throws Exception {
            Object value=getter.invoke(manager);
            if (!(value instanceof Integer))throw new IllegalStateException("Invalid native projector mode");
            return (Integer)value;
        }
        void set(int mode) throws Exception {
            setter.invoke(manager,context,mode);
        }
    }

    public static void install(Context context,Object category,Class<?> prefClass) {
        try {
            final Gateway gateway=new Gateway(context);
            int current=gateway.get();
            boolean supported=false;
            for (int value:IDS) if (value==current) supported=true;
            if (!supported)return;

            Class<?> list=Class.forName("androidx.preference.ListPreference");
            Object item=list.getConstructor(Context.class).newInstance(context);
            prefClass.getMethod("setKey",String.class).invoke(item,"projectorsoft_placement");
            prefClass.getMethod("setTitle",CharSequence.class).invoke(item,"Установка проектора");
            prefClass.getMethod("setOrder",int.class).invoke(item,232);
            prefClass.getMethod("setPersistent",boolean.class).invoke(item,false);
            CharSequence[] values=new CharSequence[IDS.length];
            Map<Integer,String> titles=new HashMap<>();
            for (int i=0;i<IDS.length;i++) {
                values[i]=String.valueOf(IDS[i]);
                titles.put(IDS[i],LABELS[i]);
            }
            list.getMethod("setEntries",CharSequence[].class).invoke(item,(Object)LABELS);
            list.getMethod("setEntryValues",CharSequence[].class).invoke(item,(Object)values);
            list.getMethod("setValue",String.class).invoke(item,String.valueOf(current));
            prefClass.getMethod("setSummary",CharSequence.class).invoke(item,titles.get(current));

            Class<?> listener=Class.forName("androidx.preference.Preference$OnPreferenceChangeListener");
            InvocationHandler callback=(proxy,method,args)-> {
                String name=method.getName();
                if ("onPreferenceChange".equals(name) && args!=null && args.length>=2) {
                    try {
                        int requested=Integer.parseInt(String.valueOf(args[1]));
                        if (!titles.containsKey(requested))return false;
                        gateway.set(requested);
                        // Do not claim an unconfirmed hardware change.
                        if (gateway.get()!=requested)return false;
                        prefClass.getMethod("setSummary",CharSequence.class)
                                .invoke(item,titles.get(requested));
                        return true;
                    } catch(Exception ex) {
                        Log.w(TAG,"Cannot change optical projection placement",ex);
                        return false;
                    }
                }
                if ("toString".equals(name))return "OemProjectorOrientationListener";
                if ("hashCode".equals(name))return System.identityHashCode(proxy);
                if ("equals".equals(name))return args!=null&&args.length>0&&proxy==args[0];
                return false;
            };
            Object fn=Proxy.newProxyInstance(listener.getClassLoader(),new Class<?>[]{listener},callback);
            prefClass.getMethod("setOnPreferenceChangeListener",listener).invoke(item,fn);
            category.getClass().getMethod("addPreference",prefClass).invoke(category,item);
        } catch (Exception e) {
            Log.w(TAG,"Native projection orientation unavailable",e);
        }
    }
}

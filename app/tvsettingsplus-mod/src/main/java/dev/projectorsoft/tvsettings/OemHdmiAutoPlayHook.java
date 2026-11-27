package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * Same OEM property and state transition as SourceSubMenuController
 * lambda$initView$11: persist.rjpj.hdmiplug.autoplay 0/1.
 * Does not change CEC, eARC, HDCP, HDMI source selection or keystone.
 */
public final class OemHdmiAutoPlayHook {
    private static final String TAG="TvSettingsPlusMOD";
    static final String KEY="projectorsoft_hdmi_autoplay";
    private static final WeakHashMap<Object, WeakReference<ProjectionToggleState>> STATES=new WeakHashMap<>();
    private static final String PROPERTY="persist.rjpj.hdmiplug.autoplay";
    private OemHdmiAutoPlayHook() {}

    private static int hardwarePortCount() throws Exception {
        Field f=Build.class.getField("A_RJ_HDMI_PORT_COUNT");
        int result=f.getInt(null);
        return result>=1&&result<=4?result:0;
    }

    private static Class<?> propertyClass() throws Exception {
        return Class.forName("android.os.SystemProperties");
    }

    static String getProperty() throws Exception {
        Class<?> cls=propertyClass();
        // Exact 8054 SourceSubMenuController.initData uses getInt(key, 0).
        String raw = (String) cls.getMethod("get", String.class, String.class)
                .invoke(null, PROPERTY, "");
        try { return Integer.parseInt(raw) == 1 ? "1" : "0"; }
        catch (NumberFormatException absentOrInvalid) { return "0"; }
    }

    private static void updateProperty(boolean enable) throws Exception {
        Class<?> cls=propertyClass();
        cls.getMethod("set",String.class,String.class)
                .invoke(null,PROPERTY,enable?"1":"0");
    }

    public static boolean install(Context ctx,Object category,Class<?> preferenceClass) {
        try {
            if(hardwarePortCount()<1)return false;
            ProjectionToggleState state=new ProjectionToggleState(new ProjectionToggleState.Backend() {
                @Override public String read() throws Exception {
                    return hardwarePortCount() >= 1 ? getProperty() : null;
                }
                @Override public void write(boolean value) throws Exception { updateProperty(value); }
            });
            Boolean current=state.refresh();
            if(current==null)return false;
            Class<?> toggle=Class.forName("androidx.preference.SwitchPreference");
            Object item=toggle.getConstructor(Context.class).newInstance(ctx);
            preferenceClass.getMethod("setKey",String.class)
                    .invoke(item,KEY);
            preferenceClass.getMethod("setTitle",CharSequence.class)
                    .invoke(item,"Автоматически открывать HDMI");
            preferenceClass.getMethod("setSummary",CharSequence.class)
                    .invoke(item,"Переключать источник при подключении HDMI");
            preferenceClass.getMethod("setOrder",int.class).invoke(item,272);
            preferenceClass.getMethod("setPersistent",boolean.class)
                    .invoke(item,false);
            toggle.getMethod("setChecked",boolean.class)
                    .invoke(item,current);

            Class<?> callbackType=Class.forName(
                    "androidx.preference.Preference$OnPreferenceChangeListener");
            InvocationHandler listener=(proxy,method,args)->{
                String methodName=method.getName();
                if("onPreferenceChange".equals(methodName)&&args!=null&&args.length>=2){
                    if(!(args[1] instanceof Boolean))return false;
                    try {
                        ProjectionToggleState.Result result=state.change((Boolean)args[1]);
                        render(item,state.value());
                        return result==ProjectionToggleState.Result.CONFIRMED;
                    }
                    catch(Exception e){
                        Log.w(TAG,"OEM HDMI hotplug autoplay unavailable",e);
                        return false;
                    }
                }
                if("toString".equals(methodName))return "OemHdmiAutoPlay";
                if("hashCode".equals(methodName))return System.identityHashCode(proxy);
                if("equals".equals(methodName))return args!=null&&args.length>0&&proxy==args[0];
                return false;
            };
            Object cb=Proxy.newProxyInstance(callbackType.getClassLoader(),
                    new Class<?>[]{callbackType},listener);
            preferenceClass.getMethod("setOnPreferenceChangeListener",callbackType)
                    .invoke(item,cb);
            Object ok=category.getClass().getMethod("addPreference",preferenceClass)
                    .invoke(category,item);
            if (!Boolean.TRUE.equals(ok)) return false;
            STATES.put(item,new WeakReference<>(state));
            return true;
        }catch(Exception e){
            Log.w(TAG,"HDMI autoplay OEM property unavailable",e);
            return false;
        }
    }

    static void refresh(Object row) throws ReflectiveOperationException {
        WeakReference<ProjectionToggleState> reference=STATES.get(row);
        ProjectionToggleState state=reference==null ? null : reference.get();
        if (state!=null) render(row,state.refresh());
    }

    private static void render(Object row,Boolean value) throws ReflectiveOperationException {
        Class<?> type=row.getClass();
        type.getMethod("setEnabled",boolean.class).invoke(row,value!=null);
        type.getMethod("setSummary",CharSequence.class).invoke(row,value==null
                ? "Текущее значение недоступно" : "Переключать источник при подключении HDMI");
        if (value!=null) type.getMethod("setChecked",boolean.class).invoke(row,value);
    }
}


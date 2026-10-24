package dev.projectorsoft.tvsettings;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.util.HashMap;
import java.util.Map;

/**
 * Initial ACTUAL OEM-control migration into native Android TV preference UI.
 *
 * Uses original factory Settings.Global keys AND exact OEM numeric mappings
 * from PictureFragment / SoundFragment, not invented values or PWM calls.
 * All UI objects are original app AndroidX preference classes instantiated
 * reflectively so the extra DEX can compile without repackaging AndroidX.
 */
public final class OemInlinePreferencesHook {
    private static final String TAG="TvSettingsPlusMOD";
    private static final String[] PIC_TITLES={
        "Пользовательский", "Стандартный", "Яркий",
        "Спорт", "Кино", "Игра", "Экономичный"};
    private static final int[] PIC_VALUES={0,7,3,2,9,10,11};
    private static final String[] TEMP_TITLES={"Пользовательская","Холодная","Стандартная","Тёплая"};
    private static final int[] TEMP_VALUES={0,1,2,3};
    private static final String[] SOUND_TITLES={
        "Стандартный","Яркий","Спорт","Кино","Музыка","Новости","Пользовательский"};
    private static final int[] SOUND_VALUES={1,2,3,4,5,6,0};
    private static final String CATEGORY_KEY="projectorsoft_quick_controls";
    private OemInlinePreferencesHook() {}

    public static int[] pictureValues() {return PIC_VALUES.clone();}
    public static int[] colorValues() {return TEMP_VALUES.clone();}
    public static int[] soundValues() {return SOUND_VALUES.clone();}

    public static void install(Object fragment) {
        if (fragment==null) return;
        try {
            Object ctx=fragment.getClass().getMethod("getContext").invoke(fragment);
            if (!(ctx instanceof Context)) return;
            Context context=(Context)ctx;
            Class<?> preference=Class.forName("androidx.preference.Preference");
            Class<?> categoryClass=Class.forName("androidx.preference.PreferenceCategory");
            Method find=fragment.getClass().getMethod("findPreference",CharSequence.class);
            Object existing=find.invoke(fragment,CATEGORY_KEY);
            if (existing != null) return;

            Object screen=fragment.getClass().getMethod("getPreferenceScreen").invoke(fragment);
            if (screen==null) return;
            Object category=categoryClass.getConstructor(Context.class).newInstance(context);
            categoryClass.getMethod("setKey",String.class).invoke(category,CATEGORY_KEY);
            categoryClass.getMethod("setTitle",CharSequence.class)
                    .invoke(category,"Быстрые настройки проектора");
            categoryClass.getMethod("setOrder",int.class).invoke(category,210);
            Method add=screen.getClass().getMethod("addPreference",preference);
            if (!(Boolean)add.invoke(screen,category)) return;

            // All values below mirror known OEM SettingsUtil.putGlobalValueInt.
            addList(category,preference,context,"projectorsoft_picture_mode",
                    "Режим изображения","picture_mode",PIC_TITLES,PIC_VALUES,7,211);
            addList(category,preference,context,"projectorsoft_color_temp",
                    "Цветовая температура","picture_color_temperature",
                    TEMP_TITLES,TEMP_VALUES,2,212);
            addList(category,preference,context,"projectorsoft_sound_mode",
                    "Режим звука","sound_style",SOUND_TITLES,SOUND_VALUES,1,213);
            addSwitchIfPresent(category,preference,context,
                    "projectorsoft_surround","Объёмное звучание","sound_sound_surround",214);
            String earc=vendorKey("KEY_SOUND_EARC");
            if (earc!=null) addSwitchIfPresent(category,preference,context,
                    "projectorsoft_earc","HDMI eARC",earc,215);
            String volume=vendorKey("KEY_SOUND_AUTO_VOLUME_CONTROL");
            if (volume!=null) addSwitchIfPresent(category,preference,context,
                    "projectorsoft_autovolume","Автоматическая громкость",volume,216);
            // Native Android TV seekbars: exact limits read from factory layout XML.
            // They remain hidden until the OEM Settings.Global value exists.
            OemInlineSlidersHook.install(context,category,preference);
            // OEM controller AdvanceSettingsSecondaryController provides
            // explicit 0/1 HDR and 0..3 MEMC options. No guessed HAL APIs.
            // Hide them until the factory settings provider already holds state.
            String hdrKey="picture_list_hdr";
            addSwitchIfPresent(category,preference,context,
                    "projectorsoft_hdr","HDR (OEM)",hdrKey,230);

            String mjcKey="tv_picture_advance_video_mjc_effect";
            String mjcRaw=Settings.Global.getString(context.getContentResolver(),mjcKey);
            if (mjcRaw != null && mjcRaw.matches("[0-3]")) {
                String[] effects={"Выключено","Низкий","Средний","Высокий"};
                int[] ids={0,1,2,3};
                addList(category,preference,context,"projectorsoft_memc",
                        "MEMC (OEM)",mjcKey,effects,ids,Integer.parseInt(mjcRaw),231);
            }
            // Stock ProjectorManager orientation codes, not calibration/keystone.
            OemProjectorOrientationHook.install(context,category,preference);
        } catch(Throwable e) {
            Log.w(TAG,"OEM quick controls unavailable; keep original settings",e);
        }
    }


    private static String vendorKey(String fieldName) {
        try {
            Class<?> cls=Class.forName(
                "com.android.tv.settings.partnercustomizer.tvsettingservice.util.PreferenceConfigUtilsBase");
            java.lang.reflect.Field field=cls.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value=field.get(null);
            return value instanceof String && !((String)value).isEmpty() ? (String)value : null;
        } catch(Exception e) {
            Log.w(TAG,"OEM key not accessible: "+fieldName);
            return null;
        }
    }

    /**
     * Conservative native switch: only expose controls which already have an
     * actual 0/1 value persisted by the original OEM application.
     * This avoids displaying eARC/auto-volume toggles on boards with no HAL.
     */
    private static void addSwitchIfPresent(Object category,Class<?> pref,Context context,
                                           String uiKey,String label,String oemKey,
                                           int order) throws Exception {
        ContentResolver resolver=context.getContentResolver();
        String initial=Settings.Global.getString(resolver,oemKey);
        if (!"0".equals(initial) && !"1".equals(initial)) return;
        Class<?> switchType=Class.forName("androidx.preference.SwitchPreference");
        Object item=switchType.getConstructor(Context.class).newInstance(context);
        pref.getMethod("setKey",String.class).invoke(item,uiKey);
        pref.getMethod("setTitle",CharSequence.class).invoke(item,label);
        pref.getMethod("setOrder",int.class).invoke(item,order);
        pref.getMethod("setPersistent",boolean.class).invoke(item,false);
        switchType.getMethod("setChecked",boolean.class).invoke(item,"1".equals(initial));
        Class<?> listener=Class.forName("androidx.preference.Preference$OnPreferenceChangeListener");
        InvocationHandler callback=(proxy,method,args)-> {
            if (method.getName().equals("onPreferenceChange") && args!=null && args.length>=2) {
                try {
                    if (!(args[1] instanceof Boolean)) return false;
                    int target=(Boolean)args[1]?1:0;
                    if (!Settings.Global.putInt(resolver,oemKey,target)) return false;
                    return Settings.Global.getInt(resolver,oemKey,-999)==target;
                } catch(Exception e) {
                    Log.w(TAG,"OEM toggle rejected "+oemKey,e);
                    return false;
                }
            }
            if (method.getName().equals("toString")) return "OemToggle:"+oemKey;
            if (method.getName().equals("hashCode")) return System.identityHashCode(proxy);
            if (method.getName().equals("equals")) return proxy==args[0];
            return false;
        };
        Object fn=Proxy.newProxyInstance(listener.getClassLoader(),
                new Class<?>[]{listener},callback);
        pref.getMethod("setOnPreferenceChangeListener",listener).invoke(item,fn);
        category.getClass().getMethod("addPreference",pref).invoke(category,item);
    }

    private static void addList(Object category,Class<?> pref,Context context,
                                String uiKey,String title,String oemKey,
                                String[] labels,int[] values,int defaultValue,
                                int order) throws Exception {
        if (labels.length!=values.length || labels.length==0)
            throw new IllegalArgumentException("Invalid factory mode table");
        Map<Integer,String> valueToTitle=new HashMap<>();
        CharSequence[] entryValues=new CharSequence[values.length];
        for (int i=0;i<values.length;i++) {
            if (valueToTitle.put(values[i],labels[i])!=null)
                throw new IllegalArgumentException("Duplicate OEM mode ID");
            entryValues[i]=Integer.toString(values[i]);
        }
        ContentResolver resolver=context.getContentResolver();
        int current=Settings.Global.getInt(resolver,oemKey,defaultValue);
        if (!valueToTitle.containsKey(current)) return; // don't invent vendor state

        Class<?> listType=Class.forName("androidx.preference.ListPreference");
        Object preference=listType.getConstructor(Context.class).newInstance(context);
        pref.getMethod("setKey",String.class).invoke(preference,uiKey);
        pref.getMethod("setTitle",CharSequence.class).invoke(preference,title);
        pref.getMethod("setSummary",CharSequence.class).invoke(preference,valueToTitle.get(current));
        pref.getMethod("setOrder",int.class).invoke(preference,order);
        pref.getMethod("setPersistent",boolean.class).invoke(preference,false);
        listType.getMethod("setEntries",CharSequence[].class)
                .invoke(preference,(Object)labels);
        listType.getMethod("setEntryValues",CharSequence[].class)
                .invoke(preference,(Object)entryValues);
        listType.getMethod("setValue",String.class)
                .invoke(preference,Integer.toString(current));

        Class<?> listenerType=Class.forName("androidx.preference.Preference$OnPreferenceChangeListener");
        InvocationHandler onChange=(proxy,method,args)-> {
            if (method.getName().equals("onPreferenceChange") && args!=null && args.length>=2) {
                try {
                    int requested=Integer.parseInt(String.valueOf(args[1]));
                    if (!valueToTitle.containsKey(requested)) return false;
                    if (!Settings.Global.putInt(resolver,oemKey,requested)) return false;
                    if (Settings.Global.getInt(resolver,oemKey,-999)!=requested) return false;
                    pref.getMethod("setSummary",CharSequence.class)
                            .invoke(preference,valueToTitle.get(requested));
                    return true;
                } catch(Exception ex) {
                    Log.w(TAG,"Failed applying OEM setting "+oemKey,ex);
                    return false;
                }
            }
            if (method.getName().equals("toString")) return "OemPreferenceListener:"+oemKey;
            if (method.getName().equals("hashCode")) return System.identityHashCode(proxy);
            if (method.getName().equals("equals")) return proxy==args[0];
            return false;
        };
        Object callback=Proxy.newProxyInstance(listenerType.getClassLoader(),
                new Class<?>[]{listenerType},onChange);
        pref.getMethod("setOnPreferenceChangeListener",listenerType).invoke(preference,callback);
        category.getClass().getMethod("addPreference",pref).invoke(category,preference);
    }
}

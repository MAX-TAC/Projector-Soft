package dev.projectorsoft.tvsettings;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
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
    private OemInlinePreferencesHook() {}

    public static int[] pictureValues() {return PIC_VALUES.clone();}
    public static int[] colorValues() {return TEMP_VALUES.clone();}
    public static int[] soundValues() {return SOUND_VALUES.clone();}
    static String[] soundTitles() {return SOUND_TITLES.clone();}

    /**
     * Compatibility bridge for older build profiles; exact 8054 installs before view creation.
     *
     * The smali patch injects this call at the return of MainFragment.onCreateView.
     * Depending on the build profile the first register can be either the fragment
     * (p0) or the inflated view, so never assume it: resolve the fragment first and
     * only then populate. Passing a View straight to populate() threw
     * NoSuchMethodException on getPreferenceScreen and silently removed the whole
     * MOD menu (verified on 8054).
     */
    public static void install(Object first,View root) {
        Object fragment=looksLikeFragment(first)?first:recoverFragment(first);
        if(fragment==null){
            android.util.Log.w(TAG,"install: no fragment found; menu not populated");
            return;
        }
        populate(fragment);
        if(root!=null)bind(fragment,root);
    }

    public static Object recoverFragment(Object first) {
        if(looksLikeFragment(first))return first;
        // The onCreateView hook passes the inflated view, not the fragment.
        // Recover the fragment from the FragmentManager that owns this view.
        View v=(first instanceof View)?(View)first:null;
        Object fromView=findFragmentForView(v);
        if(fromView!=null)return fromView;
        return findVisibleMainFragment();
    }

    /** Walks up the view tree and asks any Fragment's FragmentManager for its view owner. */
    private static Object findFragmentForView(View root) {
        if(root==null)return null;
        java.util.List<Object> candidates=new java.util.ArrayList<>();
        collectFragments(root.getContext(),candidates);
        for(Object f:candidates){
            View fv=null;
            try { fv=(View)callDeep(f,"getView"); } catch(Exception ignored) {}
            if(fv!=null && fv==root)return f;
            if(fv!=null && isSameTree(fv,root))return f;
        }
        return null;
    }

    private static boolean isSameTree(View a,View b) {
        return a.getRootView()==b.getRootView();
    }

    /** Last resort: ask Activity for its FragmentManager and take the resumed fragment. */
    @SuppressWarnings("unchecked")
    private static void collectFragments(Context context,java.util.List<Object> out) {
        try {
            Context ctx=context;
            while(ctx instanceof android.content.ContextWrapper){
                if(ctx instanceof android.app.Activity){
                    android.app.Activity act=(android.app.Activity)ctx;
                    try {
                        Object fm=callDeep(act,"getSupportFragmentManager");
                        Object list=callDeep(fm,"getFragments");
                        if(list instanceof java.util.List){
                            for(Object f:(java.util.List<Object>)list) if(looksLikeFragment(f))out.add(f);
                        }
                    } catch(Exception ignored) {}
                    return;
                }
                ctx=((android.content.ContextWrapper)ctx).getBaseContext();
            }
        } catch(Exception ignored) {}
    }

    private static Object findVisibleMainFragment() {
        return null;
    }

    public static boolean looksLikeFragment(Object candidate) {
        if(candidate==null)return false;
        for(Class<?> c=candidate.getClass();c!=null;c=c.getSuperclass()){
            if("androidx.fragment.app.Fragment".equals(c.getName())
                    ||"android.app.Fragment".equals(c.getName()))return true;
        }
        return false;
    }

    public static void populate(Object fragment) {
        if(fragment==null)return;
        String stage="context";
        try {
            Object ctx=fragment.getClass().getMethod("getContext").invoke(fragment);
            if(!(ctx instanceof Context))throw new IllegalStateException("Fragment has no Android context");
            Context context=(Context)ctx;
            stage="preference-screen";
            // getPreferenceScreen() is not declared public on this build's
            // PreferenceFragmentCompat, so getMethod() throws NoSuchMethodException
            // and the whole MOD menu silently disappeared. Walk the class
            // hierarchy with getDeclaredMethod instead (verified on 8054).
            Object screen=callDeep(fragment,"getPreferenceScreen");
            if(screen==null)throw new IllegalStateException("PreferenceScreen not created");
            stage="androidx-reflection";
            ProjectorMenuSections sections=new ProjectorMenuSections(context,screen);
            Class<?> pref=sections.preference;
            boolean exact="8054".equals(OemHdmiPortInfo.machineId());
            ProjectorMenuSections.optional("inactivity-policy",()->OemScreenTimeoutSafetyHook.ensureNever(context));
            stage="sections";
            sections.add("projectorsoft_picture_category","Изображение · Проектор",210,exact,
                    category->populatePicture(context,category,pref,exact));
            sections.add("projectorsoft_sound_category","Звук · Проектор",230,exact,
                    category->OemSoundPanel.install(context,category,pref));
            sections.add("projectorsoft_remote_category","Bluetooth · Проектор",242,false,
                    category->OemBluetoothRemoteHook.install(context,category,pref));
            sections.add("projectorsoft_placement_category","Проекция · Проектор",250,exact,category->{
                ProjectorMenuSections.optional("projection-orientation",
                        ()->OemProjectorOrientationHook.install(context,category,pref));
                OemHardwareDelegatedSettings.installProjectionLinks(context,category,pref);
            });
            sections.add("projectorsoft_interface_category","Интерфейс · Проектор",255,false,
                    category->OemFontScaleHook.install(context,category,pref));
            sections.add("projectorsoft_power_category","Питание · Проектор",260,exact,category->{
                ProjectorMenuSections.optional("power-ac",()->OemAcPowerBootHook.install(context,category,pref));
                if(exact)ProjectorMenuSections.optional("power-inactivity",
                        ()->OemScreenTimeoutSafetyHook.installStatus(context,category,pref));
                OemStockUserSettings.installSwitchOffTimer(context,category,pref);
            });
            sections.add("projectorsoft_hdmi_category","Источники HDMI · Проектор",270,exact,
                    category->populateSources(context,category,pref,exact));
            sections.add("projectorsoft_system_category","Об устройстве · Проектор",280,exact,category->{
                ProjectorMenuSections.optional("system-navigation",
                        ()->OemStandardAndroidNavigation.install(context,category,pref));
                ProjectorMenuSections.optional("system-info",()->OemSystemInfoHook.install(context,category,pref));
                ProjectorMenuSections.optional("system-firmware",
                        ()->OemHardwareDelegatedSettings.installSystemLinks(context,category,pref));
                if(!exact) {
                    ProjectorMenuSections.optional("system-compatibility",
                            ()->OemCompatibilityDiagnosticsHook.install(context,category,pref));
                    OemScreenTimeoutSafetyHook.installStatus(context,category,pref);
                }
            });
        } catch(Exception | LinkageError problem) {
            Log.w(TAG,"menu stage="+stage,problem);
        }
    }

    public static void bind(Object fragment,View root) {
        ProjectorMenuSections.optional("bind-hdmi",()->OemHdmiPortInfo.bind(fragment,root));
        ProjectorMenuSections.optional("bind-picture",()->OemPicturePanel.bind(fragment,root));
        ProjectorMenuSections.optional("bind-sound-delay",()->OemSoundDelayPanel.bind(fragment,root));
        ProjectorMenuSections.optional("bind-projection",()->OemProjectionSwitches.bind(fragment,root));
        ProjectorMenuSections.optional("bind-panel-info",()->OemPanelInfoHook.bind(fragment,root));
        ProjectorMenuSections.optional("bind-sound",()->OemSoundPanel.bind(fragment,root));
    }

    private static void populatePicture(Context context,Object category,Class<?> pref,boolean exact) {
        if(exact) {
            ProjectorMenuSections.optional("picture-readout",()->OemPicturePanel.install(context,category,pref));
            ProjectorMenuSections.optional("picture-format",
                    ()->OemStockUserSettings.installPictureFormat(context,category,pref));
        } else {
            ProjectorMenuSections.optional("picture-oem",
                    ()->OemHardwareDelegatedSettings.installPictureLink(context,category,pref));
            ProjectorMenuSections.optional("picture-legacy-readout",
                    ()->OemInlineSlidersHook.installPictureReadout(context,category,pref));
        }
        // Keep existing context gates. The 8054 picture values remain read-only.
        if(OemPictureState.mayEdit()) {
            ProjectorMenuSections.optional("picture-mode",()->addList(category,pref,context,
                    "projectorsoft_picture_mode","Режим изображения","picture_mode",PIC_TITLES,PIC_VALUES,7,211));
            ProjectorMenuSections.optional("picture-temperature",()->addList(category,pref,context,
                    "projectorsoft_color_temp","Цветовая температура","picture_color_temperature",
                    TEMP_TITLES,TEMP_VALUES,2,212));
            ProjectorMenuSections.optional("picture-hdr",()->addSwitchIfPresent(category,pref,context,
                    "projectorsoft_hdr","HDR (OEM)","picture_list_hdr",230));
            ProjectorMenuSections.optional("picture-memc",()->{
                String key="tv_picture_advance_video_mjc_effect";
                String value=Settings.Global.getString(context.getContentResolver(),key);
                if(value!=null && value.matches("[0-3]"))
                    addList(category,pref,context,"projectorsoft_memc","MEMC (OEM)",key,
                        new String[]{"Выключено","Низкий","Средний","Высокий"},
                        new int[]{0,1,2,3},Integer.parseInt(value),231);
            });
        }
        ProjectorMenuSections.optional("picture-sliders",()->OemInlineSlidersHook.installPicture(context,category,pref));
        // AISR and Live Tone depend on the video path; owner verifies on HDMI.
        OemPictureEnhanceHook.install(context,category,pref);
        if(!exact || OemPictureState.mayEdit())
            ProjectorMenuSections.optional("picture-state",()->OemPictureState.install(context,category,pref));
    }

    private static void populateSources(Context context,Object category,Class<?> pref,boolean exact) {
        ProjectorMenuSections.optional("hdmi-ports",()->OemHdmiPortInfo.install(context,category,pref));
        ProjectorMenuSections.optional("hdmi-edid",()->OemHdmiEdidHook.install(context,category,pref));
        ProjectorMenuSections.optional("hdmi-autoplay",()->OemHdmiAutoPlayHook.install(context,category,pref));
        ProjectorMenuSections.optional("hdmi-boot-source",()->OemPowerOnSourceHook.install(context,category,pref));
        boolean hasBootApp=ProjectorMenuSections.available("hdmi-boot-app",
                ()->OemPowerOnAppHook.install(context,category,pref));
        boolean hasCec=ProjectorMenuSections.available("hdmi-cec",
                ()->exact ? OemCecAutoOff8054.install(context,category,pref)
                          : OemHdmiCecHook.install(context,category,pref));
        OemHardwareDelegatedSettings.installSourceLinks(context,category,pref,hasCec,hasBootApp);
        // Game Mode and ALLM belong here: they matter when a console or PC is
        // attached, not while watching movies.
        OemGameModeHook.install(context,category,pref);
    }

    static void populateSound(Context context,Object category,Class<?> pref,Runnable soundModeChanged) throws Exception {
        populateSoundProfile(context,category,pref,soundModeChanged);
        populateSoundProcessing(context,category,pref);
        OemKeyToneHook.install(context,category,pref);
        if (!"8054".equals(OemHdmiPortInfo.machineId())) {
            OemEarcDelegatedPreference.install(context,category,pref);
        }
        // Bass/treble sliders removed: they write the same DSP-only keys as the
        // old five-band panel and do nothing on Bluetooth or HDMI.
        // OEM five-band EQ only reaches the MediaTek DSP and is silent on
        // Bluetooth (verified on 8054). The audiofx Equalizer works through
        // AudioFlinger, so it applies to every output including BT.
        // OemFiveBandEqualizerHook.install(context,category,pref);
        // Equalizer is offered on its own page via BtEqualizerPanel only.
        // Output routing needs @SystemApi, so it only works inside this
        // system-uid settings APK.
        // Output routing is offered as a dropdown on the Sound page.
    }

    static void populateSoundProfile(Context context,Object category,Class<?> pref,Runnable modeChanged)
            throws Exception {
        addList(category,pref,context,"projectorsoft_sound_mode",
                "Звуковой профиль","sound_style",SOUND_TITLES,SOUND_VALUES,1,213,modeChanged);
    }

    static void populateSoundProcessing(Context context,Object category,Class<?> pref) throws Exception {

            // Factory SoundFragment and SoundSubMenuController implement
            // Surround and Auto-Volume as plain Settings.Global 0/1 writes.
            // Expose those two only when the OEM already saved a 0/1 value.
            addSwitchIfPresent(category,pref,context,
                    "projectorsoft_surround","Объёмное звучание",
                    "sound_sound_surround",217);
            String autoVolume=vendorKey("KEY_SOUND_AUTO_VOLUME_CONTROL");
            if(autoVolume!=null) addSwitchIfPresent(category,pref,context,
                    "projectorsoft_auto_volume","Автоматическая громкость",
                    autoVolume,218);
    }

    static String readAutoVolume(Context context) {
        try {
            String key=vendorKey("KEY_SOUND_AUTO_VOLUME_CONTROL");
            return key==null ? null : Settings.Global.getString(context.getContentResolver(),key);
        } catch (RuntimeException unavailable) { return null; }
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
        if((oemKey.startsWith("picture_")||oemKey.startsWith("tv_picture_"))&&!OemPictureState.mayEdit())return;
        ContentResolver resolver=context.getContentResolver();
        final String openedMode=Settings.Global.getString(resolver,"sound_style");
        final boolean userOnlySurround = "sound_sound_surround".equals(oemKey);
        String initial=Settings.Global.getString(resolver,oemKey);
        if (!"0".equals(initial) && !"1".equals(initial)) return;
        String[] displayed = {initial};
        Class<?> switchType=Class.forName("androidx.preference.SwitchPreference");
        Object item=switchType.getConstructor(Context.class).newInstance(context);
        pref.getMethod("setKey",String.class).invoke(item,uiKey);
        pref.getMethod("setTitle",CharSequence.class).invoke(item,label);
        pref.getMethod("setOrder",int.class).invoke(item,order);
        pref.getMethod("setPersistent",boolean.class).invoke(item,false);
        switchType.getMethod("setChecked",boolean.class).invoke(item,"1".equals(initial));
        if (userOnlySurround && !"0".equals(openedMode)) {
            pref.getMethod("setEnabled",boolean.class).invoke(item,false);
            pref.getMethod("setSummary",CharSequence.class).invoke(item,
                    "Только просмотр · изменение доступно в пользовательском профиле");
        }
        Class<?> listener=Class.forName("androidx.preference.Preference$OnPreferenceChangeListener");
        InvocationHandler callback=(proxy,method,args)-> {
            if (method.getName().equals("onPreferenceChange") && args!=null && args.length>=2) {
                try {
                    if (!(args[1] instanceof Boolean)) return false;
                    int target=(Boolean)args[1]?1:0;
                    String before=Settings.Global.getString(resolver,oemKey);
                    if (!displayed[0].equals(before)) return false;
                    if (!oemKey.startsWith("picture_") && !oemKey.startsWith("tv_picture_")
                            && !openedModeMatches(resolver,openedMode)) return false;
                    if (userOnlySurround && !OemSoundValuePolicy.acceptsBandChange(
                            before, target, 0, 1, openedMode,
                            Settings.Global.getString(resolver,"sound_style"))) return false;
                    if (before.equals(Integer.toString(target))) return true;
                    if (!Settings.Global.putInt(resolver,oemKey,target)) return false;
                    if (Settings.Global.getInt(resolver,oemKey,-999)!=target) return false;
                    displayed[0]=Integer.toString(target);
                    return true;
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

    private static boolean openedModeMatches(ContentResolver resolver,String openedMode) {
        return openedMode!=null && openedMode.matches("[0-6]")
                && openedMode.equals(Settings.Global.getString(resolver,"sound_style"));
    }

    private static void addList(Object category,Class<?> pref,Context context,
                                String uiKey,String title,String oemKey,
                                String[] labels,int[] values,int defaultValue,
                                int order) throws Exception {
        addList(category,pref,context,uiKey,title,oemKey,labels,values,defaultValue,order,null);
    }

    private static void addList(Object category,Class<?> pref,Context context,
                                String uiKey,String title,String oemKey,
                                String[] labels,int[] values,int defaultValue,
                                int order,Runnable soundModeChanged) throws Exception {
        if (labels.length!=values.length || labels.length==0)
            throw new IllegalArgumentException("Invalid factory mode table");
        Map<Integer,String> valueToTitle=new HashMap<>();
        CharSequence[] entryValues=new CharSequence[values.length];
        for (int i=0;i<values.length;i++) {
            if (valueToTitle.put(values[i],labels[i])!=null)
                throw new IllegalArgumentException("Duplicate OEM mode ID");
            entryValues[i]=Integer.toString(values[i]);
        }
        if((oemKey.startsWith("picture_")||oemKey.startsWith("tv_picture_"))&&!OemPictureState.mayEdit())return;
        ContentResolver resolver=context.getContentResolver();
        final String openedMode=Settings.Global.getString(resolver,"sound_style");
        String raw=Settings.Global.getString(resolver,oemKey);
        if(raw==null)return;
        final int current;
        try {current=Integer.parseInt(raw);} catch(NumberFormatException ex){return;}
        if (!valueToTitle.containsKey(current)) return; // don't invent vendor state
        String[] displayed = {raw};

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
                    String before=Settings.Global.getString(resolver,oemKey);
                    if (!displayed[0].equals(before)) return false;
                    if (Integer.toString(requested).equals(before)) return true;
                    if (oemKey.startsWith("picture_")) {
                        if(!OemPictureState.write(context,oemKey,requested))return false;
                    } else {
                        if("sound_style".equals(oemKey)&&!OemSoundValuePolicy.acceptsControlChange(oemKey,Settings.Global.getString(resolver,oemKey),requested,openedMode,Settings.Global.getString(resolver,"sound_style")))return false;
                        if(!Settings.Global.putInt(resolver,oemKey,requested))return false;
                    }
                    if (Settings.Global.getInt(resolver,oemKey,-999)!=requested) return false;
                    displayed[0]=Integer.toString(requested);
                    pref.getMethod("setSummary",CharSequence.class)
                            .invoke(preference,valueToTitle.get(requested));
                    if ("sound_style".equals(oemKey) && requested != current && soundModeChanged != null) {
                        soundModeChanged.run();
                    }
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

    /**
     * Calls a no-arg method by walking the whole class hierarchy.
     * getMethod() only sees public members, and several androidx and OEM
     * members are package-private on this build. Verified on 8054.
     */
    public static Object callDeep(Object target,String name) throws Exception {
        for(Class<?> c=target.getClass();c!=null;c=c.getSuperclass()){
            try {
                java.lang.reflect.Method m=c.getDeclaredMethod(name);
                m.setAccessible(true);
                return m.invoke(target);
            } catch(NoSuchMethodException ignored){
                // keep walking up
            }
        }
        throw new NoSuchMethodException(name+" on "+target.getClass().getName());
    }
}

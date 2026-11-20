package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.WeakHashMap;

/** Native speaker and SPDIF delay preferences backed by the original Settings/TvServices route. */
final class OemSoundDelayPanel implements View.OnAttachStateChangeListener,
        ViewTreeObserver.OnWindowFocusChangeListener {
    private static final String TAG="TvSettingsPlusMOD";
    private static final String GROUP="projectorsoft_sound_category";
    private static final WeakHashMap<Object,WeakReference<OemSoundDelayPanel>> PANELS=new WeakHashMap<>();
    private final List<Row> rows=new ArrayList<>();
    private WeakReference<View> root=new WeakReference<>(null);
    private ViewTreeObserver observer;

    static void install(Context context,Object group,Class<?> pref) {
        if (!"8054".equals(OemHdmiPortInfo.machineId())) return;
        OemSoundDelayPanel panel=new OemSoundDelayPanel();
        panel.add(context,group,pref,"sound_speaker_delay","g_audio__speaker_delay","Задержка динамиков",242);
        panel.add(context,group,pref,"sound_spdif_delay","g_audio__spdif_delay","Задержка SPDIF",243);
        if (!panel.rows.isEmpty()) PANELS.put(group,new WeakReference<>(panel));
    }

    static String speakerUnavailableReason() {
        try {
            return available("sound_speaker_delay") ? "Штатный диапазон или значение недоступны"
                    : "Штатный профиль устройства скрывает эту настройку";
        } catch (ReflectiveOperationException | RuntimeException unavailable) {
            return "Не удалось проверить поддержку динамиков";
        }
    }

    private static boolean available(String key) throws ReflectiveOperationException {
        boolean speaker="sound_speaker_delay".equals(key);
        Class<?> stock=Class.forName("com.mediatek.wwtv.tvcenter.util.PartnerSettingsConfig");
        Object keys=stock.getMethod("getSettingsList",String.class)
                .invoke(null,speaker?"sound_effects":"audio_output");
        if (!(keys instanceof List)) return false;
        if (!speaker) return SoundDelayState.stockOffersSpdif((List<?>)keys);
        Class<?> properties=Class.forName("android.os.SystemProperties");
        Object showAll=properties.getMethod("get",String.class,String.class)
                .invoke(null,"persist.sys.settings.all","false");
        return SoundDelayState.stockOffersSpeaker((List<?>)keys,showAll);
    }

    private void add(Context context,Object group,Class<?> pref,String key,String configId,String title,int order) {
        try {
            if (!available(key)) return;
            Row row=new Row(context,pref,key,configId,title,order);
            if (Boolean.TRUE.equals(group.getClass().getMethod("addPreference",pref).invoke(group,row.item)))
                rows.add(row);
        } catch (Exception | LinkageError error) {
            Log.w(TAG,"Sound delay editor unavailable for "+key,error);
        }
    }

    static void bind(Object fragment,View view) {
        if (fragment==null || !"8054".equals(OemHdmiPortInfo.machineId())) return;
        try {
            Object group=fragment.getClass().getMethod("findPreference",CharSequence.class).invoke(fragment,GROUP);
            WeakReference<OemSoundDelayPanel> reference=PANELS.get(group);
            OemSoundDelayPanel panel=reference==null?null:reference.get();
            if (panel!=null) panel.rebind(view);
        } catch (Exception error) {
            Log.w(TAG,"Sound delay view unavailable",error);
        }
    }

    static void refresh(Object group) {
        WeakReference<OemSoundDelayPanel> reference=PANELS.get(group);
        OemSoundDelayPanel panel=reference==null?null:reference.get();
        if (panel!=null) panel.refresh();
    }

    private void rebind(View view) {
        View previous=root.get();
        if (view==previous) return;
        if (previous!=null) {
            onViewDetachedFromWindow(previous);
            previous.removeOnAttachStateChangeListener(this);
        }
        root=new WeakReference<>(view);
        if(view==null)return;
        view.addOnAttachStateChangeListener(this);
        if (view.isAttachedToWindow()) onViewAttachedToWindow(view);
    }

    @Override public void onViewAttachedToWindow(View view) {
        if (view!=root.get() || observer!=null) return;
        observer=view.getViewTreeObserver();
        observer.addOnWindowFocusChangeListener(this);
        refresh();
    }

    @Override public void onViewDetachedFromWindow(View view) {
        if (view!=root.get() || observer==null) return;
        ViewTreeObserver active=observer.isAlive() ? observer : view.getViewTreeObserver();
        if (active.isAlive()) active.removeOnWindowFocusChangeListener(this);
        observer=null;
    }

    @Override public void onWindowFocusChanged(boolean focused) {
        if (focused && observer!=null) refresh();
    }

    private void refresh() {
        for (Row row:rows) row.refresh();
    }

    private static final class Bridge implements SoundDelayState.Backend {
        private final Context context;
        private final String key, configId;
        private final Object config;
        private final Method getMin, getMax, getValue;

        Bridge(Context context,String key,String configId) throws Exception {
            this.context=context;this.key=key;this.configId=configId;
            Class<?> type=Class.forName("com.android.tv.settings.partnercustomizer.tvsettingservice.TVSettingConfig");
            config=type.getMethod("getInstance",Context.class).invoke(null,context);
            if (config==null) throw new IllegalStateException("OEM audio configuration unavailable");
            getMin=type.getMethod("getMin",String.class);
            getMax=type.getMethod("getMax",String.class);
            getValue=type.getMethod("getConfigValueInt",String.class);
        }

        @Override public SoundDelayState.Snapshot read() throws Exception {
            if (!available(key)) return null;
            List<String> selectors=selectors();
            String raw=Settings.Global.getString(context.getContentResolver(),key);
            int min=(Integer)getMin.invoke(config,configId);
            int max=(Integer)getMax.invoke(config,configId);
            int applied=(Integer)getValue.invoke(config,configId);
            if (!selectors.equals(selectors())) return null;
            return new SoundDelayState.Snapshot(min,max,raw,applied,selectors);
        }

        private List<String> selectors() {
            android.content.ContentResolver resolver=context.getContentResolver();
            return Arrays.asList(Settings.Global.getString(resolver,"sound_style"),
                    Settings.Global.getString(resolver,"sound_digital_output_to_device"),
                    Settings.Global.getString(resolver,"sound_spdif_type"));
        }

        @Override public boolean writeProvider(int milliseconds) {
            return Settings.Global.putInt(context.getContentResolver(),key,milliseconds);
        }
    }

    private final class Row {
        private final Handler main=new Handler(Looper.getMainLooper());
        private final Class<?> pref,bar;
        private final Object item;
        private final Bridge bridge;
        private SoundDelayState.Snapshot displayed;
        private boolean pending;

        Row(Context context,Class<?> pref,String key,String configId,String title,int order) throws Exception {
            this.pref=pref;
            bridge=new Bridge(context,key,configId);
            bar=Class.forName("androidx.preference.SeekBarPreference");
            item=bar.getConstructor(Context.class).newInstance(context);
            pref.getMethod("setKey",String.class).invoke(item,"projectorsoft_"+key);
            pref.getMethod("setTitle",CharSequence.class).invoke(item,title);
            pref.getMethod("setOrder",int.class).invoke(item,order);
            pref.getMethod("setPersistent",boolean.class).invoke(item,false);
            bar.getMethod("setUpdatesContinuously",boolean.class).invoke(item,false);
            Class<?> listener=Class.forName("androidx.preference.Preference$OnPreferenceChangeListener");
            Object callback=Proxy.newProxyInstance(listener.getClassLoader(),new Class<?>[]{listener},
                    (proxy,method,args)-> {
                        switch (method.getName()) {
                            case "onPreferenceChange":
                                if (args!=null && args.length>=2 && args[1] instanceof Integer)
                                    try {change((Integer)args[1]);}
                                    catch (ReflectiveOperationException error) {Log.w(TAG,"Cannot render audio delay",error);}
                                // Render only from an obtained OEM snapshot.
                                return false;
                            case "hashCode": return System.identityHashCode(proxy);
                            case "equals": return args!=null && args.length==1 && proxy==args[0];
                            case "toString": return "OemSoundDelay:"+key;
                            default: return null;
                        }
                    });
            pref.getMethod("setOnPreferenceChangeListener",listener).invoke(item,callback);
            refresh();
        }

        private void change(int tick) throws ReflectiveOperationException {
            SoundDelayState.Snapshot original=displayed;
            if (pending || original==null || !"8054".equals(OemHdmiPortInfo.machineId())) return;
            long candidate=(long)original.minMs+(long)tick*SoundDelayState.STEP_MS;
            if (candidate<Integer.MIN_VALUE || candidate>Integer.MAX_VALUE) return;
            int requested=(int)candidate;
            SoundDelayState.Write result=SoundDelayState.change(bridge,original,requested);
            if (result==SoundDelayState.Write.PENDING) {
                pending=true;
                poll(original,requested,0);
                render(null,"Ожидание применения");
            } else {
                refresh();
                if (result!=SoundDelayState.Write.APPLIED)
                    summary("Изменение не подтверждено");
            }
        }

        private void poll(SoundDelayState.Snapshot original,int requested,int attempt) {
            main.postDelayed(()->{
                SoundDelayState.Write observed=SoundDelayState.observe(bridge,original,requested);
                if (observed==SoundDelayState.Write.PENDING && attempt<2) {
                    poll(original,requested,attempt+1);
                    return;
                }
                pending=false;
                refresh();
                if (observed!=SoundDelayState.Write.APPLIED) {
                    try {summary("Применение задержки не подтверждено");}
                    catch (ReflectiveOperationException error) {Log.w(TAG,"Sound delay result unavailable",error);}
                }
            },200L*(attempt+1));
        }

        void refresh() {
            if (pending) return;
            try {
                SoundDelayState.Snapshot snapshot;
                try {snapshot=bridge.read();}
                catch (Exception error) {snapshot=null;}
                render(snapshot,snapshot==null || snapshot.storedMs==null ? "Значение недоступно"
                        : !snapshot.synchronizedValue() ? "Ожидание согласования TvServices"
                        : snapshot.storedMs+" мс");
            } catch (ReflectiveOperationException error) {
                Log.w(TAG,"Sound delay readout unavailable",error);
            }
        }

        private void render(SoundDelayState.Snapshot state,String text) throws ReflectiveOperationException {
            displayed=state;
            boolean enabled=state!=null && state.synchronizedValue();
            pref.getMethod("setEnabled",boolean.class).invoke(item,enabled);
            if (enabled) {
                bar.getMethod("setMax",int.class).invoke(item,(state.maxMs-state.minMs)/SoundDelayState.STEP_MS);
                bar.getMethod("setValue",int.class).invoke(item,(state.storedMs-state.minMs)/SoundDelayState.STEP_MS);
            }
            summary(text);
        }

        private void summary(String text) throws ReflectiveOperationException {
            pref.getMethod("setSummary",CharSequence.class).invoke(item,text);
        }
    }
}

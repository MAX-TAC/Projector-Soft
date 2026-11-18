package dev.projectorsoft.tvsettings;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class OemSoundPanel {
    private final Context context;
    private final Object group;
    private final Class<?> preference;
    private final SoundPanelState state=new SoundPanelState();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final List<Object> staticRows = new ArrayList<>();
    private Object refreshItem;
    private OemSoundPanel(Context context,Object group,Class<?> preference) {
        this.context=context;this.group=group;this.preference=preference;
    }
    public static void install(Context context,Object group,Class<?> preference) throws Exception {
        new OemSoundPanel(context,group,preference).install();
    }
    private void install() throws Exception {
        OemHardwareDelegatedSettings.installSoundLink(context,group,preference);
        OemStockUserSettings.installAudioOutput(context,group,preference);
        try {OemSoundDelayPanel.install(context,group,preference);}
        catch (Exception | LinkageError ex) {Log.w("TvSettingsPlusMOD","OEM audio delay unavailable",ex);}
        refreshItem = action("projectorsoft_sound_refresh","Обновить настройки звука",249,this::refresh);
        action("projectorsoft_sound_original","Исходные значения звука",248,this::showOriginal);
        // PreferenceGroup sorts by order. Keep the static rows by identity, not by index.
        Class<?> type = group.getClass();
        int count = (Integer) type.getMethod("getPreferenceCount").invoke(group);
        Method child = type.getMethod("getPreference",int.class);
        for (int i = 0; i < count; i++) {
            staticRows.add(child.invoke(group,i));
        }
        refresh();
    }
    private void refreshAfterModeChange() {
        main.post(() -> {
            try {
                refresh();
            } catch (Exception ex) {
                Log.w("TvSettingsPlusMOD","Sound refresh unavailable",ex);
            }
        });
    }

    private void refresh() throws Exception {
        OemSoundDelayPanel.refresh(group);
        boolean stable=state.refresh(key->Settings.Global.getString(context.getContentResolver(),key));
        Class<?> type = group.getClass();
        Method child = type.getMethod("getPreference",int.class);
        Method remove = type.getMethod("removePreference",preference);
        int count = (Integer) type.getMethod("getPreferenceCount").invoke(group);
        for (int i = count - 1; i >= 0; i--) {
            Object row = child.invoke(group,i);
            if (!staticRows.contains(row)) remove.invoke(group,row);
        }
        preference.getMethod("setSummary",CharSequence.class).invoke(refreshItem,
                stable ? "Перечитать режим и значения" : "Режим недоступен или изменился. Повторите обновление");
        try {
            addMediaVolume();
        } catch (Exception ex) {
            Log.w("TvSettingsPlusMOD","Media volume unavailable",ex);
        }
        if(stable)try {OemInlinePreferencesHook.populateSound(context,group,preference,this::refreshAfterModeChange);}
        catch(Exception ex){Log.w("TvSettingsPlusMOD","Partial sound UI; keeping OEM link and refresh",ex);}
    }

    private void addMediaVolume() throws Exception {
        if (!"8054".equals(OemHdmiPortInfo.machineId())
                || context.checkSelfPermission(android.Manifest.permission.MODIFY_AUDIO_SETTINGS)
                    != PackageManager.PERMISSION_GRANTED) return;
        AudioManager audio = context.getSystemService(AudioManager.class);
        if (audio == null || audio.isVolumeFixed()) return;
        int stream = AudioManager.STREAM_MUSIC;
        int min = audio.getStreamMinVolume(stream);
        int max = audio.getStreamMaxVolume(stream);
        int current = audio.getStreamVolume(stream);
        if (min >= max || current < min || current > max) return;

        Class<?> bar = Class.forName("androidx.preference.SeekBarPreference");
        Object item = bar.getConstructor(Context.class).newInstance(context);
        preference.getMethod("setKey",String.class).invoke(item,"projectorsoft_media_volume");
        preference.getMethod("setTitle",CharSequence.class).invoke(item,"Громкость мультимедиа");
        preference.getMethod("setOrder",int.class).invoke(item,212);
        preference.getMethod("setPersistent",boolean.class).invoke(item,false);
        bar.getMethod("setMin",int.class).invoke(item,min);
        bar.getMethod("setMax",int.class).invoke(item,max);
        bar.getMethod("setValue",int.class).invoke(item,current);
        bar.getMethod("setShowSeekBarValue",boolean.class).invoke(item,true);
        bar.getMethod("setUpdatesContinuously",boolean.class).invoke(item,false);

        Class<?> listener = Class.forName("androidx.preference.Preference$OnPreferenceChangeListener");
        Object callback = Proxy.newProxyInstance(listener.getClassLoader(),new Class<?>[]{listener},
                (proxy,method,args) -> {
                    switch (method.getName()) {
                        case "onPreferenceChange":
                            if (!(args[1] instanceof Integer)) return false;
                            int target = (Integer) args[1];
                            try {
                                if (target < min || target > max || audio.isVolumeFixed()
                                        || min != audio.getStreamMinVolume(stream)
                                        || max != audio.getStreamMaxVolume(stream)) return false;
                                audio.setStreamVolume(stream,target,0);
                                return audio.getStreamVolume(stream) == target;
                            } catch (RuntimeException ex) {
                                Log.w("TvSettingsPlusMOD","Media volume change unavailable",ex);
                                return false;
                            }
                        case "hashCode": return System.identityHashCode(proxy);
                        case "equals": return proxy == args[0];
                        case "toString": return "MediaVolume";
                        default: return null;
                    }
                });
        preference.getMethod("setOnPreferenceChangeListener",listener).invoke(item,callback);
        group.getClass().getMethod("addPreference",preference).invoke(group,item);
    }
    private void showOriginal() {
        String[] titles={"Режим","Низкие частоты","Высокие частоты","Объёмное звучание",
            "EQ 120 Гц","EQ 500 Гц","EQ 1,5 кГц","EQ 5 кГц","EQ 10 кГц"};
        Map<String,String> original=state.initial();
        StringBuilder text=new StringBuilder();
        for(int i=0;i<titles.length;i++) {
            String value=original.get(SoundPanelState.KEYS[i]);
            if(i==0&&value!=null&&value.matches("[0-6]")) {
                String[] modes={"Пользовательский","Стандартный","Яркий","Спорт","Кино","Музыка","Новости"};
                value=modes[Integer.parseInt(value)];
            }
            text.append(titles[i]).append(": ").append(value==null?"недоступно":value).append('\n');
        }
        text.append("\nСнимок действует только для этой страницы. При смене выхода или режима значения могут относиться к другому профилю. Автоматическое восстановление недоступно.");
        new AlertDialog.Builder(context).setTitle("Исходные значения звука").setMessage(text.toString())
            .setPositiveButton("Закрыть",null).show();
    }
    private interface Action {void run() throws Exception;}
    private Object action(String key,String title,int order,Action action) throws Exception {
        Object item=preference.getConstructor(Context.class).newInstance(context);
        preference.getMethod("setKey",String.class).invoke(item,key);
        preference.getMethod("setTitle",CharSequence.class).invoke(item,title);
        preference.getMethod("setOrder",int.class).invoke(item,order);
        Class<?> listener=Class.forName("androidx.preference.Preference$OnPreferenceClickListener");
        Object callback=Proxy.newProxyInstance(listener.getClassLoader(),new Class<?>[]{listener},(proxy,method,args)->{
            if("onPreferenceClick".equals(method.getName())) {
                try {action.run();}catch(Exception ex){Log.w("TvSettingsPlusMOD","Sound refresh unavailable",ex);}
                return true;
            }
            if("hashCode".equals(method.getName()))return System.identityHashCode(proxy);
            if("equals".equals(method.getName()))return args!=null&&args.length>0&&proxy==args[0];
            return "SoundPanelAction:"+key;
        });
        preference.getMethod("setOnPreferenceClickListener",listener).invoke(item,callback);
        group.getClass().getMethod("addPreference",preference).invoke(group,item);
        return item;
    }
}

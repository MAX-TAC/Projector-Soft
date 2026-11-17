package dev.projectorsoft.tvsettings;
import android.app.AlertDialog;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import java.lang.reflect.Proxy;
import java.util.Map;

/** Explicit refresh avoids retaining a ContentObserver after fragment destruction. */
public final class OemSoundPanel {
    private final Context context;
    private final Object group;
    private final Class<?> preference;
    private final SoundPanelState state=new SoundPanelState();
    private OemSoundPanel(Context context,Object group,Class<?> preference) {
        this.context=context;this.group=group;this.preference=preference;
    }
    public static void install(Context context,Object group,Class<?> preference) throws Exception {
        new OemSoundPanel(context,group,preference).refresh();
    }
    private void refresh() throws Exception {
        boolean stable=state.refresh(key->Settings.Global.getString(context.getContentResolver(),key));
        group.getClass().getMethod("removeAll").invoke(group);
        if(stable)try {OemInlinePreferencesHook.populateSound(context,group,preference);}
        catch(Exception ex){Log.w("TvSettingsPlusMOD","Partial sound UI; keeping OEM link and refresh",ex);}
        OemHardwareDelegatedSettings.installSoundLink(context,group,preference);
        action("projectorsoft_sound_refresh","Обновить настройки звука",
            stable?"Перечитать значения после смены режима":"Режим недоступен или изменился. Повторите обновление",249,()->refresh());
        action("projectorsoft_sound_original","Исходные значения звука",
            "Значения при открытии страницы; восстановление через штатные настройки",248,()->showOriginal());
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
    private void action(String key,String title,String summary,int order,Action action) throws Exception {
        Object item=preference.getConstructor(Context.class).newInstance(context);
        preference.getMethod("setKey",String.class).invoke(item,key);
        preference.getMethod("setTitle",CharSequence.class).invoke(item,title);
        preference.getMethod("setSummary",CharSequence.class).invoke(item,summary);
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
    }
}

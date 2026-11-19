package dev.projectorsoft.tvsettings;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.provider.Settings;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Adaptive backend. The Activity and its complete menu can run without OEM or AndroidX. */
final class ModMenuBackend {
    private static final String TAG="TvSettingsPlusMOD";
    private static final String PREFIX="projectorsoft_";
    private static final String OWN=ModMenuCatalog.PACKAGE;
    private final Activity activity;
    private final ModMenuCatalog.Page page;
    private final boolean exact;
    private Class<?> preference;
    private Object screen;
    private Map<String,Integer> picture=Collections.emptyMap();

    static final class State {
        final String detail;
        final Runnable action;
        State(String detail,Runnable action){this.detail=detail;this.action=action;}
    }

    ModMenuBackend(Activity activity,ModMenuCatalog.Page page){
        this.activity=activity;
        this.page=page;
        exact="8054".equals(OemHdmiPortInfo.machineId());
    }

    void load() {
        if(!exact)return;
        if(page==ModMenuCatalog.Page.PICTURE) {
            safe("picture-snapshot",()->{
                Map<String,Integer> values=PicturePanelState.read(key->
                        Settings.Global.getString(activity.getContentResolver(),key));
                if(values!=null)picture=values;
            });
        }
        if(page==ModMenuCatalog.Page.NETWORK || page==ModMenuCatalog.Page.APPS)return;
        safe("androidx-backend",()->{
            ClassLoader loader=activity.getClassLoader();
            Class<?> manager=Class.forName("androidx.preference.PreferenceManager",true,loader);
            preference=Class.forName("androidx.preference.Preference",true,loader);
            Object instance=manager.getConstructor(Context.class).newInstance(activity);
            screen=manager.getMethod("createPreferenceScreen",Context.class).invoke(instance,activity);
            loadPreferences();
        });
    }

    private void loadPreferences() {
        switch(page) {
            case BLUETOOTH:
                OemBluetoothRemoteHook.install(activity,screen,preference);
                break;
            case SOURCES:
                safe("hdmi",()->OemHdmiPortInfo.install(activity,screen,preference));
                safe("cec",()->OemCecAutoOff8054.install(activity,screen,preference));
                safe("edid",()->OemHdmiEdidHook.install(activity,screen,preference));
                safe("hdmi-autoplay",()->OemHdmiAutoPlayHook.install(activity,screen,preference));
                safe("boot-source",()->OemPowerOnSourceHook.install(activity,screen,preference));
                safe("boot-app",()->OemPowerOnAppHook.install(activity,screen,preference));
                break;
            case PROJECTION:
                safe("projection",()->OemProjectionPanel.installUnified(activity,screen,preference));
                break;
            case PICTURE:
                OemStockUserSettings.installPictureFormat(activity,screen,preference);
                break;
            case SOUND:
                safe("sound",()->OemSoundPanel.installUnified(activity,screen,preference));
                break;
            case SYSTEM:
                safe("system-info",()->OemSystemInfoHook.install(activity,screen,preference));
                safe("font-scale",()->OemFontScaleHook.install(activity,screen,preference));
                safe("ac-power",()->OemAcPowerBootHook.install(activity,screen,preference));
                safe("timer",()->OemStockUserSettings.installSwitchOffTimer(activity,screen,preference));
                safe("inactivity-status",()->OemScreenTimeoutSafetyHook.installStatus(activity,screen,preference));
                break;
            default: break;
        }
    }

    State state(ModMenuCatalog.Row row,Runnable refresh){
        try {
            switch(row.kind) {
                case STOCK:
                    Intent route=stock(row.key);
                    return CheckedPreferenceLaunch.accessible(activity,route.getComponent())
                            ? new State("Штатные настройки Android TV",()->open(()->stock(row.key))) : unavailable();
                case BACKEND:
                    if(!exact)return unavailable();
                    if("home".equals(row.key))return home()!=null
                            ? new State("Вернуться на главный экран",()->open(this::home)) : unavailable();
                    return fromPreference(row.key,refresh);
                case READ:
                    return new State(read(row.key),null);
                case BLOCKED:
                    return new State(row.key,null);
                default: return new State("",null);
            }
        } catch(Exception | LinkageError error){
            Log.w(TAG,"menu row unavailable: "+row.key,error);
            return unavailable();
        }
    }

    private String read(String key) throws Exception {
        if(!exact)return "Нет подтверждения платы 8054";
        if("HW6".equals(key) || "HW7".equals(key))return fromPreference("hdmi_info_"+key,()->{}).detail;
        if("autopair".equals(key))return OemBluetoothRemotePolicy.autoPairSettingSummary(
                Settings.Global.getString(activity.getContentResolver(),"autopair_status"));
        if("bluetooth".equals(key) || "paired".equals(key)){
            BluetoothManager service=activity.getSystemService(BluetoothManager.class);
            BluetoothAdapter radio=service==null?null:service.getAdapter();
            if(radio==null)return "Данные Bluetooth недоступны";
            if("bluetooth".equals(key))return radio.isEnabled()?"Включён":"Выключен";
            List<String> names=new ArrayList<>();
            for(BluetoothDevice device:radio.getBondedDevices()) {
                String name=device.getName();
                names.add(name==null || name.trim().isEmpty()?"Без названия":name);
            }
            Collections.sort(names);
            return names.isEmpty()?"Сохранённых устройств нет":String.join(", ",names);
        }
        if("panel".equals(key)){
            String[] current=OemPanelInfoHook.readExisting(activity);
            List<String> values=new ArrayList<>();
            String[] labels={"Тип", "Версия", "Ширина по конфигурации", "Высота по конфигурации"};
            for(int i=0;i<current.length;i++)if(current[i]!=null)values.add(labels[i]+" · "+current[i]);
            return values.isEmpty()?"Кэш панели пока недоступен":String.join("\n",values);
        }
        Integer value=picture.get(key);
        return value==null?"Сохранённое значение недоступно":"Сохранённое значение: "+value+" · только чтение";
    }

    private State fromPreference(String key,Runnable refresh) throws Exception {
        if(screen==null)return unavailable();
        Object row=screen.getClass().getMethod("findPreference",CharSequence.class).invoke(screen,PREFIX+key);
        if(row==null || !Boolean.TRUE.equals(invoke(row,"isVisible")))return unavailable();
        CharSequence summary=(CharSequence)invoke(row,"getSummary");
        String detail=summary==null?"":summary.toString();
        if(!Boolean.TRUE.equals(invoke(row,"isEnabled")) || !Boolean.TRUE.equals(invoke(row,"isSelectable")))
            return new State(detail.isEmpty()?"Только чтение":detail,null);
        // A fragment callback would need the stock Activity host. Treat it as unavailable.
        if(invoke(row,"getFragment")!=null)return unavailable();
        Intent intent=(Intent)invoke(row,"getIntent");
        if(intent!=null && !supportedIntent(intent))return unavailable();
        Object click=invoke(row,"getOnPreferenceClickListener");
        Object change=invoke(row,"getOnPreferenceChangeListener");
        if(click!=null)return new State(detail,()->safe("click-"+key,()->{
            Class<?> type=Class.forName("androidx.preference.Preference$OnPreferenceClickListener");
            type.getMethod("onPreferenceClick",preference).invoke(click,row);
        }));
        if(change!=null && hasMethod(row,"getEntryValues"))return new State(detail,()->safe("list-"+key,
                ()->choose(row,key,refresh)));
        if(change!=null && key.endsWith("_delay") && hasMethod(row,"getMax"))
            return new State(detail,()->safe("delay-"+key,()->chooseDelay(row,detail,refresh)));
        if(change!=null && hasMethod(row,"isChecked")) {
            boolean checked=Boolean.TRUE.equals(invoke(row,"isChecked"));
            return new State((checked?"Включено":"Выключено")+(detail.isEmpty()?"":" · "+detail),
                    ()->new AlertDialog.Builder(activity).setTitle(checked?"Выключить?":"Включить?")
                            .setNegativeButton("Отмена",null)
                            .setPositiveButton("Подтвердить",(d,w)->apply(row,!checked,key,refresh)).show());
        }
        return new State(detail.isEmpty()?ModMenuCatalog.LIMITED:detail,null);
    }

    private void choose(Object row,String key,Runnable refresh) throws Exception {
        CharSequence[] labels=(CharSequence[])invoke(row,"getEntries");
        CharSequence[] values=(CharSequence[])invoke(row,"getEntryValues");
        Object current=invoke(row,"getValue");
        if(labels==null || values==null || labels.length!=values.length || values.length==0)return;
        int selected=-1;
        for(int i=0;i<values.length;i++)if(values[i]!=null && values[i].toString().equals(current))selected=i;
        new AlertDialog.Builder(activity).setTitle((CharSequence)invoke(row,"getTitle"))
                .setSingleChoiceItems(labels,selected,(dialog,which)->{
                    dialog.dismiss();
                    if(values[which]!=null)apply(row,values[which].toString(),key,refresh);
                }).setNegativeButton("Отмена",null).show();
    }

    private void chooseDelay(Object row,String detail,Runnable refresh) throws Exception {
        if(!detail.matches("-?\\d+ мс"))return;
        int current=(Integer)invoke(row,"getValue");
        int max=(Integer)invoke(row,"getMax");
        int base=Integer.parseInt(detail.substring(0,detail.indexOf(' ')))-current*SoundDelayState.STEP_MS;
        if(max<1 || current<0 || current>max)return;
        SeekBar slider=new SeekBar(activity);
        slider.setMax(max);
        slider.setProgress(current);
        slider.setKeyProgressIncrement(1);
        slider.setFocusable(true);
        AlertDialog dialog=new AlertDialog.Builder(activity)
                .setTitle((CharSequence)invoke(row,"getTitle"))
                .setMessage(detail)
                .setView(slider)
                .setNegativeButton("Отмена",null)
                .setPositiveButton("Применить",(d,w)->{
                    int selected=slider.getProgress();
                    if(selected==current)return;
                    safe("sound-delay-change",()->preference.getMethod("callChangeListener",Object.class)
                            .invoke(row,selected));
                    refresh.run();
                    activity.getWindow().getDecorView().postDelayed(refresh,1300);
                }).show();
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar,int tick,boolean fromUser){
                dialog.setMessage((base+tick*SoundDelayState.STEP_MS)+" мс");
            }
            @Override public void onStartTrackingTouch(SeekBar bar){}
            @Override public void onStopTrackingTouch(SeekBar bar){}
        });
    }

    private void apply(Object row,Object value,String key,Runnable refresh) {
        safe("change-"+key,()->{
            Object accepted=preference.getMethod("callChangeListener",Object.class).invoke(row,value);
            if(Boolean.TRUE.equals(accepted)) {
                if(value instanceof String)row.getClass().getMethod("setValue",String.class).invoke(row,value);
                else if(value instanceof Boolean)row.getClass().getMethod("setChecked",boolean.class).invoke(row,value);
            } else {
                Toast.makeText(activity,"Значение не принято. Данные перечитаны.",Toast.LENGTH_SHORT).show();
            }
        });
        activity.getWindow().getDecorView().post(refresh);
    }

    private boolean supportedIntent(Intent intent){
        ComponentName component=intent.getComponent();
        if(component==null)return false;
        String owner=component.getPackageName();
        String name=component.getClassName();
        if(OWN.equals(owner))return name.startsWith(OWN+".device.sound.")
                || name.startsWith(OWN+".partnercustomizer.picture.")
                || name.startsWith(OWN+".partnercustomizer.timer.");
        return "com.rjpj.tcorrection".equals(owner) || Hdmi8054Policy.LIVE_TV.equals(owner);
    }

    private Intent stock(String name){return new Intent().setComponent(new ComponentName(OWN,OWN+"."+name));}

    private Intent home(){
        Intent home=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        ResolveInfo info=activity.getPackageManager().resolveActivity(home,0);
        if(info==null || info.activityInfo==null || info.activityInfo.applicationInfo==null
                || (info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)==0
                || OWN.equals(info.activityInfo.packageName) || "com.my.settings".equals(info.activityInfo.packageName)
                || "android".equals(info.activityInfo.packageName))return null;
        return home.setComponent(new ComponentName(info.activityInfo.packageName,info.activityInfo.name));
    }

    private void open(CheckedPreferenceLaunch.Route route){
        if(!CheckedPreferenceLaunch.open(activity,route))
            Toast.makeText(activity,"Экран недоступен",Toast.LENGTH_SHORT).show();
    }

    private static Object invoke(Object row,String method) throws ReflectiveOperationException {
        return row.getClass().getMethod(method).invoke(row);
    }
    private static boolean hasMethod(Object row,String method){
        try {row.getClass().getMethod(method);return true;} catch(NoSuchMethodException absent){return false;}
    }
    private static State unavailable(){return new State(ModMenuCatalog.LIMITED,null);}
    private static void safe(String stage,ProjectorMenuSections.Work work){
        ProjectorMenuSections.optional("unified-"+stage,work);
    }
}

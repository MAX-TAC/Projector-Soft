package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.util.Log;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceScreen;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/** A single confirmed destination for the shared OEM persist.trd.source_id property. */
final class NativeBootDestination {
    private static final String TAG="TvSettingsPlusMOD";
    enum Result { CONFIRMED, STALE, UNSUPPORTED, UNCONFIRMED }
    private NativeBootDestination(){}

    private static Map<String,String> choices(Context context) throws Exception {
        Map<String,String> known=OemPowerOnSourceHook.available(context);
        Map<String,String> result=new LinkedHashMap<>();
        for(Map.Entry<String,String> entry:known.entrySet()) {
            String key=entry.getKey();
            if("HOME".equals(key) || key.endsWith("/HW6") || key.endsWith("/HW7"))
                result.put(key,entry.getValue());
        }
        if(!result.containsKey("HOME"))return result;
        for(Map.Entry<String,String> entry:new TreeMap<>(OemPowerOnAppHook.candidates(context)).entrySet())
            result.putIfAbsent(entry.getKey(),"Приложение · "+entry.getValue());
        return result;
    }

    static Result apply(OemPowerOnAppPolicy.Property backend,Map<String,String> displayedChoices,
                        Map<String,String> freshChoices,String before,String requested) throws Exception {
        if(!displayedChoices.containsKey(requested) || !freshChoices.containsKey(requested))
            return Result.UNSUPPORTED;
        String current=backend.read();
        if(before==null || !before.equals(current))return Result.STALE;
        if(requested.equals(current))return Result.CONFIRMED;
        boolean completed=backend.write(requested);
        return completed && requested.equals(backend.read())?Result.CONFIRMED:Result.UNCONFIRMED;
    }

    static void install(Context context,PreferenceScreen screen) throws Exception {
        if(!"8054".equals(OemHdmiPortInfo.machineId()))return;
        OemPowerOnAppPolicy.Property backend=OemPowerOnAppHook.property();
        Map<String,String> offered=choices(context);
        String current=backend.read();
        if(offered.size()<2 || !offered.containsKey(current))return;
        ListPreference row=new ListPreference(context);
        row.setKey("projectorsoft_boot_destination");
        row.setTitle("При включении");
        row.setPersistent(false);
        row.setEntries(offered.values().toArray(new CharSequence[0]));
        row.setEntryValues(offered.keySet().toArray(new CharSequence[0]));
        row.setValue(current);
        row.setSummary(offered.get(current));
        row.setOnPreferenceChangeListener((item,newValue)->{
            if(!(newValue instanceof String) || !"8054".equals(OemHdmiPortInfo.machineId()))return false;
            try {
                Map<String,String> fresh=choices(context);
                Result result=apply(backend,offered,fresh,row.getValue(),(String)newValue);
                String actual=backend.read();
                if(offered.containsKey(actual) && fresh.containsKey(actual)) {
                    row.setValue(actual);
                    row.setSummary(fresh.get(actual));
                }else{
                    row.setEnabled(false);
                    row.setSummary("Текущий выбор не подтверждён");
                }
                if(result!=Result.CONFIRMED)
                    Log.w(TAG,"Boot destination not applied: "+result);
            }catch(Exception e){
                row.setEnabled(false);
                row.setSummary("Значение недоступно");
                Log.w(TAG,"Cannot confirm boot destination",e);
            }
            // The widget must render the separately read value, even on accepted writes.
            return false;
        });
        screen.addPreference(row);
    }
}

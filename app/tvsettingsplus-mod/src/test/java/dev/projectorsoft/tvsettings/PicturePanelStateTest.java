package dev.projectorsoft.tvsettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PicturePanelStateTest {
    public static void main(String[] args) {
        Map<String,String> stored=new HashMap<>();
        stored.put("picture_mode","7");
        stored.put("picture_brightness","54");
        stored.put("picture_contrast","bad");
        Map<String,Integer> first=PicturePanelState.read(stored::get);
        if(!Integer.valueOf(54).equals(first.get("picture_brightness"))
                ||first.containsKey("picture_contrast")||first.containsKey("picture_saturation"))
            throw new AssertionError(first);

        Map<String,Integer> switched=PicturePanelState.read(key->{
            if("picture_brightness".equals(key))stored.put("picture_mode","9");
            return stored.get(key);
        });
        if(switched!=null)throw new AssertionError("mixed profiles displayed");
        stored.put("picture_brightness","30");
        Map<String,Integer> refreshed=PicturePanelState.read(stored::get);
        if(!Integer.valueOf(9).equals(refreshed.get("picture_mode"))
                ||!Integer.valueOf(30).equals(refreshed.get("picture_brightness")))
            throw new AssertionError("mode/dependent values did not refresh");
        List<String> registered=new ArrayList<>();
        List<String> failed=new ArrayList<>();
        PicturePanelState.observe(key->{
            if(PicturePanelState.MODE.equals(key))throw new SecurityException("observer rejected");
            registered.add(key);
        },(key,error)->failed.add(key));
        if(failed.size()!=1 || !PicturePanelState.MODE.equals(failed.get(0))
                || registered.size()!=PicturePanelState.KEYS.length-1
                || !registered.contains("picture_contrast"))
            throw new AssertionError("A failed observer prevented registration of other settings");
        System.out.println("PASS native picture readout: missing, malformed, profile change and dependent reread");
    }
}

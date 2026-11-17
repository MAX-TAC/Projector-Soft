package dev.projectorsoft.tvsettings;

import java.util.*;

/** Exercises the transaction used by OemPictureState with a persistent fake provider/journal. */
public final class OemPictureStateTest {
    static final String KEY="picture_brightness";
    static class Fake implements PictureStateTransaction.Backend,PictureStateTransaction.Journal {
        final Map<String,String> values=new LinkedHashMap<>(),saved=new LinkedHashMap<>();
        String context="HDMI1/mode7";
        String fail;
        boolean failSave;
        int writes;
        public String read(String key) {return values.get(key);}
        public boolean write(String key,String value) {
            writes++;
            if(key.equals(fail))return false;
            values.put(key,value);
            if("picture_mode".equals(key))context="HDMI1/mode"+value;
            return true;
        }
        public String verifiedContext(){return context;}
        public Map<String,String> entries(){return new LinkedHashMap<>(saved);}
        public boolean save(String key,String value){if(failSave)return false;saved.put(key,value);return true;}
        public boolean remove(String key){saved.remove(key);return true;}
        PictureStateTransaction transaction(){return new PictureStateTransaction(this,this);}
    }
    static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    static Fake fixture(){Fake f=new Fake();f.values.put(KEY,"50");return f;}
    public static void main(String[] args) {
        Fake f=fixture();
        check(f.transaction().write(KEY,60)&&f.transaction().write(KEY,70),"repeated write");
        check(f.saved.get(KEY).endsWith("\n50"),"first original preserved");
        check(f.transaction().restore()&&"50".equals(f.values.get(KEY)),"restart/new transaction restores durable original");

        f=new Fake(); check(!f.transaction().write(KEY,70)&&f.writes==0&&f.saved.isEmpty(),"missing value must not be invented");

        f=fixture(); f.values.put("picture_contrast","40");
        f.transaction().write(KEY,60);f.transaction().write("picture_contrast",80);
        f.fail=KEY;check(!f.transaction().restore(),"partial failure reported");
        check(f.saved.containsKey(KEY)&&!f.saved.containsKey("picture_contrast"),"only successful entries removed");
        f.fail=null;check(f.transaction().restore()&&"50".equals(f.values.get(KEY)),"retry after restart");

        f=fixture(); f.transaction().write(KEY,60);f.context="HDMI1/mode3";
        int before=f.writes;check(!f.transaction().restore()&&f.writes==before,"mode mismatch no writes");
        f.context="HDMI2/mode7";check(!f.transaction().restore()&&f.writes==before,"HDMI mismatch no writes");
        f.context=null;check(!f.transaction().restore()&&f.writes==before,"unknown input/profile no writes");

        f=fixture();f.values.put("picture_mode","7");f.transaction().write(KEY,60);
        check(!f.transaction().write("picture_mode",3),"mode delegated, never scalar written");
        f.context="HDMI1/mode3"; // OEM changed mode outside MOD.
        before=f.writes;check(!f.transaction().restore()&&f.writes==before,"mode switching never blindly replays old profile");
        f.saved.put("picture_mode","HDMI1/mode7\n7");
        f.context="HDMI1/mode7";check(!f.transaction().restore(),"mode restore remains blocked even in original context");
        check(f.saved.containsKey("picture_mode"),"mode record kept for diagnostics");

        f=fixture();f.failSave=true;check(!f.transaction().write(KEY,80)&&f.writes==0,"journal persistence failure prevents write");
        f=fixture();f.saved.put(KEY,"50");check(!f.transaction().restore()&&f.writes==0,"legacy snapshot has no trusted context");
        f=fixture();f.context=null;
        check(!f.transaction().write(KEY,80)&&f.writes==0&&f.saved.isEmpty(),"UNKNOWN write: no snapshot or provider mutation");
        f.saved.put(KEY,"UNKNOWN\n50");
        check(!f.transaction().write(KEY,80)&&!f.transaction().restore()&&f.writes==0,"UNKNOWN history not replayed");
        f.context="HDMI1/mode7";
        check(!f.transaction().write(KEY,80)&&!f.transaction().restore()&&"UNKNOWN\n50".equals(f.saved.get(KEY)),"old UNKNOWN record quarantined in known context");
        System.out.println("PASS OemPictureState: repeat, absent, partial/retry, restart, mode, HDMI, unknown, journal failure, legacy");
    }
}

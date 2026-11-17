package dev.projectorsoft.tvsettings;

import java.util.Map;

/** Provider snapshot transaction. A context must identify a verified profile AND input. */
public final class PictureStateTransaction {
    public interface Backend {
        String read(String key);
        boolean write(String key,String value);
        String verifiedContext();
    }
    public interface Journal {
        Map<String,String> entries();
        boolean save(String key,String value);
        boolean remove(String key);
    }
    private final Backend backend;
    private final Journal journal;
    public PictureStateTransaction(Backend backend,Journal journal) {
        this.backend=backend; this.journal=journal;
    }
    private String context() {
        String value=backend.verifiedContext();
        return value==null||value.trim().isEmpty()?"UNKNOWN":value;
    }
    public boolean write(String key,int value) {
        String expected=context();
        if("UNKNOWN".equals(expected)||key==null||!key.startsWith("picture_")
                ||"picture_mode".equals(key)||"picture_color_temperature".equals(key))return false;
        String record=journal.entries().get(key);
        if(record!=null&&!record.startsWith(expected+"\n"))return false;
        String old=backend.read(key);
        if(old==null)return false;
        // One durable record: context and original value must survive together.
        if(record==null&&!journal.save(key,expected+"\n"+old))return false;
        if(!expected.equals(context()))return false;
        return backend.write(key,Integer.toString(value))
            && Integer.toString(value).equals(backend.read(key));
    }
    public boolean restore() {
        String expected=context();
        if("UNKNOWN".equals(expected))return false;
        boolean complete=true;
        for(Map.Entry<String,String> entry:journal.entries().entrySet()) {
            String key=entry.getKey(),record=entry.getValue();
            int split=record.indexOf('\n');
            // A mode write can trigger asynchronous changes to several OEM keys.
            // It is never replayed as an ordinary scalar restore.
            if(!key.startsWith("picture_")||"picture_mode".equals(key)||"picture_color_temperature".equals(key)||split<0
                    ||!expected.equals(record.substring(0,split))||!expected.equals(context())) {
                complete=false; continue;
            }
            String original=record.substring(split+1);
            try {
                if(backend.write(key,original)&&original.equals(backend.read(key))
                        &&expected.equals(context())) {
                    if(!journal.remove(key))complete=false;
                } else complete=false;
            } catch(RuntimeException ex) {complete=false;}
        }
        return complete;
    }
}

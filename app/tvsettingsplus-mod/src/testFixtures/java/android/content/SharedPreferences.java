package android.content;
import java.util.*;
public final class SharedPreferences {
    private final Map<String,String> data=new LinkedHashMap<>();
    public int commits;
    public Map<String,?> getAll(){return new LinkedHashMap<>(data);}
    public Editor edit(){return new Editor();}
    public final class Editor {
        private final Map<String,String> changes=new LinkedHashMap<>();
        public Editor putString(String key,String value){changes.put(key,value);return this;}
        public Editor remove(String key){changes.put(key,null);return this;}
        public boolean commit(){commits++;for(Map.Entry<String,String> e:changes.entrySet()) {
            if(e.getValue()==null)data.remove(e.getKey());else data.put(e.getKey(),e.getValue());
        }return true;}
    }
}

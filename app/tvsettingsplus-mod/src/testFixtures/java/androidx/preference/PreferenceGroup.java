package androidx.preference;
import android.content.Context;
import java.util.ArrayList;
import java.util.List;
public class PreferenceGroup extends Preference {
    public final List<Preference> children = new ArrayList<>();
    public PreferenceGroup(Context context) { super(context); }
    public boolean addPreference(Preference item) { return children.add(item); }
    public Preference find(CharSequence key) {
        for (Preference child : children) {
            if (key.equals(child.key)) return child;
            if (child instanceof PreferenceGroup) {
                Preference found = ((PreferenceGroup) child).find(key);
                if (found != null) return found;
            }
        }
        return null;
    }
}

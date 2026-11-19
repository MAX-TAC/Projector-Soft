package androidx.preference;
import android.content.Context;
public final class SwitchPreference extends Preference {
    public boolean checked;
    public SwitchPreference(Context context) { super(context); }
    public void setChecked(boolean checked) { this.checked = checked; }
}

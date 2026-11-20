package androidx.preference;

import android.content.Context;
import android.content.Intent;

public class Preference {
    public interface OnPreferenceClickListener { boolean onPreferenceClick(Preference item); }
    public interface OnPreferenceChangeListener { boolean onPreferenceChange(Preference item, Object value); }
    public String summary;
    public String key;
    public boolean visible = true;
    public boolean enabled = true;
    public boolean rejectSummary;
    private Intent intent;
    private OnPreferenceClickListener click;
    private OnPreferenceChangeListener change;

    public Preference() {}
    public Preference(Context context) {}
    public void setSummary(CharSequence value) {
        if (rejectSummary) throw new IllegalStateException("Preference refresh failed");
        summary = value == null ? null : value.toString();
    }
    public void setKey(String key) { this.key = key; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public void setTitle(CharSequence value) {}
    public void setSelectable(boolean selectable) {}
    public void setOrder(int order) {}
    public void setPersistent(boolean enabled) {}
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setIntent(Intent intent) { this.intent = intent; }
    public Intent getIntent() { return intent; }
    public void setOnPreferenceClickListener(OnPreferenceClickListener click) { this.click = click; }
    public void setOnPreferenceChangeListener(OnPreferenceChangeListener change) { this.change = change; }
    public boolean click() { return click.onPreferenceClick(this); }
    public boolean change(Object value) { return change.onPreferenceChange(this, value); }
}

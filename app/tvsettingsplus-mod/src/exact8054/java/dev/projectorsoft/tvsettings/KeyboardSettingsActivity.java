package dev.projectorsoft.tvsettings;

/** Keyboard screen with only the entries the owner wants. */
public final class KeyboardSettingsActivity extends ModSettingsActivity {
    @Override NativePage page() { return NativePage.KEYBOARD; }
}
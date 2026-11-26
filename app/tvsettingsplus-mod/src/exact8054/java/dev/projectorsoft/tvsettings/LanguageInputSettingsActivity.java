package dev.projectorsoft.tvsettings;

/** Hosts the Language & input sub-screen with two rows: system language and keyboard. */
public final class LanguageInputSettingsActivity extends ModSettingsActivity {
    @Override NativePage page() { return NativePage.LANGUAGE_INPUT; }
}
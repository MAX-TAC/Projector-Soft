package dev.projectorsoft.tvsettings;

/** Accounts screen rendered as a fragment, so the background stays visible. */
public final class AccountsSettingsActivity extends ModSettingsActivity {
    @Override NativePage page() { return NativePage.ACCOUNTS; }
}
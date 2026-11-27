package dev.projectorsoft.tvsettings;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.provider.Settings;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class FunctionalRepairsUiTest {
    private PreferenceScreen screen(Context context) {
        return new PreferenceManager(context).createPreferenceScreen(context);
    }

    @Test public void retainsEditableArcGameAllmAisrAndLiveTone() {
        Context context = RuntimeEnvironment.getApplication();
        PreferenceScreen screen = screen(context);
        ProjectorExtras.installSources(context, screen);
        ProjectorExtras.installPicture(context, screen);
        String[][] keys = {
            {"projectorsoft_arc_enabled", "hdmi_arc_control_enabled"},
            {"projectorsoft_extras_game_mode", "g_video__vid_game_mode", "tv_picture_video_game_mode"},
            {"projectorsoft_extras_allm", "g_video__vid_allm_switch", "tv_picture_video_allm"},
            {"projectorsoft_extras_aisr", "tv_picture_video_aisr"},
            {"projectorsoft_extras_live_tone", "tv_picture_video_live_tone"}
        };
        for (String[] mapping : keys) {
            Preference row = screen.findPreference(mapping[0]);
            assertTrue(mapping[0], row instanceof SwitchPreference);
            assertTrue(row.isSelectable());
            for (boolean enabled : new boolean[]{true, false}) {
                assertFalse(row.callChangeListener(enabled));
                for (int i = 1; i < mapping.length; i++)
                    assertEquals(enabled ? 1 : 0, Settings.Global.getInt(
                            context.getContentResolver(), mapping[i], -1));
                assertEquals(enabled, ((SwitchPreference) row).isChecked());
            }
        }
    }

    @Test public void edidRejectsInvalidAndStaleRequestsAndReflectsTheSavedValue() {
        Context context = RuntimeEnvironment.getApplication();
        Settings.Global.putInt(context.getContentResolver(), ProjectorExtras.EDID_KEY, 2);
        PreferenceScreen screen = screen(context);
        ProjectorExtras.installEdid(context, screen, 1);
        ListPreference row = screen.findPreference("projectorsoft_edid_version");
        row.callChangeListener("-1");
        row.callChangeListener("4");
        row.callChangeListener("bad");
        assertEquals(2, Settings.Global.getInt(context.getContentResolver(), ProjectorExtras.EDID_KEY, -1));
        Settings.Global.putInt(context.getContentResolver(), ProjectorExtras.EDID_KEY, 1);
        row.callChangeListener("0");
        assertEquals("1", row.getValue());
        row.callChangeListener("0");
        assertEquals("0", row.getValue());
        assertEquals(0, Settings.Global.getInt(context.getContentResolver(), ProjectorExtras.EDID_KEY, -1));
    }

    @Test public void absentEdidIsNotDisplayedAsAnInventedAutoMode() {
        Context context = RuntimeEnvironment.getApplication();
        PreferenceScreen screen = screen(context);
        ProjectorExtras.installEdid(context, screen, 1);
        ListPreference row = screen.findPreference("projectorsoft_edid_version");
        assertNull(row.getValue());
        assertTrue(row.isEnabled());
        assertEquals("Текущий режим не определён", row.getSummary());
        assertNull(Settings.Global.getString(context.getContentResolver(), ProjectorExtras.EDID_KEY));
    }

    @Test(expected = IllegalStateException.class)
    public void homeDoesNotReturnAnUnresolvedFallbackIntent() {
        HomeDestination.homeIntent(RuntimeEnvironment.getApplication());
    }

    @Test public void hdmiAutoplayUsesTheExactStockDefaultForAnUnsetProperty() throws Exception {
        org.robolectric.shadows.ShadowSystemProperties.override("persist.rjpj.hdmiplug.autoplay", "");
        assertEquals("0", OemHdmiAutoPlayHook.getProperty());
        org.robolectric.shadows.ShadowSystemProperties.override("persist.rjpj.hdmiplug.autoplay", "1");
        assertEquals("1", OemHdmiAutoPlayHook.getProperty());
        org.robolectric.shadows.ShadowSystemProperties.override("persist.rjpj.hdmiplug.autoplay", "bad");
        assertEquals("0", OemHdmiAutoPlayHook.getProperty());
    }

    @Test public void accountsRefreshOnReturnAndKeepExistingRows() {
        Context context = RuntimeEnvironment.getApplication();
        AccountManager manager = AccountManager.get(context);
        Account first = new Account("first@example.invalid", "test.account");
        shadowOf(manager).addAccount(first);
        ActivityController<AccountsSettingsActivity> controller =
                Robolectric.buildActivity(AccountsSettingsActivity.class).setup();
        NativeSections.Accounts page = (NativeSections.Accounts) controller.get()
                .getSupportFragmentManager().findFragmentById(android.R.id.content);
        String firstKey = "projectorsoft_account_" + first.type + "/" + first.name;
        Preference firstRow = page.findPreference(firstKey);
        assertNotNull(firstRow);
        assertFalse(firstRow.isSelectable());
        Account second = new Account("second@example.invalid", "test.account");
        shadowOf(manager).addAccount(second);
        controller.pause().resume();
        assertSame(firstRow, page.findPreference(firstKey));
        assertNotNull(page.findPreference("projectorsoft_account_" + second.type + "/" + second.name));
        assertNull(page.findPreference("projectorsoft_accounts_empty"));
        assertNotNull(page.findPreference("projectorsoft_account_add"));
    }
}

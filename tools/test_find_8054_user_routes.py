import unittest
from find_8054_user_routes import TIMER_ACTIVITY, stock_timer_activity


MANIFEST = f'''
    E: activity
      A: android:name(0x01010003)="{TIMER_ACTIVITY}" (Raw: "{TIMER_ACTIVITY}")
'''

ACTIVITY = '''
.method protected createSettingsFragment()Landroidx/fragment/app/Fragment;
    const-class v0, Lcom/android/tv/settings/partnercustomizer/timer/TimerFragment;
    const/4 v1, 0x0
    .line 39
    invoke-interface {p0, v0, v1}, Lcom/android/tv/settings/SettingsFragmentProvider;->newSettingsFragment(Ljava/lang/String;Landroid/os/Bundle;)Landroidx/fragment/app/Fragment;
.end method
'''

FRAGMENT = '''
    const-string p1, "set_timer_power_off_timer"
    const-string v0, "tv_timer_power_off_timer_values"
'''


class TimerRouteTest(unittest.TestCase):
    def test_stock_editor_uses_no_extras(self):
        self.assertTrue(stock_timer_activity(MANIFEST, ACTIVITY, FRAGMENT))

    def test_reject_wrong_component_arguments_or_missing_editor(self):
        self.assertFalse(stock_timer_activity(MANIFEST.replace("E: activity", "E: service"), ACTIVITY, FRAGMENT))
        self.assertFalse(stock_timer_activity(MANIFEST, ACTIVITY.replace("0x0", "0x1"), FRAGMENT))
        self.assertFalse(stock_timer_activity(MANIFEST, ACTIVITY.replace("{p0, v0, v1}", "{p0, v0, v2}"), FRAGMENT))
        self.assertFalse(stock_timer_activity(MANIFEST, ACTIVITY.replace("TimerFragment", "OtherFragment"), FRAGMENT))
        self.assertFalse(stock_timer_activity(MANIFEST, ACTIVITY, ""))


if __name__ == "__main__":
    unittest.main()

import unittest
from find_8054_user_routes import (PICTURE_FORMAT_ACTIVITY, TIMER_ACTIVITY,
                                   stock_picture_format, stock_timer_activity)


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


class PictureFormatRouteTest(unittest.TestCase):
    def test_stock_activity_and_exact_array_ids(self):
        manifest = MANIFEST.replace(TIMER_ACTIVITY, PICTURE_FORMAT_ACTIVITY)
        activity = ACTIVITY.replace("timer/TimerFragment", "picture/DisplayModeFragment")
        fragment = """
.method private getSupportPictureFormatArrValue(I)[Ljava/lang/String;
    const v1, 0x7f0300db
    .line 41
    invoke-virtual {v0, v1}, Landroid/content/res/Resources;->getStringArray(I)[Ljava/lang/String;
    const v2, 0x7f0300dc
    invoke-virtual {v1, v2}, Landroid/content/res/Resources;->getStringArray(I)[Ljava/lang/String;
    invoke-virtual {p0}, Lvendor/Config;->getSupportScreenModes()[I
.end method
    const-string v0, "g_fusion_picture__pictrue_format"
    const-string v1, "picture_format"
"""
        self.assertEqual(stock_picture_format(manifest, activity, fragment), (0x7f0300db, 0x7f0300dc))
        self.assertFalse(stock_picture_format(MANIFEST, activity, fragment))
        self.assertFalse(stock_picture_format(manifest, activity.replace("0x0", "0x1"), fragment))
        self.assertFalse(stock_picture_format(manifest, activity, fragment.replace("0x7f0300dc", "0x7f0300db")))
        self.assertFalse(stock_picture_format(manifest, activity, fragment.replace("getSupportScreenModes", "unknown")))
        self.assertFalse(stock_picture_format(manifest, activity, fragment + "    invoke-virtual {p0}, LX;->getArguments()Landroid/os/Bundle;"))

if __name__ == "__main__":
    unittest.main()

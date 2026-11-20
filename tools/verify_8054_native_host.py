"""Fail closed when a native UI link does not match the original 8054 ABI."""
import sys
from pathlib import Path

root = Path(sys.argv[1])


def stock(name):
    matches = list(root.glob(f'smali*/{name}.smali'))
    if len(matches) != 1:
        raise ValueError('Missing or duplicated stock type ' + name)
    return matches[0].read_text(encoding='utf-8')


prefix = 'com/android/tv/settings/'
required = {
    prefix+'TvSettingsActivity': [
        '.super Landroidx/fragment/app/FragmentActivity;',
        '<init>()V', 'isStartupVerificationRequired()Z',
        'createSettingsFragment()Landroidx/fragment/app/Fragment;'],
    prefix+'SettingsPreferenceFragment': [
        '.super Landroidx/leanback/preference/LeanbackPreferenceFragmentCompat;',
        '<init>()V', 'getPageId()I', 'onViewCreated(Landroid/view/View;Landroid/os/Bundle;)V'],
    prefix+'overlay/FlavorUtils': [
        'getFeatureFactory(Landroid/content/Context;)Lcom/android/tv/settings/overlay/FeatureFactory;'],
    prefix+'overlay/FeatureFactory': [
        'getSettingsFragmentProvider()Lcom/android/tv/settings/SettingsFragmentProvider;'],
    prefix+'SettingsFragmentProvider': [
        'newSettingsFragment(Ljava/lang/String;Landroid/os/Bundle;)Landroidx/fragment/app/Fragment;'],
    prefix+'connectivity/NetworkActivity': [
        'Lcom/android/tv/settings/connectivity/NetworkFragment;'],
    prefix+'device/apps/AppsActivity': [
        'Lcom/android/tv/settings/device/apps/AppsFragment;'],
    'com/android/tv/twopanelsettings/TwoPanelSettingsFragment': [
        'onCreatePreviewFragment(Landroidx/fragment/app/Fragment;Landroidx/preference/Preference;)Landroidx/fragment/app/Fragment;',
        'onPreferenceStartFragment(Landroidx/preference/PreferenceFragmentCompat;Landroidx/preference/Preference;)Z'],
}
for name, invariants in required.items():
    code=stock(name)
    for contract in invariants:
        lines=[line for line in code.splitlines() if contract in line]
        if not lines:
            raise ValueError(f'{name}: missing {contract}')
        if '(' in contract and name not in (prefix+'SettingsPreferenceFragment',prefix+'TvSettingsActivity'):
            if not any(line.startswith('.method') for line in lines):
                raise ValueError(f'{name}: signature is an invoke only {contract}')
    declaration=code.splitlines()[0]
    if ('final' in declaration.split()) and name in (prefix+'TvSettingsActivity',prefix+'SettingsPreferenceFragment'):
        raise ValueError('Stock host cannot be subclassed: '+name)
stock(prefix+'connectivity/NetworkFragment')
stock(prefix+'device/apps/AppsFragment')
print('PASS exact native stock host, preview, activity and theme prerequisites')

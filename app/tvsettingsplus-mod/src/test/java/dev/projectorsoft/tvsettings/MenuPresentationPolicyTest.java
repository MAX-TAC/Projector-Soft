package dev.projectorsoft.tvsettings;

public final class MenuPresentationPolicyTest {
    private static void expect(MenuPresentationPolicy.Item item,MenuPresentationPolicy.Presentation value) {
        if (MenuPresentationPolicy.forItem(item)!=value) throw new AssertionError(item+" should be "+value);
    }
    public static void main(String[] args) {
        expect(MenuPresentationPolicy.Item.LIGHT_SOURCE_BRIGHTNESS,
                MenuPresentationPolicy.Presentation.RIGHT_PANEL_SINGLE_CHOICE);
        expect(MenuPresentationPolicy.Item.PICTURE_BRIGHTNESS,
                MenuPresentationPolicy.Presentation.RIGHT_PANEL_SLIDER);
        expect(MenuPresentationPolicy.Item.IMAGE_MODE,
                MenuPresentationPolicy.Presentation.RIGHT_PANEL_SINGLE_CHOICE);
        expect(MenuPresentationPolicy.Item.HDR_MODE,
                MenuPresentationPolicy.Presentation.RIGHT_PANEL_SINGLE_CHOICE);
        expect(MenuPresentationPolicy.Item.MEMC_MODE,
                MenuPresentationPolicy.Presentation.RIGHT_PANEL_SINGLE_CHOICE);
        expect(MenuPresentationPolicy.Item.SOUND_EQUALIZER,
                MenuPresentationPolicy.Presentation.RIGHT_PANEL_NAVIGATION);
        for (MenuPresentationPolicy.Item item: new MenuPresentationPolicy.Item[] {
                MenuPresentationPolicy.Item.FOCUS_AUTO,
                MenuPresentationPolicy.Item.FOCUS_MANUAL,
                MenuPresentationPolicy.Item.KEYSTONE_AUTO,
                MenuPresentationPolicy.Item.KEYSTONE_MANUAL_FOUR_CORNER}) {
            expect(item,MenuPresentationPolicy.Presentation.ORIGINAL_CALIBRATION_SCREEN);
            if (!MenuPresentationPolicy.mustPreserveCalibrationScreen(item)) throw new AssertionError(item);
        }
        if (MenuPresentationPolicy.mustPreserveCalibrationScreen(
                MenuPresentationPolicy.Item.LIGHT_SOURCE_BRIGHTNESS)) throw new AssertionError();
        System.out.println("PASS: right-panel controls and untouched projector calibration views");
    }
}

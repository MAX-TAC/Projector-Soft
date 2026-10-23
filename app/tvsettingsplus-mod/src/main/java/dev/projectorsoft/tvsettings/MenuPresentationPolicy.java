package dev.projectorsoft.tvsettings;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Contract for merging OEM controls into the existing two-panel TV Settings UI.
 * This does not instantiate any new Activity and cannot suppress calibration video.
 */
public final class MenuPresentationPolicy {
    public enum Item {
        LIGHT_SOURCE_BRIGHTNESS,
        PICTURE_BRIGHTNESS, CONTRAST, SATURATION, SHARPNESS,
        IMAGE_MODE, HDR_MODE, MEMC_MODE, GAME_MODE,
        DIGITAL_ZOOM, ORIENTATION,
        SOUND_MODE, SOUND_EQUALIZER, AUDIO_DELAY,
        HDMI_SOURCE,
        FOCUS_AUTO, FOCUS_MANUAL, KEYSTONE_AUTO, KEYSTONE_MANUAL_FOUR_CORNER
    }
    public enum Presentation {
        RIGHT_PANEL_SINGLE_CHOICE,
        RIGHT_PANEL_SLIDER,
        RIGHT_PANEL_TOGGLE,
        RIGHT_PANEL_NAVIGATION,
        ORIGINAL_CALIBRATION_SCREEN
    }

    private static final Map<Item, Presentation> POLICY;
    static {
        EnumMap<Item,Presentation> map=new EnumMap<>(Item.class);
        map.put(Item.LIGHT_SOURCE_BRIGHTNESS,Presentation.RIGHT_PANEL_SINGLE_CHOICE);
        map.put(Item.PICTURE_BRIGHTNESS,Presentation.RIGHT_PANEL_SLIDER);
        map.put(Item.CONTRAST,Presentation.RIGHT_PANEL_SLIDER);
        map.put(Item.SATURATION,Presentation.RIGHT_PANEL_SLIDER);
        map.put(Item.SHARPNESS,Presentation.RIGHT_PANEL_SLIDER);
        map.put(Item.IMAGE_MODE,Presentation.RIGHT_PANEL_SINGLE_CHOICE);
        map.put(Item.HDR_MODE,Presentation.RIGHT_PANEL_SINGLE_CHOICE);
        map.put(Item.MEMC_MODE,Presentation.RIGHT_PANEL_SINGLE_CHOICE);
        map.put(Item.GAME_MODE,Presentation.RIGHT_PANEL_TOGGLE);
        map.put(Item.DIGITAL_ZOOM,Presentation.RIGHT_PANEL_SLIDER);
        map.put(Item.ORIENTATION,Presentation.RIGHT_PANEL_SINGLE_CHOICE);
        map.put(Item.SOUND_MODE,Presentation.RIGHT_PANEL_SINGLE_CHOICE);
        map.put(Item.SOUND_EQUALIZER,Presentation.RIGHT_PANEL_NAVIGATION);
        map.put(Item.AUDIO_DELAY,Presentation.RIGHT_PANEL_SLIDER);
        map.put(Item.HDMI_SOURCE,Presentation.RIGHT_PANEL_SINGLE_CHOICE);
        map.put(Item.FOCUS_AUTO,Presentation.ORIGINAL_CALIBRATION_SCREEN);
        map.put(Item.FOCUS_MANUAL,Presentation.ORIGINAL_CALIBRATION_SCREEN);
        map.put(Item.KEYSTONE_AUTO,Presentation.ORIGINAL_CALIBRATION_SCREEN);
        map.put(Item.KEYSTONE_MANUAL_FOUR_CORNER,Presentation.ORIGINAL_CALIBRATION_SCREEN);
        if (map.size()!=Item.values().length) throw new AssertionError("Missing item policy");
        POLICY=Collections.unmodifiableMap(map);
    }
    private MenuPresentationPolicy() {}
    public static Presentation forItem(Item item) {
        Presentation p=POLICY.get(item);
        if (p==null) throw new IllegalArgumentException("Unknown menu item: "+item);
        return p;
    }

    public static boolean mustPreserveCalibrationScreen(Item item) {
        return forItem(item)==Presentation.ORIGINAL_CALIBRATION_SCREEN;
    }
}

# TvSettingsPlus 8052: existing OEM controllers inventory

Source: original 8052 TvSettingsPlus APK. Class names, methods, view IDs and property names only; no proprietary Java source reproduced.

| Existing controller/fragment | Methods | OEM UI IDs | Example controls |
|---|---:|---:|---|
| floatmenu/controller/general/BrightnessModeSettingsSecondaryController | 9 | 5 | rjRadioGroup, rtv_general_projection_brightness_mode_auto, rtv_general_projection_brightness_mode_eco, rtv_general_projection_brightness_mode_highlight, rtv_general_projection_brightness_mode_standard |
| floatmenu/controller/general/GeneralSubMenuController | 14 | 6 | tciv_general_date_time, tciv_general_font_size, tciv_general_language_settings, tciv_general_projection_brightness_mode, tciv_general_screensaver_settings, tsv_general_power_on_up |
| floatmenu/controller/picture/PictureSubMenuController | 13 | 16 | rjRadioGroup_color_temperature, rjRadioGroup_picture_mode, rjscrollView, rtv_color_temp_cool, rtv_color_temp_custom, rtv_color_temp_standard, rtv_color_temp_warm, rtv_picture_mode_energy_saving, rtv_picture_mode_game, rtv_picture_mode_movie, rtv_picture_mode_sport |
| floatmenu/controller/picture/PictureModeValueSecondaryController | 7 | 5 | tsbv_picture_brightness, tsbv_picture_contrast, tsbv_picture_hue, tsbv_picture_saturation, tsbv_picture_sharpness |
| floatmenu/controller/picture/ColorTempValueSecondaryController | 7 | 3 | tsbv_color_temp_blue, tsbv_color_temp_green, tsbv_color_temp_red |
| floatmenu/controller/picture/AdvanceSettingsSecondaryController | 14 | 16 | btn_mjc_demo, mjc_left, mjc_mid, mjc_right, rjRadioGroup_hdr, rjRadioGroup_mjc_demo_partition, rjRadioGroup_mjc_effect, rtv_hdr_off, rtv_hdr_on, rtv_mjc_demo_all, rtv_mjc_demo_left |
| floatmenu/controller/sound/SoundSubMenuController | 15 | 13 | rjRadioGroup, rtv_sound_mode_custom, rtv_sound_mode_movie, rtv_sound_mode_music, rtv_sound_mode_news, rtv_sound_mode_sports, rtv_sound_mode_standard, rtv_sound_mode_vivid, tsv_sound_auto_volume, tsv_sound_earc, tsv_sound_surround |
| floatmenu/controller/sound/SoundModeValueSecondaryController | 7 | 2 | tsbv_sound_mode_value_bass, tsbv_sound_mode_value_treble |
| floatmenu/controller/source/SourceSubMenuController | 22 | 14 | tciv_source_av, tciv_source_bluetooth_speaker, tciv_source_edid_version, tciv_source_hdmi, tciv_source_hdmi2, tciv_source_hdmi3, tciv_source_hdmi4, tciv_source_power_on, tciv_source_power_on_app, tciv_source_usb, tsv_hdmi_cec_control |
| floatmenu/controller/source/PowerOnSourceSecondaryController | 11 | 8 | rjRadioGroup, rtv_source_power_on_av, rtv_source_power_on_hdmi1, rtv_source_power_on_hdmi2, rtv_source_power_on_hdmi3, rtv_source_power_on_hdmi4, rtv_source_power_on_off, rtv_source_power_on_usb |
| floatmenu/controller/source/PowerOnAppSecondaryController | 8 | 1 | listview_power_on_app |
| floatmenu/controller/system/SystemSubMenuController | 9 | 4 | tciv_system_factory_reset, tciv_system_info, tciv_system_local_update, tciv_system_online_update |
| floatmenu/controller/system/SystemInfoSecondaryController | 18 | 7 | tcvv_system_ambient_temperature, tcvv_system_android_version, tcvv_system_device_model, tcvv_system_device_temperature, tcvv_system_mac_address, tcvv_system_version, tcvv_system_wifi_mac_address |
| floatmenu/controller/projector/ProjectorSubMenuController | 11 | 9 | tciv_projector_auto_focus, tciv_projector_auto_keystone, tciv_projector_focus_settings, tciv_projector_four_point_keystone, tciv_projector_hv_keystone, tciv_projector_keystone_settings, tciv_projector_mode, tciv_projector_reset_keystone, tciv_projector_screen_zoom |
| floatmenu/controller/projector/FocusSettingsSecondaryController | 9 | 4 | tsv_focus_accurate_focus, tsv_focus_move_auto, tsv_focus_power_on_auto, tv_focus_accurate_focus_tip |
| floatmenu/controller/projector/KeystoneSettingsSecondaryController | 16 | 8 | tciv_keystone_auto_screen, tciv_keystone_correction, tsv_keystone_auto_obstacle_avoidance, tsv_keystone_auto_screen, tsv_keystone_move_auto, tsv_keystone_power_on_auto, tsv_keystone_vertical, tv_keystone_settings_title |
| floatmenu/controller/projector/ProjectionModeSecondaryController | 10 | 7 | iv_mode_tips, rjRadioGroup, rtv_projector_mode_ceiling_front, rtv_projector_mode_ceiling_rear, rtv_projector_mode_front, rtv_projector_mode_rear, tsv_auto_reverse |
| fragment/general/BrightnessModeSettingsFragment | 9 | 5 | rjRadioGroup, rtv_general_projection_brightness_mode_auto, rtv_general_projection_brightness_mode_eco, rtv_general_projection_brightness_mode_highlight, rtv_general_projection_brightness_mode_standard |
| fragment/general/GeneralFragment | 17 | 6 | tciv_general_date_time, tciv_general_font_size, tciv_general_language_settings, tciv_general_projection_brightness_mode, tciv_general_screensaver_settings, tsv_general_power_on_up |
| fragment/picture/PictureFragment | 42 | 1 | content |
| fragment/sound/SoundFragment | 52 | 1 | content |
| fragment/projector/ProjectorFragment | 13 | 9 | tciv_projector_auto_focus, tciv_projector_auto_keystone, tciv_projector_focus_settings, tciv_projector_four_point_keystone, tciv_projector_hv_keystone, tciv_projector_keystone_settings, tciv_projector_mode, tciv_projector_reset_keystone, tciv_projector_screen_zoom |
| fragment/source/SourceFragment | 17 | 10 | tciv_source_bluetooth_speaker, tciv_source_edid_version, tciv_source_hdmi, tciv_source_hdmi2, tciv_source_power_on, tciv_source_power_on_app, tciv_source_usb, tsv_hdmi_cec_control, tsv_hdmi_device_auto_off, tsv_source_plug_and_play |
| fragment/system/SystemFragment | 12 | 4 | tciv_system_factory_reset, tciv_system_info, tciv_system_local_update, tciv_system_online_update |

These controllers already exist in the factory APK; their presence does not prove a new unified menu is installed.
Preserve focus and keystone full-screen calibration flows. Light-source brightness must use original thermal-aware OEM action.

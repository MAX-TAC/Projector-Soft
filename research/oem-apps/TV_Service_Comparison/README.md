# Everycom MTK TVservice/tvconfig partition comparison

These ext4 volumes contain supporting hardware config and services outside ordinary Android APK folders.

All content compares SHA256. No private firmware bytes or identifiable material are published.

## bootdata_a

- Files: 89 (1 HDMI), 89 (2 HDMI)
- Identical: 86
- Changed: 3
- Paths only in one/two: 0/0

### Changed files

- boot0.jpg
- config/dtbo_mapping.ini
- pmu/pmu_mcs51_sign.bin

### Added/removed


### Selected configuration changes

#### config/dtbo_mapping.ini
- One HDMI: ['[panel]', 'mmap_mt5879_mtk_2g_an_iommu_h1v1=74', '[uboot_pcb]', 'uboot_MT5879_H1V1_B6_S=99', 'uboot_MT5879_H3V1_B6_S=100']
- Two HDMI: ['[panel]', 'mmap_mt5879_mtk_2g_an_iommu_h1v1=54', '[uboot]', 'uboot_mt5879=85', 'uboot_mt5896_e3=86']

## project_id

- Files: 1 (1 HDMI), 1 (2 HDMI)
- Identical: 1
- Changed: 0
- Paths only in one/two: 0/0

### Changed files


### Added/removed


### Selected configuration changes

## tvconfig_a

- Files: 812 (1 HDMI), 812 (2 HDMI)
- Identical: 804
- Changed: 8
- Paths only in one/two: 0/0

### Changed files

- bsp/board/BD_MT5873_H1V1_B6_S/board.ini
- bsp/chip/aq/InputSrc/BasicSound_DTV.ini
- bsp/chip/aq/InputSrc/BasicSound_HDMI.ini
- bsp/chip/aq/InputSrc/BasicSound_MM.ini
- bsp/common/PQ_general/gammatable_1.ini
- bsp/common/misc/boot0.jpg
- config/dtbo_mapping.ini
- platform/data/misc/ConfigDefaultValue.ini

### Added/removed


### Selected configuration changes

#### config/dtbo_mapping.ini
- One HDMI: ['[panel]', 'mmap_mt5879_mtk_2g_an_iommu_h1v1=74', '[uboot_pcb]', 'uboot_MT5879_H1V1_B6_S=99', 'uboot_MT5879_H3V1_B6_S=100']
- Two HDMI: ['[panel]', 'mmap_mt5879_mtk_2g_an_iommu_h1v1=54', '[uboot]', 'uboot_mt5879=85', 'uboot_mt5896_e3=86']

## tvservice_a

- Files: 185 (1 HDMI), 185 (2 HDMI)
- Identical: 135
- Changed: 50
- Paths only in one/two: 0/0

### Changed files

- bin/midaemon
- bin/midemo
- glibc/directfb/directfb-1.4-0/gfxdrivers/libdirectfb_mstar_gles2.la
- glibc/directfb/directfb-1.4-0/gfxdrivers/libdirectfb_mstar_gles2.so
- glibc/directfb/directfb-1.4-0/gfxdrivers/libdirectfb_mstar_mi.la
- glibc/directfb/directfb-1.4-0/gfxdrivers/libdirectfb_mstar_mi.so
- glibc/directfb/directfb-1.4-0/inputdrivers/libdirectfb_mtnet.la
- glibc/directfb/directfb-1.4-0/inputdrivers/libdirectfb_mtnet.so
- glibc/directfb/directfb-1.4-0/interfaces/IDirectFBFont/libidirectfbfont_dgiff.la
- glibc/directfb/directfb-1.4-0/interfaces/IDirectFBFont/libidirectfbfont_dgiff.so
- glibc/directfb/directfb-1.4-0/interfaces/IDirectFBFont/libidirectfbfont_ft2.la
- glibc/directfb/directfb-1.4-0/interfaces/IDirectFBFont/libidirectfbfont_ft2.so
- glibc/directfb/directfb-1.4-0/interfaces/IDirectFBImageProvider/libidirectfbimageprovider_bmp.la
- glibc/directfb/directfb-1.4-0/interfaces/IDirectFBImageProvider/libidirectfbimageprovider_bmp.so
- glibc/directfb/directfb-1.4-0/interfaces/IDirectFBImageProvider/libidirectfbimageprovider_dfiff.la
- glibc/directfb/directfb-1.4-0/interfaces/IDirectFBImageProvider/libidirectfbimageprovider_dfiff.so
- glibc/directfb/directfb-1.4-0/interfaces/IDirectFBImageProvider/libidirectfbimageprovider_gif.la
- glibc/directfb/directfb-1.4-0/interfaces/IDirectFBImageProvider/libidirectfbimageprovider_gif.so
- glibc/directfb/directfb-1.4-0/interfaces/IDirectFBImageProvider/libidirectfbimageprovider_jpeg.la
- glibc/directfb/directfb-1.4-0/interfaces/IDirectFBImageProvider/libidirectfbimageprovider_jpeg.so
- glibc/directfb/directfb-1.4-0/interfaces/IDirectFBImageProvider/libidirectfbimageprovider_png.la
- glibc/directfb/directfb-1.4-0/interfaces/IDirectFBImageProvider/libidirectfbimageprovider_png.so
- glibc/directfb/directfb-1.4-0/interfaces/IDirectFBVideoProvider/libidirectfbvideoprovider_gif.la
- glibc/directfb/directfb-1.4-0/interfaces/IDirectFBVideoProvider/libidirectfbvideoprovider_gif.so
- glibc/directfb/directfb-1.4-0/interfaces/IDirectFBVideoProvider/libidirectfbvideoprovider_v4l.la
- glibc/directfb/directfb-1.4-0/interfaces/IDirectFBVideoProvider/libidirectfbvideoprovider_v4l.so
- glibc/directfb/directfb-1.4-0/interfaces/IDirectFBWindows/libidirectfbwindows_default.la
- glibc/directfb/directfb-1.4-0/interfaces/IDirectFBWindows/libidirectfbwindows_default.so
- glibc/directfb/directfb-1.4-0/interfaces/IWater/libiwater_default.la
- glibc/directfb/directfb-1.4-0/interfaces/IWater/libiwater_default.so
- glibc/directfb/directfb-1.4-0/systems/libdirectfb_devmem.la
- glibc/directfb/directfb-1.4-0/systems/libdirectfb_devmem.so
- glibc/directfb/directfb-1.4-0/wm/libdirectfbwm_default.la
- glibc/directfb/directfb-1.4-0/wm/libdirectfbwm_default.so
- glibc/directfb/libdirect-1.4.so.0.2.0
- glibc/directfb/libdirect.la
- glibc/directfb/libdirectfb-1.4.so.0.2.0
- glibc/directfb/libdirectfb.la
- glibc/directfb/libfusion-1.4.so.0.2.0
- glibc/directfb/libfusion.la
- glibc/libkms.so
- glibc/libmi.so
- glibc/libmi3demo.so
- glibc/libmi_client.so
- glibc/libmi_server.so
- glibc/libmmsdk_porting.so
- glibc/libmtk_tinyalsa_link_plugin.so
- glibc/libsndcardparser.so
- glibc/libtinyalsa-master.so
- glibc/libtzcmif.so

### Added/removed


### Selected configuration changes


Errors / warnings: []

package dev.projectorsoft.tvsettings;

import java.util.List;

final class AdvancedSound8054Policy {
    private AdvancedSound8054Policy() {}

    static boolean dap(String board,List<?> soundEffects,Object atmosType) {
        if (!"8054".equals(board) || !soundEffects.contains("sound_dap") || !(atmosType instanceof Integer))
            return false;
        int type=(Integer)atmosType;
        return type==2 || type==5 || type==7 || type==8 || type==9;
    }

    static boolean dts(String board,List<?> soundEffects,Object supported,List<?> dtsGroup) {
        return "8054".equals(board) && soundEffects.contains("sound_dts")
                && Boolean.TRUE.equals(supported) && dtsGroup.contains("sound_dts_truvolumehd");
    }
}

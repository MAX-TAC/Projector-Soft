package dev.projectorsoft.tvsettings;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class AdvancedSound8054PolicyTest {
    private static void check(boolean supported) {
        if (!supported) throw new AssertionError("OEM sound eligibility");
    }

    public static void main(String[] args) {
        List<String> effects=Arrays.asList("sound_dap","sound_dts");
        List<String> loudness=Collections.singletonList("sound_dts_truvolumehd");
        for (int type:new int[]{2,5,7,8,9}) check(AdvancedSound8054Policy.dap("8054",effects,type));
        for (Object type:new Object[]{null,"2",0,1,3,4,6,10})
            check(!AdvancedSound8054Policy.dap("8054",effects,type));
        check(!AdvancedSound8054Policy.dap("8052",effects,2));
        check(!AdvancedSound8054Policy.dap("8054",Collections.emptyList(),2));
        check(AdvancedSound8054Policy.dts("8054",effects,true,loudness));
        check(!AdvancedSound8054Policy.dts("8054",effects,false,loudness));
        check(!AdvancedSound8054Policy.dts("8054",effects,null,loudness));
        check(!AdvancedSound8054Policy.dts("8054",effects,true,Collections.singletonList("sound_dts_limiter")));
        check(!AdvancedSound8054Policy.dts("8054",Collections.emptyList(),true,loudness));
        check(!AdvancedSound8054Policy.dts(null,effects,true,loudness));
        System.out.println("PASS stock Dolby and DTS availability, unsupported modes and absent DTS loudness");
    }
}

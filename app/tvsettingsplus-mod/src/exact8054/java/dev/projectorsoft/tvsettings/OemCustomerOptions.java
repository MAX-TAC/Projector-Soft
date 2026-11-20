package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.content.res.Resources;
import java.util.HashSet;
import java.util.Set;

/** Reads the paired labels and values selected by the stock customer configuration. */
final class OemCustomerOptions {
    private OemCustomerOptions() {}

    static String[][] forKey(Context context, String key) throws Exception {
        if (!"8054".equals(OemHdmiPortInfo.machineId())) return null;
        Class<?> type = Class.forName("com.android.tv.settings.partnercustomizer.tvsettingservice.util.PreferenceConfigUtils");
        Object config = type.getMethod("getInstance", Context.class).invoke(null, context);
        int[] ids = (int[]) type.getMethod("getArrayIdsByCustomer", String.class).invoke(config, key);
        return read(context, ids);
    }

    static String[][] read(Context context, int... ids) {
        if (ids == null || ids.length != 2 || ids[0] == 0 || ids[1] == 0) return null;
        Resources res = context.getResources();
        String[][] table = {res.getStringArray(ids[0]), res.getStringArray(ids[1])};
        return valid(table) ? table : null;
    }

    static boolean valid(String[][] table) {
        if (table == null || table.length != 2 || table[0] == null || table[1] == null
                || table[0].length == 0 || table[0].length != table[1].length) return false;
        Set<String> values = new HashSet<>();
        for (int i = 0; i < table[0].length; i++) {
            if (table[0][i] == null || table[0][i].trim().isEmpty() || table[1][i] == null
                    || !table[1][i].matches("-?[0-9]+") || !values.add(table[1][i])) return false;
        }
        return true;
    }
}

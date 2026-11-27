package dev.projectorsoft.tvsettings;

import android.content.Context;
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;

/** Displays saved values for settings without a verified exact-8054 write contract. */
final class ProjectorExtras {
    private ProjectorExtras() {}

    static void installSources(Context context, PreferenceGroup parent) {
        addStatus(context, parent, "projectorsoft_arc_enabled", "HDMI ARC", 201,
                "hdmi_arc_control_enabled");
        addStatus(context, parent, "projectorsoft_extras_game_mode",
                "Игровой режим (низкая задержка)", 250, "tv_picture_video_game_mode");
        addStatus(context, parent, "projectorsoft_extras_allm",
                "ALLM (авто-игровой режим)", 251, "tv_picture_video_allm");
    }

    static void installPicture(Context context, PreferenceGroup parent) {
        addStatus(context, parent, "projectorsoft_extras_aisr",
                "AISR (улучшение детализации)", 226, "tv_picture_video_aisr");
        addStatus(context, parent, "projectorsoft_extras_live_tone",
                "Live Tone", 227, "tv_picture_video_live_tone");
    }

    private static void addStatus(Context context, PreferenceGroup parent, String key,
                                  String title, int order, String storage) {
        Preference row = new Preference(context);
        row.setKey(key);
        row.setTitle(title);
        row.setOrder(order);
        row.setSelectable(false);
        int saved = Settings.Global.getInt(context.getContentResolver(), storage, -1);
        String state = saved == 1 ? "Сохранено: включено" : saved == 0
                ? "Сохранено: выключено" : "Сохранённое состояние неизвестно";
        row.setSummary(state + " · только просмотр");
        parent.addPreference(row);
    }
}

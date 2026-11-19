package dev.projectorsoft.tvsettings;

/**
 * Evidence-only classification for factory implicit KuSettings intent routes.
 * Explicit ComponentName calls into com.my.settings are outside this scope.
 */
public final class OemLegacyIntentPolicy {
    public static final String MOD="com.android.tv.settings";
    public static final String KUSETTINGS="com.my.settings";

    public enum State {
        MOD_DEFAULT,
        MOD_DEFAULT_WITH_COMPETITOR,
        OEM_DEFAULT,
        COMPETING_HANDLERS,
        MOD_NOT_VISIBLE,
        DEFAULT_UNKNOWN
    }
    private OemLegacyIntentPolicy(){}

    public static State evaluate(boolean modVisible,boolean kuVisible,
                                 String preferredPackage){
        if(!modVisible)return State.MOD_NOT_VISIBLE;
        if(kuVisible && KUSETTINGS.equals(preferredPackage))
            return State.OEM_DEFAULT;
        if(MOD.equals(preferredPackage))
            return kuVisible ? State.MOD_DEFAULT_WITH_COMPETITOR : State.MOD_DEFAULT;
        if(kuVisible)return State.COMPETING_HANDLERS;
        return State.DEFAULT_UNKNOWN;
    }

    public static String describe(State state){
        switch(state){
            case MOD_DEFAULT:
                return "TvSettingsPlus выбран системой";
            case MOD_DEFAULT_WITH_COMPETITOR:
                return "TvSettingsPlus выбран системой; KuSettings тоже зарегистрирован";
            case OEM_DEFAULT:
                return "KuSettings выбран системой";
            case COMPETING_HANDLERS:
                return "Оба приложения объявили обработчик; выбор неоднозначен";
            case MOD_NOT_VISIBLE:
                return "Маршрут TvSettingsPlus не подтверждён";
            default:
                return "Не удалось подтвердить обработчик по умолчанию";
        }
    }

    public static boolean confirmedWithoutOldPackage(State state){
        return state==State.MOD_DEFAULT;
    }
    public static boolean chosenByAndroid(State state){
        return state==State.MOD_DEFAULT || state==State.MOD_DEFAULT_WITH_COMPETITOR;
    }
}

package dev.projectorsoft.tvsettings;

/**
 * Read-only quality of evidence from ActivityManager running-services.
 * Missing entries are NEVER proof a service has stopped on Android 13.
 */
public final class OemKuSourceServicePolicy {
    public enum State { RUNNING_VISIBLE, POSSIBLY_STOPPED, NOT_VERIFIABLE }
    private OemKuSourceServicePolicy(){}

    public static State evaluate(boolean snapshotNonEmpty,
                                 boolean sourcePackageVisible,
                                 boolean timeServiceVisible){
        if(timeServiceVisible)return State.RUNNING_VISIBLE;
        if(snapshotNonEmpty && sourcePackageVisible)return State.POSSIBLY_STOPPED;
        return State.NOT_VERIFIABLE;
    }

    public static String describe(State state){
        switch(state){
            case RUNNING_VISIBLE:
                return "KuSourceRJ.TimeService видна в запущенных службах";
            case POSSIBLY_STOPPED:
                return "TimeService не найдена, хотя другие службы KuSourceRJ видны; "
                    +"возможен сбой, требуется проверка";
            default:
                return "Состояние KuSourceRJ.TimeService не подтверждено: "
                    +"Android мог ограничить видимость служб";
        }
    }
}

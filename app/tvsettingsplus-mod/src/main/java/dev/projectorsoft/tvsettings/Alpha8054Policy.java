package dev.projectorsoft.tvsettings;

/**
 * Conservative runtime eligibility gate for the 8054 alpha.
 * A positive result permits displaying experimental controls, not asserting
 * that a physical HDMI, DSP, ToF, fan or optical adjustment was tested.
 */
public final class Alpha8054Policy {
    public enum State {
        CANDIDATE, UNKNOWN_BOARD, DIFFERENT_BOARD, CONFLICTING_IDENTIFIERS,
        HDMI_UNCONFIRMED, UNSUPPORTED_ANDROID
    }

    private Alpha8054Policy() {}

    private static String normalized(String value) {
        if (value == null) return "";
        String s=value.trim();
        if ("не определено".equalsIgnoreCase(s) || "unknown".equalsIgnoreCase(s)
                || "null".equalsIgnoreCase(s)) return "";
        return s;
    }

    public static State evaluate(String propertyId,String buildId,Integer hdmiCount,String android) {
        String p=normalized(propertyId);
        String b=normalized(buildId);
        if(!p.isEmpty() && !b.isEmpty() && !p.equals(b))
            return State.CONFLICTING_IDENTIFIERS;
        String device=!p.isEmpty()?p:b;
        if(device.isEmpty())return State.UNKNOWN_BOARD;
        if(!"8054".equals(device))return State.DIFFERENT_BOARD;
        if(hdmiCount==null || hdmiCount.intValue()!=2)
            return State.HDMI_UNCONFIRMED;
        if(!"13".equals(normalized(android)))
            return State.UNSUPPORTED_ANDROID;
        return State.CANDIDATE;
    }

    public static boolean allowExperimentalControls(State state) {
        return state == State.CANDIDATE;
    }

    public static String status(State state) {
        switch(state) {
            case CANDIDATE:
                return "8054 / два HDMI / Android 13: разрешены экспериментальные пункты. "
                    +"Аппаратные функции не проверены.";
            case UNKNOWN_BOARD:
                return "Ревизия платы не определена: новые аппаратные пункты скрыты.";
            case DIFFERENT_BOARD:
                return "Это не 8054: новые аппаратные пункты альфы скрыты.";
            case CONFLICTING_IDENTIFIERS:
                return "Идентификаторы прошивки противоречат друг другу: функции скрыты.";
            case HDMI_UNCONFIRMED:
                return "8054 определён, но два HDMI не подтверждены: функции скрыты.";
            case UNSUPPORTED_ANDROID:
                return "Версия Android не соответствует тестовой базе Android 13: функции скрыты.";
            default:
                return "Неизвестное состояние проверки: функции скрыты.";
        }
    }
}

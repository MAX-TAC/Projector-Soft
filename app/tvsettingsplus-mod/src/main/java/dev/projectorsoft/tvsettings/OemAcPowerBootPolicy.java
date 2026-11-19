package dev.projectorsoft.tvsettings;

/** Pure-JVM read/write verification for the stock MtkUtil electricity-boot control. */
public final class OemAcPowerBootPolicy {
    public interface Gateway {
        Boolean read() throws Exception;
        void write(boolean enabled) throws Exception;
    }
    private OemAcPowerBootPolicy() {}

    public static boolean available(Gateway gateway) {
        if (gateway==null) return false;
        try {
            return gateway.read()!=null;
        } catch (Exception e) {
            return false;
        }
    }

    /** Reject unconfirmed writes without changing the UI state. */
    public static boolean apply(Gateway gateway,Object value) {
        if(gateway==null || !(value instanceof Boolean))return false;
        boolean requested=((Boolean)value).booleanValue();
        try {
            Boolean original=gateway.read();
            if(original==null)return false;
            if(original.booleanValue()==requested)return true;
            gateway.write(requested);
            return Boolean.valueOf(requested).equals(gateway.read());
        }catch(Exception e){
            return false;
        }
    }
}

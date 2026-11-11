package dev.projectorsoft.tvsettings;

public final class OemBluetoothRemotePolicyTest {
    private static void ok(boolean value,String message){
        if(!value)throw new AssertionError(message);
    }
    public static void main(String[] args){
        ok(OemBluetoothRemotePolicy.potentialRemote(1280),"OEM peripheral class 1280");
        ok(OemBluetoothRemotePolicy.potentialRemote(0x500),"Bluetooth HID major class");
        ok(!OemBluetoothRemotePolicy.potentialRemote(0x400),"audio class is not remote");
        ok(!OemBluetoothRemotePolicy.potentialRemote(-1),"unknown class not counted");
        ok("HID-устройство".equals(OemBluetoothRemotePolicy.safeName(null)),
            "null name fallback");
        ok("HID-устройство".equals(OemBluetoothRemotePolicy.safeName("  \t  ")),
            "blank fallback");
        ok("Remote One".equals(OemBluetoothRemotePolicy.safeName("Remote\nOne")),
            "control chars not displayed");
        ok(OemBluetoothRemotePolicy.safeName(new String(new char[120]).replace('\0','x')).length()==64,
            "bounded input label");
        ok(OemBluetoothRemotePolicy.summary(true,true,true,2)
            .contains("не подтверждено"),"bonded != connected");
        ok(OemBluetoothRemotePolicy.summary(false,false,true,-1)
            .contains("недоступен"),"adapter unavailable");
        ok(OemBluetoothRemotePolicy.summary(true,false,true,0)
            .contains("выключен"),"adapter off");
        ok(OemBluetoothRemotePolicy.summary(true,true,false,0)
            .contains("Нельзя"),"permission denied");
        ok(OemBluetoothRemotePolicy.summary(true,true,true,-1)
            .contains("недоступен"),"unreliable snapshot");
        System.out.println("PASS: read-only Bluetooth HID policy, bounded names, no fake connection claims");
    }
}

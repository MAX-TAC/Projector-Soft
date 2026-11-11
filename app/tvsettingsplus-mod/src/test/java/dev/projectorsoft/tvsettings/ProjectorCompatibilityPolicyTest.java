package dev.projectorsoft.tvsettings;

public final class ProjectorCompatibilityPolicyTest {
    private static void check(boolean condition,String message) {
        if(!condition)throw new AssertionError(message);
    }
    public static void main(String[] args) {
        check(ProjectorCompatibilityPolicy.boardWarning("8054",2).contains("не проверены"),
              "Never advertise an 8052 APK as verified on 8054");
        check(ProjectorCompatibilityPolicy.boardWarning("8054",1).contains("не подтверждено"),
              "8054 HDMI board mismatch must fail closed");
        check(ProjectorCompatibilityPolicy.boardWarning("8052",2).contains("не соответствует"),
              "Reject board port mismatch on 8052");
        check(ProjectorCompatibilityPolicy.boardWarning("8052",null).contains("Нужна проверка"),
              "Unknown HDMI count must not be treated as success");
        check(ProjectorCompatibilityPolicy.boardWarning("?",1).contains("не подтверждена"),
              "Unknown revision fails closed");
        check(ProjectorCompatibilityPolicy.boardWarning("8052",1).contains("ещё не подтверждена"),
              "Matching IDs do not prove runtime compatibility");
        for(int i=0;i<5;i++){
            check(ProjectorCompatibilityPolicy.legacyPackageState(i)!=null,
                  "All PackageManager states must have safe text");
        }
        check(ProjectorCompatibilityPolicy.legacyPackageState(2).contains("резервное охлаждение"),
              "Do not mark disabled OEM thermal watchdog as safe");
        check(ProjectorCompatibilityPolicy.legacyPackageState(99).contains("Неизвестный"),
              "Unknown PackageManager states must be reported");
        System.out.println("PASS: hardware gating and legacy safety diagnostics");
    }
}

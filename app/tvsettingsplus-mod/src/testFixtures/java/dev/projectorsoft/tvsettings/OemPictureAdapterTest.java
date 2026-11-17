package dev.projectorsoft.tvsettings;
import android.content.*;
import android.provider.Settings;
public final class OemPictureAdapterTest {
    static void check(boolean value){if(!value)throw new AssertionError();}
    public static void main(String[] args) {
        Context context=new Context();
        SharedPreferences journal=context.getSharedPreferences("projectorsoft_picture_rollback",0);
        Settings.Global.values.put("picture_brightness","50");
        Settings.Global.values.put("picture_mode","7");
        check(!OemPictureState.mayEdit());
        check(!OemPictureState.write(context,"picture_brightness",80));
        check(!OemPictureState.write(context,"picture_mode",3));
        check(journal.getAll().isEmpty()&&journal.commits==0&&Settings.Global.writes==0);
        check("50".equals(Settings.Global.values.get("picture_brightness")));
        journal.edit().putString("picture_brightness","UNKNOWN\n50").putString("picture_mode","7").commit();
        int commits=journal.commits;
        context=new Context(); // Actual production adapter reconstructed over persisted test storage.
        check(!OemPictureState.write(context,"picture_brightness",80));
        check(!OemPictureState.restore(context));
        check(journal.commits==commits&&Settings.Global.writes==0);
        check("UNKNOWN\n50".equals(journal.getAll().get("picture_brightness")));
        System.out.println("PASS production OemPictureState adapter: UNKNOWN blocks writes, snapshots and replay, including restart/legacy");
    }
}

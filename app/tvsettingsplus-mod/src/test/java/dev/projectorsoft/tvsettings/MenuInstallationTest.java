package dev.projectorsoft.tvsettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MenuInstallationTest {
    public static void main(String[] args) {
        MenuInstallation installation=new MenuInstallation();
        Object screen=new Object();
        List<String> rows=new ArrayList<>();
        rows.add("stock");
        try(MenuInstallation.Attempt attempt=installation.begin(screen)) {
            attempt.track(()->rows.remove("picture"));
            rows.add("picture");
            attempt.track(()->rows.remove("sound"));
            rows.add("sound");
            throw new IllegalStateException("mid-install failure");
        }catch(IllegalStateException expected){
            if(!"mid-install failure".equals(expected.getMessage()))throw expected;
        }
        if(!rows.equals(Arrays.asList("stock")))throw new AssertionError(rows);
        try(MenuInstallation.Attempt attempt=installation.begin(screen)) {
            if(attempt==null)throw new AssertionError("retry was blocked");
            attempt.track(()->rows.remove("picture"));
            rows.add("picture");
            attempt.track(()->rows.remove("sound"));
            rows.add("sound");
            attempt.commit();
        }
        if(installation.begin(screen)!=null || !rows.equals(Arrays.asList("stock","picture","sound")))
            throw new AssertionError("retry did not yield exactly one of each section");
        Object second=new Object();
        List<String> attempted=new ArrayList<>();
        try (MenuInstallation.Attempt attempt=installation.begin(second)) {
            attempt.track(()->attempted.add("first"));
            attempt.track(()->{throw new IllegalStateException("undo failed");});
            attempt.track(()->attempted.add("last"));
        } catch (IllegalStateException expected) {
            if(!"undo failed".equals(expected.getMessage()))throw expected;
        }
        if(!attempted.equals(Arrays.asList("last","first")))
            throw new AssertionError("failed undo prevented other cleanup");
        try (MenuInstallation.Attempt retry=installation.begin(second)) {
            if(retry==null)throw new AssertionError("failed cleanup prevented retry");
        }
        System.out.println("PASS menu installation: partial failure rolls back, reopen installs once");
    }
}

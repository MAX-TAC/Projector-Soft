package dev.projectorsoft.tvsettings;

import static dev.projectorsoft.tvsettings.ProjectionToggleState.Result.*;

public final class ProjectionToggleStateTest {
    private static final class Stock implements ProjectionToggleState.Backend {
        String persisted = "0";
        boolean available = true;
        boolean failRead;
        boolean failWrite;
        boolean apply = true;
        boolean allowed = true;
        boolean failGate;
        boolean loseReadback;
        int writes;

        @Override public String read() throws Exception {
            if (failRead) throw new Exception("getter failed");
            return available ? persisted : null;
        }

        @Override public boolean canWrite(boolean value) throws Exception {
            if (failGate) throw new Exception("dependency getter failed");
            return allowed;
        }

        @Override public void write(boolean value) throws Exception {
            writes++;
            if (apply) persisted = value ? "1" : "0";
            if (loseReadback) failRead = true;
            if (failWrite) throw new Exception("setter failed");
        }
    }

    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static void staleAndReopen() {
        Stock stock = new Stock();
        ProjectionToggleState state = new ProjectionToggleState(stock);
        check(Boolean.FALSE.equals(state.refresh()), "initial OEM state");
        stock.persisted = "1";
        check(state.change(true) == STALE && stock.writes == 0, "reject stale displayed state");
        check(Boolean.TRUE.equals(state.value()), "show external state");
        check(state.change(true) == CONFIRMED && stock.writes == 0, "no duplicate setter");
        check(state.change(false) == CONFIRMED && stock.writes == 1, "single OEM write");
        stock.persisted = "1";
        check(Boolean.TRUE.equals(state.refresh()), "refresh after OEM editor");
        check(Boolean.TRUE.equals(new ProjectionToggleState(stock).refresh()), "reopen reads OEM state");
    }

    private static void unavailableAndInvalid() {
        Stock stock = new Stock();
        ProjectionToggleState state = new ProjectionToggleState(stock);
        state.refresh();
        stock.available = false;
        check(state.change(true) == UNAVAILABLE && state.value() == null, "availability may disappear");
        stock.available = true;
        stock.persisted = "2";
        check(state.change(true) == UNAVAILABLE && state.value() == null, "invalid OEM state");
        stock.failRead = true;
        check(state.change(true) == UNAVAILABLE && stock.writes == 0, "failed getter blocks writes");
    }

    private static void delegatesWithUnavailableDependencies() {
        Stock stock = new Stock();
        stock.persisted = "1";
        ProjectionToggleState state = new ProjectionToggleState(stock);
        state.refresh();
        stock.allowed = false;
        check(state.change(false) == OEM_REQUIRED && stock.writes == 0, "active dependent uses OEM");
        stock.allowed = true;
        stock.failGate = true;
        check(state.change(false) == OEM_REQUIRED && stock.writes == 0, "dependency read failure uses OEM");
        check(Boolean.TRUE.equals(state.value()), "retain observed value when blocked by OEM gate");
        stock.failGate = false;
        check(state.change(false) == CONFIRMED && stock.writes == 1, "all dependents confirmed off");
    }

    private static void setterAndReadbackFailures() {
        Stock stock = new Stock();
        ProjectionToggleState state = new ProjectionToggleState(stock);
        state.refresh();
        stock.apply = false;
        check(state.change(true) == FAILED && Boolean.FALSE.equals(state.value()), "readback mismatch");
        stock.failWrite = true;
        check(state.change(true) == FAILED && Boolean.FALSE.equals(state.value()), "setter throws before change");
        stock.apply = true;
        check(state.change(true) == FAILED && Boolean.TRUE.equals(state.value()), "partial change is visible");
        check(stock.writes == 3, "no retry or rollback after failure");

        stock.failWrite = false;
        stock.loseReadback = true;
        check(state.change(false) == FAILED && state.value() == null && stock.writes == 4,
                "disable until readable after unknown outcome");
        stock.failRead = false;
        check(Boolean.FALSE.equals(state.refresh()), "recover actual value without writing");
    }

    public static void main(String[] args) {
        staleAndReopen();
        unavailableAndInvalid();
        setterAndReadbackFailures();
        delegatesWithUnavailableDependencies();
        System.out.println("PASS projection switch snapshots, stale writes, failure and readback");
    }
}

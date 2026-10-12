package com.techgame.state;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameClockTest {
    private static GameState state(long now) { return new GameState(() -> now); }
    @Test void GC01_ensureDeadline_unstarted() {
        GameState s=state(100_000); assertEquals(160_000, GameClock.ensureDeadline(s)); assertEquals(160_000,s.timerEnd);
    }
    @Test void GC02_ensureDeadline_existing() {
        GameState s=state(100_000); s.timerEnd=170_000;
        assertEquals(170_000,GameClock.ensureDeadline(s));
    }
    @Test void GC03_ensureDeadline_expiredNonzero() {
        GameState s=state(100_000); s.timerEnd=99_999;
        assertEquals(99_999,GameClock.ensureDeadline(s));
    }
    @Test void GC04_ensureDeadline_nullState() { assertThrows(NullPointerException.class, () -> GameClock.ensureDeadline(null)); }
    @Test void GC05_adjustDeadline_positive() {
        GameState s=state(100_000); s.timerEnd=150_000;
        GameClock.adjustDeadline(s,1); assertEquals(150_001,s.timerEnd);
    }
    @Test void GC06_adjustDeadline_zero() {
        GameState s=state(100_000); s.timerEnd=150_000;
        GameClock.adjustDeadline(s,0); assertEquals(150_000,s.timerEnd);
    }
    @Test void GC07_adjustDeadline_negative() {
        GameState s=state(100_000); s.timerEnd=150_000;
        GameClock.adjustDeadline(s,-1); assertEquals(149_999,s.timerEnd);
    }
    @Test void GC08_adjustDeadline_unstarted() {
        GameState s=state(100_000);
        GameClock.adjustDeadline(s,10_000); assertEquals(170_000,s.timerEnd);
    }
    @Test void GC09_adjustDeadline_nullState() {
        assertThrows(NullPointerException.class, () -> GameClock.adjustDeadline(null,0));
    }
    @Test void GC10_remainingMs_unstarted() {
        assertEquals(0,GameClock.remainingMs(state(100_000)));
    }
    @Test void GC11_remainingMs_oneBeforeDeadline() {
        GameState s=state(100_000); s.timerEnd=100_001;
        assertEquals(1,GameClock.remainingMs(s));
    }
    @Test void GC12_remainingMs_exactDeadline() {
        GameState s=state(100_000); s.timerEnd=100_000;
        assertEquals(0,GameClock.remainingMs(s));
    }
    @Test void GC13_remainingMs_oneAfterDeadline() {
        GameState s=state(100_000); s.timerEnd=99_999;
        assertEquals(0,GameClock.remainingMs(s));
    }
    @Test void GC14_remainingMs_nullState() {
        assertThrows(NullPointerException.class, () -> GameClock.remainingMs(null));
    }
}

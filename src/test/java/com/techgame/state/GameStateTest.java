package com.techgame.state;

import com.techgame.data.Pools;
import com.techgame.data.Progress;
import com.techgame.support.TestFixtures;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {
    private static final long NOW=100_000;
    @Test void GS01_defaultConstructor() {
        GameState s=new GameState();
        assertEquals(0,s.score); assertEquals(0,s.timerEnd);
        assertEquals(Mode.SCORED,s.mode); assertFalse(s.progress.isComplete());
        long before=System.currentTimeMillis(); long actual=s.now(); long after=System.currentTimeMillis();
        assertTrue(actual>=before && actual<=after);
    }
    @Test void GS02_customClock() {
        GameState s=new GameState(() -> NOW); assertEquals(NOW,s.now());
    }
    @Test void GS03_nullClockDeferredException() {
        GameState s=new GameState(null);
        assertThrows(NullPointerException.class, s::now);
    }
    @Test void GS04_now_returnsNegativeClockValue() {
        assertEquals(-1,new GameState(() -> -1L).now());
    }
    @Test void GS05_isScored_scored() {
        GameState s=new GameState(() -> NOW); assertTrue(s.isScored());
    }
    @Test void GS06_isScored_freeplay() {
        GameState s=new GameState(() -> NOW); s.mode=Mode.FREEPLAY; assertFalse(s.isScored());
    }
    @Test void GS07_isScored_nullModeObservedBehavior() {
        GameState s=new GameState(() -> NOW); s.mode=null; assertTrue(s.isScored());
    }
    @Test void GS08_startSession_validReset() {
        GameState s=new GameState(() -> NOW);
        s.score=99; s.timerEnd=110_000; s.playerName="Alex"; s.mode=Mode.FREEPLAY;
        s.startSession(TestFixtures.pools(6));
        assertNotNull(s.round); assertNotNull(s.pools); assertEquals(0,s.score);
        assertEquals(0,s.timerEnd); assertNull(s.playerName);
        assertEquals(Mode.SCORED,s.mode); assertFalse(s.progress.isComplete());
    }
    @Test void GS09_startSession_nullPools() {
        GameState s=new GameState(() -> NOW);
        assertThrows(NullPointerException.class, () -> s.startSession(null));
    }
    @Test void GS10_startSession_emptyDrugs() {
        GameState s=new GameState(() -> NOW);
        assertThrows(RuntimeException.class, () -> s.startSession(TestFixtures.pools(0)));
    }
    @Test void GS11_dealNextRound_validPreservesSessionState() {
        GameState s=new GameState(() -> NOW); s.startSession(TestFixtures.pools(6));
        s.score=50; s.timerEnd=150_000; s.playerName="Alex";
        Progress before=s.progress;
        s.dealNextRound();
        assertNotNull(s.round); assertNotSame(before,s.progress);
        assertFalse(s.progress.isComplete()); assertEquals(50,s.score);
        assertEquals(150_000,s.timerEnd); assertEquals("Alex",s.playerName);
    }
    @Test void GS12_dealNextRound_missingPools() {
        GameState s=new GameState(() -> NOW);
        assertThrows(NullPointerException.class, s::dealNextRound);
    }
    @Test void GS13_resetRun_withValidPools() {
        GameState s=new GameState(() -> NOW); s.startSession(TestFixtures.pools(6));
        s.score=50; s.timerEnd=150_000; s.playerName="Alex"; s.mode=Mode.FREEPLAY;
        s.progress.picked="Brand0";
        s.resetRun();
        assertEquals(0,s.score); assertEquals(0,s.timerEnd);
        assertEquals("Alex",s.playerName); assertEquals(Mode.FREEPLAY,s.mode);
        assertNotNull(s.round); assertFalse(s.progress.isComplete());
    }
    @Test void GS14_resetRun_withoutPools() {
        GameState s=new GameState(() -> NOW); s.score=50; s.timerEnd=150_000;
        s.resetRun(); assertEquals(0,s.score); assertEquals(0,s.timerEnd);
        assertNull(s.round); assertFalse(s.progress.isComplete());
    }
}

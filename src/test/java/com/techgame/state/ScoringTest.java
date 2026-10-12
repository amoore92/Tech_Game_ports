package com.techgame.state;

import com.techgame.data.Progress;
import com.techgame.support.TestFixtures;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ScoringTest {
    private static final long NOW=100_000L;
    @Test void SC22_applyWrong_alreadyExpired() {
        GameState s=TestFixtures.scored(NOW,0);
        assertTrue(Scoring.applyWrong(s));
        assertEquals(0,GameClock.remainingMs(s));
    }
    @Test void SC01_correctExactMatch() { assertTrue(Scoring.isCorrect("Zocor","Zocor")); }
    @Test void SC02_correctDifferentString() { assertFalse(Scoring.isCorrect("Lipitor","Zocor")); }
    @Test void SC03_correctNullSubmitted() { assertFalse(Scoring.isCorrect(null,"Zocor")); }
    @Test void SC04_correctNullExpected() { assertFalse(Scoring.isCorrect("Zocor",null)); }
    @Test void SC05_correctBothNull() { assertFalse(Scoring.isCorrect(null,null)); }
    @Test void SC06_correctCaseSensitive() { assertFalse(Scoring.isCorrect("zocor","Zocor")); }
    @Test void SC07_correctBothEmpty() { assertTrue(Scoring.isCorrect("","")); }

    @Test void SC08_applyCorrect_scoredRunning() {
        GameState s=TestFixtures.scored(NOW,20_000); s.score=10;
        Scoring.applyCorrect(s); assertEquals(15,s.score); assertEquals(NOW+30_000,s.timerEnd);
    }
    @Test void SC09_applyCorrect_freeplayNoChange() {
        GameState s=TestFixtures.scored(NOW,20_000); s.mode=Mode.FREEPLAY; s.score=10;
        Scoring.applyCorrect(s); assertEquals(10,s.score); assertEquals(NOW+20_000,s.timerEnd);
    }
    @Test void SC10_applyCorrect_unstarted() {
        GameState s=new GameState(() -> NOW);
        Scoring.applyCorrect(s); assertEquals(5,s.score); assertEquals(NOW+70_000,s.timerEnd);
    }
    @Test void SC11_applyCorrect_null() { assertThrows(NullPointerException.class, () -> Scoring.applyCorrect(null)); }
    @Test void SC12_applyWrong_belowFiveSeconds() {
        GameState s=TestFixtures.scored(NOW,4_999); s.score=8;
        assertTrue(Scoring.applyWrong(s)); assertEquals(8,s.score);
        assertEquals(NOW-1,s.timerEnd); assertEquals(0,GameClock.remainingMs(s));
    }
    @Test void SC13_applyWrong_exactlyFiveSeconds() {
        GameState s=TestFixtures.scored(NOW,5_000);
        assertTrue(Scoring.applyWrong(s)); assertEquals(NOW,s.timerEnd);
    }
    @Test void SC14_applyWrong_aboveFiveSeconds() {
        GameState s=TestFixtures.scored(NOW,5_001);
        assertFalse(Scoring.applyWrong(s)); assertEquals(1,GameClock.remainingMs(s));
    }
    @Test void SC15_applyWrong_freeplay() {
        GameState s=TestFixtures.scored(NOW,4_000); s.mode=Mode.FREEPLAY; s.score=8;
        assertFalse(Scoring.applyWrong(s)); assertEquals(8,s.score);
        assertEquals(NOW+4_000,s.timerEnd);
    }
    @Test void SC16_applyWrong_unstarted() {
        GameState s=new GameState(() -> NOW);
        assertFalse(Scoring.applyWrong(s)); assertEquals(NOW+55_000,s.timerEnd);
    }
    @Test void SC17_applyWrong_null() { assertThrows(NullPointerException.class, () -> Scoring.applyWrong(null)); }
    @Test void SC18_applyHandIn_scored() {
        GameState s=new GameState(() -> NOW); s.startSession(TestFixtures.pools(6));
        s.timerEnd=NOW+20_000; s.score=10;
        Progress previous=s.progress;
        Scoring.applyHandIn(s);
        assertEquals(20,s.score); assertEquals(NOW+30_000,s.timerEnd);
        assertNotSame(previous,s.progress); assertFalse(s.progress.isComplete()); assertNotNull(s.round);
    }
    @Test void SC19_applyHandIn_freeplay() {
        GameState s=new GameState(() -> NOW); s.startSession(TestFixtures.pools(6));
        s.mode=Mode.FREEPLAY; s.score=10; s.timerEnd=NOW+20_000;
        Scoring.applyHandIn(s);
        assertEquals(10,s.score); assertEquals(NOW+20_000,s.timerEnd); assertNotNull(s.round);
    }
    @Test void SC20_applyHandIn_null() { assertThrows(NullPointerException.class, () -> Scoring.applyHandIn(null)); }
    @Test void SC21_applyHandIn_noPools() {
        GameState s=new GameState(() -> NOW);
        assertThrows(RuntimeException.class, () -> Scoring.applyHandIn(s));
        // A failed hand-in can have already changed score/time: not transactional.
    }
}

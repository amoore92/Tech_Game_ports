package com.techgame.support;

import com.techgame.data.*;
import com.techgame.state.*;
import java.util.ArrayList;
import java.util.List;

/** Minimal, controlled data for specification-based tests. Not production drug data. */
public final class TestFixtures {
    private TestFixtures() {}

    public static Drug drug(int n) {
        return new Drug("Generic" + n, "Brand" + n, "Class" + n,
                "Indication" + n, "Effect" + n, "Warning" + n);
    }
    public static List<Drug> drugs(int count) {
        List<Drug> result = new ArrayList<>();
        for (int i = 0; i < count; i++) result.add(drug(i));
        return result;
    }
    public static Pools pools(int drugCount) {
        return new Pools(List.of("NAME needs DRUG"),
                new Names(List.of("Ava"), List.of("Stone")), drugs(drugCount));
    }
    public static GameState scored(long now, long timeRemaining) {
        GameState s = new GameState(() -> now);
        s.mode = Mode.SCORED;
        s.timerEnd = now + timeRemaining;
        return s;
    }
}

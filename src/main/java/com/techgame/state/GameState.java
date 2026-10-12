package com.techgame.state;

import com.techgame.data.Pools;
import com.techgame.data.Progress;
import com.techgame.data.Round;
import com.techgame.data.RoundFactory;

import java.util.function.LongSupplier;

/**
 * Session-wide state shared by every screen (the Phaser version kept this in the
 * game registry). Screens are rebuilt on every transition, so anything that must
 * survive one lives here.
 */
public final class GameState
{
    /** Source of "now" in epoch ms for {@link GameClock}. */
    private final LongSupplier clock;

    /** What new rounds are dealt from. */
    public Pools pools;
    /** The patient currently at the counter. */
    public Round round;
    /** Which build steps are done for {@link #round}. */
    public Progress progress = new Progress();
    /** Pending one-shot HUD message for the next screen, or null. */
    public Flash flash;
    public int score;
    /** Absolute end time of the shared countdown (epoch ms); 0 = clock not started. */
    public long timerEnd;
    /** Null until the player enters one this session. */
    public String playerName;
    public Mode mode = Mode.SCORED;

    /** Uses the system clock. */
    public GameState ()
    {
        this(System::currentTimeMillis);
    }

    /**
     * Uses the given time source (epoch ms). Tests pass a controllable one so
     * clock behaviour can be asserted exactly, e.g. {@code new GameState(() -> now[0])}.
     */
    public GameState (LongSupplier clock)
    {
        this.clock = clock;
    }

    /** Current time in epoch ms, from this state's time source. */
    public long now ()
    {
        return clock.getAsLong();
    }

    public boolean isScored ()
    {
        return mode != Mode.FREEPLAY;
    }

    /** Fresh session once the data has loaded. */
    public void startSession (Pools pools)
    {
        this.pools = pools;
        round = RoundFactory.makeRound(pools);
        progress = new Progress();
        flash = null;
        score = 0;
        //  The clock begins on the first Game/build screen so menu time isn't counted.
        timerEnd = 0;
        playerName = null;
        mode = Mode.SCORED;
    }

    /** After a hand-in: the next patient, with nothing built yet. */
    public void dealNextRound ()
    {
        round = RoundFactory.makeRound(pools);
        progress = new Progress();
    }

    /** From GameOver: score to 0, cleared progress, stopped clock and a new round. */
    public void resetRun ()
    {
        score = 0;
        progress = new Progress();
        flash = null;
        timerEnd = 0;
        if (pools != null)
        {
            round = RoundFactory.makeRound(pools);
        }
    }
}

package com.techgame.state;

/**
 * A single countdown that persists across the Game and build-step screens.
 *
 * <p>Each screen is rebuilt on every transition, so a per-screen timer would keep
 * resetting. Instead an ABSOLUTE end time is stored in {@link GameState} and every
 * screen just renders "how much is left" against it -- the clock keeps ticking no
 * matter which screen is showing, and any screen can end the game.
 */
public final class GameClock
{
    /** 60 seconds per game -- tweak to taste. */
    public static final long GAME_DURATION_MS = 60_000;

    private GameClock ()
    {
    }

    /**
     * Makes sure a shared deadline exists. The clock starts on the first call after
     * a reset, so time isn't drained while sitting in a menu.
     */
    public static long ensureDeadline (GameState state)
    {
        if (state.timerEnd == 0)
        {
            state.timerEnd = state.now() + GAME_DURATION_MS;
        }
        return state.timerEnd;
    }

    /**
     * Shifts the deadline by {@code deltaMs} (positive = add time). Ensures the clock
     * is running first so an adjustment never no-ops while it is stopped.
     */
    public static void adjustDeadline (GameState state, long deltaMs)
    {
        state.timerEnd = ensureDeadline(state) + deltaMs;
    }

    public static long remainingMs (GameState state)
    {
        return state.timerEnd == 0 ? 0 : Math.max(0, state.timerEnd - state.now());
    }
}

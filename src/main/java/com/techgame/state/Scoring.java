package com.techgame.state;

/**
 * The scoring and time rules for answering and handing in, kept free of UI so they
 * can be unit tested. The screens call these and only handle the visuals (flashes,
 * moving strips, switching screens).
 *
 * <p>In freeplay mode there is no clock and no score, so nothing changes.
 */
public final class Scoring
{
    /** Points for a correct drop (drug pick or label part). */
    public static final int CORRECT_POINTS = 5;
    /** Time added for a correct drop. */
    public static final long CORRECT_BONUS_MS = 10_000;
    /** Time removed for a wrong drop. */
    public static final long WRONG_PENALTY_MS = 5_000;
    /** Points for handing in a finished drug. */
    public static final int HAND_IN_POINTS = 10;
    /** Time added for handing in a finished drug. */
    public static final long HAND_IN_BONUS_MS = 10_000;

    private Scoring ()
    {
    }

    /** Is the dropped value the right answer? Exact, case-sensitive match; null is never correct. */
    public static boolean isCorrect (String value, String correct)
    {
        return value != null && value.equals(correct);
    }

    /** Correct drop: +5 points and +10 s in scored mode. */
    public static void applyCorrect (GameState state)
    {
        if (state.isScored())
        {
            state.score += CORRECT_POINTS;
            GameClock.adjustDeadline(state, CORRECT_BONUS_MS);
        }
    }

    /**
     * Wrong drop: -5 s in scored mode (score is unchanged).
     *
     * @return true if that emptied the clock, i.e. the game is over; always false in freeplay
     */
    public static boolean applyWrong (GameState state)
    {
        if (!state.isScored())
        {
            return false;
        }
        GameClock.adjustDeadline(state, -WRONG_PENALTY_MS);
        return GameClock.remainingMs(state) <= 0;
    }

    /** Hand-in of a finished drug: +10 points and +10 s in scored mode, then the next round. */
    public static void applyHandIn (GameState state)
    {
        if (state.isScored())
        {
            state.score += HAND_IN_POINTS;
            GameClock.adjustDeadline(state, HAND_IN_BONUS_MS);
        }
        state.dealNextRound();
    }
}

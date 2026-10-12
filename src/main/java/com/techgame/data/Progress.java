package com.techgame.data;

/**
 * Per-round build progress: which steps the player has completed for the current
 * drug. {@code null} = not done yet. A fresh instance is used for every new round.
 */
public final class Progress
{
    public String picked;
    public String indications;
    public String sideEffects;
    public String warnings;

    /** Ready to hand in once the drug is picked and all three label parts are placed. */
    public boolean isComplete ()
    {
        return picked != null && indications != null && sideEffects != null && warnings != null;
    }
}

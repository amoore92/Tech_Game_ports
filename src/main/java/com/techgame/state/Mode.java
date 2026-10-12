package com.techgame.state;

/** Scored = timer + score; Freeplay = no clock, no points (practise without pressure). */
public enum Mode
{
    SCORED("Scored"),
    FREEPLAY("Freeplay");

    public final String label;

    Mode (String label)
    {
        this.label = label;
    }

    public Mode toggled ()
    {
        return this == SCORED ? FREEPLAY : SCORED;
    }
}

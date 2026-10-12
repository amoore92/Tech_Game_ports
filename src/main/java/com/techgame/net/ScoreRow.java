package com.techgame.net;

/** One leaderboard entry. The score is kept as text, exactly as the backend sent it. */
public record ScoreRow(String name, String score)
{
    private static final int MAX_NAME = 12;

    /** "1.  Name  —  42" */
    public String line (int index)
    {
        String cleanName = name.length() > MAX_NAME ? name.substring(0, MAX_NAME) : name;
        return (index + 1) + ".  " + cleanName + "  —  " + score;
    }
}

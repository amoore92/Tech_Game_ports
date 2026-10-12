package com.techgame.ui;

/** An axis-aligned box in top-down world coordinates. */
public record Box(float left, float top, float right, float bottom)
{
    public static Box ofSize (float x, float y, float width, float height)
    {
        return new Box(x, y, x + width, y + height);
    }

    public float width ()
    {
        return right - left;
    }

    public float height ()
    {
        return bottom - top;
    }

    public float centerX ()
    {
        return (left + right) / 2;
    }

    public float centerY ()
    {
        return (top + bottom) / 2;
    }

    public boolean contains (float x, float y)
    {
        return x >= left && x <= right && y >= top && y <= bottom;
    }
}

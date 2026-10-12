package com.techgame.ui;

import com.badlogic.gdx.graphics.Color;

/**
 * The subset of Phaser text styles the game uses. All text is Roboto Black, centre
 * aligned, with an optional black stroke.
 *
 * @param size      font size in world pixels
 * @param stroke    black stroke thickness (Phaser strokeThickness), 0 for none
 * @param wrapWidth word-wrap width, 0 for no wrapping
 */
public record TextStyle(int size, Color color, int stroke, float wrapWidth)
{
    public TextStyle (int size, Color color, int stroke)
    {
        this(size, color, stroke, 0);
    }

    public TextStyle wrap (float width)
    {
        return new TextStyle(size, color, stroke, width);
    }

    public TextStyle withSize (int newSize)
    {
        return new TextStyle(newSize, color, stroke, wrapWidth);
    }
}

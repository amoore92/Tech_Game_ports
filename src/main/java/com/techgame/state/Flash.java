package com.techgame.state;

import com.badlogic.gdx.graphics.Color;

/** A pending one-shot HUD message, shown (and cleared) by the next screen's HUD. */
public record Flash(String text, Color color)
{
}

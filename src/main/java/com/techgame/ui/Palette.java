package com.techgame.ui;

import com.badlogic.gdx.graphics.Color;

/** Colours shared across screens (treat as read-only). */
public final class Palette
{
    public static final Color DEFAULT_BG = hex(0x253237);
    public static final Color MENU_BG = hex(0x222244);
    public static final Color BUTTON_FACE = hex(0x9db4c0);
    public static final Color BUTTON_SHADOW = hex(0x5c6b73);
    public static final Color HUD_BAR = hex(0x333333);
    public static final Color GOLD = hex(0xFFD700);
    public static final Color GREEN = hex(0x7CFC9B);
    public static final Color RED = hex(0xFF6B6B);
    public static final Color GREY = hex(0xcccccc);
    public static final Color INK = hex(0x15202b);

    private Palette ()
    {
    }

    /** 0xRRGGBB -> opaque Color. */
    public static Color hex (int rgb)
    {
        return new Color((rgb << 8) | 0xff);
    }
}

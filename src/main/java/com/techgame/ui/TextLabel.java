package com.techgame.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;

/**
 * A label that stays centred on an anchor point as its text or font changes
 * (Phaser text with {@code setOrigin(0.5)}). Not touchable, so it never steals
 * input from whatever it sits on.
 */
public class TextLabel extends Label
{
    private final Fonts fonts;
    private TextStyle style;
    private float anchorX;
    private float anchorY;
    private final boolean ready;

    public TextLabel (Fonts fonts, CharSequence text, TextStyle style)
    {
        super(text, new LabelStyle(fonts.get(style.size(), style.stroke()), Color.WHITE));
        this.fonts = fonts;
        this.style = style;
        setColor(style.color());
        setAlignment(Align.center);
        setWrap(style.wrapWidth() > 0);
        setTouchable(Touchable.disabled);
        ready = true;
        refit();
    }

    /** Centres the label on (x, y) in its parent's (y-up) coordinates. */
    public TextLabel centerOn (float x, float y)
    {
        anchorX = x;
        anchorY = y;
        refit();
        return this;
    }

    public void setFontSize (int size)
    {
        style = style.withSize(size);
        setStyle(new LabelStyle(fonts.get(size, style.stroke()), Color.WHITE));
        refit();
    }

    /** Width of the widest rendered line (Phaser's text.width). */
    public float textWidth ()
    {
        validate();
        return getGlyphLayout().width;
    }

    @Override
    public void setText (CharSequence newText)
    {
        super.setText(newText);
        if (ready) refit();
    }

    private void refit ()
    {
        if (getWrap())
        {
            setWidth(style.wrapWidth());
            setHeight(getPrefHeight());
        }
        else
        {
            setSize(getPrefWidth(), getPrefHeight());
        }
        setPosition(anchorX, anchorY, Align.center);
    }
}

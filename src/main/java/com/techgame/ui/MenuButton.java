package com.techgame.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.techgame.TechGame;

import static com.techgame.ui.Ui.flipY;

/**
 * The raised, clickable button used on every menu screen: a face over an offset
 * shadow. While pressed the face sinks onto the shadow.
 */
public class MenuButton extends Group
{
    private static final float WIDTH = 360;
    private static final float HEIGHT = 100;
    private static final float DEPTH = 12;
    private static final TextStyle LABEL_STYLE = new TextStyle(42, Color.WHITE, 6);

    private final ShapeActor shadow;
    private final ShapeActor face;
    private final TextLabel label;
    private final ClickListener click;

    /** Centred on (x, y) in top-down world coordinates. */
    public MenuButton (TechGame game, float x, float y, String text, Runnable onClick)
    {
        //  Hit area is centred on the face (the shadow pokes out down-right), as in Phaser.
        setSize(WIDTH + DEPTH, HEIGHT + DEPTH);
        setPosition(x, flipY(y), Align.center);
        setTouchable(Touchable.enabled);

        shadow = new ShapeActor(game.shapes, WIDTH, HEIGHT, Palette.BUTTON_SHADOW);
        face = new ShapeActor(game.shapes, WIDTH, HEIGHT, Palette.BUTTON_FACE).stroke(Color.WHITE, 6);
        label = new TextLabel(game.fonts, text, LABEL_STYLE);
        addActor(shadow);
        addActor(face);
        addActor(label);
        layoutParts(false);

        click = new ClickListener()
        {
            @Override
            public void clicked (InputEvent event, float x, float y)
            {
                onClick.run();
            }
        };
        addListener(click);
        Ui.hoverCursor(this, SystemCursor.Hand);
    }

    public void setText (String text)
    {
        label.setText(text);
    }

    @Override
    public void act (float delta)
    {
        super.act(delta);
        layoutParts(click.isVisualPressed());
    }

    private void layoutParts (boolean pressed)
    {
        float cx = getWidth() / 2;
        float cy = getHeight() / 2;
        float offset = pressed ? DEPTH : 0;
        shadow.centerOn(cx + DEPTH, cy - DEPTH);
        shadow.setVisible(!pressed);
        face.centerOn(cx + offset, cy - offset);
        label.centerOn(cx + offset, cy - offset);
    }
}

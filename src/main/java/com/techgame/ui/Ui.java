package com.techgame.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.Align;

/**
 * World constants and small scene2d helpers.
 *
 * <p><b>Coordinates:</b> layout constants throughout the screens are kept in the
 * original top-down world coordinates (y grows downward, as in Phaser and in the
 * artwork), so they can be compared 1:1 with the art. libGDX's y axis points up;
 * {@link #flipY} converts at the point of placement.
 */
public final class Ui
{
    /** Art is authored at 1440x2560 (2x this world). */
    public static final float WORLD_W = 720;
    public static final float WORLD_H = 1280;

    /** Phaser's 'Back.easeOut' (same formula, same 1.70158 overshoot). */
    public static final Interpolation BACK_OUT = new Interpolation.SwingOut(1.70158f);

    private Ui ()
    {
    }

    /** Top-down world y -> libGDX world y. */
    public static float flipY (float topDownY)
    {
        return WORLD_H - topDownY;
    }

    /** Is the actor's centre inside a top-down box? (Actor must be in an untransformed layer.) */
    public static boolean centerInside (Actor actor, Box box)
    {
        return box.contains(actor.getX(Align.center), WORLD_H - actor.getY(Align.center));
    }

    /** Shows {@code cursor} while the mouse is over the actor (Phaser's useHandCursor). */
    public static void hoverCursor (Actor actor, SystemCursor cursor)
    {
        actor.addListener(new InputListener()
        {
            @Override
            public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor)
            {
                if (pointer == -1) Gdx.graphics.setSystemCursor(cursor);
            }

            @Override
            public void exit (InputEvent event, float x, float y, int pointer, Actor toActor)
            {
                if (pointer == -1) Gdx.graphics.setSystemCursor(SystemCursor.Arrow);
            }
        });
    }

    /**
     * Lets the player drag the actor around; {@code onDrop} runs on release.
     * Grabbing it again cancels any snap-back still in progress.
     */
    public static void draggable (Actor actor, Runnable onDrop)
    {
        DragListener drag = new DragListener()
        {
            @Override
            public void dragStart (InputEvent event, float x, float y, int pointer)
            {
                actor.clearActions();
                actor.toFront();
            }

            @Override
            public void drag (InputEvent event, float x, float y, int pointer)
            {
                actor.moveBy(x - getTouchDownX(), y - getTouchDownY());
            }

            @Override
            public void dragStop (InputEvent event, float x, float y, int pointer)
            {
                onDrop.run();
            }
        };
        //  Phaser starts dragging on the first pixel of movement.
        drag.setTapSquareSize(0);
        actor.addListener(drag);
    }

    /** Slides the actor's centre back to a top-down home position. */
    public static void snapBack (Actor actor, float homeX, float homeY)
    {
        actor.addAction(Actions.moveToAligned(homeX, flipY(homeY), Align.center, 0.2f, BACK_OUT));
    }
}

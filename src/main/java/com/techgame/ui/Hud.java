package com.techgame.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.techgame.SceneKey;
import com.techgame.TechGame;
import com.techgame.state.Flash;
import com.techgame.state.GameClock;
import com.techgame.state.GameState;

import static com.techgame.ui.Ui.flipY;

/**
 * Shared top bar used by the build-step screens and reused by Game. Holds the
 * score, the shared countdown, and a one-line flash that explains why a pick did
 * or didn't work ("Wrong  -5 seconds" in red, "+10 seconds" in green).
 *
 * <p>In scored mode it shows the timer + score; in freeplay just a "Freeplay" tag.
 */
public final class Hud
{
    /** A text position (top-down world coordinates) and font size. */
    public record Spot(float x, float y, int size)
    {
    }

    /**
     * Where the HUD goes. {@code drawBar} draws the default dark bar behind it; Game
     * turns it off and fits the text into the background art's built-in box.
     */
    public record Layout(boolean drawBar, Spot timer, Spot score, Spot flash)
    {
        public static final Layout TOP_BAR = new Layout(true,
                new Spot(360, 40, 30), new Spot(360, 82, 28), new Spot(360, 130, 26));
    }

    private static final float TICK_SECONDS = 0.25f;

    private final TechGame game;
    private final GameState state;
    private final TextLabel flash;
    private TextLabel countdown;
    private boolean fired;   //  guard so GameOver is only started once

    public Hud (TechGame game, Group layer, Layout layout)
    {
        this.game = game;
        this.state = game.state;

        if (layout.drawBar())
        {
            layer.addActor(new ShapeActor(game.shapes, 320, 90, Palette.HUD_BAR).centerOn(360, flipY(60)));
        }

        if (state.isScored())
        {
            //  Start the clock if it isn't already running. Each tick reads the
            //  deadline back from the state (not a captured value) so mid-screen
            //  adjustments -- e.g. a wrong pick's -5s -- show up live.
            GameClock.ensureDeadline(state);
            Spot t = layout.timer();
            countdown = add(layer, t.x(), t.y(), "", new TextStyle(t.size(), Palette.GOLD, 6));
            refreshTimer();   //  paint immediately so there's no blank frame
            countdown.addAction(Actions.forever(Actions.delay(TICK_SECONDS, Actions.run(this::refreshTimer))));

            Spot s = layout.score();
            add(layer, s.x(), s.y(), "Score: " + state.score, new TextStyle(s.size(), Color.WHITE, 6));
        }
        else
        {
            float fx = layout.drawBar() ? 360 : layout.timer().x();
            float fy = layout.drawBar() ? 60 : (layout.timer().y() + layout.score().y()) / 2;
            add(layer, fx, fy, "Freeplay", new TextStyle(28, Palette.GREY, 6));
        }

        Spot f = layout.flash();
        flash = add(layer, f.x(), f.y(), "", new TextStyle(f.size(), Palette.RED, 5));

        //  Consume any pending one-shot flash left by the previous screen (e.g. the
        //  green "+10 seconds" after a correct pick advanced us here).
        Flash pending = state.flash;
        if (pending != null)
        {
            state.flash = null;
            showFlash(pending.text(), pending.color());
        }
    }

    public void showFlash (String text)
    {
        showFlash(text, Palette.RED);
    }

    public void showFlash (String text, Color color)
    {
        flash.clearActions();
        flash.setText(text);
        flash.setColor(color);
        flash.addAction(Actions.sequence(
                Actions.delay(0.7f),
                Actions.fadeOut(0.6f),
                Actions.run(() -> flash.setText(""))));
    }

    /**
     * Repaints the clock now and returns the remaining ms; starts GameOver if time
     * has run out. In freeplay there is no clock and this returns Long.MAX_VALUE.
     */
    public long refreshTimer ()
    {
        if (countdown == null)
        {
            return Long.MAX_VALUE;
        }
        long remaining = GameClock.remainingMs(state);
        countdown.setText("Time: " + (long) Math.ceil(remaining / 1000.0));
        if (remaining <= 0 && !fired)
        {
            fired = true;
            game.start(SceneKey.GAME_OVER);
        }
        return remaining;
    }

    public static String wrongFlashText (boolean scored)
    {
        return scored ? "Wrong  -5 seconds" : "Wrong - try again";
    }

    /** Queues the green "+10 seconds" message for the next screen's HUD. */
    public static void queueCorrectFlash (GameState state)
    {
        state.flash = new Flash("+10 seconds", Palette.GREEN);
    }

    private TextLabel add (Group layer, float x, float y, String text, TextStyle style)
    {
        TextLabel label = new TextLabel(game.fonts, text, style).centerOn(x, flipY(y));
        layer.addActor(label);
        return label;
    }
}

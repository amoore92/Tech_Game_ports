package com.techgame.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Align;
import com.techgame.SceneKey;
import com.techgame.TechGame;
import com.techgame.data.OptionSet;
import com.techgame.data.Round;
import com.techgame.state.Scoring;
import com.techgame.ui.Box;
import com.techgame.ui.Hud;
import com.techgame.ui.Palette;
import com.techgame.ui.ShapeActor;
import com.techgame.ui.TextLabel;
import com.techgame.ui.TextStyle;
import com.techgame.ui.Ui;

import java.util.List;

import static com.techgame.ui.Ui.flipY;

/**
 * Every step of building a drug to hand over -- the drug pick AND the three label
 * steps -- works the same way: drag the one correct value strip up onto the paper.
 * Correct -> +10s/+5pts and on to the next step; wrong -> -5s, a red flash, and
 * the strip snaps home. A wrong pick that empties the clock goes to GameOver.
 */
public class BuildStepScreen extends BaseScreen
{
    //  The paper sheet (top half) the correct strip is dragged onto.
    private static final Box PAPER = new Box(50, 225, 670, 560);

    private static final float STRIP_WIDTH = 660;
    private static final float STRIP_HEIGHT = 124;
    private static final float STRIP_START_Y = 635;
    private static final float STRIP_GAP = 138;
    private static final int MAX_FONT = 30;
    private static final int MIN_FONT = 16;

    private final BuildStep step;
    private final Hud hud;
    private TextLabel placed;
    private Group strips;
    private boolean solved;   //  guard so only the first correct drop counts

    public BuildStepScreen (TechGame game, BuildStep step)
    {
        super(game, Palette.MENU_BG);
        this.step = step;

        hud = new Hud(game, stage.getRoot(), Hud.Layout.TOP_BAR);

        Round round = state.round;
        //  Defensive: should never happen in normal flow, but don't hard-crash.
        if (round == null)
        {
            game.start(SceneKey.GAME);
            return;
        }

        String contextText = step.context.apply(round);
        if (contextText != null)
        {
            addText(360, 180, contextText, new TextStyle(26, Palette.GOLD, 5).wrap(650));
        }

        stage.addActor(new ShapeActor(game.shapes, PAPER.width(), PAPER.height(), Palette.hex(0xfdf6e3))
                .rounded(18)
                .stroke(Palette.hex(0xbfae8e), 4)
                .centerOn(PAPER.centerX(), flipY(PAPER.centerY())));

        addText(360, 268, step.heading, new TextStyle(40, Palette.hex(0x3a2f1a), 0).wrap(560));

        //  Where the placed value lands once dropped correctly.
        placed = addText(360, 415, "", new TextStyle(30, Color.BLACK, 0).wrap(560));

        //  Bottom half: 5 draggable, full-width value strips.
        strips = addLayer();
        OptionSet optionSet = step.options.apply(round);
        List<String> options = optionSet.options();
        for (int i = 0; i < options.size(); i++)
        {
            createStrip(360, STRIP_START_Y + i * STRIP_GAP, options.get(i), optionSet.correct());
        }
    }

    //  A full-width draggable strip showing one option value (kept in '/' form).
    private void createStrip (float homeX, float homeY, String value, String correct)
    {
        Group strip = new Group();
        strip.setSize(STRIP_WIDTH, STRIP_HEIGHT);
        strip.setPosition(homeX, flipY(homeY), Align.center);
        strip.setTouchable(Touchable.enabled);

        strip.addActor(new ShapeActor(game.shapes, STRIP_WIDTH, STRIP_HEIGHT, Palette.BUTTON_FACE)
                .stroke(Color.WHITE, 4));

        //  Start big for readability, then shrink only as far as needed so even the
        //  long warning strings still fit inside the strip.
        TextLabel label = new TextLabel(game.fonts, value,
                new TextStyle(MAX_FONT, Palette.hex(0x10202b), 0).wrap(STRIP_WIDTH - 30));
        int fontSize = MAX_FONT;
        while (label.getHeight() > STRIP_HEIGHT - 18 && fontSize > MIN_FONT)
        {
            fontSize -= 2;
            label.setFontSize(fontSize);
        }
        label.centerOn(STRIP_WIDTH / 2, STRIP_HEIGHT / 2);
        strip.addActor(label);

        Ui.hoverCursor(strip, SystemCursor.Hand);
        Ui.draggable(strip, () ->
        {
            if (!Ui.centerInside(strip, PAPER))
            {
                Ui.snapBack(strip, homeX, homeY);
            }
            else if (Scoring.isCorrect(value, correct))
            {
                solve(strip, value);
            }
            else
            {
                gradeWrong(strip, homeX, homeY);
            }
        });
        strips.addActor(strip);
    }

    //  Wrong drop: lose 5s and flash. Repaint the clock immediately; if that drop
    //  emptied it, the HUD already started GameOver and we stop here.
    private void gradeWrong (Group strip, float homeX, float homeY)
    {
        boolean timeUp = Scoring.applyWrong(state);
        hud.refreshTimer();   //  repaint now; starts GameOver if the clock is empty
        if (timeUp)
        {
            return;
        }
        hud.showFlash(Hud.wrongFlashText(state.isScored()));
        Ui.snapBack(strip, homeX, homeY);
    }

    //  Correct drop: record the part, reward time + points, show it on the paper,
    //  then advance to the next step.
    private void solve (Group strip, String value)
    {
        if (solved) return;
        solved = true;
        strips.setTouchable(Touchable.disabled);

        step.record.accept(state.progress, value);

        Scoring.applyCorrect(state);
        if (state.isScored())
        {
            hud.refreshTimer();   //  show the +10s right away
            Hud.queueCorrectFlash(state);
        }

        //  Tuck the chosen value onto the paper, then move on.
        strip.setVisible(false);
        placed.setText(value);

        later(0.45f, () -> game.start(step.next));
    }
}

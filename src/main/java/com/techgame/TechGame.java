package com.techgame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.techgame.net.ScoreApi;
import com.techgame.screens.BuildStep;
import com.techgame.screens.BuildStepScreen;
import com.techgame.screens.GameOverScreen;
import com.techgame.screens.GameScreen;
import com.techgame.screens.LoadingScreen;
import com.techgame.screens.MainMenuScreen;
import com.techgame.screens.ScoreboardScreen;
import com.techgame.screens.SetNameScreen;
import com.techgame.screens.SettingsScreen;
import com.techgame.state.GameState;
import com.techgame.ui.Fonts;

import java.util.function.Supplier;

/** Application entry point shared by every platform launcher. */
public class TechGame extends Game
{
    public final GameState state = new GameState();
    public final ScoreApi scores;

    public SpriteBatch batch;
    public ShapeRenderer shapes;
    public Assets assets;
    public Fonts fonts;
    /** 1x1 white texture for tinted UI drawables (text cursor, selection). */
    public Texture whitePixel;

    private Supplier<Screen> pendingScreen;

    /** @param scoresUrl base URL of the scores backend, or empty to play offline */
    public TechGame (String scoresUrl)
    {
        scores = new ScoreApi(scoresUrl);
    }

    @Override
    public void create ()
    {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        assets = new Assets();
        fonts = new Fonts(Assets.FONT);

        Pixmap pixel = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixel.setColor(Color.WHITE);
        pixel.fill();
        whitePixel = new Texture(pixel);
        pixel.dispose();

        setScreen(new LoadingScreen(this));
    }

    /**
     * Switches to a fresh instance of a screen (like Phaser's scene.start, the
     * screen is rebuilt every time). The switch happens at the start of the next
     * frame, so it is safe to call from input handlers and actions.
     */
    public void start (SceneKey key)
    {
        pendingScreen = () -> switch (key)
        {
            case MAIN_MENU -> new MainMenuScreen(this);
            case SCOREBOARD -> new ScoreboardScreen(this);
            case GAME -> new GameScreen(this);
            case SETTINGS -> new SettingsScreen(this);
            case INVENTORY -> new BuildStepScreen(this, BuildStep.DRUG);
            case LABEL_INDICATIONS -> new BuildStepScreen(this, BuildStep.INDICATIONS);
            case LABEL_SIDE_EFFECTS -> new BuildStepScreen(this, BuildStep.SIDE_EFFECTS);
            case LABEL_WARNINGS -> new BuildStepScreen(this, BuildStep.WARNINGS);
            case GAME_OVER -> new GameOverScreen(this);
        };
    }

    /** The name-entry screen, which continues on to {@code next} once confirmed. */
    public void startSetName (SceneKey next)
    {
        pendingScreen = () -> new SetNameScreen(this, next);
    }

    @Override
    public void render ()
    {
        if (pendingScreen != null)
        {
            Supplier<Screen> next = pendingScreen;
            pendingScreen = null;
            Screen old = getScreen();
            setScreen(next.get());
            if (old != null)
            {
                old.dispose();
            }
        }
        super.render();
    }

    @Override
    public void dispose ()
    {
        if (getScreen() != null)
        {
            getScreen().dispose();
        }
        batch.dispose();
        shapes.dispose();
        fonts.dispose();
        assets.dispose();
        whitePixel.dispose();
    }
}

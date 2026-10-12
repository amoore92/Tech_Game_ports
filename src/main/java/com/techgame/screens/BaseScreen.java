package com.techgame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.techgame.TechGame;
import com.techgame.state.GameState;
import com.techgame.ui.TextLabel;
import com.techgame.ui.TextStyle;

import static com.techgame.ui.Ui.WORLD_H;
import static com.techgame.ui.Ui.WORLD_W;
import static com.techgame.ui.Ui.flipY;

/**
 * Common screen plumbing: a 720x1280 stage letterboxed into the window (Phaser's
 * Scale.FIT + CENTER_BOTH), clipped so art never spills into the bars.
 */
public abstract class BaseScreen extends ScreenAdapter
{
    protected final TechGame game;
    protected final GameState state;
    protected final Stage stage;
    private final Color background;
    private boolean disposed;

    protected BaseScreen (TechGame game, Color background)
    {
        this.game = game;
        this.state = game.state;
        this.background = background;
        this.stage = new Stage(new FitViewport(WORLD_W, WORLD_H), game.batch);
    }

    @Override
    public void show ()
    {
        Gdx.input.setInputProcessor(stage);
        Gdx.graphics.setSystemCursor(SystemCursor.Arrow);
    }

    @Override
    public void hide ()
    {
        if (Gdx.input.getInputProcessor() == stage)
        {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void render (float delta)
    {
        stage.act(delta);

        ScreenUtils.clear(Color.BLACK);
        Viewport viewport = stage.getViewport();
        viewport.apply();
        HdpiUtils.glScissor(viewport.getScreenX(), viewport.getScreenY(),
                viewport.getScreenWidth(), viewport.getScreenHeight());
        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
        ScreenUtils.clear(background);
        stage.draw();
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
    }

    @Override
    public void resize (int width, int height)
    {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose ()
    {
        disposed = true;
        stage.dispose();
    }

    /** False once the player has left this screen (for late async callbacks). */
    protected boolean isActive ()
    {
        return !disposed && game.getScreen() == this;
    }

    /** Runs {@code task} after a delay (Phaser's time.delayedCall). */
    protected void later (float seconds, Runnable task)
    {
        stage.addAction(Actions.delay(seconds, Actions.run(task)));
    }

    /** Adds text centred on (x, y) in top-down world coordinates. */
    protected TextLabel addText (Group parent, float x, float y, String text, TextStyle style)
    {
        TextLabel label = new TextLabel(game.fonts, text, style).centerOn(x, flipY(y));
        parent.addActor(label);
        return label;
    }

    protected TextLabel addText (float x, float y, String text, TextStyle style)
    {
        return addText(stage.getRoot(), x, y, text, style);
    }

    /** Full-bleed art stretched over the whole world. */
    protected Image addFullScreenImage (Group parent, String path)
    {
        Image image = new Image(game.assets.texture(path));
        image.setBounds(0, 0, WORLD_W, WORLD_H);
        image.setTouchable(Touchable.disabled);
        parent.addActor(image);
        return image;
    }

    /** An untransformed, full-world group used as a draw-order layer. */
    protected Group addLayer ()
    {
        Group layer = new Group();
        layer.setTransform(false);
        layer.setTouchable(Touchable.childrenOnly);
        stage.addActor(layer);
        return layer;
    }
}

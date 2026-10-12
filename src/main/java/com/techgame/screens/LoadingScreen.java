package com.techgame.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.techgame.Assets;
import com.techgame.SceneKey;
import com.techgame.TechGame;
import com.techgame.data.GameData;
import com.techgame.ui.Palette;
import com.techgame.ui.ShapeActor;

import static com.techgame.ui.Ui.flipY;

/**
 * Boot + Preloader: shows the loading art and a progress bar while the textures
 * load, then parses the data files, starts the session and opens the main menu.
 */
public class LoadingScreen extends BaseScreen
{
    private static final float CENTER_X = 360;
    private static final float CENTER_Y = 640;
    private static final float BAR_MAX = 460;

    private final ShapeActor bar;
    private boolean finished;

    public LoadingScreen (TechGame game)
    {
        super(game, Palette.DEFAULT_BG);

        game.assets.loadBoot();
        Image background = new Image(game.assets.texture(Assets.LOADING_BG));
        background.setTouchable(Touchable.disabled);
        background.setPosition(CENTER_X, flipY(CENTER_Y), Align.center);
        stage.addActor(background);

        //  Outline of the bar, then the bar itself growing from the left.
        stage.addActor(new ShapeActor(game.shapes, 468, 32, Color.CLEAR)
                .stroke(Color.WHITE, 1)
                .centerOn(CENTER_X, flipY(CENTER_Y)));
        bar = new ShapeActor(game.shapes, 4, 28, Color.WHITE);
        bar.setPosition(CENTER_X - 232, flipY(CENTER_Y) - 14);
        stage.addActor(bar);

        game.assets.queueGame();
    }

    @Override
    public void render (float delta)
    {
        if (!finished && game.assets.update())
        {
            finished = true;
            state.startSession(GameData.parsePools(
                    game.assets.text(Assets.GREETINGS),
                    game.assets.text(Assets.NAMES),
                    game.assets.text(Assets.DRUGS)));
            game.start(SceneKey.MAIN_MENU);
        }
        bar.setWidth(4 + BAR_MAX * game.assets.progress());
        super.render(delta);
    }
}

package com.techgame.screens;

import com.badlogic.gdx.graphics.Color;
import com.techgame.Assets;
import com.techgame.SceneKey;
import com.techgame.TechGame;
import com.techgame.ui.MenuButton;
import com.techgame.ui.Palette;
import com.techgame.ui.TextStyle;

public class MainMenuScreen extends BaseScreen
{
    public MainMenuScreen (TechGame game)
    {
        super(game, Palette.DEFAULT_BG);

        //  Full-screen background; the title card is baked into the art at the
        //  top (world 75,75 -> 570,410), so buttons sit below that band.
        addFullScreenImage(stage.getRoot(), Assets.MAIN_BG);

        addText(360, 1210, "Version 1.0.0", new TextStyle(20, Color.WHITE, 6));

        stage.addActor(new MenuButton(game, 360, 600, "PLAY", () ->
        {
            //  First play this session has no name yet -> collect one, then the
            //  name screen routes on to Game. Otherwise jump straight in.
            if (state.playerName != null)
            {
                game.start(SceneKey.GAME);
            }
            else
            {
                game.startSetName(SceneKey.GAME);
            }
        }));

        stage.addActor(new MenuButton(game, 360, 760, "SCOREBOARD", () -> game.start(SceneKey.SCOREBOARD)));
        stage.addActor(new MenuButton(game, 360, 920, "SETTINGS", () -> game.start(SceneKey.SETTINGS)));
    }
}

package com.techgame.screens;

import com.badlogic.gdx.graphics.Color;
import com.techgame.SceneKey;
import com.techgame.TechGame;
import com.techgame.ui.MenuButton;
import com.techgame.ui.Palette;
import com.techgame.ui.TextStyle;

public class SettingsScreen extends BaseScreen
{
    private MenuButton modeButton;

    public SettingsScreen (TechGame game)
    {
        super(game, Palette.MENU_BG);

        addText(360, 200, "Settings", new TextStyle(60, Color.WHITE, 8));

        //  Mode toggle: scored (timer + score, ends on the clock) <-> freeplay (no
        //  timer, no score). The label updates in place each tap.
        modeButton = new MenuButton(game, 360, 460, modeLabel(), () ->
        {
            state.mode = state.mode.toggled();
            modeButton.setText(modeLabel());
        });
        stage.addActor(modeButton);

        //  Re-enter the player name. Returns here afterwards -- it does NOT start a
        //  game (unlike the PLAY path from the main menu).
        stage.addActor(new MenuButton(game, 360, 640, "Set Name", () -> game.startSetName(SceneKey.SETTINGS)));

        stage.addActor(new MenuButton(game, 360, 820, "Back to Menu", () -> game.start(SceneKey.MAIN_MENU)));
    }

    private String modeLabel ()
    {
        return "Mode: " + state.mode.label;
    }
}

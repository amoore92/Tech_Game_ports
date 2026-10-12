package com.techgame.screens;

import com.badlogic.gdx.graphics.Color;
import com.techgame.SceneKey;
import com.techgame.TechGame;
import com.techgame.net.ScoreRow;
import com.techgame.ui.MenuButton;
import com.techgame.ui.Palette;
import com.techgame.ui.TextLabel;
import com.techgame.ui.TextStyle;

import java.util.List;

/** View-only leaderboard, reachable from the main menu. Fails soft to an empty list. */
public class ScoreboardScreen extends BaseScreen
{
    private final TextLabel statusText;

    public ScoreboardScreen (TechGame game)
    {
        super(game, Palette.MENU_BG);

        addText(360, 140, "— Leaderboard —", new TextStyle(48, Palette.GOLD, 8));
        statusText = addText(360, 260, "Loading…", new TextStyle(28, Palette.GREY, 4));

        game.scores.getLeaderboard(this::showScores);

        stage.addActor(new MenuButton(game, 360, 1120, "Back", () -> game.start(SceneKey.MAIN_MENU)));
    }

    private void showScores (List<ScoreRow> rows)
    {
        //  Player may have left already -> bail.
        if (!isActive())
        {
            return;
        }

        statusText.remove();

        if (rows.isEmpty())
        {
            addText(360, 260, "No scores yet — be the first!", new TextStyle(28, Palette.GREY, 4));
            return;
        }

        for (int i = 0; i < rows.size(); i++)
        {
            ScoreRow row = rows.get(i);
            boolean isYou = row.name().equals(state.playerName);
            addText(360, 250 + i * 48, row.line(i), new TextStyle(30, isYou ? Palette.GOLD : Color.WHITE, 4));
        }
    }
}

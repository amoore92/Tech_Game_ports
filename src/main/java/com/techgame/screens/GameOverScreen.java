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

public class GameOverScreen extends BaseScreen
{
    private final TextLabel statusText;
    private final String playerName;

    public GameOverScreen (TechGame game)
    {
        super(game, Palette.MENU_BG);

        //  Read the final score + name BEFORE any reset so we can show/submit them.
        int score = state.score;
        playerName = state.playerName != null ? state.playerName : "Player";
        boolean scored = state.isScored();

        addText(360, 140, "Game Over", new TextStyle(56, Color.WHITE, 8));
        addText(360, 250, scored ? "Final Score: " + score : "Freeplay — not scored",
                new TextStyle(44, scored ? Palette.GREEN : Palette.GREY, 7));
        addText(360, 360, "— Leaderboard —", new TextStyle(34, Palette.GOLD, 6));

        //  Placeholder while the network call is in flight.
        statusText = addText(360, 430, "Loading…", new TextStyle(26, Palette.GREY, 4));

        //  Submit this run's score (scored mode only -- freeplay isn't ranked),
        //  THEN load the leaderboard so the new score is already included.
        if (scored)
        {
            game.scores.submitScore(playerName, score, ok -> game.scores.getLeaderboard(this::showScores));
        }
        else
        {
            game.scores.getLeaderboard(this::showScores);
        }

        //  Play Again -> wipe the score/result and deal a fresh round, then play.
        stage.addActor(new MenuButton(game, 360, 950, "Play Again?", () ->
        {
            state.resetRun();
            game.start(SceneKey.GAME);
        }));

        //  Quit -> reset too, so returning to the menu starts clean next time.
        stage.addActor(new MenuButton(game, 360, 1120, "Quit", () ->
        {
            state.resetRun();
            game.start(SceneKey.MAIN_MENU);
        }));
    }

    private void showScores (List<ScoreRow> rows)
    {
        //  Screen may have been left already (player clicked a button) -- bail.
        if (!isActive())
        {
            return;
        }

        statusText.remove();

        if (rows.isEmpty())
        {
            addText(360, 430, "No scores yet — be the first!", new TextStyle(26, Palette.GREY, 4));
            return;
        }

        for (int i = 0; i < rows.size(); i++)
        {
            ScoreRow row = rows.get(i);
            //  Gold-highlight rows belonging to this player.
            boolean isYou = row.name().equals(playerName);
            addText(360, 420 + i * 42, row.line(i), new TextStyle(28, isYou ? Palette.GOLD : Color.WHITE, 4));
        }
    }
}

package com.techgame.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.techgame.SceneKey;
import com.techgame.TechGame;
import com.techgame.ui.MenuButton;
import com.techgame.ui.Palette;
import com.techgame.ui.ShapeActor;
import com.techgame.ui.TextStyle;
import com.techgame.ui.Ui;

import static com.techgame.ui.Ui.flipY;

/**
 * A small name-entry screen. Reached two ways:
 * <ul>
 *   <li>from MainMenu PLAY when no name has been set this session (-> Game)</li>
 *   <li>from Settings via "Set Name" (-> back to Settings, no game starts)</li>
 * </ul>
 * The Phaser version overlaid an HTML input; a scene2d TextField covers both
 * hardware keyboards and (on mobile backends) the on-screen keyboard.
 */
public class SetNameScreen extends BaseScreen
{
    private static final int MAX_LENGTH = 16;
    private static final Color INPUT_TEXT = Palette.hex(0x222244);
    private static final Color PLACEHOLDER = Palette.hex(0x8a8a9a);

    private final SceneKey next;
    private final TextField nameInput;

    public SetNameScreen (TechGame game, SceneKey next)
    {
        super(game, Palette.MENU_BG);
        this.next = next;

        addText(360, 280, "Enter your name", new TextStyle(48, Color.WHITE, 7));

        //  The input box: white, gold border, rounded.
        stage.addActor(new ShapeActor(game.shapes, 456, 84, Color.WHITE)
                .rounded(12)
                .stroke(Palette.GOLD, 4)
                .centerOn(360, flipY(440)));

        nameInput = new TextField(state.playerName != null ? state.playerName : "", inputStyle());
        nameInput.setMessageText("Your name");
        nameInput.setMaxLength(MAX_LENGTH);
        nameInput.setAlignment(Align.center);
        nameInput.setSize(420, nameInput.getPrefHeight());
        nameInput.setPosition(360, flipY(440), Align.center);
        nameInput.setCursorPosition(nameInput.getText().length());
        //  Enter submits, same as the Confirm button.
        nameInput.setTextFieldListener((field, c) ->
        {
            if (c == '\r' || c == '\n') confirm();
        });
        Ui.hoverCursor(nameInput, SystemCursor.Ibeam);
        stage.addActor(nameInput);
        //  Focus so you can type right away.
        stage.setKeyboardFocus(nameInput);

        addText(360, 560, "Tap the box to type, then press Enter or Confirm", new TextStyle(24, Palette.GREY, 4));

        stage.addActor(new MenuButton(game, 360, 720, "Confirm", this::confirm));
    }

    private void confirm ()
    {
        String name = nameInput.getText().trim();
        state.playerName = name.isEmpty() ? "Player" : name;
        game.start(next);
    }

    private TextFieldStyle inputStyle ()
    {
        BitmapFont font = game.fonts.get(40, 0);
        TextureRegionDrawable white = new TextureRegionDrawable(game.whitePixel);
        Drawable cursor = white.tint(INPUT_TEXT);
        cursor.setMinWidth(3);
        Drawable selection = white.tint(new Color(Palette.GOLD.r, Palette.GOLD.g, Palette.GOLD.b, 0.5f));

        TextFieldStyle style = new TextFieldStyle(font, INPUT_TEXT, cursor, selection, null);
        style.messageFont = font;
        style.messageFontColor = PLACEHOLDER;
        return style;
    }
}

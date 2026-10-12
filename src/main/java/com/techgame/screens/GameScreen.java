package com.techgame.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.techgame.Assets;
import com.techgame.SceneKey;
import com.techgame.TechGame;
import com.techgame.data.Round;
import com.techgame.state.Scoring;
import com.techgame.ui.Box;
import com.techgame.ui.Hud;
import com.techgame.ui.Palette;
import com.techgame.ui.ShapeActor;
import com.techgame.ui.TextLabel;
import com.techgame.ui.TextStyle;
import com.techgame.ui.Ui;

import static com.techgame.ui.Ui.WORLD_W;
import static com.techgame.ui.Ui.flipY;

/**
 * Round bookend. At the start of a round the patient stands at the counter with
 * their request in a speech bubble; tapping the counter box opens the build flow.
 * Once the drug is built (drug picked + all three label parts placed) the finished
 * drug card appears to drag into the drop box -- handing it in banks +10s/+10pts
 * and deals the next round.
 */
public class GameScreen extends BaseScreen
{
    //  Black panel baked into the background for the timer + score.
    private static final Box SCORE_BOX = Box.ofSize(250, 110, 224.5f, 80);
    //  Clickable region on the foreground that opens the Inventory/build flow.
    private static final Box INV_BOX = Box.ofSize(87.5f, 677.5f, 262.5f, 210);
    //  Invisible box the finished drug card is dropped into.
    private static final Box DROP_BOX = new Box(50, 131, 669, 728);

    private static final float CARD_HOME_X = 360;
    private static final float CARD_HOME_Y = 1060;
    private static final Color HOVER = new Color(1, 1, 1, 0.12f);

    //  Draw-order layers (Phaser depth bands), bottom to top.
    private final Group bgLayer;
    private final Group personLayer;
    private final Group fgLayer;
    private final Group hudLayer;
    private final Group bubbleLayer;

    public GameScreen (TechGame game)
    {
        super(game, Palette.DEFAULT_BG);
        bgLayer = addLayer();
        personLayer = addLayer();
        fgLayer = addLayer();
        hudLayer = addLayer();
        bubbleLayer = addLayer();

        //  Layer 1: background (score/time box baked in).
        addFullScreenImage(bgLayer, Assets.GAME_BG);

        Round round = state.round;
        boolean complete = round != null && state.progress.isComplete();

        //  Speech bubble (returns its bottom edge so the person can sit below it).
        float bubbleBottom = 240;
        if (round != null)
        {
            bubbleBottom = createSpeechBubble(round.greetingText());
        }

        //  Layer 2: the customer, behind the counter.
        if (round != null && round.personKey() != null)
        {
            createPerson(round.personKey(), bubbleBottom);
        }

        //  Layer 3: foreground (the counter), drawn over the customer.
        addFullScreenImage(fgLayer, Assets.GAME_FG);

        //  Timer + score fitted into the background's black box.
        new Hud(game, hudLayer, new Hud.Layout(false,
                new Hud.Spot(SCORE_BOX.centerX(), 136, 26),
                new Hud.Spot(SCORE_BOX.centerX(), 170, 26),
                new Hud.Spot(360, 224, 24)));

        //  Give-up button, top-right.
        ShapeActor backButton = ShapeActor.circle(game.shapes, 40, Palette.hex(0xaa3333)).centerOn(660, flipY(60));
        backButton.setTouchable(Touchable.enabled);
        backButton.addListener(new InputListener()
        {
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button)
            {
                game.start(SceneKey.GAME_OVER);
                return true;
            }
        });
        Ui.hoverCursor(backButton, SystemCursor.Hand);
        hudLayer.addActor(backButton);

        if (complete)
        {
            //  Built and ready: drag the finished drug into the box to hand over.
            createPickupCard(round.drugCorrect());
        }
        else
        {
            //  Tap the counter box to start picking the drug.
            createInventoryZone();
        }
    }

    //  A pop-up speech bubble holding the greeting, centred near the top. Returns
    //  the top-down y of its bottom edge (tail included).
    private float createSpeechBubble (String greeting)
    {
        float topY = 248;
        float padX = 40;
        float padY = 30;
        float tail = 28;

        TextLabel text = new TextLabel(game.fonts, greeting, new TextStyle(30, Palette.INK, 0).wrap(520));
        float bw = Math.min(WORLD_W - 40, Math.max(text.textWidth() + padX * 2, 300));
        float bh = text.getHeight() + padY * 2;

        Group bubble = new Group();
        bubble.setSize(bw, bh);
        bubble.setOrigin(Align.center);
        bubble.setPosition(360, flipY(topY + bh / 2), Align.center);
        bubble.setTouchable(Touchable.disabled);

        bubble.addActor(new ShapeActor(game.shapes, bw, bh, Color.WHITE)
        {
            //  Little tail pointing down toward the customer.
            @Override
            protected void drawExtras (ShapeRenderer shapes, float x, float y, float alpha)
            {
                float cx = x + getWidth() / 2;
                shapes.setColor(1, 1, 1, alpha);
                shapes.triangle(cx - 22, y + 2, cx + 22, y + 2, cx, y - tail);
            }
        }.rounded(22).stroke(Palette.INK, 4));
        bubble.addActor(text.centerOn(bw / 2, bh / 2));
        bubbleLayer.addActor(bubble);

        //  Pop in.
        bubble.setScale(0);
        bubble.addAction(Actions.scaleTo(1, 1, 0.28f, Ui.BACK_OUT));

        return topY + bh + tail;
    }

    //  A random customer figure standing at the counter, behind the foreground.
    //  Anchored by its TOP just below the speech bubble, so its size grows
    //  downward (behind the counter) and never collides with the bubble.
    private void createPerson (String personKey, float bubbleBottom)
    {
        float size = 855;
        //  Raised up slightly; the bubble/HUD draw on top, so any overlap is hidden.
        float topY = bubbleBottom - 250;

        Image person = new Image(game.assets.texture(Assets.personPath(personKey)));
        person.setSize(size, size);
        person.setPosition(360, flipY(topY), Align.top);
        person.setTouchable(Touchable.disabled);
        personLayer.addActor(person);
    }

    //  Invisible, tappable counter box on the foreground -> Inventory build flow.
    private void createInventoryZone ()
    {
        ShapeActor zone = new ShapeActor(game.shapes, INV_BOX.width(), INV_BOX.height(), Color.CLEAR)
                .centerOn(INV_BOX.centerX(), flipY(INV_BOX.centerY()));
        zone.setTouchable(Touchable.enabled);
        zone.addListener(new ClickListener()
        {
            //  Faint highlight so it's discoverable on hover/press.
            @Override
            public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor)
            {
                super.enter(event, x, y, pointer, fromActor);
                zone.setFill(HOVER);
            }

            @Override
            public void exit (InputEvent event, float x, float y, int pointer, Actor toActor)
            {
                super.exit(event, x, y, pointer, toActor);
                zone.setFill(Color.CLEAR);
            }

            @Override
            public void clicked (InputEvent event, float x, float y)
            {
                game.start(SceneKey.INVENTORY);
            }
        });
        Ui.hoverCursor(zone, SystemCursor.Hand);
        hudLayer.addActor(zone);
    }

    //  The finished drug, sitting near the bottom. Drag it into the invisible drop
    //  box to turn it in; dropping anywhere else snaps it back home.
    private void createPickupCard (String label)
    {
        Texture cardTexture = game.assets.texture(Assets.DRUG_CARD);
        float height = 160;
        float width = cardTexture.getWidth() * height / cardTexture.getHeight();

        Group card = new Group();
        card.setSize(width, height);
        card.setPosition(CARD_HOME_X, flipY(CARD_HOME_Y), Align.center);
        card.setTouchable(Touchable.enabled);

        Image image = new Image(cardTexture);
        image.setSize(width, height);
        image.setTouchable(Touchable.disabled);
        card.addActor(image);
        card.addActor(new TextLabel(game.fonts, label, new TextStyle(30, Color.BLACK, 0).wrap(width - 40))
                .centerOn(width / 2, height / 2));

        Ui.hoverCursor(card, SystemCursor.Hand);
        Ui.draggable(card, () ->
        {
            if (Ui.centerInside(card, DROP_BOX))
            {
                card.setTouchable(Touchable.disabled);   //  no second drop
                handIn();
            }
            else
            {
                //  Missed the box -> slide back to the starting spot.
                Ui.snapBack(card, CARD_HOME_X, CARD_HOME_Y);
            }
        });
        bubbleLayer.addActor(card);
    }

    //  Hand the finished drug over: bank the bonus, deal a fresh round, and restart
    //  at the greeting. The selections were already graded in the build steps.
    private void handIn ()
    {
        Scoring.applyHandIn(state);
        game.start(SceneKey.GAME);
    }
}

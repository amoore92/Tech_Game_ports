package com.techgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.utils.Disposable;

import java.util.List;

/** Asset paths (relative to /assets) and the shared {@link AssetManager}. */
public final class Assets implements Disposable
{
    //  Shown while everything else loads (Boot scene).
    public static final String LOADING_BG = "bg.png";

    public static final String DRUG_CARD = "Drug card.png";
    //  Full-bleed scene art (authored at 1440x2560 = 2x the 720x1280 world).
    public static final String MAIN_BG = "Main.png";        //  MainMenu background (title built in)
    public static final String GAME_BG = "Background.png";  //  Game background (score/time box built in)
    public static final String GAME_FG = "Foreground.png";  //  Game foreground (counter, over the person)

    public static final String GREETINGS = "pharmacy_pickup_sentence_templates.txt";
    public static final String NAMES = "name_generator_folded_pairs.txt";
    //  Rich flashcard data: generic, brand, class, indications, side effects, warnings.
    public static final String DRUGS = "pharmacy_flashcards_detailed_corrected.csv";

    public static final String FONT = "fonts/Roboto-Black.ttf";

    /**
     * Customer figures in assets/People (rasterized from assets-src/People/*.svg at
     * build time). A random one is chosen per round.
     */
    public static final List<String> PERSON_KEYS = List.of(
            "Man_1", "Man_2", "Man_3", "Man_4", "Man_5", "Man_6",
            "Woman_1", "Woman_2", "Woman_3", "Woman_4", "Woman_5", "Woman_6",
            "Woman_Pregnant_1", "Woman_Pregnant_2");

    public static String personPath (String personKey)
    {
        return "People/" + personKey + ".png";
    }

    private final AssetManager manager = new AssetManager();
    private final TextureParameter smooth = new TextureParameter();

    public Assets ()
    {
        //  Most art is drawn well below its native size, so mipmap for clean downscaling.
        smooth.genMipMaps = true;
        smooth.minFilter = TextureFilter.MipMapLinearLinear;
        smooth.magFilter = TextureFilter.Linear;
    }

    /** Loads the loading-screen art synchronously (it has no loading screen itself). */
    public void loadBoot ()
    {
        manager.load(LOADING_BG, Texture.class, smooth);
        manager.finishLoading();
    }

    /** Queues everything else; drive it with {@link #update()}. */
    public void queueGame ()
    {
        manager.load(DRUG_CARD, Texture.class, smooth);
        manager.load(MAIN_BG, Texture.class, smooth);
        manager.load(GAME_BG, Texture.class, smooth);
        manager.load(GAME_FG, Texture.class, smooth);
        for (String key : PERSON_KEYS)
        {
            manager.load(personPath(key), Texture.class, smooth);
        }
    }

    /** Continues async loading; true once everything queued is loaded. */
    public boolean update ()
    {
        return manager.update();
    }

    public float progress ()
    {
        return manager.getProgress();
    }

    public Texture texture (String path)
    {
        return manager.get(path, Texture.class);
    }

    public String text (String path)
    {
        return Gdx.files.internal(path).readString("UTF-8");
    }

    @Override
    public void dispose ()
    {
        manager.dispose();
    }
}

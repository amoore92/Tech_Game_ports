package com.techgame.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntMap;

/**
 * Generates (and caches) one bitmap font per size/stroke combination on first use.
 *
 * <p>Glyphs are white with a black border so any text colour can be applied as a
 * tint while the stroke stays black (every stroke in the game is black).
 */
public final class Fonts implements Disposable
{
    /**
     * Glyphs are rasterized at this multiple of their world size and drawn scaled
     * down, so text stays sharp when the window is larger than the 720x1280 world
     * (the Phaser version rendered text at device pixel ratio, capped at 2x).
     */
    private static final int TEXT_RES = 2;
    private static final String CHARACTERS = FreeTypeFontGenerator.DEFAULT_CHARS + "—–…’‘“”•";

    private final FreeTypeFontGenerator generator;
    private final IntMap<BitmapFont> cache = new IntMap<>();

    public Fonts (String ttfPath)
    {
        generator = new FreeTypeFontGenerator(Gdx.files.internal(ttfPath));
    }

    public BitmapFont get (int size, int strokeThickness)
    {
        int key = size * 100 + strokeThickness;
        BitmapFont font = cache.get(key);
        if (font == null)
        {
            font = generate(size, strokeThickness);
            cache.put(key, font);
        }
        return font;
    }

    private BitmapFont generate (int size, int strokeThickness)
    {
        FreeTypeFontParameter p = new FreeTypeFontParameter();
        p.size = size * TEXT_RES;
        p.characters = CHARACTERS;
        p.color = Color.WHITE;
        //  Phaser centres the stroke on the glyph outline, so only half of it
        //  shows outside the glyph; FreeType's border grows purely outward.
        p.borderWidth = strokeThickness * TEXT_RES / 2f;
        p.borderColor = Color.BLACK;
        p.genMipMaps = true;
        p.minFilter = TextureFilter.MipMapLinearLinear;
        p.magFilter = TextureFilter.Linear;

        BitmapFont font = generator.generateFont(p);
        font.getData().setScale(1f / TEXT_RES);
        font.setUseIntegerPositions(false);
        return font;
    }

    @Override
    public void dispose ()
    {
        for (BitmapFont font : cache.values())
        {
            font.dispose();
        }
        cache.clear();
        generator.dispose();
    }
}

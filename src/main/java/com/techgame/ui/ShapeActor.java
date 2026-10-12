package com.techgame.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Align;

/**
 * A filled rectangle / rounded rectangle / circle with an optional stroke, drawn
 * with a {@link ShapeRenderer} (Phaser's shape objects and Graphics fills).
 *
 * <p>Like Phaser, the stroke is centred on the shape's edge. Not touchable unless
 * enabled explicitly.
 */
public class ShapeActor extends Actor
{
    private static final int CIRCLE_SEGMENTS = 64;

    private final ShapeRenderer shapes;
    private final Color fill = new Color();
    private final Color stroke = new Color();
    private float strokeWidth;
    private float cornerRadius;
    private boolean circle;

    public ShapeActor (ShapeRenderer shapes, float width, float height, Color fill)
    {
        this.shapes = shapes;
        this.fill.set(fill);
        setSize(width, height);
        setTouchable(Touchable.disabled);
    }

    public static ShapeActor circle (ShapeRenderer shapes, float radius, Color fill)
    {
        ShapeActor actor = new ShapeActor(shapes, radius * 2, radius * 2, fill);
        actor.circle = true;
        return actor;
    }

    public ShapeActor stroke (Color color, float width)
    {
        stroke.set(color);
        strokeWidth = width;
        return this;
    }

    public ShapeActor rounded (float radius)
    {
        cornerRadius = radius;
        return this;
    }

    /** Centres the shape on (x, y) in its parent's (y-up) coordinates. */
    public ShapeActor centerOn (float x, float y)
    {
        setPosition(x, y, Align.center);
        return this;
    }

    public void setFill (Color color)
    {
        fill.set(color);
    }

    public Color getFill ()
    {
        return fill;
    }

    @Override
    public Actor hit (float x, float y, boolean touchable)
    {
        if (!circle) return super.hit(x, y, touchable);
        if (touchable && getTouchable() != Touchable.enabled) return null;
        if (!isVisible()) return null;
        float r = getWidth() / 2;
        float dx = x - r;
        float dy = y - r;
        return dx * dx + dy * dy <= r * r ? this : null;
    }

    @Override
    public void draw (Batch batch, float parentAlpha)
    {
        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(batch.getProjectionMatrix());
        shapes.setTransformMatrix(batch.getTransformMatrix());
        shapes.begin(ShapeType.Filled);

        float alpha = getColor().a * parentAlpha;
        float x = getX();
        float y = getY();
        float w = getWidth();
        float h = getHeight();

        if (fill.a > 0)
        {
            shapes.setColor(fill.r, fill.g, fill.b, fill.a * alpha);
            if (circle)
            {
                shapes.circle(x + w / 2, y + h / 2, w / 2, CIRCLE_SEGMENTS);
            }
            else
            {
                fillRounded(x, y, w, h, cornerRadius);
            }
        }
        if (strokeWidth > 0)
        {
            shapes.setColor(stroke.r, stroke.g, stroke.b, stroke.a * alpha);
            if (circle)
            {
                ring(x + w / 2, y + h / 2, w / 2 - strokeWidth / 2, w / 2 + strokeWidth / 2, 0, 360);
            }
            else
            {
                strokeRounded(x, y, w, h, cornerRadius, strokeWidth);
            }
        }
        drawExtras(shapes, x, y, alpha);

        shapes.end();
        batch.begin();
    }

    /** Hook for subclasses to add more filled shapes in the same pass (local coords). */
    protected void drawExtras (ShapeRenderer shapes, float x, float y, float alpha)
    {
    }

    //  Rounded rect as three rects plus four quarter-circle corners (no overlaps,
    //  so translucent fills blend evenly).
    private void fillRounded (float x, float y, float w, float h, float r)
    {
        if (r <= 0)
        {
            shapes.rect(x, y, w, h);
            return;
        }
        int segments = cornerSegments(r);
        shapes.rect(x + r, y, w - 2 * r, h);
        shapes.rect(x, y + r, r, h - 2 * r);
        shapes.rect(x + w - r, y + r, r, h - 2 * r);
        shapes.arc(x + r, y + r, r, 180, 90, segments);
        shapes.arc(x + w - r, y + r, r, 270, 90, segments);
        shapes.arc(x + w - r, y + h - r, r, 0, 90, segments);
        shapes.arc(x + r, y + h - r, r, 90, 90, segments);
    }

    //  A band of width t centred on the outline.
    private void strokeRounded (float x, float y, float w, float h, float r, float t)
    {
        float ht = t / 2;
        if (r <= 0)
        {
            shapes.rect(x - ht, y - ht, w + t, t);
            shapes.rect(x - ht, y + h - ht, w + t, t);
            shapes.rect(x - ht, y + ht, t, h - t);
            shapes.rect(x + w - ht, y + ht, t, h - t);
            return;
        }
        shapes.rect(x + r, y - ht, w - 2 * r, t);
        shapes.rect(x + r, y + h - ht, w - 2 * r, t);
        shapes.rect(x - ht, y + r, t, h - 2 * r);
        shapes.rect(x + w - ht, y + r, t, h - 2 * r);
        float in = Math.max(0, r - ht);
        float out = r + ht;
        ring(x + r, y + r, in, out, 180, 90);
        ring(x + w - r, y + r, in, out, 270, 90);
        ring(x + w - r, y + h - r, in, out, 0, 90);
        ring(x + r, y + h - r, in, out, 90, 90);
    }

    private void ring (float cx, float cy, float rIn, float rOut, float startDeg, float degrees)
    {
        int segments = degrees >= 360 ? CIRCLE_SEGMENTS : cornerSegments(rOut);
        float step = degrees / segments * MathUtils.degreesToRadians;
        float a0 = startDeg * MathUtils.degreesToRadians;
        for (int i = 0; i < segments; i++)
        {
            float a1 = a0 + step;
            float c0 = MathUtils.cos(a0), s0 = MathUtils.sin(a0);
            float c1 = MathUtils.cos(a1), s1 = MathUtils.sin(a1);
            float ix0 = cx + c0 * rIn, iy0 = cy + s0 * rIn;
            float ix1 = cx + c1 * rIn, iy1 = cy + s1 * rIn;
            float ox0 = cx + c0 * rOut, oy0 = cy + s0 * rOut;
            float ox1 = cx + c1 * rOut, oy1 = cy + s1 * rOut;
            shapes.triangle(ix0, iy0, ox0, oy0, ox1, oy1);
            shapes.triangle(ix0, iy0, ox1, oy1, ix1, iy1);
            a0 = a1;
        }
    }

    private static int cornerSegments (float radius)
    {
        return Math.max(8, (int) radius);
    }
}

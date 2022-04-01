/*
The MIT License (MIT)

Copyright (c) 2014 Amulya Khare

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package com.amulyakhare.textdrawable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

/**
 * @author amulya
 * @datetime 14 Oct 2014, 3:53 PM
 * <p/>
 * MIT LICENSE: https://github.com/amulyakhare/TextDrawable/blob/master/LICENSE
 */
public class TextDrawable extends ShapeDrawable {

    private static final float SHADE_FACTOR = 0.9f;
    private final Paint textPaint;
    private final Paint borderPaint;
    private final String text;
    private final int color;
    private final RectShape shape;
    private final int height;
    private final int width;
    private final int fontSize;
    private final float radius;
    private final int borderThickness;

    private TextDrawable(Builder builder) {
        super(builder.shape);

        // shape properties
        shape = builder.shape;
        height = builder.height;
        width = builder.width;
        radius = builder.radius;

        // text and color
        text = builder.toUpperCase ? builder.text.toUpperCase() : builder.text;
        color = builder.color;

        // text paint settings
        fontSize = builder.fontSize;
        textPaint = new Paint();
        textPaint.setColor(builder.textColor);
        textPaint.setAntiAlias(true);
        textPaint.setFakeBoldText(builder.isBold);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTypeface(builder.font);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setStrokeWidth(builder.borderThickness);

        // border paint settings
        borderThickness = builder.borderThickness;
        borderPaint = new Paint();
        borderPaint.setColor(getDarkerShade(color));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderThickness);

        // drawable paint color
        Paint paint = getPaint();
        paint.setColor(color);
    }

    public static IShapeBuilder builder() {
        return new Builder();
    }

    private int getDarkerShade(@ColorInt int color) {
        return Color.rgb((int) (SHADE_FACTOR * Color.red(color)), (int) (SHADE_FACTOR * Color
                .green(color)), (int) (SHADE_FACTOR * Color.blue(color)));
    }

    private void drawBorder(Canvas canvas) {
        RectF rect = new RectF(getBounds());
        rect.inset(borderThickness / 2, borderThickness / 2);

        if (shape instanceof OvalShape) {
            canvas.drawOval(rect, borderPaint);
        } else if (shape instanceof RoundRectShape) {
            canvas.drawRoundRect(rect, radius, radius, borderPaint);
        } else {
            canvas.drawRect(rect, borderPaint);
        }
    }

    @Override
    public int getIntrinsicWidth() {
        return width;
    }

    @Override
    public int getIntrinsicHeight() {
        return height;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Rect r = getBounds();


        // draw border
        if (borderThickness > 0) {
            drawBorder(canvas);
        }

        int count = canvas.save();
        canvas.translate(r.left, r.top);

        // draw text
        int width = this.width < 0 ? r.width() : this.width;
        int height = this.height < 0 ? r.height() : this.height;
        int fontSize = this.fontSize < 0 ? (Math.min(width, height) / 2) : this.fontSize;
        textPaint.setTextSize(fontSize);
        canvas.drawText(text, width / 2, height / 2 - ((textPaint.descent() + textPaint.ascent())
                / 2), textPaint);

        canvas.restoreToCount(count);

    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        textPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        textPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public interface IConfigBuilder {
        IConfigBuilder width(int width);

        IConfigBuilder height(int height);

        IConfigBuilder textColor(int color);

        IConfigBuilder withBorder(int thickness);

        IConfigBuilder useFont(Typeface font);

        IConfigBuilder fontSize(int size);

        IConfigBuilder bold();

        IConfigBuilder toUpperCase();

        IShapeBuilder endConfig();
    }

    public interface IBuilder {

        TextDrawable build(@NonNull String text, @ColorInt int color);
    }

    public interface IShapeBuilder {

        IConfigBuilder beginConfig();

        IBuilder rect();

        IBuilder round();

        IBuilder roundRect(int radius);

        TextDrawable buildRect(@NonNull String text, @ColorInt int color);

        TextDrawable buildRoundRect(@NonNull String text, @ColorInt int color, int radius);

        TextDrawable buildRound(@NonNull String text, @ColorInt int color);
    }

    public static class Builder implements IConfigBuilder, IShapeBuilder, IBuilder {

        public int textColor;
        public float radius;
        private String text;
        private int color;
        private int borderThickness;
        private int width;
        private int height;
        private Typeface font;
        private RectShape shape;
        private int fontSize;
        private boolean isBold;
        private boolean toUpperCase;

        private Builder() {
            text = "";
            color = Color.GRAY;
            textColor = Color.WHITE;
            borderThickness = 0;
            width = -1;
            height = -1;
            shape = new RectShape();
            font = Typeface.create("sans-serif-light", Typeface.NORMAL);
            fontSize = -1;
            isBold = false;
            toUpperCase = false;
        }

        public IConfigBuilder width(int width) {
            this.width = width;
            return this;
        }

        public IConfigBuilder height(int height) {
            this.height = height;
            return this;
        }

        public IConfigBuilder textColor(int color) {
            this.textColor = color;
            return this;
        }

        public IConfigBuilder withBorder(int thickness) {
            this.borderThickness = thickness;
            return this;
        }

        public IConfigBuilder useFont(Typeface font) {
            this.font = font;
            return this;
        }

        public IConfigBuilder fontSize(int size) {
            this.fontSize = size;
            return this;
        }

        public IConfigBuilder bold() {
            this.isBold = true;
            return this;
        }

        public IConfigBuilder toUpperCase() {
            this.toUpperCase = true;
            return this;
        }

        @Override
        public IShapeBuilder endConfig() {
            return this;
        }

        @Override
        public IConfigBuilder beginConfig() {
            return this;
        }

        @Override
        public IBuilder rect() {
            this.shape = new RectShape();
            return this;
        }

        @Override
        public IBuilder round() {
            this.shape = new OvalShape();
            return this;
        }

        @Override
        public IBuilder roundRect(int radius) {
            this.radius = radius;
            float[] radii = {radius, radius, radius, radius, radius, radius, radius, radius};
            this.shape = new RoundRectShape(radii, null, null);
            return this;
        }

        @Override
        public TextDrawable buildRect(@NonNull String text, @ColorInt int color) {
            rect();
            return build(text, color);
        }

        @Override
        public TextDrawable buildRoundRect(@NonNull String text, @ColorInt int color, int radius) {
            roundRect(radius);
            return build(text, color);
        }

        @Override
        public TextDrawable buildRound(@NonNull String text, @ColorInt int color) {
            round();
            return build(text, color);
        }

        @Override
        public TextDrawable build(@NonNull String text, @ColorInt int color) {
            this.color = color;
            this.text = text;
            return new TextDrawable(this);
        }
    }
}

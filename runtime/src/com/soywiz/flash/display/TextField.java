package com.soywiz.flash.display;

import com.soywiz.flash.backend.EngineContext;
import com.soywiz.flash.backend.TextAlign;
import com.soywiz.flash.util.Color;
import com.soywiz.flash.util.Rectangle;

public class TextField extends DisplayObject {
    String text = "text";
    String fontFamily = "Arial";
    int width = 100;
    int height = 100;
    int size = 20;
    Color color = Color.red;
    TextAlign align = TextAlign.LEFT;

    @Override
    protected void renderInternal(EngineContext context) {
        context.drawText((float) x, (float) y, width, height, text, fontFamily, color, size, align);
    }

    @Override
    public Rectangle getLocalUntransformedBounds() {
        return new Rectangle(0, 0, width, height);
    }

}

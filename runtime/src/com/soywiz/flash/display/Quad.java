package com.soywiz.flash.display;

import com.soywiz.flash.backend.EngineContext;
import com.soywiz.flash.util.Color;
import com.soywiz.flash.util.Rectangle;

public class Quad extends DisplayObject {
    public int width = 100;
    public int height = 100;
    public Color color = Color.red;

    @Override
    protected void renderInternal(EngineContext context) {
        context.drawSolid(width, height, color);
    }

    @Override
    public Rectangle getLocalUntransformedBounds() {
        return new Rectangle(0, 0, width, height);
    }

}

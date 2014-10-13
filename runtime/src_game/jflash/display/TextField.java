package jflash.display;

import jflash.backend.EngineContext;
import jflash.backend.TextAlign;
import jflash.util.Color;
import jflash.util.Rectangle;

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

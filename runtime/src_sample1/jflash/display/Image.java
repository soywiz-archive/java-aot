package jflash.display;

import jflash.backend.EngineContext;
import jflash.backend.Texture;
import jflash.util.Rectangle;

public class Image extends DisplayObject {
    private Texture texture;

    @Override
    protected void renderInternal(EngineContext context) {
        context.drawImage(texture.width, texture.height, texture);
    }

    @Override
    public Rectangle getLocalUntransformedBounds() {
        return new Rectangle(0, 0, texture.width, texture.height);
    }

}

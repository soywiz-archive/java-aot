package jflash.backend.swing;

import jflash.backend.TextureBase;

import java.awt.image.BufferedImage;

public class SwingTextureBase implements TextureBase {
    final public BufferedImage image;

    public SwingTextureBase(BufferedImage image) {
        this.image = image;
    }

    @Override
    public int width() {
        return image.getWidth();
    }

    @Override
    public int height() {
        return image.getHeight();
    }
}
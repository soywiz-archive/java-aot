package jflash.backend;

public class Texture {
    public final TextureBase base;
    public final int x;
    public final int y;
    public final int width;
    public final int height;

    public Texture(TextureBase base) {
        this(base, 0, 0, base.width(), base.height());
    }

    public Texture(TextureBase base, int x, int y, int width, int height) {
        this.base = base;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    Texture slice(int x, int y, int width, int height) {
        // @TODO: Check limits
        return new Texture(base, this.x + x, this.y + y, width, height);
    }

    @Override
    public String toString() {
        return "Texture((" + x + "," + y + ")-(" + width + "," + height + "))";
    }
}

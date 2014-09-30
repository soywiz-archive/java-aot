package jflash.util;

public class Size {
    public float width;
    public float height;

    public Size() {
    }

    public Size(float width, float height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public String toString() {
        return "Size(" + width + ", " + height + ")";
    }
}

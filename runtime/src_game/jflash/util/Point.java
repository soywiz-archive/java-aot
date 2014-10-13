package jflash.util;

public class Point {
    public float x;
    public float y;

    public Point() {
        this(0, 0);
    }

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Point setXY(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    @Override
    public String toString() {
        return "Point(" + x + ", " + y + ")";
    }
}

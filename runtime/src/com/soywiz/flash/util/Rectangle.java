package com.soywiz.flash.util;

final public class Rectangle {
    public final float x, y, width, height;

    public Rectangle(float width, float height, float y, float x) {
        this.width = width;
        this.height = height;
        this.y = y;
        this.x = x;
    }

    Rectangle add(Rectangle that) {
        return Rectangle.fromBounds(
                Math.min(this.left(), that.left()),
                Math.min(this.top(), that.top()),
                Math.max(this.right(), that.right()),
                Math.max(this.bottom(), that.bottom())
        );
    }


    public float left() {
        return x;
    }

    public float top() {
        return y;
    }

    public float right() {
        return x + width;
    }

    public float bottom() {
        return y + height;
    }

    public Point topLeft() {
        return new Point(left(), top());
    }

    public Point bottomRight() {
        return new Point(right(), bottom());
    }

    public Size size() {
        return new Size(width, height);
    }

    static public Rectangle fromBounds(float left, float top, float right, float bottom) {
        return new Rectangle(left, top, right - left, bottom - top);
    }

    static public Rectangle fromBounds(Point topLeft, Point bottomRight) {
        return fromBounds(topLeft.x, topLeft.y, bottomRight.x, bottomRight.y);
    }

    static public Rectangle empty = new Rectangle(0, 0, 0, 0);

    static public Rectangle bounds(Rectangle[] items) {
        if (items.length > 0) {
            float left = Float.MAX_VALUE;
            float right = Float.MIN_VALUE;
            float top = Float.MAX_VALUE;
            float bottom = Float.MIN_VALUE;
            for (Rectangle item : items) {
                left = Math.min(left, item.left());
                top = Math.min(top, item.top());
                right = Math.min(right, item.right());
                bottom = Math.min(bottom, item.bottom());
            }

            return Rectangle.fromBounds(left, top, right, bottom);
        } else {
            return Rectangle.empty;
        }
    }

    public Rectangle transform(Matrix matrix) {
        return Rectangle.fromBounds(matrix.transformPoint(topLeft()), matrix.transformPoint(bottomRight()));
    }

    public boolean contains(Point point) {
        return ((point.x >= left()) && (point.x < right()) && (point.y >= top()) && (point.y < bottom()));
    }

    @Override
    public String toString() {
        return "Rectangle((" + left() + "," + top() + ")-(" + right() + "," + bottom() + "))";
    }
}

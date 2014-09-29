package com.soywiz.flash.util;

public class Color {
    public final int r, g, b, a;

    public Color(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public int toInt() {
        return (a << 24) | (r << 16) | (b << 8) | (g << 0);
    }

    static public Color black = new Color(0, 0, 0, 255);
    static public Color red = new Color(255, 0, 0, 255);

}

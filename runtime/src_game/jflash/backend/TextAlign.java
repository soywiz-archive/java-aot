package jflash.backend;

public enum TextAlign {
    LEFT(0.0), CENTER(0.5), RIGHT(1.0);

    public final double ratio;

    TextAlign(double ratio) {
        this.ratio = ratio;
    }
}

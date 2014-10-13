package jflash.backend;

import jflash.util.Matrix;

public class State {
    public Matrix matrix = new Matrix();
    public float alpha = 1.0f;

    public void copyFrom(State that) {
        this.matrix.copyFrom(that.matrix);
        this.alpha = that.alpha;
    }
}

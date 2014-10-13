package jflash.util;

public class MatrixTransform {
    public float x = 0, y = 0, scaleX = 1, scaleY = 1, rotation = 0, skewX = 0, skewY = 0;

    public MatrixTransform(float x, float y, float scaleX, float scaleY, float rotation, float skewX, float skewY) {
        this.x = x;
        this.y = y;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.rotation = rotation;
        this.skewX = skewX;
        this.skewY = skewY;
    }

    static MatrixTransform fromMatrix(Matrix matrix) {
        float M_PI_4 = (float) (Math.PI / 4.0);
        float rotation = 0;
        float skewX = 0;
        float skewY = 0;

        float x = matrix.tx;
        float y = matrix.ty;

        skewX = (float) Math.atan(-matrix.c / matrix.d);
        skewY = (float) Math.atan(matrix.b / matrix.a);

        if (Float.isNaN(skewX)) skewX = 0.0f;
        if (Float.isNaN(skewY)) skewY = 0.0f;

        double scaleY = (skewX > -M_PI_4 && skewX < M_PI_4) ? matrix.d / Math.cos(skewX) : -matrix.c / Math.sin(skewX);
        double scaleX = (skewY > -M_PI_4 && skewY < M_PI_4) ? matrix.a / Math.cos(skewY) : matrix.b / Math.sin(skewY);

        if (Math.abs(skewX - skewY) < 0.0001) {
            rotation = skewX;
            skewX = 0;
            skewY = 0;
        } else {
            rotation = 0;
        }

        return new MatrixTransform(x, y, (float) scaleX, (float) scaleY, rotation, skewX, skewY);
    }

}

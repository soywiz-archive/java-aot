package jflash.util;

public class Matrix {
    public float a = 1, b = 0, c = 0, d = 1, tx = 0, ty = 0;

    public Matrix() {
    }

    public Matrix(float a, float b, float c, float d, float tx, float ty) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.tx = tx;
        this.ty = ty;
    }

    public void concat(Matrix m) {
        float a1 = a * m.a + b * m.c;
        b = a * m.b + b * m.d;
        a = a1;

        float c1 = c * m.a + d * m.c;
        d = c * m.b + d * m.d;
        c = c1;

        float tx1 = tx * m.a + ty * m.c + m.tx;
        ty = tx * m.b + ty * m.d + m.ty;
        tx = tx1;
    }

    public void preconcat(Matrix m) {
        Matrix temp = new Matrix();
        temp.copyFrom(m);
        temp.concat(this);
        this.copyFrom(temp);
    }

    public void copyFrom(Matrix sourceMatrix) {
        a = sourceMatrix.a;
        b = sourceMatrix.b;
        c = sourceMatrix.c;
        d = sourceMatrix.d;
        tx = sourceMatrix.tx;
        ty = sourceMatrix.ty;
    }

    public Matrix setTo(float a, float b, float c, float d, float tx, float ty) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.tx = tx;
        this.ty = ty;
        return this;
    }

    public void identity() {
        a = 1;
        b = 0;
        c = 0;
        d = 1;
        tx = 0;
        ty = 0;
    }

    public Matrix invert() {
        float norm = a * d - b * c;

        if (norm == 0) {
            a = 0;
            b = 0;
            c = 0;
            d = 0;
            tx = -tx;
            ty = -ty;
        } else {
            norm = 1.0f / norm;
            float a1 = d * norm;
            d = a * norm;
            a = a1;
            b *= -norm;
            c *= -norm;

            float tx1 = -a * tx - c * ty;
            ty = -b * tx - d * ty;
            tx = tx1;
        }

        return this;
    }

    public Matrix mult(Matrix m) {
        return setTo(
                a * m.a + b * m.c,
                a * m.b + b * m.d,
                c * m.a + d * m.c,
                c * m.b + d * m.d,
                tx * m.a + ty * m.c + m.tx,
                tx * m.b + ty * m.d + m.ty
        );
    }

    public void rotate(float theta) {
        float cos = (float) Math.cos(theta);
        float sin = (float) Math.sin(theta);

        float a1 = a * cos - b * sin;
        b = a * sin + b * cos;
        a = a1;

        float c1 = c * cos - d * sin;
        d = c * sin + d * cos;
        c = c1;

        float tx1 = tx * cos - ty * sin;
        ty = tx * sin + ty * cos;
        tx = tx1;
    }

    public void scale(float sx, float sy) {
        a *= sx;
        b *= sy;
        c *= sx;
        d *= sy;
        tx *= sx;
        ty *= sy;
    }

    static private Matrix translateM = new Matrix();

    public void translate(float dx, float dy) {
        translateM.tx = dx;
        translateM.ty = dy;
        this.concat(translateM);
    }

    public Point transformPoint(Point pos) {
        return transformPoint(pos, new Point());
    }

    public Point transformPoint(Point pos, Point out) {
        return out.setXY(pos.x * a + pos.y * c + tx, pos.x * b + pos.y * d + ty);
    }
}

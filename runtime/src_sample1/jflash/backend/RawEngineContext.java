package jflash.backend;

import jflash.util.Color;
import jflash.util.Matrix;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class RawEngineContext extends EngineContext {
    final protected States states = new States();

    @Override
    public void clear(Color color) {

    }

    @Override
    public void drawSolid(int width, int height, Color color) {

    }

    @Override
    public void drawImage(int width, int height, Texture texture) {

    }

    @Override
    public InputStream openFile(String path) throws FileNotFoundException {
        return null;
    }

    @Override
    public Texture createImageFromBytes(byte[] data) {
        return null;
    }

    @Override
    public void drawText(float x, float y, float width, float height, String text, String fontFamily, Color color, int size, TextAlign align) {

    }

    @Override
    public void save() {
        states.save();
    }

    @Override
    public void restore() {
        states.restore();
    }

    @Override
    public void translate(float x, float y) {
        states.get().matrix.translate(x, y);
    }

    @Override
    public void scale(float sx, float sy) {
        states.get().matrix.scale(sx, sy);
    }

    @Override
    public void rotate(float angle) {
        states.get().matrix.rotate(angle);
    }

    @Override
    public void alpha(float alpha) {
        states.get().alpha *= alpha;
    }

    @Override
    public void loop(Component root) {

    }

}


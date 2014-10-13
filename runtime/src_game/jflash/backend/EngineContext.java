package jflash.backend;

import jflash.util.Color;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

abstract public class EngineContext {
    static public EngineContext instance = null;

    abstract public void clear(Color color);

    abstract public void present();

    abstract public void drawSolid(int width, int height, Color color);

    abstract public void drawImage(int width, int height, Texture texture);

    abstract public InputStream openFile(String path) throws FileNotFoundException;

    abstract public Texture createImageFromBytes(byte[] data);

    abstract public void drawText(float x, float y, float width, float height, String text, String fontFamily, Color color, int size, TextAlign align);

    abstract public void save();

    abstract public void restore();

    abstract public void translate(float x, float y);

    abstract public void scale(float sx, float sy);

    abstract public void rotate(float angle);

    abstract public void alpha(float alpha);

    abstract public void loop(Component root);

    final public void keep(Runnable callback) {
        this.save();
        try {
            callback.run();
        } finally {
            this.restore();
        }
    }

    public byte[] readFile(String path) throws IOException {
	    InputStream is = openFile(path);
	    try {
		    byte[] data = new byte[is.available()];
		    is.read(data);
		    return data;
	    } finally {
		    is.close();
	    }
    }
}

package jflash.backend;

abstract public class SimpleEngineContext {
    abstract public void clear();
    abstract public void present();
    abstract public void drawTriangles(int[] indices, double[] data);

    abstract public void loop(Component root);
}

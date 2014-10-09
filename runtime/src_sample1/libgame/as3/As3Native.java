package libgame.as3;

public class As3Native {
    native static public void onEnterFrame(Runnable callback);
    native static public void clear(int color);
    native static public void present();
    native static public void drawTriangles(int color, float[] vertices);
}

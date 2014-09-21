package libgame;

import libcore.CPP;
import libcore.CPPMethod;

public class SDL {
    static private boolean once = false;
    static private void initOnce() {
        if (!once) {
            once = true;
            SDLApi.init();
        }
    }

    static public SDLWindow createWindow(String title, int width, int height) {
        initOnce();
        return new SDLWindow(SDLApi.createWindow(title, width, height));
    }

    static public void delay(int ms) {
        SDLApi.delay(ms);
    }
}

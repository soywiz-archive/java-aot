package libgame;

import jflash.backend.Component;
import jflash.backend.RawEngineContext;
import jflash.util.Color;

public class SDLEngineContext extends RawEngineContext {
    @Override
    public void drawSolid(int width, int height, Color color) {
        System.out.println("drawSolid(" + width + ", " + height + ")");
        super.drawSolid(width, height, color);
    }

    @Override
    public void loop(Component root) {
        SDL.init();
        SDLWindow win = SDL.createWindow("Hello SDL from java-aot!", 640, 480);
        SDLRenderer renderer = win.createRenderer();
        boolean running = true;
        while (true) {
            SDLEvent event;
            while ((event = SDL.pollEvent()) != null) {
                if (event.getType() == SDLEventType.SDL_QUIT) {
                    running = false;
                    break;
                }
                //System.out.println("" + event.getCode());
            }

            if (!running) break;

            GL.clearColor(1f, 1f, 0f, 1f);
            GL.clear();

            root.update(20);
            root.render(this);

            win.swap();
            SDL.delay(20);
        }
        renderer.dispose();
        win.dispose();
    }
}

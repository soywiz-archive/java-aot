package libgame;

import jflash.backend.Component;
import jflash.backend.RawEngineContext;
import jflash.util.Color;

public class SDLEngineContext extends RawEngineContext {
    static float[] vertices = {
        0.0f, 0.5f, 0.0f,
        -0.5f, -0.5f, 0.0f,
        0.5f, -0.5f, 0.0f
    };

    @Override
    public void drawSolid(int width, int height, Color color) {
        System.out.println("drawSolid(" + width + ", " + height + ")");
        //GL.drawSimple();
        GL.drawTestTriangles(vertices);
        super.drawSolid(width, height, color);
    }

    int program;

    @Override
    public void loop(Component root) {
        SDL.init();

        SDLWindow win = SDL.createWindow("Hello SDL from java-aot!", 640, 480);
        SDLRenderer renderer = win.createRenderer();

        GL.initTest();
        /*
        program = GL.createProgram(
                "attribute vec4 vPosition; void main() { gl_Position = vPosition; }",
                "precision mediump float; void main() { gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0); }"
        );
        GL.useProgram(program);
        */

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

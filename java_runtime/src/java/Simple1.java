package java;

import libcore.Native;
import libgame.*;

public class Simple1 {
	static public int sum(int[] args) {
		int sum = 0;
		for (int arg : args) sum += arg;
		return sum;
	}

	static public int main(String[] args) {
		int[] list = new int[10];

		list[0] = 1;
		list[2] = 20;

		System.arraycopy(list, 0, list, 4, 4);

		int len = sum(list);

		Native.putchar(':');

		System.out.println("" + System.currentTimeMillis());
		System.out.println("" + System.currentTimeMillis());

		for (int n = 0; n < len; n++) {
			if ((n % 2) == 0) {
				System.out.println(("Hello world!" + n).toUpperCase());
				//System.out.println("Hello world!" + n);
			}
		}

		System.out.println("" + System.currentTimeMillis());

		//for (int n = 0; n < 10; n++) {
		//	for (int m = 0; m < 10; m++) {
		//		System.out.println("" + n + "x" + m + "=" + (n * m));
		//	}
		//}

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
                System.out.println("" + event.getCode());
            }

            if (!running) break;

            GL.clearColor(1f, 1f, 0f, 1f);
            GL.clear();
            win.swap();
            SDL.delay(20);
        }
        win.dispose();

		return 0;
	}
}

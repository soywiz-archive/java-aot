package libgame;

import libcore.CPP;
import libcore.CPPMethod;

@CPP(
    framework = "SDL2",
    header = "#include <SDL.h>"
)
class SDLApi {
    @CPPMethod("void libgame_SDLApi::init() { SDL_Init(SDL_INIT_EVERYTHING); }")
    static public native void init();

    @CPPMethod(
            "int64 libgame_SDLApi::createWindow(java_lang_String *title, int32 width, int32 height) {" +
                    "   char *title_cptr = JavaString_to_cstr_byte(title);" +
                    "   SDL_Window *win = SDL_CreateWindow(title_cptr, SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED, width, height, SDL_WINDOW_OPENGL);" +
                    "   delete title_cptr;" +
                    "   return (int64)(void *)win;" +
                    "   " +
                    "}")
    static public native long createWindow(String title, int width, int height);

    @CPPMethod("void libgame_SDLApi::destroyWindow(int64 id) { SDL_DestroyWindow((SDL_Window *)(void*)id); }")
    static public native void destroyWindow(long id);


    @CPPMethod("void libgame_SDLApi::delay(int32 ms) { SDL_Delay(ms); }")
    static public native void delay(int ms);



}

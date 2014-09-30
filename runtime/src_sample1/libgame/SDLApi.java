package libgame;

import libcore.CPPClass;
import libcore.CPPMethod;

@CPPClass(
    library = "SDL2",
    //cflags = "-D_THREAD_SAFE -lm -liconv -Wl,-framework,OpenGL -Wl,-framework,ForceFeedback -lobjc -Wl,-framework,Cocoa -Wl,-framework,Carbon -Wl,-framework,IOKit -Wl,-framework,CoreAudio -Wl,-framework,AudioToolbox -Wl,-framework,AudioUnit",
	//  -lopengl32 -lshell32 -luser32 -lgdi32 -lwinmm -limm32 -lole32 -lkernel32 -lversion -lOleAut32 -lstdc++
	cflags = "-D_THREAD_SAFE",
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

    @CPPMethod("void libgame_SDLApi::destroyWindow(int64 window) { SDL_DestroyWindow((SDL_Window *)(void*)window); }")
    static public native void destroyWindow(long window);

    @CPPMethod("int64 libgame_SDLApi::createRenderer(int64 window) { return (int64)(void*)SDL_CreateRenderer((SDL_Window *)(void*)window, -1, SDL_RENDERER_ACCELERATED); }")
    static public native long createRenderer(long window);

    @CPPMethod("void libgame_SDLApi::destroyRenderer(int64 renderer) { SDL_DestroyRenderer((SDL_Renderer *)(void*)renderer); }")
    static public native void destroyRenderer(long renderer);

    @CPPMethod("void libgame_SDLApi::delay(int32 ms) { SDL_Delay(ms); }")
    static public native void delay(int ms);

    @CPPMethod("void libgame_SDLApi::swapWindow(int64 window) { SDL_GL_SwapWindow((SDL_Window *)(void*)window); }")
    static public native void swapWindow(long id);

    @CPPMethod("SDL_Event test_event; int32 libgame_SDLApi::eventPoll() {return SDL_PollEvent(&test_event); }")
    static public native int eventPoll();

    @CPPMethod("int32 libgame_SDLApi::eventGetType() { return test_event.type; }") static public native int eventGetType();
    @CPPMethod("int32 libgame_SDLApi::eventGetCode() { return test_event.user.code; }") static public native int eventGetCode();
    @CPPMethod("int64 libgame_SDLApi::eventGetData1() { return (int64)(void*)test_event.user.data1; }") static public native long eventGetData1();
    @CPPMethod("int64 libgame_SDLApi::eventGetData2() { return (int64)(void*)test_event.user.data2; }") static public native long eventGetData2();


}

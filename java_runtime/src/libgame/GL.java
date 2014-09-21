package libgame;

import libcore.CPP;
import libcore.CPPMethod;

@CPP(
        framework = "OpenGL",
        header = "#include <gl.h>"
)
public class GL {
    @CPPMethod("void libgame_GL::clear() { ::glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); }")
    static public native void clear();

    @CPPMethod("void libgame_GL::clearColor(float32 r, float32 g, float32 b, float32 a) { ::glClearColor(r, g, b, a); }")
    static public native void clearColor(float r, float g, float b, float a);
}

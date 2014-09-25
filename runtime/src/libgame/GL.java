package libgame;

import libcore.CPPClass;
import libcore.CPPMethod;

@CPPClass(
        framework = "OpenGL",
        header = "\n" +
	        "#ifdef _WIN32\n" +
	        "#  include <GL/gl.h>\n" +
	        "#else\n" +
	        "#  include <gl.h>\n" +
	        "#endif\n"
)
public class GL {
    @CPPMethod("void libgame_GL::clear() { ::glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); }")
    static public native void clear();

    @CPPMethod("void libgame_GL::clearColor(float32 r, float32 g, float32 b, float32 a) { ::glClearColor(r, g, b, a); }")
    static public native void clearColor(float r, float g, float b, float a);
}

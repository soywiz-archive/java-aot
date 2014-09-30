package libgame;

import libcore.CPPClass;
import libcore.CPPMethod;

@CPPClass(
    framework = "OpenGL"
)
public class GL {
    static public native void clear();
    static public native void clearColor(float r, float g, float b, float a);

    /*
    @CPPMethod("void libgame_GL::drawSimple() {   //Depth test and culling.\n" +
            "    glEnable(GL_DEPTH_TEST);\n" +
            "    glDisable(GL_CULL_FACE);\n" +
            "\n" +
            "    //Setting the projection matrix.\n" +
            "\tglMatrixMode( GL_PROJECTION );\n" +
            "\tglLoadIdentity();\n" +
            "\tgluPerspective(60.0f, 800.0f / 600.0f, 0.1f, 40000.0f);\n" +
            "\n" +
            "\t//Setting up the view matrix.\n" +
            "\tglMatrixMode( GL_MODELVIEW );\n" +
            "\tglLoadIdentity();\n" +
            "    float camera_x = 1.0f;\n" +
            "    float camera_y = 1.0f;\n" +
            "    float camera_z = 1.0f;    \n" +
            "    float up_x = 0.0f;\n" +
            "    float up_y = 0.0f;\n" +
            "    float up_z = 0.1f;\n" +
            "    float lookat_x = 0.0f;\n" +
            "    float lookat_y = 0.0f;\n" +
            "    float lookat_z = 0.0f;\n" +
            "\tgluLookAt(camera_x, camera_y, camera_z,\n" +
            "\t\t\t  lookat_x, lookat_y, lookat_z,\n" +
            "\t\t\t  up_x,     up_y,     up_z);\n     glBegin(GL_TRIANGLES);\n" +
            "    glColor3f(1.0f, 0.0f, 0.0f);\n" +
            "    glVertex3f(0.0f, 0.0f, 0.0f);\n" +
            "    \n" +
            "    glColor3f(0.0f, 1.0f, 0.0f);\n" +
            "    glVertex3f(0.3f, 0.0f, 0.0f);\n" +
            "    \n" +
            "    glColor3f(0.0f, 0.0f, 1.0f);\n" +
            "    glVertex3f(0.0f, 0.3f, 0.0f);\n" +
            "    glEnd();\n}")
    static public native void drawSimple();
    */
    @CPPMethod("#include \"gl.cpp\"")
    static public native int createProgram(String vs, String fs);

    static public native int useProgram(int program);

    static public native void initTest();
    static public native void drawTestTriangle();
}

#ifdef _WIN32
    #include <GL/gl.h>
    #include <GL/glu.h>
#else
    #include <gl.h>
    #include <glu.h>
#endif

void libgame_GL::clear() {
    ::glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
}

void libgame_GL::clearColor(float32 r, float32 g, float32 b, float32 a) {
    ::glClearColor(r, g, b, a);
}

int myCreateShader(char* shaderSrc, int type) {
    GLint compiled;
    GLint shader = glCreateShader(type);
    glShaderSource(shader, 1, &shaderSrc, NULL);
    glCompileShader(shader);
    glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
    if (!compiled) {
        GLint infoLen = 0;
        glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLen);
        if(infoLen > 1) {
            char* infoLog = (char*)malloc(sizeof(char) * infoLen);
            glGetShaderInfoLog(shader, infoLen, NULL, infoLog);
            fprintf(stderr, "Error compiling shader:\n%s\n", infoLog);
            free(infoLog);
        }
        glDeleteShader(shader);
        return 0;
    }

    return shader;
}

int32 libgame_GL::createProgram(java_lang_String *vs, java_lang_String *fs) {
    char* vs_str = JavaString_to_cstr_byte(vs);
    char* fs_str = JavaString_to_cstr_byte(fs);
    int vertexShader = myCreateShader(vs_str, GL_VERTEX_SHADER);
    int fragmentShader = myCreateShader(fs_str, GL_FRAGMENT_SHADER);
    GLuint programObject = glCreateProgram();
    glAttachShader(programObject, vertexShader);
    glAttachShader(programObject, fragmentShader);
    glLinkProgram(programObject);

    GLint linked;
    glGetProgramiv(programObject, GL_LINK_STATUS, &linked);
    if(!linked) {
        GLint infoLen = 0;
        glGetProgramiv(programObject, GL_INFO_LOG_LENGTH, &infoLen);
        if(infoLen > 1) {
            char* infoLog = (char*)malloc(sizeof(char) * infoLen);
            glGetProgramInfoLog(programObject, infoLen, NULL, infoLog);
            fprintf(stderr, "Error linking program:\n%s\n", infoLog);
            free(infoLog);
        }
        glDeleteProgram(programObject);
        return 0;
    }
    return programObject;
}

int32 libgame_GL::useProgram(int program) {
    glUseProgram(program);
    return 0;
}

GLfloat vVertices[] = {0.0f, 0.5f, 0.0f, -0.5f, -0.5f, 0.0f, 0.5f, -0.5f, 0.0f};

void libgame_GL::initTest() {
    glDisable(GL_DEPTH_TEST);
    glDisable(GL_CULL_FACE);
    glMatrixMode( GL_PROJECTION );
    glLoadIdentity();
    gluPerspective(60.0f, 800.0f / 600.0f, 0.1f, 40000.0f);
    glMatrixMode( GL_MODELVIEW );
    glLoadIdentity();

    float camera_x = 1.0f;
    float camera_y = 1.0f;
    float camera_z = 1.0f;
    float up_x = 0.0f;
    float up_y = 0.0f;
    float up_z = 0.1f;
    float lookat_x = 0.0f;
    float lookat_y = 0.0f;
    float lookat_z = 0.0f;
    gluLookAt(camera_x, camera_y, camera_z, lookat_x, lookat_y, lookat_z, up_x,     up_y,     up_z);
}

void libgame_GL::drawTestTriangle() {
    glBegin(GL_TRIANGLES);
        glColor3f(1.0f, 0.0f, 0.0f);
        glVertex3f(0.0f, 0.0f, 0.0f);

        glColor3f(0.0f, 1.0f, 0.0f);
        glVertex3f(0.3f, 0.0f, 0.0f);

        glColor3f(0.0f, 0.0f, 1.0f);
        glVertex3f(0.0f, 0.3f, 0.0f);
    glEnd();

    /*
    glBegin( GL_QUADS );
        glVertex2f( -0.5f, -0.5f );
        glVertex2f( 0.5f, -0.5f );
        glVertex2f( 0.5f, 0.5f );
        glVertex2f( -0.5f, 0.5f );
    glEnd();
    */
    /*
    glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 0, vVertices);
    glEnableVertexAttribArray(0);
    glDrawArrays(GL_TRIANGLES, 0, 3);
    */
}